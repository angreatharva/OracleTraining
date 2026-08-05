/**
 * One team member's positions and trades.
 *
 * Reached from the Team screen, which passes the userId through the router as a parameter.
 * Read-only: a manager can see a report's holdings and history but cannot trade for them -
 * the backend rejects that regardless of what this screen offers.
 */
define([
  "knockout",
  "ojs/ojarraydataprovider",
  "services/UserService",
  "services/PortfolioService",
  "services/TradingService",
  "utils/ScreenState",
  "utils/format"
], function (ko, ArrayDataProvider, UserService, PortfolioService, TradingService,
             ScreenState, format) {
  "use strict";

  /**
   * @param {{userId: (number|string)}} params supplied by CoreRouter as ?userId=...
   */
  function TeamDetailViewModel(params) {
    var self = this;

    self.state = ScreenState.create();
    self.format = format;

    // Router parameters arrive as strings from the URL.
    self.userId = ko.observable(params && params.userId ? Number(params.userId) : null);

    self.member = ko.observable(null);
    self.positions = ko.observableArray([]);
    self.trades = ko.observableArray([]);

    self.positionsDP = new ArrayDataProvider(self.positions, { keyAttributes: "productId" });
    self.tradesDP = new ArrayDataProvider(self.trades, { keyAttributes: "transactionId" });

    self.hasPositions = ko.pureComputed(function () { return self.positions().length > 0; });
    self.hasTrades = ko.pureComputed(function () { return self.trades().length > 0; });

    self.totalValue = ko.pureComputed(function () {
      return self.positions().reduce(function (sum, p) {
        return sum + (Number(p.currentValuation) || 0);
      }, 0);
    });
    self.totalProfitLoss = ko.pureComputed(function () {
      return self.positions().reduce(function (sum, p) {
        return sum + (Number(p.profitLoss) || 0);
      }, 0);
    });
    self.profitLossClass = ko.pureComputed(function () {
      return format.profitLossClass(self.totalProfitLoss());
    });

    self.noUser = ko.pureComputed(function () { return self.userId() === null; });

    self.load = function () {
      var userId = self.userId();
      if (!userId) { return; }

      self.state.run(function () {
        return UserService.getUser(userId);
      }).then(function (user) {
        if (!user) { return undefined; }
        self.member(user);

        // 404 here just means this investor has no portfolio account yet.
        return self.state.runAllowingNotFound(function () {
          return TradingService.getInvestmentOverview(userId);
        }).then(function (overview) {
          self.positions(overview ? (overview.positions || []) : []);

          if (!overview || !overview.portfolioAccountId) {
            return undefined;
          }
          return self.state.runAllowingNotFound(function () {
            return TradingService.listTrades({ portfolioAccountId: overview.portfolioAccountId });
          }).then(function (trades) {
            var rows = (trades || []).slice().sort(function (a, b) {
              return String(b.transactionDate).localeCompare(String(a.transactionDate));
            });
            self.trades(rows);
            return undefined;
          });
        });
      });
    };

    self.connected = function () {
      self.load();
    };
  }

  return TeamDetailViewModel;
});
