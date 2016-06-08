# USAGE

## For the Editor
1. Copy `/jquery` to your CKEDITOR `/plugins` directory

2. When instantiating the editor with JavaScript, ensure that you enable
   `jquery` as an extra plugin (and if need be, load it externally)

        CKEDITOR.plugins.addExternal('jquery', 'path/to/jquery/'); // if you are loading it externally
        editor.config.extraPlugins += 'jquery';
        editor.config.allowedContent = true;

## In the Editor

Simply click the jQuery icon in the toolbar to embed an instance of jQuery at the top of the editor's content.
