/**
 * Trade - not implemented yet.
 *
 * Next step: BUY/SELL form; if no holding exists for the product, call addHolding first, then submitTrade. Handle the 422 FAILED response as a normal outcome
 * Primary API: ProductService.listProducts, PortfolioService.addHolding, TradingService.submitTrade
 *
 * Follow the pattern in viewModels/dashboard.js (investor) or viewModels/team.js (manager):
 * observables for data / isLoading / errorMessage, load in connected(), and normalise
 * failures through services/ApiErrorNormalizer.
 */
define(["knockout"], function (ko) {
  "use strict";

  function ScreenViewModel() {
    var self = this;
    self.title = ko.observable("Trade");
    self.note = ko.observable("BUY/SELL form; if no holding exists for the product, call addHolding first, then submitTrade. Handle the 422 FAILED response as a normal outcome");
    self.api = ko.observable("ProductService.listProducts, PortfolioService.addHolding, TradingService.submitTrade");

    self.connected = function () {
      // Load data here.
    };
  }

  return ScreenViewModel;
});
