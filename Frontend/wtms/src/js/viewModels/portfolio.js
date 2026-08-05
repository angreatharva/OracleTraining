/**
 * Portfolio: the account plus its holdings.
 *
 * Uses PortfolioService.getSummary, which returns the account, the holdings and the totals
 * in one call. Those totals come from each holding's stored `marketValue` and are not
 * refreshed against Product Service - so they can legitimately differ from the Dashboard,
 * which re-reads live prices. The view says so rather than leaving the discrepancy
 * looking like a bug.
 */
define([
  "knockout",
  "ojs/ojarraydataprovider",
  "services/PortfolioService",
  "services/ProductService",
  "services/SessionStore",
  "utils/ScreenState",
  "utils/derived",
  "utils/format"
], function (ko, ArrayDataProvider, PortfolioService, ProductService, SessionStore,
             ScreenState, derived, format) {
  "use strict";

  function PortfolioViewModel() {
    var self = this;

    self.state = ScreenState.create();
    self.format = format;

    self.account = ko.observable(null);
    self.holdings = ko.observableArray([]);
    self.totalCost = ko.observable(0);
    self.marketValue = ko.observable(0);
    self.unrealizedGainLoss = ko.observable(0);

    self.holdingsDP = new ArrayDataProvider(self.holdings, { keyAttributes: "holdingId" });

    self.hasAccount = ko.pureComputed(function () { return self.account() !== null; });
    self.hasHoldings = ko.pureComputed(function () {
      return !self.state.isLoading() && self.holdings().length > 0;
    });
    self.isEmpty = ko.pureComputed(function () {
      return !self.state.isLoading() && !self.state.errorMessage() &&
        self.hasAccount() && self.holdings().length === 0;
    });
    self.gainLossClass = ko.pureComputed(function () {
      return format.profitLossClass(self.unrealizedGainLoss());
    });

    // ---------------------------------------------------------------------
    // Presentation-only derivations over the holdings already loaded above.
    // No extra fetch, no extra state.
    // ---------------------------------------------------------------------

    /** Return on cost, as a percentage. Drives the meter on the gain/loss tile. */
    self.returnPercent = ko.pureComputed(function () {
      var cost = Number(self.totalCost()) || 0;
      return cost > 0 ? (Number(self.unrealizedGainLoss()) / cost) * 100 : 0;
    });

    /** Fixed window so the bar moves against a stable scale rather than rescaling itself. */
    self.returnMeterValue = ko.pureComputed(function () {
      return Math.max(-25, Math.min(25, self.returnPercent()));
    });

    self.returnMeterThresholds = [
      { max: -0.0001, color: "#c74634" },
      { max: 0.0001, color: "#8c8c8c" },
      { color: "#4c8c3f" }
    ];

    self.allocationRows = derived.array(function () {
      return self.holdings()
        .filter(function (h) { return (Number(h.marketValue) || 0) > 0; })
        .map(function (h) {
          return {
            id: "alloc-" + h.holdingId,
            series: h.productName,
            value: Number(h.marketValue) || 0
          };
        });
    });

    self.allocationDP = new ArrayDataProvider(self.allocationRows, { keyAttributes: "id" });

    /** Cost against market value, per holding - where the gain actually sits. */
    self.costVsValueRows = derived.array(function () {
      var rows = [];
      self.holdings().forEach(function (h) {
        var quantity = Number(h.quantity) || 0;
        rows.push({
          id: "cost-" + h.holdingId,
          series: "Cost",
          group: h.productName,
          value: quantity * (Number(h.averageCost) || 0)
        });
        rows.push({
          id: "mkt-" + h.holdingId,
          series: "Market value",
          group: h.productName,
          value: Number(h.marketValue) || 0
        });
      });
      return rows;
    });

    self.costVsValueDP = new ArrayDataProvider(self.costVsValueRows, { keyAttributes: "id" });

    /** A chart of one holding is noise. */
    self.hasChartableData = ko.pureComputed(function () {
      return self.holdings().length > 1;
    });

    self.accountInitials = ko.pureComputed(function () {
      var account = self.account();
      return account ? "P" + String(account.portfolioAccountId).slice(-2) : "P";
    });

    self.load = function () {
      var userId = SessionStore.userId();
      if (!userId) { return; }

      self.state.runAllowingNotFound(function () {
        return PortfolioService.getAccountByUser(userId);
      }).then(function (account) {
        if (!account) {
          return undefined;
        }
        self.account(account);

        return self.state.run(function () {
          return PortfolioService.getSummary(account.portfolioAccountId);
        }).then(function (summary) {
          if (!summary) { return undefined; }

          self.totalCost(summary.totalCost);
          self.marketValue(summary.marketValue);
          self.unrealizedGainLoss(summary.unrealizedGainLoss);

          var holdings = summary.holdings || [];
          if (holdings.length === 0) {
            self.holdings([]);
            return undefined;
          }

          // Holdings carry only productId, so fetch the names to make the table readable.
          // One call per distinct product; the catalogue is small.
          var productIds = holdings.map(function (h) { return h.productId; })
            .filter(function (id, i, all) { return all.indexOf(id) === i; });

          return Promise.all(productIds.map(function (id) {
            return ProductService.getProduct(id).catch(function () { return null; });
          })).then(function (products) {
            var nameById = {};
            products.forEach(function (p) {
              if (p) { nameById[p.productId] = p.productName; }
            });
            // Portfolio weight needs the total across all rows, so it is derived here
            // alongside the other display fields rather than per row in the view.
            var valuationTotal = holdings.reduce(function (sum, h) {
              return sum + (Number(h.marketValue) || 0);
            }, 0);

            self.holdings(holdings.map(function (h) {
              return Object.assign({}, h, {
                productName: nameById[h.productId] || ("Product " + h.productId),
                quantityDisplay: format.quantity(h.quantity),
                averageCostDisplay: format.money(h.averageCost),
                marketValueDisplay: format.money(h.marketValue),
                unrealizedGainLossDisplay: format.signedMoney(h.unrealizedGainLoss),
                weightPercent: valuationTotal > 0
                  ? ((Number(h.marketValue) || 0) / valuationTotal) * 100
                  : 0
              });
            }));
            return undefined;
          });
        });
      });
    };

    self.connected = function () {
      self.load();
    };
  }

  return PortfolioViewModel;
});
