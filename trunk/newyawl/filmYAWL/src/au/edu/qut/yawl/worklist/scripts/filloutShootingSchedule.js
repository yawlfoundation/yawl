var count = 1;
var table_count = 1;
var temp_day_count = 1;
var temp_table_count = 1;
var table_num = 0;
var temp_character_count = 1;
var temp_requirements_count = 1;
var days_count = 1;

var headerSize = 0;
var footerSize = 0;
var headerAndFooterSize = headerSize + footerSize;

var charheaderSize = 1;
var charfooterSize = 0;
var charheaderAndFooterSize = charheaderSize + charfooterSize;

function deleteRequirementsRow(day, table) {
	var temp_requirements_count = document.getElementById("sd"+day+"_requirementscount"+table).value;
    var requirementsTABLE = document.getElementById("sd"+day+"_requirements"+table);
    var rows = requirementsTABLE.rows.length;
    if (rows > headerAndFooterSize) {
        //delete from the bottom, removing 1 for the 0-based index.
        requirementsTABLE.deleteRow(rows-(footerSize+1));
        if (temp_requirements_count > 0) {
            document.getElementById("sd"+day+"_requirementscount"+table).value = -- temp_requirements_count;
        }

        if (temp_requirements_count == 0) {
            //add an empty row after the last row has been deleted.
            //this allows all data-containing rows to be deleted and for there to be a single empty row
            //at any point in time.
            addRequirementsRow(day, table);
        }
    }
}

function addRequirementsRow(day, table) { //works
	var requirementsTABLE = document.getElementById("sd"+day+"_requirements"+table);
	var row = requirementsTABLE.insertRow(requirementsTABLE.rows.length-footerSize);
	var itemCELL = row.insertCell(0);
	var requirementsCELL = row.insertCell(1);
	//get the number of REQUIREMENTS entries
	temp_requirements_count = document.getElementById("sd"+day+"_requirementscount"+table).value;
	temp_requirements_count ++;
	document.getElementById("sd"+day+"_requirementscount"+table).value = temp_requirements_count;
	//set the row to be aligned at the top
	row.setAttribute("valign", "top");
	//the ITEM drop-down box information
	var requirementsDROPDOWN =  document.createElement("SELECT");
	requirementsDROPDOWN.setAttribute("name", "sd"+day+"_items"+table+"_"+temp_requirements_count);
	requirementsDROPDOWN.setAttribute("id", "sd"+day+"_items"+table+"_"+temp_requirements_count);
	requirementsDROPDOWN.title = "Select an Item";
	
	requirementsDROPDOWN.appendChild(createDropdownList("Background Actors"));
	requirementsDROPDOWN.appendChild(createDropdownList("Stunts"));
	requirementsDROPDOWN.appendChild(createDropdownList("Vehicles"));
	requirementsDROPDOWN.appendChild(createDropdownList("Props"));
	requirementsDROPDOWN.appendChild(createDropdownList("Camera"));
	requirementsDROPDOWN.appendChild(createDropdownList("Special Effects"));
	requirementsDROPDOWN.appendChild(createDropdownList("Wardrobe"));
	requirementsDROPDOWN.appendChild(createDropdownList("Makeup/Hair"));
	requirementsDROPDOWN.appendChild(createDropdownList("Animals"));
	requirementsDROPDOWN.appendChild(createDropdownList("Animal Wrangler"));
	requirementsDROPDOWN.appendChild(createDropdownList("Music"));
	requirementsDROPDOWN.appendChild(createDropdownList("Sound"));
	requirementsDROPDOWN.appendChild(createDropdownList("Art Department"));
	requirementsDROPDOWN.appendChild(createDropdownList("Set Dressing"));
	requirementsDROPDOWN.appendChild(createDropdownList("Greenery"));
	requirementsDROPDOWN.appendChild(createDropdownList("Special Equipment"));
	requirementsDROPDOWN.appendChild(createDropdownList("Security"));
	requirementsDROPDOWN.appendChild(createDropdownList("Additional Labour"));
	requirementsDROPDOWN.appendChild(createDropdownList("Visual Effects"));
	requirementsDROPDOWN.appendChild(createDropdownList("Mechanical Effects"));
	requirementsDROPDOWN.appendChild(createDropdownList("Miscellaneous"));
	requirementsDROPDOWN.appendChild(createDropdownList("Notes"));
	//append the new row to the REQUIREMENTS table
	itemCELL.appendChild(requirementsDROPDOWN);
	requirementsCELL.appendChild(createTextArea("sd"+day+"_requirements"+table+"_"+temp_requirements_count, 40, 0, "", "Enter Set Requirements. [String Value]"));
}

function deleteCharacterRow(day, table) {
	var temp_character_count = document.getElementById("sd"+day+"_charactercount"+table).value;
    var characterTABLE = document.getElementById("sd"+day+"_characters"+table);
    var rows = characterTABLE.rows.length;
    if (rows > charheaderAndFooterSize) {
        //delete from the bottom, removing 1 for the 0-based index.
        characterTABLE.deleteRow(rows-(charfooterSize+1));
        if (temp_character_count > 0) {
            document.getElementById("sd"+day+"_charactercount"+table).value = -- temp_character_count;
        }

        if (temp_character_count == 0) {
            //add an empty row after the last row has been deleted.
            //this allows all data-containing rows to be deleted and for there to be a single empty row
            //at any point in time.
            addCharacterRow(day, table);
        }
    }
}
function addCharacterRow(day, table) {//works
	var characterTABLE= document.getElementById("sd"+day+"_characters"+table);
	var row = characterTABLE.insertRow(characterTABLE.rows.length-charfooterSize);
	var cell = row.insertCell(0);
	//get the number of CHARACTERS entries
	temp_character_count = document.getElementById("sd"+day+"_charactercount"+table).value;
	temp_character_count ++;
	document.getElementById("sd"+day+"_charactercount"+table).value = temp_character_count;
	//append the new row to the CHARACTERS table
	cell.appendChild(createAnyTextTextBox("sd"+day+"_charactername"+table+"_"+temp_character_count, 20, "","Enter Character Name. [String Value] (make sure it matches a character from cast list)"));
}

function addMealBreak(day, table) {//works
	var mealTABLE = document.getElementById("sd"+day+"_mealbreak"+table);
	var row = mealTABLE.insertRow(mealTABLE.rows.length-footerSize);
	var mealCELL = row.insertCell(0);
	var mealtimesLABEL = row.insertCell(1);
	var mealtimesCELL = row.insertCell(2);
	var mealDROPDOWN =  document.createElement("SELECT");
	
	mealDROPDOWN.setAttribute("name", "sd"+day+"_meal"+table);
	mealDROPDOWN.setAttribute("id", "sd"+day+"_meal"+table);
	mealDROPDOWN.title = "Select a Meal";
	mealDROPDOWN.appendChild(createDropdownList("Breakfast"));
	mealDROPDOWN.appendChild(createDropdownList("Morning Tea"));
	mealDROPDOWN.appendChild(createDropdownList("Lunch"));
	mealDROPDOWN.appendChild(createDropdownList("Afternoon Tea"));
	mealDROPDOWN.appendChild(createDropdownList("Dinner"));
	mealDROPDOWN.appendChild(createDropdownList("Supper"));
	
	mealCELL.appendChild(mealDROPDOWN);
	mealtimesLABEL.appendChild(createBoldLabel("Times"));
	mealtimesCELL.appendChild(createAnyTextTextBox("sd"+day+"_break"+table, 25, "", "Enter Meal Times. [String Value]"));
	
	document.getElementById("sd"+day+"_mealbreakbutton"+table).disabled = true;
}

function calculateMod(day) {
	var day_num = document.getElementById("day_number_" + day).value;
	var table_count = document.getElementById("table_count_" + day).value;
	var temp_number =0;
	var temp_numerator = 0;
	
	for (var i = 1; i<=table_count; i++) {
		var number = parseInt(document.getElementById("sd"+day_num+"_pages"+i).value);
		var numerator = parseInt(document.getElementById("sd"+day_num+"_pagesnum"+i).value);
		temp_number += number;
		temp_numerator += numerator;
	}
	
	var excess = temp_number + Math.floor(temp_numerator/8);
	var mod = temp_numerator % 8;
	
	//alert(excess + " mod " + mod);
	
	document.getElementById("sd" + day_num + "_totalpages").value = excess;
	document.getElementById("sd" + day_num + "_totalpagesnum").value = mod;
}

//function for bold text
function createBoldLabel(text) {
	var label = document.createElement("STRONG");
	label.appendChild(document.createTextNode(text));
	return label;
}

function deleteShootingDay() {
	var temp_day_count = document.getElementById("days").value;
    var daysTABLE = document.getElementById("shooting_days");
    var rows = daysTABLE.rows.length;
    if (rows > headerAndFooterSize) {
        //delete from the bottom, removing 1 for the 0-based index.
        daysTABLE.deleteRow(rows-(footerSize+1));
        if (temp_day_count > 0) {
            document.getElementById("days").value = -- temp_day_count;
        }

        if (temp_day_count == 0) {
            //add an empty row after the last row has been deleted.
            //this allows all data-containing rows to be deleted and for there to be a single empty row
            //at any point in time.
            addShootingDay();
        }
    }
}

function addShootingDay () {
	//update the day count
	days_count = document.getElementById("days").value;
	days_count ++;
	document.getElementById("days").value = days_count;
	//declare the elements
	var tbody2 = document.getElementById("shooting_days");
	var shootingdayROW = tbody2.insertRow(tbody2.rows.length);
	var shootingdayCELL = shootingdayROW.insertCell(0);
	var shootingdayTABLE = document.createElement("TABLE");

	//the DAY table information
	shootingdayTABLE.setAttribute("width", "700");
	shootingdayTABLE.setAttribute("border", "0");
	shootingdayTABLE.setAttribute("cellspacing", "0");
	shootingdayTABLE.setAttribute("cellpadding", "0");
	shootingdayTABLE.setAttribute("id", "day" + days_count);
	
	//the graphical HEADER row information
	var topROW = shootingdayTABLE.insertRow(shootingdayTABLE.rows.length);
	var topleftCELL = topROW.insertCell(0);
	var topmiddleCELL = topROW.insertCell(1);
	var toprightCELL = topROW.insertCell(2);
	topleftCELL.className = "header-left";
	topmiddleCELL.className = "header-middle";
	toprightCELL.className = "header-right";
	topleftCELL.setAttribute("width", "15");
	toprightCELL.setAttribute("width", "15");
	topmiddleCELL.colSpan = "4";
	topmiddleCELL.appendChild(createBoldLabel("Shoot Day # "));
	topmiddleCELL.appendChild(createNumberTextBox("sd"+days_count+"_number", 5, "", "Enter Shoot Day Number. [Number Value]"));
	
	//the general DAY DETAILS table information
	var headerinfo_row = shootingdayTABLE.insertRow(shootingdayTABLE.rows.length);
	var headerinfo_left = headerinfo_row.insertCell(0);
	var headerinfo_cell = headerinfo_row.insertCell(1);
	var headerinfo_right = headerinfo_row.insertCell(2);
	headerinfo_left.className = "left";
    headerinfo_left.appendChild(document.createTextNode("\u00a0"));
	headerinfo_right.className = "right";
    headerinfo_right.appendChild(document.createTextNode("\u00a0"));
	var generaldaydetailsTABLE = document.createElement("TABLE");
	generaldaydetailsTABLE.setAttribute("width", "670");
	generaldaydetailsTABLE.setAttribute("border", "0");
	generaldaydetailsTABLE.setAttribute("cellspacing", "0");
	generaldaydetailsTABLE.setAttribute("cellpadding", "0");
	//row 1 - hidden counts
	var daydetailROW1 = generaldaydetailsTABLE.insertRow(generaldaydetailsTABLE.rows.length);
	var hidden_cell = daydetailROW1.insertCell(0);
	hidden_cell.appendChild(createHiddenField("day_number_" + days_count, days_count));
	hidden_cell.appendChild(createHiddenField("table_count_" + days_count, 1));
	//row 2 - shoot date and shoot weekday
	var daydetailROW2 = generaldaydetailsTABLE.insertRow(generaldaydetailsTABLE.rows.length);
	var shootdateLABEL = daydetailROW2.insertCell(0);
	var shootdateCELL = daydetailROW2.insertCell(1);
	var weekdayLABEL = daydetailROW2.insertCell(2);
	var weekdayCELL = daydetailROW2.insertCell(3);
	shootdateLABEL.appendChild(createBoldLabel("Shoot Day Date "));
	shootdateLABEL.setAttribute("width", "150");
	shootdateCELL.appendChild(createRealDateTextBox("sd"+days_count+"_date", 20, "", "Enter Shoot Day Date. [Date Value DD-MM-YYYY]"));
	shootdateCELL.setAttribute("width", "185");
	weekdayLABEL.appendChild(createBoldLabel("Shoot Day Weekday "));
	weekdayLABEL.setAttribute("width", "150");
	weekdayCELL.appendChild(createAnyTextTextBox("sd"+days_count+"_weekday", 20, "", "Enter Weekday. [String Value]"));
	weekdayCELL.setAttribute("width", "185");
	//row 3 - weekday and date
	var daydetailROW3 = generaldaydetailsTABLE.insertRow(generaldaydetailsTABLE.rows.length);
	var crewcallLABEL = daydetailROW3.insertCell(0);
	var crewcallCELL = daydetailROW3.insertCell(1);
	var traveltolocLABEL = daydetailROW3.insertCell(2);
	var traveltolocCELL = daydetailROW3.insertCell(3);
	crewcallLABEL.appendChild(createBoldLabel("Crew Call "));
	crewcallLABEL.setAttribute("width", "150");
	crewcallCELL.appendChild(createDateTextBox("sd"+days_count+"_crew", 20, "", "Enter Crew Call. [Time Value HH:MM:SS]"));
	crewcallCELL.setAttribute("width", "185");
	traveltolocLABEL.appendChild(createBoldLabel("Travel to Loc "));
	traveltolocLABEL.setAttribute("width", "150");
	traveltolocCELL.appendChild(createDateTextBox("sd"+days_count+"_traveltoloc", 20, "", "Enter Travel to Location. [Time Value HH:MM:SS]"));
	traveltolocCELL.setAttribute("width", "185");
	//row 4 - bump in
	var daydetailROW4 = generaldaydetailsTABLE.insertRow(generaldaydetailsTABLE.rows.length);
	var bumpinLABEL = daydetailROW4.insertCell(0);
	var bumpinCELL = daydetailROW4.insertCell(1);	
	bumpinLABEL.appendChild(createBoldLabel("Bump In "));
	bumpinLABEL.setAttribute("width", "150");
	bumpinCELL.appendChild(createTextBoxWithNoValidation("sd"+days_count+"_bumpin", 20, "", "Enter Bump-In - If Necessary. [Time Value HH:MM:SS]"));
	bumpinCELL.setAttribute("width", "185");
	bumpinCELL.colSpan = "3";
	//row 5 - start of day notes
	var daydetailROW5 = generaldaydetailsTABLE.insertRow(generaldaydetailsTABLE.rows.length);
	var startLABEL = daydetailROW5.insertCell(0);
	var startCELL = daydetailROW5.insertCell(1);
	startLABEL.appendChild(createBoldLabel("Start of Day Notes "));
	startLABEL.setAttribute("width", "150");
	startCELL.appendChild(createTextArea("sd"+days_count+"_start", 60,0, "", "Enter Start Day Notes - If Necessary. [String Value]"));
	startCELL.colSpan = "3";
	
	headerinfo_cell.appendChild(generaldaydetailsTABLE);
	headerinfo_cell.colSpan = "4";	
	//the SCENE table information
	var sceneTABLE = document.createElement("TABLE");
	var entire_row = shootingdayTABLE.insertRow(shootingdayTABLE.rows.length);
	var cell_left = entire_row.insertCell(0);
	var cell_middle = entire_row.insertCell(1);
	var cell_right = entire_row.insertCell(2);
	cell_left.className = "left";
    cell_left.appendChild(document.createTextNode("\u00a0"));
	cell_right.className = "right";
    cell_right.appendChild(document.createTextNode("\u00a0"));
	sceneTABLE.setAttribute("width", "670");
	sceneTABLE.setAttribute("border", "0");
	sceneTABLE.setAttribute("cellspacing", "0");
	sceneTABLE.setAttribute("cellpadding", "0");
	sceneTABLE.setAttribute("id", "scenes" + days_count);
	sceneTABLE.appendChild(createSceneTable(days_count));
	cell_middle.appendChild(sceneTABLE);
	cell_middle.colSpan = "4";
	
	//the ADD SCENE button information
	var button_row = shootingdayTABLE.insertRow(shootingdayTABLE.rows.length);
	var button_left = button_row.insertCell(0);
	var button_cell = button_row.insertCell(1);
	var button_right = button_row.insertCell(2);
	var addBUTTON = document.createElement("INPUT");
	var deleteBUTTON = document.createElement("INPUT");
	button_left.className = "left";
    button_left.appendChild(document.createTextNode("\u00a0"));
	button_right.className = "right";
    button_right.appendChild(document.createTextNode("\u00a0"));
	addBUTTON.setAttribute("type", "button");
	addBUTTON.setAttribute("value", "Add Scene");
	addBUTTON.setAttribute("onClick", "addScene('scenes"+days_count+"', '"+days_count+"');");
	deleteBUTTON.setAttribute("type", "button");
	deleteBUTTON.setAttribute("value", "Delete Scene");
	deleteBUTTON.setAttribute("onClick", "deleteScene('scenes"+days_count+"', '"+days_count+"');");
	button_cell.appendChild(addBUTTON);
	button_cell.appendChild(deleteBUTTON);
	button_cell.colSpan = "4";
	
	//the END OF DAY NOTES textbox information
	var endday_row = shootingdayTABLE.insertRow(shootingdayTABLE.rows.length);
	var endday_left = endday_row.insertCell(0);
	var endday_cell1 = endday_row.insertCell(1);
	var endday_cell2 = endday_row.insertCell(2);
	var endday_right = endday_row.insertCell(3);
	endday_left.className = "left";
    endday_left.appendChild(document.createTextNode("\u00a0"));
	endday_right.className = "right";
    endday_right.appendChild(document.createTextNode("\u00a0"));
	endday_cell1.setAttribute("width", "150");
	endday_cell2.colSpan = "3";

	endday_cell1.appendChild(createBoldLabel("End of Day Notes "));
	endday_cell2.appendChild(createTextArea("sd"+days_count+"_end", 60,0, "", "Enter End of Day Notes. [String Value]"));
	
	// Total Script Pages
	var totalday_row = shootingdayTABLE.insertRow(shootingdayTABLE.rows.length);
	var totalday_left = totalday_row.insertCell(0);
	var totalscriptpagesLABEL = totalday_row.insertCell(1);
	var totalscriptpagesCELL = totalday_row.insertCell(2);
	var calculatebuttonCELL = totalday_row.insertCell(3);
	var totalday_right = totalday_row.insertCell(4);
	var calculate = document.createElement("INPUT");
	
	totalday_left.className = "left";
    totalday_left.appendChild(document.createTextNode("\u00a0"));
	totalday_right.className = "right";
    totalday_right.appendChild(document.createTextNode("\u00a0"));
	
	calculate.setAttribute("type", "button");
	calculate.setAttribute("value", "Calculate");
	calculate.setAttribute("onClick", "calculateMod("+days_count+");");
	
	calculatebuttonCELL.colSpan = "2";
	totalscriptpagesLABEL.appendChild(createBoldLabel("Total Script Pages"));
	totalscriptpagesCELL.appendChild(createNumberTextBox("sd"+days_count+"_totalpages", 4, "", "Calculate Script Pages. [Number Value]"));
	totalscriptpagesCELL.appendChild(document.createTextNode("\u00a0"));
	totalscriptpagesCELL.appendChild(createNumberTextBox("sd"+days_count+"_totalpagesnum", 2, "", "Calculate Script Pages. [Number Value]"));
	totalscriptpagesCELL.appendChild(createBoldLabel(" /8 pgs"));
	calculatebuttonCELL.appendChild(calculate);
	
	//footer details
	var footer_row = shootingdayTABLE.insertRow(shootingdayTABLE.rows.length);
	var footer_cell = footer_row.insertCell(0);
	footer_cell.className = "bottom";
	footer_cell.colSpan = "6";
	footer_cell.setAttribute("height", "10");
	//append the entire DAY entry
	shootingdayCELL.appendChild(shootingdayTABLE);
}

function updateTableCount(number) {
	temp_day_count = number;
	if (document.getElementById("table_count_"+temp_day_count) == null) {
		temp_table_count =1;
	} else { 
		temp_table_count = document.getElementById("table_count_"+temp_day_count).value;
		temp_table_count ++;
		document.getElementById("table_count_"+temp_day_count).value = temp_table_count;
	}
}

function deleteScene(table_name, table_number) {
	var temp_scene_count = document.getElementById("table_count_"+table_number).value;
    var scenesTABLE = document.getElementById(table_name);
    var rows = scenesTABLE.rows.length;
    if (rows > headerAndFooterSize) {
        //delete from the bottom, removing 1 for the 0-based index.
        scenesTABLE.deleteRow(rows-(footerSize+1));
        if (temp_scene_count > 0) {
            document.getElementById("table_count_"+table_number).value = -- temp_scene_count;
        }

        if (temp_scene_count == 0) {
            //add an empty row after the last row has been deleted.
            //this allows all data-containing rows to be deleted and for there to be a single empty row
            //at any point in time.
            addScene(table_name, table_number);
        }
    }
}

function addScene(table_name, table_number){
	var tbody1 = document.getElementById(table_name);
	var row = tbody1.insertRow(tbody1.rows.length);
	var cell_middle = row.insertCell(0);
	cell_middle.colSpan = "6";
	//append the new SCENE to the DAY table
	cell_middle.appendChild(createSceneTable(table_number));
}

function createSceneTable (table_number) {//done
	updateTableCount(table_number);
	var sceneTABLE = document.createElement("TABLE");
	sceneTABLE.setAttribute("width", "670");
	sceneTABLE.setAttribute("border", "0");
	sceneTABLE.setAttribute("cellspacing", "0");
	sceneTABLE.setAttribute("cellpadding", "0");
	//header row
	var header_row = sceneTABLE.insertRow(sceneTABLE.rows.length);
	var header_cell = header_row.insertCell(0);
	header_cell.className = "top";
	header_cell.colSpan = "6";
	header_cell.setAttribute("height", "10");
	//row 1
	var row1 = sceneTABLE.insertRow(sceneTABLE.rows.length);
	var left1CELL = row1.insertCell(0)
	var scenenoLABEL = row1.insertCell(1);
	var scenenoCELL = row1.insertCell(2);
	var right1CELL = row1.insertCell(3);
	left1CELL.className = "left";
    left1CELL.appendChild(document.createTextNode("\u00a0"));
	right1CELL.className = "right";
    right1CELL.appendChild(document.createTextNode("\u00a0"));
	scenenoLABEL.setAttribute("width", "160");
	scenenoCELL.colSpan = "3";
	scenenoLABEL.appendChild(createBoldLabel("Scene Number"));
	scenenoCELL.appendChild(createAnyTextTextBox("sd"+temp_day_count+"_scene" + temp_table_count, 25, "", "Enter Scene Number. [String Value]"));
	//row 2
	var row2 = sceneTABLE.insertRow(sceneTABLE.rows.length-footerSize);
	var left2CELL = row2.insertCell(0);
	var setnameLABEL = row2.insertCell(1);
	var setnameCELL = row2.insertCell(2);
	var locationidLABEL = row2.insertCell(3);
	var locationidCELL = row2.insertCell(4);
	var right2CELL = row2.insertCell(5);
	left2CELL.className = "left";
    left2CELL.appendChild(document.createTextNode("\u00a0"));
	right2CELL.className = "right";
    right2CELL.appendChild(document.createTextNode("\u00a0"));
	setnameLABEL.setAttribute("width", "160");
	setnameCELL.setAttribute("width", "160");
	locationidLABEL.setAttribute("width", "160");
	locationidCELL.setAttribute("width", "160");
	setnameLABEL.appendChild(createBoldLabel("Set Name"));
	setnameCELL.appendChild(createAnyTextTextBox("sd"+temp_day_count+"_set" + temp_table_count,20, "", "Enter Set Name. [String Value]"));
	locationidLABEL.appendChild(createBoldLabel("Location ID "));
	locationidCELL.appendChild(createAnyTextTextBox("sd"+temp_day_count+"_locationID" + temp_table_count, 20, "", "Enter Location ID. [String Value] (make sure it matches an id from location notes)"));
	//row 3
	var row3 = sceneTABLE.insertRow(sceneTABLE.rows.length-footerSize);
	var left3CELL = row3.insertCell(0);
	var synopsisLABEL = row3.insertCell(1);
	var synopsisCELL = row3.insertCell(2);
	var right3CELL = row3.insertCell(3);
	left3CELL.className = "left";
    left3CELL.appendChild(document.createTextNode("\u00a0"));
	right3CELL.className = "right";
    right3CELL.appendChild(document.createTextNode("\u00a0"));
	synopsisLABEL.setAttribute("width", "160");
	synopsisCELL.colSpan="3";
	synopsisLABEL.appendChild(createBoldLabel("Synopsis"));
	synopsisCELL.appendChild(createAnyTextTextBox("sd"+temp_day_count+"_synopsis" + temp_table_count,78, "", "Enter Synopsis. [String Value]"));
	
	//row 4
	var row4 = sceneTABLE.insertRow(sceneTABLE.rows.length);
	var left4CELL = row4.insertCell(0);
	var addressLABEL = row4.insertCell(1);
	var addressCELL = row4.insertCell(2);
	var right4CELL = row4.insertCell(3);
	left4CELL.className = "left";
    left4CELL.appendChild(document.createTextNode("\u00a0"));
	right4CELL.className = "right";
    right4CELL.appendChild(document.createTextNode("\u00a0"));
	addressLABEL.setAttribute("width", "160");
	addressCELL.colSpan="3";
	addressLABEL.appendChild(createBoldLabel("Address"));
	addressCELL.appendChild(createAnyTextTextBox("sd"+temp_day_count+"_address" + temp_table_count, 78, "", "Enter Address. [String Value]"));
	//row 5
	var row5 = sceneTABLE.insertRow(sceneTABLE.rows.length);
	var left5CELL = row5.insertCell(0);
	var interiorCELL = row5.insertCell(1);
	var exteriorCELL = row5.insertCell(2);
	var dayCELL = row5.insertCell(3);
	var nightCELL = row5.insertCell(4);
	var right5CELL = row5.insertCell(5);
	left5CELL.className = "left";
    left5CELL.appendChild(document.createTextNode("\u00a0"));
	right5CELL.className = "right";
    right5CELL.appendChild(document.createTextNode("\u00a0"));
	interiorCELL.setAttribute("width", "160");
	exteriorCELL.setAttribute("width", "160");
	dayCELL.setAttribute("width", "160");
	nightCELL.setAttribute("width", "160");
	interiorCELL.appendChild(createRadioButton("sd"+temp_day_count+"_intext" + temp_table_count, "INT", true));
	interiorCELL.appendChild(createBoldLabel("Interior "));
	exteriorCELL.appendChild(createRadioButton("sd"+temp_day_count+"_intext" + temp_table_count, "EXT", false));
	exteriorCELL.appendChild(createBoldLabel("Exterior "));
	dayCELL.appendChild(createBoldLabel("Day "));
	dayCELL.appendChild(createRadioButton("sd"+temp_day_count+"_daynight" + temp_table_count, "Day", true));
	nightCELL.appendChild(createBoldLabel("Night "));
	nightCELL.appendChild(createRadioButton("sd"+temp_day_count+"_daynight" + temp_table_count, "Night", false));
	//row 6
	var row6 = sceneTABLE.insertRow(sceneTABLE.rows.length);
	var left6CELL = row6.insertCell(0);
	var scriptpagesLABEL = row6.insertCell(1);
	var scriptpagesCELL = row6.insertCell(2);
	var shoottimesLABEL = row6.insertCell(3);
	var shoottimesCELL = row6.insertCell(4);
	var right6CELL = row6.insertCell(5);
	left6CELL.className = "left";
    left6CELL.appendChild(document.createTextNode("\u00a0"));
	right6CELL.className = "right";
    right6CELL.appendChild(document.createTextNode("\u00a0"));
	scriptpagesLABEL.setAttribute("width", "160");
	scriptpagesCELL.setAttribute("width", "160");
	shoottimesLABEL.setAttribute("width", "160");
	shoottimesCELL.setAttribute("width", "160");
	scriptpagesLABEL.appendChild(createBoldLabel("No. of Script Pages"));
	scriptpagesCELL.appendChild(createNumberTextBox("sd"+temp_day_count+"_pages" + temp_table_count, 4, "", "Enter Script Pages. [Number Value]"));
	scriptpagesCELL.appendChild(document.createTextNode("\u00a0"));
	scriptpagesCELL.appendChild(createNumberTextBox("sd"+temp_day_count+"_pagesnum" + temp_table_count, 2, "", "Enter Script Pages. [Number Value]"));
	scriptpagesCELL.appendChild(createBoldLabel(" /8 pgs"));
	shoottimesLABEL.appendChild(createBoldLabel("Est. Shoot Times"));
	shoottimesCELL.appendChild(createAnyTextTextBox("sd"+temp_day_count+"_shoottimes" + temp_table_count, 20, "", "Enter Est. Shoot Times. [String Value]"));
	//row 7
	var row7 = sceneTABLE.insertRow(sceneTABLE.rows.length);
	var left7CELL = row7.insertCell(0);
	var scripttimingLABEL = row7.insertCell(1);
	var scripttimingCELL = row7.insertCell(2);
	var right7CELL = row7.insertCell(3);
	left7CELL.className = "left";
    left7CELL.appendChild(document.createTextNode("\u00a0"));
	right7CELL.className = "right";
    right7CELL.appendChild(document.createTextNode("\u00a0"));
	scripttimingLABEL.setAttribute("width", "160");
	scripttimingLABEL.setAttribute("width", "160");
	scripttimingCELL.colSpan = "3";
	scripttimingLABEL.appendChild(createBoldLabel("Est. Script Timing"));
	scripttimingCELL.appendChild(createDateTextBox("sd"+temp_day_count+"_scripttime" + temp_table_count, 20, "", "Enter Est. Script Timing. [Time Value HH:MM:SS]"));
	//row 8
	var row8 = sceneTABLE.insertRow(sceneTABLE.rows.length);
	var left8CELL = row8.insertCell(0);
	var characterLABEL = row8.insertCell(1);
	var characterCELL = row8.insertCell(2);
	var right8CELL = row8.insertCell(3);
	var addcharacterROW = document.createElement("TR");
	var addcharacterCELL = addcharacterROW.insertCell(0);
	var addcharacterBUTTON = document.createElement("INPUT");
	var deletecharacterBUTTON = document.createElement("INPUT");
	left8CELL.className = "left";
    left8CELL.appendChild(document.createTextNode("\u00a0"));
	right8CELL.className = "right";
    right8CELL.appendChild(document.createTextNode("\u00a0"));
	row8.vAlign = "top";
	characterLABEL.setAttribute("width", "160");
	characterCELL.colSpan = "3";
	addcharacterBUTTON.setAttribute("type", "button");
	addcharacterBUTTON.setAttribute("value", "Add Character");
	addcharacterBUTTON.setAttribute("onClick", "addCharacterRow("+temp_day_count+","+temp_table_count+");");
	deletecharacterBUTTON.setAttribute("type", "button");
	deletecharacterBUTTON.setAttribute("value", "Delete Character");
	deletecharacterBUTTON.setAttribute("onClick", "deleteCharacterRow("+temp_day_count+","+temp_table_count+");");
	
	var characterTABLE = document.createElement("TABLE");
	var charactertableROW = characterTABLE.insertRow(characterTABLE.rows.length);
	var charactertableCELL = charactertableROW.insertCell(0);
	characterTABLE.setAttribute("width", "510");
	characterTABLE.setAttribute("border", "0");
	characterTABLE.setAttribute("cellspacing", "0");
	characterTABLE.setAttribute("cellpadding", "0");
	characterTABLE.setAttribute("id","sd"+temp_day_count+"_characters" + temp_table_count);
	charactertableCELL.appendChild(createAnyTextTextBox("sd"+temp_day_count+"_charactername" + temp_table_count+"_1", 20, "", "Enter Character Name. [String Value] (make sure it matches a character from cast list)"));
	
	characterLABEL.appendChild(createBoldLabel("Characters"));
	addcharacterCELL.appendChild(addcharacterBUTTON);
	addcharacterCELL.appendChild(deletecharacterBUTTON);
	addcharacterCELL.appendChild(createHiddenField("sd"+temp_day_count+"_charactercount" + temp_table_count, 1));
	
	characterCELL.appendChild(characterTABLE);
	characterCELL.appendChild(addcharacterROW);
	//row 9
	var row9 = sceneTABLE.insertRow(sceneTABLE.rows.length);
	var left9CELL = row9.insertCell(0);
	var requirementsLABEL = row9.insertCell(1);
	var requirementsCELL = row9.insertCell(2);
	var right9CELL = row9.insertCell(3);
	var addrequirementsBUTTON_row = document.createElement("TR");
	var addrequirementsBUTTON_cell = addrequirementsBUTTON_row.insertCell(0);
	var addrequirementsBUTTON = document.createElement("INPUT");
	var deleterequirementsBUTTON = document.createElement("INPUT");
	left9CELL.className = "left";
    left9CELL.appendChild(document.createTextNode("\u00a0"));
	right9CELL.className = "right";
    right9CELL.appendChild(document.createTextNode("\u00a0"));
	row9.setAttribute("valign", "top");
	requirementsLABEL.setAttribute("width", "160");
	requirementsCELL.colSpan = "3";
	addrequirementsBUTTON.setAttribute("type", "button");
	addrequirementsBUTTON.setAttribute("value", "Add Requirements");
	addrequirementsBUTTON.setAttribute("onClick", "addRequirementsRow("+temp_day_count+","+temp_table_count+");");
	deleterequirementsBUTTON.setAttribute("type", "button");
	deleterequirementsBUTTON.setAttribute("value", "Delete Requirements");
	deleterequirementsBUTTON.setAttribute("onClick", "deleteRequirementsRow("+temp_day_count+","+temp_table_count+");");
	var requirementsTABLE = document.createElement("TABLE");
	var requirementstableROW = requirementsTABLE.insertRow(requirementsTABLE.rows.length);
	var table_cell1 = requirementstableROW.insertCell(0);
	var table_cell2 = requirementstableROW.insertCell(1);
	var requirementsDROPDOWN =  document.createElement("SELECT");
	requirementsTABLE.setAttribute("width", "510");
	requirementsTABLE.setAttribute("border", "0");
	requirementsTABLE.setAttribute("cellspacing", "0");
	requirementsTABLE.setAttribute("cellpadding", "0");
	requirementsTABLE.setAttribute("id","sd"+temp_day_count+"_requirements"+temp_table_count);
	requirementsDROPDOWN.setAttribute("name", "sd"+temp_day_count+"_items"+temp_table_count+"_1");
	requirementsDROPDOWN.setAttribute("id", "sd"+temp_day_count+"_items"+temp_table_count+"_1");
	requirementsDROPDOWN.title = "Select an Item";
	requirementsDROPDOWN.appendChild(createDropdownList("Background Actors"));
	requirementsDROPDOWN.appendChild(createDropdownList("Stunts"));
	requirementsDROPDOWN.appendChild(createDropdownList("Vehicles"));
	requirementsDROPDOWN.appendChild(createDropdownList("Props"));
	requirementsDROPDOWN.appendChild(createDropdownList("Camera"));
	requirementsDROPDOWN.appendChild(createDropdownList("Special Effects"));
	requirementsDROPDOWN.appendChild(createDropdownList("Wardrobe"));
	requirementsDROPDOWN.appendChild(createDropdownList("Makeup/Hair"));
	requirementsDROPDOWN.appendChild(createDropdownList("Animals"));
	requirementsDROPDOWN.appendChild(createDropdownList("Animal Wrangler"));
	requirementsDROPDOWN.appendChild(createDropdownList("Music"));
	requirementsDROPDOWN.appendChild(createDropdownList("Sound"));
	requirementsDROPDOWN.appendChild(createDropdownList("Art Department"));
	requirementsDROPDOWN.appendChild(createDropdownList("Set Dressing"));
	requirementsDROPDOWN.appendChild(createDropdownList("Greenery"));
	requirementsDROPDOWN.appendChild(createDropdownList("Special Equipment"));
	requirementsDROPDOWN.appendChild(createDropdownList("Security"));
	requirementsDROPDOWN.appendChild(createDropdownList("Additional Labour"));
	requirementsDROPDOWN.appendChild(createDropdownList("Visual Effects"));
	requirementsDROPDOWN.appendChild(createDropdownList("Mechanical Effects"));
	requirementsDROPDOWN.appendChild(createDropdownList("Miscellaneous"));
	requirementsDROPDOWN.appendChild(createDropdownList("Notes"));
	table_cell1.appendChild(requirementsDROPDOWN);
	table_cell2.appendChild(createTextArea("sd"+temp_day_count+"_requirements"+temp_table_count+"_1", 40,0, "", "Enter Set Requirements. [String Value]"));
	requirementsLABEL.appendChild(createBoldLabel("Set Requirements"));
	addrequirementsBUTTON_cell.appendChild(addrequirementsBUTTON);
	addrequirementsBUTTON_cell.appendChild(deleterequirementsBUTTON);
	addrequirementsBUTTON_cell.appendChild(createHiddenField("sd"+temp_day_count+"_requirementscount" + temp_table_count, 1));
	requirementsCELL.appendChild(requirementsTABLE);
	requirementsCELL.appendChild(addrequirementsBUTTON_row);
	//row 10
	var row10 = sceneTABLE.insertRow(sceneTABLE.rows.length);
	var left10CELL = row10.insertCell(0);
	var buttonCELL = row10.insertCell(1);
	var tableCELL = row10.insertCell(2);
	var right10CELL = row10.insertCell(3);
	var mealBUTTON = document.createElement("INPUT");
	var mealTABLE = document.createElement("TABLE");
	left10CELL.className = "left";
    left10CELL.appendChild(document.createTextNode("\u00a0"));
	right10CELL.className = "right";
    right10CELL.appendChild(document.createTextNode("\u00a0"));
	tableCELL.colSpan = "3";
	mealBUTTON.setAttribute("type", "button");
	mealBUTTON.setAttribute("value", "Add Meal Break");
	mealBUTTON.setAttribute("name", "sd"+temp_day_count+"_mealbreakbutton"+temp_table_count);
	mealBUTTON.setAttribute("id", "sd"+temp_day_count+"_mealbreakbutton"+temp_table_count);
	mealBUTTON.setAttribute("onClick", "addMealBreak("+temp_day_count+","+temp_table_count+");");
	mealTABLE.setAttribute("width", "510");
	mealTABLE.setAttribute("border", "0");
	mealTABLE.setAttribute("cellspacing", "0");
	mealTABLE.setAttribute("cellpadding", "0");
	mealTABLE.setAttribute("id","sd"+temp_day_count+"_mealbreak"+temp_table_count);
	buttonCELL.appendChild(mealBUTTON);
	tableCELL.appendChild(mealTABLE);
	//footer row
	var footer_row = sceneTABLE.insertRow(sceneTABLE.rows.length);
	var footer_cell = footer_row.insertCell(0);
	footer_cell.className = "bottom";
	footer_cell.colSpan = "6";
	footer_cell.setAttribute("height", "10");
	//return the created table
	return sceneTABLE;
}