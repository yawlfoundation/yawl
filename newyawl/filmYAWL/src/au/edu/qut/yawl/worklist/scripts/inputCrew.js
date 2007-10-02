var count = 1;
var headerSize = 2;
var footerSize = 1;
var headerAndFooterSize = headerSize + footerSize;
function deleteCrewRow() {
    var table = document.getElementById("crew");
    var rows = table.rows.length;
	count = document.getElementById("count").value;
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
            addCrewRow();
        }
    }
}

function addCrewRow(){
	count = document.getElementById("count").value;

    var table = document.getElementById("crew");
    var row = table.insertRow(table.rows.length-footerSize);

    row.setAttribute("align", "center");
    row.vAlign = "top";
    row.setAttribute("height", "30");

    var leftCELL = row.insertCell(0);
    leftCELL.className = "left";
    leftCELL.appendChild(document.createTextNode("\u00a0"));

    var roleCELL = row.insertCell(1);
    var firstnameCELL = row.insertCell(2);
    var lastnameCELL = row.insertCell(3);
    var contactnoCELL = row.insertCell(4);
    var emailCELL = row.insertCell(5);

    var rightCELL = row.insertCell(6);
    rightCELL.className = "right";
    rightCELL.appendChild(document.createTextNode("\u00a0"));


    count ++;
    document.getElementById("count").value = count;
    

    roleCELL.appendChild(createAnyTextTextBox("role_" + count, 32, "", "Enter Role. [String Value]"));
    firstnameCELL.appendChild(createAnyTextTextBox("firstname_" + count, 10, "", "Enter First Name. [String Value]"));
    lastnameCELL.appendChild(createAnyTextTextBox("lastname_" + count, 15, "", "Enter Last Name. [String Value]"));
    contactnoCELL.appendChild(createAnyTextTextBox("contactno_" + count, 8, "", "Enter Contact Number. [String Value]"));
    emailCELL.appendChild(createEmailTextBox("email_" + count, 20, "", "Enter Email Address. [String Value]"));
}