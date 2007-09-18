var telNumberValidation = "^[\\d\\s\\+\\-]+$";
var emailValidation = "^((\\w)+(\\.)*)+\\w+@(\\w)+(\\w\\.)*(\\.[A-Za-z]+)+$";
var textValidation = "^[A-Za-z\\s]+$";
var textAreaValidation = "^[A-Za-z\\s,().0-9!\\[\\]\\\\/@\\$%^\\*\\-]+$";
//var dateValidation = "^[0-2][0-3]:[0-5][0-9]:[0-5][0-9]$";
//var dateValidation = "^\\d{2}:\\d{2}:\\d{2}$";
var dateValidation = "^((?:[01]\\d)|(?:2[0-3])):([0-5]\\d):([0-5]\\d)$";
var numberValidation = "^(\\d)+$"

function validateFields(formName) {
    var form = document.getElementsByName(formName)[0];
    var elements = form.elements;
    var isInError = false;
    for (var x =0; x < elements.length; x++) {
        var element = elements[x];
        if (element.type == 'text' || element.type == 'textarea') {
            var pattern = element.getAttribute("pattern");
            if (pattern != null) {
                if (pattern == 'date') {
                    pattern = dateValidation;
                }

                if (pattern == 'any_text') {
                    pattern = textAreaValidation;                    
                }

                if (pattern == 'text') {
                    pattern = textValidation;    
                }

                if (pattern == 'number') {
                    pattern = numberValidation;                    
                }
                
                if (element.value.search(pattern) == -1) {
                    element.className = "error";
                    isInError = true;
                } else {
                    element.className = "valid";
                }
            }
        }
   }

   if (isInError) {
       alert("Some fields were incomplete.\nPlease complete them and try again.");
   }
   return !isInError;
}

function showFieldsInError(fieldIds) {    
    for (var x = 0; x < fieldIds.length; x++) {
        var field = document.getElementById(fieldIds[x]);        
        field.className = "error";        
    }

    if (fieldIds.length > 0) {
        alert("Some fields were incomplete.\nPlease complete them and try again.");
    }
}

function showValidFields(fieldIds) {
    for (var x = 0; x < fieldIds.length; x++) {
        var field = document.getElementById(fieldIds[x]);
        field.className = "valid";
    }
}

function createTextBoxWithNoValidation(id, size, value) {
	var input =  document.createElement("INPUT");
	input.setAttribute("size", size);
	input.setAttribute("name", id);
	input.setAttribute("id", id);
	input.setAttribute("value", value);    
    return input;
}

function createTextBox(id, size, value, tooltip) {
    return createCommonValidationTextBox(id, size, value, tooltip, textValidation);
}

function createTelNumberTextBox(id, size, value, tooltip) {
    return createCommonValidationTextBox(id, size, value, tooltip, telNumberValidation);
}

function createEmailTextBox(id, size, value, tooltip) {
    return createCommonValidationTextBox(id, size, value, tooltip, emailValidation);
}

function createDateTextBox(id, size, value, tooltip) {
    return createCommonValidationTextBox(id, size, value, tooltip, dateValidation);    
}

function createNumberTextBox(id, size, value, tooltip) {
    return createCommonValidationTextBox(id, size, value, tooltip, numberValidation);
}

function createAnyTextTextBox(id, size, value, tooltip) {
    return createCommonValidationTextBox(id, size, value, tooltip, textAreaValidation);
}

function createSpecialValidationTextBox(id, size, value, tooltip, specialValidation) {
    return createCommonValidationTextBox(id, size, value, tooltip, specialValidation);
}

function createCommonValidationTextBox(id, size, value, tooltip, validation) {
    var input =  document.createElement("INPUT");
    input.setAttribute("size", size);
    input.setAttribute("name", id);
    input.setAttribute("id", id);
    input.setAttribute("value", value);
    input.title = tooltip;
    //you have to escape all the escaped RE characters eg. \w has to be \\w.
    input.setAttribute("pattern", validation);
    return input;
}

function createSignatureApplet(property, propertyCount) {
    var applet = document.createElement("applet");
    applet.setAttribute("code", "signature.SignA.class");
    applet.setAttribute("archive", "SignA.jar");
    applet.setAttribute("width", "350");
    applet.setAttribute("height", "60");
    applet.setAttribute("name", "SignA");
    applet.setAttribute("MAYSCRIPT", "MAYSCRIPT");

    createAppletParmeter(applet, "load_url", "");
    createAppletParmeter(applet, "save_url", "c:/");
    createAppletParmeter(applet, "propertyRoot", property);
    createAppletParmeter(applet, "index", propertyCount);
    return applet;
}

function createAppletParmeter(applet, property, value) {
    var param = document.createElement("param");
    param.setAttribute("name", property);
    param.setAttribute("value", value);
    applet.appendChild(param);
}

function getParam(name){
  var start=location.search.indexOf("?"+name+"=");
  if (start<0) start=location.search.indexOf("&"+name+"=");
  if (start<0) return '';
  start += name.length+2;
  var end=location.search.indexOf("&",start)-1;
  if (end<0) end=location.search.length;
  var result='';
  for(var i=start;i<=end;i++) {
    var c=location.search.charAt(i);
    result=result+(c=='+'?' ':c);
  }
  //window.alert('Result = '+result);
  return unescape(result);
}

function getParameters(){
	document.form1.workItemID.value = getParam('workItemID');
	document.form1.userID.value = getParam('userID');
	document.form1.sessionHandle.value = getParam('sessionHandle');
	document.form1.JSESSIONID.value = getParam('JSESSIONID');
	document.form1.submit.value = "htmlForm";
}

//function for textarea details
function createTextArea(id, size, value, tooltip) {
	var input =  document.createElement("TEXTAREA");
	input.setAttribute("cols", size);
	input.setAttribute("name", id);
	input.setAttribute("id", id);
    input.setAttribute("value", value);
    input.title = tooltip;
    //re-add this validation if required.
//    input.setAttribute("pattern", textAreaValidation);
    return input;
}

//function for dropdown list details
function createDropdownList(name) {
	var option = document.createElement("OPTION");
	option.setAttribute("value", name);
	option.appendChild(document.createTextNode(name));
	return option;
}