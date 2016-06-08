/**

Copyright (c) 2013 University of Oxford

All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
* Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
* Neither the name of the University of Oxford nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*/

(function($) {
// fix url to live url when deployed
// e.g. 'library/courses-js-widget/';
var url = 'https://rawgit.com/lokothodida/courses-js-widget/dev/';
var scripts = [
  url + 'lib/dataTables/js/jquery.dataTables.min.js',
  url + 'lib/moment/moment.min.js',
  url + 'js/column.js',
  url + 'js/parametersreader.js',
  url + 'js/options.js',
  url + 'js/widgetui.js',
  url + 'js/oxdatacall.js',
  url + 'js/responseparser.js',
  url + 'js/tablebuilder.js',
  url + 'js/courses-widget.js',
  url + 'js/row.js',
];
var loadedScripts = [];   // keep track of loaded scripts so they don't get loaded repeatedly
var CSSLoaded = false;
var scope = this;         // alias for IIF scope

// jQuery plugin
$.fn.oxfordCoursesWidget = function(options) {
  var settings = {};
  var _this = this;

  var getScripts = function(scripts) {
    //console.log(scriptsLoaded);
    if (scripts.length) {
      var scriptUrl = scripts.shift();

      $.ajax({
        url: scriptUrl,
        async: false,
        dataType: 'script',
        success: function() {
          getScripts(scripts);
        },
        error: function(a,b,c) {
          throw 'Dependency "' + scriptUrl + '" failed to load';
        },
      });
    } else {
      // bring globals into scope
      for (i in OxfordCoursesWidget) {
        var global = OxfordCoursesWidget[i];
        scope[i] = global;
      }

      // load css
      if (!CSSLoaded) {
        add_css('https://static.data.ox.ac.uk/lib/DataTables/media/css/jquery.dataTables.css');
        add_css('https://static.data.ox.ac.uk/courses-js-widget/courses.css');
        CSSLoaded = true;
      }

      settings = $.extend({
        dataTablesConfig: {}
      }, options);

      return _this.each(function(i, e) {
        setUp(e, settings.dataTablesConfig);
      });
    }
  };

  return getScripts(scripts);
};

// now bind the functionality to the containers
var bindToContainers = function() {
  $('.courses-widget-container, [data-researcher-training-tool]').each(function(i, e) {
    var $e = $(e);
    var checkTables = $e.find('table');

    // transform to a widget only if it hasn't already been transformed before
    if (checkTables.length == 0) {
      $e.oxfordCoursesWidget();
    }
  });
};

$(document).ready(bindToContainers);
//bindToContainers();

})(jQuery);
