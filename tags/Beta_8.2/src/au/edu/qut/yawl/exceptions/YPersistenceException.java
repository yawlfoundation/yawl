/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.exceptions;

/*
 * 
 * @author Lachlan Aldred
 * 

 */

/**
 * Exception which indicates a failure has occured within the persistence layer of the YAWL engine.<P>
 *
 * Notes: This exception should be caught and handled as a fatal exception within the engine code. As it
 *        indicates some failure to persist a runtime object to storage, the usual action would be to gracefully
 *        terminate the engine without processing any other work.
 */
public class YPersistenceException extends YAWLException {
    public YPersistenceException(String message) {
        super(message);
    }

    public YPersistenceException(Throwable cause) {
        super(cause);
    }

    public YPersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
