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
  "services/ProductService",
  "services/TradingService",
  "utils/ScreenState",
  "utils/derived",
  "utils/format"
], function (ko, ArrayDataProvider, UserService, PortfolioService, ProductService, TradingService,
             ScreenState, derived, format) {
  "use strict";

  /**
   * @param {{params: {userId: (number|string)}}} params supplied by ModuleRouterAdapter.
   */
  function TeamDetailViewModel(params) {
    var self = this;

    self.state = ScreenState.create();
    self.format = format;

    // ModuleRouterAdapter wraps CoreRouter's route parameters in `params`. Values arrive
    // as strings from UrlParamAdapter, e.g. ?ojr=team-detail;userId=2.
    var routeParams = params && params.params ? params.params : {};
    self.userId = ko.observable(routeParams.userId ? Number(routeParams.userId) : null);

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

    // ---------------------------------------------------------------------
    // Presentation-only derivations over the positions already loaded.
    // ---------------------------------------------------------------------

    self.memberInitials = ko.pureComputed(function () {
      var member = self.member();
      return member ? format.initials(member.fullName) : "?";
    });

    self.totalInvested = ko.pureComputed(function () {
      return self.positions().reduce(function (sum, p) {
        return sum + ((Number(p.currentQuantity) || 0) * (Number(p.averageBuyPrice) || 0));
      }, 0);
    });

    self.returnPercent = ko.pureComputed(function () {
      var invested = self.totalInvested();
      return invested > 0 ? (self.totalProfitLoss() / invested) * 100 : 0;
    });

    self.returnMeterValue = ko.pureComputed(function () {
      return Math.max(-25, Math.min(25, self.returnPercent()));
    });

    self.returnMeterThresholds = [
      { max: -0.0001, color: "#c74634" },
      { max: 0.0001, color: "#8c8c8c" },
      { color: "#4c8c3f" }
    ];

    self.allocationRows = derived.array(function () {
      return self.positions()
        .filter(function (p) { return (Number(p.currentValuation) || 0) > 0; })
        .map(function (p) {
          return {
            id: "alloc-" + p.productId,
            series: p.productName,
            value: Number(p.currentValuation) || 0
          };
        });
    });

    self.allocationDP = new ArrayDataProvider(self.allocationRows, { keyAttributes: "id" });

    self.profitLossRows = derived.array(function () {
      return self.positions().map(function (p) {
        var pl = Number(p.profitLoss) || 0;
        return {
          id: "pl-" + p.productId,
          group: p.productName,
          value: pl,
          color: pl < 0 ? "#c74634" : "#4c8c3f"
        };
      });
    });

    self.profitLossDP = new ArrayDataProvider(self.profitLossRows, { keyAttributes: "id" });

    /** A chart of one position is noise. */
    self.hasChartableData = ko.pureComputed(function () {
      return self.positions().length > 1;
    });

    self.load = function () {
      var userId = self.userId();
      if (!userId) { return; }

      self.state.run(function () {
        return UserService.getUser(userId);
      }).then(function (user) {
        if (!user) { return undefined; }
        self.member(user);

        // Resolve the selected investor's account, then load its stored portfolio summary.
        // This produces GET /portfolio/api/portfolios/{portfolioAccountId}/summary.
        return self.state.runAllowingNotFound(function () {
          return PortfolioService.getAccountByUser(userId);
        }).then(function (account) {
          if (!account || !account.portfolioAccountId) {
            return undefined;
          }

          return self.state.runAllowingNotFound(function () {
            return PortfolioService.getSummary(account.portfolioAccountId);
          }).then(function (summary) {
            if (!summary) { return undefined; }

            var holdings = summary.holdings || [];
            return Promise.all(holdings.map(function (holding) {
              return ProductService.getProduct(holding.productId).catch(function () { return null; });
            })).then(function (products) {
              self.positions(holdings.map(function (holding, index) {
                var quantity = Number(holding.quantity) || 0;
                var value = Number(holding.marketValue) || 0;
                return {
                  productId: holding.productId,
                  productName: products[index] ? products[index].productName : ("Product " + holding.productId),
                  currentQuantity: holding.quantity,
                  averageBuyPrice: holding.averageCost,
                  currentPrice: quantity ? value / quantity : 0,
                  currentValuation: holding.marketValue,
                  profitLoss: holding.unrealizedGainLoss
                };
              }));

              return self.state.runAllowingNotFound(function () {
                return TradingService.listTrades({ portfolioAccountId: account.portfolioAccountId });
              });
            });
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
