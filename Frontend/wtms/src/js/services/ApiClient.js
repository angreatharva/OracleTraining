/**
 * Thin `fetch` wrapper for the WealthTrack API Gateway.
 *
 * Deliberately does not retry: no backend endpoint is idempotent, so a retried POST can
 * debit money or apply a holding change twice.
 */
define(["config/appConfig", "services/SessionStore"], function (appConfig, SessionStore) {
  "use strict";

  /**
   * Error thrown for any non-2xx response.
   * @constructor
   * @param {number} status
   * @param {string} message
   * @param {*} body parsed error payload, when the server sent one
   */
  function ApiError(status, message, body) {
    this.name = "ApiError";
    this.status = status;
    this.message = message;
    this.body = body;
    // Captured so the stack points at the caller, not at this constructor.
    if (Error.captureStackTrace) {
      Error.captureStackTrace(this, ApiError);
    }
  }
  ApiError.prototype = Object.create(Error.prototype);
  ApiError.prototype.constructor = ApiError;

  /** Called when the server rejects our token; wired up by appController. */
  var unauthorizedHandler = null;

  function withQuery(url, query) {
    if (!query) {
      return url;
    }
    var params = new URLSearchParams();
    Object.keys(query).forEach(function (key) {
      var value = query[key];
      if (value !== null && value !== undefined && value !== "") {
        params.append(key, String(value));
      }
    });
    var queryString = params.toString();
    return queryString ? url + "?" + queryString : url;
  }

  function parseBody(response) {
    if (response.status === 204) {
      return Promise.resolve(null);
    }
    var contentType = response.headers.get("content-type") || "";
    if (contentType.indexOf("application/json") !== -1) {
      return response.json();
    }
    return response.text().then(function (text) {
      return text.length ? text : null;
    });
  }

  function request(method, service, path, options) {
    options = options || {};

    var controller = new AbortController();
    var timeoutId = window.setTimeout(function () {
      controller.abort();
    }, appConfig.requestTimeoutMs);

    var headers = { Accept: "application/json" };
    if (options.body !== undefined) {
      headers["Content-Type"] = "application/json";
    }
    if (!options.anonymous) {
      var token = SessionStore.token();
      if (token) {
        headers.Authorization = "Bearer " + token;
      }
    }

    return fetch(withQuery(appConfig.serviceUrl(service, path), options.query), {
      method: method,
      headers: headers,
      body: options.body === undefined ? undefined : JSON.stringify(options.body),
      signal: controller.signal
    })
      .then(function (response) {
        return parseBody(response).then(function (body) {
          if (!response.ok) {
            // 401 means the token is missing, expired or rejected -> the session is over.
            // 403 is a real authorization decision and must NOT sign the user out: they are
            // signed in correctly, they simply may not see that particular record.
            if (response.status === 401 && !options.anonymous) {
              SessionStore.clear();
              if (unauthorizedHandler) {
                unauthorizedHandler();
              }
            }
            throw new ApiError(
              response.status,
              method + " " + path + " failed with " + response.status,
              body
            );
          }
          return body;
        });
      })
      .then(
        function (value) {
          window.clearTimeout(timeoutId);
          return value;
        },
        function (error) {
          window.clearTimeout(timeoutId);
          throw error;
        }
      );
  }

  return {
    ApiError: ApiError,

    /** @param {Function} handler invoked when a request is rejected with 401 */
    onUnauthorized: function (handler) {
      unauthorizedHandler = handler;
    },

    get: function (service, path, query) {
      return request("GET", service, path, { query: query });
    },

    post: function (service, path, body) {
      return request("POST", service, path, { body: body === undefined ? {} : body });
    },

    put: function (service, path, body) {
      return request("PUT", service, path, { body: body === undefined ? {} : body });
    },

    patch: function (service, path, body) {
      return request("PATCH", service, path, { body: body === undefined ? {} : body });
    },

    del: function (service, path) {
      return request("DELETE", service, path, {});
    },

    /** Used only by login: sends no token, and a 401 must not trigger the logout handler. */
    postAnonymous: function (service, path, body) {
      return request("POST", service, path, { body: body, anonymous: true });
    }
  };
});
