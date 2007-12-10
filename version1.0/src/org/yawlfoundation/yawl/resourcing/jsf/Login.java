/*
 * Login.java
 *
 * Created on October 21, 2007, 6:32 PM
 * Copyright adamsmj
 */
package org.yawlfoundation.yawl.resourcing.jsf;

import org.yawlfoundation.yawl.engine.interfce.Interface_Client;
import org.yawlfoundation.yawl.resourcing.rsInterface.WorkQueueGateway;
import com.sun.rave.web.ui.appbase.AbstractPageBean;
import com.sun.rave.web.ui.component.*;

import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlPanelGrid;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.validator.LengthValidator;
import javax.faces.validator.ValidatorException;

/**
 * <p>Page bean that corresponds to a similarly named JSP page.  This
 * class contains component definitions (and initialization code) for
 * all components that you have defined on this page, as well as
 * lifecycle methods and event handlers where you may add behavior
 * to respond to incoming events.</p>
 */
public class Login extends AbstractPageBean {
    // <editor-fold defaultstate="collapsed" desc="Creator-managed Component Definition">
    private int __placeholder;
    
    /**
     * <p>Automatically managed component initialization.  <strong>WARNING:</strong>
     * This method is automatically generated, so any user-specified code inserted
     * here is subject to being replaced.</p>
     */
    private void _init() throws Exception {
        valLenUserName.setMaximum(20);
        valLenUserName.setMinimum(4);
        valLenPassword.setMaximum(20);
        valLenPassword.setMinimum(4);
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

    private PanelGroup groupPanel1 = new PanelGroup();

    public PanelGroup getGroupPanel1() {
        return groupPanel1;
    }

    public void setGroupPanel1(PanelGroup pg) {
        this.groupPanel1 = pg;
    }

    private HtmlPanelGrid gridPanel1 = new HtmlPanelGrid();

    public HtmlPanelGrid getGridPanel1() {
        return gridPanel1;
    }

    public void setGridPanel1(HtmlPanelGrid hpg) {
        this.gridPanel1 = hpg;
    }

    private HtmlPanelGrid gridPanel2 = new HtmlPanelGrid();

    public HtmlPanelGrid getGridPanel2() {
        return gridPanel2;
    }

    public void setGridPanel2(HtmlPanelGrid hpg) {
        this.gridPanel2 = hpg;
    }

    private Button btnLogin = new Button();

    public Button getBtnLogin() {
        return btnLogin;
    }

    public void setBtnLogin(Button b) {
        this.btnLogin = b;
    }

    private Label label2 = new Label();

    public Label getLabel2() {
        return label2;
    }

    public void setLabel2(Label l) {
        this.label2 = l;
    }

    private TextField txtUserName = new TextField();

    public TextField getTxtUserName() {
        return txtUserName;
    }

    public void setTxtUserName(TextField tf) {
        this.txtUserName = tf;
    }

    private Label label1 = new Label();

    public Label getLabel1() {
        return label1;
    }

    public void setLabel1(Label l) {
        this.label1 = l;
    }

    private PasswordField txtPassword = new PasswordField();

    public PasswordField getTxtPassword() {
        return txtPassword;
    }

    public void setTxtPassword(PasswordField pf) {
        this.txtPassword = pf;
    }

    private LengthValidator valLenUserName = new LengthValidator();

    public LengthValidator getValLenUserName() {
        return valLenUserName;
    }

    public void setValLenUserName(LengthValidator lv) {
        this.valLenUserName = lv;
    }

    private LengthValidator valLenPassword = new LengthValidator();

    public LengthValidator getValLenPassword() {
        return valLenPassword;
    }

    public void setValLenPassword(LengthValidator lv) {
        this.valLenPassword = lv;
    }

    private Message message1 = new Message();

    public Message getMessage1() {
        return message1;
    }

    public void setMessage1(Message m) {
        this.message1 = m;
    }

    private Message message2 = new Message();

    public Message getMessage2() {
        return message2;
    }

    public void setMessage2(Message m) {
        this.message2 = m;
    }
    
    // </editor-fold>


    /** 
     * <p>Construct a new Page bean instance.</p>
     */
    public Login() {
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
     * <p>Return a reference to the scoped data bean.</p>
     */
    protected ApplicationBean getApplicationBean() {
        return (ApplicationBean)getBean("ApplicationBean");
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
            log("Page1 Initialization Failure", e);
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


    public void textField1_processValueChange(ValueChangeEvent event) {
        // TODO: Replace with your code
        
    }


    public String btnLogin_action() {
        // TODO: Process the button click action. Return value is a navigation
        // case name where null will return to the same page.
        String user = (String) txtUserName.getText() ;
        String pword = (String) txtPassword.getText();
        if (validateUser(user, pword)) return "showUserQueues" ;
        return null;
    }


    public void txtUserName_validate(FacesContext context, UIComponent component, Object value) {
        // TODO: Check the value parameter here, and if not valid, do something like this:
        // throw new ValidatorException(new FacesMessage("Not a valid value!"));
        String s = (String) value ;
        if (s.length() < 4) throw new ValidatorException(new FacesMessage("Not a valid value!"));
    }
    
    private boolean validateUser(String u, String p) {
        String handle = getApplicationBean().getWorkQueueGateway().login(u, p);
        if (Interface_Client.successful(handle)) {
            WorkQueueGateway wqg = getApplicationBean().getWorkQueueGateway();
            SessionBean sb = getSessionBean();
            sb.setSessionhandle(handle);
            sb.setParticipant(wqg.getParticipantFromUserID(u));
            return true ;
        }
        else {
   //         throw new ValidatorException(new FacesMessage("Not a valid value!"));
            return false ;
        }
    }
}

