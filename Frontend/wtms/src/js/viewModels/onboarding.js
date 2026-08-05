/**
 * Onboard a new investor.
 *
 * Four sequential calls, and the backend has no transaction spanning them:
 *   1. POST /api/users            -> creates the login
 *   2. POST /api/user-details     -> risk / KYC profile
 *   3. POST /api/portfolios       -> portfolio account
 *   4. POST /api/bank-accounts    -> bank account
 *
 * The bank account is opened automatically: only userId (and, if given, the opening
 * deposit) is sent. Bank Service fills in a house-bank name, IFSC and a generated account
 * number for anything left blank, so the manager never has to key in bank details for a
 * brand-new investor. It becomes the investor's primary account and its balance is
 * thereafter maintained automatically by the trading flow's own debit/credit calls - nothing
 * further to wire up here.
 *
 * If any step fails, the earlier records still exist. Rather than pretend otherwise, each
 * step reports its own outcome so the manager can see exactly how far it got and finish the
 * rest by hand. Steps are not retried automatically: re-running step 1 would fail on the
 * unique email anyway.
 */
define([
  "knockout",
  "ojs/ojarraydataprovider",
  "services/UserService",
  "services/PortfolioService",
  "services/BankService",
  "services/SessionStore",
  "services/ApiErrorNormalizer",
  "utils/ScreenState",
  "models/enums"
], function (ko, ArrayDataProvider, UserService, PortfolioService, BankService, SessionStore,
             ApiErrorNormalizer, ScreenState, enums) {
  "use strict";

  /** role_id 2 = INVESTOR, seeded in the schema. */
  var INVESTOR_ROLE_ID = 2;

  function OnboardingViewModel() {
    var self = this;

    self.state = ScreenState.create();

    self.fullName = ko.observable("");
    self.email = ko.observable("");
    self.phone = ko.observable("");
    self.password = ko.observable("");
    self.dateOfBirth = ko.observable("");
    self.riskLevel = ko.observable("MODERATE");
    self.riskLevelOptions = enums.RISK_LEVEL;
    self.openingBalance = ko.observable("");

    /** The same list as a DataProvider, for oj-c-select-single. */
    self.riskLevelDP = new ArrayDataProvider(
      enums.RISK_LEVEL.map(function (value) { return { value: value, label: value }; }),
      { keyAttributes: "value" }
    );

    /** Step decoration. Presentation only - `status` is still 'ok' | 'failed' | 'skipped'. */
    self.stepIcon = function (status) {
      if (status === "ok") { return "✓"; }
      return status === "failed" ? "✕" : "–";
    };

    self.stepIconClass = function (status) {
      if (status === "ok") { return "wtms-step-icon wtms-step-ok"; }
      return status === "failed"
        ? "wtms-step-icon wtms-step-failed"
        : "wtms-step-icon wtms-step-skipped";
    };

    self.isSubmitting = ko.observable(false);
    /** One entry per step: {label, status: 'ok'|'failed'|'skipped', detail}. */
    self.steps = ko.observableArray([]);
    self.createdUser = ko.observable(null);

    self.canSubmit = ko.pureComputed(function () {
      return !self.isSubmitting() &&
        self.fullName().trim().length > 0 &&
        self.email().trim().length > 0 &&
        self.password().length >= 8;
    });

    self.hasResult = ko.pureComputed(function () { return self.steps().length > 0; });

    function record(label, status, detail) {
      self.steps.push({ label: label, status: status, detail: detail || "" });
    }

    self.submit = function () {
      if (!self.canSubmit()) { return; }

      self.isSubmitting(true);
      self.steps([]);
      self.createdUser(null);
      self.state.clearMessages();

      var managerId = SessionStore.userId();

      UserService.createUser({
        roleId: INVESTOR_ROLE_ID,
        managerId: managerId,
        password: self.password(),
        email: self.email().trim(),
        fullName: self.fullName().trim(),
        phone: self.phone().trim() || undefined,
        status: "ACTIVE"
      }).then(function (user) {
        record("Create login", "ok", user.email + " (user #" + user.userId + ")");
        self.createdUser(user);

        return UserService.createUserDetail({
          userId: user.userId,
          dateOfBirth: self.dateOfBirth() || undefined,
          riskLevel: self.riskLevel(),
          kycStatus: "PENDING"
        }).then(function () {
          record("Create risk profile", "ok", self.riskLevel() + ", KYC PENDING");
        }, function (error) {
          record("Create risk profile", "failed", ApiErrorNormalizer.normalize(error).message);
        }).then(function () {
          return PortfolioService.createAccount({ userId: user.userId }).then(function (account) {
            record("Open portfolio account", "ok", "account #" + account.portfolioAccountId);
          }, function (error) {
            record("Open portfolio account", "failed", ApiErrorNormalizer.normalize(error).message);
          });
        }).then(function () {
          var deposit = self.openingBalance().trim();
          return BankService.createAccount({
            userId: user.userId,
            openingBalance: deposit.length > 0 ? Number(deposit) : undefined
          }).then(function (account) {
            record("Open bank account", "ok",
              account.bankName + " " + account.maskedAccountNumber);
          }, function (error) {
            record("Open bank account", "failed", ApiErrorNormalizer.normalize(error).message);
          });
        });
      }, function (error) {
        var normalized = ApiErrorNormalizer.normalize(error);
        record("Create login", "failed", normalized.message);
        record("Create risk profile", "skipped", "not attempted - the login was not created");
        record("Open portfolio account", "skipped", "not attempted - the login was not created");
        record("Open bank account", "skipped", "not attempted - the login was not created");
        self.state.fieldErrors(normalized.fieldErrors);
      }).then(function () {
        self.isSubmitting(false);

        var failed = self.steps().filter(function (s) { return s.status === "failed"; });
        if (failed.length === 0) {
          self.state.successMessage(
            "Investor onboarded. They can sign in with the password you set."
          );
          self.fullName("");
          self.email("");
          self.phone("");
          self.password("");
          self.dateOfBirth("");
          self.openingBalance("");
        } else if (self.createdUser()) {
          self.state.errorMessage(
            "Partially completed. The login exists, but the steps marked below did not " +
            "finish - complete them from the other screens (a bank account can be opened " +
            "from Accounts)."
          );
        } else {
          self.state.errorMessage("Nothing was created.");
        }
      });
    };

    self.disconnected = function () {
      self.password("");
    };
  }

  return OnboardingViewModel;
});
