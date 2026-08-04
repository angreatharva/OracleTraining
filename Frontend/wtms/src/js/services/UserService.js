/** User Service (USER-SERVICE, port 8082) - roles, users and risk/KYC profiles. */
define(["services/ApiClient"], function (ApiClient) {
  "use strict";

  return {
    /** Readable by any authenticated user; used to label users by role. */
    listRoles: function () {
      return ApiClient.get("user", "/api/roles");
    },

    /** MANAGER only. */
    listUsers: function () {
      return ApiClient.get("user", "/api/users");
    },

    /** Self, or a manager reading one of their direct reports. */
    getUser: function (userId) {
      return ApiClient.get("user", "/api/users/" + userId);
    },

    /** A manager's own direct reports. Pass the manager's own userId. */
    listTeam: function (managerId) {
      return ApiClient.get("user", "/api/users/manager/" + managerId);
    },

    getUserDetailByUserId: function (userId) {
      return ApiClient.get("user", "/api/user-details/user/" + userId);
    },

    /** MANAGER only. `password` is plaintext here and BCrypt-hashed by the server. */
    createUser: function (request) {
      return ApiClient.post("user", "/api/users", request);
    },

    /** MANAGER only. Does not change the password - see AuthService.changePassword. */
    updateUser: function (userId, request) {
      return ApiClient.put("user", "/api/users/" + userId, request);
    },

    /** MANAGER only. */
    createUserDetail: function (request) {
      return ApiClient.post("user", "/api/user-details", request);
    }
  };
});
