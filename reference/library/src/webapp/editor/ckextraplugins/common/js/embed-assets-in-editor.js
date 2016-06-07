/**
 * Embed javascript and css assets in the editor content
 * @param params
 *    @param editor
 *    @param id
 *    @param scripts
 *    @param stylesheets
 */
embedAssetsInCKEditor =  function(params) {
  params = $.extend({
    scripts: [],
    stylesheets: [],
  }, params);
  var data = params.editor.getData();
  var $data = $('<div>').append($(data));
  var scripts = [];
  var stylesheets = [];
  var assets = $data.find('#' + params.id);
  var container = (assets.length) ? assets.detach().empty() : $('<div/>').attr({ id: params.id }).hide();

  // load scripts
  for (i in params.scripts) {
    container.append(
      $('<script>').attr({
        type: 'text/javascript',
        src: params.scripts[i],
      })
    );
  }

  // load stylesheets
  for (i in params.stylesheets) {
    // we append a script that loads the css because ckeditor validates
    // <link> tags to not be included in <div> (meaning that they get
    // pushed out of the container)
    container.append(
      $('<script>').attr({
        type: 'text/javascript',
      }).html(
        '$("<link/>", {' +
        '   rel: "stylesheet",' +
        '   type: "text/css",' +
        '   href: "' + params.stylesheets[i] + '"' +
        '}).appendTo("head");'
      )
    );
  }

  $data.prepend(container);

  // add to the current ckeditor instance
  var instance = params.editor.name;
  CKEDITOR.instances[instance].setData($data.html());
};
