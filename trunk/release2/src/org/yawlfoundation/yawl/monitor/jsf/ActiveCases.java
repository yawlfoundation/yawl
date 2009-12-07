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
import org.yawlfoundation.yawl.resourcing.jsf.MessagePanel;

import javax.faces.FacesException;
import javax.faces.component.UIColumn;
import javax.faces.component.html.HtmlDataTable;
import javax.faces.component.html.HtmlOutputText;

/**
 *  Backing bean for the active cases page.
 *
 *  @author Michael Adams
 *  BPM Group, QUT Australia
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


    private HtmlOutputText colCaseIDHeader = new HtmlOutputText();

    public HtmlOutputText getColCaseIDHeader() { return colCaseIDHeader; }

    public void setColCaseIDHeader(HtmlOutputText hot) { colCaseIDHeader = hot; }


    private UIColumn colSpecName = new UIColumn();

    public UIColumn getColSpecName() { return colSpecName; }

    public void setColSpecName(UIColumn uic) { colSpecName = uic; }


    private HtmlOutputText colSpecNameRows = new HtmlOutputText();

    public HtmlOutputText getColSpecNameRows() { return colSpecNameRows; }

    public void setColSpecNameRows(HtmlOutputText hot) { colSpecNameRows = hot; }


    private HtmlOutputText colSpecNameHeader = new HtmlOutputText();

    public HtmlOutputText getColSpecNameHeader() { return colSpecNameHeader; }

    public void setColSpecNameHeader(HtmlOutputText hot) { colSpecNameHeader = hot; }



    private UIColumn colVersion = new UIColumn();

    public UIColumn getColVersion() { return colVersion; }

    public void setColVersion(UIColumn uic) { colVersion = uic; }


    private HtmlOutputText colVersionRows = new HtmlOutputText();

    public HtmlOutputText getColVersionRows() { return colVersionRows; }

    public void setColVersionRows(HtmlOutputText hot) { colVersionRows = hot; }


    private HtmlOutputText colVersionHeader = new HtmlOutputText();

    public HtmlOutputText getColVersionHeader() { return colVersionHeader; }

    public void setColVersionHeader(HtmlOutputText hot) { colVersionHeader = hot; }


    private UIColumn colStartedBy = new UIColumn();

    public UIColumn getColStartedBy() { return colStartedBy; }

    public void setColStartedBy(UIColumn uic) { colStartedBy = uic; }


    private HtmlOutputText colStartedByRows = new HtmlOutputText();

    public HtmlOutputText getColStartedByRows() { return colStartedByRows; }

    public void setColStartedByRows(HtmlOutputText hot) { colStartedByRows = hot; }


    private HtmlOutputText colStartedByHeader = new HtmlOutputText();

    public HtmlOutputText getColStartedByHeader() { return colStartedByHeader; }

    public void setColStartedByHeader(HtmlOutputText hot) { colStartedByHeader = hot; }


    private UIColumn colStartTime = new UIColumn();

    public UIColumn getColStartTime() { return colStartTime; }

    public void setColStartTime(UIColumn uic) { colStartTime = uic; }


    private HtmlOutputText colStartTimeRows = new HtmlOutputText();

    public HtmlOutputText getColStartTimeRows() { return colStartTimeRows; }

    public void setColStartTimeRows(HtmlOutputText hot) { colStartTimeRows = hot; }


    private HtmlOutputText colStartTimeHeader = new HtmlOutputText();

    public HtmlOutputText getColStartTimeHeader() { return colStartTimeHeader; }

    public void setColStartTimeHeader(HtmlOutputText hot) { colStartTimeHeader = hot; }



    private UIColumn colAge = new UIColumn();

    public UIColumn getColAge() { return colAge; }

    public void setColAge(UIColumn uic) { colAge = uic; }


    private HtmlOutputText colAgeRows = new HtmlOutputText();

    public HtmlOutputText getColAgeRows() { return colAgeRows; }

    public void setColAgeRows(HtmlOutputText hot) { colAgeRows = hot; }


    private HtmlOutputText colAgeHeader = new HtmlOutputText();

    public HtmlOutputText getColAgeHeader() { return colAgeHeader; }

    public void setColAgeHeader(HtmlOutputText hot) { colAgeHeader = hot; }



    private HiddenField hdnRowIndex = new HiddenField();

    public HiddenField getHdnRowIndex() { return hdnRowIndex; }

    public void setHdnRowIndex(HiddenField hf) { hdnRowIndex = hf; }


    private PanelLayout pnlContainer ;

    public PanelLayout getPnlContainer() { return pnlContainer; }

    public void setPnlContainer(PanelLayout pnl) { pnlContainer = pnl; }


    private PanelGroup pnlGroup ;

    public PanelGroup getPnlGroup() { return pnlGroup; }

    public void setPnlGroup(PanelGroup group) { pnlGroup = group; }


    /*******************************************************************************/

    private MonitorClient mc = getApplicationBean().getMonitorClient();
    private SessionBean _sb = getSessionBean();
    private MessagePanel msgPanel = _sb.getMessagePanel();


    /**
     * Overridden method that is called immediately before the page is rendered
     */
    public void prerender() {
        msgPanel.show();
        _sb.refreshActiveCases();
    }




    public String btnRefresh_action() {
        return null ;
    }

//    // upload the chosen spec file
//    public String btnUpload_action() {
//        UploadedFile uploadedFile = fileUpload1.getUploadedFile();
//
//        if (uploadedFile != null) {
//            String uploadedFileName = stripPath(uploadedFile.getOriginalName());
//            if (validExtension(uploadedFileName)) {
//                String fileAsString = uploadedFile.getAsString() ;
//                uploadSpec(uploadedFileName, fileAsString) ;
//            }
//            else msgPanel.error(
//                    "Only yawl files with an extension of '.yawl' or '.xml' may be uploaded.");
//        }
//        return null;
//    }
//
//
//    private boolean validExtension(String fileName) {
//        return fileName.endsWith(".xml") || fileName.endsWith(".yawl");
//    }
//
//    // upload the chosen spec into the engine
//    private void uploadSpec(String fileName, String fileContents) {
//        int BOF = fileContents.indexOf("<?xml");
//        int EOF = fileContents.indexOf("</specificationSet>");
//        if (BOF != -1 && EOF != -1) {
//            fileContents = fileContents.substring(BOF, EOF + 19) ;         // trim file
//            String handle = _sb.getSessionhandle() ;
//            String result = _rm.uploadSpecification(fileContents, fileName, handle);
//            if (! Interface_Client.successful(result))
//                msgPanel.error(msgPanel.format((result)));
//
//            _sb.refreshLoadedSpecs();
//        }
//        else msgPanel.error("The file '" + fileName + "' does not appear to be a " +
//                            "valid YAWL specification description or is malformed. " +
//                            "Please check the file and/or its contents and try again.");
//    }
//
//
//    // attempts to launch a new case
//    public String btnLaunch_action() {
//
//        // get selected spec
//        String refPage = null ;
//        try {
//            Integer selectedRowIndex = new Integer((String) hdnRowIndex.getValue());
//            SpecificationData spec = _sb.getLoadedSpec(selectedRowIndex);
//
//            // make sure the latest spec version is selected
//            YSpecVersion latestVersion = _sb.getLatestLoadedSpecVersion(spec);
//            YSpecVersion selectedVersion = new YSpecVersion(spec.getSpecVersion());
//            if (selectedVersion.compareTo(latestVersion) >= 0) {
//                refPage = startCase(spec) ;
//            }
//            else {
//                msgPanel.error("Unable to start case. Only the latest version of a " +
//                               "specification may be launched. The latest loaded version " +
//                               "of this specification is '" + latestVersion +
//                               "', the selected version is '" + selectedVersion +
//                               "'. Please select the latest version.");
//            }
//        }
//        catch (NumberFormatException nfe) {
//            msgPanel.error("No specification selected to launch.") ;
//        }
//
//        return refPage;
//    }
//
//
//    // cancels the selected case
//    public String btnCancelCase_action() {
//
//        // get selected case
//        String choice = _sb.getRunningCaseListChoice() ;
//        if ((choice != null) && (choice.length() > 0)) {
//            choice = choice.substring(0, choice.indexOf(':')) ;    // get casenbr prefix
//            String result = cancelCase(choice) ;
//            if (! Interface_Client.successful(result))
//                msgPanel.error("Could not cancel case.\n\n" +  msgPanel.format(result)) ;
//        }
//        else msgPanel.error("No case selected to cancel.");
//
//        return null;
//    }
//
//
//    // cancels the case with the id passed
//    private String cancelCase(String caseID) {
//        try {
//            _sb.setRunningCaseListChoice(null) ;
//            String handle = _sb.getSessionhandle() ;
//            return _rm.cancelCase(caseID, handle);
//        }
//        catch (IOException ioe) {
//            msgPanel.error("IOException when attempting to cancel case") ;
//            return null;
//        }
//    }
//
//
//    // unloads the chosen spec from the engine
//    private String unloadSpec(SpecificationData spec) {
//        try {
//            String handle = _sb.getSessionhandle() ;
//            return _rm.unloadSpecificationData(spec.getID(), spec.getSpecVersion(), handle);
//        }
//        catch (IOException ioe) {
//            msgPanel.error("IOException when attempting to unload specification") ;
//            return null ;
//        }
//    }
//
//
//    // attempts to unload a spec from the engine
//    public String btnUnload_action() {
//
//        // get selected spec
//        try {
//            Integer selectedRowIndex = new Integer((String) hdnRowIndex.getValue());
//            SpecificationData spec = _sb.getLoadedSpec(selectedRowIndex);
//            if (spec != null) {
//                String result = unloadSpec(spec) ;
//                if (result.indexOf("success") == -1) {
//                    result = JDOMUtil.formatXMLString(result);
//                    msgPanel.error("Could not unload specification.\n\n" + result);
//                }
//                else {
//                    hdnRowIndex.setValue(null);
//                    _sb.refreshLoadedSpecs();
//                }
//            }
//        }
//        catch (NumberFormatException nfe) {
//            msgPanel.error("No specification selected to unload.");
//        }
//        return null;
//    }
//
//
//    // starts a new case instance
//    private String startCase(SpecificationData specData) {
//
//        if (specData != null) {
//            List inputParams = specData.getInputParams();
//            if (! inputParams.isEmpty()) {
//                _sb.setDynFormType(ApplicationBean.DynFormType.netlevel);
//                _sb.setLoadedSpecListChoice(specData);
//
//                DynFormFactory df = (DynFormFactory) getBean("DynFormFactory");
//                df.setHeaderText("Starting an Instance of: " + specData.getID());
//                if (df.initDynForm("YAWL 2.0 Case Management - Launch Case")) {
//                    return "showDynForm" ;
//                }
//                else {
//                    msgPanel.error("Could not successfully start case - problem " +
//                                   "initialising dynamic form from specification. " +
//                                   "Please see the log files for details.");
//                }
//            }
//            else {
//
//                // no case params to worry about
//                YSpecificationID ySpecID = new YSpecificationID(specData.getID(),
//                                                                specData.getSpecVersion());
//                beginCase(ySpecID, _sb.getSessionhandle());
//            }
//        }
//        return null ;
//    }
//
//
//    // starts a new case (either directly from startCase() or via a postback action)
//    private void beginCase(YSpecificationID specID, String handle) {
//        String caseData = null ;
//        String result ;
//        if (_sb.isCaseLaunch()) {
//            caseData = getDynFormFactory().getDataList();
//            _sb.setCaseLaunch(false);
//        }
//        try {
//            result = _rm.launchCase(specID, caseData, handle);
//            if (Interface_Client.successful(result))
//                updateRunningCaseList();
//            else
//                msgPanel.error("Unsuccessful case start:" + msgPanel.format(result)) ;
//        }
//        catch (IOException ioe) {
//            msgPanel.error("IOException when attempting to unload specification") ;
//        }
//    }
//
//
//    // refreshes list of running cases
//    private void updateRunningCaseList() {
//        String handle = _sb.getSessionhandle() ;
//        Set<SpecificationData> specDataSet = _rm.getSpecList(handle) ;
//        if (specDataSet != null) {
//            ArrayList<String> caseList = new ArrayList<String>();
//            for (SpecificationData specData : specDataSet) {
//                List<String> caseIDs = _rm.getRunningCasesAsList(specData.getSpecID(), handle);
//                for (String caseID : caseIDs) {
//                    String line = String.format("%s: %s (%s)", caseID, specData.getID(),
//                                                specData.getSpecVersion());
//                    caseList.add(line) ;
//                }
//            }
//
//            // sort the list using a treeset
//            TreeSet<String> caseTree = new TreeSet<String>(caseList) ;
//
//            // convert to options array
//            Option[] options = new Option[caseTree.size()] ;
//            int i = 0 ;
//            for (String caseStr : caseTree) options[i++] = new Option(caseStr) ;
//            _sb.setRunningCaseListOptions(options);
//        }
//    }
//
//
//    private void activateButtons() {
//        List<SpecificationData> list = _sb.getLoadedSpecs();
//        boolean noSpecsSelected = (list == null) || list.isEmpty() ;
//        btnUnload.setDisabled(noSpecsSelected);
//        btnLaunch.setDisabled(noSpecsSelected);
//    }

}