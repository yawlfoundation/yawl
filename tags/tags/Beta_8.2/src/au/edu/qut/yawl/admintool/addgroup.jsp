<%@page import="java.sql.*" %>
<%@page import="java.util.*" %>
<%@page import="au.edu.qut.yawl.admintool.*" %>
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

<% 	
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
<h2>Adding Organisational Groups</h2>
<table bgcolor="lightGrey">
<form method="post" action="http://localhost:8080/admintool/addServlet" name="addGroup">
	<input type="hidden" name="which_form" value="addGroup"/>
	<tr>
		<td>Organisational Group:</td>
		<td>
			<input type="text" name="group"/>
		</td>
	</tr>
	<tr>
		<td>Select type of the Organisational Group</td>
		<td><select name="grouptype">
			<option>Group</option>
			<option>Unit</option>
			<option>Team</option>
			<option>Branch</option>
			<option>Division</option>
			<option>Organisation</option>
		</select>
		</td>
	</tr>
	<tr>
		<td>Working day for this group starts at:</td>
		<td><select name="starttime">
			<option>00</option>
			<option>01</option>
			<option>02</option>
			<option>03</option>
			<option>04</option>
			<option>05</option>
			<option>06</option>
			<option>07</option>
			<option>08</option>
			<option>09</option>
			<option>10</option>
			<option>11</option>
			<option>12</option>
			<option>13</option>
			<option>14</option>
			<option>15</option>
			<option>16</option>
			<option>17</option>
			<option>18</option>
			<option>19</option>
			<option>20</option>
			<option>21</option>
			<option>22</option>
			<option>23</option>
		</select></td>
		<td>Hour</td>
	</tr>
	<tr>
		<td>Working day for this group ends at:</td>
		<td><select name="endtime">
			<option>00</option>
			<option>01</option>
			<option>02</option>
			<option>03</option>
			<option>04</option>
			<option>05</option>
			<option>06</option>
			<option>07</option>
			<option>08</option>
			<option>09</option>
			<option>10</option>
			<option>11</option>
			<option>12</option>
			<option>13</option>
			<option>14</option>
			<option>15</option>
			<option>16</option>
			<option>17</option>
			<option>18</option>
			<option>19</option>
			<option>20</option>
			<option>21</option>
			<option>22</option>
			<option>23</option>
		</select></td>
		<td>Hour</td>
	</tr>
	<tr>
		<td>
			<input type="submit" value=" Submit " name="action"/>
		</td>
	</tr>
</form>
</table>

<p>
<%@include file="footer.jsp" %>
</body>
</html>
