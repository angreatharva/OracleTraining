/**
 * Enum value unions mirroring the backend Java enums.
 * Keep these in sync with the `enums` package of each microservice.
 */

/** User Service */
export type UserStatus = "ACTIVE" | "INACTIVE" | "SUSPENDED";
export type RiskLevel = "LOW" | "MODERATE" | "HIGH";
export type KycStatus = "PENDING" | "VERIFIED" | "REJECTED";

/** Bank Service */
export type AccountType = "SAVINGS" | "CURRENT" | "SALARY" | "NRE" | "NRO" | "OTHER";
export type BankAccountStatus = "ACTIVE" | "INACTIVE" | "BLOCKED" | "CLOSED";
export type DocumentType =
  | "PAN"
  | "AADHAAR"
  | "PASSPORT"
  | "DRIVING_LICENSE"
  | "VOTER_ID"
  | "OTHER";
export type VerificationStatus = "PENDING" | "VERIFIED" | "REJECTED";
export type RecordStatus = "ACTIVE" | "INACTIVE";

/** Portfolio Service */
export type PortfolioAccountStatus = "ACTIVE" | "SUSPENDED" | "CLOSED";
export type HoldingStatus = "ACTIVE" | "MATURED" | "CLOSED";

/** Product Service */
export type ProductStatus = "ACTIVE" | "INACTIVE";
export type RiskCategory = "LOW" | "MODERATE" | "HIGH";
export type PriceMethod = "MARKET" | "NAV" | "FIXED";

/** Trading Service */
export type TransactionType = "BUY" | "SELL";
export type TransactionStatus = "PENDING" | "COMPLETED" | "FAILED" | "REVERSED";
