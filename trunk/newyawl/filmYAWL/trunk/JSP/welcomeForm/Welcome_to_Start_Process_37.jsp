<%@ page import="java.io.*" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.net.URL" %>
<%@ page import="java.net.URLConnection" %>

<%@ page import="java.math.BigInteger" %>
<%@ page import="com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl" %>
<%@ page import="java.text.SimpleDateFormat" %>


<%@ page import="javax.xml.bind.JAXBElement" %>
<%@ page import="javax.xml.bind.JAXBContext" %>
<%@ page import="javax.xml.bind.Marshaller" %>
<%@ page import="org.yawlfoundation.sb.welcometoproduction.*"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>Welcome to the DPR Production Process</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<script language="javascript">
function getParam(name)
{
  var start=location.search.indexOf("?"+name+"=");
  if (start<0) start=location.search.indexOf("&"+name+"=");
  if (start<0) return '';
  start += name.length+2;
  var end=location.search.indexOf("&",start)-1;
  if (end<0) end=location.search.length;
  var result='';
  for(var i=start;i<=end;i++) {
    var c=location.search.charAt(i);
    result=result+(c=='+'?' ':c);
  }
  //window.alert('Result = '+result);
  return unescape(result);
}

function getParameters(){
	document.form1.workItemID.value = getParam('workItemID');
	document.form1.userID.value = getParam('userID');
	document.form1.sessionHandle.value = getParam('sessionHandle');
	document.form1.specID.value = getParam('specID');
	document.form1.submit.value = "htmlForm";
}
</script>
</head>

<body onLoad="getParameters()">
<h1>Welcome to Porchlight Production Process</h1>
<form name="form1" method="post" onSubmit="return getCount(this)">
  <table width="900"  border="0">
				<%
				//populate facades
				GeneralInfoType gi = new GeneralInfoType();
				gi.setProduction("Prime Mover");
				gi.setDate(XMLGregorianCalendarImpl.parse(new SimpleDateFormat("yyyy-MM-dd").format(new Date())));
				gi.setWeekday(new SimpleDateFormat("EEEE", Locale.ENGLISH).format(new Date()));
			
                out.println("<td><strong>Production</strong></td><td><input name='production' type='text' id='production' value='"+gi.getProduction()+"' readonly></td><td>&nbsp;</td>");
                out.println("<td><strong>Date</strong></td><td><input name='date' type='text' id='date' value='"+gi.getDate().getDay()+"-"+gi.getDate().getMonth()+"-"+gi.getDate().getYear()+"' readonly></td><td>&nbsp;</td>");
                out.println("<td><strong>Day</strong></td><td><input name='weekday' type='text' id='weekday' value='"+gi.getWeekday()+"' readonly></td>");
                out.println("<td><strong>Shoot Day #</strong></td><td><input name='shoot_day' type='text' id='shoot_day'></td>");
				out.println("</tr></table></td></tr>");	
				
		       try {
		            //retrieves the weather forecast for NSW Central West Slopes and Plains
		    	    URL targetUrl =  new URL("http://www.bom.gov.au/products/IDN10062.shtml");
		            URLConnection targetUrlCon = targetUrl.openConnection();

		        	BufferedReader reader = new BufferedReader(new InputStreamReader(targetUrlCon.getInputStream()));//  targetUrl.openStream()
		        	//BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("NewFile.html")));

		        	StringBuffer a = new StringBuffer();
		            String str;
		            while ((str=reader.readLine())!= null)
		            	a.append(str);
		            
		            str="<P>"+a.substring(a.indexOf("<h4>CENTRAL WEST SLOPES AND PLAINS</h4>"), a.indexOf("IDN1006203<BR>"))+"</P>";
		            str=str.replaceAll("<h4>CENTRAL WEST SLOPES AND PLAINS</h4>", "<B>Weather - NSW: CENTRAL WEST SLOPES AND PLAINS</B><BR>"+a.substring(a.indexOf("Issued at"), a.indexOf("2007<BR>"))+"<BR>");
		            str=str.replaceAll("<!--#includevirtual=\"../fwo/IDN1006_0_1_2_menu.html\" -->","");
		       		str=str.replaceAll("                           ","   ");
		       		str=str.replaceAll("                    ","   ");
		       		str=str.replaceAll("Parkes :","\nParkes: ");
		       		out.println(str);
		       		out.println("<td><font size='2'>© Copyright Commonwealth of Australia 2007, Bureau of Meteorology (ABN 92 637 533 532)</td>");
		       		
		    	}catch (Exception e) {
		    	System.out.println(e.getMessage());
		    }
		    if(request.getParameter("Submission") != null){
		    		gi.setShootDayNo(new BigInteger(request.getParameter("shoot_day")));
		    		
		    		WelcomeToStartProcessType wtsp = new WelcomeToStartProcessType();
		    		wtsp.setGeneralInfo(gi);
    		
		    	    ObjectFactory ob = new ObjectFactory();
		    	    JAXBElement<WelcomeToStartProcessType> wtspElement = (JAXBElement<WelcomeToStartProcessType>)ob.createWelcomeToStartProcess(wtsp);
		    		
		    	    ByteArrayOutputStream xmlOS = new ByteArrayOutputStream();
		    		JAXBContext jc = JAXBContext.newInstance("org.yawlfoundation.sb.welcometoproduction");
		    		Marshaller m = jc.createMarshaller();
		    	    m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
		    	    //m.marshal( wtspElement, new File("./webapps/testing/continuity/wtsp.xml") );//output to file
		    	    m.marshal(wtspElement, xmlOS);//out to ByteArray
		    		String result = xmlOS.toString().replaceAll("ns2:", "");
		    	    System.out.println(result);
		    	    
		    	    String workItemID = new String(request.getParameter("workItemID"));
		    	    String sessionHandle = new String(request.getParameter("sessionHandle"));
		    	    String userID = new String(request.getParameter("userID"));
		    	    String submit = new String(request.getParameter("submit"));
		    		
		    	    response.sendRedirect(response.encodeURL(getServletContext().getInitParameter("HTMLForms")+"/yawlFormServlet?workItemID="+workItemID+"&sessionHandle="+sessionHandle+"&userID="+userID+"&submit="+submit+"&inputData="+result));
		    	   
		    	}	
		    	
%>
		
	<tr><td>&nbsp;</td></tr>
	
		<tr><td>
			<input type="hidden" name="workItemID" id="workItemID"/>
			<input type="hidden" name="userID" id="userID"/>
			<input type="hidden" name="sessionHandle" id="sessionHandle"/>
			<input type="hidden" name="specID" id="specID"/>
			<input type="hidden" name="submit" id="submit"/>
		</td></tr>
  </table>
  <p><input type="submit" name="Submission" value="Submission"></p>
</form>
</body>
</html>