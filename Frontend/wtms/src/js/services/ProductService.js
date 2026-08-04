/** Product Service (PRODUCT-SERVICE, port 8086) - the investable product catalogue. */
define(["services/ApiClient"], function (ApiClient) {
  "use strict";

  return {
    /** Readable by anyone signed in; an investor cannot trade what they cannot see. */
    listProducts: function (filter) {
      return ApiClient.get("product", "/api/products", filter);
    },

    /** Authoritative quote - Trading executes at this `currentPrice`, not at what the UI shows. */
    getProduct: function (productId) {
      return ApiClient.get("product", "/api/products/" + productId);
    },

    listProductTypes: function (filter) {
      return ApiClient.get("product", "/api/product-types", filter);
    },

    /** MANAGER only. */
    createProduct: function (request) {
      return ApiClient.post("product", "/api/products", request);
    },

    /** MANAGER only. Full replacement - every required field must be resent. */
    updateProduct: function (productId, request) {
      return ApiClient.put("product", "/api/products/" + productId, request);
    },

    /** MANAGER only. */
    createProductType: function (request) {
      return ApiClient.post("product", "/api/product-types", request);
    }
  };
});
