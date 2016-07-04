# USAGE

Ensure that you have jQuery v1.11.1+ running on your pages.

## For the Editor
1. Copy `/oxam` to your CKEDITOR `/plugins` directory

2. When instantiating the editor with JavaScript, ensure that you enable
   `oxam` as an extra plugin (and if need be, load it externally)

        CKEDITOR.plugins.addExternal('oxam', 'path/to/oxam/'); // if you are loading it externally
        editor.config.extraPlugins += 'oxam';
        editor.config.allowedContent = true;                   // else the 'data-' attributes get stripped

## For your pages
The dependencies for displaying the results will be loaded into the editor once you click `OK` on the dialog.
If for some reason your dependencies get removed whilst editing inside of CKEditor,
simply edit one of your embeddings and click `OK` on the dialog and the dependencies will be inserted again.
