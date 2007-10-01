var headerSize = 2;
var footerSize = 1;
var headerAndFooterSize = headerSize + footerSize;

function addRow(){
    var count = document.getElementById("count").value;
    var table = document.getElementById("table1");
    var row = table.insertRow(table.rows.length - footerSize);

    var leftCELL = row.insertCell(0);
    var camCELL = row.insertCell(1);
    var soundCELL = row.insertCell(2);
    var sceneCELL = row.insertCell(3);
    var slateCELL = row.insertCell(4);
    var takeCELL = row.insertCell(5);
    var printCELL = row.insertCell(6);
    var durationCELL = row.insertCell(7);
    var commentsCELL = row.insertCell(8);
    var rightCELL = row.insertCell(9);

    var previous_cam = "cam";
    var previous_sound = "sound";
    var previous_scene = "scene";
    var previous_slate = "0";
    var default_comment = "comment"
    var default_duration = "12:00:00";
    var default_take = "0";

    //get the previous rows values to display as defaults.
    if (count > 0) {
        previous_cam = document.getElementById("cam_" + count).value;
        previous_sound = document.getElementById("sound_" + count).value;
        previous_scene = document.getElementById("scene_" + count).value;
        previous_slate = document.getElementById("slate_" + count).value;
    }

    count ++;
    document.getElementById("count").value = count;
    var current_comments = "comments_" + count;
    
    row.setAttribute("valign", "top");
    row.setAttribute("align", "center");
    leftCELL.className = "left";
    leftCELL.appendChild(document.createTextNode("\u00a0"));
    rightCELL.className = "right";
    rightCELL.appendChild(document.createTextNode("\u00a0"));

    camCELL.appendChild(createAnyTextTextBox("cam_" + count, 5, previous_cam, "Enter Camera Roll. [String Value]"));
    soundCELL.appendChild(createAnyTextTextBox("sound_" + count, 5, previous_sound, "Enter Sound Roll. [String Value]"));
    sceneCELL.appendChild(createAnyTextTextBox("scene_" + count, 5, previous_scene, "Enter Scene Number. [String Value] (make sure this matches a scene number from the call sheet)"));
    slateCELL.appendChild(createAnyTextTextBox("slate_" + count, 5, previous_slate, "Enter Slate. [String Value]"));
    takeCELL.appendChild(createNumberTextBox("take_" + count, 5, "", "Enter Take Number. [Number Value]"));
    printCELL.appendChild(createCheckBox("print_" + count, "", false));
    durationCELL.appendChild(createDateTextBox("duration_" + count, 8, "","Enter Take Duration. [Time Value HH:MM:SS]"));

    var inp8 = createTextArea(current_comments, 30, "", "Enter Shot Description. [String Value]");
    if (count == 1) {
        take.value = default_take;
        duration.value = default_duration;
        inp8.setAttribute("value", default_comment);
    }
    commentsCELL.appendChild(inp8);
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

