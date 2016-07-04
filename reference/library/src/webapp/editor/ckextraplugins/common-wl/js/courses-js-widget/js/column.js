if (!window.OxfordCoursesWidget) {
  OxfordCoursesWidget = {};
}

OxfordCoursesWidget.Column = function(name, text, classname) {
  this.name       = name
  this.classname  = classname
  this.text       = text

  this.toHtml = function() {
    return $('<th/>', {'text': text, 'class': classname});
  }
};
