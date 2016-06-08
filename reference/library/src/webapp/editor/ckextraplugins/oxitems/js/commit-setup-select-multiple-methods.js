// for setup method on select-multiple fields with jQuery plugin 'Chosen' bound to them
var setupSelectMultipleChosen = function(element, classname) {
  // pull values as a string
  var values = element.getAttribute('data-' + classname) || [];

  // if we have a string, split it into an array
  if (typeof values == 'string')
    values = values.split(',');

  // set the default values of the dropdown and trigger chosen to update
  // the dropdown list accordingly
  $('.' + classname + '.to_chosen select').val(values).trigger('chosen:updated');
};

// for commit method on select-multiple fields with jQuery plugin 'Chosen' bound to them
var commitSelectMultipleChosen = function(element, classname) {
  // pull values from the select field
  var values = $('.' + classname + '.to_chosen select').val() || '';

  // if we have an array, build a string from it
  if (values.length)
    values = values.join(',');

  element.setAttribute('data-' + classname, values);
};
