/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
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
import org.yawlfoundation.yawl.exceptions.YAWLException;

import javax.faces.FacesException;
import javax.faces.event.ValueChangeEvent;
import java.util.List;

/**
 * Backing bean for the secondary resources mgt form
 *
 * @author Michael Adams
 *
 * Date: 31/01/2008
 * Last Date: 16/03/2008
 */

public class secResourceMgt extends AbstractPageBean {
    private int __placeholder;

    private void _init() throws Exception { }

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

    private PanelLayout pnlParticipants = new PanelLayout();

    public PanelLayout getPnlParticipants() {
        return pnlParticipants;
    }

    public void setPnlParticipants(PanelLayout pl) {
        this.pnlParticipants = pl;
    }

    private PanelLayout pnlRoles = new PanelLayout();

    public PanelLayout getPnlRoles() {
        return pnlRoles;
    }

    public void setPnlRoles(PanelLayout pl) {
        this.pnlRoles = pl;
    }

    private PanelLayout pnlNHResources = new PanelLayout();

    public PanelLayout getPnlNHResources() {
        return pnlNHResources;
    }

    public void setPnlNHResources(PanelLayout pl) {
        this.pnlNHResources = pl;
    }

    private PanelLayout pnlNHCategories = new PanelLayout();

    public PanelLayout getPnlNHCategories() {
        return pnlNHCategories;
    }

    public void setPnlNHCategories(PanelLayout pl) {
        this.pnlNHCategories = pl;
    }

    private PanelLayout pnlSelected = new PanelLayout();

    public PanelLayout getPnlSelected() {
        return pnlSelected;
    }

    public void setPnlSelected(PanelLayout pl) {
        this.pnlSelected = pl;
    }

    private StaticText sttParticipants = new StaticText();

    public StaticText getSttParticipants() {
        return sttParticipants;
    }

    public void setSttParticipants(StaticText st) {
        this.sttParticipants = st;
    }

    private StaticText sttRoles = new StaticText();

    public StaticText getSttRoles() {
        return sttRoles;
    }

    public void setSttRoles(StaticText st) {
        this.sttRoles = st;
    }

    private StaticText sttNHResources = new StaticText();

    public StaticText getSttNHResources() {
        return sttNHResources;
    }

    public void setSttNHResources(StaticText st) {
        this.sttNHResources = st;
    }

    private StaticText sttNHCategories = new StaticText();

    public StaticText getSttNHCategories() {
        return sttNHCategories;
    }

    public void setSttNHCategories(StaticText st) {
        this.sttNHCategories = st;
    }

    private StaticText sttSelected = new StaticText();

    public StaticText getSttSelected() {
        return sttSelected;
    }

    public void setSttSelected(StaticText st) {
        this.sttSelected = st;
    }

    private StaticText sttTitle = new StaticText();

    public StaticText getSttTitle() {
        return sttTitle;
    }

    public void setSttTitle(StaticText st) {
        this.sttTitle = st;
    }

    private Listbox lbxParticipants = new Listbox() ;

    public Listbox getLbxParticipants() {
        return lbxParticipants;
    }

    public void setLbxParticipants(Listbox lb) {
        this.lbxParticipants = lb;
    }

    private Listbox lbxRoles = new Listbox();

    public Listbox getLbxRoles() {
        return lbxRoles;
    }

    public void setLbxRoles(Listbox lb) {
        this.lbxRoles = lb;
    }

    private Listbox lbxNHResources = new Listbox();

    public Listbox getLbxNHResources() {
        return lbxNHResources;
    }

    public void setLbxNHResources(Listbox lb) {
        this.lbxNHResources = lb;
    }

    private Listbox lbxNHCategories = new Listbox();

    public Listbox getLbxNHCategories() {
        return lbxNHCategories;
    }

    public void setLbxNHCategories(Listbox lb) {
        this.lbxNHCategories = lb;
    }

    private Listbox lbxSelected = new Listbox();

    public Listbox getLbxSelected() {
        return lbxSelected;
    }

    public void setLbxSelected(Listbox lb) {
        this.lbxSelected = lb;
    }


    private Button btnRemove = new Button();

    public Button getBtnRemove() {
        return btnRemove;
    }

    public void setBtnRemove(Button b) {
        this.btnRemove = b;
    }

    private Button btnDone = new Button();

    public Button getBtnDone() {
        return btnDone;
    }

    public void setBtnDone(Button b) {
        this.btnDone = b;
    }

    private Button btnSave = new Button();

    public Button getBtnSave() {
        return btnSave;
    }

    public void setBtnSave(Button b) {
        this.btnSave = b;
    }

    private Button btnCheck = new Button();

    public Button getBtnCheck() {
        return btnCheck;
    }

    public void setBtnCheck(Button b) {
        this.btnCheck = b;
    }

    private PanelLayout pnlContainer ;

    public PanelLayout getPnlContainer() { return pnlContainer; }

    public void setPnlContainer(PanelLayout pnl) { pnlContainer = pnl; }



    /**
     * <p>Construct a new Page bean instance.</p>
     */
    public secResourceMgt() {
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
     * <p>Return a reference to the scoped data bean.</p>
     */
    protected RequestBean getRequestBean() {
        return (RequestBean)getBean("RequestBean");
    }


    public void init() {
        super.init();
        try {
            _init();
        } catch (Exception e) {
            log("participantData Initialization Failure", e);
            throw e instanceof FacesException ? (FacesException) e: new FacesException(e);
        }
    }

    public void preprocess() { }

    public void destroy() { }


    /**************************************************************************/

    private final SessionBean _sb = getSessionBean();
    private final MessagePanel msgPanel = _sb.getMessagePanel() ;


    public void prerender() {
        _sb.checkLogon();
        _sb.setActivePage(ApplicationBean.PageRef.secResMgt);
        _sb.refreshSelectedSecondaryResourcesOptions();
        enableButtons();
        showMessagePanel();
    }


    public void lbxParticipants_processValueChange(ValueChangeEvent event) {
        _sb.addSecondaryParticipant((String) event.getNewValue());
        lbxParticipants.setSelected(null);
    }

    public void lbxRoles_processValueChange(ValueChangeEvent event) {
        _sb.addSecondaryRole((String) event.getNewValue());
        lbxRoles.setSelected(null);
    }

    public void lbxNHResources_processValueChange(ValueChangeEvent event) {
        _sb.addSecondaryNHResource((String) event.getNewValue());
        lbxNHResources.setSelected(null);
    }

    public void lbxNHCategories_processValueChange(ValueChangeEvent event) {
        _sb.addSecondaryNHCategory((String) event.getNewValue());
        lbxNHCategories.setSelected(null);
    }

    public void lbxSelected_processValueChange(ValueChangeEvent event) {

    }

    public String btnDone_action() {
        return "showAdminQueues";
    }

    public String btnRemove_action() {
        if (_sb.getSelectedSecondaryResource() == null) {
            msgPanel.error("Please select a resource to remove.");
        }
        else if (_sb.removeSelectedSecondaryResource()) {
            _sb.setSelectedSecondaryResource(null);
        }
        return null;
    }

    public String btnCheck_action() {
        try {
            List<String> problems = _sb.checkSelectedSecondaryResources();
            if (problems.isEmpty()) {
                msgPanel.success("All selected secondary resources are currently available.");
            }
            else {
                msgPanel.info(problems);
            }
        }
        catch (YAWLException ye) {
            msgPanel.error(ye.getMessage());
        }
        return null;
    }

    public String btnSave_action() {
        _sb.saveSelectedSecondaryResources();
        msgPanel.success("Selected Secondary Resources saved for workitem.");
        return null;
    }


    private void enableButtons() {
        boolean noSelections = (_sb.getSelectedSecondaryResources().length == 0);
        btnCheck.setDisabled(noSelections);
        btnSave.setDisabled(noSelections);
        btnRemove.setDisabled(noSelections || (_sb.getSelectedSecondaryResource() == null));
    }


    private void showMessagePanel() {
        body1.setFocus(msgPanel.hasMessage() ? "form1:pfMsgPanel:btnOK001" :
                "form1:lbxSelected");
        _sb.showMessagePanel();
    }

}