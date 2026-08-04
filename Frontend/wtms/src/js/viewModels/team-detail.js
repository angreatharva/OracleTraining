/**
 * Team member - not implemented yet.
 *
 * Next step: one investor's portfolio, holdings and trades; reached from the Team screen
 * Primary API: PortfolioService.getAccountByUser, TradingService.getInvestmentOverview
 *
 * Follow the pattern in viewModels/dashboard.js (investor) or viewModels/team.js (manager):
 * observables for data / isLoading / errorMessage, load in connected(), and normalise
 * failures through services/ApiErrorNormalizer.
 */
define(["knockout"], function (ko) {
  "use strict";

  function ScreenViewModel() {
    var self = this;
    self.title = ko.observable("Team member");
    self.note = ko.observable("one investor's portfolio, holdings and trades; reached from the Team screen");
    self.api = ko.observable("PortfolioService.getAccountByUser, TradingService.getInvestmentOverview");

    self.connected = function () {
      // Load data here.
    };
  }

  return ScreenViewModel;
});
