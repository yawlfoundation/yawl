/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.worklet.support;

import au.edu.qut.yawl.worklist.model.WorkItemRecord;

import java.io.* ;
import java.text.SimpleDateFormat ;
import java.util.Date ;

import org.jdom.Element ;
import org.jdom.output.* ;


/** 
 *  A log file implementation (debug & events).
 * 
 *  @author Michael Adams
 *  BPM Group, QUT Australia
 *  m3.adams@qut.edu.au
 *  v0.7, 10/12/2005
 */
 
public class Logger {
	
	// event type descriptors
	public static final String eCheckOut = "CheckOutWorkItem";
	public static final String eLaunch = "WorkletLaunched";
	public static final String eCheckIn = "CheckInWorkItem";
	public static final String eCancel = "WorkletCancelled";
	public static final String eComplete = "WorkletCompleted";
	
    // paths to log files
   	private static final String _logDir = Library.wsLogsDir;
	private String _fullPath ;
	private static String _errPath   = _logDir + "exception.log";
	private static String _eventPath = _logDir + "eventLog.csv" ;
	
	// output objects
	private static PrintWriter _log = null;
	private static XMLOutputter _xo = null ;
	
	// date format for normal/debug logging
    private	static SimpleDateFormat _sdf  = new SimpleDateFormat (
		                                          "yyyy.MM.dd hh:mm:ss :- ");
	// date formate for the event log	                                          
    private	static SimpleDateFormat _sdfe  = new SimpleDateFormat (
		                                          "yyyy.MM.dd hh:mm:ss");

	/**
	 *  Logger constructor
	 *  @param logFileName - the file name for this log
	 */
	public Logger(String logFileName) {
		_fullPath = _logDir + logFileName ;	
	}
	
//===========================================================================//
	
	/**
	 *  Writes a line to the log file, preceded by the current timestamp
	 *  @param s - the String to write to the log
	 */
    public void write(String s) {
		try {
	        _log = new PrintWriter(new FileWriter(_fullPath, true));
	        if (s.length() > 0) _log.println(_sdf.format( new Date() ) + s); 
	        else _log.println() ; 		        
		    _log.close() ;
		} 
		catch (IOException e) {
			e.printStackTrace();
		}        	       
   	}
   	
//===========================================================================//
	
    /**
     *  Fully writes a JDOM Element to the log 
     *  @param e - the JDOM Element to write to the log 	
     */
   	public void write(Element e) {
		try {
	        _log = new PrintWriter(new FileWriter(_fullPath, true));
	        _xo = new XMLOutputter(Format.getPrettyFormat()) ; 
 
			if (e != null)
			   _xo.output(e, _log) ;
			else
			   _log.print("element is null");
						   
			_log.println(); 		        
			_log.close() ;
		} 
		catch (IOException ioe) {
			write(e.toString());         // see if we can print it as string
		}    		
   	}
   	
//===========================================================================//
	
    /**
     *  writes an Exception's stack trace to the log file
     *  @param x - the Exception object to write to the log  	
     */
   	public void write(Exception x) {
		try {
	        _log = new PrintWriter(new FileWriter(_errPath, true));
			x.printStackTrace(_log); 		        
			_log.close() ;
		} 
		catch (IOException e) {
			e.printStackTrace();
		}     		
   	}
   	
//===========================================================================//
	
    /**
     *  writes an event to the event log
     *  @param event - the type of event to log
     *  @param caseId - the case that caused the event
     *  @param specId - the specification id of the case
     *  @param taskid - the id of the task the workletwas subbed for
     *  @param parentCaseId - the case id of the original workitem
     */  	
   	public static void logEvent(String event, String caseId, String specId,
   	                            String taskId, String parentCaseId) {

   		StringBuffer s = new StringBuffer() ;
   		s.append(_sdfe.format(new Date())) ; s.append(",") ;
   		s.append(event); s.append(",") ;
    	s.append(caseId); s.append(",") ;
   		s.append(specId); s.append(",") ;
   		s.append(taskId); s.append(",") ;
   		s.append(parentCaseId);
   		
   		try {
   		   PrintWriter log = new PrintWriter(new FileWriter(_eventPath, true));   		
   		   log.println(s.toString()) ;
   		   log.close() ;
		} 
		catch (IOException e) {
			e.printStackTrace();
		}        	       
    }
    
//===========================================================================//
    
    /**
     *  writes an event to the event log
     *  @param event - the type of event to log
     *  @param wir - the workitem that triggered the event
     */  
    public static void logEvent(String event, WorkItemRecord wir) {
    	logEvent(event, wir.getCaseID(), wir.getSpecificationID(), 
    	                                          wir.getTaskID(), "");
    }
    
//===========================================================================//

   	/** Writes a blank line to the log */
   	public void writeBlankLine() {
   		this.write("");
   	}
   	
//===========================================================================//
	
    /**
     *  Writes the string passed as a header (centred and encased in /'s)
     *  @param s - the String to write as a header to the log
     */
   	public void writeAsHeader(String s) {
   		int pad = (78 - s.length())/2 ;
   		String sepChars = Library.getSepChars(pad) ;
   		this.writeBlankLine() ;
   		this.write(Library.newline + sepChars + " " + s + " " + sepChars);
   	}

//===========================================================================//
	
  	/**
  	 *  writes a separator line to the log file
  	 *  see the Library class to set the separator character
  	 *  @param len - the length of the line to print
  	 *  @param newline - true to complete the line
  	 */
   	public void writeSeparator(int len, boolean newline) {
		try {
	        _log = new PrintWriter(new FileWriter(_fullPath, true));
	        for (int i=0;i<len;i++) _log.print(Library.getSepChars(1)) ;
	        if (newline) _log.println() ;   		
			_log.close() ;

		} 
		catch (IOException e) {
			e.printStackTrace();
		}        	       
   	}
 
//===========================================================================//
//===========================================================================//

}

