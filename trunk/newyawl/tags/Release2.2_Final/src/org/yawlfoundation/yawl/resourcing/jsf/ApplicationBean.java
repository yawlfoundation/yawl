/*
 * Copyright (c) 2004-2011 The YAWL Foundation. All rights reserved.
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

import com.sun.rave.web.ui.appbase.AbstractApplicationBean;
import com.sun.rave.web.ui.component.Link;
import com.sun.rave.web.ui.component.PanelLayout;
import com.sun.rave.web.ui.component.StaticText;
import org.yawlfoundation.yawl.authentication.YExternalClient;
import org.yawlfoundation.yawl.elements.YAWLServiceReference;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.resourcing.ResourceManager;
import org.yawlfoundation.yawl.resourcing.WorkQueue;
import org.yawlfoundation.yawl.resourcing.jsf.comparator.YAWLServiceComparator;
import org.yawlfoundation.yawl.resourcing.jsf.comparator.YExternalClientComparator;
import org.yawlfoundation.yawl.resourcing.jsf.dynform.FormParameter;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;
import org.yawlfoundation.yawl.util.YBuildProperties;

import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    // REQUIRED AND/OR IMPLEMENTED ABSTRACT PAGE BEAN METHODS //

    private int __placeholder;

    private void _init() throws Exception { }

    /** Constructor */
    public ApplicationBean() { }

    public void init() {
        super.init();

        // Initialize automatically managed components - do not modify
        try {
            _init();
        } catch (Exception e) {
            log("ApplicationBean Initialization Failure", e);
            throw e instanceof FacesException ? (FacesException) e: new FacesException(e);
        }

        // Add init code here that must complete *after* managed components are initialized
        _rm.registerJSFApplicationReference(this) ;
    }

    public void destroy() { }

    public String getLocaleCharacterEncoding() {
        return super.getLocaleCharacterEncoding();
    }


    /*******************************************************************************/

    // GLOBAL ENUMS AND COMPONENTS //

    public enum PageRef { adminQueues, caseMgt, customServices, dynForm, Login,
        orgDataMgt, nonHumanMgt, participantData, selectUser, userWorkQueues,
        viewProfile, addInstance, teamQueues, externalClients, calendarMgt, secResMgt;

        public String getFileName() { return name() + ".jsp"; }
    }


    public enum TabRef { offered, allocated, started, suspended, unoffered, worklisted }

    public enum DynFormType { netlevel, tasklevel }


    // favIcon appears in the browser's address bar for all pages
    private Link favIcon = new Link() ;

    public Link getFavIcon() { return favIcon; }

    public void setFavIcon(Link link) { favIcon = link; }


    /*******************************************************************************/

    // MEMBERS AND METHODS USED BY ALL SESSIONS //

    private static final int PAGE_AUTO_REFRESH_RATE = 30 ;

    // reference to resource manager
    private ResourceManager _rm = ResourceManager.getInstance();

    public ResourceManager getResourceManager() { return _rm; }


    public int getDefaultJSFRefreshRate() {
        return PAGE_AUTO_REFRESH_RATE ;
    }


    // mapping of participant id to each session
    private Map<String, SessionBean> sessionReference =
            new ConcurrentHashMap<String, SessionBean>();

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


    /**********************************************************************/

    // set of participants currently logged on
    private Set<String> liveUsers = new HashSet<String>();

    public Set<String> getLiveUsers() {
        return liveUsers;
    }

    public void setLiveUsers(Set<String> userSet) {
        liveUsers = userSet;
    }

    public void addLiveUser(String userid) {
        liveUsers.add(userid);
    }

    public void removeLiveUser(String userid) {
        if (isLoggedOn(userid)) liveUsers.remove(userid);
    }

    public boolean isLoggedOn(String userid) {
        return liveUsers.contains(userid);
    }


    /** @return true if the id passed is not a currently used userid */
    public boolean isUniqueUserID(String id) {
        return (! getResourceManager().isKnownUserID(id));
    }


    /**********************************************************************/

    /** @return true if the workitem has no parameters */
    public boolean isEmptyWorkItem(WorkItemRecord wir) {
        try {
            Map<String, FormParameter> params = getWorkItemParams(wir);
            return ((params == null) || (params.size() == 0)) ;
        }
        catch (Exception e) { return false; }
    }


    private Map<String, Map<String, FormParameter>> _workItemParams = new
            ConcurrentHashMap<String, Map<String, FormParameter>>();


    public Map<String, FormParameter> getWorkItemParams(WorkItemRecord wir) {
        Map<String, FormParameter> result = _workItemParams.get(wir.getID());
        if (result == null) {
            try {
                result = getResourceManager().getWorkItemParamsInfo(wir);
                if (result != null)
                    _workItemParams.put(wir.getID(), result);
            }
            catch (Exception e) { return null; }
        }
        return result;
    }

    public void removeWorkItemParams(WorkItemRecord wir) {
        _workItemParams.remove(wir.getID());
        removeFromReofferMap(wir);
    }


    public void removeWorkItemParamsForCase(String caseID) {
        Set<String> toRemove = new HashSet<String>();
        for (String id : _workItemParams.keySet()) {
            if (id.startsWith(caseID + ".")) {
                toRemove.add(id);
            }
        }
        for (String id : toRemove) {
            _workItemParams.remove(id);
        }
        removeCaseFromReofferMap(caseID);
    }


    /**********************************************************************/

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


    public String rPadSp(String str, int padlen) {
        int len = padlen - str.length();
        if (len < 1) return str ;

        StringBuilder result = new StringBuilder(str) ;
        for (int i = 0; i < len; i++) {
            result.append("&nbsp;");
        }
        return result.toString();
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

    public void refresh() {
        FacesContext context = FacesContext.getCurrentInstance();
        Application application = context.getApplication();
        ViewHandler viewHandler = application.getViewHandler();
        UIViewRoot viewRoot = viewHandler.createView(context, context
             .getViewRoot().getViewId());
        context.setViewRoot(viewRoot);
    }

    
    public void redirect(String uri) {
        try {
            ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
            if (context != null) {
                context.redirect(uri);
            }
        }
        catch (IOException ioe) {
            // nothing to do
        }
    }

    public void synch() {
        _rm.sanitiseCaches();
    }


    private boolean exceptionServiceEnabled = _rm.hasExceptionServiceEnabled() ;

    public boolean isExceptionServiceEnabled() {
        return exceptionServiceEnabled;
    }

    public void setExceptionServiceEnabled(boolean enabled) {
        exceptionServiceEnabled = enabled;
    }


    private boolean visualizerEnabled = _rm.isVisualiserEnabled() ;

    public boolean isVisualizerEnabled() {
        return visualizerEnabled;
    }

    public void setVisualizerEnabled(boolean enabled) {
        visualizerEnabled = enabled;
    }


    private String _resourceServiceBaseURI = null;

    public String getResServiceBaseURI() {
        if (_resourceServiceBaseURI == null) {
            String uri = _rm.getServiceURI();
            _resourceServiceBaseURI = uri.replaceFirst("/resourceService/ib", "");
        }
        return _resourceServiceBaseURI;
    }



    public int getActiveQueue(String tabName) {
        int result = WorkQueue.UNDEFINED;
        if (tabName != null) {
            if (tabName.equals("tabOffered")) {
                result = WorkQueue.OFFERED;
            }
            else if (tabName.equals("tabAllocated")) {
                result = WorkQueue.ALLOCATED;
            }
            else if (tabName.equals("tabStarted")) {
                result = WorkQueue.STARTED;
            }
            else if (tabName.equals("tabSuspended")) {
                result = WorkQueue.SUSPENDED;
            }
            else if (tabName.equals("tabUnoffered")) {
                result = WorkQueue.UNOFFERED;
            }
            else if (tabName.equals("tabWorklisted")) {
                result = WorkQueue.WORKLISTED;
            }
        }
        return result;
    }


    /**********************************************************************/

    // the footer text is the text line at the bottom of every worklist and admin page

    private PanelLayout footerPanel;

    public PanelLayout getFooterPanel() { return footerPanel; }

    public void setFooterPanel(PanelLayout panel) { footerPanel = panel; }


    private PanelLayout footerTextPanel;

    public PanelLayout getFooterTextPanel() { return footerTextPanel; }

    public void setFooterTextPanel(PanelLayout panel) { footerTextPanel = panel; }


    private StaticText footerStaticText;

    public StaticText getFooterStaticText() { return footerStaticText; }

    public void setFooterStaticText(StaticText st) { footerStaticText = st; }


    private String _footerText = "";

    public String getFooterText() {
        if (_footerText.length() == 0) {          // only need to build the text once
            String version = "";
            String rsBuild = "";
            String engBuild = "";
            YBuildProperties buildProps = _rm.getBuildProperties();
            if (buildProps != null) {
                version = "YAWL version " + buildProps.getVersion();
                rsBuild = String.format(" | Resource Service build %s (%s)",
                        formatBuildNumber(buildProps.getVersion(), buildProps.getBuildNumber()),
                        buildProps.getBuildDate());
            }
            String engBuildProps = _rm.getEngineBuildProperties();
            if (_rm.successful(engBuildProps)) {
                XNode responseNode = new XNodeParser().parse(engBuildProps);
                XNode propsNode = responseNode.getChild("buildproperties");
                engBuild = String.format(" | Engine build %s (%s)",
                        formatBuildNumber(propsNode.getChildText("Version"),
                                propsNode.getChildText("BuildNumber")),
                        propsNode.getChildText("BuildDate"));
            }
            _footerText = version + engBuild + rsBuild;
        }
        return _footerText;
    }


    private String formatBuildNumber(String version, String buildNumber) {
        Pattern pattern = Pattern.compile("[\\d|\\.]+");
        Matcher m = pattern.matcher(version);
        m.find();
        return m.group() + "." + buildNumber.replaceAll(",", "");
    }


    /**********************************************************************/

    public String checkPassword(String password, String confirmPassword) {
        if (password.length() == 0) {
            return "No password entered.";
        }
        else {
            if (password.length() < 4) {
                return "Password must contain at least 4 characters.";
            }

            if (password.contains(" ")) {
                return "Password cannot contain spaces.";
            }
            if (! password.equals(confirmPassword)) {
                return "Password and confirmation are different.";
            }
        }
        return null;
    }

    /*************************************************************************/

    // if an offered workitem has no resource map (usually a pre-2.0 spec), it can't
    // be reoffered on the admin screen. This map keeps track of listed workitems and
    // whether they can be reoffered or not.

    private Map<String, Boolean> _canReofferMap = new ConcurrentHashMap<String, Boolean>() ;

    public boolean canReoffer(WorkItemRecord wir) {
        Boolean okToReoffer = _canReofferMap.get(wir.getID());
        if (okToReoffer == null) {
            okToReoffer = (_rm.getCachedResourceMap(wir) != null);
            _canReofferMap.put(wir.getID(), okToReoffer);
        }
        return okToReoffer;
    }

    public void removeFromReofferMap(WorkItemRecord wir) {
        _canReofferMap.remove(wir.getID());
    }

    public void removeCaseFromReofferMap(String caseID) {
        int len = caseID.length();
        for (String id : _canReofferMap.keySet()) {
            if (id.startsWith(caseID) && ":.".contains(id.substring(len, len + 1))) {
                _canReofferMap.remove(id);
            }
        }
    }


    /****** This section used by the 'Service Mgt' Page ***************************/

    List<YAWLServiceReference> registeredServices;


    public List<YAWLServiceReference> getRegisteredServices() {
        if (registeredServices == null) refreshRegisteredServices() ;
        return registeredServices;
    }


    public void setRegisteredServices(List<YAWLServiceReference> services) {
        registeredServices = services ;
    }


    public String removeRegisteredService(int listIndex) {
        String result = null;
        try {
            YAWLServiceReference service = registeredServices.get(listIndex);
            result = _rm.removeRegisteredService(service.getServiceID());
            if (_rm.successful(result)) refreshRegisteredServices();
        }
        catch (IOException ioe) {
            // message ...
        }
        return result ;
    }


    public String addRegisteredService(String name, String pw, String uri, String doco) {
        String result = null;
        try {
            YAWLServiceReference service = new YAWLServiceReference(uri, null, name, pw, doco);
            result = _rm.addRegisteredService(service);
            if (_rm.successful(result)) refreshRegisteredServices();
        }
        catch (IOException ioe) {
            // message ...
        }
        return result ;
    }


    public void refreshRegisteredServices() {
        Set<YAWLServiceReference> services = _rm.getRegisteredServices();
        if (services != null) {

            // get & sort the items
            List<YAWLServiceReference> servList = new ArrayList<YAWLServiceReference>();
            for (YAWLServiceReference service : services) {
                if (service.isAssignable())
                    servList.add(service) ;
            }
            Collections.sort(servList, new YAWLServiceComparator());
            synchronized(this) {
                registeredServices = servList;
            }    
        }
        else registeredServices = null ;
    }


    /****** This section used by the 'External App Mgt' Page ***************************/

    List<YExternalClient> externalClients;


    public List<YExternalClient> getExternalClients() {
        if (externalClients == null) refreshExternalClients() ;
        return externalClients;
    }


    public void setExternalClients(List<YExternalClient> clients) {
        externalClients = clients ;
    }


    public YExternalClient getSelectedExternalClient(int listIndex) {
        return externalClients.get(listIndex);
    }


    public String removeExternalClient(int listIndex) {
        String result;
        try {
            YExternalClient client = externalClients.get(listIndex);
            result = _rm.removeExternalClient(client.getUserName());
            if (_rm.successful(result)) refreshExternalClients();
        }
        catch (IOException ioe) {
            result = "Error attempting to remove client. Please see the log files for details.";
        }
        return result ;
    }


    public String updateExternalClient(String name, String pw, String doco) {
        try {
            return _rm.updateExternalClient(name, pw, doco);
        }
        catch (IOException ioe) {
            return "Error attempting to update client. Please see the log files for details.";
        }
    }



    public String addExternalClient(String name, String pw, String doco) {
        String result;
        if (name.equals("admin")) {
            return "Cannot add client 'admin' because it is a reserved client name.";
        }
        try {
            YExternalClient client = new YExternalClient(name, pw, doco);
            result = _rm.addExternalClient(client);
            if (_rm.successful(result)) refreshExternalClients();
        }
        catch (IOException ioe) {
            result = "Error attempting to add new client. Please see the log files for details.";
        }
        return result ;
    }


    public void refreshExternalClients() {
        try {
            List<YExternalClient> clients =
                    new ArrayList<YExternalClient>(_rm.getExternalClients());
            Collections.sort(clients, new YExternalClientComparator());
            synchronized(this) {
                externalClients = clients;
            }
        }
        catch (IOException ioe) {
            externalClients = null;
        }
    }

}