/*
 * userWorkQueues.java
 *
 * Created on October 23, 2007, 11:18 AM
 * Copyright adamsmj
 */
package org.yawlfoundation.yawl.resourcing.jsf;

import org.yawlfoundation.yawl.resourcing.WorkQueue;
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import com.sun.rave.web.ui.appbase.AbstractPageBean;
import com.sun.rave.web.ui.component.*;

import javax.faces.FacesException;
import java.util.Set;

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
    
    // </editor-fold>


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
        // TODO: Process the button click action. Return value is a navigation
        // case name where null will return to the same page.
        
        return null;
    }


    public String btnDeallocate_action() {
        // TODO: Process the button click action. Return value is a navigation
        // case name where null will return to the same page.
        
        return null;
    }


    public String btnDelegate_action() {
        // TODO: Process the button click action. Return value is a navigation
        // case name where null will return to the same page.
        
        return null;
    }


    public String btnSkip_action() {
        // TODO: Process the button click action. Return value is a navigation
        // case name where null will return to the same page.
        
        return null;
    }


    public String btnPile_action() {
        // TODO: Process the button click action. Return value is a navigation
        // case name where null will return to the same page.
        
        return null;
    }


    public String btnAccept_action() {
        // TODO: Process the button click action. Return value is a navigation
        // case name where null will return to the same page.
        
        return null;
    }


    public String btnUnsuspend_action() {
        // TODO: Process the button click action. Return value is a navigation
        // case name where null will return to the same page.
        
        return null;
    }


    public String btnSuspend_action() {
        // TODO: Process the button click action. Return value is a navigation
        // case name where null will return to the same page.
        
        return null;
    }


    public String btnStateless_action() {
        // TODO: Process the button click action. Return value is a navigation
        // case name where null will return to the same page.
        
        return null;
    }

    private int populateQueue(int queueType) {
        Participant p = getSessionBean().getParticipant();
        Set<WorkItemRecord> queue = p.getWorkQueues().getQueuedWorkItems(queueType);
        pfQueueUI itemsInsert = (pfQueueUI) getBean("pfQueueUI");
        Listbox lbx = itemsInsert.getLbxItems();
     //   lbx.
        return -1 ;
    }

    public String tabOffered_action() {
        int itemCount = populateQueue(WorkQueue.OFFERED);
        getTabStarted().setText(String.format("Offered (%s)", itemCount));
        return null;
    }


    public String tabAllocated_action() {
        populateQueue(WorkQueue.ALLOCATED);
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





    public String btnView_action() {
        // TODO: Process the button click action. Return value is a navigation
        // case name where null will return to the same page.
        
        return null;
    }


    public String btnComplete_action() {
        // TODO: Process the button click action. Return value is a navigation
        // case name where null will return to the same page.
        
        return null;
    }
}

