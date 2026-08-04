/**
 * Trade history - not implemented yet.
 *
 * Next step: filterable table of past trades
 * Primary API: TradingService.listTrades
 *
 * Follow the pattern in viewModels/dashboard.js (investor) or viewModels/team.js (manager):
 * observables for data / isLoading / errorMessage, load in connected(), and normalise
 * failures through services/ApiErrorNormalizer.
 */
define(["knockout"], function (ko) {
  "use strict";

  function ScreenViewModel() {
    var self = this;
    self.title = ko.observable("Trade history");
    self.note = ko.observable("filterable table of past trades");
    self.api = ko.observable("TradingService.listTrades");

    self.connected = function () {
      // Load data here.
    };
  }

  return ScreenViewModel;
});
