/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */
package org.yawlfoundation.yawl.engine.interfce.interfaceX;

import org.yawlfoundation.yawl.engine.YWorkItem;
import org.jdom.Document;

import java.util.List;

/**
 * Interface to be implemented by classes which register with the engine to receive
 * notifications of (i) the occurrence of process exceptions and (ii) possible
 * process 'points' to be checked for exceptions.
 *
 *  @author Michael Adams
 *  04/07/2006
 *
 * Based heavily on the ObserverGateway interface writtem by Andrew Hastie
 */

public interface ExceptionGateway
{
    String getScheme();

    /**
     * Announced by the engine when a new workitem gets enabled to allow for the
     * checking of pre & post constraints.
     * @param item - the enabled workitem
     * @param data - the workitem's data
     * @param preCheck - true if before item execution, false if after
     */
    void announceCheckWorkItemConstraints(YWorkItem item, Document data, boolean preCheck) ;

    /**
     * Announced by the engine when a case begins or completes to allow for the
     * checking of pre & post constraints.
     * @param specID - the id of the specfication of which the case is an instance
     * @param caseID - the id of the case
     * @param data - the case's data
     * @param preCheck - true if before item execution, false if after
     */
    void announceCheckCaseConstraints(String specID, String caseID, String data, boolean preCheck) ;


    /**
     * Announced by the engine when a workitem has reached a deadline.
     * @param item - the timed out workitem (associated with the timeService)
     * @param taskList - a list of tasks running in parallel with the timedout workitem
     */
    void announceTimeOut(YWorkItem item, List taskList);


    /**
     * Announced by the engine when a case has been cancelled
     * @param caseID - the cancelled case
     */
    void announceCaseCancellation(String caseID);


    /************** FOR FUTURE IMPLEMENTATION *************/
    void announceWorkitemAbort(YWorkItem item);

    void announceResourceUnavailable(YWorkItem item);

    void announceConstraintViolation(YWorkItem item);
    /******************************************************/

}
