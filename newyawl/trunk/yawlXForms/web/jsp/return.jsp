<%@ page import="java.io.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.text.DateFormat" %>
<%@ page import="org.w3c.dom.Document" %>
<%@ page import="org.chiba.xml.util.*" %>
<%@ page import="org.chiba.xml.xforms.*" %>
<%@ page import="org.chiba.util.DateUtil" %>

<%@ page session="true" %>
<%@ page errorPage="error.jsp" %>

<%
	//check the session for a processor-instance
	ChibaProcessor cp = (ChibaProcessor)session.getAttribute("chiba.processor");
	if (cp == null) {
		%>
		Fatal Error: No Processor instance found in your session.
		<%
		return;
	}

    cp.getResponse(out);
%>