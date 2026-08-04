/**
 * One shape for every backend error.
 *
 * The five services do not agree on an error body. Bank returns `{..., fieldErrors}`,
 * Portfolio returns `{..., validationErrors}`, and User, Product and Trading return a bare
 * `{timestamp, status, message}`. Rather than teach every form about all three, everything
 * is funnelled through here.
 */
define(["services/ApiClient"], function (ApiClient) {
  "use strict";

  var DEFAULT_MESSAGES = {
    0: "Could not reach the server. Is the API Gateway running on port 8081?",
    401: "Your session has expired. Please sign in again.",
    403: "You do not have permission to do that.",
    404: "That record no longer exists.",
    409: "That change conflicts with existing data.",
    500: "Something went wrong on the server."
  };

  return {
    /**
     * @param {*} error anything thrown by ApiClient
     * @returns {{status:number, message:string, fieldErrors:Object}}
     */
    normalize: function (error) {
      if (error instanceof ApiClient.ApiError) {
        var body = (error.body && typeof error.body === "object") ? error.body : {};

        // Read both map names, so the caller sees one shape whichever service answered.
        var fieldErrors = {};
        Object.keys(body.validationErrors || {}).forEach(function (k) {
          fieldErrors[k] = body.validationErrors[k];
        });
        Object.keys(body.fieldErrors || {}).forEach(function (k) {
          fieldErrors[k] = body.fieldErrors[k];
        });

        return {
          status: error.status,
          message: body.message || body.error || DEFAULT_MESSAGES[error.status] ||
            ("Request failed with status " + error.status),
          fieldErrors: fieldErrors
        };
      }

      // AbortError from the ApiClient timeout, or a network-level failure.
      if (error && error.name === "AbortError") {
        return { status: 0, message: "The request took too long and was cancelled.", fieldErrors: {} };
      }

      return {
        status: 0,
        message: (error && error.message) ? error.message : DEFAULT_MESSAGES[0],
        fieldErrors: {}
      };
    }
  };
});
