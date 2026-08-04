import { AccountType, BankAccountStatus, DocumentType, RecordStatus, VerificationStatus } from "./enums";

/** Bank Service — `BankAccountResponse`. Account numbers are masked server-side. */
export interface BankAccount {
  bankAccountId: number;
  userId: number;
  bankName: string;
  branchName: string | null;
  maskedAccountNumber: string;
  accountType: AccountType;
  ifscCode: string | null;
  balance: number;
  primaryAccount: boolean;
  status: BankAccountStatus;
  createdAt: string;
  updatedAt: string;
}

/** Bank Service — `KycDocumentResponse`. Document numbers are masked server-side. */
export interface KycDocument {
  kycDocumentId: number;
  userId: number;
  documentType: DocumentType;
  maskedDocumentNumber: string;
  fileName: string | null;
  verificationStatus: VerificationStatus;
  submittedDate: string | null;
  verifiedDate: string | null;
  status: RecordStatus;
  createdAt: string;
  updatedAt: string;
}
