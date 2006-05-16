/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organisations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.worklet.support;

import java.util.* ;
import java.io.* ;

import org.apache.log4j.Logger;

/**
 *  The support library class of static methods 
 *  for the worklet selection service 
 *
 *  @author Michael Adams
 *  BPM Group, QUT Australia
 *  m3.adams@qut.edu.au
 *  v0.7, 10/12/2005
 */

public class Library {
	
	// various file paths to the service installation & repository files
	public static final String wsHomeDir       = getHomeDir() ;
    public static final String wsRepositoryDir = getRepositoryDir() ;
    public static final String wsLogsDir       = wsRepositoryDir + "logs/" ;
    public static final String wsWorkletsDir   = wsRepositoryDir + "worklets/" ;
    public static final String wsSelRulesDir   = wsRepositoryDir + "selectionRules/" ;
    public static final String wsExRulesDir    = wsRepositoryDir + "exceptionRules/" ;
    public static final String wsSelectedDir   = wsRepositoryDir + "selected/" ;
    
    public static final String newline = System.getProperty("line.separator");

    private static Logger _log = Logger.getLogger("au.edu.qut.yawl.worklet.support.Library"); 
    

//===========================================================================//
	
    /**
     *  returns the file path to the worklet service inside the local 
     *  Tomcat installation
     */
	private static String getHomeDir() {
		return (getEnvString("catalina_home").replace('\\', '/' ) + 
	                         "/webapps/workletService/") ;
	}
	
//===========================================================================//
	
	/** returns the file path to the worklet repository & files */
	private static String getRepositoryDir() {
		Properties prop = new Properties();
		String fName = wsHomeDir + "workletService.properties" ;
	    
	    try {
	        prop.load(new FileInputStream(fName));
	        return prop.getProperty("repositoryDir");    // get from prop file
	    } 
	    catch (IOException e) {
	    	_log.error("Can't read workletService.properties files" , e);
	    	return null ;
	    }
	}
	
//===========================================================================//
	
	/**
	 *  gets the value of the requested OS environment variable
	 *  @param key - the OS environment variable
	 *  @return the value as a String
	 */
	public static String getEnvString(String key) {

	   // get name of operating system	
	   String osName = System.getProperty("os.name").toUpperCase();
	   String cmd ;
	    
	   // select the right cmd for the OS
	   if (osName.startsWith("WINDOWS 9")) cmd = "command.com /c echo" ;
	   else if (osName.startsWith("WIN")) cmd = "cmd.exe /c echo" ;
	   else cmd = "/bin/env echo" ;
	  
	   //add the key
	   cmd = cmd + " %" + key.toUpperCase() + "%" ;
	
	   try {
	  	  Process p = Runtime.getRuntime().exec(cmd);      //run it
	      return new BufferedReader(
	     	     new InputStreamReader(p.getInputStream())).readLine();
	   }   	    
	   catch (Exception e) {
	      _log.error("Can't get env variable " + cmd, e);
	      return "" ;	
	   }
	}
	
//===========================================================================//
	
    /** removes the xxx_ part from the front of a taskid */
	public static String getTaskNameFromId(String tid) {
		if (tid.length() == 0) return null ;
		
		int pos = tid.indexOf('_') ;                   // remove xxx_
		return tid.substring(pos + 1) ;		           // from start of task 
	}
	
//===========================================================================//
	
	/** returns a string of characters of length 'len' */  	
   	public static String getSepChars(int len) {
   		StringBuffer sb = new StringBuffer(len) ;
	    for (int i=0;i<len;i++) sb.append('/') ;
	    return sb.toString() ;
   	}
   	
//===========================================================================//
	
    /**
     *  converts the contents of a file to a String
     *  @param fName the name of the file
     *  @return the String representing the file's contents
     */ 	
   	public static String FileToString(String fName) {
       String fLine ;
       StringBuffer result = new StringBuffer();

       try {
       	  if (! fileExists(fName)) return null ;     // don't go further if no file
       	  
          FileReader fread = new FileReader(fName); 
          BufferedReader bufread = new BufferedReader( fread );

          fLine = bufread.readLine() ;        // read first line
          while( fLine != null ) {            
             result.append(fLine) ;
             fLine = bufread.readLine() ;     // read next line
          }
          bufread.close();
          fread.close();
          return result.toString() ; 
       }
       catch( FileNotFoundException fnfe ) {
          _log.error( "File not found! - " + fName, fnfe ) ;
          return null ;
       }
       catch( IOException ioe ) {
          _log.error( "IO Exception when reading file - " + fName, ioe ) ;
          return null ;
       }
   }
   	
   	
   /** returns true if the file is found */	
   public static boolean fileExists(String fName) {
      File f = new File(fName) ;
      return f.exists() ;        	
   }	
   
//===========================================================================//
//===========================================================================//
	
}

