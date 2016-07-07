/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2016 Etudes, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
var readyToSaveEssay = true;

function saveEssayAnswer(delay, obj, sId, decodeId, propertyReference) {
	if (delay) {
		if (readyToSaveEssay) {
			readyToSaveEssay = false;
			
			// save
			submitEssayAnswer(obj, sId, decodeId, propertyReference)
			
			// wait
			var timeoutId;
			clearTimeout(timeoutId);
			timeoutId = setTimeout(function() {
		    	readyToSaveEssay = true;
		    }, 30000);
		}
	}
	else {
		readyToSaveEssay = true;
    	submitEssayAnswer(obj, sId, decodeId, propertyReference)
	}		
}

function submitEssayAnswer(obj, sId, decodeId, propertyReference) {
	
	var answerdata = CKEDITOR.instances[obj.id].getData();
	
	var propDecodeId = "prop_"+ decodeId;
	var changedDecodeId = "changed_"+ decodeId;
	var objId = obj.id;
	
	var reqData = {};
	reqData["destination_"] = "STAY_SAVE_AUTO";
	// jquery pass dynamic variable name
	reqData[decodeId] = obj.id;
	reqData[propDecodeId] = propertyReference;
	reqData[changedDecodeId] = "true";
	reqData[objId] = answerdata;
	
	var f = document.form0;
	
	var pURL = f.action;
	
	$.ajax({
        type: "POST",
        url: pURL,
        data: reqData,
        success: function(data, textStatus, jqXHR ) {
        },
		error: function(jqXHR, textStatus, errorThrown) {
			console.log('submitEssayAnswer:errorThrown:'+ errorThrown);            
		}
    });
}

function saveSingleAnswer(obj, sId, decodeId, propertyReference) {
	var propDecodeId = "prop_"+ decodeId;
	var changedDecodeId = "changed_"+ decodeId;
	var objId = obj.id;
	
	var reqData = {};
	reqData["destination_"] = "STAY_SAVE_AUTO";
	// jquery pass dynamic variable name
	reqData[decodeId] = obj.id;
	reqData[propDecodeId] = propertyReference;
	reqData[changedDecodeId] = "true";
	reqData[objId] = obj.value;
	
	var f = document.form0;
	
	var pURL = f.action;
	
	$.ajax({
        type: "POST",
        url: pURL,
        data: reqData,
        success: function(data, textStatus, jqXHR ) {
        },
		error: function(jqXHR, textStatus, errorThrown) {
			console.log('saveSingleAnswer:errorThrown:'+ errorThrown);            
		}
    });
}

function saveMultipleAnswers(obj, sId, decodeId, propertyReference) {
	var propDecodeId = "prop_"+ decodeId;
	var changedDecodeId = "changed_"+ decodeId;
	var objId = obj.id;
	var objName = obj.name;
	
	var reqData = {};
	reqData["destination_"] = "STAY_SAVE_AUTO";
	// jquery pass dynamic variable name
	reqData[decodeId] = objName;
	reqData[propDecodeId] = propertyReference;
	reqData[changedDecodeId] = "true";
	
	var f = document.form0;
	
	var choices = document.getElementsByName(obj.name);
	
	var choice = null;
	var choiceValues = [];
	var j = 0;
	
	for (i=0; i < choices.length; i++) {
		choice = choices[i];
		
		if (choice.checked) {			
			choiceValues[j] = choice.value;
			j++;
		}
	}
	
	if (choiceValues.length == 0) {
		choiceValues[0] = -1;		// as jquery is not sending empty array as param value
	}
	
	reqData[objName] = choiceValues;
	
	var pURL = f.action;
	
	$.ajax({
        type: "POST",
        url: pURL,
        data: reqData,
        traditional: true,	// to avoid [] in the param name
        success: function(data, textStatus, jqXHR ) {
        },
		error: function(jqXHR, textStatus, errorThrown) {
			console.log('saveSingleAnswer:errorThrown:'+ errorThrown);            
		}
    });
}

var readyToSaveFillIn = true;
function saveFillInAnswer(delay, obj, sId, decodeId, propertyReference) {
	
	if (delay) {
		if (readyToSaveEssay) {
			readyToSaveEssay = false;
			
			// save
			submitFillInAnswer(obj, sId, decodeId, propertyReference)
			
			// wait
			var timeoutId;
			clearTimeout(timeoutId);
			timeoutId = setTimeout(function() {
		    	readyToSaveEssay = true;
		    }, 5000);
		}
	}
	else {
		readyToSaveEssay = true;
		submitFillInAnswer(obj, sId, decodeId, propertyReference)
	}		
}

function submitFillInAnswer(obj, sId, decodeId, propertyReference) {
	
	var propDecodeId = "prop_"+ decodeId;
	var changedDecodeId = "changed_"+ decodeId;
	var objId = obj.id;
	var objName = obj.name;
	
	var fillIns = document.getElementsByName(obj.name);
	
	var reqData = {};
	
	if (fillIns.length > 1) {
		
		var fillIn = null;
		var fillInValues = [];
		var j = 0;
		
		for (i=0; i < fillIns.length; i++) {
			fillIn = fillIns[i];
			
			fillInValues[j] = fillIn.value;
			j++;
		}
		
		reqData[decodeId] = objName;
		reqData[objName] = fillInValues;		
	} else {
		
		var answerdata = obj.value;
		reqData[objId] = answerdata;
		reqData[decodeId] = obj.id;
	} 
	
	reqData["destination_"] = "STAY_SAVE_AUTO";
	// jquery pass dynamic variable name
	reqData[propDecodeId] = propertyReference;
	reqData[changedDecodeId] = "true";
	
	var f = document.form0;
	
	var pURL = f.action;
	
	$.ajax({
        type: "POST",
        url: pURL,
        data: reqData,
        traditional: true,	// to avoid [] in the param name
        success: function(data, textStatus, jqXHR ) {
        },
		error: function(jqXHR, textStatus, errorThrown) {
			console.log('submitFillInAnswer:errorThrown:'+ errorThrown);            
		}
    });
}

function saveFillInlineAnswer(objName, sId, decodeId, propertyReference) {
	var propDecodeId = "prop_"+ decodeId;
	var changedDecodeId = "changed_"+ decodeId;
	
	var fillInlineAnswersObj = document.getElementsByName(objName);
	
	var fillInlineAnswer = null
	var choiceValues = [];
	for (i=0; i < fillInlineAnswersObj.length; i++) {
		fillInlineAnswer = fillInlineAnswersObj[i];
		var selVal = fillInlineAnswer.options[fillInlineAnswer.selectedIndex].value;
		choiceValues[i] = selVal;
	}
	
	var reqData = {};
	reqData["destination_"] = "STAY_SAVE_AUTO";
	// jquery pass dynamic variable name
	reqData[decodeId] = objName;
	reqData[propDecodeId] = propertyReference;
	reqData[changedDecodeId] = "true";
	
	reqData[objName] = choiceValues;
	
	var f = document.form0;
	
	var pURL = f.action;
	
	$.ajax({
        type: "POST",
        url: pURL,
        data: reqData,
        traditional: true,	// to avoid [] in the param name
        success: function(data, textStatus, jqXHR ) {
        },
		error: function(jqXHR, textStatus, errorThrown) {
			console.log('saveFillInlineAnswer:errorThrown:'+ errorThrown);            
		}
    });
}

function saveMatchingAnswer(obj, sId, decodeId, propertyReference) {
	
	var propDecodeId = "prop_"+ decodeId;
	var changedDecodeId = "changed_"+ decodeId;
	var objId = obj.id;
	
	var reqData = {};
	reqData["destination_"] = "STAY_SAVE_AUTO";
	// jquery pass dynamic variable name
	reqData[decodeId] = obj.id;
	reqData[propDecodeId] = propertyReference;
	reqData[changedDecodeId] = "true";
	reqData[objId] = obj.value;
	
	var f = document.form0;
	
	var pURL = f.action;
	
	$.ajax({
        type: "POST",
        url: pURL,
        data: reqData,
        success: function(data, textStatus, jqXHR ) {
        },
		error: function(jqXHR, textStatus, errorThrown) {
			console.log('saveMatchingAnswer:errorThrown:'+ errorThrown);            
		}
    });
}