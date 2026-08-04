/**
 * Root view model: owns the shell, the session and role-gated navigation.
 *
 * The router is built from the routes the signed-in role may reach, and a
 * `beforeStateChange` guard rejects anything else - including a hand-typed URL.
 */
define([
  "knockout",
  "ojs/ojcontext",
  "ojs/ojcorerouter",
  "ojs/ojmodulerouter-adapter",
  "ojs/ojknockoutrouteradapter",
  "ojs/ojurlparamadapter",
  "ojs/ojarraydataprovider",
  "ojs/ojmodule-element-utils",
  "ojs/ojresponsiveutils",
  "ojs/ojresponsiveknockoututils",
  "config/appConfig",
  "config/routes",
  "services/SessionStore",
  "services/AuthService",
  "services/ApiClient",
  "ojs/ojknockout",
  "ojs/ojbutton",
  "ojs/ojtoolbar",
  "ojs/ojmenu",
  "ojs/ojmodule-element",
  "ojs/ojnavigationlist"
], function (
  ko, Context, CoreRouter, ModuleRouterAdapter, KnockoutRouterAdapter, UrlParamAdapter,
  ArrayDataProvider, ModuleElementUtils, ResponsiveUtils, ResponsiveKnockoutUtils,
  appConfig, routes, SessionStore, AuthService, ApiClient
) {
  "use strict";

  function RootViewModel() {
    var self = this;

    var smQuery = ResponsiveUtils.getFrameworkQuery("sm-only");
    if (smQuery) {
      self.smScreen = ResponsiveKnockoutUtils.createMediaQueryObservable(smQuery);
    } else {
      self.smScreen = ko.observable(false);
    }

    self.appName = ko.observable(appConfig.appName);
    self.userLogin = ko.observable("");
    self.userRole = ko.observable("");
    self.isAuthenticated = ko.observable(false);
    self.navDataProvider = ko.observable(new ArrayDataProvider([], { keyAttributes: "path" }));

    self.router = null;
    self.selection = null;

    // The router is rebuilt when a different role signs in, so the view must not bind
    // directly to a specific adapter instance - replacing the property would leave the
    // binding pointing at the old one. Binding to a computed that reads whichever adapter
    // is current keeps oj-module live across rebuilds.
    self.moduleAdapter = ko.observable(null);
    self.moduleConfig = ko.pureComputed(function () {
      var adapter = self.moduleAdapter();
      return adapter ? ko.unwrap(adapter.koObservableConfig) : { view: [], viewModel: null };
    });

    self.footerLinks = [
      { name: "About Oracle", linkId: "aboutOracle", linkTarget: "http://www.oracle.com/us/corporate/index.html#menu-about" },
      { name: "Contact Us", linkId: "contactUs", linkTarget: "http://www.oracle.com/us/corporate/contact/index.html" },
      { name: "Legal Notices", linkId: "legalNotices", linkTarget: "http://www.oracle.com/us/legal/index.html" },
      { name: "Terms Of Use", linkId: "termsOfUse", linkTarget: "http://www.oracle.com/us/legal/terms/index.html" }
    ];

    /** Passed into the login module so it can tell us when sign-in succeeded. */
    self.loginParams = {
      onSignedIn: function () {
        self.startSession();
      }
    };

    // oj-module does not resolve module names by itself - it needs a {view, viewModel}
    // config, which ModuleElementUtils builds from the conventional
    // views/<name>.html + viewModels/<name> pair. (Routed screens get this for free from
    // ModuleRouterAdapter; the login screen sits outside the router, so it is built here.)
    self.loginConfig = ko.observable({ view: [], viewModel: null });
    ModuleElementUtils.createConfig({
      name: "login",
      params: self.loginParams
    }).then(function (config) {
      self.loginConfig(config);
    });

    self.startSession = function () {
      var session = SessionStore.get();
      if (!session) {
        self.endSession();
        return;
      }
      self.userLogin(session.user.email);
      self.userRole(session.roleName);
      // Build the router first: the nav list binds selection.path, and that binding is
      // created the moment isAuthenticated flips true.
      self.buildRouter(session.roleName);
      self.isAuthenticated(true);
    };

    self.endSession = function () {
      AuthService.logout();
      self.isAuthenticated(false);
      self.userLogin("");
      self.userRole("");
    };

    self.signOut = function () {
      self.endSession();
      // Drop any ?path= so a stale route does not reappear at the next sign-in.
      window.history.replaceState(null, "", window.location.pathname);
      window.location.reload();
    };

    /**
     * Builds the router for a role. CoreRouter's route table is fixed at construction, so a
     * different user signing in needs a fresh router rather than a mutation.
     */
    self.buildRouter = function (role) {
      var home = routes.landingPath(role);
      var allowed = routes.routesFor(role);

      // JET allows only one root CoreRouter at a time, and the route table is fixed at
      // construction - so a different role signing in during the same page load needs the
      // previous router torn down first, not just replaced.
      if (self.router) {
        self.router.destroy();
        self.router = null;
        self.moduleAdapter(null);
        self.selection = null;
      }

      var routeConfigs = [{ path: "", redirect: home }].concat(
        allowed.map(function (route) {
          return {
            path: route.path,
            detail: { label: route.label, iconClass: route.iconClass }
          };
        })
      );

      self.router = new CoreRouter(routeConfigs, {
        urlAdapter: new UrlParamAdapter(),
        beforeStateChange: function (args, complete) {
          // Block navigation to a screen this role may not see. Without this, a bookmarked
          // manager URL would load an investor into a screen of nothing but 403s.
          if (args.path && args.path !== "" && !routes.isAllowed(args.path, role)) {
            complete(Promise.reject(new Error('Not permitted to open "' + args.path + '"')));
            return;
          }
          complete(Promise.resolve());
        }
      });

      self.moduleAdapter(new ModuleRouterAdapter(self.router));
      self.selection = new KnockoutRouterAdapter(self.router);

      self.navDataProvider(
        new ArrayDataProvider(
          routes.navRoutesFor(role).map(function (route) {
            return {
              path: route.path,
              detail: { label: route.label, iconClass: route.iconClass }
            };
          }),
          { keyAttributes: "path" }
        )
      );

      self.router.sync().catch(function () {
        // A rejected sync means the stored URL is unreachable for this role; go home.
        self.router.go({ path: home });
      });
    };

    // Any 401 means the token is gone or expired: return to the login screen rather than
    // leaving the user on a page that silently fails to load.
    ApiClient.onUnauthorized(function () {
      self.endSession();
    });

    // A stored session survives a refresh, so rebuild from it when one is present.
    if (SessionStore.isAuthenticated()) {
      self.startSession();
    } else {
      SessionStore.clear();
    }

    Context.getPageContext().getBusyContext().applicationBootstrapComplete();
  }

  return new RootViewModel();
});
