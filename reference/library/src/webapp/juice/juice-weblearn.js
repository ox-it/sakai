jQuery(document).ready(function () {
	// Assumes that the page calling this file is in a directory also containing the juice folder
	// Can be moved to whereever you want, just correct the paths
	juice.setDebug(false);
	juice.loadJs("/library/juice/metadefs/sakaich_metadef.js");
	juice.loadJs("/library/juice/extensions/extendedbyJuice.js");	
	juice.loadJs("/library/juice/extensions/daiaAvailability.js");
	juice.loadCss("/library/juice/panels/juiceDefault.css");	
	juice.onAllLoaded(runExtensions);
});

function runExtensions(){
	var expandIcon = "/library/image/sakai/expand.gif?panel=Main";
	var collapseIcon = "/library/image/sakai/collapse.gif?panel=Main";
	sakaich_metadef();
	if(juice.hasMeta()){
		if(juice.hasMeta("primo_ids")){
			
			// ****************	
			// Get Print Availability
			// ****************
			
			var availServer = "/library-availability/library"; // DAIA server for print availability
			var availabilityDiv = '<div id="availability"></div>';
			var insert_avail = new JuiceInsert(availabilityDiv,"span.Z3988","after");
			
			// call daiaAvailability
			/*
			 * Constructor arguments:
			 * arg: ju - instance of juice
			 * arg: insert - JuiceInsert to use
			 * arg: targetDiv - id of element to place image in
			 * arg: availIDs - Juice Meta element containing array of IDs for DAIA requests
			 * arg: availServer - url of availability server
			 * arg: availType - set to 'online' to treat all availability as online, otherwise will treat DAIA response generically
			 * arg: format - format to return DAIA results [json only format currently supported]
			 * arg: noLines - number of availability lines to display unhidden. 
			 *                Remaining lines will be hidden and 'show' button added.
			 *                Any 'open access' availability will be shown whatever this value
			 *                Ignored when availType == 'online'
			 * arg: toggleExpand - URL of image to be used for the toggleAvailability 'expand' function where some results are hidden. Not used when availType == 'online'
			 * arg: toggleCollapse - URL of image to be used for the toggleAvailability 'collapse' function where some results are hidden. Not used when availType == 'online'
			 */
			
			new daiaAvailability(juice,insert_avail,"availability","primo_ids",availServer,"print","jsonp",1,expandIcon,collapseIcon);
		}
		if(juice.hasMeta("coins")) {
			// ****************	
			// Get Electronic Availability
			// ****************

			// Create new Juice Meta that contains OpenURLs rather than just COINS 
			// This is so we can use a proper http URI for DAIA request

			var base_url = "http://oxfordsfx-direct.hosted.exlibrisgroup.com/oxford?";
			openurls = new(Array);
			var coins = juice.getMetaValues("coins");
			for (var i = 0; i < coins.length; i++){
				openurls.push(base_url + coins[i]);
			};
			juice.setMeta("openurls",openurls);

			var eavailServer = "/library-availability/eias"; // DAIA server for electronic availability
			var eavailabilityDiv = '<div id="e-availability"></div>';
			var insert_eavail = new JuiceInsert(eavailabilityDiv,"span.Z3988","after");
			
			// call daiaAvailability
			/*
			 * Constructor arguments:
			 * arg: ju - instance of juice
			 * arg: insert - JuiceInsert to use
			 * arg: targetDiv - id of element to place image in
			 * arg: availIDs - Juice Meta element containing array of IDs for DAIA requests
			 * arg: availServer - url of availability server
			 * arg: availType - set to 'online' to treat all availability as online, otherwise will treat DAIA response generically
			 * arg: format - format to return DAIA results [json only format currently supported]
			 * arg: noLines - number of availability lines to display unhidden. 
			 *                Remaining lines will be hidden and 'show' button added.
			 *                Any 'open access' availability will be shown whatever this value
			 *                Ignored when availType == 'online'
			 * arg: toggleExpand - URL of image to be used for the toggleAvailability 'expand' function where some results are hidden. Not used when availType == 'online'
			 * arg: toggleCollapse - URL of image to be used for the toggleAvailability 'collapse' function where some results are hidden. Not used when availType == 'online'
			 */
			
			new daiaAvailability(juice,insert_eavail,"online-availability","openurls",eavailServer,"online","jsonp",0,expandIcon,collapseIcon);
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