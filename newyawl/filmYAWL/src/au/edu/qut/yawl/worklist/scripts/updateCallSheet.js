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
    cell.colSpan = (colSpan == null) ? 1 : colSpan;
	cell.setAttribute("width", (width == null) ?  15 : width);
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
    locationnotesCELL.appendChild(createTextArea("locations_notes_" + locations_count, 10,0, "", "location notes"));
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
    countVar++;
    document.getElementById(countName).value = countVar;
    return countVar;
}

function decCount(countName) {
    var  countVar = getCountByName(countName);
    countVar--;
    document.getElementById(countName).value = countVar;
    return countVar;
}

var cateringTable = "catering";
var cateringCountName = "catering_count";
var cateringHeaderSize = 2;
var cateringFooterSize = 2;
function addCateringRow(){
    var catering_count = getCountByName(cateringCountName);
	var table = document.getElementById(cateringTable);
	var row = table.insertRow(table.rows.length - cateringFooterSize);
    row.setAttribute("align", "center");
    row.setAttribute("valign", "top");
    row.setAttribute("height", "30");

    insertPadding(row, 0, "left");
    var cateringmealCELL = row.insertCell(1);
	var cateringtimeCELL = row.insertCell(2);
	var cateringnumbersCELL = row.insertCell(3);
	var cateringlocationCELL = row.insertCell(4);    
    insertPadding(row, 5, "right");
    var cateringmealDROPDOWN = document.createElement("SELECT");

    var previous_numbers = "";
    var previous_location = "";

    if (catering_count > 1) {
        previous_numbers = document.getElementById("catering_numbers_" + catering_count).value;
        previous_location = document.getElementById("catering_location_" + catering_count).value;
    } 

    catering_count = incCount(cateringCountName);

    cateringmealCELL.appendChild(createCateringMealDropDown(cateringmealDROPDOWN, catering_count));
    cateringtimeCELL.appendChild(createDateTextBox("catering_time_" + catering_count, 8, "", "enter catering time"));
	cateringnumbersCELL.appendChild(createNumberTextBox("catering_numbers_" + catering_count, 8, previous_numbers, "enter catering numbers"));
	cateringlocationCELL.appendChild(createAnyTextTextBox("catering_location_" + catering_count, 50, previous_location, "enter catering location"));
    
}

function deleteCateringRow() {
    deleteRows(cateringTable, cateringCountName, cateringHeaderSize, cateringFooterSize, function(){addCateringRow();});
}

function createCateringMealDropDown(cateringmealDROPDOWN, catering_count) {
    cateringmealDROPDOWN.setAttribute("name", "catering_meal_" + catering_count);
    cateringmealDROPDOWN.setAttribute("id", "catering_meal_" + catering_count);
    cateringmealDROPDOWN.appendChild(createDropdownList("Breakfast"));
    cateringmealDROPDOWN.appendChild(createDropdownList("Morning Tea"));
    cateringmealDROPDOWN.appendChild(createDropdownList("Lunch"));
    cateringmealDROPDOWN.appendChild(createDropdownList("Afternoon Tea"));
    cateringmealDROPDOWN.appendChild(createDropdownList("Dinner"));
    cateringmealDROPDOWN.appendChild(createDropdownList("Supper"));
    return cateringmealDROPDOWN;
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
	locationCELL.appendChild(createTextArea("ad_schedule_location_" + ad_scene_count, 10,0));
	charactersCELL.appendChild(createTextArea("ad_schedule_characters_" + ad_scene_count, 10,0));

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
	requirementsCELL.appendChild(createTextArea("sr"+ table_num + "_requirements_" + description_count, 40,0, "","enter description"));
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
	requirementsCELL.appendChild(createTextArea("sr"+ requirements_count + "_requirements_1", 40, 0, "","enter requirements"));

    table.setAttribute("width", "510");
	table.setAttribute("border", "0");
	table.setAttribute("cellspacing", "0");
	table.setAttribute("cellpadding", "0");
	table.setAttribute("id", "description_" + requirements_count);
    
    return table;
}

var dynamicSceneCountName = "dyn_scene_count";
function getNextSceneCount() {
    var count = getCountByName(dynamicSceneCountName);
    return ++count;
}

/**
 * Updates alls ids of inputs and tables which contain the token with the count supplied.
 * eg. <input id="something_$_else"> will resolve to <input id="something_10_else"> if the function is called with
  * updateWithCount(parent, 10, "$", buttonHandler)
 * @param parent
 * @param count
 * @param token
 * @param buttonHandler
 */
function updateWithCount(parent, count, token, buttonHandler) {
        var elements = parent.getElementsByTagName("input");
        var templateId = null;
        var updatedId = null;
        var x = 0;
        if (elements != null) {
            for (; x < elements.length; x++) {
                if (elements[x].id.indexOf(token) != -1) {
                    updatedId = elements[x].id.replace(token, count);
                    elements[x].id = updatedId;
                    elements[x].name = updatedId;

                    if (buttonHandler != null) {
                        //the button handles may need dynamic values such as the count..check for that in the passed
                        //function, buttonHandler.
                        buttonHandler(elements[x], count);
                    }
                }
            }
        }

        elements = parent.getElementsByTagName("table");
        if (elements != null) {
            templateId = null;
            updatedId = null;
            for (x = 0; x < elements.length; x++) {
                if (elements[x].id.indexOf(token) != -1) {
                    updatedId = elements[x].id.replace(token, count);
                    elements[x].id = updatedId;         
                }
           }
        }
}

/**
 * This function wraps the action handlers for the button clicks on the form.
 * TODO: I was trying to dynamically update the function returned such that we can inject the count dynamically.
 * I didn't have time do to this so below is the work around. If you come across some code to dynamically update
 * a called-function then that would be a better solution.
 * 
 */
var buttonHandlersForScene = function handleDynamicButtonClicks(element, count) {
    if (element.type == 'button') {
        //check method calls.
        var functionVar = element.onclick;
        //TODO: this is a workaround. If you can dynamically insert the count parameter into the
        //function call do so.
        if (functionVar.toString().indexOf("addArtistDetailsRow") != -1) {
            element.onclick = function() {addArtistDetailsRow(count);};
         } else if (functionVar.toString().indexOf("deleteArtistDetailsRow") != -1) {
            element.onclick = function() {deleteArtistDetailsRow(count);};
        } else if (functionVar.toString().indexOf("addMealBreakRow") != -1) {
            element.onclick = function() {addDynamicMealBreakRow(count);};
        }
    }
}

var sceneTable = "scene";
function deleteSceneRow() {
    var table = document.getElementById(sceneTable);
    var tableBodies = table.getElementsByTagName("tbody");
    var scene_num = getCountByName(dynamicSceneCountName);

    if (scene_num > 0) {
        for (var x = 0; x < tableBodies.length; x++) {
            //find the last tbody with the id that matches the scene number.
            if (tableBodies[x].id == scene_num) {
                //remove that tbody and all its children and then reduce the count.
                table.removeChild(tableBodies[x]);
                decCount(dynamicSceneCountName);

                if (scenesAreEmpty()) {
                    //add a new scene if all the scenes have been deleted.
                    addSceneRow();    
                }

                return;    //return because we have done what we came here for, no need to keep searching.
            }
        }
    }
}

function scenesAreEmpty() {
    return getCountByName(dynamicSceneCountName) == 0;
}

function addSceneRow() {
    var table = document.getElementById(sceneTable);
    //sample table body. check the jsp for a tbody with id of "sample
    var tableBody = table.getElementsByTagName("tbody")[0];

    //make a copy of the sample table.
    var tableBodyClone = tableBody.cloneNode(true);
    var scene_num = getNextSceneCount();

    //change its style so it is visible. The sample is "hidden" by default.
    tableBodyClone.className = "valid";

    //update the copies id with a counter. This makes it easy to delete later on.
    tableBodyClone.id = scene_num;
    table.appendChild(tableBodyClone);

    //replace any tokens with the appropriate counters.
    updateWithCount(tableBodyClone, scene_num, "$", buttonHandlersForScene);

    //inc scene count.
    incCount(dynamicSceneCountName);    
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

var artistDetailHeaderSize = 1;
var artistDetailFooterSize = 0;
var artistDetailTableRoot = "artist";
var artistDetailCountNameRoot = "artist_count" 
function addArtistDetailsRow(scene_num) {
    var artist_count = incCount(artistDetailCountNameRoot + "_" + scene_num)
    var table = document.getElementById(artistDetailTableRoot + "_"+scene_num);
    var row = table.insertRow(table.rows.length - artistDetailFooterSize);

    var characterCELL = row.insertCell(0);
    var artistCELL = row.insertCell(1);
    var pickupCELL = row.insertCell(2);
    var makeupCELL = row.insertCell(3);
    var wardrobeCELL = row.insertCell(4);
    var onsetCELL = row.insertCell(5);

	characterCELL.appendChild(createAnyTextTextBox("ss" + scene_num + "_character_" + artist_count, 15, "",
            "enter character"));
	artistCELL.appendChild(createAnyTextTextBox("ss" + scene_num + "_artist_" + artist_count, 15, "",
            "enter artist"));
	pickupCELL.appendChild(createAnyTextTextBox("ss" + scene_num + "_pickup_" + artist_count, 6, "",
            "enter pickup"));
	makeupCELL.appendChild(createAnyTextTextBox("ss" + scene_num + "_makeup_" + artist_count, 6, "",
            "enter makeup"));
	wardrobeCELL.appendChild(createAnyTextTextBox("ss" + scene_num + "_wardrobe_" + artist_count, 6, "",
            "enter wardrobe"));
	onsetCELL.appendChild(createAnyTextTextBox("ss" + scene_num + "_onset_" + artist_count, 6, "",
            "enter onset"));
}

function deleteArtistDetailsRow(scene_num) {
    deleteRows(artistDetailTableRoot + "_"+scene_num,
               artistDetailCountNameRoot + "_" + scene_num,
               artistDetailHeaderSize,
               artistDetailFooterSize,
               function() {addArtistDetailsRow(scene_num)},
               scene_num);
}

function addDynamicMealBreakRow(scene_num){
    document.getElementById("mealbreak_"+scene_num).className = "valid";
    document.getElementById("ss"+scene_num+"_meal").className = "valid";
    document.getElementById("ss"+scene_num+"_times").className = "valid";
    document.getElementById("ss"+ scene_num +"_mealbutton").disabled = true;
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