import { KycStatus, RiskLevel, UserStatus } from "./enums";

/** User Service — `RoleResponse`. */
export interface Role {
  roleId: number;
  roleName: string;
}

/** User Service — `UserResponse`. `passwordHash` is intentionally never returned. */
export interface User {
  userId: number;
  roleId: number;
  managerId: number | null;
  email: string;
  fullName: string;
  phone: string | null;
  status: UserStatus;
  createdAt: string;
  updatedAt: string;
}

/** User Service — `UserDetailResponse`. One risk/KYC profile per user. */
export interface UserDetail {
  userDetailId: number;
  userId: number;
  dateOfBirth: string | null;
  riskLevel: RiskLevel;
  riskScore: number | null;
  kycStatus: KycStatus;
}
