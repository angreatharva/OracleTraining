import * as ko from "knockout";

/**
 * Products ViewModel — the investable product catalogue.
 *
 * Intended data source: `ProductService.listProducts({ status: "ACTIVE" })`.
 */
class ProductsViewModel {
  readonly heading: ko.Observable<string>;
  readonly isLoading: ko.Observable<boolean>;
  readonly errorMessage: ko.Observable<string>;

  constructor() {
    this.heading = ko.observable("Products");
    this.isLoading = ko.observable(false);
    this.errorMessage = ko.observable("");
  }

  connected(): void {
    // e.g. ProductService.listProducts({ status: "ACTIVE" }).then(...)
  }

  disconnected(): void {
    // no-op
  }

  transitionCompleted(): void {
    // no-op
  }
}

export = ProductsViewModel;
