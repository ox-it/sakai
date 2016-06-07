// methods for getting the current site data
var getAvailableSites = function() {
    var sites = [];
    var groupPath = '/group/';
    var forwardSlash = '/';
    var currentSiteId = sakai.editor.collectionId.replace(groupPath, '').replace(forwardSlash,'');

    // all other sites
  $.ajax({
    url: '/direct/site.json',
    dataType: 'json',
    async: false,
    success: function(json) {
      var data = json['site_collection'];

      for (i in data) {
        var site = data[i];
        if (site.id==currentSiteId){
            sites.push([site.title, groupPath + site.entityId + forwardSlash]);
        }
      }
    }
  });

  return sites;
};
