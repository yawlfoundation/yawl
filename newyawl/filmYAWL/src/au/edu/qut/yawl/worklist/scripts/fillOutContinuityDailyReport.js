var scenes_count = 1;

function calculateTotal() {
	scenes_count = document.getElementById("scene_timing_count").value;
	var total_est_timing = 0;
	var total_act_timing = 0;
	var total_variance = 0;
	var total_cumulative = 0;
	var total_pages = 0;
	var total_pagesnum = 0;
	for(var count = 1; count <= scenes_count; count ++) {
		
		var est_timing = document.getElementById("est_timing_" + count).value;
		var act_timing = document.getElementById("actual_timing_" + count).value;
		if (count <= 1) {
			var prev_cumulative = document.getElementById("prev_shot_acttiming").value;
		}else if (count >1){
			var prev_cumulative = document.getElementById("cumulative_running_" + (count-1)).value;
		}
		var pages = parseInt(document.getElementById("page_count_" + count).value);
		var pagesnum = parseInt(document.getElementById("page_count_num_" + count).value);
		
		var est_timing_array = est_timing.split(":");
		var act_timing_array = act_timing.split(":");
		var prev_cumulative_array = prev_cumulative.split(":");
		
		var timeA = new Date(0, 0, 0, est_timing_array[0], est_timing_array[1], est_timing_array[2]);
		var timeB = new Date(0, 0, 0, act_timing_array[0], act_timing_array[1], act_timing_array[2]);
		var timeC = new Date(0, 0, 0, prev_cumulative_array[0], prev_cumulative_array[1], prev_cumulative_array[2]);
		
		esttimingHOURS = timeA.getHours();
		esttimingMINUTES = timeA.getMinutes();
		esttimingSECONDS = timeA.getSeconds();
		var esttimingTIME = (esttimingHOURS *1000*60*60) + (esttimingMINUTES *1000*60) + (esttimingSECONDS *1000);
		
		acttimingHOURS = timeB.getHours();
		acttimingMINUTES = timeB.getMinutes();
		acttimingSECONDS = timeB.getSeconds();
		var acttimingTIME = (acttimingHOURS *1000*60*60) + (acttimingMINUTES *1000*60) + (acttimingSECONDS *1000);
		
		cumulativeHOURS = timeC.getHours();
		cumulativeMINUTES = timeC.getMinutes();
		cumulativeSECONDS = timeC.getSeconds();
		var cumulativeTIME = (cumulativeHOURS *1000*60*60) + (cumulativeMINUTES *1000*60) + (cumulativeSECONDS *1000);
		
		var variance = acttimingTIME - esttimingTIME;
		var cumulative =  cumulativeTIME + acttimingTIME;
		
		total_est_timing += esttimingTIME;
		total_act_timing += acttimingTIME;
		total_variance += variance;
		total_pages += pages;
		total_pagesnum += pagesnum;
		
		if (variance < 0) {
			variance = variance * -1;
			document.getElementById("variance_sign_" + count).value = "-";
		}else{
			document.getElementById("variance_sign_" + count).value = "+";
		}
		
		calculateDuration(variance, "variance_" + count);
		calculateDuration(cumulative, "cumulative_running_" + count);
	}
	
	var excess = total_pages + Math.floor(total_pagesnum/8);
	var mod = total_pagesnum % 8;
	document.getElementById("total_page_count").value = excess;
	document.getElementById("total_page_count_num").value = mod;
	
	calculateDuration(total_est_timing, "total_est_timing");
	calculateDuration(total_act_timing, "total_actual_timing");
	if (total_variance < 0) {
		total_variance = total_variance * -1;
		document.getElementById("total_variance_sign").value = "-";
	}else{
		document.getElementById("total_variance_sign").value = "+";
	}
	calculateDuration(total_variance, "total_variance");
	document.getElementById("total_cumulative_running").value = document.getElementById("cumulative_running_" + scenes_count).value;
}

function calculateDuration(millisecond, element) {
	var duration = "";

	var hoursDifference = Math.floor(millisecond/1000/60/60);
	millisecond = millisecond - hoursDifference*1000*60*60
	var minutesDifference = Math.floor(millisecond/1000/60);
	variance = millisecond - minutesDifference*1000*60
	var secondsDifference = Math.floor(millisecond/1000);
	
	secondsDifference = secondsDifference % 60;
	var excessSECONDS =  Math.floor(secondsDifference/60);
	
	minutesDifference = excessSECONDS + minutesDifference % 60;
	var excessMINUTES =  Math.floor(minutesDifference/60);
	
	hoursDifference += excessMINUTES;
	
	if(hoursDifference.toString().length == 1) { 
	duration += "0" + hoursDifference.toString() + ":";
	} else {
	duration += hoursDifference.toString() + ":";
	}
	if(minutesDifference.toString().length == 1) { 
	duration +=  "0" + minutesDifference.toString() + ":";
	} else {
	duration+= minutesDifference.toString() + ":";
	}
	if(secondsDifference.toString().length == 1) { 
	duration +=  "0" + secondsDifference.toString();
	} else {
	duration += secondsDifference.toString() ;
	}
	document.getElementById(element).value = duration;
}

function calculateScriptTiming(){
//shot today
	document.getElementById("shot_today_scenes").value = document.getElementById("scene_timing_count").value;
	document.getElementById("shot_today_pages").value = document.getElementById("total_page_count").value;
	document.getElementById("shot_today_pagesnum").value = document.getElementById("total_page_count_num").value;
	document.getElementById("shot_today_esttiming").value = document.getElementById("total_est_timing").value;
	document.getElementById("shot_today_acttiming").value = document.getElementById("total_actual_timing").value;
	
//shot to date
	var prev_shot_scenes = parseInt(document.getElementById("prev_shot_scenes").value);
	var prev_shot_pages = parseInt(document.getElementById("prev_shot_pages").value);
	var prev_shot_pagesnum = parseInt(document.getElementById("prev_shot_pagesnum").value);
	var prev_shot_esttiming = document.getElementById("prev_shot_esttiming").value;
	var prev_shot_acttiming = document.getElementById("prev_shot_acttiming").value;
	
	var shot_today_scenes = parseInt(document.getElementById("shot_today_scenes").value);
	var shot_today_pages = parseInt(document.getElementById("shot_today_pages").value);
	var shot_today_pagesnum = parseInt(document.getElementById("shot_today_pagesnum").value);
	var shot_today_esttiming = document.getElementById("shot_today_esttiming").value;
	var shot_today_acttiming = document.getElementById("shot_today_acttiming").value;	

	var temp_number1 = prev_shot_pages + shot_today_pages;
	var temp_numerator1 = prev_shot_pagesnum + shot_today_pagesnum;
	
	var excess1 = temp_number1 + Math.floor(temp_numerator1/8);
	var mod1 = temp_numerator1 % 8;
	
	var p_est_timing_array = prev_shot_esttiming.split(":");
	var p_act_timing_array = prev_shot_acttiming.split(":");
	var st_est_timing_array = shot_today_esttiming.split(":");
	var st_act_timing_array = shot_today_acttiming.split(":");
	
	var p_timeA = new Date(0, 0, 0, p_est_timing_array[0], p_est_timing_array[1], p_est_timing_array[2]);
	var p_timeB = new Date(0, 0, 0, p_act_timing_array[0], p_act_timing_array[1], p_act_timing_array[2]);
	var st_timeA = new Date(0, 0, 0, st_est_timing_array[0], st_est_timing_array[1], st_est_timing_array[2]);
	var st_timeB = new Date(0, 0, 0, st_act_timing_array[0], st_act_timing_array[1], st_act_timing_array[2]);
	
	p_esttimingHOURS = p_timeA.getHours();
	p_esttimingMINUTES = p_timeA.getMinutes();
	p_esttimingSECONDS = p_timeA.getSeconds();
	var p_esttimingTIME = (p_esttimingHOURS *1000*60*60) + (p_esttimingMINUTES *1000*60) + (p_esttimingSECONDS *1000);
	p_acttimingHOURS = p_timeB.getHours();
	p_acttimingMINUTES = p_timeB.getMinutes();
	p_acttimingSECONDS = p_timeB.getSeconds();
	var p_acttimingTIME = (p_acttimingHOURS *1000*60*60) + (p_acttimingMINUTES *1000*60) + (p_acttimingSECONDS *1000);
	st_esttimingHOURS = st_timeA.getHours();
	st_esttimingMINUTES = st_timeA.getMinutes();
	st_esttimingSECONDS = st_timeA.getSeconds();
	var st_esttimingTIME = (st_esttimingHOURS *1000*60*60) + (st_esttimingMINUTES *1000*60) + (st_esttimingSECONDS *1000);
	st_acttimingHOURS = st_timeB.getHours();
	st_acttimingMINUTES = st_timeB.getMinutes();
	st_acttimingSECONDS = st_timeB.getSeconds();
	var st_acttimingTIME = (st_acttimingHOURS *1000*60*60) + (st_acttimingMINUTES *1000*60) + (st_acttimingSECONDS *1000);
	
	var temp_esttiming = p_esttimingTIME + st_esttimingTIME;
	var temp_acttiming = p_acttimingTIME + st_acttimingTIME;
	
	document.getElementById("shot_to_date_scenes").value = prev_shot_scenes + shot_today_scenes;
	document.getElementById("shot_to_date_pages").value = excess1;
	document.getElementById("shot_to_date_pagesnum").value = mod1;
	calculateDuration(temp_esttiming, "shot_to_date_esttiming");
	calculateDuration(temp_acttiming, "shot_to_date_acttiming");



//to be shot
	var shot_to_date_scenes = parseInt(document.getElementById("shot_to_date_scenes").value);
	var shot_to_date_pages = parseInt(document.getElementById("shot_to_date_pages").value);
	var shot_to_date_pagesnum = parseInt(document.getElementById("shot_to_date_pagesnum").value);
	var shot_to_date_esttiming = document.getElementById("shot_to_date_esttiming").value;
	var shot_to_date_acttiming = document.getElementById("shot_to_date_acttiming").value;
	
	var total_scenes = parseInt(document.getElementById("total_scenes").value);
	var total_pages = parseInt(document.getElementById("total_pages").value);
	var total_pagesnum = parseInt(document.getElementById("total_pagesnum").value);
	var total_esttiming = document.getElementById("total_esttiming").value;
	
	var temp_number2 = total_pages - shot_to_date_pages;
	var temp_numerator2 = total_pagesnum - shot_to_date_pagesnum;
	
	var excess2 = temp_number2 + Math.floor(temp_numerator2/8);
	var mod2 = temp_numerator2 % 8;
	
	var t_est_timing_array = total_esttiming.split(":");
	var std_est_timing_array = shot_to_date_esttiming.split(":");
	
	var t_timeA = new Date(0, 0, 0, t_est_timing_array[0], t_est_timing_array[1], t_est_timing_array[2]);
	var std_timeA = new Date(0, 0, 0, std_est_timing_array[0], std_est_timing_array[1], std_est_timing_array[2]);
	
	t_esttimingHOURS = t_timeA.getHours();
	t_esttimingMINUTES = t_timeA.getMinutes();
	t_esttimingSECONDS = t_timeA.getSeconds();
	var t_esttimingTIME = (t_esttimingHOURS *1000*60*60) + (t_esttimingMINUTES *1000*60) + (t_esttimingSECONDS *1000);
	std_esttimingHOURS = std_timeA.getHours();
	std_esttimingMINUTES = std_timeA.getMinutes();
	std_esttimingSECONDS = std_timeA.getSeconds();
	var std_esttimingTIME = (std_esttimingHOURS *1000*60*60) + (std_esttimingMINUTES *1000*60) + (std_esttimingSECONDS *1000);
	
	var temp_esttiming2 = t_esttimingTIME - std_esttimingTIME;

	document.getElementById("remaining_scenes").value = total_scenes - shot_to_date_scenes;
	document.getElementById("remaining_pages").value = excess2;
	document.getElementById("remaining_pagesnum").value = mod2;
	calculateDuration(temp_esttiming2, "remaining_esttiming");
	calculateDuration(temp_esttiming2, "remaining_acttiming");
	
	//total
	var std_acttiming = document.getElementById("shot_to_date_acttiming").value;
	var rem_esttiming = document.getElementById("remaining_esttiming").value;
	
	var std_acttiming_array = std_acttiming.split(":");
	var rem_esttiming_array = rem_esttiming.split(":");
	
	var stdA_timeA = new Date(0, 0, 0, std_acttiming_array[0], std_acttiming_array[1], std_acttiming_array[2]);
	var remE_timeA = new Date(0, 0, 0, rem_esttiming_array[0], rem_esttiming_array[1], rem_esttiming_array[2]);
	
	stdA_acttimingHOURS = stdA_timeA.getHours();
	stdA_acttimingMINUTES = stdA_timeA.getMinutes();
	stdA_acttimingSECONDS = stdA_timeA.getSeconds();
	var stdA_acttimingTIME = (stdA_acttimingHOURS *1000*60*60) + (stdA_acttimingMINUTES *1000*60) + (stdA_acttimingSECONDS *1000);
	remE_esttimingHOURS = remE_timeA.getHours();
	remE_esttimingMINUTES = remE_timeA.getMinutes();
	remE_esttimingSECONDS = remE_timeA.getSeconds();
	var remE_esttimingTIME = (remE_esttimingHOURS *1000*60*60) + (remE_esttimingMINUTES *1000*60) + (remE_esttimingSECONDS *1000);
	
	var temp_timing = stdA_acttimingTIME + remE_esttimingTIME;
	calculateDuration(temp_timing, "total_acttiming");
}

var count = 1;
function addScenes(name){
	var cell = document.getElementById(name);
	
	count = document.getElementById(name + "_count").value;
	count ++;
	document.getElementById(name + "_count").value = count;
	
	cell.appendChild(createTextBox(name + "_" + count,2,false,"text",""));
}
function addSlates(name){
	var cell = document.getElementById(name);
	
	count = document.getElementById(name + "_count").value;
	count ++;
	document.getElementById(name + "_count").value = count;
	
	cell.appendChild(createTextBox(name + "_" + count,2,false,"text",""));
}
function addSet(name){
	var cell = document.getElementById(name);
	
	count = document.getElementById(name + "_count").value;
	count ++;
	document.getElementById(name + "_count").value = count;
	
	cell.appendChild(createTextBox(name + "_" + count,25,false,"text",""));
}

var unit_count = 1;
function addUnit(){
	unit_count =  document.getElementById("unit_count").value;
	unit_count ++;
	document.getElementById("unit_count").value = unit_count;
	
	var tbody = document.getElementById("slate_no").getElementsByTagName("tbody") [0];
	var row = document.createElement("TR");
	var unitCELL = document.createElement("TD");
	var slateCELL = document.createElement("TD");
	var buttonCELL = document.createElement("TD");
	//var slateBUTTON = document.createElement("INPUT");
	
	slateCELL.setAttribute("id", "unit" + unit_count);
	
	//slateBUTTON.setAttribute("type", "button");
	//slateBUTTON.setAttribute("value", "Insert Slate");
	//slateBUTTON.setAttribute("onClick", "addSlates('unit"+ unit_count +"');");
	
	unitCELL.appendChild(createTextBox("unit" + unit_count + "_name", 15, false, "text",""));
	slateCELL.appendChild(createTextBox("unit" + unit_count + "_1", 2, false, "text",""));
	buttonCELL.appendChild(slateBUTTON);
	buttonCELL.appendChild(createTextBox("unit" + unit_count + "_count", 15, false, "hidden",1));
	
	row.appendChild(leftright("left"));
	row.appendChild(unitCELL);
	row.appendChild(slateCELL);
	row.appendChild(buttonCELL);
	row.appendChild(leftright("right"));
	
	tbody.appendChild(row);
}

var location_count = 1;
function addLocation() {
	var tbody = document.getElementById("locations").getElementsByTagName("tbody") [0];
	var row = document.createElement("TR");
	var locationnameCELL = document.createElement("TD");
	var locationaddressCELL = document.createElement("TD");
	var locationsetCELL = document.createElement("TD");
	var buttonCELL = document.createElement("TD");
	//var setBUTTON = document.createElement("INPUT");
	
	location_count = document.getElementById("location_count").value;
	location_count ++;
	document.getElementById("location_count").value = location_count;
	
	row.vAlign = "top";
	locationsetCELL.setAttribute("id", "location_set_" + location_count);
	locationsetCELL.style.display = "block";
	
	//setBUTTON.setAttribute("type", "button");
	//setBUTTON.setAttribute("value", "Insert Set");
	//setBUTTON.setAttribute("onClick", "addSet('location_set_" + location_count + "');");
	
	locationnameCELL.appendChild(createTextBox("location_name_" + location_count , 25, false, "text",""));
	locationaddressCELL.appendChild(createTextBox("location_address_" + location_count , 25, false, "text",""));
	locationsetCELL.appendChild(createTextBox("location_set_" + location_count + "_1" , 25, false, "text",""));
	//buttonCELL.appendChild(setBUTTON);
	//buttonCELL.appendChild(createTextBox("location_set_" + location_count + "_count", 15, false, "hidden",1));
	
	row.appendChild(leftright("left"));
	row.appendChild(locationnameCELL);
	row.appendChild(locationaddressCELL);
	row.appendChild(locationsetCELL);
	row.appendChild(buttonCELL);
	row.appendChild(leftright("right"));
	
	tbody.appendChild(row);

}

var scene_timing_count = 1;
function addSceneTiming() {
	scene_timing_count = document.getElementById("scene_timing_count").value;
	var tbody = document.getElementById("scene_timing").getElementsByTagName("tbody") [0];
	
	scene_timing_count ++;
	document.getElementById("scene_timing_count").value = scene_timing_count;
	
	var row = document.createElement("TR");
	var sceneCELL = document.createElement("TD");
	var pageCELL = document.createElement("TD");
	var esttimingCELL = document.createElement("TD");
	var actualtimingCELL = document.createElement("TD");
	var varianceCELL = document.createElement("TD");
	var cumulativerunningCELL = document.createElement("TD");
	var varianceDROPDOWN =  document.createElement("SELECT");
	
	row.setAttribute("align", "center");
	
	varianceDROPDOWN.setAttribute("name", "variance_sign_" + scene_timing_count);
	varianceDROPDOWN.setAttribute("id", "variance_sign_" + scene_timing_count);
	varianceDROPDOWN.appendChild(createDropdownList("+"));
	varianceDROPDOWN.appendChild(createDropdownList("-"));
	varianceDROPDOWN.disabled = true;
	
	sceneCELL.appendChild(createTextBox("scene_no_" + scene_timing_count, 8, false,"text",""));
	pageCELL.appendChild(createTextBox("page_count_" + scene_timing_count, 4, false,"text",""));
	pageCELL.appendChild(document.createTextNode("\u00a0"));
	pageCELL.appendChild(document.createTextNode("\u00a0"));
	pageCELL.appendChild(createTextBox("page_count_num_" + scene_timing_count, 2, false,"text",""));
	pageCELL.appendChild(document.createTextNode(" /8"));
	esttimingCELL.appendChild(createTextBox("est_timing_" + scene_timing_count, 8, false,"text",""));
	actualtimingCELL.appendChild(createTextBox("actual_timing_" + scene_timing_count, 8, false,"text",""));
	varianceCELL.appendChild(varianceDROPDOWN);
	varianceCELL.appendChild(document.createTextNode("\u00a0"));
	varianceCELL.appendChild(document.createTextNode("\u00a0"));
	varianceCELL.appendChild(createTextBox("variance_" + scene_timing_count, 8, true,"text",""));
	cumulativerunningCELL.appendChild(createTextBox("cumulative_running_" + scene_timing_count, 8, true,"text",""));
	
	row.appendChild(leftright("left"));
	row.appendChild(sceneCELL);
	row.appendChild(pageCELL);
	row.appendChild(esttimingCELL);
	row.appendChild(actualtimingCELL);
	row.appendChild(varianceCELL);
	row.appendChild(cumulativerunningCELL);
	row.appendChild(leftright("right"));
	
	tbody.appendChild(row);
}

function leftright(side) {
	var cell = document.createElement("TD");
	cell.className = side;
	cell.appendChild(document.createTextNode("\u00a0"));
	return cell;
}
	
//function for textbox details
function createTextBox(id, size, ro, type, value) {
	var input =  document.createElement("INPUT");
	input.setAttribute("size", size);
	input.setAttribute("name", id);
	input.setAttribute("id", id);
	input.setAttribute("value", value);
	input.setAttribute("type", type);
	input.readOnly = ro;
	return input;
}

function createDropdownList(name) {
	var option = document.createElement("OPTION");
	option.setAttribute("value", name);
	option.appendChild(document.createTextNode(name));
	return option;
}