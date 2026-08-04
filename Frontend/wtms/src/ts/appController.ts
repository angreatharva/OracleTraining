/**
 * @license
 * Copyright (c) 2014, 2026, Oracle and/or its affiliates.
 * Licensed under The Universal Permissive License (UPL), Version 1.0
 * as shown at https://oss.oracle.com/licenses/upl/
 * @ignore
 */
import * as ko from "knockout";
import * as ResponsiveUtils from "ojs/ojresponsiveutils";
import * as ResponsiveKnockoutUtils from "ojs/ojresponsiveknockoututils";
import Context = require("ojs/ojcontext");
import CoreRouter = require("ojs/ojcorerouter");
import ModuleRouterAdapter = require("ojs/ojmodulerouter-adapter");
import KnockoutRouterAdapter = require("ojs/ojknockoutrouteradapter");
import UrlParamAdapter = require("ojs/ojurlparamadapter");
import ArrayDataProvider = require("ojs/ojarraydataprovider");
import { appConfig } from "./config/appConfig";

interface NavDetail {
  label: string;
  iconClass: string;
}

/**
 * Root ViewModel: owns the application shell and navigation.
 *
 * Routing is CoreRouter + ModuleRouterAdapter. Each route resolves a ViewModel from
 * `viewModels/<path>.ts` and a View from `views/<path>.html`, so the two files must
 * always share a base name.
 */
class RootViewModel {
  smScreen: ko.Observable<boolean> | undefined;
  appName: ko.Observable<string>;
  userLogin: ko.Observable<string>;
  footerLinks: Array<object>;

  router: CoreRouter<NavDetail>;
  moduleAdapter: ModuleRouterAdapter<NavDetail>;
  selection: KnockoutRouterAdapter<NavDetail>;
  navDataProvider: ArrayDataProvider<string, CoreRouter.DetailedRouteConfig<NavDetail>>;

  constructor() {
    // media queries for responsive layouts
    const smQuery: string | null = ResponsiveUtils.getFrameworkQuery("sm-only");
    if (smQuery) {
      this.smScreen = ResponsiveKnockoutUtils.createMediaQueryObservable(smQuery);
    }

    // routes: the first entry redirects the empty path to the landing module
    const routes: Array<CoreRouter.DetailedRouteConfig<NavDetail>> = [
      { path: "", redirect: "dashboard" },
      { path: "dashboard", detail: { label: "Dashboard", iconClass: "oj-ux-ico-bar-chart" } },
      { path: "portfolio", detail: { label: "Portfolio", iconClass: "oj-ux-ico-folder" } },
      { path: "trades", detail: { label: "Trades", iconClass: "oj-ux-ico-transaction" } },
      { path: "products", detail: { label: "Products", iconClass: "oj-ux-ico-catalog" } }
    ];

    this.router = new CoreRouter<NavDetail>(routes, {
      urlAdapter: new UrlParamAdapter()
    });
    this.router.sync();

    this.moduleAdapter = new ModuleRouterAdapter<NavDetail>(this.router);
    this.selection = new KnockoutRouterAdapter<NavDetail>(this.router);

    // the redirect entry is not a navigable destination, so it is excluded from the nav list
    this.navDataProvider = new ArrayDataProvider(routes.slice(1), { keyAttributes: "path" });

    // header
    this.appName = ko.observable(appConfig.appName);
    this.userLogin = ko.observable("john.hancock@oracle.com");

    // footer
    this.footerLinks = [
      { name: "About Oracle", linkId: "aboutOracle", linkTarget: "http://www.oracle.com/us/corporate/index.html#menu-about" },
      { name: "Contact Us", id: "contactUs", linkTarget: "http://www.oracle.com/us/corporate/contact/index.html" },
      { name: "Legal Notices", id: "legalNotices", linkTarget: "http://www.oracle.com/us/legal/index.html" },
      { name: "Terms Of Use", id: "termsOfUse", linkTarget: "http://www.oracle.com/us/legal/terms/index.html" },
      { name: "Your Privacy Rights", id: "yourPrivacyRights", linkTarget: "http://www.oracle.com/us/legal/privacy/index.html" }
    ];

    // release the application bootstrap busy state
    Context.getPageContext().getBusyContext().applicationBootstrapComplete();
  }
}

export default new RootViewModel();
