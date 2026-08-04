/**
 * Product catalogue - not implemented yet.
 *
 * Next step: product type and product CRUD, including price updates
 * Primary API: ProductService.listProducts, createProduct, updateProduct
 *
 * Follow the pattern in viewModels/dashboard.js (investor) or viewModels/team.js (manager):
 * observables for data / isLoading / errorMessage, load in connected(), and normalise
 * failures through services/ApiErrorNormalizer.
 */
define(["knockout"], function (ko) {
  "use strict";

  function ScreenViewModel() {
    var self = this;
    self.title = ko.observable("Product catalogue");
    self.note = ko.observable("product type and product CRUD, including price updates");
    self.api = ko.observable("ProductService.listProducts, createProduct, updateProduct");

    self.connected = function () {
      // Load data here.
    };
  }

  return ScreenViewModel;
});
