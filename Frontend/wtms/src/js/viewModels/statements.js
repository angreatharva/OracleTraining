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
  "utils/format"
], function (ko, ArrayDataProvider, ojdatetimepicker, TradingService, ScreenState, format) {
  "use strict";

  function StatementsViewModel() {
    var self = this;

    self.state = ScreenState.create();
    self.format = format;
    self.transactions = ko.observableArray([]);
    self.transactionsDP = new ArrayDataProvider(self.transactions, { keyAttributes: "transactionId" });
    self.startDate = ko.observable("");
    self.endDate = ko.observable("");

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
