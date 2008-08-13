/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing.jsf;

import com.sun.rave.web.ui.appbase.AbstractPageBean;
import com.sun.rave.web.ui.component.*;
import com.sun.rave.web.ui.model.Option;
import com.sun.rave.web.ui.model.UploadedFile;
import org.yawlfoundation.yawl.engine.interfce.Interface_Client;
import org.yawlfoundation.yawl.engine.interfce.SpecificationData;
import org.yawlfoundation.yawl.resourcing.ResourceManager;
import org.yawlfoundation.yawl.resourcing.jsf.dynform.DynFormFactory;
import org.yawlfoundation.yawl.util.JDOMUtil;

import javax.faces.FacesException;
import javax.faces.component.UIColumn;
import javax.faces.component.html.HtmlDataTable;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.event.ValueChangeEvent;
import java.io.IOException;
import java.util.*;

/**
 *  Backing bean for the case mgt page.
 *
 *  @author Michael Adams
 *  BPM Group, QUT Australia
 *  v0.1, 19/12/2007
 *
 *
 *  Last Date: 05/01/2008
 */

public class caseMgt extends AbstractPageBean {

    private int __placeholder;

    private void _init() throws Exception { }

    /** Constructor */
    public caseMgt() { }


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


    private Upload fileUpload1 = new Upload();

    public Upload getFileUpload1() { return fileUpload1; }

    public void setFileUpload1(Upload u) { fileUpload1 = u; }


    private Button btnUpload = new Button();

    public Button getBtnUpload() { return btnUpload; }

    public void setBtnUpload(Button b) { btnUpload = b; }


    private StaticText staticText1 = new StaticText();

    public StaticText getStaticText1() { return staticText1; }

    public void setStaticText1(StaticText st) { staticText1 = st; }


    private Button btnLaunch = new Button();

    public Button getBtnLaunch() { return btnLaunch; }

    public void setBtnLaunch(Button b) { btnLaunch = b; }


    private Button btnCancelCase = new Button();

    public Button getBtnCancelCase() { return btnCancelCase; }

    public void setBtnCancelCase(Button b) { btnCancelCase = b; }


    private StaticText staticText2 = new StaticText();

    public StaticText getStaticText2() { return staticText2; }

    public void setStaticText2(StaticText st) { staticText2 = st; }


    private Listbox lbxRunningCases = new Listbox();

    public Listbox getLbxRunningCases() { return lbxRunningCases; }

    public void setLbxRunningCases(Listbox l) { lbxRunningCases = l; }


    private StaticText staticText3 = new StaticText();

    public StaticText getStaticText3() { return staticText3; }

    public void setStaticText3(StaticText st) { staticText3 = st; }


    private Button btnUnload = new Button();

    public Button getBtnUnload() { return btnUnload; }

    public void setBtnUnload(Button b) { btnUnload = b; }


    private MessageGroup msgBox = new MessageGroup();

    public MessageGroup getMsgBox() { return msgBox; }

    public void setMsgBox(MessageGroup mg) { msgBox = mg; }


    private PanelLayout layoutPanel1 = new PanelLayout();

    public PanelLayout getLayoutPanel1() { return layoutPanel1; }

    public void setLayoutPanel1(PanelLayout pl) { layoutPanel1 = pl; }


    private PanelLayout layoutPanel2 = new PanelLayout();

    public PanelLayout getLayoutPanel2() { return layoutPanel2; }

    public void setLayoutPanel2(PanelLayout pl) { layoutPanel2 = pl; }


    private PanelLayout layoutPanel3 = new PanelLayout();

    public PanelLayout getLayoutPanel3() { return layoutPanel3; }

    public void setLayoutPanel3(PanelLayout pl) { layoutPanel3 = pl; }


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


    private UIColumn colDescription = new UIColumn();

    public UIColumn getColDescription() { return colDescription; }

    public void setColDescription(UIColumn uic) { colDescription = uic; }


    private HtmlOutputText colDescriptionRows = new HtmlOutputText();

    public HtmlOutputText getColDescriptionRows() { return colDescriptionRows; }

    public void setColDescriptionRows(HtmlOutputText hot) { colDescriptionRows = hot; }


    private HtmlOutputText colDescriptionHeader = new HtmlOutputText();

    public HtmlOutputText getColDescriptionHeader() { return colDescriptionHeader; }

    public void setColDescriptionHeader(HtmlOutputText hot) { colDescriptionHeader = hot; }


    private UIColumn colSBar = new UIColumn();

    public UIColumn getColSBar() { return colSBar; }

    public void setColSBar(UIColumn uic) { colSBar = uic; }

    
    private HiddenField hdnRowIndex = new HiddenField();

    public HiddenField getHdnRowIndex() { return hdnRowIndex; }

    public void setHdnRowIndex(HiddenField hf) { hdnRowIndex = hf; }


    private Script script1 = new Script();

    public Script getScript1() { return script1; }

    public void setScript1(Script s) { script1 = s; }


    /*******************************************************************************/

    private ResourceManager _rm = getApplicationBean().getResourceManager() ;
    private SessionBean _sb = getSessionBean();
    private MessagePanel msgPanel = _sb.getMessagePanel();

    public void fileUpload1_processValueChange(ValueChangeEvent event) { }


    /** @return a reference to the session scoped factory bean. */
    private DynFormFactory getDynFormFactory() {
        return (DynFormFactory) getBean("DynFormFactory");
    }

    
    /**
     * Overridden method that is called immediately before the page is rendered
     */
    public void prerender() {
        _sb.checkLogon();
        msgPanel.show(650, 130, "absolute");

        // take postback action on case launch
        if (_sb.isCaseLaunch()) {
            String specID = _sb.getLoadedSpecListChoice() ;
            if (specID != null)
                beginCase(specID, _sb.getSessionhandle());
        }
        updateRunningCaseList();
        _sb.setActivePage(ApplicationBean.PageRef.caseMgt);
    }


    // removes the path part from an absolute filename
    private String stripPath(String fileName) {
        return stripPath(stripPath(fileName, '/'), '\\');
    }


    private String stripPath(String fileName, char slash) {
        int index = fileName.lastIndexOf(slash);
        if (index >= 0) fileName = fileName.substring(index + 1);
        return fileName ;
    }


    // upload the chosen spec file
    public String btnUpload_action() {
        UploadedFile uploadedFile = fileUpload1.getUploadedFile();

        if (uploadedFile != null) {
            String uploadedFileName = stripPath(uploadedFile.getOriginalName());
            if (uploadedFileName.endsWith(".xml")) {
                String fileAsString = uploadedFile.getAsString() ;
                uploadSpec(uploadedFileName, fileAsString) ;
            }
            else msgPanel.error("Only files with an 'xml' extension may be uploaded.");
        }
        return null;
    }

    
    // upload the chosen spec into the engine
    private void uploadSpec(String fileName, String fileContents) {
        int BOF = fileContents.indexOf("<?xml");
        int EOF = fileContents.indexOf("</specificationSet>");
        if (BOF != -1 && EOF != -1) {
            fileContents = fileContents.substring(BOF, EOF + 19) ;         // trim file
            String handle = _sb.getSessionhandle() ;
            String result = _rm.uploadSpecification(fileContents, fileName, handle);
            if (! Interface_Client.successful(result))
                msgPanel.error(msgPanel.format((result)));

            _sb.refreshLoadedSpecs();
        }
        else msgPanel.error("The file '" + fileName + "' does not appear to be a " +
                            "valid YAWL specification description or is malformed. " +
                            "Please check the file and/or its contents and try again.");
    }


    // attempts to launch a new case
    public String btnLaunch_action() {

        // get selected spec
        String refPage = null ;
        try {
            Integer selectedRowIndex = new Integer((String) hdnRowIndex.getValue());
            SpecificationData spec = _sb.getLoadedSpec(selectedRowIndex - 1);
            refPage = startCase(spec) ;
        }
        catch (NumberFormatException nfe) {
            msgPanel.error("No specification selected to launch.") ;
        }

        return refPage;
    }


    // cancels the selected case
    public String btnCancelCase_action() {

        // get selected case
        String choice = _sb.getRunningCaseListChoice() ;
        if ((choice != null) && (choice.length() > 0)) {
            choice = choice.substring(0, choice.indexOf(':')) ;    // drop casenbr prefix
            String result = cancelCase(choice) ;
            if (! Interface_Client.successful(result))
                msgPanel.error("Could not cancel case.\n\n" +  msgPanel.format(result)) ;
        }
        else msgPanel.error("No case selected to cancel.");
        
        return null;
    }


    // cancels the case with the id passed
    private String cancelCase(String caseID) {
        try {
            String handle = _sb.getSessionhandle() ;
            return _rm.cancelCase(caseID, handle);
        }
        catch (IOException ioe) {
            msgPanel.error("IOException when attempting to cancel case") ;
            return null;
        }
    }


    // unloads the chosen spec from the engine
    private String unloadSpec(String specID) {
        try {
            String handle = _sb.getSessionhandle() ;
            return _rm.unloadSpecification(specID, handle);
        }
        catch (IOException ioe) {
            msgPanel.error("IOException when attempting to unload specification") ;
            return null ;
        }
    }


    // attempts to unload a spec from the engine
    public String btnUnload_action() {

        // get selected spec
        try {
            Integer selectedRowIndex = new Integer((String) hdnRowIndex.getValue());
            SpecificationData spec = _sb.getLoadedSpec(selectedRowIndex - 1);
            String result = unloadSpec(spec.getID()) ;
            if (result.indexOf("success") == -1) {
                result = JDOMUtil.formatXMLString(result);
                msgPanel.error("Could not unload specification.\n\n" + result);
            }
            else {
                _sb.refreshLoadedSpecs();
            }
        }
        catch (NumberFormatException nfe) {
            msgPanel.error("No specification selected to unload.");
        }
        return null;
    }


    // starts a new case instance
    private String startCase(SpecificationData specData) {

        if (specData != null) {
            List inputParams = specData.getInputParams();
            if (! inputParams.isEmpty()) {
                _sb.setDynFormType(ApplicationBean.DynFormType.netlevel);
                _sb.setLoadedSpecListChoice(specData.getID());

                DynFormFactory df = (DynFormFactory) getBean("DynFormFactory");
                df.setHeaderText("Starting an Instance of: " + specData.getID());
                if (df.initDynForm("YAWL 2.0 Case Management - Launch Case")) {
                    return "showDynForm" ;
                }
                else {
                    msgPanel.error("Could not successfully start case - problem " +
                                   "initialising dynamic form from specification. " +
                                   "Please see the log files for details.");
                }
            }
            else {

                // no case params to worry about
                beginCase(specData.getID(), _sb.getSessionhandle());
            }
        }
        return null ;
    }


    // starts a new case (either directly from startCase() or via a postback action)
    private void beginCase(String specID, String handle) {
        String caseData = null ;
        String result ;
        if (_sb.isCaseLaunch()) {
            caseData = getDynFormFactory().getDataList();
            _sb.setCaseLaunch(false);
        }
        try {
            result = _rm.launchCase(specID, caseData, handle);
            if (Interface_Client.successful(result))
                updateRunningCaseList();
            else
                msgPanel.error("Unsuccessful case start:" + msgPanel.format(result)) ;
        }
        catch (IOException ioe) {
            msgPanel.error("IOException when attempting to unload specification") ;
        }
    }


    // refreshes list of running cases
    private void updateRunningCaseList() {
        String handle = _sb.getSessionhandle() ;
        Set<SpecificationData> specDataSet = _rm.getSpecList(handle) ;
        if (specDataSet != null) {
            ArrayList<Option> caseList = new ArrayList<Option>();
            for (SpecificationData specData : specDataSet) {
                List<String> caseIDs = _rm.getRunningCasesAsList(specData.getID(), handle);

                // srt the list using a treeset
                TreeSet<String> caseTree = new TreeSet<String>(caseIDs) ;
                for (String caseID : caseTree)
                    caseList.add(new Option(caseID + ": " + specData.getID())) ;
            }

            // convert to options array
            Option[] options = new Option[caseList.size()] ;
            int i = 0 ;
            for (Option option : caseList) options[i++] = option ;          
            _sb.setRunningCaseListOptions(options);
        }
    }

}

