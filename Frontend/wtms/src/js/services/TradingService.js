/**
 * Trading Service (TRADING-SERVICE, port 8085) - BUY/SELL orchestration, statements and
 * the live investment overview.
 */
define(["services/ApiClient"], function (ApiClient) {
  "use strict";

  return {
    /**
     * Submits a BUY or SELL. The backend runs a synchronous saga across Product, Portfolio
     * and Bank, then persists the outcome.
     *
     * A rejected trade is not an error in business terms: the backend answers HTTP 422 with
     * a FAILED TradeTransaction rather than the shared error schema, so it is unwrapped and
     * returned here like a success. Check `transactionStatus`, not the HTTP status.
     *
     * `failureReason` is readable only on this response - a later GET returns it as null,
     * because it is never persisted. Capture it now if you want to show it.
     *
     * Never retry this call: the backend has no idempotency keys, so a retry can debit twice.
     *
     * @returns {Promise<Object>} the trade, COMPLETED or FAILED
     */
    submitTrade: function (request) {
      return ApiClient.post("trading", "/api/trade-transactions", request)
        .catch(function (error) {
          if (error instanceof ApiClient.ApiError && error.status === 422 && error.body) {
            return error.body;
          }
          throw error;
        });
    },

    /** An investor is pinned to their own portfolio account server-side. */
    listTrades: function (filter) {
      return ApiClient.get("trading", "/api/trade-transactions", filter);
    },

    getTrade: function (transactionId) {
      return ApiClient.get("trading", "/api/trade-transactions/" + transactionId);
    },

    listStatements: function (filter) {
      return ApiClient.get("trading", "/api/portfolio-statements", filter);
    },

    /**
     * Positions with live Product prices - the primary "what do I own and what is it worth"
     * view, and the one screen that needs a single call rather than several.
     */
    getInvestmentOverview: function (userId) {
      return ApiClient.get("trading", "/api/investment-overview/users/" + userId);
    }
  };
});
