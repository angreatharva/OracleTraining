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

    /** Up to 4 decimals, trailing zeros trimmed - holdings can be fractional. */
    quantity: function (value) {
      if (isBlank(value) || isNaN(Number(value))) {
        return EMPTY;
      }
      return Number(value).toLocaleString(undefined, { maximumFractionDigits: 4 });
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
