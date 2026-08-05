/**
 * BUY / SELL order entry.
 *
 * Two backend behaviours shape this screen:
 *
 * 1. A trade needs an existing holdingId - there is no first-holding auto-creation. So for a
 *    BUY of something not yet held, this screen opens the holding first and then trades.
 *    Those are two separate, non-atomic calls; if the trade then fails, the new holding
 *    stays behind. That is surfaced rather than hidden.
 *
 * 2. A rejected trade is HTTP 422 carrying a normal TradeTransactionResponse, not an error
 *    envelope. TradingService unwraps it, so a FAILED trade arrives here as a resolved
 *    value. `failureReason` is readable only on that response and is never persisted, so it
 *    is captured into lastTrade immediately.
 */
define([
  "knockout",
  "ojs/ojarraydataprovider",
  "services/ProductService",
  "services/PortfolioService",
  "services/BankService",
  "services/TradingService",
  "services/SessionStore",
  "utils/ScreenState",
  "utils/format"
], function (ko, ArrayDataProvider, ProductService, PortfolioService, BankService,
             TradingService, SessionStore, ScreenState, format) {
  "use strict";

  /**
   * Smallest quantity the backend accepts when opening a holding
   * (CreateHoldingRequest.quantity is @DecimalMin("0.0001") - zero is rejected).
   */
  var MIN_HOLDING_QUANTITY = 0.0001;

  function TradeViewModel() {
    var self = this;

    self.state = ScreenState.create();
    self.format = format;

    self.products = ko.observableArray([]);
    self.bankAccounts = ko.observableArray([]);
    self.holdings = ko.observableArray([]);
    self.portfolioAccountId = ko.observable(null);

    self.productsDP = new ArrayDataProvider(self.products, { keyAttributes: "productId" });
    self.bankAccountsDP = new ArrayDataProvider(self.bankAccounts, { keyAttributes: "bankAccountId" });

    self.selectedProductId = ko.observable(null);
    self.selectedBankAccountId = ko.observable(null);
    self.transactionType = ko.observable("BUY");
    self.quantity = ko.observable(1);

    self.isSubmitting = ko.observable(false);
    /** The trade returned by the last submit - COMPLETED or FAILED. */
    self.lastTrade = ko.observable(null);

    self.selectedProduct = ko.pureComputed(function () {
      var id = self.selectedProductId();
      return self.products().filter(function (p) { return p.productId === id; })[0] || null;
    });

    /** The holding for the selected product, if the investor already owns it. */
    self.existingHolding = ko.pureComputed(function () {
      var id = self.selectedProductId();
      return self.holdings().filter(function (h) { return h.productId === id; })[0] || null;
    });

    self.heldQuantity = ko.pureComputed(function () {
      var holding = self.existingHolding();
      return holding ? Number(holding.quantity) : 0;
    });

    self.isSell = ko.pureComputed(function () { return self.transactionType() === "SELL"; });

    // Keep the call-to-action label bound to the selected order side.
    self.submitLabel = ko.pureComputed(function () {
      if (self.isSubmitting()) { return "Submitting..."; }
      return self.isSell() ? "Sell" : "Buy";
    });

    self.willOpenHolding = ko.pureComputed(function () {
      return !self.isSell() && !!self.selectedProductId() && !self.existingHolding();
    });

    self.currentPriceText = ko.pureComputed(function () {
      var product = self.selectedProduct();
      return product ? format.money(product.currentPrice) : format.EMPTY;
    });

    /** Indicative only - the server executes at Product Service's current price. */
    self.estimatedTotal = ko.pureComputed(function () {
      var product = self.selectedProduct();
      var qty = Number(self.quantity());
      if (!product || !qty || isNaN(qty)) {
        return 0;
      }
      return Number(product.currentPrice) * qty;
    });

    self.estimatedTotalText = ko.pureComputed(function () {
      return format.money(self.estimatedTotal());
    });

    self.validationMessage = ko.pureComputed(function () {
      var qty = Number(self.quantity());
      if (!self.selectedProductId()) { return "Choose a product."; }
      if (!self.selectedBankAccountId()) { return "Choose a bank account."; }
      if (!qty || isNaN(qty) || qty < 0.01) { return "Quantity must be at least 0.01."; }
      if (self.isSell() && !self.existingHolding()) { return "You do not hold this product."; }
      if (self.isSell() && qty > self.heldQuantity()) {
        return "You hold only " + format.quantity(self.heldQuantity()) + " units.";
      }
      return "";
    });

    self.canSubmit = ko.pureComputed(function () {
      return !self.isSubmitting() &&
        self.validationMessage() === "" &&
        self.portfolioAccountId() !== null;
    });

    self.tradeSucceeded = ko.pureComputed(function () {
      var trade = self.lastTrade();
      return !!trade && trade.transactionStatus === "COMPLETED";
    });

    self.tradeFailed = ko.pureComputed(function () {
      var trade = self.lastTrade();
      return !!trade && trade.transactionStatus !== "COMPLETED";
    });

    // ---------------------------------------------------------------------------------

    function loadHoldings(accountId) {
      return PortfolioService.listHoldings(accountId).then(function (holdings) {
        self.holdings(holdings || []);
      });
    }

    self.load = function () {
      var userId = SessionStore.userId();
      if (!userId) { return; }

      self.state.run(function () {
        return Promise.all([
          ProductService.listProducts({ status: "ACTIVE" }),
          BankService.listAccounts({ status: "ACTIVE" }),
          // A 404 here means no portfolio account yet; handled explicitly below.
          PortfolioService.getAccountByUser(userId).catch(function () { return null; })
        ]);
      }).then(function (results) {
        if (!results) { return undefined; }

        self.products(results[0] || []);
        self.bankAccounts(results[1] || []);

        var account = results[2];
        if (!account) {
          self.state.errorMessage(
            "You do not have a portfolio account yet. Ask your manager to open one."
          );
          return undefined;
        }
        self.portfolioAccountId(account.portfolioAccountId);

        // Preselect the primary account - most investors have exactly one.
        var primary = self.bankAccounts().filter(function (a) { return a.primaryAccount; })[0];
        if (primary) { self.selectedBankAccountId(primary.bankAccountId); }

        return loadHoldings(account.portfolioAccountId);
      });
    };

    /**
     * Resolves the holdingId to trade against, opening a holding first when this is a BUY of
     * something not yet held.
     * @returns {Promise<number>}
     */
    function resolveHoldingId() {
      var existing = self.existingHolding();
      if (existing) {
        return Promise.resolve(existing.holdingId);
      }
      return PortfolioService.addHolding(self.portfolioAccountId(), {
        productId: self.selectedProductId(),
        // Cannot be 0 - the backend rejects anything below 0.0001. A brand-new holding is
        // therefore left with a fractional residue. That is a backend constraint, not a
        // UI choice, and it is called out in the view.
        quantity: MIN_HOLDING_QUANTITY,
        averageCost: 0
      }).then(function (holding) {
        return holding.holdingId;
      });
    }

    self.submit = function () {
      if (!self.canSubmit()) { return; }

      self.isSubmitting(true);
      self.lastTrade(null);

      var openedHolding = self.willOpenHolding();

      self.state.run(function () {
        return resolveHoldingId().then(function (holdingId) {
          return TradingService.submitTrade({
            portfolioAccountId: self.portfolioAccountId(),
            holdingId: holdingId,
            productId: self.selectedProductId(),
            bankAccountId: self.selectedBankAccountId(),
            transactionType: self.transactionType(),
            quantity: Number(self.quantity()),
            // Required by the API, but the server replaces it with the live price.
            unitPrice: Number(self.selectedProduct().currentPrice)
          });
        });
      }).then(function (trade) {
        self.isSubmitting(false);

        if (!trade) {
          // The request itself failed; state.errorMessage already says why. If we had
          // already opened a holding, say so - it is now sitting there with no trade.
          if (openedHolding) {
            self.state.errorMessage(
              self.state.errorMessage() +
              " A holding was opened for this product before the failure, so it now exists with an almost-zero balance."
            );
          }
          return undefined;
        }

        self.lastTrade(trade);
        // Refresh holdings so a follow-up SELL sees the new quantity.
        return loadHoldings(self.portfolioAccountId());
      });
    };

    self.reset = function () {
      self.lastTrade(null);
      self.state.clearMessages();
    };

    self.connected = function () {
      self.load();
    };
  }

  return TradeViewModel;
});
