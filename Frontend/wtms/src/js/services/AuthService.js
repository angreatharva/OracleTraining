/**
 * Authentication against User Service (`/user/api/auth/**` through the gateway).
 * `POST /api/auth/login` is the only unauthenticated endpoint in the whole system.
 */
define(["services/ApiClient", "services/SessionStore"], function (ApiClient, SessionStore) {
  "use strict";

  return {
    /**
     * Signs in and stores the session.
     * Rejects with ApiError status 401 on bad credentials - the backend answers identically
     * for an unknown email and a wrong password, so do not report them differently.
     */
    login: function (email, password) {
      return ApiClient.postAnonymous("user", "/api/auth/login", {
        email: email,
        password: password
      }).then(function (response) {
        SessionStore.set({
          token: response.token,
          expiresAt: response.expiresAt,
          roleName: response.roleName,
          user: response.user
        });
        return response;
      });
    },

    /**
     * Clears the local session. There is no server-side logout: nothing tracks issued
     * tokens, so the token stays technically valid until it expires. Short expiry is what
     * limits the window - do not present this to users as revocation.
     */
    logout: function () {
      SessionStore.clear();
    },

    /** Re-reads the signed-in user, e.g. to confirm a stored token is still accepted. */
    me: function () {
      return ApiClient.get("user", "/api/auth/me");
    },

    changePassword: function (currentPassword, newPassword) {
      return ApiClient.post("user", "/api/auth/change-password", {
        currentPassword: currentPassword,
        newPassword: newPassword
      });
    }
  };
});
