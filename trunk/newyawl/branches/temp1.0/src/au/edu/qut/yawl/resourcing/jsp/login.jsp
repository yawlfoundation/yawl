<%--<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN"--%>
                      <%--"http://www.w3.org/TR/html4/strict.dtd">--%>

<%--<%@ page import="au.edu.qut.yawl.resourcing.rsInterface.WorkQueueGateway,--%>
                 <%--java.util.List" %>--%>

<%--<% WorkQueueGateway wq = new WorkQueueGateway(); %>--%>

<html>
    <head>
        <title>newYAWL Login</title>

   	    <%--<style type="text/css">--%>
		    <%--@import "./dojo/dojo/resources/dojo.css";--%>
  		    <%--@import "./dojo/dijit/themes/soria/soria.css";--%>
		    <%--@import "./dojo/dijit/themes/soria/soria_rtl.css";--%>
		    <%--@import "./dojo/dijit/tests/css/dijitTests.css";--%>

            <%--/* pre-loader specific stuff to prevent unsightly flash of unstyled content */--%>
            <%--#loader {--%>
                <%--padding:0;--%>
                <%--margin:0;--%>
                <%--position:absolute;--%>
                <%--top:0; left:0;--%>
                <%--width:100%; height:100%;--%>
                <%--background:#ededed;--%>
                <%--z-index:999;--%>
                <%--vertical-align:center;--%>
            <%--}--%>
            <%--#loaderInner {--%>
                <%--padding:5px;--%>
                <%--position:relative;--%>
                <%--left:0;--%>
                <%--top:0;--%>
                <%--width:175px;--%>
                <%--background:#3c3;--%>
                <%--color:#fff; 			--%>
            <%--}--%>
        <%--</style>--%>

        <%--<script type="text/javascript" src="./dojo/dojo/dojo.js"--%>
                <%--djConfig="isDebug: true, parseOnLoad: false"></script>--%>

   <%--<!--     <script type="text/javascript" src="./dojo/dijit/dijit.js"></script>--%>
        <%--<script type="text/javascript" src="./dojo/dijit/dijit-all.js"></script>--%>
   <%---->--%>
        <%--<script type="text/javascript">--%>
            <%--dojo.require("dojo.parser");--%>
            <%--dojo.require("dijit.Tooltip");--%>
            <%--dojo.require("dijit.form.TextBox");--%>
            <%--dojo.require("dijit.form.Button");--%>
            <%--dojo.require("dijit.layout.ContentPane");--%>
            <%--dojo.require("dijit.Dialog");--%>

            <%--logMessage = console.debug;--%>

            <%--var thirdDlg;--%>

            <%--function showChoice(box) {--%>
                <%--alert(box.options[box.selectedIndex].text)--%>
            <%--}--%>

		    <%--function setVal1(value) {--%>
                <%--alert("Selected: "+value);--%>
            <%--}--%>



                    <%--function hideLoader(){--%>
			<%--var loader = dojo.byId('loader');--%>
			<%--dojo.fadeOut({ node: loader, duration:500,--%>
				<%--onEnd: function(){--%>
					<%--loader.style.display = "none";--%>
				<%--}--%>
			<%--}).play();--%>
		<%--}--%>

		<%--dojo.addOnLoad(function() {--%>
			<%--dojo.parser.parse(dojo.byId('loginForm'));--%>
            <%--dojo.byId('loaderInner').innerHTML += " done.";--%>
			<%--setTimeout("hideLoader()",250);--%>
		<%--});--%>
         <%--</script>--%>

            <%@ include file="header.jsp"  %>

    </head>

    <body class="soria" id="theForm">

        <%@ include file="banner.jsp"  %>

        <!-- Include check login code -->
        <%@include file="checkLogin.jsp"%>


    <!--<!-- basic preloader: --><!--<div id="loader"><div id="loaderInner">Loading Page ... </div></div>-->

        <!--<h1 align="center">-->
            <!--<img src="./images/newYAWL.jpg" alt="newYAWL 1.0"/>-->
        <!--</h1>-->


        <%
        boolean loginFailed = (request.getAttribute("failed") != null);
        if (request.getMethod().equals("GET") || loginFailed) {
            if (loginFailed) {
        %>
               <h2 align="center"><font color='red'><em>
                   <%=request.getAttribute("reason")%>. Please try again.
               </em></font></h2>
        <%
            }
            else {
        %>
               <h2 align="center">Please enter your username and password</h2>
        <%
            }
        %>

           <p> </p>

           <center>
              <form method="post" action="<%=  request.getRequestURL().toString()  %>">
                 <div dojoType="dijit.layout.ContentPane"
                      id="lefter" align="left"
                      style="width: 154px; height: 160px; border: 1px solid black;
                      padding: 10px; background-color: #e2ecf3">

                     <label for="txtUserid"><b>User Name</b></label>
                     <input type="text" length="20" name="txtUserid"
                            id="txtUserid" dojoType="dijit.form.TextBox"><br>
                     <p> </p>

                     <label for="txtPassword"><b>Password</b></label>
                     <input type="password" length="20" name="txtPassword"
                            id="txtPassword" dojoType="dijit.form.TextBox"><br>
                     <p></p>
                     <span dojoType="dijit.Tooltip"  connectId="txtPassword">
                          Enter a password between 4 and 10 characters
                     </span>

                     <div align="center">
                         <button id="btnLogin" dojoType="dijit.form.Button"
                                 type="submit">
             	               <b>Login</b>
		                 </button>
 		                 <span dojoType="dijit.Tooltip" connectId="btnLogin">
                             Click to login
                         </span>
                     </div>
                 </div>

              </form>
          </center>
    <p> </p>
    <hr align="center" width="300" size="2">

    <div align="center">
        <small>
            YAWL is distributed under the
            <a href="http://www.gnu.org/copyleft/lesser.html">LGPL</a>.
        </small>
    </div>


        <%
        }
        else {                     // request.getMethod().equals(POST)
            String userid = request.getParameter("txtUserid");
            String password = request.getParameter("txtPassword");
            sessionHandle = wqGateway.login(userid, password);
            if (wqGateway.successful(sessionHandle)){
               session.setAttribute("sessionHandle", sessionHandle);
               session.setAttribute("userid", userid);
               application.getRequestDispatcher("/workqueue").forward(request, response);
            }
            else {
                request.setAttribute("failed", "true");
                request.setAttribute("reason", sessionHandle) ;
                application.getRequestDispatcher("/login").forward(request, response);                
            }
        }
        %>



    </body>
</html>