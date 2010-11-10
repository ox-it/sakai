/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009, 2010 Etudes, Inc.
 * 
 * Portions completed before September 1, 2008
 * Copyright (c) 2007, 2008 The Regents of the University of Michigan & Foothill College, ETUDES Project
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

// functions for Ambrosia applications (1.1)

// note: make sure this loads AFTER the sakai headscripts
// take over the setMainFrameHeightNow function
window.sakaiSetMainFrameHeightNow = window.setMainFrameHeightNow;
window.setMainFrameHeightNow = ambrosiaSetMainFrameHeightNow;
var ambrosiaSetMainFrameHeightNowId = null;
function ambrosiaSetMainFrameHeightNow(id)
{
	if (ambrosiaSetMainFrameHeightNowId == null)
	{
		ambrosiaSetMainFrameHeightNowId = id;
	}
	var scroll = null;
	if (id == null)
	{
		id = ambrosiaSetMainFrameHeightNowId;
		scroll = findParentScroll();
	}

	var frame = parent.document.getElementById(id);
	if (frame != null)
	{
		// only if we are really large
		var scrollingDiv = document.getElementById("ambrosiaInterfaceScroll");
		if (scrollingDiv != null)
		{
			var height = document.body.offsetHeight;
			if (height > 32000)
			{
				// this makes the scrolling div holding the interface have a scroll bar that fits on the screen
				var newHeight = parent.innerHeight - frame.offsetTop;
				scrollingDiv.style.height = newHeight + "px";
				scrollingDiv.style.overflow="auto";
			}
		}

		// scroll both window and parent to 0 to assure we are at the top	
		window.scrollTo(0,0);
		parent.window.scrollTo(0,0);
	
		sakaiSetMainFrameHeightNow(id);
		
		if (scroll != null)
		{
			parent.window.scrollTo(scroll[0], scroll[1]);
		}
		
		// anchor
		else if (ambrosiaAnchorId != null)
		{
			var anchor = document.getElementById(ambrosiaAnchorId);
			if (anchor != null)
			{
				var posInParent = findPosition(frame);
				var anchorPos = findPosition(anchor);
				parent.window.scrollTo(posInParent[0]+anchorPos[0], posInParent[1]+anchorPos[1]-12);
			}
		}
	}

	// not in a frame
	else
	{
		// anchor
		if (ambrosiaAnchorId != null)
		{
			var anchor = document.getElementById(ambrosiaAnchorId);
			if (anchor != null)
			{
				var anchorPos = findPosition(anchor);
				window.scrollTo(anchorPos[0], anchorPos[1]-12);
			}
		}
	}
}

function ambrosiaAlterMainFrameHeight(delta)
{
	var frame = parent.document.getElementById(ambrosiaSetMainFrameHeightNowId);
	if (frame)
	{
		var objToResize = (frame.style) ? frame.style : frame;
		objToResize.height = parseInt(objToResize.height) + delta + "px";
	}
}


//find parent's scroll
function findParentScroll()
{
	var x = 0;
	var y = 0;
	if (parent.pageYOffset)
	{
		x = parent.pageXOffset;
		y = parent.pageYOffset;
	}
	else if (parent.document.documentElement && parent.document.documentElement.scrollTop)
	{
		x = parent.document.documentElement.scrollLeft;
		y = parent.document.documentElement.scrollTop;
	}
	else if (parent.document.body)
	{
		x = parent.document.body.scrollLeft;
		y = parent.document.body.scrollTop;
	}
	
	return [x,y];
}

function trim(s)
{
	return s.replace(/^\s+/g, "").replace(/\s+$/g, "");
}

function showConfirm(name)
{
	var el = document.getElementById(name);
	if (el.style.display == "none")
	{
		el.style.left = ((document.body.scrollWidth / 2) - (parseInt(el.style.width) / 2)) + "px";
		el.style.top = (-1 * (parseInt(el.style.height) + 10)) + "px";
		if (parent)
		{
			confirmX = (parent.window.pageXOffset) ? parent.window.pageXOffset : (parent.document.documentElement.scrollLeft + parent.document.body.scrollLeft);
			confirmY = (parent.window.pageYOffset) ? parent.window.pageYOffset : (parent.document.documentElement.scrollTop + parent.document.body.scrollTop);
			parent.window.scrollTo(0,0);
		}
		else
		{
			confirmX = (window.pageXOffset) ? window.pageXOffset : (document.documentElement.scrollLeft + document.body.scrollLeft);
			confirmY = (window.pageYOffset) ? window.pageYOffset : (document.documentElement.scrollTop + document.body.scrollTop);
			window.scrollTo(0,0);
		}
	}
	el.style.display = "";

	if (parseInt(el.style.top) < -10)
	{
		el.style.top = (parseInt(el.style.top) + 10) + "px";
		setTimeout("showConfirm('" + name + "')",10);
	}
	else
	{
		el.style.top = "0px";
	}
}

var confirmedAction="";
var confirmX=0;
var confirmY=0;

function hideConfirm(name, action)
{
	if (action != "") confirmedAction = action;
	var el = document.getElementById(name);

	if (parseInt(el.style.top) > (-1 * (parseInt(el.style.height))))
	{
		el.style.top = (parseInt(el.style.top) - 10) + "px";
		setTimeout("hideConfirm('" + name + "','')",10);
	}
	else
	{
		el.style.top = (-1 * (parseInt(el.style.height) + 10)) + "px"
		el.style.display = "none";
		if (parent)
		{
			parent.window.scrollTo(confirmX,confirmY);
		}
		else
		{
			window.scrollTo(confirmX,confirmY);
		}
		if (confirmedAction != "") eval(confirmedAction);
		confirmedAction="";
	}
}

function getInt(str)
{
	// assume radix 10
	var rv = parseInt(str, 10);
	
	// return 0 if its not a parsable int
	if (isNaN(rv)) rv = 0;
	return rv;
}

function getFloat(str)
{
	var rv = parseFloat(str);
	
	// return 0 if its not a parsable int
	if (isNaN(rv)) rv = 0.0;
	return rv;
}

function ambrosiaDurationChange(source, validateId)
{
	ambrosiaValidateDuration(source, validateId);
}

function ambrosiaValidateDuration(source, validateId)
{
	if (source == null) return true;
	var reg = new RegExp("^[0-9]+:[0-9]{2}$", "i");

	var str = trim(source.value);
	if (str != "")
	{
		if (reg.exec(str) == null)
		{
			ambrosiaShowInline(validateId);
			return false;
		}
	}

	ambrosiaHideInline(validateId);
	return true;
}

function ambrosiaDateChange(source, validateId, submit)
{
	ambrosiaValidateDate(source, validateId, submit);
}

function ambrosiaValidateDate(source, validateId, submit)
{
	if (source == null) return true;
	// Dec 1, 2007 12:00 AM
	var reg = new RegExp("^(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec) [0-3]?[0-9]{1}, [0-9]{4} (0|00|1|01|2|02|3|03|4|04|5|05|6|06|7|07|8|08|9|09|10|11|12|13|14|15|16|17|18|19|20|21|22|23):[0-5]{1}[0-9]{1} (am|pm){1}$", "i");

	var str = trim(source.value);
	if (str != "")
	{
		if (reg.exec(str) == null)
		{
			ambrosiaShowInline(validateId);
			return false;
		}
	}

	ambrosiaHideInline(validateId);
	if (submit != null) return ambrosiaSubmit(submit);
	return true;
}

function ambrosiaCountChange(source, shadowId, summaryId, min, max, validateId)
{
	// validate
	if (ambrosiaValidateInt(source, min, max, validateId))
	{
		// then summary
		if (summaryId != null) ambrosiaCountSummaryInt(source, shadowId, summaryId)
	}
}

function ambrosiaValidateInt(source, min, max, validateId)
{
	if (source == null) return true;
	var str = trim(source.value);
	if (str != "")
	{
		var value = parseInt(str, 10);
		if (isNaN(value))
		{
			ambrosiaShowInline(validateId);
			return false;
		}
		if (value != str)
		{
			ambrosiaShowInline(validateId);
			return false;
		}

		if ((min != null) && (value < parseInt(min, 10)))
		{
			ambrosiaShowInline(validateId);
			return false;
		}

		if ((max != null) && (value > parseInt(max, 10)))
		{
			ambrosiaShowInline(validateId);
			return false;
		}
	}

	ambrosiaHideInline(validateId);
	return true;
}

function ambrosiaCountSummaryInt(source, shadowId, summaryId)
{
	// get the objects
	var summary = document.getElementById(summaryId);
	var shadow = document.getElementById(shadowId);
	
	var oldValue = 0;
	var newValue = source.value;

	// read the old value and store the new value if we have a shadow
	if (shadow != null)
	{
		oldValue = shadow.value;
		shadow.value = newValue;
	}
	
	// update the summary
	if (summary != null)
	{
		// (for a field) summary.value = getInt(summary.value) - getInt(oldValue) + getInt(newValue);
		summary.innerHTML = getInt(summary.innerHTML) - getInt(oldValue) + getInt(newValue);
	}
}

function ambrosiaFloatChange(source, shadowId, summaryId, defaultValue, min, max, validateId)
{
	// validate
	if (ambrosiaValidateFloat(source, min, max, validateId))
	{
		// then summary
		if (summaryId != null) ambrosiaCountSummaryFloat(source, shadowId, summaryId, defaultValue)
	}
}

function ambrosiaSelectChange(source, idsArray, selectAllId)
{
	var selectAllEl = document.getElementById(selectAllId);
	if (selectAllEl == null) return;

	// check the group to see if all are checked
	var allChecked = true;
	for (var i in idsArray)
	{
		var el = document.getElementById(idsArray[i]);
		if (el != null)
		{
			if (!el.checked)
			{
				allChecked = false;
			}
		}
	}

	selectAllEl.checked = allChecked;
}

function ambrosiaSelectGroup(source, idsArray)
{
	var setting = source.checked;
	for (var i in idsArray)
	{
		var el = document.getElementById(idsArray[i]);
		if (el != null)
		{
			el.checked = setting;
		}
	}
}	

function ambrosiaValidateFloat(source, min, max, validateId)
{
	if (source == null) return true;
	var str = trim(source.value);
	if (str != "")
	{
		var value = parseFloat(str, 10);
		if (isNaN(value))
		{
			ambrosiaShowInline(validateId);
			return false;
		}
		if (value != str)
		{
			ambrosiaShowInline(validateId);
			return false;
		}

		if ((min != null) && (value < parseFloat(min, 10)))
		{
			ambrosiaShowInline(validateId);
			return false;
		}

		if ((max != null) && (value > parseFloat(max, 10)))
		{
			ambrosiaShowInline(validateId);
			return false;
		}
	}

	ambrosiaHideInline(validateId);
	return true;
}

function ambrosiaCountSummaryFloat(source, shadowId, summaryId, defaultValue)
{
	// get the objects
	var summary = document.getElementById(summaryId);
	var shadow = document.getElementById(shadowId);
	
	var oldValue = 0;
	var newValue = source.value;

	// apply the default if the newValue is blank
	if (newValue == "")
	{
		newValue = defaultValue;
		source.value = defaultValue;
	}

	// read the old value and store the new value if we have a shadow
	if (shadow != null)
	{
		oldValue = shadow.value;
		shadow.value = newValue;
	}
	
	// update the summary
	if (summary != null)
	{
		// for a text field
		if (summary.value)
		{
			summary.value = getFloat(summary.value) - getFloat(oldValue) + getFloat(newValue);
		}
		// for some html container like span
		else
		{
			var summaryValue = getFloat(summary.innerHTML);
			summary.innerHTML = summaryValue - getFloat(oldValue) + getFloat(newValue);
		}
	}
}

function ambrosiaSubmit(destination)
{
	document.form0.destination_.value=destination;
	document.form0.submit();
}

function ambrosiaNavigate(enabled, enableFunction, confirm, confirmDivId, validateFlag, submit, destination, root, requirementsFunction, requirementsDivId, portal)
{
	if (requirementsFunction != null)
	{
		if (!eval(requirementsFunction))
		{
			showConfirm(requirementsDivId);
			return;
		}
	}
	if (!enabled)
	{
		if (confirm)
		{
			eval(enableFunction);
			showConfirm(confirmDivId);
 			return;
 		}
 		else
 		{
 			return;
 		}
	}
	if (submitted)
	{
		return;
	}
	if ((!validateFlag) || validate())
	{
		submitted=true;
		if (submit)
		{
			document.form0.destination_.value=destination;
			document.form0.submit();
		}
		else
		{
			if (portal)
			{
				parent.location=root + destination;
			}
			else
			{
				document.location=root + destination;
			}
		}
	}
}

// dependencies is an array of arrays, the inner array[0] is the selection value, a reversed flag, and the rest are field ids
function ambrosiaSelectDependencies(selected, dependencies)
{
	for (var d=0; d < dependencies.length; d++)
	{
		var list = dependencies[d];
		var value = list[0];
		var reversed = list[1];
		var doIt = (selected == value);
		if (reversed) doIt = !doIt;
		if (doIt)
		{
			for (var i=2; i < list.length; i++)
			{
				var target = document.getElementById(list[i]);
				if (target == null) continue;
				if (target.disabled == true)
				{
					target.disabled = false;
					if (target.type == "radio")
					{
						target.checked = true;
					}
				}
				if (target.tagName == "IMG")
				{
					if (target.style.display == "none")
						target.style.display = "inline";
				}
			}
		}
		
		else
		{
			for (var i=2; i < list.length; i++)
			{
				var target = document.getElementById(list[i]);
				if (target == null) continue;
				target.disabled = true;
				if (target.type == "text")
				{
					target.value = "";
				}
				else if (target.type == "radio")
				{
					target.checked = false;
				}
				else if (target.type == "checkbox")
				{
					target.checked = false;
				}
				else if (target.tagName == "IMG")
				{
					if (target.style.display == "inline")
						target.style.display = "none";
				}
				if (target.onchange) target.onchange();
			}
		}
	}
}

function ambrosiaTextOptions(obj, textId)
{
	if (obj == null) return;
	var txt = document.getElementById(textId);
	if (txt == null) return;

	if (obj.value != "")
	{
		txt.value = obj.value;
	}
	
	obj.value = "";
}

function ambrosiaNextSibling(obj, tag)
{
	var next = obj.nextSibling;
	while (next && next.nodeName != tag)
	{
		next = next.nextSibling;
	}
	return next;
}

function ambrosiaPrevSibling(obj, tag)
{
	var prev = obj.previousSibling;
	while (prev && prev.nodeName != tag)
	{
		prev = prev.previousSibling;
	}
	return prev;
}

function ambrosiaFirstSibling(obj, tag)
{
	var first = obj;
	if (first != null)
	{
		var tmp = first;
		while (tmp != null)
		{
			tmp = ambrosiaPrevSibling(tmp, tag);
			if (tmp != null) first = tmp;
		}
	}
	return first;
}

function ambrosiaNthSibling(obj, tag, n)
{
	// n is 1 based
	var count = 1;	
	var candidate = ambrosiaFirstSibling(obj, "TR");
	while ((candidate != null) && (count < n))
	{
		count++;
		candidate = ambrosiaNextSibling(candidate, "TR");
	}
	
	return candidate;
}

function ambrosiaWhichSibling(obj, tag)
{
	var count = 1;	
	var candidate = ambrosiaFirstSibling(obj, "TR");
	while ((candidate != null) && (candidate != obj))
	{
		count++;
		candidate = ambrosiaNextSibling(candidate, "TR");
	}
	if (candidate == null) return 0;
	return count;
}

function ambrosiaParent(obj, tag)
{
	if (obj == null) return null;

	var up = obj.parentNode;
	if (up == null) return null;
	if (up.nodeName == tag)
	{
		return up;
	}
	return ambrosiaParent(up, tag);
}

function ambrosiaFindChild(obj, tag, idRoot)
{
	if (obj == null) return null;
	if ((obj.nodeName == tag) && (obj.id != null) && (obj.id.substring(0, idRoot.length) == idRoot)) return obj;
	if (obj.childNodes == null) return null;
	for (var i = 0; i < obj.childNodes.length; i++)
	{
		var candidate = ambrosiaFindChild(obj.childNodes[i], tag, idRoot);
		if (candidate != null) return candidate;
	}
}

function ambrosiaFirstChild(obj, tag)
{
	if (obj == null) return null;
	if (obj.nodeName == tag) return obj;
	if (obj.childNodes == null) return null;
	for (var i = 0; i < obj.childNodes.length; i++)
	{
		var candidate = ambrosiaFirstChild(obj.childNodes[i], tag);
		if (candidate != null) return candidate;
	}
}

function ambrosiaTableReorderRowPosition(innerObj, position)
{
	var toPos = parseInt(position);
	if (isNaN(toPos)) return true;

	var obj = innerObj;
	if (obj.nodeName != "TR")
	{
		obj = ambrosiaParent(obj, "TR");
	}
	if (obj == null) return true;
	
	var objPos = ambrosiaWhichSibling(obj, "TR");

	if (toPos < objPos)
	{
		var target = ambrosiaNthSibling(obj, "TR", toPos);
		obj.parentNode.insertBefore(obj, target);
		return false;
	}
	else
	{
		var target = ambrosiaNthSibling(obj, "TR", toPos);
		if (target) target = ambrosiaNextSibling(target, "TR");
		if (target)
		{
			obj.parentNode.insertBefore(obj, target);
		}
		else
		{
			obj.parentNode.appendChild(obj);
		}
	}
}

function ambrosiaRenumberSelect(selectIdRoot, innerObj)
{
	var obj = innerObj;
	if (obj.nodeName != "TR")
	{
		obj = ambrosiaParent(obj, "TR");
	}
	if (obj == null) return true;

	var target = ambrosiaFirstSibling(obj, "TR");
	var index = 0;
	while (target != null)
	{
		// find a select in target that has a name matching selectIdRoot
		var select = ambrosiaFindChild(target, "SELECT", selectIdRoot);
		if (select != null)
		{
			// change the options to have the nth selected
			ambrosiaSetNthSelected(select, index);
		}
		target = ambrosiaNextSibling(target, "TR");
		index++;
	}
}

function ambrosiaSetNthSelected(obj, index)
{
	// index is 0 based
	var option = ambrosiaFirstChild(obj, "OPTION");
	var i = 0;
	while (option != null)
	{
		if (i == index)
		{
			option.selected = true;
		}
		else
		{
			option.selected = false;
		}
		option = ambrosiaNextSibling(option, "OPTION");
		i++;
	}
}

function ambrosiaTableReorderPosition(innerObj, position, selectIdRoot)
{
	ambrosiaTableReorderRowPosition(innerObj, position);
	ambrosiaRenumberSelect(selectIdRoot, innerObj);
}

function ambrosiaTableReorder(event, innerObj)
{
// window.event || event for ie?
	if ((event == null) || (innerObj == null)) return true;
	var code = event.keyCode;

	if ((code != 38) && (code != 40)) return true;

	var obj = innerObj;
	if (obj.nodeName != "TR")
	{
		obj = ambrosiaParent(obj, "TR");
	}
	if (obj == null) return true;

	if (code == 38)
	{
		var prev = ambrosiaPrevSibling(obj, "TR");
		if (prev)
		{
			obj.parentNode.insertBefore(obj, prev);
			innerObj.focus();
		}
		return false;
	}

	if (code == 40)
	{
		var next = ambrosiaNextSibling(obj, "TR");
		if (next) next = ambrosiaNextSibling(next, "TR");
		if (next)
		{
			obj.parentNode.insertBefore(obj, next);
			innerObj.focus();
		}
		else
		{
			obj.parentNode.appendChild(obj);
			innerObj.focus();
		}
		return false;
	}
	
	return true;
}

function ambrosiaCountChecked(name)
{
	var objs = document.getElementsByName(name);
	var count = 0;
	for (var i=0; i < objs.length; i++)
	{
		if (objs[i].name == name)
		{
			if (objs[i].checked) count++;
		}
	}
	return count;
}

function ambrosiaToggleSection(name, title1, title2, maxHeight, minHeight)
{
	var el = document.getElementById(name);
	if (el == null) return;
	var titleEl1 = document.getElementById(title1);
	var titleEl2 = document.getElementById(title2);
	if (parseInt(el.style.height) == minHeight)
	{
		if (titleEl1 != null) titleEl1.style.display = "none";
		if (titleEl2 != null) titleEl2.style.display = "block";
		expandSection(name, maxHeight);
	}
	else
	{
		if (titleEl2 != null) titleEl2.style.display = "none";
		if (titleEl1 != null) titleEl1.style.display = "block";
		contractSection(name, minHeight);
	}
}

function expandSection(name, maxHeight)
{
	var el = document.getElementById(name);
	if (el == null) return;

	// record the full height
	el.ambrosiaFullHeight = el.scrollHeight;

	el.ambrosiaMaxHeight = maxHeight;
	if (el.ambrosiaMaxHeight == 0)
	{
		el.ambrosiaMaxHeight = el.ambrosiaFullHeight;
	}

	if (el.ambrosiaMaxHeight > el.ambrosiaFullHeight)
	{
		el.ambrosiaFinalHeight = el.ambrosiaFullHeight;
	}
	else
	{
		el.ambrosiaFinalHeight = el.ambrosiaMaxHeight;
	}

	expandToFullHeight(name)
}

function expandToFullHeight(name)
{
	var el = document.getElementById(name);
	if (el == null) return;
	if (parseInt(el.style.height) + 60 < el.ambrosiaFinalHeight)
	{
		ambrosiaAlterMainFrameHeight(60);
		el.style.height = (parseInt(el.style.height) + 60) + "px";
		setTimeout("expandToFullHeight('" + name + "')",5);
	}
	else
	{
		var delta = el.ambrosiaFinalHeight - parseInt(el.style.height);
		ambrosiaAlterMainFrameHeight(delta);
		el.style.height = el.ambrosiaFinalHeight + "px";
		if (el.ambrosiaFinalHeight != el.ambrosiaFullHeight)
		{
			el.style.overflow = "auto";
		}
	}
}

function expandToFullHeightNow(name)
{
	var el = document.getElementById(name);
	if (el == null) return;

	el.style.height = el.ambrosiaFinalHeight + "px";
	if (el.ambrosiaFinalHeight != el.ambrosiaFullHeight)
	{
		el.style.overflow = "auto";
	}
	setMainFrameHeightNow(null);
}

function ambrosiaExpandSectionNow(name, maxHeight)
{
	var el = document.getElementById(name);
	if (el == null) return;

	if (el.scrollHeight > maxHeight)
	{
		el.style.height = maxHeight + "px";
		el.style.overflow = "auto";
	}
	else
	{
		el.style.height = el.scrollHeight + "px";
	}
}

function contractSection(name, minHeight)
{
	contractFromFullHeight(name, minHeight);
}

function contractFromFullHeight(name, minHeight)
{
	var el = document.getElementById(name);
	if (el == null) return;
	if (parseInt(el.style.height) - 60 > minHeight)
	{
		el.style.overflow = "hidden";
		el.style.height = (parseInt(el.style.height) - 60) + "px";
		ambrosiaAlterMainFrameHeight(-60);
		setTimeout("contractFromFullHeight('" + name + "'," + minHeight + ")", 5);
	}
	else
	{
		var delta = (parseInt(el.style.height) * -1) + minHeight;
		el.style.height = minHeight + "px";
		if (el.scrollTop) el.scrollTop = 0;
		ambrosiaAlterMainFrameHeight(delta);
	}
}

function contractFromFullHeightNow(name, minHeight)
{
	var el = document.getElementById(name);
	if (el == null) return;

	el.style.height = minHeight + "px";
	if (el.scrollTop) el.scrollTop = 0;
	el.style.overflow = "hidden";
	setMainFrameHeightNow(null);
}

function ambrosiaToggleVisibility(name)
{
	var el = document.getElementById(name);
	if (el == null) return;
	if (el.style.visibility == "hidden")
	{
		el.style.visibility = "visible";
		el.style.overflow = "auto";
	}
	else
	{
		if (el.scrollTop) el.scrollTop = 0;
		el.style.overflow = "hidden";
		el.style.visibility = "hidden";
	}
}

function ambrosiaShowInline(name)
{
	var el = document.getElementById(name);
	if (el == null) return;
	if (el.style.display == "none")
		el.style.display = "inline";
}

function ambrosiaHideInline(name)
{
	var el = document.getElementById(name);
	if (el == null) return;
	if (el.style.display == "inline")
		el.style.display = "none";
}

function ambrosiaSetupHtmlEdit(name, docsArea)
{
	ambrosiaSetupHtmlEditTiny(name, docsArea);
	//ambrosiaSetupHtmlEditFck(name, docsArea);
}

var ambrosiaFileBrowserDestination = null;

function ambrosiaFileBrowser(field_name, url, type, win)
{
	tinyMCE.activeEditor.windowManager.open(
	{
		file : ambrosiaFileBrowserDestination + "/" + type,
		width : 700,
		height : 500,
		resizable : "yes",
		close_previous : "no",
		scrollbars : "yes"
	},
	{
		window : win,
		input : field_name
	});
	return false;
}

function ambrosiaChooseAttachment(url)
{
	var win = tinyMCEPopup.getWindowArg("window");
	win.document.getElementById(tinyMCEPopup.getWindowArg("input")).value = url;
	if (win.getImageData) win.getImageData();
	tinyMCEPopup.close();
}

function ambrosiaDoneAttachments()
{
	tinyMCEPopup.close();
}

var ambrosiaTinyCss = null;

function ambrosiaTinyInit(picker, mode)
{
	// mode: all, full, small, tall
	var tinyMode = "none";
	if (mode == "all") tinyMode = "textareas";

	if (picker == null)
	{
		// full and tall will be 7 pixels taller than height
		if ((mode == "full") || (mode == "all"))
		{
			tinyMCE.init(
			{
				mode: tinyMode,
				editor_selector: "ambrosiaHtmlEdit_full",
				convert_urls: false,
				plugins: "safari,fullscreen,emotions,paste",
				theme: "advanced",
				theme_advanced_buttons1: "bold,italic,underline,strikethrough,sub,sup,separator,outdent,indent,justifyleft,justifycenter,justifyright,justifyfull,separator,bullist,numlist,fontselect,fontsizeselect,forecolor,backcolor",
				theme_advanced_buttons2: "fullscreen,separator,undo,redo,separator,link,unlink,separator,pastetext,pasteword,separator,image,emotions,charmap,separator,code",
				theme_advanced_buttons3: "",
				extended_valid_elements: "+a[id|style|rel|rev|charset|hreflang|dir|lang|tabindex|accesskey|type|name|href|target:_blank|title|class|onfocus|onblur|onclick|" + 
										 "ondblclick|onmousedown|onmouseup|onmouseover|onmousemove|onmouseout|onkeypress|onkeydown|onkeyup]",
				fullscreen_new_window : true,
				fullscreen_settings :
					{
						theme_advanced_toolbar_location : "top",
						theme_advanced_buttons1: "fullscreen,separator,undo,redo,separator,bold,italic,underline,strikethrough,sub,sup,separator,outdent,indent,justifyleft,justifycenter,justifyright,justifyfull,separator,bullist,numlist,separator,link,unlink,separator,fontselect,fontsizeselect,forecolorpicker,backcolorpicker,separator,pastetext,pasteword,separator,image,emotions,charmap,separator,code",
						theme_advanced_buttons2: "",
						theme_advanced_buttons3: ""
					},
				tab_focus: ":prev,:next",
				content_css: (ambrosiaTinyCss == null) ? "" : ambrosiaTinyCss,
				width: 600,
				height: 137
			});
		}
		
		if ((mode == "tall") || (mode == "all"))
		{
			tinyMCE.init(
			{
				mode: tinyMode,
				editor_selector: "ambrosiaHtmlEdit_tall",
				convert_urls: false,
				plugins: "safari,fullscreen,emotions,paste",
				theme: "advanced",
				theme_advanced_buttons1: "bold,italic,underline,strikethrough,sub,sup,separator,outdent,indent,justifyleft,justifycenter,justifyright,justifyfull,separator,bullist,numlist,fontselect,fontsizeselect,forecolor,backcolor",
				theme_advanced_buttons2: "fullscreen,separator,undo,redo,separator,link,unlink,separator,pastetext,pasteword,separator,image,emotions,charmap,separator,code",
				theme_advanced_buttons3: "",
				extended_valid_elements: "+a[id|style|rel|rev|charset|hreflang|dir|lang|tabindex|accesskey|type|name|href|target:_blank|title|class|onfocus|onblur|onclick|" + 
										 "ondblclick|onmousedown|onmouseup|onmouseover|onmousemove|onmouseout|onkeypress|onkeydown|onkeyup]",
				fullscreen_new_window : true,
				fullscreen_settings :
					{
						theme_advanced_toolbar_location : "top",
						theme_advanced_buttons1: "fullscreen,separator,undo,redo,separator,bold,italic,underline,strikethrough,sub,sup,separator,outdent,indent,justifyleft,justifycenter,justifyright,justifyfull,separator,bullist,numlist,separator,link,unlink,separator,fontselect,fontsizeselect,forecolorpicker,backcolorpicker,separator,pastetext,pasteword,separator,image,emotions,charmap,separator,code",
						theme_advanced_buttons2: "",
						theme_advanced_buttons3: ""
					},
				tab_focus: ":prev,:next",
				content_css: (ambrosiaTinyCss == null) ? "" : ambrosiaTinyCss,
				width: 600,
				height: 226
			});
		}

		// small will be 10 pixels taller than height, min of 156px
		if ((mode == "small") || (mode == "all"))
		{
			tinyMCE.init(
			{
				mode: tinyMode,
				editor_selector: "ambrosiaHtmlEdit_small",
				convert_urls: false,
				plugins: "safari,fullscreen,emotions,paste",
				theme: "advanced",
				theme_advanced_buttons1: "bold,italic,underline,strikethrough,sub,sup,separator,outdent,indent,justifyleft,justifycenter,justifyright,justifyfull",
				theme_advanced_buttons2: "bullist,numlist,fontselect,fontsizeselect,forecolor,backcolor",
				theme_advanced_buttons3: "fullscreen,separator,undo,redo,separator,link,unlink,separator,pastetext,pasteword,separator,image,emotions,charmap,separator,code",
				extended_valid_elements: "+a[id|style|rel|rev|charset|hreflang|dir|lang|tabindex|accesskey|type|name|href|target:_blank|title|class|onfocus|onblur|onclick|" + 
										 "ondblclick|onmousedown|onmouseup|onmouseover|onmousemove|onmouseout|onkeypress|onkeydown|onkeyup]",
				fullscreen_new_window : true,
				fullscreen_settings :
					{
						theme_advanced_toolbar_location : "top",
						theme_advanced_buttons1: "fullscreen,separator,undo,redo,separator,bold,italic,underline,strikethrough,sub,sup,separator,outdent,indent,justifyleft,justifycenter,justifyright,justifyfull,separator,bullist,numlist,separator,link,unlink,separator,fontselect,fontsizeselect,forecolorpicker,backcolorpicker,separator,pastetext,pasteword,separator,image,emotions,charmap,separator,code",
						theme_advanced_buttons2: "",
						theme_advanced_buttons3: ""
					},
				tab_focus: ":prev,:next",
				content_css: (ambrosiaTinyCss == null) ? "" : ambrosiaTinyCss,
				width: 300,
				height: 174
			});
		}
	}
	else
	{
		ambrosiaFileBrowserDestination = picker;

		if ((mode == "full") || (mode == "all"))
		{
			tinyMCE.init(
			{
				mode: tinyMode,
				editor_selector: "ambrosiaHtmlEdit_full",
				convert_urls: false,
				plugins: "safari,fullscreen,emotions,paste",
				theme: "advanced",
				theme_advanced_buttons1: "bold,italic,underline,strikethrough,sub,sup,separator,outdent,indent,justifyleft,justifycenter,justifyright,justifyfull,separator,bullist,numlist,fontselect,fontsizeselect,forecolor,backcolor",
				theme_advanced_buttons2: "fullscreen,separator,undo,redo,separator,link,unlink,separator,pastetext,pasteword,separator,image,emotions,charmap,separator,code",
				theme_advanced_buttons3: "",
				file_browser_callback: "ambrosiaFileBrowser",
				extended_valid_elements: "+a[id|style|rel|rev|charset|hreflang|dir|lang|tabindex|accesskey|type|name|href|target:_blank|title|class|onfocus|onblur|onclick|" + 
										 "ondblclick|onmousedown|onmouseup|onmouseover|onmousemove|onmouseout|onkeypress|onkeydown|onkeyup]",
				fullscreen_new_window : true,
				fullscreen_settings :
					{
						theme_advanced_toolbar_location : "top",
						theme_advanced_buttons1: "fullscreen,separator,undo,redo,separator,bold,italic,underline,strikethrough,sub,sup,separator,outdent,indent,justifyleft,justifycenter,justifyright,justifyfull,separator,bullist,numlist,separator,link,unlink,separator,fontselect,fontsizeselect,forecolorpicker,backcolorpicker,separator,pastetext,pasteword,separator,image,emotions,charmap,separator,code",
						theme_advanced_buttons2: "",
						theme_advanced_buttons3: ""
					},
				tab_focus: ":prev,:next",
				content_css: (ambrosiaTinyCss == null) ? "" : ambrosiaTinyCss,
				width: 600,
				height: 137
			});
		}

		if ((mode == "tall") || (mode == "all"))
		{
			tinyMCE.init(
			{
				mode: tinyMode,
				editor_selector: "ambrosiaHtmlEdit_tall",
				convert_urls: false,
				plugins: "safari,fullscreen,emotions,paste",
				theme: "advanced",
				theme_advanced_buttons1: "bold,italic,underline,strikethrough,sub,sup,separator,outdent,indent,justifyleft,justifycenter,justifyright,justifyfull,separator,bullist,numlist,fontselect,fontsizeselect,forecolor,backcolor",
				theme_advanced_buttons2: "fullscreen,separator,undo,redo,separator,link,unlink,separator,pastetext,pasteword,separator,image,emotions,charmap,separator,code",
				theme_advanced_buttons3: "",
				file_browser_callback: "ambrosiaFileBrowser",
				extended_valid_elements: "+a[id|style|rel|rev|charset|hreflang|dir|lang|tabindex|accesskey|type|name|href|target:_blank|title|class|onfocus|onblur|onclick|" + 
										 "ondblclick|onmousedown|onmouseup|onmouseover|onmousemove|onmouseout|onkeypress|onkeydown|onkeyup]",
				fullscreen_new_window : true,
				fullscreen_settings :
					{
						theme_advanced_toolbar_location : "top",
						theme_advanced_buttons1: "fullscreen,separator,undo,redo,separator,bold,italic,underline,strikethrough,sub,sup,separator,outdent,indent,justifyleft,justifycenter,justifyright,justifyfull,separator,bullist,numlist,separator,link,unlink,separator,fontselect,fontsizeselect,forecolorpicker,backcolorpicker,separator,pastetext,pasteword,separator,image,emotions,charmap,separator,code",
						theme_advanced_buttons2: "",
						theme_advanced_buttons3: ""
					},
				tab_focus: ":prev,:next",
				content_css: (ambrosiaTinyCss == null) ? "" : ambrosiaTinyCss,
				width: 600,
				height: 226
			});
		}

		if ((mode == "small") || (mode == "all"))
		{
			tinyMCE.init(
			{
				mode: tinyMode,
				editor_selector: "ambrosiaHtmlEdit_small",
				convert_urls: false,
				plugins: "safari,fullscreen,emotions,paste",
				theme: "advanced",
				theme_advanced_buttons1: "bold,italic,underline,strikethrough,sub,sup,separator,outdent,indent,justifyleft,justifycenter,justifyright,justifyfull",
				theme_advanced_buttons2: "bullist,numlist,fontselect,fontsizeselect,forecolor,backcolor",
				theme_advanced_buttons3: "fullscreen,separator,undo,redo,separator,link,unlink,separator,pastetext,pasteword,separator,image,emotions,charmap,separator,code",
				file_browser_callback: "ambrosiaFileBrowser",
				extended_valid_elements: "+a[id|style|rel|rev|charset|hreflang|dir|lang|tabindex|accesskey|type|name|href|target:_blank|title|class|onfocus|onblur|onclick|" + 
										 "ondblclick|onmousedown|onmouseup|onmouseover|onmousemove|onmouseout|onkeypress|onkeydown|onkeyup]",
				fullscreen_new_window : true,
				fullscreen_settings :
					{
						theme_advanced_toolbar_location : "top",
						theme_advanced_buttons1: "fullscreen,separator,undo,redo,separator,bold,italic,underline,strikethrough,sub,sup,separator,outdent,indent,justifyleft,justifycenter,justifyright,justifyfull,separator,bullist,numlist,separator,link,unlink,separator,fontselect,fontsizeselect,forecolorpicker,backcolorpicker,separator,pastetext,pasteword,separator,image,emotions,charmap,separator,code",
						theme_advanced_buttons2: "",
						theme_advanced_buttons3: ""
					},
				tab_focus: ":prev,:next",
				content_css: (ambrosiaTinyCss == null) ? "" : ambrosiaTinyCss,
				width: 300,
				height: 174
			});
		}
	}
}

function ambrosiaEnableHtmlEdit(htmlComponent)
{
	if (htmlComponent == null) return;
	if (htmlComponent.renderedId == null) return;
	if (htmlComponent.textAreaId == null) return;
	if (htmlComponent.toggleId == null) return;
	if (htmlComponent.mode == null) return;
	var renderedEl = document.getElementById(htmlComponent.renderedId);
	if (renderedEl == null) return;
	var textAreaEl = document.getElementById(htmlComponent.textAreaId);
	if (textAreaEl == null) return;
	var toggelEl = document.getElementById(htmlComponent.toggleId);
	if (toggelEl == null) return;

	if (!htmlComponent.enabled)
	{
		htmlComponent.enabled = true;
		// toggleEl.style.display = "none";
		ambrosiaTinyInit(ambrosiaTinyPicker, htmlComponent.mode);
		tinyMCE.execCommand("mceAddControl", false, htmlComponent.renderedId);

		var editor = tinyMCE.get(htmlComponent.renderedId);
		editor.onChange.add(function(ed){textAreaEl.value = ed.getContent();});
	}
	else
	{
		var editor = tinyMCE.get(htmlComponent.renderedId);
		if (editor == null) return;

		htmlComponent.enabled = false;
		var newContent = editor.getContent();
		textAreaEl.value = newContent;

		tinyMCE.execCommand('mceRemoveControl', false, htmlComponent.renderedId);
		// toggleEl.style.display = "none";
	}
}

function ambrosiaPopupDate(id)
{
	// get the current date value
	var el = document.getElementById(id);
	if (el == null) return;
	
	var timeStamp = new Date(el.value);
	if (isNaN(timeStamp)) timeStamp = new Date();

	var popup = new calendar2(el);
	if (popup == null) return;
	popup.popup();
	//el.select();
}

window.sakaiSetFocus = window.setFocus;
window.setFocus = ambrosiaSetFocus;
function ambrosiaSetFocus(elements)
{
	if ((elements != null) && (elements.length == 1))
	{
		var focus = document.getElementById(elements[0]);
		if ((focus != null) && ("none" == focus.style.display))
		{
			var editor = tinyMCE.get(focus.id);
			if (editor != null)
			{
				editor.focus(false);
				return;
			}
		}
	}
	sakaiSetFocus(elements);
}
