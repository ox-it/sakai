var bindFolderListingToPreview = function($preview, dialog, fullPath, openToFolder, path) {
  $preview.html($(getPluginDialogHtml(path, 'preview.html')).html());
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
