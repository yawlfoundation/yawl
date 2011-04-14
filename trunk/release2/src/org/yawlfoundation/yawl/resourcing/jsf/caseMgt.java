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
import com.sun.rave.web.ui.model.UploadedFile;
import org.jdom.Element;
import org.yawlfoundation.yawl.elements.YSpecVersion;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.SpecificationData;
import org.yawlfoundation.yawl.resourcing.ResourceManager;
import org.yawlfoundation.yawl.resourcing.datastore.eventlog.LogMiner;
import org.yawlfoundation.yawl.resourcing.jsf.dynform.DynFormFactory;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;

import javax.faces.FacesException;
import javax.faces.component.UIColumn;
import javax.faces.component.html.HtmlDataTable;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

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



    private UIColumn colVersion = new UIColumn();

    public UIColumn getColVersion() { return colVersion; }

    public void setColVersion(UIColumn uic) { colVersion = uic; }


    private HtmlOutputText colVersionRows = new HtmlOutputText();

    public HtmlOutputText getColVersionRows() { return colVersionRows; }

    public void setColVersionRows(HtmlOutputText hot) { colVersionRows = hot; }


    private HtmlOutputText colVersionHeader = new HtmlOutputText();

    public HtmlOutputText getColVersionHeader() { return colVersionHeader; }

    public void setColVersionHeader(HtmlOutputText hot) { colVersionHeader = hot; }


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


    // the next 3 buttons are available only when the exception service is enabled

    private Button btnRaiseException = new Button();

    public Button getBtnRaiseException() { return btnRaiseException; }

    public void setBtnRaiseException(Button b) { btnRaiseException = b; }


    private Button btnRejectWorklet = new Button();

    public Button getBtnRejectWorklet() { return btnRejectWorklet; }

    public void setBtnRejectWorklet(Button b) { btnRejectWorklet = b; }


    private Button btnWorkletAdmin = new Button();

    public Button getBtnWorkletAdmin() { return btnWorkletAdmin; }

    public void setBtnWorkletAdmin(Button b) { btnWorkletAdmin = b; }


    private Button btnGetInfo = new Button();

    public Button getBtnGetInfo() { return btnGetInfo; }

    public void setBtnGetInfo(Button b) { btnGetInfo = b; }


    private Button btnDownloadLog = new Button();

    public Button getBtnDownloadLog() { return btnDownloadLog; }

    public void setBtnDownloadLog(Button b) { btnDownloadLog = b; }


    /*******************************************************************************/

    private final ResourceManager _rm = getApplicationBean().getResourceManager() ;
    private final SessionBean _sb = getSessionBean();
    private final MessagePanel msgPanel = _sb.getMessagePanel();

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
        _sb.setActivePage(ApplicationBean.PageRef.caseMgt);
        showMessagePanel();

        // take postback action on case launch
        if (_sb.isCaseLaunch()) {
            beginCase(_sb.getLoadedSpecListChoice());
        }
        updateRunningCaseList();
        _sb.refreshLoadedSpecs();
        activateButtons();
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


    public String btnRefresh_action() {
        return null ;
    }


    public String btnGetInfo_action() {
        showSpecInfo();
        return null ;
    }


    public String btnDownloadLog_action() {
        downloadLog();
        return null ;
    }


    private void redirectTo(String url) {
        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect(url);
        }
        catch (IOException ioe) {
            msgPanel.error(ioe.getMessage());
        }
    }


    public String btnRejectWorklet_action() {
        String caseID = getSelectedCaseID() ;
        if (caseID != null) {
            String ixURI = _rm.getExceptionServiceURI();
            if (ixURI != null) {
                redirectTo(ixURI + "/rejectWorklet?caseID=" + caseID);
            }
        }
        else msgPanel.error("No worklet case selected to reject.");

        return null ;
    }

    public String btnRaiseException_action() {
        String caseID = getSelectedCaseID() ;
        if (caseID != null) {
            String ixURI = _rm.getExceptionServiceURI();
            if (ixURI != null) {
                redirectTo(ixURI + "/caseException?caseID=" + caseID);
            }
        }
        else msgPanel.error("No case selected to raise exception against.");

        return null ;
    }

    public String btnWorkletAdmin_action() {
        String ixURI = _rm.getExceptionServiceURI();
        if (ixURI != null) {
            redirectTo(ixURI + "/wsAdminTasks?sH=" + _sb.getSessionhandle());
        }
        else msgPanel.error("Could not find the Worklet Service.");

        return null ;
    }



    // upload the chosen spec file
    public String btnUpload_action() {
        UploadedFile uploadedFile = fileUpload1.getUploadedFile();

        if (uploadedFile != null) {
            String uploadedFileName = stripPath(uploadedFile.getOriginalName());
            if (validExtension(uploadedFileName)) {
                String fileAsString ;

                // try getting the uploaded spec in the correct encoding
                try {
                    fileAsString = new String(uploadedFile.getBytes(), "UTF-8");
                }
                catch (UnsupportedEncodingException e) {

                     // fallback to plain string
                    fileAsString = uploadedFile.getAsString() ;

                }
                
                uploadSpec(uploadedFileName, fileAsString) ;
            }
            else msgPanel.error(
                    "Only yawl files with an extension of '.yawl' or '.xml' may be uploaded.");
        }
        return null;
    }


    private boolean validExtension(String fileName) {
        return fileName.endsWith(".xml") || fileName.endsWith(".yawl");
    }
    
    // upload the chosen spec into the engine
    private void uploadSpec(String fileName, String fileContents) {
        int BOF = fileContents.indexOf("<?xml");
        int EOF = fileContents.indexOf("</specificationSet>");
        if (BOF != -1 && EOF != -1) {
            fileContents = fileContents.substring(BOF, EOF + 19) ;         // trim file
            if (hasUniqueDescriptors(fileContents)) {
                String result = _rm.uploadSpecification(fileContents, fileName);
                if (! _rm.successful(result)) processErrorMsg(result);
                _sb.refreshLoadedSpecs();
            }
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
            SpecificationData spec = _sb.getLoadedSpec(selectedRowIndex);

            // make sure the latest spec version is selected
            YSpecVersion latestVersion = _sb.getLatestLoadedSpecVersion(spec);
            YSpecVersion selectedVersion = new YSpecVersion(spec.getSpecVersion());
            if (selectedVersion.compareTo(latestVersion) >= 0) {
                refPage = startCase(spec) ;
            }
            else {
                msgPanel.error("Unable to start case. Only the latest version of a " +
                               "specification may be launched. The latest loaded version " +
                               "of this specification is '" + latestVersion +
                               "', the selected version is '" + selectedVersion +
                               "'. Please select the latest version.");
            }
        }
        catch (NumberFormatException nfe) {
            msgPanel.error("No specification selected to launch.") ;
        }

        return refPage;
    }


    // cancels the selected cases
    public String btnCancelCase_action() {

        // get selected case
        String choice = getSelectedCaseID() ;
        if (choice != null) {
            String result = cancelCase(choice) ;
            if (! _rm.successful(result))
                msgPanel.error("Could not cancel case.\n\n" +  msgPanel.format(result)) ;
        }
        else msgPanel.error("No case selected to cancel.");

        _sb.setRunningCaseListChoices(null) ;
        return null;
    }

    
    private String getSelectedCaseID() {
        String result = null ;
        String choice = _sb.getRunningCaseListChoice() ;
        if ((choice != null) && (choice.length() > 0)) {
            result = choice.substring(0, choice.indexOf(':')) ;    // get casenbr prefix
        }
        return result;
    }


    // cancels the case with the id passed
    private String cancelCase(String caseID) {
        try {
            _sb.setRunningCaseListChoice(null) ;
            getApplicationBean().removeWorkItemParamsForCase(caseID);            
            return _rm.cancelCase(caseID, _sb.getSessionhandle());
        }
        catch (IOException ioe) {
            msgPanel.error("IOException when attempting to cancel case.") ;
            return null;
        }
    }


    // unloads the chosen spec from the engine
    private String unloadSpec(SpecificationData spec) {
        try {
            return _rm.unloadSpecification(spec.getID());
        }
        catch (IOException ioe) {
            msgPanel.error("IOException when attempting to unload specification.") ;
            return null ;
        }
    }


    // attempts to unload a spec from the engine
    public String btnUnload_action() {

        // get selected spec
        try {
            Integer selectedRowIndex = new Integer((String) hdnRowIndex.getValue());
            SpecificationData spec = _sb.getLoadedSpec(selectedRowIndex);
            if (spec != null) {
                String result = unloadSpec(spec) ;
                if (! result.contains("success")) {
                    msgPanel.error(result);
                }
                else {
                    hdnRowIndex.setValue(null);
                    _sb.refreshLoadedSpecs();
                }
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
            if (! inputParams.isEmpty() && ! specData.hasExternalCaseDataGateway()) {
                _sb.setDynFormType(ApplicationBean.DynFormType.netlevel);
                _sb.setLoadedSpecListChoice(specData);

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
                beginCase(specData.getID());
            }
        }
        return null ;
    }


    // starts a new case (either directly from startCase() or via a postback action)
    private void beginCase(YSpecificationID specID) {
        String caseData = null ;
        String result ;
        if (_sb.isCaseLaunch()) {
            caseData = getDynFormFactory().getDataList();
            _sb.setCaseLaunch(false);
        }
        try {
            result = _rm.launchCase(specID, caseData, _sb.getSessionhandle());
            if (_rm.successful(result))
                updateRunningCaseList();
            else
                msgPanel.error("Unsuccessful case start:" + msgPanel.format(result)) ;
        }
        catch (IOException ioe) {
            msgPanel.error("IOException when attempting to launch case.") ;
        }
    }


    // refreshes list of running cases
    private void updateRunningCaseList() {
        XNode node = _rm.getAllRunningCases();
        ArrayList<String> caseList = new ArrayList<String>();
        if (node != null) {
            for (XNode specNode : node.getChildren()) {
                 for (XNode caseID : specNode.getChildren()) {
                     String line = String.format("%s: %s (%s)", caseID.getText(),
                                                 specNode.getAttributeValue("uri"),
                                                 specNode.getAttributeValue("version"));
                     caseList.add(line) ;

                 }
            }

            // sort the list using a treeset
            TreeSet<String> caseTree = new TreeSet<String>(caseList) ;

            // convert to options array
            Option[] options = new Option[caseTree.size()] ;
            int i = 0 ;
            for (String caseStr : caseTree) options[i++] = new Option(caseStr) ;
            _sb.setRunningCaseListOptions(options);
        }
    }


    private void showSpecInfo() {
        try {
            Integer selectedRowIndex = new Integer((String) hdnRowIndex.getValue());
            SpecificationData spec = _sb.getLoadedSpec(selectedRowIndex);

            if (spec != null) {
                msgPanel.setTitleText("Specification Meta Data");
                msgPanel.info("URI: " + spec.getSpecURI());
                msgPanel.info("VERSION: " + spec.getSpecVersion());
                msgPanel.info("DESCRIPTION: " + spec.getDocumentation());
                if (spec.getSpecIdentifier() != null) {
                    msgPanel.info("IDENTIFIER: " + spec.getSpecIdentifier());
                }
                if ((spec.getMetaTitle() != null) && (spec.getMetaTitle().length() > 0)) {
                    msgPanel.info("META-TITLE: " + spec.getMetaTitle());
                }
                if (spec.getSchemaVersion() != null) {
                    msgPanel.info("SCHEMA VERSION: " + spec.getSchemaVersion());
                }
                if (spec.getAuthors() != null) {
                    msgPanel.info("AUTHOR(S): " + spec.getAuthors());
                }
                if (spec.getRootNetID() != null) {
                    msgPanel.info("ROOT NET: " + spec.getRootNetID());
                }
                if (spec.getStatus() != null) {
                    msgPanel.info("ENGINE STATUS: " + spec.getStatus());
                }
                if (spec.getExternalDataGateway() != null) {
                    msgPanel.info("EXTERNAL DATA GATEWAY: " + spec.getExternalDataGateway());
                }
            }
        }
        catch (NumberFormatException nfe) {
            msgPanel.error("Please select a specification to get info for.") ;
        }        
    }


    private void downloadLog() {
        try {
            Integer selectedRowIndex = new Integer((String) hdnRowIndex.getValue());
            SpecificationData spec = _sb.getLoadedSpec(selectedRowIndex);

            if (spec != null) {
                String log = LogMiner.getInstance().getMergedXESLog(spec.getID(), true);
                if (log != null) {
                    String filename = String.format("%s%s_xes.xml", spec.getSpecURI(),
                            spec.getSpecVersion());
                    FacesContext context = FacesContext.getCurrentInstance();
                    HttpServletResponse response =
                            ( HttpServletResponse ) context.getExternalContext().getResponse();
                    response.setContentType("text/xml");
                    response.setHeader("Content-Disposition",
                            "attachment;filename=\"" + filename + "\"");
                    OutputStream os = response.getOutputStream();
                    OutputStreamWriter osw = new OutputStreamWriter(os);
                    osw.write(log);
                    osw.flush();
                    osw.close();
                    FacesContext.getCurrentInstance().responseComplete();
               //     msgPanel.success("Data successfully exported to file '" + filename + "'.");
                }
                else msgPanel.error("Unable to create export file: malformed xml.");
            }
        }
        catch (IOException ioe) {
            msgPanel.error("Unable to create export file. Please see the log for details.");
        }
        catch (NumberFormatException nfe) {
            msgPanel.error("Please select a specification to download the log for.") ;
        }
    }


    private void activateButtons() {
        List<SpecificationData> list = _sb.getLoadedSpecs();
        boolean noSpecsSelected = (list == null) || list.isEmpty() ;
        btnUnload.setDisabled(noSpecsSelected);
        btnLaunch.setDisabled(noSpecsSelected);
    }


    private boolean hasUniqueDescriptors(String specxml) {
        if ((specxml == null) || (specxml.length() == 0)) {
            msgPanel.error("Invalid specification file: null or empty contents.");
            return false;
        }
        YSpecificationID specID = getDescriptors(specxml);
        if (specID != null) {
            if (! specID.isValid()) {
                msgPanel.error("Invalid specification: missing identifier or incorrect version.");
                return false;
            }
            List<SpecificationData> loadedSpecs = _sb.getLoadedSpecs();
            if (loadedSpecs != null) {
                for (SpecificationData spec : loadedSpecs) {
                    if (spec.getID().equals(specID)) {
                        if (specID.getUri().equals(spec.getSpecURI())) {
                            msgPanel.error("This specification is already loaded.");
                        }
                        else {
                            msgPanel.error("A specification with the same id and " +
                                    "version (but different name) is already loaded.");                            
                        }
                        return false;
                    }
                    else if (specID.isPreviousVersionOf(spec.getID())) {
                        if (specID.getUri().equals(spec.getSpecURI())) {
                            msgPanel.error("A later version of this specification is " +
                                    "already loaded.");
                        }
                        else {
                            msgPanel.error("A later version of a specification with the " +
                                    "same id (but different name) is already loaded.");                            
                        }
                        return false;
                    }
                    else if (specID.getUri().equals(spec.getSpecURI()) &&
                             (! specID.hasMatchingIdentifier(spec.getID()))) {
                        msgPanel.error("A specification with the same name, but a different " +
                                "id, is already loaded. Please change the name and try again.");
                        return false;
                    }
                }
            }
            return true;                                // no loaded or matching specs
        }
        return false;                            // null specID means problem with spec
    }


    private YSpecificationID getDescriptors(String specxml) {
        YSpecificationID descriptors = null;
        XNode specNode = new XNodeParser().parse(specxml);
        if (specNode != null) {
            String schemaVersion = specNode.getAttributeValue("version");
            XNode specification = specNode.getChild("specification");
            if (specification != null) {
                String uri = specification.getAttributeValue("uri");
                String version = "0.1";
                String uid = null;
                if ((schemaVersion != null) && schemaVersion.startsWith("2.")) {
                    XNode metadata = specification.getChild("metaData");
                    version = metadata.getChildText("version");
                    uid = metadata.getChildText("identifier");
                }
                descriptors = new YSpecificationID(uid, version, uri);
            }
            else msgPanel.error("Malformed specification: 'specification' node not found.");
        }
        else msgPanel.error("Malformed specification: unable to parse.");
        
        return descriptors;
    }

    
    private void processErrorMsg(String msg) {
        Element root = JDOMUtil.stringToElement(msg);
        if (root != null) {
            Element reason = root.getChild("reason");
            if (reason != null) {
                List children = reason.getChildren();
                if (children != null) {
                    for (Object child : children) {
                        Element e = (Element) child;
                        if (e.getName().equals("warning")) {
                            msgPanel.warn(JDOMUtil.elementToStringDump(e));
                        }
                        else if (e.getName().equals("error")) {
                            msgPanel.error(JDOMUtil.elementToStringDump(e));
                            msgPanel.setTitleText("Upload Specification Failed");
                        }
                        else {
                            msgPanel.info(JDOMUtil.elementToStringDump(e));
                        }
                    }
                }
            }
        }
    }


    private void showMessagePanel() {
        body1.setFocus(msgPanel.hasMessage() ? "form1:pfMsgPanel:btnOK001" :
                "form1:lbxRunningCases");        
        _sb.showMessagePanel();
    }
}

