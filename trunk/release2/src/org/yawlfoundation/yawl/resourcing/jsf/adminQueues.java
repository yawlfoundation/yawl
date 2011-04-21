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

import com.sun.rave.web.ui.appbase.AbstractPageBean;
import com.sun.rave.web.ui.component.*;
import com.sun.rave.web.ui.model.Option;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.resourcing.ResourceManager;
import org.yawlfoundation.yawl.resourcing.WorkQueue;
import org.yawlfoundation.yawl.resourcing.jsf.comparator.WorkItemAgeComparator;

import javax.faces.FacesException;
import javax.faces.context.ExternalContext;
import java.io.IOException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/*
 * The backing bean for the YAWL 2.0 admin work queues form
 *
 * @author Michael Adams
 * Date: 23/10/2007
 *
 * Last Date: 30/05/2008
 */

public class adminQueues extends AbstractPageBean {

    // REQUIRED AND/OR IMPLEMENTED ABSTRACT PAGE BEAN METHODS //

    private int __placeholder;

    private void _init() throws Exception { }

    public adminQueues() { }


    //Return references to scoped data beans //

    protected RequestBean getRequestBean() {
        return (RequestBean)getBean("RequestBean");
    }

    protected ApplicationBean getApplicationBean() {
        return (ApplicationBean)getBean("ApplicationBean");
    }

    protected SessionBean getSessionBean() {
        return (SessionBean)getBean("SessionBean");
    }


    public void init() {
        super.init();
        // *Note* - this logic should NOT be modified
        try {
            _init();
        } catch (Exception e) {
            log("userWorkQueues Initialization Failure", e);
            throw e instanceof FacesException ? (FacesException) e: new FacesException(e);
        }
//        tabUnOffered_action();
    }


    public void preprocess() { }

    public void destroy() { }


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


    private Tab tabUnOffered = new Tab();

    public Tab getTabUnOffered() { return tabUnOffered; }

    public void setTabUnOffered(Tab t) { tabUnOffered = t; }


    private PanelLayout lpUnOffered = new PanelLayout();

    public PanelLayout getLpUnOffered() { return lpUnOffered; }

    public void setLpUnOffered(PanelLayout pl) { lpUnOffered = pl; }


    private Tab tabWorklisted = new Tab();

    public Tab getTabWorklisted() { return tabWorklisted; }

    public void setTabWorklisted(Tab t) { tabWorklisted = t; }


    private PanelLayout lpWorklisted = new PanelLayout();

    public PanelLayout getLpWorklisted() { return lpWorklisted; }

    public void setLpWorklisted(PanelLayout pl) { lpWorklisted = pl; }


    private Button btnOffer = new Button();

    public Button getBtnOffer() { return btnOffer; }

    public void setBtnOffer(Button b) { btnOffer = b;}


    private Button btnStart = new Button();

    public Button getBtnStart() { return btnStart; }

    public void setBtnStart(Button b) { btnStart = b; }


    private Button btnReallocate = new Button();

    public Button getBtnReallocate() { return btnReallocate; }

    public void setBtnReallocate(Button b) { btnReallocate = b; }


    private Button btnAllocate = new Button();

    public Button getBtnAllocate() { return btnAllocate; }

    public void setBtnAllocate(Button b) { this.btnAllocate = b; }


    private Button btnReoffer = new Button();

    public Button getBtnReoffer() { return btnReoffer; }

    public void setBtnReoffer(Button b) { btnReoffer = b; }


    private Button btnRestart = new Button();

    public Button getBtnRestart() { return btnRestart; }

    public void setBtnRestart(Button b) { btnRestart = b; }


    private Label lblAssignedTo ;

    public Label getLblAssignedTo() { return lblAssignedTo; }

    public void setLblAssignedTo(Label l) { lblAssignedTo = l; }


    private Label lblResourceState ;

    public Label getLblResourceState() { return lblResourceState; }

    public void setLblResourceState(Label l) { lblResourceState = l; }


    private TextField txtResourceState ;

    public TextField getTxtResourceState() { return txtResourceState; }

    public void setTxtResourceState(TextField t) { txtResourceState = t; }


    private DropDown cbbAssignedTo ;

    public DropDown getCbbAssignedTo() { return cbbAssignedTo; }

    public void setCbbAssignedTo(DropDown dd) { cbbAssignedTo = dd; }


    private Checkbox cbxDirectToMe = new Checkbox();

    public Checkbox getCbxDirectToMe() { return cbxDirectToMe; }

    public void setCbxDirectToMe(Checkbox c) { cbxDirectToMe = c; }


    private Button btnSynch = new Button();

    public Button getBtnSynch() { return btnSynch; }

    public void setBtnSynch(Button btn) { btnSynch = btn; }


    private Meta metaRefresh = new Meta();

    public Meta getMetaRefresh() { return metaRefresh; }

    public void setMetaRefresh(Meta m) { metaRefresh = m; }


    private PanelLayout pnlContainer ;

    public PanelLayout getPnlContainer() { return pnlContainer; }

    public void setPnlContainer(PanelLayout pnl) { pnlContainer = pnl; }


    /********************************************************************************/

    // SPECIFIC DELARATIONS AND METHODS //

    private final SessionBean _sb = getSessionBean();
    private final MessagePanel _msgPanel = _sb.getMessagePanel();

    /**
     * Overridden method that is called immediately before the page is rendered
     */
    public void prerender() {
        _sb.checkLogon();
        _sb.setActivePage(ApplicationBean.PageRef.adminQueues);

        // hide 'direct to me' checkbox if logged on with 'admin' userid
        cbxDirectToMe.setVisible(_sb.getParticipant() != null);

        // take appropriate postback action if required
        if (! _sb.performAdminQueueAction()) {
           _msgPanel.error("Could not complete workitem action." +
                    " Please see the log files for details.");               
        }
        showMessagePanel();

        // goto last selected tab
        if (_sb.getSourceTab() != null) {
            tabSet.setSelected(_sb.getSourceTab());
            _sb.setSourceTab(null);
        }

        String selTabName = tabSet.getSelected() ;
        Tab selTab = null;

        if (selTabName == null) {
            selTab = initDefaultTab();
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
            else {
                selTab = initDefaultTab();                
            }
        }
        updateTabHeaders(selTab) ;
        _sb.setActiveTab(tabSet.getSelected());
    }


    private Tab initDefaultTab() {
        // this is the first rendering of the page in this session
        WorkItemRecord wir = _sb.getChosenWIR(WorkQueue.UNOFFERED) ;
        if (wir != null) ((pfQueueUI) getBean("pfQueueUI")).populateTextBoxes(wir);

   //     setRefreshRate(0) ;               // get default refresh rate from web.xml
        tabSet.setSelected("tabUnOffered");
        tabUnOffered_action() ;           // default
        return tabUnOffered;
    }


    // BUTTON AND TAB ACTIONS //

    public String btnRefresh_action() {
        return null ;
    }

    public String btnSynch_action() {
        getApplicationBean().synch();
        return null ;
    }

    public String btnOffer_action() {
        return selectParticipant("Offer") ;
    }

    public String btnAllocate_action() {
        return selectParticipant("Allocate") ;
    }

    public String btnStart_action() {
        return selectParticipant("Start") ;
    }


    public String btnReoffer_action() {
        return selectParticipant("Reoffer") ;
    }

    public String btnReallocate_action() {
        return selectParticipant("Reallocate") ;
    }

    public String btnRestart_action() {
        return selectParticipant("Restart") ;
    }

    public String tabUnOffered_action() {
        populateQueue(WorkQueue.UNOFFERED);
        return null;
    }

    public String tabWorklisted_action() {
        populateQueue(WorkQueue.WORKLISTED);
        return null;
    }


    // initialise and show the user select form //
    public String selectParticipant(String action) {
        String nextPage = null;
        _sb.setAdminQueueAction(action) ;
        if (cbxDirectToMe.isChecked()) {
            _sb.setDirectToMeChoice(_sb.getParticipant().getID());
        }
        else {
            _sb.setDirectToMeChoice(null);
            if (ResourceManager.getInstance().getOrgDataSet().getParticipantCount() == 0) {
                _msgPanel.error("Unable to assign workitem: " +
                    "Missing or empty organisational database. Please check and, if " +
                    "necessary, add some participants via the 'User Mgt' form, then " +
                    "return to this form to assign the workitem.");
            }
            else {
                _sb.setBlankStartOfParticipantList(false);
                _sb.setSelectUserListOptions(_sb.getOrgDataParticipantList());
                _sb.configureSelectUserListBox(action);
                _sb.setUserListFormHeaderText(action + " selected workitem(s) to:") ;
                _sb.setNavigateTo("showAdminQueues");
                nextPage = "userSelect" ;
            }
        }
        return nextPage;
    }


    // force a refresh of this page //
    public void forceRefresh() {
        ExternalContext externalContext = getFacesContext().getExternalContext();
        if (externalContext != null) {
            try {
                externalContext.redirect("adminQueues.jsp");
            }
            catch (IOException ioe) {}
        }
    }


    // Highlight selected tab's name and show queue's item count on each tab
    private void updateTabHeaders(Tab selected) {
        tabUnOffered.setStyle("");
        tabWorklisted.setStyle("");
        if (selected != null) selected.setStyle("color: #3277ba");
        tabUnOffered.setText(String.format("Unoffered (%d)",
                     _sb.getQueueSize(WorkQueue.UNOFFERED)));
        tabWorklisted.setText(String.format("Worklisted (%d)",
                     _sb.getQueueSize(WorkQueue.WORKLISTED)));
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
        Set<WorkItemRecord> queue = _sb.refreshQueue(queueType);
        boolean queueHasItems = (! ((queue == null) || queue.isEmpty()));
        ((pfQueueUI) getBean("pfQueueUI")).clearQueueGUI();
        ((pfQueueUI) getBean("pfQueueUI")).getTxtDocumentation().setReadOnly(! queueHasItems);

        if (queueHasItems) {
            addItemsToListOptions(queue, _sb.getChosenWIR(queueType)) ;
            showWorkItem(_sb.getChosenWIR(queueType), queueType);
            result = queue.size() ;
        }
        else {
            _sb.setWorklistChoice(null);
            if (queueType == WorkQueue.WORKLISTED) clearWorklistedFields();
            disableAllButtons(queueType);
        }
        
        return result ;
    }

    private void showWorkItem(WorkItemRecord wir, int queueType) {
        pfQueueUI itemsSubPage = (pfQueueUI) getBean("pfQueueUI");
        Listbox lbx = itemsSubPage.getLbxItems();
        lbx.setSelected(wir.getID());
        itemsSubPage.populateTextBoxes(wir) ;
        if (queueType == WorkQueue.WORKLISTED) {
            _sb.populateAdminQueueAssignedList(wir) ;
            cbbAssignedTo.setItems(_sb.getAdminQueueAssignedList());
            lblAssignedTo.setText(_sb.getAssignedToText());
            txtResourceState.setText(wir.getResourceStatus());
            processButtonEnablement(wir);
        }
        else enableUnofferedButtons(! wir.hasStatus(WorkItemRecord.statusSuspended));
    }

    
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

    /**
     * Enable or disable buttons based on the status of the selected workitem
     * @param wir the selected workitem
     */
    private void processButtonEnablement(WorkItemRecord wir) {
        btnReoffer.setDisabled(! getApplicationBean().canReoffer(wir));
        String status = wir.getResourceStatus();
        if (status != null) {
            btnReallocate.setDisabled(status.equals(WorkItemRecord.statusResourceOffered));
            btnRestart.setDisabled(status.equals(WorkItemRecord.statusResourceOffered) ||
                                   status.equals(WorkItemRecord.statusResourceAllocated));
        }
        else {                                      // null = no status; so play it safe
            btnReallocate.setDisabled(true);
            btnRestart.setDisabled(true);
        }
    }


    private void disableAllButtons(int queueType) {
        if (queueType == WorkQueue.UNOFFERED) {
            btnOffer.setDisabled(true);
            btnAllocate.setDisabled(true);
            btnStart.setDisabled(true);
        }
        else {
            btnReoffer.setDisabled(true);
            btnReallocate.setDisabled(true);
            btnRestart.setDisabled(true);
        }
    }


    private void enableUnofferedButtons(boolean liveStatus) {
        btnOffer.setDisabled(false);
        btnAllocate.setDisabled(false);
        btnStart.setDisabled(! liveStatus);
    }


    private void clearWorklistedFields() {
        cbbAssignedTo.setItems(null);
        lblAssignedTo.setText("Assigned To");
        txtResourceState.setText(" ");        
    }

    private void showMessagePanel() {
        body1.setFocus(_msgPanel.hasMessage() ? "form1:pfMsgPanel:btnOK001" :
                "form1:pfQueueUI:lbxItems");
        _sb.showMessagePanel();
    }

}
