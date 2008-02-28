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
import org.yawlfoundation.yawl.resourcing.jsf.comparator.WorkItemAgeComparator;

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
public class adminQueues extends AbstractPageBean {
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

    private Tab tabUnOffered = new Tab();

    public Tab getTabUnOffered() {
        return tabUnOffered;
    }

    public void setTabUnOffered(Tab t) {
        this.tabUnOffered = t;
    }

    private PanelLayout lpUnOffered = new PanelLayout();

    public PanelLayout getLpUnOffered() {
        return lpUnOffered;
    }

    public void setLpUnOffered(PanelLayout pl) {
        this.lpUnOffered = pl;
    }

    private Tab tabWorklisted = new Tab();

    public Tab getTabWorklisted() {
        return tabWorklisted;
    }

    public void setTabWorklisted(Tab t) {
        this.tabWorklisted = t;
    }

    private PanelLayout lpWorklisted = new PanelLayout();

    public PanelLayout getLpWorklisted() {
        return lpWorklisted;
    }

    public void setLpWorklisted(PanelLayout pl) {
        this.lpWorklisted = pl;
    }

    private Button btnOffer = new Button();

    public Button getBtnOffer() {
        return btnOffer;
    }

    public void setBtnOffer(Button b) {
        this.btnOffer = b;
    }


    private Button btnStart = new Button();

    public Button getBtnStart() {
        return btnStart;
    }

    public void setBtnStart(Button b) {
        this.btnStart = b;
    }

    private Button btnReallocate = new Button();

    public Button getBtnReallocate() {
        return btnReallocate;
    }

    public void setBtnReallocate(Button b) {
        this.btnReallocate = b;
    }

    private Button btnAllocate = new Button();

    public Button getBtnAllocate() {
        return btnAllocate;
    }

    public void setBtnAllocate(Button b) {
        this.btnAllocate = b;
    }

    private Button btnReoffer = new Button();

    public Button getBtnReoffer() {
        return btnReoffer;
    }

    public void setBtnReoffer(Button b) {
        this.btnReoffer = b;
    }

    private Button btnRestart = new Button();

    public Button getBtnRestart() {
        return btnRestart;
    }

    public void setBtnRestart(Button b) {
        this.btnRestart = b;
    }

    private Label lblAssignedTo ;

    private Label lblResourceState ;

//    private TextField txtAssignedTo ;

    private TextField txtResourceState ;

    public Label getLblAssignedTo() {
        return lblAssignedTo;
    }

    public void setLblAssignedTo(Label lblAssignedTo) {
        this.lblAssignedTo = lblAssignedTo;
    }

    public Label getLblResourceState() {
        return lblResourceState;
    }

    public void setLblResourceState(Label lblResourceState) {
        this.lblResourceState = lblResourceState;
    }

//    public TextField getTxtAssignedTo() {
//        return txtAssignedTo;
//    }
//
//    public void setTxtAssignedTo(TextField txtAssignedTo) {
//        this.txtAssignedTo = txtAssignedTo;
//    }

    public TextField getTxtResourceState() {
        return txtResourceState;
    }

    public void setTxtResourceState(TextField txtResourceState) {
        this.txtResourceState = txtResourceState;
    }

    private DropDown cbbAssignedTo ;

    public DropDown getCbbAssignedTo() {
        return cbbAssignedTo;
    }

    public void setCbbAssignedTo(DropDown cbbAssignedTo) {
        this.cbbAssignedTo = cbbAssignedTo;
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
    public adminQueues() {
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
        tabUnOffered_action();
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

        // 
        getSessionBean().performAdminQueueAction();


        if (getSessionBean().getSourceTab() != null) {
            tabSet.setSelected(getSessionBean().getSourceTab());
            getSessionBean().setSourceTab(null);
        }

        String selTabName = tabSet.getSelected() ;
        Tab selTab = null;

        if (selTabName == null) {

            // this is the first rendering of the page in this session
            WorkItemRecord wir = getSessionBean().getChosenWIR(WorkQueue.UNOFFERED) ;
            if (wir != null) ((pfQueueUI) getBean("pfQueueUI")).populateTextBoxes(wir);

       //     setRefreshRate(0) ;               // get default refresh rate from web.xml
            tabSet.setSelected("tabUnOffered");
            selTab = tabUnOffered;
            tabUnOffered_action() ;           // default
        }
        else {
            if (selTabName.equals("tabUnOffered")) {
                tabUnOffered_action() ;
                selTab = tabUnOffered;
            }
            else if (selTabName.equals("tabWorklisted")) {
                tabWorklisted_action() ;
                selTab = tabWorklisted;
            }
        }
        updateTabHeaders(selTab) ;
        getSessionBean().setActiveTab(tabSet.getSelected());
        getSessionBean().setActivePage("adminQueues");
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


    public String btnOffer_action() {
        return showUserList("Offer") ;
    }

    public String btnAllocate_action() {
        return showUserList("Allocate") ;
    }

    public String btnStart_action() {
        return showUserList("Start") ;
    }


    public String btnReoffer_action() {
        return showUserList("Reoffer") ;
    }

    public String btnReallocate_action() {
        return showUserList("Reallocate") ;
    }

    public String btnRestart_action() {
        return showUserList("Restart") ;
    }


    public String showUserList(String action) {
        SessionBean sb = getSessionBean();
        sb.setAdminQueueAction(action) ; 
        sb.setSelectUserListOptions(sb.getOrgDataParticipantList());
        sb.setUserListFormHeaderText(action + " selected workitem(s) to:") ;
        sb.setNavigateTo("showAdminQueues");
        return "userSelect" ;
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


    private void updateTabHeaders(Tab selected) {
        tabUnOffered.setStyle("");
        tabWorklisted.setStyle("");
        if (selected != null) selected.setStyle("color: #3277ba");
        tabUnOffered.setText(String.format("Unoffered (%d)",
                     getSessionBean().getQueueSize(WorkQueue.UNOFFERED)));
        tabWorklisted.setText(String.format("Worklisted (%d)",
                     getSessionBean().getQueueSize(WorkQueue.WORKLISTED)));
    }

    public String tabUnOffered_action() {
        int itemCount = populateQueue(WorkQueue.UNOFFERED);
        return null;
    }


    public String tabWorklisted_action() {
        int itemCount = populateQueue(WorkQueue.WORKLISTED);
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
            metaRefresh.setContent(rate + "; url=./adminQueues.jsp");
        }
    }

    /******************************************************************************/

    private int populateQueue(int queueType) {
        int result = -1;                                    // default for empty queue
        Set<WorkItemRecord> queue = getSessionBean().refreshQueue(queueType);
        ((pfQueueUI) getBean("pfQueueUI")).clearQueueGUI();

        if (queue != null) {
            if (!queue.isEmpty()) {
                WorkItemRecord firstWir = addItemsToListOptions(queue) ;
                WorkItemRecord choice = getSessionBean().getChosenWIR(queueType) ;
                if (choice == null) choice = firstWir ;
                showWorkItem(choice, queueType);
            }
            result = queue.size() ;
        }
        return result ;
    }

    private void showWorkItem(WorkItemRecord wir, int queueType) {
        pfQueueUI itemsSubPage = (pfQueueUI) getBean("pfQueueUI");
        Listbox lbx = itemsSubPage.getLbxItems();
        lbx.setSelected(wir.getID());
        itemsSubPage.populateTextBoxes(wir) ;
        if (queueType == WorkQueue.WORKLISTED) {
            getSessionBean().populateAdminQueueAssignedList(wir) ;
            cbbAssignedTo.setItems(getSessionBean().getAdminQueueAssignedList());
            lblAssignedTo.setText(getSessionBean().getAssignedToText());
            txtResourceState.setText(wir.getResourceStatus());
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
            if (wir != null) {
                if (i==0) {
                    getSessionBean().setChosenWIR(wir);          // return first listed
                    result = wir;
                }
                options[i++] = new Option(wir.getID()) ;
            }
        }
        getSessionBean().setWorklistOptions(options);
        return result ;
    }


}