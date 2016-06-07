st = getTime();
data = getSortedData("title", true);
for (i = 0; i < data.length; i++) {
    data[i].fullRead();
}

function filter_fn(data) {
    return (!Search_Re || (data.title.match(Search_Re) || data.description.match(Search_Re))) &&
            (!Type_Filter || (data.type == Type_Filter)) &&
            (!Div_Filter || (data.division == Div_Filter));
}

console.log("Took: ", getTime() - st, "ms");
st = getTime();
Search_Re = RegExp("Shake");
Type_Filter = 'audio';
Div_Filter = '';

data.filter(filter_fn);
console.log(data.length);
console.log("Took: ", getTime() - st, "ms");