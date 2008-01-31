/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing.jsf;

import com.sun.rave.web.ui.appbase.AbstractPageBean;

import javax.faces.FacesException;
import javax.faces.component.html.HtmlDataTable;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.UIColumn;
import javax.faces.event.ValueChangeEvent;

import com.sun.rave.web.ui.model.UploadedFile;
import com.sun.rave.web.ui.model.Option;
import com.sun.rave.web.ui.component.*;

import org.yawlfoundation.yawl.resourcing.rsInterface.WorkQueueGateway;
import org.yawlfoundation.yawl.engine.interfce.SpecificationData;
import org.yawlfoundation.yawl.engine.interfce.Interface_Client;
import org.yawlfoundation.yawl.util.JDOMUtil;

import org.jdom.Element;

import java.util.*;
import java.io.IOException;

/**
 *  Backing bean that corresponds to a similarly named JSP page.
 *
 *  @author Michael Adams
 *  BPM Group, QUT Australia
 *  v0.1, 19/12/2007
 *
 *  Boilerplate code generated by Sun Java Studio Creator 2.1
 *
 *  Last Date: 05/01/2008
 */

public class caseMgt extends AbstractPageBean {

    /*******************************************************************************/
    /**************** START Creator auto generated code ****************************/
    /*******************************************************************************/

    private int __placeholder;

    private void _init() throws Exception { }
    
    private Page page1 = new Page();

    public Page getPage1() { return page1; }

    public void setPage1(Page p) { this.page1 = p; }

    private Html html1 = new Html();

    public Html getHtml1() { return html1; }

    public void setHtml1(Html h) { this.html1 = h; }

    private Head head1 = new Head();

    public Head getHead1() { return head1; }

    public void setHead1(Head h) { this.head1 = h; }

    private Link link1 = new Link();

    public Link getLink1() { return link1; }

    public void setLink1(Link l) { this.link1 = l; }

    private Body body1 = new Body();

    public Body getBody1() { return body1; }

    public void setBody1(Body b) { this.body1 = b; }

    private Form form1 = new Form();

    public Form getForm1() { return form1; }

    public void setForm1(Form f) { this.form1 = f; }

    private Upload fileUpload1 = new Upload();

    public Upload getFileUpload1() { return fileUpload1; }

    public void setFileUpload1(Upload u) { this.fileUpload1 = u; }

    private Button btnUpload = new Button();

    public Button getBtnUpload() { return btnUpload; }

    public void setBtnUpload(Button b) { this.btnUpload = b; }

    private StaticText staticText1 = new StaticText();

    public StaticText getStaticText1() { return staticText1; }

    public void setStaticText1(StaticText st) { this.staticText1 = st; }

    private Button btnLaunch = new Button();

    public Button getBtnLaunch() { return btnLaunch; }

    public void setBtnLaunch(Button b) { this.btnLaunch = b; }

    private Button btnCancelCase = new Button();

    public Button getBtnCancelCase() { return btnCancelCase; }

    public void setBtnCancelCase(Button b) { this.btnCancelCase = b; }

//    private Listbox lbxLoadedSpecs = new Listbox();
//
//    public Listbox getLbxLoadedSpecs() { return lbxLoadedSpecs; }
//
//    public void setLbxLoadedSpecs(Listbox l) { this.lbxLoadedSpecs = l; }

    private StaticText staticText2 = new StaticText();

    public StaticText getStaticText2() { return staticText2; }

    public void setStaticText2(StaticText st) { this.staticText2 = st; }

    private Listbox lbxRunningCases = new Listbox();

    public Listbox getLbxRunningCases() { return lbxRunningCases; }

    public void setLbxRunningCases(Listbox l) { this.lbxRunningCases = l; }

    private StaticText staticText3 = new StaticText();

    public StaticText getStaticText3() { return staticText3; }

    public void setStaticText3(StaticText st) { this.staticText3 = st; }

    private Button btnUnload = new Button();

    public Button getBtnUnload() { return btnUnload; }

    public void setBtnUnload(Button b) { this.btnUnload = b; }

    private MessageGroup msgBox = new MessageGroup();

    public MessageGroup getMsgBox() { return msgBox; }

    public void setMsgBox(MessageGroup mg) { this.msgBox = mg; }

    private PanelLayout layoutPanel1 = new PanelLayout();

    public PanelLayout getLayoutPanel1() { return layoutPanel1; }

    public void setLayoutPanel1(PanelLayout pl) { this.layoutPanel1 = pl; }

    private PanelLayout layoutPanel2 = new PanelLayout();

    public PanelLayout getLayoutPanel2() { return layoutPanel2; }

    public void setLayoutPanel2(PanelLayout pl) { this.layoutPanel2 = pl; }

    private PanelLayout layoutPanel3 = new PanelLayout();

    public PanelLayout getLayoutPanel3() { return layoutPanel3; }

    public void setLayoutPanel3(PanelLayout pl) { this.layoutPanel3 = pl; }

    private HtmlDataTable dataTable1 = new HtmlDataTable();

    public HtmlDataTable getDataTable1() {
        return dataTable1;
    }

    public void setDataTable1(HtmlDataTable hdt) {
        this.dataTable1 = hdt;
    }

    private UIColumn colName = new UIColumn();

    public UIColumn getColName() {
        return colName;
    }

    public void setColName(UIColumn uic) {
        this.colName = uic;
    }

    private HtmlOutputText colNameRows = new HtmlOutputText();

    public HtmlOutputText getColNameRows() {
        return colNameRows;
    }

    public void setColNameRows(HtmlOutputText hot) {
        this.colNameRows = hot;
    }

    private HtmlOutputText colNameHeader = new HtmlOutputText();

    public HtmlOutputText getColNameHeader() {
        return colNameHeader;
    }

    public void setColNameHeader(HtmlOutputText hot) {
        this.colNameHeader = hot;
    }

    private UIColumn colDescription = new UIColumn();

    public UIColumn getColDescription() {
        return colDescription;
    }

    public void setColDescription(UIColumn uic) {
        this.colDescription = uic;
    }

    private HtmlOutputText colDescriptionRows = new HtmlOutputText();

    public HtmlOutputText getColDescriptionRows() {
        return colDescriptionRows;
    }

    public void setColDescriptionRows(HtmlOutputText hot) {
        this.colDescriptionRows = hot;
    }

    private HtmlOutputText colDescriptionHeader = new HtmlOutputText();

    public HtmlOutputText getColDescriptionHeader() {
        return colDescriptionHeader;
    }

    public void setColDescriptionHeader(HtmlOutputText hot) {
        this.colDescriptionHeader = hot;
    }

    private UIColumn colSBar = new UIColumn();

    public UIColumn getColSBar() {
        return colSBar;
    }

    public void setColSBar(UIColumn uic) {
        this.colSBar = uic;
    }

    
    private HiddenField hdnRowIndex = new HiddenField();

    public HiddenField getHdnRowIndex() {
        return hdnRowIndex;
    }

    public void setHdnRowIndex(HiddenField hf) {
        this.hdnRowIndex = hf;
    }

    private Script script1 = new Script();

    public Script getScript1() {
        return script1;
    }

    public void setScript1(Script s) {
        this.script1 = s;
    }
    

    /** Constructor */
    public caseMgt() { }

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

    /** @return a reference to the session scoped factory bean. */
    private DynFormFactory getDynFormFactory() {
        return (DynFormFactory) getBean("DynFormFactory");
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

    public void fileUpload1_processValueChange(ValueChangeEvent event) { }

    /*******************************************************************************/
    /**************** END Creator auto generated code  *****************************/
    /*******************************************************************************/



    public void prerender() {
        if (getSessionBean().isCaseLaunch()) {
            String specID = getSessionBean().getLoadedSpecListChoice() ;
            if (specID != null)
                beginCase(specID, getSessionBean().getSessionhandle());    
        }
        updateRunningCaseList();
   //     updateLoadedSpecList() ;
        getSessionBean().setActivePage("caseMgt");        
    }

    private String stripPath(String fileName) {
        return stripPath(stripPath(fileName, '/'), '\\');
    }
    
    private String stripPath(String fileName, char slash) {
        int index = fileName.lastIndexOf(slash);
        if (index >= 0) fileName = fileName.substring(index + 1);
        return fileName ;
    }

    public String btnUpload_action() {
        UploadedFile uploadedFile = fileUpload1.getUploadedFile();

        if (uploadedFile != null) {
            String uploadedFileName = stripPath(uploadedFile.getOriginalName());
            String fileAsString = uploadedFile.getAsString() ;
            uploadSpec(uploadedFileName, fileAsString) ;
        }
        return null;
    }

    private void uploadSpec(String fileName, String fileContents) {
        int BOF = fileContents.indexOf("<?xml");
        int EOF = fileContents.indexOf("</specificationSet>");
        if (BOF != -1 && EOF != -1) {
            fileContents = fileContents.substring(BOF, EOF + 19) ;
            WorkQueueGateway wqg = getApplicationBean().getWorkQueueGateway() ;
            String handle = getSessionBean().getSessionhandle() ;
            String htmlErr = "";
            String result = wqg.uploadSpecification(fileContents, fileName, handle);
            if (Interface_Client.successful(result))
                getSessionBean().refreshLoadedSpecs();
        //    if(_worklistController.successful(replyFromYAWL)){}
        }
    }

    public String btnLaunch_action() {

        // get selected spec
        String refPage = null ;
        try {
            Integer selectedRowIndex = new Integer((String) hdnRowIndex.getValue());
            SpecificationData spec = getSessionBean().getLoadedSpec(selectedRowIndex - 1);
            refPage = startCase(spec) ;
        }
        catch (NumberFormatException nfe) {
            // message about row not selected
        }

        return refPage;
    }


    public String btnCancelCase_action() {

        // get selected case
        String choice = getSessionBean().getRunningCaseListChoice() ;
        if (choice != null) {
            choice = choice.substring(0, choice.indexOf(':')) ;
            cancelCase(choice) ;
        }    
        return null;
    }

    private String cancelCase(String caseID) {
        try {
            String handle = getSessionBean().getSessionhandle() ;
            WorkQueueGateway wqg = getApplicationBean().getWorkQueueGateway() ;
            return wqg.cancelCase(caseID, handle);           
        }
        catch (IOException ioe) {
            return null ;
        }
    }


    private String unloadSpec(String specID) {
        try {
            String handle = getSessionBean().getSessionhandle() ;
            WorkQueueGateway wqg = getApplicationBean().getWorkQueueGateway() ;
            return wqg.unloadSpecification(specID, handle);
        }
        catch (IOException ioe) {
            return null ;
        }
    }



    public String btnUnload_action() {

        // get selected spec
        try {
            Integer selectedRowIndex = new Integer((String) hdnRowIndex.getValue());
            SpecificationData spec = getSessionBean().getLoadedSpec(selectedRowIndex - 1);
            String result = unloadSpec(spec.getID()) ;
            if (result.indexOf("success") == -1) {
                error("An info message about this action");
            }
            else {
                getSessionBean().refreshLoadedSpecs();
            }
        }
        catch (NumberFormatException nfe) {
            // message about row not selected
        }
        return null;
    }


    private String startCase(SpecificationData specData) {

        if (specData != null) {
            List inputParams = specData.getInputParams();
            if (! inputParams.isEmpty()) {
                Map<String, FormParameter> paramMap =
                        getApplicationBean().yParamListToFormParamMap(inputParams) ;

                getSessionBean().setDynFormParams(paramMap);
                getSessionBean().setDynFormLevel("case");
                getSessionBean().setLoadedSpecListChoice(specData.getID());

                DynFormFactory df = (DynFormFactory) getBean("DynFormFactory");
                df.setHeaderText(
                        "Starting an Instance of Specification '" + specData.getID() + "'" );
                df.initDynForm(new ArrayList<FormParameter>(paramMap.values()),
                               "YAWL Case Management - Launch Case") ;
                return "showDynForm" ;
            }
            else {

                // no case params to worry about
                beginCase(specData.getID(), getSessionBean().getSessionhandle());
            }
        }
        return null ;
    }


    private void beginCase(String specID, String handle) {
        String caseData = null ;
        String result ;
        if (getSessionBean().isCaseLaunch()) {
            Map paramMap = getSessionBean().getDynFormParams();

            if ((paramMap != null) && (! paramMap.isEmpty())) {
                paramMap = getDynFormFactory().updateValues(paramMap) ;
                Element data = new Element(specID) ;
                for (Object o : paramMap.values()) {
                    FormParameter param = (FormParameter) o ;
                    Element child = new Element(param.getName());
                    child.setText(param.getValue());
                    data.addContent(child);
                }
                caseData = JDOMUtil.elementToStringDump(data) ;
            }
            getSessionBean().resetDynFormParams();
            getSessionBean().setCaseLaunch(false);
        }
        try {
            result = getApplicationBean().getWorkQueueGateway()
                                         .launchCase(specID, caseData, handle);
            if (Interface_Client.successful(result)) updateRunningCaseList();
        }
        catch (IOException ioe) {
            // something
        }
        // show result to user

    }

//    private void updateLoadedSpecList() {
//        WorkQueueGateway wqg = getApplicationBean().getWorkQueueGateway() ;
//        String handle = getSessionBean().getSessionhandle() ;
//        Set<SpecificationData> specDataSet = wqg.getLoadedSpecs(handle) ;
//        if (specDataSet != null) {
//
//            // put the items into a treeset so they are sorted
//            TreeSet<String> specInfo = new TreeSet<String>();
//            for (SpecificationData specData : specDataSet) {
//                String spec = specData.getID() + " :\t" + specData.getDocumentation() ;
//                specInfo.add(spec) ;
//            }
//
//            // now add them to the listbox
//            Option[] options = new Option[specInfo.size()] ;
//            int i = 0 ;
//            for (String specStr : specInfo) {
//                options[i++] = new Option(specStr) ;
//            }
//            getSessionBean().setLoadedSpecListOptions(options);
//        }
//    }

    private void updateRunningCaseList() {
        WorkQueueGateway wqg = getApplicationBean().getWorkQueueGateway() ;
        String handle = getSessionBean().getSessionhandle() ;
        Set<SpecificationData> specDataSet = wqg.getSpecList(handle) ;
        if (specDataSet != null) {
            ArrayList<Option> caseList = new ArrayList<Option>();
            for (SpecificationData specData : specDataSet) {
                List<String> caseIDs = wqg.getRunningCases(specData.getID(), handle);

                // srt the list using a treeset
                TreeSet<String> caseTree = new TreeSet<String>(caseIDs) ;
                for (String caseID : caseTree)
                    caseList.add(new Option(caseID + ": " + specData.getID())) ;
            }

            // convert to options array
            Option[] options = new Option[caseList.size()] ;
            int i = 0 ;
            for (Option option : caseList ) options[i++] = option ;          
            getSessionBean().setRunningCaseListOptions(options);
        }
    }




}

