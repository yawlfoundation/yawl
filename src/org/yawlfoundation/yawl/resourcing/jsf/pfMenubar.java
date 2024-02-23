/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
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

    private SessionBean _sb = (SessionBean) getBean("SessionBean");

    private final String BTN_WORKQUEUES = "WorkQueues";
    private final String BTN_USERMGT = "UserMgt";
    private final String BTN_CASEMGT = "CaseMgt";
    private final String BTN_LOGOUT = "Logout";
    private final String BTN_PROFILE = "Profile";
    private final String BTN_ADMINQUEUES = "AdminQueues";
    private final String BTN_SERVICEMGT = "ServiceMgt";
    private final String BTN_ORGDATAMGT = "OrgDataMgt";
    private final String BTN_TEAMQUEUES = "TeamQueues";
    private final String BTN_EXTCLIENTS = "ExternalClients";
    private final String BTN_NHRESOURCES = "NonHumanResources";
    private final String BTN_CALENDAR = "CalendarMgt";

    private final int BTN_WIDTH = 95;
    private final int BOOKEND_WIDTH = 38;


    public void construct(boolean isAdminID) {
        int width = 0;
        int menuCount = 0;
        Participant p = _sb.getParticipant();
        PanelLayout userMenu = constructUserMenu(p);
        PanelLayout adminMenu = constructAdminMenu(p);

        if (userMenu != null) {
            menuPanel.getChildren().add(userMenu);
            width = getMenuWidth(userMenu);
            menuCount++;
        }
        if (adminMenu != null) {
            menuPanel.getChildren().add(adminMenu);
            width = getMenuWidth(adminMenu);
            menuCount++;
            if (userMenu != null) userMenu.setStyle(userMenu.getStyle() + "left: 285px;");
        }
        _sb.setMenuBarCount(menuCount);
        menuPanel.setStyle(String.format("width: %dpx", width));

        if (isAdminID)
            showSelection(BTN_ADMINQUEUES);
        else
            showSelection(BTN_WORKQUEUES);
    }


    private PanelLayout constructUserMenu(Participant p) {
        if (p == null) return null;

        PanelLayout userMenu = new PanelLayout();
        userMenu.getChildren().add(makeBookEnd(0, 1));
        userMenu.getChildren().add(makeButton(BTN_WORKQUEUES, userMenu));
        userMenu.getChildren().add(makeButton(BTN_PROFILE, userMenu));

        if (! p.isAdministrator()) {
            if (canViewTeamQueues(p)) {
                userMenu.getChildren().add(makeButton(BTN_TEAMQUEUES, userMenu));
            }

            // non-admin participants may have case mgt privileges
            if (p.getUserPrivileges().canManageCases()) {
                userMenu.getChildren().add(makeButton(BTN_CASEMGT, userMenu));
            }
        }

        userMenu.getChildren().add(makeButton(BTN_LOGOUT, userMenu));
        userMenu.getChildren().add(makeBookEnd(getRightBookEndLeftPos(userMenu), 1));
        int width = getMenuWidth(userMenu);
        userMenu.setStyle(String.format("width: %dpx; position: absolute;", width));

        return userMenu;
    }


    private PanelLayout constructAdminMenu(Participant p) {
        PanelLayout adminMenu = null;
        if ((p == null) || p.isAdministrator()) {
            adminMenu = new PanelLayout();
            adminMenu.getChildren().add(makeBookEnd(0, 2));
            adminMenu.getChildren().add(makeButton(BTN_ADMINQUEUES, adminMenu));
            if (p != null) {
                adminMenu.getChildren().add(makeButton(BTN_TEAMQUEUES, adminMenu));
            }
            adminMenu.getChildren().add(makeButton(BTN_CASEMGT, adminMenu));
            adminMenu.getChildren().add(makeButton(BTN_USERMGT, adminMenu));
            adminMenu.getChildren().add(makeButton(BTN_ORGDATAMGT, adminMenu));
            adminMenu.getChildren().add(makeButton(BTN_NHRESOURCES, adminMenu));
            adminMenu.getChildren().add(makeButton(BTN_CALENDAR, adminMenu));
            adminMenu.getChildren().add(makeButton(BTN_SERVICEMGT, adminMenu));
            adminMenu.getChildren().add(makeButton(BTN_EXTCLIENTS, adminMenu));
            if (p == null) {
                adminMenu.getChildren().add(makeButton(BTN_LOGOUT, adminMenu));
            }
            adminMenu.getChildren().add(makeBookEnd(getRightBookEndLeftPos(adminMenu), 2));
            int width = getMenuWidth(adminMenu);
            int top = (p != null) ? 40 : 0;
            adminMenu.setStyle(String.format("width: %dpx; top: %dpx; position: absolute;",
                    width, top));
        }
        return adminMenu;
    }


    private int getMenuWidth(PanelLayout menu) {
        return ((menu.getChildCount() - 2) * BTN_WIDTH) + (BOOKEND_WIDTH * 2) + 2;
    }


    private ImageComponent makeBookEnd(int left, int i) {
        ImageComponent image = new ImageComponent();
        image.setStyle("height:30px; width:39px; position:absolute; left:" + left + "px");
        image.setId((left==0 ? "menuLeft" : "menuRight") + i);
        image.setUrl(left==0 ? "/resources/menuLeft.png" : "/resources/menuRight.png");
        image.setWidth(39);
        image.setHeight(30);
        return image;
    }

    private Button makeButton(String btnType, PanelLayout menu) {
        Button result = new Button();
        result.setId("menubtn" + btnType);
        result.setText(getButtonText(btnType));
        result.setStyleClass("menubarButton");
        result.setStyle(String.format("left: %dpx", getButtonLeft(menu)));
        result.setAction(bindButtonListener(btnType));
        return result;
    }


    private int getButtonLeft(PanelLayout menu) {
        return BOOKEND_WIDTH + (BTN_WIDTH * (menu.getChildCount() - 1));
    }

    private int getRightBookEndLeftPos(PanelLayout menu) {
        return getButtonLeft(menu) + 1;
    }

    private String getButtonText(String btnType) {
        if (btnType.equals(BTN_WORKQUEUES)) return "Work Queues";
        if (btnType.equals(BTN_ADMINQUEUES)) return "Admin Queues";
        if (btnType.equals(BTN_CASEMGT)) return "Cases";
        if (btnType.equals(BTN_USERMGT)) return "Users";
        if (btnType.equals(BTN_ORGDATAMGT)) return "Org Data";
        if (btnType.equals(BTN_LOGOUT)) return "Logout";
        if (btnType.equals(BTN_NHRESOURCES)) return "Assets";
        if (btnType.equals(BTN_CALENDAR)) return "Calendar";
        if (btnType.equals(BTN_PROFILE)) return "Edit Profile";
        if (btnType.equals(BTN_SERVICEMGT)) return "Services";
        if (btnType.equals(BTN_EXTCLIENTS)) return "Client Apps";
        if (btnType.equals(BTN_TEAMQUEUES)) return "Team Queues";
        return "";
    }


    private MethodBinding bindButtonListener(String btnType) {
        String listenerName = String.format("#{pfMenubar.btn%sAction}", btnType);
        Application app = FacesContext.getCurrentInstance().getApplication();
        return app.createMethodBinding(listenerName, new Class[0]);
    }


    private void showSelection(String btnType) {
        for (Object o : menuPanel.getChildren()) {
            if (o instanceof PanelLayout) {
                for (Object obj : ((PanelLayout) o).getChildren()) {
                    if (obj instanceof Button) {
                        Button btn = (Button) obj;
                        if (btn.getId().equals("menubtn" + btnType))
                            btn.setStyleClass("menubarButtonSelected");
                        else
                            btn.setStyleClass("menubarButton");
                    }    
                }
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


    public String btnExternalClientsAction() {
        showSelection(BTN_EXTCLIENTS);
        return "showExternalClients";
    }


    public String btnNonHumanResourcesAction() {
        showSelection(BTN_NHRESOURCES);
        return "showNonHumanResources";
    }


    public String btnCalendarMgtAction() {
        showSelection(BTN_CALENDAR);
        return "showCalendar";
    }

}