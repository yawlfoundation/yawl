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