/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing.jsf;

import org.yawlfoundation.yawl.resourcing.rsInterface.WorkQueueGateway;
import org.yawlfoundation.yawl.elements.data.YParameter;
import com.sun.rave.web.ui.appbase.AbstractApplicationBean;
import javax.faces.FacesException;
import javax.faces.context.FacesContext;
import javax.faces.application.NavigationHandler;
import javax.faces.application.Application;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Application scope data bean for the worklist and admin pages.
 *
 *  @author Michael Adams
 *  BPM Group, QUT Australia
 *  v0.1, 21/10/2007
 *
 *  Boilerplate code generated by Sun Java Studio Creator 2.1
 *
 *  Last Date: 05/01/2008
 */

public class ApplicationBean extends AbstractApplicationBean {

    /*******************************************************************************/
    /**************** START Creator auto generated code ****************************/
    /*******************************************************************************/

    private int __placeholder;

    private void _init() throws Exception { }

    /** Constructor */
    public ApplicationBean() { }

    public void init() {
        super.init();

        // Add init code here that must complete *before* managed components are initialized

        // Initialize automatically managed components - do not modify
        try {
            _init();
        } catch (Exception e) {
            log("ApplicationBean Initialization Failure", e);
            throw e instanceof FacesException ? (FacesException) e: new FacesException(e);
        }

        // Add init code here that must complete *after* managed components are initialized
        _wqg.registerJSFApplicationReference(this) ;
    }

    public void destroy() { }

    public String getLocaleCharacterEncoding() {
        return super.getLocaleCharacterEncoding();
    }

    /*******************************************************************************/
    /**************** END Creator auto generated code  *****************************/
    /*******************************************************************************/

    // reference of gateway to resourceService
    private WorkQueueGateway _wqg = WorkQueueGateway.getInstance();


    public WorkQueueGateway getWorkQueueGateway() { return _wqg ; }

    public int getDefaultJSFRefreshRate() { return 5 ; }

    private Map<String, SessionBean> sessionReference = new HashMap<String, SessionBean>();

    public void addSessionReference(String participantID, SessionBean sBean) {
        sessionReference.put(participantID, sBean) ;
    }

    public SessionBean getSessionReference(String participantID) {
        return sessionReference.get(participantID) ;
    }

    public void removeSessionReference(String participantID) {
        sessionReference.remove(participantID) ;
    }

    public void refreshUserWorkQueues(String participantID) {
        SessionBean sessionBean = sessionReference.get(participantID) ;
        if (sessionBean != null) sessionBean.refreshUserWorkQueues();
    }


    /**
     * formats a long time value into a string of the form 'ddd:hh:mm:ss'
     * @param age the time value (in milliseconds)
     * @return the formatted time string
     */
    public String formatAge(long age) {
        long secsPerHour = 60 * 60 ;
        long secsPerDay = 24 * secsPerHour ;
        age = age / 1000 ;                             // ignore the milliseconds

        long days = age / secsPerDay ;
        age %= secsPerDay ;
        long hours = age / secsPerHour ;
        age %= secsPerHour ;
        long mins = age / 60 ;
        age %= 60 ;                                    // seconds leftover
        return String.format("%d:%02d:%02d:%02d", days, hours, mins, age) ;
    }


    public Map<String, FormParameter> yParamListToFormParamMap(List params) {
        Map<String, FormParameter> result = new HashMap<String, FormParameter>();
        for (Object obj : params) {
            YParameter param = (YParameter) obj ;
            result.put(param.getName(), new FormParameter(param)) ;
        }
        if (result.isEmpty()) result = null ;
        return result ;
    }

    public String rPad (String str, int padlen) {
        int len = padlen - str.length();
        if (len < 1) return str ;

        StringBuilder padded = new StringBuilder(str);
        char[] spaces  = new char[len] ;
        for (int i = 0; i < len; i++) spaces[i] = ' ';
        padded.append(spaces) ;
        return padded.toString();
    }
}
