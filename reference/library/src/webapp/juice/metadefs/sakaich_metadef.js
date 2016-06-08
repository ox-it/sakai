function sakaich_metadef() {
    var isbns = new(Array);
    var primo_ids = new(Array);
    var aleph_ids = new(Array);
    var coins = new(Array);
    var aleph_id_re = /oxfaleph\d*/;
    var pref_libs = new(Array);
    var pref_lib_types = ["collegeLibrary", "deptLibrary", "userLibrary"];
    $jq('span.Z3988').each(function() {
        coin = $jq(this).attr('title');
        coins.push(coin);
        bib_item = z3988_parse(coin);
        isbns.push(unescape(bib_item["rft.isbn"]));
        bib_ids = bib_item["rft_ids"];
        // This assumes that Primo IDs contain a specific string
        // Not a brilliant way of doing this
        // We need to ensure one and only one Primo & Aleph id per item - this uses first one found of each
        if(bib_ids && bib_ids.length>0) {
            for(var i = 0; i < bib_ids.length; i++) {
                var numAleph_ids = 0;
                var numPrimo_ids = 0;
                if (bib_ids[i].search(/primo_library/) > 0) {
                    if(numPrimo_ids==0){
                        primo_ids.push(bib_ids[i]);
                        numPrimo_ids++;
                    }
                    if (bib_ids[i].search(/oxfaleph/) > 0) {
                        if(numAleph_ids==0){
                            aleph_ids.push(bib_ids[i].match(aleph_id_re)[0]);
                            numAleph_ids++;
                        }

                    }
                    break;
                }
            }
            if (numAleph_ids==0){
                aleph_ids.push("");
            }
            if (numPrimo_ids==0){
                primo_ids.push("");
            }
        } else {
            // Add an empty ID for those without a Primo ID
            aleph_ids.push("");
            primo_ids.push("");
        }


    })
    $jq('input[type="hidden"]').each(function() {
        if(pref_lib_types.indexOf($jq(this).attr('id')) > -1 ) {
            pref_libs.push($jq(this).attr('value'));
        }
    });
    juice.setMeta("image_isbns",isbns);
    juice.setMeta("primo_ids",primo_ids);
    juice.setMeta("coins",coins);
    juice.setMeta("aleph_ids",aleph_ids);
    juice.setMeta("pref_libs",pref_libs);
    juice.debugMeta();
}

function z3988_parse(coin) {
    var openurl_elements = new(Array);
    var key_value = new(Array);
    openurl_elements = coin.split('&');
    var bib_item = new Object;
    var rft_ids = new(Array);
    for(i=0;i<openurl_elements.length;i++) {
        key_value = openurl_elements[i].split('=');
        //handle possibility of repeated rft_id - put into an array here?
        if(key_value[0]=="rft_id") {
            rft_ids.push(key_value[1]);
        } else {
            bib_item[key_value[0]] = key_value[1];
        }
    }
    bib_item["rft_ids"] = rft_ids;
    return bib_item;
}

/**
 * Function : dump()
 * Arguments: The data - array,hash(associative array),object
 *    The level - OPTIONAL
 * Returns  : The textual representation of the array.
 * This function was inspired by the print_r function of PHP.
 * This will accept some data as the argument and return a
 * text that will be a more readable version of the
 * array/hash/object that is given.
 * Docs: http://www.openjs.com/scripts/others/dump_function_php_print_r.php
 */

// Included only for purposes of debugging

function dump(arr,level) {
    var dumped_text = "";
    if(!level) level = 0;

    //The padding given at the beginning of the line.
    var level_padding = "";
    for(var j=0;j<level+1;j++) level_padding += "    ";

    if(typeof(arr) == 'object') { //Array/Hashes/Objects
        for(var item in arr) {
            var value = arr[item];

            if(typeof(value) == 'object') { //If it is an array,
                dumped_text += level_padding + "'" + item + "' ...\n";
                dumped_text += dump(value,level+1);
            } else {
                dumped_text += level_padding + "'" + item + "' => \"" + value + "\"\n";
            }
        }
    } else { //Stings/Chars/Numbers etc.
        dumped_text = "===>"+arr+"<===("+typeof(arr)+")";
    }
    return dumped_text;
}