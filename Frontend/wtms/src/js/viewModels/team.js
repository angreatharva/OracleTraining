/**
 * Manager landing screen: the signed-in manager's direct reports.
 *
 * The backend resolves "my team" from user.manager_id, so passing your own userId is the
 * whole query. Selecting a row drills into team-detail with that userId as a route param.
 */
define([
  "knockout",
  "ojs/ojarraydataprovider",
  "services/UserService",
  "services/SessionStore",
  "config/navigation",
  "utils/ScreenState",
  "utils/format"
], function (ko, ArrayDataProvider, UserService, SessionStore, navigation, ScreenState, format) {
  "use strict";

  function TeamViewModel() {
    var self = this;

    self.state = ScreenState.create();
    self.format = format;

    self.members = ko.observableArray([]);
    self.dataProvider = new ArrayDataProvider(self.members, { keyAttributes: "userId" });

    /** Presentation-only counts over the members already loaded. */
    self.activeCount = ko.pureComputed(function () {
      return self.members().filter(function (m) { return m.status === "ACTIVE"; }).length;
    });

    self.inactiveCount = ko.pureComputed(function () {
      return self.members().length - self.activeCount();
    });

    self.hasMembers = ko.pureComputed(function () {
      return !self.state.isLoading() && self.members().length > 0;
    });
    self.isEmpty = ko.pureComputed(function () {
      return !self.state.isLoading() && !self.state.errorMessage() && self.members().length === 0;
    });

    self.load = function () {
      var managerId = SessionStore.userId();
      if (!managerId) { return; }

      self.state.run(function () {
        return UserService.listTeam(managerId);
      }).then(function (members) {
        self.members(members || []);
      });
    };

    self.openMember = function (member) {
      navigation.go("team-detail", { userId: member.userId });
    };

    self.connected = function () {
      self.load();
    };
  }

  return TeamViewModel;
});
