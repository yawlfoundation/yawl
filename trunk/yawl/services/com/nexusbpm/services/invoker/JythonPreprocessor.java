/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.services.invoker;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.python.core.PyException;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

import com.nexusbpm.services.NexusService;
import com.nexusbpm.services.data.NexusServiceData;
import com.nexusbpm.services.data.Variable;

/**
 * Preprocesses inline jython code in data sent to the Nexus Service invoker before
 * the data gets passed to the services.
 * 
 * @author Nathan Rose
 * @author Dean Mao
 */
public class JythonPreprocessor {
//    private String errorString = "";
//    private int errorNumber = 1;
    
    /**
     * The delimiter indicating the starting point where a dynamic attribute
     * is inserted.
     */
    public static final String START_DELIMITER = "<<<";

    /**
     * The delimiter indicating the starting point where a dynamic attribute
     * is inserted.
     */
    public static final String END_DELIMITER = ">>>";
    
    /**
     * The maximum number of passes to make when processing the variables.
     */
    public static final int MAXIMUM_PASSES = 100;
    
    private PythonInterpreter interpreter;
    
    private NexusServiceData data;
    
    private List<String> variables;
    
    private Map<String, String> initialValues;
    private Map<String, String> processedValues;
    
    private static boolean initialized = false;
    
    private static String JYTHON_HOME = "C:/Progra~1/jython-21"; // default if jython.properties isn't found
    
    void setData( NexusServiceData data ) {
        this.data = data;
    }
    
    private void initialize() {
        if( !initialized ) {
            initialized = true;
            // get jython properties
            InputStream in = NexusService.class.getResourceAsStream( "jython.properties" );
            Properties p = new Properties();
            if( in == null ) {
                System.out.println( "Could not find jython properties file! Using default properties." );
                p.setProperty( "python.home", JYTHON_HOME );
            }
            else {
                try {
                    p.load( in );
//                    JYTHON_HOME = p.getProperty( "jython.home" );
//                    System.out.println( "Using jython home:" + JYTHON_HOME );
                }
                catch( IOException e ) {
                    System.out.println( "Error reading in jython.properties!" );
                    e.printStackTrace( System.out );
                }
            }
            
//            // Get the Jython home and jar paths.
//            File home = new File( JYTHON_HOME );
//            File jar = new File( home, "jython.jar" );
//            // Initialize the python interpreter with the correct home directory and classpath.
            String classPath = System.getProperty( "java.class.path" );
//            String jythonJar = jar.getAbsolutePath();
//            classPath = classPath + File.pathSeparator + jythonJar;
            //props.setProperty( "python.verbose", "debug" );
//            props.setProperty( "python.home", JYTHON_HOME );
            if( p.getProperty( "java.class.path" ) == null ) {
            	p.setProperty( "java.class.path", classPath );
            }
            PythonInterpreter.initialize( System.getProperties(), p, new String[ 0 ] );
        }
    }
    
    /**
     * Creates the preprocessor and determines which variables need to be processed.
     */
    JythonPreprocessor( NexusServiceData data ) {
        variables = new ArrayList<String>();
        initialValues = new HashMap<String, String>();
        processedValues = new HashMap<String, String>();
        this.data = data;
        
        initialize();
        
        List<String> vars = data.getVariableNames();
        
        for( Iterator<String> iter = vars.iterator(); iter.hasNext(); ) {
            String name = iter.next();
            variables.add( name );
            if( data.getType( name ).equals( Variable.TYPE_TEXT )
                    && data.getPlain( name ) != null
                    && data.getPlain( name ).indexOf( START_DELIMITER ) != -1 ) {
                initialValues.put( name, data.getPlain( name ) );
            }
        }
    }
    
    /**
     * Preprocesses the variables that need to be preprocessed.
     */
    void evaluate() {
        // working queue
        List<String> queue = new LinkedList<String>();
        // queue of things that still need work
        List<String> recheckQueue = new LinkedList<String>( variables );
        int passes = 0;
        Set<String> visitedSet = new HashSet<String>();
        Set<String> includeVars = new HashSet<String>();
        
        // first find all the variables that don't need to be evaluated
        for( Iterator<String> iter = recheckQueue.iterator(); iter.hasNext(); ) {
            String name = iter.next();
            if( !initialValues.containsKey( name ) ) {
                includeVars.add( name );
                iter.remove();
            }
        }
        
        boolean evalMore = true;
        
        StringWriter writer = null;
        
        // for each pass...
        while( recheckQueue.size() > 0 && passes < MAXIMUM_PASSES && evalMore ) {
            passes += 1;
//            System.out.println( "Pass " + passes );
            
            evalMore = false;
            
            writer = new StringWriter();
            
            // move everything that needs to be worked on into the working queue
            while( recheckQueue.size() > 0 ) {
                queue.add( recheckQueue.remove( 0 ) );
            }
            
            // then process each item in the working queue once
            while( queue.size() > 0 ) {
                String name = queue.remove( 0 );
                String val = data.getPlain( name );
                
                boolean evalFail = false;
                
                // first evaluate
                try {
                    val = eval( val, includeVars );
                    
                    data.setPlain( name, val );
                }
                catch( PyException e ) {
                    evalFail = true;
                    writer.write( "Error evaluating variable '" + name + "':\n" +
                            indent( "\t", new BufferedReader( new StringReader( e.toString() ) ) ) );
                }
                
                processedValues.put( name, val );
                if( evalFail ) {
                    // this variable either depends on itself or on a variable that
                    // hasn't completed yet
                    recheckQueue.add( name );
                }
                else if( val == null || val.indexOf( START_DELIMITER ) == -1 ||
                        visitedSet.contains( val ) ) {
                    // the variable evaluation is finished
                    includeVars.add( name );
                    evalMore = true;
//                    System.out.println( name + " finished" );
                }
                else {
                    // this descriptor couldn't be completely evaluated
//                    LOG.debug( "delaying processing of: " + descriptor.getName() );
                    visitedSet.add( val );
                    recheckQueue.add( name );
                    evalMore = true;
//                    System.out.println( name + " not finished, but evaluated" );
                }
            }
        }
        
        if( !evalMore ) {
            // an error prevented the processor from making any further passes
            StringBuffer b = new StringBuffer();
            b.append( "Error pre-processing Jython!\n" );
            b.append( "Successfully processed variables:\n" );
            for( Iterator<String> iter = includeVars.iterator(); iter.hasNext(); ) {
                b.append( iter.next() );
                if( iter.hasNext() ) {
                    b.append( ", " );
                }
            }
            b.append( "\nUnsuccessfully processed variables:\n" );
            for( Iterator<String> iter = initialValues.keySet().iterator(); iter.hasNext(); ) {
                String name = iter.next();
                if( !includeVars.contains( name ) ) {
                    b.append( name );
                    if( iter.hasNext() ) {
                        b.append( ", " );
                    }
                }
            }
            b.append( "\nErrors encountered on the last pass:\n" );
            b.append( indent( "\t", new BufferedReader( new StringReader( writer.toString() ) ) ) );
            
            data.addStatusMessage( b.toString() );
//            System.out.println( b.toString() );
        }
    }
    
    private String indent( String tab, BufferedReader r ) {
        String s;
        String ret = "";
        try {
            while( (s = r.readLine()) != null ) {
                ret += tab + s + "\n";
            }
        }
        catch( IOException e ) {
            // shouldn't happen, since we're reading from strings and not a file or anything
            // but print it out, just in case
            e.printStackTrace( System.out );
        }
        return ret;
    }
    
    /**
     * Restores the values that haven't been changed since the preprocessor processed
     * them to their original values before they were preprocessed.
     */
    void restore() {
        for( Iterator<String> iter = initialValues.keySet().iterator(); iter.hasNext(); ) {
            try {
                String name = iter.next();
                String processedValue = processedValues.get( name );
                Object finalValue = data.get( name );
                if( finalValue == processedValue ||
                        ( finalValue != null
                                && processedValue != null
                                && data.getType( name ).equals( Variable.TYPE_TEXT )
                                && finalValue.equals( processedValue ) ) ) {
                    // if the value wasn't changed by the component then restore the value
                    data.setPlain( name, initialValues.get( name ) );
                }
            }
            catch( Exception e ) {
                e.printStackTrace( System.out );
            }
        }
    }
    
    /**
     * Processes inline Jython code in the given string, returning the resulting
     * string. Returns <tt>null</tt> if <tt>s</tt> is <tt>null</tt>.
     * @param s the string to process
     * @param includeVars the names of the variables to include in the interpreter's context
     * @return the result of processing the given string.
     */
    private String eval( String s, Set<String> includeVars ) throws PyException {
        if( s != null ) {
            // find delimited string blocks denoted by "START_DELIMITER
            // <code/variables> END_DELIMITER" and evaluate
            // each one. Return final result with the string blocks replaced by
            // its evaluated return value.
            String[] startSubstrings = s.split( START_DELIMITER );
            String result = "";
            int startIndex = 0;
            if( s.indexOf( START_DELIMITER ) != 0 ) {
                result = startSubstrings[ 0 ];
                startIndex = 1;
            }
            for( int i = startIndex; i < startSubstrings.length; i++ ) {
                String startString = startSubstrings[ i ];
                String[] segments = startString.split( END_DELIMITER );
                String code = segments[ 0 ];
                if( code != null && ( !code.equals( "" ) ) ) {
                    setupInterpreter( includeVars );
                    result += interpret( code );
                }
                if( segments.length > 1 ) {
                    result += segments[ 1 ];
                }
            }
//            component.setErrorOutput( component.getErrorOutput() + errorString );
            return result;
        }
        else {
            return null;
        }
    }
    
    private void setupInterpreter( Set<String> includeVars ) {
        interpreter = new PythonInterpreter();
        for( Iterator<String> iter = includeVars.iterator(); iter.hasNext(); ) {
            String name = iter.next();
            try {
                interpreter.set( name, data.get( name ) );
            }
            catch( ClassNotFoundException e ) {
            }
            catch( IOException e ) {
            }
        }
    }
    
    /**
     * Interprets the given Jython code and returns the result.
     * @param s the string of Jython code to interpret.
     * @return the result of interpreting the given Jython code.
     */
    private String interpret( String s ) throws PyException {
        StringWriter outputWriter = new StringWriter();
        StringWriter errWriter = new StringWriter();
        interpreter.setOut( outputWriter );
        interpreter.setErr( errWriter );
        PyObject pyObj = interpreter.eval( s );
        return pyObj.__str__().toString();
    }

//    private String attachError( String error ) {
//        errorNumber = errorNumber + 1;
//        errorString += "\nCode processor error #" + errorNumber + ":\n" + error + "\n";
//        System.out.println( "Error has occured:\n" + errorString );
//        return "<code processor error:" + errorNumber + ">";
//    }
}
