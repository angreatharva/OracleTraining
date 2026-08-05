/**
 * Portfolio statements: list and detail.
 *
 * Read-only by design. Statement creation is a SERVICE-token endpoint - the opening and
 * closing values are supplied in the request body, so letting a user call it would let them
 * author their own figures.
 */
define([
  "knockout",
  "ojs/ojarraydataprovider",
  "services/TradingService",
  "utils/ScreenState",
  "utils/format"
], function (ko, ArrayDataProvider, TradingService, ScreenState, format) {
  "use strict";

  function StatementsViewModel() {
    var self = this;

    self.state = ScreenState.create();
    self.format = format;

    self.statements = ko.observableArray([]);
    self.statementsDP = new ArrayDataProvider(self.statements, { keyAttributes: "statementId" });
    self.selected = ko.observable(null);

    self.hasStatements = ko.pureComputed(function () {
      return !self.state.isLoading() && self.statements().length > 0;
    });
    self.isEmpty = ko.pureComputed(function () {
      return !self.state.isLoading() && !self.state.errorMessage() && self.statements().length === 0;
    });

    self.movement = ko.pureComputed(function () {
      var s = self.selected();
      return s ? Number(s.closingValue) - Number(s.openingValue) : 0;
    });
    self.movementClass = ko.pureComputed(function () {
      return format.profitLossClass(self.movement());
    });

    self.load = function () {
      self.state.runAllowingNotFound(function () {
        return TradingService.listStatements();
      }).then(function (statements) {
        var rows = (statements || []).slice().sort(function (a, b) {
          return String(b.statementEnd).localeCompare(String(a.statementEnd));
        });
        self.statements(rows);
      });
    };

    self.select = function (statement) {
      self.selected(statement);
    };

    self.clearSelection = function () {
      self.selected(null);
    };

    self.connected = function () {
      self.load();
    };
  }

  return StatementsViewModel;
});
