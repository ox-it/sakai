# USAGE

Ensure that you have jQuery v1.11.1+ running on your pages.

## For the Editor
1. Copy `/twitter` to your CKEDITOR `/plugins` directory

2. When instantiating the editor with JavaScript, ensure that you enable
   `twitter` as an extra plugin (and if need be, load it externally)

        CKEDITOR.plugins.addExternal('twitter', 'path/to/twitter/'); // if you are loading it externally
        editor.config.extraPlugins += 'twitter';
        editor.config.allowedContent = true;                         // else the 'data=' attributes get stripped

## For your pages
The dependencies for displaying the timeline will be loaded into the editor once you click `OK` on the dialog.
If for some reason your dependencies get removed whilst editing inside of CKEditor,
simply edit one of your timelines and click `OK` on the dialog and the dependencies will be inserted again.
