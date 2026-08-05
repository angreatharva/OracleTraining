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
