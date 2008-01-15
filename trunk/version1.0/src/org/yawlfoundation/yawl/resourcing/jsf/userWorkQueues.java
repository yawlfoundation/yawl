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

import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.resourcing.WorkQueue;
import org.yawlfoundation.yawl.resourcing.rsInterface.WorkQueueGateway;
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.jdom.Element;

import javax.faces.FacesException;
import javax.faces.context.ExternalContext;

import java.util.*;
import java.io.IOException;

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
        if (getSessionBean().getSourceTab() != null) {
            tabSet.setSelected(getSessionBean().getSourceTab());
            getSessionBean().setSourceTab(null);
        }
        
        if (getSessionBean().isDelegating()) postDelegate();
        else if (getSessionBean().isWirEdit()) postEditWIR() ;
        
        String selTabName = tabSet.getSelected() ;
        Tab selTab = null;

        if (selTabName == null) {

            // this is the first rendering of the page in this session
            WorkItemRecord wir = getSessionBean().getChosenWIR(WorkQueue.OFFERED) ;
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
        getSessionBean().setActivePage("userWorkQueues");
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


    public String btnStart_action() {
        return doAction(WorkQueue.ALLOCATED, "start") ;
    }


    public String btnDeallocate_action() {
        return doAction(WorkQueue.ALLOCATED, "deallocate") ;
    }


    public String btnDelegate_action() {
        String pid = getSessionBean().getParticipant().getID();
        WorkQueueGateway wqg = getGateway();
        Set<Participant> underlings = wqg.getReportingToParticipant(pid);

        if (underlings != null) {
            // build the option list
            Option[] options = new Option[underlings.size()];
            int i = 0 ;
            for (Participant p : underlings) {
                options[i++] = new Option(p.getID(), p.getFullName());
            }
            getSessionBean().setSelectUserListOptions(options);
            getSessionBean().setUserListFormHeaderText("Delegate workitem to:") ;
            return "userSelect" ;
        }
        else {
            // message no underlings
            return null ;
        }
    }

    private void postDelegate() {
        WorkQueueGateway wqg = getGateway();
        SessionBean sb = getSessionBean();
        if (sb.isDelegating()) {
            Participant pFrom = sb.getParticipant();
            String userIDTo = sb.getSelectUserListChoice() ;        // this is the p-id
            Participant pTo =  wqg.getParticipant(userIDTo) ;
            WorkItemRecord wir = sb.getChosenWIR(WorkQueue.ALLOCATED);
            String handle = sb.getSessionhandle() ;

            try {
                wqg.delegateItem(pFrom, pTo, wir, handle);
                // message successful
            }
            catch (Exception e) {
               // show connection error or timeout
            }
            sb.setDelegating(false);
            ApplicationBean ab = getApplicationBean() ;
        }
        
    }


    public String btnSkip_action() {
        ExternalContext externalContext = getFacesContext().getExternalContext();
        if (externalContext != null) {
            Map s = externalContext.getSessionMap() ;
            Map a = externalContext.getApplicationMap();
            System.out.println(s) ;
            System.out.println(a) ;
        }



  //      return doAction(WorkQueue.ALLOCATED, "skip") ;
        return null;
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


    public String btnAccept_action() {
        return doAction(WorkQueue.OFFERED, "acceptOffer") ;
    }

    private String doAction(int queueType, String action) {
        Participant p = getSessionBean().getParticipant();
        WorkItemRecord wir = getSessionBean().getChosenWIR(queueType);
        String handle = getSessionBean().getSessionhandle() ;
        WorkQueueGateway wqg = getGateway() ;
        try {
            if (action.equals("acceptOffer"))
                wqg.acceptOffer(p, wir, handle);
            else if (action.equals("deallocate"))
                wqg.deallocateItem(p, wir, handle);
            else if (action.equals("skip"))
                wqg.skipItem(p, wir, handle);
            else if (action.equals("start"))
                wqg.startItem(p, wir, handle);
            else if (action.equals("pile"))
                wqg.pileItem(p, wir, handle);
            else if (action.equals("suspend"))
                wqg.suspendItem(p, wir, handle);
            else if (action.equals("unsuspend"))
                wqg.unsuspendItem(p, wir, handle);
            else if (action.equals("complete")) {
                if (! getSessionBean().isDirty(wir.getID())) {
                    //message about not editing the item
                }
                wqg.completeItem(p, wir, handle);
                getSessionBean().removeDirtyFlag(wir.getID());
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
        // TODO: Process the button click action. Return value is a navigation
        // case name where null will return to the same page.
        Participant pFrom = getSessionBean().getParticipant();
        Participant pTo = null ;     //todo - get to part
        boolean stateful = false; //todo - get stateful/stateless
        String handle = getSessionBean().getSessionhandle() ;
        WorkItemRecord wir = getSessionBean().getChosenWIR(WorkQueue.ALLOCATED);
        try {
            getGateway().reallocateItem(pFrom, pTo, wir, stateful, handle);
            return null;
        }
        catch (Exception e) {
           // show connection error or timeout
            return "loginPage" ;
        }        
    }

    public String btnView_action() {
        WorkQueueGateway wqg = getGateway() ;
        String handle = getSessionBean().getSessionhandle() ;
        WorkItemRecord wir = getSessionBean().getChosenWIR(WorkQueue.STARTED);
        try {
            Map<String, FormParameter> params = wqg.getWorkItemParams(wir, handle) ;
            if (params != null) {
                SessionBean sb = getSessionBean();
                sb.setDynFormHeaderText("Edit Work Item '" + wir.getID() + "'" );
                sb.setDynFormParams(params);
                sb.setDynFormLevel("item");
                sb.setSourceTab("tabStarted");
                sb.initDynForm(new ArrayList<FormParameter>(params.values()), "Edit Work Item") ;
                return "showDynForm" ;
            }
            else {
                   // no params to view
            }
        }
        catch (Exception e) {}
        return null ;
    }

    private void postEditWIR() {
        if (getSessionBean().isWirEdit()) {
            Map<String, FormParameter> paramMap = getSessionBean().getDynFormParams();
            if (! paramMap.isEmpty()) {
                WorkItemRecord wir = getSessionBean().getChosenWIR(WorkQueue.STARTED);
                Element data = new Element(getGateway().getDecompID(wir)) ;
                for (FormParameter param : paramMap.values()) {
                    Element child = new Element(param.getName());
                    child.setText(param.getValue());
                    data.addContent(child);
                }
                wir.setUpdatedData(data);
                getGateway().updateWIRCache(wir) ;
            }
            getSessionBean().setWirEdit(false);
        }

    }

    private WorkQueueGateway getGateway() {
        return getApplicationBean().getWorkQueueGateway();
    }

    public String btnComplete_action() {
        return doAction(WorkQueue.STARTED, "complete") ;
    }

    private void updateTabHeaders(Tab selected) {
        tabOffered.setStyle("");
        tabAllocated.setStyle("");
        tabStarted.setStyle("");
        tabSuspended.setStyle("");
        if (selected != null) selected.setStyle("color: blue");
        
        int[] itemCount = new int[4] ;
        for (int queue = WorkQueue.OFFERED; queue <= WorkQueue.SUSPENDED; queue++)
            itemCount[queue] = getSessionBean().getQueueSize(queue) ;
        tabOffered.setText(String.format("Offered (%d)", itemCount[WorkQueue.OFFERED]));
        tabAllocated.setText(String.format("Allocated (%d)", itemCount[WorkQueue.ALLOCATED]));
        tabStarted.setText(String.format("Started (%d)", itemCount[WorkQueue.STARTED]));
        tabSuspended.setText(String.format("Suspended (%d)", itemCount[WorkQueue.SUSPENDED]));
    }

    public String tabOffered_action() {
        int itemCount = populateQueue(WorkQueue.OFFERED);
//        if (itemCount > -1)
//            tabOffered.setText(String.format("Offered (%d)", itemCount));
//        else
//            tabOffered.setText("Offered");
        return null;
    }


    public String tabAllocated_action() {
        int itemCount = populateQueue(WorkQueue.ALLOCATED);
//        if (itemCount > -1)
//            tabAllocated.setText(String.format("Allocated (%d)", itemCount));
//        else
//            tabAllocated.setText("Allocated");
        return null;
    }


    public String tabStarted_action() {
        int itemCount = populateQueue(WorkQueue.STARTED);
//        if (itemCount > -1)
//            tabStarted.setText(String.format("Started (%d)", itemCount));
//        else
//            tabStarted.setText("Started");
        return null;
    }


    public String tabSuspended_action() {
        int itemCount = populateQueue(WorkQueue.SUSPENDED);
//        if (itemCount > -1)
//            tabSuspended.setText(String.format("Suspended (%d)", itemCount));
//        else
//            tabSuspended.setText("Suspended");
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
        Set<WorkItemRecord> queue = getSessionBean().getQueue(queueType);
        pfQueueUI itemsSubPage = (pfQueueUI) getBean("pfQueueUI");
        Listbox lbx = itemsSubPage.getLbxItems();
        itemsSubPage.clearQueueGUI();
        if (queue != null) {
            if (!queue.isEmpty()) {
                WorkItemRecord firstWir = addItemsToListOptions(queue) ;

                WorkItemRecord choice = getSessionBean().getChosenWIR(queueType) ;
                if (choice == null) {
                    lbx.setSelected(firstWir.getID());
                    itemsSubPage.populateTextBoxes(firstWir) ;
                }
                else {
                    lbx.setSelected(choice.getID());
                    itemsSubPage.populateTextBoxes(choice) ;
                }
            }
            return queue.size() ;
        }    
        else {
            return -1 ;    // null queue
        }
    }


    private WorkItemRecord addItemsToListOptions(Set<WorkItemRecord> queue) {
        Option[] options = new Option[queue.size()] ;
        WorkItemRecord result = null;
        SortedSet<WorkItemRecord> qSorted =
                               new TreeSet<WorkItemRecord>(new WorkItemAgeComparator());
        qSorted.addAll(queue);
        int i = 0 ;
        for (WorkItemRecord wir : qSorted) {
            if (i==0) {
                getSessionBean().setChosenWIR(wir);          // return first listed
                result = wir;
            }
            options[i++] = new Option(wir.getID()) ;
        }
        getSessionBean().setWorklistOptions(options);
        return result ;
    }


}

