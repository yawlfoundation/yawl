var artist_count = 1;
var artistHeaderSize = 3;
var artistFooterSize = 2;
var artistHeaderAndFooterSize = artistHeaderSize + artistFooterSize;

var extras_count = 0;
var backgroundArtistHeaderSize = 3;
var backgroundArtistFooterSize = 2;
var backgroundArtistHeaderAndFooterSize = backgroundArtistHeaderSize + backgroundArtistFooterSize;

var child_count = 0;
var crew_count = 7;

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

	artistCELL.appendChild(createTextBox("artist_" + artist_count, 15, "", "enter artist"));
	puCELL.appendChild(createTextBox("artist_pu_" + artist_count, 6, "", "enter artist pu"));
	muwdcallscheduledCELL.appendChild(createTextBox("artist_muwdcall_scheduled_" + artist_count, 6, "", "enter muwd call"));
	muwdcallactualCELL.appendChild(createTextBox("artist_muwdcall_actual_" + artist_count, 6, "", "enter muwd call actual"));
	mealCELL.appendChild(createTextBox("artist_meal_" + artist_count, 6, "", "enter artist meal time (hh:mm:ss)"));
	wrapCELL.appendChild(createTextBox("artist_wrap_" + artist_count, 6, "", "enter artist wrap time (hh:mm:ss)"));
	travelCELL.appendChild(createTextBox("artist_travel_" + artist_count, 6, "", "enter artist travel time (hh:mm:ss)"));

    signatureCELL.appendChild(createSignatureApplet("artist", artist_count));
    var hiddenSignatureUrl = document.createElement("input");
    hiddenSignatureUrl.type = "hidden";
    hiddenSignatureUrl.name = "artist_signature_" + artist_count;
    signatureCELL.appendChild(hiddenSignatureUrl);
}

function deleteArtistRow() {
    var table = document.getElementById("artist");
    var rows = table.rows.length;

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

	artistCELL.appendChild(createTextBox("backgroundartist_" + extras_count, 15, "", "enter background artist"));
	puCELL.appendChild(createTextBox("backgroundartist_pu_" + extras_count, 6, "", "enter pu"));
	muwdcallscheduledCELL.appendChild(createTextBox("backgroundartist_muwdcall_scheduled_" + extras_count, 6, "", "enter scheduled muwdcall"));
	muwdcallactualCELL.appendChild(createTextBox("backgroundartist_muwdcall_actual_" + extras_count, 6, "", "enter actual muwdcallt"));
	mealCELL.appendChild(createTextBox("backgroundartist_meal_" + extras_count, 6, "", "enter meal time"));
	wrapCELL.appendChild(createTextBox("backgroundartist_wrap_" + extras_count, 6, ""), "enter wrap time");
	travelCELL.appendChild(createTextBox("backgroundartist_travel_" + extras_count, 6, "", "enter travel time"));

    signatureCELL.appendChild(createSignatureApplet("backgroundartist", extras_count));
    var hiddenSignatureUrl = document.createElement("input");
    hiddenSignatureUrl.type = "hidden";
    hiddenSignatureUrl.name = "backgroundartist_signature_" + extras_count;
    signatureCELL.appendChild(hiddenSignatureUrl);
}

//function for adding child information
function addChildRow(){
	child_count = document.getElementById("child_count").value;
	var tbody = document.getElementById("child").getElementsByTagName("tbody")[0];
	var row = document.createElement("TR");
	var leftCELL = document.createElement("TD");
	var childrenCELL = document.createElement("TD");
	var puCELL = document.createElement("TD");
	var muwdcallscheduledCELL = document.createElement("TD");
	var muwdcallactualCELL = document.createElement("TD");
	var mealCELL = document.createElement("TD");
	var wrapCELL = document.createElement("TD");
	var travelCELL = document.createElement("TD");
	var remarksCELL = document.createElement("TD");
	var rightCELL = document.createElement("TD");

	child_count ++;
	document.getElementById("child_count").value = child_count;

	row.setAttribute("valign", "top");
	row.setAttribute("align", "center");

	leftCELL.className = "left";
	leftCELL.appendChild(document.createTextNode("\u00a0"));
	rightCELL.className = "right";
	rightCELL.appendChild(document.createTextNode("\u00a0"));

	childrenCELL.appendChild(createTextBox("children_" + child_count, 15, ""));
	puCELL.appendChild(createTextBox("children_pu_" + child_count, 6, ""));
	muwdcallscheduledCELL.appendChild(createTextBox("children_muwdcall_scheduled_" + child_count, 6, ""));
	muwdcallactualCELL.appendChild(createTextBox("children_muwdcall_actual_" + child_count, 6, ""));
	mealCELL.appendChild(createTextBox("children_meal_" + child_count, 6, ""));
	wrapCELL.appendChild(createTextBox("children_wrap_" + child_count, 6, ""));
	travelCELL.appendChild(createTextBox("children_travel_" + child_count, 6, ""));
	remarksCELL.appendChild(createTextArea("children_remarks_" + child_count, 15));

	row.appendChild(leftCELL);
	row.appendChild(childrenCELL);
	row.appendChild(puCELL);
	row.appendChild(muwdcallscheduledCELL);
	row.appendChild(muwdcallactualCELL);
	row.appendChild(mealCELL);
	row.appendChild(wrapCELL);
	row.appendChild(travelCELL);
	row.appendChild(remarksCELL);
	row.appendChild(rightCELL);
	tbody.appendChild(row);
	//alert(row.innerHTML);
}

function addCrewRow(){
	crew_count = document.getElementById("crew_count").value;
	var tbody = document.getElementById("crew").getElementsByTagName("tbody")[0];
	var row = document.createElement("TR");
	var leftCELL = document.createElement("TD");
	var crewCELL = document.createElement("TD");
	var callCELL = document.createElement("TD");
	var travelinCELL = document.createElement("TD");
	var loccallCELL = document.createElement("TD");
	var mealCELL = document.createElement("TD");
	var wrapCELL = document.createElement("TD");
	var wraplocCELL = document.createElement("TD");
	var departlocCELL = document.createElement("TD");
	var traveloutCELL = document.createElement("TD");
	var remarksCELL = document.createElement("TD");
	var rightCELL = document.createElement("TD");
	var inp1 =  document.createElement("SELECT");

	crew_count ++;
	document.getElementById("crew_count").value = crew_count;

	row.setAttribute("valign", "top");
	row.setAttribute("align", "center");

	leftCELL.className = "left";
	leftCELL.appendChild(document.createTextNode("\u00a0"));
	rightCELL.className = "right";
	rightCELL.appendChild(document.createTextNode("\u00a0"));

	inp1.setAttribute("name", "crew_" + crew_count);
	inp1.setAttribute("id", "crew_" + crew_count);

	inp1.setAttribute("width", "10");


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


	crewCELL.appendChild(inp1);
	crewCELL.appendChild(document.createElement("BR"));
	crewCELL.appendChild(createTextBox("crew_other_" + crew_count, 15, "[If other, specify]"));
	callCELL.appendChild(createTextBox("crew_call_" + crew_count, 5,""));
	travelinCELL.appendChild(createTextBox("crew_travelin_" + crew_count, 5,""));
	loccallCELL.appendChild(createTextBox("crew_loccall_" + crew_count, 5,""));
	mealCELL.appendChild(createTextBox("crew_meal_" + crew_count, 5, ""));
	wrapCELL.appendChild(createTextBox("crew_wrap_" + crew_count, 5, ""));
	wraplocCELL.appendChild(createTextBox("crew_wraploc_" + crew_count, 5, ""));
	departlocCELL.appendChild(createTextBox("crew_departloc_" + crew_count, 5, ""));
	traveloutCELL.appendChild(createTextBox("crew_travelout_" + crew_count, 5, ""));
	remarksCELL.appendChild(createTextArea("crew_remarks_" + crew_count, 8));

	row.appendChild(leftCELL);
	row.appendChild(crewCELL);
	row.appendChild(callCELL);
	row.appendChild(travelinCELL);
	row.appendChild(loccallCELL);
	row.appendChild(mealCELL);
	row.appendChild(wrapCELL);
	row.appendChild(wraplocCELL);
	row.appendChild(departlocCELL);
	row.appendChild(traveloutCELL);
	row.appendChild(remarksCELL);
	row.appendChild(rightCELL);
	tbody.appendChild(row);
	//alert(row.innerHTML);
}
function addMealRow(){
	meal_count = document.getElementById("meal_count").value;
	var tbody = document.getElementById("meal").getElementsByTagName("tbody")[0];
	var row = document.createElement("TR");
	var leftCELL = document.createElement("TD");
	var mealCELL = document.createElement("TD");
	var fromCELL = document.createElement("TD");
	var toCELL = document.createElement("TD");
	var numbersCELL = document.createElement("TD");
	var locationCELL = document.createElement("TD");
	var remarksCELL = document.createElement("TD");
	var rightCELL = document.createElement("TD");
	var inp1 =  document.createElement("SELECT");

	meal_count ++;
	document.getElementById("meal_count").value = meal_count;

	row.setAttribute("valign", "top");
	row.setAttribute("align", "center");

	leftCELL.className = "left";
	leftCELL.appendChild(document.createTextNode("\u00a0"));
	rightCELL.className = "right";
	rightCELL.appendChild(document.createTextNode("\u00a0"));

	inp1.setAttribute("name", "meal_" + meal_count);
	inp1.setAttribute("id", "meal_" + meal_count);

	inp1.setAttribute("width", "10");

	inp1.appendChild(createDropdownList("Breakfast"));
	inp1.appendChild(createDropdownList("Morning Tea"));
	inp1.appendChild(createDropdownList("Lunch"));
	inp1.appendChild(createDropdownList("Afternoon Tea"));
	inp1.appendChild(createDropdownList("Dinner"));
	inp1.appendChild(createDropdownList("Supper"));

	mealCELL.appendChild(inp1);
	fromCELL.appendChild(createTextBox("meal_timefrom_" + meal_count, 6, ""));
	toCELL.appendChild(createTextBox("meal_timeto_" + meal_count, 6, ""));
	toCELL.appendChild(createHidden("meal_duration_" + meal_count, 6));
	numbersCELL.appendChild(createTextBox("meal_numbers_" + meal_count, 6, ""));
	locationCELL.appendChild(createTextBox("meal_location_" + meal_count, 20, ""));
	remarksCELL.appendChild(createTextArea("meal_remarks_" + meal_count, 20));

	row.appendChild(leftCELL);
	row.appendChild(mealCELL);
	row.appendChild(fromCELL);
	row.appendChild(toCELL);
	row.appendChild(numbersCELL);
	row.appendChild(locationCELL);
	row.appendChild(remarksCELL);
	row.appendChild(rightCELL);
	tbody.appendChild(row);
	//alert(row.innerHTML);
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

//function for textarea details
function createTextArea(id, size) {
	var input =  document.createElement("TEXTAREA");
	input.setAttribute("cols", size);
	input.setAttribute("name", id);
	input.setAttribute("id", id);
	return input;
}

//function for dropdown list details
function createDropdownList(name) {
	var option = document.createElement("OPTION");
	option.setAttribute("value", name);
	option.appendChild(document.createTextNode(name));
	return option;
}