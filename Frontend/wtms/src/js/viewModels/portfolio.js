/**
 * Portfolio: the account plus its holdings.
 *
 * Uses PortfolioService.getSummary, which returns the account, the holdings and the totals
 * in one call. Those totals come from each holding's stored `marketValue` and are not
 * refreshed against Product Service - so they can legitimately differ from the Dashboard,
 * which re-reads live prices. The view says so rather than leaving the discrepancy
 * looking like a bug.
 */
define([
  "knockout",
  "ojs/ojarraydataprovider",
  "services/PortfolioService",
  "services/ProductService",
  "services/SessionStore",
  "utils/ScreenState",
  "utils/format"
], function (ko, ArrayDataProvider, PortfolioService, ProductService, SessionStore,
             ScreenState, format) {
  "use strict";

  function PortfolioViewModel() {
    var self = this;

    self.state = ScreenState.create();
    self.format = format;

    self.account = ko.observable(null);
    self.holdings = ko.observableArray([]);
    self.totalCost = ko.observable(0);
    self.marketValue = ko.observable(0);
    self.unrealizedGainLoss = ko.observable(0);

    self.holdingsDP = new ArrayDataProvider(self.holdings, { keyAttributes: "holdingId" });

    self.hasAccount = ko.pureComputed(function () { return self.account() !== null; });
    self.hasHoldings = ko.pureComputed(function () {
      return !self.state.isLoading() && self.holdings().length > 0;
    });
    self.isEmpty = ko.pureComputed(function () {
      return !self.state.isLoading() && !self.state.errorMessage() &&
        self.hasAccount() && self.holdings().length === 0;
    });
    self.gainLossClass = ko.pureComputed(function () {
      return format.profitLossClass(self.unrealizedGainLoss());
    });

    self.load = function () {
      var userId = SessionStore.userId();
      if (!userId) { return; }

      self.state.runAllowingNotFound(function () {
        return PortfolioService.getAccountByUser(userId);
      }).then(function (account) {
        if (!account) {
          return undefined;
        }
        self.account(account);

        return self.state.run(function () {
          return PortfolioService.getSummary(account.portfolioAccountId);
        }).then(function (summary) {
          if (!summary) { return undefined; }

          self.totalCost(summary.totalCost);
          self.marketValue(summary.marketValue);
          self.unrealizedGainLoss(summary.unrealizedGainLoss);

          var holdings = summary.holdings || [];
          if (holdings.length === 0) {
            self.holdings([]);
            return undefined;
          }

          // Holdings carry only productId, so fetch the names to make the table readable.
          // One call per distinct product; the catalogue is small.
          var productIds = holdings.map(function (h) { return h.productId; })
            .filter(function (id, i, all) { return all.indexOf(id) === i; });

          return Promise.all(productIds.map(function (id) {
            return ProductService.getProduct(id).catch(function () { return null; });
          })).then(function (products) {
            var nameById = {};
            products.forEach(function (p) {
              if (p) { nameById[p.productId] = p.productName; }
            });
            self.holdings(holdings.map(function (h) {
              return Object.assign({}, h, {
                productName: nameById[h.productId] || ("Product " + h.productId)
              });
            }));
            return undefined;
          });
        });
      });
    };

    self.connected = function () {
      self.load();
    };
  }

  return PortfolioViewModel;
});
