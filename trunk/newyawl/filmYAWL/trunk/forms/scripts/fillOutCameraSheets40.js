var count = 1;
var headerSize = 2;
var footerSize = 1;
var headerAndFooterSize = headerSize + footerSize;

function deleteRow() {
    var table = document.getElementById("cam_roll_info");
    var rows = table.rows.length;

    if (rows > headerAndFooterSize) {
        //delete from the bottom, removing 1 for the 0-based index.
        table.deleteRow(rows-(footerSize+1));
        if (count > 0) {
            document.getElementById("count").value = --count;
        }

        if (count == 0) {
            //add an empty row after the last row has been deleted.
            //this allows all data-containing rows to be deleted and for there to be a single empty row
            //at any point in time.
            addRow();
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


    magCELL.appendChild(createTextBox("mag_number_" + count, 5, ""));
    slateCELL.appendChild(createTextBox("slate_" + count, 5, ""));
    takeCELL.appendChild(createTextBox("take_" + count, 5, ""));
    counterreadingCELL.appendChild(createTextBox("counter_reading_" + count, 6, current_counter_reading));
    takelengthCELL.appendChild(createTextBox("take_length_" + count, 6, ""));
    printCELL.appendChild(printINPUT);
    bwCELL.appendChild(createRadioButton("print_setting_" + count, "B/W"));
    colourCELL.appendChild(createRadioButton("print_setting_" + count, "Colour"));
    notesCELL.appendChild(createTextArea("notes_" + count, 30));

}


//function for radio button details
function createRadioButton(id, value) {
var input =  document.createElement("INPUT");
input.setAttribute("type", "radio");
input.setAttribute("name", id);
input.setAttribute("id", id);
input.setAttribute("value", value);
return input;
}

function calculate(){
	var s_ends = null;
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