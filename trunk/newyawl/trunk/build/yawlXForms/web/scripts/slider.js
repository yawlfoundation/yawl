var DEBUG = false;

/*	slider control 
 *  methodnames with a leading '_' stand for a "private" method
 *
 *
 *	the slider control renders similar to the JSlider swing control from Java.
 *	it is set by "start", "end", "step", and "labelStep". 
 *	start and end define the range, step defines the size of a (minor) "tick", so
 *	the number of valid values is (start - end) / step.
 *	labelStep is the size of (major) "ticks". usually it is bigger than step, but
 *	it might be equal. Labels are only shown at major ticks. minor ticks only show
 *	a (smaller) scale line.
 
 *	restrictions:
 *  start, end and labelStep must be multiples of step. like:
 * 	(-2, 2, 0.5, 1) is correct.
 *	(-2, 2, 0.5, 0.75) is not correct (0.75 is not a multiple of 0.5).
 *	
 *	"API":
 *	a slider can be created with 
 *		createSlider(formname, sliderID, sliderName(=controlName), width, height, start, end, step).
 *		createSlider(formname, sliderID, sliderName(=controlName), width, height, start, end, step, labelStep, showInputField, onClick, onChange).
 *	the value can be set with 
 *		setSliderValue(sliderID, value).
 *	the layout can be set with 
 *		setLayout(layoutName);
 *			there are different layouts for the control.
 *			"default"	is simple test layout
 *			"windows"	is the windows XP slider layout
 *			...
 *			all known layout names are provided as a public Constant
 * 
 * OPEN ISSUES:
 * - behaviour at a reload of the page is errorous:
 * 	 slider is set back to tick 0, but is not replaced with empty
 *	 picture on a click to another tick.
 * - height attribute is not implemented.
 * - font size and family are not yet configurable nor dynamic.
 */



// *******************************************************
// ****************** PUBLIC CONSTANTS *******************
// *******************************************************
var LAYOUT_DEFAULT = "default";
var LAYOUT_WINDOWS = "windows";



// *******************************************************
// ****************** PRIVATE CONSTANTS ******************
// *******************************************************
var _TICK_CONTROL_PREFIX = "_T_"; // used as prefix for current tick number (internal hidden field)
var _PICTURE_NAME_PREFIX = "_PT_"; // prefix for image name inside a slider



// *******************************************************
// ************* PRIVATE GLOBAL VARIABLES ****************
// *******************************************************
var _layout = new Array();	// the current layout parameters - are set with setLayout()
var _sliders = new Array(); // global array with all slider's data arrays - for easier access

// *******************************************************
// ******************** INIT LAYOUT **********************
// *******************************************************
	setLayout(LAYOUT_DEFAULT); // set the current layout data to "simple"


// *******************************************************
// ********************* PUBLIC METHODS ******************
// *******************************************************

function createSlider1(formName, sliderID, sliderName, width, start, end, step, labelStep) {
	return createSlider(formName, sliderID, sliderName, width, "", start, end, step, labelStep, false, false, false);
}

function createSlider2(formName, sliderID, sliderName, start, end, step, showInputField, onClick, onChange) {
	return createSlider(formName, sliderID, sliderName, "", "", start, end, step, false, showInputField, onClick, onChange);
}

function createSlider(formName, sliderID, sliderName, width, height, start, end, step, labelStep, showInputField, onClick, onChange) {
	// check for correct slider ID
	if (_sliders[sliderName]) {
		alert('there is already a slider with ID \'' + sliderName + '\'');
		return false;
	}

	// check, if start and end are dividable by step and if labelStep is a multiple of step.
	if (start % step != 0) {
		alert('slider \'' + sliderName + '\': start(' + start + ') must be a multiple of step(' + step + ')');
		return false;
	}
	if (end % step != 0) {
		alert('slider \'' + sliderName + '\': end(' + end + ') must be a multiple of step(' + step + ')');
		return false;
	}

	// if start and end are in reverse order, range is inversed
	if (end < start) {
		range = start - end;
	} else {
		range = end - start;
	}
	ticks = range / step + 1;

	if (labelStep) {
		if (labelStep % step != 0) {
			alert('slider \'' + sliderName + '\': labelStep(' + labelStep + ') must be a multiple of step (' + step + ')');
			return false;
		}
	} else {
		labelStep = step * (range / 5);
		if (labelStep < 2) {
			labelStep = 2;
		}
	}

	// cell dimensions
	if (width && width > 0) {
		cellWidth = Math.round(width / ticks);
	} else {
		cellWidth = _layout['minimalCellWidth'];
	}
	width = ticks * cellWidth;
	
	ctrlType = "hidden";
	border = 0;
	if (DEBUG || showInputField) {
		ctrlType = "text";
		border = 1;
	}

	pointerWidth = _layout['pointerWidth'];
	if (cellWidth < _layout['pointerWidth']) {
		pointerWidth = cellWidth;
	}

	spacerWidthLeft = parseInt((cellWidth - _layout['scaleLineWidth']) / 2);
	spacerWidthRight = cellWidth - spacerWidthLeft - _layout['scaleLineWidth'];

	sliderData = new Array();
	sliderData["formName"] = formName;
	sliderData["sliderID"] = sliderID;
	sliderData["sliderName"] = sliderName;
	sliderData["width"] = width;
	sliderData['height'] = height;
	sliderData['start'] = 1 * start;
	sliderData['end'] = 1 * end;
	sliderData['step'] = 1 * step;
	sliderData['labelStep'] = labelStep;
	sliderData['range'] = range;
	sliderData['ticks'] = ticks;
	sliderData['cellWidth'] = cellWidth;
	sliderData['sliderPointerWidth'] = pointerWidth;
	sliderData['spacerWidthLeft'] = spacerWidthLeft;
	sliderData['spacerWidthRight'] = spacerWidthRight;
	sliderData['showInputField'] = showInputField;
	sliderData['onClick'] = onClick;
	sliderData['onChange'] = onChange;
	
	// put slider into global array
	_sliders[sliderName] = sliderData;
	
	document.write(' <input type="' + ctrlType + '" name="' + sliderName + '" id="' + sliderID + '" value="' + start + '"');
	if (onClick) {
		document.write(' onClick="' + onClick + '"');
	}
	if (onChange) {
		document.write(' onChange="' + onChange + '"');
	}
	if (showInputField) {
		//document.write(' onFocus="alert(\'focus\');"');
		//document.write(' onBlur="alert(\'blur\');"');
	}
	
	document.writeln('>');
	document.writeln('<input type="' + (DEBUG ? 'text' : 'hidden') + '" name="' + _TICK_CONTROL_PREFIX + sliderName + '" value="' + 0 + '">');
	document.writeln('<table width="' + width + '" border="' + border + '">');
	
	if (_layout['order'] == "up") {
		_labelRow(sliderData);
		_scaleRow(sliderData);
		_pointerRow(sliderData);
	} else {
		_pointerRow(sliderData);
		_scaleRow(sliderData);
		_labelRow(sliderData);
	}

	document.writeln('</table>');
	
	return true;
}


function setSlider(sliderName, value) {
	/**	set the slider <sliderName> to the value.
	 *	@return	false, if unknown slider or invalid value. true otherwise.
	 */
	sliderData = _sliders[sliderName];
	if (typeof sliderData == "undefined") {
		alert("slider '" + sliderName + "' is not defined");
		return false;
	}
	if (value < sliderData["start"] || value > sliderData["end"]) {
		alert("value '" + value + "' is not valid for slider '" + sliderName + "'");
		return false;
	}
	tick = (value - sliderData["start"]) / sliderData["step"];
	return _setSlider(sliderName, value, tick);
}



// *******************************************************
// ********************* PRIVATE METHODS ******************
// *******************************************************

function _setSlider(sliderName, value, step) {
	// alert('setSlider: ' + sliderName + ' to ' + value + ' at step ' + step); 
	sliderData = _sliders[sliderName];
	form = document.forms[sliderData["formName"]];
	elm = form.elements[sliderName];
	oldValue = elm.value;
	elmStep = form.elements[_TICK_CONTROL_PREFIX + sliderName];
	oldStep = elmStep.value;
	if (elm && elmStep && (step != oldStep)) {
		elm.value = value;
		elmStep.value = step;
		
		if (DEBUG) {
			alert(elm.name + ' -> ' + elm.value + ' old: ' + oldStep);
			alert(document.images[_PICTURE_NAME_PREFIX + sliderName + '_' + oldStep].name);
			alert(document.images[_PICTURE_NAME_PREFIX + sliderName + '_' + step].name);
		}
		
		document.images[_PICTURE_NAME_PREFIX + sliderName + '_' + oldStep].src = _layout['spacerURL'];
		document.images[_PICTURE_NAME_PREFIX + sliderName + '_' + oldStep].width = sliderData['cellWidth'];

		document.images[_PICTURE_NAME_PREFIX + sliderName + '_' + step].src = _layout["pointerURL"];
		document.images[_PICTURE_NAME_PREFIX + sliderName + '_' + step].width = sliderData['sliderPointerWidth'];
		
		// explicitly do onChange, because it is not triggered, when changes are programmatically.
		if (sliderData['onChange'] && value != oldValue) {
			eval(sliderData['onChange']);
		}
		
	}
	return false;
}

function _labelRow(sliderData) {
	document.writeln('<tr><td>\n\t<table width="100%" cellspacing="0" cellpadding="0"><tr>');
	if (sliderData["cellWidth"] > 15) {
		for (i=0; i<sliderData['ticks']; i++) {
			if (i == 0 || i == (sliderData['ticks']-1) || ((i * sliderData['step']) % sliderData['labelStep'] == 0)) {
				document.writeln('\t\t<td width="' + sliderData['cellWidth'] + '" align="center" nowrap>' + _fix(sliderData['start'] + (i * sliderData['step']), 2) + '</td>');
			} else  {
				document.writeln('\t\t<td width="' + sliderData['cellWidth'] + '"></td>');
			}
		}
	} else {
		labels = sliderData['range'] / sliderData['labelStep'];
		defaultWidth = sliderData['width'] / labels;
		for (i=0; i<=labels; i++) {
			width = defaultWidth;
			align = "center";
			if (i == 0) {
				width = parseInt(width / 2);
				align = "left";
			}
			if (i == labels) {
				width = parseInt(width / 2);
				align = "right";
			}
			document.writeln('\t\t<td width="' + width + '" align="' + align + '" nowrap>' + _fix(sliderData['start'] + (i * sliderData['labelStep']), 2) + '</td>');
		}
	}
	document.writeln('\t</tr></table>\n</td></tr>');
}

function _scaleRow(sliderData) {
	document.writeln('<tr><td>\n\t<table width="100%" cellspacing="0" cellpadding="0"><tr>');
	for (i=0; i<sliderData['ticks']; i++) {
		sVal = (sliderData['start'] + (i * sliderData['step']));
		if (i == 0 || i == (sliderData['ticks']-1) || ((i * sliderData['step']) % sliderData['labelStep'] == 0)) {
			lineH = _layout['scaleLabelLineHeight'];
		} else  {
			lineH = _layout['scaleTickLineHeight'];
		}
		document.writeln('\t\t<td width="' + sliderData['cellWidth'] + '" align="center" valign="bottom">' +
		'<table cellspacing="0" cellpadding="0" width="100%"><tr>' +
		'<td width="1" valign="bottom">' +
		_a_starttag(sliderData, sVal, i) + 
		'<img src="' + _layout['spacerURL'] + '" border="0" width="' + sliderData['spacerWidthLeft'] + '" height="' + _layout['scaleLabelLineHeight'] + '" align="bottom" alt="' + sVal + '"/></td>' +
		'</a>' +
		'<td width="1" valign="bottom">' +
		_a_starttag(sliderData, sVal, i) + 
		'<img src="' + _layout['scaleLineURL'] + '" border="0" height="' + lineH + '" width="' + _layout['scaleLineWidth'] + '" align="bottom" alt="' + sVal + '"/></td>' +
		'</a>' +
		'<td width="1" valign="bottom">' +
		_a_starttag(sliderData, sVal, i) + 
		'<img src="' + _layout['spacerURL'] + '" border="0" width="' + sliderData['spacerWidthRight'] + '" height="' + _layout['scaleLabelLineHeight'] + '" align="bottom" alt="' + sVal + '"/></td>' +
		'</a>' +
		'</tr></table>' +
		'</td>');
	}
	document.writeln('\t</tr></table>\n</td></tr>');
}

function _pointerRow(sliderData) {

	document.writeln('<tr><td>\n\t<table width="100%" cellspacing="0" cellpadding="0"><tr>');
	for (i=0; i<sliderData['ticks']; i++) {
		pictureName = _PICTURE_NAME_PREFIX + sliderData['sliderName'] + '_' + i;
		sVal = (sliderData['start'] + (i * sliderData['step']));

		url = _layout['spacerURL'];
		width = sliderData['cellWidth'];
		if (i == 0) {
			url = _layout['pointerURL'];
			width = sliderData['sliderPointerWidth'];
		}

		document.write('\t\t<td align="center" width="' + sliderData['cellWidth'] + '">');
		document.writeln(
		_a_starttag(sliderData, sVal, i) + 
		'<img name="' + pictureName + '" border="0" src="' + url + '" width="' + width + '" height="' + _layout['pointerHeight'] + '" alt="' + sVal + '"/>' +
		'</a>'
		);
		document.writeln("</td>");
	}
	document.writeln('\t</tr></table>\n</td></tr>');
}

// gibt die zahl auf decimal stellen nach dem komma zurück
function _fix(val, decimal) {
	if (!isNaN(val)) {
		return parseInt(val * 100) / 100;
	}
}

function _a_starttag(sliderData, value, tick) {
	return '<a href="js:/set value to \'' + value + '\'" onclick="return _setSlider(\'' + sliderData['sliderName'] + '\', \'' + value + '\', \'' + tick + '\')">';
}




// *******************************************************
// ********************* LAYOUT METHODS ******************
// *******************************************************

function setLayout(layout) {
	switch (layout) {
		case "windows":
			_layout['order'] 				= "down";
			_layout['scaleLineURL'] 		= "images/slider/windowsXP/tick.gif";
			_layout['scaleLineWidth'] 		= 1;
			_layout['scaleTickLineHeight']	= 4;
			_layout['scaleLabelLineHeight']	= 7;
			_layout['pointerURL'] 			= "images/slider/windowsXP/pointer.gif";
			_layout['pointerWidth'] 		= 11;
			_layout['pointerHeight'] 		= 20;
			_layout['spacerURL'] 			= "images/slider/space.gif";
			_layout['minimalCellWidth'] 	= 15;
			break;
		default:
			_layout['order'] 				= "up";
			_layout['scaleLineURL'] 		= "images/slider/tick.gif";
			_layout['scaleLineWidth'] 		= 2;
			_layout['scaleTickLineHeight']	= 5;
			_layout['scaleLabelLineHeight']	= 10;
			_layout['pointerURL'] 			= "images/slider/pointer.gif";
			_layout['pointerWidth'] 		= 11;
			_layout['pointerHeight'] 		= 20;
			_layout['spacerURL'] 			= "images/slider/space.gif";
			_layout['minimalCellWidth'] 	= 15;
	}
}

