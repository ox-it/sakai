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
		for(var i in bid_id) {
			if (bid_id[i] && bid_id[i].search(/primo_library/) > 0) {
				primo_ids.push(bid_id[i]);
			}
		}
		if (primo_ids.length == 0) {
			// Add an empty ID for those without a Primo ID
			primo_ids.push("");
		}
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