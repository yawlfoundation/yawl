/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are commited to improving
 * workflow technology.
 */
package au.edu.qut.yawl.worklet.support;

import java.util.* ;
import java.io.* ;

import org.apache.log4j.Logger;

/**
 *  The support library class of static methods for the worklet service
 *
 *  @author Michael Adams
 *  BPM Group, QUT Australia
 *  m3.adams@qut.edu.au
 *  v0.8, 04/07/2006
 */

public class Library {
	
	// various file paths to the service installation & repository files
	public static final String wsHomeDir       = getHomeDir() ;
    public static final String wsRepositoryDir = getRepositoryDir() ;
    public static final String wsLogsDir       = wsRepositoryDir + "logs/" ;
    public static final String wsWorkletsDir   = wsRepositoryDir + "worklets/" ;
    public static final String wsRulesDir      = wsRepositoryDir + "rules/" ;
    public static final String wsSelectedDir   = wsRepositoryDir + "selected/" ;
    public static final boolean wsPersistOn    = getPersistOn() ;

    private static String _resDir = null ;
    private static String _persist = null ;
    
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

    /** returns the file path to the worklet repository */
    private static String getRepositoryDir() {
        if (_resDir == null) getProperties();
        return _resDir;
    }

//===========================================================================//
	
    /** returns the value for persistence from the properties file */
 	private static boolean getPersistOn() {
        boolean result = false ;                           // default result
        if (_persist == null) getProperties();             // read the prop file
        if (_persist != null) {                            // we have a value
           result = _persist.equalsIgnoreCase("on")   ||   // a few options
                    _persist.equalsIgnoreCase("yes")  ||
                    _persist.equalsIgnoreCase("true") ||
                    _persist.equalsIgnoreCase("y");
        }

        return result;
	}
	
//===========================================================================//

    /** returns the file path to the worklet repository & files */
    private static void getProperties() {
        Properties prop = new Properties();
        String fName = wsHomeDir + "workletService.properties" ;

        try {
            prop.load(new FileInputStream(fName));
            _resDir = prop.getProperty("repositoryDir");    // get values from prop file
            _persist = prop.getProperty("persist");
        }
        catch (IOException e) {
            _log.error("Can't read workletService.properties files" , e);
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
	
    /** removes the ddd_ part from the front or rear of a taskid */
	public static String getTaskNameFromId(String tid) {
		if (tid.length() == 0) return null ;            // no string passed
        if (tid.indexOf('_') == -1) return tid ;        // no change required

        String[] split = tid.split("_");

        // find out which side has the decomp'd taskid
        char c = tid.charAt(0);

        if (Character.isDigit(c))                      // if tid starts with a digit
           return split[1] ;                           // return name after the '_'
        else
           return split[0] ;                           // return name before the '_'
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
   	
//===========================================================================//

   /** returns true if the file is found */	
   public static boolean fileExists(String fName) {
      File f = new File(fName) ;
      return f.exists() ;        	
   }

//===========================================================================//

    /** returns a list of objects as a String of csv's */
    public static String listItems(List list) {
        String s = "" ;
        for (Object o : list) {
            s += o + ", ";
        }
        return s ;
    }

//===========================================================================//

    /** appends a formatted line with the passed title and value to the StringBuffer */
    public static StringBuffer appendLine(StringBuffer s, String title, String item){
        if (title == null) title = "null" ;
        if (item == null) item = "null" ;
        s.append(title); s.append(": "); s.append(item); s.append(newline);
        return s ;
    }

//===========================================================================//

    /** appends an XML formatted line with the passed tag and value to the StringBuffer */
    public static StringBuffer appendXML(StringBuffer s, String tag, String value) {
        String open  = '<' + tag + '>' ;
        String close = "</" + tag + '>';

        // replace all <'s and &'s with unmarkedup equivalents
        if (value.indexOf('&') > -1) value = value.replaceAll("&", "&amp;");
        if (value.indexOf('<') > -1) value = value.replaceAll("<", "&lt;");

        s.append(open); s.append(value); s.append(close);
        return s;
    }

   
//===========================================================================//
//===========================================================================//
	
}  // ends

