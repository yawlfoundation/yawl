// JavaScript Document
//var onetypeValidation = "^[1-9]+$";
var onetypeValidation = "^[1-9]\\d*$";
var zerotypeValidation = "^[0-9]+$";
var doubleValidation = "^[-+]?[0-9]+(\.[0-9]+)?$";
var phoneValidation = "^[\\d\\s\\+\\-\\/\\(\\)]+$";
var timeValidation = "^((?:[01]\\d)|(?:2[0-3])):([0-5]\\d)$";
var textValidation = "^[A-Za-z\\s,().0-9!\\[\\]\\?\\'\\;\\\\/@\\$%^\\*\\+\\-\\_\\:\\u00A1-\\uFFFF]+$";
//got this off the internet. It seems to work for all combinations, maybe use a similar one for our purposes.
//It is way too long, so consider a simpler solution.
//general format is dd/mm/yyyy.
var realdateValidation = "^((((0?[1-9]|[12]\\d|3[01])[\\.\\-\/](0?[13578]|1[02])[\\.\\-\/]" +
                         "((1[6-9]|[2-9]\\d)?\\d{2}))|((0?[1-9]|[12]\\d|30)[\\.\\-\/]" +
                         "(0?[13456789]|1[012])[\\.\\-\/]((1[6-9]|[2-9]\\d)?\\d{2}))|" +
                         "((0?[1-9]|1\\d|2[0-8])[\\.\\-\/]0?2[\\.\\-\/]((1[6-9]|[2-9]\\d)?\\d{2}))|" +
                         "(29[\\.\\-\/]0?2[\\.\\-\/]((1[6-9]|[2-9]\\d)?(0[48]|[2468][048]|[13579][26])|" +
                         "((16|[2468][048]|[3579][26])00)|00)))|(((0[1-9]|[12]\\d|3[01])(0[13578]|1[02])" +
                         "((1[6-9]|[2-9]\\d)?\\d{2}))|((0[1-9]|[12]\\d|30)(0[13456789]|1[012])" +
                         "((1[6-9]|[2-9]\\d)?\\d{2}))|((0[1-9]|1\\d|2[0-8])02((1[6-9]|[2-9]\\d)?\\d{2}))|" +
                         "(2902((1[6-9]|[2-9]\\d)?(0[48]|[2468][048]|[13579][26])|((16|[2468][048]|[3579][26])00)|" +
                         "00))))$";

//---------------------------------Validation Code-------------------------------//
function validateForm(formName){
	var form = document.getElementsByName(formName)[0];
    var elements = form.elements;
    var isInError = false;
    for (var x =0; x < elements.length; x++) {
        var element = elements[x]; 
		if (element.type == 'text' && ! element.readOnly) {
			var pattern = element.getAttribute("pattern");
			
			if (pattern == 'textValidation') {
                pattern = textValidation;
            }
			
			if (pattern == 'phoneValidation') {
                pattern = phoneValidation;
            }
			
			if (pattern == 'realdateValidation') {
               	pattern = realdateValidation;
            }
			
			if (pattern == 'onetypeValidation') {
                pattern = onetypeValidation;
            }
			
			if (pattern == 'zerotypeValidation') {
               	pattern = zerotypeValidation;
            }
			
			if (pattern == 'doubleValidation') {
               	pattern = doubleValidation;
           }
		   if (pattern == 'timeValidation') {
               	pattern = timeValidation;
           }

			if (element.value.search(pattern) == -1) {
				element.className = "error";
				isInError = true;
			} else {
				element.className = "valid";
			}
		}
	}
	
	if (isInError) {
       alert("Some fields were incomplete.\nPlease complete them and try again.");
   }
   return !isInError;
}
//---------------------------------Trackpoints Code-----------------------------//
function deleteTrackpointRow(element){
	if(element.parentNode.parentNode.parentNode.childNodes.length >3){
		element.parentNode.parentNode.parentNode.removeChild(element.parentNode.parentNode);
	}
}

function addTrackpointRow(){
	var trackpointLines = document.getElementById('trackpoints');

	var newLine = document.createElement('div');
	newLine.setAttribute("id", "trackpointEntry");

	var trackpoint = createTextField("entry", "trackpoint", "Enter Trackpoint [String]", "", "textValidation");
	var deleteLine = createTrackpointDelete();

	newLine.appendChild(trackpoint);
	newLine.appendChild(deleteLine);

	trackpointLines.appendChild(newLine);
}

function createTrackpointDelete(){
	var div = document.createElement('div');
	div.setAttribute('id',"orderLinesDelete");
	var deleteButton = document.createElement("INPUT");
	deleteButton.setAttribute("type", "button");
	deleteButton.setAttribute("name", "delete");
	deleteButton.setAttribute("id", "delete");
	deleteButton.setAttribute("value", "Delete");
	deleteButton.setAttribute("onClick", "deleteTrackpointRow(this)");
	div.appendChild(deleteButton);
	return div;
}

//---------------------------------Trackpoints Code-----------------------------//
function deletePackagesRow(element){
	if(element.parentNode.parentNode.parentNode.childNodes.length >3){
		element.parentNode.parentNode.parentNode.removeChild(element.parentNode.parentNode);
	}
}

function addPackagesRow(){
	var trackpointLines = document.getElementById('allPackages');

	var newLine = document.createElement('div');
	newLine.setAttribute("id", "packageEntry");

	var packageid = createTextField("packageid", "packageID", "Enter Package ID [String]", "", "textValidation");
	var volume = createVolumeDropdown("packagesVolume", "volume", "Select the Appropriate Volume");
	var deleteLine = createPackagesDelete();

	newLine.appendChild(packageid);
	newLine.appendChild(volume);
	newLine.appendChild(deleteLine);

	trackpointLines.appendChild(newLine);
}

function createPackagesDelete(){
	var div = document.createElement('div');
	div.setAttribute('id',"packageDelete");
	var deleteButton = document.createElement("INPUT");
	deleteButton.setAttribute("type", "button");
	deleteButton.setAttribute("name", "delete");
	deleteButton.setAttribute("id", "delete");
	deleteButton.setAttribute("value", "Delete");
	deleteButton.setAttribute("onClick", "deletePackagesRow(this)");
	div.appendChild(deleteButton);
	return div;
}

function createVolumeDropdown(divName, name, tooltip){
	var div = document.createElement('div');
	div.setAttribute('id', divName);
	var input =  document.createElement("SELECT");
	input.setAttribute("name", name);
	input.setAttribute("id", name);

	var o1 = document.createElement("OPTION");
	o1.setAttribute("value", "25");
	o1.appendChild(document.createTextNode("25"));

	var o2 = document.createElement("OPTION");
	o2.setAttribute("value", "50");
	o2.appendChild(document.createTextNode("50"));
	o2.selected = true;

	var o3 = document.createElement("OPTION");
	o3.setAttribute("value", "100");
	o3.appendChild(document.createTextNode("100"));

	var o4 = document.createElement("OPTION");
	o4.setAttribute("value", "200");
	o4.appendChild(document.createTextNode("200"));

	input.appendChild(o1);
	input.appendChild(o2);
	input.appendChild(o3);
	input.appendChild(o4);

	div.appendChild(input);
	return div;
}


//---------------------------------Trackpoints Notice Code-----------------------------//
function deleteTrackpointNoticeRow(element){
	if(element.parentNode.parentNode.parentNode.childNodes.length >1){
		element.parentNode.parentNode.parentNode.removeChild(element.parentNode.parentNode);
	}
}

function addTrackpointNoticeRow(){
	var trackpointLines = document.getElementById('allLines');

	var newLine = document.createElement('div');
	newLine.setAttribute("id", "trackpointNoticeEntry");

	var ordernumber = createTextField("trackpointOrderNumber", "ordernumber", "Enter Order Number [String]", "", "textValidation");
	var shipmentnumber = createTextField("trackpointShipmentNumber", "shipmentnumber", "Enter Shipment Number [String]", "", "textValidation");
	var trackpoint = createTextField("trackpointTrackpoint", "trackpointN", "Enter Trackpoint [String]", "", "textValidation");
	var arrivaltime = createTextField("trackpointArrivalTime", "arrivaltime", "Enter Arrive Time [HH:MM:SS]", "", "timeValidation");
	var departuretime = createTextField("trackpointDepartureTime", "departuretime", "Enter Departure Time [HH:MM:SS]", "", "timeValidation");
	var notes = createTextField("trackpointNotes", "notes", "Enter Notes [String]", "", "textValidation");
	var deleteLine = createTrackpointNoticeDelete();

	newLine.appendChild(ordernumber);
	newLine.appendChild(shipmentnumber);
	newLine.appendChild(trackpoint);
	newLine.appendChild(arrivaltime);
	newLine.appendChild(departuretime);
	newLine.appendChild(notes);
	newLine.appendChild(deleteLine);

	trackpointLines.appendChild(newLine);
}

function createTrackpointNoticeDelete(){
	var div = document.createElement('div');
	div.setAttribute('id',"trackpointDelete");
	var deleteButton = document.createElement("INPUT");
	deleteButton.setAttribute("type", "button");
	deleteButton.setAttribute("name", "delete");
	deleteButton.setAttribute("id", "delete");
	deleteButton.setAttribute("value", "Delete");
	deleteButton.setAttribute("onClick", "deleteTrackpointNoticeRow(this)");
	div.appendChild(deleteButton);
	return div;
}

//---------------------------------Order Row Code-------------------------------//

function renumberLineNumbers(){
	var lines = document.getElementsByName("linenumber");
	for (i=0; i< lines.length; i++){
		lines[i].value = i+1;
	}
}


function deleteOrderRow(element){
	if(element.parentNode.parentNode.parentNode.childNodes.length >3){
		element.parentNode.parentNode.parentNode.removeChild(element.parentNode.parentNode);
		renumberLineNumbers();
	}
}

function addOrderRow(){
	var orderLines = document.getElementById('allLines');
	
	var newLine = document.createElement('div');
	newLine.setAttribute("id", "entry");
	
	var lines = document.getElementsByName("linenumber");
	
	var linenumber = createTextField("orderLineNumber", "linenumber", "Enter Line Number [Integer]", lines.length  + 1, "onetypeValidation");
	var unitcode = createTextField("orderUnitCode", "unitcode","Enter Unit Code [String]", "", "textValidation");
	var unitdescription = createTextArea("orderUnitDescription", "unitdescription", "Enter Unit Description [String]", "", "");
	var unitquantity = createTextField("orderUnitQuantity", "unitquantity", "Enter Unit Quantity [Integer]", "", "onetypeValidation");
	var action = createDropdown("orderAction", "action", "Select the Appropriate Action");
	var deleteLine = createOrderDelete();
		
	newLine.appendChild(linenumber);
	newLine.appendChild(unitcode);
	newLine.appendChild(unitdescription);
	newLine.appendChild(unitquantity);
	newLine.appendChild(action);
	newLine.appendChild(deleteLine);
	
	orderLines.appendChild(newLine);
}

function createTextField(divName, name, tooltip, value, validation){
	var div = document.createElement('div');
	div.setAttribute('id', divName);
	div.appendChild(createCommonValidationTextBox(name, tooltip, value, validation));
	return div;
}

function createTextArea(divName, name, tooltip, value){
	var div = document.createElement('div');
	div.setAttribute('id', divName);
	div.appendChild(createNoValidationTextArea(name, tooltip, value));
	return div;
}

function createDropdown(divName, name, tooltip){
	var div = document.createElement('div');
	div.setAttribute('id', divName);
	var input =  document.createElement("SELECT");
	input.setAttribute("name", name);
	input.setAttribute("id", name);
	
	var o1 = document.createElement("OPTION");
	o1.setAttribute("value", "");
	o1.appendChild(document.createTextNode(" "));
	
	var o2 = document.createElement("OPTION");
	o2.setAttribute("value", "Added");
	o2.appendChild(document.createTextNode("Added"));
	o2.selected = true;
	
	var o3 = document.createElement("OPTION");
	o3.setAttribute("value", "Modified");
	o3.appendChild(document.createTextNode("Modified"));
	
	input.appendChild(o1);
	input.appendChild(o2);
	input.appendChild(o3);
	
	div.appendChild(input);
	return div;
}

function createOrderDelete(){
	var div = document.createElement('div');
	div.setAttribute('id',"orderLinesDelete");
	var deleteButton = document.createElement("INPUT");
	deleteButton.setAttribute("type", "button");
	deleteButton.setAttribute("name", "delete");
	deleteButton.setAttribute("id", "delete");
	deleteButton.setAttribute("value", "Delete");
	deleteButton.setAttribute("onClick", "deleteOrderRow(this)");
	div.appendChild(deleteButton);
	return div;
}

function createCommonValidationTextBox(name, tooltip, value, validation) {
    var input =  document.createElement("INPUT");
    input.setAttribute("name", name);
    input.setAttribute("id", name);
    input.setAttribute("value", value);
	input.setAttribute("pattern", validation);
    input.title = tooltip;
    return input;
}

function createNoValidationTextArea(name, tooltip, value) {
    var input =  document.createElement("TEXTAREA");
    input.setAttribute("name", name);
    input.setAttribute("id", name);
    input.setAttribute("value", value);
    input.title = tooltip;
    return input;
}