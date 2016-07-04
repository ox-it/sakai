var BindYouTubeSearchToContainer = function(container, searchResults, result) {
  // initialization
  var init = function() {
    bindToContainer();
    pushFormIntoIframe();
    closeDialogOnResultClick();
    fixFrameForChrome();
  };

  // initial binding
  var bindToContainer = function() {
    // initial binding
    container.itemSearch({
      service: YouTubeSearchService,
      resultsContainer: searchResults,
      displayResult: result.display,
      pagination: 1
    });

    // add css classes
    container.find('input').addClass('searchQuery cke_dialog_ui_input_text')
                          .attr('placeholder', 'Search here...');
    container.find('a').addClass('searchButton cke_dialog_ui_button cke_dialog_ui_button_ok')
                      .html('<span class="icon"></span>');
  };

  // now initialize iframe which will isolate the search field (so we can submit
  // the form without closing the dialog)
  var pushFormIntoIframe = function() {
    var iframe = $('<iframe src="about:blank"></iframe>').attr('id', 'youTubeSearchIframe');
    container.after(iframe);

    iframe.load(function() {
      var contents = $(this).contents();

      // fix iframe css
      contents.find('head').append($('head script, head link').clone());
      contents.find('body').html(container);
      contents.find('body').css({ padding: 0, width: '100%' });
    });
  };

  var closeDialogOnResultClick = function() {
    searchResults.on('click', '.result', function(e) {
      $('#youTubeDialog .searchResultId').val($(this).data('src'));
      clickDialogOK();
      e.preventDefault();
    });
  };

  var clickDialogOK = function() {
    var ckDialog = window.CKEDITOR.dialog.getCurrent();
    var ckOk = ckDialog._.buttons['ok'];
    ckOk.click();
  };

  // the iframe in chrome is empty
  // but the contents still appear before the frame
  // so we need to hide the frame in chrome
  var fixFrameForChrome = function() {
    var browser = navigator.userAgent;

    if (browser.indexOf("Chrome") > -1) {
      $('#youTubeSearchIframe').hide();
    }
  }

  // run initialization
  init();
};
