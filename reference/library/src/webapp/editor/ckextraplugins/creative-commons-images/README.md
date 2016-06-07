# USAGE

Ensure that you have jQuery v1.11.1+ running on your pages.

## For the Editor
1. Copy `/creative-commons-images` and `/common` to your CKEDITOR `/plugins` directory

2. Edit `/creative-commons-images/js/key.js`. Set the variable in that file to your valid
   Flickr API Key.

        var FlickrApiKey = 'YourApiKey';

3. When instantiating the editor with JavaScript, ensure that you enable
   `creative-commons-images` as an extra plugin (and if need be, load it externally)

        CKEDITOR.plugins.addExternal('creative-commons-images', 'path/to/creative-commons-images/'); // if you are loading it externally
        editor.config.extraPlugins += 'creative-commons-images';

## Known bugs
1. When editing an image link, there is a clash between this plugin and the images plugin,
   so the old url will always be stored instead. If you want to change the image after
   embedding it, delete the image in the editor first and then embed the desired one.
