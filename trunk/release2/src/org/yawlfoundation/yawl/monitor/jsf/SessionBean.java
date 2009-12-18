/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.monitor.jsf;

import com.sun.rave.web.ui.appbase.AbstractSessionBean;
import com.sun.rave.web.ui.component.Button;
import com.sun.rave.web.ui.component.Script;
import org.yawlfoundation.yawl.engine.instance.CaseInstance;
import org.yawlfoundation.yawl.engine.instance.WorkItemInstance;
import org.yawlfoundation.yawl.monitor.sort.CaseOrder;
import org.yawlfoundation.yawl.monitor.sort.ItemOrder;
import org.yawlfoundation.yawl.monitor.sort.TableSorter;
import org.yawlfoundation.yawl.resourcing.jsf.MessagePanel;

import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.application.NavigationHandler;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

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
        getApplicationBean().removeLiveUser(userid);

        FacesContext context = FacesContext.getCurrentInstance();
        if (context != null) {             // if null, session already destroyed
            HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
            session.invalidate();
        }
    }


    // if seesionhandle is invalid, logs out of session //
//    public void checkLogon() {
// //       if (! _rm.isValidUserSession(sessionhandle)) {
//            doLogout();
//            gotoPage("msLogin");
////        }
//    }


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

    private String title ;

    public String getTitle() {
        title = "YAWL 2.0 Worklist :: ";
        if (activePage == ApplicationBean.PageRef.casesPage)
             title += "Active Cases" ;
        return title ;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    private MessagePanel messagePanel = new MessagePanel() ;

    public MessagePanel getMessagePanel() {
        return messagePanel;
    }

    public void setMessagePanel(MessagePanel messagePanel) {
        this.messagePanel = messagePanel;
    }

    private TableSorter _sorter = new TableSorter();


    /*** ACTIVE CASES ***/

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
        if (getCurrentItemOrder().getColumn() == TableSorter.ItemColumn.Undefined) {
            sortCaseItems(TableSorter.ItemColumn.ItemID);
        }
        else {
            refreshCaseItems(caseInstance.getCaseID(), false);
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


}