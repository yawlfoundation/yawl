<%@page import="java.sql.*" %>
<%@page import="java.util.*" %>
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

<h2>Modifying roles</h2>

<% 	
	ServletContext context = getServletContext();
	String persistOn = context.getInitParameter("EnablePersistance");
	boolean _persistanceConfiguredOn = "true".equalsIgnoreCase(persistOn);
	if (_persistanceConfiguredOn) {
	if (request.getParameter("success") != null){
		String success = new String(request.getParameter("success"));
		System.out.println("Success:" + success);
		if (success.compareTo("true")==0) {
		%>
			<font color="green">The data was successfully entered into the database!</font>
			<p>
		<%	
		}
	}
%>	

<%
	if (request.getParameter("failure") != null){
		String failure = new String(request.getParameter("failure"));
		if(failure.compareTo("true") == 0) {
		%>
			<font color="red">The data was not entered into the database because an SQLException was thrown. 
			Check whether you entered valid values. More details about the Exception are available in the logfile of Tomcat.</font>
			<p>
		<%	
		}
	}
%>

<%
	if (request.getParameter("same") != null){
		String same = new String(request.getParameter("same"));
		if(same.compareTo("true") == 0) {
		%>
			<font color="red">A role cannot incorporate itself. Select 2 different roles.</font>
			<p>
		<%	
		}
	}
%>

<%
    DatabaseGatewayImpl _model = DatabaseGatewayImpl.getInstance(_persistanceConfiguredOn);

    String id = request.getParameter("role");
    String CHECKED_ATTRIBUTE_VALUE = " checked=\"checked\" ";

    Role[] roles = _model.getRoles();
     
    Resource[] resources = _model.getResources();

    Resource[] resInRole = _model.getResourcesPerformingRole(request.getParameter("role"));
%>

		

<p>
<h2>Details for a human resource</h2>
<table bgcolor="lightgrey">
<form method="post" action="./editRoleAction" name="SelectRole">
<input type="hidden" name="action" value="delRole"/>
<tr>
	<td>Select Role:</td>
	<td><select name="role" onchange="javascript:loadRoleDetails(document.forms['SelectRole'].role.options[document.forms['SelectRole'].role.selectedIndex].text);">
	      <option>
		--New Role--
	      </option>
			<%
				String roleRes = "";
				for (int i=0; i<roles.length; i++) {
				%>	
					<option <% if (roles[i].getRoleName().equals(id)) {out.write("SELECTED");} %>><%=roles[i].getRoleName() %></option>
				<%
				}
			%>
		</select></td></tr>
<tr>
            <td>
                <input type="submit" value=" Delete Role " name="action"/>
            </td>
</tr>
</form>
</table>
</p>
<p>

<table bgcolor="lightGrey">
Enter the name of the role you would like to add and click Add Role
<form method="post" action="./editRoleAction" name="addRole">
	<input type="hidden" name="action" value="addRole"/>
	<tr>
		<td>Role:</td>
		<td>
			<input type="text" name="addrole"/>
		</td>
	</tr>
	<tr>
		<td>
			<input type="submit" value=" Add Role " name="action"/>
		</td>
	</tr>
</form>
</table>
</p>
<h2>Set the human resources for a role</h2>
<table bgcolor="lightgrey">
<form method="post" action="./editRoleAction" name=humanform onsubmit="selectall();">
<input type="hidden" name="action" value="assignRole2Humans"/>
<input type="hidden" name="role" value="<%=id%>"/>
<input type="hidden" name="selecthuman"/>
	<tr>
		<td>Available Resources :</td>
<%--		<td rowspan=1>--%>
		<td>		
		<td>		
		<td>Allocated Resources :</td>
    </tr>
<tr>
   <td rowspan=2>
	<select name="selecthuman2" size=10  style="width: 150px;">
			<%
		if (resources != null) {
		    List roleres = new ArrayList();
		    if (resInRole!=null) {
			roleres.addAll(Arrays.asList(resInRole));
		    }
		    List all = Arrays.asList(resources);

			for (int i=0; i<all.size(); i++) {
        			if (resources[i] instanceof HumanResource) {

			          HumanResource resc = (HumanResource) all.get(i);
				  System.out.println(resc.getRsrcID());
				  boolean found = false;
 				    for (int j = 0; j < roleres.size();j++) {
				      if ((((HumanResource) roleres.get(j)).getRsrcID()).equals(resc.getRsrcID())) {
						found = true;
					}	
				   }
			          if(!found) {	
				  %>	
					<option><%=resources[i].getRsrcID() %></option>
				  <%
				}
			     }
                          }
                       }
		%>

	</select></td>
<td>

<A 
onMouseOver="return changeImage()" 
onMouseOut= "return changeImageBack()" 
onMouseDown="return handleMDown()" 
onMouseUp="return handleMUp()"
><img
name="jsbutton" src="image1.jpg" width="28" height="28" border="0" 
alt="javascript button"></A> 
</td>
<td></td>
<td ROWSPAN=2>

<select name=allocateselect size=10 multiple style="width: 150px;">

			<%
				if (resInRole != null) {
					for (int i=0; i<resInRole.length; i++) {
					//if (resources[i] instanceof HumanResource) {
					%>	
						<option><%=resInRole[i].getRsrcID() %> <%out.print(resInRole.length);%></option>
					<%
					}//}
				}
			%>
</select>

</td>
</tr>

<tr>
<td>
<A 
onMouseOver="return changeImage2()" 
onMouseOut= "return changeImageBack2()" 
onMouseDown="return handleMDown2()" 
onMouseUp="return handleMUp2()"
><img
name="jsbutton2" src="image2.jpg" width="28" height="28" border="0" 
alt="javascript button"></A> 
<td>
</tr>
	</tr>
	<tr>
		<td>
			<input type="submit" value=" Update Resources by Role " name="action"/>
		</td>
	</tr>
</form>
</table>



<SCRIPT language="JavaScript">


upImage = new Image();
upImage.src = "image1.jpg";
downImage = new Image();
downImage.src = "image3.jpg"
normalImage = new Image();
normalImage.src = "image1.jpg";

upImage2 = new Image();
upImage2.src = "image2.jpg";
downImage2 = new Image();
downImage2.src = "image4.jpg"
normalImage1 = new Image();
normalImage1.src = "image2.jpg";

function changeImage()
{
  document.images["jsbutton"].src= upImage.src;
  return true;
}

function changeImage2()
{
  document.images["jsbutton2"].src= upImage2.src;
  return true;
}

function changeImageBack() 
{
   document.images["jsbutton"].src = normalImage.src;
   return true;
}

function changeImageBack2() 
{
   document.images["jsbutton2"].src = normalImage2.src;
   return true;
}

function handleMDown()
{
 document.images["jsbutton"].src = downImage.src;
 return true;
}

function handleMDown2()
{
 document.images["jsbutton2"].src = downImage2.src;
 return true;
}



function handleMUp()
{

 changeImage();

 var s = document.forms['humanform'].selecthuman2.options[document.forms['humanform'].selecthuman2.selectedIndex].text;

	 document.forms['humanform'].allocateselect.options[document.forms['humanform'].allocateselect.length] = 

	  new Option(s,s);

	 document.forms['humanform'].selecthuman2.options[document.forms['humanform'].selecthuman2.selectedIndex] = null;

//      document.forms['humanform'].selectrole.value = s;
//	loc = window.location;
//	document.humanform.submit();
//	window.location = loc;

 	return true;

}


function handleMUp2()
{

 changeImage2();


 var s = document.forms['humanform'].allocateselect.options[document.forms['humanform'].allocateselect.selectedIndex].text;

	 document.forms['humanform'].selecthuman2.options[document.forms['humanform'].selecthuman2.length] = 

	  new Option(s,s);

	 document.forms['humanform'].allocateselect.options[document.forms['humanform'].allocateselect.selectedIndex] = null;


 return true;
}


function selectall() {
  var selected = "";
  for (var i = 0; i < document.humanform.allocateselect.length ; i++) {
   selected = selected + "$" + document.forms['humanform'].allocateselect.options[i].text;
 }

 document.forms['humanform'].selecthuman.value = selected;

 return true;

}


function loadRoleDetails(role) {

   window.location = "roles.jsp?role=" + role;

   return true;
}

</SCRIPT> 
<%
} else {
  out.println("<a> <font color=\"red\">This page has been disabled because persistence is switched off!</font></a>");
}
%>
</body>
</html>
