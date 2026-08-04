/**
 * Profile - not implemented yet.
 *
 * Next step: show own user and risk profile, and allow a password change
 * Primary API: AuthService.me, UserService.getUserDetailByUserId, AuthService.changePassword
 *
 * Follow the pattern in viewModels/dashboard.js (investor) or viewModels/team.js (manager):
 * observables for data / isLoading / errorMessage, load in connected(), and normalise
 * failures through services/ApiErrorNormalizer.
 */
define(["knockout"], function (ko) {
  "use strict";

  function ScreenViewModel() {
    var self = this;
    self.title = ko.observable("Profile");
    self.note = ko.observable("show own user and risk profile, and allow a password change");
    self.api = ko.observable("AuthService.me, UserService.getUserDetailByUserId, AuthService.changePassword");

    self.connected = function () {
      // Load data here.
    };
  }

  return ScreenViewModel;
});
