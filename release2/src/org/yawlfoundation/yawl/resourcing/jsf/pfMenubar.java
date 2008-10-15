/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing.jsf;

import com.sun.rave.web.ui.appbase.AbstractFragmentBean;
import com.sun.rave.web.ui.component.Button;
import com.sun.rave.web.ui.component.ImageComponent;
import com.sun.rave.web.ui.component.PanelLayout;
import org.yawlfoundation.yawl.resourcing.resource.Participant;

import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;

/*
 * Fragment bean that provides a menubar for each form
 *
 * @author: Michael Adams
 * Date: 26/01/2008
 *
 * Last date: 10/05/2008
 */

public class pfMenubar extends AbstractFragmentBean {

    // Required jsf members and abstract method implementations //

    private int __placeholder;

    private void _init() throws Exception { }

    public pfMenubar() {  }

    public void init() {
        super.init();

        // *Note* - this code should NOT be modified
        try {
            _init();
        } catch (Exception e) {
            log("pfMenu Initialization Failure", e);
            throw e instanceof FacesException ? (FacesException) e: new FacesException(e);
        }
    }

    public void destroy() { }
    
    /*******************************************************/

    // form component (a container for dynamically generated buttons)  //
    
    private PanelLayout menuPanel = new PanelLayout();

    public PanelLayout getMenuPanel() { return menuPanel; }

    public void setMenuPanel(PanelLayout pl) { menuPanel = pl; }


    /*********************************************************/

    // IMPLEMENTATION //

    private SessionBean _sb = (SessionBean)getBean("SessionBean");

    private final String BTN_WORKQUEUES = "WorkQueues";
    private final String BTN_USERMGT = "UserMgt";
    private final String BTN_CASEMGT = "CaseMgt";
    private final String BTN_LOGOUT = "Logout";
    private final String BTN_PROFILE = "Profile";
    private final String BTN_ADMINQUEUES = "AdminQueues";
    private final String BTN_SERVICEMGT = "ServiceMgt";
    private final String BTN_ORGDATAMGT = "OrgDataMgt";
    private final String BTN_TEAMQUEUES = "TeamQueues";

    private final int BTN_WIDTH = 95;
    private final int BOOKEND_WIDTH = 38;


    public void construct(boolean isAdminID) {
        int left = 0;
        menuPanel.getChildren().add(makeBookEnd(0));
        Participant p = _sb.getParticipant();

        // all participants get profile and workqueue access
        if (p != null) {
            menuPanel.getChildren().add(makeButton(BTN_WORKQUEUES));
            menuPanel.getChildren().add(makeButton(BTN_PROFILE));

            // participants with admin or team view access get to view teams
            if (p.isAdministrator() || canViewTeamQueues(p)) {
                menuPanel.getChildren().add(makeButton(BTN_TEAMQUEUES));
            }
        }

        // the "admin" user (p == null) and users with admin privileges get admin access
        if ((p == null) || p.isAdministrator()) {
            menuPanel.getChildren().add(makeButton(BTN_ADMINQUEUES));
            menuPanel.getChildren().add(makeButton(BTN_CASEMGT));
            menuPanel.getChildren().add(makeButton(BTN_USERMGT));
            menuPanel.getChildren().add(makeButton(BTN_ORGDATAMGT));
            menuPanel.getChildren().add(makeButton(BTN_SERVICEMGT));
        }
        else {

            // non-admin participants may have case mgt privileges
           if (p.getUserPrivileges().canManageCases()) {
               menuPanel.getChildren().add(makeButton(BTN_CASEMGT));
           }
        }

        // and everyone gets to logoff
        menuPanel.getChildren().add(makeButton(BTN_LOGOUT));

        menuPanel.getChildren().add(makeBookEnd(getRightBookEndLeftPos()));

        int width = ((menuPanel.getChildCount() - 2) * BTN_WIDTH) + (BOOKEND_WIDTH * 2) + 2;
        menuPanel.setStyle(String.format("width: %dpx", width));

        if (isAdminID)
            showSelection(BTN_ADMINQUEUES);
        else
            showSelection(BTN_WORKQUEUES);
    }


    private ImageComponent makeBookEnd(int left) {
        ImageComponent image = new ImageComponent();
        image.setStyle("height: 30px; width: 39px; position: absolute; left: " + left + "px");
        image.setId(left==0 ? "menuLeft" : "menuRight");
        image.setUrl(left==0 ? "/resources/menuLeft.png" : "/resources/menuRight.png");
        return image;
    }

    private Button makeButton(String btnType) {
        Button result = new Button();
        result.setId("menubtn" + btnType);
        result.setText(getButtonText(btnType));
        result.setStyleClass("menubarButton");
        result.setStyle(String.format("left: %dpx", getButtonLeft()));
        result.setAction(bindButtonListener(btnType));
        return result;
    }


    private int getButtonLeft() {
        return BOOKEND_WIDTH + (BTN_WIDTH * (menuPanel.getChildCount() - 1));
    }

    private int getRightBookEndLeftPos() {
        return getButtonLeft() + 1;
    }

    private String getButtonText(String btnType) {
        String result = null;
        if (btnType.equals(BTN_WORKQUEUES)) result = "Work Queues";
        if (btnType.equals(BTN_USERMGT)) result = "Users";
        if (btnType.equals(BTN_CASEMGT)) result = "Cases";
        if (btnType.equals(BTN_LOGOUT)) result = "Logout";
        if (btnType.equals(BTN_PROFILE)) result = "Edit Profile";
        if (btnType.equals(BTN_ADMINQUEUES)) result = "Admin Queues";
        if (btnType.equals(BTN_SERVICEMGT)) result = "Services";
        if (btnType.equals(BTN_ORGDATAMGT)) result = "Org Data";
        if (btnType.equals(BTN_TEAMQUEUES)) result = "Team Queues";
        return result;
    }


    private MethodBinding bindButtonListener(String btnType) {
        String listenerName = String.format("#{pfMenubar.btn%sAction}", btnType);
        Application app = FacesContext.getCurrentInstance().getApplication();
        return app.createMethodBinding(listenerName, new Class[0]);
    }


    private void showSelection(String btnType) {
        for (Object o : menuPanel.getChildren()) {
            if (o instanceof Button) {
                Button btn = (Button) o;
                if (btn.getId().equals("menubtn" + btnType))
                    btn.setStyleClass("menubarButtonSelected");
                else
                    btn.setStyleClass("menubarButton");
            }
        }
    }

    private boolean canViewTeamQueues(Participant p) {
        return p.getUserPrivileges().canViewOrgGroupItems() ||
               p.getUserPrivileges().canViewTeamItems();
    }


    // BUTTON ACTIONS //

    public String btnWorkQueuesAction() {
        showSelection(BTN_WORKQUEUES);
        return "showUserQueues";
    }

    public String btnUserMgtAction() {
        showSelection(BTN_USERMGT);
        _sb.resetPageDefaults(ApplicationBean.PageRef.participantData);
        return "showEditUserData";
    }

    public String btnCaseMgtAction() {
        showSelection(BTN_CASEMGT);
        return "showCaseMgt";
    }

    public String btnLogoutAction() {
        _sb.doLogout();
        return "loginPage";
    }


    public String btnProfileAction() {
        showSelection(BTN_PROFILE);
        return "showViewProfile";
    }


    public String btnAdminQueuesAction() {
        showSelection(BTN_ADMINQUEUES);
        return "showAdminQueues";
    }


    public String btnServiceMgtAction() {
        showSelection(BTN_SERVICEMGT);
        return "showServiceMgt";
    }


    public String btnOrgDataMgtAction() {
        showSelection(BTN_ORGDATAMGT);
        return "showEditOrgData";
    }


    public String btnTeamQueuesAction() {
        showSelection(BTN_TEAMQUEUES);
        return "showTeamQueues";
    }


}