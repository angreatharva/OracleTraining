/**
 * Application entry point. Loaded by main.js once RequireJS is configured.
 */
define([
  "knockout",
  "ojs/ojbootstrap",
  "appController",
  // JET components used across the screens. RequireJS needs them loaded somewhere that
  // always runs; doing it here keeps individual view models free of boilerplate.
  "ojs/ojknockout",
  "ojs/ojbutton",
  "ojs/ojtoolbar",
  "ojs/ojmenu",
  "ojs/ojmodule-element",
  "ojs/ojnavigationlist",
  "ojs/ojformlayout",
  "ojs/ojinputtext",
  "ojs/ojinputnumber",
  "ojs/ojlabel",
  "ojs/ojtable",
  "ojs/ojselectsingle",
  "ojs/ojprogress-circle",
  "ojs/ojdatetimepicker",
  /*
   * All charts use the classic pack, for two separate reasons:
   *
   * 1. Bar, pie and donut have no Core Pack equivalent in 20.1 at all - only area, line,
   *    picto and tag cloud were ported.
   * 2. The Core Pack charts that do exist do not work here. oj-c-area-chart and
   *    oj-c-line-chart upgrade cleanly, accept their DataProvider and report no error,
   *    but paint an empty plot area - verified in the browser against a bare, minimal
   *    chart with a plain-array provider, so it is not a data or markup problem. The
   *    other Core Pack visuals (meter bar, rating gauge) paint correctly, so this is
   *    specific to the chart family.
   *
   * Revisit on the next JET upgrade; until then oj-c-*-chart is not usable in this app.
   */
  "ojs/ojchart",
  // Core Pack. These are the Redwood-native components the screens are built from.
  "oj-c/message-banner",
  "oj-c/list-view",
  "oj-c/list-item-layout",
  "oj-c/select-single",
  "oj-c/badge",
  "oj-c/avatar",
  "oj-c/meter-bar",
  "oj-c/rating-gauge",
  "oj-c/progress-bar",
  "oj-c/skeleton"
], function (ko, Bootstrap, rootViewModel) {
  "use strict";

  function init() {
    ko.applyBindings(rootViewModel, document.getElementById("globalBody"));
  }

  Bootstrap.whenDocumentReady().then(function () {
    // In a hybrid (e.g. Cordova) environment wait for deviceready before touching plugins.
    if (document.body.classList.contains("oj-hybrid")) {
      document.addEventListener("deviceready", init);
    } else {
      init();
    }
  });
});
