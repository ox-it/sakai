/**
  * Title:        VimeoEmbed
  * Description:  Turn any element into an embedded Vimeo Video
  * Author:       Lawrence Okoth-Odida
  * Version:      0.1
  * Date:         30/07/2014
  
  * BASIC USE
      1. Create a div with a class (e.g. 'vimeo-embed') and give it the pseudo
         data attribute src (e.g. 'data-src="vidID"'), where its value is the id
         of the Vimeo video
      2. Call vimeoEmbed on the div (e.g. $('.vimeo-embed').vimeoEmbed())

  * ADVANCED USE
      vimeoEmbed takes an object literal as a parameter for the options. Set the
      defaults for the videos on the page with them. For example, if you want
      all the videos on the page to be large and allow for full screen, use:

        $('.vimeo-embed').vimeoEmbed({
          width: 1280,
          height: 720,
          allowfullscreen: true
        });

      Available parameters are src, height, width, frameborder, allowfullscreen,
      startfrom and autoplay.

  * @param {object} options are the page-wide defaults for any given video
  */
(function($) {
  $.fn.vimeoEmbed = function(options) {
    // settings
    var settings = $.extend({
      src:             '',
      width:           560,
      height:          315,
      frameborder:     0,
      allowfullscreen: false,
      startfrom:       '0s',
      autoplay:        false
    }, options);
    
    /**
      * This pulls the parameters out of the object we are replacing
      * @param {object} embed is an element selected for replacement
      */
    var getParams = function(embed) {
      return {
        width:           getParam(embed, 'width') || settings.width,
        height:          getParam(embed, 'height') || settings.height,
        src:             getUrl(embed),
        frameborder:     getParam(embed, 'frameborder') || settings.frameborder,
        allowfullscreen: getParam(embed, 'allowfullscreen') || settings.allowfullscreen,
        autoplay:        getParam(embed, 'autoplay') || settings.autoplay,
      }
    }

    /**
      * This gets a single parameter from the object we are replacing
      * @param {object} embed is an element selected for replacement
      * @param {string} param is the name of the parameter being searched for
      * @param {string} def is the default value for the parameter
      */
    var getParam = function($element, param) {
      return $element.attr(param) || $element.attr('data-' + param);
    }

    /**
      * This gets a full vimeo-embed url
      */
    var getUrl = function($element) {
      var url = 'https://player.vimeo.com/video/';

      url += getParam($element, 'src') || settings.src;

      return url;
    }

    /**
      * Build the correct Vimeo Embed markup
      * @param {object} params is a literal of all of the parameter keys and values
      */
    var embedVideo = function(params) {
      var iframe = $('<iframe/>');
      
      // go through each parameter and add it to the iframe
      $.each(params, function(key, value) {
        iframe.attr(key, value);
      });

      return iframe;
    }

    return this.each(function() {
      $item = $(this);
      $item.replaceWith(embedVideo(getParams($item)));
    });
  };

  // automatically bind to data-vimeo-embed divs
  $(document).ready(function() {
    $('[data-vimeo-embed]').vimeoEmbed();
  });
}(jQuery));
