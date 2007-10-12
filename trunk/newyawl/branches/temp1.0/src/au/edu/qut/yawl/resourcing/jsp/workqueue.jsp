<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN"
            "http://www.w3.org/TR/html4/strict.dtd">
<%@ page import="au.edu.qut.yawl.resourcing.rsInterface.WorkQueueGateway"%>
<%@ page import="java.util.List" %>

<%
    WorkQueueGateway wq = new WorkQueueGateway();
    String userName = wq.getFullNameForUserID((String) session.getAttribute("userid"));
%>

<html>
    <head>
        <title>newYAWL Work Queues for <%= userName %></title>

   	    <style type="text/css">
		    @import "./dojo/dojo/resources/dojo.css";
  		    @import "./dojo/dijit/themes/soria/soria.css";
		    @import "./dojo/dijit/themes/soria/soria_rtl.css";
		    @import "./dojo/dijit/tests/css/dijitTests.css";
	    </style>

        <script type="text/javascript" src="./dojo/dojo/dojo.js"
                djConfig="isDebug: true, parseOnLoad: true"></script>
        
        <script type="text/javascript">
            dojo.require("dojo.parser");
            dojo.require("dijit.Toolbar");
            dojo.require("dijit.Tooltip");
            dojo.require("dijit.layout.LayoutContainer");
            dojo.require("dijit.layout.ContentPane");
            dojo.require("dijit.layout.TabContainer");
            dojo.require("dijit.form.ComboBox");
            dojo.require("dijit.form.Button");
            dojo.require("dojo.data.ItemFileReadStore");
		   
            function showChoice(box) {
                alert(box.options[box.selectedIndex].text)
            }

		    function setVal1(value) {
                console.debug("Selected: "+value);
            }
         </script>

    </head>

    <body class="soria">

    <h1 align=middle>
        <img src="./images/newYAWLdraft.gif"
             alt="Welcome to newYAWL 1.0" align="center"/>
    </h1>


    <h2>newYAWL Work Queues for <%= userName %></h2>


    <div dojoType="dijit.layout.LayoutContainer" id="main"
         style="width: 100%; height: 100%">

        <div dojoType="dijit.layout.ContentPane" layoutAlign="top"
            id="header" align="left"  style="background-color:#bcd2ee; padding: 5px">
       </div>


        <div dojoType="dijit.layout.ContentPane" layoutAlign="bottom"
            id="footer" align="left" style="background-color:#bcd2ee; padding: 5px">
               <span style="float:right;">newYAWL v1.0</span>
               <i>Status Information and messages will go here</i>
       </div>

       <div dojoType="dijit.layout.ContentPane" layoutAlign="left"
            id="lefter" align="left" style="width: 5px; height: 100%; background-color:#bcd2ee">
       </div>
       <div dojoType="dijit.layout.ContentPane" layoutAlign="right"
            id="righter" align="right" style="width: 5px; height: 100%; background-color:#bcd2ee">
       </div>


        <div id="mainTabContainer" dojoType="dijit.layout.TabContainer"
             layoutAlign="client" style="width:700px;height:500px">

             <div id="offerList" dojoType="dijit.layout.ContentPane"
		          title="Offered">
                 <select size="20" id="offerbox" style="width:100px; padding: 10px" onchange="javascript:showChoice(this)">
                 <%
                     List<String> l = wq.getQueuedItems() ;
                     for (String s: l) {
                  %>
                     <option><%= s %></option>

                  <% } %>
                  </select>

            </div>
            <div id="AllocateList" dojoType="dijit.layout.ContentPane"
                 title="Allocated">
                <select size="6" name="state1"
                    dojoType="dijit.form.ComboBox"
                    autocomplete="false"
                    value="California"
                    onChange="setVal1" >
                    <option selected="selected">California</option>
                    <option >Illinois</option>
                    <option >New York</option>
                    <option >Texas</option>
                </select>
             </div>
             <div id="startList" dojoType="dijit.layout.ContentPane"
                  title="Started" selected="true" style="padding: 10px">
                 <p><b>&nbsp WorkItem</b></p>
                 <select size="20" id="startbox" style="width:200px; padding: 5px" onchange="javascript:showChoice(this)">
				  	 <option value="A">Entry A</option>
					 <option value="B">Entry B</option>
					 <option value="C">Entry C</option>
					 <option value="D">Entry D</option>
					 <option value="E">Entry E</option>
					 <option value="F">Entry F</option>
				 </select>
             </div>
  <!--          <span dojoType="dijit.Tooltip" connectId="offerList">Click to see all offers.</span> -->
        </div>
   </div> 
</body></html>