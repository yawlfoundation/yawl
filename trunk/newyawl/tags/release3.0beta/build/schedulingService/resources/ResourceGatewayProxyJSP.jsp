<%
/*------------------------------------------------------------------------
 @author tbe
 @version $Id: SchedulingServiceJSP.jsp 20500 2010-07-12 12:53:17Z tbe $
 
 proxy class to avoid CSRF problems on access from custom form to resource
 service via AJAX
--------------------------------------------------------------------------*/
%>

<%@ page import="org.apache.log4j.Logger" %>
<%@ page import="java.util.*" %>
<%@ page import="java.io.IOException" %>
<%@ page import="org.yawlfoundation.yawl.scheduling.util.Utils" %>
<%@ page import="org.yawlfoundation.yawl.scheduling.ConfigManager" %>
<%@ page import="org.yawlfoundation.yawl.scheduling.util.PropertyReader" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>

<%

	Logger logger = Logger.getLogger(this.getClass());
	String url = PropertyReader.getInstance().getYAWLProperty("ResourceGatewayClient.backEndURI");
	String ret = Utils.sendRequest(url, request.getParameterMap());
	//logger.debug("------------params="+Utils.toString(request.getParameterMap())+", ret=" + ret);
	
	if (ret.startsWith("<failure>")) {
		//throw new IOException(ret);
		logger.warn("ret=" + ret);
	} else if (!ret.isEmpty()) {
		int idx1 = ret.indexOf("("), idx2 = ret.lastIndexOf(")");
		String ret2 = ret.substring(idx1+1, idx2); // remove jsonp
		Map map = Utils.parseJSON2Map(ret2);
		map.remove("None");
		SortedMap sortedMap = new TreeMap(new Utils.ValueComparer(map, false));
		sortedMap.putAll(map);
		ret = ret.substring(0, idx1+1) + Utils.getJSON(sortedMap) + ret.substring(idx2); // add jsonp
	} else {
		ret = ((String[])request.getParameterMap().get("callback"))[0] + "({})";
	}
	
	//logger.debug("------------ret=" + ret);
	//logger.debug("------------ret2=" + new String(ret.getBytes(), "UTF-8"));
	out.print(ret);

%>