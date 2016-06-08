jQuery(document).ready(function () {
    // Assumes that the page calling this file is in a directory also containing the juice folder
    // Can be moved to whereever you want, just correct the paths
    juice.setDebug(false);
    juice.loadJs("/library/juice/metadefs/sakaich_metadef.js");
    juice.loadJs("/library/juice/extensions/extendedbyJuice.js");
    juice.loadJs("/library/juice/extensions/daiaAvailability.js");
    juice.loadJs("/library/juice/extensions/oxfalephAvailability.js");
    juice.loadJs("/library/juice/extensions/oxfelectronicAvailability.js");
    juice.loadCss("/library/juice/panels/juiceDefault.css");
    juice.onAllLoaded(runExtensions);
});

function runExtensions(){
    sakaich_metadef();
    if(juice.hasMeta()){
        if(juice.hasMeta("aleph_ids")){

            // ****************
            // Get Print Availability
            // ****************

            // Do this via new availability service...

            var availServer = "/library-availability/library";
            var availabilityDiv = '<div class="availability"></div>';
            var availabilityHeadDiv = 'div.availabilityHeader';
            var insert_avail = new JuiceInsert(availabilityDiv,availabilityHeadDiv,"replace");

            // call oxfalephAvailability
            /*
             * Constructor arguments:
             * arg: ju - instance of juice
             * arg: insert - JuiceInsert to use
             * arg: targetDiv - id of element to place image in
             * arg: availIDs - Juice Meta element containing array of IDs
             * arg: availServer - url of availability server
             * arg: numberOfLines - number of availability lines to display unhidden.
             */
            new oxfalephAvailability(juice,insert_avail,"availability","aleph_ids",availServer,"print","jsonp");
        }

        if(juice.hasMeta("eavail_ids")) {
            // ****************    
            // Get Electronic Availability
            // ****************
            var base_url = "http://oxfordsfx-direct.hosted.exlibrisgroup.com/oxford?";
            openurls = new(Array);
            var coins = juice.getMetaValues("eavail_ids");
            for (var i = 0; i < coins.length; i++){
                openurls.push(base_url + coins[i]);
            };
            juice.setMeta("openurls",openurls);

            var eavailServer = "/library-availability/eias"; // DAIA server for electronic availability
            var eavailabilityDiv = '<div class="e-avail"></div>';
            var insert_eavail = new JuiceInsert(eavailabilityDiv,"div.itemAction.links","prepend");
            new oxfelectronicAvailability(juice,insert_eavail,"e-avail","openurls",eavailServer);
        }

        // ****************
        // Put footer in
        // ****************

        doCreatedBy();


    }
}

function doCreatedBy(){
    new extendedbyJuice(juice);
} 