var telNumberValidation = "^[\\d\\s\\+\\-\\/\\(\\)]+$";
var emailValidation = "^((\\w)+(\\.)*)+\\w+@(\\w)+(\\w\\.)*(\\.[A-Za-z]+)+$";
var textValidation = "^[A-Za-z\\s]+$";
var textAreaValidation = "^[A-Za-z\\s,().0-9!\\[\\]\\?\\'\\;\\\\/@\\$%^\\*\\-\\_\\:]+$";
var timeValidation = "^((?:[01]\\d)|(?:2[0-3])):([0-5]\\d):([0-5]\\d)$";
var numberValidation = "^(\\d)+$"

//got this off the internet. It seems to work for all combinations, maybe use a similar one for our purposes.
//It is way too long, so consider a simpler solution.
//general format is dd/mm/yyyy.
var realDateValidation = "^((((0?[1-9]|[12]\\d|3[01])[\\.\\-\/](0?[13578]|1[02])[\\.\\-\/]" +
                         "((1[6-9]|[2-9]\\d)?\\d{2}))|((0?[1-9]|[12]\\d|30)[\\.\\-\/]" +
                         "(0?[13456789]|1[012])[\\.\\-\/]((1[6-9]|[2-9]\\d)?\\d{2}))|" +
                         "((0?[1-9]|1\\d|2[0-8])[\\.\\-\/]0?2[\\.\\-\/]((1[6-9]|[2-9]\\d)?\\d{2}))|" +
                         "(29[\\.\\-\/]0?2[\\.\\-\/]((1[6-9]|[2-9]\\d)?(0[48]|[2468][048]|[13579][26])|" +
                         "((16|[2468][048]|[3579][26])00)|00)))|(((0[1-9]|[12]\\d|3[01])(0[13578]|1[02])" +
                         "((1[6-9]|[2-9]\\d)?\\d{2}))|((0[1-9]|[12]\\d|30)(0[13456789]|1[012])" +
                         "((1[6-9]|[2-9]\\d)?\\d{2}))|((0[1-9]|1\\d|2[0-8])02((1[6-9]|[2-9]\\d)?\\d{2}))|" +
                         "(2902((1[6-9]|[2-9]\\d)?(0[48]|[2468][048]|[13579][26])|((16|[2468][048]|[3579][26])00)|" +
                         "00))))$";

function validateFields(formName) {
    var form = document.getElementsByName(formName)[0];
    var elements = form.elements;
    var isInError = false;
    for (var x =0; x < elements.length; x++) {
        var element = elements[x];        
        if ((element.type == 'text' || element.type == 'textarea') &&
            doesNotContainToken(element, "$") &&
            element.className != 'hidden') {
            var pattern = element.getAttribute("pattern");
            if (pattern != null) {
                //this is really time validation. change.
                if (pattern == 'date' || pattern == 'time') {
                    pattern = timeValidation;
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

                if (pattern == 'tel') {
                    pattern = telNumberValidation;
                }

				if (pattern == 'email') {
                    pattern = emailValidation;
                }

                if (pattern == 'real_date') {
                    pattern = realDateValidation;
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

   function doesNotContainToken(element, token) {
       return element.name.indexOf(token) == -1;
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
    return createCommonValidationTextBox(id, size, value, tooltip, timeValidation);
}

function createRealDateTextBox(id, size, value, tooltip) {
    return createCommonValidationTextBox(id, size, value, tooltip, realDateValidation);
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
function createAnyTextTextArea(id, size, value, tooltip) {
	var input =  document.createElement("TEXTAREA");
	input.setAttribute("cols", size);
	input.setAttribute("name", id);
	input.setAttribute("id", id);
    input.setAttribute("value", value);
    input.title = tooltip;
	input.setAttribute("pattern", textAreaValidation);
    return input;
}

//function for dropdown list details
function createDropdownList(name) {
	var option = document.createElement("OPTION");
	option.setAttribute("value", name);
	option.appendChild(document.createTextNode(name));
	return option;
}

function createCheckBox(name, value, checked) {
    var checkbox =  document.createElement("INPUT");
    checkbox.setAttribute("type", "checkbox");
    if (checked) {
        checkbox.setAttribute("checked", "");
    }

    checkbox.setAttribute("value", value);
    checkbox.setAttribute("name", name);
    checkbox.setAttribute("id", name);
    return checkbox;
}

/**
 * Call this function if you need to delete a single row.
 * @param tableName The name of the table to delete the row from.
 * @param countName The name of the counter to update.
 * @param headerSize The header size of the table. (number of rows above the data).
 * @param footerSize The footer size of the table (number of rows below the data).
 * @param addFn The function to add a single new row.
 * @param paramForFn The parameter for the add function if any.
  */
function deleteRows(tableName, countName, headerSize, footerSize, addFn, paramForFn) {
        var table = document.getElementById(tableName);
        var rows = table.rows.length;
        var count = getCountByName(countName);
        if (rows > (headerSize+footerSize)) {
            //delete from the bottom, removing 1 for the 0-based index.
            table.deleteRow(rows-(footerSize+1));
            if (count > 0) {
                count = decCount(countName);
            }

            if (count == 0) {
                //add an empty row after the last row has been deleted. this allows all data-containing rows to be
                // deleted and for there to be a single empty row at any point in time.
                //TODO: workaround to avoid adding another function. refactor if possible.
                if (paramForFn != null) {
                    addFn(paramForFn);//calls a parametered-function.
                } else {
                    addFn();
                }
            }
        }
}
/**
 * Call this function if you need to delete more than a single row.
 * @param tableName The name of the table to delete the row from.
 * @param countName The name of the counter to update.
 * @param headerSize The header size of the table. (number of rows above the data).
 * @param footerSize The footer size of the table (number of rows below the data).
 * @param addFn The function to add a single new row.
 * @param paramForFn The parameter for the add function if any.
 * @param delNum The number of rows to delete, per deletion.
 */
function deleteMultipleRows(tableName, countName, headerSize, footerSize, addFn, paramForFn, delNum) {
        var table = document.getElementById(tableName);
        var rows = table.rows.length;
        var count = getCountByName(countName);

        if (rows - (headerSize+footerSize) >= delNum) {
            //delete from the bottom, removing 1 for the 0-based index.

            for (var x = 0; x < delNum; x++) {
                table.deleteRow(rows-(footerSize+1));
                rows = table.rows.length;
            }

            if (count > 0) {
                count = decCount(countName);
            }

            if (count == 0) {
                //add an empty row after the last row has been deleted. this allows all data-containing rows to be
                // deleted and for there to be a single empty row at any point in time.
                //TODO: workaround to avoid adding another function. refactor if possible.
                if (paramForFn != null) {
                    addFn(paramForFn);//calls a parametered-function.
                } else {
                    addFn();
                }
            }
        }
}


function createHiddenField(id, value) {
    var input =  document.createElement("INPUT");
    input.setAttribute("type", "hidden");
    input.setAttribute("name", id);
    input.setAttribute("id", id);
    input.setAttribute("value", value);
    return input;
}

function createRadioButton(id, value) {
    var input =  document.createElement("INPUT");
    input.setAttribute("type", "radio");
    input.setAttribute("name", id);
    input.setAttribute("id", id);
    input.setAttribute("value", value);
    return input;
}
