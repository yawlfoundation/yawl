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
 */package org.yawlfoundation.yawl.resourcing.jsf;

import com.sun.rave.web.ui.appbase.AbstractFragmentBean;
import com.sun.rave.web.ui.component.*;
import com.sun.rave.web.ui.model.DefaultOptionsList;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;

import javax.faces.FacesException;
import javax.faces.event.ValueChangeEvent;


public class pfQueueUI extends AbstractFragmentBean {
    // <editor-fold defaultstate="collapsed" desc="Creator-managed Component Definition">
    private int __placeholder;
    
    private void _init() throws Exception {
    }

    private Listbox lbxItems = new Listbox();

    public Listbox getLbxItems() {
        return lbxItems;
    }

    public void setLbxItems(Listbox l) {
        this.lbxItems = l;
    }

    private DefaultOptionsList lbxItemsDefaultOptions = new DefaultOptionsList();

    public DefaultOptionsList getLbxItemsDefaultOptions() {
        return lbxItemsDefaultOptions;
    }

    public void setLbxItemsDefaultOptions(DefaultOptionsList dol) {
        this.lbxItemsDefaultOptions = dol;
    }

    private Label lblSpecID = new Label();

    public Label getLblSpecID() {
        return lblSpecID;
    }

    public void setLblSpecID(Label l) {
        this.lblSpecID = l;
    }

    private TextField txtSpecID = new TextField();

    public TextField getTxtSpecID() {
        return txtSpecID;
    }

    public void setTxtSpecID(TextField tf) {
        this.txtSpecID = tf;
    }

    private Label lblCaseID = new Label();

    public Label getLblCaseID() {
        return lblCaseID;
    }

    public void setLblCaseID(Label l) {
        this.lblCaseID = l;
    }

    private Label lblCreated = new Label();

    public Label getLblCreated() {
        return lblCreated;
    }

    public void setLblCreated(Label l) {
        this.lblCreated = l;
    }

    private TextField txtCreated = new TextField();

    public TextField getTxtCreated() {
        return txtCreated;
    }

    public void setTxtCreated(TextField tf) {
        this.txtCreated = tf;
    }

    private TextField txtCaseID = new TextField();

    public TextField getTxtCaseID() {
        return txtCaseID;
    }

    public void setTxtCaseID(TextField tf) {
        this.txtCaseID = tf;
    }

    private TextField txtAge = new TextField();

    public TextField getTxtAge() {
        return txtAge;
    }

    public void setTxtAge(TextField tf) {
        this.txtAge = tf;
    }

    private TextField txtStatus = new TextField();

    public TextField getTxtStatus() {
        return txtStatus;
    }

    public void setTxtStatus(TextField tf) {
        this.txtStatus = tf;
    }

    private TextField txtTaskID = new TextField();

    public TextField getTxtTaskID() {
        return txtTaskID;
    }

    public void setTxtTaskID(TextField tf) {
        this.txtTaskID = tf;
    }

    private Label lblTaskID = new Label();

    public Label getLblTaskID() {
        return lblTaskID;
    }

    public void setLblTaskID(Label l) {
        this.lblTaskID = l;
    }

    private Label lblAge = new Label();

    public Label getLblAge() {
        return lblAge;
    }

    public void setLblAge(Label l) {
        this.lblAge = l;
    }

    private Label lblStatus = new Label();

    public Label getLblStatus() {
        return lblStatus;
    }

    public void setLblStatus(Label l) {
        this.lblStatus = l;
    }

    private Label lblItems = new Label();

    public Label getLblItems() {
        return lblItems;
    }

    public void setLblItems(Label l) {
        this.lblItems = l;
    }


    private Label lblDocumentation = new Label();

    public Label getLblDocumentation() {
        return lblDocumentation;
    }

    public void setLblDocumentation(Label l) {
        lblDocumentation = l;
    }


    private TextArea txtDocumentation = new TextArea();

    public TextArea getTxtDocumentation() {
        return txtDocumentation;
    }

    public void setTxtDocumentation(TextArea ta) {
        txtDocumentation = ta;
    }

    private PanelGroup pnlGroup ;

    public PanelGroup getPnlGroup() { return pnlGroup; }

    public void setPnlGroup(PanelGroup group) { pnlGroup = group; }

    
    // </editor-fold>
    
    public pfQueueUI() {
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
    protected SessionBean getSessionBean() {
        return (SessionBean)getBean("SessionBean");
    }


    /** 
     * <p>Return a reference to the scoped data bean.</p>
     */
    protected ApplicationBean getApplicationBean() {
        return (ApplicationBean)getBean("ApplicationBean");
    }


    public void init() {
        super.init();

        // *Note* - this logic should NOT be modified
        try {
            _init();
        } catch (Exception e) {
            log("pfQueueUI Initialization Failure", e);
            throw e instanceof FacesException ? (FacesException) e: new FacesException(e);
        }
    }

    public void destroy() {
    }


    public void lbxItems_processValueChange(ValueChangeEvent event) {
        getSessionBean().setSourceTabAfterListboxSelection();
    }


    public void txtDocumentation_processValueChange(ValueChangeEvent event) {
        if ((((String) event.getOldValue()).trim().length() > 0) ||
            (((String) event.getNewValue()).trim().length() > 0)) {    

            getSessionBean().updateWIRDoco((String) event.getNewValue());
        }
    }

    
    protected void populateTextBoxes(WorkItemRecord wir) {
        txtCaseID.setText(wir.getCaseID());
        txtSpecID.setText(wordWrap(getSpecStr(wir), 20));
        txtStatus.setText(wir.getStatus());
        String taskName = wir.getTaskName();
        if (taskName == null) taskName = wir.getTaskID();
        txtTaskID.setText(wordWrap(taskName, 20));
        txtDocumentation.setText(wir.getDocumentation());
        txtCreated.setText(wir.getEnablementTime()) ;
        setAgeField(wir);
    }


    protected String getSpecStr(WorkItemRecord wir) {
        return String.format("%s (%s)", wir.getSpecURI(), wir.getSpecVersion());
    }


    protected void clearQueueGUI() {
        getSessionBean().setWorklistOptions(null);
        txtCaseID.setText(" ");
        txtSpecID.setText(" ");
        txtTaskID.setText(" ");
        txtStatus.setText(" ");
        txtCreated.setText(" ");
        txtAge.setText(" ");
        txtDocumentation.setText(" ");
    }

    
    private String wordWrap(String s, int maxLen) {
        String result = "";
        s = s.replaceAll("_", " ");
        while (s.length() > maxLen) {
            int spacePos = s.indexOf(" ");
            if ((spacePos == -1) || (spacePos > maxLen - 1)) {
                result += s.substring(0, maxLen - 1) + "- ";
                s = s.substring(maxLen - 1);
            }
            else {
                result += s.substring(0, spacePos + 1);
                s = s.substring(spacePos + 1);
            }
        }
        result += s;
        return result;
    }


    private void setAgeField(WorkItemRecord wir) {
        long now = System.currentTimeMillis();
        long time;
        String labelText;
        try {
            String expiryStr = wir.getTimerExpiry();
            if (expiryStr != null) {
                time = Long.parseLong(expiryStr) - now;          // a future amt of time
                labelText = "Expires in";
            }
            else {
                time = now - Long.parseLong(wir.getEnablementTimeMs());
                labelText = "Age";
            }
            getSessionBean().setLblAgeText(labelText);
            txtAge.setText(getApplicationBean().formatAge(time));
        }
        catch (NumberFormatException nfe) {
            txtAge.setText("<unavailable>");
        }
    }

}
