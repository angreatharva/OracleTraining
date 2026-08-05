/**
 * Lets a screen navigate without reaching back into the root view model.
 *
 * The CoreRouter instance is owned by appController and is rebuilt whenever a different role
 * signs in, so screens must not capture it. They ask this module at the moment they navigate.
 */
define([], function () {
  "use strict";

  var router = null;

  return {
    /** Called by appController each time it builds a router. */
    setRouter: function (instance) {
      router = instance;
    },

    /**
     * @param {string} path a route path from config/routes
     * @param {Object} [params] route parameters, surfaced in the URL by UrlParamAdapter
     * @returns {Promise}
     */
    go: function (path, params) {
      if (!router) {
        return Promise.reject(new Error("No router is active"));
      }
      return router.go({ path: path, params: params || {} });
    }
  };
});
