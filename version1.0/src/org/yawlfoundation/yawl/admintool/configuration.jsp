<%@page import="org.yawlfoundation.yawl.engine.interfce.*" %>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>YAWL Administration and Monitoring Tool</title>
<meta name="Pragma" content="no-cache"/>
<meta name="Cache-Control" content="no-cache"/>
<meta name="Expires" content="0"/>
<link rel="stylesheet" href="./graphics/common.css">
</head>
<frameset rows="27%,*" border="0">
	<frame src="bannerframe.jsp" frameborder="no" noresize></frame>
	<frameset cols="25%,*">
		<frame name="mainframe" src="configurationmenu.html" frameborder="no" noresize/>
		<frame name="right" src="menumessage.html" frameborder="no" noresize/>
	</frameset>
</frameset>
<body>
<%@include file="checkLogin.jsp" %>
</body>
</html>