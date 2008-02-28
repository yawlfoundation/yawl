/*
 * pfMenu.java
 *
 * Created on October 31, 2007, 10:41 AM
 * Copyright adamsmj
 */
package org.yawlfoundation.yawl.resourcing.jsf;

import com.sun.rave.web.ui.appbase.AbstractFragmentBean;
import com.sun.rave.web.ui.component.ImageComponent;
import com.sun.rave.web.ui.component.ImageHyperlink;
import com.sun.rave.web.ui.component.PanelLayout;

import javax.faces.FacesException;

/**
 * <p>Fragment bean that corresponds to a similarly named JSP page
 * fragment.  This class contains component definitions (and initialization
 * code) for all components that you have defined on this fragment, as well as
 * lifecycle methods and event handlers where you may add behavior
 * to respond to incoming events.</p>
 */
public class pfMenu extends AbstractFragmentBean {
    // <editor-fold defaultstate="collapsed" desc="Creator-managed Component Definition">
    private int __placeholder;
    
    /**
     * <p>Automatically managed component initialization. <strong>WARNING:</strong>
     * This method is automatically generated, so any user-specified code inserted
     * here is subject to being replaced.</p>
     */
    private void _init() throws Exception {
    }

    private PanelLayout layoutPanel1 = new PanelLayout();

    public PanelLayout getLayoutPanel1() {
        return layoutPanel1;
    }

    public void setLayoutPanel1(PanelLayout pl) {
        this.layoutPanel1 = pl;
    }

    private ImageHyperlink mnuLogout = new ImageHyperlink();

    public ImageHyperlink getMnuLogout() {
        return mnuLogout;
    }

    public void setMnuLogout(ImageHyperlink ih) {
        this.mnuLogout = ih;
    }

    private ImageHyperlink mnuMyProfile = new ImageHyperlink();

    public ImageHyperlink getMnuMyProfile() {
        return mnuMyProfile;
    }

    public void setMnuMyProfile(ImageHyperlink ih) {
        this.mnuMyProfile = ih;
    }

    private ImageHyperlink mnuUserWorkQueues = new ImageHyperlink();

    public ImageHyperlink getMnuUserWorkQueues() {
        return mnuUserWorkQueues;
    }

    public void setMnuUserWorkQueues(ImageHyperlink ih) {
        this.mnuUserWorkQueues = ih;
    }

    private ImageHyperlink mnuCaseMgt = new ImageHyperlink();

    public ImageHyperlink getMnuCaseMgt() {
        return mnuCaseMgt;
    }

    public void setMnuCaseMgt(ImageHyperlink ih) {
        this.mnuCaseMgt = ih;
    }

    private ImageHyperlink mnuServiceMgt = new ImageHyperlink();

    public ImageHyperlink getMnuServiceMgt() {
        return mnuServiceMgt;
    }

    public void setMnuServiceMgt(ImageHyperlink ih) {
        this.mnuServiceMgt = ih;
    }

    private ImageHyperlink mnuOrgDataMgt = new ImageHyperlink();

    public ImageHyperlink getMnuOrgDataMgt() {
        return mnuOrgDataMgt;
    }

    public void setMnuOrgDataMgt(ImageHyperlink ih) {
        this.mnuOrgDataMgt = ih;
    }

    private ImageHyperlink mnuAdminQueues = new ImageHyperlink();

    public ImageHyperlink getMnuAdminQueues() {
        return mnuAdminQueues;
    }

    public void setMnuAdminQueues(ImageHyperlink ih) {
        this.mnuAdminQueues = ih;
    }


    private ImageComponent imgSelected = new ImageComponent();

    public ImageComponent getImgSelected() {
        return imgSelected;
    }

    public void setImgSelected(ImageComponent ic) {
        this.imgSelected = ic;
    }
    // </editor-fold>
    
    public pfMenu() {
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
     * <p>Return a reference to the scoped data bean.</p>
     */
    protected SessionBean getSessionBean() {
        return (SessionBean)getBean("SessionBean");
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
            log("pfMenu Initialization Failure", e);
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


    public String mnuUserWorkQueues_action() {
        getSessionBean().checkLogon();
        getSessionBean().setMnuSelectorStyle("top: 72px");
        return "showUserQueues";
    }


    public String mnuMyProfile_action() {
        // TODO: Replace with your code
        
        return null;
    }


    public String mnuCaseMgt_action() {
        getSessionBean().checkLogon();
        getSessionBean().setMnuSelectorStyle("top: 100px");
        return "showCaseMgt";
    }


    public String mnuServiceMgt_action() {
        getSessionBean().checkLogon();
        getSessionBean().setMnuSelectorStyle("top: 128px");
        return "showServiceMgt";
    }


    public String mnuOrgDataMgt_action() {
        getSessionBean().checkLogon();
        getSessionBean().setMnuSelectorStyle("top: 152px");
        return "showEditOrgData";
    }


    public String mnuAdminQueues_action() {
        getSessionBean().checkLogon();
        getSessionBean().setMnuSelectorStyle("top: 180px");
        return "showAdminQueues";
    }


    public String mnuAdUserQueues_action() {
        // TODO: Replace with your code
        
        return null;
    }


    public String mnuTeamQueues_action() {
        // TODO: Replace with your code
        
        return null;
    }


    public String mnuLogout_action() {
        getSessionBean().doLogout();
        return "loginPage";
    }

}
