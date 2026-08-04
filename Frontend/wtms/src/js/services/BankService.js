/** Bank Service (BANK-SERVICE, port 8083) - accounts, balances and KYC metadata. */
define(["services/ApiClient"], function (ApiClient) {
  "use strict";

  return {
    /**
     * An investor is narrowed to their own accounts server-side whatever they pass.
     * A manager must supply a userId, and it must be one of their reports.
     * @param {{userId?:number, status?:string, primary?:boolean}} [filter]
     */
    listAccounts: function (filter) {
      return ApiClient.get("bank", "/api/bank-accounts", filter);
    },

    getAccount: function (bankAccountId) {
      return ApiClient.get("bank", "/api/bank-accounts/" + bankAccountId);
    },

    createAccount: function (request) {
      return ApiClient.post("bank", "/api/bank-accounts", request);
    },

    makePrimary: function (bankAccountId) {
      return ApiClient.patch("bank", "/api/bank-accounts/" + bankAccountId + "/primary");
    },

    /** MANAGER only - this is how an account is blocked or closed administratively. */
    updateAccount: function (bankAccountId, request) {
      return ApiClient.put("bank", "/api/bank-accounts/" + bankAccountId, request);
    },

    listKycDocuments: function (filter) {
      return ApiClient.get("bank", "/api/kyc-documents", filter);
    },

    createKycDocument: function (request) {
      return ApiClient.post("bank", "/api/kyc-documents", request);
    },

    /** MANAGER only - an investor must not be able to approve their own KYC. */
    setVerification: function (kycDocumentId, verificationStatus) {
      return ApiClient.put("bank", "/api/kyc-documents/" + kycDocumentId + "/verification", {
        verificationStatus: verificationStatus
      });
    }

    // debit and credit are intentionally absent: the backend accepts them only from a
    // SERVICE token, because money movement belongs to the Trading saga.
  };
});
