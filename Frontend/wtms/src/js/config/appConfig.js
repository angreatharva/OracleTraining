/**
 * Application-wide configuration.
 *
 * All backend traffic goes through the WealthTrack API Gateway (default port 8081), which
 * strips the first path segment before forwarding. So `/trading/api/trade-transactions`
 * reaches Trading Service as `/api/trade-transactions`.
 */
define([], function () {
  "use strict";

  var appConfig = {
    appName: "WealthTrack",

    /** Base URL of the API Gateway. */
    gatewayUrl: "http://localhost:8081",

    /** Gateway route prefixes, one per microservice. */
    servicePrefix: {
      user: "/user",
      bank: "/bank",
      portfolio: "/portfolio",
      trading: "/trading",
      product: "/product"
    },

    /** Milliseconds before an outbound request is aborted. */
    requestTimeoutMs: 15000,

    /**
     * Builds a full gateway URL for a service-relative path such as `/api/users`.
     * @param {string} service one of the keys of servicePrefix
     * @param {string} path service-relative path, starting with a slash
     * @returns {string}
     */
    serviceUrl: function (service, path) {
      var prefix = appConfig.servicePrefix[service];
      if (prefix === undefined) {
        throw new Error("Unknown service: " + service);
      }
      return appConfig.gatewayUrl + prefix + path;
    }
  };

  return appConfig;
});
