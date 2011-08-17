<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="1.2"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:jsp="http://java.sun.com/JSP/Page"
          xmlns:ui="http://www.sun.com/web/ui">

<!--
  ~ Copyright (c) 2004-2011 The YAWL Foundation. All rights reserved.
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
        <ui:page binding="#{secResourceMgt.page1}" id="page1">
            <ui:html binding="#{secResourceMgt.html1}" id="html1">

                <ui:head binding="#{secResourceMgt.head1}" id="head1"
                         title="YAWL 2.2: Secondary Resources">
                    <ui:link binding="#{secResourceMgt.link1}" id="link1"
                             url="/resources/stylesheet.css"/>

                    <ui:link binding="#{ApplicationBean.favIcon}" id="lnkFavIcon"
                             rel="shortcut icon"
                            type="image/x-icon" url="/resources/favicon.ico"/>

                    <ui:script binding="#{SessionBean.script}" id="script1"
                               url="/resources/script.js"/>

                </ui:head>

                <ui:body binding="#{secResourceMgt.body1}" id="body1"
                         style="-rave-layout: grid">

                    <ui:form binding="#{secResourceMgt.form1}" id="form1">

                        <!-- include banner -->
                        <jsp:include page="pfHeader.jspf"/>

                        <center>
                            <ui:staticText binding="#{secResourceMgt.sttTitle}"
                                           id="sttTitle"
                                           styleClass="pageHeading"
                                           style="top: 25px"
                                           text="#{SessionBean.secondaryResourcesTitle}"/>

                        <ui:panelLayout binding="#{secResourceMgt.pnlContainer}"
                                        id="pnlContainer"
                                        styleClass="secondaryResourcesContainerPanel">

                       <!-- Participants -->
                       <ui:panelLayout binding="#{secResourceMgt.pnlParticipants}"
                                         id="pnlParticipants"
                                         styleClass="secondaryResourcesAvailablePanel"
                                         style="top:0; left: 0">

                          <ui:staticText binding="#{secResourceMgt.sttParticipants}"
                                         id="sttParticipants"
                                         styleClass="pageSubheading"
                                         style="left: 12px; top: 12px"
                                         text="Participants"/>

                          <ui:listbox binding="#{secResourceMgt.lbxParticipants}"
                                      id="lbxParticipants"
                                      items="#{SessionBean.orgDataParticipantList}"
                                      valueChangeListener="#{secResourceMgt.lbxParticipants_processValueChange}"
                                      onKeyPress="return disableEnterKey(event);"
                                      onChange="common_timeoutSubmitForm(this.form, 'lbxParticipants');"
                                      styleClass="secondaryResourcesListbox"/>

                       </ui:panelLayout>

                            <!-- Roles -->
                            <ui:panelLayout binding="#{secResourceMgt.pnlRoles}"
                                              id="pnlRoles"
                                              styleClass="secondaryResourcesAvailablePanel"
                                              style="top:300px; left:0">

                               <ui:staticText binding="#{secResourceMgt.sttRoles}"
                                              id="sttRoles"
                                              styleClass="pageSubheading"
                                              style="left: 12px; top: 12px"
                                              text="Roles"/>

                               <ui:listbox binding="#{secResourceMgt.lbxRoles}"
                                           id="lbxRoles"
                                           items="#{SessionBean.sortedRoleList}"
                                            valueChangeListener="#{secResourceMgt.lbxRoles_processValueChange}"
                                            onKeyPress="return disableEnterKey(event);"
                                            onChange="common_timeoutSubmitForm(this.form, 'lbxRoles');"
                                           styleClass="secondaryResourcesListbox"/>

                            </ui:panelLayout>

                            <!-- NHResources -->
                            <ui:panelLayout binding="#{secResourceMgt.pnlNHResources}"
                                              id="pnlNHResources"
                                              styleClass="secondaryResourcesAvailablePanel"
                                              style="top:0; left: 250px">

                               <ui:staticText binding="#{secResourceMgt.sttNHResources}"
                                              id="sttNHResources"
                                              styleClass="pageSubheading"
                                              style="left: 12px; top: 12px"
                                              text="Assets"/>

                               <ui:listbox binding="#{secResourceMgt.lbxNHResources}"
                                           id="lbxNHResources"
                                           items="#{SessionBean.nhResourcesList}"
                                           valueChangeListener="#{secResourceMgt.lbxNHResources_processValueChange}"
                                            onKeyPress="return disableEnterKey(event);"
                                            onChange="common_timeoutSubmitForm(this.form, 'lbxNHResources');"
                                            styleClass="secondaryResourcesListbox"/>

                            </ui:panelLayout>

                            <!-- NHCategories -->
                            <ui:panelLayout binding="#{secResourceMgt.pnlNHCategories}"
                                              id="pnlNHCategories"
                                              styleClass="secondaryResourcesAvailablePanel"
                                              style="top:300px; left: 250px">

                               <ui:staticText binding="#{secResourceMgt.sttNHCategories}"
                                              id="sttNHCategories"
                                              styleClass="pageSubheading"
                                              style="left: 12px; top: 12px"
                                              text="Categories"/>

                               <ui:listbox binding="#{secResourceMgt.lbxNHCategories}"
                                           id="lbxNHCategories"
                                           items="#{SessionBean.nhResourcesCategoryListExpanded}"
                                           valueChangeListener="#{secResourceMgt.lbxNHCategories_processValueChange}"
                                           onKeyPress="return disableEnterKey(event);"
                                           onChange="common_timeoutSubmitForm(this.form, 'lbxNHCategories');"
                                           styleClass="secondaryResourcesListbox"/>

                            </ui:panelLayout>

                            <!-- Selected Resources -->
                            <ui:panelLayout binding="#{secResourceMgt.pnlSelected}"
                                              id="pnlSelected"
                                              styleClass="secondaryResourcesAvailablePanel"
                                              style="top:0; left: 500px; height: 600px; width: 298px; border-left: 2px solid gray">

                               <ui:staticText binding="#{secResourceMgt.sttSelected}"
                                              id="sttSelected"
                                              styleClass="pageSubheading"
                                              style="left: 12px; top: 12px"
                                              text="Selected Resources"/>

                               <ui:listbox binding="#{secResourceMgt.lbxSelected}"
                                           id="lbxSelected"
                                           items="#{SessionBean.selectedSecondaryResources}"
                                           selected="#{SessionBean.selectedSecondaryResource}"
                                           valueChangeListener="#{secResourceMgt.lbxSelected_processValueChange}"
                                           onKeyPress="return disableEnterKey(event);"
                                           onChange="common_timeoutSubmitForm(this.form, 'lbxSelected');"
                                           style="height: 450px; left: 12px; top: 35px; position: absolute; width: 276px"/>

                                <ui:button action="#{secResourceMgt.btnDone_action}"
                                           binding="#{secResourceMgt.btnDone}"
                                           id="btnDone"
                                           styleClass="orgDataButton"
                                           style="left: 160px; top: 550px"
                                           toolTip="Return to Admin Queues"
                                           text="Done"/>

                                <ui:button action="#{secResourceMgt.btnRemove_action}"
                                           binding="#{secResourceMgt.btnRemove}"
                                           id="btnRemove"
                                           styleClass="orgDataButton"
                                           style="left: 160px; top: 500px"
                                           toolTip="Remove the Selected Resource"
                                           text="Remove"/>

                                <ui:button action="#{secResourceMgt.btnCheck_action}"
                                           binding="#{secResourceMgt.btnCheck}"
                                           id="btnCheck"
                                           styleClass="orgDataButton"
                                           style="left: 30px; top: 500px"
                                           toolTip="Check availability of selected resources"
                                           text="Check"/>

                                <ui:button action="#{secResourceMgt.btnSave_action}"
                                           binding="#{secResourceMgt.btnSave}"
                                           id="btnSave"
                                           styleClass="orgDataButton"
                                           style="left: 30px; top: 550px"
                                           toolTip="Save the Selected Resources for this workitem"
                                           text="Save"/>
                            </ui:panelLayout>

                               <div><jsp:include page="pfMsgPanel.jspf"/></div>

                            </ui:panelLayout>  <!-- pnlContainer -->

                         </center>
                        <div><jsp:include page="pfFooter.jspf"/></div>
                    </ui:form>
                </ui:body>
            </ui:html>
        </ui:page>
    </f:view>
</jsp:root>
