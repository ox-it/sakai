# USAGE

Ensure that you have jQuery v1.11.1+ running on your pages.

## For the Editor
1. Copy `/youtube` and `/common` to your CKEDITOR `/plugins` directory

2. Edit `/youtube/js/key.js`. Set the variable in that file to your valid
   YouTube Search API V3 Key.

        var googleApiKey = 'YourApiKey';

3. When instantiating the editor with JavaScript, ensure that you enable
   `youtube` as an extra plugin (and if need be, load it externally)

        CKEDITOR.plugins.addExternal('youtube', 'path/to/youtube/'); // if you are loading it externally
        editor.config.extraPlugins += 'youtube';
        editor.config.allowedContent = true;                         // else the 'data=' attributes get stripped

## For your pages

The dependencies for displaying the videos will be loaded into the editor once you click `OK` on the dialog.
If for some reason your dependencies get removed whilst editing inside of CKEditor,
simply edit one of your videos and click `OK` on the dialog and the dependencies will be inserted again.
