<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="1.2" xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:jsp="http://java.sun.com/JSP/Page"
          xmlns:ui="http://www.sun.com/web/ui">

<!--
  ~ Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
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
        <ui:page binding="#{ActiveCases.page1}" id="page1">
            <ui:html binding="#{ActiveCases.html1}" id="html1">
                <ui:head binding="#{ActiveCases.head1}" id="head1"
                         title="YAWL Monitor Service - Active Cases">

                    <ui:link binding="#{ActiveCases.link1}" id="link1"
                             url="/resources/stylesheet.css"/>

                    <ui:link binding="#{ApplicationBean.favIcon}" id="lnkFavIcon"
                             rel="shortcut icon"
                            type="image/x-icon" url="/resources/favicon.ico"/>

                    <ui:script binding="#{SessionBean.script}" id="script1"
                               url="/resources/script.js"/>

                </ui:head>
                <ui:body binding="#{ActiveCases.body1}" id="body1"
                         style="-rave-layout: grid">
                    <ui:form binding="#{ActiveCases.form1}" id="form1">

                        <!-- include banner -->
                        <div><jsp:directive.include file="pfHeader.jspf"/></div>

                        <center>

                        <ui:panelLayout binding="#{ActiveCases.pnlContainer}"
                                        id="pnlContainer"
                                        styleClass="casesContainerPanel">

                        <ui:panelLayout binding="#{ActiveCases.layoutPanel}"
                                        id="layoutPanel1"
                                        styleClass="casesPanel"
                                        style="position: absolute">

                            <ui:staticText binding="#{ActiveCases.staticText}"
                                           id="staticText1"
                                           styleClass="pageHeading"
                                           style="left: 12px; top: 12px"
                                           text="Active Cases"/>

                            <ui:button binding="#{SessionBean.btnRefresh}"
                                        action="#{ActiveCases.btnRefresh_action}"
                                        id="btnRefresh"
                                        imageURL="/resources/refresh.png"
                                        styleClass="refreshButton"
                                        toolTip="Refresh Active Cases"
                                        text=""/>

                             <ui:button binding="#{SessionBean.btnLogout}"
                                        action="#{ActiveCases.btnLogout_action}"
                                        id="btnLogout"
                                        imageURL="/resources/logout.png"
                                        styleClass="logoutButton"
                                        toolTip="Logout"
                                        text=""/>

                         </ui:panelLayout>


                            <ui:staticText binding="#{ActiveCases.stUptime}"
                                           id="stuptime"
                                           style="position:absolute; left: 15px; top: 52px;"
                                           text="#{ActiveCases.startupTime}"/>

                            <ui:panelGroup binding="#{ActiveCases.pnlGroup}"
                                            id="pnlGroup"
                                            styleClass="casesTablePnlGroup">
                                                        
                             <h:dataTable binding="#{ActiveCases.dataTable}"
                                         headerClass="dataTableHeader"
                                         id="dataTable1"
                                         cellpadding="3"
                                         styleClass="dataTable"
                                         columnClasses="caseIDCol,
                                                        specNameCol,
                                                        versionCol,
                                                        startTimeCol"
                                         value="#{SessionBean.activeCases}"
                                         var="currentRow"
                                         width="570">

                                <h:column binding="#{ActiveCases.colCaseID}"
                                          id="colCaseID">
                                           <f:facet name="header" >
                                               <h:commandLink value="#{ActiveCases.caseIDHeaderText}"
                                                       action="#{ActiveCases.caseIDHeaderClick}"/>
                                           </f:facet>
                                    <h:outputText binding="#{ActiveCases.colCaseIDRows}"
                                                  id="colCaseIDRows"
                                                  styleClass="dataTableText"
                                                  value="#{currentRow.caseID}"/>
                                </h:column>

                                <h:column binding="#{ActiveCases.colSpecName}"
                                          id="colSpecName">
                                    <f:facet name="header" >
                                        <h:commandLink value="#{ActiveCases.specNameHeaderText}"
                                                action="#{ActiveCases.specNameHeaderClick}"/>
                                    </f:facet>
                                    <h:outputText binding="#{ActiveCases.colSpecNameRows}"
                                                  id="colSpecNameRows"
                                                  styleClass="dataTableText"
                                                  value="#{currentRow.specName}"/>
                                </h:column>

                                <h:column binding="#{ActiveCases.colVersion}"
                                          id="colVersion">
                                    <f:facet name="header" >
                                        <h:commandLink value="#{ActiveCases.specVersionHeaderText}"
                                                action="#{ActiveCases.specVersionHeaderClick}"/>
                                    </f:facet>
                                    <h:outputText binding="#{ActiveCases.colVersionRows}"
                                                  id="colVersionRows"
                                                  styleClass="dataTableText"
                                                  value="#{currentRow.specVersion}"/>
                                </h:column>

                                <h:column binding="#{ActiveCases.colStartTime}"
                                          id="colStartTime">
                                    <f:facet name="header" >
                                        <h:commandLink value="#{ActiveCases.startTimeHeaderText}"
                                                action="#{ActiveCases.startTimeHeaderClick}"/>
                                    </f:facet>
                                    <h:outputText binding="#{ActiveCases.colStartTimeRows}"
                                                  id="colStartTimeRows"
                                                  styleClass="dataTableText"
                                                  value="#{currentRow.startTimeAsDateString}"/>
                                </h:column>

                            </h:dataTable>
                           </ui:panelGroup>

                            <div><jsp:include page="pfMsgPanel.jspf"/></div>

                         </ui:panelLayout>

                        <ui:hiddenField binding="#{ActiveCases.hdnRowIndex}" id="hdnRowIndex"/>

                        <ui:button binding="#{ActiveCases.btnDetails}"
                                   action="#{ActiveCases.btnDetails_action}"
                                   id="btnDetails"
                                   style="display: none"
                                   text=""/>

                        </center>
                    </ui:form>

                    <ui:script>
                        addOnclickToDatatableRows();
                    </ui:script>

                </ui:body>
            </ui:html>
        </ui:page>
    </f:view>
</jsp:root>
