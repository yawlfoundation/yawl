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

package org.yawlfoundation.yawl.monitor.jsf;

import com.sun.rave.web.ui.appbase.AbstractSessionBean;
import com.sun.rave.web.ui.component.Button;
import com.sun.rave.web.ui.component.Script;
import com.sun.rave.web.ui.component.PanelLayout;
import org.jdom.Element;
import org.yawlfoundation.yawl.engine.instance.CaseInstance;
import org.yawlfoundation.yawl.engine.instance.ParameterInstance;
import org.yawlfoundation.yawl.engine.instance.WorkItemInstance;
import org.yawlfoundation.yawl.logging.table.YLogEvent;
import org.yawlfoundation.yawl.monitor.MonitorClient;
import org.yawlfoundation.yawl.monitor.sort.CaseOrder;
import org.yawlfoundation.yawl.monitor.sort.ItemOrder;
import org.yawlfoundation.yawl.monitor.sort.ParamOrder;
import org.yawlfoundation.yawl.monitor.sort.TableSorter;
import org.yawlfoundation.yawl.resourcing.datastore.eventlog.BaseEvent;
import org.yawlfoundation.yawl.resourcing.datastore.eventlog.ResourceEvent;
import org.yawlfoundation.yawl.resourcing.jsf.*;
import org.yawlfoundation.yawl.util.JDOMUtil;

import javax.faces.FacesException;
import javax.faces.event.ActionEvent;
import javax.faces.application.Application;
import javax.faces.application.NavigationHandler;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/*
 * Session scope data bean for all the worklist and admin pages. Each logged in user
 * gets an individual instance of this object.
 *
 * @author Michael Adams
 *
 * Create Date: 21/10/2007
 * Last Date: 28/05/2008
 */


public class SessionBean extends AbstractSessionBean {

    // REQUIRED AND/OR IMPLEMENTED ABSTRACT SESSION BEAN METHODS //

    private int __placeholder;

    private void _init() throws Exception { }

     // Constructor
    public SessionBean() { }

    /** @return a reference to the application data bean. */
    protected ApplicationBean getApplicationBean() {
        return (ApplicationBean) getBean("ApplicationBean");
    }

    public void init() {
        super.init();

        // *Note* - this code should NOT be modified
        try {
            _init();
        } catch (Exception e) {
            log("SessionBean1 Initialization Failure", e);
            throw e instanceof FacesException ? (FacesException) e: new FacesException(e);
        }
    }

    public void passivate() { }

    public void activate() { }

    public void destroy() {

        // getApplicationBean() will throw a NPE if the session has already
        // timed out due to inactivity
        try {
            ApplicationBean app = getApplicationBean();
            if (app != null) {
              //  if (participant != null) {
              //      app.removeSessionReference(participant.getID()) ;
              //      app.removeLiveUser(userid);
              //  }

            }
        }
        catch (Exception e) {
            // write to log that session has already expired
        }
    }

    /*****************************************************************************/

    // COMPONENTS SHARED BY SEVERAL PAGES //

    private Script script = new Script();                     // shared jscript ref

    public Script getScript() { return script; }

    public void setScript(Script s) { script = s; }


    private Button btnRefresh = new Button();

    public Button getBtnRefresh() { return btnRefresh; }

    public void setBtnRefresh(Button btn) { btnRefresh = btn; }


    private Button btnLogout = new Button();

    public Button getBtnLogout() { return btnLogout; }

    public void setBtnLogout(Button btn) { btnLogout = btn; }


    private Button btnBack = new Button();

    public Button getBtnBack() { return btnBack; }

    public void setBtnBack(Button btn) { btnBack = btn; }


    /******************************************************************************/

    // MEMBERS, GETTERS & SETTERS //

    private String userid;                          // current userid of this session

    public String getUserid() { return userid; }

    public void setUserid(String id) { userid = id; }


    private String sessionhandle;                   // engine allocated handle

    public String getSessionhandle() { return sessionhandle; }

    public void setSessionhandle(String handle) { sessionhandle = handle; }


    private String userFullName ;                  // full name of current user

    public void setUserName(String userName) { userFullName = userName ; }

    public String getUserName() { return userFullName ; }



    /** @return the id of the external http session */
    public String getExternalSessionID() {
        HttpSession session = getExternalSession();
        return (session != null) ? session.getId() : null ;
    }


    /** @return the external http session */
    public HttpSession getExternalSession() {
        ExternalContext externalContext = getFacesContext().getExternalContext();
        if (externalContext != null)
           return ((HttpSession) externalContext.getSession(false));
        else
           return null ;
    }

    int defaultSessionTimeoutValue = 3600;                              // 60 minutes

    public int getDefaultSessionTimeoutValue() {
        return defaultSessionTimeoutValue;
    }

    public void setDefaultSessionTimeoutValue(int value) {
        defaultSessionTimeoutValue = value;
    }

    public void resetSessionTimeout() {
        HttpSession session = getExternalSession();
         if (defaultSessionTimeoutValue != session.getMaxInactiveInterval()) {
             session.setMaxInactiveInterval(defaultSessionTimeoutValue);
         }
    }

    boolean sessionTimeoutValueChanged = false;

    public boolean isSessionTimeoutValueChanged() {
        return sessionTimeoutValueChanged;
    }

    public void setSessionTimeoutValueChanged(boolean changed) {
        sessionTimeoutValueChanged = changed;
    }


    /*******************************************************************************/

    // ENUMS FOR ACTIVE PAGES AND DYN FORM TYPES //

    private ApplicationBean.PageRef activePage ;


    public ApplicationBean.PageRef getActivePage() { return activePage; }

    public void setActivePage(ApplicationBean.PageRef page) {
        activePage = page;
    }



    /**********************************************************************************/

    // PAGE NAVIGATION METHODS //

    // logs out of session //
    public void doLogout() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context != null) {             // if null, session already destroyed
            HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
            session.invalidate();
        }
    }



    /**
     * redirects to the specified page
     * @param page the name of the page to go to
     */
    public void gotoPage(String page) {
        Application app = getApplication() ;
        if (app != null) {
            NavigationHandler navigator = app.getNavigationHandler();
            navigator.handleNavigation(getFacesContext(), null, page);
        }

        // if app is null, session has been destroyed
        else {
            try {
                FacesContext.getCurrentInstance().getExternalContext().redirect("msLogin.jsp");
            }
            catch (IOException ioe) {
                // message about destroyed app
            }
        }
    }


    /********************************************************************************/

    private MonitorClient _monClient = getApplicationBean().getMonitorClient();

    private PanelLayout transparentPanel = new PanelLayout();

     public PanelLayout getTransparentPanel() {
         return transparentPanel;
     }

     public void setTransparentPanel(PanelLayout panel) {
         transparentPanel = panel;
     }


    private MessagePanel messagePanel = new MessagePanel() ;

    public MessagePanel getMessagePanel() {
        return messagePanel;
    }

    public void setMessagePanel(MessagePanel messagePanel) {
        this.messagePanel = messagePanel;
    }

    public void showMessagePanel() {
        transparentPanel.setVisible(messagePanel.isVisible());
        messagePanel.show(240);
    }

    public String messagePanelOKBtnAction(ActionEvent event) {
        showMessagePanel();
        getApplicationBean().refresh();
        return null;
    }


    private TableSorter _sorter = new TableSorter();

    public void clearCaches() {
        initActiveCases();
    }


    /*** ACTIVE CASES ***/

    private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd H:mm:ss");

    public String getStartupTime() {
        long startTime = _monClient.getStartupTime();
        return (startTime > 0) ? dateFormatter.format(new Date(startTime)) : "Unavailable";
    }


    public String getCaseStartTime() {
        return (caseStartTime > 0) ? dateFormatter.format(new Date(caseStartTime)) : "Unavailable";
    }


    private String caseData;
    private String startingServiceName;
    private String caseStartedByName;
    private long caseStartTime;

    public String getCaseData() {
        return caseData;
    }

    public String getStartingServiceName() {
        return startingServiceName ;
    }
    
    public String getCaseStartedByName() {
        return caseStartedByName ;
    }


    private List<CaseInstance> activeCases = initActiveCases();

    public List<CaseInstance> getActiveCases() {
        return activeCases;
    }

    public void setActiveCases(List<CaseInstance> caseList) {
        activeCases = caseList;
    }

    public List<CaseInstance> refreshActiveCases(boolean sortPending) {
        try {
            activeCases = getApplicationBean().getMonitorClient().getCases();
            if (! sortPending) activeCases = _sorter.applyCaseOrder(activeCases);
        }
        catch (IOException ioe) {
            activeCases = null;
        }
        return activeCases;
    }


    public void sortActiveCases(TableSorter.CaseColumn column) {
        refreshActiveCases(true);
        activeCases = _sorter.sort(activeCases, column);
    }


    public CaseOrder getCurrentCaseOrder() {
        return _sorter.getCaseOrder();
    }


    private List<CaseInstance> initActiveCases() {
        refreshActiveCases(true);
        return _sorter.sort(activeCases, TableSorter.CaseColumn.Case);
    }


    /*** ITEMS FOR CASE ***/

    private CaseInstance selectedCase ;

    public CaseInstance getSelectedCase() { return selectedCase; }

    public void setSelectedCase(CaseInstance caseInstance) {
        selectedCase = caseInstance;
        String caseID = caseInstance.getCaseID();
        if (getCurrentItemOrder().getColumn() == TableSorter.ItemColumn.Undefined) {
            sortCaseItems(TableSorter.ItemColumn.ItemID);
        }
        else {
            refreshCaseItems(caseID, false);
        }

        try {
            caseData = _monClient.getCaseData(caseID);
            String caseEvent = _monClient.getCaseEvent(caseID, "CaseStart");
            if (! caseEvent.startsWith("<fail")) {
                Element caseElem = JDOMUtil.stringToElement(caseEvent).getChild("event");
                caseStartTime = new Long(caseElem.getChildText("timestamp"));
                startingServiceName = _monClient.getServiceName(new Long(caseElem.getChildText("serviceKey")));
            }
            else {
                caseStartTime = 0;
                startingServiceName = "Unavailable";
            }
            caseStartedByName = _monClient.getCaseStartedBy(caseID);
        }
        catch (IOException ioe) {
            caseData =  "Unavailable";
            caseStartTime = 0;
            startingServiceName = "Unavailable";
            caseStartedByName = "Unavailable";
        }
    }

    public void setCaseSelection(int index) {
        setSelectedCase(activeCases.get(index));
    }

    private List<WorkItemInstance> caseItems;

    public List<WorkItemInstance> getCaseItems() { return caseItems; }

    public void setCaseItems(List<WorkItemInstance> items) { caseItems = items; }

    public List<WorkItemInstance> refreshCaseItems(boolean sortPending) {
        return refreshCaseItems(selectedCase.getCaseID(), sortPending);
    }

    public List<WorkItemInstance> refreshCaseItems(String caseID, boolean sortPending) {
        try {
            caseItems = getApplicationBean().getMonitorClient().getWorkItems(caseID);
            if (! sortPending) caseItems = _sorter.applyItemOrder(caseItems);
        }
        catch (IOException ioe) {
            caseItems = null;
        }
        return caseItems;
    }

    public void sortCaseItems(TableSorter.ItemColumn column) {
        refreshCaseItems(selectedCase.getCaseID(), true);
        caseItems = _sorter.sort(caseItems, column);
    }


    public ItemOrder getCurrentItemOrder() {
        return _sorter.getItemOrder();
    }


    /**** PARAMS FOR WORKITEM ***/

    private WorkItemInstance selectedItem;

    public WorkItemInstance getSelectedItem() { return selectedItem; }

    public void setItemSelection(int index) {
        setSelectedItem(caseItems.get(index));
    }


    public void setSelectedItem(WorkItemInstance itemInstance) {
        selectedItem = itemInstance;
        if (getCurrentParamOrder().getColumn() == TableSorter.ParamColumn.Undefined) {
            sortItemParams(TableSorter.ParamColumn.Name);
        }
        else {
            refreshItemParams(itemInstance.getID(), false);
        }
    }

    public ParamOrder getCurrentParamOrder() {
        return _sorter.getParamOrder();
    }

    private List<ParameterInstance> itemParams;

    public List<ParameterInstance> getItemParams() { return itemParams; }

    public void setItemParams(List<ParameterInstance> params) { itemParams = params; }

    public List<ParameterInstance> refreshItemParams(boolean sortPending) {
        return refreshItemParams(selectedItem.getID(), sortPending);
    }

    public List<ParameterInstance> refreshItemParams(String itemID, boolean sortPending) {
        try {
            itemParams = _monClient.getParameters(itemID);
            if (! sortPending) itemParams = _sorter.applyParamOrder(itemParams);
            refreshItemEngineLogEvents(itemID);
            refreshItemResourceLogEvents(itemID);
        }
        catch (IOException ioe) {
            itemParams = new ArrayList<ParameterInstance>();
        }
        return itemParams;
    }

    public void sortItemParams(TableSorter.ParamColumn column) {
        refreshItemParams(selectedItem.getID(), true);
        if (itemParams != null) {
            itemParams = _sorter.sort(itemParams, column);
        }    
    }

    public void refreshItemEngineLogEvents(String itemID) {
        try {
            itemEngineLogEvents = _monClient.getEventsForWorkItem(itemID);
        }
        catch (IOException ioe) {
            itemEngineLogEvents = new ArrayList<YLogEvent>();
        }
        Collections.sort(itemEngineLogEvents, new EngineLogEventTimeComparator());
    }

    public void refreshItemResourceLogEvents(String itemID) {
        try {
            itemResourceLogEvents = _monClient.getResourceEventsForWorkItem(itemID);
        }
        catch (IOException ioe) {
            itemResourceLogEvents = new ArrayList<ResourceEvent>();
        }
        Collections.sort(itemResourceLogEvents, new ResourceLogEventTimeComparator());
    }

    private List<YLogEvent> itemEngineLogEvents = null;

    private List<ResourceEvent> itemResourceLogEvents = null;


    public List<YLogEvent> getItemEngineLogEvents() {
        return (itemEngineLogEvents != null) ? itemEngineLogEvents : new ArrayList<YLogEvent>();
    }

    public List<ResourceEvent> getItemResourceLogEvents() {
        return (itemResourceLogEvents != null) ? itemResourceLogEvents : new ArrayList<ResourceEvent>();
    }

    class EngineLogEventTimeComparator implements Comparator<YLogEvent> {

        public int compare(YLogEvent e1, YLogEvent e2) {
            long difference = e1.getTimestamp() - e2.getTimestamp();

            // guard against integer overrun
            return difference > 0 ? 1 : difference < 0 ? -1 : 0;
        }
    }

    class ResourceLogEventTimeComparator implements Comparator<BaseEvent> {

        public int compare(BaseEvent e1, BaseEvent e2) {
            long difference = e1.get_timeStamp() - e2.get_timeStamp();

            // guard against integer overrun
            return difference > 0 ? 1 : difference < 0 ? -1 : 0;
        }
    }

}