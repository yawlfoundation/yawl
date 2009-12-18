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
import org.yawlfoundation.yawl.monitor.sort.ItemOrder;
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

public class CaseItems extends AbstractPageBean {

    private int __placeholder;

    private void _init() throws Exception { }

    /** Constructor */
    public CaseItems() { }


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


    private StaticText staticText = new StaticText();

    public StaticText getStaticText() { return staticText; }

    public void setStaticText(StaticText st) { staticText = st; }


    private PanelLayout layoutPanel = new PanelLayout();

    public PanelLayout getLayoutPanel() { return layoutPanel; }

    public void setLayoutPanel(PanelLayout pl) { layoutPanel = pl; }


    private HtmlDataTable dataTable = new HtmlDataTable();

    public HtmlDataTable getDataTable() { return dataTable; }

    public void setDataTable(HtmlDataTable hdt) { dataTable = hdt; }


    private UIColumn colItemID = new UIColumn();

    public UIColumn getColItemID() { return colItemID; }

    public void setColItemID(UIColumn uic) { colItemID = uic; }


    private HtmlOutputText colItemIDRows = new HtmlOutputText();

    public HtmlOutputText getColItemIDRows() { return colItemIDRows; }

    public void setColItemIDRows(HtmlOutputText hot) { colItemIDRows = hot; }


    private UIColumn colTaskID = new UIColumn();

    public UIColumn getColTaskID() { return colTaskID; }

    public void setColTaskID(UIColumn uic) { colTaskID = uic; }


    private HtmlOutputText colTaskIDRows = new HtmlOutputText();

    public HtmlOutputText getColTaskIDRows() { return colTaskIDRows; }

    public void setColTaskIDRows(HtmlOutputText hot) { colTaskIDRows = hot; }


    private UIColumn colStatus = new UIColumn();

    public UIColumn getColStatus() { return colStatus; }

    public void setColStatus(UIColumn uic) { colStatus = uic; }


    private HtmlOutputText colStatusRows = new HtmlOutputText();

    public HtmlOutputText getColStatusRows() { return colStatusRows; }

    public void setColStatusRows(HtmlOutputText hot) { colStatusRows = hot; }


    private UIColumn colService = new UIColumn();

    public UIColumn getColService() { return colService; }

    public void setColService(UIColumn uic) { colService = uic; }


    private HtmlOutputText colServiceRows = new HtmlOutputText();

    public HtmlOutputText getColServiceRows() { return colServiceRows; }

    public void setColServiceRows(HtmlOutputText hot) { colServiceRows = hot; }


    private UIColumn colEnabledTime = new UIColumn();

    public UIColumn getColEnabledTime() { return colEnabledTime; }

    public void setColEnabledTime(UIColumn uic) { colEnabledTime = uic; }


    private HtmlOutputText colEnabledTimeRows = new HtmlOutputText();

    public HtmlOutputText getColEnabledTimeRows() { return colEnabledTimeRows; }

    public void setColEnabledTimeRows(HtmlOutputText hot) { colEnabledTimeRows = hot; }


    private UIColumn colStartTime = new UIColumn();

    public UIColumn getColStartTime() { return colStartTime; }

    public void setColStartTime(UIColumn uic) { colStartTime = uic; }


    private HtmlOutputText colStartTimeRows = new HtmlOutputText();

    public HtmlOutputText getColStartTimeRows() { return colStartTimeRows; }

    public void setColStartTimeRows(HtmlOutputText hot) { colStartTimeRows = hot; }


    private UIColumn colCompletionTime = new UIColumn();

    public UIColumn getColCompletionTime() { return colCompletionTime; }

    public void setColCompletionTime(UIColumn uic) { colCompletionTime = uic; }


    private HtmlOutputText colCompletionTimeRows = new HtmlOutputText();

    public HtmlOutputText getColCompletionTimeRows() { return colCompletionTimeRows; }

    public void setColCompletionTimeRows(HtmlOutputText hot) { colCompletionTimeRows = hot; }


    private UIColumn colTimerStatus = new UIColumn();

    public UIColumn getColTimerStatus() { return colTimerStatus; }

    public void setColTimerStatus(UIColumn uic) { colTimerStatus = uic; }


    private HtmlOutputText colTimerStatusRows = new HtmlOutputText();

    public HtmlOutputText getColTimerStatusRows() { return colTimerStatusRows; }

    public void setColTimerStatusRows(HtmlOutputText hot) { colTimerStatusRows = hot; }


    private UIColumn colTimerExpiry = new UIColumn();

    public UIColumn getColTimerExpiry() { return colTimerExpiry; }

    public void setColTimerExpiry(UIColumn uic) { colTimerExpiry = uic; }


    private HtmlOutputText colTimerExpiryRows = new HtmlOutputText();

    public HtmlOutputText getColTimerExpiryRows() { return colTimerExpiryRows; }

    public void setColTimerExpiryRows(HtmlOutputText hot) { colTimerExpiryRows = hot; }


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


    private String caseIDHeaderText = "CaseID  v" ;

    public String getCaseIDHeaderText() { return caseIDHeaderText; }

    public void setCaseIDHeaderText(String s) { caseIDHeaderText = s; }


    private String taskIDHeaderText = "TaskID" ;

    public String getTaskIDHeaderText() { return taskIDHeaderText; }

    public void setTaskIDHeaderText(String s) { taskIDHeaderText = s; }


    private String statusHeaderText = "Status" ;

    public String getStatusHeaderText() { return statusHeaderText; }

    public void setStatusHeaderText(String s) { statusHeaderText = s; }


    private String serviceHeaderText = "Service" ;

    public String getServiceHeaderText() { return serviceHeaderText; }

    public void setServiceHeaderText(String s) { serviceHeaderText = s; }


    private String enabledTimeHeaderText = "Enabled" ;

    public String getEnabledTimeHeaderText() { return enabledTimeHeaderText; }

    public void setEnabledTimeHeaderText(String s) { enabledTimeHeaderText = s; }


    private String startTimeHeaderText = "Started" ;

    public String getStartTimeHeaderText() { return startTimeHeaderText; }

    public void setStartTimeHeaderText(String s) { startTimeHeaderText = s; }


    private String completionTimeHeaderText = "Completed" ;

    public String getCompletionTimeHeaderText() { return completionTimeHeaderText; }

    public void setCompletionTimeHeaderText(String s) { completionTimeHeaderText = s; }


    private String timerStatusHeaderText = "Timer" ;

    public String getTimerStatusHeaderText() { return timerStatusHeaderText; }

    public void setTimerStatusHeaderText(String s) { timerStatusHeaderText = s; }


    private String timerExpiryHeaderText = "Expires" ;

    public String getTimerExpiryHeaderText() { return timerExpiryHeaderText; }

    public void setTimerExpiryHeaderText(String s) { timerExpiryHeaderText = s; }


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
        _sb.refreshCaseItems(false);
        setHeaderButtonText();
        return null ;
    }

    public String btnLogout_action() {
        _sb.doLogout();
        return "loginPage";
    }


    public String btnDetails_action() {
   //     return "showWorkItems";
        return null;
    }

    public String btnBack_action() {
        return "showCases";
    }


    public String caseIDHeaderClick() {
        sortTable(TableSorter.ItemColumn.ItemID);
        return null ;
    }

    public String taskIDHeaderClick() {
        sortTable(TableSorter.ItemColumn.TaskID);
        return null ;
    }

    public String statusHeaderClick() {
        sortTable(TableSorter.ItemColumn.Status);
        return null ;
    }

    public String serviceHeaderClick() {
        sortTable(TableSorter.ItemColumn.Service);
        return null ;
    }

    public String enabledTimeHeaderClick() {
        sortTable(TableSorter.ItemColumn.EnabledTime);
        return null ;
    }

    public String startTimeHeaderClick() {
        sortTable(TableSorter.ItemColumn.StartTime);
        return null ;
    }

    public String completionTimeHeaderClick() {
        sortTable(TableSorter.ItemColumn.CompletionTime);
        return null ;
    }

    public String timerStatusHeaderClick() {
        sortTable(TableSorter.ItemColumn.TimerStatus);
        return null ;
    }

    public String timerExpiryHeaderClick() {
        sortTable(TableSorter.ItemColumn.TimerExpiry);
        return null ;
    }

    
    private void sortTable(TableSorter.ItemColumn column) {
        _sb.sortCaseItems(column);
        setHeaderButtonText();
    }


    private void setHeaderButtonText() {
        resetHeaderButtons();
        ItemOrder currentOrder = _sb.getCurrentItemOrder();
        boolean ascending = currentOrder.isAscending();
        switch (currentOrder.getColumn()) {
            case ItemID : caseIDHeaderText += getOrderIndicator(ascending); break;
            case TaskID : taskIDHeaderText += getOrderIndicator(ascending); break;
            case Status : statusHeaderText += getOrderIndicator(ascending); break;
            case Service : serviceHeaderText += getOrderIndicator(ascending); break;
            case EnabledTime : enabledTimeHeaderText += getOrderIndicator(ascending); break;
            case StartTime : startTimeHeaderText += getOrderIndicator(ascending); break;
            case CompletionTime : completionTimeHeaderText += getOrderIndicator(ascending); break;
            case TimerStatus : timerStatusHeaderText += getOrderIndicator(ascending); break;
            case TimerExpiry : timerExpiryHeaderText += getOrderIndicator(ascending);
        }
    }


    private void resetHeaderButtons() {
        caseIDHeaderText = "CaseID";
        taskIDHeaderText = "TaskID" ;
        statusHeaderText = "Status" ;
        serviceHeaderText = "Service" ;
        enabledTimeHeaderText = "Enabled" ;
        startTimeHeaderText = "Started" ;
        completionTimeHeaderText = "Completed" ;
        timerStatusHeaderText = "Timer" ;
        timerExpiryHeaderText = "Expires" ;
    }


    private String getOrderIndicator(boolean ascending) {
        return ascending ? "  v" : "  ^";
    }

}