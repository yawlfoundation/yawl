/**
 * Support for <xf:textarea chiba:mediatype="text/html"/> with FCKEditor
 *
 * David Jouve
 *
 */

// Global properties for all styledTextarea controls
var _styledTextareaScriptPath; //URL to the editor scripts repository
var _styledTextareaToolbar; //Toolbar to use in the editors
var _styledTextareaHeight; //Height of textarea
var _styledTextareaTimeOutBeforeValueChanged; //When control defined whith incremental='true', time of inactivity to wait before performing a Flux.setXFormsValue

/*
 * Sets Global properties shared by all styledTextarea (called in XSLT)
 * - Toolbar to use in the editors
 * - Height of textarea
 * - URL to the editor scripts repository
 * - time of inactivity to wait before performing a Flux.setXFormsValue (in ms)
 */
function _setStyledTextareaGlobalProperties (toolbar, height, scriptPath, timeout) {
    _styledTextareaToolbar = toolbar;
    _styledTextareaHeight = height;
    _styledTextareaScriptPath = scriptPath;
    _styledTextareaTimeOutBeforeValueChanged = timeout;
}


// Initialize all styledTextarea on window load event
_listenEvent(window, "load", initalizeAllStyledTextareas);
function _listenEvent(object, eventType, func, useCapture) {
    if (object.addEventListener) {
        object.addEventListener (eventType, func, useCapture);
        return true;
    } else if (object.attachEvent) {
        var result = object.attachEvent ("on"+eventType, func);
        return result;
    }
    alert("Event cannot be listened");
}


/*
 * Gets elements by class name
 */
function _getElementsByClassName(classname){
    var rl = new Array();
    var re = new RegExp('(^| )'+classname+'( |$)');
    var ael;
    var op = (navigator.userAgent.indexOf("Opera") != -1) ? true : false;
    if (document.all && !op) {
        ael = document.all;
    } else {
        ael = document.getElementsByTagName('*');
    }
    for(i=0, j=0 ; i<ael.length ; i++) {
        if(re.test(ael[i].className)) {
            rl[j] = ael[i];
            j++;
        }
    }
    return rl;
}


/*
 * Initalize all styledTextareas except those that are nested in repeat-prototype
 */
function initalizeAllStyledTextareas () {
    // Are textareas included in repeat-prototype ?
    var prototypes = _getElementsByClassName("repeat-prototype");
    var textareaPrototypes = new Array();
    for(var i=0; i < prototypes.length; i++) {
        var textareas = prototypes[i].getElementsByTagName("textarea");
        for(var j=0; j < textareas.length; j++) {
            textareaPrototypes[textareas[j].id] = textareas[j];
        }
    }

    var allTextareas = document.getElementsByTagName("textarea");
    for (var i=0; i < allTextareas.length; i++) {
        // do not initialize a styledTextarea which is into a protototype
        if(textareaPrototypes[allTextareas[i].id] == null) {
            initalizeStyledTextarea(allTextareas[i]);
        }
    }
}

/*
 * Initialize the specified textarea. The textarea is set as a styledTextarea
 */
function initalizeStyledTextarea (textarea) {
    if(textarea.id && _hasClass(textarea, "mediatype-text-html")) {
        // update the name attribute before initialization : name should be "d_" + controlID
        controlID = _getStyledTextareaControlID(textarea.id);
        if(textarea.name != "d_"+controlID) {
            textarea.name = "d_"+controlID;
        }

        _debug("[StyledTextarea] Initialize "+textarea.id+" ("+textarea.name+")");
        var oFCKeditor = new FCKeditor(textarea.id) ;
        oFCKeditor.BasePath = _styledTextareaScriptPath ;
        oFCKeditor.ToolbarSet  = _styledTextareaToolbar;
        oFCKeditor.Height = _styledTextareaHeight;
        oFCKeditor.ReplaceTextarea() ;
     }
}

/*
 * Gets XForms control ID form the textarea ID.
 */
function _getStyledTextareaControlID (textareaID) {
    var controlID = textareaID;
    var valueSuffixIndex = textareaID.lastIndexOf("-value");
    if(valueSuffixIndex > 0) {
        controlID = textareaID.substring(0,valueSuffixIndex);
    }
    return controlID;
}


// Used to avoid useless setXFormsValue (if the value has not changed, no setXFormsValue is performed)
var _styledTextareaLastKnownValue = new Array();

// Used when a setValue occurs while the editor is initializing : setValue processing must be deferred
var _styledTextareaDeferredSetInnerHTML = new Array();

/*
 * Sets the HTML content of the specified textarea.
 */
function _styledTextareaSetInnerHTML (textarea, value) {
    var editorInstance = FCKeditorAPI.GetInstance(textarea.id);
    if( editorInstance != null && editorInstance.Status != FCK_STATUS_NOTLOADED) {
         //Editor instance must be completly initialized before setting its value
        _debug("[StyledTextarea] SetInnerHTML STATUS "+textarea.id+" : "+editorInstance.Status);
        if(document.all) {
            // If IE (Not needed with FireFox)
            textarea.value = value;
        }
        editorInstance.SetHTML(value);

        //Store the value as known value
        _styledTextareaLastKnownValue[textarea.id] = value;

        //Any deferred setInnerHTML is canceled
        _styledTextareaDeferredSetInnerHTML[textarea.id] = null;
    } else {
        _styledTextareaDeferredSetInnerHTML[textarea.id] = value;
        _debug("[StyledTextarea] Deferring SetInnerHTML : "+textarea.id);
    }
}


/*
 * Sets the XForms value. Performs a Flux.setXFormsValue
 */
function _styledTextareaSetXFormsValue(textareaID) {
    _styledTextareaLastTimeoutIDs[textareaID] = null;
    DWREngine.setErrorHandler(handleExceptions);

    var editorInstance = FCKeditorAPI.GetInstance(textareaID);
    // Get XForms control ID form textarea element
    var controlID = _getStyledTextareaControlID(textareaID);
    var value = editorInstance.GetXHTML();

    // Is the setXFormsValue really needed ?
    if(_styledTextareaLastKnownValue[textareaID] != value) {
        _clear();
        _debug("Flux.setXFormsValue: " + controlID + "='" + value + "'");
        useLoadingMessage();

        DWREngine.setOrdered(true);
        DWREngine.setErrorHandler(handleExceptions);

        var sessionKey = document.getElementById("chibaSessionKey").value;
        Flux.setXFormsValue(updateUI, controlID, value, sessionKey);

        // Store the new last known value
        _styledTextareaLastKnownValue[textareaID] = value;
    }
}



// Mapping textareaID ==> timeout (avoid performing a Flux.setXFormsValue for each keydown..)
var _styledTextareaLastTimeoutIDs = new Array();

/*
 * Deferred SetXFormsValue for the <textarea chiba:mediatype='text/html'/>
 * Avoid performing a Flux.setXFormsValue for each keydown.. when incremental = 'true'
 */
function _styledTextareaDeferredSetXFormsValue(editorInstance) {
    _debug("[StyledTextarea] DeferredSetXFormsValue : "+ editorInstance.Name);
    var textareaID = editorInstance.Name;
    if(_styledTextareaLastTimeoutIDs[textareaID] != null) {
        clearTimeout(_styledTextareaLastTimeoutIDs[textareaID]);
    }
    _styledTextareaLastTimeoutIDs[textareaID] = setTimeout("_styledTextareaSetXFormsValue('"+textareaID+"')", _styledTextareaTimeOutBeforeValueChanged);
    return true;
}


/**
 * Called when editor initialization is complete in order to listen user event
 */
function FCKeditor_OnComplete(editorInstance) {
    _debug("[StyledTextarea] Initialization completed : "+ editorInstance.Name);
    var textareaID = editorInstance.Name;
    var textareaElement = document.getElementById(textareaID);

    if(textareaElement.onkeyup && textareaElement.onkeyup != "") {
        //textarea defined with incremental = true
        editorInstance.Events.AttachEvent('OnKeyDown', _styledTextareaDeferredSetXFormsValue);
        editorInstance.Events.AttachEvent('OnPaste', _styledTextareaDeferredSetXFormsValue);
        editorInstance.Events.AttachEvent('OnSelectionChange', _styledTextareaDeferredSetXFormsValue);
    }

    editorInstance.Events.AttachEvent('OnBlur', FCKeditor_OnBlur);
    editorInstance.Events.AttachEvent('OnFocus', FCKeditor_OnFocus);

    // Storing the new last known value
    _styledTextareaLastKnownValue[textareaID] = textareaElement.value;

    //If a setValue has occurred before the end of initialization :
    if(_styledTextareaDeferredSetInnerHTML[textareaID] !=  null) {
        _styledTextareaSetInnerHTML(textareaElement, _styledTextareaDeferredSetInnerHTML[textareaID]);
    }
}

function FCKeditor_OnBlur (editorInstance) {
    var textareaID = editorInstance.Name;
    _styledTextareaSetXFormsValue(textareaID);
}

function FCKeditor_OnFocus (editorInstance) {
    var textareaID = editorInstance.Name;
    var textareaElement = document.getElementById(textareaID);

    // Simulate click event on textarea element (needed to refresh repeat index)
    if(textareaElement.fireEvent) {
         // If IE
         textareaElement.fireEvent('onclick');
    } else {
        var evObj = document.createEvent('MouseEvents');
        evObj.initEvent('click', true, true);
        textareaElement.dispatchEvent(evObj);
    }
}
