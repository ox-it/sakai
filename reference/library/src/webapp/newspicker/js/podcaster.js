/**
 * Implements RSS Podcast chunked parsing and provides dynamic loading for 
 * a content container. Also loads the template for displaying podcasts.
 *
 * TODO
 *  - Optimize scroll condition
 */
 
/* array.filter fix */
if (!Array.prototype.filter){Array.prototype.filter=function(fun){var len=this.length;if(typeof fun!="function") throw new TypeError();var res = new Array();
    var thisp = arguments[1];for (var i=0;i<len;i++){if (i in this){var val = this[i];if (fun.call(thisp,val,i,this)) res.push(val);}}return res;};}

/* Timer */
function getTime() { return new Date().getTime(); }

/* Log */
function console_log() {
    //console.log.apply(console, console_log.arguments); 
    }

// encapsulation
function PodcastPickerInit(o) {    
    /* Options + defaults */
    OPTIONS = $.extend({
        // important settings
        'rssFile': 'podcasts.xml',
        'chunkSize': 8,
        'genericThumbnails': false,      // set to true to use generic audio/video etc icons
        'filename': 'podcaster.js',      // name of this file
        // callbacks
        'onSelect': function(p) {},
        'onShow': function(d) {},
        'onHide': function(d) {},
        'onReady': function() {},
        // other settings
        'selectElement': '.podcast',
        'id': 'PodcastPickerGUIContainer',
        'podcastTemplate' : '../podcast.thtml',    // relative to the js file
        'pickerTemplate': '../picker.thtml',       // relative to the js file
        'defaultSortField': 'date',
        'defaultSortAsc': false
    }, o);

    var HIGHLIGHT_REPLACE = '<em>$1</em>';

    // initialized when XML file returned
    var XPATH = {};

    /* Flags */
    var _Ready_ = {
        Template: false,
        PickerTemplate: false,
        Data: false
    };
    var _LoadNext_ = false; // used as request to load next chunk. Set to true by scroll condition if mouse is down
    var _MouseDown_ = false; // used to check if scrolling is done by mouse

    // GUI components
    var Picker = null;
    var DisplayContainer = null;
    var ScrollContainer = null;
    var LoadingElement = null;

    var PodcastDataRoot = null;         // podcast data xml root node
    var PodcastData = null;             // all podcast data
    var CurrentPodcastData = null;      // currently used podcast data set
    var Highlight_Re = null;            // highlighter regexp
    var Search_Re = null;               // search regexp
    var Search_Desc = null;             // indicates whether to search descriptions as well
    var Type_Filter = '';               // type filter
    var Sort_Field = OPTIONS['defaultSortField'];            // sort field
    var Sort_Asc = OPTIONS['defaultSortAsc'];               // default sort order
    var Div_Filter = '';                // division filter
    
    var TriggerElement = null;          // Element that triggered the GUI
    var handleSelect = OPTIONS['onSelect'];
    /* Global Functions */
    function bootLoader(flag) {
        // attempts to start the main script, but only if all the flags in _Ready_ are true
        // if a flag is passed, that flag is set to true before attempting bootload
        if (typeof(flag) != 'undefined') _Ready_[flag] = true;
        f = true;
        $.each(_Ready_, function(k, v) {
            return f = v;
        });
        if (f) main();    
    }

    /* Returns a full dataset, sorted according to given criteria */
    var _sortFunctions = {
        'titleA': function(a,b) { return a.title.localeCompare(b.title); },
        'titleD': function(a,b) { return b.title.localeCompare(a.title); }
    };
    var _getSortedDataCache = {};
    function getSortedData(field, asc) {
        key = field + (asc ? "A" : "D");
        if (!_getSortedDataCache.hasOwnProperty(key)) {
            DataCopy = PodcastData.slice(0);
            DataCopy.sort(_sortFunctions[key]);
            _getSortedDataCache[key] = DataCopy;
        }
        return _getSortedDataCache[key];
    }
    // updates current data set according to Sort_Field and Sort_Asc
    function updateSortData() {;
        CurrentPodcastData = getSortedData(Sort_Field, Sort_Asc);
    }
    
    // determines if a path is absolute - STUB
    function isAbsolute(path) { 
        return false;
    }
    
    // function, that returns a path relative to the JS file
    var relativePath = (function() {
        var prefix = $('script[src$=' + OPTIONS.filename + ']').attr('src').replace(RegExp(OPTIONS.filename + '$'), '');
        return function(path) {
            if (isAbsolute(path)) return path;
            return prefix + path;
        };
    })();

    // loads 'count' podcasts matching the filter
    var chunkLoadPodcasts = (function() {
        var _chunkDataSet = null;  // current data set
        var _filter_fn = function(data) {
            return (!Search_Re || (data.title.match(Search_Re) || (Search_Desc && data.description.match(Search_Re)))) &&
                   (!Type_Filter || (data.type == Type_Filter)) &&
                   (!Div_Filter || (data.division == Div_Filter))
        }
        return function(restart) {
            var st = getTime();
            var count = OPTIONS['chunkSize'];
            var total = null;
            if (restart) {
                _chunkDataSet = CurrentPodcastData.filter(_filter_fn);
                total = _chunkDataSet.length;
            }
            while (_chunkDataSet.length > 0 && count > 0) {
                var data = _chunkDataSet.shift();
                data.fullRead();
                DisplayContainer.trigger('appendPodcast', [data]);
                count--;
            }
            
            LoadingElement.hide();
            console_log("Took: ", getTime() - st,"ms");
            return total;
        }
    })();
       
        
    // full read function, reads in data for display
    function _nullFunc() {};
    function _fullRead() {
        itemEl = this.el;    
        thumbNode = itemEl.find(XPATH.THUMBNAIL);
        // this.updated = itemEl.find('[nodeName=atom:updated]').text(),
        this.thumbnail_url = thumbNode.attr('url');
        this.updated = itemEl.find(XPATH.UPDATED).text().substring(0,10);
        this.division_label = itemEl.find(XPATH.CATEGORY).attr('label');
        this.faculty_label = itemEl.find(XPATH.FACULTY).attr('label');
        this.fullRead = _nullFunc;  // replace fullRead with an empty func
    }
    // partial read function, reads in data for filtering/search
    function _partialRead() {
        itemEl = this.el;
        
        type = itemEl.find('guid').text().match(/(-)([A-z]*)$/)[2];
        if (type == 'podcasts') type = 'audio';
        this.type = type;
        
        this.division = itemEl.find(XPATH.CATEGORY).attr('term');
        this.description = itemEl.find('description').text();
        this.partialRead = _nullFunc;
    }
    // pre-reads data necessary for initial sorting, from the <item> node
    // to partially read in data for filtering, invoke partialRead
    // to fully read in data for display, invoke fullRead
    function preloadPodcastData(itemEl, i) {
        return {
            index: i,           // item index, usually passed by the parent loop structure (seems useless)
            title: itemEl.find('title').text(),
            el: itemEl,
            fullRead: _fullRead,
            partialRead: _partialRead
        };
    }
    // reads all important podcast data from a preread podcast data

    
    // load instances of the podcast display template and the picker
    var Template;
    $.get(relativePath(OPTIONS.podcastTemplate), function(data, st, xhr) {
        Template = $(data);
        bootLoader('Template');
    });
    
    /** 
        PICKER INITIALIZATION + INTERFACE SETUP
        
        Inject the template straight into the body and rig up the interface
    */
    $.get(relativePath(OPTIONS.pickerTemplate), function(data, st, xhr) {
        
        if (st = 'success') {
            Picker = $(document.createElement('div')).attr('id', OPTIONS['id']).addClass('jqmWindow PickerGUI').append($(data));
            Picker.jqm({
                modal: true,
                onShow: function(hash) { hash.w.fadeIn(function() { OPTIONS['onShow'](hash.w); });},
                onHide: function(hash) { hash.w.fadeOut(100, function() { hash.o.fadeOut(); OPTIONS['onHide'](hash.w); });}
            });
            // rig up close button
            Picker.jqmAddClose(Picker.find('a.closeDialog'));
            
            
            
            // inject into body
            $('body').append(Picker);
            
            // select main elements
            DisplayContainer = Picker.find(".container");   // where podcasts are displayed
            ScrollContainer = Picker.find(".viewport");    // where the scroll event is to be intercepted
            LoadingElement = Picker.find('#loading'); // loading notification element
            
            // visualizes a podcast and appends it at the end of the display container
            DisplayContainer.bind('appendPodcast', function(e, podcast) {
                // clone the template
                podcastEl = Template.clone();
                
                title = Highlight_Re ? podcast.title.replace(Highlight_Re, HIGHLIGHT_REPLACE) : podcast.title;
                description = (Highlight_Re && Search_Desc) ? podcast.description.replace(Highlight_Re, HIGHLIGHT_REPLACE) : podcast.description;
                
                
                // fill it
                podcastEl
                    .find('.title').html(title).end()
                    .find('.updated').text(podcast.updated).end()
                    .find('.division').text(podcast.division_label).end()
                    .find('.type').text(podcast.type).end()
                    .find('.faculty').text(podcast.faculty_label).end()
                    .find('.description').html(description).end()
                ;
                
                // If generic thumbnails are used then don't even attempt to load the originals.
                // Otherwise IE starts complaining about mixed content.
                if (OPTIONS['genericThumbnails']) {
                    podcastEl
                        .find('.thumbnail').hide().end()
                        .find('.genericPlaceHolder').addClass(podcast.type + ' GenericType thumbnail').end()
                    ;
                    
                } else {
                	podcastEl.find('.thumbnail').attr('src', podcast.thumbnail_url).end()
                }
                
                // add data
                podcastEl.data('data', podcast);
                
                // UI it
                // podcastEl.find('button').button();
                
                // append it
                $(this).append(podcastEl);
            });
            
            // reload content, trigger this event whenever content criterion has been changed
            DisplayContainer.bind('update', function(e) {
                ScrollContainer.scrollTop(0);
                $(this).empty();
                var count = chunkLoadPodcasts(true);
                $('.matches_count b').text(count);
                Picker.find('.no_results').css('display', (count == 0) ? 'block' : 'none');
            });
             
            // rig up select button
            Picker.find(OPTIONS['selectElement']).live('click', function(e) {
                podcast = $(this).closest('.podcast').data('data');
                handleSelect.apply(TriggerElement, [podcast]);
                Picker.jqmHide();
            });
            
            /* Rig up search */
            function searchFunction(e) {        
                term = $('#podcast_search_box').val().replace(/^\s*/, '').replace(/\s*$/, '');
                if (term.length > 0) {       
                    // detect case sensitivity and set regexp flag
                    cs_fl = ($('#podcast_search_cs').is(':checked')) ? '' : 'i';
                    Search_Desc = ($('#podcast_search_desc').is(':checked'));
                    // set up Search_Re and Highlight_Re
                    Search_Re = RegExp(term.replace(/ +/g, ".*"), cs_fl);    // replace series of spaces as anything in between
                    Highlight_Re = RegExp('(' + term.replace(/ +/g, "|") + ')', "g" + cs_fl);
                } else {
                    Search_Re = null;
                    Highlight_Re = null;
                }        
                DisplayContainer.trigger('update');
            }
            
            deferredSearchFn = $.defer(450, 'deferredSearchTimer', searchFunction);
            immediateSearchFn = $.defer(1, 'deferredSearchTimer', searchFunction);
            $('#podcast_search_box').bind('deferred keyup', function(e) {
                if ((e.keyCode || e.which) == 13) {
                    immediateSearchFn(e);
                } else {
                    deferredSearchFn(e);
                }
            });
            $('#podcast_search_box').bind('search', searchFunction);
            $('#podcast_search_cs').bind('click', searchFunction);  // rig case-sensitivity checkbox
            $('#podcast_search_desc').bind('click', searchFunction);  // rig search descriptions checkbox
            
            /* 7. Rig up Filter + Icons */
            $('#podcast_type_filter_all').attr('checked', true);
            $('#podcast_type_filter_box').buttonset();
            $('#podcast_type_filter_box input').live('click', function(e) {
                Type_Filter = $('#podcast_type_filter_box input:checked').val();
                DisplayContainer.trigger('update');
            });
            $('#podcast_type_filter_all').button('option', 'icons', {primary: 'ui-icon-signal-diag'});
            $('#podcast_type_filter_audio').button('option', 'icons', {primary: 'ui-icon-volume-on'});
            $('#podcast_type_filter_video').button('option', 'icons', {primary: 'ui-icon-video'});
            $('#podcast_type_filter_document').button('option', 'icons', {primary: 'ui-icon-document'});
            // set default filter
            
            /* 8. Rig up sort */
            $('#podcast_sort_box').buttonset();
            $('#podcast_sort_box button').bind('refresh', function() {
                $(this).siblings().removeClass('selected').each(function() {
                    $(this).button('option', 'icons', 
                        $.extend($(this).button('option', 'icons') ,{ secondary: false})
                    )
                });
                $(this).addClass('selected').button('option', 'icons', 
                    $.extend($(this).button('option', 'icons'), { secondary: Sort_Asc ? "ui-icon-triangle-1-n" : "ui-icon-triangle-1-s"})
                );
            }).click(function(e) {
                newField = $(this).attr('id').match(/(-)([a-z]*)$/)[2];
                if (newField == Sort_Field) {
                    // toggle direction
                    Sort_Asc = !Sort_Asc;
                } else {
                    Sort_Asc = true;
                    Sort_Field = newField;
                }
                updateSortData();
                $(this).trigger('refresh');
                DisplayContainer.trigger('update');
            });
            $('#podcast_sort_by-title').button('option', 'icons', {primary: 'ui-icon-grip-dotted-horizontal'});
            $('#podcast_sort_by-date').button('option', 'icons', {primary: 'ui-icon-clock'});
            
            /* 9. Division Filtering */
            $('#podcast_division_filter').val('');
            $('#podcast_division_filter').change(function(e) {
                Div_Filter = $(this).val();
                DisplayContainer.trigger('update');
            });
            
            
            // announce completion
            bootLoader('PickerTemplate'); 
        } // if (st = 'success')
    });
    

    /*
        Grab the RSS file and pre-process it
    */
    $.ajax({
        url: OPTIONS.rssFile, 
        dataType: 'xml',
        success: function(data) {
            st = getTime();
            PodcastDataRoot = $(data);  // store xml - DO NOT REMOVE OR SAFARI COMPLAINS
            
            // test xpath selection capabilites
            xpath_backslash = ($(data).find('item:first').find('atom\\:updated').length > 0);
            XPATH.THUMBNAIL = xpath_backslash ? 'media\\:thumbnail' : '[nodeName=media:thumbnail]';
            XPATH.CATEGORY = xpath_backslash ? 'atom\\:category:first' : '[nodeName=atom:category]:first';
            XPATH.FACULTY = xpath_backslash ? 'atom\\:category:eq(1)' : '[nodeName=atom:category]:eq(1)';
            XPATH.UPDATED = xpath_backslash ? 'atom\\:updated' : '[nodeName=atom:updated]';
            
            // preprocess the data - extract information that will used
            // in top-level handling
            PodcastData = [];
            $(data).find('item').each(function(i) {
                data = preloadPodcastData($(this), i);
                data.partialRead();
                PodcastData.push(data);
            });
            
            // update sorted cache
            _getSortedDataCache['dateD'] = PodcastData;
            DataCopy = PodcastData.slice(0);
            DataCopy.reverse();
            _getSortedDataCache['dateA'] = DataCopy;
             
            updateSortData();            
            
            console_log("Preprocessing: ", getTime() - st,"ms");
            bootLoader('Data');
        }
    });

    /* Main Script is run only when all items are ready */
    var control_Obj = {
        trigger: function(el) {
            TriggerElement = el;
            Picker.jqmShow();
        }
    };
    
    function main() {  
        $('#podcast_sort_by-' + OPTIONS['defaultSortField']).trigger('refresh');
        $('#podcast_search_box').trigger('search');
        
        // rig up scroll event
        ScrollContainer.scroll(function(e) {
            // scroll condition (note the -100 which loads stuff slightly earlier)
            if (ScrollContainer.scrollTop() >= DisplayContainer.height() - ScrollContainer.height() - 100) {
                // allow on-mouse-up loading - strangely, this only works on firefox
                if ($.browser.mozilla && _MouseDown_) {
                    _LoadNext_ = true;
                    LoadingElement.show();
                } else chunkLoadPodcasts();
            }
        });
        
        ScrollContainer.mousedown(function(e) {
            _MouseDown_ = true;
        });
        
        ScrollContainer.mouseup(function(e) {
            if (_LoadNext_) chunkLoadPodcasts();
            _LoadNext_ = false;
            _MouseDown_ = false;
        });
        
        // call ready callback on a 'control object', a proxy to control the podcast picker
        OPTIONS['onReady']({
            trigger: function(el) {
                // watch out for elements inside the picker GUI!                
                if ($(el).closest('#' + OPTIONS.id).length != 0) return;
                TriggerElement = el;
                Picker.jqmShow();
            }
        });
    }
    
    // return a 'summary' object
    return {
        getStatus: function() {
            return _Ready_;
        }
    }
}
