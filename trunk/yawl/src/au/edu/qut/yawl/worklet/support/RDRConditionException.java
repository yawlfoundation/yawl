/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organisations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.worklet.support;


/**
 *  RDRConditionException Class.
 *
 *  An RDRConditionException is thrown when an attenpt is made to evaluate
 *  a rule's condition and is found to be malformed or does not evaluate
 *  to a boolean result.
 *
 *  @author Michael Adams
 *  BPM Group, QUT Australia
 *  m3.adams@qut.edu.au
 *  v0.7, 10/12/2005
 */

public class RDRConditionException extends Exception {
   RDRConditionException() {
       super();
   }
   
   RDRConditionException(String message) {
       super(message);
   }

}  