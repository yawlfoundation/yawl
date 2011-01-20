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

import com.sun.rave.web.ui.appbase.AbstractSessionBean;
import com.sun.rave.web.ui.component.Button;
import com.sun.rave.web.ui.component.Listbox;
import com.sun.rave.web.ui.component.PanelLayout;
import com.sun.rave.web.ui.component.Script;
import com.sun.rave.web.ui.model.Option;
import org.yawlfoundation.yawl.authentication.YExternalClient;
import org.yawlfoundation.yawl.elements.YAWLServiceReference;
import org.yawlfoundation.yawl.elements.YSpecVersion;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.SpecificationData;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.resourcing.QueueSet;
import org.yawlfoundation.yawl.resourcing.ResourceManager;
import org.yawlfoundation.yawl.resourcing.ResourceMap;
import org.yawlfoundation.yawl.resourcing.WorkQueue;
import org.yawlfoundation.yawl.resourcing.jsf.comparator.*;
import org.yawlfoundation.yawl.resourcing.jsf.dynform.DynFormFactory;
import org.yawlfoundation.yawl.resourcing.jsf.dynform.FormParameter;
import org.yawlfoundation.yawl.resourcing.resource.*;
import org.yawlfoundation.yawl.resourcing.resource.nonhuman.NonHumanResource;
import org.yawlfoundation.yawl.util.JDOMUtil;

import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.application.NavigationHandler;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.text.DateFormat;
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
                if (participant != null) {
                    app.removeSessionReference(participant.getID()) ;
                    app.removeLiveUser(userid);
                }

            }
        }
        catch (Exception e) {
            // write to log that session has already expired
        }
    }

    /*****************************************************************************/

    private ResourceManager _rm = getApplicationBean().getResourceManager();

    /*****************************************************************************/

    // COMPONENTS SHARED BY SEVERAL PAGES //

    private Script script = new Script();                     // shared jscript ref

    public Script getScript() { return script; }

    public void setScript(Script s) { script = s; }


    private Button btnRefresh = new Button();

    public Button getBtnRefresh() { return btnRefresh; }

    public void setBtnRefresh(Button btn) { btnRefresh = btn; }


    /******************************************************************************/

    // MEMBERS, GETTERS & SETTERS //

    private String userid;                          // current userid of this session

    public String getUserid() { return userid; }

    public void setUserid(String id) { userid = id; }


    private String sessionhandle;                   // engine allocated handle

    public String getSessionhandle() { return sessionhandle; }

    public void setSessionhandle(String handle) { sessionhandle = handle; }


    private Participant participant ;              // logged on participant

    public Participant getParticipant() { return participant ; }

    public void setParticipant(Participant p) {
        participant = p ;

        // set some other members relevant to this participant
        queueSet = p.getWorkQueues() ;
        userFullName = p.getFullName() ;
        getApplicationBean().addSessionReference(p.getID(), this) ;
        if (p.isAdministrator())
            adminQueueSet = _rm.getAdminQueues();
    }


    private String userFullName ;                  // full name of current user

    public void setUserName(String userName) { userFullName = userName ; }

    public String getUserName() { return userFullName ; }


    private QueueSet adminQueueSet = null;         // admin work queues

    public QueueSet getAdminQueueSet() { return adminQueueSet; }

    public void setAdminQueueSet(QueueSet qSet) { adminQueueSet = qSet; }


    private QueueSet queueSet ;                     // the user's work queues

    public QueueSet getQueueSet() { return queueSet; }

    public void setQueueSet(QueueSet qSet) { queueSet = qSet; }


    /** @return the set of wir's for the queue passed */
    public Set<WorkItemRecord> getQueue(int qType) {
        Set<WorkItemRecord> result = null ;
        QueueSet qSet = (qType < WorkQueue.UNOFFERED) ? queueSet : adminQueueSet ;
        if (qSet != null) result = qSet.getQueuedWorkItems(qType) ;

        return result ;
    }


    /** @return the number of workitems in the queue passed */
    public int getQueueSize(int qType) {
        QueueSet qSet = (qType < WorkQueue.UNOFFERED) ? queueSet : adminQueueSet ;
        return ( (qSet != null) ? qSet.getQueueSize(qType) : 0 ) ;
    }


    /** Updates the queue data members (ie participant or admin queues) */
    public Set<WorkItemRecord> refreshQueue(int qType) {
        if (qType < WorkQueue.UNOFFERED) {
            if (participant != null)
                queueSet = participant.getWorkQueues() ;
        }
        else 
           adminQueueSet = _rm.getAdminQueues();

        return getQueue(qType) ;
    }

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

    private ApplicationBean.DynFormType dynFormType ;


    public ApplicationBean.PageRef getActivePage() { return activePage; }

    public void setActivePage(ApplicationBean.PageRef page) {
        activePage = page;
        if (page != ApplicationBean.PageRef.participantData) {
            setEditedParticipant((Participant) null);
        }
        if (page != ApplicationBean.PageRef.externalClients) {
            setAddClientAccountMode(true);
        }
    }


    public ApplicationBean.DynFormType getDynFormType() { return dynFormType; }

    public void setDynFormType(ApplicationBean.DynFormType type) {
        dynFormType = type;
    }

    public DynFormFactory getDynFormFactoryInstance() {
        return (DynFormFactory) getBean("DynFormFactory");
    }

    
    /*******************************************************************************/

    // LISTBOX MEMBERS, GETTERS & SETTERS

    // option sets for the various listboxes                      //  PAGES  //
    private Option[] worklistOptions;                             // pfQueueUI
    private Option[] loadedSpecListOptions;                       // case mgt
    private Option[] runningCaseListOptions;                      // case mgt
    private Option[] selectUserListOptions;                       // user select
    private Option[] ownedResourceAttributes;                     // user mgt
    private Option[] availableResourceAttributes;                 // user mgt
    private Option[] piledTasks;                                  // user profile
    private Option[] chainedCases;                                // user profile
    private Option[] orgDataOptions;                              // org data mgt
    private Option[] orgDataBelongsItems;                         // org data mgt
    private Option[] orgDataGroupItems;                           // org data mgt
    private Option[] nhResourcesOptions;                          // nonhuman resource mgt
    private Option[] nhResourcesCategoryItems;                    // nonhuman resource mgt
    private Option[] nhResourcesSubcategoryItems;                 // nonhuman resource mgt


    public Option[] getWorklistOptions() {
        return worklistOptions;
    }

    public Option[] getLoadedSpecListOptions() {
        return loadedSpecListOptions;
    }

    public Option[] getRunningCaseListOptions() {
        return runningCaseListOptions;
    }

    public Option[] getSelectUserListOptions() {
        return selectUserListOptions;
    }

    public Option[] getOwnedResourceAttributes() {
        return ownedResourceAttributes;
    }

    public Option[] getAvailableResourceAttributes() {
        return availableResourceAttributes;
    }

    public Option[] getPiledTasks() {
        piledTasks = getParticipantPiledTasks();
        return piledTasks;
    }

    public Option[] getChainedCases() {
        chainedCases = getParticipantChainedCases();
        return chainedCases;
    }

    public Option[] getOrgDataOptions() {
        return orgDataOptions;
    }

    public Option[] getOrgDataBelongsItems() {
        return orgDataBelongsItems;
    }

    public Option[] getOrgDataGroupItems() {
        return orgDataGroupItems;
    }

    public Option[] getNhResourcesOptions() {
        return nhResourcesOptions;
    }

    public Option[] getNhResourcesCategoryItems() {
        return nhResourcesCategoryItems;
    }

    public Option[] getNhResourcesSubcategoryItems() {
        return nhResourcesSubcategoryItems;
    }

    public void setWorklistOptions(Option[] options) {
        worklistOptions = options;
    }

    public void setLoadedSpecListOptions(Option[] options) {
        loadedSpecListOptions = options;
    }

    public void setRunningCaseListOptions(Option[] options) {
        runningCaseListOptions = options;
    }

    public void setSelectUserListOptions(Option[] options) {
        selectUserListOptions = options;
    }

    public void setOwnedResourceAttributes(Option[] attributes) {
        ownedResourceAttributes = attributes;
    }

    public void setAvailableResourceAttributes(Option[] attributes) {
        availableResourceAttributes = attributes;
    }

    public void setPiledTasks(Option[] options) {
         piledTasks = options;
    }

     public void setChainedCases(Option[] options) {
         chainedCases = options;
    }

    public void setOrgDataOptions(Option[] orgDataOptions) {
        this.orgDataOptions = orgDataOptions;
    }

    public void setOrgDataBelongsItems(Option[] orgDataBelongsItems) {
        this.orgDataBelongsItems = orgDataBelongsItems;
    }

    public void setOrgDataGroupItems(Option[] orgDataGroupItems) {
        this.orgDataGroupItems = orgDataGroupItems;
    }

    public void setNhResourcesOptions(Option[] nhResourcesOptions) {
        this.nhResourcesOptions = nhResourcesOptions;
    }

    public void setNhResourcesCategoryItems(Option[] nhResourcesCategoryItems) {
        this.nhResourcesCategoryItems = nhResourcesCategoryItems;
    }

    public void setNhResourcesSubcategoryItems(Option[] nhResourcesSubcategoryItems) {
        this.nhResourcesSubcategoryItems = nhResourcesSubcategoryItems;
    }


  // user selection from each listbox

    private String worklistChoice;
    private YSpecificationID loadedSpecListChoice;
    private String runningCaseListChoice;
    private String selectUserListChoice;
    private String piledTasksChoice;
    private String chainedCasesChoice;
    private String orgDataChoice;
    private String orgDataBelongsChoice;
    private String orgDataGroupChoice;
    private String nhResourcesChoice;
    private String nhResourcesCategoryChoice;
    private String nhResourcesSubcategoryChoice;


    public String getWorklistChoice() { return worklistChoice; }
    public YSpecificationID getLoadedSpecListChoice() { return loadedSpecListChoice; }
    public String getRunningCaseListChoice() { return runningCaseListChoice; }
    public String getSelectUserListChoice() { return selectUserListChoice; }
    public String getOrgDataChoice() { return orgDataChoice; }
    public String getPiledTasksChoice() { return piledTasksChoice; }
    public String getChainedCasesChoice() { return chainedCasesChoice; }
    public String getOrgDataBelongsChoice() { return orgDataBelongsChoice; }
    public String getOrgDataGroupChoice() { return orgDataGroupChoice; }
    public String getNhResourcesChoice() { return nhResourcesChoice; }
    public String getNhResourcesCategoryChoice() { return nhResourcesCategoryChoice; }
    public String getNhResourcesSubcategoryChoice() { return nhResourcesSubcategoryChoice; }

    public void setWorklistChoice(String choice) { worklistChoice = choice ; }
    public void setRunningCaseListChoice(String choice) { runningCaseListChoice = choice ; }
    public void setSelectUserListChoice(String choice) { selectUserListChoice = choice; }
    public void setOrgDataChoice(String choice) { orgDataChoice = choice ;}
    public void setPiledTasksChoice(String choice) { piledTasksChoice = choice; }
    public void setChainedCasesChoice(String choice) { chainedCasesChoice = choice; }
    public void setOrgDataBelongsChoice(String choice) { orgDataBelongsChoice = choice; }
    public void setOrgDataGroupChoice(String choice) { orgDataGroupChoice = choice; }
    public void setNhResourcesChoice(String choice) { nhResourcesChoice = choice; }
    public void setNhResourcesCategoryChoice(String choice) { nhResourcesCategoryChoice = choice; }
    public void setNhResourcesSubcategoryChoice(String choice) { nhResourcesSubcategoryChoice = choice; }

    public void setLoadedSpecListChoice(SpecificationData choice) {        
        loadedSpecListChoice = choice.getID() ;
    }

    /********************************************************************************/

    private String directToMeChoice;

    public void setDirectToMeChoice(String choice) { directToMeChoice = choice; }

    public String getDirectToMeChoice() { return directToMeChoice; }


    private Listbox lbxUserList = new Listbox();

    public Listbox getLbxUserList() { return lbxUserList; }

    public void setLbxUserList(Listbox l) { lbxUserList = l; }


    public Object getSelectUserListBoxSelections() {
        return getLbxUserList().getSelected();
    }


    public void configureSelectUserListBox(String action) {
        if (action.equals("Offer") || action.equals("Reoffer")) {
            lbxUserList.setMultiple(true);
            lbxUserList.setSelected(selectUserListChoices);
        }
        else {
            lbxUserList.setMultiple(false);
            lbxUserList.setSelected(selectUserListChoice);
        }
  //      lbxUserList.setSelected(selectUserListChoices);
    }


    private Listbox lbxRunningCases = new Listbox();

    public Listbox getLbxRunningCases() { return lbxRunningCases; }

    public void setLbxRunningCases(Listbox l) { lbxRunningCases = l; }


    public Object getRunningCasesListBoxSelections() {
        return lbxRunningCases.getSelected();
    }


    public void configureRunningCasesListBox() {
        lbxRunningCases.setMultiple(true);
        lbxRunningCases.setSelected(runningCaseListChoices);
    }



    private Object[] runningCaseListChoices;

    public Object[] getRunningCaseListChoices() {
        return runningCaseListChoices;
    }

    public void setRunningCaseListChoices(Object[] list) {
        runningCaseListChoices = list;
    }

    /******************************************************************/

    private ArrayList selectUserListChoices;

    public ArrayList getSelectUserListChoices() {
        return selectUserListChoices;
    }

    public void setSelectUserListChoices(ArrayList list) {
        selectUserListChoices = list;
    }



    /********************************************************************************/

    // WORKITEM SELECTED FROM LIST //

    // the wir matching the item id selected by the user
    private WorkItemRecord chosenWIR = null;

    public void setChosenWIR(WorkItemRecord wir) {
        chosenWIR = wir ;
        worklistChoice = (wir != null) ? wir.getID() : null;
    }

    /** @return the WorkItemRecord for the id selected in the list */
    public WorkItemRecord getChosenWIR(int qType) {
        if (worklistChoice != null) {
            Set<WorkItemRecord> items = getQueue(qType);
            if (items != null) {
                for (WorkItemRecord wir : items) {
                    if (wir != null) {
                        if (wir.getID().equals(worklistChoice)) {
                            chosenWIR = wir ;
                            return wir;
                        }
                    }
                }
            }
        }
        return null ;
    }

    /** @return true if the chosen item in the list is also the first listed item */
    public boolean isFirstWorkItemChosen() {
        if ((worklistOptions != null) && (worklistOptions.length > 0)) {
            String first = (String) getWorklistOptions()[0].getValue();
     //       return (first.equals(worklistChoice));
            return (first.equals(chosenWIR.getID()));
        }
        else return false;
    }


    /**********************************************************************************/

    // PAGE NAVIGATION METHODS //

    // logs out of session //
    public void doLogout() {
        _rm.logout(sessionhandle) ;
        setEditedParticipant((Participant) null);
        getApplicationBean().removeLiveUser(userid);
        setUserid(null);
        
        FacesContext context = FacesContext.getCurrentInstance();
        if (context != null) {             // if null, session already destroyed
            HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
            session.invalidate();
        }
    }


    // if seesionhandle is invalid, logs out of session //
    public void checkLogon() {
        if (! _rm.isValidUserSession(sessionhandle)) {
            doLogout();
            gotoPage("Login");
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
                FacesContext.getCurrentInstance().getExternalContext().redirect("Login.jsp");
            }
            catch (IOException ioe) {
                // message about destroyed app
            }
        }
    }


    /********************************************************************************/

    // FLAGS FOR POSTBACK ACTIONS //

    private boolean caseLaunch = false ;

    public boolean isCaseLaunch() { return caseLaunch; }

    public void setCaseLaunch(boolean caseLaunch) { this.caseLaunch = caseLaunch; }


    private boolean delegating = false ;

    public boolean isDelegating() { return delegating; }

    public void setDelegating(boolean delegating) { this.delegating = delegating; }


    private boolean reallocating = false ;

    public boolean isReallocating() { return reallocating; }

    public void setReallocating(boolean reallocating) { this.reallocating = reallocating; }


    private boolean reallocatingStateful ;

    public boolean isReallocatingStateful() {
        return reallocatingStateful;
    }

    public void setReallocatingStateful(boolean reallocatingStateful) {
        this.reallocatingStateful = reallocatingStateful;
    }


    private boolean customFormPost = false ;

    public boolean isCustomFormPost() { return customFormPost ; }

    public void setCustomFormPost(boolean flag) { customFormPost = flag ; }


    /********************************************************************************/
    
    private String title ;

    public String getTitle() {
        title = "YAWL 2.1 Worklist";
        if ((activePage == ApplicationBean.PageRef.userWorkQueues) && (participant != null))
             title += ": " + participant.getFullName() ;
        return title ;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    private String mnuSelectorStyle = "top: 72px";            // on workqueues initially

    public String getMnuSelectorStyle() {
        return mnuSelectorStyle;
    }

    public void setMnuSelectorStyle(String mnuSelectorStyle) {
        this.mnuSelectorStyle = mnuSelectorStyle;
    }

    private String userListFormHeaderText ;

    public String getUserListFormHeaderText() {
        return userListFormHeaderText;
    }

    public void setUserListFormHeaderText(String userListFormHeaderText) {
        this.userListFormHeaderText = userListFormHeaderText;
    }

    private Option[] getParticipantPiledTasks() {
        Option[] result = null ;
        Set<ResourceMap> taskMaps = _rm.getPiledTaskMaps(participant) ;
        if (! taskMaps.isEmpty()) {
            result = new Option[taskMaps.size()];
            int i = 0;
            for (ResourceMap map : taskMaps)
                result[i++] = new Option(map, formatPiledTaskString(map));
        }
        return result;
    }

    private String formatPiledTaskString(ResourceMap map) {
        YSpecificationID specID = map.getSpecID();
        return String.format("%s (%s)::%s", specID.getUri(),
                      specID.getVersionAsString(), map.getTaskID()) ;
    }

    private Option[] getParticipantChainedCases() {
        Option[] result = null ;
        Set<String> cases = _rm.getChainedCases(participant) ;
        if (! cases.isEmpty()) {
            result = new Option[cases.size()];
            int i = 0;
            for (String id : cases)
                result[i++] = new Option(id);
        }
        return result;
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

            // sort the items
            List<YAWLServiceReference> servList = new ArrayList<YAWLServiceReference>();
            for (YAWLServiceReference service : services) {
                if (service.isAssignable())
                    servList.add(service) ;
            }
            registeredServices = servList;
            Collections.sort(registeredServices, new YAWLServiceComparator());
        }
        else
            registeredServices = null ;
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
            externalClients = new ArrayList<YExternalClient>(_rm.getExternalClients());
        }
        catch (IOException ioe) {
            externalClients = null; 
        }
        if (externalClients != null) {
            Collections.sort(externalClients, new YExternalClientComparator());
        }
    }


    /****** This section used by the 'Case Mgt' Page ***************************/

    // a list of all specs loaded into the engine
    List<SpecificationData> loadedSpecs ;

    // a list of schema libraries (user defined types) for each spec
    Hashtable<String, String> schemaLibraries;

    public List<SpecificationData> getLoadedSpecs() {
        if (loadedSpecs == null) refreshLoadedSpecs() ;

        return loadedSpecs;
    }


    public void setLoadedSpecs(List<SpecificationData> specs) {
        loadedSpecs = specs ;
    }
    

    public void refreshLoadedSpecs() {
        Set<SpecificationData> specDataSet = _rm.getLoadedSpecs() ;

        if (specDataSet != null) {
            loadedSpecs = new ArrayList<SpecificationData>(specDataSet);
            Collections.sort(loadedSpecs, new SpecificationDataComparator());
        //    refreshSchemaLibraries() ;
        }
        else
            loadedSpecs = null ;
    }


    public SpecificationData getLoadedSpec(int listIndex) {
        if (! loadedSpecs.isEmpty())
            return loadedSpecs.get(listIndex);
        else
            return null;
    }


    public YSpecVersion getLatestLoadedSpecVersion(SpecificationData spec) {
        YSpecVersion result = new YSpecVersion("0.1");
        for (SpecificationData sd : loadedSpecs) {
            if (sd.getID().equals(spec.getID())) {
                 YSpecVersion thisVersion = new YSpecVersion(sd.getSpecVersion());
                 if (result.compareTo(thisVersion) < 0) {
                     result = thisVersion;
                 }
            }
        }
        return result;    
    }


    public boolean isLoadedSpec(String uri, String version, String documentation) {
        for (SpecificationData sd : loadedSpecs) {
            if (sd.getSpecURI().equals(uri) &&
                sd.getSpecVersion().equals(version) &&
                sd.getDocumentation().equals(documentation)) {

                return true;
            }
        }
        return false;
    }

    public String getCaseSchema() {
        return _rm.getDataSchema(getLoadedSpecListChoice()) ;
    }

    public Map<String, FormParameter> getCaseParams() {
        return _rm.getCaseInputParams(getLoadedSpecListChoice());
    }

    public String getInstanceData(String schema) {
        return _rm.getInstanceData(schema, getLoadedSpecListChoice()) ;
    }


    public String getTaskSchema(WorkItemRecord wir) {
        YSpecificationID specID = new YSpecificationID(wir);
        return _rm.getDataSchema(wir, specID) ;
    }

    public String getInstanceData(String schema, WorkItemRecord wir) {
        if (wir.getUpdatedData() != null)
            return JDOMUtil.elementToStringDump(wir.getUpdatedData());
        else
            return _rm.getInstanceData(schema, wir) ;
    }


    

    private void refreshSchemaLibraries() {
//        schemaLibraries = new Hashtable<String, String>();
//        for (SpecificationData spec : loadedSpecs) {
//            try {
//                String schema = _rm.getSchemaLibrary(spec.getID(),
//                                                                      sessionhandle) ;
//                if (schema != null)
//                    schemaLibraries.put(spec.getID(), schema) ;
//            }
//            catch (Exception e) {
//                // some kind of io or jdom exception
//            }
//        }
    }

//    public String getSchemaLibrary(String specID) {
//        String result = schemaLibraries.get(specID) ;
//
//        // not in local cache, try getting it from engine
//        if (result == null) {
//            result = _rm.getDataSchema(specID) ;
//
//            // not in engine = problem or not loaded
//            if (result != null) schemaLibraries.put(specID, result) ;
//        }
//
//        return result ;
//    }

    /****** This section used by the 'Admin Queues' Page ***************************/

    private Option[] adminQueueAssignedList ;


    public Option[] getAdminQueueAssignedList() {
        return adminQueueAssignedList;
    }

    public void setAdminQueueAssignedList(Option[] list) {
        adminQueueAssignedList = list ;
    }

    public void populateAdminQueueAssignedList(WorkItemRecord wir) {
        adminQueueAssignedList = null ;
        Set<Participant> pSet = _rm.getParticipantsAssignedWorkItem(wir) ;
        if (pSet != null) {
            adminQueueAssignedList = new Option[pSet.size()];
            ArrayList<Participant> pList = new ArrayList<Participant>(pSet);
            Collections.sort(pList, new ParticipantNameComparator());
            int i = 0 ;

            for (Participant p : pList) {
                adminQueueAssignedList[i++] = new Option(p.getID(),
                                            p.getLastName() + ", " + p.getFirstName()) ;
            }
        }
    }

    public String getFirstAssignedToID() {
        if ((adminQueueAssignedList != null) && (adminQueueAssignedList.length > 0)) {
            Option option = adminQueueAssignedList[0];
            return option.getLabel();
        }
        else return null;
    }

    public String getAssignedToText() {
        if ((adminQueueAssignedList == null) || (adminQueueAssignedList.length == 0))
            return "Assigned To";
        else
            return "Assigned To (" + adminQueueAssignedList.length + ")" ;
    }

    private String adminQueueAction ;

    public String getAdminQueueAction() {
        return adminQueueAction;
    }

    public void setAdminQueueAction(String adminQueueAction) {
        this.adminQueueAction = adminQueueAction;
    }

    public boolean performAdminQueueAction() {
        boolean result = true;
        if (getAdminQueueAction() != null) {
            result = performAdminQueueAction(getAdminQueueAction());
            setAdminQueueAction(null);
        }
        return result;
    }

    public boolean performAdminQueueAction(String action) {
        boolean success = true;
        WorkItemRecord wir;
        String[] pidList = null;

        if (directToMeChoice != null) {
            pidList = new String[] {directToMeChoice};
        }
        else {
            Object selected = getLbxUserList().getSelected();
            if (selected != null) {                       // null if browser-back-btn'ed

                // If its a multi-select list, selected will be a String[]; if its a
                // single-select list, selected will be a single String, in which case
                // we need to create a new String array and add the selected String as
                // its only member. Each String represents a participant id.
                pidList = (selected instanceof String[]) ? (String[]) selected :
                              new String[] {(String) selected};
            }
        }
        if (pidList != null) {
            if (action.startsWith("Re")) {
                wir = getChosenWIR(WorkQueue.WORKLISTED);
                _rm.reassignWorklistedItem(wir, pidList, action) ;
            }
            else  {
                wir = getChosenWIR(WorkQueue.UNOFFERED);
                success = _rm.assignUnofferedItem(wir, pidList, action) ;
            }
        }
        return success;
    }


    String navigateTo ;

    public String getNavigateTo() {
        return navigateTo;
    }

    public void setNavigateTo(String navigateTo) {
        this.navigateTo = navigateTo;
    }


    boolean redirectToMe = false;

    public boolean isRedirectToMe() {
        return redirectToMe;
    }

    public void setRedirectToMe(boolean redirectToMe) {
        this.redirectToMe = redirectToMe;
    }

    /****** This section used by the 'Org Data Mgt' Page ***************************/

    public enum Mode {add, edit}
    private Mode _orgMgtMode = Mode.edit;

    public Mode getOrgMgtMode() { return _orgMgtMode; }
    public void setOrgMgtMode(Mode mode) { _orgMgtMode = mode; }

    private Mode _nhrMgtMode = Mode.edit;

    public Mode getNhrMgtMode() { return _nhrMgtMode; }
    public void setNhrMgtMode(Mode mode) { _nhrMgtMode = mode; }


    private Option[] orgDataParticipantList ;
    private HashMap<String, Participant> participantMap ;


    public Option[] getOrgDataParticipantList() {
        if (orgDataParticipantList == null) refreshOrgDataParticipantList() ;
        return orgDataParticipantList;
    }

    private HashMap<String, Participant> getParticipantMap() {
        participantMap = _rm.getOrgDataSet().getParticipantMap();
        return participantMap;
    }

    private boolean blankStartOfList ;

    // if true, the first entry in the participant list will be empty
    public void setBlankStartOfParticipantList(boolean b) {
        if (b != blankStartOfList) {                           // if there's a change
            blankStartOfList = b ;
            refreshOrgDataParticipantList();
        }
    }


    public void setOrgDataParticipantList(Option[] list) {
        orgDataParticipantList = list ;
    }



    public void refreshOrgDataParticipantList() {
        HashMap<String, Participant> pMap = getParticipantMap();

        if (pMap != null) {
            int i = 0 ;
            if (blankStartOfList) {
                orgDataParticipantList = new Option[pMap.size() + 1];
                i = 1 ;

                // make the first option blank (for initial screen & add users)
                orgDataParticipantList[0] = new Option("", "");
            }
            else orgDataParticipantList = new Option[pMap.size()];
            
            ArrayList<Participant> pList = new ArrayList<Participant>(pMap.values());
            Collections.sort(pList, new ParticipantNameComparator());
            for (Participant p : pList) {
                orgDataParticipantList[i++] = new Option(p.getID(),
                                            p.getLastName() + ", " + p.getFirstName()) ;
            }
        }
        else
            orgDataParticipantList = null ;
    }

    
    private boolean addParticipantMode = false ;

    public boolean isAddParticipantMode() {
        return addParticipantMode;
    }

    public void setAddParticipantMode(boolean addParticipantMode) {
        this.addParticipantMode = addParticipantMode;
    }

    private Participant addedParticipant;

    public Participant getAddedParticipant() {
        return addedParticipant;
    }

    public void setAddedParticipant(Participant p) {

        // if done with temp participant (add mode is over) cleanup references
        if ((p == null) && (addedParticipant != null))
            addedParticipant.removeAttributeReferences();

        addedParticipant = p;
    }

    // stores the currently selected participant on the 'User Mgt' form
    private Participant editedParticipant ;

    public Participant getEditedParticipant() { return editedParticipant; }

    public void setEditedParticipant(Participant p) {
        if (editedParticipant != null) editedParticipant.removeAttributeReferences();
        editedParticipant = p;
    }

    public Participant setEditedParticipant(String pid) {
        if (editedParticipant != null) editedParticipant.removeAttributeReferences();
        editedParticipant = getParticipantMap().get(pid).clone();
        return editedParticipant;
    }

    public void saveParticipantUpdates(Participant temp) {
        Participant p = getParticipantMap().get(temp.getID());
        p.merge(temp);
        p.save();
        setEditedParticipant(p.getID()) ;             // reset
    }

    
    public Option[] getFullResourceAttributeListPlusNil(String tab) {
        Option[] result = null;
        Option[] list = getFullResourceAttributeList(tab);
        if (list != null) {
            result = new Option[list.length + 1];
            result[0] = new Option("nil", "nil");
            for (int i = 1; i < result.length; i++)
                result[i] = list[i-1];
        }
        return result;
    }

    public Option[] getFullResourceAttributeList(String tab) {
        Option[] options = null;
        if (tab.equals("tabRoles")) {
            options = getRoleList(_rm.getOrgDataSet().getRoleMap());
        }
        else if (tab.equals("tabPosition")) {
            options = getPositionList(_rm.getOrgDataSet().getPositionMap());

        }
        else if (tab.equals("tabCapability"))  {
            options = getCapabilityList(_rm.getOrgDataSet().getCapabilityMap());
        }
        else if (tab.equals("tabOrgGroup")) {
            options = getOrgGroupList(_rm.getOrgDataSet().getOrgGroupMap());
        }
        sortOptions(options);
        availableResourceAttributes = options;
        return options ;
    }

    private void sortOptions(Option[] options) {
        if (options != null) Arrays.sort(options, new OptionComparator());
    }


    public Option[] getFullNhResourcesList(String tab) {
        Option[] options = null;
        if (tab.equals("tabResources")) {
            options = getNhResourcesList(_rm.getOrgDataSet().getNonHumanResourceMap());
        }
        else if (tab.equals("tabCategories")) {
//            options = getNhResourcesCategoryList(_rm.getOrgDataSet().getNonHumanResourceCategoryMap());
        }                            // todo
//        else if (tab.equals("tabSubcategories"))  {
//            options = getCapabilityList(_rm.getOrgDataSet().getCapabilityMap());
//        }
        sortOptions(options);
        return options ;
    }

    public Option[] getParticipantAttributeList(String tab, Participant p) {
        Option[] options = null;
        if (tab.equals("tabRoles")) {
            Set<Role> roleSet = p.getRoles() ;
            HashMap<String, Role> roleMap = new HashMap<String, Role>();
            for (Role r : roleSet) roleMap.put(r.getID(), r) ;
            options = getRoleList(roleMap);
        }
        else if (tab.equals("tabPosition")) {
            Set<Position> posSet = p.getPositions() ;
            HashMap<String, Position> posMap = new HashMap<String, Position>();
            for (Position pos : posSet) posMap.put(pos.getID(), pos) ;
            options = getPositionList(posMap);
        }
        else if (tab.equals("tabCapability"))  {
            Set<Capability> capSet = p.getCapabilities() ;
            HashMap<String, Capability> capMap = new HashMap<String, Capability>();
            for (Capability c : capSet) capMap.put(c.getID(), c) ;
            options = getCapabilityList(capMap);
        }
        ownedResourceAttributes = options;
        return options ;

    }


    private Option[] getRoleList(HashMap<String, Role> roleMap) {
        if (roleMap != null) {
            Option[] result = new Option[roleMap.size()];
            int i = 0 ;
            for (String id : roleMap.keySet()) {
                Role r = roleMap.get(id);
                result[i++] = new Option(id, r.getName()) ;
            }
            return result ;
        }
        else return null ;

    }

    private Option[] getPositionList(HashMap<String, Position> positionMap) {
        if (positionMap != null) {
            Option[] result = new Option[positionMap.size()];
            int i = 0 ;
            for (String id : positionMap.keySet()) {
                Position p = positionMap.get(id);
                result[i++] = new Option(id, p.getTitle()) ;
            }
            return result ;
        }
        else return null ;
    }

    private Option[] getCapabilityList(HashMap<String, Capability> capabilityMap) {
        if (capabilityMap != null) {
            Option[] result = new Option[capabilityMap.size()];
            int i = 0 ;
            for (String id : capabilityMap.keySet()) {
                Capability c = capabilityMap.get(id);
                result[i++] = new Option(id, c.getCapability()) ;
            }
            return result ;
        }
        else return null ;
    }

    private Option[] getOrgGroupList(HashMap<String, OrgGroup> orgGroupMap) {
        if (orgGroupMap != null) {
            Option[] result = new Option[orgGroupMap.size()];
            int i = 0 ;
            for (String id : orgGroupMap.keySet()) {
                OrgGroup o = orgGroupMap.get(id);
                result[i++] = new Option(id, o.getGroupName()) ;
            }
            return result ;
        }
        else return null ;
    }


    private Option[] getNhResourcesList(Map<String, NonHumanResource> resMap) {
        if (resMap != null) {
            Option[] result = new Option[resMap.size()];
            int i = 0 ;
            for (String id : resMap.keySet()) {
                NonHumanResource r = resMap.get(id);
                result[i++] = new Option(id, r.getName()) ;
            }
            return result ;
        }
        else return null ;
    }


    private Option[] getNhResourcesCategoryList(Map<String, String> resMap) {
        if (resMap != null) {
            Option[] result = new Option[resMap.size()];
            int i = 0 ;
            for (String id : resMap.keySet()) {
                result[i++] = new Option(id, resMap.get(id)) ;
            }
            return result ;
        }
        else return null ;
    }


    private boolean orgDataItemRemovedFlag ;

    public boolean isOrgDataItemRemovedFlag() {
        return orgDataItemRemovedFlag;
    }

    public void setOrgDataItemRemovedFlag(boolean flag) {
        orgDataItemRemovedFlag = flag;
    }

    private boolean nhResourcesItemRemovedFlag ;

    public boolean isNhResourcesItemRemovedFlag() {
        return nhResourcesItemRemovedFlag;
    }

    public void setNhResourcesItemRemovedFlag(boolean flag) {
        orgDataItemRemovedFlag = flag;
    }

    private String activeResourceAttributeTab = "tabRoles";           // start value

    public String getActiveResourceAttributeTab() {
        return activeResourceAttributeTab;
    }

    public void setActiveResourceAttributeTab(String activeResourceAttributeTab) {
        this.activeResourceAttributeTab = activeResourceAttributeTab;
    }

    public void unselectResourceAttribute(String id) {
        unselectResourceAttribute(id, getParticipantForCurrentMode());
    }

    public void unselectResourceAttribute(String id, Participant p) {   
        if (activeResourceAttributeTab.equals("tabRoles"))
            p.removeRole(id);
        else if (activeResourceAttributeTab.equals("tabPosition"))
            p.removePosition(id);
        else if (activeResourceAttributeTab.equals("tabCapability"))
            p.removeCapability(id);

        // refresh the 'owned' list
        getParticipantAttributeList(activeResourceAttributeTab, p) ;
    }

    public void selectResourceAttribute(String id) {
        selectResourceAttribute(id, getParticipantForCurrentMode());
    }

    public Participant getParticipantForCurrentMode() {
        return isAddParticipantMode() ? addedParticipant : editedParticipant ;
    }

    private void selectResourceAttribute(String id, Participant p) {
        if (activeResourceAttributeTab.equals("tabRoles"))
            p.addRole(id);
        else if (activeResourceAttributeTab.equals("tabPosition"))
            p.addPosition(id);
        else if (activeResourceAttributeTab.equals("tabCapability"))
            p.addCapability(id);

        // refresh the 'owned' list
        getParticipantAttributeList(activeResourceAttributeTab, p) ;
    }

    // resets all edits by reloading participant from org database
    public Participant resetParticipant() {
        if (editedParticipant != null)
            editedParticipant = setEditedParticipant(editedParticipant.getID());
        return editedParticipant ;
    }

    public String addParticipant(Participant p) {
        String newID = _rm.addParticipant(p);
        refreshOrgDataParticipantList();
        editedParticipant = p.clone() ;
        return newID;
    }

    public void removeParticipant(Participant p) {
        Participant pToRemove = participantMap.get(p.getID());
        setEditedParticipant((Participant) null);
        _rm.removeParticipant(pToRemove);
        refreshOrgDataParticipantList();
    }

    /******************************************************************************/

    // Methods to initialise page values

    public String getInitSpecID() {
        if (chosenWIR != null) return chosenWIR.getSpecIdentifier();
        return "" ;
    }

    public void setInitSpecID(String id) {}

    public String getInitCaseID() {
        if (chosenWIR != null) return chosenWIR.getCaseID();
        return "" ;
    }

    public void setInitCaseID(String id) {}

    public String getInitTaskID() {
        if (chosenWIR != null) return chosenWIR.getTaskID();
        return "" ;
    }

    public void setInitTaskID(String id) {}

    public String getInitStatus() {
        if (chosenWIR != null) return chosenWIR.getStatus();
        return "" ;
    }

    public void setInitStatus(String id) {}

    public String getInitAge() {
        String result = null ;
        if (chosenWIR != null) {
            try {
                long eTime = Long.parseLong(chosenWIR.getEnablementTimeMs());
                long age = System.currentTimeMillis() - eTime ;
                result = getApplicationBean().formatAge(age);
            }
            catch (NumberFormatException nfe) {
                result = "<unavailable>" ;
            }
        }
        return result ;
    }

    public void setInitAge(String id) {}

    public String getInitCreatedDate() {
        if (chosenWIR != null) {
            return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                             .format(chosenWIR.getEnablementTimeMs());
        }
        return "" ;
    }

    public void setInitCreatedDate(String id) {}

    public String getInitTabText(int n) {
        String result = WorkQueue.getQueueName(n) ;
        if (queueSet != null) {
            int qSize = queueSet.getQueueSize(n) ;
            if (qSize > 0) result = result + String.format(" (%d)", qSize);
        }
        return result ;
    }

    public String getInitOfferedTabText() { return getInitTabText(WorkQueue.OFFERED) ; }

    public String getInitAllocatedTabText() { return getInitTabText(WorkQueue.ALLOCATED) ; }

    public String getInitStartedTabText() { return getInitTabText(WorkQueue.STARTED) ; }

    public String getInitSuspendedTabText() { return getInitTabText(WorkQueue.SUSPENDED) ; }

    public String getInitUnOfferedTabText() { return getInitTabText(WorkQueue.UNOFFERED) ; }

    public String getInitWorklistedTabText() { return getInitTabText(WorkQueue.WORKLISTED) ; }


    public void setInitTabText(String s) {}

    public String getInitTabStyle() { return "color: #3277ba" ; }


    private boolean wirEdit ;

    public boolean isWirEdit() {
        return wirEdit;
    }

    public void setWirEdit(boolean wirEdit) {
        this.wirEdit = wirEdit;
    }

    private boolean completeAfterEdit ;

    public boolean isCompleteAfterEdit() {
        return completeAfterEdit;
    }

    public void setCompleteAfterEdit(boolean completeAfterEdit) {
        this.completeAfterEdit = completeAfterEdit;
    }

    private Set<String> warnedWIRSet = new HashSet<String>();

    public void setWarnedForNonEdit(String id) {
        warnedWIRSet.add(id) ;
    }

    public boolean hasWarnedForNonEdit(String id) {
        return warnedWIRSet.contains(id);
    }

    public void removeWarnedForNonEdit(String id) {
        warnedWIRSet.remove(id);
    }

    public String sourceTab ;

    public String getSourceTab() {
        return sourceTab;
    }

    public void setSourceTab(String sourceTab) {
        this.sourceTab = sourceTab;
    }

    private String activeTab;

    public String getActiveTab() {
        return activeTab;
    }

    public void setActiveTab(String activeTab) {
        this.activeTab = activeTab;
    }    

    public void setSourceTabAfterListboxSelection() {
        sourceTab = activeTab;
    }


    public void refreshUserWorkQueues() {
//        if (activePage.equals("userWorkQueues")) {
//            userWorkQueues uwq = ((userWorkQueues) getBean("userWorkQueues"));
//            if (uwq != null) uwq.forceRefresh();
//        }
    }

    /*****************************************************************************/

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
        String style = (activePage != ApplicationBean.PageRef.dynForm) ? null :
                        "border: none;";
        transparentPanel.setStyle(style);
        transparentPanel.setVisible(messagePanel.hasMessage());
        messagePanel.show(getOuterPanelWidth());
    }

    public String messagePanelOKBtnAction(ActionEvent event) {
        showMessagePanel();
        getApplicationBean().refresh();
        return null;
    }


    private int getOuterPanelWidth() {
        switch (activePage) {
            case adminQueues     : return 798;
            case caseMgt         : return 602;
            case customServices  : return 666;
            case Login           : return 240;
            case orgDataMgt      : return 698;
            case participantData : return 670;
            case userWorkQueues  : return 798;
            case viewProfile     : return 538;
            case addInstance     : return 306;
            case teamQueues      : return 796;
            case externalClients : return 666;
            case dynForm         : return getDynFormFactoryInstance().getFormWidth();
            default: return -1;
        }
    }

    private int getOuterPanelHeight() {
        switch (activePage) {
            case adminQueues     : return 328;
            case caseMgt         : return 590;
            case customServices  : return 515;
            case orgDataMgt      : return 328;
            case participantData : return 543;
            case userWorkQueues  : return 328;
            case viewProfile     : return 377;
            case addInstance     : return 370;
            case teamQueues      : return 350;
            case externalClients : return 485;
            default: return -1;
        }
    }

    /*****************************************************************************/


//    public void setConnectionTimeout() {
//        if (! getApplicationBean().isSessionTimeoutSet()) {
//            int interval = this.getExternalSession().getMaxInactiveInterval();
//            _rm.setConnectionTimeoutInterval(interval * 1000);      // secs --> msecs
//            getApplicationBean().setSessionTimeoutSet(true);
//        }
//    }

    /*****************************************************************************/
    

    private String orgDataListLabelText = "List Label";

    public String getOrgDataListLabelText() {
        return orgDataListLabelText;
    }

    public void setOrgDataListLabelText(String orgDataListLabelText) {
        this.orgDataListLabelText = orgDataListLabelText;
    }

    private String orgDataBelongsLabelText = "Belongs To";

    public String getOrgDataBelongsLabelText() {
        return orgDataBelongsLabelText;
    }

    public void setOrgDataBelongsLabelText(String orgDataBelongsLabelText) {
        this.orgDataBelongsLabelText = orgDataBelongsLabelText;
    }

    private String orgDataGroupLabelText = "Org Group";

    public String getOrgDataGroupLabelText() {
        return orgDataGroupLabelText;
    }

    public void setOrgDataGroupLabelText(String orgDataGroupLabelText) {
        this.orgDataGroupLabelText = orgDataGroupLabelText;
    }

    private String nhResourceListLabelText = "List Label";

    public String getNhResourceListLabelText() {
        return nhResourceListLabelText;
    }

    public void setNhResourceListLabelText(String text) {
        orgDataListLabelText = text;
    }


    public void resetPageDefaults(ApplicationBean.PageRef page) {
        switch (page) {
            case participantData :
                setEditedParticipant((Participant) null);
                setActiveResourceAttributeTab(null);
                setAddParticipantMode(false);
                setAddedParticipant(null);                
                break ;
        }
    }


    // Add Instance
    private String addInstanceParamVal ;

    public String getAddInstanceParamVal() {
        return addInstanceParamVal;
    }

    public void setAddInstanceParamVal(String val) {
        addInstanceParamVal = val;
    }

    public boolean isAddInstance() { return addInstanceParamVal != null ; }

    private String addInstanceParamName ;

    public String getAddInstanceParamName() {
        return addInstanceParamName;
    }

    public void setAddInstanceParamName(String name) {
        addInstanceParamName = name;
    }

    public String getAddInstanceParamNameLabelText() {
        return addInstanceParamName + ":";
    }

    public void clearAddInstanceParam() {
        addInstanceParamVal = null ;
        addInstanceParamName = null;
        addInstanceItemID = null;
    }

    private String addInstanceItemID ;

    public String getAddInstanceItemID() {
        return addInstanceItemID;
    }

    public void setAddInstanceItemID(String itemID) {
        addInstanceItemID = itemID;
    }

    private String addInstanceHeader ;

    public String getAddInstanceHeader() {
        return addInstanceHeader;
    }

    public void setAddInstanceHeader(String taskID) {
        addInstanceHeader = "Create a New Workitem Instance of Task '" + taskID + "'";
    }

    //////
    
    private boolean teamRBSelected = true;

    private boolean orgGroupRBSelected = false;

    public boolean getTeamRBSelected() { return teamRBSelected; }

    public boolean isTeamRBSelected() { return teamRBSelected; }

    public void setTeamRBSelected(boolean selected) { teamRBSelected = selected; }

    public boolean getOrgGroupRBSelected() { return orgGroupRBSelected; }

    public boolean isOrgGroupRBSelected() { return orgGroupRBSelected; }

    public void setOrgGroupRBSelected(boolean selected) { orgGroupRBSelected = selected; }

    public boolean getTeamRBDisabled() {
        return (participant == null) ||
               (! (participant.isAdministrator() ||
                   participant.getUserPrivileges().canViewTeamItems()));
    }

    public boolean getOrgGroupRBDisabled() {
        return (participant == null) ||
               (! (participant.isAdministrator() ||
                   participant.getUserPrivileges().canViewOrgGroupItems()));
    }

    private WorkItemRecord selectedTeamQueueWIR ;

    public WorkItemRecord getSelectedTeamQueueWIR() {
        return selectedTeamQueueWIR;
    }

    public void setSelectedTeamQueueWIR(WorkItemRecord wir) {
        selectedTeamQueueWIR = wir;
    }

    private String resourceState  ;

    public String getResourceState() {
        return resourceState;
    }

    public void setResourceState(String state) {
        resourceState = state;
    }


    private PanelLayout pnlUploadBlockout = new PanelLayout();

     public PanelLayout getPnlUploadBlockout() {
         return pnlUploadBlockout;
     }

     public void setPnlUploadBlockout(PanelLayout panel) {
         pnlUploadBlockout = panel;
     }



    private boolean orgDataUploadPanelVisible = false;

    public boolean isOrgDataUploadPanelVisible() {
        return orgDataUploadPanelVisible;
    }

    public void setOrgDataUploadPanelVisible(boolean visible) {
        orgDataUploadPanelVisible = visible;
    }

    public String getFooterPanelStyle() {
        String style = "top: %dpx; height:100%%; width:100%%; position:relative;";
        int top = 80;
        if (messagePanel.isVisible()) {
            int overhang =  (messagePanel.getHeight() + 60) - getOuterPanelHeight();
            if (overhang > 0) {
                top += overhang;
            }
        }
        return String.format(style, top);
    }

    private boolean visualiserReferred = false;

    public boolean isVisualiserReferred() {
        return visualiserReferred;
    }

    public void setVisualiserReferred(boolean referred) {
        visualiserReferred = referred;
    }

    private WorkItemRecord visualiserEditedWIR = null;

    public WorkItemRecord getVisualiserEditedWIR() {
        return visualiserEditedWIR;
    }

    public void setVisualiserEditedWIR(WorkItemRecord wir) {
        visualiserEditedWIR = wir;
    }

    private boolean rssAlreadyLoggedOn = false;

    public boolean isRssAlreadyLoggedOn() {
        return rssAlreadyLoggedOn;
    }

    public void setRssAlreadyLoggedOn(boolean loggedOn) {
        rssAlreadyLoggedOn = loggedOn;
    }

    private FormViewer formViewerInstance = null ;

    public FormViewer getFormViewerInstance() {
        return formViewerInstance;
    }

    public void setFormViewerInstance(FormViewer instance) {
        formViewerInstance = instance;
    }

    private WorkItemRecord rssFormWIR = null;

    public WorkItemRecord getRssFormWIR() {
        return rssFormWIR;
    }

    public void setRssFormWIR(WorkItemRecord wir) {
        rssFormWIR = wir;
    }

    public void resetPostFormDisplay() {
        setFormViewerInstance(null);
        setRssFormWIR(null);
        setRssFormDisplay(false);
        setWirEdit(false);
        setCompleteAfterEdit(false);
        setCustomFormPost(false);
        setShowYAWLBanner(true);
    }

    private boolean rssFormDisplay = false ;

    public boolean isRssFormDisplay() {
        return rssFormDisplay;
    }

    public void setRssFormDisplay(boolean display) {
        rssFormDisplay = display;
    }

    private boolean addClientAccountMode = true ;

    public boolean isAddClientAccountMode() {
        return addClientAccountMode;
    }

    public void setAddClientAccountMode(boolean mode) {
        addClientAccountMode = mode;
    }


    private Option[] orgDataMembers;

    public Option[] getOrgDataMembers() {
        return orgDataMembers;
    }

    public int setOrgDataMembers(AbstractResourceAttribute attribute) {
        int membership = 0;
        if (attribute != null) {
            orgDataMembers = new Option[attribute.getResources().size()];
            int i = 0;
            for (AbstractResource resource : attribute.getResources()) {
                Participant p = (Participant) resource;
                orgDataMembers[i++] = new Option(p.getID(), p.getFullName()) ;
            }
            membership = orgDataMembers.length;
        }
        else {
            orgDataMembers = null;
        }
        return membership;
    }


    private boolean showYAWLBanner = true;

    public boolean isShowYAWLBanner() { return showYAWLBanner; }

    public void setShowYAWLBanner(boolean show) { showYAWLBanner = show; }
    
}


