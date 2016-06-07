// These functions look for metadata in the page and add it to the juice metadata.

function sakaich_metadef() {
	var isbns = new(Array);
	var primo_ids = new(Array);
	var coins = new(Array);
	$jq('span.Z3988').each(function() {
		var coin = $jq(this).attr('title');
		coins.push(coin);
		var bib_item = z3988_parse(coin);
		isbns.push(unescape(bib_item["rft.isbn"]));
		var bid_id = (bib_item["rft_id"] instanceof Array)?bib_item["rft_id"]:[bib_item["rft_id"]];
		// This assumes that Primo IDs contain a specific string
		// Not a brilliant way of doing this
		var primo_id = "";
		for(var i in bid_id) {
			// This only deals with one ID per item.
			if (bid_id[i] && bid_id[i].search(/primo_library/) > 0) {
				primo_id = bid_id[i];
				break;
			}
		}
		// Array must be the same size as number of items so push something.
		primo_ids.push(primo_id);
	})
	juice.setMeta("image_isbns",isbns);
	juice.setMeta("primo_ids",primo_ids);
	juice.setMeta("coins",coins);
	//juice.debugMeta();
}

function z3988_parse(coin) {
	var openurl_elements = coin.split('&');
	var bib_item = {};
	for(i=0;i<openurl_elements.length;i++) {
		var key_value = openurl_elements[i].split('=');
		var key = key_value[0];
		var value = key_value[1];
		if (bib_item[key]) {
			if (bib_item[key] instanceof Array) {
				bib_item[key].push(value);
			} else {
				bib_item[key] = [bib_item[key], value];
			}
		} else {
			bib_item[key] = value;
		}
	}
	
	return bib_item;

}