/**
 * Own profile and password change. Reachable by both roles.
 *
 * The risk/KYC profile (user-detail) is a separate record and may not exist, so a 404 there
 * is normal rather than an error.
 */
define([
  "knockout",
  "services/AuthService",
  "services/UserService",
  "services/SessionStore",
  "utils/ScreenState",
  "utils/format"
], function (ko, AuthService, UserService, SessionStore, ScreenState, format) {
  "use strict";

  function ProfileViewModel() {
    var self = this;

    self.state = ScreenState.create();
    self.passwordState = ScreenState.create();
    self.format = format;

    self.user = ko.observable(null);
    self.detail = ko.observable(null);
    self.roleName = ko.observable(SessionStore.role() || "");

    self.currentPassword = ko.observable("");
    self.newPassword = ko.observable("");
    self.confirmPassword = ko.observable("");
    self.isChanging = ko.observable(false);

    // ---------------------------------------------------------------------
    // Presentation-only derivations over the two records already loaded.
    // ---------------------------------------------------------------------

    self.initials = ko.pureComputed(function () {
      var user = self.user();
      return user ? format.initials(user.fullName) : "?";
    });

    /**
     * Risk level as a 5-point rating, so the gauge reads left-to-right as
     * conservative-to-aggressive. LOW/MODERATE/HIGH are the only values the backend uses.
     */
    self.riskRating = ko.pureComputed(function () {
      var detail = self.detail();
      switch (detail && detail.riskLevel) {
        case "LOW": return 1;
        case "MODERATE": return 3;
        case "HIGH": return 5;
        default: return 0;
      }
    });

    self.hasRiskScore = ko.pureComputed(function () {
      var detail = self.detail();
      return !!detail && detail.riskScore !== null && detail.riskScore !== undefined;
    });

    self.riskScoreValue = ko.pureComputed(function () {
      var detail = self.detail();
      return self.hasRiskScore() ? Number(detail.riskScore) : 0;
    });

    self.passwordMismatch = ko.pureComputed(function () {
      return self.confirmPassword().length > 0 && self.newPassword() !== self.confirmPassword();
    });

    self.canChangePassword = ko.pureComputed(function () {
      return !self.isChanging() &&
        self.currentPassword().length > 0 &&
        self.newPassword().length >= 8 &&
        !self.passwordMismatch();
    });

    self.load = function () {
      self.state.run(function () {
        return AuthService.me();
      }).then(function (user) {
        if (!user) { return undefined; }
        self.user(user);

        // The risk/KYC profile is optional - absence is not an error.
        return self.state.runAllowingNotFound(function () {
          return UserService.getUserDetailByUserId(user.userId);
        }).then(function (detail) {
          self.detail(detail || null);
          return undefined;
        });
      });
    };

    self.changePassword = function () {
      if (!self.canChangePassword()) { return; }

      self.isChanging(true);
      self.passwordState.run(function () {
        return AuthService.changePassword(self.currentPassword(), self.newPassword());
      }).then(function (result) {
        self.isChanging(false);
        // changePassword returns 204, so a successful call resolves with null - which is
        // indistinguishable from the handled-error undefined. Use the error message instead.
        if (!self.passwordState.errorMessage()) {
          self.passwordState.successMessage("Password changed. It applies the next time you sign in.");
          self.currentPassword("");
          self.newPassword("");
          self.confirmPassword("");
        }
      });
    };

    self.connected = function () {
      self.load();
    };

    self.disconnected = function () {
      self.currentPassword("");
      self.newPassword("");
      self.confirmPassword("");
    };
  }

  return ProfileViewModel;
});
