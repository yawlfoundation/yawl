<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="java.sql.*" errorPage="" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<title>PDF Form Generator - Finish</title>
</head>

<body>
<a href="./repository/<%=session.getAttribute("fileName")%>">Download PDF form, <%=session.getAttribute("fileName")%>
</a>
</body>
</html>
