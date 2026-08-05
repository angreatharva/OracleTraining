/**
 * Product catalogue: list, create and update products.
 *
 * Write access here is effectively write access to every trade's execution price - Trading
 * replaces whatever the client sends with the `currentPrice` read from this catalogue. That
 * is why the backend restricts these endpoints to MANAGER, and why the price field is
 * called out in the view.
 *
 * PUT is a full replacement, not a patch: every required field has to be resent, so editing
 * loads the existing product into the form first.
 */
define([
  "knockout",
  "ojs/ojarraydataprovider",
  "services/ProductService",
  "utils/ScreenState",
  "utils/format",
  "models/enums"
], function (ko, ArrayDataProvider, ProductService, ScreenState, format, enums) {
  "use strict";

  function CatalogViewModel() {
    var self = this;

    self.state = ScreenState.create();
    self.formState = ScreenState.create();
    self.format = format;

    self.products = ko.observableArray([]);
    self.productTypes = ko.observableArray([]);
    self.productsDP = new ArrayDataProvider(self.products, { keyAttributes: "productId" });

    self.riskOptions = enums.RISK_CATEGORY;
    self.priceMethodOptions = enums.PRICE_METHOD;
    self.statusOptions = enums.PRODUCT_STATUS;

    /** null = creating; otherwise the productId being replaced. */
    self.editingId = ko.observable(null);
    self.isSaving = ko.observable(false);
    self.showForm = ko.observable(false);

    self.form = {
      productTypeId: ko.observable(null),
      productName: ko.observable(""),
      basePrice: ko.observable(1),
      currentPrice: ko.observable(1),
      minimumInvestment: ko.observable(1),
      riskCategory: ko.observable("MODERATE"),
      priceMethod: ko.observable("MARKET"),
      status: ko.observable("ACTIVE")
    };

    self.hasProducts = ko.pureComputed(function () {
      return !self.state.isLoading() && self.products().length > 0;
    });
    self.formTitle = ko.pureComputed(function () {
      return self.editingId() === null ? "New product" : "Edit product #" + self.editingId();
    });
    self.canSave = ko.pureComputed(function () {
      return !self.isSaving() &&
        !!self.form.productTypeId() &&
        self.form.productName().trim().length > 0 &&
        Number(self.form.currentPrice()) >= 0.01;
    });

    self.load = function () {
      self.state.run(function () {
        return Promise.all([
          ProductService.listProducts(),
          ProductService.listProductTypes()
        ]);
      }).then(function (results) {
        if (!results) { return; }
        self.products(results[0] || []);
        self.productTypes(results[1] || []);
        if (!self.form.productTypeId() && self.productTypes().length > 0) {
          self.form.productTypeId(self.productTypes()[0].productTypeId);
        }
      });
    };

    self.startCreate = function () {
      self.editingId(null);
      self.form.productName("");
      self.form.basePrice(1);
      self.form.currentPrice(1);
      self.form.minimumInvestment(1);
      self.form.riskCategory("MODERATE");
      self.form.priceMethod("MARKET");
      self.form.status("ACTIVE");
      if (self.productTypes().length > 0) {
        self.form.productTypeId(self.productTypes()[0].productTypeId);
      }
      self.formState.clearMessages();
      self.showForm(true);
    };

    /** PUT replaces the whole record, so prefill every field from the existing product. */
    self.startEdit = function (product) {
      self.editingId(product.productId);
      self.form.productTypeId(product.productTypeId);
      self.form.productName(product.productName);
      self.form.basePrice(product.basePrice);
      self.form.currentPrice(product.currentPrice);
      self.form.minimumInvestment(product.minimumInvestment);
      self.form.riskCategory(product.riskCategory);
      self.form.priceMethod(product.priceMethod);
      self.form.status(product.status);
      self.formState.clearMessages();
      self.showForm(true);
    };

    self.cancel = function () {
      self.showForm(false);
      self.formState.clearMessages();
    };

    self.save = function () {
      if (!self.canSave()) { return; }

      var payload = {
        productTypeId: Number(self.form.productTypeId()),
        productName: self.form.productName().trim(),
        basePrice: Number(self.form.basePrice()),
        currentPrice: Number(self.form.currentPrice()),
        minimumInvestment: Number(self.form.minimumInvestment()),
        riskCategory: self.form.riskCategory(),
        priceMethod: self.form.priceMethod(),
        status: self.form.status()
      };

      self.isSaving(true);
      var editingId = self.editingId();

      self.formState.run(function () {
        return editingId === null
          ? ProductService.createProduct(payload)
          : ProductService.updateProduct(editingId, payload);
      }).then(function (saved) {
        self.isSaving(false);
        if (saved) {
          self.showForm(false);
          self.state.successMessage(
            editingId === null ? "Product created." : "Product updated."
          );
          self.load();
        }
      });
    };

    self.connected = function () {
      self.load();
    };
  }

  return CatalogViewModel;
});
