/**
 * Bank account administration for the manager's team.
 *
 * Note PUT /api/bank-accounts/{id} is a partial update despite being a PUT - only non-null
 * fields are applied. So changing status sends just `status` and leaves everything else
 * untouched, which is the opposite of how the product PUT behaves.
 */
define([
  "knockout",
  "services/UserService",
  "services/BankService",
  "services/SessionStore",
  "utils/ScreenState",
  "utils/format",
  "models/enums"
], function (ko, UserService, BankService, SessionStore, ScreenState, format, enums) {
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

    self.connected = function () {
      self.loadTeam();
    };
  }

  return BankAdminViewModel;
});
