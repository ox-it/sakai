// methods relating to getting/manipulating site/path information
var removeEmptyElements = function(array) {
  return array.filter(function(e) {
                    return e;
                  });
};

var getSiteFromRelativePath = function(path) {
  var fullpath = path.split('/');
      fullpath = removeEmptyElements(fullpath);
  var site = fullpath[0] || null;
      site += ('/' + fullpath[1]) || null;

  return '/' + site + '/';
};

var getDirectoryFromPath = function(path) {
  var fullpath = path.split('/');
      fullpath = removeEmptyElements(fullpath);

  return '/' + fullpath.slice(2).join('/') + '/';
};

var bindFolderListingToPreview = function($preview, dialog, fullPath, openToFolder, path) {
  $preview.html($(getFolderListingDialogHtml(path, 'preview.html')).html());
  $preview.find('[data-folder-listing]')
    .attr('data-directory', getSiteFromRelativePath(fullPath))
    .folderListing({
      openToFolder: openToFolder,
      onFileEvent: function(file) {
        window.open(file);
      },
      onFolderEvent: function(folder) {
        var directory = getDirectoryFromPath(folder);
        dialog.setValueOf('settings', 'directory', directory);
      },
    });
};
