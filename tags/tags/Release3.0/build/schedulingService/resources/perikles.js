var sessionHandle = "sessionHandle";
var count = 0;
var msgAjaxError = "msgAjaxError";
var msgCancelWithoutSave = "msgCancelWithoutSave";
var msgCompleteForce = "msgCompleteForce";

// Cachen der Dropdown-Inhalte
var ajaxCache = {};

function addCloneAfterOriginal(original) {
	count++;
	var clone = original.cloneNode(true);
	replaceId(clone, "#", "#" + count);
	original.parentNode.insertBefore(clone, original.nextSibling);
}

function addCloneBeforeInsert(original, id, insert) {
	count++;
	var clone = original.cloneNode(true);
	if (count%2==1) {
		clone.style.backgroundColor = "#FFEEBB";
	}
	replaceId(clone, clone.id, id + count);
	insert.parentNode.insertBefore(clone, insert);
}

/**
 * replace string from 'orig' to '_' of id of field and his childs with 'repl',
 * e.g. replaceId(clone, "#", "#" + 2):
	 	 Activity_Induction_Reservation_#1_Resource --> Activity_Induction_Reservation_#2_Resource
 * e.g. replaceId(clone, "ReservationTemplate", "Activity_Induction_Reservation_#" + 2):
	   ReservationTemplate_Resource --> Activity_Induction_Reservation_#2_Resource
 */
function replaceId(field, orig, repl) {
	if (field.id) {
		field.id = newName(field.id, orig, repl);
		/*if (field.name && field.type!="radio") {
			field.name = field.id;
		}*/
		if (field.name) {
			field.name = newName(field.name, orig, repl);
		}
		
		// clean input fields
		/*if (field.type=="text" || field.type=="password" || field.type=="checkbox"
			|| field.type=="radio" || field.type=="file") {
			field.value = "";
		}*/
	}
	
	var childs = field.childNodes;
	for (var i=0;i<childs.length;i++) {
		replaceId(childs[i], orig, repl);
	}
}

function newName(oldname, orig, repl) {
	var firstIdx = oldname.indexOf(orig);
	var first = oldname.substring(0, firstIdx);
	var last = oldname.substring(firstIdx);
	var lastIdx = last.indexOf("_");
	if (lastIdx>0) {
		last = last.substring(lastIdx);
	} else {
		last = "";
	}
	return first + repl + last;		
}

/**
 * removes field and its error and warning field
 */
function removeFieldAndItsError(field) {
	var fieldId = field.id;
	field.parentNode.removeChild(field);

	var errorField = document.getElementById(fieldId + "_error");
	if (errorField != null) {
		errorField.parentNode.removeChild(errorField);
	}
	var warningField = document.getElementById(fieldId + "_warning");
	if (warningField != null) {
		warningField.parentNode.removeChild(warningField);
	}
}

/**
 * remove all errors and warnings from field
 */
function removeErrorWarning(field) {
	if (field!=null) {
		field.title = "";
		field.className = field.className.replace(/ errorInputTD/g,"");
		field.className = field.className.replace(/ errorServerTD/g,"");
		field.className = field.className.replace(/ warningInputTD/g,"");
	}	
}

/**
 * remove server errors and warnings from field
 */
function removeServerError(field) {
	if (field!=null) {
		//alert(field.name+": className=" + field.className + ", title=" + field.title);
		var regExpr = new RegExp(msgAjaxError,"gi");
		var idx = field.title.search(regExpr);
		//alert("search("+regExpr+")=" + idx + ", search(/Serverfehler/)=" + field.title.search(/Serverfehler/));
		if (idx>-1) {
			field.title = field.title.substring(0, idx);
			field.title = $.trim(field.title);
		}
		field.className = field.className.replace(/ errorServerTD/g,"");
		//alert("set field.className=" + field.className + ", field.title=" + field.title);
	}
}

function getObjectsFromServer(objectName, prevFieldValue, elementThis) {
	var useMock = false, url, data, req;
	
	//alert("objectName=" + objectName + ", prevFieldValue=" + prevFieldValue);
	if (useMock) {
		url = "ResourceServiceJSP.jsp",
		data = "objectName=" + objectName;
		data += "&prevFieldValue=" + prevFieldValue;
		req = data;
	} else {
		url = "ResourceGatewayProxyJSP.jsp";
		data = "action=";
		if (objectName=="Id" && prevFieldValue=="human") {
			data += "getParticipantIdentifiers";
		} else if (objectName=="Id" && prevFieldValue=="non-human") {
			data += "getNonHumanResourceIdentifiers";
		} else if (objectName=="Id") {
			return null;
		} else if (objectName=="Role") {
			data += "getRoleIdentifiers";
		} else if (objectName=="Capability") {
			data += "getCapabilityIdentifiers";
		} else if (objectName=="Category") {
			data += "getNonHumanCategories";
		} else if (objectName=="SubCategory") {
			/*if (prevFieldValue=="ITS-Kapazität") {
			  	alert("objectName=" + objectName + ", prevFieldValue=" + prevFieldValue);
				//prevFieldValue = "ITS-Kapazit&auml;t";
			}*/
			data += "getNonHumanSubCategories&id="+prevFieldValue;
		} else {
			if (elementThis!=null && elementThis.className.search(/errorServerTD/)==-1) {
				elementThis.title += " " + msgAjaxError+": unknown objectName '"+objectName+"'";
				elementThis.className += " errorServerTD";
				elementThis.title = $.trim(elementThis.title);
				//alert("set elementThis.title=" + elementThis.title);
			}
			return null;
		}
		req = data + "&format=JSON&sessionHandle="+sessionHandle;
	}
	
	var ret = ajaxCache[data];	
	//alert("ret=" + ret);
	if (ret==null) {
		//alert("hole vom Server: " + data);
		var ok = true;
		ret= $.ajax(
		{
			type: "POST",
			url: url,
			data: req,
			dataType: "jsonp",
//			scriptCharset: "ISO-8859-1",
			async: false, //TODO@tbe: asynchron möglich?
//			beforeSend: function(XMLHttpRequest){
//				XMLHttpRequest.withCredentials = "true";
//			},
			success: function(data, textStatus, XMLHttpRequest){
				//alert("send successful=" + data + ", textStatus=" + textStatus);
			},
			error: function(XMLHttpRequest, textStatus, errorThrown){
				//alert(msgAjaxError+"="+errorThrown+", elementThis="+(elementThis==null?null:elementThis.name));
				//alert("XMLHttpRequest="+XMLHttpRequest);
				ok = false;
				if (elementThis!=null && elementThis.className.search(/errorServerTD/)==-1) {
					elementThis.title += " " + msgAjaxError+": "+url+"?"+req+" ==> "+errorThrown;
					elementThis.className += " errorServerTD";
					elementThis.title = $.trim(elementThis.title);
					//alert("set elementThis.title=" + elementThis.title);
				}
			}
		}).responseText;
		//alert("ok=" + ok + ", ret=" + ret);
		if (ok) {
			ajaxCache[data] = ret;
		} else {
			ret = null;
		}
	}
		
	// remove server error if server or cache returns correct value 
	if (ret!=null) {
		removeServerError(elementThis);
	}

	return ret;
}

function getDropDownBox(objectName, prevFieldValue, selected, elementThis) {
	var myJson = getObjectsFromServer(objectName, prevFieldValue, elementThis);
	//alert("-----------------------myJson:\n" + myJson);
	var dropdownbox = document.createElement("select");
	var optionDefault = document.createElement("option");
	optionDefault.setAttribute("value", "");
	optionDefault.setAttribute("selected", "selected");
	var textDefault = document.createTextNode("---");
	optionDefault.appendChild(textDefault);	
	dropdownbox.appendChild(optionDefault);

	if (myJson != null/* && myJson.indexOf("<", 0)<0*/) {
		var myJsonObj = jsonParse(myJson);
		for (var k in myJsonObj) {
			//alert(k + '=' + myJsonObj[k]);		
			var option = document.createElement("option");
			option.value = k;
			if (selected == k) {
				//alert("------erkenne option:\n" + k);
				option.setAttribute("selected", "selected");
				optionDefault.removeAttribute("selected");
			}
			var text = document.createTextNode(myJsonObj[k]);
			option.appendChild(text);	
			dropdownbox.appendChild(option);
		}
	}
	
	//alert("-----------------------innerHTML:\n" + dropdownbox.innerHTML);
	return dropdownbox;
}

function writeDropDownBox(objectName, prevFieldValue, selected, thisId) {
	//alert("objectName: " + objectName + ", prevFieldValue: " + prevFieldValue + ", selected: " + selected + ", thisId: " + thisId);
	var elementThis = document.getElementById(thisId);
	var dropDownBox = getDropDownBox(objectName, prevFieldValue, selected, elementThis);
	//alert("writeDropDownBox: dropDownBox.innerHTML: " + dropDownBox.innerHTML);
	/*if (objectName=="Id") {
		alert(objectName+"."+prevFieldValue+".dropDownBox.length=" + dropDownBox.options.length);
	}*/
	document.write(dropDownBox.innerHTML);
}

function actualizeDropDownBox(thisObjectName, otherObjectName, thisId) {
	var elementThis = document.getElementById(thisId);
	var value;
	if (elementThis.options) {
		value = elementThis.options[elementThis.selectedIndex].value;
	} else {
		value = elementThis.value;
	}
	//alert("this: " + thisId + "=" + value);
	
	//alert("thisObjectName: " + thisObjectName + ", otherObjectName: " + otherObjectName);
	var otherId = newName(thisId, thisObjectName, otherObjectName);
	var elementOther = document.getElementById(otherId);
	//alert("otherId: " + otherId + "=" + elementOther + ", html: " + (elementOther==null ? "" : elementOther.innerHTML));
	if (elementOther != null) {
		var dropDownBox = getDropDownBox(otherObjectName, value, '', elementOther);
		//alert("elementOther.length: " + elementOther.options.length + ", " + otherObjectName+"."+value+".dropDownBox.length=" + dropDownBox.options.length);
		
		/*for(i=0; i<elementOther.options.length; i++) {
			elementOther.options[i] = null;
		}

		for (i=0; i<dropDownBox.options.length; i++) {
			var opt = new Option(dropDownBox.options[i].text, dropDownBox.options[i].value, false, dropDownBox.options[i].selected);
			elementOther.options[i] = opt;
		}*/
		
		elementOther.innerHTML = dropDownBox.innerHTML;
	}
}

/**
 * 
 * @param newValueFieldKey : id of element which time value will be set by this function
 * @param oldValueFieldKey : id of element which time value was edit in custom form
 * @param durationFieldKey : id of 'Duration' element
 * @param factor : add/substract duration to/from oldValue?
 * @param swapIsPossible
 * @param calFormat
 */
function addMinutes2DateField(newValueFieldKey, oldValueFieldKey, durationFieldKey, factor, swapIsPossible, calFormat) {
	var newValueField = document.getElementById(newValueFieldKey);
	var oldValueField = document.getElementById(oldValueFieldKey);
	var durationField = document.getElementById(durationFieldKey);
	var minutes = durationField.value;
	
	// errors, warnings aller Felder entfernen
	removeErrorWarning(newValueField);
	removeErrorWarning(oldValueField);
	removeErrorWarning(durationField);
	
	// bei ungültiger duration entweder duration aus from-to berechnen oder abbrechen,
	// sonst hängt sich der Kalender auf
	//alert("minutes: "+minutes+", isNaN(minutes): "+isNaN(minutes)+", minutes=='': "+(minutes==""));
	if ((minutes=="") || isNaN(minutes)) {
		if (oldValueField.value && newValueField.value) {
			//alert("berechne duration...");
			var oldDate = getDateFromString(oldValueField.value);
			var newDate = getDateFromString(newValueField.value);
			minutes = (newDate.getTime() - oldDate.getTime()) /1000/60*factor;
			durationField.value = minutes;
		}
		return;
	}
	
	minutes = minutes*factor;
	//alert("minutes=" + minutes/1 + ", old=" + oldValueField.value + ", new=" + newValueField.value);
	
	if (!oldValueField.value) {
		if (swapIsPossible && newValueField.value) {
			var tmpValueField = oldValueField;
			oldValueField = newValueField;
			newValueField = tmpValueField;
			minutes = 0-minutes;
		} else {
			return;
		}
	}
		
	//dd.mm.yyyy hh:ii
	var oldValue = oldValueField.value;
	//alert("oldValue=" + oldValue);
	var time = getDateFromString(oldValue);
	//alert("oldtime=" + time);
	time = new Date(time.getTime() + minutes*60*1000);
	//alert("newtime=" + time);
	
	var timeStr = calFormat;
	var day = time.getDate();
	var mon = time.getMonth()+1;
	var yea = time.getFullYear();
	var hou = time.getHours();
	var min = time.getMinutes();
	//alert("newtimeStr=" + day + "." + mon + "." + yea + " " + hou + ":" + min);
	
	timeStr = timeStr.replace('dd', day<10 ? "0"+day : day);
	timeStr = timeStr.replace('mm', mon<10 ? "0"+mon : mon);
	timeStr = timeStr.replace('yyyy', yea);
	timeStr = timeStr.replace('hh', hou<10 ? "0"+hou : hou);
	timeStr = timeStr.replace('ii', min<10 ? "0"+min : min);
	
	newValueField.value = timeStr;	
}

function setRescheduling(reschedulingKey) {
	// save rescheduling informations
	document.getElementById("reschedulingKey").value=reschedulingKey;	
}

function submitRescheduling(reschedulingKey) {
	// save rescheduling informations and submit form
	setRescheduling(reschedulingKey);
	saveTab();
	document.bla.submit();
}

// nur für Format dd.mm.yyyy hh:ii
function getDateFromString(value) {
	var date = new Date(value.substr(6,4), value.substr(3,2)-1, value.substr(0,2),
			value.substr(11,2), value.substr(14,2), 0);
	return date;
}

function disableResourceType(id) {
	var input = document.getElementById(id);
	input.value = "";
	input.selectedIndex=0;
	input.title = "";
	input.className = input.className.replace(/ errorServerTD/g,"");
	input.className = input.className.replace(/ errorInputTD/g,"");
	var td = input.parentNode;
	td.style.display = 'none';	
	td.previousSibling.style.display = 'none';	
	td.nextSibling.style.display = 'none';	
}

function enableResourceType(id) {
	var td = document.getElementById(id).parentNode;
	td.style.display = 'table-cell';	
	td.previousSibling.style.display = 'table-cell';
	td.nextSibling.style.display = 'table-cell';	
}

function enableButton(name, enable) {
	//alert("form onchange("+name+") hat geklappt!!!");
	//document.getElementsByName(name)[0].disabled = false;
	var field = document.getElementById(name);
	//alert("document.getElementById("+name+")=" + field);
	if (field != null) {
		field.disabled = !enable;
		
		/*if (enable) {
			field.disabled = false;
		} else {
			field.disabled = true;
			//field.disabled.value = "disabled";
		}*/
		
		//alert(field.name + ": disabled=" + field.disabled + ", disabled.value=" + field.disabled.value);
	}
}

function dontSave(event) {
	//var save = document.getElementsByName("Save")[0];
	var save = document.getElementById("Save");
	//alert("save="+save+", save.disabled="+save.disabled+", event.keyCode="+event.keyCode+", typeof(event.keyCode)="+typeof(event.keyCode)+", event.which="+event.which+", typeof(event.which)="+typeof(event.which));
	// nur auf Maustaste oder Space reagieren
	if (!save.disabled && (event.keyCode==32 || typeof(event.keyCode)=="undefined" || typeof(event.which)=="undefined")) {
		if (confirm(msgCancelWithoutSave)) {
			return true;
		} else {
			return false;
		}
	} else {
		return true;
	}
}

function saveTab() {
	var selectedTab = $("#tabs").tabs('option', 'selected');
	var field = document.getElementById("selectedTab");
	field.value=selectedTab;
	
//	var localePic = document.getElementById("LocalePic");
//	var language = document.getElementById("language");
//	language.value = localePic.value; // IE doesnt transfer value of input fields with type "image"
	
	//alert("set selectedTab="+field.value);
	return true;
}

function completeForce() {
	if (confirm(msgCompleteForce)) {
		document.bla.CompleteForce.value="CompleteForce";
		document.bla.submit();
	}
	
}

/**
 * try to fix sexycombobox bug: only first combobox works with jquery tabs in firefox
 * must be fired after change of jquery tab 
 * @param field
 *
function fixSexyComboboxes2(field) {
	//alert("field.tagName="+field.tagName+", field.className="+field.className);
	if (field.tagName=="DIV" && field.className=="list-wrapper invisible") {
		alert("fix "+field.tagName+": "+field.className);
		field.style.overflowY = "scroll";
		field.style.height = "200px";
	}
	
	var childs = field.childNodes;
	for (var i=0;i<childs.length;i++) {
		fixSexyComboboxes2(childs[i]);
	}
}*/

/**
 * try to fix sexycombobox bug: only first combobox works with jquery tabs in firefox
 * must be fired after change of jquery tab 
 * @param field
 *
function fixSexyComboboxes(field) {
	//alert("field.tagName="+field.tagName+", field.className="+field.className);
	if (field.tagName=="DIV" && field.className=="icon") {
		alert("fix "+field.tagName+": "+field.className);
		field.onclick = new Function("fixSexyComboboxes2(document.bla)");
	}
	
	var childs = field.childNodes;
	for (var i=0;i<childs.length;i++) {
		fixSexyComboboxes(childs[i]);
	}
}*/

/**
 * @see http://forum.jswelt.de/tutorials-javascript/36563-editierbare-combobox-very-simple.html
 * @param el
 */
function comboBox(el, length)
{
    var val = el.options[el.selectedIndex].value;
    if(!val && (length==0 || length==el.options.length))
    {
         var input = document.createElement('input');
         el.parentNode.appendChild( input );
         input.focus();
         el.style.display = 'none';
         input.onblur = function()
         {
              var new_val = input.value;
              el.style.display = '';
              el.parentNode.removeChild( input );
              if(new_val)
              {
                  var o = new Option(new_val, new_val);
                  var l = el.options.length;
                  var tmp = el.options[l];
                  el.options[l] = o;
                  el.options[l+1] = tmp;
                  el.selectedIndex = l;
              }
         };
    }
}
