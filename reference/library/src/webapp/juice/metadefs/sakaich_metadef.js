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
        bib_id = bib_item["rft_id"];
        // This assumes that Primo IDs contain a specific string
        // Not a brilliant way of doing this
        // We need to ensure one and only one id per item (think about an array of rft_id, filter for primo_library, pick first entry if multiple match)
        for (var j=0; j<bib_id.length; j++) {
            if (bib_id[j] && bib_id[j].search(/primo_library/) > 0) {
                primo_ids.push(bib_id[j]);
                if (bib_id[j].search(/oxfaleph/) > 0) {
                    aleph_ids.push(bib_id[j].match(aleph_id_re)[0]);
                }
            } else {
                // Add an empty ID for those without a Primo ID
                primo_ids.push("");
            }
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