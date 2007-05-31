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
    <script src="http://maps.google.com/maps?file=api&amp;v=2.x&amp;key=ABQIAAAAtOjLpIVcO8im8KJFR8pcMhQjskl1-YgiA_BGX2yRrf7htVrbmBTWZt39_v1rJ4xxwZZCEomegYBo1w" type="text/javascript"></script>
    <script type="text/javascript">
    //<![CDATA[

    var map = null;
    var geocoder = null;
	var marker1 = null;
	var marker2 = null;
	
	function init(){
		// Create a base icon for all of our markers that specifies the
		// shadow, icon dimensions, etc.
		var baseIcon = new GIcon();
		baseIcon.shadow = "http://www.google.com/mapfiles/shadow50.png";
		baseIcon.iconSize = new GSize(20, 34);
		baseIcon.shadowSize = new GSize(37, 34);
		baseIcon.iconAnchor = new GPoint(9, 34);
		baseIcon.infoWindowAnchor = new GPoint(9, 2);
		baseIcon.infoShadowAnchor = new GPoint(18, 25);
		
		var icon1 = new GIcon(baseIcon);
		icon1.image = "http://www.google.com/mapfiles/marker" + "A" + ".png";
		
		var icon2 = new GIcon(baseIcon);
		icon2.image = "http://www.google.com/mapfiles/marker" + "B" + ".png";
		
	    marker1 = new GMarker(new GLatLng(-32.25, 148.80), {draggable: true, icon: icon1, title: "Unit A, AD: John Denver", bouncy: false});
	    marker2 = new GMarker(new GLatLng(-32.50, 148.50), {draggable: true, icon: icon2, title: "Unit B, AD: David Myer", bouncy: false});
		
		document.getElementById("point1_lat").value = marker1.getPoint().lat();
		document.getElementById("point1_lng").value = marker1.getPoint().lng();
		document.getElementById("point2_lat").value = marker2.getPoint().lat();
		document.getElementById("point2_lng").value = marker2.getPoint().lng();
		
		GEvent.addListener(marker1, "dragend", function() {
		    document.getElementById("point1_lat").value = marker1.getPoint().lat();
		    document.getElementById("point1_lng").value = marker1.getPoint().lng();
		});
		
		GEvent.addListener(marker2, "dragend", function() {
		    document.getElementById("point2_lat").value = marker2.getPoint().lat();
		    document.getElementById("point2_lng").value = marker2.getPoint().lng();
		});
	}
	
    function load() {
      if (GBrowserIsCompatible()) {
        map = new GMap2(document.getElementById("map"));
        map.addControl(new GSmallMapControl());
        map.addControl(new GMapTypeControl());
        map.setCenter(new GLatLng(-32.25, 148.60), 9);
		map.enableScrollWheelZoom();
		geocoder = new GClientGeocoder();

		init();
		map.addOverlay(marker1);
		map.addOverlay(marker2);
      }
    }

	function setMarker1(lat, lng){
		marker1.setPoint(new GLatLng(lat, lng));
	}
	
	function setMarker2(lat, lng){
		marker2.setPoint(new GLatLng(lat, lng));
	}
    //]]>
    </script>
</head>

<body onload="getParameters(); load()" onunload="GUnload()">
<h1>Welcome to Porchlight Production Process</h1>
    <form name="map" action="#" onsubmit="showAddress(this.address.value); return false">
      <div id="map" style="width: 600px; height: 500px"></div>
	</form>
	<form name="unit1" action="#" onsubmit="setMarker1(this.point1_lat.value, this.point1_lng.value); return false">
	  <p>Unit A
		<input type="text" size="20" id="point1_lat" name="point1_lat"/>
		<input type="text" size="20" id="point1_lng" name="point1_lng"/>
        <input type="submit" value="Set A" />
	  </p>
	</form>
	<form name="unit2" action="#" onsubmit="setMarker2(this.point2_lat.value, this.point2_lng.value); return false">
	  <p>Unit B
		<input type="text" size="20" id="point2_lat" name="point2_lat"/>
		<input type="text" size="20" id="point2_lng" name="point2_lng"/>
        <input type="submit" value="Set B" />	  
      </p>
    </form>
<form name="form1" method="post" onSubmit="return getCount(this)">
  <table width="900"  border="0">
				<%
				//populate facades
				GeneralInfoType gi = new GeneralInfoType();
				gi.setProduction("Prime Mover");
				gi.setDate(XMLGregorianCalendarImpl.parse(new SimpleDateFormat("yyyy-MM-dd").format(new Date())));
				gi.setWeekday(new SimpleDateFormat("EEEE", Locale.ENGLISH).format(new Date()));
			
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
		    	    
		    	    //response.getWriter().write(result);
		    	    
		    	    session.setAttribute("inputData", result);
		    	    
		    	    String workItemID = new String(request.getParameter("workItemID"));
		    	    String sessionHandle = new String(request.getParameter("sessionHandle"));
		    	    String userID = new String(request.getParameter("userID"));
		    	    String submit = new String(request.getParameter("submit"));
		    	    
		    	    response.sendRedirect(response.encodeURL(getServletContext().getInitParameter("HTMLForms")+"/yawlFormServlet?workItemID="+workItemID+"&sessionHandle="+sessionHandle+"&userID="+userID+"&submit="+submit));
		    	    return;
		    	}		    	    

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
		            
		            str="<P>"+a.substring(a.indexOf("<h4>CENTRAL WEST SLOPES AND PLAINS</h4>"), a.indexOf("IDN1006203"))+"</P>";
		            str=str.replaceAll("<h4>CENTRAL WEST SLOPES AND PLAINS</h4>", "<B>Weather - NSW: CENTRAL WEST SLOPES AND PLAINS</B><BR>"+a.substring(a.indexOf("Issued at"), a.indexOf("2007<BR>"))+"<BR>");
		            str=str.replaceAll("<!--#includevirtual=\"../fwo/IDN1006_0_1_2_menu.html\" -->","");
		       		str=str.replaceAll("                           ","   ");
		       		str=str.replaceAll("                    ","   ");
		       		str=str.replaceAll("Parkes :","\nParkes: ");
		       		out.println(str);
		       		out.println("<td><font size='2'>© Copyright Commonwealth of Australia 2007, Bureau of Meteorology (ABN 92 637 533 532)</td>");
		       		
		    	}
		       	catch (Exception e) {
		    		System.out.println(e.getMessage());
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