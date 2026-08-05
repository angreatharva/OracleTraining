/**
 * Investor transaction history. This screen intentionally uses the Trading endpoint rather
 * than portfolio statements: investors need to see the individual BUY and SELL records.
 */
define([
  "knockout",
  "ojs/ojarraydataprovider",
  "ojs/ojdatetimepicker",
  "services/TradingService",
  "utils/ScreenState",
  "utils/derived",
  "utils/format"
], function (ko, ArrayDataProvider, ojdatetimepicker, TradingService, ScreenState, derived, format) {
  "use strict";

  function StatementsViewModel() {
    var self = this;

    self.state = ScreenState.create();
    self.format = format;
    self.transactions = ko.observableArray([]);
    self.transactionsDP = new ArrayDataProvider(self.transactions, { keyAttributes: "transactionId" });
    self.startDate = ko.observable("");
    self.endDate = ko.observable("");

    // ---------------------------------------------------------------------
    // Presentation-only summaries over the rows already loaded.
    // ---------------------------------------------------------------------

    self.completedCount = ko.pureComputed(function () {
      return self.transactions().filter(function (t) {
        return t.transactionStatus === "COMPLETED";
      }).length;
    });

    /** Completed only, split by side: money out against money in. */
    self.boughtValue = ko.pureComputed(function () {
      return self.transactions().reduce(function (sum, t) {
        return t.transactionStatus === "COMPLETED" && t.transactionType === "BUY"
          ? sum + (Number(t.totalAmount) || 0) : sum;
      }, 0);
    });

    self.soldValue = ko.pureComputed(function () {
      return self.transactions().reduce(function (sum, t) {
        return t.transactionStatus === "COMPLETED" && t.transactionType === "SELL"
          ? sum + (Number(t.totalAmount) || 0) : sum;
      }, 0);
    });

    /**
     * Completed value per calendar day and side, oldest first. Two series so buying and
     * selling can be read against each other over the selected range.
     */
    self.dailyRows = derived.array(function () {
      var byDay = {};
      self.transactions().forEach(function (t) {
        if (t.transactionStatus !== "COMPLETED") { return; }
        var day = format.date(t.transactionDate);
        if (!byDay[day]) { byDay[day] = { BUY: 0, SELL: 0 }; }
        byDay[day][t.transactionType] += Number(t.totalAmount) || 0;
      });

      var rows = [];
      Object.keys(byDay).sort().forEach(function (day) {
        ["BUY", "SELL"].forEach(function (side) {
          rows.push({
            id: day + "-" + side,
            group: day,
            series: side === "BUY" ? "Bought" : "Sold",
            value: byDay[day][side]
          });
        });
      });
      return rows;
    });

    self.dailyDP = new ArrayDataProvider(self.dailyRows, { keyAttributes: "id" });

    /** Two points make a trend; one does not. */
    self.hasChartableData = ko.pureComputed(function () {
      return self.dailyRows().length > 2;
    });

    self.hasTransactions = ko.pureComputed(function () {
      return !self.state.isLoading() && self.transactions().length > 0;
    });
    self.isEmpty = ko.pureComputed(function () {
      return !self.state.isLoading() && !self.state.errorMessage() && self.transactions().length === 0;
    });

    self.load = function () {
      var filter = {};
      // oj-input-date supplies yyyy-mm-dd. Trading expects LocalDateTime, so make the
      // range inclusive of both selected calendar days.
      if (self.startDate()) { filter.startDate = self.startDate() + "T00:00:00"; }
      if (self.endDate()) { filter.endDate = self.endDate() + "T23:59:59.999999999"; }

      self.state.runAllowingNotFound(function () {
        return TradingService.listTrades(filter);
      }).then(function (transactions) {
        var rows = (transactions || []).slice().sort(function (a, b) {
          return String(b.transactionDate).localeCompare(String(a.transactionDate));
        });
        self.transactions(rows);
      });
    };

    self.applyFilters = function () { self.load(); };
    self.clearFilters = function () {
      self.startDate("");
      self.endDate("");
      self.load();
    };

    self.connected = function () { self.load(); };
  }

  return StatementsViewModel;
});
