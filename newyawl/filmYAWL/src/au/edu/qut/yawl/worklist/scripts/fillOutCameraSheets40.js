var count = 1;
var headerSize = 3;
var footerSize = 1;
var headerAndFooterSize = headerSize + footerSize;

function deleteRow() {
    var table = document.getElementById("cam_roll_info");
    var rows = table.rows.length;
	var temp_count = document.getElementById("count").value;
    if (rows > headerAndFooterSize) {
        //delete from the bottom, removing 1 for the 0-based index.
		if (temp_count > 1) {
        	table.deleteRow(rows-(footerSize+1));
			document.getElementById("count").value = --temp_count;
		} 
    }
}

function addRow(){

    var table = document.getElementById("cam_roll_info");
    var row = table.insertRow(table.rows.length - footerSize);

    var leftCELL = row.insertCell(0);
    var magCELL = row.insertCell(1);
    var slateCELL = row.insertCell(2);
    var takeCELL = row.insertCell(3);
    var counterreadingCELL = row.insertCell(4);
    var takelengthCELL = row.insertCell(5);
    var printCELL = row.insertCell(6);
    var bwCELL = row.insertCell(7);
    var colourCELL = row.insertCell(8);
    var notesCELL = row.insertCell(9);
    var rightCELL = row.insertCell(10);
    
    var printINPUT =  document.createElement("INPUT");
	
	var count = document.getElementById("count").value;


    var previous_counter_reading = parseInt(document.getElementById("counter_reading_" + count).value);
    var previous_take_length = parseInt(document.getElementById("take_length_" + count).value);
    var current_counter_reading = previous_counter_reading + previous_take_length;

    count ++;
    document.getElementById("count").value = count;
	
    row.setAttribute("valign", "top");
    row.setAttribute("align", "center");

    leftCELL.className = "left";
    leftCELL.appendChild(document.createTextNode("\u00a0"));
    rightCELL.className = "right";
    rightCELL.appendChild(document.createTextNode("\u00a0"));

    printINPUT.setAttribute("type", "checkbox");
    printINPUT.setAttribute("value", "True");
    printINPUT.setAttribute("name", "print_" + count);
    printINPUT.setAttribute("id", "print_" + count);


    magCELL.appendChild(createAnyTextTextBox("mag_number_" + count, 5, "", "Enter Mag Number. [String Value]"));
    slateCELL.appendChild(createAnyTextTextBox("slate_" + count, 5, "", "Enter Slate. [String Value]"));
    takeCELL.appendChild(createNumberTextBox("take_" + count, 5, "", "Enter Take Number. [Number Value]"));
    counterreadingCELL.appendChild(createNumberTextBox("counter_reading_" + count, 6, current_counter_reading, "Enter Counter Reading. [Number Value]"));
    takelengthCELL.appendChild(createNumberTextBox("take_length_" + count, 6, "", "Enter Take Length. [Number Value]"));
    printCELL.appendChild(printINPUT);
    bwCELL.appendChild(createRadioButton("print_setting_" + count, "B/W", true));
    colourCELL.appendChild(createRadioButton("print_setting_" + count, "Colour", false));
    notesCELL.appendChild(createTextArea("notes_" + count, 30, "", "Enter Notes. [String Value]"));

}

function calculate(){
	var s_ends = null;
	var count = document.getElementById("count").value;
	var t_exposed = parseInt(document.getElementById("counter_reading_"+ count).value) + parseInt(document.getElementById("take_length_"+ count).value);
	document.getElementById("total_exposed").value = t_exposed;
	s_ends = parseInt(document.getElementById("footage_loaded").value ) - t_exposed;
	s_ends_minimum = parseInt(document.getElementById("short_end_minimum").value);
	if (s_ends < s_ends_minimum){
		document.getElementById("waste").value = s_ends;
		document.getElementById("short_ends").value = 0;
	}else{
		document.getElementById("waste").value = 0;
		document.getElementById("short_ends").value = s_ends;
	}
}