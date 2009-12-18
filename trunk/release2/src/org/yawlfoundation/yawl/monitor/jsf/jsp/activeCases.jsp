<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="1.2" xmlns:f="http://java.sun.com/jsf/core" xmlns:h="http://java.sun.com/jsf/html" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:ui="http://www.sun.com/web/ui">
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


                            <ui:panelGroup binding="#{ActiveCases.pnlGroup}"
                                            id="pnlGroup"
                                            style="position: absolute"
                                            styleClass="tablePnlGroup">

                                <ui:button binding="#{ActiveCases.btnCaseHeader}"
                                           action="#{ActiveCases.btnCaseHeader_action}"
                                           id="btnCaseHeader"
                                           styleClass="headerButton"
                                           style="left: 0; width: 87px;"
                                           text="#{ActiveCases.btnCaseHeaderText}"/>
                                
                                <ui:button binding="#{ActiveCases.btnSpecNameHeader}"
                                           action="#{ActiveCases.btnSpecNameHeader_action}"
                                           id="btnSpecNameHeader"
                                           styleClass="headerButton"
                                           style="left: 87px; width: 225px;"
                                           text="#{ActiveCases.btnSpecNameHeaderText}"/>

                                <ui:button binding="#{ActiveCases.btnSpecVersionHeader}"
                                           action="#{ActiveCases.btnSpecVersionHeader_action}"
                                           id="btnSpecVersionHeader"
                                           styleClass="headerButton"
                                           style="left: 312px; width: 85px;"
                                           text="#{ActiveCases.btnSpecVersionHeaderText}"/>

                                <ui:button binding="#{ActiveCases.btnStartTimeHeader}"
                                           action="#{ActiveCases.btnStartTimeHeader_action}"
                                           id="btnStartTimeHeader"
                                           styleClass="headerButton"
                                           style="left: 397px; width: 171px;"
                                           text="#{ActiveCases.btnStartTimeHeaderText}"/>


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
                                              <h:outputText value="Case"/>
                                           </f:facet>
                                    <h:outputText binding="#{ActiveCases.colCaseIDRows}"
                                                  id="colCaseIDRows"
                                                  styleClass="dataTableText"
                                                  value="#{currentRow.caseID}"/>
                                </h:column>

                                <h:column binding="#{ActiveCases.colSpecName}"
                                          id="colSpecName">
                                    <f:facet name="header" >
                                       <h:outputText value="Spec Name"/>
                                    </f:facet>
                                    <h:outputText binding="#{ActiveCases.colSpecNameRows}"
                                                  id="colSpecNameRows"
                                                  styleClass="dataTableText"
                                                  value="#{currentRow.specName}"/>
                                </h:column>

                                <h:column binding="#{ActiveCases.colVersion}"
                                          id="colVersion">
                                    <f:facet name="header" >
                                       <h:outputText value="Version"/>
                                    </f:facet>
                                    <h:outputText binding="#{ActiveCases.colVersionRows}"
                                                  id="colVersionRows"
                                                  styleClass="dataTableText"
                                                  value="#{currentRow.specVersion}"/>
                                </h:column>

                                <h:column binding="#{ActiveCases.colStartTime}"
                                          id="colStartTime">
                                    <f:facet name="header" >
                                       <h:outputText value="Start Time"/>
                                    </f:facet>
                                    <h:outputText binding="#{ActiveCases.colStartTimeRows}"
                                                  id="colStartTimeRows"
                                                  styleClass="dataTableText"
                                                  value="#{currentRow.startTimeAsDateString}"/>
                                </h:column>

                            </h:dataTable>
                            </ui:panelGroup>

                         </ui:panelLayout>

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
                        <ui:panelLayout binding="#{SessionBean.messagePanel}"
                                        id="msgPanel"
                                        panelLayout="flow"/>
                        </center>

                        <ui:hiddenField binding="#{ActiveCases.hdnRowIndex}" id="hdnRowIndex"/>

                        <ui:button binding="#{ActiveCases.btnDetails}"
                                   action="#{ActiveCases.btnDetails_action}"
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
