/*
 * dynForm.java
 *
 * Created on 6 January 2008, 11:57
 * Copyright adamsmj
 */

package org.yawlfoundation.yawl.resourcing.jsf;

import com.sun.rave.web.ui.appbase.AbstractPageBean;
import com.sun.rave.web.ui.component.*;

import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import java.util.HashMap;
import java.util.Map;

public class dynForm extends AbstractPageBean {

    // Constructor
     public dynForm() { }

    /****** JSF Required Members and Methods ***************************************/

    private int __placeholder;

    private void _init() throws Exception { }

    /** @return a reference to the application scoped data bean. */
    protected ApplicationBean getApplicationBean() {
        return (ApplicationBean) getBean("ApplicationBean");
    }

    /** @return a reference to the session scoped data bean. */
    protected SessionBean getSessionBean() {
        return (SessionBean) getBean("SessionBean");
    }

    /** @return a reference to the session scoped factory bean. */
    private DynFormFactory getDynFormFactory() {
        return (DynFormFactory) getBean("DynFormFactory");
    }

    public void init() {
        super.init();

        // *Note* - JSF requirement this block should NOT be modified
        try {
            _init();
        } catch (Exception e) {
            log("dynForm Initialization Failure", e);
            throw e instanceof FacesException ? (FacesException) e: new FacesException(e);
        }
    }

    /****** Abstract Method Implementations *************************************/

    public void preprocess() {
    }

    public void prerender() {
        getSessionBean().checkLogon();
        getSessionBean().getMessagePanel().show(140, 500, "absolute");
        getSessionBean().setActivePage(ApplicationBean.PageRef.dynForm);
        setOKText();
    }

    public void destroy() { }


    /****** Page Components ****************************************************/

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


    private StaticText txtHeader = new StaticText();

    public StaticText getTxtHeader() { return txtHeader; }

    public void setTxtHeader(StaticText st) { txtHeader = st; }


    private Button btnOK = new Button();

    public Button getBtnOK() { return btnOK; }

    public void setBtnOK(Button b) { btnOK = b; }


    private Button btnCancel = new Button();

    public Button getBtnCancel() { return btnCancel; }

    public void setBtnCancel(Button b) { btnCancel = b; }


    /****** Custom Methods ******************************************************/

    /**
     * Updates workitem parameters with values added by user
     * @return a reference to the referring page
     */
    public String btnOK_action() {
        String referringPage;
        SessionBean sb = getSessionBean();
        if (! getDynFormFactory().validateInputs())
            return null;

        if (sb.getDynFormType() == ApplicationBean.DynFormType.netlevel) {
            sb.setCaseLaunch(true);                        // temp flag for post action
            referringPage = "showCaseMgt";
        }
        else {
            sb.setWirEdit(true) ;
            referringPage = "showUserQueues";
        }
        return referringPage;
    }


    /**
     * Returns to referring page without saving changed values
     * @return a reference to the referring page
     */
    public String btnCancel_action() {
        if (getSessionBean().getDynFormType() == ApplicationBean.DynFormType.netlevel)
           return "showCaseMgt";
        else
           return "showUserQueues";
    }

    /******************************************************************************/

    // This member and method are used as value change listeners for dynamic
    // components. They aren't currently used but have been left in case of
    // future need.
            
    private Map<String, String> updateMap = new HashMap<String, String>();

    public void saveValueChange(ValueChangeEvent event) {
        String value ;
        UIComponent source = event.getComponent() ;

        if (source instanceof Checkbox)
            value = event.getNewValue().toString();
        else
            value = (String) event.getNewValue() ;

        updateMap.put(source.getId(), value);
    }

    private void setOKText() {
        SessionBean sb = getSessionBean() ;
        if (sb.getDynFormType() == ApplicationBean.DynFormType.netlevel)
            btnOK.setText("Start");
        else
            btnOK.setText("Save");
    }

    public String btnOccursAction(ActionEvent event) {
        Button source = (Button) event.getComponent() ;
        SubPanel parent = (SubPanel) source.getParent();
        String btnType = (String) source.getText();
        getApplicationBean().refresh();
        getDynFormFactory().processOccursAction(parent, btnType);
        return null;
    }

}



