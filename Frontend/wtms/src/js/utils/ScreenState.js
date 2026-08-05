/**
 * The loading / error / empty plumbing every screen repeats.
 *
 * Without this each view model hand-rolls the same four observables and the same
 * try/catch-and-normalise dance. Compose one of these instead:
 *
 *     var state = ScreenState.create();
 *     state.run(function () { return SomeService.list(); })
 *          .then(function (rows) { if (rows) { self.rows(rows); } });
 */
define(["knockout", "services/ApiErrorNormalizer"], function (ko, ApiErrorNormalizer) {
  "use strict";

  return {
    create: function () {
      var state = {};

      state.isLoading = ko.observable(false);
      state.errorMessage = ko.observable("");
      state.successMessage = ko.observable("");
      state.fieldErrors = ko.observable({});

      state.clearMessages = function () {
        state.errorMessage("");
        state.successMessage("");
        state.fieldErrors({});
      };

      /** Per-field message from the server, for form validation display. */
      state.fieldError = function (fieldName) {
        return state.fieldErrors()[fieldName] || "";
      };

      /**
       * Runs a promise-returning action with loading and error handling attached.
       *
       * Always resolves - never rejects - so callers do not need their own catch. On a
       * handled failure it resolves with `undefined` and the message is already on
       * `errorMessage`, so `if (result)` is the success test.
       *
       * @param {Function} action returns a Promise
       * @param {{quiet: boolean}} [options] quiet skips the loading flag, for background refreshes
       * @returns {Promise<*|undefined>}
       */
      state.run = function (action, options) {
        var quiet = !!(options && options.quiet);
        if (!quiet) {
          state.isLoading(true);
        }
        state.clearMessages();

        return action().then(
          function (value) {
            state.isLoading(false);
            return value;
          },
          function (error) {
            var normalized = ApiErrorNormalizer.normalize(error);
            state.errorMessage(normalized.message);
            state.fieldErrors(normalized.fieldErrors);
            state.isLoading(false);
            return undefined;
          }
        );
      };

      /**
       * Same as run, but treats 404 as "nothing here yet" rather than an error - several
       * screens legitimately hit a user who has no portfolio account or no records.
       * @returns {Promise<*|undefined>}
       */
      state.runAllowingNotFound = function (action) {
        state.isLoading(true);
        state.clearMessages();

        return action().then(
          function (value) {
            state.isLoading(false);
            return value;
          },
          function (error) {
            var normalized = ApiErrorNormalizer.normalize(error);
            if (normalized.status !== 404) {
              state.errorMessage(normalized.message);
              state.fieldErrors(normalized.fieldErrors);
            }
            state.isLoading(false);
            return undefined;
          }
        );
      };

      return state;
    }
  };
});
