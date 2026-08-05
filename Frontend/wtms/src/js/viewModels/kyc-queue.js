/**
 * KYC verification queue.
 *
 * A manager can only see documents belonging to their own direct reports, so the queue is
 * assembled per team member rather than by asking for "all pending" - the backend requires
 * a userId on that filter for a manager and would reject an unscoped call.
 */
define([
  "knockout",
  "services/UserService",
  "services/BankService",
  "services/SessionStore",
  "utils/ScreenState",
  "utils/format"
], function (ko, UserService, BankService, SessionStore, ScreenState, format) {
  "use strict";

  function KycQueueViewModel() {
    var self = this;

    self.state = ScreenState.create();
    self.format = format;

    self.pending = ko.observableArray([]);
    self.busyId = ko.observable(null);
    self.showAll = ko.observable(false);

    self.hasPending = ko.pureComputed(function () {
      return !self.state.isLoading() && self.pending().length > 0;
    });
    self.isEmpty = ko.pureComputed(function () {
      return !self.state.isLoading() && !self.state.errorMessage() && self.pending().length === 0;
    });

    self.load = function () {
      var managerId = SessionStore.userId();
      if (!managerId) { return; }

      self.state.run(function () {
        return UserService.listTeam(managerId).then(function (team) {
          var members = team || [];
          if (members.length === 0) { return []; }

          // One call per report. Fine for a handful of reports; if teams grow the backend
          // would need a manager-scoped KYC query.
          return Promise.all(members.map(function (member) {
            var filter = { userId: member.userId };
            if (!self.showAll()) { filter.verificationStatus = "PENDING"; }
            return BankService.listKycDocuments(filter)
              .then(function (docs) {
                return (docs || []).map(function (doc) {
                  return Object.assign({}, doc, {
                    ownerName: member.fullName,
                    ownerEmail: member.email
                  });
                });
              })
              .catch(function () { return []; });
          })).then(function (perMember) {
            return perMember.reduce(function (all, list) { return all.concat(list); }, []);
          });
        });
      }).then(function (docs) {
        self.pending(docs || []);
      });
    };

    self.toggleShowAll = function () {
      self.showAll(!self.showAll());
      self.load();
    };

    function setStatus(doc, status) {
      self.busyId(doc.kycDocumentId);
      self.state.run(function () {
        return BankService.setVerification(doc.kycDocumentId, status);
      }).then(function (updated) {
        self.busyId(null);
        if (updated) {
          self.state.successMessage(
            doc.documentType + " for " + doc.ownerName + " marked " + status + "."
          );
          self.load();
        }
      });
    }

    self.approve = function (doc) { setStatus(doc, "VERIFIED"); };
    self.reject = function (doc) { setStatus(doc, "REJECTED"); };

    self.connected = function () {
      self.load();
    };
  }

  return KycQueueViewModel;
});
