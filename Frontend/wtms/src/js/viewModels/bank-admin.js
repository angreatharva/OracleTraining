/**
 * Account admin - not implemented yet.
 *
 * Next step: change bank account status for a team member
 * Primary API: BankService.listAccounts, BankService.updateAccount
 *
 * Follow the pattern in viewModels/dashboard.js (investor) or viewModels/team.js (manager):
 * observables for data / isLoading / errorMessage, load in connected(), and normalise
 * failures through services/ApiErrorNormalizer.
 */
define(["knockout"], function (ko) {
  "use strict";

  function ScreenViewModel() {
    var self = this;
    self.title = ko.observable("Account admin");
    self.note = ko.observable("change bank account status for a team member");
    self.api = ko.observable("BankService.listAccounts, BankService.updateAccount");

    self.connected = function () {
      // Load data here.
    };
  }

  return ScreenViewModel;
});
