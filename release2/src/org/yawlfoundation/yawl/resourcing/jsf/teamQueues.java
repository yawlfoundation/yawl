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
import org.yawlfoundation.yawl.resourcing.QueueSet;
import org.yawlfoundation.yawl.resourcing.ResourceManager;
import org.yawlfoundation.yawl.resourcing.jsf.comparator.WorkItemAgeComparator;
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.resourcing.resource.Position;

import javax.faces.FacesException;
import javax.faces.context.ExternalContext;
import java.io.IOException;
import java.util.HashSet;
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

public class teamQueues extends AbstractPageBean {

    // REQUIRED AND/OR IMPLEMENTED ABSTRACT PAGE BEAN METHODS //

    private int __placeholder;

    private void _init() throws Exception { }

    public teamQueues() { }


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
            log("teamQueues Initialization Failure", e);
            throw e instanceof FacesException ? (FacesException) e: new FacesException(e);
        }
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


    private PanelLayout lpQueues = new PanelLayout();

    public PanelLayout getLpQueues() { return lpQueues; }

    public void setLpQueues(PanelLayout pl) { lpQueues = pl; }


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


    private PanelLayout rbGroup = new PanelLayout();

    public PanelLayout getRbGroup() { return rbGroup; }

    public void setRbGroup(PanelLayout pl) { rbGroup = pl; }


    private RadioButton rbTeam ;

    public RadioButton getRbTeam() { return rbTeam; }

    public void setRbTeam(RadioButton rb) {rbTeam = rb; }


    private RadioButton rbOrgGroup ;

    public RadioButton getRbOrgGroup() { return rbOrgGroup; }

    public void setRbOrgGroup(RadioButton rb) {rbOrgGroup = rb; }


    private Meta metaRefresh = new Meta();

    public Meta getMetaRefresh() { return metaRefresh; }

    public void setMetaRefresh(Meta m) { metaRefresh = m; }


    private PanelLayout pnlContainer ;

    public PanelLayout getPnlContainer() { return pnlContainer; }

    public void setPnlContainer(PanelLayout pnl) { pnlContainer = pnl; }


    /********************************************************************************/

    // SPECIFIC DELARATIONS AND METHODS //

    private final SessionBean _sb = getSessionBean();
    private final ResourceManager _rm = getApplicationBean().getResourceManager();

    private enum qSet { team, orgGroup }

    /**
     * Overridden method that is called immediately before the page is rendered
     */
    public void prerender() {
        _sb.checkLogon();
        _sb.setActivePage(ApplicationBean.PageRef.teamQueues);
        _sb.showMessagePanel();
        
        ((pfQueueUI) getBean("pfQueueUI")).clearQueueGUI();
        if (enableRadioButtons()) {

            // get team or group members (if any)
            qSet set = _sb.isTeamRBSelected() ? qSet.team : qSet.orgGroup ;
            WorkItemRecord wir = buildMembersList(set, _sb.getWorklistChoice());
            showWorkItem(wir);
        }
    }


    // BUTTON AND TAB ACTIONS //

    public String btnRefresh_action() {
        return null ;
    }


    private WorkItemRecord buildMembersList(qSet setType, String selectedID) {
        WorkItemRecord result = null ;
        Participant p = _sb.getParticipant();
        Set<Participant> underlings = new HashSet<Participant>();
        if (setType == qSet.team) {
            Set<Participant> reportingTo = _rm.getOrgDataSet().getParticipantsReportingTo(p.getID());
            if (reportingTo != null)
                underlings.addAll(reportingTo);
        }
        else {
            Set<Position> posSet = p.getPositions();
            for (Position pos : posSet) {
                underlings.addAll(_rm.getOrgDataSet().getOrgGroupMembers(pos.getOrgGroup()));
            }
        }

        Option[] options = null;
        if (! underlings.isEmpty()) {
            SortedSet<WorkItemRecord> wirSet =
                    new TreeSet<WorkItemRecord>(new WorkItemAgeComparator());

            for (Participant pu : underlings) {
                QueueSet qSet = pu.getWorkQueues();
                if (qSet != null)
                    wirSet.addAll(qSet.getWorklistedQueues().getAll());
            }
            // build the option list
            options = new Option[wirSet.size()];
            int i = 0 ;
            for (WorkItemRecord wir : wirSet) {
                if (i==0) result = wir ;                 // return first wir by default
                options[i++] = new Option(wir.getID());
                if ((selectedID != null) && selectedID.equals(wir.getID()))
                    result = wir ;                       // return matching wir if any
            }
        }

        _sb.setWorklistOptions(options);
        _sb.setChosenWIR(result);
        if (result != null) _sb.populateAdminQueueAssignedList(result) ;
        return result;
    }



    // force a refresh of this page //
    public void forceRefresh() {
        ExternalContext externalContext = getFacesContext().getExternalContext();
        if (externalContext != null) {
            try {
                externalContext.redirect("teamQueues.jsp");
            }
            catch (IOException ioe) {}
        }
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
            metaRefresh.setContent(rate + "; url=./teamQueues.jsp");
        }
    }


    private void showWorkItem(WorkItemRecord wir) {
        if (wir != null) {
            pfQueueUI itemsSubPage = (pfQueueUI) getBean("pfQueueUI");
            Listbox lbx = itemsSubPage.getLbxItems();
            lbx.setSelected(wir.getID());
            itemsSubPage.populateTextBoxes(wir) ;
            _sb.setResourceState(wir.getResourceStatus());
            if (txtResourceState != null) {
                txtResourceState.setText(wir.getResourceStatus());
            }
        }
        else {
            if (cbbAssignedTo != null) {
                cbbAssignedTo.setItems(null);
                _sb.setAdminQueueAssignedList(new Option[0]);
                txtResourceState.setText("");
            }
        }
    }


    private boolean enableRadioButtons() {
        if (_sb.getTeamRBDisabled()) {
            _sb.setTeamRBSelected(false);
            _sb.setOrgGroupRBSelected(true);
        }
        if (_sb.getOrgGroupRBDisabled()) {
            _sb.setOrgGroupRBSelected(false);               // team is true by default                              
        }
        return ! (_sb.getTeamRBDisabled() && _sb.getOrgGroupRBDisabled());
    }

}