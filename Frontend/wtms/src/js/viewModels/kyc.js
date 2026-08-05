/**
 * The investor's own KYC documents: list, and submit new metadata.
 *
 * "Submit" records metadata only - there is no file upload endpoint. The document number is
 * write-only: responses return `maskedDocumentNumber`, so a submitted document can never be
 * edited or re-displayed in full.
 *
 * Verification is deliberately absent - only a manager can approve or reject.
 */
define([
  "knockout",
  "ojs/ojarraydataprovider",
  "services/BankService",
  "services/SessionStore",
  "utils/ScreenState",
  "utils/format",
  "models/enums"
], function (ko, ArrayDataProvider, BankService, SessionStore, ScreenState, format, enums) {
  "use strict";

  function KycViewModel() {
    var self = this;

    self.state = ScreenState.create();
    self.format = format;

    self.documents = ko.observableArray([]);
    self.documentsDP = new ArrayDataProvider(self.documents, { keyAttributes: "kycDocumentId" });

    self.documentTypeOptions = enums.DOCUMENT_TYPE;
    self.newDocumentType = ko.observable("PAN");
    self.newDocumentNumber = ko.observable("");
    self.newFileName = ko.observable("");
    self.isSubmitting = ko.observable(false);

    self.hasDocuments = ko.pureComputed(function () {
      return !self.state.isLoading() && self.documents().length > 0;
    });
    self.isEmpty = ko.pureComputed(function () {
      return !self.state.isLoading() && !self.state.errorMessage() && self.documents().length === 0;
    });
    self.canSubmit = ko.pureComputed(function () {
      return !self.isSubmitting() && self.newDocumentNumber().trim().length > 0;
    });

    self.load = function () {
      self.state.runAllowingNotFound(function () {
        return BankService.listKycDocuments();
      }).then(function (documents) {
        self.documents(documents || []);
      });
    };

    self.submit = function () {
      if (!self.canSubmit()) { return; }
      var userId = SessionStore.userId();
      if (!userId) { return; }

      self.isSubmitting(true);
      self.state.run(function () {
        return BankService.createKycDocument({
          userId: userId,
          documentType: self.newDocumentType(),
          documentNumber: self.newDocumentNumber().trim(),
          // Omit rather than send "": the backend treats blank strings as values.
          fileName: self.newFileName().trim() || undefined
        });
      }).then(function (created) {
        self.isSubmitting(false);
        if (created) {
          self.state.successMessage("Document submitted and awaiting verification.");
          self.newDocumentNumber("");
          self.newFileName("");
          self.load();
        }
      });
    };

    self.connected = function () {
      self.load();
    };
  }

  return KycViewModel;
});
