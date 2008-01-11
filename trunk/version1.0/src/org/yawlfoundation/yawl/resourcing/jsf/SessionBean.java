/*
 * SessionBean1.java
 *
 * Created on October 21, 2007, 6:32 PM
 * Copyright adamsmj
 */
package org.yawlfoundation.yawl.resourcing.jsf;

import com.sun.rave.web.ui.appbase.AbstractSessionBean;
import com.sun.rave.web.ui.component.*;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.resourcing.QueueSet;
import org.yawlfoundation.yawl.resourcing.WorkQueue;

import javax.faces.FacesException;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.text.DateFormat;
import java.io.IOException;

/**
 * <p>Session scope data bean for your application.  Create properties
 *  here to represent cached data that should be made available across
 *  multiple HTTP requests for an individual user.</p>
 *
 * <p>An instance of this class will be created for you automatically,
 * the first time your application evaluates a value binding expression
 * or method binding expression that references a managed bean using
 * this class.</p>
 */
public class SessionBean extends AbstractSessionBean {
    // <editor-fold defaultstate="collapsed" desc="Creator-managed Component Definition">
    private int __placeholder;
    
    /**
     * <p>Automatically managed component initialization.  <strong>WARNING:</strong>
     * This method is automatically generated, so any user-specified code inserted
     * here is subject to being replaced.</p>
     */
    private void _init() throws Exception {
    }
    // </editor-fold>


    /** 
     * <p>Construct a new session data bean instance.</p>
     */
    public SessionBean() {
    }

    /** 
     * <p>Return a reference to the scoped data bean.</p>
     */
    protected ApplicationBean getApplicationBean() {
        return (ApplicationBean)getBean("ApplicationBean");
    }

    /** 
     * <p>This method is called when this bean is initially added to
     * session scope.  Typically, this occurs as a result of evaluating
     * a value binding or method binding expression, which utilizes the
     * managed bean facility to instantiate this bean and store it into
     * session scope.</p>
     * 
     * <p>You may customize this method to initialize and cache data values
     * or resources that are required for the lifetime of a particular
     * user session.</p>
     */
    public void init() {
        // Perform initializations inherited from our superclass
        super.init();
        // Perform application initialization that must complete
        // *before* managed components are initialized
        // TODO - add your own initialiation code here

        // <editor-fold defaultstate="collapsed" desc="Creator-managed Component Initialization">
        // Initialize automatically managed components
        // *Note* - this logic should NOT be modified
        try {
            _init();
        } catch (Exception e) {
            log("SessionBean1 Initialization Failure", e);
            throw e instanceof FacesException ? (FacesException) e: new FacesException(e);
        }
        // </editor-fold>
        // Perform application initialization that must complete
        // *after* managed components are initialized
        // TODO - add your own initialization code here
    }

    /** 
     * <p>This method is called when the session containing it is about to be
     * passivated.  Typically, this occurs in a distributed servlet container
     * when the session is about to be transferred to a different
     * container instance, after which the <code>activate()</code> method
     * will be called to indicate that the transfer is complete.</p>
     * 
     * <p>You may customize this method to release references to session data
     * or resources that can not be serialized with the session itself.</p>
     */
    public void passivate() {
    }

    /** 
     * <p>This method is called when the session containing it was
     * reactivated.</p>
     * 
     * <p>You may customize this method to reacquire references to session
     * data or resources that could not be serialized with the
     * session itself.</p>
     */
    public void activate() {
    }

    /** 
     * <p>This method is called when this bean is removed from
     * session scope.  Typically, this occurs as a result of
     * the session timing out or being terminated by the application.</p>
     * 
     * <p>You may customize this method to clean up resources allocated
     * during the execution of the <code>init()</code> method, or
     * at any later time during the lifetime of the application.</p>
     */
    public void destroy() {
        getApplicationBean().removeSessionReference(participant.getID()) ;
    }

    /**
     * Holds value of property userid.
     */
    private String userid;

    /**
     * Getter for property userid.
     * @return Value of property userid.
     */
    public String getUserid() {

        return this.userid;
    }

    /**
     * Setter for property userid.
     * @param userid New value of property userid.
     */
    public void setUserid(String userid) {

        this.userid = userid;
    }

    /**
     * Holds value of property sessionhandle.
     */
    private String sessionhandle;

    /**
     * Getter for property sessionhandle.
     * @return Value of property sessionhandle.
     */
    public String getSessionhandle() {

        return this.sessionhandle;
    }

    /**
     * Setter for property sessionhandle.
     * @param sessionhandle New value of property sessionhandle.
     */
    public void setSessionhandle(String sessionhandle) {

        this.sessionhandle = sessionhandle;
    }

    public String getExternalSessionID() {
        ExternalContext externalContext = getFacesContext().getExternalContext();
        if (externalContext != null)
           return ((HttpSession) externalContext.getSession(false)).getId();
        else
           return null ;
    }

    private Participant participant ;

    public Participant getParticipant() { return participant ; }

    public void setParticipant(Participant p) {
        participant = p ;
        queueSet = p.getWorkQueues() ;
        userFullName = p.getFullName() ;
        getApplicationBean().addSessionReference(p.getID(), this) ;
    }

    private String userFullName ;

    public void setUserName(String userName) { userFullName = userName ; }

    public String getUserName() { return userFullName ; }


    private QueueSet queueSet ;

    public QueueSet getQueueSet() { return queueSet; }

    public void setQueueSet(QueueSet qSet) { queueSet = qSet; }

    public Set<WorkItemRecord> getQueue(int qType) {
        Set<WorkItemRecord> result = null ;
        if (queueSet != null) {
            result = queueSet.getQueuedWorkItems(qType) ;
        }
        return result ;
    }

    public int getQueueSize(int qType) {
        return queueSet.getQueueSize(qType) ;
    }

    public Set<WorkItemRecord> refreshQueue(int qType) {
        queueSet = participant.getWorkQueues() ;
        return getQueue(qType) ;
    }


    /**
     * Holds value of property worklistOptions.
     */
    private com.sun.rave.web.ui.model.Option[] worklistOptions;
    private com.sun.rave.web.ui.model.Option[] loadedSpecListOptions;
    private com.sun.rave.web.ui.model.Option[] runningCaseListOptions;
    private com.sun.rave.web.ui.model.Option[] selectUserListOptions;

    /**
     * Getter for property worklistOptions.
     * @return Value of property worklistOptions.
     */
    public com.sun.rave.web.ui.model.Option[] getWorklistOptions() {
        return this.worklistOptions;
    }

    public com.sun.rave.web.ui.model.Option[] getLoadedSpecListOptions() {
        return this.loadedSpecListOptions;
    }

    public com.sun.rave.web.ui.model.Option[] getRunningCaseListOptions() {
        return this.runningCaseListOptions;
    }

    public com.sun.rave.web.ui.model.Option[] getSelectUserListOptions() {
        return this.selectUserListOptions;
    }

    /**
     * Setter for property worklistOptions.
     * @param worklistOptions New value of property worklistOptions.
     */
    public void setWorklistOptions(com.sun.rave.web.ui.model.Option[] worklistOptions) {
        this.worklistOptions = worklistOptions;
    }

    public void setLoadedSpecListOptions(com.sun.rave.web.ui.model.Option[] options) {
        loadedSpecListOptions = options;
    }

    public void setRunningCaseListOptions(com.sun.rave.web.ui.model.Option[] options) {
        runningCaseListOptions = options;
    }

    public void setSelectUserListOptions(com.sun.rave.web.ui.model.Option[] options) {
        selectUserListOptions = options;
    }

    /**
     * Holds value of property worklistChoice.
     */
    private String worklistChoice;
    private String loadedSpecListChoice;
    private String runningCaseListChoice;
    private String selectUserListChoice;

    private WorkItemRecord chosenWIR = null;

    /**
     * Getter for property worklistChoice.
     * @return Value of property worklistChoice.
     */
    public String getWorklistChoice() { return worklistChoice; }
    public String getLoadedSpecListChoice() { return loadedSpecListChoice; }
    public String getRunningCaseListChoice() { return runningCaseListChoice; }
    public String getSelectUserListChoice() { return selectUserListChoice; }

    /**
     * Setter for property worklistChoice.
     * @param choice New value of property worklistChoice.
     */
    public void setWorklistChoice(String choice) { worklistChoice = choice ; }
    public void setLoadedSpecListChoice(String choice) { loadedSpecListChoice = choice ; }
    public void setRunningCaseListChoice(String choice) { runningCaseListChoice = choice ; }
    public void setSelectUserListChoice(String choice) { selectUserListChoice = choice; }


    public WorkItemRecord getChosenWIR(int qType) {
        if (participant != null) {
            Set<WorkItemRecord> items = participant.getWorkQueues().getQueuedWorkItems(qType);
            if (items != null) {
                for (WorkItemRecord wir : items) {
                    if (wir.getID().equals(worklistChoice)) {
                        chosenWIR = wir ;
                        break ;
                    }
                }
                return chosenWIR ;
            }
        }    
        return null ;
    }

    public void setChosenWIR(WorkItemRecord wir) { chosenWIR = wir ; }

    private String dynFormHeaderText ;

    public String getDynFormHeaderText() { return dynFormHeaderText ; }

    public void setDynFormHeaderText(String headerText) {
        dynFormHeaderText = headerText ;
    }

    public void doLogout() {
        getApplicationBean().getWorkQueueGateway().logout(sessionhandle) ;
    }

    public void checkLogon() {
        boolean isActiveSession = getApplicationBean().getWorkQueueGateway()
                                                      .isValidSession(sessionhandle) ;
        if (! isActiveSession) {
            try {
                FacesContext.getCurrentInstance().getExternalContext().redirect("Login.jsp");
            }
            catch (IOException ioe) {
                // do something here
            }
        }
    }

    private Map<String, FormParameter> dynFormParams ;

    public Map<String, FormParameter> getDynFormParams() {
        return dynFormParams;
    }

    public void setDynFormParams(Map<String, FormParameter> dynFormParams) {
        this.dynFormParams = dynFormParams;
    }

    private boolean caseLaunch = false ;

    public boolean isCaseLaunch() { return caseLaunch; }

    public void setCaseLaunch(boolean caseLaunch) { this.caseLaunch = caseLaunch; }

    private boolean delegating = false ;

    public boolean isDelegating() { return delegating; }

    public void setDelegating(boolean delegating) { this.delegating = delegating; }

    private String userListFormHeaderText ;

    public String getUserListFormHeaderText() {
        return userListFormHeaderText;
    }

    public void setUserListFormHeaderText(String userListFormHeaderText) {
        this.userListFormHeaderText = userListFormHeaderText;
    }

    /******************************************************************************/

    // Methods to initialise page values

    public String getInitSpecID() {
        if (chosenWIR != null) return chosenWIR.getSpecificationID();
        return "" ;
    }

    public void setInitSpecID(String id) {}

    public String getInitCaseID() {
        if (chosenWIR != null) return chosenWIR.getCaseID();
        return "" ;
    }

    public void setInitCaseID(String id) {}

    public String getInitTaskID() {
        if (chosenWIR != null) return chosenWIR.getCaseID();
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


    public void setInitTabText(String s) {}

    public void initDynForm(List<FormParameter> params, String title) {
        dynForm df = (dynForm) getBean("dynForm") ;
        PanelLayout dynPanel = df.getCompPanel() ;
        df.getHead1().setTitle(title);


        boolean focusSet = false ;
        for (FormParameter param : params) {
            String pName = param.getName();
            Label label = new Label() ;
            label.setId("lbl" + pName);
            label.setText(pName + ": ");
            dynPanel.getChildren().add(label);
            if (param.getDataTypeName().equals("boolean")) {
                Checkbox cbox = new Checkbox();
                cbox.setId("cbx" + pName);
                String val = param.getValue() ;
                if (val == null) val = "false" ;
                cbox.setSelected(val.equalsIgnoreCase("true")) ;
                cbox.setReadOnly(param.isInputOnly());
                dynPanel.getChildren().add(cbox);
                if (! focusSet) {
                    df.getBody1().setFocus("form1:" + cbox.getId());
                    focusSet = true ;
                }
            }
            else {
                if (param.isInputOnly()) {
                    StaticText roText = new StaticText() ;
                    roText.setId("stt" + pName);
                    roText.setText(getApplicationBean().rPad(param.getValue(), 40));
                    roText.setStyle("border: 1px solid black; padding: 3px; " +
                            "background-color: white; color: gray; font-style: italic");
                    dynPanel.getChildren().add(roText);
                }
                else {
                    TextField textField = new TextField() ;
                    textField.setId("txt" + pName);
                    textField.setText(param.getValue());
                    textField.setRequired(param.isMandatory() || param.isRequired());
                    dynPanel.getChildren().add(textField);
                    if (! focusSet) {
                        df.getBody1().setFocus("form1:" + textField.getId());
                        focusSet = true ;
                    }                    
                }
            }
            
            StaticText space = new StaticText();
            space.setId("staticText" + dynPanel.getChildCount());
            space.setText("---------------------------------------------------------");
            space.setStyle("color: #eceaea");

            dynPanel.getChildren().add(space) ;      // insert some space between inputs
        }

        // resize page for the number of fields added
        int height = dynPanel.getChildCount() * 10 ;
        dynPanel.setStyle("border-style: inset; border-color: gray; padding: 10px; " +
                          "background-color: #eceaea; left: 138px; top: 186px; " +
                          "position: absolute; width: 220px; height: " + height + "px");
        Button btnOK = df.getBtnOK();
        Button btnCancel = df.getBtnCancel();
        btnOK.setStyle("font-size: 14px; height: 30px; left: 270px; " +
                       "position: absolute; width: 66px; top: " + (186 + height + 30) + "px");
        btnCancel.setStyle("font-size: 14px; height: 30px; left: 170px; " +
                       "position: absolute; width: 66px; top: " + (186 + height + 30) + "px");
    }


    private String dynFormLevel;

    public String getDynFormLevel() {
        return dynFormLevel;
    }

    public void setDynFormLevel(String dynFormLevel) {
        this.dynFormLevel = dynFormLevel;
    }

    private boolean wirEdit ;

    public boolean isWirEdit() {
        return wirEdit;
    }

    public void setWirEdit(boolean wirEdit) {
        this.wirEdit = wirEdit;
    }

    private Set<String> dirtyWIRSet = new HashSet<String>();

    public void setDirtyFlag(String id) {
        dirtyWIRSet.add(id) ;
    }

    public boolean isDirty(String id) {
        return dirtyWIRSet.contains(id);
    }

    public void removeDirtyFlag(String id) {
        dirtyWIRSet.remove(id);
    }

    public String sourceTab ;

    public String getSourceTab() {
        return sourceTab;
    }

    public void setSourceTab(String sourceTab) {
        this.sourceTab = sourceTab;
    }

    private String activePage ;

    public String getActivePage() {
        return activePage;
    }

    public void setActivePage(String activePage) {
        this.activePage = activePage;
    }

    public void refreshUserWorkQueues() {
//        if (activePage.equals("userWorkQueues")) {
//            userWorkQueues uwq = ((userWorkQueues) getBean("userWorkQueues"));
//            if (uwq != null) uwq.forceRefresh();
//        }
    }

}


