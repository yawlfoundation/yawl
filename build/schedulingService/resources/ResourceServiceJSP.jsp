<%
/*------------------------------------------------------------------------
 @author tbe
 @version $Id: SchedulingServiceJSP.jsp 20500 2010-07-12 12:53:17Z tbe $
--------------------------------------------------------------------------*/
%>

<%@ page import="org.apache.log4j.Logger" %>
<%@ page import="org.yawlfoundation.yawl.scheduling.resource.*" %>
<%@ page import="org.yawlfoundation.yawl.scheduling.util.Utils" %>
<%@ page import="java.util.*" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>

<%
	Logger logger = LogManager.getLogger(this.getClass());

	String objectName = request.getParameter("objectName");
	String prevFieldValue = request.getParameter("prevFieldValue");
	
	ResourceServiceInterface rs = ResourceServiceInterface.getInstance();
	Map<String, Object> objects = rs.getDropdownContent(objectName, prevFieldValue);
	
	String json = Utils.getJSON(objects);
	logger.debug("objects("+objectName+", "+prevFieldValue+") as json: " + json);
	out.print(json);

%>