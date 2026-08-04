/**
 * Portfolio - not implemented yet.
 *
 * Next step: show the account summary and a holdings table
 * Primary API: PortfolioService.getAccountByUser then getSummary
 *
 * Follow the pattern in viewModels/dashboard.js (investor) or viewModels/team.js (manager):
 * observables for data / isLoading / errorMessage, load in connected(), and normalise
 * failures through services/ApiErrorNormalizer.
 */
define(["knockout"], function (ko) {
  "use strict";

  function ScreenViewModel() {
    var self = this;
    self.title = ko.observable("Portfolio");
    self.note = ko.observable("show the account summary and a holdings table");
    self.api = ko.observable("PortfolioService.getAccountByUser then getSummary");

    self.connected = function () {
      // Load data here.
    };
  }

  return ScreenViewModel;
});
