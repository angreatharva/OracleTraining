/**
 * The investor's own bank accounts.
 *
 * Read plus "make primary" only. Creating an account needs the real account number, which
 * is write-only server-side, and changing status is manager-only - so neither belongs here.
 */
define([
  "knockout",
  "ojs/ojarraydataprovider",
  "services/BankService",
  "utils/ScreenState",
  "utils/format"
], function (ko, ArrayDataProvider, BankService, ScreenState, format) {
  "use strict";

  function BankViewModel() {
    var self = this;

    self.state = ScreenState.create();
    self.format = format;

    self.accounts = ko.observableArray([]);
    self.accountsDP = new ArrayDataProvider(self.accounts, { keyAttributes: "bankAccountId" });
    self.busyAccountId = ko.observable(null);

    // ---------------------------------------------------------------------
    // Presentation-only derivations over the accounts already loaded.
    // ---------------------------------------------------------------------

    self.totalBalance = ko.pureComputed(function () {
      return self.accounts().reduce(function (sum, a) {
        return sum + (Number(a.balance) || 0);
      }, 0);
    });

    self.activeCount = ko.pureComputed(function () {
      return self.accounts().filter(function (a) { return a.status === "ACTIVE"; }).length;
    });

    self.primaryAccount = ko.pureComputed(function () {
      return self.accounts().filter(function (a) { return a.primaryAccount; })[0] || null;
    });

    /**
     * Scale for the per-row balance meters. Each row is drawn against the largest balance
     * rather than the total, so the biggest account fills its bar and the rest read as a
     * proportion of it - which is the comparison worth making across a handful of accounts.
     */
    self.maxBalance = ko.pureComputed(function () {
      return self.accounts().reduce(function (max, a) {
        return Math.max(max, Number(a.balance) || 0);
      }, 0) || 1;
    });

    self.hasAccounts = ko.pureComputed(function () {
      return !self.state.isLoading() && self.accounts().length > 0;
    });
    self.isEmpty = ko.pureComputed(function () {
      return !self.state.isLoading() && !self.state.errorMessage() && self.accounts().length === 0;
    });

    self.load = function () {
      // No userId needed: the backend narrows an investor to their own accounts.
      self.state.runAllowingNotFound(function () {
        return BankService.listAccounts();
      }).then(function (accounts) {
        self.accounts(accounts || []);
      });
    };

    /** @param {{bankAccountId:number, status:string, primaryAccount:boolean}} account */
    self.makePrimary = function (account) {
      if (account.primaryAccount || account.status !== "ACTIVE") {
        return;
      }
      self.busyAccountId(account.bankAccountId);
      self.state.run(function () {
        return BankService.makePrimary(account.bankAccountId);
      }).then(function (updated) {
        self.busyAccountId(null);
        if (updated) {
          self.state.successMessage("Primary account updated.");
          self.load();
        }
      });
    };

    self.connected = function () {
      self.load();
    };
  }

  return BankViewModel;
});
