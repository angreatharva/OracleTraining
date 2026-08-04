/**
 * Investor dashboard - the reference pattern for a data-loading screen.
 *
 * Copy this shape for new screens: observables for data / isLoading / errorMessage, load in
 * `connected()`, and always route failures through ApiErrorNormalizer so every service's
 * error body renders the same way.
 */
define([
  "knockout",
  "ojs/ojarraydataprovider",
  "services/TradingService",
  "services/SessionStore",
  "services/ApiErrorNormalizer"
], function (ko, ArrayDataProvider, TradingService, SessionStore, ApiErrorNormalizer) {
  "use strict";

  function DashboardViewModel() {
    var self = this;

    self.isLoading = ko.observable(false);
    self.errorMessage = ko.observable("");
    self.positions = ko.observableArray([]);

    self.dataProvider = new ArrayDataProvider(self.positions, { keyAttributes: "productId" });

    self.totalInvested = ko.pureComputed(function () {
      return self.positions().reduce(function (sum, p) { return sum + (p.investedValue || 0); }, 0);
    });
    self.totalValue = ko.pureComputed(function () {
      return self.positions().reduce(function (sum, p) { return sum + (p.currentValuation || 0); }, 0);
    });
    self.totalProfitLoss = ko.pureComputed(function () {
      return self.totalValue() - self.totalInvested();
    });
    self.profitLossClass = ko.pureComputed(function () {
      return self.totalProfitLoss() < 0 ? "oj-text-color-danger" : "oj-text-color-success";
    });

    self.hasPositions = ko.pureComputed(function () {
      return !self.isLoading() && self.positions().length > 0;
    });
    self.isEmpty = ko.pureComputed(function () {
      return !self.isLoading() && !self.errorMessage() && self.positions().length === 0;
    });

    self.load = function () {
      var userId = SessionStore.userId();
      if (!userId) {
        return;
      }
      self.isLoading(true);
      self.errorMessage("");

      // One call: the backend already aggregates holdings, completed trades and live
      // Product prices into positions.
      TradingService.getInvestmentOverview(userId)
        .then(function (overview) {
          self.positions(overview.positions || []);
          self.isLoading(false);
        })
        .catch(function (error) {
          var normalized = ApiErrorNormalizer.normalize(error);
          // 404 just means this investor has no portfolio account yet - not an error state.
          if (normalized.status === 404) {
            self.positions([]);
          } else {
            self.errorMessage(normalized.message);
          }
          self.isLoading(false);
        });
    };

    /** Called by oj-module each time the view is attached. */
    self.connected = function () {
      self.load();
    };
  }

  return DashboardViewModel;
});
