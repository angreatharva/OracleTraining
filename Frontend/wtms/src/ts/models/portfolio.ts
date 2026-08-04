import { HoldingStatus, PortfolioAccountStatus } from "./enums";

/** Portfolio Service — `PortfolioAccountResponse`. One portfolio account per user. */
export interface PortfolioAccount {
  portfolioAccountId: number;
  userId: number;
  accountStatus: PortfolioAccountStatus;
  openedDate: string | null;
  closedDate: string | null;
  createdAt: string;
  updatedAt: string;
}

/** Portfolio Service — `PortfolioHoldingResponse`. */
export interface PortfolioHolding {
  holdingId: number;
  portfolioAccountId: number;
  productId: number;
  quantity: number;
  averageCost: number;
  marketValue: number;
  unrealizedGainLoss: number;
  holdingStatus: HoldingStatus;
  lastValuedAt: string | null;
  createdAt: string;
  updatedAt: string;
}

/**
 * Portfolio Service — `PortfolioSummaryResponse`.
 * Computed from the *stored* holding `marketValue`; it does not refresh product quotes.
 * Use `InvestmentOverview` from Trading Service when live prices matter.
 */
export interface PortfolioSummary {
  portfolioAccount: PortfolioAccount;
  holdings: PortfolioHolding[];
  totalCost: number;
  marketValue: number;
  unrealizedGainLoss: number;
}
