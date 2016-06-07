var BindCreativeCommonsImageSearchToDialog = function(path, dialog) {
  var container = $('#creativeCommonsImageSearchForm');
  var searchResults = $('#creativeCommonsImageSearchResults');
  var result = new CreativeCommonsImageSearchResult(path);

  // initialization
  var init = function() {
    bindToContainer();
    pushFormIntoIframe();
    changeDialogOnResultClick();
  };

  // initial binding
  var bindToContainer = function() {
    var id     = $('<input name="id" type="hidden" id="creativeCommonsImageSearchResultId" / >');
    var type = $('<select name="type"/ >');
    var options = ['text', 'tags'];

    for (i in options) {
      var option = options[i];
      type.append($('<option/>').html(option).val(option));
    }

    container.itemSearch({
      service: CreativeCommonsImageSearchService,
      resultsContainer: searchResults,
      displayResult: result.display,
      registerElements: [
        id,
        type,
      ]
    });

    container.find('input').addClass('searchQuery cke_dialog_ui_input_text');
    container.find('select').addClass('cke_dialog_ui_input_select');
    container.find('a').addClass('searchButton cke_dialog_ui_button cke_dialog_ui_button_ok');
    container.find('input').first().attr('placeholder', 'Title');
  };

  // now initialize iframe which will isolate the search field (so we can submit
  // the form without closing the dialog)
  var pushFormIntoIframe = function() {
    var iframe = $('<iframe src="about:blank"></iframe>').attr('id', 'creativeCommonsImageSearchIframe');
    container.after(iframe);

    iframe.load(function() {
      var contents = $(this).contents();

      contents.find('head').append($('head script, head link').clone());
      contents.find('body').html(container);
      contents.find('body').css({ padding: 0, width: '100%' });
    });
  };

  var changeDialogOnResultClick = function() {
    searchResults.on('click', '.sizes a', function(e) {
      dialog.setValueOf('settings', 'src', $(this).attr('href'));
      dialog.setValueOf('settings', 'credit', $(this).closest('.sizes').data('title'));
      dialog.selectPage('settings');
      e.preventDefault();
    });
  };

  init();
}
