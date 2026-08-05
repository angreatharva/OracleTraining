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
  "utils/format",
  "models/enums"
], function (ko, ArrayDataProvider, TradingService, ScreenState, format, enums) {
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
