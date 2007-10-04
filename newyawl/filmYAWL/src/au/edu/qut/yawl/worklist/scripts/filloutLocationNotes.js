var count = 1;
//This is the number of rows allocated for the header part of the cast table.
var headerSize = 0;
var footerSize = 0;
var headerAndFooterSize = headerSize + footerSize;
function deleteEntry() {
    var table = document.getElementById("locations");
    var rows = table.rows.length;
	count = document.getElementById("count").value;
    if (rows > headerAndFooterSize) {
        //delete from the bottom, removing 1 for the 0-based index.
        table.deleteRow(rows-(footerSize+1));
		table.deleteRow(rows-(footerSize+2));
		table.deleteRow(rows-(footerSize+3));
		table.deleteRow(rows-(footerSize+4));
		table.deleteRow(rows-(footerSize+5));
		table.deleteRow(rows-(footerSize+6));
		table.deleteRow(rows-(footerSize+7));
		table.deleteRow(rows-(footerSize+8));
		table.deleteRow(rows-(footerSize+9));
		table.deleteRow(rows-(footerSize+10));
		table.deleteRow(rows-(footerSize+11));
        if (count > 0) {
            document.getElementById("count").value = --count;
        }

        if (count == 0) {
            //add an empty row after the last row has been deleted.
            //this allows all data-containing rows to be deleted and for there to be a single empty row
            //at any point in time.
            createEntry();
        }
    }
}

function createEntry(){
  
	var table = document.getElementById("locations");
	
	count = document.getElementById("count").value;
	count ++;
	document.getElementById("count").value = count;
	
	// header information
	var entry_top_row = table.insertRow(table.rows.length-footerSize);
	var entry_top_cell1 = entry_top_row.insertCell(0);
	var entry_top_cell2 = entry_top_row.insertCell(1);
	var entry_top_cell3 = entry_top_row.insertCell(2);
	entry_top_cell1.className = "header-left";
	entry_top_cell2.appendChild(document.createTextNode("Location " + count));
	entry_top_cell2.colSpan = "4";
	entry_top_cell2.className = "header-middle";
	entry_top_cell3.className = "header-right";
	entry_top_cell1.setAttribute("width", "15");
	entry_top_cell3.setAttribute("width", "15");
	//first row - ID and Name
	var row1 = table.insertRow(table.rows.length-footerSize);
	var left1CELL = row1.insertCell(0);
	var locationidLABEL = row1.insertCell(1);
	var locationidCELL = row1.insertCell(2);
	var locationnameLABEL = row1.insertCell(3);
	var locationnameCELL = row1.insertCell(4);
	var right1CELL = row1.insertCell(5);
	left1CELL.className = "left";
    left1CELL.appendChild(document.createTextNode("\u00a0"));
	right1CELL.className = "right";
    right1CELL.appendChild(document.createTextNode("\u00a0"));
	locationidLABEL.appendChild(createBoldLabel("Location ID"));
	locationidLABEL.setAttribute("width", "150");
	locationidCELL.appendChild(createAnyTextTextBox("location_ID_" + count, 25, "", "Enter Location ID. [String Value]"));
	locationnameLABEL.appendChild(createBoldLabel("Location Name"));
	locationnameCELL.appendChild(createAnyTextTextBox("location_name_" + count, 25, "", "Enter Location Name. [String Value]"));
	
	//second row - Contact and Number
	var row2 = table.insertRow(table.rows.length-footerSize);
	var left2CELL = row2.insertCell(0);
	var contactLABEL = row2.insertCell(1);
	var contactCELL = row2.insertCell(2);
	var contactnoLABEL = row2.insertCell(3);
	var contactnoCELL = row2.insertCell(4);
	var right2CELL = row2.insertCell(5);
	left2CELL.className = "left";
    left2CELL.appendChild(document.createTextNode("\u00a0"));
	right2CELL.className = "right";
    right2CELL.appendChild(document.createTextNode("\u00a0"));
	contactLABEL.appendChild(createBoldLabel("Contact"));
	contactLABEL.setAttribute("width", "150");
	contactCELL.appendChild(createAnyTextTextBox("contact_" + count, 25,"", "Enter Contact Person. [String Value]"));
	contactnoLABEL.appendChild(createBoldLabel("Contact No."));
	contactnoCELL.appendChild(createAnyTextTextBox("phone_" + count, 25,"", "Enter Contact Number. [String Value]"));
	
	//third row - UBD
	var row3 = table.insertRow(table.rows.length-footerSize);
	var left3CELL = row3.insertCell(0);
	var ubdLABEL = row3.insertCell(1);
	var ubdCELL = row3.insertCell(2);
	var right3CELL = row3.insertCell(3);
	left3CELL.className = "left";
    left3CELL.appendChild(document.createTextNode("\u00a0"));
	right3CELL.className = "right";
    right3CELL.appendChild(document.createTextNode("\u00a0"));
	ubdCELL.colSpan = "3";
	ubdLABEL.appendChild(createBoldLabel("UBD Map Ref"));
	ubdCELL.appendChild(createAnyTextTextBox("ubd_" + count, 25, "", "Enter UBD reference. [String Value]"));
	
	//fourth row - Address
	var row4 = table.insertRow(table.rows.length-footerSize);
	var left4CELL = row4.insertCell(0);
	var addressLABEL = row4.insertCell(1);
	var addressCELL = row4.insertCell(2);
	var right4CELL = row4.insertCell(3);
	left4CELL.className = "left";
    left4CELL.appendChild(document.createTextNode("\u00a0"));
	right4CELL.className = "right";
    right4CELL.appendChild(document.createTextNode("\u00a0"));
	addressCELL.colSpan = "3";
	addressLABEL.appendChild(createBoldLabel("Address"));
	addressLABEL.setAttribute("width", "150");
	addressCELL.appendChild(createAnyTextTextBox("address_" + count, 80, "", "Enter Location Address. [String Value]"));
	
	//fifth row - Police
	var row5 = table.insertRow(table.rows.length-footerSize);
	var left5CELL = row5.insertCell(0);
	var policeLABEL = row5.insertCell(1);
	var policeCELL = row5.insertCell(2);
	var right5CELL = row5.insertCell(3);
	left5CELL.className = "left";
    left5CELL.appendChild(document.createTextNode("\u00a0"));
	right5CELL.className = "right";
    right5CELL.appendChild(document.createTextNode("\u00a0"));
	policeCELL.colSpan = "3";
	policeLABEL.appendChild(createBoldLabel("Police"));
	policeLABEL.setAttribute("width", "150");
	policeCELL.appendChild(createAnyTextTextBox("police_" + count, 80,"", "Enter Police Details. [String Value]"));

	
	//sixth row - Hospital
	var row6 = table.insertRow(table.rows.length-footerSize);
	var left6CELL = row6.insertCell(0);
	var hospitalLABEL = row6.insertCell(1);
	var hospitalCELL = row6.insertCell(2);
	var right6CELL = row6.insertCell(3);
	left6CELL.className = "left";
    left6CELL.appendChild(document.createTextNode("\u00a0"));
	right6CELL.className = "right";
    right6CELL.appendChild(document.createTextNode("\u00a0"));
	hospitalCELL.colSpan= "3";
	hospitalLABEL.appendChild(createBoldLabel("Hospital"));
	hospitalCELL.appendChild(createAnyTextTextBox("hospital_" + count, 80,"", "Enter Hospital Details. [String Value]"));

	//seventh row - Parking
	var row7 = table.insertRow(table.rows.length-footerSize);
	var left7CELL = row7.insertCell(0);
	var parkingLABEL = row7.insertCell(1);
	var parkingCELL = row7.insertCell(2);
	var right7CELL = row7.insertCell(3);
	left7CELL.className = "left";
    left7CELL.appendChild(document.createTextNode("\u00a0"));
	right7CELL.className = "right";
    right7CELL.appendChild(document.createTextNode("\u00a0"));
	parkingCELL.colSpan = "3";
	parkingLABEL.appendChild(createBoldLabel("Parking"));
	parkingLABEL.setAttribute("width", "150");
	parkingCELL.appendChild(createTextArea("parking_" + count, 70, 0, "", "Enter Parking Instructions. [String Value]"));
	
	//eighth row - Unit
	var row8 = table.insertRow(table.rows.length-footerSize);
	var left8CELL = row8.insertCell(0);
	var unitLABEL = row8.insertCell(1);
	var unitCELL = row8.insertCell(2);
	var right8CELL = row8.insertCell(3);
	left8CELL.className = "left";
    left8CELL.appendChild(document.createTextNode("\u00a0"));
	right8CELL.className = "right";
    right8CELL.appendChild(document.createTextNode("\u00a0"));
	unitCELL.colSpan = "3";
	unitLABEL.appendChild(createBoldLabel("Unit"));
	unitLABEL.setAttribute("width", "150");
	unitCELL.appendChild(createTextArea("unit_" + count, 70, 0, "", "Enter Unit Instructions. [String Value]"));
	
	//ninth row - notes
	var row9 =table.insertRow(table.rows.length-footerSize);
	var left9CELL = row9.insertCell(0);
	var locationnotesLABEL = row9.insertCell(1);
	var locationnotesCELL = row9.insertCell(2);
	var right9CELL = row9.insertCell(3);
	left9CELL.className = "left";
    left9CELL.appendChild(document.createTextNode("\u00a0"));
	left9CELL.setAttribute("width", "15");
	right9CELL.className = "right";
    right9CELL.appendChild(document.createTextNode("\u00a0"));
	right9CELL.setAttribute("width", "15");
	locationnotesCELL.colSpan = "3";
	locationnotesLABEL.appendChild(createBoldLabel("Location Notes"));
	locationnotesLABEL.setAttribute("width", "150");
	locationnotesCELL.appendChild(createTextArea("notes_" + count, 70, 5,"", "Enter Location Notes. [String Value]"));
	
	//bottom information
	var entry_bottom_row = table.insertRow(table.rows.length-footerSize);
	var entry_bottom_cell = entry_bottom_row.insertCell(0);
	entry_bottom_cell.colSpan = "6";
	entry_bottom_cell.className = "bottom";
	entry_bottom_cell.appendChild(document.createTextNode("\u00a0"));
	entry_bottom_cell.setAttribute("height", "20");
}
function createBoldLabel(text) {
	var label = document.createElement("STRONG");
	label.appendChild(document.createTextNode(text));
	return label;
}