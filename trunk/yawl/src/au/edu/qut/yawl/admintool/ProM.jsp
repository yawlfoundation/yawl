<%@page import="au.edu.qut.yawl.admintool.*" %>
<%@page import="au.edu.qut.yawl.admintool.model.*" %>
<%@page import="au.edu.qut.yawl.engine.interfce.*" %>

<html xmlns="http://www.w3.org/1999.xhtml">
    <head>
        <title>YAWL Administration and Monitoring Tool</title>
        <meta name="Pragma" content="no-cache"/>
        <meta name="Cache-Control" content="no-cache"/>
        <meta name="Expires" content="0"/>
        <link rel="stylesheet" href="./graphics/common.css">
    </head>
    <body>

        <script type="text/javascript">

        </script>

        <%@include file="checkLogin.jsp" %>
        <%@include file="YAWLnavigation.jsp" %>

        <% 	
            ServletContext context = getServletContext();
            String persistOn = context.getInitParameter("EnablePersistance");
            boolean _persistanceConfiguredOn = "true".equalsIgnoreCase(persistOn);
            if (_persistanceConfiguredOn) {
	            DatabaseGatewayImpl _model = DatabaseGatewayImpl.getInstance(_persistanceConfiguredOn);


	            String[] specIDs = _model.getSpecs();
            }



        %>	



	if (request.getParameter("xmllink") != null){
		String xmllink = new String(request.getParameter("xmllink"));
		%>
			<a href="<%out.print(xmllink);%>">Prom XML</a>
		<%
	}

	if (request.getParameter("wrongdate") != null){
		%>
			<a> The date was entered in the wrong format </a>
		<%
	}


	<form method="post" action="./MakeXML" name="MakeXML">

	<table width="95%" bgcolor="lightgrey">
	<tr>
							<td width="200"/>
							<td width="120"/>
							<td width="120"/>
							<td width="120"/>
							<td width="120"/>
							</tr>


          <tr>
             <td> 
                Specification </td>
             <td>
                <select name="selectspec">

                  <option>--All--</option>
                     <%
                       for (int i = 0; i < specIDs.length;i++) {
                        %><option><%
                        out.print(specIDs[i]);
                        %></option><%
                       }%>
                </select>

             </td>
         </tr>


	<tr>
	<td>Start Time: </td>
        <td>
            <input type="text" name="from" value=""/>
        </td>
	</tr>
	<tr>
	<td>End Time</td>
        <td>
            <input type="text" name="until" value=""/>

        </td>
	</tr>
	</tr>
		<td>
			<input type="submit" value="Create ProM XML file" name="action"/>
		</td>
	<tr>
	</form>

     <%@include file="footer.jsp" %>
    </body>


</html>