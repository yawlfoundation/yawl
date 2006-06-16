/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.admintool;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.StringTokenizer;
import java.util.Vector;

public class DateTransform {

    Vector v = new Vector();
    String text = new String();

    Connection con = null;

    //todo refactor this into separate classes as necessary

    public String getData(String eventid) {
	String return_data = ""  ;
	String beginning = "<Attribute name=\"";
	String end = "</Attribute>";
	String middle = "\">";

	try {
	    Statement st = null;
	    if (con != null) {
		st = con.createStatement();
		ResultSet rs = st.executeQuery("select * from data where eventid = '"+ eventid +"'");
			
		while (rs.next()) {		    		
		    return_data = return_data + "\n" + 
			beginning + 
			rs.getString(2) + 
			middle + 
			rs.getString(3) + 
			end;
		}

	    }
   
	} catch (Exception e) {
	    e.printStackTrace();
	}      
	System.out.println(return_data);
	return return_data;
	
    }

    public boolean checklist(String s) {
	if (v.size()==0) {
	    try {
		BufferedReader in = new BufferedReader(new FileReader("./yawllist.xml"));
		String line = in.readLine();
		while (line!=null) {
		    text = text + line;
		    line = in.readLine();
		    
		}
	    }catch (Exception e) {
		e.printStackTrace();
	    }
	    
	    StringTokenizer st = new StringTokenizer(text);
	    while (st.hasMoreTokens()) {
		v.add(st.nextToken());
	    }
	}
	
	for (int i = 0; i < v.size();i++) {
	    String old = (String) v.elementAt(i);
	    if (old.equals(s)) {
		return true;
	    }
	}

	return false;
    }

    public void writeFile() {
	System.out.println("v.size(): " + v.size());
	try {
	    BufferedWriter buf = new BufferedWriter(new FileWriter("./yawllist.xml"));
	    for (int i = 0; i < v.size();i++) {
		buf.write((String) v.elementAt(i),0,((String) v.elementAt(i)).length());
		buf.write(" ",0,1);
	    }
	    buf.close();
	}catch (Exception e) {
	    e.printStackTrace();
	}
	
    }

    public String createlist(String s) {
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
		    return "";
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
		    return "";
		}
	    }
	    else {
		while (v.removeElement(s));
		return "";
	    }
	    
	}	
	
	return "";
    }

    public String transformId(String caseid, String tempid) {

	String sub = null;
	if (tempid.indexOf(".")==-1) {
	    return caseid;
	} else {
	    sub = caseid+tempid.substring(tempid.indexOf("."),tempid.length());
	}

	return sub;
    }

    public static void main(String argv[]) {
	System.out.println(DateTransform.transform(new Long(argv[0]).longValue()));
    }	


    /*
      Should have another parameter which states
      in what granularity the completion time should
      be returned in (.e.g. days, year, months, minutes);
     */
    public static int getTime(long timespan, int granularity) {
	
	String date = null;

	int time = 9;

	Calendar cal = Calendar.getInstance();
	cal.setTimeZone(TimeZone.getTimeZone("GMT-0"));
	cal.setTimeInMillis(new Long(timespan).longValue());		
	
	int year = new Integer(cal.get(Calendar.YEAR)).intValue();
	int month = new Integer(cal.get(Calendar.MONTH)).intValue();	    

	if (granularity == Calendar.YEAR) {
	    time = year;
	} else if (granularity == Calendar.MONTH) {
	    time = month + year*12;
	} else if (granularity == Calendar.DATE) {
	    time = (int) timespan/1000;
	    time = (int) time/60;
	    time = (int) time/60;
	    time = (int) time/24;
	} else if (granularity == Calendar.HOUR_OF_DAY) {
	    time = (int) timespan/1000;
	    time = (int) time/60;
	    time = (int) time/60;
	} else if (granularity == Calendar.MINUTE) {
	    time = (int) timespan/1000;
	    time = (int) time/60;
	    
	} else if (granularity == Calendar.SECOND) {
	    time = (int) timespan/1000;
	}

	
	
	/*
	  Completion time in minutes
	 */
	return time;
	
    }

    public static String transform(long millis) {
	
	String date = null;
	
	Calendar cal = Calendar.getInstance();
	//cal.setTimeZone(TimeZone.getTimeZone("GMT-0"));

	cal.setTimeInMillis(millis);
	
	String year = new Integer(cal.get(Calendar.YEAR)).toString();
	String month = new Integer(cal.get(Calendar.MONTH)).toString();
	String day = new Integer(cal.get(Calendar.DATE)).toString();
	String hour = new Integer(cal.get(Calendar.HOUR_OF_DAY)).toString();
	String min = new Integer(cal.get(Calendar.MINUTE)).toString();
	String second = new Integer(cal.get(Calendar.SECOND)).toString();
	
	year = new Integer(new Integer(year).intValue()).toString();
	day = new Integer(new Integer(day).intValue()).toString();
	month = new Integer(new Integer(month).intValue()+1).toString();
	
	System.out.println(year + " " + day);


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
	 
	date = year+"-"+month+"-"+day+"  "+hour+":"+min+":"+second;
	    
	return date;
    }

    public static String convertTime(String time) {
	Calendar cal = Calendar.getInstance();
	try {
	    
	    StringTokenizer split = new StringTokenizer(time);
	    String date1 = split.nextToken();
	    String time1 = split.nextToken();

	    split = new StringTokenizer(date1,"-");
	    String day = split.nextToken();
	    String mon = split.nextToken();
	    String year = split.nextToken();
	    
	    split = new StringTokenizer(time1,":");
	    String hour = split.nextToken();
	    String min = split.nextToken();
	    String sec = split.nextToken();
	    	    
	    cal.set(new Integer(year).intValue(),
		    new Integer(mon).intValue()-1,
		    new Integer(day).intValue(),
		    new Integer(hour).intValue(),
		    new Integer(min).intValue(),
		    new Integer(sec).intValue());
	    System.out.println(cal.getTimeInMillis());
	} catch (Exception e) {
	    e.printStackTrace();
	}	
	return new Long(cal.getTimeInMillis()).toString();
    }

    public static String convertRelativeTime(long timetaken) {
	String sec = ""+(timetaken/1000)%60;
	String min = ""+(timetaken/(1000*60))%60;
	String hrs = ""+(timetaken/(1000*60*60))%24;
	String days = ""+timetaken/(1000*60*60*24);	
	String daystime = days + " - " + hrs+":"+min+":"+sec;       
	//get current time
	return daystime;
    }

}
