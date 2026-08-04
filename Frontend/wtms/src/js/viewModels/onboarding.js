/**
 * Onboard investor - not implemented yet.
 *
 * Next step: create user, then user-detail, then portfolio account - three sequential non-atomic calls, so report which succeeded on partial failure
 * Primary API: UserService.createUser, UserService.createUserDetail, PortfolioService.createAccount
 *
 * Follow the pattern in viewModels/dashboard.js (investor) or viewModels/team.js (manager):
 * observables for data / isLoading / errorMessage, load in connected(), and normalise
 * failures through services/ApiErrorNormalizer.
 */
define(["knockout"], function (ko) {
  "use strict";

  function ScreenViewModel() {
    var self = this;
    self.title = ko.observable("Onboard investor");
    self.note = ko.observable("create user, then user-detail, then portfolio account - three sequential non-atomic calls, so report which succeeded on partial failure");
    self.api = ko.observable("UserService.createUser, UserService.createUserDetail, PortfolioService.createAccount");

    self.connected = function () {
      // Load data here.
    };
  }

  return ScreenViewModel;
});
