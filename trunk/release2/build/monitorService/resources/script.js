/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

/*
 *  A set of javascript functions for use by worklist JSF pages
 *
 *  @author Michael Adams
 *  Created: 25/01/2008
 *  Last Date: 25/01/2008
 *  For: YAWL 2.0
 */
/***********************************************************************************/

// FOR DATATABLES //

// adds the hightlightAndSelectRow() function to the onclick event of each datatable row;
// also inits the background colour of each row.
function addOnclickToDatatableRows() {
    var trs = document.getElementById('form1:dataTable1')
                      .getElementsByTagName('tbody')[0]
                      .getElementsByTagName('tr');
    for (var i = 0; i < trs.length; i++) {
        trs[i].onclick = new Function("highlightAndSelectRow(this)");
        trs[i].bgColor = getBgColorForRow(i);
    }
}


// changes the background colour of the currently selected row, and puts the index
// of the selected row in a hidden form field
function highlightAndSelectRow(tr) {
    var trs = document.getElementById('form1:dataTable1')
                      .getElementsByTagName('tbody')[0]
                      .getElementsByTagName('tr');
    for (var i = 0; i < trs.length; i++) {
        if (trs[i] == tr) {
            trs[i].bgColor = '#ffffd0';           // yellow for selected row

            // add row index to hidden field 
            document.getElementById('form1:hdnRowIndex').value = trs[i].rowIndex;

            // trigger selection
            document.getElementById('form1:btnDetails').click();
        }
        else trs[i].bgColor = getBgColorForRow(i);
    }
}


// set alternate background colours for data table rows
function getBgColorForRow(i) {
    if (i % 2 == 0) return '#ffffff';             // white for even rows
    else return '#e6e6fa' ; //'#e6f0fd';                        // light blue for odd rows

}

/*************************************************************************************/

// gives the user a chance to back out of a deletion
function confirmDelete() {
    var okPressed = confirm("Are you sure you want to remove this participant?");
    return okPressed;       
}

