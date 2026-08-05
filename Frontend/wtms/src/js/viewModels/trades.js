/**
 * Trade history with filters.
 *
 * The backend pins an investor to their own portfolio account regardless of what is sent,
 * so no account filter is needed here. There is no pagination on any list endpoint - the
 * whole table comes back - so filtering by status and type happens server-side where it can,
 * and the date range is left to the caller.
 */
define([
  "knockout",
  "ojs/ojarraydataprovider",
  "services/TradingService",
  "utils/ScreenState",
  "utils/derived",
  "utils/format",
  "models/enums"
], function (ko, ArrayDataProvider, TradingService, ScreenState, derived, format, enums) {
  "use strict";

  function TradesViewModel() {
    var self = this;

    self.state = ScreenState.create();
    self.format = format;

    self.trades = ko.observableArray([]);
    self.tradesDP = new ArrayDataProvider(self.trades, { keyAttributes: "transactionId" });

    // "" means no filter; the ApiClient drops empty query values.
    self.statusFilter = ko.observable("");
    self.typeFilter = ko.observable("");

    self.statusOptions = [""].concat(enums.TRANSACTION_STATUS);
    self.typeOptions = [""].concat(enums.TRANSACTION_TYPE);

    /**
     * The same option lists as DataProviders, for oj-c-select-single. The empty-string
     * entry is the "no filter" case the ApiClient drops from the query.
     */
    function optionsProvider(values, allLabel) {
      return new ArrayDataProvider(
        values.map(function (value) {
          return { value: value, label: value === "" ? allLabel : value };
        }),
        { keyAttributes: "value" }
      );
    }

    self.statusDP = optionsProvider(self.statusOptions, "All statuses");
    self.typeDP = optionsProvider(self.typeOptions, "All types");

    // ---------------------------------------------------------------------
    // Presentation-only summaries over the rows already loaded.
    // ---------------------------------------------------------------------

    function countWhere(predicate) {
      return self.trades().filter(predicate).length;
    }

    self.completedCount = ko.pureComputed(function () {
      return countWhere(function (t) { return t.transactionStatus === "COMPLETED"; });
    });
    self.failedCount = ko.pureComputed(function () {
      return countWhere(function (t) { return t.transactionStatus === "FAILED"; });
    });

    /** Completed trades only: pending and failed ones moved no money. */
    self.settledValue = ko.pureComputed(function () {
      return self.trades().reduce(function (sum, t) {
        return t.transactionStatus === "COMPLETED" ? sum + (Number(t.totalAmount) || 0) : sum;
      }, 0);
    });

    /** Completed value split by side - what was bought against what was sold. */
    self.sideRows = derived.array(function () {
      var totals = { BUY: 0, SELL: 0 };
      self.trades().forEach(function (t) {
        if (t.transactionStatus === "COMPLETED" && totals[t.transactionType] !== undefined) {
          totals[t.transactionType] += Number(t.totalAmount) || 0;
        }
      });
      return Object.keys(totals)
        .filter(function (side) { return totals[side] > 0; })
        .map(function (side) {
          return {
            id: "side-" + side,
            series: side,
            value: totals[side],
            color: side === "SELL" ? "#b8862c" : "#3d7bb1"
          };
        });
    });

    self.sideDP = new ArrayDataProvider(self.sideRows, { keyAttributes: "id" });

    /** Completed value per calendar day, oldest first - the shape of recent activity. */
    self.dailyRows = derived.array(function () {
      var byDay = {};
      self.trades().forEach(function (t) {
        if (t.transactionStatus !== "COMPLETED") { return; }
        var day = format.date(t.transactionDate);
        byDay[day] = (byDay[day] || 0) + (Number(t.totalAmount) || 0);
      });
      return Object.keys(byDay).sort().map(function (day) {
        return { id: "day-" + day, group: day, value: byDay[day] };
      });
    });

    self.dailyDP = new ArrayDataProvider(self.dailyRows, { keyAttributes: "id" });

    /** One point is not a trend, and one bar is not a comparison. */
    self.hasChartableData = ko.pureComputed(function () {
      return self.dailyRows().length > 1 || self.sideRows().length > 1;
    });

    self.hasTrades = ko.pureComputed(function () {
      return !self.state.isLoading() && self.trades().length > 0;
    });
    self.isEmpty = ko.pureComputed(function () {
      return !self.state.isLoading() && !self.state.errorMessage() && self.trades().length === 0;
    });

    self.load = function () {
      self.state.runAllowingNotFound(function () {
        return TradingService.listTrades({
          status: self.statusFilter(),
          type: self.typeFilter()
        });
      }).then(function (trades) {
        // Newest first: the backend returns insertion order.
        var rows = (trades || []).slice().sort(function (a, b) {
          return String(b.transactionDate).localeCompare(String(a.transactionDate));
        });
        self.trades(rows);
      });
    };

    self.applyFilters = function () {
      self.load();
    };

    self.clearFilters = function () {
      self.statusFilter("");
      self.typeFilter("");
      self.load();
    };

    self.connected = function () {
      self.load();
    };
  }

  return TradesViewModel;
});
