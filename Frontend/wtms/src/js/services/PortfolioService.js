/** Portfolio Service (PORTFOLIO-SERVICE, port 8084) - accounts, holdings and valuation. */
define(["services/ApiClient"], function (ApiClient) {
  "use strict";

  return {
    /** The usual entry point: an investor has exactly one portfolio account. */
    getAccountByUser: function (userId) {
      return ApiClient.get("portfolio", "/api/portfolios/by-user/" + userId);
    },

    getAccount: function (portfolioAccountId) {
      return ApiClient.get("portfolio", "/api/portfolios/" + portfolioAccountId);
    },

    listHoldings: function (portfolioAccountId, status) {
      return ApiClient.get(
        "portfolio",
        "/api/portfolios/" + portfolioAccountId + "/holdings",
        { status: status }
      );
    },

    /**
     * Uses stored valuations and does not refresh quotes. For live prices use
     * TradingService.getInvestmentOverview, which re-reads Product Service. The two views
     * can legitimately disagree.
     */
    getSummary: function (portfolioAccountId) {
      return ApiClient.get("portfolio", "/api/portfolios/" + portfolioAccountId + "/summary");
    },

    /**
     * Opens a holding. The trade screen calls this before a first BUY, because the backend
     * has no first-holding auto-creation and a trade needs an existing holdingId.
     */
    addHolding: function (portfolioAccountId, request) {
      return ApiClient.post(
        "portfolio",
        "/api/portfolios/" + portfolioAccountId + "/holdings",
        request
      );
    },

    /** MANAGER only. */
    createAccount: function (request) {
      return ApiClient.post("portfolio", "/api/portfolios", request);
    },

    /** MANAGER only. */
    updateStatus: function (portfolioAccountId, accountStatus) {
      return ApiClient.patch(
        "portfolio",
        "/api/portfolios/" + portfolioAccountId + "/status",
        { accountStatus: accountStatus }
      );
    }

    // The /api/portfolios/internal/trades* endpoints are Trading-to-Portfolio commands.
    // They require a SERVICE token and the gateway blocks them outright - never call them.
  };
});
