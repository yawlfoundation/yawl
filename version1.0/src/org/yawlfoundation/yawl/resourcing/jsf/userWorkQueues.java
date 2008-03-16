/*
 * userWorkQueues.java
 *
 * Created on October 23, 2007, 11:18 AM
 * Copyright adamsmj
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
import java.io.IOException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * <p>Page bean that corresponds to a similarly named JSP page.  This
 * class contains component definitions (and initialization code) for
 * all components that you have defined on this page, as well as
 * lifecycle methods and event handlers where you may add behavior
 * to respond to incoming events.</p>
 */
public class userWorkQueues extends AbstractPageBean {
    // <editor-fold defaultstate="collapsed" desc="Creator-managed Component Definition">
    private int __placeholder;
    
    /**
     * <p>Automatically managed component initialization.  <strong>WARNING:</strong>
     * This method is automatically generated, so any user-specified code inserted
     * here is subject to being replaced.</p>
     */
    private void _init() throws Exception {
    }
    
    private Page page1 = new Page();
    
    public Page getPage1() {
        return page1;
    }
    
    public void setPage1(Page p) {
        this.page1 = p;
    }
    
    private Html html1 = new Html();
    
    public Html getHtml1() {
        return html1;
    }
    
    public void setHtml1(Html h) {
        this.html1 = h;
    }
    
    private Head head1 = new Head();
    
    public Head getHead1() {
        return head1;
    }
    
    public void setHead1(Head h) {
        this.head1 = h;
    }
    
    private Link link1 = new Link();
    
    public Link getLink1() {
        return link1;
    }
    
    public void setLink1(Link l) {
        this.link1 = l;
    }
    
    private Body body1 = new Body();
    
    public Body getBody1() {
        return body1;
    }
    
    public void setBody1(Body b) {
        this.body1 = b;
    }
    
    private Form form1 = new Form();
    
    public Form getForm1() {
        return form1;
    }
    
    public void setForm1(Form f) {
        this.form1 = f;
    }

    private TabSet tabSet = new TabSet();

    public TabSet getTabSet() {
        return tabSet;
    }

    public void setTabSet(TabSet ts) {
        this.tabSet = ts;
    }

    private Tab tabOffered = new Tab();

    public Tab getTabOffered() {
        return tabOffered;
    }

    public void setTabOffered(Tab t) {
        this.tabOffered = t;
    }

    private PanelLayout lpOffered = new PanelLayout();

    public PanelLayout getLpOffered() {
        return lpOffered;
    }

    public void setLpOffered(PanelLayout pl) {
        this.lpOffered = pl;
    }

    private Tab tabAllocated = new Tab();

    public Tab getTabAllocated() {
        return tabAllocated;
    }

    public void setTabAllocated(Tab t) {
        this.tabAllocated = t;
    }

    private PanelLayout lpAllocated = new PanelLayout();

    public PanelLayout getLpAllocated() {
        return lpAllocated;
    }

    public void setLpAllocated(PanelLayout pl) {
        this.lpAllocated = pl;
    }

    private Tab tabStarted = new Tab();

    public Tab getTabStarted() {
        return tabStarted;
    }

    public void setTabStarted(Tab t) {
        this.tabStarted = t;
    }

    private PanelLayout lpStarted = new PanelLayout();

    public PanelLayout getLpStarted() {
        return lpStarted;
    }

    public void setLpStarted(PanelLayout pl) {
        this.lpStarted = pl;
    }

    private Button btnAccept = new Button();

    public Button getBtnAccept() {
        return btnAccept;
    }

    public void setBtnAccept(Button b) {
        this.btnAccept = b;
    }

    private Tab tabSuspended = new Tab();

    public Tab getTabSuspended() {
        return tabSuspended;
    }

    public void setTabSuspended(Tab t) {
        this.tabSuspended = t;
    }

    private PanelLayout lpSuspended = new PanelLayout();

    public PanelLayout getLpSuspended() {
        return lpSuspended;
    }

    public void setLpSuspended(PanelLayout pl) {
        this.lpSuspended = pl;
    }

    private Button btnStart = new Button();

    public Button getBtnStart() {
        return btnStart;
    }

    public void setBtnStart(Button b) {
        this.btnStart = b;
    }

    private Button btnDeallocate = new Button();

    public Button getBtnDeallocate() {
        return btnDeallocate;
    }

    public void setBtnDeallocate(Button b) {
        this.btnDeallocate = b;
    }

    private Button btnDelegate = new Button();

    public Button getBtnDelegate() {
        return btnDelegate;
    }

    public void setBtnDelegate(Button b) {
        this.btnDelegate = b;
    }

    private Button btnSkip = new Button();

    public Button getBtnSkip() {
        return btnSkip;
    }

    public void setBtnSkip(Button b) {
        this.btnSkip = b;
    }

    private Button btnPile = new Button();

    public Button getBtnPile() {
        return btnPile;
    }

    public void setBtnPile(Button b) {
        this.btnPile = b;
    }

    private Button btnChain = new Button();

    public Button getBtnChain() {
        return btnChain;
    }

    public void setBtnChain(Button b) {
        this.btnChain = b;
    }

    private Button btnSuspend = new Button();

    public Button getBtnSuspend() {
        return btnSuspend;
    }

    public void setBtnSuspend(Button b) {
        this.btnSuspend = b;
    }

    private Button btnStateless = new Button();

    public Button getBtnStateless() {
        return btnStateless;
    }

    public void setBtnStateless(Button b) {
        this.btnStateless = b;
    }

    private Button btnStateful = new Button();

    public Button getBtnStateful() {
        return btnStateful;
    }

    public void setBtnStateful(Button b) {
        this.btnStateful = b;
    }

    private Button btnUnsuspend = new Button();

    public Button getBtnUnsuspend() {
        return btnUnsuspend;
    }

    public void setBtnUnsuspend(Button b) {
        this.btnUnsuspend = b;
    }

    private Button btnComplete = new Button();

    public Button getBtnComplete() {
        return btnComplete;
    }

    public void setBtnComplete(Button b) {
        this.btnComplete = b;
    }

    private Button btnView = new Button();

    public Button getBtnView() {
        return btnView;
    }

    public void setBtnView(Button b) {
        this.btnView = b;
    }

    private Meta metaRefresh = new Meta();

    public Meta getMetaRefresh() {
        return metaRefresh;
    }

    public void setMetaRefresh(Meta m) {
        this.metaRefresh = m;
    }
    
    // </editor-fold>

    private boolean pageLoaded = false ;
    /** 
     * <p>Construct a new Page bean instance.</p>
     */
    public userWorkQueues() {
    }

    private SessionBean _sb = getSessionBean();

    private ResourceManager _rm = getApplicationBean().getResourceManager();

    /** 
     * <p>Return a reference to the scoped data bean.</p>
     */
    protected RequestBean getRequestBean() {
        return (RequestBean)getBean("RequestBean");
    }


    /** 
     * <p>Return a reference to the scoped data bean.</p>
     */
    protected ApplicationBean getApplicationBean() {
        return (ApplicationBean)getBean("ApplicationBean");
    }

    /** 
     * <p>Return a reference to the scoped data bean.</p>
     */
    protected SessionBean getSessionBean() {
        return (SessionBean)getBean("SessionBean");
    }

    /** @return a reference to the session scoped factory bean. */
    private DynFormFactory getDynFormFactory() {
        return (DynFormFactory) getBean("DynFormFactory");
    }

    

    /** 
     * <p>Callback method that is called whenever a page is navigated to,
     * either directly via a URL, or indirectly via page navigation.
     * Customize this method to acquire resources that will be needed
     * for event handlers and lifecycle methods, whether or not this
     * page is performing post back processing.</p>
     * 
     * <p>Note that, if the current request is a postback, the property
     * values of the components do <strong>not</strong> represent any
     * values submitted with this request.  Instead, they represent the
     * property values that were saved for this view when it was rendered.</p>
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
            log("userWorkQueues Initialization Failure", e);
            throw e instanceof FacesException ? (FacesException) e: new FacesException(e);
        }
        // </editor-fold>
        // Perform application initialization that must complete
        // *after* managed components are initialized
        // TODO - add your own initialization code here
        tabOffered_action();
    }

    /** 
     * <p>Callback method that is called after the component tree has been
     * restored, but before any event processing takes place.  This method
     * will <strong>only</strong> be called on a postback request that
     * is processing a form submit.  Customize this method to allocate
     * resources that will be required in your event handlers.</p>
     */
    public void preprocess() {
    }

    /** 
     * <p>Callback method that is called just before rendering takes place.
     * This method will <strong>only</strong> be called for the page that
     * will actually be rendered (and not, for example, on a page that
     * handled a postback and then navigated to a different page).  Customize
     * this method to allocate resources that will be required for rendering
     * this page.</p>
     */
    public void prerender() {
        getSessionBean().checkLogon();
        msgPanel.show();
        if (_sb.getSourceTab() != null) {
            tabSet.setSelected(_sb.getSourceTab());
            _sb.setSourceTab(null);
        }
        
        if (_sb.isDelegating()) postDelegate();
        else if (_sb.isReallocating()) postReallocate();
        else if (_sb.isWirEdit()) postEditWIR() ;
        
        String selTabName = tabSet.getSelected() ;
        Tab selTab = null;

        if (selTabName == null) {

            // this is the first rendering of the page in this session
            WorkItemRecord wir = _sb.getChosenWIR(WorkQueue.OFFERED) ;
            if (wir != null) ((pfQueueUI) getBean("pfQueueUI")).populateTextBoxes(wir);
            
       //     setRefreshRate(0) ;               // get default refresh rate from web.xml
            tabSet.setSelected("tabOffered");
            selTab = tabOffered;
            tabOffered_action() ;           // default
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
        updateTabHeaders(selTab) ;
        _sb.setActiveTab(tabSet.getSelected());
        _sb.setActivePage(ApplicationBean.PageRef.userWorkQueues);
    }

    /** 
     * <p>Callback method that is called after rendering is completed for
     * this request, if <code>init()</code> was called (regardless of whether
     * or not this was the page that was actually rendered).  Customize this
     * method to release resources acquired in the <code>init()</code>,
     * <code>preprocess()</code>, or <code>prerender()</code> methods (or
     * acquired during execution of an event handler).</p>
     */
    public void destroy() {
    }

    private MessagePanel msgPanel = getSessionBean().getMessagePanel() ;


    public String btnRefresh_action() {
        return null ;
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
            if (action.equals("acceptOffer"))
                _rm.acceptOffer(p, wir);
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
                if (! _sb.isDirty(wir.getID())) {
                    //message about not editing the item
                }
                _rm.checkinItem(p, wir, handle);
                _sb.removeDirtyFlag(wir.getID());
                _sb.resetDynFormParams();
                
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
//        String handle = _sb.getSessionhandle() ;
        WorkItemRecord wir = _sb.getChosenWIR(WorkQueue.STARTED);
//        try {
//            Map<String, FormParameter> params ;
//            if (_sb.isDirty(wir.getID()))
//                params = _sb.getDynFormParams() ;
//            else {
//                params = _rm.getWorkItemParamsInfo(wir, handle) ;
//                _sb.setDynFormParams(params);
//            }
 //           if (params != null) {
                _sb.setDynFormType(ApplicationBean.DynFormType.tasklevel);
                _sb.setSourceTab("tabStarted");

                DynFormFactory df = (DynFormFactory) getBean("DynFormFactory");
                df.setHeaderText("Edit Work Item: " + wir.getID());
                df.setDisplayedWIR(wir);
                df.initDynForm("YAWL 2.0 - Edit Work Item") ;

                return "showDynForm" ;
//            }
//            else {
//                   // no params to view
//            }
//        }
//        catch (Exception e) {}
//        return null ;
    }

    private void postEditWIR() {
        if (_sb.isWirEdit()) {
            WorkItemRecord wir = _sb.getChosenWIR(WorkQueue.STARTED);
            Element data = JDOMUtil.stringToElement(getDynFormFactory().getDataList());

            wir.setUpdatedData(data);
            getApplicationBean().getResourceManager().getWorkItemCache().update(wir) ;
            _sb.setDirtyFlag(wir.getID());

            _sb.setWirEdit(false);
        }

    }


    public String btnComplete_action() {
        return doAction(WorkQueue.STARTED, "complete") ;
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

            btnComplete.setDisabled(! (emptyItem || _sb.isDirty(wir.getID())));
            if (btnComplete.isDisabled())
                btnComplete.setToolTip("The selected workitem needs editing before it can complete");
            else
                btnComplete.setToolTip(null);
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
                                       btnComplete.setDisabled(isEmptyQueue);
                                       break;
            case WorkQueue.SUSPENDED : btnUnsuspend.setDisabled(isEmptyQueue);
        }
    }
}

