import { ApiClient } from "./ApiClient";
import { BankAccount, KycDocument } from "../models/bank";
import { BankAccountStatus, DocumentType, VerificationStatus } from "../models/enums";

/** Bank Service (`BANK-SERVICE`, port 8083) — accounts, balances, and KYC document metadata. */
export const BankService = {
  listAccounts: (filter?: {
    userId?: number;
    status?: BankAccountStatus;
    primary?: boolean;
  }): Promise<BankAccount[]> => ApiClient.get("bank", "/api/bank-accounts", filter),

  getAccount: (bankAccountId: number): Promise<BankAccount> =>
    ApiClient.get("bank", `/api/bank-accounts/${bankAccountId}`),

  makePrimary: (bankAccountId: number): Promise<BankAccount> =>
    ApiClient.patch("bank", `/api/bank-accounts/${bankAccountId}/primary`),

  listKycDocuments: (filter?: {
    userId?: number;
    verificationStatus?: VerificationStatus;
    documentType?: DocumentType;
  }): Promise<KycDocument[]> => ApiClient.get("bank", "/api/kyc-documents", filter)

  // Debit and credit are intentionally omitted: money movement belongs to the Trading
  // saga, not to the UI. Call POST /api/trade-transactions instead.
};
