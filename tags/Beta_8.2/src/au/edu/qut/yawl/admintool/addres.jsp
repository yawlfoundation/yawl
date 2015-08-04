<%@page import="java.sql.*" %>
<%@page import="java.util.*" %>
<%@page import="au.edu.qut.yawl.admintool.DatabaseGatewayImpl" %>
<%@page import="au.edu.qut.yawl.admintool.model.Resource" %>
<%@page import="au.edu.qut.yawl.admintool.model.Role" %>
<%@page import="au.edu.qut.yawl.admintool.model.HumanResourceRole" %>
<%@page import="au.edu.qut.yawl.admintool.model.HumanResource" %>
<%@page import="au.edu.qut.yawl.engine.interfce.*" %>


<h2>Create and Modify Resources</h2>

<p>
Enter the details of the resource you would like to add to the system and click submit.
<p>
<%
	ServletContext context = getServletContext();
	String persistOn = context.getInitParameter("EnablePersistance");
	boolean _persistanceConfiguredOn = "true".equalsIgnoreCase(persistOn);

        DatabaseGatewayImpl _model = DatabaseGatewayImpl.getInstance(_persistanceConfiguredOn);
	if (request.getParameter("success") != null){
		String success = new String(request.getParameter("success"));
		if (success.compareTo("true")==0) {
		%>
			<font color="green">The data was successfully entered into the YAWL system!</font>
			<p/>
		<%
		}
	}
	else if (request.getParameter("failure") != null){
		String failureMsg = new String(request.getParameter("failure"));
		%>
			<font color="red">
            <%= failureMsg %>  <br />
			Check whether you entered valid values.
			</font>
		<%
	}

	String id = "";
	String CHECKED_ATTRIBUTE_VALUE = " checked=\"checked\" ";

    HumanResource hResource = null;
    Resource resource = null;

    id = request.getParameter("user");

    if(id == null) {
        id = request.getParameter("resourceID");
    }

    if (!"--New User--".equals(id)) {
        resource =  _model.getResource(id);
        if (resource instanceof HumanResource) {
            hResource = (HumanResource) resource;
        }
    }

    Resource[] resources = _model.getResources();

%>

<p>
<h2>Details for a human resource</h2>

<form method="post" action="./editResourceAction" name="delResource">
<input type="hidden" name="action" value="delresource"/>

<table bgcolor="lightgrey">
    <tr>
        <td>Select Human ResourceID:</td>
        <td><select name="selectresource"
            onchange="javascript:loadUserDetails(this.options[this.selectedIndex].text);">
              <option>
            --New User--
              </option>
                <%
                    for (int i = 0; i < resources.length; i++) {
                            %>
                            <option
                            <%= resources[i].getRsrcID().equalsIgnoreCase(id)? "selected=\"selected\"":"" %>
                            >
                                <%= resources[i].getRsrcID() %>
                            </option>
                            <%
                    }
                %>
            </select></td>
            </tr>
            <td>
                <input type="submit" value=" Delete Resource " name="action"/>
            </td>
        </tr>

</table>
</form>

</p>
<form method="post" action="./editResourceAction" name="addResource">
<input type="hidden" name="action" value="addResource"/>

<table width="95%" bgcolor="lightgrey">
<tr>
							<td width="200"/>
							<td width="120"/>
							<td width="120"/>
							<td width="120"/>
							<td width="120"/>
							</tr>
	<tr>
		<td>Resource ID</td>
        <td>
            <input type="text" name="resourceID" value="<%=id != null ? id : ""%>"/>
        </td>
	</tr>
	<tr>
		<td>Description</td>
        <td>
            <input type="text" name="resdescription"
                value="<%= (resource != null && resource.getDescription() != null) ?
                            resource.getDescription():""%>"/>
        </td>
	</tr>
	<tr>
		<td>Is of Type</td>
		<td>
            <input type="radio" name="type" value="Human"
            <%= resource instanceof HumanResource? CHECKED_ATTRIBUTE_VALUE:"" %>/>Human
        </td>
		<td>
            <input type="radio" name="type" value="Non-Human"
            <%= resource instanceof Resource && !(resource instanceof HumanResource) ?
                CHECKED_ATTRIBUTE_VALUE:"" %> />Non-Human
        </td>
	</tr>
	<tr>
		<td>
            <font color="blue">
            If the resource is of type 'Human' fill in the fields below:
            </font>
        </td>
	</tr>
	<tr>
		<td>Given Name:</td>
		<td>
			<input type="text" name="givenname"
                value="<%= (resource instanceof HumanResource && hResource.getGivenName() != null)?
                            hResource.getGivenName(): ""%>"
            />
		</td>
	</tr>
	<tr>
		<td>Surname:</td>
		<td>
			<input type="text" name="surname"
                value="<%= (resource instanceof HumanResource && hResource.getSurname() != null) ?
                            hResource.getSurname():"" %>"
            />
		</td>
	</tr>
	<tr>
		<td>Has access to:</td>
		<td>
            <input type="radio" name="usertype" value="User"
                <%= hResource != null && !hResource.getIsAdministrator() ?
                    CHECKED_ATTRIBUTE_VALUE: ""%>
            />Worklist
        </td>
		<td>
            <input type="radio" name="usertype" value="Admin"
                <%= hResource != null && hResource.getIsAdministrator() ?
                    CHECKED_ATTRIBUTE_VALUE: ""%>
            />Administration Tool
        </td>
	</tr>
	<tr>
		<td>Initial Password:</td>
		<td>
			<input type="password" name="password" value=""/>
		</td>
	</tr>
	<tr>
		<td>Confirm Password:</td>
		<td>
			<input type="password" name="password2" value=""/>
		</td>
	</tr>
		<td>
			<input type="submit" value=" Update Resource " name="action"/>
		</td>
	<tr>
</table>
</form>

<%
if (_persistanceConfiguredOn) {
%>
<h2>Set the roles for a human resource</h2>
<form method="post" action="./editResourceAction" name=resourceform onsubmit="selectall();">
<input type="hidden" name="action" value="assignHuman2Roles"/>
<input type="hidden" name="resourceRole" value="<%=id%>"/>
<input type="hidden" name="selectrole"/>

<table bgcolor="lightgrey">
	<tr>
		<td>Available Roles :</td>
<%--		<td rowspan=1>--%>
		<td></td>
		<td></td>
		<td>Allocated Roles :</td>
    </tr>
    <tr>
        <td rowspan=2>
        <select name="selectrole2" size=10  style="width: 150px;">
		<%
            List rolesByResource = new ArrayList();
            if(resource != null){
		Role[] roles= _model.getRolesPerformedByResource(resource.getRsrcID());
		if (roles != null) {
	                rolesByResource.addAll(Arrays.asList(roles));
		}
                List allRoles = Arrays.asList(_model.getRoles());
                for (int i = 0; i < allRoles.size(); i++) {
                    Role role = (Role) allRoles.get(i);
                    if (!rolesByResource.contains(role)) {
                    %>
                        <option><%= role.getRoleName() %></option>
                    <%
                    }
                }
            }
        %>
		</select>
        </td>
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

	<td>
	</td>

        <td rowspan="2">
            <select name=allocateselect size=10 multiple style="width: 150px;">
            <%
                  for(int j = 0; j < rolesByResource.size();j++) {
                      Role role = (Role) rolesByResource.get(j);
                  %>
                    <option><%= role.getRoleName() %></option>
                      <%
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
        </td>
    </tr>
	<tr>
		<td>
			<input type="submit" value=" Update Roles By Resource " name="action"/>
		</td>
	</tr>
</table>
</form>


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

 var s = document.forms['resourceform'].selectrole2.options[document.forms['resourceform'].selectrole2.selectedIndex].text;

	 document.forms['resourceform'].allocateselect.options[document.forms['resourceform'].allocateselect.length] =

	  new Option(s,s);

	 document.forms['resourceform'].selectrole2.options[document.forms['resourceform'].selectrole2.selectedIndex] = null;

//      document.forms['resourceform'].selectrole.value = s;
//	loc = window.location;
//	document.resourceform.submit();
//	window.location = loc;

 	return true;
}

function handleMUp2()
{
 changeImage2();


 var s = document.forms['resourceform'].allocateselect.options[document.forms['resourceform'].allocateselect.selectedIndex].text;

	 document.forms['resourceform'].selectrole2.options[document.forms['resourceform'].selectrole2.length] =

	  new Option(s,s);

	 document.forms['resourceform'].allocateselect.options[document.forms['resourceform'].allocateselect.selectedIndex] = null;


 return true;
}

function selectall() {
  var selected = "";
  for (var i = 0; i < document.resourceform.allocateselect.length ; i++) {
   selected = selected + "$" + document.forms['resourceform'].allocateselect.options[i].text;
 }

 document.forms['resourceform'].selectrole.value = selected;

 return true;
}
</SCRIPT>
<%
}
%>
<SCRIPT language="JavaScript">
function loadUserDetails(user) {

   window.location = "organizational.jsp?user=" + user;

   return true;


}
</SCRIPT>

</body>
</html>
