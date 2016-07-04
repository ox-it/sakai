# USAGE

Ensure that you have jQuery v1.11.1+ running on your pages.

## For the Editor
1. Copy `/solo-citation` and `/common-wl` to your CKEDITOR `/plugins` directory

2. When instantiating the editor with JavaScript, ensure that you enable
   `solo-citation` as an extra plugin (and if need be, load it externally)

        CKEDITOR.plugins.addExternal('solo-citation', 'path/to/solo-citation/'); // if you are loading it externally
        editor.config.extraPlugins += 'solo-citation';
        editor.config.allowedContent = true;                                     // else the 'data=' attributes get stripped

## In the Editor
1. If you wish to push all of the citations into one block at a certain point
   in the page, create a `div` with the id `citationsContainer` and follow
   instruction 3 on `For your pages`.

## For your pages
The dependencies for displaying the citation will be loaded into the editor once you click `OK` on the dialog.
If for some reason your dependencies get removed whilst editing inside of CKEditor,
simply edit one of your citations and click `OK` on the dialog and the dependencies will be inserted again.
