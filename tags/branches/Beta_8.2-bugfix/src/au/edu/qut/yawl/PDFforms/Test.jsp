<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="java.sql.*" errorPage="" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<title>PDF Demonstration</title>
<style type="text/css">
<!--
.style1 {
	font-family: Arial, Helvetica, sans-serif;
	font-weight: bold;
	font-size: 14px;
}
.style3 {font-family: Arial, Helvetica, sans-serif; font-size: 12px; }
-->
</style>
</head>

<body>
<form name="form1" method="post" action="handler">
  <p class="style1">This page simulates a Worklist Handler sending a workitem to the PDF Component.</p>
  <p><span class="style3">Work Item Record: </span><br>
    <textarea name="workitem" cols="50" rows="10"><itemRecord><caseID>C1234</caseID><taskID>T1234</taskID><status>Executing</status><uniqueID>U1234</uniqueID><data><Element><Name>Ignatius</Name><Address/><Phone>4-1381-9873</Phone><Age/></Element></data><specID>S1234</specID><enablementTime>99</enablementTime></itemRecord></textarea>
  </p>
  <p>
    <input type="submit" name="Submit" value="Submit">
</p>
</form>
</body>
</html>
