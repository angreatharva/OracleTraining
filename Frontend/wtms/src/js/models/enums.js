/**
 * Enum values mirroring the backend Java enums, plus JSDoc type definitions for the
 * response DTOs.
 *
 * The arrays are genuinely useful at runtime - they populate dropdowns and drive status
 * styling. The typedefs below carry no runtime cost but give editors autocomplete on
 * server responses, which is the main thing lost by not using TypeScript. Keep both in
 * sync with the `enums` package of each microservice.
 */
define([], function () {
  "use strict";

  return {
    /** User Service */
    USER_STATUS: ["ACTIVE", "INACTIVE", "SUSPENDED"],
    RISK_LEVEL: ["LOW", "MODERATE", "HIGH"],
    KYC_STATUS: ["PENDING", "VERIFIED", "REJECTED"],

    /** Bank Service */
    ACCOUNT_TYPE: ["SAVINGS", "CURRENT", "SALARY", "NRE", "NRO", "OTHER"],
    BANK_ACCOUNT_STATUS: ["ACTIVE", "INACTIVE", "BLOCKED", "CLOSED"],
    DOCUMENT_TYPE: ["PAN", "AADHAAR", "PASSPORT", "DRIVING_LICENSE", "VOTER_ID", "OTHER"],
    VERIFICATION_STATUS: ["PENDING", "VERIFIED", "REJECTED"],
    RECORD_STATUS: ["ACTIVE", "INACTIVE"],

    /** Portfolio Service */
    PORTFOLIO_ACCOUNT_STATUS: ["ACTIVE", "SUSPENDED", "CLOSED"],
    HOLDING_STATUS: ["ACTIVE", "MATURED", "CLOSED"],

    /** Product Service */
    PRODUCT_STATUS: ["ACTIVE", "INACTIVE"],
    RISK_CATEGORY: ["LOW", "MODERATE", "HIGH"],
    PRICE_METHOD: ["MARKET", "NAV", "FIXED"],

    /** Trading Service */
    TRANSACTION_TYPE: ["BUY", "SELL"],
    TRANSACTION_STATUS: ["PENDING", "COMPLETED", "FAILED", "REVERSED"],

    /** Roles, seeded as role_id 1 and 2. */
    ROLES: ["MANAGER", "INVESTOR"],

    /** Maps any status value to an Oracle JET colour class, for badges. */
    statusClass: function (status) {
      switch (status) {
        case "ACTIVE":
        case "COMPLETED":
        case "VERIFIED":
          return "oj-badge-success";
        case "PENDING":
          return "oj-badge-warning";
        case "FAILED":
        case "REJECTED":
        case "BLOCKED":
          return "oj-badge-danger";
        default:
          return "oj-badge-neutral";
      }
    }
  };
});

/* ---------------------------------------------------------------------------------------
 * Response shapes returned by the backend. Documentation only - no runtime effect.
 *
 * @typedef {Object} User
 * @property {number} userId
 * @property {number} roleId
 * @property {number|null} managerId
 * @property {string} email
 * @property {string} fullName
 * @property {string|null} phone
 * @property {string} status              ACTIVE | INACTIVE | SUSPENDED
 *
 * @typedef {Object} BankAccount
 * @property {number} bankAccountId
 * @property {number} userId
 * @property {string} bankName
 * @property {string} maskedAccountNumber the real number is never returned
 * @property {number} balance
 * @property {boolean} primaryAccount
 * @property {string} status
 *
 * @typedef {Object} InvestmentProduct
 * @property {number} productId
 * @property {string} productName
 * @property {number} currentPrice        the price Trading actually executes at
 * @property {string} riskCategory
 * @property {boolean} active
 *
 * @typedef {Object} PortfolioAccount
 * @property {number} portfolioAccountId
 * @property {number} userId
 * @property {string} accountStatus
 *
 * @typedef {Object} PortfolioHolding
 * @property {number} holdingId
 * @property {number} portfolioAccountId
 * @property {number} productId
 * @property {number} quantity
 * @property {number} averageCost
 * @property {number} marketValue
 * @property {number} unrealizedGainLoss
 * @property {string} holdingStatus
 *
 * @typedef {Object} TradeTransaction
 * @property {number} transactionId
 * @property {number} portfolioAccountId
 * @property {number} holdingId
 * @property {number} productId
 * @property {string} transactionType     BUY | SELL
 * @property {number} quantity
 * @property {number} unitPrice
 * @property {number} totalAmount
 * @property {string} transactionStatus
 * @property {string|null} failureReason  only present on the immediate 422; never persisted
 *
 * @typedef {Object} InvestmentPosition
 * @property {number} productId
 * @property {string} productName
 * @property {number} currentQuantity
 * @property {number} averageBuyPrice
 * @property {number} currentPrice
 * @property {number} investedValue
 * @property {number} currentValuation
 * @property {number} profitLoss
 * ------------------------------------------------------------------------------------ */
