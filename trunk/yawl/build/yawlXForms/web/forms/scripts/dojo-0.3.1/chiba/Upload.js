/*
	Copyright (c) 2004-2006, The Dojo Foundation
	All Rights Reserved.

	Licensed under the Academic Free License version 2.1 or above OR the
	modified BSD license. For more information on Dojo licensing, see:

		http://dojotoolkit.org/community/licensing.shtml
*/

dojo.provide("chiba.Upload");

dojo.require("dojo.widget.*");
dojo.require("dojo.event");
dojo.require("dojo.html");

dojo.widget.defineWidget(
	"chiba.XFUpload",
	dojo.widget.HtmlWidget,
	{
		widgetType: "XFUpload",
        templatePath: dojo.uri.dojoUri('chiba/templates/HtmlUpload.html'),

        // parameters
        disabled: "",
        id: "",
        xformsId:"",
        name: "",
        xfreadonly: "false",
        tabIndex: -1,
        css:"",
        enabledNodes:new Array(),

        inputNode: null,
        progress: null,
        progressBackground:null,
        fillInTemplate: function(){
            //todo: this var is a candidate for a (to be implemented) superclass
            this.xformsId = this.id.substring(0,this.id.length - 6);
//            dojo.debug("Upload xformsId: " + this.xformsId);

            this.progress.id = this.xformsId + "-progress";
            this.progressBackground.id = this.xformsId + "-progress-bg";

            var xformsControl = dojo.byId(this.xformsId);
            _addClass(xformsControl,this.css);

            if(this.xfreadonly == "true"){
                this.inputNode.disabled = true;
            }

        },
        onChange: function(){
            if(this.xfreadonly != "true"){
                var action = confirm("Really upload?");
                if(action){
                    this._submitFile(this.inputNode);
                }else{
                    this.inputNode.value = "";
                }
            }else{
               this.inputNode.disabled = true;
            }
        },
        updateProgress: function (value){
            var progressDiv   = document.getElementById(this.xformsId + "-progress-bg");
            if(value!=0){
                progressDiv.style.width = value + "%";
            }

            if(value == 100){
                //stop polling
                clearInterval(progressUpdate);

                var foo = this.enabledNodes;
                for(var i=0,j=this.enabledNodes.length; i<j; i++){
                    var control = this.enabledNodes.pop();
                    control.disabled = false;
                }

                //reset progress bar
                var elemId = this.xformsId + "-progress-bg";

                setTimeout("document.getElementById('" + elemId + "').style.width=0",2000);
                setTimeout("Effect.BlindUp('" + this.xformsId + "-progress')",1500);
            }
        },
        _submitFile: function(){
            //disable all uploads that have a different id than the current.
            //this ensures that only the current upload file will be sent and re-sending for multiple uploads is avoided
            var foo = document.getElementsByClassName("upload","chibaform");
            for(var i=0,j=foo.length; i<j; i++){
                var ctrl = dojo.byId(foo[i].id + "-value");
                if(ctrl.id != this.id && !ctrl.disabled){
                    //store enabled controls in array for later re-activation
                    ctrl.disabled=true;
                    //store for restoring original state after submit
                    this.enabledNodes.push(ctrl);
                }
            }


            Effect.BlindDown(this.xformsId + "-progress");

            var path = this.inputNode.value;
            var filename = path.substring(path.lastIndexOf("/")+1);

            //polling Chiba for update information and submit the form
            var sessionKey = dojo.byId("chibaSessionKey").value;
            progressUpdate = setInterval("Flux.fetchProgress(updateUI,'" + this.xformsId + "','" + filename + "','" + sessionKey + "')",500);
            document.forms[0].target= "UploadTarget";
            document.forms[0].submit();
            return true;

        }
    }
);


