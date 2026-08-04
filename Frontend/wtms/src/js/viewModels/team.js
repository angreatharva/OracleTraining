/**
 * Manager landing screen - the manager-side reference pattern.
 *
 * Lists the signed-in manager's direct reports. The backend resolves "my team" from
 * user.manager_id, so passing your own userId is the whole query.
 */
define([
  "knockout",
  "ojs/ojarraydataprovider",
  "services/UserService",
  "services/SessionStore",
  "services/ApiErrorNormalizer"
], function (ko, ArrayDataProvider, UserService, SessionStore, ApiErrorNormalizer) {
  "use strict";

  function TeamViewModel() {
    var self = this;

    self.isLoading = ko.observable(false);
    self.errorMessage = ko.observable("");
    self.members = ko.observableArray([]);

    self.dataProvider = new ArrayDataProvider(self.members, { keyAttributes: "userId" });

    self.hasMembers = ko.pureComputed(function () {
      return !self.isLoading() && self.members().length > 0;
    });
    self.isEmpty = ko.pureComputed(function () {
      return !self.isLoading() && !self.errorMessage() && self.members().length === 0;
    });

    self.load = function () {
      var managerId = SessionStore.userId();
      if (!managerId) {
        return;
      }
      self.isLoading(true);
      self.errorMessage("");

      UserService.listTeam(managerId)
        .then(function (members) {
          self.members(members || []);
          self.isLoading(false);
        })
        .catch(function (error) {
          self.errorMessage(ApiErrorNormalizer.normalize(error).message);
          self.isLoading(false);
        });
    };

    self.connected = function () {
      self.load();
    };
  }

  return TeamViewModel;
});
