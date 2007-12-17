/*
 * pfQueueUI.java
 *
 * Created on October 23, 2007, 2:37 PM
 * Copyright adamsmj
 */
package org.yawlfoundation.yawl.resourcing.jsf;

import com.sun.rave.web.ui.appbase.AbstractFragmentBean;
import com.sun.rave.web.ui.component.Label;
import com.sun.rave.web.ui.component.Listbox;
import com.sun.rave.web.ui.component.TextField;
import com.sun.rave.web.ui.model.DefaultOptionsList;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.resourcing.ResourceManager;
import org.yawlfoundation.yawl.resourcing.resource.Participant;

import javax.faces.FacesException;
import javax.faces.event.ValueChangeEvent;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Set;

/**
 * <p>Fragment bean that corresponds to a similarly named JSP page
 * fragment.  This class contains component definitions (and initialization
 * code) for all components that you have defined on this fragment, as well as
 * lifecycle methods and event handlers where you may add behavior
 * to respond to incoming events.</p>
 */
public class pfQueueUI extends AbstractFragmentBean {
    // <editor-fold defaultstate="collapsed" desc="Creator-managed Component Definition">
    private int __placeholder;
    
    /**
     * <p>Automatically managed component initialization. <strong>WARNING:</strong>
     * This method is automatically generated, so any user-specified code inserted
     * here is subject to being replaced.</p>
     */
    private void _init() throws Exception {
    }

    private Listbox lbxItems = new Listbox();

    public Listbox getLbxItems() {
        return lbxItems;
    }

    public void setLbxItems(Listbox l) {
        this.lbxItems = l;
    }

    private DefaultOptionsList lbxItemsDefaultOptions = new DefaultOptionsList();

    public DefaultOptionsList getLbxItemsDefaultOptions() {
        return lbxItemsDefaultOptions;
    }

    public void setLbxItemsDefaultOptions(DefaultOptionsList dol) {
        this.lbxItemsDefaultOptions = dol;
    }

    private Label lblSpecID = new Label();

    public Label getLblSpecID() {
        return lblSpecID;
    }

    public void setLblSpecID(Label l) {
        this.lblSpecID = l;
    }

    private TextField txtSpecID = new TextField();

    public TextField getTxtSpecID() {
        return txtSpecID;
    }

    public void setTxtSpecID(TextField tf) {
        this.txtSpecID = tf;
    }

    private Label lblCaseID = new Label();

    public Label getLblCaseID() {
        return lblCaseID;
    }

    public void setLblCaseID(Label l) {
        this.lblCaseID = l;
    }

    private Label lblCreated = new Label();

    public Label getLblCreated() {
        return lblCreated;
    }

    public void setLblCreated(Label l) {
        this.lblCreated = l;
    }

    private TextField txtCreated = new TextField();

    public TextField getTxtCreated() {
        return txtCreated;
    }

    public void setTxtCreated(TextField tf) {
        this.txtCreated = tf;
    }

    private TextField txtCaseID = new TextField();

    public TextField getTxtCaseID() {
        return txtCaseID;
    }

    public void setTxtCaseID(TextField tf) {
        this.txtCaseID = tf;
    }

    private TextField txtAge = new TextField();

    public TextField getTxtAge() {
        return txtAge;
    }

    public void setTxtAge(TextField tf) {
        this.txtAge = tf;
    }

    private TextField txtStatus = new TextField();

    public TextField getTxtStatus() {
        return txtStatus;
    }

    public void setTxtStatus(TextField tf) {
        this.txtStatus = tf;
    }

    private TextField txtTaskID = new TextField();

    public TextField getTxtTaskID() {
        return txtTaskID;
    }

    public void setTxtTaskID(TextField tf) {
        this.txtTaskID = tf;
    }

    private Label lblTaskID = new Label();

    public Label getLblTaskID() {
        return lblTaskID;
    }

    public void setLblTaskID(Label l) {
        this.lblTaskID = l;
    }

    private Label lblAge = new Label();

    public Label getLblAge() {
        return lblAge;
    }

    public void setLblAge(Label l) {
        this.lblAge = l;
    }

    private Label lblStatus = new Label();

    public Label getLblStatus() {
        return lblStatus;
    }

    public void setLblStatus(Label l) {
        this.lblStatus = l;
    }

    private Label lblItems = new Label();

    public Label getLblItems() {
        return lblItems;
    }

    public void setLblItems(Label l) {
        this.lblItems = l;
    }
    // </editor-fold>
    
    public pfQueueUI() {
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
    protected SessionBean getSessionBean() {
        return (SessionBean)getBean("SessionBean");
    }


    /** 
     * <p>Return a reference to the scoped data bean.</p>
     */
    protected ApplicationBean getApplicationBean() {
        return (ApplicationBean)getBean("ApplicationBean");
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
            log("pfQueueUI Initialization Failure", e);
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


    public void lbxItems_processValueChange(ValueChangeEvent event) {
        System.out.println("&&&&&&&& in process value changed &&&&&&&&");
        String itemID = (String) lbxItems.getSelected() ;
        if (itemID != null) {
            Participant p = getSessionBean().getParticipant();
            Set<WorkItemRecord> queue = p.getWorkQueues().getQueuedWorkItems(0);
            if (queue != null) {
                for (WorkItemRecord wir : queue) {
                    if (itemID.equals(wir.getID())) {
                        populateTextBoxes(wir) ;
                        break ;
                    }
                }
            }
            else System.out.println("%%%%%%% processValueChanged, queue is null");
        }    
    }

    
    protected void populateTextBoxes(WorkItemRecord wir) {
        System.out.println("&&&&&&&&&&&& popTextBoxes with = " + wir.getID()) ;

        String enabledStr ;

        txtCaseID.setText(wir.getCaseID());
        txtSpecID.setText(wir.getSpecificationID());
        txtTaskID.setText(wir.getTaskID());
        txtStatus.setText(wir.getStatus());

        long enabled = wir.getEnablementTimeMs();
        long age = System.currentTimeMillis() - enabled ;
        txtAge.setText(formatAge(age));
        enabledStr = DateFormat.getDateTimeInstance(
                            DateFormat.SHORT, DateFormat.SHORT).format(enabled);
        txtCreated.setText(enabledStr);
    }

    protected void clearQueueGUI() {
        getSessionBean().setListOptions(null);
        txtCaseID.setText(" ");
        txtSpecID.setText(" ");
        txtTaskID.setText(" ");
        txtStatus.setText(" ");
        txtCreated.setText(" ");
        txtAge.setText(" ");

    }


    private String formatAge(long age) {
        long secsPerHour = 60 * 60 ;
        long secsPerDay = 24 * secsPerHour ;
        age = age / 1000 ;                             // ignore the milliseconds

        long days = age / secsPerDay ;
        age %= secsPerDay ;
        long hours = age / secsPerHour ;
        age %= secsPerHour ;
        long mins = age / 60 ;
        age %= 60 ;                                    // seconds leftover
        return String.format("%d:%02d:%02d:%02d", days, hours, mins, age) ;
    }
}
