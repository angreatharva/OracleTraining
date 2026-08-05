/**
 * Investor dashboard - the reference pattern for a data-loading screen.
 *
 * Copy this shape for new screens: a ScreenState for the loading/error plumbing, load in
 * `connected()`, and let ScreenState.run turn any service failure into a displayable
 * message. `run` never rejects, so a falsy result means "it failed and the message is
 * already on state.errorMessage".
 *
 * One call does the work here: the backend already aggregates holdings, completed trades and
 * live Product prices into positions.
 */
define([
  "knockout",
  "ojs/ojarraydataprovider",
  "services/TradingService",
  "services/SessionStore",
  "utils/ScreenState",
  "utils/derived",
  "utils/format"
], function (ko, ArrayDataProvider, TradingService, SessionStore, ScreenState, derived, format) {
  "use strict";

  function DashboardViewModel() {
    var self = this;

    self.state = ScreenState.create();
    self.format = format;

    self.positions = ko.observableArray([]);
    self.dataProvider = new ArrayDataProvider(self.positions, { keyAttributes: "productId" });

    self.totalInvested = ko.pureComputed(function () {
      return self.positions().reduce(function (sum, p) {
        return sum + (Number(p.investedValue) || 0);
      }, 0);
    });
    self.totalValue = ko.pureComputed(function () {
      return self.positions().reduce(function (sum, p) {
        return sum + (Number(p.currentValuation) || 0);
      }, 0);
    });
    self.totalProfitLoss = ko.pureComputed(function () {
      return self.totalValue() - self.totalInvested();
    });
    self.profitLossClass = ko.pureComputed(function () {
      return format.profitLossClass(self.totalProfitLoss());
    });

    // ---------------------------------------------------------------------
    // Presentation-only derivations over `self.positions` - nothing below adds a
    // data flow, a fetch or a piece of state; it is the same array the table already
    // renders, reshaped for the dataviz components.
    //
    // The chart rows use derived.array rather than pureComputed because
    // ArrayDataProvider rejects a computed outright: see utils/derived.
    // ---------------------------------------------------------------------

    /** Return on cost, as a percentage. Drives the meter under the P/L tile. */
    self.returnPercent = ko.pureComputed(function () {
      var invested = self.totalInvested();
      return invested > 0 ? (self.totalProfitLoss() / invested) * 100 : 0;
    });

    /**
     * The meter is a fixed -25%..+25% window so the bar has a stable scale to move
     * against; a real return outside that range pins to the end rather than
     * rescaling the axis under the user.
     */
    self.returnMeterValue = ko.pureComputed(function () {
      return Math.max(-25, Math.min(25, self.returnPercent()));
    });

    self.returnMeterThresholds = [
      { max: -0.0001, color: "#c74634" },
      { max: 0.0001, color: "#8c8c8c" },
      { color: "#4c8c3f" }
    ];

    /**
     * Flat rows for `oj-chart`'s DataProvider form: one row per (series, group)
     * pair. Invested vs current value, side by side per product.
     */
    self.investedVsValueRows = derived.array(function () {
      var rows = [];
      self.positions().forEach(function (p) {
        var name = p.productName;
        rows.push({
          id: "inv-" + p.productId,
          series: "Invested",
          group: name,
          value: Number(p.investedValue) || 0
        });
        rows.push({
          id: "val-" + p.productId,
          series: "Current value",
          group: name,
          value: Number(p.currentValuation) || 0
        });
      });
      return rows;
    });

    self.investedVsValueDP = new ArrayDataProvider(self.investedVsValueRows, {
      keyAttributes: "id"
    });

    /** One slice per product, sized by current valuation. */
    self.allocationRows = derived.array(function () {
      return self.positions()
        .filter(function (p) { return (Number(p.currentValuation) || 0) > 0; })
        .map(function (p) {
          return {
            id: "alloc-" + p.productId,
            series: p.productName,
            value: Number(p.currentValuation) || 0
          };
        });
    });

    self.allocationDP = new ArrayDataProvider(self.allocationRows, {
      keyAttributes: "id"
    });

    /** Per-product profit/loss, coloured by sign - a diverging bar. */
    self.profitLossRows = derived.array(function () {
      return self.positions().map(function (p) {
        var pl = Number(p.profitLoss) || 0;
        return {
          id: "pl-" + p.productId,
          group: p.productName,
          value: pl,
          color: pl < 0 ? "#c74634" : "#4c8c3f"
        };
      });
    });

    self.profitLossDP = new ArrayDataProvider(self.profitLossRows, {
      keyAttributes: "id"
    });

    /** A chart of one bar is noise, so the charts appear only from two positions up. */
    self.hasChartableData = ko.pureComputed(function () {
      return self.positions().length > 1;
    });

    self.hasPositions = ko.pureComputed(function () {
      return !self.state.isLoading() && self.positions().length > 0;
    });
    self.isEmpty = ko.pureComputed(function () {
      return !self.state.isLoading() && !self.state.errorMessage() && self.positions().length === 0;
    });

    self.load = function () {
      var userId = SessionStore.userId();
      if (!userId) { return; }

      // 404 means no portfolio account yet, which is a normal state for a new investor.
      self.state.runAllowingNotFound(function () {
        return TradingService.getInvestmentOverview(userId);
      }).then(function (overview) {
        var loaded = overview ? (overview.positions || []) : [];

        // Portfolio weight is presentation only, but it needs the total across all
        // rows - so it is derived here, alongside the other display fields, rather
        // than per row in the view.
        var valuationTotal = loaded.reduce(function (sum, p) {
          return sum + (Number(p.currentValuation) || 0);
        }, 0);

        self.positions(loaded.map(function (position) {
          return Object.assign({}, position, {
            weightPercent: valuationTotal > 0
              ? ((Number(position.currentValuation) || 0) / valuationTotal) * 100
              : 0,
            currentQuantityDisplay: format.quantity(position.currentQuantity),
            averageBuyPriceDisplay: format.money(position.averageBuyPrice),
            currentPriceDisplay: format.money(position.currentPrice),
            investedValueDisplay: format.money(position.investedValue),
            currentValuationDisplay: format.money(position.currentValuation),
            profitLossDisplay: format.signedMoney(position.profitLoss)
          });
        }));
      });
    };

    self.connected = function () {
      self.load();
    };
  }

  return DashboardViewModel;
});
