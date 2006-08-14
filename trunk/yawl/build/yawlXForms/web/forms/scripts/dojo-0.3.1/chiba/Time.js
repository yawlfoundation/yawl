/*
	Copyright (c) 2004-2006, The Dojo Foundation
	All Rights Reserved.

	Licensed under the Academic Free License version 2.1 or above OR the
	modified BSD license. For more information on Dojo licensing, see:

		http://dojotoolkit.org/community/licensing.shtml
*/

dojo.provide("chiba.Time");

dojo.require("dojo.widget.*");
dojo.require("dojo.event");
dojo.require("dojo.html");
dojo.require("dojo.widget.Spinner");
dojo.require("dojo.widget.validate");

/*
todo:
- support incremental
*/
dojo.widget.defineWidget(
	"chiba.XFTime",
	dojo.widget.HtmlWidget,
	{
		widgetType: "XFTime",
        templatePath: dojo.uri.dojoUri('chiba/templates/HtmlTime.html'),

        // parameters
        id: "",
        name: "",
        value:"",
        hoursInputWidget:null,
        hoursSpinnerWidget:null,
        inputNode:null,


        postMixInProperties: function(){
        },
        fillInTemplate: function(args, frag) {
            var hoursInputNode = document.createElement("span");
            this.inputNode.appendChild(hoursInputNode);
            var datePropsHoursInput = { value: this.value, delta:"1", min:"1", max:"60",seperator:"", maxlength:"2", id:this.id+"-hours", widgetId:this.widgetId+"-hours"};
            this.hoursInputWidget = dojo.widget.createWidget("AdjustableIntegerTextBox", datePropsHoursInput, hoursInputNode);

            var hoursSpinnerNode = document.createElement("span");
            hoursInputNode.appendChild(hoursSpinnerNode);
            var dateProbsHoursSpinner = {inputWidgetId:this.widgetId+"-hours"};
            this.hoursSpinnerWidget = dojo.widget.createWidget("Spinner", dateProbsHoursSpinner, hoursSpinnerNode);

            dojo.debug("WidgetId: " + this.widgetId);
            dojo.debug("Id: " + this.id);
        }
    }
);

