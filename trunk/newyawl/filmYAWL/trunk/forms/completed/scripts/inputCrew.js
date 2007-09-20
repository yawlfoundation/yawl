var count = 1;
var headerSize = 2;
var footerSize = 1;
var headerAndFooterSize = headerSize + footerSize;
function deleteCrewRow() {
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

    var breakline = document.createElement("BR");
    var otherroleINPUT = document.createElement("INPUT");
    var roleINPUT =  document.createElement("SELECT");

    count ++;
    document.getElementById("count").value = count;
    
    roleINPUT.setAttribute("name", "role_" + count);
    roleINPUT.setAttribute("id", "role_" + count);
    populateRoles(roleINPUT);

    roleCELL.setAttribute("id", "rolecell_" + count);
    roleCELL.appendChild(roleINPUT);
    roleCELL.appendChild(breakline);    
    roleCELL.appendChild(createTextBoxWithNoValidation("role_other_" + count, 32, "[If Other, please specify]"));
    firstnameCELL.appendChild(createTextBox("firstname_" + count, 10, "", "enter your first name"));
    lastnameCELL.appendChild(createTextBox("lastname_" + count, 15, "", "enter your last name"));
    contactnoCELL.appendChild(createTelNumberTextBox("contactno_" + count, 8, "", "enter your contact number"));
    emailCELL.appendChild(createEmailTextBox("email_" + count, 20, "", "enter your email address"));
}

function populateRoles(roleINPUT) {
	roleINPUT.appendChild(addDropListItem("Production Manager"));
    roleINPUT.appendChild(addDropListItem("1st AD"));
    roleINPUT.appendChild(addDropListItem("2nd AD"));
    roleINPUT.appendChild(addDropListItem("Camera Assistant"));
    roleINPUT.appendChild(addDropListItem("Camera Operator"));
    roleINPUT.appendChild(addDropListItem("Continuity"));
    roleINPUT.appendChild(addDropListItem("Director"));
    roleINPUT.appendChild(addDropListItem("D.O.P."));
    roleINPUT.appendChild(addDropListItem("Editor"));
    roleINPUT.appendChild(addDropListItem("Producer"));
    roleINPUT.appendChild(addDropListItem("Sound Recordist"));
    roleINPUT.appendChild(addDropListItem("Supervising Production Manager"));
    roleINPUT.appendChild(addDropListItem("Other ..."));
}

//function for dropdown list details
function addDropListItem(name) {
	var option = document.createElement("OPTION");
	option.setAttribute("value", name);
	option.appendChild(document.createTextNode(name));
	return option;
}