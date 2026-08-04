/**
 * KYC queue - not implemented yet.
 *
 * Next step: list PENDING documents and approve or reject them
 * Primary API: BankService.listKycDocuments({verificationStatus:'PENDING'}), setVerification
 *
 * Follow the pattern in viewModels/dashboard.js (investor) or viewModels/team.js (manager):
 * observables for data / isLoading / errorMessage, load in connected(), and normalise
 * failures through services/ApiErrorNormalizer.
 */
define(["knockout"], function (ko) {
  "use strict";

  function ScreenViewModel() {
    var self = this;
    self.title = ko.observable("KYC queue");
    self.note = ko.observable("list PENDING documents and approve or reject them");
    self.api = ko.observable("BankService.listKycDocuments({verificationStatus:'PENDING'}), setVerification");

    self.connected = function () {
      // Load data here.
    };
  }

  return ScreenViewModel;
});
