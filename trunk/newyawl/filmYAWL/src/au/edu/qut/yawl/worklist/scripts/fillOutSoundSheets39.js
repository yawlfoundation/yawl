var headerSize = 2;
var footerSize = 2;
var headerAndFooterSize = headerSize + footerSize;

function validateWithTechSpec(form) {

    if (!validateRadioButtons("sample_rate") ||
        !validateRadioButtons("bit_rate") ||
        !validateRadioButtons("timecode") ||
        !validateRadioButtons("sound_mixer") ||
        !validateRadioButtons("user_bits")) {
        alert("Select at least one option per tech spec.");
        return false;
    }

    return validateFields(form);
}

function validateRadioButtons(groupName) {
    var groupElements = document.getElementsByName(groupName);
    var checked = false;
    for (var x = 0; x < groupElements.length; x++) {
        if (groupElements[x].checked) {
            document.getElementById(groupName + "_grouping").className = "validCellColor";
            return true;
        }
    }
    document.getElementById(groupName + "_grouping").className = "errorCellColor";
    return false;
}

function addRow(){
    var count = document.getElementById("count").value;
    count ++;
    document.getElementById("count").value = count;
    
    var table = document.getElementById("table1");
    var row = table.insertRow(table.rows.length-footerSize);
    
    var leftCELL = row.insertCell(0);
    var camrollCELL = row.insertCell(1);
    var sceneCELL = row.insertCell(2);
    var slateCELL = row.insertCell(3);
    var takeCELL = row.insertCell(4);
    var printCELL = row.insertCell(5);
    var timecodeCELL = row.insertCell(6);
    var remarksCELL = row.insertCell(7);
    var rightCELL = row.insertCell(8);

    var current_remarks = "remarks_" + count;

    row.setAttribute("align","center");

    leftCELL.className = "left";
    leftCELL.appendChild(document.createTextNode("\u00a0"));
    rightCELL.className = "right";
    rightCELL.appendChild(document.createTextNode("\u00a0"));

    camrollCELL.appendChild(createAnyTextTextBox("cam_roll_" + count, 5, "", "Enter Cam Roll. [String Value]"));
    sceneCELL.appendChild(createAnyTextTextBox("scene_" + count, 5, "", "Enter Scene Number. [String Value]"));
    slateCELL.appendChild(createAnyTextTextBox("slate_" + count, 5, "", "Enter Slate. [String Value]"));
    takeCELL.appendChild(createNumberTextBox("take_" + count, 5, "", "Enter Take Number. [Number Value]"));
    printCELL.appendChild(createCheckBox("print_" + count, "", false));
    timecodeCELL.appendChild(createDateTextBox("timecode_" + count, 8, "", "Enter Timecode. [Time Value HH:MM:SS]"));
    remarksCELL.appendChild(createTextArea("remarks_" + count, 30, "", "Enter Comment. [String Value]"));
}

function deleteRow() {
    var table = document.getElementById("table1");
    var rows = table.rows.length;
    var count = document.getElementById("count").value;

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

function getCount (form) {
document.getElementById("count").value = count;
return true;
}
