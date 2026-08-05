/**
 * Investor dashboard - the reference pattern for a data-loading screen.
 *
 * Copy this shape for new screens: a ScreenState for the loading/error plumbing, load in
 * `connected()`, and let ScreenState.run turn any service failure into a displayable
 * message. `run` never rejects, so a falsy result means "it failed and the message is
 * already on state.errorMessage".
 *
 * One call does the work here: the backend already aggregates holdings, completed trades and
 * live Product prices into positions.
 */
define([
  "knockout",
  "ojs/ojarraydataprovider",
  "services/TradingService",
  "services/SessionStore",
  "utils/ScreenState",
  "utils/format"
], function (ko, ArrayDataProvider, TradingService, SessionStore, ScreenState, format) {
  "use strict";

  function DashboardViewModel() {
    var self = this;

    self.state = ScreenState.create();
    self.format = format;

    self.positions = ko.observableArray([]);
    self.dataProvider = new ArrayDataProvider(self.positions, { keyAttributes: "productId" });

    self.totalInvested = ko.pureComputed(function () {
      return self.positions().reduce(function (sum, p) {
        return sum + (Number(p.investedValue) || 0);
      }, 0);
    });
    self.totalValue = ko.pureComputed(function () {
      return self.positions().reduce(function (sum, p) {
        return sum + (Number(p.currentValuation) || 0);
      }, 0);
    });
    self.totalProfitLoss = ko.pureComputed(function () {
      return self.totalValue() - self.totalInvested();
    });
    self.profitLossClass = ko.pureComputed(function () {
      return format.profitLossClass(self.totalProfitLoss());
    });

    self.hasPositions = ko.pureComputed(function () {
      return !self.state.isLoading() && self.positions().length > 0;
    });
    self.isEmpty = ko.pureComputed(function () {
      return !self.state.isLoading() && !self.state.errorMessage() && self.positions().length === 0;
    });

    self.load = function () {
      var userId = SessionStore.userId();
      if (!userId) { return; }

      // 404 means no portfolio account yet, which is a normal state for a new investor.
      self.state.runAllowingNotFound(function () {
        return TradingService.getInvestmentOverview(userId);
      }).then(function (overview) {
        self.positions((overview ? (overview.positions || []) : []).map(function (position) {
          return Object.assign({}, position, {
            currentQuantityDisplay: format.quantity(position.currentQuantity),
            averageBuyPriceDisplay: format.money(position.averageBuyPrice),
            currentPriceDisplay: format.money(position.currentPrice),
            investedValueDisplay: format.money(position.investedValue),
            currentValuationDisplay: format.money(position.currentValuation),
            profitLossDisplay: format.signedMoney(position.profitLoss)
          });
        }));
      });
    };

    self.connected = function () {
      self.load();
    };
  }

  return DashboardViewModel;
});
