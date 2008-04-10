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
import org.yawlfoundation.yawl.resourcing.resource.Participant;

import javax.faces.FacesException;

public class pfMenu extends AbstractFragmentBean {

    private int __placeholder;
    
    private void _init() throws Exception {
    }

    private PanelLayout menuPanel = new PanelLayout();

    public PanelLayout getMenuPanel() {
        return menuPanel;
    }

    public void setMenuPanel(PanelLayout pl) {
        this.menuPanel = pl;
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

    private ImageHyperlink mnuUserMgt = new ImageHyperlink();

    public ImageHyperlink getMnuUserMgt() {
        return mnuUserMgt;
    }

    public void setMnuUserMgt(ImageHyperlink ih) {
        this.mnuUserMgt = ih;
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


    public void init() {
        super.init();

        // *Note* - this code should NOT be modified
        try {
            _init();
        } catch (Exception e) {
            log("pfMenu Initialization Failure", e);
            throw e instanceof FacesException ? (FacesException) e: new FacesException(e);
        }
        
        Participant p = getSessionBean().getParticipant();
        if (p == null) {                                          // means user="admin"
            mnuUserWorkQueues.setVisible(false) ;
            mnuMyProfile.setVisible(false);
        }
        else if (! p.isAdministrator()) {
            if (p.getUserPrivileges().canManageCases()) {
                mnuCaseMgt.setVisible(true);
                menuPanel.setStyle("height: 130px");
            }
            else {
                mnuCaseMgt.setVisible(false);
                menuPanel.setStyle("height: 95px");
            }
            mnuAdminQueues.setVisible(false);
            mnuServiceMgt.setVisible(false);
            mnuOrgDataMgt.setVisible(false);
            mnuUserMgt.setVisible(false);
        }
    }

    public void destroy() { }


    public String mnuMyProfile_action() {
        getSessionBean().checkLogon();
        getSessionBean().setMnuSelectorStyle("top: 44px");
        return "showViewProfile";
    }


    public String mnuUserWorkQueues_action() {
         getSessionBean().checkLogon();
         getSessionBean().setMnuSelectorStyle("top: 72px");
         return "showUserQueues";
    }


    public String mnuCaseMgt_action() {
        getSessionBean().checkLogon();
        getSessionBean().setMnuSelectorStyle("top: 100px");
        return "showCaseMgt";
    }


    public String mnuAdminQueues_action() {
        getSessionBean().checkLogon();
        getSessionBean().setMnuSelectorStyle("top: 128px");
        return "showAdminQueues";
    }


    public String mnuServiceMgt_action() {
        getSessionBean().checkLogon();
        getSessionBean().setMnuSelectorStyle("top: 152px");
        return "showServiceMgt";
    }


    public String mnuUserMgt_action() {
        getSessionBean().checkLogon();
        getSessionBean().setMnuSelectorStyle("top: 180px");
        return "showEditUserData";
    }

    public String mnuOrgDataMgt_action() {
        getSessionBean().checkLogon();
        getSessionBean().setMnuSelectorStyle("top: 208px");
        return "showEditOrgData";
    }


    public String mnuAdUserQueues_action() {
        return null;
    }


    public String mnuTeamQueues_action() {
         return null;
    }


    public String mnuLogout_action() {
        getSessionBean().doLogout();
        return "loginPage";
    }

}
