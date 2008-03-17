/*
 * customServices.java
 *
 * Created on 24 January 2008, 16:09
 * Copyright adamsmj
 */
package org.yawlfoundation.yawl.resourcing.jsf;

import com.sun.rave.web.ui.appbase.AbstractPageBean;
import com.sun.rave.web.ui.component.*;
import org.yawlfoundation.yawl.elements.YAWLServiceReference;

import javax.faces.FacesException;
import javax.faces.component.UIColumn;
import javax.faces.component.html.HtmlDataTable;
import javax.faces.component.html.HtmlOutputText;
import java.util.List;

/**
 * <p>Page bean that corresponds to a similarly named JSP page.  This
 * class contains component definitions (and initialization code) for
 * all components that you have defined on this page, as well as
 * lifecycle methods and event handlers where you may add behavior
 * to respond to incoming events.</p>
 */
public class customServices extends AbstractPageBean {
    // <editor-fold defaultstate="collapsed" desc="Creator-managed Component Definition">
    private int __placeholder;
    
    /**
     * <p>Automatically managed component initialization.  <strong>WARNING:</strong>
     * This method is automatically generated, so any user-specified code inserted
     * here is subject to being replaced.</p>
     */
    private void _init() throws Exception {
    }
    
    private Page page1 = new Page();
    
    public Page getPage1() {
        return page1;
    }
    
    public void setPage1(Page p) {
        this.page1 = p;
    }
    
    private Html html1 = new Html();
    
    public Html getHtml1() {
        return html1;
    }
    
    public void setHtml1(Html h) {
        this.html1 = h;
    }
    
    private Head head1 = new Head();
    
    public Head getHead1() {
        return head1;
    }
    
    public void setHead1(Head h) {
        this.head1 = h;
    }
    
    private Link link1 = new Link();
    
    public Link getLink1() {
        return link1;
    }
    
    public void setLink1(Link l) {
        this.link1 = l;
    }
    
    private Body body1 = new Body();
    
    public Body getBody1() {
        return body1;
    }
    
    public void setBody1(Body b) {
        this.body1 = b;
    }
    
    private Form form1 = new Form();
    
    public Form getForm1() {
        return form1;
    }
    
    public void setForm1(Form f) {
        this.form1 = f;
    }

    private PanelLayout pnlAddService = new PanelLayout();

    public PanelLayout getPnlAddService() {
        return pnlAddService;
    }

    public void setPnlAddService(PanelLayout pl) {
        this.pnlAddService = pl;
    }

    private StaticText staticText1 = new StaticText();

    public StaticText getStaticText1() {
        return staticText1;
    }

    public void setStaticText1(StaticText st) {
        this.staticText1 = st;
    }

    private TextField txtName = new TextField();

    public TextField getTxtName() {
        return txtName;
    }

    public void setTxtName(TextField tf) {
        this.txtName = tf;
    }

    private TextField txtURL = new TextField();

    public TextField getTxtURL() {
        return txtURL;
    }

    public void setTxtURL(TextField tf) {
        this.txtURL = tf;
    }

    private TextArea txtDescription = new TextArea();

    public TextArea getTxtDescription() {
        return txtDescription;
    }

    public void setTxtDescription(TextArea ta) {
        this.txtDescription = ta;
    }

    private PanelLayout pnlServices = new PanelLayout();

    public PanelLayout getPnlServices() {
        return pnlServices;
    }

    public void setPnlServices(PanelLayout pl) {
        this.pnlServices = pl;
    }

    private StaticText staticText2 = new StaticText();

    public StaticText getStaticText2() {
        return staticText2;
    }

    public void setStaticText2(StaticText st) {
        this.staticText2 = st;
    }

    private HtmlDataTable dataTable1 = new HtmlDataTable();

    public HtmlDataTable getDataTable1() {
        return dataTable1;
    }

    public void setDataTable1(HtmlDataTable hdt) {
        this.dataTable1 = hdt;
    }

//    private DefaultTableDataModel dataTable1Model = new DefaultTableDataModel();
//
//    public DefaultTableDataModel getDataTable1Model() {
//        return dataTable1Model;
//    }
//
//    public void setDataTable1Model(DefaultTableDataModel dtdm) {
//        this.dataTable1Model = dtdm;
//    }

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

    private UIColumn colURI = new UIColumn();

    public UIColumn getColURI() {
        return colURI;
    }

    public void setColURI(UIColumn uic) {
        this.colURI = uic;
    }

    private HtmlOutputText colURIRows = new HtmlOutputText();

    public HtmlOutputText getColURIRows() {
        return colURIRows;
    }

    public void setColURIRows(HtmlOutputText hot) {
        this.colURIRows = hot;
    }

    private HtmlOutputText colURIHeader = new HtmlOutputText();

    public HtmlOutputText getColURIHeader() {
        return colURIHeader;
    }

    public void setColURIHeader(HtmlOutputText hot) {
        this.colURIHeader = hot;
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

    private Button btnRemove = new Button();

    public Button getBtnRemove() {
        return btnRemove;
    }

    public void setBtnRemove(Button b) {
        this.btnRemove = b;
    }

    private Button btnAdd = new Button();

    public Button getBtnAdd() {
        return btnAdd;
    }

    public void setBtnAdd(Button b) {
        this.btnAdd = b;
    }

    private Button btnClear = new Button();

    public Button getBtnClear() {
        return btnClear;
    }

    public void setBtnClear(Button b) {
        btnClear = b;
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



    private List<YAWLServiceReference> dataList ;

    public List<YAWLServiceReference> getDataList() {
        return dataList;
    }

    public void setDataList(List<YAWLServiceReference> dataList) {
        this.dataList = dataList;
    }

    // </editor-fold>


    /** 
     * <p>Construct a new Page bean instance.</p>
     */
    public customServices() {
    }

    /** 
     * <p>Return a reference to the scoped data bean.</p>
     */
    protected ApplicationBean getApplicationBean() {
        return (ApplicationBean)getBean("ApplicationBean");
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
    protected RequestBean getRequestBean() {
        return (RequestBean)getBean("RequestBean");
    }


    /** 
     * <p>Callback method that is called whenever a page is navigated to,
     * either directly via a URL, or indirectly via page navigation.
     * Customize this method to acquire resources that will be needed
     * for event handlers and lifecycle methods, whether or not this
     * page is performing post back processing.</p>
     * 
     * <p>Note that, if the current request is a postback, the property
     * values of the components do <strong>not</strong> represent any
     * values submitted with this request.  Instead, they represent the
     * property values that were saved for this view when it was rendered.</p>
     */
    public void init() {
        // Perform initializations inherited from our superclass
        super.init();
        // Perform application initialization that must complete
        // *before* managed components are initialized
        // TODO - add your own initialiation code here

        // <editor-fold defaultstate="collapsed" desc="Creator-managed Component Initialization">
        // Initialize automatically managed components
        // *Note* - this logic should NOT be modified
        try {
            _init();
        } catch (Exception e) {
            log("customServices Initialization Failure", e);
            throw e instanceof FacesException ? (FacesException) e: new FacesException(e);
        }
        // </editor-fold>
        // Perform application initialization that must complete
        // *after* managed components are initialized
        // TODO - add your own initialization code here

    }

    /** 
     * <p>Callback method that is called after the component tree has been
     * restored, but before any event processing takes place.  This method
     * will <strong>only</strong> be called on a postback request that
     * is processing a form submit.  Customize this method to allocate
     * resources that will be required in your event handlers.</p>
     */
    public void preprocess() {
    }

    /** 
     * <p>Callback method that is called just before rendering takes place.
     * This method will <strong>only</strong> be called for the page that
     * will actually be rendered (and not, for example, on a page that
     * handled a postback and then navigated to a different page).  Customize
     * this method to allocate resources that will be required for rendering
     * this page.</p>
     */
    public void prerender() {
        getSessionBean().checkLogon();
        getSessionBean().getMessagePanel().show(338, 150, "absolute");
        getSessionBean().setActivePage(ApplicationBean.PageRef.customServices);
    }

    /** 
     * <p>Callback method that is called after rendering is completed for
     * this request, if <code>init()</code> was called (regardless of whether
     * or not this was the page that was actually rendered).  Customize this
     * method to release resources acquired in the <code>init()</code>,
     * <code>preprocess()</code>, or <code>prerender()</code> methods (or
     * acquired during execution of an event handler).</p>
     */
    public void destroy() {
    }


    public String btnRemove_action() {
        try {
            Integer selectedRowIndex = new Integer((String) hdnRowIndex.getValue());
            getSessionBean().removeRegisteredService(selectedRowIndex - 1);
        }
        catch (NumberFormatException nfe) {
            // message about row not selected
        }
        
        return null;
    }


    public String btnAdd_action() {
        String name = (String) txtName.getText() ;
        String uri = (String) txtURL.getText();
        String doco = (String) txtDescription.getText();
        getSessionBean().addRegisteredService(name, uri, doco);
        clearInputs();
        return null;
    }

    
    public String btnClear_action() {
        clearInputs();
        return null;
    }

    private void clearInputs() {
        txtName.setText("");
        txtURL.setText("");
        txtDescription.setText("");
    }
    
}

