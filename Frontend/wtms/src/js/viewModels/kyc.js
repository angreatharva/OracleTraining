/**
 * KYC documents - not implemented yet.
 *
 * Next step: list own documents and submit new metadata
 * Primary API: BankService.listKycDocuments, BankService.createKycDocument
 *
 * Follow the pattern in viewModels/dashboard.js (investor) or viewModels/team.js (manager):
 * observables for data / isLoading / errorMessage, load in connected(), and normalise
 * failures through services/ApiErrorNormalizer.
 */
define(["knockout"], function (ko) {
  "use strict";

  function ScreenViewModel() {
    var self = this;
    self.title = ko.observable("KYC documents");
    self.note = ko.observable("list own documents and submit new metadata");
    self.api = ko.observable("BankService.listKycDocuments, BankService.createKycDocument");

    self.connected = function () {
      // Load data here.
    };
  }

  return ScreenViewModel;
});
