/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.monitor.jsf;

import com.sun.rave.web.ui.appbase.AbstractPageBean;
import com.sun.rave.web.ui.component.*;
import org.yawlfoundation.yawl.monitor.MonitorClient;
import org.yawlfoundation.yawl.monitor.sort.CaseOrder;
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
 *  02/12/2008
 */

public class ActiveCases extends AbstractPageBean {

    private int __placeholder;

    private void _init() throws Exception { }

    /** Constructor */
    public ActiveCases() { }


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
            log("Active Cases Initialization Failure", e);
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


    private StaticText staticText = new StaticText();

    public StaticText getStaticText() { return staticText; }

    public void setStaticText(StaticText st) { staticText = st; }


    private PanelLayout layoutPanel = new PanelLayout();

    public PanelLayout getLayoutPanel() { return layoutPanel; }

    public void setLayoutPanel(PanelLayout pl) { layoutPanel = pl; }


    private HtmlDataTable dataTable = new HtmlDataTable();

    public HtmlDataTable getDataTable() { return dataTable; }

    public void setDataTable(HtmlDataTable hdt) { dataTable = hdt; }


    private UIColumn colCaseID = new UIColumn();

    public UIColumn getColCaseID() { return colCaseID; }

    public void setColCaseID(UIColumn uic) { colCaseID = uic; }


    private HtmlOutputText colCaseIDRows = new HtmlOutputText();

    public HtmlOutputText getColCaseIDRows() { return colCaseIDRows; }

    public void setColCaseIDRows(HtmlOutputText hot) { colCaseIDRows = hot; }


    private UIColumn colSpecName = new UIColumn();

    public UIColumn getColSpecName() { return colSpecName; }

    public void setColSpecName(UIColumn uic) { colSpecName = uic; }


    private HtmlOutputText colSpecNameRows = new HtmlOutputText();

    public HtmlOutputText getColSpecNameRows() { return colSpecNameRows; }

    public void setColSpecNameRows(HtmlOutputText hot) { colSpecNameRows = hot; }


    private UIColumn colVersion = new UIColumn();

    public UIColumn getColVersion() { return colVersion; }

    public void setColVersion(UIColumn uic) { colVersion = uic; }


    private HtmlOutputText colVersionRows = new HtmlOutputText();

    public HtmlOutputText getColVersionRows() { return colVersionRows; }

    public void setColVersionRows(HtmlOutputText hot) { colVersionRows = hot; }


    private UIColumn colStartTime = new UIColumn();

    public UIColumn getColStartTime() { return colStartTime; }

    public void setColStartTime(UIColumn uic) { colStartTime = uic; }


    private HtmlOutputText colStartTimeRows = new HtmlOutputText();

    public HtmlOutputText getColStartTimeRows() { return colStartTimeRows; }

    public void setColStartTimeRows(HtmlOutputText hot) { colStartTimeRows = hot; }


    private HiddenField hdnRowIndex = new HiddenField();

    public HiddenField getHdnRowIndex() { return hdnRowIndex; }

    public void setHdnRowIndex(HiddenField hf) { hdnRowIndex = hf; }


    private PanelLayout pnlContainer ;

    public PanelLayout getPnlContainer() { return pnlContainer; }

    public void setPnlContainer(PanelLayout pnl) { pnlContainer = pnl; }


    private PanelGroup pnlGroup ;

    public PanelGroup getPnlGroup() { return pnlGroup; }

    public void setPnlGroup(PanelGroup group) { pnlGroup = group; }


    private Button btnDetails = new Button();

    public Button getBtnDetails() { return btnDetails; }

    public void setBtnDetails(Button b) { btnDetails = b; }


    private Button btnCaseHeader = new Button();

    public Button getBtnCaseHeader() { return btnCaseHeader; }

    public void setBtnCaseHeader(Button b) { btnCaseHeader = b; }


    private Button btnSpecNameHeader = new Button();

    public Button getBtnSpecNameHeader() { return btnSpecNameHeader; }

    public void setBtnSpecNameHeader(Button b) { btnSpecNameHeader = b; }


    private Button btnSpecVersionHeader = new Button();

    public Button getBtnSpecVersionHeader() { return btnSpecVersionHeader; }

    public void setBtnSpecVersionHeader(Button b) { btnSpecVersionHeader = b; }


    private Button btnStartTimeHeader = new Button();

    public Button getBtnStartTimeHeader() { return btnStartTimeHeader; }

    public void setBtnStartTimeHeader(Button b) { btnStartTimeHeader = b; }


    private String btnCaseHeaderText = "Case  v" ;

    public String getBtnCaseHeaderText() { return btnCaseHeaderText; }

    public void setBtnCaseHeaderText(String s) { btnCaseHeaderText = s; }


    private String btnSpecNameHeaderText = "Spec Name" ;

    public String getBtnSpecNameHeaderText() { return btnSpecNameHeaderText; }

    public void setBtnSpecNameHeaderText(String s) { btnSpecNameHeaderText = s; }


    private String btnSpecVersionHeaderText = "Version" ;

    public String getBtnSpecVersionHeaderText() { return btnSpecVersionHeaderText; }

    public void setBtnSpecVersionHeaderText(String s) { btnSpecVersionHeaderText = s; }


    private String btnStartTimeHeaderText = "Start Time" ;

    public String getBtnStartTimeHeaderText() { return btnStartTimeHeaderText; }

    public void setBtnStartTimeHeaderText(String s) { btnStartTimeHeaderText = s; }


    /*******************************************************************************/

    private SessionBean _sb = getSessionBean();
    private MessagePanel msgPanel = _sb.getMessagePanel();


    /**
     * Overridden method that is called immediately before the page is rendered
     */
    public void prerender() {
        msgPanel.show();
    }


    public String btnRefresh_action() {
        _sb.refreshActiveCases(false);
        setHeaderButtonText();
        return null ;
    }

    public String btnLogout_action() {
        _sb.doLogout();
        return "loginPage";
    }


    public String btnDetails_action() {
        Integer selectedRowIndex = new Integer((String) hdnRowIndex.getValue()) - 1;
        System.out.println("Index: " + selectedRowIndex);
        _sb.setCaseSelection(selectedRowIndex);
        return "showWorkItems";
    }

    public String btnCaseHeader_action() {
        sortTable(TableSorter.CaseColumn.Case);
        return null ;
    }

    public String btnSpecNameHeader_action() {
        sortTable(TableSorter.CaseColumn.SpecName);
        return null ;
    }

    public String btnSpecVersionHeader_action() {
        sortTable(TableSorter.CaseColumn.Version);
        return null ;
    }

    public String btnStartTimeHeader_action() {
        sortTable(TableSorter.CaseColumn.StartTime);
        return null ;
    }

    private void sortTable(TableSorter.CaseColumn column) {
        _sb.sortActiveCases(column);
        setHeaderButtonText();
    }

    private void setHeaderButtonText() {
        resetHeaderButtons();
        CaseOrder currentOrder = _sb.getCurrentCaseOrder();
        boolean ascending = currentOrder.isAscending();
        switch (currentOrder.getColumn()) {
            case Case : btnCaseHeaderText += getOrderIndicator(ascending); break;
            case SpecName : btnSpecNameHeaderText += getOrderIndicator(ascending); break;
            case Version : btnSpecVersionHeaderText += getOrderIndicator(ascending); break;
            case StartTime : btnStartTimeHeaderText += getOrderIndicator(ascending);
        }

    }

    private void resetHeaderButtons() {
        btnCaseHeaderText = "Case";
        btnSpecNameHeaderText = "Spec Name" ;
        btnSpecVersionHeaderText = "Version" ;
        btnStartTimeHeaderText = "Start Time" ;
    }


    private String getOrderIndicator(boolean ascending) {
        return ascending ? "  v" : "  ^";
    }

}