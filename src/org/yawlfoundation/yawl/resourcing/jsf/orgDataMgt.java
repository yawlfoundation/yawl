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
import com.sun.rave.web.ui.model.Option;
import com.sun.rave.web.ui.model.UploadedFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yawlfoundation.yawl.resourcing.ResourceManager;
import org.yawlfoundation.yawl.resourcing.datastore.orgdata.DataBackupEngine;
import org.yawlfoundation.yawl.resourcing.datastore.orgdata.ResourceDataSet;

import javax.faces.FacesException;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

/*
 * The backing bean for the YAWL 2.0 org data mgt form
 *
 * @author Michael Adams
 * Date: 10/12/2007
 *
 * Last Date: 09/04/2008
 */

public class orgDataMgt extends AbstractPageBean {

    private int __placeholder;

    private void _init() throws Exception { }

    public orgDataMgt() { }


    // Return reference to scoped data beans
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

        try {
            _init();
        } catch (Exception e) {
            log("userWorkQueues Initialization Failure", e);
            throw e instanceof FacesException ? (FacesException) e: new FacesException(e);
        }
    }

    public void preprocess() { }

    public void destroy() { }


    /*******************************************************************************/

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


    private Tab tabRoles = new Tab();

    public Tab getTabRoles() { return tabRoles; }

    public void setTabRoles(Tab t) { tabRoles = t; }


    private PanelLayout lpRoles = new PanelLayout();

    public PanelLayout getLpRoles() { return lpRoles; }

    public void setLpRoles(PanelLayout pl) { lpRoles = pl; }


    private Tab tabCapability = new Tab();

    public Tab getTabCapability() { return tabCapability; }

    public void setTabCapability(Tab t) { tabCapability = t; }


    private PanelLayout lpCapabilities = new PanelLayout();

    public PanelLayout getLpCapabilities() { return lpCapabilities; }

    public void setLpCapabilities(PanelLayout pl) { lpCapabilities = pl; }


    private Tab tabPosition = new Tab();

    public Tab getTabPosition() { return tabPosition; }

    public void setTabPosition(Tab t) { tabPosition = t; }


    private PanelLayout lpPositions = new PanelLayout();

    public PanelLayout getLpPositions() { return lpPositions; }

    public void setLpPositions(PanelLayout pl) { lpPositions = pl; }


    private Tab tabOrgGroup = new Tab();

    public Tab getTabOrgGroup() { return tabOrgGroup; }

    public void setTabOrgGroup(Tab t) { tabOrgGroup = t; }


    private PanelLayout lpOrgGroups = new PanelLayout();

    public PanelLayout getLpOrgGroups() { return lpOrgGroups; }

    public void setLpOrgGroups(PanelLayout pl) { lpOrgGroups = pl; }


    private Button btnSave = new Button();

    public Button getBtnSave() { return btnSave; }

    public void setBtnSave(Button b) { btnSave = b; }


    private Button btnReset = new Button();

    public Button getBtnReset() { return btnReset; }

    public void setBtnReset(Button b) { btnReset = b; }


    private Button btnClose = new Button();

    public Button getBtnClose() { return btnClose; }

    public void setBtnClose(Button b) { btnClose = b; }


    private Button btnRemove = new Button();

    public Button getBtnRemove() { return btnRemove; }

    public void setBtnRemove(Button b) { btnRemove = b; }


    private Button btnAdd = new Button();

    public Button getBtnAdd() { return btnAdd; }

    public void setBtnAdd(Button b) { btnAdd = b; }


    private Button btnExport = new Button();

    public Button getBtnExport() { return btnExport; }

    public void setBtnExport(Button btn) { btnExport = btn; }


    private Button btnImport = new Button();

    public Button getBtnImport() { return btnImport; }

    public void setBtnImport(Button btn) { btnImport = btn; }


    private Button btnUpload = new Button();

    public Button getBtnUpload() { return btnUpload; }

    public void setBtnUpload(Button b) { btnUpload = b; }


    private Button btnCancelUpload = new Button();

    public Button getBtnCancelUpload() { return btnCancelUpload; }

    public void setBtnCancelUpload(Button b) { btnCancelUpload = b; }


    private StaticText staticText1 = new StaticText();

    public StaticText getStaticText1() { return staticText1; }

    public void setStaticText1(StaticText st) { staticText1 = st; }



    private Upload fileUpload = new Upload();

    public Upload getFileUpload() { return fileUpload; }

    public void setFileUpload(Upload u) { fileUpload = u; }


    private PanelLayout pnlContainer ;

    public PanelLayout getPnlContainer() { return pnlContainer; }

    public void setPnlContainer(PanelLayout pnl) { pnlContainer = pnl; }


    private Meta metaRefresh = new Meta();

    public Meta getMetaRefresh() { return metaRefresh; }

    public void setMetaRefresh(Meta m) { metaRefresh = m; }


    private PanelLayout pnlUpload;

    public PanelLayout getPnlUpload() { return pnlUpload; }

    public void setPnlUpload(PanelLayout pnl) { pnlUpload = pnl; }


    /********************************************************************************/


    public enum AttribType { role, position, capability, orggroup }

    private final SessionBean _sb = getSessionBean();
    private final ResourceManager _rm = getApplicationBean().getResourceManager();
    private final ResourceDataSet orgDataSet = _rm.getOrgDataSet();
    private final MessagePanel msgPanel = _sb.getMessagePanel() ;
    private final pfOrgData innerForm = (pfOrgData) getBean("pfOrgData");
    private final Logger _log = LogManager.getLogger(this.getClass());


    // Callback method that is called just before rendering takes place.
    public void prerender() {
        _sb.checkLogon();
        _sb.setActivePage(ApplicationBean.PageRef.orgDataMgt);

        // abort load if org data isn't currently available
        if (_sb.orgDataIsRefreshing()) return;

        showMessagePanel();

        String selTabName = tabSet.getSelected() ;
        Tab selTab = null;

        if (selTabName == null) {
       //     setRefreshRate(0) ;               // get default refresh rate from web.xml
            tabSet.setSelected("tabRoles");
            selTab = tabRoles;
            _sb.getOrgDataOptions();
            setMode(SessionBean.Mode.edit);
            nullifyChoices();
            tabRoles_action() ;           // default
            setVisibleComponents("tabRoles");
        }
        else {
            if (btnAdd.getText().equals("Add")) {
                innerForm.setCombosToNil();
                innerForm.getTxtName().setText("");
                innerForm.getLblMembers().setText("Members (0)");
                _sb.setOrgDataMembers(null);
            }
            else {
                btnRemove.setDisabled(_sb.getOrgDataOptions().length == 0);                
            }
            
            if (! _sb.getActiveTab().equals(selTabName)) {
                nullifyChoices();
                setMode(SessionBean.Mode.edit);
                setVisibleComponents(selTabName);
            }

            if (selTabName.equals("tabRoles")) {
                tabRoles_action() ;
                selTab = tabRoles;
            }
            else if (selTabName.equals("tabCapability")) {
                tabCapabilities_action() ;
                selTab = tabCapability;
            }
            else if (selTabName.equals("tabPosition")) {
                tabPositions_action() ;
                selTab = tabPosition;
            }
            else if (selTabName.equals("tabOrgGroup")) {
                tabOrgGroups_action() ;
                selTab = tabOrgGroup;
            }
        }
        updateTabHeaders(selTab) ;
        _sb.setActiveTab(tabSet.getSelected());
        _sb.setActivePage(ApplicationBean.PageRef.orgDataMgt);

        if (_sb.isOrgDataItemRemovedFlag()) {
            btnReset_action();
            _sb.setOrgDataItemRemovedFlag(false) ;
        }
        checkDataModsEnabled();
    }



    public String btnRefresh_action() {
        return null ;
    }


    public String btnExport_action() {
        DataBackupEngine exporter = new DataBackupEngine();
        String result = exporter.exportOrgData();
        try {
            if (result != null) {
                FacesContext context = FacesContext.getCurrentInstance();
                HttpServletResponse response =
                     ( HttpServletResponse ) context.getExternalContext().getResponse();
                response.setContentType("text/xml");
                response.setCharacterEncoding("UTF-8");
                response.setHeader("Content-Disposition",
                                   "attachment;filename=\"YAWLOrgDataExport.ybkp\"");
                OutputStream os = response.getOutputStream();
                OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
                osw.write(result);
                osw.flush();
                osw.close();
                FacesContext.getCurrentInstance().responseComplete();
            //    msgPanel.success("Data successfully exported to file 'YAWLOrgDataExport.ybkp'");
            }
            else msgPanel.error("Unable to create export file: malformed xml.");
        }
        catch (IOException ioe) {
            msgPanel.error("Unable to create export file. Please see the log for details.");
        }
        return null ;
    }


    public String btnImport_action() {
        _sb.setOrgDataUploadPanelVisible(true);
        return null ;
    }


    public void fileUpload_processValueChange(ValueChangeEvent event) { }    


    public String btnUpload_action() {
        UploadedFile uploadedFile = fileUpload.getUploadedFile();
        String fileName = uploadedFile.getOriginalName();
        if (fileName.length() > 0) {
            if (fileName.endsWith(".ybkp")) {
                DataBackupEngine importer = new DataBackupEngine();
                String xml;
                try {
                    xml = new String(uploadedFile.getBytes(), "UTF-8");
                }
                catch (UnsupportedEncodingException uee) {
                    xml = uploadedFile.getAsString();
                }
                List<String> result = importer.importOrgData(xml);
                if (! ((result == null) || result.isEmpty())) {
                    if (result.size() == 1) {
                        msgPanel.error(result);
                    }
                    else {
                        _sb.refreshOrgDataParticipantList();
                        msgPanel.success(result);
                    }
                }
                else {
                    msgPanel.error("Data import failed. Please see log file for details.");
                }
            }
            else msgPanel.error(
                "Only exported YAWL Org Data files with an extension of '.ybkp' may be uploaded.");
        }

        _sb.setOrgDataUploadPanelVisible(false);
        return null ;
    }


    public String btnCancelUpload_action() {
        _sb.setOrgDataUploadPanelVisible(false);
        return null ;
    }    

    
    public String btnSave_action() {
        if (innerForm.saveChanges(_sb.getOrgDataChoice()))
            populateForm(getAttribType(_sb.getActiveTab())) ;
        return null;
     }


    public String btnAdd_action() {
        // if 'new', we're in edit mode - move to add mode
        if (btnAdd.getText().equals("New")) {
            setMode(SessionBean.Mode.add);
        }
        else {
            if (innerForm.addNewItem(_sb.getActiveTab())) {
                setMode(SessionBean.Mode.edit);
                msgPanel.success("New item added successfully.");
            }
        }
        return null;
    }


    public String btnReset_action() {

        // if in 'add new' mode, discard inputs and go back to edit mode
        if (btnAdd.getText().equals("Add")) {
            setMode(SessionBean.Mode.edit);
        }
        return null ;
    }


    public String btnRemove_action() {
        String id = _sb.getOrgDataChoice();
        if (id != null) {
            AttribType type = getAttribType(_sb.getActiveTab());
            try {
                switch (type) {
                    case role       : orgDataSet.removeRole(id); break ;
                    case capability : orgDataSet.removeCapability(id); break ;
                    case position   : orgDataSet.removePosition(id); break;
                    case orggroup   : orgDataSet.removeOrgGroup(id);
                }
                innerForm.clearFieldsAfterRemove();
                nullifyChoices();
                msgPanel.success("Chosen item successfully removed.");
            }
            catch (Exception e) {
                msgPanel.error("Could not remove chosen item. See log file for details.");
                _log.error("Handled Exception: Unable to remove resource attribute", e);
            }
            _sb.setOrgDataItemRemovedFlag(true) ;
            _sb.setOrgDataChoice(null);
        }
        return null;
    }

    
    public String tabRoles_action() {
        if (getMode() == SessionBean.Mode.edit) populateForm(AttribType.role);
        _sb.setOrgDataListLabelText("Roles");
        return null;
    }


    public String tabCapabilities_action() {
        if (getMode() == SessionBean.Mode.edit) populateForm(AttribType.capability);
        _sb.setOrgDataListLabelText("Capabilities");
        return null;
    }


    public String tabPositions_action() {
        if (getMode() == SessionBean.Mode.edit) populateForm(AttribType.position);
        _sb.setOrgDataListLabelText("Positions");
        return null;
    }


    public String tabOrgGroups_action() {
        if (getMode() == SessionBean.Mode.edit) populateForm(AttribType.orggroup);
        _sb.setOrgDataListLabelText("Org Groups");
        return null;
    }


    private SessionBean.Mode getMode() {
        return _sb.getOrgMgtMode();
    }

    private void setMode(SessionBean.Mode mode) {
        if (mode == SessionBean.Mode.edit) {
            innerForm.setAddMode(false);
            btnAdd.setText("New");
            btnAdd.setToolTip("Add a new item");
            btnReset.setToolTip("Discard unsaved changes");
            btnSave.setDisabled(false);
            btnRemove.setDisabled(false);
            populateForm(getAttribType(_sb.getActiveTab())) ;            
        }
        else {
            innerForm.setAddMode(true);
            btnAdd.setText("Add");
            btnAdd.setToolTip("Save entered data to create a new " + getActiveAttribText());
            btnReset.setToolTip("Discard data and revert to edit mode");
            btnSave.setDisabled(true);
            btnRemove.setDisabled(true);
            if (getAttribType(_sb.getActiveTab()) == AttribType.orggroup) {
               _sb.setOrgDataGroupItems(innerForm.getOrgGroupTypeOptions());
            }
        }
        _sb.setOrgMgtMode(mode);
    }


    private void setVisibleComponents(String tabName) {
        innerForm.setVisibleComponents(tabName);
    }


    private void nullifyChoices() {
        _sb.setOrgDataChoice(null);
        _sb.setOrgDataBelongsChoice(null);
        _sb.setOrgDataGroupChoice(null);
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
        tabRoles.setStyle("");
        tabCapability.setStyle("");
        tabPosition.setStyle("");
        tabOrgGroup.setStyle("");
        if (selected != null) selected.setStyle("color: #3277ba");
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


    private int populateForm(AttribType aType) {
        int result = -1;                                    // default for empty queue
        if (aType != null) {
            Option[] attribs = _sb.getFullResourceAttributeList(getTabString(aType));
            _sb.setOrgDataOptions(attribs);
            innerForm.getLbxItems().setItems(attribs);
            if ((attribs != null) && (attribs.length > 0)) {
                String id = _sb.getOrgDataChoice();
                if (id == null) id = (String) attribs[0].getValue();
                showItem(id, aType);
                result = attribs.length ;
            }
            else innerForm.clearFields();
        }
        return result ;
    }

    private String getTabString(AttribType type) {
        switch (type) {
            case role       : return "tabRoles";
            case capability : return "tabCapability";
            case position   : return "tabPosition";
            case orggroup   : return "tabOrgGroup";
        }
        return "";
    }


    private AttribType getAttribType(String tabName) {
        if (tabName != null) {
            if (tabName.equals("tabRoles")) return AttribType.role;
            if (tabName.equals("tabCapability")) return AttribType.capability;
            if (tabName.equals("tabPosition")) return AttribType.position;
            if (tabName.equals("tabOrgGroup")) return AttribType.orggroup;
        }
        return null;
    }

    private String getActiveAttribText() {
        String activeTab = _sb.getActiveTab();
        if (activeTab != null) {
            AttribType type = getAttribType(activeTab);
            if (type != null)
                return type.name();
        }
        return "role";       // default 
    }

    private void showItem(String id, AttribType type) {
        innerForm.populateGUI(id, type);
    }


    private void showMessagePanel() {
        body1.setFocus(msgPanel.hasMessage() ? "form1:pfMsgPanel:btnOK001" :
                "form1:pfOrgData:txtName");
        _sb.showMessagePanel();
    }


    private void checkDataModsEnabled() {
        if (! orgDataSet.isExternalOrgDataModsAllowed()) {
            disableButton(btnAdd);
            disableButton(btnRemove);
            disableButton(btnSave);
            disableButton(btnReset);
        }
    }

    private void disableButton(Button b) {
       b.setToolTip("External data modifications are disabled");
       b.setDisabled(true);
    }

}
