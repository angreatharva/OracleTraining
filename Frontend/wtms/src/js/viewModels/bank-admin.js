/**
 * Bank account administration for the manager's team.
 *
 * Note PUT /api/bank-accounts/{id} is a partial update despite being a PUT - only non-null
 * fields are applied. So changing status sends just `status` and leaves everything else
 * untouched, which is the opposite of how the product PUT behaves.
 */
define([
  "knockout",
  "ojs/ojarraydataprovider",
  "services/UserService",
  "services/BankService",
  "services/SessionStore",
  "utils/ScreenState",
  "utils/format",
  "models/enums"
], function (ko, ArrayDataProvider, UserService, BankService, SessionStore, ScreenState,
             format, enums) {
  "use strict";

  function BankAdminViewModel() {
    var self = this;

    self.state = ScreenState.create();
    self.format = format;

    self.team = ko.observableArray([]);
    self.accounts = ko.observableArray([]);
    self.selectedUserId = ko.observable(null);
    self.busyId = ko.observable(null);
    self.statusOptions = enums.BANK_ACCOUNT_STATUS;

    /** Opening deposit for a newly-opened account; everything else is filled in server-side. */
    self.openingBalance = ko.observable("");
    self.isOpeningAccount = ko.observable(false);

    self.teamDP = new ArrayDataProvider(self.team, { keyAttributes: "userId" });
    self.accountsDP = new ArrayDataProvider(self.accounts, { keyAttributes: "bankAccountId" });

    // ---------------------------------------------------------------------
    // Presentation-only derivations over the accounts already loaded.
    // ---------------------------------------------------------------------

    self.selectedMember = ko.pureComputed(function () {
      var id = self.selectedUserId();
      return self.team().filter(function (m) { return m.userId === id; })[0] || null;
    });

    self.totalBalance = ko.pureComputed(function () {
      return self.accounts().reduce(function (sum, a) {
        return sum + (Number(a.balance) || 0);
      }, 0);
    });

    self.activeCount = ko.pureComputed(function () {
      return self.accounts().filter(function (a) { return a.status === "ACTIVE"; }).length;
    });

    self.blockedCount = ko.pureComputed(function () {
      return self.accounts().filter(function (a) { return a.status === "BLOCKED"; }).length;
    });

    /** Scale for the per-row balance meters - each row against the largest balance. */
    self.maxBalance = ko.pureComputed(function () {
      return self.accounts().reduce(function (max, a) {
        return Math.max(max, Number(a.balance) || 0);
      }, 0) || 1;
    });

    self.hasAccounts = ko.pureComputed(function () {
      return !self.state.isLoading() && self.accounts().length > 0;
    });
    self.isEmpty = ko.pureComputed(function () {
      return !self.state.isLoading() && !self.state.errorMessage() &&
        self.selectedUserId() !== null && self.accounts().length === 0;
    });

    self.loadTeam = function () {
      var managerId = SessionStore.userId();
      if (!managerId) { return; }

      self.state.run(function () {
        return UserService.listTeam(managerId);
      }).then(function (team) {
        self.team(team || []);
        if (self.team().length > 0 && self.selectedUserId() === null) {
          self.selectedUserId(self.team()[0].userId);
          self.loadAccounts();
        }
      });
    };

    self.loadAccounts = function () {
      var userId = self.selectedUserId();
      if (!userId) { return; }

      self.state.runAllowingNotFound(function () {
        // A manager must name whose accounts they want; unscoped listing is rejected.
        return BankService.listAccounts({ userId: Number(userId) });
      }).then(function (accounts) {
        self.accounts(accounts || []);
      });
    };

    self.selectedUserId.subscribe(function () {
      self.accounts([]);
      self.loadAccounts();
    });

    self.setStatus = function (account, status) {
      if (account.status === status) { return; }

      self.busyId(account.bankAccountId);
      self.state.run(function () {
        // Partial update: send only what is changing.
        return BankService.updateAccount(account.bankAccountId, { status: status });
      }).then(function (updated) {
        self.busyId(null);
        if (updated) {
          self.state.successMessage(
            "Account " + account.maskedAccountNumber + " set to " + status + "."
          );
          self.loadAccounts();
        }
      });
    };

    self.block = function (account) { self.setStatus(account, "BLOCKED"); };
    self.activate = function (account) { self.setStatus(account, "ACTIVE"); };

    /**
     * Opens a new account for the selected investor. Only userId (and, optionally, an
     * opening deposit) is sent - Bank Service fills in a house-bank name, IFSC and a
     * generated account number for anything left blank. This is how a manager finishes an
     * onboarding that reported the bank-account step as failed, or opens an additional
     * account by hand.
     */
    self.openAccount = function () {
      var userId = self.selectedUserId();
      if (!userId || self.isOpeningAccount()) { return; }

      var deposit = self.openingBalance().trim();
      self.isOpeningAccount(true);
      self.state.run(function () {
        return BankService.createAccount({
          userId: Number(userId),
          openingBalance: deposit.length > 0 ? Number(deposit) : undefined
        });
      }).then(function (account) {
        self.isOpeningAccount(false);
        if (account) {
          self.state.successMessage(
            "Opened " + account.bankName + " " + account.maskedAccountNumber + "."
          );
          self.openingBalance("");
          self.loadAccounts();
        }
      });
    };

    self.connected = function () {
      self.loadTeam();
    };
  }

  return BankAdminViewModel;
});
