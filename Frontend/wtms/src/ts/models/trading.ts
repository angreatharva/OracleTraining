import { TransactionStatus, TransactionType } from "./enums";

/**
 * Trading Service — `TradeTransactionResponse`.
 * `failureReason` is populated only on the immediate 422 response of a failed trade;
 * a later GET of the same transaction returns it as null.
 */
export interface TradeTransaction {
  transactionId: number;
  portfolioAccountId: number;
  holdingId: number;
  productId: number;
  transactionType: TransactionType;
  quantity: number;
  unitPrice: number;
  totalAmount: number;
  transactionStatus: TransactionStatus;
  transactionDate: string;
  failureReason: string | null;
}

/**
 * Trading Service — `CreateTradeTransactionRequest`.
 * The submitted `unitPrice` is *not* trusted: Trading replaces it with the current price
 * from Product Service before computing `totalAmount`.
 */
export interface CreateTradeRequest {
  portfolioAccountId: number;
  holdingId: number;
  productId: number;
  bankAccountId: number;
  transactionType: TransactionType;
  quantity: number;
  unitPrice: number;
}

/** Trading Service — `PortfolioStatementResponse`. */
export interface PortfolioStatement {
  statementId: number;
  portfolioAccountId: number;
  holdingId: number | null;
  transactionId: number | null;
  statementStart: string;
  statementEnd: string;
  openingValue: number;
  closingValue: number;
  generatedAt: string;
  status: string;
  transactionIds: number[];
}

/** Trading Service — `InvestmentPositionResponse`, one row per held product. */
export interface InvestmentPosition {
  productId: number;
  productName: string;
  boughtQuantity: number;
  soldQuantity: number;
  currentQuantity: number;
  averageBuyPrice: number;
  currentPrice: number;
  investedValue: number;
  currentValuation: number;
  profitLoss: number;
  lastTransactionDate: string | null;
}

/**
 * Trading Service — `InvestmentOverviewResponse`.
 * Aggregates Portfolio holdings, Trading's own completed trades, and live Product prices.
 */
export interface InvestmentOverview {
  userId: number;
  portfolioAccountId: number;
  positions: InvestmentPosition[];
}
