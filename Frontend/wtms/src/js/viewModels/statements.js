/**
 * Statements - not implemented yet.
 *
 * Next step: list statements and show one in detail
 * Primary API: TradingService.listStatements
 *
 * Follow the pattern in viewModels/dashboard.js (investor) or viewModels/team.js (manager):
 * observables for data / isLoading / errorMessage, load in connected(), and normalise
 * failures through services/ApiErrorNormalizer.
 */
define(["knockout"], function (ko) {
  "use strict";

  function ScreenViewModel() {
    var self = this;
    self.title = ko.observable("Statements");
    self.note = ko.observable("list statements and show one in detail");
    self.api = ko.observable("TradingService.listStatements");

    self.connected = function () {
      // Load data here.
    };
  }

  return ScreenViewModel;
});
