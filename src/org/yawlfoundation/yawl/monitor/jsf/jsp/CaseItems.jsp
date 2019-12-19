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
        <ui:page binding="#{CaseItems.page1}" id="page1">
            <ui:html binding="#{CaseItems.html1}" id="html1">
                <ui:head binding="#{CaseItems.head1}" id="head1"
                         title="YAWL Monitor Service - Selected Case Detail">

                    <ui:link binding="#{CaseItems.link1}" id="link1"
                             url="/resources/stylesheet.css"/>

                    <ui:link binding="#{ApplicationBean.favIcon}" id="lnkFavIcon"
                             rel="shortcut icon"
                            type="image/x-icon" url="/resources/favicon.ico"/>

                    <ui:script binding="#{SessionBean.script}" id="script1"
                               url="/resources/script.js"/>

                </ui:head>
                <ui:body binding="#{CaseItems.body1}" id="body1"
                         style="-rave-layout: grid">
                    <ui:form binding="#{CaseItems.form1}" id="form1">

                        <!-- include banner -->
                        <div><jsp:directive.include file="pfHeader.jspf"/></div>

                        <center>

                        <ui:panelLayout binding="#{CaseItems.pnlContainer}"
                                        id="pnlContainer"
                                        styleClass="itemsContainerPanel">

                        <ui:panelLayout binding="#{CaseItems.layoutPanel}"
                                        id="layoutPanel1"
                                        styleClass="casesPanel"
                                        style="position: absolute; left:160px">

                            <ui:staticText binding="#{CaseItems.staticText}"
                                           id="staticText1"
                                           styleClass="pageHeading"
                                           style="left: 12px; top: 12px"
                                           text="Work Items of Selected Case"/>

                            <ui:button binding="#{SessionBean.btnRefresh}"
                                        action="#{CaseItems.btnRefresh_action}"
                                        id="btnRefresh"
                                        imageURL="/resources/refresh.png"
                                        styleClass="refreshButton"
                                        toolTip="Refresh Work Items"
                                        text=""/>

                             <ui:button binding="#{SessionBean.btnLogout}"
                                        action="#{CaseItems.btnLogout_action}"
                                        id="btnLogout"
                                        imageURL="/resources/logout.png"
                                        styleClass="logoutButton"
                                        toolTip="Logout"
                                        text=""/>

                             <ui:button binding="#{SessionBean.btnBack}"
                                        action="#{CaseItems.btnBack_action}"
                                        id="btnBack"
                                        imageURL="/resources/back.png"
                                        styleClass="backButton"
                                        toolTip="Return to Active Cases"
                                        text=""/>

                         </ui:panelLayout>

                         <ui:panelGroup binding="#{CaseItems.pnlGroupCaseData}"
                                        id="pnlGroupCaseData"
                                        styleClass="caseDataPnlGroup">

                            <ui:staticText binding="#{CaseItems.stCaseStart}"
                                           id="stCaseStart"
                                           styleClass="pageSubheading"
                                           style="left: 12px; top: 12px"
                                           text="Case Start"/>

                             <ui:staticText binding="#{CaseItems.stStartingService}"
                                            id="stStartingService"
                                            styleClass="pageSubheading"
                                            style="left: 12px; top: 52px"
                                            text="Starting Service"/>

                             <ui:staticText binding="#{CaseItems.stStartedBy}"
                                            id="stStartedBy"
                                            styleClass="pageSubheading"
                                            style="left: 12px; top: 92px"
                                            text="Started By"/>

                             <ui:staticText binding="#{CaseItems.stCaseStartText}"
                                            id="stCaseStartText"
                                            style="left: 12px; top: 30px; position: absolute"
                                            text="#{SessionBean.caseStartTime}"/>

                             <ui:staticText binding="#{CaseItems.stStartingServiceText}"
                                            id="stStartingServiceText"
                                            style="left: 12px; top: 70px; position: absolute"
                                            text="#{SessionBean.startingServiceName}"/>

                             <ui:staticText binding="#{CaseItems.stStartedByText}"
                                            id="stStartedByText"
                                            style="left: 12px; top: 110px; position: absolute"
                                            text="#{SessionBean.caseStartedByName}"/>

                             <ui:staticText binding="#{CaseItems.stCaseData}"
                                            id="stCaseData"
                                            styleClass="pageSubheading"
                                            style="left: 202px; top: 12px"
                                            text="Case Data"/>

                            <ui:textArea binding="#{CaseItems.taCaseData}"
                                         id="txtCaseData"
                                         styleClass="caseDataTextArea"
                                         rows="4"
                                         disabled="true"
                                         text="#{SessionBean.caseData}"/>
                         </ui:panelGroup>    


                            <ui:panelGroup binding="#{CaseItems.pnlGroup}"
                                            id="pnlGroup"
                                            styleClass="itemsTablePnlGroup">


                            <h:dataTable binding="#{CaseItems.dataTable}"
                                         headerClass="dataTableHeader"
                                         id="dataTable1"
                                         cellpadding="3"
                                         styleClass="dataTable"
                                         columnClasses="itemIDCol,
                                                        taskIDCol,
                                                        statusCol,
                                                        serviceCol,
                                                        timeCol,
                                                        timeCol,
                                                        timeCol,
                                                        timerStatusCol,
                                                        timerExpiryCol"
                                         value="#{SessionBean.caseItems}"
                                         var="currentRow"
                                         width="940">

                                <h:column binding="#{CaseItems.colItemID}"
                                          id="colItemID">
                                           <f:facet name="header" >
                                               <h:commandLink value="#{CaseItems.caseIDHeaderText}"
                                                       action="#{CaseItems.caseIDHeaderClick}"/>
                                           </f:facet>
                                    <h:outputText binding="#{CaseItems.colItemIDRows}"
                                                  id="colItemIDRows"
                                                  styleClass="dataTableItemText"
                                                  value="#{currentRow.caseID}"/>
                                </h:column>

                                <h:column binding="#{CaseItems.colTaskID}"
                                          id="colTaskID">
                                    <f:facet name="header" >
                                        <h:commandLink value="#{CaseItems.taskIDHeaderText}"
                                                action="#{CaseItems.taskIDHeaderClick}"/>
                                    </f:facet>
                                    <h:outputText binding="#{CaseItems.colTaskIDRows}"
                                                  id="colTaskIDRows"
                                                  styleClass="dataTableItemText"
                                                  value="#{currentRow.taskID}"/>
                                </h:column>

                                <h:column binding="#{CaseItems.colStatus}"
                                          id="colStatus">
                                    <f:facet name="header" >
                                        <h:commandLink value="#{CaseItems.statusHeaderText}"
                                                action="#{CaseItems.statusHeaderClick}"/>
                                    </f:facet>
                                    <h:outputText binding="#{CaseItems.colStatusRows}"
                                                  id="colStatusRows"
                                                  styleClass="dataTableItemText"
                                                  value="#{currentRow.plainStatus}"/>
                                </h:column>

                                <h:column binding="#{CaseItems.colService}"
                                          id="colService">
                                    <f:facet name="header" >
                                        <h:commandLink value="#{CaseItems.serviceHeaderText}"
                                                action="#{CaseItems.serviceHeaderClick}"/>
                                    </f:facet>
                                    <h:outputText binding="#{CaseItems.colServiceRows}"
                                                  id="colServiceRows"
                                                  styleClass="dataTableItemText"
                                                  value="#{currentRow.resourceName}"/>
                                </h:column>

                                <h:column binding="#{CaseItems.colEnabledTime}"
                                          id="colEnabledTime">
                                    <f:facet name="header" >
                                        <h:commandLink value="#{CaseItems.enabledTimeHeaderText}"
                                                action="#{CaseItems.enabledTimeHeaderClick}"/>
                                    </f:facet>
                                    <h:outputText binding="#{CaseItems.colEnabledTimeRows}"
                                                  id="colEnabledTimeRows"
                                                  styleClass="dataTableItemText"
                                                  value="#{currentRow.enabledTimeAsDateString}"/>
                                </h:column>

                                <h:column binding="#{CaseItems.colStartTime}"
                                          id="colStartTime">
                                    <f:facet name="header" >
                                        <h:commandLink value="#{CaseItems.startTimeHeaderText}"
                                                action="#{CaseItems.startTimeHeaderClick}"/>
                                    </f:facet>
                                    <h:outputText binding="#{CaseItems.colStartTimeRows}"
                                                  id="colStartTimeRows"
                                                  styleClass="dataTableItemText"
                                                  value="#{currentRow.startTimeAsDateString}"/>
                                </h:column>

                                <h:column binding="#{CaseItems.colCompletionTime}"
                                          id="colCompletionTime">
                                    <f:facet name="header" >
                                        <h:commandLink value="#{CaseItems.completionTimeHeaderText}"
                                                action="#{CaseItems.completionTimeHeaderClick}"/>
                                    </f:facet>
                                    <h:outputText binding="#{CaseItems.colCompletionTimeRows}"
                                                  id="colCompletionTimeRows"
                                                  styleClass="dataTableItemText"
                                                  value="#{currentRow.completionTimeAsDateString}"/>
                                </h:column>

                                <h:column binding="#{CaseItems.colTimerStatus}"
                                          id="colTimerStatus">
                                    <f:facet name="header" >
                                        <h:commandLink value="#{CaseItems.timerStatusHeaderText}"
                                                action="#{CaseItems.timerStatusHeaderClick}"/>
                                    </f:facet>
                                    <h:outputText binding="#{CaseItems.colTimerStatusRows}"
                                                  id="colTimerStatusRows"
                                                  styleClass="dataTableItemText"
                                                  value="#{currentRow.timerStatus}"/>
                                </h:column>

                                <h:column binding="#{CaseItems.colTimerExpiry}"
                                          id="colTimerExpiry">
                                    <f:facet name="header" >
                                       <h:commandLink value="#{CaseItems.timerExpiryHeaderText}"
                                               action="#{CaseItems.timerExpiryHeaderClick}"/>
                                    </f:facet>
                                    <h:outputText binding="#{CaseItems.colTimerExpiryRows}"
                                                  id="colTimerExpiryRows"
                                                  styleClass="dataTableItemText"
                                                  value="#{currentRow.timerExpiryAsCountdown}"/>
                                </h:column>

                            </h:dataTable>
                            </ui:panelGroup>

                        <div><jsp:include page="pfMsgPanel.jspf"/></div>

                         </ui:panelLayout>

                        </center>

                        <ui:hiddenField binding="#{CaseItems.hdnRowIndex}" id="hdnRowIndex"/>

                        <ui:button binding="#{CaseItems.btnDetails}"
                                   action="#{CaseItems.btnDetails_action}"
                                   id="btnDetails"
                                   style="display: none"
                                   text=""/>

                    </ui:form>

                    <ui:script>
                        addOnclickToDatatableRows();
                    </ui:script>

                </ui:body>
            </ui:html>
        </ui:page>
    </f:view>
</jsp:root>
