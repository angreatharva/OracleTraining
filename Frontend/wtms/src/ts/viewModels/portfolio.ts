import * as ko from "knockout";

/**
 * Portfolio ViewModel.
 *
 * Intended data source: `PortfolioService.getSummary(portfolioAccountId)` for the account
 * and its holdings. Note this returns stored valuations, not refreshed quotes.
 */
class PortfolioViewModel {
  readonly heading: ko.Observable<string>;
  readonly isLoading: ko.Observable<boolean>;
  readonly errorMessage: ko.Observable<string>;

  constructor() {
    this.heading = ko.observable("Portfolio");
    this.isLoading = ko.observable(false);
    this.errorMessage = ko.observable("");
  }

  connected(): void {
    // e.g. PortfolioService.getAccountByUser(userId).then(...)
  }

  disconnected(): void {
    // no-op
  }

  transitionCompleted(): void {
    // no-op
  }
}

export = PortfolioViewModel;
