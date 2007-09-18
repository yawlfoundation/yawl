//var count = 1;
var headerSize = 2;
var footerSize = 2;
var headerAndFooterSize = headerSize + footerSize;

function addRow(){

//var tbody = document.getElementById("table1").getElementsByTagName("tbody")[0];
//var row = document.createElement("TR");
//var leftCELL = document.createElement("TD");
//var magCELL = document.createElement("TD");
//var slateCELL = document.createElement("TD");
//var takeCELL = document.createElement("TD");
//var counterreadingCELL = document.createElement("TD");
//var takelengthCELL = document.createElement("TD");
//var printCELL = document.createElement("TD");
//var bwCELL = document.createElement("TD");
//var colourCELL = document.createElement("TD");
//var notesCELL = document.createElement("TD");
//var rightCELL = document.createElement("TD");
//var printINPUT =  document.createElement("INPUT");

    var table = document.getElementById("table1");
    var row = table.insertCell(table.rows.length - footerSize);

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


    var previous_counter_reading = parseInt(document.getElementById("counter_reading_" + count).value);
    var previous_take_length = parseInt(document.getElementById("take_length_" + count).value);
    var current_counter_reading = previous_counter_reading + previous_take_length;

    var count = document.getElementById("count").value;
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

//    row.appendChild(leftCELL);
//    row.appendChild(magCELL);
//    row.appendChild(slateCELL);
//    row.appendChild(takeCELL);
//    row.appendChild(counterreadingCELL);
//    row.appendChild(takelengthCELL);
//    row.appendChild(printCELL);
//    row.appendChild(bwCELL);
//    row.appendChild(colourCELL);
//    row.appendChild(notesCELL);
//    row.appendChild(rightCELL);
//    tbody.appendChild(row);
    //alert(row.innerHTML);
}

//function for textbox details
//function createTextBox(id, size, value) {
//var input =  document.createElement("INPUT");
//input.setAttribute("size", size);
//input.setAttribute("name", id);
//input.setAttribute("id", id);
//input.setAttribute("value", value);
//return input;
//}

//function for radio button details
function createRadioButton(id, value) {
var input =  document.createElement("INPUT");
input.setAttribute("type", "radio");
input.setAttribute("name", id);
input.setAttribute("id", id);
input.setAttribute("value", value);
return input;
}

//function for textarea details
//function createTextArea(id, size) {
//var input =  document.createElement("TEXTAREA");
//input.setAttribute("cols", size);
//input.setAttribute("name", id);
//input.setAttribute("id", id);
//return input;
//}


//function getParam(name)
//{
//  var start=location.search.indexOf("?"+name+"=");
//  if (start<0) start=location.search.indexOf("&"+name+"=");
//  if (start<0) return '';
//  start += name.length+2;
//  var end=location.search.indexOf("&",start)-1;
//  if (end<0) end=location.search.length;
//  var result='';
//  for(var i=start;i<=end;i++) {
//    var c=location.search.charAt(i);
//    result=result+(c=='+'?' ':c);
//  }
//  //window.alert('Result = '+result);
//  return unescape(result);
//}
//
//function getParameters(){
//	document.form1.workItemID.value = getParam('workItemID');
//	document.form1.userID.value = getParam('userID');
//	document.form1.sessionHandle.value = getParam('sessionHandle');
//	document.form1.JSESSIONID.value = getParam('JSESSIONID');
//	document.form1.submit.value = "htmlForm";
//}

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