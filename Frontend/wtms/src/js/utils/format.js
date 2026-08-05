/**
 * Display formatting shared by every screen.
 *
 * Money and quantity come off the wire as JSON numbers converted from Java BigDecimal, so
 * they are already plain numbers here - these helpers only control presentation. Anything
 * null or undefined renders as an em dash rather than "null" or "NaN".
 */
define([], function () {
  "use strict";

  var EMPTY = "—";

  function isBlank(value) {
    return value === null || value === undefined || value === "";
  }

  return {
    EMPTY: EMPTY,

    /** Two decimal places with thousands separators, e.g. 123380.77 -> "123,380.77". */
    money: function (value) {
      if (isBlank(value) || isNaN(Number(value))) {
        return EMPTY;
      }
      return Number(value).toLocaleString(undefined, {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
      });
    },

    /** Two decimal places with thousands separators for all displayed unit quantities. */
    quantity: function (value) {
      if (isBlank(value) || isNaN(Number(value))) {
        return EMPTY;
      }
      return Number(value).toLocaleString(undefined, {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
      });
    },

    /** A signed amount, so a loss reads as "-1,234.00" rather than "1,234.00". */
    signedMoney: function (value) {
      if (isBlank(value) || isNaN(Number(value))) {
        return EMPTY;
      }
      var n = Number(value);
      return (n > 0 ? "+" : "") + this.money(n);
    },

    /** Oracle JET text colour class for a profit/loss figure. */
    profitLossClass: function (value) {
      var n = Number(value);
      if (isNaN(n) || n === 0) {
        return "";
      }
      return n < 0 ? "oj-text-color-danger" : "oj-text-color-success";
    },

    /** `2026-08-04T21:30:27.894` -> `2026-08-04 21:30`. Backend sends local date-times. */
    dateTime: function (value) {
      if (isBlank(value)) {
        return EMPTY;
      }
      return String(value).replace("T", " ").slice(0, 16);
    },

    /** `2026-08-04` unchanged; tolerates a full date-time. */
    date: function (value) {
      return isBlank(value) ? EMPTY : String(value).slice(0, 10);
    },

    /**
     * `variant` for oj-c-badge, for any status value the backend uses.
     * The class-based statusClass below stays for markup that still uses plain spans.
     */
    statusVariant: function (status) {
      switch (status) {
        case "ACTIVE":
        case "COMPLETED":
        case "VERIFIED":
          return "success";
        case "PENDING":
          return "warning";
        case "FAILED":
        case "REJECTED":
        case "BLOCKED":
          return "danger";
        case "INACTIVE":
        case "CLOSED":
        case "SUSPENDED":
          return "neutral";
        default:
          return "neutralSubtle";
      }
    },

    /** "Priya Raman" -> "PR". Feeds oj-c-avatar where there is no image to show. */
    initials: function (fullName) {
      if (isBlank(fullName)) {
        return "?";
      }
      var parts = String(fullName).trim().split(/\s+/);
      var first = parts[0].charAt(0);
      var last = parts.length > 1 ? parts[parts.length - 1].charAt(0) : "";
      return (first + last).toUpperCase();
    },

    /** BUY / SELL badge colour. Neither is good or bad, so both read as informational. */
    tradeTypeVariant: function (transactionType) {
      return transactionType === "SELL" ? "warningSubtle" : "infoSubtle";
    },

    /**
     * A plain number, defaulting to 0.
     *
     * Views need this because JET evaluates binding expressions through a CSP-safe
     * evaluator that exposes only the binding context - global functions are not in
     * scope, so `Number(x)` inside a template throws "Variable Number is undefined"
     * and silently kills the surrounding render. Reaching it through `format` works
     * because `format` is a view model member.
     */
    numeric: function (value) {
      var n = Number(value);
      return isNaN(n) ? 0 : n;
    },

    /** A percentage with one decimal, e.g. 12.5 -> "12.5%". */
    percent: function (value) {
      if (isBlank(value) || isNaN(Number(value))) {
        return EMPTY;
      }
      return Number(value).toFixed(1) + "%";
    },

    /** Badge colour for any status value the backend uses. */
    statusClass: function (status) {
      switch (status) {
        case "ACTIVE":
        case "COMPLETED":
        case "VERIFIED":
          return "oj-badge oj-badge-success";
        case "PENDING":
          return "oj-badge oj-badge-warning";
        case "FAILED":
        case "REJECTED":
        case "BLOCKED":
          return "oj-badge oj-badge-danger";
        default:
          return "oj-badge";
      }
    }
  };
});
