// generic browser functions

var isIE = function() {  //  http://stackoverflow.com/questions/19999388/jquery-check-if-user-is-using-ie
    var msie = window.navigator.userAgent.indexOf("MSIE ");
    return msie > 0 || !!navigator.userAgent.match(/Trident.*rv\:11\./);
};