// methods for getting the user data
var getCurrentUserData = function() {
  var data = {};

  $.ajax({
    url: '/direct/user/current.json',
    dataType: 'json',
    async: false,
    success: function(json) {
      data = json;
    }
  });

  return data;
};

var getAvailableSites = function() {
  var sites = [['Select a site', '']];

  // my workspace
  if (currentUser.eid) {
    sites.push(['My Workspace', '/user/' + currentUser.id + '/']);
  }

  // all other sites
  $.ajax({
    url: '/direct/site.json',
    dataType: 'json',
    async: false,
    success: function(json) {
      var data = json['site_collection'];

      for (i in data) {
        var site = data[i];
        sites.push([site.title, '/group/' + site.entityId + '/']);
      }
    }
  });

  return sites;
};

var currentUser = getCurrentUserData();
