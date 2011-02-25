/*
 * Copyright (c) 2004-2010 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.resourcing.jsf;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.resourcing.ResourceManager;
import org.yawlfoundation.yawl.resourcing.jsf.dynform.DynFormFactory;
import org.yawlfoundation.yawl.util.HttpURLValidator;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Author: Michael Adams
 * Creation Date: 1/09/2009
 */
public class FormViewer {

    private SessionBean _sb;
    private ResourceManager _rm;
    private Logger _log;

    public FormViewer() {
        _rm = ResourceManager.getInstance();
        _log = Logger.getLogger(this.getClass());
    }

    public FormViewer(SessionBean sb) {
        this();
        setSessionBean(sb); 
    }

    public SessionBean getSessionBean() { return _sb; }

    public void setSessionBean(SessionBean sb) { _sb = sb; }


    public String display(WorkItemRecord wir) {
        String result = null;

        // if there's a custom form for this item, use it
        if (wir.getCustomFormURL() != null) {
            result = showCustomForm(wir) ;
        }

        // otherwise default to a dynamic form
        if (result == null) {
            _sb.setDynFormType(ApplicationBean.DynFormType.tasklevel);

            DynFormFactory factory = _sb.getDynFormFactoryInstance();
            factory.setDisplayedWIR(wir);
            if (factory.initDynForm("YAWL 2.1 - Edit Work Item")) {
                result = "showDynForm" ;
            }
            else {
                result = "<failure>Cannot view item contents - problem initialising " +
                               "dynamic form from task specification. " +
                               "Please see the log files for details.</failure>";
            }
        }
        return result;
    }


    public String postDisplay(WorkItemRecord wir) {
        String result = "<success/>";
        if (_sb.isWirEdit()) {
            result = postEditWIR(wir);
        }
        else if (_sb.isCustomFormPost()){
            result = postCustomForm(wir) ;
        }
        return result;
    }

    
    private String showCustomForm(WorkItemRecord wir) {
        String url = wir.getCustomFormURL();
        if (url != null) {
            String uriPlusParams = buildURI(wir);

            // check custom form exists and responds without error
            String validateMsg = HttpURLValidator.validate(uriPlusParams);
            if (validateMsg.equals("<success/>")) {
                _sb.setCustomFormPost(true);
                adjustSessionTimeout(wir);
                return uriPlusParams;   // return validated custom form url incl. params
            }
            else _log.warn("Missing or invalid custom form: '" + url + "', message: " +
                    StringUtil.unwrap(validateMsg) + ". Defaulting to dynamic form.");
        }
        else _log.warn("Unspecified form URI. Defaulting to dynamic form.");

        return null;                   // invalid form
    }


    private String buildURI(WorkItemRecord wir) {
        StringBuilder redir = new StringBuilder(wir.getCustomFormURL());
        redir.append((redir.indexOf("?") == -1) ? "?" : "&")      // any static params?
             .append("workitem=")
             .append(wir.getID())
             .append("&participantid=")
             .append(_sb.getParticipant().getID())
             .append("&handle=")
             .append(_sb.getSessionhandle())
             .append("&source=")
             .append(getSourceURI());

        return redir.toString();
    }


    private void adjustSessionTimeout(WorkItemRecord wir) {

        // get new timeout value (if any)
        String rawValue = null;
        Element data = wir.getDataList();
        if (data != null) {
            rawValue = data.getChildText("ySessionTimeout");
        }

        // convert to int, remember current timeout, set new timeout (as secs)
        if (rawValue != null) {
            try {
                int minutes = new Integer(rawValue);
                HttpSession session = _sb.getExternalSession();
                _sb.setDefaultSessionTimeoutValue(session.getMaxInactiveInterval()) ;
                session.setMaxInactiveInterval(minutes * 60);
                _sb.setSessionTimeoutValueChanged(true);
            }
            catch (NumberFormatException nfe) {
                // bad timeout value supplied - nothing further to do
            }
        }
    }


    private String getSourceURI() {
        String result = "";
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        if (context != null) {
            HttpServletRequest request = (HttpServletRequest) context.getRequest();
            if (request != null) {
                result = request.getRequestURL().toString();
            }
        }
        return result;
    }


    /** updates a workitem after editing on a dynamic form */
    private String postEditWIR(WorkItemRecord wir) {
        String result = "<success/>";
        if (_sb.isWirEdit()) {
            if (wir != null) {
                Element data = JDOMUtil.stringToElement(
                                        _sb.getDynFormFactoryInstance().getDataList());
                wir.setUpdatedData(data);
                _rm.getWorkItemCache().update(wir) ;
                if (_sb.isCompleteAfterEdit()) result = completeWorkItem(wir);
            }
            else {
                result = "<failure>Could not complete workitem. Check log for details.</failure>";
            }
        }
        return result;
    }


    /** takes necessary action after editing a custom form */
    private String postCustomForm(WorkItemRecord wir) {
        String result = "<success/>";

        // reset session timeout if previously changed
        if (_sb.isSessionTimeoutValueChanged()) {
            _sb.resetSessionTimeout();
            _sb.setSessionTimeoutValueChanged(false);
        }

        // retrieve completion flag - if any
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        HttpServletRequest request = (HttpServletRequest) context.getRequest();
        String complete = request.getParameter("complete");

        // complete wir if requested
        if ((complete != null) && complete.equalsIgnoreCase("true")) {
            result = completeWorkItem(wir);
        }
        return result;
    }


    private String completeWorkItem(WorkItemRecord wir) {
        String result = _rm.checkinItem(_sb.getParticipant(), wir);
        return _rm.successful(result) ? "<success/>" : result ;
    }

}
