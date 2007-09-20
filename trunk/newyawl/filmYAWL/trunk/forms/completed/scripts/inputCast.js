var count = 1;
//This is the number of rows allocated for the header part of the cast table.
var headerSize = 2;
var footerSize = 1;
var headerAndFooterSize = headerSize + footerSize;
function deleteCastRow() {
    var table = document.getElementById("crew");
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
            addCastRow();
        }
    }
}

function addCastRow(){
    count = document.getElementById("count").value;

    var table = document.getElementById("crew");
    var row = table.insertRow(table.rows.length-footerSize);

    row.setAttribute("align", "center");
    row.vAlign = "top";
    row.setAttribute("height", "30");

    var leftCELL = row.insertCell(0);
    leftCELL.className = "left";
    leftCELL.appendChild(document.createTextNode("\u00a0"));

    var characterCELL = row.insertCell(1);
    var artistCELL = row.insertCell(2);
    var agentCELL = row.insertCell(3);
    var contactnoCELL = row.insertCell(4);

    var rightCELL = row.insertCell(5);
    rightCELL.className = "right";
    rightCELL.appendChild(document.createTextNode("\u00a0"));

    count ++;
    document.getElementById("count").value = count;

	characterCELL.appendChild(createTextBox("character_" + count, 20, "", "enter character name"));
	artistCELL.appendChild(createTextBox("artist_" + count, 20, "", "enter artist name"));
	agentCELL.appendChild(createTextBox("agent_" + count, 20, "", "enter agent name"));
	contactnoCELL.appendChild(createTelNumberTextBox("contactno_" + count, 10, "", "enter contact number"));
}

//function getParam(name){
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