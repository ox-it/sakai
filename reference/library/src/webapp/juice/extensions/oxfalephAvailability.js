// oxfalephAvailability.js
// -------------------
// Version: 0.5
// Author: Owen Stephens
// Last Edit: 23/08/2015

// Displays availability from oxfaleph compliant information

/*
 * Constructor arguments:
 * arg: ju - instance of juice
 * arg: insert - JuiceInsert to use
 * arg: targetDiv - id of element to place image in
 * arg: availIDs - Juice Meta element containing array of IDs for oxfaleph requests
 * arg: availServer - url of availability server
 */

//***************************
//******** Notes ************
//***************************
//
//No notes
//
//***************************

function oxfalephAvailability(ju,insert, targetDiv, availIDs, availServer){
    // Initialise extension
    id = "oxfalephAvailability";
    this.targetDiv = targetDiv;
    this.availIDs = availIDs; // Which IDs from the MetaValues to pass to the oxfaleph availability service
    this.availServer = availServer; // URL of the oxfaleph compliant availability service
    this.format = "jsonp"; // Currently only supports JSONP

    initFunc = this.start;
    if(arguments.length){
        oxfalephAvailability.superclass.init.call(this,id,initFunc,null,insert,ju);
        oxfalephAvailability.superclass.startup.call(this);
    }
}

oxfalephAvailability.prototype = new JuiceProcess();
oxfalephAvailability.prototype.constructor = oxfalephAvailability;
oxfalephAvailability.superclass = JuiceProcess.prototype;

oxfalephAvailability.prototype.start = function(){

    if(juice.hasMeta(this.availIDs)){
        var ids = juice.getMetaValues(this.availIDs);
        for(var i=0; i < ids.length; i++){
            this.div_id = i;
            // check there is a valid id to use
            if (ids[i] != "undefined" && ids[i].length >0) {
                this.avail_url = this.availServer + "?id=" + encodeURIComponent(ids[i]) + "&format=" + this.format;
                this.getoxfaleph(ids[i]);
            }
            else {
                // Do nothing
            }
        }
    }
}

oxfalephAvailability.prototype.getoxfaleph = function(oxfalephId){
    //Could simplify this I suspect
    var This = this;
    var oxfalephId = oxfalephId;
    var url = this.avail_url;
    var id = this.div_id;
    var oxfaleph_div = null;
    try {
        $jq.getJSON(url + '&callback=?', function(data) {
            if(data){
                if(data.error > 0) {
                    juice.debugOutln(data.error);
                    console.log("Error retrieving Aleph availability for "+oxfalephId+" with error message: " + data.error);
                } else {
                    var holdings_html =	'';
                    var holding_html_open = '';
                    var holding_html_content = '';
                    var holding_html_close = '';
                    holding_html_open = '' +'<div class="availabilityHeader" style="color:#002147">' +
                        '<div style="clear:both;"><div class="availabilityTitle" style="margin-top:10px; margin-bottom:5px; float:left;"><strong>Availability</strong></div>'+
                        '<div class="availabilityNav" style="float:right;"><a href="#" onclick="hideAll(\''+oxfalephId+'\')">Hide all Libraries</a>';
                    if(juice.hasMeta("pref_libs")) {
                        holding_html_open += ' / <a href="#" onclick="showPreferred(\''+oxfalephId+'\')">Show preferred Libraries</a></div>';
                    }
                    holding_html_open += ' / <a href="#" onclick="showAll(\''+oxfalephId+'\')">Show all Libraries</a></div>' +
                        '</div></div><!-- end of availabilityHeader div -->' +
                        '<div class="availabilityTable"><table class="oxfaleph_summary daia_summary" cellspacing="0" width="100%" id="'+oxfalephId+'"><thead><tr><th>Library' +
                        '<img alt="Show more libraries" src="/library/image/sakai/expand.gif" ' +
                        'onclick="toggleAvailability(\''+oxfalephId+'\',\'/library/image/sakai/expand.gif?panel=Main\',\'/library/image/sakai/collapse.gif?panel=Main\');">' +
                        '<span id="toggleAvailability">(click arrow to show more availability)</span>' +
                        '</th><th>Shelfmark</th><th>Description</th><th>Lending Status</th><th>Availability</th><th>Library info</th>' +
                        '</tr></thead><tbody class="' + oxfalephId + ' collapsed" style="display:none;">';
                    holding_html_close += '</tbody></table></div><!-- end availabilityTable div -->';


                    var mapHTML ='(map coming soon)';

                    $jq.each(data.document[0].item, function(index, oxfaleph_doc) {
                            holdings_html += '<tr class="'+oxfaleph_doc.libcode+' collapsed'+'"><td>'+oxfaleph_doc.libname+'</td>'+
                                '<td>'+oxfaleph_doc.itemshelf+'</td>'+
                                '<td>'+oxfaleph_doc.itemdesc+'</td>'+
                                '<td>'+oxfaleph_doc.itemtype+'</td>'+
                                '<td>'+oxfaleph_doc.availableitems+' / '+oxfaleph_doc.totalitems+'</td>'+
                                '<td>'+mapHTML+'</td></tr>'; //need to get URL into this row
                        }
                    );
                }
            }
            else {
                juice.debugOutln('Unable to get any availability information using the oxfaleph response: ' + dump(data) + '<br />');
                // To display feedback in the UI, assign an appropriate message to oxfaleph_div here
            }
            juice.debugOutln(dump(data) + '<br />');
            oxfaleph_div = holding_html_open + holdings_html + holding_html_close
            This.displayoxfaleph(oxfaleph_div, id);
            if(juice.hasMeta("pref_libs")) {
                showPreferred(oxfalephId);
            }
        });
    }
    catch (err) {
        juice.debugOutln(err);
    }
}

oxfalephAvailability.prototype.displayoxfaleph = function(oxfaleph_div, id) {
    if(oxfaleph_div) {
        this.showInsert(id);
        this.insert = this.getInsertObject(id);
        this.insert.append(oxfaleph_div);
    }
}

function hideAll(id) {
    console.log("Hiding");
    $jq('#' + id).addClass('collapsed').fadeOut('fast');
}

function showAll(id) {
    $jq('#' + id).removeClass('collapsed').fadeIn('fast');
    $jq('#' + id + '> tbody > tr').removeClass('collapsed').fadeIn('fast');
}

function showPreferred(id) {
    // For each row of availability table:
    //    Check if the class is the right library code and add/remove collapsed
    // Also make sure table showing
    var libs = juice.getMetaValues("pref_libs");
    if($jq('#' + id).is('.collapsed')){
        $jq('#' + id).removeClass('collapsed').fadeIn('fast');
    }
    $jq('#' + id + '> tbody > tr').addClass('collapsed').fadeOut('fast');
    for	(i = 0; i < libs.length; i++) {
        $jq('#' + id + ' > tbody > tr.'+libs[i]).removeClass('collapsed').fadeIn('fast');
    }
}
