// oxfelectronicAvailability.js
// -------------------
// Version: 0.5
// Author: Owen Stephens
// Last Edit: 09/10/2015

// Displays availability from oxfelectronic compliant information

/*
 * Constructor arguments:
 * arg: ju - instance of juice
 * arg: insert - JuiceInsert to use
 * arg: targetDiv - id of element to place image in
 * arg: availIDs - Juice Meta element containing array of COINS (http://ocoins.info)
 * arg: availServer - url of availability server
 */

//***************************
//******** Notes ************
//***************************
//
//No notes
//
//*************************** 

function oxfelectronicAvailability(ju,insert, targetDiv, availIDs, availServer){
    // Initialise extension
    id = "oxfelectronicAvailability";
    this.targetDiv = targetDiv;
    this.availIDs = availIDs; // Which IDs from the MetaValues to pass to the oxfelectronic availability service
    this.availServer = availServer; // URL of the availability service
    this.format = "jsonp"; // Currently only supports JSONP

    initFunc = this.start;
    if(arguments.length){
        oxfelectronicAvailability.superclass.init.call(this,id,initFunc,null,insert,ju);
        oxfelectronicAvailability.superclass.startup.call(this);
    }
}

oxfelectronicAvailability.prototype = new JuiceProcess();
oxfelectronicAvailability.prototype.constructor = oxfelectronicAvailability;
oxfelectronicAvailability.superclass = JuiceProcess.prototype;

oxfelectronicAvailability.prototype.start = function(){
    if(juice.hasMeta(this.availIDs)){
        var ids = juice.getMetaValues(this.availIDs);
                
        for(var i=0; i < ids.length; i++){
            this.div_id = i;
            // check there is a valid id to use
            if (ids[i] != "undefined" && ids[i].length >0) {
                this.avail_url = this.availServer + "?id=" + encodeURIComponent(ids[i]) + "&format=" + this.format;
                this.getoxfelectronic();
            }
            else {
                // Do nothing
            }
        }
    } 
}

oxfelectronicAvailability.prototype.getoxfelectronic = function(){
    var This = this;
    var url = this.avail_url;
    var id = this.div_id;
    var oxfelectronic_div = null;
    var holdings_html = '';

/*
// Can't get OA link from availability service currently
    if (rftId && rftId.search(/primo_library/) > 0) {
        var primoAvailUrl = this.availServer + "?id=" + encodeURIComponent(rftId) + "&format=" + this.format;;
        //try availability via Primo first
        // if successful return and exit
        try {
            $jq.getJSON(primoAvailUrl + '&callback=?', function(data) {
                if(data){
                    $jq.each(data.document, function(index, oxfelectronic_doc) {
                        if (oxfelectronic_doc.error) {
                            juice.debugOutln(oxfelectronic_doc.error);
                            // To display feedback in the UI, assign an appropriate message to oxfelectronic_div here
                        } else {
                            //iterate through items in DAIA doc to get any HREFs
                        }
                    });
                } else {
                    juice.debugOutln('Unable to get any availability information using the oxfelectronic response: ' + dump(data) + '<br />');
                    // To display feedback in the UI, assign an appropriate message to oxfelectronic_div here
                }
                juice.debugOutln(dump(data) + '<br />');
                //return HREF and exit
            });
        }
        catch (err) {
            juice.debugOutln(err);
        }
    }
*/
    //if we get here, fall back on OpenURL to EIAS
    try {
        $jq.getJSON(url + '&callback=?', function(data) {
            if(data){
                $jq.each(data.document, function(index, daia_doc) {
                    if (daia_doc.error) {
                        juice.debugOutln(daia_doc.error);
                        // To display feedback in the UI, assign an appropriate message to oxfelectronic_div here
                    }
                    else {
                        oxfelectronic_div = This.electronic_version(daia_doc);
                    }
                });
            }
            else {
                juice.debugOutln('Unable to get any availability information using the DAIA response: ' + dump(data) + '<br />');
                // To display feedback in the UI, assign an appropriate message to oxfelectronic_div here
            }
            juice.debugOutln(dump(data) + '<br />');
            This.displayDaia(oxfelectronic_div, id);
        });
    }
    catch (err) {
        juice.debugOutln(err);
    }
}

oxfelectronicAvailability.prototype.electronic_version = function(daia_doc){
    var This = this;
    
    // Specific approach to DAIA which assumes all items are online and so URLs go to full text
    var e_html = '';
    var item_count = This.item_count(daia_doc);
    if (typeof(item_count) == 0) {
        juice.debugOutln('No items in DAIA document: ' + dump(daia_doc) + '<br />');
        // To display feedback on the lack of online availability in the UI, assign an appropriate message to e_html here
    } else if (item_count > 1) {
        //Use SFX link
        e_html += '<a target="_blank" href="' + daia_doc.id + '">Electronic version</a>';
    } else {
        //Display the link from the single item
        $jq.each(daia_doc.item, function(index, daia_item) {
            if (daia_item) {
                e_html += '<a target="_blank" href="' + This.item_href(daia_item) + '">Electronic version</a>';
            }
        });
    }
    ret = e_html;
    return ret;
}

oxfelectronicAvailability.prototype.oxfelectronic_err = function(oxfelectronic_doc){
    var This = this;
    var ret = '';
    var error = '';
    error = oxfelectronic_doc.error.content + ": " + oxfelectronic_doc.error.errno;
    ret = error;
    return ret;
}

oxfelectronicAvailability.prototype.href = function(oxfelectronic_doc){
    var ret = '';
    if (oxfelectronic_doc.href) {
        ret =     oxfelectronic_doc.href;
    }
    return ret;
}

oxfelectronicAvailability.prototype.oa_urls = function(oxfelectronic_item){
    var This = this;
    var ret = '';
    var urls = new Array();
        $jq.each(oxfelectronic_item.available, function(index, oxfelectronic_available) {
            if (This.oxfelectronic_service(oxfelectronic_available) == 'openaccess') {
                urls.push(This.oxfelectronic_service_url(oxfelectronic_available));
            }
        });
    ret =     urls;
    return ret;
}

oxfelectronicAvailability.prototype.item_count = function(daia_doc) {
    var ret = '';
    if(typeof(daia_doc.item) == 'undefined') {
        ret = 0;
    } else {
        ret = daia_doc.item.length;
    }
    return ret;
}

oxfelectronicAvailability.prototype.displayDaia = function(daia_div, id) {
    if(daia_div) {
        this.showInsert(id);
        this.insert = this.getInsertObject(id);
        this.insert.append(daia_div);
    }
}

oxfelectronicAvailability.prototype.item_href = function(daia_item){
    var ret = '';
    ret =   daia_item.href;
    return ret;
}

oxfelectronicAvailability.prototype.item_label = function(daia_item){
    var ret = '';
    ret =   daia_item.label;
    return ret;
}

oxfelectronicAvailability.prototype.id = function(daia_doc){
    var ret = '';
    ret =   daia_doc.id;        
    return ret;
}