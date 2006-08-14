/*
	Copyright (c) 2004-2006, The Dojo Foundation
	All Rights Reserved.

	Licensed under the Academic Free License version 2.1 or above OR the
	modified BSD license. For more information on Dojo licensing, see:

		http://dojotoolkit.org/community/licensing.shtml
*/

dojo.provide("chiba.Boolean");

dojo.require("dojo.widget.*");
dojo.require("dojo.event");
dojo.require("dojo.html");

/*
todo:
- support incremental
*/
dojo.widget.defineWidget(
	"chiba.XFBoolean",
	dojo.widget.HtmlWidget,
	{
		widgetType: "XFBoolean",
        templatePath: dojo.uri.dojoUri('chiba/templates/HtmlCheckBox.html'),

        // parameters
        id: "",
        name: "",
        checked:"",
        tabIndex: 1,
        xfreadonly: "false",
        xfincremental: "true",
        imgNode: null,
        inputNode: null,
        postMixInProperties: function(){
        },
        fillInTemplate: function(args, frag) {
            if(this.checked != "true") {
                this.inputNode.checked = null;
            }
            if(this.xfreadonly == "true"){
                this.inputNode.disabled = true;
            }
            if(this.xfincremental == "true"){
                dojo.event.connect(this.inputNode, "onchange", this, "_updateControl");
            }else{
                dojo.event.connect(this.inputNode, "onblur", this, "_updateControl");
            }
        },
        _updateControl: function(){
           if(this.xfreadonly != "true"){
                this.checked = !this.checked;
                this.inputNode.checked = this.checked;

                DWREngine.setOrdered(true);
                DWREngine.setErrorHandler(handleExceptions);
                var sessionKey = document.getElementById("chibaSessionKey").value;
                Flux.setXFormsValue(updateUI,  this.widgetId.substring(0,this.widgetId.length - 6), this.checked,sessionKey);
            }else{
               this.inputNode.disabled = true;
           }
        }
    }
);


