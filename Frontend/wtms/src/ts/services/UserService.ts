import { ApiClient } from "./ApiClient";
import { Role, User, UserDetail } from "../models/user";

/** User Service (`USER-SERVICE`, port 8082) — roles, users, and risk/KYC profiles. */
export const UserService = {
  listRoles: (): Promise<Role[]> => ApiClient.get("user", "/api/roles"),

  getRole: (roleId: number): Promise<Role> => ApiClient.get("user", `/api/roles/${roleId}`),

  listUsers: (): Promise<User[]> => ApiClient.get("user", "/api/users"),

  getUser: (userId: number): Promise<User> => ApiClient.get("user", `/api/users/${userId}`),

  getUserByEmail: (email: string): Promise<User> =>
    ApiClient.get("user", `/api/users/email/${encodeURIComponent(email)}`),

  getUserDetailByUserId: (userId: number): Promise<UserDetail> =>
    ApiClient.get("user", `/api/user-details/user/${userId}`)
};
