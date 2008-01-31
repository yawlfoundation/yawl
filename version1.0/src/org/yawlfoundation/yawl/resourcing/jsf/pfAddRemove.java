/*
 * pfAddRemove.java
 *
 * Created on 26 January 2008, 21:34
 * Copyright adamsmj
 */
package org.yawlfoundation.yawl.resourcing.jsf;

import com.sun.rave.web.ui.appbase.AbstractFragmentBean;
import javax.faces.FacesException;

import com.sun.rave.web.ui.model.DefaultOptionsList;
import com.sun.rave.web.ui.model.MultipleSelectOptionsList;
import com.sun.rave.web.ui.model.Option;
import com.sun.rave.web.ui.component.*;
import org.yawlfoundation.yawl.resourcing.resource.Participant;

/**
 * <p>Fragment bean that corresponds to a similarly named JSP page
 * fragment.  This class contains component definitions (and initialization
 * code) for all components that you have defined on this fragment, as well as
 * lifecycle methods and event handlers where you may add behavior
 * to respond to incoming events.</p>
 */
public class pfAddRemove extends AbstractFragmentBean {
    // <editor-fold defaultstate="collapsed" desc="Creator-managed Component Definition">
    private int __placeholder;
    
    /**
     * <p>Automatically managed component initialization. <strong>WARNING:</strong>
     * This method is automatically generated, so any user-specified code inserted
     * here is subject to being replaced.</p>
     */
    private void _init() throws Exception {
    }

    private Button btnDelete = new Button();

    public Button getBtnDelete() {
        return btnDelete;
    }

    public void setBtnDelete(Button b) {
        this.btnDelete = b;
    }

    private Button btnNew = new Button();

    public Button getBtnNew() {
        return btnNew;
    }

    public void setBtnNew(Button b) {
        this.btnNew = b;
    }

    private TextField txtNew = new TextField();

    public TextField getTxtNew() {
        return txtNew;
    }

    public void setTxtNew(TextField tf) {
        this.txtNew = tf;
    }

    private Button btnUnselect = new Button();

    public Button getBtnUnselect() {
        return btnUnselect;
    }

    public void setBtnUnselect(Button b) {
        this.btnUnselect = b;
    }

    private Button btnSelect = new Button();

    public Button getBtnSelect() {
        return btnSelect;
    }

    public void setBtnSelect(Button b) {
        this.btnSelect = b;
    }

    private Listbox lbxOwns = new Listbox();

    public Listbox getLbxOwns() {
        return lbxOwns;
    }

    public void setLbxOwns(Listbox l) {
        this.lbxOwns = l;
    }

    private DefaultOptionsList listbox1DefaultOptions = new DefaultOptionsList();

    public DefaultOptionsList getListboxDefaultOptions1() {
        return listbox1DefaultOptions;
    }

    public void setListbox1DefaultOptions(DefaultOptionsList dol) {
        this.listbox1DefaultOptions = dol;
    }

    private Listbox lbxAvailable = new Listbox();

    public Listbox getLbxAvailable() {
        return lbxAvailable;
    }

    public void setLbxAvailable(Listbox l) {
        this.lbxAvailable = l;
    }

    private DefaultOptionsList listbox2DefaultOptions = new DefaultOptionsList();

    public DefaultOptionsList getListbox2DefaultOptions() {
        return listbox2DefaultOptions;
    }

    public void setListbox2DefaultOptions(DefaultOptionsList dol) {
        this.listbox2DefaultOptions = dol;
    }

    private Label label1 = new Label();

    public Label getLabel1() {
        return label1;
    }

    public void setLabel1(Label l) {
        this.label1 = l;
    }

    private Label label2 = new Label();

    public Label getLabel2() {
        return label2;
    }

    public void setLabel2(Label l) {
        this.label2 = l;
    }

    // </editor-fold>
    
    public pfAddRemove() {
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
     * <p>Callback method that is called whenever a page containing
     * this page fragment is navigated to, either directly via a URL,
     * or indirectly via page navigation.  Override this method to acquire
     * resources that will be needed for event handlers and lifecycle methods.</p>
     * 
     * <p>The default implementation does nothing.</p>
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
            log("pfAddRemove Initialization Failure", e);
            throw e instanceof FacesException ? (FacesException) e: new FacesException(e);
        }
        // </editor-fold>
        // Perform application initialization that must complete
        // *after* managed components are initialized
        // TODO - add your own initialization code here

    }

    /** 
     * <p>Callback method that is called after rendering is completed for
     * this request, if <code>init()</code> was called.  Override this
     * method to release resources acquired in the <code>init()</code>
     * resources that will be needed for event handlers and lifecycle methods.</p>
     * 
     * <p>The default implementation does nothing.</p>
     */
    public void destroy() {
    }




    public String btnSelect_action() {
        String id = (String) lbxAvailable.getSelected() ;
        if (id != null) {
            SessionBean sb = getSessionBean();
            sb.selectResourceAttribute(id) ;
            populateLists(sb.getActiveResourceAttributeTab(), sb.getEditedParticipant());
        }
        return null;
    }


    public String btnUnselect_action() {
        String id = (String) lbxOwns.getSelected() ;
        if (id != null) {
            SessionBean sb = getSessionBean();
            sb.unselectResourceAttribute(id) ;
            populateLists(sb.getActiveResourceAttributeTab(), sb.getEditedParticipant());
        }
        return null;
    }

    public void populateLists(String selTab, Participant p) {
        if (selTab == null) selTab = "tabRoles" ;
        lbxAvailable.setItems(getSessionBean().getFullResourceAttributeList(selTab)) ;
        lbxOwns.setItems(getSessionBean().getParticipantAttributeList(selTab, p));
    }
}
