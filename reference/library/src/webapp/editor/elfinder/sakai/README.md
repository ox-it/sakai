# elFinder Sakai Configuration
The files in this directory are Sakai-specific configurations for elFinder 2.1.

## Build vs Source
`elfinder.src.html` uses the elFinder 2.1 source code (in `../src/`)

`elfinder.build.html` uses a compressed/built version of elFinder 2.1 (in
`../build/`)

## Initialization
The client-side initialization is contained in the `js/init.js` file. It
parses the URL parameters provided to the elfinder html file that is loaded
and uses this to help initialize the file browser.

## Styling
`css/moono/` contains a Moono theme (to replicate CKEditor's Moono theme) that
is loaded after the default elFinder skin. `css/sakai` contains further changes
for the file browser that are Sakai-specific.

Furthermore, Javascript has been used to modify/move certain UI elements within
elFinder. These are also defined in the `init.js` file.
