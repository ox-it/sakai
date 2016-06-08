CKEDITOR.plugins.add('youtube', {
  requires: 'dialog,fakeobjects',
  icons: 'youtube',

  init: function(editor) {
    editor.addCommand('youtube', new CKEDITOR.dialogCommand('youtubeDialog'));
    editor.ui.addButton( 'youtube', {
      label: 'Embed Youtube Video',
      command: 'youtube',
      toolbar: 'insert'
    });

    if (editor.contextMenu) {
      editor.addMenuGroup('youtubeGroup');

      editor.addMenuItem( 'youtubeItem', {
        label: 'Edit Video',
        icon: this.path + 'icons/youtube.png',
        command: 'youtube',
        group: 'youtubeGroup'
      });

      editor.contextMenu.addListener(function(element) {
        if (element && element.is('img') && element.data('cke-real-element-type') == 'div' && element.hasClass('cke_youtube')) {
          return { youtubeItem: CKEDITOR.TRISTATE_OFF };
        }
      });
    }

    // ensure the contents css is an array
    if (!Array.isArray(CKEDITOR.config.contentsCss)) {
      CKEDITOR.config.contentsCss = [CKEDITOR.config.contentsCss];
    }

    var css = [
      this.path + 'css/contents.css',
      '//maxcdn.bootstrapcdn.com/font-awesome/4.1.0/css/font-awesome.min.css',
    ];

    // only load css if they aren't already in the array
    for (i in css) {
      if (CKEDITOR.config.contentsCss.indexOf(css[i]) < 0) {
        CKEDITOR.config.contentsCss.push(css[i]);
      }
    }

    CKEDITOR.dialog.add( 'youtubeDialog', this.path + 'dialogs/youtube.js' );
  },

  afterInit: function(editor) {
    var dataProcessor = editor.dataProcessor;
    var dataFilter = dataProcessor && dataProcessor.dataFilter;

    if (dataFilter) {
      dataFilter.addRules({
        elements: {
          div: function(element) {
            var returnedElement = element;

            if (element.attributes['data-youtube-embed']) {
              returnedElement = editor.createFakeParserElement(element, 'cke_youtube', 'div', false);
            }
            return returnedElement;
          }
        }
      });
    }
  }
});
