<%@ page import="java.io.ByteArrayInputStream" %>
<%@ page import="java.io.ByteArrayOutputStream" %>
<%@ page import="java.io.File" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.math.BigInteger" %>
<%@ page import="com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl" %>
<%@ page import="javax.xml.bind.JAXBElement" %>
<%@ page import="javax.xml.bind.JAXBContext" %>
<%@ page import="javax.xml.bind.Marshaller" %>
<%@ page import="javax.xml.bind.Unmarshaller" %>
<%@ page import="org.yawlfoundation.sb.shootingschedule.*"%>
<%@ page import="javazoom.upload.*"%>
<%@ page import="java.util.*"%>
<%@ page import="java.io.*"%>
<%@ page buffer="1024kb" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>Shooting Schedule</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<!--<script language="javascript">

var count = 1;
var table_count = 1;
var temp_day_count = 1;
var temp_table_count = 1;
var table_num = 0;
var temp_character_count = 1;
var temp_requirements_count = 1;
var days_count = 1;

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

//function for bold text
function createBoldLabel(text) {
var label = document.createElement("STRONG");
label.appendChild(document.createTextNode(text));
return label;
}

function addClass(class){
	var cell = document.createElement("TD");
	cell.className = class;
	cell.appendChild(document.createTextNode("\u00a0"));
	cell.setAttribute("width", "15");
	return cell;
}

function addRequirementsRow(day, table) {
	var tbody = document.getElementById("sd"+day+"_requirements"+table);
	var row = document.createElement("TR");
	var cell1 = document.createElement("TD");
	var cell2 = document.createElement("TD");
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
	cell1.appendChild(requirementsDROPDOWN);
	cell2.appendChild(createTextArea("sd"+day+"_requirements"+table+"_"+temp_requirements_count, 20));
	row.appendChild(cell1);
	row.appendChild(cell2);
	tbody.appendChild(row);
}

function addScene(table_name, table_number){
	updateTableCount(table_number);
	var tbody1 = document.getElementById(table_name);
	var row = document.createElement("TR");
	var cell_middle = document.createElement("TD");
	cell_middle.setAttribute("colspan", "6");
	//append the new SCENE to the DAY table
	cell_middle.appendChild(createTable());
	row.appendChild(cell_middle);
	tbody1.appendChild(row);
}

function addShootingDay () {
	count ++;
	updateTableCount(count);
	//update the day count
	days_count = document.getElementById("days").value;
	days_count ++;
	document.getElementById("days").value = days_count;
	//declare the elements
	var tbody2 = document.getElementById("shooting_days");
	var table = document.createElement("TABLE");
	var info_row = document.createElement("TR");
	//the graphical HEADER row information
	var entry_top_row = document.createElement("TR");
	var entry_top_cell1 = document.createElement("TD");
	var entry_top_cell2 = document.createElement("TD");
	var entry_top_cell3 = document.createElement("TD");
	entry_top_cell1.setAttribute("class", "header-left");
	entry_top_cell2.setAttribute("class", "header-middle");
	entry_top_cell2.setAttribute("colspan", "4");
	entry_top_cell3.setAttribute("class", "header-right");
	entry_top_cell2.appendChild(createBoldLabel("Shoot Day # "));
	entry_top_cell2.appendChild(createInput("sd"+temp_day_count+"_number", 5, "text", ""));
	entry_top_row.appendChild(entry_top_cell1);
	entry_top_row.appendChild(entry_top_cell2);
	entry_top_row.appendChild(entry_top_cell3);
	
	//the general DAY DETAILS table information
	var table3 = document.createElement("TABLE");
	var headerinfo_row = document.createElement("TR");
	var headerinfo_cell = document.createElement("TD");
	table3.setAttribute("width", "670");
	table3.setAttribute("border", "0");
	table3.setAttribute("cellspacing", "0");
	table3.setAttribute("cellpadding", "0");
	table3.appendChild(addDayDetails());
	headerinfo_cell.appendChild(table3);
	headerinfo_cell.setAttribute("colspan", "4");
	headerinfo_row.appendChild(addClass("left"));
	headerinfo_row.appendChild(headerinfo_cell);
	headerinfo_row.appendChild(addClass("right"));
	
	//the DAY table information
	table.setAttribute("width", "700");
	table.setAttribute("border", "0");
	table.setAttribute("cellspacing", "0");
	table.setAttribute("cellpadding", "0");
	table.setAttribute("id", "day" + count);
	
	//the SCENE table information
	var table2 = document.createElement("TABLE");
	var cell_middle = document.createElement("TD");
	var entire_row = document.createElement("TR");
	table2.setAttribute("width", "670");
	table2.setAttribute("border", "0");
	table2.setAttribute("cellspacing", "0");
	table2.setAttribute("cellpadding", "0");
	table2.setAttribute("id", "scenes" + count);
	table2.appendChild(createTable());
	cell_middle.appendChild(table2);
	cell_middle.setAttribute("colspan", "4");
	entire_row.appendChild(addClass("left"));
	entire_row.appendChild(cell_middle);
	entire_row.appendChild(addClass("right"));
	
	//the ADD SCENE button information
	var button_row = document.createElement("TR");
	var button_cell = document.createElement("TD");
	var button = document.createElement("INPUT");
	button.setAttribute("type", "button");
	button.setAttribute("value", "Add Scene");
	button.setAttribute("onClick", "addScene('scenes"+count+"', "+count+");");
	button_cell.appendChild(button);
	button_cell.setAttribute("colspan", "4");
	button_row.appendChild(addClass("left"));
	button_row.appendChild(button_cell);
	button_row.appendChild(addClass("right"));
	
	//the END OF DAY NOTES textbox information
	var endday_row = document.createElement("TR");
	var endday_cell1 = document.createElement("TD");
	var endday_cell2 = document.createElement("TD");
	var totalday_row = document.createElement("TR");
	var totalday_cell1 = document.createElement("TD");
	var totalday_cell2 = document.createElement("TD");
	var totalday_cell3 = document.createElement("TD");
	endday_cell1.setAttribute("width", "150");
	endday_cell2.setAttribute("colspan", "3");

	endday_cell1.appendChild(createBoldLabel("End of Day Notes "));
	endday_cell2.appendChild(createTextArea("sd"+temp_day_count+"_end", 50));
	endday_row.appendChild(addClass("left"));
	endday_row.appendChild(endday_cell1);
	endday_row.appendChild(endday_cell2);
	endday_row.appendChild(addClass("right"));
	
	var calculate = document.createElement("INPUT");
	calculate.setAttribute("type", "button");
	calculate.setAttribute("value", "Calculate");
	calculate.setAttribute("onClick", "calculateMod("+count+");");
	
	
	totalday_cell3.setAttribute("colspan", "2");
	totalday_cell1.appendChild(createBoldLabel("Total Script Pages"));
	totalday_cell2.appendChild(createInput("sd"+temp_day_count+"_totalpages", 4, "text", ""));
	totalday_cell2.appendChild(document.createTextNode("\u00a0"));
	totalday_cell2.appendChild(createInput("sd"+temp_day_count+"_totalpagesnum", 2, "text", ""));
	totalday_cell2.appendChild(createBoldLabel(" /8 pgs"));
	totalday_cell3.appendChild(calculate);
	totalday_row.appendChild(addClass("left"));
	totalday_row.appendChild(totalday_cell1);
	totalday_row.appendChild(totalday_cell2);
	totalday_row.appendChild(totalday_cell3);
	totalday_row.appendChild(addClass("right"));
	
	//append the entire DAY entry
	table.appendChild(entry_top_row);
	table.appendChild(headerinfo_row);
	table.appendChild(entire_row);
	table.appendChild(button_row);
	table.appendChild(endday_row);
	table.appendChild(totalday_row);
	table.appendChild(addFooterRow());
	tbody2.appendChild(table);
}

function addDayDetails() {
	var rows = document.createElement("TR");
	//hidden row
	var row_hidden = document.createElement("TR");
	var hidden_cell = document.createElement("TD");
	
	hidden_cell.appendChild(createInput("day_number_" + count, 20, "hidden", count));
	hidden_cell.appendChild(createInput("table_count_" + count, 20, "hidden", table_count));
	row_hidden.appendChild(hidden_cell);
	
	//row 1 - day number and crew call
	var row1 = document.createElement("TR");
	var label1_cell = document.createElement("TD");
	var shootday_cell = document.createElement("TD");
	var label2_cell = document.createElement("TD");
	var crew_call_cell = document.createElement("TD");
	
	label1_cell.appendChild(createBoldLabel("Shoot Day Date "));
	label1_cell.setAttribute("width", "150");
	shootday_cell.appendChild(createInput("sd"+temp_day_count+"_date", 20, "text", ""));
	shootday_cell.setAttribute("width", "185");
	
	
	label2_cell.appendChild(createBoldLabel("Shoot Day Weekday "));
	label2_cell.setAttribute("width", "150");
	crew_call_cell.appendChild(createInput("sd"+temp_day_count+"_weekday", 20, "text", ""));
	crew_call_cell.setAttribute("width", "185");
	row1.appendChild(label1_cell);
	row1.appendChild(shootday_cell);
	row1.appendChild(label2_cell);
	row1.appendChild(crew_call_cell);
	//row 2 - weekday and date
	var row2 = document.createElement("TR");
	var label3_cell = document.createElement("TD");
	var weekday_cell = document.createElement("TD");
	var label4_cell = document.createElement("TD");
	var date_cell = document.createElement("TD");
	
	label3_cell.appendChild(createBoldLabel("Crew Call "));
	label3_cell.setAttribute("width", "150");
	weekday_cell.appendChild(createInput("sd"+temp_day_count+"_crew", 20, "text", ""));
	weekday_cell.setAttribute("width", "185");
	
	label4_cell.appendChild(createBoldLabel("Travel to Loc "));
	label4_cell.setAttribute("width", "150");
	date_cell.appendChild(createInput("sd"+temp_day_count+"_traveltoloc", 20, "text", ""));
	date_cell.setAttribute("width", "185");
	row2.appendChild(label3_cell);
	row2.appendChild(weekday_cell);
	row2.appendChild(label4_cell);
	row2.appendChild(date_cell);
	
	//row 2 - weekday and date
	var row2a = document.createElement("TR");
	var label3a_cell = document.createElement("TD");
	var weekdaya_cell = document.createElement("TD");
	
	label3a_cell.appendChild(createBoldLabel("Bump In "));
	label3a_cell.setAttribute("width", "150");
	weekdaya_cell.appendChild(createInput("sd"+temp_day_count+"_bumpin", 20, "text", ""));
	weekdaya_cell.setAttribute("width", "185");
	weekdaya_cell.setAttribute("colspan", "3");
	
	row2a.appendChild(label3a_cell);
	row2a.appendChild(weekdaya_cell);

	
	//row 3 - start of day notes
	var row3 = document.createElement("TR");
	var label5_cell = document.createElement("TD");
	var start_cell = document.createElement("TD");
	
	var start = document.createElement("INPUT");
	start.setAttribute("name", "sd"+temp_day_count+"_start");
	start.setAttribute("id", "sd"+temp_day_count+"_start");
	start.setAttribute("size", "73");
	
	label5_cell.appendChild(createBoldLabel("Start of Day Notes "));
	label5_cell.setAttribute("width", "150");
	start_cell.appendChild(start);
	start_cell.setAttribute("colspan", "3");
	row3.appendChild(label5_cell);
	row3.appendChild(start_cell);
	
	//compile each row
	rows.appendChild(row_hidden);
	rows.appendChild(row1);
	rows.appendChild(row2);
	rows.appendChild(row2a);
	rows.appendChild(row3);
	return rows;
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

function addHeaderRow(){
	var header_row = document.createElement("TR");
	var header_cell = document.createElement("TD");
	header_cell.setAttribute("class", "top");
	header_cell.setAttribute("colspan", "6");
	header_cell.setAttribute("height", "10");
	header_row.appendChild(header_cell);
	return header_row;
}

function addFooterRow(){
	var footer_row = document.createElement("TR");
	var footer_cell = document.createElement("TD");
	footer_cell.setAttribute("class", "bottom");
	footer_cell.setAttribute("colspan", "6");
	footer_cell.setAttribute("height", "10");
	footer_row.appendChild(footer_cell);
	return footer_row;
}



function createTable () {
	var table = document.createElement("TABLE");
	table.setAttribute("width", "670");
	table.setAttribute("border", "0");
	table.setAttribute("cellspacing", "0");
	table.setAttribute("cellpadding", "0");
	table.appendChild(addHeaderRow());
	table.appendChild(addDetails());
	table.appendChild(addFooterRow());
	return table;
}


function addDetails(){
	var rows = document.createElement("TR");
	rows.appendChild(addRow1());
	rows.appendChild(addRow2());
	rows.appendChild(addRow3());
	rows.appendChild(addRow4());
	rows.appendChild(addRow5());
	rows.appendChild(addRow5a());
	rows.appendChild(addRow6());
	rows.appendChild(addRow7());
	rows.appendChild(addRow8());
	return rows;
}

function addRow1 () {
	var row = document.createElement("TR");
	var label_cell = document.createElement("TD");
	var input_cell = document.createElement("TD");
	var input = document.createElement("INPUT");
	
	label_cell.setAttribute("width", "160");
	input_cell.setAttribute("colspan", "3");
	
	input.setAttribute("name", "sd"+temp_day_count+"_scene" + temp_table_count);
	input.setAttribute("id", "sd"+temp_day_count+"_scene" + temp_table_count);
	
	label_cell.appendChild(createBoldLabel("Scene Number"));
	input_cell.appendChild(input);
	
	row.appendChild(addClass("left"));
	row.appendChild(label_cell);
	row.appendChild(input_cell);
	row.appendChild(addClass("right"));
	return row;
}

function addRow2 () {
	var row = document.createElement("TR");
	var label_cell1 = document.createElement("TD");
	var input_cell1 = document.createElement("TD");
	var label_cell2 = document.createElement("TD");
	var input_cell2 = document.createElement("TD");
	
	label_cell1.setAttribute("width", "160");
	label_cell2.setAttribute("width", "160");
	input_cell1.setAttribute("width", "160");
	input_cell2.setAttribute("width", "160");
	
	label_cell1.appendChild(createBoldLabel("Set Name"));
	input_cell1.appendChild(createInput("sd"+temp_day_count+"_set" + temp_table_count,20, "text", ""));
	label_cell2.appendChild(createBoldLabel("Synopsis"));
	input_cell2.appendChild(createInput("sd"+temp_day_count+"_synopsis" + temp_table_count,20, "text", ""));
	
	row.appendChild(addClass("left"));
	row.appendChild(label_cell1);
	row.appendChild(input_cell1);
	row.appendChild(label_cell2);
	row.appendChild(input_cell2);
	row.appendChild(addClass("right"));
	return row;
}

function addRow3 () {
	var row = document.createElement("TR");
	var label_cell1 = document.createElement("TD");
	var input_cell1 = document.createElement("TD");
	var label_cell2 = document.createElement("TD");
	var input_cell2 = document.createElement("TD");
	
	label_cell1.setAttribute("width", "160");
	label_cell2.setAttribute("width", "160");
	input_cell1.setAttribute("width", "160");
	input_cell2.setAttribute("width", "160");
	
	label_cell1.appendChild(createBoldLabel("Location ID "));
	input_cell1.appendChild(createInput("sd"+temp_day_count+"_locationID" + temp_table_count, 20, "text", ""));
	label_cell2.appendChild(createBoldLabel("Address"));
	input_cell2.appendChild(createInput("sd"+temp_day_count+"_address" + temp_table_count, 20, "text", ""));
	
	row.appendChild(addClass("left"));
	row.appendChild(label_cell1);
	row.appendChild(input_cell1);
	row.appendChild(label_cell2);
	row.appendChild(input_cell2);
	row.appendChild(addClass("right"));
	return row;
}

function addRow4 () {
	var row = document.createElement("TR");
	var input_cell1 = document.createElement("TD");
	var input_cell2 = document.createElement("TD");
	var input_cell3 = document.createElement("TD");
	var input_cell4 = document.createElement("TD");
	
	input_cell1.setAttribute("width", "160");
	input_cell2.setAttribute("width", "160");
	input_cell3.setAttribute("width", "160");
	input_cell4.setAttribute("width", "160");
	
	input_cell1.appendChild(createBoldLabel("Interior "));
	input_cell1.appendChild(createInput("sd"+temp_day_count+"_intext" + temp_table_count,0,"radio", "int"));
	input_cell2.appendChild(createBoldLabel("Exterior "));
	input_cell2.appendChild(createInput("sd"+temp_day_count+"_intext" + temp_table_count,0,"radio", "ext"));
	input_cell3.appendChild(createBoldLabel("Day "));
	input_cell3.appendChild(createInput("sd"+temp_day_count+"_daynight" + temp_table_count,0,"radio", "day"));
	input_cell4.appendChild(createBoldLabel("Night "));
	input_cell4.appendChild(createInput("sd"+temp_day_count+"_daynight" + temp_table_count,0,"radio", "night"));
	
	row.appendChild(addClass("left"));
	row.appendChild(input_cell1);
	row.appendChild(input_cell2);
	row.appendChild(input_cell3);
	row.appendChild(input_cell4);
	row.appendChild(addClass("right"));
	return row;
}

function addRow5 () {
	var row = document.createElement("TR");
	var label_cell1 = document.createElement("TD");
	var input_cell1 = document.createElement("TD");
	var label_cell2 = document.createElement("TD");
	var input_cell2 = document.createElement("TD");
	
	label_cell1.setAttribute("width", "160");
	label_cell2.setAttribute("width", "160");
	input_cell1.setAttribute("width", "160");
	input_cell2.setAttribute("width", "160");
	
	label_cell1.appendChild(createBoldLabel("No. of Script Pages"));
	input_cell1.appendChild(createInput("sd"+temp_day_count+"_pages" + temp_table_count, 4, "text", ""));
	input_cell1.appendChild(document.createTextNode("\u00a0"));
	input_cell1.appendChild(createInput("sd"+temp_day_count+"_pagesnum" + temp_table_count, 2, "text", ""));
	input_cell1.appendChild(createBoldLabel(" /8 pgs"));
	label_cell2.appendChild(createBoldLabel("Est. Shoot Times"));
	input_cell2.appendChild(createInput("sd"+temp_day_count+"_times" + temp_table_count, 20, "text", ""));
	
	row.appendChild(addClass("left"));
	row.appendChild(label_cell1);
	row.appendChild(input_cell1);
	row.appendChild(label_cell2);
	row.appendChild(input_cell2);
	row.appendChild(addClass("right"));
	return row;
}

function addRow5a () {
	var row = document.createElement("TR");
	var label_cell1 = document.createElement("TD");
	var input_cell1 = document.createElement("TD");
	
	label_cell1.setAttribute("width", "160");
	input_cell1.setAttribute("width", "160");
	input_cell1.setAttribute("colspan", "3");
	
	label_cell1.appendChild(createBoldLabel("Est. Script Timing"));
	input_cell1.appendChild(createInput("sd"+temp_day_count+"_pages" + temp_table_count, 20, "text", ""));
	
	row.appendChild(addClass("left"));
	row.appendChild(label_cell1);
	row.appendChild(input_cell1);
	row.appendChild(addClass("right"));
	return row;
}

function addRow6 () {
	var row = document.createElement("TR");
	var button_row = document.createElement("TR");
	var label_cell = document.createElement("TD");
	var input_cell = document.createElement("TD");
	var button_cell = document.createElement("TD");
	var table = document.createElement("TABLE");
	var table_row = document.createElement("TR");
	var table_cell = document.createElement("TD");
	var table_input = document.createElement("INPUT");
	var button = document.createElement("INPUT");
	var hidden = document.createElement("INPUT");
	
	row.setAttribute("valign", "top");
	
	label_cell.setAttribute("width", "160");
	input_cell.setAttribute("colspan", "3");
	
	button.setAttribute("type", "button");
	button.setAttribute("value", "Add Character");
	button.setAttribute("onClick", "addCharacterRow("+temp_day_count+","+temp_table_count+");");
	
	table.setAttribute("width", "510");
	table.setAttribute("border", "0");
	table.setAttribute("cellspacing", "0");
	table.setAttribute("cellpadding", "0");
	table.setAttribute("id","sd"+temp_day_count+"_characters" + temp_table_count);
	
	table_input.setAttribute("name", "sd"+temp_day_count+"_charactername" + temp_table_count+"_1");
	table_input.setAttribute("id", "sd"+temp_day_count+"_charactername" + temp_table_count+"_1");
	
	label_cell.appendChild(createBoldLabel("Characters"));
	
	table_cell.appendChild(table_input);
	
	button_cell.appendChild(button);
	button_cell.appendChild(createInput("sd"+temp_day_count+"_charactercount" + temp_table_count, 5, "hidden", 1));
	button_row.appendChild(button_cell);
	
	table_row.appendChild(table_cell);
	table.appendChild(table_row);
	
	input_cell.appendChild(table);
	input_cell.appendChild(button_row);
	
	row.appendChild(addClass("left"));
	row.appendChild(label_cell);
	row.appendChild(input_cell);
	row.appendChild(addClass("right"));
	
	return row;
}

function addRow7 () {

	var row = document.createElement("TR");
	var addrequirementsBUTTON_row = document.createElement("TR");
	var label_cell = document.createElement("TD");
	var input_cell = document.createElement("TD");
	var addrequirementsBUTTON_cell = document.createElement("TD");
	var table = document.createElement("TABLE");
	var table_row = document.createElement("TR");
	var table_cell1 = document.createElement("TD");
	var table_cell2 = document.createElement("TD");
	var description = document.createElement("TEXTAREA");
	var requirementsDROPDOWN =  document.createElement("SELECT");
	var addrequirementsBUTTON = document.createElement("INPUT");
	
	row.setAttribute("valign", "top");
	
	label_cell.setAttribute("width", "160");
	input_cell.setAttribute("colspan", "3");
	
	addrequirementsBUTTON.setAttribute("type", "button");
	addrequirementsBUTTON.setAttribute("value", "Add Requirements");
	addrequirementsBUTTON.setAttribute("onClick", "addRequirementsRow("+temp_day_count+","+temp_table_count+");");

	table.setAttribute("width", "510");
	table.setAttribute("border", "0");
	table.setAttribute("cellspacing", "0");
	table.setAttribute("cellpadding", "0");
	table.setAttribute("id","sd"+temp_day_count+"_requirements"+temp_table_count);
	
	requirementsDROPDOWN.setAttribute("name", "sd"+temp_day_count+"_items"+temp_table_count+"_1");
	requirementsDROPDOWN.setAttribute("id", "sd"+temp_day_count+"_items"+temp_table_count+"_1");
	
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
	
	description.setAttribute("name", "sd"+temp_day_count+"_requirements"+temp_table_count+"_1");
	description.setAttribute("id", "sd"+temp_day_count+"_requirements"+temp_table_count+"_1");
	
	label_cell.appendChild(createBoldLabel("Set Requirements"));
	
	table_cell1.appendChild(requirementsDROPDOWN);
	table_cell2.appendChild(description);
	
	addrequirementsBUTTON_cell.appendChild(addrequirementsBUTTON);
	addrequirementsBUTTON_cell.appendChild(createInput("sd"+temp_day_count+"_requirementscount" + temp_table_count, 5, "hidden", 1));
	addrequirementsBUTTON_row.appendChild(addrequirementsBUTTON_cell);
	
	table_row.appendChild(table_cell1);
	table_row.appendChild(table_cell2);
	table.appendChild(table_row);
	
	input_cell.appendChild(table);
	input_cell.appendChild(addrequirementsBUTTON_row);
	
	row.appendChild(addClass("left"));
	row.appendChild(label_cell);
	row.appendChild(input_cell);
	row.appendChild(addClass("right"));
	
	return row;
}

function addRow8() {
	var row = document.createElement("TR");
	var button_cell = document.createElement("TD");
	var table_cell = document.createElement("TD");
	var button = document.createElement("INPUT");
	var table = document.createElement("TABLE");

	table_cell.setAttribute("colspan", "3");
	
	button.setAttribute("type", "button");
	button.setAttribute("value", "Add Meal Break");
	button.setAttribute("name", "sd"+temp_day_count+"_mealbreakbutton"+temp_table_count);
	button.setAttribute("id", "sd"+temp_day_count+"_mealbreakbutton"+temp_table_count);
	button.setAttribute("onClick", "addMealBreak("+temp_day_count+","+temp_table_count+");");

	table.setAttribute("width", "510");
	table.setAttribute("border", "0");
	table.setAttribute("cellspacing", "0");
	table.setAttribute("cellpadding", "0");
	table.setAttribute("id","sd"+temp_day_count+"_mealbreak"+temp_table_count);
	
	button_cell.appendChild(button);
	table_cell.appendChild(table);
	
	row.appendChild(addClass("left"));
	row.appendChild(button_cell);
	row.appendChild(table_cell);
	row.appendChild(addClass("right"));
	
	return row;
}

function addMealBreak(day, table) {
	var tbody = document.getElementById("sd"+day+"_mealbreak"+table);
	var row = document.createElement("TR");
	var mealCELL = document.createElement("TD");
	var mealDROPDOWN =  document.createElement("SELECT");
	var mealtimesLABEL = document.createElement("TD");
	var mealtimesCELL = document.createElement("TD");
	
	
	mealDROPDOWN.setAttribute("name", "sd"+day+"_meal"+table);
	mealDROPDOWN.setAttribute("id", "sd"+day+"_meal"+table);
	mealDROPDOWN.appendChild(createDropdownList("Breakfast"));
	mealDROPDOWN.appendChild(createDropdownList("Morning Tea"));
	mealDROPDOWN.appendChild(createDropdownList("Lunch"));
	mealDROPDOWN.appendChild(createDropdownList("Afternoon Tea"));
	mealDROPDOWN.appendChild(createDropdownList("Dinner"));
	mealDROPDOWN.appendChild(createDropdownList("Supper"));
	
	mealCELL.appendChild(mealDROPDOWN);
	mealtimesLABEL.appendChild(createBoldLabel("Times"));
	mealtimesCELL.appendChild(createInput("sd"+day+"_break"+table, 25, "text", ""));

	row.appendChild(mealCELL);
	row.appendChild(mealtimesLABEL);
	row.appendChild(mealtimesCELL);
	
	tbody.appendChild(row);
	
	document.getElementById("sd"+day+"_mealbreakbutton"+table).disabled = true;
}

function addCharacterRow(day, table) {
	var tbody = document.getElementById("sd"+day+"_characters"+table);
	var row = document.createElement("TR");
	var cell = document.createElement("TD");
	//get the number of CHARACTERS entries
	temp_character_count = document.getElementById("sd"+day+"_charactercount"+table).value;
	temp_character_count ++;
	document.getElementById("sd"+day+"_charactercount"+table).value = temp_character_count;
	//append the new row to the CHARACTERS table
	cell.appendChild(createInput("sd"+day+"_charactername"+table+"_"+temp_character_count, 20, "text",""));
	row.appendChild(cell);
	tbody.appendChild(row);
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

</script>-->
<!-- Stylesheet imports -->
<link href="../forms_completed/graphics/style.css" rel="stylesheet" type="text/css">
<link href="../forms_completed/styles/common.css" rel="stylesheet" type="text/css" />

<!-- javascript imports -->
<script type="text/javascript" src="../scripts/common.js"></script>
<script type="text/javascript" src="../forms_completed/scripts/filloutShootingSchedule.js"></script>
</head>

<body>

<% 
	String xml = null;
	
	if (MultipartFormDataRequest.isMultipartFormData(request)) 
	{
		System.out.println("mrequest workitemid: "+request.getParameter("workItemID"));
		System.out.println("mrequest userid: "+request.getParameter("userID"));
		System.out.println("mrequest sessionHandle: "+request.getParameter("sessionHandle"));
		System.out.println("mrequest submit: "+request.getParameter("submit"));
		System.out.println("mrequest jsessionid: "+request.getParameter("JSESSIONID"));
         MultipartFormDataRequest mrequest = new MultipartFormDataRequest(request);
         String todo = null;
		 StringBuffer result = new StringBuffer();
		 
         if (mrequest != null)
		 {
			todo = mrequest.getParameter("todo");
		 }
		 
	     if ( (todo != null) && (todo.equalsIgnoreCase("upload")) )
	     {
            Hashtable files = mrequest.getFiles();
            if ( (files != null) && (!files.isEmpty()) )
            {
                UploadFile file = (UploadFile) files.get("uploadfile");
				InputStream in = file.getInpuStream();
				
				int i = in.read();
				while (i != -1) {
					result.append((char) i);
					i = in.read();
				}
			}
			
            int beginOfFile = result.indexOf("<?xml");
            int endOfFile = result.indexOf("</ns2:Input_Shooting_Schedule>");
            if(beginOfFile != -1 && endOfFile != -1){
                xml = result.substring(
                    beginOfFile,
                    endOfFile + 30);
				//System.out.println("xml: "+xml);
    		}
		}
	}
	else{
		xml = "<?xml version='1.0' encoding='UTF-8'?><ns2:Input_Shooting_Schedule xmlns:ns2='http://www.yawlfoundation.org/sb/shootingSchedule' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.yawlfoundation.org/sb/shootingSchedule shootingScheduleType.xsd '><production>new</production><shootingSchedule><lastUpdatedDate>2007-07-07</lastUpdatedDate><director>me</director><producer>you</producer><startDate>2006-06-06</startDate><scheduledFinish>2005-05-05</scheduledFinish><revisedFinish>2004-04-04</revisedFinish><scheduledShootingDays>20</scheduledShootingDays></shootingSchedule><totalScenes>0</totalScenes><totalPageTime><number>0</number><numerator>0</numerator></totalPageTime><originalTiming>12:00:00</originalTiming></ns2:Input_Shooting_Schedule>";
		//xml = (String)session.getAttribute("outputData");
		xml = xml.replaceAll("<Input_Shooting_Schedule", "<ns2:Input_Shooting_Schedule xmlns:ns2='http://www.yawlfoundation.org/sb/shootingSchedule'");
		xml = xml.replaceAll("</Input_Shooting_Schedule","</ns2:Input_Shooting_Schedule");
		//System.out.println("outputData xml: "+xml+" --- ");
	}
	
	ByteArrayInputStream xmlBA = new ByteArrayInputStream(xml.getBytes());
	JAXBContext jc = JAXBContext.newInstance("org.yawlfoundation.sb.shootingschedule");
	Unmarshaller u = jc.createUnmarshaller();
	JAXBElement issElement = (JAXBElement) u.unmarshal(xmlBA);	//creates the root element from XML file	            
	InputShootingScheduleType iss = (InputShootingScheduleType) issElement.getValue();
	ShootingScheduleType ss = iss.getShootingSchedule(); 
%>

<table width="700" border="0" align="center" cellpadding="0" cellspacing="0">
<tr><td colspan="3" class="background_top">&nbsp;</td></tr>
<tr>
<td width="14" class="background_left">&nbsp;</td>
<td><h1 align="center">Shooting Schedule </h1>      
<form name="form1" method="post">
<table width="700" border="0" align="center" cellpadding="0" cellspacing="0">
<tr>
<td>
<table width='700' border='0' cellspacing='0' cellpadding='0'>
<tr>
<td width="15" align="right" class="header-left">&nbsp;</td>
<td height="20" colspan='4' class="header-middle">General Info </td>
<td width="15" class="header-right">&nbsp;</td>
</tr>
<tr height="30">
<td width="15" class="left">&nbsp;</td>
<td><strong>Production</strong></td>
<td><input name='production' type='text' id='production' value="<%= iss.getProduction() %>"></td>
<td><strong>Director</strong></td>
<td><input name='director' type='text' id='director' value="<%=ss.getDirector() %>"></td>
<td width="15" class="right">&nbsp;</td>
</tr>
<tr height="30">
<td class="left">&nbsp;</td>
<td><strong>Producer</strong></td>
<td><input name='producer' type='text' id='producer' value="<%=ss.getProducer() %>"></td>
<td><strong>Last Updated Date</strong></td>
<td><input name='last_updated' type='text' id='last_updated' value="<%=ss.getLastUpdatedDate() %>"></td>
<td class="right">&nbsp;</td>
</tr>
<tr height="30"><td colspan="10" class='bottom'>&nbsp;</td></tr>
</table>
</td>
</tr>
<tr>
<td>
<table width='700' border='0' cellspacing='0' cellpadding='0'>
<tr>
<td width="15" align="right" class="header-left">&nbsp;</td>
<td height="20" colspan='4' class="header-middle">Scheduled Date Brief </td>
<td width="15" class="header-right">&nbsp;</td>
</tr>
<tr height="30">
<td class="left">&nbsp;</td>
<td><strong>Start Date </strong></td>
<td><input name='start_date' type='text' id='start_date' value="<%=ss.getStartDate() %>"></td>
<td><strong>Scheduled Finish</strong></td>
<td><input name='scheduled_finish' type='text' id='scheduled_finish' value="<%=ss.getScheduledFinish() %>"></td>
<td class="right">&nbsp;</td>
</tr>
<tr height="30">
<td class="left">&nbsp;</td>
<td><strong>Revised Finish </strong></td>
<td><input name='revised_finish' type='text' id='revised_finish' value="<% if(ss.getRevisedFinish() != null) {out.print(ss.getRevisedFinish());} %>"></td>
<td><strong>Scheduled Shooting Days </strong></td>
<td><input name='scheduled_shooting_days' type='text' id='scheduled_shooting_days' value="<%=ss.getScheduledShootingDays() %>"></td>
<td class="right">&nbsp;</td>
</tr>
<tr height="30"><td colspan="10" class='bottom'>&nbsp;</td></tr>
</table>					
</td>
</tr>
<tr>
<td>
<table width="700" border="0" cellpadding="0" cellspacing="0" id="shooting_days">
<tr>
<td>
<% int a=0;
int tables = 0;
if(ss.getSingleDaySchedule().size() != 0) { %>

<table width="700" border="0" cellpadding="0" cellspacing="0" id="<% out.print("day" + a); %>">
	<% 
	for(SingleDayScheduleType sds : ss.getSingleDaySchedule()){
	a++;
	%>
	<tr>
		<td width="15" class="header-left">&nbsp;</td>
		<td colspan="4" class="header-middle">Shoot Day # 
		<input name="<% out.print("sd" + a + "_number");%>" type="text" id="<% out.print("sd" + a + "_number");%>" size="5" value="<%=sds.getShootDayNo() %>"></td>
		<td width="15" class="header-right">&nbsp;</td>
	</tr>
	<tr>
		<td class="left">&nbsp;</td>
		<td colspan="4" align="left">&nbsp;</td>
		<td class="right">&nbsp;</td>
	</tr>
	<tr>
		<td class="left">&nbsp;</td>
		<td width="150" align="left"><strong>Shoot Day Date</strong></td>
		<td width="185" align="left"><input name="<% out.print("sd" + a + "_date");%>" type="text" id="<% out.print("sd" + a + "_date");%>" value="<%=sds.getShootDayDate() %>"></td>
		<td width="150" align="left"><strong>Shoot Day Weekday</strong></td>
		<td width="185" align="left"><input name="<% out.print("sd" + a + "_weekday");%>" type="text" id="<% out.print("sd" + a + "_weekday");%>" value="<%=sds.getShootDayWeekday() %>"></td>
		<td class="right">&nbsp;</td>
	</tr>
	<tr>
		<td class="left">&nbsp;</td>
		<td width="150" align="left"><strong>Crew Call </strong></td>
		<td width="185" align="left"><input name="<% out.print("sd" + a + "_crew");%>" type="text" id="<% out.print("sd" + a + "_crew");%>" value="<%=sds.getCrewCall() %>"></td>
		<td width="150" align="left"><strong>Travel To Loc </strong></td>
		<td width="185" align="left"><input name="<% out.print("sd" + a + "_traveltoloc");%>" type="text" id="<% out.print("sd" + a + "_traveltoloc");%>" value="<%=sds.getTravelToLoc() %>"></td>
		<td class="right">&nbsp;</td>
	</tr>
	<tr>
		<td class="left">&nbsp;</td>
		<td align="left"><strong>Bump In</strong></td>
		<td colspan="3" align="left"><input name="<% out.print("sd" + a + "_bumpin");%>" type="text" id="<% out.print("sd" + a + "_bumpin");%>" size="8" value="<%=sds.getBumpIn() %>"></td>
		<td class="right">&nbsp;</td>
	</tr>
	<tr>
		<td class="left">&nbsp;</td>
		<td width="150" align="left"><strong>Start of Day Notes </strong></td>
		<td colspan="3" align="left"><input name="<% out.print("sd" + a + "_start");%>" type="text" id="<% out.print("sd" + a + "_start");%>" size="73" value="<%=sds.getStartDayNotes() %>"></td>
		<td class="right">&nbsp;</td>
	</tr>
<tr>
<td class="left">&nbsp;</td>
<td colspan="4" align="left">
<table width="670" border="0" cellpadding="0" cellspacing="0" id="<% out.print("scenes" + a); %>">
	<% int b=0;
	for(SceneScheduleType scs : sds.getSceneSchedule()) {
	b++;
	%>
		<tr><td colspan="6" class="top">&nbsp;</td></tr>
		<tr>
			<td width="15" class="left">&nbsp;</td>
			<td colspan="4" valign="top">&nbsp;</td>
			<td width="15" class="right">&nbsp;</td>
		</tr>
		<tr>
			<td width="15" height="30" class="left">&nbsp;</td>
			<td width="160" height="30" valign="top"><strong>Scene Number</strong></td>
			<td height="30" colspan="3" valign="top"><input name="<% out.print("sd" + a + "_scene" + b);%>" type="text" id="<% out.print("sd" + a + "_scene" + b);%>" value="<%= scs.getScene() %>"></td>
			<td height="30" class="right">&nbsp;</td>
		</tr>
		<tr>
			<td height="30" class="left">&nbsp;</td>
			<td height="30" valign="top"><strong>Set Name</strong></td>
			<td height="30" valign="top"><input name="<% out.print("sd" + a + "_set" + b);%>" type="text" id="<% out.print("sd" + a + "_set" + b);%>" value="<%= scs.getSet()%>"></td>
			<td height="30" valign="top"><strong>Synopsis</strong></td>
			<td height="30" valign="top"><input name="<% out.print("sd" + a + "_synopsis" + b);%>" type="text" id="<% out.print("sd" + a + "_synopsis" + b);%>" value="<%= scs.getSynopsis() %>"></td>
			<td height="30" class="right">&nbsp;</td>
		</tr>
		<tr>
			<td width="15" height="30" class="left">&nbsp;</td>
			<td width="160" height="30" valign="top"><strong>Location ID </strong></td>
			<td width="160" height="30" valign="top"><input name="<% out.print("sd" + a + "_locationID" + b);%>" type="text" id="<% out.print("sd" + a + "_locationID" + b);%>" value="<%= scs.getLocationID()%>"></td>
			<td width="160" height="30" valign="top"><strong>Address</strong></td>
			<td width="160" height="30" valign="top"><input name="<% out.print("sd" + a + "_address" + b);%>" type="text" id="<% out.print("sd" + a + "_address" + b);%>" value="<%= scs.getAddress()%>"></td>
			<td height="30" class="right">&nbsp;</td>
		</tr>
		<tr>
			<td width="15" height="30" class="left">&nbsp;</td>
			<td width="160" height="30" valign="top"><strong>Interior</strong><input name="<% out.print("sd" + a + "_intext" + b);%>" type="radio" value="int" <% if(scs.getINEX().equals("int")){ out.print("checked"); } %>></td>
			<td width="160" height="30" valign="top"><strong>Exterior</strong><input name="<% out.print("sd" + a + "_intext" + b);%>" type="radio" value="ext" <% if(scs.getINEX().equals("ext")){ out.print("checked"); } %>></td>
			<td width="160" height="30" valign="top"><strong>Day</strong>
			<input name="<% out.print("sd" + a + "_daynight" + b);%>" type="radio" value="day" <% if(scs.getDN().equals("day")){ out.print("checked"); } %>></td>
			<td width="160" height="30" valign="top"><strong>Night</strong><input name="<% out.print("sd" + a + "_daynight" + b);%>" type="radio" value="night" <% if(scs.getDN().equals("night")){ out.print("checked"); } %>></td>
			<td height="30" class="right">&nbsp;</td>
		</tr>
		<% PageTimeType pt = scs.getPageTime(); %>
		<tr>
			<td height="30" class="left">&nbsp;</td>
			<td height="30" valign="top"><strong>No. of Script Pages</strong></td>
			<td height="30" valign="top">
			<input name="<% out.print("sd" + a + "_pages" + b);%>" type="text" id="<% out.print("sd" + a + "_pages" + b);%>" size="4" value="<%=pt.getNumber() %>">
			<input name="<% out.print("sd" + a + "_pagesnum" + b);%>" type="text" id="<% out.print("sd" + a + "_pagesnum" + b);%>" size="2" value="<%=pt.getNumerator() %>">
			<strong>/8 pgs </strong></td>
			<td height="30" valign="top"><strong>Est. Shoot Times </strong></td>
			<td height="30" valign="top"><input name="<% out.print("sd" + a + "_shoottimes" + b);%>" type="text" id="<% out.print("sd" + a + "_shoottimes" + b);%>" value="<%= scs.getEstShootTimes()%>"></td>
			<td height="30" class="right">&nbsp;</td>
		</tr>
		<tr>
			<td height="30" class="left">&nbsp;</td>
			<td height="30" valign="top"><strong>Est. Script Timing </strong></td>
			<td height="30" colspan="3" valign="top"><input name="<% out.print("sd" + a + "_scripttime" + b);%>" type="text" id="<% out.print("sd" + a + "_scripttime" + b);%>" value="<%=scs.getEstScriptTiming() %>"></td>
			<td height="30" class="right">&nbsp;</td>
		</tr>
		<tr>
			<td height="30" rowspan="2" class="left">&nbsp;</td>
			<td height="30" rowspan="2" valign="top"><strong>Characters</strong></td>
			<td height="15" colspan="3" valign="top">
			<table width="510" border="0" cellpadding="0" cellspacing="0" id="<% out.print("sd" + a + "_characters" + b);%>">
				<% int c=0;
				CharactersType ct = scs.getCharacters();
				List<String> characters_list = ct.getCharacter();
					for(String characters : characters_list) {
						c ++;%>
					<tr><td><input name="<% out.print("sd" + a + "_charactername" + b + "_" + c);%>" type="text" id="<% out.print("sd" + a + "_charactername" + b + "_" + c);%>" value="<%=characters%>"></td></tr>
				<% } %>
			</table>													  
			</td>
			<td height="30" rowspan="2" class="right">&nbsp;</td>
		</tr>
		<tr><td height="15" colspan="3" valign="top"><input name="button3" type="button" onClick="addCharacterRow(<% out.print(a +","+ b); %>);" value="Add Character"/><input name="<% out.print("sd" + a + "_charactercount" + b);%>" type="hidden" id="<% out.print("sd" + a + "_charactercount" + b);%>" value="<% out.print(c);%>"></td></tr>
		<tr>
			<td width="15" height="30" rowspan="2" class="left">&nbsp;</td>
			<td width="160" height="30" rowspan="2" valign="top"><strong>Set Requirements </strong></td>
			<td height="15" colspan="3" valign="top">
			<table width="510" border="0" cellpadding="0" cellspacing="0" id="<% out.print("sd" + a + "_requirements" + b );%>">
				<% int d=0;
				SetRequirementsType sr = scs.getSetRequirements();
				for(SingleEntryType se : sr.getSingleEntry()) {
					d++;
				%>  
					<tr valign="top">
						<td><select name="<% out.print("sd" + a + "_items" + b + "_" + d);%>" id="<% out.print("sd" + a + "_items" + b + "_" + d);%>">
						<option value="Background Actors" <% if(se.getItem().equals("Background Actors")) { out.print("selected"); }%>>Background Actors</option>
						<option value="Stunts" <% if(se.getItem().equals("Stunts")) { out.print("selected"); }%>>Stunts</option>
						<option value="Vehicles" <% if(se.getItem().equals("Vehicles")) { out.print("selected"); }%>>Vehicles</option>
						<option value="Props" <% if(se.getItem().equals("Props")) { out.print("selected"); }%>>Props</option>
						<option value="Camera" <% if(se.getItem().equals("Camera")) { out.print("selected"); }%>>Camera</option>
						<option value="Special Effects" <% if(se.getItem().equals("Special Effects")) { out.print("selected"); }%>>Special Effects</option>
						<option value="Wardrobe" <% if(se.getItem().equals("Wardrobe")) { out.print("selected"); }%>>Wardrobe</option>
						<option value="Makeup/Hair" <% if(se.getItem().equals("Makeup/Hair")) { out.print("selected"); }%>>Makeup/Hair</option>
						<option value="Animals" <% if(se.getItem().equals("Animals")) { out.print("selected"); }%>>Animals</option>
						<option value="Animal Wrangler" <% if(se.getItem().equals("Animal Wrangler")) { out.print("selected"); }%>>Animal Wrangler</option>
						<option value="Music" <% if(se.getItem().equals("Music")) { out.print("selected"); }%>>Music</option>
						<option value="Sound" <% if(se.getItem().equals("Sound")) { out.print("selected"); }%>>Sound</option>
						<option value="Art Department" <% if(se.getItem().equals("Art Department")) { out.print("selected"); }%>>Art Department</option>
						<option value="Set Dressing" <% if(se.getItem().equals("Set Dressing")) { out.print("selected"); }%>>Set Dressing</option>
						<option value="Greenery" <% if(se.getItem().equals("Greenary")) { out.print("selected"); }%>>Greenery</option>
						<option value="Special Equipment" <% if(se.getItem().equals("Special Equipment")) { out.print("selected"); }%>>Special Equipment</option>
						<option value="Security" <% if(se.getItem().equals("Security")) { out.print("selected"); }%>>Security</option>
						<option value="Additional Labour" <% if(se.getItem().equals("Additional Labour")) { out.print("selected"); }%>>Additional Labour</option>
						<option value="Visual Effects" <% if(se.getItem().equals("Visual Effects")) { out.print("selected"); }%>>Visual Effects</option>
						<option value="Mechanical Effects" <% if(se.getItem().equals("Mechanical Effects")) { out.print("selected"); }%>>Mechanical Effects</option>
						<option value="Miscellaneous" <% if(se.getItem().equals("Miscellaneous")) { out.print("selected"); }%>>Miscellaneous</option>
						<option value="Notes" <% if(se.getItem().equals("Notes")) { out.print("selected"); }%>>Notes</option>
						</select></td>
						<td><textarea name="<% out.print("sd" + a + "_requirements" + b + "_" + d);%>" id="<% out.print("sd" + a + "_requirements" + b + "_" + d);%>" cols="20"><%=se.getRequirements() %></textarea></td>
					</tr>
				<%}%> 
			</table>														
			</td>
			<td height="30" rowspan="2" class="right">&nbsp;</td>
		</tr>
		<tr>
		<td height="15" colspan="3" valign="top"><input name="requirements1" type="button" id="requirements1" onClick="addRequirementsRow(<% out.print(a +","+ b); %>);" value="Add Set Requirements">
		<input name="<% out.print("sd" + a + "_requirementscount" + b);%>" type="hidden" id="<% out.print("sd" + a + "_requirementscount" + b);%>" value="<%if (d==0) {out.print("1");}else{out.print(d);}%>"></td>
		</tr>
		<% if(scs.getMealBreak() != null) {
		MealBreakType mb = scs.getMealBreak();%>
		<tr>
			<td class="left">&nbsp;</td>
			<td><input name="<% out.print("sd" + a + "_mealbreakbutton" + b);%>" type="button" id="<% out.print("sd" + a + "_mealbreakbutton" + b);%>" onClick="addMealBreak(<% out.print(a +","+ b); %>);" value="Add Meal Break" disabled></td>
			<td colspan="3">
			<table width="510" border="0" cellpadding="0" cellspacing="0" id="<% out.print("sd" + a + "_mealbreak" + b);%>">
			<tr><td><select name="<% out.print("sd" + a + "_meal" + b);%>" id="<% out.print("sd" + a + "_meal" + b);%>">
			<option value="Breakfast" <% if(mb.getMeal().equals("Breakfast")) { out.print("selected"); }%>>Breakfast</option>
			<option value="Morning Tea" <% if(mb.getMeal().equals("Morning Tea")) { out.print("selected"); }%>>Morning Tea</option>
			<option value="Lunch" <% if(mb.getMeal().equals("Lunch")) { out.print("selected"); }%>>Lunch</option>
			<option value="Afternoon Tea" <% if(mb.getMeal().equals("Afternoon Tea")) { out.print("selected"); }%>>Afternoon Tea</option>
			<option value="Dinner" <% if(mb.getMeal().equals("Dinner")) { out.print("selected"); }%>>Dinner</option>
			<option value="Supper" <% if(mb.getMeal().equals("Supper")) { out.print("selected"); }%>>Supper</option>
			
			</select></td><td><strong>Times</strong></td><td><input name="<% out.print("sd" + a + "_break" + b);%>" type="text" id="<% out.print("sd" + a + "_break" + b);%>" value="<%=mb.getBreak()%>"></td></tr>
			
			</table></td>
			<td class="right">&nbsp;</td>
		</tr>
		<%}else{%>
		<tr>
			<td class="left">&nbsp;</td>
			<td><input name="<% out.print("sd" + a + "_mealbreakbutton" + b);%>" type="button" id="<% out.print("sd" + a + "_mealbreakbutton" + b);%>" onClick="addMealBreak(<% out.print(a +","+ b); %>);" value="Add Meal Break"></td>
			<td colspan="3">
				<table width="510" border="0" cellpadding="0" cellspacing="0" id="<% out.print("sd" + a + "_mealbreak" + b);%>">
				</table>
			</td>
			<td class="right">&nbsp;</td>
		</tr>
		<%}%>
		<tr><td colspan="6" class="bottom">&nbsp;</td></tr>
	<%}//end of scenescheduletype loop - b%>
</table>											
</td>
<td class="right">&nbsp;</td>
</tr>
<tr>
	<td class="left">&nbsp;</td>
	<td colspan="4" align="left"><input name="button2" type="button" onClick="addScene(<% out.print("'scenes" +a+ "'," +a);%>);" value="Add Scene"/>
	<input name="button2" type="button" onClick="deleteScene(<% out.print("'scenes" +a+ "'," +a);%>);" value="Delete Scene"/>
	<input name="<% out.print("day_number_" + a);%>" type="hidden" id="<% out.print("day_number_" + a);%>" value="<%=a%>">
	<input name="<% out.print("table_count_" + a);%>" type="hidden" id="<% out.print("table_count_" + a);%>" value="<%=b %>"></td>
	<td class="right">&nbsp;</td>
</tr>
<tr>
	<td class="left">&nbsp;</td>
	<td width="150"><strong>End of Day Notes</strong></td>
	<td colspan="3">
	<p>
	<textarea name="<% out.print("sd" + a + "_end");%>" cols="50" id="<% out.print("sd" + a + "_end");%>"><%= sds.getEndDayNotes() %></textarea>
	</p></td>
	<td class="right">&nbsp;</td>
</tr>
<tr>
	<td class="left">&nbsp;</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td class="right">&nbsp;</td>
</tr>
<% PageTimeType pt2 = sds.getTotalScriptPages();%>
<tr>
	<td class="left">&nbsp;</td>
	<td><strong>Total Script Pages </strong></td>
	<td><input name="<% out.print("sd" + a + "_totalpages");%>" type="text" id="<% out.print("sd" + a + "_totalpages");%>" size="4" value="<%= pt2.getNumber() %>">
	<input name="<% out.print("sd" + a + "_totalpagesnum");%>" type="text" id="<% out.print("sd" + a + "_totalpagesnum");%>" size="2" value="<%= pt2.getNumerator() %>">
	<strong>/8 pgs</strong> </td>
	<td><input name="button22" type="button" onClick="calculateMod(<%=a%>);" value="Calculate"/></td>
	<td>&nbsp;</td>
	<td class="right">&nbsp;</td>
</tr>
<tr><td colspan="6" class="bottom">&nbsp;</td></tr>
<% }  //end of loop - a%>
</table>	
<%} else {%>
<table width="700" border="0" cellpadding="0" cellspacing="0" id="day1">
	<tr>
		<td width="15" class="header-left">&nbsp;</td>
		<td colspan="4" class="header-middle">Shoot Day # 
		<input name="sd1_number" type="text" id="sd1_number" size="5" value=""></td>
		<td width="15" class="header-right">&nbsp;</td>
	</tr>
	<tr>
		<td class="left">&nbsp;</td>
		<td colspan="4" align="left">&nbsp;</td>
		<td class="right">&nbsp;</td>
	</tr>
	<tr>
		<td class="left">&nbsp;</td>
		<td width="150" align="left"><strong>Shoot Day Date</strong></td>
		<td width="185" align="left"><input name="sd1_date" type="text" id="sd1_date" value=""></td>
		<td width="150" align="left"><strong>Shoot Day Weekday</strong></td>
		<td width="185" align="left"><input name="sd1_weekday" type="text" id="sd1_weekday" value=""></td>
		<td class="right">&nbsp;</td>
	</tr>
	<tr>
		<td class="left">&nbsp;</td>
		<td width="150" align="left"><strong>Crew Call </strong></td>
		<td width="185" align="left"><input name="sd1_crew" type="text" id="sd1_crew"></td>
		<td width="150" align="left"><strong>Travel To Loc </strong></td>
		<td width="185" align="left"><input name="sd1_traveltoloc" type="text" id="sd1_traveltoloc"></td>
		<td class="right">&nbsp;</td>
	</tr>
	<tr>
		<td class="left">&nbsp;</td>
		<td align="left"><strong>Bump In</strong></td>
		<td colspan="3" align="left"><input name="sd1_bumpin" type="text" id="sd1_bumpin" size="8" value=""></td>
		<td class="right">&nbsp;</td>
	</tr>
	<tr>
		<td class="left">&nbsp;</td>
		<td width="150" align="left"><strong>Start of Day Notes </strong></td>
		<td colspan="3" align="left"><input name="sd1_start" type="text" id="sd1_start" size="73" value=""></td>
		<td class="right">&nbsp;</td>
	</tr>
	<tr>
	<td class="left">&nbsp;</td>
	<td colspan="4" align="left">
		<table width="670" border="0" cellpadding="0" cellspacing="0" id="scenes1">
		<tr><td colspan="6" class="top">&nbsp;</td></tr>
		<tr>
			<td width="15" class="left">&nbsp;</td>
			<td colspan="4" valign="top">&nbsp;</td>
			<td width="15" class="right">&nbsp;</td>
		</tr>
		<tr>
			<td width="15" height="30" class="left">&nbsp;</td>
			<td width="160" height="30" valign="top"><strong>Scene Number</strong></td>
			<td height="30" colspan="3" valign="top"><input name="sd1_scene1" type="text" id="sd1_scene" value=""></td>
			<td height="30" class="right">&nbsp;</td>
		</tr>
		<tr>
			<td height="30" class="left">&nbsp;</td>
			<td height="30" valign="top"><strong>Set Name</strong></td>
			<td height="30" valign="top"><input name="sd1_set1" type="text" id="sd1_set1" value=""></td>
			<td height="30" valign="top"><strong>Synopsis</strong></td>
			<td height="30" valign="top"><input name="sd1_synopsis1" type="text" id="sd1_synopsis1" value=""></td>
			<td height="30" class="right">&nbsp;</td>
		</tr>
		<tr>
			<td width="15" height="30" class="left">&nbsp;</td>
			<td width="160" height="30" valign="top"><strong>Location ID </strong></td>
			<td width="160" height="30" valign="top"><input name="sd1_locationID1" type="text" id="sd1_locationID1" value=""></td>
			<td width="160" height="30" valign="top"><strong>Address</strong></td>
			<td width="160" height="30" valign="top"><input name="sd1_address1" type="text" id="sd1_address" value=""></td>
			<td height="30" class="right">&nbsp;</td>
		</tr>
		<tr>
			<td width="15" height="30" class="left">&nbsp;</td>
			<td width="160" height="30" valign="top"><strong>Interior</strong><input name="sd1_intext1" type="radio" value="int"></td>
			<td width="160" height="30" valign="top"><strong>Exterior</strong><input name="sd1_intext1" type="radio" value="ext"></td>
			<td width="160" height="30" valign="top"><strong>Day</strong><input name="sd1_daynight1" type="radio" value="day"></td>
			<td width="160" height="30" valign="top"><strong>Night</strong><input name="sd1_daynight1" type="radio" value="night"></td>
			<td height="30" class="right">&nbsp;</td>
		</tr>
		<tr>
			<td height="30" class="left">&nbsp;</td>
			<td height="30" valign="top"><strong>No. of Script Pages</strong></td>
			<td height="30" valign="top">
			<input name="sd1_pages1" type="text" id="sd1_pages1" size="4" value="">
			<input name="sd1_pagesnum1" type="text" id="sd1_pagesnum1" size="2" value="">
			<strong>/8 pgs </strong></td>
			<td height="30" valign="top"><strong>Est. Shoot Times </strong></td>
			<td height="30" valign="top"><input name="sd1_shoottimes1" type="text" id="sd1_shoottimes1" value=""></td>
			<td height="30" class="right">&nbsp;</td>
		</tr>
		<tr>
			<td height="30" class="left">&nbsp;</td>
			<td height="30" valign="top"><strong>Est. Script Timing </strong></td>
			<td height="30" colspan="3" valign="top"><input name="sd1_scripttime1" type="text" id="sd1_scripttime1" value=""></td>
			<td height="30" class="right">&nbsp;</td>
		</tr>
		<tr>
			<td height="30" rowspan="2" class="left">&nbsp;</td>
			<td height="30" rowspan="2" valign="top"><strong>Characters</strong></td>
			<td height="15" colspan="3" valign="top">
			<table width="510" border="0" cellpadding="0" cellspacing="0" id="sd1_characters1">
				<tr><td><input name="sd1_charactername1_1" type="text" id="sd1_charactername1_1" value=""></td></tr>
			</table>													  
			</td>
			<td height="30" rowspan="2" class="right">&nbsp;</td>
		</tr>
		<tr><td height="15" colspan="3" valign="top"><input name="button3" type="button" onClick="addCharacterRow(1,1);" value="Add Character"/><input name="sd1_charactercount1" type="hidden" id="sd1_charactercount1" value="1"></td></tr>
		<tr>
			<td width="15" height="30" rowspan="2" class="left">&nbsp;</td>
			<td width="160" height="30" rowspan="2" valign="top"><strong>Set Requirements </strong></td>
			<td height="15" colspan="3" valign="top">
				<table width="510" border="0" cellpadding="0" cellspacing="0" id="sd1_requirements1"> 
					<tr valign="top">
					<td><select name="sd1_items1_1" id="sd1_items1_1">
					  <option value="Background Actors">Background Actors</option>
					  <option value="Stunts">Stunts</option>
					  <option value="Vehicles">Vehicles</option>
					  <option value="Props">Props</option>
					  <option value="Camera">Camera</option>
					  <option value="Special Effects">Special Effects</option>
					  <option value="Wardrobe">Wardrobe</option>
					  <option value="Makeup/Hair">Makeup/Hair</option>
					  <option value="Animals">Animals</option>
					  <option value="Animal Wrangler">Animal Wrangler</option>
					  <option value="Music">Music</option>
					  <option value="Sound">Sound</option>
					  <option value="Art Department">Art Department</option>
					  <option value="Set Dressing">Set Dressing</option>
					  <option value="Greenery">Greenery</option>
					  <option value="Special Equipment">Special Equipment</option>
					  <option value="Security">Security</option>
					  <option value="Additional Labour">Additional Labour</option>
					  <option value="Visual Effects">Visual Effects</option>
					  <option value="Mechanical Effects">Mechanical Effects</option>
					  <option value="Miscellaneous">Miscellaneous</option>
					  <option value="Notes">Notes</option>
					</select></td>
					<td><textarea name="sd1_requirements1_1" id="sd1_requirements1_1" cols="20"></textarea></td>
					</tr>
				</table>														
			</td>
			<td height="30" rowspan="2" class="right">&nbsp;</td>
		</tr>
		<tr>
			<td height="15" colspan="3" valign="top"><input name="requirements1" type="button" id="requirements1" onClick="addRequirementsRow(1,1);" value="Add Set Requirements">
			<input name="sd1_requirementscount1" type="hidden" id="sd1_requirementscount1" value="1"></td>
		</tr>
		<tr>
			<td class="left">&nbsp;</td>
			<td><input name="sd1_mealbreakbutton1" type="button" id="sd1_mealbreakbutton1" onClick="addMealBreak(1,1);" value="Add Meal Break"></td>
			<td colspan="3">
				<table width="510" border="0" cellpadding="0" cellspacing="0" id="sd1_mealbreak1">
				
				</table></td>
			<td class="right">&nbsp;</td>
		</tr>
		<tr><td colspan="6" class="bottom">&nbsp;</td></tr>
		</table>											
	</td>
	<td class="right">&nbsp;</td>
	</tr>
	<tr>
	<td class="left">&nbsp;</td>
	<td colspan="4" align="left"><input name="button2" type="button" onClick="addScene('scenes1',1);" value="Add Scene"/>
	  <input name="button23" type="button" onClick="deleteScene('scenes1',1);" value="Delete Scene"/>
	<input name="day_number_1" type="hidden" id="day_number_1" value="1">
	<input name="table_count_1" type="hidden" id="table_count_1" value="1"></td>
	<td class="right">&nbsp;</td>
	</tr>
	<tr>
	<td class="left">&nbsp;</td>
	<td width="150"><strong>End of Day Notes</strong></td>
	<td colspan="3">
	<p>
	<textarea name="sd1_end" cols="50" id="sd1_end"></textarea>
	</p></td>
	<td class="right">&nbsp;</td>
	</tr>
	<tr>
	<td class="left">&nbsp;</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td class="right">&nbsp;</td>
	</tr>
	<tr>
	<td class="left">&nbsp;</td>
	<td><strong>Total Script Pages </strong></td>
	<td><input name="sd1_totalpages" type="text" id="sd1_totalpages" size="4" value="">
	<input name="sd1_totalpagesnum" type="text" id="sd1_totalpagesnum" size="2" value="">
	<strong>/8 pgs</strong> </td>
	<td><input name="button22" type="button" onClick="calculateMod(1);" value="Calculate"/></td>
	<td>&nbsp;</td>
	<td class="right">&nbsp;</td>
	</tr>
	<tr><td colspan="6" class="bottom">&nbsp;</td></tr>
	</table>	
<%}%>		


</td>
</tr>
</table>					
</td>
</tr>
</table>
<p align="center">
<input name="button" type="button" onClick="addShootingDay();" value="Add Shooting Day"/>
<input type="button" value="Print"  onclick="window.print()">
<input type="submit" name="Save" value="Save">
<input type="submit" name="Submission" value="Submission">
<input type="hidden" name="workItemID" id="workItemID">
<input type="hidden" name="userID" id="userID">
<input type="hidden" name="sessionHandle" id="sessionHandle">
<input type="hidden" name="JSESSIONID" id="JSESSIONID">
<input type="hidden" name="submit" id="submit">

<input name="days" type="hidden" id="days" value="<%if (a==0) {out.print("1");}else{out.print(a);}%>">
</p>
</form>		</td>
<td width="14" class="background_right">&nbsp;</td></tr>
<tr><td colspan="3">
<!-- LOAD -->
<form method="post" action="Fill_Out_Shooting_Schedule.jsp?formType=load&workItemID=<%= request.getParameter("workItemID") %>&userID=<%= request.getParameter("userID") %>&sessionHandle=<%= request.getParameter("sessionHandle") %>&JSESSIONID=<%= request.getParameter("JSESSIONID") %>&submit=htmlForm" name="upform" enctype="MULTIPART/FORM-DATA">
<table width="60%" border="0" cellspacing="1" cellpadding="1" align="center" class="style1">
<tr>
<td align="left"><strong>Select a file to upload :</strong></td>
</tr>
<tr>
<td align="left">
<input type="file" name="uploadfile" size="50">
</td>
</tr>
<tr>
<td align="left">
<input type="hidden" name="todo" value="upload">
<input type="submit" name="Submit" value="Upload">
<input type="reset" name="Reset" value="Cancel">
</td>
</tr>
</table>
<br>
<br>
</form>
<!-- END LOAD -->
</td></tr>
<tr>
<td colspan="3" class="background_bottom">&nbsp;</td>
</tr>
</table>

<%
if(request.getParameter("Submission") != null){
int total_scenes = 0;
	int total_pages = 0;
	int total_pagesnum = 0;
	String array[] = {"","",""}; 
	int original_timing_array[] = {0,0,0}; 
	String original_timing;
	int days_count = Integer.parseInt(request.getParameter("days"));
	ShootingScheduleType sst = new ShootingScheduleType();
	//start of singledayschedule loop
	for(int current_day=1; current_day<=days_count; current_day ++) {
		SingleDayScheduleType sdst = new SingleDayScheduleType();
		sdst.setShootDayNo(new BigInteger(request.getParameter("sd"+current_day+"_number")));
		sdst.setShootDayDate(XMLGregorianCalendarImpl.parse(request.getParameter("sd"+current_day+"_date")));
		sdst.setShootDayWeekday(request.getParameter("sd"+current_day+"_weekday"));
		sdst.setCrewCall(XMLGregorianCalendarImpl.parse(request.getParameter("sd"+current_day+"_crew")));
		sdst.setTravelToLoc(XMLGregorianCalendarImpl.parse(request.getParameter("sd"+current_day+"_traveltoloc")));
		if(!(request.getParameter("sd"+current_day+"_bumpin").equals(""))) {
			sdst.setBumpIn(XMLGregorianCalendarImpl.parse(request.getParameter("sd"+current_day+"_bumpin")));
		}
		if(!(request.getParameter("sd"+current_day+"_start").equals(""))) {
			sdst.setStartDayNotes(request.getParameter("sd"+current_day+"_start"));
		}
		
		int table_count = Integer.parseInt(request.getParameter("table_count_"+current_day));
		total_scenes += table_count;
		//start of sceneschedule loop
		for(int current_table=1; current_table<=table_count; current_table ++) {
			SceneScheduleType scst = new SceneScheduleType();
			scst.setScene(request.getParameter("sd"+current_day+"_scene"+current_table));
			PageTimeType ptt = new PageTimeType();
			ptt.setNumber(new BigInteger(request.getParameter("sd"+current_day+"_pages"+current_table)));
			ptt.setNumerator(new BigInteger(request.getParameter("sd"+current_day+"_pagesnum"+current_table)));
			scst.setPageTime(ptt);
			scst.setDN(request.getParameter("sd"+current_day+"_daynight"+current_table));
			scst.setINEX(request.getParameter("sd"+current_day+"_intext"+current_table));
			scst.setLocationID(request.getParameter("sd"+current_day+"_locationID"+current_table));
			scst.setSet(request.getParameter("sd"+current_day+"_set"+current_table));
			scst.setAddress(request.getParameter("sd"+current_day+"_address"+current_table));
			scst.setSynopsis(request.getParameter("sd"+current_day+"_synopsis"+current_table));
			
			int character_count = Integer.parseInt(request.getParameter("sd"+current_day+"_charactercount"+current_table));
			CharactersType ct = new CharactersType();
			//start of character loop
			for(int current_character=1; current_character<=character_count; current_character ++) {
				ct.getCharacter().add(request.getParameter("sd"+current_day+"_charactername"+current_table+"_"+current_character));
			}//end of character loop
			scst.setCharacters(ct);
			
			scst.setEstShootTimes(request.getParameter("sd"+current_day+"_shoottimes"+current_table));
			scst.setEstScriptTiming(XMLGregorianCalendarImpl.parse(request.getParameter("sd"+current_day+"_scripttime"+current_table)));
			original_timing = request.getParameter("sd"+current_day+"_scripttime"+current_table);
			array = original_timing.split(":");
			original_timing_array[0] += Integer.parseInt(array[0]);
			original_timing_array[1] += Integer.parseInt(array[1]);
			original_timing_array[2] += Integer.parseInt(array[2]);
			
			int requirements_count = Integer.parseInt(request.getParameter("sd"+current_day+"_requirementscount"+current_table));
			SetRequirementsType srt = new SetRequirementsType();
			//start of setrequirements loop
			for(int current_requirements=1; current_requirements<=requirements_count; current_requirements ++) {
				SingleEntryType set = new SingleEntryType();
				set.setItem(request.getParameter("sd"+current_day+"_items"+current_table+"_"+current_requirements));
				set.setRequirements(request.getParameter("sd"+current_day+"_requirements"+current_table+"_"+current_requirements));
				srt.getSingleEntry().add(set);
			}//end of setrequirements loop
			
			scst.setSetRequirements(srt);
			if (request.getParameter("sd"+current_day+"_meal"+current_table) != null) {
				MealBreakType mbt = new MealBreakType();
				mbt.setMeal(request.getParameter("sd"+current_day+"_meal"+current_table));				
				mbt.setBreak(request.getParameter("sd"+current_day+"_break"+current_table));
				scst.setMealBreak(mbt);
			}
			sdst.getSceneSchedule().add(scst);
		}//end of sceneschedule loop
		sdst.setEndDayNotes(request.getParameter("sd"+current_day+"_end"));
		total_pages += Integer.parseInt(request.getParameter("sd"+current_day+"_totalpages"));
		total_pagesnum += Integer.parseInt(request.getParameter("sd"+current_day+"_totalpagesnum"));
		PageTimeType ptt2 = new PageTimeType();
		ptt2.setNumber(new BigInteger(request.getParameter("sd"+current_day+"_totalpages")));
		ptt2.setNumerator(new BigInteger(request.getParameter("sd"+current_day+"_totalpagesnum")));
		sdst.setTotalScriptPages(ptt2);
		sst.getSingleDaySchedule().add(sdst);
	}// end of singledayschedule loop
		
		
	sst.setLastUpdatedDate(XMLGregorianCalendarImpl.parse(request.getParameter("last_updated")));
	sst.setDirector(request.getParameter("director"));
	sst.setProducer(request.getParameter("producer"));
	sst.setStartDate(XMLGregorianCalendarImpl.parse(request.getParameter("start_date")));
	sst.setScheduledFinish(XMLGregorianCalendarImpl.parse(request.getParameter("scheduled_finish")));
	if(!(request.getParameter("revised_finish").equals(""))){
		sst.setRevisedFinish(XMLGregorianCalendarImpl.parse(request.getParameter("revised_finish")));
	}
	sst.setScheduledShootingDays(new BigInteger(request.getParameter("scheduled_shooting_days")));
	
	
	iss.setProduction(request.getParameter("production"));
	iss.setShootingSchedule(sst); 
	String total_scenes_string = Integer.toString(total_scenes);
	iss.setTotalScenes(new BigInteger(total_scenes_string));
	
	int final_pages = total_pages + (int)Math.floor(total_pagesnum / 8);
	int final_pagesnum = total_pagesnum % 8;
	String final_pages_string = Integer.toString(final_pages);
	String final_pagesnum_string = Integer.toString(final_pagesnum);
		PageTimeType ptt3 = new PageTimeType();
		ptt3.setNumber(new BigInteger(final_pages_string));
		ptt3.setNumerator(new BigInteger(final_pagesnum_string));
	iss.setTotalPageTime(ptt3);
	
	original_timing_array[1] = original_timing_array[1] + (int)Math.floor(original_timing_array[2] / 60);
	original_timing_array[2] = original_timing_array[2] % 60;
	
	original_timing_array[0] = original_timing_array[0] + (int)Math.floor(original_timing_array[1] / 60);
	original_timing_array[1] = original_timing_array[1] % 60;
	
	array[0] = Integer.toString(original_timing_array[0]);
	if (array[0].length() <= 1) {
		array[0] = "0" + array[0];
	}
	
	array[1] = Integer.toString(original_timing_array[1]);
	if (array[1].length() <= 1) {
		array[1] = "0" + array[1];
	}
	
	array[2] = Integer.toString(original_timing_array[2]);
	if (array[2].length() <= 1) {
		array[2] = "0" + array[2];
	}
	
	
	original_timing = array[0] + ":" + array[1] + ":" + array[2];
	iss.setOriginalTiming(XMLGregorianCalendarImpl.parse(original_timing));
	
	
	Marshaller m = jc.createMarshaller();
    m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
    File f = new File("./backup/ShootingSchedule_"+new SimpleDateFormat("dd-MM-yyyy_hh-mm-ss").format(new Date())+".xml");
    m.marshal( issElement,  f);//output to file
    
	ByteArrayOutputStream xmlOS = new ByteArrayOutputStream();
    m.marshal(issElement, xmlOS);//out to ByteArray
	String result = xmlOS.toString().replaceAll("ns2:", "");
    
    String workItemID = new String(request.getParameter("workItemID"));
    String sessionHandle = new String(request.getParameter("sessionHandle"));
    String userID = new String(request.getParameter("userID"));
    String submit = new String(request.getParameter("submit"));
  
    session.setAttribute("inputData", result);//to be possibly replaced
    response.sendRedirect(response.encodeURL(getServletContext().getInitParameter("HTMLForms")+"/yawlFormServlet?workItemID="+workItemID+"&sessionHandle="+sessionHandle+"&userID="+userID+"&submit="+submit));
    return;
}

else if(request.getParameter("Save") != null){
int total_scenes = 0;
	int total_pages = 0;
	int total_pagesnum = 0;
	String array[] = {"","",""}; 
	int original_timing_array[] = {0,0,0}; 
	String original_timing;
	int days_count = Integer.parseInt(request.getParameter("days"));
	ShootingScheduleType sst = new ShootingScheduleType();
	//start of singledayschedule loop
	for(int current_day=1; current_day<=days_count; current_day ++) {
		SingleDayScheduleType sdst = new SingleDayScheduleType();
		sdst.setShootDayNo(new BigInteger(request.getParameter("sd"+current_day+"_number")));
		sdst.setShootDayDate(XMLGregorianCalendarImpl.parse(request.getParameter("sd"+current_day+"_date")));
		sdst.setShootDayWeekday(request.getParameter("sd"+current_day+"_weekday"));
		sdst.setCrewCall(XMLGregorianCalendarImpl.parse(request.getParameter("sd"+current_day+"_crew")));
		sdst.setTravelToLoc(XMLGregorianCalendarImpl.parse(request.getParameter("sd"+current_day+"_traveltoloc")));
		if(!(request.getParameter("sd"+current_day+"_bumpin").equals(""))) {
			sdst.setBumpIn(XMLGregorianCalendarImpl.parse(request.getParameter("sd"+current_day+"_bumpin")));
		}
		if(!(request.getParameter("sd"+current_day+"_start").equals(""))) {
			sdst.setStartDayNotes(request.getParameter("sd"+current_day+"_start"));
		}
		
		int table_count = Integer.parseInt(request.getParameter("table_count_"+current_day));
		total_scenes += table_count;
		//start of sceneschedule loop
		for(int current_table=1; current_table<=table_count; current_table ++) {
			SceneScheduleType scst = new SceneScheduleType();
			scst.setScene(request.getParameter("sd"+current_day+"_scene"+current_table));
			PageTimeType ptt = new PageTimeType();
			ptt.setNumber(new BigInteger(request.getParameter("sd"+current_day+"_pages"+current_table)));
			ptt.setNumerator(new BigInteger(request.getParameter("sd"+current_day+"_pagesnum"+current_table)));
			scst.setPageTime(ptt);
			scst.setDN(request.getParameter("sd"+current_day+"_daynight"+current_table));
			scst.setINEX(request.getParameter("sd"+current_day+"_intext"+current_table));
			scst.setLocationID(request.getParameter("sd"+current_day+"_locationID"+current_table));
			scst.setSet(request.getParameter("sd"+current_day+"_set"+current_table));
			scst.setAddress(request.getParameter("sd"+current_day+"_address"+current_table));
			scst.setSynopsis(request.getParameter("sd"+current_day+"_synopsis"+current_table));
			
			int character_count = Integer.parseInt(request.getParameter("sd"+current_day+"_charactercount"+current_table));
			CharactersType ct = new CharactersType();
			//start of character loop
			for(int current_character=1; current_character<=character_count; current_character ++) {
				ct.getCharacter().add(request.getParameter("sd"+current_day+"_charactername"+current_table+"_"+current_character));
			}//end of character loop
			scst.setCharacters(ct);
			
			scst.setEstShootTimes(request.getParameter("sd"+current_day+"_shoottimes"+current_table));
			scst.setEstScriptTiming(XMLGregorianCalendarImpl.parse(request.getParameter("sd"+current_day+"_scripttime"+current_table)));
			original_timing = request.getParameter("sd"+current_day+"_scripttime"+current_table);
			array = original_timing.split(":");
			original_timing_array[0] += Integer.parseInt(array[0]);
			original_timing_array[1] += Integer.parseInt(array[1]);
			original_timing_array[2] += Integer.parseInt(array[2]);
			
			int requirements_count = Integer.parseInt(request.getParameter("sd"+current_day+"_requirementscount"+current_table));
			SetRequirementsType srt = new SetRequirementsType();
			//start of setrequirements loop
			for(int current_requirements=1; current_requirements<=requirements_count; current_requirements ++) {
				SingleEntryType set = new SingleEntryType();
				set.setItem(request.getParameter("sd"+current_day+"_items"+current_table+"_"+current_requirements));
				set.setRequirements(request.getParameter("sd"+current_day+"_requirements"+current_table+"_"+current_requirements));
				srt.getSingleEntry().add(set);
			}//end of setrequirements loop
			
			scst.setSetRequirements(srt);
			if (request.getParameter("sd"+current_day+"_meal"+current_table) != null) {
				MealBreakType mbt = new MealBreakType();
				mbt.setMeal(request.getParameter("sd"+current_day+"_meal"+current_table));				
				mbt.setBreak(request.getParameter("sd"+current_day+"_break"+current_table));
				scst.setMealBreak(mbt);
			}
			sdst.getSceneSchedule().add(scst);
		}//end of sceneschedule loop
		sdst.setEndDayNotes(request.getParameter("sd"+current_day+"_end"));
		total_pages += Integer.parseInt(request.getParameter("sd"+current_day+"_totalpages"));
		total_pagesnum += Integer.parseInt(request.getParameter("sd"+current_day+"_totalpagesnum"));
		PageTimeType ptt2 = new PageTimeType();
		ptt2.setNumber(new BigInteger(request.getParameter("sd"+current_day+"_totalpages")));
		ptt2.setNumerator(new BigInteger(request.getParameter("sd"+current_day+"_totalpagesnum")));
		sdst.setTotalScriptPages(ptt2);
		sst.getSingleDaySchedule().add(sdst);
	}// end of singledayschedule loop
		
		
	sst.setLastUpdatedDate(XMLGregorianCalendarImpl.parse(request.getParameter("last_updated")));
	sst.setDirector(request.getParameter("director"));
	sst.setProducer(request.getParameter("producer"));
	sst.setStartDate(XMLGregorianCalendarImpl.parse(request.getParameter("start_date")));
	sst.setScheduledFinish(XMLGregorianCalendarImpl.parse(request.getParameter("scheduled_finish")));
	if(!(request.getParameter("revised_finish").equals(""))){
		sst.setRevisedFinish(XMLGregorianCalendarImpl.parse(request.getParameter("revised_finish")));
	}
	sst.setScheduledShootingDays(new BigInteger(request.getParameter("scheduled_shooting_days")));
	
	
	iss.setProduction(request.getParameter("production"));
	iss.setShootingSchedule(sst); 
	String total_scenes_string = Integer.toString(total_scenes);
	iss.setTotalScenes(new BigInteger(total_scenes_string));
	
	int final_pages = total_pages + (int)Math.floor(total_pagesnum / 8);
	int final_pagesnum = total_pagesnum % 8;
	String final_pages_string = Integer.toString(final_pages);
	String final_pagesnum_string = Integer.toString(final_pagesnum);
		PageTimeType ptt3 = new PageTimeType();
		ptt3.setNumber(new BigInteger(final_pages_string));
		ptt3.setNumerator(new BigInteger(final_pagesnum_string));
	iss.setTotalPageTime(ptt3);
	
	original_timing_array[1] = original_timing_array[1] + (int)Math.floor(original_timing_array[2] / 60);
	original_timing_array[2] = original_timing_array[2] % 60;
	
	original_timing_array[0] = original_timing_array[0] + (int)Math.floor(original_timing_array[1] / 60);
	original_timing_array[1] = original_timing_array[1] % 60;
	
	
	array[0] = Integer.toString(original_timing_array[0]);
	if (array[0].length() <= 1) {
		array[0] = "0" + array[0];
	}
	
	array[1] = Integer.toString(original_timing_array[1]);
	if (array[1].length() <= 1) {
		array[1] = "0" + array[1];
	}
	
	array[2] = Integer.toString(original_timing_array[2]);
	if (array[2].length() <= 1) {
		array[2] = "0" + array[2];
	}
	
	
	original_timing = array[0] + ":" + array[1] + ":" + array[2];
	iss.setOriginalTiming(XMLGregorianCalendarImpl.parse(original_timing));
	
	Marshaller m = jc.createMarshaller();
	m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
	
	ByteArrayOutputStream xmlOS = new ByteArrayOutputStream();
	m.marshal(issElement, xmlOS);//out to ByteArray

	response.setHeader("Content-Disposition", "attachment;filename=\"ShootingSchedule_"+new SimpleDateFormat("dd-MM-yyyy_hh-mm-ss").format(new Date())+"_l.xml\";");
	response.setHeader("Content-Type", "text/xml");

	ServletOutputStream outs = response.getOutputStream();
	xmlOS.writeTo(outs);
	outs.close();
}
%>

</body>
</html>