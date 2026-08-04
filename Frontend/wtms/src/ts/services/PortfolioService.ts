import { ApiClient } from "./ApiClient";
import { PortfolioAccount, PortfolioHolding, PortfolioSummary } from "../models/portfolio";
import { HoldingStatus, PortfolioAccountStatus } from "../models/enums";

/** Portfolio Service (`PORTFOLIO-SERVICE`, port 8084) — accounts, holdings, and valuation. */
export const PortfolioService = {
  listAccounts: (): Promise<PortfolioAccount[]> => ApiClient.get("portfolio", "/api/portfolios"),

  getAccount: (portfolioAccountId: number): Promise<PortfolioAccount> =>
    ApiClient.get("portfolio", `/api/portfolios/${portfolioAccountId}`),

  getAccountByUser: (userId: number): Promise<PortfolioAccount> =>
    ApiClient.get("portfolio", `/api/portfolios/by-user/${userId}`),

  updateAccountStatus: (
    portfolioAccountId: number,
    accountStatus: PortfolioAccountStatus
  ): Promise<PortfolioAccount> =>
    ApiClient.patch("portfolio", `/api/portfolios/${portfolioAccountId}/status`, { accountStatus }),

  listHoldings: (portfolioAccountId: number, status?: HoldingStatus): Promise<PortfolioHolding[]> =>
    ApiClient.get("portfolio", `/api/portfolios/${portfolioAccountId}/holdings`, { status }),

  getHolding: (holdingId: number): Promise<PortfolioHolding> =>
    ApiClient.get("portfolio", `/api/portfolios/holdings/${holdingId}`),

  /** Uses stored valuations, not live quotes — see `TradingService.getInvestmentOverview`. */
  getSummary: (portfolioAccountId: number): Promise<PortfolioSummary> =>
    ApiClient.get("portfolio", `/api/portfolios/${portfolioAccountId}/summary`)

  // The /api/portfolios/internal/trades* endpoints are Trading-to-Portfolio commands.
  // They are routable but must never be called from the UI.
};
