//function for textbox details
function createInput(id, size, type, value) {
	var input =  document.createElement("INPUT");
	input.setAttribute("type", type);
	input.setAttribute("size", size);
	input.setAttribute("name", id);
	input.setAttribute("id", id);
	input.setAttribute("value", value);
	return input;
}

//function for dropdown list details
function createDropdownList(name) {
	var option = document.createElement("OPTION");
	option.setAttribute("value", name);
	option.appendChild(document.createTextNode(name));
	return option;
}

//function for bold text
function createBoldLabel(text) {
	var label = document.createElement("STRONG");
	label.appendChild(document.createTextNode(text));
    return label;
}

function addClass(name, length){
	var cell = document.createElement("TD");
	cell.className = name;
	cell.appendChild(document.createTextNode("\u00a0"));
	cell.colSpan = length;
	cell.setAttribute("width", "15");
	return cell;
}

function insertPadding(row, index, className, colSpan, width){
	var cell = row.insertCell(index);
	cell.className = className;
	cell.appendChild(document.createTextNode("\u00a0"));
	cell.colSpan = colSpan;
	cell.setAttribute("width", width);
	return cell;
}

//var locations_count;
var locationHeaderSize = 2;
var locationFooterSize = 2;
var locationHeaderAndFooterSize = locationHeaderSize + locationFooterSize;
var locationsCountName = "locations_count";
var locationsTable = "locations"
var addLocationsRowVar = function addLocationsRow(){
    var locations_count = incCount(locationsCountName);
    var table = document.getElementById(locationsTable);
    var row = table.insertRow(table.rows.length - locationFooterSize);
    row.setAttribute("align", "center");
	row.vAlign = "top";

    insertPadding(row, 0, "left", 1, 15);
    var locationnameCELL = row.insertCell(1);
	var locationaddressCELL = row.insertCell(2);
	var locationcontactCELL = row.insertCell(3);
	var locationcontactnoCELL = row.insertCell(4);
	var locationubdCELL = row.insertCell(5);
	var locationnotesCELL = row.insertCell(6);
    insertPadding(row, 7, "right", 1, 15);

    locationnameCELL.appendChild(createAnyTextTextBox("locations_name_" + locations_count, 15, "", "location name"));
    locationaddressCELL.appendChild(createAnyTextTextBox("locations_address_" + locations_count, 20, "", "location address"));
    locationcontactCELL.appendChild(createAnyTextTextBox("locations_contact_" + locations_count, 15, "", "location contact"));
    locationcontactnoCELL.appendChild(createTelNumberTextBox("locations_contact_no_" + locations_count, 10, "", "location contact number"));
    locationubdCELL.appendChild(createAnyTextTextBox("locations_UBD_" + locations_count, 8, "", "UBD ref location"));
    locationnotesCELL.appendChild(createTextArea("locations_notes_" + locations_count, 10, "", "location notes"));
}

function addLocationsRow() {
    addLocationsRowVar();
}

function deleteLocationsRow() {
    deleteRows(locationsTable,
               locationsCountName,
               locationHeaderSize,
               locationFooterSize,
               addLocationsRowVar);
}

function getCountByName(countName) {
    return document.getElementById(countName).value; 
}

function  incCount(countName) {
    var  countVar = getCountByName(countName);
    countVar ++;
    document.getElementById(countName).value = countVar;
    return countVar;
}

function decCount(countName) {
    var  countVar = getCountByName(countName);
    countVar--;
    document.getElementById(countName).value = countVar;
    return countVar;
}

var catering_count;
function addCateringRow(){
	catering_count = document.getElementById("catering_count").value;
	var cateringTABLE = document.getElementById("catering").getElementsByTagName("tbody")[0];
	var row = document.createElement("TR");
	var cateringmealCELL = document.createElement("TD");
	var cateringtimeCELL = document.createElement("TD");
	var cateringnumbersCELL = document.createElement("TD");
	var cateringlocationCELL = document.createElement("TD");
	var cateringmealDROPDOWN = document.createElement("SELECT");

	var previous_numbers = document.getElementById("catering_numbers_" + catering_count).value;
	var previous_location = document.getElementById("catering_location_" + catering_count).value;

	catering_count ++;
	document.getElementById("catering_count").value = catering_count;

	row.setAttribute("align", "center");
	row.setAttribute("valign", "top");
	row.setAttribute("height", "30");

	cateringmealDROPDOWN.setAttribute("name", "catering_meal_" + catering_count);
	cateringmealDROPDOWN.setAttribute("id", "catering_meal_" + catering_count);

	cateringmealDROPDOWN.appendChild(createDropdownList("Breakfast"));
	cateringmealDROPDOWN.appendChild(createDropdownList("Morning Tea"));
	cateringmealDROPDOWN.appendChild(createDropdownList("Lunch"));
	cateringmealDROPDOWN.appendChild(createDropdownList("Afternoon Tea"));
	cateringmealDROPDOWN.appendChild(createDropdownList("Dinner"));
	cateringmealDROPDOWN.appendChild(createDropdownList("Supper"));

	cateringmealCELL.appendChild(cateringmealDROPDOWN);
	cateringtimeCELL.appendChild(createInput("catering_time_" + catering_count, 8, "text", ""));
	cateringnumbersCELL.appendChild(createInput("catering_numbers_" + catering_count, 8, "text", previous_numbers));
	cateringlocationCELL.appendChild(createInput("catering_location_" + catering_count, 50, "text", previous_location));

	row.appendChild(addClass("left", "1"));
	row.appendChild(cateringmealCELL);
	row.appendChild(cateringtimeCELL);
	row.appendChild(cateringnumbersCELL);
	row.appendChild(cateringlocationCELL);
	row.appendChild(addClass("right", "1"));

	cateringTABLE.appendChild(row);
}

var callCountName = "call_count";
var callTable = "call_times";
var callHeaderSize = 7;
var callFooterSize = 3;
var callHeaderAndFooterSize = callHeaderSize + callFooterSize;

var addCallTimesRowVar = function addCallTimesRow() {
    var call_count = incCount(callCountName);
    var table = document.getElementById(callTable);
    var row = table.insertRow(table.rows.length - callFooterSize);
	row.setAttribute("height", "30");
    
    insertPadding(row, 0, "left", 1, 15);
    var callnameCELL = row.insertCell(1);
    var calltimeCELL = row.insertCell(2);
    calltimeCELL.setAttribute("align", "center");
    var calllocationCELL = row.insertCell(3);
    calllocationCELL.setAttribute("align", "center");
    insertPadding(row, 4, "right", 1, 15);

    callnameCELL.appendChild(createAnyTextTextBox("call_" + call_count, 25, "[other call]", "call name"));
	calltimeCELL.appendChild(createDateTextBox("call_time_" + call_count, 25, "", "call time"));
	calllocationCELL.appendChild(createAnyTextTextBox("call_location_" + call_count, 25, "", "call location"));
}

function addCallTimesRow() {
    addCallTimesRowVar();    
}

function deleteCallTimesRow() {
    deleteRows(callTable,
               callCountName,
               callHeaderSize,
               callFooterSize,
               addCallTimesRowVar);
}

var otherTimesCountName ="others_count";
var otherTimesTable = "other_times";
var otherTimesHeaderSize = 2;
var otherTimesFooterSize = 2;
var otherTimesHeaderAndFooterSize = otherTimesHeaderSize + otherTimesFooterSize;
                                         
var addWrapTimesRowVar = function addWrapTimesRow(){
    var wrap_count = incCount(otherTimesCountName);
    var table = document.getElementById(otherTimesTable);
    var row = table.insertRow(table.rows.length - otherTimesFooterSize); 
    row.setAttribute("height", "30");

    insertPadding(row, 0, "left", 1, 15);
    var wrapLABEL = row.insertCell(1);
    var wrapCELL = row.insertCell(2);
    insertPadding(row, 3, "right", 1, 15);
    
	wrapLABEL.appendChild(createAnyTextTextBox("wrap_" + wrap_count, 15, "[other wrap]", "enter wrap name"));
	wrapCELL.appendChild(createDateTextBox("wrap_time_" + wrap_count, 15, "", "enter wrap time"));
}

function addWrapTimesRow() {
    addWrapTimesRowVar();
}

function deleteWrapTimesRow() {
    deleteRows(otherTimesTable,
               otherTimesCountName,
               otherTimesHeaderSize,
               otherTimesFooterSize,
               addWrapTimesRowVar);
}

var ad_scene_count;
function addAdvancedSceneRow(){
	var advancedsceneTABLE = document.getElementById("advanced_scenes").getElementsByTagName("tbody")[0];
	var row = document.createElement("TR");
	var sceneCELL = document.createElement("TD");
	var dnCELL = document.createElement("TD");
	var inexCELL = document.createElement("TD");
	var pagesCELL = document.createElement("TD");
	var setsynopsisCELL = document.createElement("TD");
	var locationCELL = document.createElement("TD");
	var charactersCELL = document.createElement("TD");

	ad_scene_count = document.getElementById("ad_scene_count").value;
	ad_scene_count ++;
	document.getElementById("ad_scene_count").value = ad_scene_count;

	row.vAlign = "top";
	row.setAttribute("height", "30");

	sceneCELL.appendChild(createInput("ad_schedule_scene_" + ad_scene_count, 4, "text", ""));
	dnCELL.appendChild(createInput("ad_schedule_dn_" + ad_scene_count, 4, "text", ""));
	inexCELL.appendChild(createInput("ad_schedule_inex_" + ad_scene_count, 4, "text", ""));
	pagesCELL.appendChild(createInput("ad_schedule_pages_" + ad_scene_count, 4, "text", ""));
	pagesCELL.appendChild(document.createTextNode("\u00a0"));
	pagesCELL.appendChild(createInput("ad_schedule_pagesnum_" + ad_scene_count, 2, "text", ""));
	pagesCELL.appendChild(createBoldLabel(" /8pgs"));
	setsynopsisCELL.appendChild(createInput("ad_schedule_setsynopsis_" + ad_scene_count, 15, "text", ""));
	locationCELL.appendChild(createTextArea("ad_schedule_location_" + ad_scene_count, 10));
	charactersCELL.appendChild(createTextArea("ad_schedule_characters_" + ad_scene_count, 10));

	row.appendChild(sceneCELL);
	row.appendChild(dnCELL);
	row.appendChild(inexCELL);
	row.appendChild(pagesCELL);
	row.appendChild(setsynopsisCELL);
	row.appendChild(locationCELL);
	row.appendChild(charactersCELL);

	advancedsceneTABLE.appendChild(row);
}

var setReqDescriptionHeaderSize = 1;
var setReqDescriptionFooterSize = 0;
var setReqDescriptionHeaderAndFooterSize = setReqDescriptionHeaderSize + setReqDescriptionFooterSize;
var setReqDescriptionCountRoot = "description_count";
var setReqDescriptionTableRoot = "description";
var addDescriptionRowVar = function addDescriptionRow(table_num) {
    var description_count = incCount("description_count_"+table_num);
    var table = document.getElementById("description_"+table_num);
    var row = table.insertRow(table.rows.length - setReqDescriptionFooterSize);
    row.vAlign = "top";

    var sceneCELL = row.insertCell(0);
    var requirementsCELL = row.insertCell(1);

	sceneCELL.appendChild(createAnyTextTextBox("sr"+ table_num + "_scene_" + description_count, 20, "",
            "enter scene"));
	requirementsCELL.appendChild(createTextArea("sr"+ table_num + "_requirements_" + description_count, 40, "",
            "enter description"));
}

function addDescriptionRow(table_num) {
    addDescriptionRowVar(table_num);
}

function deleteDescriptionRow(table_num) {
    deleteRows(setReqDescriptionTableRoot + "_" + table_num,
               setReqDescriptionCountRoot + "_" + table_num,
               setReqDescriptionHeaderSize,
               setReqDescriptionFooterSize,
               addDescriptionRowVar,
               table_num);
}

var requirementsRowHeaderSize = 2;
var requirementsRowFooterSize = 2;
var requirementsTable = "set_requirements";
var requirementsCountName = "requirements_count";
var requirementsRowsToDelete = 2; 
function addRequirementsRow() {    
    var requirements_count = incCount("requirements_count");

    var table = document.getElementById(requirementsTable);
    var row1 = table.insertRow(table.rows.length - requirementsRowFooterSize);
    row1.vAlign = "top";
    var row2 = table.insertRow(table.rows.length - requirementsRowFooterSize);
    row2.vAlign = "top";

    setRequirementsRow1(row1, requirements_count);
    setRequirementsRow2(row2, requirements_count);
}

function deleteRequirementsRow() {
    deleteMultipleRows(requirementsTable, requirementsCountName, requirementsRowHeaderSize, requirementsRowFooterSize,
            function(){addRequirementsRow();}, null, requirementsRowsToDelete);
}

function setRequirementsRow2(row2, requirements_count) {
    insertPadding(row2, 0, "left", 1, 15);
    var blankCELL = row2.insertCell(1);
    var addButton = createButton("requirementsbutton", "Insert Description");
    addButton.onclick = function(){addDescriptionRow(requirements_count);};

    var deleteButton = createButton("deleteDescription", "Delete Description");
    deleteButton.onclick = function(){deleteDescriptionRow(requirements_count);};

    var insertDescriptionCELL = row2.insertCell(2);
    insertDescriptionCELL.appendChild(addButton);
    insertDescriptionCELL.appendChild(deleteButton);
    insertDescriptionCELL.appendChild(createHiddenField("description_count_"+ requirements_count, 1));
    insertPadding(row2, 3, "right", 1, 15);    
}

function setRequirementsRow1(row1, requirements_count) {
    insertPadding(row1, 0, "left", 1, 15);
    var itemCELL = row1.insertCell(1);
    var requirementstableCELL = row1.insertCell(2);
    itemCELL.appendChild(createBoldLabel("Item"));
    itemCELL.appendChild(document.createElement("BR"));
    itemCELL.appendChild(createAnyTextTextBox("sr"+ requirements_count + "_item", 20, "", "enter item"));
    requirementstableCELL.appendChild(addRequirementsSubTable(requirements_count));
    insertPadding(row1, 3, "right", 1, 15);    
}

function createButton(id, value) {
    var button = document.createElement("INPUT");
    button.setAttribute("name", id);
    button.setAttribute("id", id);
    button.setAttribute("type", "button");
    button.setAttribute("value", value);
    return button;
}

function addRequirementsSubTable(requirements_count){

    var table = document.createElement("TABLE");

    var sceneLABEL = document.createElement("TD");
	var requirementsLABEL = document.createElement("TD");
    var row3 = table.insertRow(table.rows.length);
    row3.vAlign = "top";    
    var row4 = table.insertRow(table.rows.length);
	row4.vAlign = "top";    


    var sceneCELL = row4.insertCell(0);
    var requirementsCELL = row4.insertCell(1);

	sceneLABEL.appendChild(createBoldLabel("Scene"));
	requirementsLABEL.appendChild(createBoldLabel("Requirements"));
    row3.appendChild(sceneLABEL);
    row3.appendChild(requirementsLABEL);
    
    sceneCELL.appendChild(createAnyTextTextBox("sr"+ requirements_count + "_scene_1", 20, "", "enter scene"));
	requirementsCELL.appendChild(createTextArea("sr"+ requirements_count + "_requirements_1", 40, "",
            "enter requirements"));

    table.setAttribute("width", "510");
	table.setAttribute("border", "0");
	table.setAttribute("cellspacing", "0");
	table.setAttribute("cellpadding", "0");
	table.setAttribute("id", "description_" + requirements_count);
    
    return table;
}

var scene_count;
function addSceneRow(scene_num) {
	var tbody = document.getElementById("scene").getElementsByTagName("tbody")[0];

	scene_count = document.getElementById("scene_count").value;
	scene_count ++;
	document.getElementById("scene_count").value = scene_count;

	//first row
	var row1 = document.createElement("TR");
	row1.appendChild(addClass("top","8"));

	//second row
	var row2 = document.createElement("TR");
	var sceneLABEL = document.createElement("TD");
	var pagetimeLABEL = document.createElement("TD");
	var dnLABEL = document.createElement("TD");
	var ieLABEL = document.createElement("TD");
	var setlocationLABEL = document.createElement("TD");
	var synopsisLABEL = document.createElement("TD");

	sceneLABEL.appendChild(createBoldLabel("Scene"));
	pagetimeLABEL.appendChild(createBoldLabel("Page Time"));
	dnLABEL.appendChild(createBoldLabel("D/N"));
	ieLABEL.appendChild(createBoldLabel("I/E"));
	setlocationLABEL.appendChild(createBoldLabel("Set/Location"));
	synopsisLABEL.appendChild(createBoldLabel("Synopsis"));

	row2.appendChild(addClass("left", "1"));
	row2.appendChild(sceneLABEL);
	row2.appendChild(pagetimeLABEL);
	row2.appendChild(dnLABEL);
	row2.appendChild(ieLABEL);
	row2.appendChild(setlocationLABEL);
	row2.appendChild(synopsisLABEL);
	row2.appendChild(addClass("right", "1"));

	//third row
	var row3 = document.createElement("TR");
	var sceneCELL = document.createElement("TD");
	var pageCELL = document.createElement("TD");
	var dnCELL = document.createElement("TD");
	var inexCELL = document.createElement("TD");
	var setlocationCELL = document.createElement("TD");
	var synopsisCELL = document.createElement("TD");

	sceneCELL.appendChild(createInput("ss" + scene_count +"_scene", 6, "text", ""));
	pageCELL.appendChild(createInput("ss" + scene_count +"_pages", 4, "text", ""));
	pageCELL.appendChild(document.createTextNode("\u00a0"));
	pageCELL.appendChild(createInput("ss" + scene_count +"_pagesnum",2, "text", ""));
	pageCELL.appendChild(createBoldLabel(" /8pgs"));
	dnCELL.appendChild(createInput("ss" + scene_count +"_dn", 6, "text", ""));
	inexCELL.appendChild(createInput("ss" + scene_count +"_inex", 6, "text", ""));
	setlocationCELL.appendChild(createInput("ss" + scene_count +"_setlocation", 15, "text", ""));
	synopsisCELL.appendChild(createInput("ss" + scene_count +"_synopsis", 15, "text", ""));

	row3.appendChild(addClass("left", "1"));
	row3.appendChild(sceneCELL);
	row3.appendChild(pageCELL);
	row3.appendChild(dnCELL);
	row3.appendChild(inexCELL);
	row3.appendChild(setlocationCELL);
	row3.appendChild(synopsisCELL);
	row3.appendChild(addClass("right", "1"));

	//fourth row
	var row4 = document.createElement("TR");
	var artisttableCELL = document.createElement("TD");

	artisttableCELL.colSpan = "6";

	artisttableCELL.appendChild(createArtistInfoTable(scene_count));
	row4.appendChild(addClass("left", "1"));
	row4.appendChild(artisttableCELL);
	row4.appendChild(addClass("right", "1"));

	//fourth row buttons
	var row4B = document.createElement("TR");
	var r4Bc2 = document.createElement("TD");

	r4Bc2.colSpan = "6";
	r4Bc2.setAttribute("align", "left");

	var button = document.createElement("INPUT");
	button.setAttribute("name", "artistbutton");
	button.setAttribute("id", "artistbutton");
	button.setAttribute("type", "button");
	button.setAttribute("value", "Add Artist Details");
	button.onclick = function(){addArtistDetailsRow(scene_count);};

	r4Bc2.appendChild(button);
	r4Bc2.appendChild(createInput("artist_count_" + scene_count,10,"hidden", 1));

	row4B.appendChild(addClass("left", "1"));
	row4B.appendChild(r4Bc2);
	row4B.appendChild(addClass("right", "1"));

	//fifth row
	var row5 = document.createElement("TR");
	var estshoottimesLABEL = document.createElement("TD");
	var estshootimesCELL = document.createElement("TD");

	estshoottimesLABEL.colSpan = "2";
	estshootimesCELL.colSpan = "4";
	estshootimesCELL.setAttribute("align", "left");

	estshoottimesLABEL.appendChild(createBoldLabel("Est Shoot Times"));
	estshootimesCELL.appendChild(createInput("ss" + scene_count +"_estshootingtime",20,"text",""));

	row5.appendChild(addClass("left", "1"));
	row5.appendChild(estshoottimesLABEL);
	row5.appendChild(estshootimesCELL);
	row5.appendChild(addClass("right", "1"));

	//sixth row
	var row6 = document.createElement("TR");
	var r6c2 = document.createElement("TD");
	var r6c3 = document.createElement("TD");
	var r6table = document.createElement("TABLE");

	r6c2.colSpan = "2";
	r6c3.colSpan = "4";

	r6table.setAttribute("width", "400");
	r6table.setAttribute("border", "0");
	r6table.setAttribute("cellspacing", "0");
	r6table.setAttribute("cellpadding", "0");
	r6table.setAttribute("id", "mealbreak_" + scene_count);

	r6table.appendChild(document.createElement("TBODY"));

	var button = document.createElement("INPUT");
	button.setAttribute("name", "ss"+ scene_count + "_mealbutton");
	button.setAttribute("id", "ss"+ scene_count + "_mealbutton");
	button.setAttribute("type", "button");
	button.setAttribute("value", "Add Meal Break");
	button.onclick = function(){addMealBreakRow(scene_count);};

	r6c2.appendChild(button);
	r6c3.appendChild(r6table);
	row6.appendChild(addClass("left", "1"));
	row6.appendChild(r6c2);
	row6.appendChild(r6c3);
	row6.appendChild(addClass("right", "1"));

	//seventh row
	var row7 = document.createElement("TR");
	row7.appendChild(addClass("bottom", "8"));

	//compile everything
	tbody.appendChild(row1);
	tbody.appendChild(row2);
	tbody.appendChild(row3);
	tbody.appendChild(row4);
	tbody.appendChild(row4B);
	tbody.appendChild(row5);
	tbody.appendChild(row6);
	tbody.appendChild(row7);
}

function createArtistInfoTable (){
	var artistTABLE = document.createElement("TABLE");
	var tbody = document.createElement("TBODY");
	var row1 = document.createElement("TR");
	var row2 = document.createElement("TR");
	var characterLABEL = document.createElement("TD");
	var artistLABEL = document.createElement("TD");
	var pickupLABEL = document.createElement("TD");
	var makeupLABEL = document.createElement("TD");
	var wardrobeLABEL = document.createElement("TD");
	var onsetLABEL = document.createElement("TD");
	var characterCELL = document.createElement("TD");
	var artistCELL = document.createElement("TD");
	var pickupCELL = document.createElement("TD");
	var makeupCELL = document.createElement("TD");
	var wardrobeCELL = document.createElement("TD");
	var onsetCELL = document.createElement("TD");

	artistTABLE.setAttribute("width", "640");
	artistTABLE.setAttribute("border", "0");
	artistTABLE.setAttribute("cellspacing", "0");
	artistTABLE.setAttribute("cellpadding", "0");
	artistTABLE.setAttribute("id", "artist_" + scene_count);

	characterLABEL.appendChild(createBoldLabel("Character"));
	artistLABEL.appendChild(createBoldLabel("Artist"));
	pickupLABEL.appendChild(createBoldLabel("Pickup"));
	makeupLABEL.appendChild(createBoldLabel("Makeup"));
	wardrobeLABEL.appendChild(createBoldLabel("Wardrobe"));
	onsetLABEL.appendChild(createBoldLabel("On Set"));

	characterCELL.appendChild(createInput("ss" + scene_count + "_character_1",15,"text", ""));
	artistCELL.appendChild(createInput("ss" + scene_count + "_artist_1",15,"text", ""));
	pickupCELL.appendChild(createInput("ss" + scene_count + "_pickup_1",6,"text", ""));
	makeupCELL.appendChild(createInput("ss" + scene_count + "_makeup_1",6,"text", ""));
	wardrobeCELL.appendChild(createInput("ss" + scene_count + "_wardrobe_1",6,"text", ""));
	onsetCELL.appendChild(createInput("ss" + scene_count + "_onset_1",6,"text", ""));

	row1.appendChild(characterLABEL);
	row1.appendChild(artistLABEL);
	row1.appendChild(pickupLABEL);
	row1.appendChild(makeupLABEL);
	row1.appendChild(wardrobeLABEL);
	row1.appendChild(onsetLABEL);

	row2.appendChild(characterCELL);
	row2.appendChild(artistCELL);
	row2.appendChild(pickupCELL);
	row2.appendChild(makeupCELL);
	row2.appendChild(wardrobeCELL);
	row2.appendChild(onsetCELL);

	tbody.appendChild(row1);
	tbody.appendChild(row2);

	artistTABLE.appendChild(tbody);

	return artistTABLE;
}

var artist_count;
function addArtistDetailsRow(scene_num) {
	var artistTABLE = document.getElementById("artist_"+scene_num).getElementsByTagName("tbody")[0];
	var row = document.createElement("TR");
	var characterCELL = document.createElement("TD");
	var artistCELL = document.createElement("TD");
	var pickupCELL = document.createElement("TD");
	var makeupCELL = document.createElement("TD");
	var wardrobeCELL = document.createElement("TD");
	var onsetCELL = document.createElement("TD");

	artist_count = document.getElementById("artist_count_" + scene_num).value;
	artist_count ++;
	document.getElementById("artist_count_" + scene_num).value = artist_count;

	characterCELL.appendChild(createInput("ss" + scene_num + "_character_" + artist_count, 15, "text", ""));
	artistCELL.appendChild(createInput("ss" + scene_num + "_artist_" + artist_count, 15, "text", ""));
	pickupCELL.appendChild(createInput("ss" + scene_num + "_pickup_" + artist_count, 6, "text", ""));
	makeupCELL.appendChild(createInput("ss" + scene_num + "_makeup_" + artist_count, 6, "text", ""));
	wardrobeCELL.appendChild(createInput("ss" + scene_num + "_wardrobe_" + artist_count, 6, "text", ""));
	onsetCELL.appendChild(createInput("ss" + scene_num + "_onset_" + artist_count, 6, "text", ""));

	row.appendChild(characterCELL);
	row.appendChild(artistCELL);
	row.appendChild(pickupCELL);
	row.appendChild(makeupCELL);
	row.appendChild(wardrobeCELL);
	row.appendChild(onsetCELL);

	artistTABLE.appendChild(row);
}

function addMealBreakRow(scene_num){
	var mealbreakTABLE = document.getElementById("mealbreak_"+scene_num).getElementsByTagName("tbody")[0];
	var row = document.createElement("TR");
	var mealLABEL = document.createElement("TD");
	var mealCELL = document.createElement("TD");
	var cell3 = document.createElement("TD");
	var timesLABEL = document.createElement("TD");
	var timesCELL = document.createElement("TD");

	mealLABEL.appendChild(createBoldLabel("Meal"));
	mealCELL.appendChild(createInput("ss" + scene_num + "_meal", 25, "text", ""));
	timesLABEL.appendChild(createBoldLabel("Times"));
	timesCELL.appendChild(createInput("ss" + scene_num + "_times", 25, "text", ""));

	row.appendChild(mealLABEL);
	row.appendChild(mealCELL);
	row.appendChild(cell3);
	row.appendChild(timesLABEL);
	row.appendChild(timesCELL);

	mealbreakTABLE.appendChild(row);
	document.getElementById("ss"+ scene_num +"_mealbutton").disabled = true;
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

function calculateMod() {
	var count = document.getElementById("scene_count").value;
	var temp_number =0;
	var temp_numerator = 0;

	for (var i = 1; i<=count; i++) {
		var number = parseInt(document.getElementById("ss"+i+"_pages").value);
		var numerator = parseInt(document.getElementById("ss"+i+"_pagesnum").value);
		temp_number += number;
		temp_numerator += numerator;
	}

	var excess = temp_number + Math.floor(temp_numerator/8);
	var mod = temp_numerator % 8;

	//alert(excess + " mod " + mod);

	document.getElementById("total_script_pages").value = excess;
	document.getElementById("total_script_pagesnum").value = mod;
}