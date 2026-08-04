/**
 * The screen map, and which role may reach each screen.
 *
 * Hiding a nav entry is a convenience, not a security boundary: every backend service
 * independently rejects a call the caller is not entitled to make, so a user who types the
 * URL directly still gets 403 from the API. The guard exists so the UI does not open
 * screens that would render nothing but errors.
 *
 * To add a screen: add an entry here, then create viewModels/<path>.js and
 * views/<path>.html with the same base name - ModuleRouterAdapter resolves them by name.
 */
define([], function () {
  "use strict";

  var INVESTOR = "INVESTOR";
  var MANAGER = "MANAGER";

  /** Shown in the nav bar. */
  var NAV_ROUTES = [
    { path: "dashboard", label: "Dashboard", iconClass: "oj-ux-ico-bar-chart", roles: [INVESTOR] },
    { path: "portfolio", label: "Portfolio", iconClass: "oj-ux-ico-folder", roles: [INVESTOR] },
    { path: "trade", label: "Trade", iconClass: "oj-ux-ico-transaction", roles: [INVESTOR] },
    { path: "trades", label: "History", iconClass: "oj-ux-ico-clock", roles: [INVESTOR] },
    { path: "statements", label: "Statements", iconClass: "oj-ux-ico-file-text", roles: [INVESTOR] },
    { path: "bank", label: "Bank", iconClass: "oj-ux-ico-bank", roles: [INVESTOR] },
    { path: "kyc", label: "KYC", iconClass: "oj-ux-ico-badge", roles: [INVESTOR] },

    { path: "team", label: "Team", iconClass: "oj-ux-ico-contact-group", roles: [MANAGER] },
    { path: "onboarding", label: "Onboard", iconClass: "oj-ux-ico-add-person", roles: [MANAGER] },
    { path: "catalog", label: "Products", iconClass: "oj-ux-ico-catalog", roles: [MANAGER] },
    { path: "kyc-queue", label: "KYC Queue", iconClass: "oj-ux-ico-approval", roles: [MANAGER] },
    { path: "bank-admin", label: "Accounts", iconClass: "oj-ux-ico-bank", roles: [MANAGER] },

    { path: "profile", label: "Profile", iconClass: "oj-ux-ico-contact", roles: [INVESTOR, MANAGER] }
  ];

  /** Reachable by navigation but not listed in the nav bar (drill-down screens). */
  var HIDDEN_ROUTES = [
    { path: "team-detail", label: "Team member", iconClass: "oj-ux-ico-contact", roles: [MANAGER] }
  ];

  var ALL = NAV_ROUTES.concat(HIDDEN_ROUTES);

  function allowedFor(list, role) {
    return list.filter(function (route) {
      return route.roles.indexOf(role) !== -1;
    });
  }

  return {
    ALL: ALL,

    /** Where each role lands after signing in. */
    landingPath: function (role) {
      return role === MANAGER ? "team" : "dashboard";
    },

    /** Every screen the role may open, including hidden ones. */
    routesFor: function (role) {
      return allowedFor(ALL, role);
    },

    /** Just the nav-bar entries. */
    navRoutesFor: function (role) {
      return allowedFor(NAV_ROUTES, role);
    },

    isAllowed: function (path, role) {
      var match = ALL.filter(function (route) {
        return route.path === path;
      })[0];
      return match !== undefined && match.roles.indexOf(role) !== -1;
    }
  };
});
