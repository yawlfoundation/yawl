var count = 1;
//This is the number of rows allocated for the header part of the cast table.
var headerSize = 1;
var footerSize = 0;
var headerAndFooterSize = headerSize + footerSize;
function deleteEntry() {
    var table = document.getElementById("locations");
    var rows = table.rows.length;

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
	locationidCELL.appendChild(createTextBox("location_ID_" + count, 25, "", "Enter Location ID. [String Value - Compulsory]"));
	locationnameLABEL.appendChild(createBoldLabel("Location Name"));
	locationnameCELL.appendChild(createTextBox("location_name_" + count, 25, "", "Enter Location Name. [String Value - Compulsory]"));
	
	//second row - Address and UBD
	var row2 = table.insertRow(table.rows.length-footerSize);
	var left2CELL = row2.insertCell(0);
	var addressLABEL = row2.insertCell(1);
	var addressCELL = row2.insertCell(2);
	var ubdLABEL = row2.insertCell(3);
	var ubdCELL = row2.insertCell(4);
	var right2CELL = row2.insertCell(5);
	left2CELL.className = "left";
    left2CELL.appendChild(document.createTextNode("\u00a0"));
	right2CELL.className = "right";
    right2CELL.appendChild(document.createTextNode("\u00a0"));
	addressLABEL.appendChild(createBoldLabel("Address"));
	addressLABEL.setAttribute("width", "150");
	addressCELL.appendChild(createTextBox("address_" + count, 25, "", "Enter Location Address. [String Value - Compulsory]"));
	ubdLABEL.appendChild(createBoldLabel("UBD Map Ref"));
	ubdCELL.appendChild(createTextBox("ubd_" + count, 25, "", "Enter UBD Map reference. [String Value - Compulsory]"));
	
	//third row - Police and Hospital
	var row3 = table.insertRow(table.rows.length-footerSize);
	var left3CELL = row3.insertCell(0);
	var policeLABEL = row3.insertCell(1);
	var policeCELL = row3.insertCell(2);
	var hospitalLABEL = row3.insertCell(3);
	var hospitalCELL = row3.insertCell(4);
	var right3CELL = row3.insertCell(5);
	left3CELL.className = "left";
    left3CELL.appendChild(document.createTextNode("\u00a0"));
	right3CELL.className = "right";
    right3CELL.appendChild(document.createTextNode("\u00a0"));
	policeLABEL.appendChild(createBoldLabel("Police"));
	policeLABEL.setAttribute("width", "150");
	policeCELL.appendChild(createTextBox("police_" + count, 25,"", "Enter Police Details. [String Value - Compulsory]"));
	hospitalLABEL.appendChild(createBoldLabel("Hospital"));
	hospitalCELL.appendChild(createTextBox("hospital_" + count, 25,"", "Enter Hospital Details. [String Value - Compulsory]"));
	
	//fourth row - Contact and Number
	var row4 = table.insertRow(table.rows.length-footerSize);
	var left4CELL = row4.insertCell(0);
	var contactLABEL = row4.insertCell(1);
	var contactCELL = row4.insertCell(2);
	var contactnoLABEL = row4.insertCell(3);
	var contactnoCELL = row4.insertCell(4);
	var right4CELL = row4.insertCell(5);
	left4CELL.className = "left";
    left4CELL.appendChild(document.createTextNode("\u00a0"));
	right4CELL.className = "right";
    right4CELL.appendChild(document.createTextNode("\u00a0"));
	contactLABEL.appendChild(createBoldLabel("Contact"));
	contactLABEL.setAttribute("width", "150");
	contactCELL.appendChild(createTextBox("contact_" + count, 25,"", "Enter Contact Person. [String Value - Compulsory]"));
	contactnoLABEL.appendChild(createBoldLabel("Contact No."));
	contactnoCELL.appendChild(createNumberTextBox("phone_" + count, 25,"", "Enter Contact Number. [String Value - Compulsory]"));
	
	//fifth row - Parking
	var row5 = table.insertRow(table.rows.length-footerSize);
	var left5CELL = row5.insertCell(0);
	var parkingLABEL = row5.insertCell(1);
	var parkingCELL = row5.insertCell(2);
	var right5CELL = row5.insertCell(3);
	left5CELL.className = "left";
    left5CELL.appendChild(document.createTextNode("\u00a0"));
	right5CELL.className = "right";
    right5CELL.appendChild(document.createTextNode("\u00a0"));
	parkingCELL.colSpan = "3";
	parkingLABEL.appendChild(createBoldLabel("Parking"));
	parkingLABEL.setAttribute("width", "150");
	parkingCELL.appendChild(createTextBox("parking_" + count, 80,"", "Enter Parking Details. [String Value - Compulsory]"));
	
	//sixth row - Unit
	var row6 = table.insertRow(table.rows.length-footerSize);
	var left6CELL = row6.insertCell(0);
	var unitLABEL = row6.insertCell(1);
	var unitCELL = row6.insertCell(2);
	var right6CELL = row6.insertCell(3);
	left6CELL.className = "left";
    left6CELL.appendChild(document.createTextNode("\u00a0"));
	right6CELL.className = "right";
    right6CELL.appendChild(document.createTextNode("\u00a0"));
	unitCELL.colSpan = "3";
	unitLABEL.appendChild(createBoldLabel("Unit"));
	unitLABEL.setAttribute("width", "150");
	unitCELL.appendChild(createTextBox("unit_" + count, 80,"", "Enter Unit Details. [String Value - Compulsory]"));
	
	//seven row - notes
	var row7 =table.insertRow(table.rows.length-footerSize);
	var left7CELL = row7.insertCell(0);
	var locationnotesLABEL = row7.insertCell(1);
	var locationnotesCELL = row7.insertCell(2);
	var input11 = row7.insertCell(3);
	var right7CELL = row7.insertCell(4);
	left7CELL.className = "left";
    left7CELL.appendChild(document.createTextNode("\u00a0"));
	left7CELL.setAttribute("width", "15");
	right7CELL.className = "right";
    right7CELL.appendChild(document.createTextNode("\u00a0"));
	right7CELL.setAttribute("width", "15");
	locationnotesCELL.colSpan = "2";
	locationnotesLABEL.appendChild(createBoldLabel("Location Notes"));
	locationnotesLABEL.setAttribute("width", "150");
	locationnotesCELL.appendChild(createTextArea("notes_" + count, 50,"", "Enter Location Notes. [String Value]"));
	
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