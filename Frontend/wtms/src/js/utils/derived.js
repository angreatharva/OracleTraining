/**
 * Derived observable arrays that can back an ArrayDataProvider.
 *
 * ArrayDataProvider accepts an Array or a ko.observableArray and nothing else - its
 * `_isObservableArray` check requires `destroyAll`, which a ko.computed does not have, so
 * handing it a pureComputed throws "Invalid data type" at construction time.
 *
 * That matters here because most chart and message data on these screens *is* derived:
 * reshaped from an array the screen already loaded. `array()` bridges the gap - it runs the
 * derivation in a computed and pushes each result into a real observableArray, so the
 * provider both accepts it and re-reads it whenever the source changes.
 *
 *     self.rows = derived.array(function () {
 *       return self.positions().map(toChartRow);
 *     });
 *     self.rowsDP = new ArrayDataProvider(self.rows, { keyAttributes: "id" });
 *
 * The result reads exactly like the pureComputed it replaces - `self.rows()` returns the
 * array, so `.length` and `.filter` in a view keep working unchanged.
 */
define(["knockout"], function (ko) {
  "use strict";

  return {
    /**
     * @param {Function} compute returns the derived array; re-runs on any observable it reads
     * @returns {ko.ObservableArray} kept in sync with `compute`
     */
    array: function (compute) {
      var target = ko.observableArray([]);

      // Retained on the observable itself so the computed is not collected while the
      // observable it feeds is still alive, and so screens can dispose it if they need to.
      target.derivation = ko.computed(function () {
        target(compute() || []);
      });

      return target;
    }
  };
});
