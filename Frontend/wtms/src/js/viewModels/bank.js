/**
 * Bank accounts - not implemented yet.
 *
 * Next step: list accounts and let the user set a primary
 * Primary API: BankService.listAccounts, BankService.makePrimary
 *
 * Follow the pattern in viewModels/dashboard.js (investor) or viewModels/team.js (manager):
 * observables for data / isLoading / errorMessage, load in connected(), and normalise
 * failures through services/ApiErrorNormalizer.
 */
define(["knockout"], function (ko) {
  "use strict";

  function ScreenViewModel() {
    var self = this;
    self.title = ko.observable("Bank accounts");
    self.note = ko.observable("list accounts and let the user set a primary");
    self.api = ko.observable("BankService.listAccounts, BankService.makePrimary");

    self.connected = function () {
      // Load data here.
    };
  }

  return ScreenViewModel;
});
