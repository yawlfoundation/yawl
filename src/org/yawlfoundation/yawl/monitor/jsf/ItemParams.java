/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.monitor.jsf;

import com.sun.rave.web.ui.appbase.AbstractPageBean;
import com.sun.rave.web.ui.component.*;
import org.yawlfoundation.yawl.monitor.sort.ParamOrder;
import org.yawlfoundation.yawl.monitor.sort.TableSorter;
import org.yawlfoundation.yawl.resourcing.jsf.MessagePanel;

import javax.faces.FacesException;
import javax.faces.component.UIColumn;
import javax.faces.component.html.HtmlDataTable;
import javax.faces.component.html.HtmlOutputText;

/**
 *  Backing bean for the active cases page.
 *
 *  @author Michael Adams
 *  10/12/2009
 */

public class ItemParams extends AbstractPageBean {

    private int __placeholder;

    private void _init() throws Exception { }

    /** Constructor */
    public ItemParams() { }


     // Return references to scoped data beans //

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
            log("caseItems Initialization Failure", e);
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


    private StaticText stEngineLogHeading = new StaticText();

    public StaticText getStEngineLogHeading() { return stEngineLogHeading; }

    public void setStEngineLogHeading(StaticText st) { stEngineLogHeading = st; }


    private StaticText stResourceLogHeading = new StaticText();

    public StaticText getStResourceLogHeading() { return stResourceLogHeading; }

    public void setStResourceLogHeading(StaticText st) { stResourceLogHeading = st; }


    private StaticText staticText = new StaticText();

    public StaticText getStaticText() { return staticText; }

    public void setStaticText(StaticText st) { staticText = st; }


    private PanelLayout layoutPanel = new PanelLayout();

    public PanelLayout getLayoutPanel() { return layoutPanel; }

    public void setLayoutPanel(PanelLayout pl) { layoutPanel = pl; }


    private HtmlDataTable dataTable = new HtmlDataTable();

    public HtmlDataTable getDataTable() { return dataTable; }

    public void setDataTable(HtmlDataTable hdt) { dataTable = hdt; }


    private UIColumn colName = new UIColumn();

    public UIColumn getColName() { return colName; }

    public void setColName(UIColumn uic) { colName = uic; }


    private HtmlOutputText colNameRows = new HtmlOutputText();

    public HtmlOutputText getColNameRows() { return colNameRows; }

    public void setColNameRows(HtmlOutputText hot) { colNameRows = hot; }


    private UIColumn colDataType = new UIColumn();

    public UIColumn getColDataType() { return colDataType; }

    public void setColDataType(UIColumn uic) { colDataType = uic; }


    private HtmlOutputText colDataTypeRows = new HtmlOutputText();

    public HtmlOutputText getColDataTypeRows() { return colDataTypeRows; }

    public void setColDataTypeRows(HtmlOutputText hot) { colDataTypeRows = hot; }


    private UIColumn colDataSchema = new UIColumn();

    public UIColumn getColDataSchema() { return colDataSchema; }

    public void setColDataSchema(UIColumn uic) { colDataSchema = uic; }


    private HtmlOutputText colDataSchemaRows = new HtmlOutputText();

    public HtmlOutputText getColDataSchemaRows() { return colDataSchemaRows; }

    public void setColDataSchemaRows(HtmlOutputText hot) { colDataSchemaRows = hot; }


    private UIColumn colUsage = new UIColumn();

    public UIColumn getColUsage() { return colUsage; }

    public void setColUsage(UIColumn uic) { colUsage = uic; }


    private HtmlOutputText colUsageRows = new HtmlOutputText();

    public HtmlOutputText getColUsageRows() { return colUsageRows; }

    public void setColUsageRows(HtmlOutputText hot) { colUsageRows = hot; }


    private UIColumn colInputPredicate = new UIColumn();

    public UIColumn getColInputPredicate() { return colInputPredicate; }

    public void setColInputPredicate(UIColumn uic) { colInputPredicate = uic; }


    private HtmlOutputText colInputPredicateRows = new HtmlOutputText();

    public HtmlOutputText getColInputPredicateRows() { return colInputPredicateRows; }

    public void setColInputPredicateRows(HtmlOutputText hot) { colInputPredicateRows = hot; }


    private UIColumn colOutputPredicate = new UIColumn();

    public UIColumn getColOutputPredicate() { return colOutputPredicate; }

    public void setColOutputPredicate(UIColumn uic) { colOutputPredicate = uic; }


    private HtmlOutputText colOutputPredicateRows = new HtmlOutputText();

    public HtmlOutputText getColOutputPredicateRows() { return colOutputPredicateRows; }

    public void setColOutputPredicateRows(HtmlOutputText hot) { colOutputPredicateRows = hot; }


    private UIColumn colOriginalValue = new UIColumn();

    public UIColumn getColOriginalValue() { return colOriginalValue; }

    public void setColOriginalValue(UIColumn uic) { colOriginalValue = uic; }


    private HtmlOutputText colOriginalValueRows = new HtmlOutputText();

    public HtmlOutputText getColOriginalValueRows() { return colOriginalValueRows; }

    public void setColOriginalValueRows(HtmlOutputText hot) { colOriginalValueRows = hot; }


    private UIColumn colDefaultValue = new UIColumn();

    public UIColumn getColDefaultValue() { return colDefaultValue; }

    public void setColDefaultValue(UIColumn uic) { colDefaultValue = uic; }


    private HtmlOutputText colDefaultValueRows = new HtmlOutputText();

    public HtmlOutputText getColDefaultValueRows() { return colDefaultValueRows; }

    public void setColDefaultValueRows(HtmlOutputText hot) { colDefaultValueRows = hot; }


    private UIColumn colValue = new UIColumn();

    public UIColumn getColValue() { return colValue; }

    public void setColValue(UIColumn uic) { colValue = uic; }


    private HtmlOutputText colValueRows = new HtmlOutputText();

    public HtmlOutputText getColValueRows() { return colValueRows; }

    public void setColValueRows(HtmlOutputText hot) { colValueRows = hot; }


    private HtmlDataTable dtabEngineLog = new HtmlDataTable();

    public HtmlDataTable getDtabEngineLog() { return dtabEngineLog; }

    public void setDtabEngineLog(HtmlDataTable hdt) { dtabEngineLog = hdt; }


    private UIColumn colEngineEventTime = new UIColumn();

    public UIColumn getColEngineEventTime() { return colEngineEventTime; }

    public void setColEngineEventTime(UIColumn uic) { colEngineEventTime = uic; }


    private UIColumn colEngineEvent = new UIColumn();

    public UIColumn getColEngineEvent() { return colEngineEvent; }

    public void setColEngineEvent(UIColumn uic) { colEngineEvent = uic; }


    private HtmlOutputText colEngineTimeRows = new HtmlOutputText();

    public HtmlOutputText getColEngineTimeRows() { return colEngineTimeRows; }

    public void setColEngineTimeRows(HtmlOutputText hot) { colEngineTimeRows = hot; }


    private HtmlOutputText colEngineEventRows = new HtmlOutputText();

    public HtmlOutputText getColEngineEventRows() { return colEngineEventRows; }

    public void setColEngineEventRows(HtmlOutputText hot) { colEngineEventRows = hot; }


    private HtmlDataTable dtabResourceLog = new HtmlDataTable();

    public HtmlDataTable getDtabResourceLog() { return dtabResourceLog; }

    public void setDtabResourceLog(HtmlDataTable hdt) { dtabResourceLog = hdt; }


    private UIColumn colResourceEventTime = new UIColumn();

    public UIColumn getColResourceEventTime() { return colResourceEventTime; }

    public void setColResourceEventTime(UIColumn uic) { colResourceEventTime = uic; }


    private UIColumn colResourceEvent = new UIColumn();

    public UIColumn getColResourceEvent() { return colResourceEvent; }

    public void setColResourceEvent(UIColumn uic) { colResourceEvent = uic; }


    private UIColumn colResourceUser = new UIColumn();

    public UIColumn getColResourceUser() { return colResourceUser; }

    public void setColResourceUser(UIColumn uic) { colResourceUser = uic; }


    private HtmlOutputText colResourceTimeRows = new HtmlOutputText();

    public HtmlOutputText getColResourceTimeRows() { return colResourceTimeRows; }

    public void setColResourceTimeRows(HtmlOutputText hot) { colResourceTimeRows = hot; }


    private HtmlOutputText colResourceEventRows = new HtmlOutputText();

    public HtmlOutputText getColResourceEventRows() { return colResourceEventRows; }

    public void setColResourceEventRows(HtmlOutputText hot) { colResourceEventRows = hot; }


    private HtmlOutputText colResourceUserRows = new HtmlOutputText();

    public HtmlOutputText getColResourceUserRows() { return colResourceUserRows; }

    public void setColResourceUserRows(HtmlOutputText hot) { colResourceUserRows = hot; }


    private HiddenField hdnRowIndex = new HiddenField();

    public HiddenField getHdnRowIndex() { return hdnRowIndex; }

    public void setHdnRowIndex(HiddenField hf) { hdnRowIndex = hf; }


    private PanelLayout pnlContainer ;

    public PanelLayout getPnlContainer() { return pnlContainer; }

    public void setPnlContainer(PanelLayout pnl) { pnlContainer = pnl; }


    private PanelGroup pnlGroup ;

    public PanelGroup getPnlGroup() { return pnlGroup; }

    public void setPnlGroup(PanelGroup group) { pnlGroup = group; }


    private PanelGroup pnlGroupLogData ;

    public PanelGroup getPnlGroupLogData() { return pnlGroupLogData; }

    public void setPnlGroupLogData(PanelGroup group) { pnlGroupLogData = group; }


    private PanelGroup itemResourceLogPnlGroup ;

    public PanelGroup getItemResourceLogPnlGroup() { return itemResourceLogPnlGroup; }

    public void setItemResourceLogPnlGroup(PanelGroup group) { itemResourceLogPnlGroup = group; }


    private PanelGroup itemEngineLogPnlGroup ;

    public PanelGroup getItemEngineLogPnlGroup() { return itemEngineLogPnlGroup; }

    public void setItemEngineLogPnlGroup(PanelGroup group) { itemEngineLogPnlGroup = group; }

       
    private Button btnDetails = new Button();

    public Button getBtnDetails() { return btnDetails; }

    public void setBtnDetails(Button b) { btnDetails = b; }


    private String nameHeaderText = "Name  v" ;

    public String getNameHeaderText() { return nameHeaderText; }

    public void setNameHeaderText(String s) { nameHeaderText = s; }


    private String dataTypeHeaderText = "DataType" ;

    public String getDataTypeHeaderText() { return dataTypeHeaderText; }

    public void setDataTypeHeaderText(String s) { dataTypeHeaderText = s; }


    private String dataSchemaHeaderText = "DataSchema" ;

    public String getDataSchemaHeaderText() { return dataSchemaHeaderText; }

    public void setDataSchemaHeaderText(String s) { dataSchemaHeaderText = s; }


    private String usageHeaderText = "Usage" ;

    public String getUsageHeaderText() { return usageHeaderText; }

    public void setUsageHeaderText(String s) { usageHeaderText = s; }


    private String inputPredicateHeaderText = "Input Pred." ;

    public String getInputPredicateHeaderText() { return inputPredicateHeaderText; }

    public void setInputPredicateHeaderText(String s) { inputPredicateHeaderText = s; }


    private String outputPredicateHeaderText = "Output Pred." ;

    public String getOutputPredicateHeaderText() { return outputPredicateHeaderText; }

    public void setOutputPredicateHeaderText(String s) { outputPredicateHeaderText = s; }


    private String originalValueHeaderText = "Orig. Value" ;

    public String getOriginalValueHeaderText() { return originalValueHeaderText; }

    public void setOriginalValueHeaderText(String s) { originalValueHeaderText = s; }


    private String defaultValueHeaderText = "Def. Value" ;

    public String getDefaultValueHeaderText() { return defaultValueHeaderText; }

    public void setDefaultValueHeaderText(String s) { defaultValueHeaderText = s; }


    private String valueHeaderText = "Value" ;

    public String getValueHeaderText() { return valueHeaderText; }

    public void setValueHeaderText(String s) { valueHeaderText = s; }


    /*******************************************************************************/

    private SessionBean _sb = getSessionBean();
    private MessagePanel msgPanel = _sb.getMessagePanel();


    /**
     * Overridden method that is called immediately before the page is rendered
     */
    public void prerender() {
        _sb.showMessagePanel();
    }


    public String btnRefresh_action() {
        _sb.refreshItemParams(false);
        setHeaderButtonText();
        return null ;
    }

    public String btnLogout_action() {
        _sb.doLogout();
        return "loginPage";
    }


    public String btnDetails_action() {
        Integer selectedRowIndex = new Integer((String) hdnRowIndex.getValue()) - 1;
        return null;
    }

    public String btnBack_action() {
        _sb.refreshCaseItems(false);
        return "showWorkItems";
    }


    public String nameHeaderClick() {
        sortTable(TableSorter.ParamColumn.Name);
        return null ;
    }

    public String dataTypeHeaderClick() {
        sortTable(TableSorter.ParamColumn.DataType);
        return null ;
    }

    public String dataSchemaHeaderClick() {
        sortTable(TableSorter.ParamColumn.DataSchema);
        return null ;
    }

    public String usageHeaderClick() {
        sortTable(TableSorter.ParamColumn.Usage);
        return null ;
    }

    public String inputPredicateHeaderClick() {
        sortTable(TableSorter.ParamColumn.InputPredicate);
        return null ;
    }

    public String outputPredicateHeaderClick() {
        sortTable(TableSorter.ParamColumn.OutputPredicate);
        return null ;
    }

    public String originalValueHeaderClick() {
        sortTable(TableSorter.ParamColumn.OriginalValue);
        return null ;
    }

    public String defaultValueHeaderClick() {
        sortTable(TableSorter.ParamColumn.DefaultValue);
        return null ;
    }

    public String valueHeaderClick() {
        sortTable(TableSorter.ParamColumn.Value);
        return null ;
    }


    private void sortTable(TableSorter.ParamColumn column) {
        _sb.sortItemParams(column);
        setHeaderButtonText();
    }


    private void setHeaderButtonText() {
        resetHeaderButtons();
        ParamOrder currentOrder = _sb.getCurrentParamOrder();
        boolean ascending = currentOrder.isAscending();
        switch (currentOrder.getColumn()) {
            case Name : nameHeaderText += getOrderIndicator(ascending); break;
            case DataType : dataTypeHeaderText += getOrderIndicator(ascending); break;
            case DataSchema : dataSchemaHeaderText += getOrderIndicator(ascending); break;
            case Usage : usageHeaderText += getOrderIndicator(ascending); break;
            case InputPredicate : inputPredicateHeaderText += getOrderIndicator(ascending); break;
            case OutputPredicate : outputPredicateHeaderText += getOrderIndicator(ascending); break;
            case OriginalValue : originalValueHeaderText += getOrderIndicator(ascending); break;
            case DefaultValue : defaultValueHeaderText += getOrderIndicator(ascending); break;
            case Value : valueHeaderText += getOrderIndicator(ascending);
        }
    }


    private void resetHeaderButtons() {
        nameHeaderText = "Name";
        dataTypeHeaderText = "DataType" ;
        dataSchemaHeaderText = "DataSchema" ;
        usageHeaderText = "Usage" ;
        inputPredicateHeaderText = "Input Pred." ;
        outputPredicateHeaderText = "Output Pred." ;
        originalValueHeaderText = "Orig. Value" ;
        defaultValueHeaderText = "Def. Value" ;
        valueHeaderText = "Value" ;
    }


    private String getOrderIndicator(boolean ascending) {
        return ascending ? "  v" : "  ^";
    }

}