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
            if (ids[i] != "undefined" && ids[i].length >0 && /^oxfaleph\d{9}$/.test(ids[i])) {
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
    var libWeb = libraryWebsites();
    try {
        $jq.getJSON(url + '&callback=?', function(data) {
            if(data){
                if(data.error > 0) {
                    juice.debugOutln(data.error);
                } else {
                    var holdings_html =	'';
                    var holding_html_open = '';
                    var holding_html_content = '';
                    var holding_html_close = '';
                    holding_html_open = '' +'<div class="availabilityHeader">' +
                        ''+
                        '<div class="availabilityNav" style="float:left;"><a href="#" onclick="return hideAll(\''+oxfalephId+'\')">Hide Availability</a>';
                    if(juice.hasMeta("pref_libs")) {
                        holding_html_open += ' / <a href="#" onclick="return showPreferred(\''+oxfalephId+'\')">Show preferred Libraries</a></div>';
                    }
                    holding_html_open += ' / <a href="#" onclick="return showAll(\''+oxfalephId+'\')">Show Availability</a></div>' +
                        '</div><!-- end of availabilityHeader div -->' +
                        '<div style="clear:both;"></div>' +
                        '<div class="availabilityTable collapsed" id="'+oxfalephId+ '" style="display: none" >' +
                        '<div class="availabilityTitle" style="margin-top:10px; margin-bottom:5px; color:#002147"><strong>Availability</strong></div>' +
                        '<table class="oxfaleph_summary daia_summary" cellspacing="0" width="100%"><thead><tr><th>Library' +
                        '</th><th>Shelfmark</th><th>Description</th><th>Lending Status</th><th>Availability</th><th>Library website</th>' +
                        '</tr></thead><tbody  >';
                    holding_html_close += '</tbody></table></div><!-- end availabilityTable div -->';
                    $jq.each(data.document[0].item, function(index, oxfaleph_doc) {
                            var library_info = '';
                            if(libWeb[oxfaleph_doc.libcode]) {
                                library_info = '<a target="_blank" href="'+libWeb[oxfaleph_doc.libcode]+'"><i class="fa fa-info-circle"></i></a>';
                            }
                            holdings_html += '<tr class="'+oxfaleph_doc.libcode+' collapsed'+'"><td>'+oxfaleph_doc.libname+'</td>'+
                                '<td>'+oxfaleph_doc.itemshelf+'</td>'+
                                '<td>'+oxfaleph_doc.itemdesc+'</td>'+
                                '<td>'+oxfaleph_doc.itemtype+'</td>'+
                                '<td>'+oxfaleph_doc.availableitems+' / '+oxfaleph_doc.totalitems+'</td>'+
                                '<td>'+library_info+'</td></tr>';
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
    $jq('#' + id).addClass('collapsed').fadeOut('fast');
    return false;
}

function showAll(id) {
    $jq('#' + id).removeClass('collapsed').fadeIn('fast');
    $jq('#' + id + '> tbody > tr').removeClass('collapsed').fadeIn('fast');
    return false;
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
    return false;
}

function libraryWebsites() {
    var libWeb = {"ANNCL":"http://www.st-annes.ox.ac.uk/about/library","ANTCL":"http://www.sant.ox.ac.uk/current-members/college-library","ANTRE":"http://www.sant.ox.ac.uk/research-centres/russian-and-eurasian-studies-centre","ASCCL":"https://www.asc.ox.ac.uk/library","ASHDL":"http://www.bodleian.ox.ac.uk/sackler","BALDL":"http://www.prm.ox.ac.uk/balfour.html","BJLBL":"http://www.bodleian.ox.ac.uk/bjl","BLFCL":"http://www.bfriars.ox.ac.uk/general/library/","BLLCL":"https://www.balliol.ox.ac.uk/about-balliol/library","BNCCL":"http://www.bnc.ox.ac.uk/","BODBL":"http://www.bodleian.ox.ac.uk/bodley","BOLBL":"http://www.bodleian.ox.ac.uk/","CCLBL":"http://www.bodleian.ox.ac.uk/ccl","CEDBL":"http://www.bodleian.ox.ac.uk/conted","CSLBL":"http://www.bodleian.ox.ac.uk/ccl","EDUBL":"http://www.bodleian.ox.ac.uk/education","EFLBL":"http://www.bodleian.ox.ac.uk/english","HCLBL":"http://www.bodleian.ox.ac.uk/medicine","HCLKC":"http://www.bodleian.ox.ac.uk/medicine","HEBBL":"http://www.ochjs.ac.uk/mullerlibrary/","HFLBL":"http://www.bodleian.ox.ac.uk/history","INDBL":"http://www.bodleian.ox.ac.uk/","LACBL":"http://www.bodleian.ox.ac.uk/lac","LAWBL":"http://www.bodleian.ox.ac.uk/law","MTHBL":"https://www.maths.ox.ac.uk/members/library","OILBL":"http://www.bodleian.ox.ac.uk/oil","PTFBL":"http://www.bodleian.ox.ac.uk/ptfl","RHOBL":"http://www.bodleian.ox.ac.uk/","RSLBL":"http://www.bodleian.ox.ac.uk/science","SACBL":"http://www.bodleian.ox.ac.uk/sackler","SBSBL":"http://www.bodleian.ox.ac.uk/business","SCABL":"http://www.bodleian.ox.ac.uk/anthropology","SSLBL":"http://www.bodleian.ox.ac.uk/ssl","STFBL":"http://www.bodleian.ox.ac.uk/subjects-and-libraries/subjects/staff","TASBL":"http://www.bodleian.ox.ac.uk/taylor","TAYBL":"http://www.bodleian.ox.ac.uk/taylor","THEBL":"http://www.bodleian.ox.ac.uk/ptfl","VHLBL":"http://www.bodleian.ox.ac.uk/vhl","WELBL":"http://www.bodleian.ox.ac.uk/wellcome","ZOOBL":"http://www.bodleian.ox.ac.uk/science/resources/alexander-library","ARABL":"http://www.bodleian.ox.ac.uk/using/disability/aracu","BODCA":"http://www.bodleian.ox.ac.uk/","BODEA":"http://www.bodleian.ox.ac.uk/","BODFA":"http://www.bodleian.ox.ac.uk/","BODHA":"http://www.bodleian.ox.ac.uk/","BODSA":"http://www.bodleian.ox.ac.uk/","BODXA":"http://www.bodleian.ox.ac.uk/","GOOGL":"http://www.bodleian.ox.ac.uk/","SYSBL":"http://www.bodleian.ox.ac.uk/bdlss","BODTS":"http://www.bodleian.ox.ac.uk/","EFLCD":"http://www.bodleian.ox.ac.uk/english","HCLCD":"http://www.bodleian.ox.ac.uk/medicine","HCLCK":"http://www.bodleian.ox.ac.uk/medicine","HEBCD":"http://www.ochjs.ac.uk/mullerlibrary/","HFLCD":"http://www.bodleian.ox.ac.uk/history","OILCD":"http://www.bodleian.ox.ac.uk/oil","PTFCD":"http://www.bodleian.ox.ac.uk/ptfl","RSLCD":"http://www.bodleian.ox.ac.uk/science","TASCD":"http://www.bodleian.ox.ac.uk/taylor","TAYCD":"http://www.bodleian.ox.ac.uk/taylor","CAMCL":"http://www.campion.ox.ac.uk/","CARDL":"http://www.careers.ox.ac.uk/","CATCL":"https://www.stcatz.ox.ac.uk/Library","CCCCL":"http://www.ccc.ox.ac.uk/Library-and-Archives/","CHCCL":"http://www.chch.ox.ac.uk/","CMSNU":"http://www.ocms.ac.uk/content/index.php?q=ocms/facilities/library","COMDL":"http://intranet.cs.ox.ac.uk/library/","COSNU":"https://www.oxfordshire.gov.uk/cms/public-site/oxfordshire-history-centre","DOMDL":"http://www.materials.ox.ac.uk/local/library.html","EARDL":"http://www.bodleian.ox.ac.uk/subjects-and-libraries/libraries?id=51","ENGDL":"http://www.eng.ox.ac.uk/","EXECL":"http://www.exeter.ox.ac.uk/college/library","GRFNU":"http://www.bodleian.ox.ac.uk/subjects-and-libraries/libraries?id=122","GTCCL":"http://www.gtc.ox.ac.uk/college-life/library-and-information-services/library.html","HEBNU":"http://www.ochjs.ac.uk/mullerlibrary/","HERCL":"http://www.hertford.ox.ac.uk/my-hertford/library","HINNU":"http://www.ochs.org.uk/library","HMCCL":"http://www.hmc.ox.ac.uk/pages/default.asp?id=20","HSCDL":"http://www.bodleian.ox.ac.uk/subjects-and-libraries/subjects/human_sciences","HUGCL":"http://www.st-hughs.ox.ac.uk/life-here/library/","ICLNU":"http://www.clpic.ox.ac.uk/library/","IESNU":"http://www.oxfordenergy.org/about/library/","ISLNU":"http://www.oxcis.ac.uk/library.html","JESCL":"http://libguides.bodleian.ox.ac.uk/jesus","KEBCL":"http://www.keble.ox.ac.uk/about/library","KELCL":"http://www.kellogg.ox.ac.uk/study/kellogg-college-library/","LANDL":"http://www.lang.ox.ac.uk/library/index.html","LCRCL":"http://www.linacre.ox.ac.uk/facilities/library","LINCL":"http://www.lincoln.ox.ac.uk/Library","LMHCL":"http://www.lmh.ox.ac.uk/About-LMH/Library-%281%29.aspx","MAGCH":"http://www.magd.ox.ac.uk/libraries-and-archives/","MAGCL":"http://www.magd.ox.ac.uk/libraries-and-archives/","MAGCD":"http://www.magd.ox.ac.uk/libraries-and-archives/","MATDL":"https://www.maths.ox.ac.uk/members/library","MECDL":"http://www.sant.ox.ac.uk/research-centres/middle-east-centre/middle-east-centre-mec-library","MERCL":"http://www.merton.ox.ac.uk/library-and-archives","MFONU":"http://www.mfo.ac.uk/en/library","MHSDL":"http://www.mhs.ox.ac.uk/collections/library/","MNSCL":"http://www.mansfield.ox.ac.uk/prospective/library.html","NEWCL":"http://www.new.ox.ac.uk/library-and-archives","NUFCL":"http://www.nuffield.ox.ac.uk/Resources/Library/Pages/Library-home.aspx","OIIDL":"http://www.oii.ox.ac.uk/about/library/","OLIDL":"http://www.learning.ox.ac.uk/resources/library/","ORLCL":"http://www.oriel.ox.ac.uk/about/library","OUMDL":"http://www.oum.ox.ac.uk/collect/library.htm","OUSNU":"http://www.oxford-union.org/library","PEMCL":"http://www.pmb.ox.ac.uk/students/library-archives","PHLNU":"http://www.puseyhouse.org.uk/library.html","QUECL":"http://www.queens.ox.ac.uk/library/","REGCL":"http://www.rpc.ox.ac.uk/main-library/","RUSDL":"http://www.rsa.ox.ac.uk/about/library","SEHCL":"http://www.seh.ox.ac.uk/about-college/library","SOMCL":"http://www.some.ox.ac.uk/library-it/","SPCCL":"http://www.spc.ox.ac.uk/college-life/library","STADL":"http://www.stats.ox.ac.uk/about_us/library","STBCL":"http://www.st-benets.ox.ac.uk/library-regulations-borrowing","STJCL":"http://www.sjc.ox.ac.uk/385/Library-and-archives.html","STXCL":"http://www.stx.ox.ac.uk/about-st-cross/library","SYSSL":"http://www.bodleian.ox.ac.uk/bdlss","CEUHL":"http://www.ceu.ox.ac.uk/","OPTHL":"http://www.ndcn.ox.ac.uk/divisions/nlo","TRICL":"http://www.trinity.ox.ac.uk/library/","UNVCL":"http://www.univ.ox.ac.uk/content/libraries-and-archives","WADCL":"http://www.wadham.ox.ac.uk/about-wadham/library","WOLCL":"https://www.wolfson.ox.ac.uk/library","WORCL":"http://www.worc.ox.ac.uk/visiting/library"};
    return libWeb;
}