import { ApiClient, ApiError } from "./ApiClient";
import {
  CreateTradeRequest,
  InvestmentOverview,
  PortfolioStatement,
  TradeTransaction
} from "../models/trading";
import { TransactionStatus, TransactionType } from "../models/enums";

/**
 * Trading Service (`TRADING-SERVICE`, port 8085) — BUY/SELL orchestration,
 * statements, and the live investment overview.
 */
export const TradingService = {
  /**
   * Submits a BUY or SELL. The backend runs a synchronous saga across Product, Portfolio,
   * and Bank, then persists the outcome.
   *
   * A rejected trade is not an exception in business terms: the backend answers HTTP 422
   * with a `FAILED` TradeTransaction rather than the shared error schema, so it is unwrapped
   * and returned here. `failureReason` is only readable on this response — a later GET
   * returns it as null.
   *
   * Never retry this call: the backend has no idempotency keys, so a retry can debit twice.
   */
  submitTrade: async (request: CreateTradeRequest): Promise<TradeTransaction> => {
    try {
      return await ApiClient.post<TradeTransaction>("trading", "/api/trade-transactions", request);
    } catch (error) {
      if (error instanceof ApiError && error.status === 422 && error.body) {
        return error.body as TradeTransaction;
      }
      throw error;
    }
  },

  listTrades: (filter?: {
    portfolioAccountId?: number;
    transactionStatus?: TransactionStatus;
    transactionType?: TransactionType;
    from?: string;
    to?: string;
  }): Promise<TradeTransaction[]> => ApiClient.get("trading", "/api/trade-transactions", filter),

  getTrade: (transactionId: number): Promise<TradeTransaction> =>
    ApiClient.get("trading", `/api/trade-transactions/${transactionId}`),

  listStatements: (filter?: {
    portfolioAccountId?: number;
    status?: string;
    from?: string;
    to?: string;
  }): Promise<PortfolioStatement[]> => ApiClient.get("trading", "/api/portfolio-statements", filter),

  getStatement: (statementId: number): Promise<PortfolioStatement> =>
    ApiClient.get("trading", `/api/portfolio-statements/${statementId}`),

  /** Positions with live Product prices — the primary "what do I own and what is it worth" view. */
  getInvestmentOverview: (userId: number): Promise<InvestmentOverview> =>
    ApiClient.get("trading", `/api/investment-overview/users/${userId}`)
};
