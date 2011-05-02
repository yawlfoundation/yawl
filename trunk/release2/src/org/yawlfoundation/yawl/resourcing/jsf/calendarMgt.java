/*
 * Copyright (c) 2004-2011 The YAWL Foundation. All rights reserved.
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
import org.yawlfoundation.yawl.resourcing.ResourceManager;
import org.yawlfoundation.yawl.resourcing.calendar.CalendarEntry;
import org.yawlfoundation.yawl.resourcing.calendar.CalendarException;
import org.yawlfoundation.yawl.resourcing.calendar.CalendarRow;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.faces.FacesException;
import javax.faces.component.UIColumn;
import javax.faces.component.html.HtmlDataTable;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.event.ValueChangeEvent;
import java.util.GregorianCalendar;

/**
 *  Backing bean for the calendar mgt page.
 *
 *  @author Michael Adams
 *  BPM Group, QUT Australia
 */

public class calendarMgt extends AbstractPageBean {

    private int __placeholder;

    private void _init() throws Exception { }

    /** Constructor */
    public calendarMgt() { }


     // Return references to scoped data beans //

    protected RequestBean getRequestBean() {
        return (RequestBean)getBean("RequestBean");
    }

    protected SessionBean getSessionBean() {
        return (SessionBean)getBean("SessionBean");
    }

    protected ApplicationBean getApplicationBean() {
        return (ApplicationBean)getBean("ApplicationBean");
    }


    public void init() {
        super.init();

        // *Note* - this logic should NOT be modified
        try {
            _init();
        } catch (Exception e) {
            log("caseMgt Initialization Failure", e);
            throw e instanceof FacesException ? (FacesException) e: new FacesException(e);
        }
    }

    public void preprocess() { }

    public void destroy() { }


    /*********************************************************************************/

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


    private Button btnAdd = new Button();

    public Button getBtnAdd() { return btnAdd; }

    public void setBtnAdd(Button b) { btnAdd = b; }


    private Button btnUpdate = new Button();

    public Button getBtnUpdate() { return btnUpdate; }

    public void setBtnUpdate(Button b) { btnUpdate = b; }


    private PanelLayout layoutPanel2 = new PanelLayout();

    public PanelLayout getLayoutPanel2() { return layoutPanel2; }

    public void setLayoutPanel2(PanelLayout pl) { layoutPanel2 = pl; }


    private HtmlDataTable dataTable1 = new HtmlDataTable();

    public HtmlDataTable getDataTable1() { return dataTable1; }

    public void setDataTable1(HtmlDataTable hdt) { dataTable1 = hdt; }


    private UIColumn colName = new UIColumn();

    public UIColumn getColName() { return colName; }

    public void setColName(UIColumn uic) { colName = uic; }


    private HtmlOutputText colNameRows = new HtmlOutputText();

    public HtmlOutputText getColNameRows() { return colNameRows; }

    public void setColNameRows(HtmlOutputText hot) { colNameRows = hot; }


    private HtmlOutputText colNameHeader = new HtmlOutputText();

    public HtmlOutputText getColNameHeader() { return colNameHeader; }

    public void setColNameHeader(HtmlOutputText hot) { colNameHeader = hot; }


    private UIColumn colStartTime = new UIColumn();

    public UIColumn getColStartTime() { return colStartTime; }

    public void setColStartTime(UIColumn uic) { colStartTime = uic; }


    private HtmlOutputText colStartTimeRows = new HtmlOutputText();

    public HtmlOutputText getColStartTimeRows() { return colStartTimeRows; }

    public void setColStartTimeRows(HtmlOutputText hot) { colStartTimeRows = hot; }


    private HtmlOutputText colStartTimeHeader = new HtmlOutputText();

    public HtmlOutputText getColStartTimeHeader() { return colStartTimeHeader; }

    public void setColStartTimeHeader(HtmlOutputText hot) { colStartTimeHeader = hot; }


    private UIColumn colEndTime = new UIColumn();

    public UIColumn getColEndTime() { return colEndTime; }

    public void setColEndTime(UIColumn uic) { colEndTime = uic; }


    private HtmlOutputText colEndTimeRows = new HtmlOutputText();

    public HtmlOutputText getColEndTimeRows() { return colEndTimeRows; }

    public void setColEndTimeRows(HtmlOutputText hot) { colEndTimeRows = hot; }


    private HtmlOutputText colEndTimeHeader = new HtmlOutputText();

    public HtmlOutputText getColEndTimeHeader() { return colEndTimeHeader; }

    public void setColEndimeHeader(HtmlOutputText hot) { colEndTimeHeader = hot; }


    private UIColumn colStatus = new UIColumn();

    public UIColumn getColStatus() { return colStatus; }

    public void setColStatus(UIColumn uic) { colStatus = uic; }


    private HtmlOutputText colStatusRows = new HtmlOutputText();

    public HtmlOutputText getColStatusRows() { return colStatusRows; }

    public void setColStatusRows(HtmlOutputText hot) { colStatusRows = hot; }


    private HtmlOutputText colStatusHeader = new HtmlOutputText();

    public HtmlOutputText getColStatusHeader() { return colStatusHeader; }

    public void setColStatusHeader(HtmlOutputText hot) { colStatusHeader = hot; }


    private UIColumn colWorkload = new UIColumn();

    public UIColumn getColWorkload() { return colWorkload; }

    public void setColWorkload(UIColumn uic) { colWorkload = uic; }


    private HtmlOutputText colWorkloadRows = new HtmlOutputText();

    public HtmlOutputText getColWorkloadRows() { return colWorkloadRows; }

    public void setColWorkloadRows(HtmlOutputText hot) { colWorkloadRows = hot; }


    private HtmlOutputText colWorkloadHeader = new HtmlOutputText();

    public HtmlOutputText getColWorkloadHeader() { return colWorkloadHeader; }

    public void setColWorkloadHeader(HtmlOutputText hot) { colWorkloadHeader = hot; }


    private UIColumn colComment = new UIColumn();

    public UIColumn getColComment() { return colComment; }

    public void setColComment(UIColumn uic) { colComment = uic; }


    private HtmlOutputText colCommentRows = new HtmlOutputText();

    public HtmlOutputText getColCommentRows() { return colCommentRows; }

    public void setColCommentRows(HtmlOutputText hot) { colCommentRows = hot; }


    private HtmlOutputText colCommentHeader = new HtmlOutputText();

    public HtmlOutputText getCoCommentHeader() { return colCommentHeader; }

    public void setColCommentHeader(HtmlOutputText hot) { colCommentHeader = hot; }


    private UIColumn colSBar = new UIColumn();

    public UIColumn getColSBar() { return colSBar; }

    public void setColSBar(UIColumn uic) { colSBar = uic; }


    private HiddenField hdnRowIndex = new HiddenField();

    public HiddenField getHdnRowIndex() { return hdnRowIndex; }

    public void setHdnRowIndex(HiddenField hf) { hdnRowIndex = hf; }


    private Script script1 = new Script();

    public Script getScript1() { return script1; }

    public void setScript1(Script s) { script1 = s; }


    private PanelLayout pnlContainer ;

    public PanelLayout getPnlContainer() { return pnlContainer; }

    public void setPnlContainer(PanelLayout pnl) { pnlContainer = pnl; }


    private PanelGroup pnlGroup ;

    public PanelGroup getPnlGroup() { return pnlGroup; }

    public void setPnlGroup(PanelGroup group) { pnlGroup = group; }


    private Button btnDelete = new Button();

    public Button getBtnDelete() { return btnDelete; }

    public void setBtnDelete(Button b) { btnDelete = b; }


    private Button btnYesterday = new Button();

    public Button getBtnYesterday() { return btnYesterday; }

    public void setBtnYesterday(Button b) { btnYesterday = b; }


    private Button btnTomorrow = new Button();

    public Button getBtnTomorrow() { return btnTomorrow; }

    public void setBtnTomorrow(Button b) { btnTomorrow = b; }


    private Calendar calComponent = new Calendar();

    public Calendar getCalComponent() { return calComponent; }

    public void setCalComponent(Calendar cal) { calComponent = cal; }


    public Label lblFilter = new Label();

    public Label getLblFilter() { return lblFilter; }

    public void setLblFilter(Label lbl) { lblFilter = lbl; }


    private DropDown cbbFilter = new DropDown();

    public DropDown getCbbFilter() { return cbbFilter; }

    public void setCbbFilter(DropDown cbb) { cbbFilter = cbb; }


    public Label lblResource = new Label();

    public Label getLblResource() { return lblResource; }

    public void setLblResource(Label lbl) { lblResource = lbl; }


    private DropDown cbbResource = new DropDown();

    public DropDown getCbbResource() { return cbbResource; }

    public void setCbbResource(DropDown cbb) { cbbResource = cbb; }


    private PanelLayout calPanel = new PanelLayout();

    public PanelLayout getCalPanel() { return calPanel; }

    public void setCalPanel(PanelLayout pl) { calPanel = pl; }


    private StaticText headingStart = new StaticText();

    public StaticText getHeadingStart() { return headingStart; }

    public void setHeadingStart(StaticText st) { headingStart = st; }


    private StaticText headingEnd = new StaticText();

    public StaticText getHeadingEnd() { return headingEnd; }

    public void setHeadingEnd(StaticText st) { headingEnd = st; }


    private StaticText headingName = new StaticText();

    public StaticText getHeadingName() { return headingName; }

    public void setHeadingName(StaticText st) { headingName = st; }


    private StaticText headingStatus = new StaticText();

    public StaticText getHeadingStatus() { return headingStatus; }

    public void setHeadingStatus(StaticText st) { headingStatus = st; }


    private StaticText headingWorkload = new StaticText();

    public StaticText getHeadingWorkload() { return headingWorkload; }

    public void setHeadingWorkload(StaticText st) { headingWorkload = st; }


    private StaticText headingComments = new StaticText();

    public StaticText getHeadingComments() { return headingComments; }

    public void setHeadingComments(StaticText st) { headingComments = st; }


    private PanelLayout editPanel = new PanelLayout();

    public PanelLayout getEditPanel() { return editPanel; }

    public void setEditPanel(PanelLayout pl) { editPanel = pl; }


    private PanelLayout upperPanel = new PanelLayout();

    public PanelLayout getUpperPanel() { return upperPanel; }

    public void setUpperPanel(PanelLayout pl) { upperPanel = pl; }


    public Label lblStart = new Label();

    public Label getLblStart() { return lblStart; }

    public void setLblStart(Label lbl) { lblStart = lbl; }


    public Label lblEnd = new Label();

    public Label getLblEnd() { return lblEnd; }

    public void setLblEnd(Label lbl) { lblEnd = lbl; }


    public Label lblUntil = new Label();

    public Label getLblUntil() { return lblUntil; }

    public void setLblUntil(Label lbl) { lblUntil = lbl; }


    public Label lblWorkload = new Label();

    public Label getLblWorkload() { return lblWorkload; }

    public void setLblWorkload(Label lbl) { lblWorkload = lbl; }


    public Label lblComments = new Label();

    public Label getLblComments() { return lblComments; }

    public void setLblComments(Label lbl) { lblComments = lbl; }


    private Calendar calDuration = new Calendar();

    public Calendar getCalDuration() { return calDuration; }

    public void setCalDuration(Calendar cal) { calDuration = cal; }


    private TextField txtStartTime = new TextField();

    public TextField getTxtStartTime() { return txtStartTime; }

    public void setTxtStartTime(TextField tf) { txtStartTime = tf; }


    private TextField txtEndTime = new TextField();

    public TextField getTxtEndTime() { return txtEndTime; }

    public void setTxtEndTime(TextField tf) { txtEndTime = tf; }


    private TextField txtWorkload = new TextField();

    public TextField getTxtWorkload() { return txtWorkload; }

    public void setTxtWorkload(TextField tf) { txtWorkload = tf; }


    private TextField txtComments = new TextField();

    public TextField getTxtComments() { return txtComments; }

    public void setTxtComments(TextField tf) { txtComments = tf; }


    private Button btnClear = new Button();

    public Button getBtnClear() { return btnClear; }

    public void setBtnClear(Button b) { btnClear = b; }


    private String btnAddText = "Add";

    public String getBtnAddText() { return btnAddText; }

    public void setBtnAddText(String text) { btnAddText = text; }


    /*******************************************************************************/

    private final ResourceManager _rm = ResourceManager.getInstance() ;
    private final SessionBean _sb = getSessionBean();
    private final MessagePanel _msgPanel = _sb.getMessagePanel();
    private final GregorianCalendar _greg = new GregorianCalendar();

    private enum Mode {Add, Edit}

    public void cbbFilter_processValueChange(ValueChangeEvent event) {
        _sb.setCalFilterSelection((String) event.getNewValue());
        nullifyResourceCombo();
    }


    public void cbbResource_processValueChange(ValueChangeEvent event) {
        _sb.setCalResourceSelection((String) event.getNewValue()); 
    }


    /**
     * Overridden method that is called immediately before the page is rendered
     */
    public void prerender() {
        _sb.checkLogon();
        setActivePage();
        showMessagePanel();
        processMode();
        setFilter();
        activateButtons();
    }

    private void processMode() {
        boolean isEditMode = (getMode() == Mode.Edit);
        btnAddText = isEditMode ? "Save" : "Add";
        btnDelete.setDisabled(isEditMode);
        btnUpdate.setDisabled(isEditMode);
        btnTomorrow.setDisabled(isEditMode);
        btnYesterday.setDisabled(isEditMode);
        calComponent.setDisabled(isEditMode);
        cbbFilter.setDisabled(isEditMode);
        cbbResource.setDisabled(isEditMode);
    }


    public String btnRefresh_action() {
        return null ;
    }


    public String btnAdd_action() {
        if (validateFields()) {
            long startTime = getTime((String) txtStartTime.getText());
            long endTime = getTime((String) txtEndTime.getText());
            if (startTime < endTime) {
                int workload = getWorkloadValue();
                String comment = (String) txtComments.getText();
                if (getMode() == Mode.Edit) {                  // update existing entry
                    CalendarEntry entry = getSelectedEntry();
                    if (entry != null) {
                        entry.setStartTime(startTime);
                        entry.setEndTime(endTime);
                        entry.setWorkload(workload);
                        entry.setComment(comment);
                        _rm.getCalendar().updateEntry(entry);
                    }
                    else {
                        _msgPanel.error("Could not retrieve selected row to update.");
                    }
                }
                else {                                         // add new entry
                    try {
                        _sb.addCalendarEntry(startTime, endTime, workload, comment);
                    }
                    catch (CalendarException ce) {
                        _msgPanel.error("Could not add entry: " + ce.getMessage());
                    }
                }
                clearFields();
            }
            else _msgPanel.error("End time must come after start time.");
        }
        return null;
    }


    public String btnUpdate_action() {
        try {
            Integer selectedRowIndex = new Integer((String) hdnRowIndex.getValue());
            CalendarRow row = _sb.getSelectedCalendarRow(selectedRowIndex);
            if (row != null) {
                btnAddText = "Save";
                txtStartTime.setText(row.getStartTimeAsString());
                txtEndTime.setText(row.getEndTimeAsString());
                txtWorkload.setText(row.getWorkload());
                txtComments.setText(row.getComment());
                setMode(Mode.Edit);
            }
        }
        catch (NumberFormatException nfe) {
            _msgPanel.warn("Please select a row to edit.");
        }
        return null;
    }


    public String btnDelete_action() {
        try {
            Integer selectedRowIndex = new Integer((String) hdnRowIndex.getValue());
            String result = _sb.removeCalendarRow(selectedRowIndex);
            if (result.startsWith("<fail")) {
                _msgPanel.error(result);
            }
        }
        catch (NumberFormatException nfe) {
            _msgPanel.error("No entry selected to remove.");
        }
        catch (Exception e) {
            _msgPanel.error("Could not remove entry. See logs for details");
            e.printStackTrace();
        }
        return null;        
    }


    public String btnClear_action() {
        clearFields();
        return null ;
    }

    public String btnYesterday_action() {
        incDate(-1);
        return null;
    }


    public String btnTomorrow_action() {
        incDate(1);
        return null;
    }


    public void incDate(int amt) {
        _greg.setTimeInMillis(calComponent.getSelectedDate().getTime());
        _greg.add(GregorianCalendar.DAY_OF_YEAR, amt);
        _sb.setSelectedCalMgtDate(_greg.getTime());        
    }


    private void activateButtons() {
    }


    private CalendarEntry getSelectedEntry() {
        CalendarRow row = _sb.getSelectedCalendarRow();
        return (row != null) ? row.toCalendarEntry() : null;
    }


    private void setFilter() {
        String selectedFilter = _sb.getCalFilterSelection();
        cbbFilter.setSelected(selectedFilter);
        cbbResource.setDisabled(! selectedFilter.startsWith("Selected"));
        if (cbbResource.isDisabled()) {
            _sb.setCalResourceSelection(null);
        }
        setAddModeInputs(selectedFilter);
    }


    private void setAddModeInputs(String selectedFilter) {
        boolean disable = selectedFilter.equals("Unfiltered");
        txtStartTime.setDisabled(disable);
        txtEndTime.setDisabled(disable);
        txtWorkload.setDisabled(disable);
        txtComments.setDisabled(disable);
        btnAdd.setDisabled(disable);
     }


    public Option[] getCalendarMgtFilterComboItems() {
        Option[] options = new Option[6];
        options[0] = new Option("Unfiltered");
        options[1] = new Option("All Resources");
        options[2] = new Option("All Participants");
        options[3] = new Option("All Assets");
        options[4] = new Option("Selected Participant");
        options[5] = new Option("Selected Asset");
        return options;
    }


    private void setActivePage() {
        _sb.setActivePage(ApplicationBean.PageRef.calendarMgt);
    }

    
    private void showMessagePanel() {
        if (_msgPanel.hasMessage()) body1.setFocus("form1:pfMsgPanel:btnOK001");
        _sb.showMessagePanel();
    }


    private Mode getMode() {
        return _sb.isAddCalendarRowMode() ? Mode.Add : Mode.Edit;
    }

    private void setMode(Mode mode) {
        _sb.setAddCalendarRowMode(mode == Mode.Add) ;
    }


    private boolean validateFields() {
        boolean valid;
        String start = (String) txtStartTime.getText();
        valid = validateTimeField(start, "Start Time");
        String end = (String) txtEndTime.getText();
        valid = valid && validateTimeField(end, "End Time");
        int workload = getWorkloadValue();
        return valid && validateWorkloadField(workload);
    }


    private boolean validateTimeField(String time, String label) {
        if (! time.matches("(([01]?\\d)|(2[0-3])):([0-5]\\d)")) {
            _msgPanel.error("Invalid " + label);
            return false;
        }
        return true;
    }

    private boolean validateWorkloadField(int load) {
        if ((load < 1) || (load > 100)) {
            _msgPanel.error("Invalid workload: must be between 1 and 100");
            return false;
        }
        return true;
    }


    private void nullifyResourceCombo() {
        cbbResource.setSelected(null);
        cbbResource.setItems(null);
        _sb.setCalResourceSelection(null);
    }


    private void clearFields() {
        txtStartTime.setText("");
        txtEndTime.setText("");
        txtWorkload.setText("");
        txtComments.setText("");
        calDuration.setValue("");
        _sb.setSelectedCalendarRowIndex(-1);
        setMode(Mode.Add);
    }


    private long getTime(String timeStr) {
        long midnight = calComponent.getSelectedDate().getTime();
        String[] hrsAndMins = timeStr.split(":");
        long hrs = StringUtil.strToLong(hrsAndMins[0], 0);
        long mins = StringUtil.strToLong(hrsAndMins[1], 0);
        return midnight + ((hrs * 60 + mins) * 60000);             // add time in msecs 
    }


    private int getWorkloadValue() {
        Object load = txtWorkload.getText();
        if (load instanceof Integer) {
            return (Integer) load;
        }
        return StringUtil.strToInt((String) load, 100);
    }

}