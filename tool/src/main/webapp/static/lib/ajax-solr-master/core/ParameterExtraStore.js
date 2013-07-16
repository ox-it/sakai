(function (callback) {
  if (typeof define === 'function' && define.amd) {
    define(['core/ParameterStore'], callback);
  }
  else {
    callback();
  }
}(function () {

/**
 * A parameter store that allows extra parameters to be added that the request but not
 * modifiable through the standard calls.
 *
 * <p>Configure the manager with:</p>
 *
 * <pre>
 * Manager.setStore(new AjaxSolr.ParameterExtraStore({extra: "fq=field:value"));
 * </pre>

 * @class ParameterExtraStore
 * @augments AjaxSolr.ParameterExtraStore
 */
AjaxSolr.ParameterExtraStore = AjaxSolr.ParameterStore.extend(
  {
  /**
   * The extra parameters that should be added on.
   */
  extra: "",

  /**
   * Returns the Solr parameters as a query string.
   * This implementation adds on an extra bit.
   *
   * <p>IE6 calls the default toString() if you write <tt>store.toString()
   * </tt>. So, we need to choose another name for toString().</p>
   */
  string: function() {
    var original = AjaxSolr.ParameterStore.prototype.string.call(this);
    return [original, this.extra].join('&');
  },

});

}));
