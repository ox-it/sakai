var relativePath;
$(function() {
	var editFolderPath = $( "input[name='edit-folder-path']" ).val();
	relativePath = $('#folder-path').val();
	$('.listing').attr('data-directory', relativePath).folderListing({
		onFolderEvent: function(folderCollectionId) {
			var folder = folderCollectionId.attr('rel');
			var directory = getDirectoryFromPath(folder);
			updatePathInput(directory);
		},
		onFileEvent: function(file) {
			window.open(file);
		},
		displayRootDirectory: true,
	});
	if( editFolderPath !== undefined){
		updatePathInput(editFolderPath);
	}	
});
function updatePathInput(data){
	$('#active-folder').val(data);
	$('#folder-path').val(data);
	
}