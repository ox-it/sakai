var getOxfordSkillCodes = function() {
  var skillCodes = [];
  var skillCodesUrl = 'https://data.ox.ac.uk/sparql/?query=SELECT+%3Fcode+%3Flabel+WHERE+{%0D%0A++%3Chttps%3A%2F%2Fdata.ox.ac.uk%2Fid%2Fox-rdf%2Fconcept-scheme%3E+skos%3AhasTopConcept%2Fskos%3Anarrower*+%3Fconcept+.%0D%0A++%3Fconcept+skos%3AprefLabel+%3Flabel+%3B%0D%0A++++skos%3Anotation+%3Fcode+.%0D%0A++FILTER+%28DATATYPE%28%3Fcode%29+%3D+%3Chttps%3A%2F%2Fdata.ox.ac.uk%2Fid%2Fox-rdf%2Fnotation%3E%29%0D%0A}+ORDER+BY+%3Flabel&format=srj&common_prefixes=on';

  $.ajax({
    dataType: 'json',
    url: skillCodesUrl,
    async: false,
    success: function(data) {
      // loop through, adding the correctly formatted array to skillCodes
      var skills = data.results.bindings;
      var skill = {};
      for (i = 0; i < skills.length; i++) {
        skill = skills[i];
        skillCodes.push([skill.label.value, skill.code.value]);
      }
    }
  });

  return skillCodes;
};
