<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN"
                      "http://www.w3.org/TR/html4/strict.dtd">

<%@ page import="au.edu.qut.yawl.resourcing.rsInterface.WorkQueueGateway" %>

<%!
    WorkQueueGateway wqGateway ;

    public void jspInit() {
        ServletContext context = getServletContext();
        wqGateway = WorkQueueGateway.getInstance();

        // initialise the exception service add-ins to the worklist
       String _ixURI = context.getInitParameter("InterfaceX_BackEnd");
    }
%>

<style type="text/css">
 @import "./dojo/dojo/resources/dojo.css";
   @import "./dojo/dijit/themes/soria/soria.css";
 @import "./dojo/dijit/themes/soria/soria_rtl.css";
 @import "./dojo/dijit/tests/css/dijitTests.css";

 /* pre-loader specific stuff to prevent unsightly flash of unstyled content */
 #loader {
     padding:0;
     margin:0;
     position:absolute;
     top:0; left:0;
     width:100%; height:100%;
     background:#ededed;
     z-index:999;
     vertical-align:center;
 }
 #loaderInner {
     padding:5px;
     position:relative;
     left:0;
     top:0;
     width:175px;
     background:#3c3;
     color:#fff;
 }
</style>

<script type="text/javascript" src="./dojo/dojo/dojo.js"
        djConfig="isDebug: true, parseOnLoad: false"></script>

<script type="text/javascript">
    dojo.require("dojo.parser");
    dojo.require("dijit.Tooltip");
    dojo.require("dijit.form.TextBox");
    dojo.require("dijit.form.Button");
    dojo.require("dijit.layout.ContentPane");
    dojo.require("dijit.Dialog");

    function showChoice(box) {
        alert(box.options[box.selectedIndex].text)
    }

    function setVal1(value) {
        alert("Selected: "+value);
    }

    function hideLoader(){
        var loader = dojo.byId('loader');
        dojo.fadeOut({ node: loader, duration:500,
                       onEnd: function(){loader.style.display = "none";}}).play();
    }

    dojo.addOnLoad(function() {
        dojo.parser.parse(dojo.byId('theForm'));
        dojo.byId('loaderInner').innerHTML += " done.";
        setTimeout("hideLoader()",250);
    });
</script>



