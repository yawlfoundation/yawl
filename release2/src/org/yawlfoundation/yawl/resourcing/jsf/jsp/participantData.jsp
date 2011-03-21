<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="1.2"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:jsp="http://java.sun.com/JSP/Page"
          xmlns:ui="http://www.sun.com/web/ui">

<!--
  ~ Copyright (c) 2004-2010 The YAWL Foundation. All rights reserved.
  ~ The YAWL Foundation is a collaboration of individuals and
  ~ organisations who are committed to improving workflow technology.
  ~
  ~ This file is part of YAWL. YAWL is free software: you can
  ~ redistribute it and/or modify it under the terms of the GNU Lesser
  ~ General Public License as published by the Free Software Foundation.
  ~
  ~ YAWL is distributed in the hope that it will be useful, but WITHOUT
  ~ ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
  ~ or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
  ~ Public License for more details.
  ~
  ~ You should have received a copy of the GNU Lesser General Public
  ~ License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
  -->

    <jsp:directive.page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"/>
    
    <f:view>
        <ui:page binding="#{participantData.page1}" id="page1">
            <ui:html binding="#{participantData.html1}" id="html1">
                <ui:head binding="#{participantData.head1}" id="head1"
                         title="YAWL 2.2: User Maintenance">
                    <ui:link binding="#{participantData.link1}" id="link1"
                             url="/resources/stylesheet.css"/>

                    <ui:link binding="#{ApplicationBean.favIcon}" id="lnkFavIcon"
                             rel="shortcut icon"
                            type="image/x-icon" url="/resources/favicon.ico"/>
                    
                    <ui:script binding="#{SessionBean.script}" id="script1"
                               url="/resources/script.js"/>

                </ui:head>
                <ui:body binding="#{participantData.body1}" id="body1"
                         style="-rave-layout: grid">
                    <ui:form binding="#{participantData.form1}" id="form1">

                        <!-- include banner -->
                        <jsp:include page="pfHeader.jspf"/>

                        <div style="top: 20px; position: relative">
                            <jsp:directive.include file="pfMenubar.jspf"/>
                        </div>
                        <center>

                        <ui:panelLayout binding="#{participantData.pnlContainer}"
                                        id="pnlContainer"
                                        styleClass="userMgtContainerPanel">

                        <!-- Select Participant Panel -->
                        <ui:panelLayout binding="#{participantData.pnlSelectUser}"
                                        id="pnlSelectUser"
                                        style="height: 34px; position: absolute; left: 0"
                                        styleClass="orgDataPanel">

                            <ui:dropDown binding="#{participantData.cbbParticipants}"
                                         id="cbbParticipants"
                                         items="#{SessionBean.orgDataParticipantList}"
                                         onChange="common_timeoutSubmitForm(this.form, 'cbbParticipants');"
                                         toolTip="Select a participant to view, edit or remove"
                                         style="left: 100px; top: 8px; position: absolute; width: 288px"
                                         valueChangeListener="#{participantData.cbbParticipants_processValueChange}"/>

                            <ui:label binding="#{participantData.label1}"
                                      id="label1" style="left: 12px; top: 12px; position: absolute"
                                      text="Participant:"/>
                        </ui:panelLayout>



                        <ui:panelLayout binding="#{participantData.pnlPrivileges}"
                                        id="pnlPrivileges"
                                        styleClass="orgDataPanel"
                                        style="position: absolute; height: 333px; left: 411px; width: 255px">

                            <ui:staticText binding="#{participantData.staticText1}"
                                           id="staticText1"
                                           styleClass="pageSubheading"
                                           style="left: 12px; top: 12px"
                                           text="Privileges"/>

                            <ui:checkbox binding="#{participantData.cbxChooseItemToStart}"
                                         id="cbxChooseItemToStart"
                                         label="Choose Which Work Item to Start"
                                         styleClass="orgDataPrivCheckBox"
                                         selected="false"
                                         style="top: 40px"/>

                            <ui:checkbox binding="#{participantData.cbxStartConcurrent}"
                                         id="cbxStartConcurrent"
                                         label="Start Work Items Concurrently"
                                         styleClass="orgDataPrivCheckBox"
                                         selected="false"
                                         style="top: 83px"/>

                            <ui:checkbox binding="#{participantData.cbxReorderItems}"
                                         id="cbxReorderItems"
                                         label="Reorder Work Items"
                                         styleClass="orgDataPrivCheckBox"
                                         selected="false"
                                         style="top: 126px"/>

                            <ui:checkbox binding="#{participantData.cbxViewTeamItems}"
                                         id="cbxViewTeamItems"
                                         label="View All Work Items of Team"
                                         styleClass="orgDataPrivCheckBox"
                                         selected="false"
                                         style="top: 169px"/>

                            <ui:checkbox binding="#{participantData.cbxViewOrgGroupItems}"
                                         id="cbxViewOrgGroupItems"
                                         label="View All Work items of Org Group"
                                         styleClass="orgDataPrivCheckBox"
                                         selected="false"
                                         style="top: 213px"/>

                            <ui:checkbox binding="#{participantData.cbxChainItems}"
                                         id="cbxChainItems"
                                         label="Chain Work Item Execution"
                                         styleClass="orgDataPrivCheckBox"
                                         selected="false"
                                         style="top: 256px"/>

                            <ui:checkbox binding="#{participantData.cbxManageCases}"
                                         id="cbxManageCases"
                                         label="Manage Cases"
                                         styleClass="orgDataPrivCheckBox"
                                         selected="false"
                                         style="top: 299px"/>
                        </ui:panelLayout>


                        <ui:panelLayout binding="#{participantData.pnlUserDetails}"
                                        id="pnlUserDetails"
                                        styleClass="orgDataPanel"
                                        style="position: absolute; height: 293px; top: 40px; left: 0">

                            <ui:label binding="#{participantData.lblFirstName}"
                                      for="txtFirstName"
                                      id="lblFirstName"
                                      style="left: 12px; top: 12px; position: absolute"
                                      text="First Name:"/>

                            <ui:label binding="#{participantData.lblLastName}"
                                      for="txtLastName"
                                      id="lblLastName"
                                      style="left: 12px; top: 48px; position: absolute"
                                      text="Last Name:"/>

                            <ui:label binding="#{participantData.lblUserID}"
                                      for="txtUserID"
                                      id="lblUserID"
                                      style="left: 12px; top: 84px; position: absolute"
                                      text="User ID:"/>

                            <ui:label binding="#{participantData.lblDesc}"
                                      for="txtDesc"
                                      id="lblDesc"
                                      style="left: 12px; top: 126px; position: absolute"
                                      text="Description:"/>

                            <ui:label binding="#{participantData.lblNotes}"
                                      for="txtNotes"
                                      id="lblNotes"
                                      style="left: 12px; top: 210px; position: absolute"
                                      text="Notes:"/>

                            <ui:textField binding="#{participantData.txtFirstName}"
                                          id="txtFirstName"
                                          style="left: 100px; top: 12px; width: 280px; position: absolute"/>

                            <ui:textField binding="#{participantData.txtLastName}"
                                          id="txtLastName"
                                          style="left: 100px; top: 48px; width: 280px; position: absolute"/>

                            <ui:textField binding="#{participantData.txtUserID}"
                                          id="txtUserID"
                                          style="left: 100px; top: 84px; width: 150px; position: absolute"/>

                            <ui:checkbox binding="#{participantData.cbxAdmin}"
                                         id="cbxAdmin"
                                         label="Administrator"
                                         labelLevel="2"
                                         style="left: 270px; top: 84px; position: absolute"/>

                            <ui:textArea binding="#{participantData.txtDesc}"
                                         id="txtDesc"
                                         style="left: 100px; top: 126px; width: 285px; height: 60px; position: absolute"/>

                            <ui:textArea binding="#{participantData.txtNotes}"
                                         id="txtNotes"
                                         style="left: 100px; top: 210px; width: 285px; height: 60px; position: absolute"/>
                        </ui:panelLayout>


                        <ui:tabSet binding="#{participantData.tabSetAttributes}"
                                   id="tabSetAttributes"
                                   selected="tabRoles"
                                   style="border: 2px solid gray; height: 200px; top: 339px; left: 0; position: absolute; width: 406px">

                            <ui:tab binding="#{participantData.tabRoles}"
                                    action="#{participantData.tabRoles_action}"
                                    id="tabRoles"
                                    text="Roles">

                                <ui:panelLayout
                                        binding="#{participantData.tabPanelRole}"
                                        id="tabPanelRole"
                                        styleClass="userOrgDataTabPanel"/>
                            </ui:tab>

                            <ui:tab binding="#{participantData.tabPosition}"
                                    action="#{participantData.tabPosition_action}"
                                    id="tabPosition"
                                    text="Positions">

                                <ui:panelLayout binding="#{participantData.tabPanelPosition}"
                                                id="tabPanelPosition"
                                                styleClass="userOrgDataTabPanel"/>
                            </ui:tab>

                            <ui:tab binding="#{participantData.tabCapability}"
                                    action="#{participantData.tabCapability_action}"
                                    id="tabCapability"
                                    text="Capabilities">

                                <ui:panelLayout binding="#{participantData.tabPanelCapability}"
                                                id="tabPanelCapability"
                                                styleClass="userOrgDataTabPanel"/>
                            </ui:tab>

                        </ui:tabSet>


                        <!-- Password Panel -->

                        <ui:panelLayout binding="#{participantData.pnlNewPassword}"
                                        id="pnlNewPassword"
                                        styleClass="orgDataPanel"
                                        style="position: absolute; height: 110px; top: 339px; left: 411px; width: 255px">

                            <ui:staticText binding="#{participantData.sttPassword}"
                                           id="sttPassword"
                                           styleClass="pageSubheading"
                                           style="left: 12px; top: 12px"
                                           text="Password"/>

                            <!--The following two fields provide a workaround to the
                                firefox 3 and chrome password managers, which insert
                                the pw from the login form to the first passwordfield
                                on a form and then the userid from the login form to
                                the previously occurring text field. The following
                                fields will consume the fillins and never display. -->

                            <ui:textField id="ff3workaroundtf"
                                          label="userid"
                                          style="display: none" />
                            
                            <ui:passwordField id="ff3workaroundpw"
                                              style="display: none" />

                            <!--=====================================================-->
                            
                            <ui:label binding="#{participantData.lblPassword}"
                                      for="txtNewPassword"
                                      id="lblPassword"
                                      style="left: 12px; top: 40px; position: absolute"
                                      text="New:"/>

                            <ui:label binding="#{participantData.lblConfirm}"
                                      for="txtConfirmPassword"
                                      id="lblConfirm"
                                      style="left: 12px; top: 76px; position: absolute"
                                      text="Confirm:"/>

                            <ui:passwordField binding="#{participantData.txtNewPassword}"
                                              id="txtNewPassword"
                                              style="left: 70px; top: 40px; width: 160px; position: absolute"/>

                            <ui:passwordField binding="#{participantData.txtConfirmPassword}"
                                              id="txtConfirmPassword"
                                              style="left: 70px; top: 76px; width: 160px; position: absolute"/>

                        </ui:panelLayout>

                        <!-- Buttons -->

                        <ui:button action="#{participantData.btnSave_action}"
                                   binding="#{participantData.btnSave}"
                                   id="btnSave"
                                   styleClass="orgDataButton"
                                   style="left: 431px; top: 466px"
                                   toolTip="Save changes for the current participant"
                                   text="Save"/>

                        <ui:button action="#{participantData.btnAdd_action}"
                                   binding="#{participantData.btnAdd}"
                                   id="btnAdd"
                                   styleClass="orgDataButton"
                                   style="left: 551px; top: 466px"
                                   text="New"/>

                        <ui:button action="#{participantData.btnReset_action}"
                                   binding="#{participantData.btnReset}"
                                   id="btnReset"
                                   styleClass="orgDataButton"
                                   style="left:431px; top: 509px"
                                   text="Reset"/>
                        
                        <ui:button action="#{participantData.btnRemove_action}"
                                   binding="#{participantData.btnRemove}"
                                   id="btnRemove"
                                   styleClass="orgDataButton"
                                   style="left: 551px;top: 509px"
                                   toolTip="Permanently remove the current participant"
                                   onClick="return confirmDelete()"
                                   text="Remove"/>

                        <div style="position: absolute">
                            <jsp:directive.include file="pfAddRemove.jspf"/>
                        </div>

                        <div><jsp:include page="pfMsgPanel.jspf"/></div>

                        </ui:panelLayout>

                        </center>
                        <div><jsp:include page="pfFooter.jspf"/></div>
                    </ui:form>
                </ui:body>
            </ui:html>
        </ui:page>
    </f:view>
</jsp:root>
