var concat = require('concat');

concat([
  'js/sakai.js',
  'js/query.js',
  'js/tools.js',
  'js/ui.js',
  'js/resizer.js',
  'js/options.js',
  'js/editors.js',
  'js/confirm.js',
  'js/init.js'
], 'js/build.js', function (error) {
  if (!error) {
    console.log("Build successful");
  } else {
    console.log("Build unsuccessful!", error);
  }
});
