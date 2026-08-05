/**
 * Sign-in screen.
 *
 * Receives `onSignedIn` from the shell via oj-module params, and calls it once a session
 * exists so the shell can build the role-appropriate router.
 */
define([
  "knockout",
  "ojs/ojarraydataprovider",
  "utils/derived",
  "services/AuthService",
  "services/ApiErrorNormalizer"
], function (ko, ArrayDataProvider, derived, AuthService, ApiErrorNormalizer) {
  "use strict";

  /**
   * @param {{onSignedIn: Function}} params passed straight in by oj-module - JET invokes a
   *        constructor view model with the params object itself, not with a context wrapper.
   */
  function LoginViewModel(params) {
    var self = this;

    var onSignedIn = (params && params.onSignedIn) || function () {
      // Fallback: reloading works because the session is already in sessionStorage and the
      // shell rebuilds from it on start. Slower and it flashes, so the callback is preferred.
      window.location.reload();
    };

    self.email = ko.observable("");
    self.password = ko.observable("");
    self.errorMessage = ko.observable("");
    self.isSubmitting = ko.observable(false);

    /** The same errorMessage, shaped for oj-c-message-banner. Presentation only. */
    self.messages = derived.array(function () {
      return self.errorMessage()
        ? [{
            id: "error",
            severity: "error",
            summary: self.errorMessage(),
            closeAffordance: "off"
          }]
        : [];
    });
    self.messagesDP = new ArrayDataProvider(self.messages, { keyAttributes: "id" });

    self.canSubmit = ko.pureComputed(function () {
      return !self.isSubmitting() &&
        self.email().trim().length > 0 &&
        self.password().length > 0;
    });

    self.submit = function () {
      if (!self.canSubmit()) {
        return;
      }
      self.isSubmitting(true);
      self.errorMessage("");

      AuthService.login(self.email().trim(), self.password())
        .then(function () {
          // Do not keep the plaintext password in an observable once it has been used.
          self.password("");
          self.isSubmitting(false);
          onSignedIn();
        })
        .catch(function (error) {
          var normalized = ApiErrorNormalizer.normalize(error);
          // The backend answers identically for an unknown email and a wrong password on
          // purpose, so the UI must not invent a more specific message either.
          self.errorMessage(
            normalized.status === 401 ? "Invalid email or password." : normalized.message
          );
          self.isSubmitting(false);
        });
    };

    self.disconnected = function () {
      self.password("");
    };
  }

  return LoginViewModel;
});
