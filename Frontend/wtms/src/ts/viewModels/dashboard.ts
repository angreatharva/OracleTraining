import * as ko from "knockout";

/**
 * Dashboard ViewModel.
 *
 * Intended data source: `TradingService.getInvestmentOverview(userId)`, which already
 * aggregates holdings, completed trades, and live product prices in one call.
 */
class DashboardViewModel {
  readonly heading: ko.Observable<string>;
  readonly isLoading: ko.Observable<boolean>;
  readonly errorMessage: ko.Observable<string>;

  constructor() {
    this.heading = ko.observable("Dashboard");
    this.isLoading = ko.observable(false);
    this.errorMessage = ko.observable("");
  }

  /** Called by oj-module each time the view is attached. Load view data here. */
  connected(): void {
    // e.g. this.load(TradingService.getInvestmentOverview(currentUserId));
  }

  /** Called when the view is detached. Cancel in-flight work and subscriptions here. */
  disconnected(): void {
    // no-op
  }

  transitionCompleted(): void {
    // no-op
  }
}

export = DashboardViewModel;
