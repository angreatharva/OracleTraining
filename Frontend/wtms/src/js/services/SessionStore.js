/**
 * Holds the signed-in session.
 *
 * Uses `sessionStorage` rather than `localStorage` deliberately: the token is a bearer
 * credential, so scoping it to the tab means closing the tab ends the session instead of
 * leaving a usable token on disk.
 *
 * Nothing here is a security control. The token is signed and every backend service
 * verifies it independently, so tampering with what is stored below only breaks this
 * client's own UI - it cannot grant access.
 */
define([], function () {
  "use strict";

  var STORAGE_KEY = "wealthtrack.session";
  var cached;
  var loaded = false;

  function read() {
    if (loaded) {
      return cached;
    }
    try {
      var raw = window.sessionStorage.getItem(STORAGE_KEY);
      cached = raw ? JSON.parse(raw) : null;
    } catch (e) {
      // Corrupt or unreadable storage should sign the user out, not crash the app.
      cached = null;
    }
    loaded = true;
    return cached;
  }

  return {
    /** @param {{token:string, expiresAt:string, roleName:string, user:Object}} session */
    set: function (session) {
      cached = session;
      loaded = true;
      window.sessionStorage.setItem(STORAGE_KEY, JSON.stringify(session));
    },

    clear: function () {
      cached = null;
      loaded = true;
      window.sessionStorage.removeItem(STORAGE_KEY);
    },

    get: function () {
      return read();
    },

    token: function () {
      var s = read();
      return s ? s.token : null;
    },

    user: function () {
      var s = read();
      return s ? s.user : null;
    },

    userId: function () {
      var s = read();
      return s ? s.user.userId : null;
    },

    role: function () {
      var s = read();
      return s ? s.roleName : null;
    },

    isManager: function () {
      var s = read();
      return !!s && s.roleName === "MANAGER";
    },

    isInvestor: function () {
      var s = read();
      return !!s && s.roleName === "INVESTOR";
    },

    /**
     * Client-side expiry check, so the UI can return to login before making a request that
     * is certain to fail. The backend enforces expiry regardless.
     */
    isExpired: function () {
      var s = read();
      if (!s) {
        return true;
      }
      var expiresAt = Date.parse(s.expiresAt);
      return isNaN(expiresAt) ? false : expiresAt <= Date.now();
    },

    isAuthenticated: function () {
      var s = read();
      if (!s) {
        return false;
      }
      var expiresAt = Date.parse(s.expiresAt);
      return isNaN(expiresAt) ? true : expiresAt > Date.now();
    }
  };
});
