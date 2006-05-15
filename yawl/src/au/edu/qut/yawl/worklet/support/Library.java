/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.worklet.support;

import java.util.* ;
import java.io.* ;

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
	public static String wsHomeDir       = getHomeDir() ;
    public static String wsRepositoryDir = getRepositoryDir() ;  
    public static String wsLogsDir       = wsRepositoryDir + "logs/" ;
    public static String wsWorkletsDir   = wsRepositoryDir + "worklets/" ;
    public static String wsRulesDir      = wsRepositoryDir + "rules/" ;
    public static String wsSelectedDir   = wsRepositoryDir + "selected/" ;
    
    public static String newline = System.getProperty("line.separator"); 
    

//===========================================================================//
	
    /**
     *  returns the file path to the worklet service inside the local 
     *  Tomcat installation
     */
	public static String getHomeDir() {
		return (getEnvString("catalina_home").replace('\\', '/' ) + 
	                         "/webapps/workletSelector/") ;
	}
	
//===========================================================================//
	
	/** returns the file path to the worklet repository & files */
	public static String getRepositoryDir() {
		Properties prop = new Properties();
		String fName = wsHomeDir + "workletSelector.properties" ;
	    
	    try {
	        prop.load(new FileInputStream(fName));
	        return prop.getProperty("repositoryDir");    // get from prop file
	    } 
	    catch (IOException e) {
  	        Logger _log = new Logger("library.log");
	    	_log.write(e);
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
	   	  Logger _log = new Logger("library.log");
	      _log.write(e);
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
       Logger _log = new Logger("library.log");
    	
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
          _log.write( "File not found! - " + fName ) ;
          _log.write(fnfe);         
          return null ;
       }
       catch( IOException ioe ) {
          _log.write( "IO Exception when reading file - " + fName ) ;
          _log.write(ioe);         
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

