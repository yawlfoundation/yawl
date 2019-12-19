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
        <ui:page binding="#{ItemParams.page1}" id="page1">
            <ui:html binding="#{ItemParams.html1}" id="html1">
                <ui:head binding="#{ItemParams.head1}" id="head1"
                         title="YAWL Monitor Service - Selected Case Detail">

                    <ui:link binding="#{ItemParams.link1}" id="link1"
                             url="/resources/stylesheet.css"/>

                    <ui:link binding="#{ApplicationBean.favIcon}" id="lnkFavIcon"
                             rel="shortcut icon"
                            type="image/x-icon" url="/resources/favicon.ico"/>

                    <ui:script binding="#{SessionBean.script}" id="script1"
                               url="/resources/script.js"/>

                </ui:head>
                <ui:body binding="#{ItemParams.body1}" id="body1"
                         style="-rave-layout: grid">
                    <ui:form binding="#{ItemParams.form1}" id="form1">

                        <!-- include banner -->
                        <div><jsp:directive.include file="pfHeader.jspf"/></div>

                        <center>

                        <ui:panelLayout binding="#{ItemParams.pnlContainer}"
                                        id="pnlContainer"
                                        styleClass="itemsContainerPanel">

                        <ui:panelLayout binding="#{ItemParams.layoutPanel}"
                                        id="layoutPanel1"
                                        styleClass="casesPanel"
                                        style="position: absolute; left:160px">

                            <ui:staticText binding="#{ItemParams.staticText}"
                                           id="staticText1"
                                           styleClass="pageHeading"
                                           style="left: 12px; top: 12px"
                                           text="Parameters of Selected Work Item"/>

                            <ui:button binding="#{SessionBean.btnRefresh}"
                                        action="#{ItemParams.btnRefresh_action}"
                                        id="btnRefresh"
                                        imageURL="/resources/refresh.png"
                                        styleClass="refreshButton"
                                        toolTip="Refresh Parameters"
                                        text=""/>

                             <ui:button binding="#{SessionBean.btnLogout}"
                                        action="#{ItemParams.btnLogout_action}"
                                        id="btnLogout"
                                        imageURL="/resources/logout.png"
                                        styleClass="logoutButton"
                                        toolTip="Logout"
                                        text=""/>

                             <ui:button binding="#{SessionBean.btnBack}"
                                        action="#{ItemParams.btnBack_action}"
                                        id="btnBack"
                                        imageURL="/resources/back.png"
                                        styleClass="backButton"
                                        toolTip="Return to Work Items"
                                        text=""/>

                         </ui:panelLayout>


                        <ui:panelGroup binding="#{ItemParams.pnlGroupLogData}"
                                       id="pnlGroupLogData"
                                       styleClass="caseDataPnlGroup">

                           <ui:staticText binding="#{ItemParams.stEngineLogHeading}"
                                          id="stEngineLogHeading"
                                          styleClass="pageSubheading"
                                          style="left: 12px; top: 12px"
                                          text="Engine Log Events"/>

                           <ui:staticText binding="#{ItemParams.stResourceLogHeading}"
                                          id="stResourceLogHeading"
                                          styleClass="pageSubheading"
                                          style="left: 312px; top: 12px"
                                          text="Resource Log Events"/>

                           <ui:panelGroup binding="#{ItemParams.itemEngineLogPnlGroup}"
                                          id="itemEngineLogPnlGroup"
                                          style="left:12px"
                                          styleClass="itemLogTablePnlGroup">

                           <h:dataTable binding="#{ItemParams.dtabEngineLog}"
                                         headerClass="dataTableSubHeader"
                                         id="dtabEngineLog"
                                         cellpadding="3"
                                         style="position: absolute"
                                         columnClasses="logTimeCol, logDataCol"
                                         value="#{SessionBean.itemEngineLogEvents}"
                                         var="currentRow"
                                         width="276">

                                <h:column binding="#{ItemParams.colEngineEventTime}"
                                          id="colEngineEventTime">
                                    <h:outputText binding="#{ItemParams.colEngineTimeRows}"
                                                  id="colEngineEventTimeRows"
                                                  styleClass="dataTableItemText"
                                                  value="#{currentRow.timestampMidString}"/>
                                </h:column>

                               <h:column binding="#{ItemParams.colEngineEvent}"
                                         id="colEngineEvent">
                                   <h:outputText binding="#{ItemParams.colEngineEventRows}"
                                                 id="colEngineEventRows"
                                                 styleClass="dataTableItemText"
                                                 value="#{currentRow.descriptor}"/>
                               </h:column>

                            </h:dataTable>

                            </ui:panelGroup>

                           <ui:panelGroup binding="#{ItemParams.itemResourceLogPnlGroup}"
                                          id="itemResourceLogPnlGroup"
                                          style="left:312px"
                                          styleClass="itemLogTablePnlGroup">

                            <h:dataTable binding="#{ItemParams.dtabResourceLog}"
                                          headerClass="dataTableSubHeader"
                                          id="dtabResourceLog"
                                          cellpadding="3"
                                          style="position: absolute"
                                          columnClasses="logTimeCol, logDataCol, logDataCol"
                                          value="#{SessionBean.itemResourceLogEvents}"
                                          var="currentRow"
                                          width="276">

                                 <h:column binding="#{ItemParams.colResourceEventTime}"
                                           id="colResourceEventTime">
                                      <h:outputText binding="#{ItemParams.colResourceTimeRows}"
                                                   id="colResourceEventTimeRows"
                                                   styleClass="dataTableItemText"
                                                   value="#{currentRow.timeStampMidString}"/>
                                 </h:column>

                                <h:column binding="#{ItemParams.colResourceEvent}"
                                          id="colResourceEvent">
                                    <h:outputText binding="#{ItemParams.colResourceEventRows}"
                                                  id="colResourceEventRows"
                                                  styleClass="dataTableItemText"
                                                  value="#{currentRow._event}"/>
                                </h:column>

                                <h:column binding="#{ItemParams.colResourceUser}"
                                          id="colResourceUser">
                                    <h:outputText binding="#{ItemParams.colResourceUserRows}"
                                                  id="colResourceUserRows"
                                                  styleClass="dataTableItemText"
                                                  value="#{currentRow._resourceID}"/>
                                </h:column>

                             </h:dataTable>       

                          </ui:panelGroup>

                         </ui:panelGroup>

                            <ui:panelGroup binding="#{ItemParams.pnlGroup}"
                                            id="pnlGroup"
                                            style="position: absolute"
                                            styleClass="itemsTablePnlGroup">

                            <h:dataTable binding="#{ItemParams.dataTable}"
                                         headerClass="dataTableHeader"
                                         id="dataTable1"
                                         cellpadding="3"
                                         styleClass="dataTable"
                                         columnClasses="itemIDCol,
                                                        taskIDCol,
                                                        dataCol,
                                                        statusCol,
                                                        dataCol,
                                                        dataCol,
                                                        dataCol,
                                                        dataCol,
                                                        dataCol"
                                         value="#{SessionBean.itemParams}"
                                         var="currentRow"
                                         width="940">

                                <h:column binding="#{ItemParams.colName}"
                                          id="colName">
                                           <f:facet name="header" >
                                               <h:commandLink value="#{ItemParams.nameHeaderText}"
                                                       action="#{ItemParams.nameHeaderClick}"/>
                                           </f:facet>
                                    <h:outputText binding="#{ItemParams.colNameRows}"
                                                  id="colNameRows"
                                                  styleClass="dataTableItemText"
                                                  value="#{currentRow.name}"/>
                                </h:column>

                                <h:column binding="#{ItemParams.colDataType}"
                                          id="colDataType">
                                    <f:facet name="header" >
                                        <h:commandLink value="#{ItemParams.dataTypeHeaderText}"
                                                action="#{ItemParams.dataTypeHeaderClick}"/>
                                    </f:facet>
                                    <h:outputText binding="#{ItemParams.colDataTypeRows}"
                                                  id="colDataTypeRows"
                                                  styleClass="dataTableItemText"
                                                  value="#{currentRow.dataType}"/>
                                </h:column>

                                <h:column binding="#{ItemParams.colDataSchema}"
                                          id="colDataSchema">
                                    <f:facet name="header" >
                                        <h:commandLink value="#{ItemParams.dataSchemaHeaderText}"
                                                action="#{ItemParams.dataSchemaHeaderClick}"/>
                                    </f:facet>
                                    <h:outputText binding="#{ItemParams.colDataSchemaRows}"
                                                  id="colDataSchemaRows"
                                                  styleClass="dataTableItemText"
                                                  escape="true"
                                                  value="#{currentRow.dataSchema}"/>
                                </h:column>

                                <h:column binding="#{ItemParams.colUsage}"
                                          id="colUsage">
                                    <f:facet name="header" >
                                        <h:commandLink value="#{ItemParams.usageHeaderText}"
                                                action="#{ItemParams.usageHeaderClick}"/>
                                    </f:facet>
                                    <h:outputText binding="#{ItemParams.colUsageRows}"
                                                  id="colUsageRows"
                                                  styleClass="dataTableItemText"
                                                  value="#{currentRow.usageString}"/>
                                </h:column>

                                <h:column binding="#{ItemParams.colInputPredicate}"
                                          id="colInputPredicate">
                                    <f:facet name="header" >
                                        <h:commandLink value="#{ItemParams.inputPredicateHeaderText}"
                                                action="#{ItemParams.inputPredicateHeaderClick}"/>
                                    </f:facet>
                                    <h:outputText binding="#{ItemParams.colInputPredicateRows}"
                                                  id="colInputPredicateRows"
                                                  styleClass="dataTableItemText"
                                                  escape="true"
                                                  value="#{currentRow.inputPredicate}"/>
                                </h:column>

                                <h:column binding="#{ItemParams.colOutputPredicate}"
                                          id="colOutputPredicate">
                                    <f:facet name="header" >
                                        <h:commandLink value="#{ItemParams.outputPredicateHeaderText}"
                                                action="#{ItemParams.outputPredicateHeaderClick}"/>
                                    </f:facet>
                                    <h:outputText binding="#{ItemParams.colOutputPredicateRows}"
                                                  id="colOutputPredicateRows"
                                                  styleClass="dataTableItemText"
                                                  escape="true"
                                                  value="#{currentRow.outputPredicate}"/>
                                </h:column>

                                <h:column binding="#{ItemParams.colOriginalValue}"
                                          id="colOriginalValue">
                                    <f:facet name="header" >
                                        <h:commandLink value="#{ItemParams.originalValueHeaderText}"
                                                action="#{ItemParams.originalValueHeaderClick}"/>
                                    </f:facet>
                                    <h:outputText binding="#{ItemParams.colOriginalValueRows}"
                                                  id="colOriginalValueRows"
                                                  styleClass="dataTableItemText"
                                                  escape="true"
                                                  value="#{currentRow.originalValue}"/>
                                </h:column>

                                <h:column binding="#{ItemParams.colDefaultValue}"
                                          id="colDefaultValue">
                                    <f:facet name="header" >
                                        <h:commandLink value="#{ItemParams.defaultValueHeaderText}"
                                                action="#{ItemParams.defaultValueHeaderClick}"/>
                                    </f:facet>
                                    <h:outputText binding="#{ItemParams.colDefaultValueRows}"
                                                  id="colDefaultValueRows"
                                                  styleClass="dataTableItemText"
                                                  escape="true"
                                                  value="#{currentRow.defaultValue}"/>
                                </h:column>

                                <h:column binding="#{ItemParams.colValue}"
                                          id="colValue">
                                    <f:facet name="header" >
                                       <h:commandLink value="#{ItemParams.valueHeaderText}"
                                               action="#{ItemParams.valueHeaderClick}"/>
                                    </f:facet>
                                    <h:outputText binding="#{ItemParams.colValueRows}"
                                                  id="colValueRows"
                                                  styleClass="dataTableItemText"
                                                  escape="true"
                                                  value="#{currentRow.value}"/>
                                </h:column>

                            </h:dataTable>
                            </ui:panelGroup>

                        <div><jsp:include page="pfMsgPanel.jspf"/></div>

                         </ui:panelLayout>

                        </center>

                        <ui:hiddenField binding="#{ItemParams.hdnRowIndex}" id="hdnRowIndex"/>

                        <ui:button binding="#{ItemParams.btnDetails}"
                                   action="#{ItemParams.btnDetails_action}"
                                   id="btnDetails"
                                   style="display: none"
                                   text=""/>

                    </ui:form>

                    <ui:script>
                        addBgColorToDatatableRows('form1:dataTable1');
                        addBgColorToDatatableRows('form1:dtabEngineLog');
                        addBgColorToDatatableRows('form1:dtabResourceLog');
                    </ui:script>

                </ui:body>
            </ui:html>
        </ui:page>
    </f:view>
</jsp:root>
