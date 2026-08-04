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
  "ojs/ojprogress-circle"
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
