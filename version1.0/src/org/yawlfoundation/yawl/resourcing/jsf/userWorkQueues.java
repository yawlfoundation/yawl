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
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.util.JDOMUtil;

import javax.faces.FacesException;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
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
 * Last Date: 28/04/2008
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

        tabOffered_action();                     // select the offered worklist on init
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


    private Meta metaRefresh = new Meta();

    public Meta getMetaRefresh() { return metaRefresh; }

    public void setMetaRefresh(Meta m) { metaRefresh = m; }
    

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
        getSessionBean().checkLogon();                     // check session still live
        msgPanel.show(395, 150, "absolute");               // show msgs (if any)

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

        // get the last selected tab
        String selTabName = tabSet.getSelected() ;
        Tab selTab = null;

        // if no last selected tab, this is the first rendering of the page
        if (selTabName == null) {

            // default to offered list
            WorkItemRecord wir = _sb.getChosenWIR(WorkQueue.OFFERED) ;
            if (wir != null) ((pfQueueUI) getBean("pfQueueUI")).populateTextBoxes(wir);
            tabSet.setSelected("tabOffered");
            selTab = tabOffered;
            tabOffered_action() ;
        }    
        else {
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
        }
        updateTabHeaders(selTab) ;          // highlight selected tab and update counts

        _sb.setActiveTab(tabSet.getSelected());
        _sb.setActivePage(ApplicationBean.PageRef.userWorkQueues);

        //     setRefreshRate(0) ;               // get default refresh rate from web.xml
    }





    public String btnRefresh_action() {
        return null ;
    }

    public String btnNewInstance_action() {
        return null;
    }


    public String btnStart_action() {
        return doAction(WorkQueue.ALLOCATED, "start") ;
    }


    public String btnDeallocate_action() {
        return doAction(WorkQueue.ALLOCATED, "deallocate") ;
    }


    public String btnDelegate_action() {
        String pid = _sb.getParticipant().getID();
        Set<Participant> underlings = _rm.getParticipantsReportingTo(pid);

        if (underlings != null) {
            // build the option list
            Option[] options = new Option[underlings.size()];
            int i = 0 ;
            for (Participant p : underlings) {
                options[i++] = new Option(p.getID(), p.getFullName());
            }
            _sb.setSelectUserListOptions(options);
            _sb.setUserListFormHeaderText("Delegate workitem to:") ;
            _sb.setNavigateTo("showUserQueues");
            return "userSelect" ;
        }
        else {
            // message no underlings
            return null ;
        }
    }

    private void postDelegate() {
        if (_sb.isDelegating()) {
            Participant pFrom = _sb.getParticipant();
            String pIDTo = _sb.getSelectUserListChoice() ;        // this is the p-id
            Participant pTo =  _rm.getParticipant(pIDTo) ;
            WorkItemRecord wir = _sb.getChosenWIR(WorkQueue.ALLOCATED);

            try {
                _rm.delegateWorkItem(pFrom, pTo, wir);
                // message successful
            }
            catch (Exception e) {
               // show connection error or timeout
            }
            _sb.setDelegating(false);
        }
        
    }


    public String btnSkip_action() {
        return doAction(WorkQueue.ALLOCATED, "skip") ;
    }

    public void forceRefresh() {
        ExternalContext externalContext = getFacesContext().getExternalContext();
        if (externalContext != null) {
            try {
                externalContext.redirect("userWorkQueues.jsp");
            }
            catch (IOException ioe) {}
        }
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

    private String doAction(int queueType, String action) {
        Participant p = _sb.getParticipant();
        WorkItemRecord wir = _sb.getChosenWIR(queueType);
        String handle = _sb.getSessionhandle() ;
        try {
            if (action.equals("acceptOffer")) {
                if (wir != null)
                    _rm.acceptOffer(p, wir);
                else
                    msgPanel.info("Another participant has already accepted this offer.");
            }
            else if (action.equals("deallocate"))
                _rm.deallocateWorkItem(p, wir);
            else if (action.equals("skip"))
                _rm.skipWorkItem(p, wir, handle);
            else if (action.equals("start"))
                _rm.start(p, wir, handle);
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
            else if (action.equals("suspend"))
                _rm.suspendWorkItem(p, wir);
            else if (action.equals("unsuspend"))
                _rm.unsuspendWorkItem(p, wir);
            else if (action.equals("complete")) {
                if (wir.getUpdatedData() == null) {
                    //message about not editing the item
                }
                String result = _rm.checkinItem(p, wir, handle);
                if (_rm.successful(result))
                    _sb.resetDynFormParams();
                else msgPanel.error(msgPanel.format(result)) ;
                
            }
            
            return null ;
        }
        catch (Exception e) {
           // show connection error or timeout
            return "loginPage" ;
        }
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

    private String reallocateItem(boolean stateful) {

        Set<Participant> pSet = _rm.getParticipants();

        if (pSet != null) {
            // build the option list
            Option[] options = new Option[pSet.size()];
            int i = 0 ;
            for (Participant p : pSet) {
                options[i++] = new Option(p.getID(), p.getFullName());
            }
            _sb.setReallocatingStateful(stateful);
            _sb.setSelectUserListOptions(options);
            _sb.setUserListFormHeaderText("Reallocate workitem to:") ;
            _sb.setNavigateTo("showUserQueues");
            return "userSelect" ;
        }
        else return null ;

    }



    private void postReallocate() {
        SessionBean sb = _sb;
        if (sb.isReallocating()) {
            Participant pFrom = sb.getParticipant();
            String userIDTo = sb.getSelectUserListChoice() ;        // this is the p-id
            Participant pTo = _rm.getParticipant(userIDTo) ;
            WorkItemRecord wir = sb.getChosenWIR(WorkQueue.STARTED);

            if (sb.isReallocatingStateful())
                _rm.reallocateStatefulWorkItem(pFrom, pTo, wir);
            else
                _rm.reallocateStatelessWorkItem(pFrom, pTo, wir);

            sb.setReallocating(false);
        }
    }


    public String btnStateful_action() {
        return reallocateItem(true);
    }

    public String btnView_action() {
        _sb.setSourceTab("tabStarted");
        WorkItemRecord wir = _sb.getChosenWIR(WorkQueue.STARTED);
        if (wir.getCustomFormURL() != null) {
            showCustomForm(wir) ;
            return null ;
        }
        else {
            _sb.setDynFormType(ApplicationBean.DynFormType.tasklevel);
            DynFormFactory df = (DynFormFactory) getBean("DynFormFactory");
            df.setHeaderText("Edit Work Item: " + wir.getID());
            df.setDisplayedWIR(wir);
            df.initDynForm("YAWL 2.0 - Edit Work Item") ;
            return "showDynForm" ;
        }
    }


    private void postEditWIR() {
        if (_sb.isWirEdit()) {
            WorkItemRecord wir = _sb.getChosenWIR(WorkQueue.STARTED);
            Element data = JDOMUtil.stringToElement(getDynFormFactory().getDataList());
            wir.setUpdatedData(data);
            _rm.getWorkItemCache().update(wir) ;
            _sb.setWirEdit(false);
        }
    }


    public String btnComplete_action() {
        return doAction(WorkQueue.STARTED, "complete") ;
    }


    private void showCustomForm(WorkItemRecord wir) {
        String url = wir.getCustomFormURL();
        if (url != null) {
            _sb.setCustomFormPost(true);
            String xml = wir.toXML();
            FacesContext context = FacesContext.getCurrentInstance();
            try {
                if (wir.getUpdatedData() != null) {
                    WorkItemRecord dirtywir = wir.clone() ;
                    dirtywir.setDataList(wir.getUpdatedData());
                    xml = dirtywir.toXML();
                }
                context.getExternalContext().getSessionMap().put("workitem", xml);
                context.getExternalContext().redirect(url);
            }
            catch (Exception e) {
                _sb.setCustomFormPost(false);
                msgPanel.error("IO Exception attempting to display custom form.");
            }
        }
    }


    private void postCustomForm() {

        // retrieve and remove wir from custom form's post data
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext extContext = context.getExternalContext();
        String wirXML = (String) extContext.getSessionMap().remove("workitem");
        WorkItemRecord updated = _rm.unMarshallWIR(wirXML);

        // update edited wir
        WorkItemRecord wir = _sb.getChosenWIR(WorkQueue.STARTED);
        wir.setUpdatedData(updated.getDataList());
        _rm.getWorkItemCache().update(wir) ;

        _sb.setCustomFormPost(false);                                  // reset flag
    }


    private void updateTabHeaders(Tab selected) {
        tabOffered.setStyle("");
        tabAllocated.setStyle("");
        tabStarted.setStyle("");
        tabSuspended.setStyle("");
        if (selected != null) selected.setStyle("color: #3277ba");
        
        int[] itemCount = new int[4] ;
        for (int queue = WorkQueue.OFFERED; queue <= WorkQueue.SUSPENDED; queue++)
            itemCount[queue] = _sb.getQueueSize(queue) ;
        tabOffered.setText(String.format("Offered (%d)", itemCount[WorkQueue.OFFERED]));
        tabAllocated.setText(String.format("Allocated (%d)", itemCount[WorkQueue.ALLOCATED]));
        tabStarted.setText(String.format("Started (%d)", itemCount[WorkQueue.STARTED]));
        tabSuspended.setText(String.format("Suspended (%d)", itemCount[WorkQueue.SUSPENDED]));
    }

    public String tabOffered_action() {
        populateQueue(WorkQueue.OFFERED);
        processUserPrivileges(WorkQueue.OFFERED) ;
        return null;
    }


    public String tabAllocated_action() {
        populateQueue(WorkQueue.ALLOCATED);
        return null;
    }


    public String tabStarted_action() {
        populateQueue(WorkQueue.STARTED);
        processUserPrivileges(WorkQueue.STARTED) ;
        return null;
    }


    public String tabSuspended_action() {
        populateQueue(WorkQueue.SUSPENDED);
        return null;
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

    /******************************************************************************/


    private int populateQueue(int queueType) {
        int result = -1;                                    // default for empty queue
        Set<WorkItemRecord> queue = _sb.refreshQueue(queueType);
        processButtonEnablement(queueType) ;
        ((pfQueueUI) getBean("pfQueueUI")).clearQueueGUI();

        if ((queue != null) && (! queue.isEmpty())) {
            WorkItemRecord firstWir = addItemsToListOptions(queue) ;
            WorkItemRecord choice = _sb.getChosenWIR(queueType) ;
            if (choice == null) choice = firstWir ;
            showWorkItem(choice);
            processTaskPrivileges(choice, queueType) ;
            result = queue.size() ;
        }
        return result ;
    }


    private void showWorkItem(WorkItemRecord wir) {
        pfQueueUI itemsSubPage = (pfQueueUI) getBean("pfQueueUI");
        Listbox lbx = itemsSubPage.getLbxItems();
        lbx.setSelected(wir.getID());
        itemsSubPage.populateTextBoxes(wir) ;
    }


    private WorkItemRecord addItemsToListOptions(Set<WorkItemRecord> queue) {
        Option[] options = new Option[queue.size()] ;
        WorkItemRecord result = null;
        SortedSet<WorkItemRecord> qSorted =
                               new TreeSet<WorkItemRecord>(new WorkItemAgeComparator());
        qSorted.addAll(queue);
        int i = 0 ;
        for (WorkItemRecord wir : qSorted) {
            if (wir != null) {
                if (i==0) {
                    _sb.setChosenWIR(wir);          // return first listed
                    result = wir;
                }
                options[i++] = new Option(wir.getID()) ;
            }
        }
        _sb.setWorklistOptions(options);
        return result ;
    }

    
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
            btnView.setDisabled(emptyItem);
            if (btnView.isDisabled())
                btnView.setToolTip("The selected workitem has no parameters to view/edit");
            else
                btnView.setToolTip(null);

            btnComplete.setDisabled(! (emptyItem || (wir.getUpdatedData() != null)));
            if (btnComplete.isDisabled())
                btnComplete.setToolTip("The selected workitem needs editing before it can complete");
            else
                btnComplete.setToolTip(null);

            // set 'New Instance' button (not a task priv but convenient to do it here)
            if (wir != null)  {
                String canCreate = wir.getAllowsDynamicCreation();
                btnNewInstance.setDisabled((canCreate != null) &&
                                            ! canCreate.equalsIgnoreCase("true"));
            }    
        }
    }


    private void processUserPrivileges(int queue) {
        Participant p = _sb.getParticipant();
        if (! p.isAdministrator()) {
            if ((queue == WorkQueue.OFFERED) && (_sb.getQueueSize(WorkQueue.OFFERED) > 0))
                btnChain.setDisabled(! p.getUserPrivileges().canChainExecution());

            if ((queue == WorkQueue.STARTED) && (_sb.getQueueSize(WorkQueue.STARTED) > 0)){
                btnStart.setDisabled(! p.getUserPrivileges().canStartConcurrent());

                if (! (p.getUserPrivileges().canChooseItemToStart() ||
                       p.getUserPrivileges().canReorder())) {
                    btnStart.setDisabled(! _sb.isFirstWorkItemChosen());
                }
            }
        }
    }


    private void processButtonEnablement(int queueType) {
        boolean isEmptyQueue = (_sb.getQueueSize(queueType) == 0) ;
        switch (queueType) {
            case WorkQueue.OFFERED   : btnAccept.setDisabled(isEmptyQueue);
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
}

