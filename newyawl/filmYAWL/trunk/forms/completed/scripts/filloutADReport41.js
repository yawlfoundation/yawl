var artist_count = 1;
var artistHeaderSize = 3;
var artistFooterSize = 2;
var artistHeaderAndFooterSize = artistHeaderSize + artistFooterSize;

var extras_count = 0;
var backgroundArtistHeaderSize = 3;
var backgroundArtistFooterSize = 2;
var backgroundArtistHeaderAndFooterSize = backgroundArtistHeaderSize + backgroundArtistFooterSize;

var child_count = 0;
var childHeaderSize = 3;
var childFooterSize = 2;
var childHeaderAndFooterSize = childHeaderSize + childFooterSize;

var crew_count = 0;
var crewHeaderSize = 2;
var crewFooterSize = 3;
var crewHeaderAndFooterSize = crewHeaderSize + crewFooterSize;

var meal_count=0;
var mealHeaderSize = 2;
var mealFooterSize = 2;
var mealHeaderAndFooterSize = mealHeaderSize + mealFooterSize;

function validateWithSignatories(form) {
    artist_count = document.getElementById("artist_count").value;
    extras_count = document.getElementById("extras_count").value;    
    return validateSignatory("artist_signature", artist_count) &&
           validateSignatory("backgroundartist_signature", extras_count) && validateFields(form);
}

function validateSignatory(sigPropertyRoot, numOfSigs) {
    for (var x = 0; x < numOfSigs; x++) {
        var signatureName = sigPropertyRoot +"_" + (x+1);
        var signatory = document.getElementById(signatureName);
        if (signatory != null && signatory.value == '') {
            alert("Some artists have not signed yet.");
            return false;
        }
    }

    return true;
}

function calculateDuration() {
	var meal = document.getElementById("meal_count").value;
	for(var count = 1; count <= meal; count ++) {
		var duration = "";
		var start = document.getElementById("meal_timefrom_" + count).value;
		var finish = document.getElementById("meal_timeto_" + count).value;

		var start_array = start.split(":");
		var finish_array = finish.split(":");

		var date1 = new Date(0, 0, 0, start_array[0], start_array[1], start_array[2]);
		var date2 = new Date(0, 0, 0, finish_array[0], finish_array[1], finish_array[2]);

		var milliseconds1 = date1.getTime();
		var milliseconds2 = date2.getTime();

		var difference = milliseconds2 - milliseconds1;

		var hoursDifference = Math.floor(difference/1000/60/60);
		difference = difference - hoursDifference*1000*60*60
		var minutesDifference = Math.floor(difference/1000/60);
		difference = difference - minutesDifference*1000*60
		var secondsDifference = Math.floor(difference/1000);

		if(hoursDifference.toString().length == 1) {
		duration += "0" + hoursDifference.toString() + ":";
		} else {
		duration += hoursDifference.toString() + ":";
		}
		if(minutesDifference.toString().length == 1) {
		duration +=  "0" + minutesDifference.toString() + ":";
		} else {
		duration += minutesDifference.toString() + ":";
		}
		if(secondsDifference.toString().length == 1) {
		duration +=  "0" + secondsDifference.toString();
		} else {
		duration += secondsDifference.toString() ;
		}
		document.getElementById("meal_duration_"+count).value = duration;
	}
}

//function for adding artist information
//TODO: If possible automatically generate add and delete code.
function addArtistRow(){    
    artist_count = document.getElementById("artist_count").value;
    artist_count++;
    document.getElementById("artist_count").value = artist_count;

    var table = document.getElementById("artist");
    var row = table.insertRow(table.rows.length - artistFooterSize);
    var leftCELL = row.insertCell(0);
    var artistCELL = row.insertCell(1);
    var puCELL = row.insertCell(2);
    var muwdcallscheduledCELL = row.insertCell(3);
    var muwdcallactualCELL = row.insertCell(4);
    var mealCELL = row.insertCell(5);
    var wrapCELL = row.insertCell(6);
    var travelCELL = row.insertCell(7);
    var signatureCELL = row.insertCell(8);
    var rightCELL = row.insertCell(9);

	row.setAttribute("valign", "top");
	row.setAttribute("align", "center");
	row.setAttribute("height", "30");

	leftCELL.className = "left";
	leftCELL.appendChild(document.createTextNode("\u00a0"));
	rightCELL.className = "right";
	rightCELL.appendChild(document.createTextNode("\u00a0"));

	artistCELL.appendChild(createTextBoxWithNoValidation("artist_" + artist_count, 15, "", "enter artist"));
	puCELL.appendChild(createTextBoxWithNoValidation("artist_pu_" + artist_count, 6, "", "enter artist pu"));
	muwdcallscheduledCELL.appendChild(createTextBoxWithNoValidation("artist_muwdcall_scheduled_" + artist_count, 6, "", "enter muwd call"));
	muwdcallactualCELL.appendChild(createTextBoxWithNoValidation("artist_muwdcall_actual_" + artist_count, 6, "", "enter muwd call actual"));
	mealCELL.appendChild(createTextBoxWithNoValidation("artist_meal_" + artist_count, 6, "", "enter artist meal time (hh:mm:ss)"));
	wrapCELL.appendChild(createTextBoxWithNoValidation("artist_wrap_" + artist_count, 6, "", "enter artist wrap time (hh:mm:ss)"));
	travelCELL.appendChild(createTextBoxWithNoValidation("artist_travel_" + artist_count, 6, "", "enter artist travel time (hh:mm:ss)"));

    signatureCELL.appendChild(createSignatureApplet("artist", artist_count));
    var hiddenSignatureUrl = document.createElement("input");
    hiddenSignatureUrl.type = "hidden";
    hiddenSignatureUrl.name = "artist_signature_" + artist_count;
    hiddenSignatureUrl.id = "artist_signature_" + artist_count;
    signatureCELL.appendChild(hiddenSignatureUrl);
}

function deleteArtistRow() {
    var table = document.getElementById("artist");
    var rows = table.rows.length;
    artist_count = document.getElementById("artist_count").value;

    if (rows > artistHeaderAndFooterSize) {
        //delete from the bottom, removing 1 for the 0-based index.
        table.deleteRow(rows-(artistFooterSize+1));
        if (artist_count > 0) {
            document.getElementById("artist_count").value = --artist_count;
        }

        if (artist_count == 0) {
            //add an empty row after the last row has been deleted.
            //this allows all data-containing rows to be deleted and for there to be a single empty row
            //at any point in time.
            addArtistRow();
        }
    }    
}

function deleteBackgroundArtistRow () {
    var table = document.getElementById("background_artist");
    var rows = table.rows.length;
    extras_count = document.getElementById("extras_count").value;
    
    if (rows > artistHeaderAndFooterSize) {
        //delete from the bottom, removing 1 for the 0-based index.
        table.deleteRow(rows-(backgroundArtistFooterSize+1));
        if (extras_count > 0) {
            document.getElementById("extras_count").value = --extras_count;
        }

        if (extras_count == 0) {
            //add an empty row after the last row has been deleted.
            //this allows all data-containing rows to be deleted and for there to be a single empty row
            //at any point in time.
            addBackgroundArtistRow();
        }
    }
}

//function for adding background artist information
function addBackgroundArtistRow(){
    extras_count = document.getElementById("extras_count").value;
    extras_count ++;
    document.getElementById("extras_count").value = extras_count;
    
    var table = document.getElementById("background_artist");
    var row = table.insertRow(table.rows.length - backgroundArtistFooterSize);

    var leftCELL = row.insertCell(0);
    var artistCELL = row.insertCell(1);
    var puCELL = row.insertCell(2);
    var muwdcallscheduledCELL = row.insertCell(3);
    var muwdcallactualCELL = row.insertCell(4);
    var mealCELL = row.insertCell(5);
    var wrapCELL = row.insertCell(6);
    var travelCELL = row.insertCell(7);
    var signatureCELL = row.insertCell(8);
    var rightCELL = row.insertCell(9);

	row.setAttribute("valign", "top");
	row.setAttribute("align", "center");
	row.setAttribute("height", "30");

	leftCELL.className = "left";
	leftCELL.appendChild(document.createTextNode("\u00a0"));
	rightCELL.className = "right";
	rightCELL.appendChild(document.createTextNode("\u00a0"));

	artistCELL.appendChild(createTextBoxWithNoValidation("backgroundartist_" + extras_count, 15, "", "enter background artist"));
	puCELL.appendChild(createTextBoxWithNoValidation("backgroundartist_pu_" + extras_count, 6, "", "enter pu"));
	muwdcallscheduledCELL.appendChild(createTextBoxWithNoValidation("backgroundartist_muwdcall_scheduled_" + extras_count, 6, "", "enter scheduled muwdcall"));
	muwdcallactualCELL.appendChild(createTextBoxWithNoValidation("backgroundartist_muwdcall_actual_" + extras_count, 6, "", "enter actual muwdcallt"));
	mealCELL.appendChild(createTextBoxWithNoValidation("backgroundartist_meal_" + extras_count, 6, "", "enter meal time"));
	wrapCELL.appendChild(createTextBoxWithNoValidation("backgroundartist_wrap_" + extras_count, 6, ""), "enter wrap time");
	travelCELL.appendChild(createTextBoxWithNoValidation("backgroundartist_travel_" + extras_count, 6, "", "enter travel time"));

    signatureCELL.appendChild(createSignatureApplet("backgroundartist", extras_count));
    var hiddenSignatureUrl = document.createElement("input");
    hiddenSignatureUrl.type = "hidden";
    hiddenSignatureUrl.name = "backgroundartist_signature_" + extras_count;
    hiddenSignatureUrl.id = "backgroundartist_signature_" + extras_count;
    signatureCELL.appendChild(hiddenSignatureUrl);
}

//function for adding child information
function addChildRow(){
    child_count = document.getElementById("child_count").value;
    child_count ++;
	document.getElementById("child_count").value = child_count;
    
    var table = document.getElementById("child");
    var row = table.insertRow(table.rows.length - childFooterSize);
    
    var leftCELL = row.insertCell(0);
    var childrenCELL = row.insertCell(1);
    var puCELL = row.insertCell(2);
    var muwdcallscheduledCELL = row.insertCell(3);
    var muwdcallactualCELL = row.insertCell(4);
    var mealCELL = row.insertCell(5);
    var wrapCELL = row.insertCell(6);
    var travelCELL = row.insertCell(7);
    var remarksCELL = row.insertCell(8);
    var rightCELL = row.insertCell(9);

	row.setAttribute("valign", "top");
	row.setAttribute("align", "center");

	leftCELL.className = "left";
	leftCELL.appendChild(document.createTextNode("\u00a0"));
	rightCELL.className = "right";
	rightCELL.appendChild(document.createTextNode("\u00a0"));

	childrenCELL.appendChild(createTextBox("children_" + child_count, 15, "", "enter child actor"));
	puCELL.appendChild(createTextBox("children_pu_" + child_count, 6, "", "enter pu"));
	muwdcallscheduledCELL.appendChild(createDateTextBox("children_muwdcall_scheduled_" + child_count, 6, "", "child muwdcall scheduled"));
	muwdcallactualCELL.appendChild(createDateTextBox("children_muwdcall_actual_" + child_count, 6, "", "child muwdcall actual"));
	mealCELL.appendChild(createDateTextBox("children_meal_" + child_count, 6, "", "enter meal time"));
	wrapCELL.appendChild(createDateTextBox("children_wrap_" + child_count, 6, "", "enter wrap time"));
	travelCELL.appendChild(createDateTextBox("children_travel_" + child_count, 6, "", "enter travel time"));
	remarksCELL.appendChild(createTextArea("children_remarks_" + child_count, 15, "", "enter remarks"));
}

function deleteChildRow () {    
    var table = document.getElementById("child");
    var rows = table.rows.length;
    child_count = document.getElementById("child_count").value;
    
    if (rows > childHeaderAndFooterSize) {
        //delete from the bottom, removing 1 for the 0-based index.
        table.deleteRow(rows-(childFooterSize+1));
        if (child_count > 0) {
            document.getElementById("child_count").value = --child_count;
        }

        if (child_count == 0) {
            //add an empty row after the last row has been deleted.
            //this allows all data-containing rows to be deleted and for there to be a single empty row
            //at any point in time.
            addChildRow();
        }
    }
}

function addCrewRow(){
	crew_count = document.getElementById("crew_count").value;
    crew_count ++;
    document.getElementById("crew_count").value = crew_count;

    var table = document.getElementById("crew");
    var row = table.insertRow(table.rows.length - crewFooterSize);

    var leftCELL = row.insertCell(0);
    var crewCELL = row.insertCell(1);
    var callCELL = row.insertCell(2);
    var travelinCELL = row.insertCell(3);
    var loccallCELL = row.insertCell(4);
    var mealCELL = row.insertCell(5);
    var wrapCELL = row.insertCell(6);
    var wraplocCELL = row.insertCell(7);
    var departlocCELL = row.insertCell(8);
    var traveloutCELL = row.insertCell(9);
    var remarksCELL = row.insertCell(10);
    var rightCELL = row.insertCell(11);
    var inp1 =  document.createElement("SELECT");

	row.setAttribute("valign", "top");
	row.setAttribute("align", "center");

	leftCELL.className = "left";
	leftCELL.appendChild(document.createTextNode("\u00a0"));
	rightCELL.className = "right";
	rightCELL.appendChild(document.createTextNode("\u00a0"));

	inp1.setAttribute("name", "crew_" + crew_count);
	inp1.setAttribute("id", "crew_" + crew_count);
	inp1.setAttribute("width", "10");
    populateCrewList(inp1);

    crewCELL.appendChild(inp1);
	crewCELL.appendChild(document.createElement("BR"));
	crewCELL.appendChild(createTextBoxWithNoValidation("crew_other_" + crew_count, 15, "[If other, specify]"));

    callCELL.appendChild(createDateTextBox("crew_call_" + crew_count, 5,"", "call"));
	travelinCELL.appendChild(createDateTextBox("crew_travelin_" + crew_count, 5,"", "travel in time"));
	loccallCELL.appendChild(createDateTextBox("crew_loccall_" + crew_count, 5,"", "local call time"));
	mealCELL.appendChild(createDateTextBox("crew_meal_" + crew_count, 5, "", "meal time"));
	wrapCELL.appendChild(createDateTextBox("crew_wrap_" + crew_count, 5, "", "wrapt ime"));
	wraplocCELL.appendChild(createDateTextBox("crew_wraploc_" + crew_count, 5, "", "wrap local time"));
	departlocCELL.appendChild(createDateTextBox("crew_departloc_" + crew_count, 5, "", "departure local time"));
	traveloutCELL.appendChild(createDateTextBox("crew_travelout_" + crew_count, 5, "", "travel out time"));
	remarksCELL.appendChild(createTextArea("crew_remarks_" + crew_count, 8));
}

function populateCrewList(inp1) {
    inp1.appendChild(createDropdownList("2nd AD"));
    inp1.appendChild(createDropdownList("Continuity"));
    inp1.appendChild(createDropdownList("Camera"));
    inp1.appendChild(createDropdownList("Sound"));
    inp1.appendChild(createDropdownList("Makeup/Hair"));
    inp1.appendChild(createDropdownList("Wardrobe"));
    inp1.appendChild(createDropdownList("Unit"));
    inp1.appendChild(createDropdownList("Grips"));
    inp1.appendChild(createDropdownList("Electrics"));
    inp1.appendChild(createDropdownList("Standby Props"));
    inp1.appendChild(createDropdownList("Other ..."));    
}

function deleteCrewRow () {    
    var table = document.getElementById("crew");
    var rows = table.rows.length;
    crew_count = document.getElementById("crew_count").value;

    if (rows > crewHeaderAndFooterSize) {
        //delete from the bottom, removing 1 for the 0-based index.
        table.deleteRow(rows-(crewFooterSize+1));
        if (crew_count > 0) {
            document.getElementById("crew_count").value = --crew_count;
        }

        if (crew_count == 0) {
            //add an empty row after the last row has been deleted.
            //this allows all data-containing rows to be deleted and for there to be a single empty row
            //at any point in time.
            addCrewRow();
        }
    }
}

function addMealRow(){
	meal_count = document.getElementById("meal_count").value;
    meal_count ++;
    document.getElementById("meal_count").value = meal_count;

    var table = document.getElementById("meal");
    var row = table.insertRow(table.rows.length - mealFooterSize);

    var leftCELL = row.insertCell(0);
    var mealCELL = row.insertCell(1);
    var fromCELL = row.insertCell(2);
    var toCELL = row.insertCell(3);
    var numbersCELL = row.insertCell(4);
    var locationCELL = row.insertCell(5);
    var remarksCELL = row.insertCell(6);
    var rightCELL = row.insertCell(7);
    var inp1 =  document.createElement("SELECT");

    row.setAttribute("valign", "top");
	row.setAttribute("align", "center");

	leftCELL.className = "left";
	leftCELL.appendChild(document.createTextNode("\u00a0"));
	rightCELL.className = "right";
	rightCELL.appendChild(document.createTextNode("\u00a0"));

	inp1.setAttribute("name", "meal_" + meal_count);
	inp1.setAttribute("id", "meal_" + meal_count);
	inp1.setAttribute("width", "10");
    populateMeals(inp1);

    mealCELL.appendChild(inp1);
	fromCELL.appendChild(createDateTextBox("meal_timefrom_" + meal_count, 6, ""));
	toCELL.appendChild(createDateTextBox("meal_timeto_" + meal_count, 6, ""));
	toCELL.appendChild(createHidden("meal_duration_" + meal_count, 6));
	numbersCELL.appendChild(createNumberTextBox("meal_numbers_" + meal_count, 6, ""));
	locationCELL.appendChild(createTextBox("meal_location_" + meal_count, 20, ""));
	remarksCELL.appendChild(createTextArea("meal_remarks_" + meal_count, 20));
}

function populateMeals(inp1) {
    inp1.appendChild(createDropdownList("Breakfast"));
    inp1.appendChild(createDropdownList("Morning Tea"));
    inp1.appendChild(createDropdownList("Lunch"));
    inp1.appendChild(createDropdownList("Afternoon Tea"));
    inp1.appendChild(createDropdownList("Dinner"));
    inp1.appendChild(createDropdownList("Supper"));
}

function deleteMealRow () {
    var table = document.getElementById("meal");
    var rows = table.rows.length;
    meal_count = document.getElementById("meal_count").value;

    if (rows > mealHeaderAndFooterSize) {
        //delete from the bottom, removing 1 for the 0-based index.
        table.deleteRow(rows-(mealFooterSize+1));
        if (meal_count > 0) {
            document.getElementById("meal_count").value = --meal_count;
        }

        if (meal_count == 0) {
            //add an empty row after the last row has been deleted.
            //this allows all data-containing rows to be deleted and for there to be a single empty row
            //at any point in time.
            addMealRow();
        }
    }
}

//function for textbox details
function createHidden(id, size) {
	var input =  document.createElement("INPUT");
	input.setAttribute("size", size);
	input.setAttribute("name", id);
	input.setAttribute("id", id);
	input.setAttribute("type", "hidden");
	return input;
}