import * as ko from "knockout";

/**
 * Trades ViewModel — trade history and order entry.
 *
 * Intended data sources: `TradingService.listTrades(...)` for history and
 * `TradingService.submitTrade(...)` for BUY/SELL. A submitted trade may come back with
 * status `FAILED`; treat that as a normal outcome to render, not an exception.
 */
class TradesViewModel {
  readonly heading: ko.Observable<string>;
  readonly isLoading: ko.Observable<boolean>;
  readonly isSubmitting: ko.Observable<boolean>;
  readonly errorMessage: ko.Observable<string>;

  constructor() {
    this.heading = ko.observable("Trades");
    this.isLoading = ko.observable(false);
    // Guards against double submission: the backend has no idempotency keys.
    this.isSubmitting = ko.observable(false);
    this.errorMessage = ko.observable("");
  }

  connected(): void {
    // e.g. TradingService.listTrades({ portfolioAccountId }).then(...)
  }

  disconnected(): void {
    // no-op
  }

  transitionCompleted(): void {
    // no-op
  }
}

export = TradesViewModel;
