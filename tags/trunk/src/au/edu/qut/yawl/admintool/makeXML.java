/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


/**
 * @author heijens
 * version: 19.07.2005
 * This class will create an XML file for the data 
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

package au.edu.qut.yawl.admintool;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.Vector;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class makeXML extends HttpServlet{
	
	private String startfile = new String("");
	private String file = new String("");
	private String complete = new String("");
	Vector v = new Vector();
	
	public void doGet(HttpServletRequest req, HttpServletResponse res){
		
	}
	
	
	public void doPost(HttpServletRequest req, HttpServletResponse res){
		
		String specification = req.getParameter("selectspec");
		
		String from = req.getParameter("from");
		System.out.println("from = " + from);
		String until = req.getParameter("until");
		System.out.println("until = " + until);
		if ((from.charAt(2) != '-' && from.charAt(5) != '-') || (until.charAt(2) != '-' && until.charAt(5) != '-')) {
			System.out.println("wrongdate format");
			String url =  new String(getServletContext().getInitParameter("admintool")+"/3rdparty.jsp?wrongdate=true");
			try {
				res.sendRedirect(res.encodeURL(url));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		else {	
//	after Tore has changed the milliseconds to timestamps this will need to be changed
			
			int fromyear  = Integer.parseInt(from.substring(6,10));
			int frommonth  = Integer.parseInt(from.substring(3,5));
			int fromday  = Integer.parseInt(from.substring(0,2));
						
			int untilyear  = Integer.parseInt(until.substring(6,10));
			int untilmonth  = Integer.parseInt(until.substring(3,5));
			int untilday  = Integer.parseInt(until.substring(0,2));
			
			String fromDate = transformDateToMilliSeconds(fromyear, frommonth, fromday);
			String untilDate = transformDateToMilliSeconds(untilyear, untilmonth, untilday);
						
			createHeading(specification);
			Vector subxmlStrings = readDataFromDb(specification, fromDate, untilDate, res);
			
			for (int i=0; i< subxmlStrings.size(); i++) {
				String casexml = (String) subxmlStrings.get(i);
				file = file + casexml;
			}
			complete = startfile + file + "</Process></WorkflowLog>";
					
			writeXMLfile(complete);
			
			String url =  new String(getServletContext().getInitParameter("admintool")+"/3rdparty.jsp?created=true");
			try {
				res.sendRedirect(res.encodeURL(url));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
			e1.printStackTrace();
			}
		}
		
	}
		
	
	/**
	 * Tranforms a date to milliseconds
	 * @param year is the year part of the date that needs to be transformed
	 * @param month is the month part of the date that needs to be transformed
	 * @param day is the day part of the date that needs to be tranformed
	 * @return the data entered in milliseconds
	 */
	public String transformDateToMilliSeconds(int year, int month, int day) {
		int newyear = year - 1900;
		int newmonth = month - 1;
		Date date = new Date(newyear, newmonth, day);
		Calendar cal =  Calendar.getInstance();
		cal.clear();
		cal.setTime(date);
		long x = cal.getTimeInMillis();
		Long l = new Long(x); 
		String milliseconds = l.toString();
		return milliseconds;
	}
	
	public static String transform(String millis) {
		
		String date = null;

		Calendar cal = Calendar.getInstance();

		cal.setTimeInMillis(new Long(millis).longValue());
		
		String year = new Integer(cal.get(Calendar.YEAR)).toString();
		String month = new Integer(cal.get(Calendar.MONTH)).toString();
		String day = new Integer(cal.get(Calendar.DATE)).toString();
		String hour = new Integer(cal.get(Calendar.HOUR)).toString();
		String min = new Integer(cal.get(Calendar.MINUTE)).toString();
		String second = new Integer(cal.get(Calendar.SECOND)).toString();
					
		if (new Integer(month).intValue() < 10) {
		    month = "0" + month;
		}
		if (new Integer(day).intValue() < 10) {
		    day = "0" + day;
		}
		if (new Integer(hour).intValue() < 10) {
		    hour = "0" + hour;
		}
		if (new Integer(second).intValue() < 10) {
		    second = "0" + second;
		}
		if (new Integer(min).intValue() < 10) {
		    min = "0" + min;
		}

		date = year+"-"+month+"-"+day+"T"+hour+":"+min+":"+second;
		
		return date;
	    }
	
	/**
	 *  Creates the start of the xml-file
	 */
	private void createHeading(String specification) {
		startfile = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<WorkflowLog>" +
				"<Data>" +
				"<Attribute name=\"author\">Admin</Attribute>" +
				"<Attribute name=\"date\">current_date</Attribute>" +
				"</Data>" +
				"<Source program=\"self made\">" +
				"<Data>" +
				"<Attribute name=\"program info\">na</Attribute>" +
				"</Data>" +
				"</Source>" +
				"<Process id=\"main_process\">" +
				"<Data>" +
				"<Attribute name=\"description\">na</Attribute>" +
				"</Data>";
	}
	
	
	/**
	 * Reads the correct data from the database that needs to be put into the XML file
	 * @param specification specifies the specification that the logging data needs to be retrieved from
	 * @param from is the start value of the date range for which logging data needs to be retrieved 
	 * @param until is the end value of the date range for which logging data needs to be retrieved 
	 * @param res 
	 */
	public Vector readDataFromDb(String specification, String from, String until, HttpServletResponse res){
		
		Vector caseids = new Vector();
		Vector xmlstrings = new Vector();
		String identifier = "";
		String event = "";
		String originator = "";
		String time = "";
		String date = "";
		String caseid = "";
		
		Connection connection = null;
		DBconnection.loadDriver("org.postgresql.Driver");
		
		boolean isClosed = DBconnection.getConnection();
		if(isClosed == true){
			DBconnection.getConnection();
		}
		
		LinkedList rowkeyList = new LinkedList();
		LinkedList identifierList = new LinkedList();
		LinkedList taskidList = new LinkedList();
		LinkedList resourceList = new LinkedList();
		LinkedList timeList = new LinkedList();
		LinkedList eventList = new LinkedList();
		LinkedList description = new LinkedList();
		
		try {
			Statement statement = DBconnection.createStatement();
			String sql = "SELECT * FROM workitemevent " +
					"WHERE description = '"+specification+"' AND time BETWEEN '"+from+"' AND '"+until+"';";
			ResultSet rs = DBconnection.getResultSet(statement, sql);
			
			while (rs.next()) {
				rowkeyList.add(rs.getString(1));
				identifierList.add(rs.getString(2));
				taskidList.add(rs.getString(3));
				resourceList.add(rs.getString(4));
				timeList.add(rs.getString(5));
				eventList.add(rs.getString(6));
				description.add(rs.getString(7));
			}
			statement.close();
		}	
		catch (Exception e) {
			//nothing needs doing
		}
		if (connection != null) {
			DBconnection.killConnection();
		}
		
		for (int i=0; i<identifierList.size(); i++) {
			identifier = (String) identifierList.get(i);
			createlist(identifier);
			
			caseid = "";
			if (identifier.indexOf('.') != -1){
				caseid = identifier.substring(0, identifier.indexOf('.'));
			} else {
				caseid = identifier;
			}
			// add the caseid to the caseids vector if it's not in there yet 
			// and create a new String for the case
			if (!caseids.contains(caseid)) {
				caseids.add(caseid);
				//xmlstrings.add(new String());
			}
		}
					
		//for every caseid in the vector print the caseid once
		for (int j=0; j<caseids.size(); j++) {
			String onecaseid = (String) caseids.get(j);
			String xmlfile = "";
				//(String) xmlstrings.get(caseids.indexOf(onecaseid));
				xmlfile = "<ProcessInstance id=\""+onecaseid+"\">" +
				"<Data>" +
				"<Attribute name=\"description\">element</Attribute>" +
				"</Data>";
		
				//then add the audittrailentries for this case
				for (int i=0; i<identifierList.size(); i++) {
					identifier = (String) identifierList.get(i);
					event = (String) eventList.get(i);
					originator = (String) resourceList.get(i);
					time = (String) timeList.get(i);
					date = transform(time);
					if (v.contains(identifier) && identifier.startsWith(onecaseid)) {
						xmlfile = xmlfile + "<AuditTrailEntry><Data><Attribute name=\"empty\">0</Attribute></Data>" +
						"<WorkflowModelElement>"+identifier+"</WorkflowModelElement>" +
						"<EventType>"+event+"</EventType>" +
						"<Originator>"+originator+"</Originator>" +
						"<Timestamp>"+date+"</Timestamp>" +
						"</AuditTrailEntry>"; 
					}
				}
				xmlfile = xmlfile + "</ProcessInstance>";
				xmlstrings.add(xmlfile);
		}
		return xmlstrings;
	}	
		
	/**
	 * Checks the identifiers to determine which work items are parents 
	 * and which work items are children. Only the children will be added to the xml file.
	 * @param s the string that contains the identifier
	 * @return
	 */
	public void createlist(String s) {
		if (!v.contains(s)) {
		    System.out.println(v);
		    v.add(s);
		}
		for (int i = 0; i < v.size();i++) {
		    String old = (String) v.elementAt(i);
		    
		    if (old.lastIndexOf(".")>-1) {
			String temp1 = old.substring(0,old.lastIndexOf("."));
			
			String temp2 = null;
			if (s.lastIndexOf(".")>-1)
			    temp2 = s.substring(0,s.lastIndexOf("."));
			else
			    temp2 = s;
			
			/*
			  is the old one shortened equal to the new one, then do nothing because we
			  already have a decomposition
			*/

			if (temp1.equals(s)) {
			    v.removeElement(s);
			}
			
			/*
			  if the new one shortened is equal to the old one, the old must be removed and
			  new inserted
			*/
			if (temp2.equals(old)) {		   
			    System.out.println(temp1);
			    System.out.println(temp2);
			    while (v.removeElement(old)){
				System.out.println(old);
			    }
			    System.out.println("**************");
			}
		    }
		    else {
			while (v.removeElement(s));
		    }
		    
		}	
		
	}
	
	/**
	 * Converts the constructed String to an output file
	 * @param xmlString the String that needs to be put into an output file
	 */
	
	public void writeXMLfile (String xmlString) {
		try {
		  FileOutputStream file = new FileOutputStream("C:\\yawlaudit.xml",true);
		  DataOutputStream dstream = new DataOutputStream(file);
		  dstream.writeBytes(xmlString + "\n\r\n");
		  dstream.close();
		  file.close();
		} catch (Exception e) {
			System.out.println("Cannot create/open logfile");
		}
		
	}
	
}
	