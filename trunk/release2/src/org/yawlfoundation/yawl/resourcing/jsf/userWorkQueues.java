/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing.jsf;

import com.sun.rave.web.ui.appbase.AbstractPageBean;
import com.sun.rave.web.ui.component.*;
import com.sun.rave.web.ui.model.Option;
import org.jdom.Element;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.resourcing.ResourceManager;
import org.yawlfoundation.yawl.resourcing.TaskPrivileges;
import org.yawlfoundation.yawl.resourcing.WorkQueue;
import org.yawlfoundation.yawl.resourcing.jsf.comparator.WorkItemAgeComparator;
import org.yawlfoundation.yawl.resourcing.jsf.dynform.DynFormFactory;
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.faces.FacesException;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/*
 * The backing bean for the YAWL 2.0 user work queues form
 *
 * @author Michael Adams
 * Date: 21/10/2007
 *
 * Last Date: 28/05/2008
 */

public class userWorkQueues extends AbstractPageBean {

    // REQUIRED AND/OR IMPLEMENTED ABSTRACT PAGE BEAN METHODS //

    private int __placeholder;

    private void _init() throws Exception { }


    public void init() {
        super.init();

        // *Note* - this logic should NOT be modified
        try {
            _init();
        } catch (Exception e) {
            log("userWorkQueues Initialization Failure", e);
            throw e instanceof FacesException ? (FacesException) e: new FacesException(e);
        }
    }

    public void preprocess() { }

    public void destroy() { }


    public userWorkQueues() { }

    // Return references to scoped data beans //
    protected RequestBean getRequestBean() {
        return (RequestBean)getBean("RequestBean");
    }

    protected ApplicationBean getApplicationBean() {
        return (ApplicationBean)getBean("ApplicationBean");
    }

    protected SessionBean getSessionBean() {
        return (SessionBean)getBean("SessionBean");
    }

    /********************************************************************************/

    // PAGE COMPONENT DECLARATIONS, GETTERS & SETTERS //

    private Page page1 = new Page();
    
    public Page getPage1() { return page1; }
    
    public void setPage1(Page p) { page1 = p; }
    

    private Html html1 = new Html();
    
    public Html getHtml1() { return html1; }
    
    public void setHtml1(Html h) { html1 = h; }

    
    private Head head1 = new Head();
    
    public Head getHead1() { return head1; }
    
    public void setHead1(Head h) { head1 = h; }
    

    private Link link1 = new Link();
    
    public Link getLink1() { return link1; }
    
    public void setLink1(Link l) { link1 = l; }

    
    private Body body1 = new Body();
    
    public Body getBody1() { return body1; }
    
    public void setBody1(Body b) { body1 = b; }


    private Form form1 = new Form();
    
    public Form getForm1() { return form1; }
    
    public void setForm1(Form f) { form1 = f; }


    private TabSet tabSet = new TabSet();

    public TabSet getTabSet() { return tabSet; }

    public void setTabSet(TabSet ts) { tabSet = ts; }


    private Tab tabOffered = new Tab();

    public Tab getTabOffered() { return tabOffered; }

    public void setTabOffered(Tab t) { tabOffered = t; }


    private PanelLayout lpOffered = new PanelLayout();

    public PanelLayout getLpOffered() { return lpOffered; }

    public void setLpOffered(PanelLayout pl) { lpOffered = pl; }


    private Tab tabAllocated = new Tab();

    public Tab getTabAllocated() { return tabAllocated; }

    public void setTabAllocated(Tab t) { tabAllocated = t; }


    private PanelLayout lpAllocated = new PanelLayout();

    public PanelLayout getLpAllocated() { return lpAllocated; }

    public void setLpAllocated(PanelLayout pl) { lpAllocated = pl; }


    private Tab tabStarted = new Tab();

    public Tab getTabStarted() { return tabStarted; }

    public void setTabStarted(Tab t) { tabStarted = t; }


    private PanelLayout lpStarted = new PanelLayout();

    public PanelLayout getLpStarted() { return lpStarted; }

    public void setLpStarted(PanelLayout pl) { lpStarted = pl; }


    private Button btnAccept = new Button();

    public Button getBtnAccept() { return btnAccept; }

    public void setBtnAccept(Button b) { btnAccept = b; }


    private Tab tabSuspended = new Tab();

    public Tab getTabSuspended() { return tabSuspended; }

    public void setTabSuspended(Tab t) { tabSuspended = t; }


    private PanelLayout lpSuspended = new PanelLayout();

    public PanelLayout getLpSuspended() { return lpSuspended; }

    public void setLpSuspended(PanelLayout pl) { lpSuspended = pl; }


    private Button btnStart = new Button();

    public Button getBtnStart() { return btnStart; }

    public void setBtnStart(Button b) { btnStart = b; }


    private Button btnDeallocate = new Button();

    public Button getBtnDeallocate() { return btnDeallocate; }

    public void setBtnDeallocate(Button b) { btnDeallocate = b; }


    private Button btnDelegate = new Button();

    public Button getBtnDelegate() { return btnDelegate; }

    public void setBtnDelegate(Button b) { btnDelegate = b; }


    private Button btnSkip = new Button();

    public Button getBtnSkip() { return btnSkip; }

    public void setBtnSkip(Button b) { btnSkip = b; }


    private Button btnPile = new Button();

    public Button getBtnPile() { return btnPile; }

    public void setBtnPile(Button b) { btnPile = b; }


    private Button btnChain = new Button();

    public Button getBtnChain() { return btnChain; }

    public void setBtnChain(Button b) { btnChain = b; }


    private Button btnSuspend = new Button();

    public Button getBtnSuspend() { return btnSuspend; }

    public void setBtnSuspend(Button b) { btnSuspend = b; }


    private Button btnStateless = new Button();

    public Button getBtnStateless() { return btnStateless; }

    public void setBtnStateless(Button b) { btnStateless = b; }


    private Button btnStateful = new Button();

    public Button getBtnStateful() { return btnStateful; }

    public void setBtnStateful(Button b) { btnStateful = b; }


    private Button btnUnsuspend = new Button();

    public Button getBtnUnsuspend() { return btnUnsuspend; }

    public void setBtnUnsuspend(Button b) { btnUnsuspend = b; }


    private Button btnComplete = new Button();

    public Button getBtnComplete() { return btnComplete; }

    public void setBtnComplete(Button b) { btnComplete = b; }


    private Button btnView = new Button();

    public Button getBtnView() { return btnView; }

    public void setBtnView(Button b) { btnView = b; }


    private Button btnNewInstance = new Button();

    public Button getBtnNewInstance() { return btnNewInstance; }

    public void setBtnNewInstance(Button b) { btnNewInstance = b; }


    private Button btnAcceptStart = new Button();

    public Button getBtnAcceptStart() { return btnAcceptStart; }

    public void setBtnAcceptStart(Button b) { btnAcceptStart = b; }


    private Meta metaRefresh = new Meta();

    public Meta getMetaRefresh() { return metaRefresh; }

    public void setMetaRefresh(Meta m) { metaRefresh = m; }


    private PanelLayout pnlContainer ;

    public PanelLayout getPnlContainer() { return pnlContainer; }

    public void setPnlContainer(PanelLayout pnl) { pnlContainer = pnl; }
    

    private Button btnVisualiser = new Button();

    public Button getBtnVisualiser() { return btnVisualiser; }

    public void setBtnVisualiser(Button btn) { btnVisualiser = btn; }


    /********************************************************************************/

    // SPECIFIC DELARATIONS AND METHODS //

    private SessionBean _sb = getSessionBean();
    private ResourceManager _rm = getApplicationBean().getResourceManager();
    private MessagePanel msgPanel = _sb.getMessagePanel() ;


    /** @return a reference to the session scoped form factory bean. */
    private DynFormFactory getDynFormFactory() {
        return (DynFormFactory) getBean("DynFormFactory");
    }


    /**
     * Overridden method that is called immediately before the page is rendered
     */
    public void prerender() {
        _sb.checkLogon();                                  // check session still live
        msgPanel.show(100, 0, "relative");                 // show msgs (if any)

        // return to same tab on a refresh
        if (_sb.getSourceTab() != null) {
            tabSet.setSelected(_sb.getSourceTab());
            _sb.setSourceTab(null);
        }

        // check flags & take post-roundtrip action if any are set 
        if (_sb.isDelegating()) postDelegate();
        else if (_sb.isReallocating()) postReallocate();
        else if (_sb.isCustomFormPost()) postCustomForm();
        else if (_sb.isWirEdit()) postEditWIR() ;
        else if (_sb.isAddInstance()) postAddInstance();

        // get the last selected tab
        String selTabName = tabSet.getSelected() ;
        Tab selTab = null;

        // if no last selected tab, this is the first rendering of the page
        if (selTabName != null) {
            if (selTabName.equals("tabOffered")) {
                tabOffered_action() ;
                selTab = tabOffered;
            }
            else if (selTabName.equals("tabAllocated")) {
                tabAllocated_action() ;
                selTab = tabAllocated;
            }
            else if (selTabName.equals("tabStarted")) {
                tabStarted_action() ;
                selTab = tabStarted;
            }
            else if (selTabName.equals("tabSuspended")) {
                tabSuspended_action() ;
                selTab = tabSuspended;
            }
            else {
                selTab = initDefaultTab();
            }
        }
        else {
            selTab = initDefaultTab();
        }    

        updateTabHeaders(selTab) ;          // highlight selected tab and update counts

        _sb.setActiveTab(tabSet.getSelected());
        _sb.setActivePage(ApplicationBean.PageRef.userWorkQueues);

        //     setRefreshRate(0) ;               // get default refresh rate from web.xml
    }


    private Tab initDefaultTab() {
        // default to offered list
        WorkItemRecord wir = _sb.getChosenWIR(WorkQueue.OFFERED) ;
        if (wir != null) ((pfQueueUI) getBean("pfQueueUI")).populateTextBoxes(wir);
        tabSet.setSelected("tabOffered");
        tabOffered_action() ;
        return tabOffered;
    }


    /**********************************************************************************/

    // TAB ACTIONS //

    public String tabOffered_action() {
        populateQueue(WorkQueue.OFFERED);
        processUserPrivileges(WorkQueue.OFFERED) ;
        return null;
    }


    public String tabAllocated_action() {
        populateQueue(WorkQueue.ALLOCATED);
        processUserPrivileges(WorkQueue.ALLOCATED) ;
        return null;
    }


    public String tabStarted_action() {
        populateQueue(WorkQueue.STARTED);
        return null;
    }


    public String tabSuspended_action() {
        populateQueue(WorkQueue.SUSPENDED);
        return null;
    }


    /**********************************************************************************/

    // BUTTON ACTIONS //

    public String btnRefresh_action() {
        return null ;
    }


    public String btnVisualise_action() {
        return "showVisualiser" ;
    }


    public String btnNewInstance_action() {
        _sb.setSourceTab("tabStarted");                      // come back to started tab
        WorkItemRecord wir = _sb.getChosenWIR(WorkQueue.STARTED);
        if (wir != null) {
            String paramName = _rm.getMIFormalInputParamName(wir);
            if (paramName != null) {
                _sb.setAddInstanceParamName(paramName);
                _sb.setAddInstanceItemID(wir.getID());
                _sb.setAddInstanceHeader(wir.getTaskIDForDisplay());
                return "addInstance";
            }
            else
                msgPanel.error("Could not retrieve task parameter from Engine for new instance creation");
        }
        return null;
    }

    public String btnStart_action() {
        return doAction(WorkQueue.ALLOCATED, "start") ;
    }

    public String btnDeallocate_action() {
        return doAction(WorkQueue.ALLOCATED, "deallocate") ;
    }

    public String btnSkip_action() {
        return doAction(WorkQueue.ALLOCATED, "skip") ;
    }

    public String btnPile_action() {
        return doAction(WorkQueue.ALLOCATED, "pile") ;
    }

    public String btnChain_action() {
        return doAction(WorkQueue.OFFERED, "chain") ;
    }

    public String btnAccept_action() {
        return doAction(WorkQueue.OFFERED, "acceptOffer") ;
    }

    public String btnAcceptStart_action() {
        return doAction(WorkQueue.OFFERED, "acceptStart") ;
    }

    public String btnUnsuspend_action() {
        return doAction(WorkQueue.SUSPENDED, "unsuspend") ;
    }

    public String btnSuspend_action() {
        return doAction(WorkQueue.STARTED, "suspend") ;
    }

    public String btnStateless_action() {
        return reallocateItem(false) ;
    }

    public String btnStateful_action() {
        return reallocateItem(true);
    }

    public String btnComplete_action() {
        return doAction(WorkQueue.STARTED, "complete") ;
    }


    public String btnView_action() {
        _sb.setSourceTab("tabStarted");                      // come back to started tab
        WorkItemRecord wir = _sb.getChosenWIR(WorkQueue.STARTED);

        // maybe the wir was part of a cancellation set and now it's gone
        if (wir == null) {
            msgPanel.error("Cannot view item contents - it appears that the " +
                           "selected item has been removed or cancelled. " +
                           "Please see the log files for details.");
            return null;
        }

        // if there's a custom form for this item, use it
        if (wir.getCustomFormURL() != null) {
            showCustomForm(wir) ;
            return null ;
        }

        // otherwise default to a dynamic form
        else {
            _sb.setDynFormType(ApplicationBean.DynFormType.tasklevel);
            DynFormFactory df = (DynFormFactory) getBean("DynFormFactory");
            df.setHeaderText("Edit Work Item: " + wir.getCaseID());
            df.setDisplayedWIR(wir);
            if (df.initDynForm("YAWL 2.0 - Edit Work Item")) {
                return "showDynForm" ;
            }
            else {
                msgPanel.error("Cannot view item contents - problem initialising " +
                               "dynamic form from task specification. " +
                               "Please see the log files for details.");
                return null;
            }
        }
    }


    public String btnDelegate_action() {
        String redirect = buildSubordinatesList("delegate") ;
        if (redirect != null)
            _sb.setUserListFormHeaderText("Delegate workitem to:") ;
        return redirect;
    }


    /**
     * Reallocate a workitem to the chosen participant
     * @param stateful - if true, reallocate with state preserved
     * @return which form to navigate to
     */
    private String reallocateItem(boolean stateful) {
        String redirect = buildSubordinatesList("reallocate") ;
        if (redirect != null) {
            _sb.setReallocatingStateful(stateful);
            _sb.setUserListFormHeaderText("Reallocate workitem to:") ;
        }
        return redirect;
    }


    private String buildSubordinatesList(String action) {
        String result = null;
        String pid = _sb.getParticipant().getID();

        // participant can only delegate/reallocate to those reporting to he or she
        Set<Participant> underlings = _rm.getParticipantsReportingTo(pid);
        if (underlings != null) {

            // build the option list
            Option[] options = new Option[underlings.size()];
            int i = 0 ;
            for (Participant p : underlings) {
                options[i++] = new Option(p.getID(), p.getFullName());
            }
            _sb.setSelectUserListOptions(options);
            _sb.setNavigateTo("showUserQueues");
            result = "userSelect";
        }
        else {
             msgPanel.warn(String.format(
                "There are no participants that you are authorised to %s to.", action));
        }
        return result;
    }


    /** Performs the appropriate user action with the specified queue */
    private String doAction(int queueType, String action) {
        Participant p = _sb.getParticipant();
        WorkItemRecord wir = _sb.getChosenWIR(queueType);
        String handle = _sb.getSessionhandle() ;
        try {
            if (action.equals("deallocate"))
                _rm.deallocateWorkItem(p, wir);
            else if (action.equals("skip"))
                _rm.skipWorkItem(p, wir, handle);
            else if (action.equals("start")) {
                if (! _rm.start(p, wir, handle)) {
                    msgPanel.error("Could not start workitem '" + wir.getID() +
                     "'. Please see the log files for details.");   
                }
            }    
            else if (action.equals("suspend"))
                _rm.suspendWorkItem(p, wir);
            else if (action.equals("unsuspend"))
                _rm.unsuspendWorkItem(p, wir);
            else if (action.equals("acceptOffer")) {
                if (wir != null)
                    _rm.acceptOffer(p, wir);
                else
                    msgPanel.info("Another participant has already accepted this offer.");
            }
            else if (action.equals("acceptStart")) {
                if (wir != null) {
                    _rm.acceptOffer(p, wir);

                    // if the accepted offer has a system-initiated start, it's
                    // already started, so don't do it again
                    if (wir.getResourceStatus().equals(WorkItemRecord.statusResourceAllocated)) {
                        if (! _rm.start(p, wir, handle)) {
                            msgPanel.error("Could not start workitem '" + wir.getID() +
                             "'. Please see the log files for details.");
                        }
                    }    
                }
                else
                    msgPanel.info("Another participant has already accepted this offer.");
            }
            else if (action.equals("pile")) {
                String result = _rm.pileWorkItem(p, wir);
                if (result.startsWith("Cannot"))
                    msgPanel.error(result);
                else
                    msgPanel.success(result);
            }
            else if (action.equals("chain")) {
                String result = _rm.chainCase(p, wir);
                if (result.startsWith("Cannot"))
                    msgPanel.error(result);
                else
                    msgPanel.success(result);
            }
            else if (action.equals("complete")) {

                // warn user if workitem has params but hasn't yet been edited
                if ((! getApplicationBean().isEmptyWorkItem(wir)) &&
                   (wir.getUpdatedData() == null) && (! _sb.hasWarnedForNonEdit(wir.getID()))) {
                    msgPanel.info("Warning: This item has not been edited. If you are " +
                                  "sure you want to complete without editing, click " +
                                  "the 'Complete' button again.");
                    _sb.setWarnedForNonEdit(wir.getID()) ;
                    return null ;
                }
                
                completeWorkItem(wir, p);
            }
        }
        catch (Exception e) {
            msgPanel.error("The attempt to " + action + " the selected workitem was "+
                           "unsuccessful. Please check the log files for details.");
            _rm.getLogger().error("Exception in user work queues: ", e);
        }
        return null ;                                         // stay on same form
    }



    /** prepare workitem data, pass to and show custom form */
    private void showCustomForm(WorkItemRecord wir) {
        String url = wir.getCustomFormURL();
        if (url != null) {
            _sb.setCustomFormPost(true);
            try {

                // adjust session timeout value if required
                adjustSessionTimeout(wir);

                // show the custom form
                StringBuilder redir = new StringBuilder(url);
                redir.append("?workitem=")
                     .append(wir.getID())
                     .append("&handle=")
                     .append(_sb.getSessionhandle());

                FacesContext.getCurrentInstance().getExternalContext().redirect(redir.toString());
            }
            catch (Exception e) {
                _sb.setCustomFormPost(false);
                msgPanel.error("IO Exception attempting to display custom form: " +
                               e.getMessage());
            }
        }
    }


    /**********************************************************************************/

    // POST BACK ACTIONS //

    /** perform reallocation after user selection on secondary screen */
    private void postReallocate() {
        if (_sb.isReallocating()) {
            Participant pFrom = _sb.getParticipant();
            String userIDTo = _sb.getSelectUserListChoice() ;        // this is the p-id
            Participant pTo = _rm.getParticipant(userIDTo) ;
            WorkItemRecord wir = _sb.getChosenWIR(WorkQueue.STARTED);

            boolean successful ;
            if (_sb.isReallocatingStateful())
                successful = _rm.reallocateStatefulWorkItem(pFrom, pTo, wir);
            else
                successful = _rm.reallocateStatelessWorkItem(pFrom, pTo, wir);

            if (successful)
               msgPanel.success("Workitem successfully reallocated.");
            else
               msgPanel.error("Failed to reallocate workitem.");

            _sb.setReallocating(false);                              // reset flag
            forceRefresh() ;                                         // to show message
        }
    }


    /** perform delegation after user selection on secondary screen */
    private void postDelegate() {
        if (_sb.isDelegating()) {
            Participant pFrom = _sb.getParticipant();
            String pIDTo = _sb.getSelectUserListChoice() ;        // this is the p-id
            Participant pTo =  _rm.getParticipant(pIDTo) ;
            WorkItemRecord wir = _sb.getChosenWIR(WorkQueue.ALLOCATED);

            if (_rm.delegateWorkItem(pFrom, pTo, wir))
                msgPanel.success("Workitem successfully delegated.");
            else
                msgPanel.error("Failed to delegate workitem");

            _sb.setDelegating(false);
            forceRefresh() ;                                         // to show message
        }
    }


    /** updates a workitem after editing on a dynamic form */
    private void postEditWIR() {
        if (_sb.isWirEdit()) {
            WorkItemRecord wir = _sb.getChosenWIR(WorkQueue.STARTED);
            if (wir != null) {
                Element data = JDOMUtil.stringToElement(getDynFormFactory().getDataList());
                wir.setUpdatedData(data);
                _rm.getWorkItemCache().update(wir) ;

                if (_sb.isCompleteAfterEdit()) {
                    completeWorkItem(wir, _sb.getParticipant());
                }
            }
            else {
                msgPanel.error("Could not complete workitem. Check log for details.");
            }
        }
        _sb.setWirEdit(false);
        _sb.setCompleteAfterEdit(false);
        if (msgPanel.hasMessage()) forceRefresh();
    }


    /** takes necessary action after editing a custom form */
    private void postCustomForm() {

        // reset session timeout if previously changed
        if (_sb.isSessionTimeoutValueChanged()) {
            _sb.resetSessionTimeout();
            _sb.setSessionTimeoutValueChanged(false);
        }

        // retrieve completion flag - if any
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        Boolean complete = (Boolean) context.getSessionMap().remove("complete_on_post");

        // complete wir if requested
        if ((complete != null) && complete) {
            WorkItemRecord wir = _sb.getChosenWIR(WorkQueue.STARTED);
            completeWorkItem(wir, _sb.getParticipant());
        }

        _sb.setCustomFormPost(false);                                  // reset flag
    }


    private void postAddInstance() {
        String paramValue = _sb.getAddInstanceParamVal();
        if (paramValue != null) {
            String data = StringUtil.wrap(paramValue, _sb.getAddInstanceParamName());
            WorkItemRecord newWir = _rm.createNewWorkItemInstance(
                                              _sb.getAddInstanceItemID(), data);
            if (newWir != null) {
                newWir.setResourceStatus(WorkItemRecord.statusResourceAllocated);
                _sb.getParticipant().getWorkQueues().addToQueue(newWir, WorkQueue.ALLOCATED);
                msgPanel.success("New instance successfully created and added to allocate queue.");
            }
            else msgPanel.error("Create Instance Error: " +
                                "Engine failed to create new instance. See logs for details.");
        }
        else msgPanel.error("Create Instance Error: " +
                            "Problem reading parameter value from New Instance form.");

        _sb.clearAddInstanceParam();
    }


    private void completeWorkItem(WorkItemRecord wir, Participant p) {
        String result = _rm.checkinItem(p, wir, _sb.getSessionhandle());
        if (_rm.successful(result))
            _sb.removeWarnedForNonEdit(wir.getID());
        else
            msgPanel.error(msgPanel.format(result)) ;
    }


    /*******************************************************************************/

    // MISC METHODS //

    /** refreshes the page */
    public void forceRefresh() {
        ExternalContext externalContext = getFacesContext().getExternalContext();
        if (externalContext != null) {
            try {
                externalContext.redirect("userWorkQueues.jsp");
            }
            catch (IOException ioe) {}
        }
    }


    // Highlight selected tab's name and show queue's item count on each tab
    private void updateTabHeaders(Tab selected) {

        // reset tab heading styles and highlight the selected one
        tabOffered.setStyle("");
        tabAllocated.setStyle("");
        tabStarted.setStyle("");
        tabSuspended.setStyle("");
        if (selected != null) selected.setStyle("color: #3277ba");

        // get counts for each queue
        int[] itemCount = new int[4] ;
        for (int queue = WorkQueue.OFFERED; queue <= WorkQueue.SUSPENDED; queue++)
            itemCount[queue] = _sb.getQueueSize(queue) ;

        // update heading text for each tab
        tabOffered.setText(String.format("Offered (%d)", itemCount[WorkQueue.OFFERED]));
        tabAllocated.setText(String.format("Allocated (%d)", itemCount[WorkQueue.ALLOCATED]));
        tabStarted.setText(String.format("Started (%d)", itemCount[WorkQueue.STARTED]));
        tabSuspended.setText(String.format("Suspended (%d)", itemCount[WorkQueue.SUSPENDED]));
    }

    /**
     * Sets the auto refresh rate of the page
     * @param rate if <0, disables page refreshes; if >0, set refresh rate to that
     *        number of seconds; if 0, set the rate to the default provided by the
     *        resourceService's web.xml
     */
    public void setRefreshRate(int rate) {
        if (rate < 0)
            metaRefresh.setContent(null) ;
        else {
            if (rate == 0) rate = getApplicationBean().getDefaultJSFRefreshRate() ;
            metaRefresh.setContent(rate + "; url=./userWorkQueues.jsp");
        }
    }


    /**
     * Populates the specified queue's workitem list and shows the selected items details
     * @param queueType the queue to populate
     * @return the number of items in the queue
     */
    private int populateQueue(int queueType) {
        int result = -1;                                    // default for empty queue
        Set<WorkItemRecord> queue = _sb.refreshQueue(queueType);
        processButtonEnablement(queueType) ;                // disable btns if queue empty
        ((pfQueueUI) getBean("pfQueueUI")).clearQueueGUI();

        if ((queue != null) && (! queue.isEmpty())) {

            // add items to listbox and get first or selected one in list
            addItemsToListOptions(queue, _sb.getChosenWIR(queueType)) ;
            WorkItemRecord choice = _sb.getChosenWIR(queueType) ;         
            showWorkItem(choice);                                   // show details
            processTaskPrivileges(choice, queueType) ;
            result = queue.size() ;
        }
        else {
            if (! (_sb.isDelegating() || _sb.isReallocating() ||
                   _sb.isCustomFormPost() || _sb.isWirEdit()))
                _sb.setWorklistChoice(null);
        }
        return result ;
    }

    /** populate the text fields with the details of the workitem */
    private void showWorkItem(WorkItemRecord wir) {
        pfQueueUI itemsSubPage = (pfQueueUI) getBean("pfQueueUI");
        itemsSubPage.getLbxItems().setSelected(wir.getID());
        itemsSubPage.populateTextBoxes(wir) ;
    }


    /**
     * Create list of queued items and add to listbox (via sessionbean)
     * @param queue the set of items in the queue
     * @return the first item in the list
     */
    private void addItemsToListOptions(Set<WorkItemRecord> queue,
                                                 WorkItemRecord selected) {
        Option[] options = new Option[queue.size()] ;
        WorkItemRecord first = null;
        boolean listContainsSelected = false;
        SortedSet<WorkItemRecord> qSorted =
                               new TreeSet<WorkItemRecord>(new WorkItemAgeComparator());
        qSorted.addAll(queue);
        int i = 0 ;
        for (WorkItemRecord wir : qSorted) {
            if (wir != null) {
                if (i==0) first = wir;                       // get first non-null item
                options[i++] = new Option(wir.getID()) ;
                if ((selected != null) && (selected.getID().equals(wir.getID()))) {
                    listContainsSelected = true;
                }
            }
        }
        if (! listContainsSelected) {
            _sb.setChosenWIR(first);                             // set first listed
        }
        _sb.setWorklistOptions(options);
    }


    /** Enable/disable buttons depending on user/task privileges */
    private void processTaskPrivileges(WorkItemRecord wir, int qType) {
        Participant p = _sb.getParticipant();
        if (qType == WorkQueue.ALLOCATED) {
            btnDeallocate.setDisabled(! _rm.hasUserTaskPrivilege(p, wir,
                                               TaskPrivileges.CAN_DEALLOCATE));
            btnDelegate.setDisabled(! _rm.hasUserTaskPrivilege(p, wir, 
                                               TaskPrivileges.CAN_DELEGATE));
            btnSkip.setDisabled(! _rm.hasUserTaskPrivilege(p, wir,
                                               TaskPrivileges.CAN_SKIP));
            btnPile.setDisabled(! _rm.hasUserTaskPrivilege(p, wir,
                                               TaskPrivileges.CAN_PILE));
        }
        else if (qType == WorkQueue.STARTED) {
            btnSuspend.setDisabled(! _rm.hasUserTaskPrivilege(p, wir,
                                               TaskPrivileges.CAN_SUSPEND));
            btnStateful.setDisabled(! _rm.hasUserTaskPrivilege(p, wir,
                                               TaskPrivileges.CAN_REALLOCATE_STATEFUL));
            btnStateless.setDisabled(! _rm.hasUserTaskPrivilege(p, wir,
                                               TaskPrivileges.CAN_REALLOCATE_STATELESS));

            // set view & complete buttons
            boolean emptyItem = getApplicationBean().isEmptyWorkItem(wir);
            btnView.setDisabled(emptyItem && (wir.getCustomFormURL() == null));
            btnComplete.setDisabled(! (emptyItem || (wir.getUpdatedData() != null)));

            // set 'New Instance' button (not a task priv but convenient to do it here)
            if (wir != null)  {
                String canCreate = wir.getAllowsDynamicCreation();
                btnNewInstance.setDisabled((canCreate != null) &&
                                            ! canCreate.equalsIgnoreCase("true"));
            }
        }
    }


    /** Enable/disable buttons using user privileges */
    private void processUserPrivileges(int queue) {
        Participant p = _sb.getParticipant();
        if (! p.isAdministrator()) {
            if ((queue == WorkQueue.OFFERED) && (_sb.getQueueSize(WorkQueue.OFFERED) > 0)) {
                btnChain.setDisabled(! p.getUserPrivileges().canChainExecution());
                btnAcceptStart.setDisabled(isStartDisabled(p));

            }
            if ((queue == WorkQueue.ALLOCATED) && (_sb.getQueueSize(WorkQueue.ALLOCATED) > 0)) {

                btnStart.setDisabled(isStartDisabled(p));

                if (! btnStart.isDisabled() &&
                   (! (p.getUserPrivileges().canChooseItemToStart() ||
                       p.getUserPrivileges().canReorder()))) {
                    btnStart.setDisabled(! _sb.isFirstWorkItemChosen());
                }
            }
        }
    }


    // returns true if the participant does not have start-concurrent privileges
    // and there is already a started workitem on their workqueues
    private boolean isStartDisabled(Participant p) {
        return (! p.getUserPrivileges().canStartConcurrent()) &&
               ((_sb.getQueueSize(WorkQueue.STARTED) > 0) ||
               (_sb.getQueueSize(WorkQueue.SUSPENDED) > 0));
    }


    /** Disables buttons if queue is empty, enables them if not */
    private void processButtonEnablement(int queueType) {
        btnVisualiser.setVisible(_sb.isVisualiserEnabled());

        boolean isEmptyQueue = (_sb.getQueueSize(queueType) == 0) ;
        switch (queueType) {
            case WorkQueue.OFFERED   : btnAccept.setDisabled(isEmptyQueue);
                                       btnAcceptStart.setDisabled(isEmptyQueue);
                                       btnChain.setDisabled(isEmptyQueue);
                                       break;
            case WorkQueue.ALLOCATED : btnStart.setDisabled(isEmptyQueue);
                                       btnDeallocate.setDisabled(isEmptyQueue);
                                       btnDelegate.setDisabled(isEmptyQueue);
                                       btnSkip.setDisabled(isEmptyQueue);
                                       btnPile.setDisabled(isEmptyQueue);
                                       break;
            case WorkQueue.STARTED   : btnView.setDisabled(isEmptyQueue);
                                       btnSuspend.setDisabled(isEmptyQueue);
                                       btnStateful.setDisabled(isEmptyQueue);
                                       btnStateless.setDisabled(isEmptyQueue);
                                       btnNewInstance.setDisabled(isEmptyQueue);
                                       btnComplete.setDisabled(isEmptyQueue);
                                       break;
            case WorkQueue.SUSPENDED : btnUnsuspend.setDisabled(isEmptyQueue);
        }
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

    /********************************************************************************/
}

