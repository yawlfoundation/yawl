<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="1.2" xmlns:f="http://java.sun.com/jsf/core" xmlns:h="http://java.sun.com/jsf/html" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:ui="http://www.sun.com/web/ui">
    <jsp:directive.page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"/>
    <f:view>
        <ui:page binding="#{ActiveCases.page1}" id="page1">
            <ui:html binding="#{ActiveCases.html1}" id="html1">
                <ui:head binding="#{ActiveCases.head1}" id="head1"
                         title="YAWL 2.0 Case Management">

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

                        <!-- Upload Panel -->
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

                            <h:dataTable binding="#{ActiveCases.dataTable}"
                                         headerClass="dataTableHeader"
                                         id="dataTable"
                                         cellpadding="3"
                                         styleClass="dataTable"
                                         columnClasses="caseIDCol,
                                                        specNameCol,
                                                        versionCol,
                                                        startedByCol,
                                                        startTimeCol,
                                                        ageCol"                                    
                                         value="#{SessionBean.activeCases}"
                                         var="currentRow"
                                         width="570">

                                <h:column binding="#{ActiveCases.colCaseID}"
                                          id="colCaseID">
                                    <h:outputText binding="#{ActiveCases.colCaseIDRows}"
                                                  id="colCaseIDRows"
                                                  value="#{currentRow.caseID}"/>
                                </h:column>

                                <h:column binding="#{ActiveCases.colSpecName}"
                                          id="colSpecName">
                                    <h:outputText binding="#{ActiveCases.colSpecNameRows}"
                                                  id="colSpecNameRows"
                                                  value="#{currentRow.specName}"/>
                                </h:column>

                                <h:column binding="#{ActiveCases.colVersion}"
                                          id="colVersion">
                                    <h:outputText binding="#{ActiveCases.colVersionRows}"
                                                  id="colVersionRows"
                                                  value="#{currentRow.specVersion}"/>
                                </h:column>

                                <h:column binding="#{ActiveCases.colStartedBy}"
                                          id="colStartedBy">
                                    <h:outputText binding="#{ActiveCases.colStartedByRows}"
                                                  id="colStartedByRows"
                                                  value="#{currentRow.startedBy}"/>
                                </h:column>

                                <h:column binding="#{ActiveCases.colStartTime}"
                                          id="colStartTime">
                                    <h:outputText binding="#{ActiveCases.colStartTimeRows}"
                                                  id="colStartTimeRows"
                                                  value="#{currentRow.startTimeAsDateString}"/>
                                </h:column>

                                <h:column binding="#{ActiveCases.colAge}"
                                          id="colAge">
                                    <h:outputText binding="#{ActiveCases.colAgeRows}"
                                                  id="colAgeRows"
                                                  value="#{currentRow.ageAsDateString}"/>
                                </h:column>
                            </h:dataTable>
                            </ui:panelGroup>

                         </ui:panelLayout>

                            <ui:button binding="#{SessionBean.btnRefresh}"
                                       action="#{ActiveCases.btnRefresh_action}"
                                       id="btnRefresh"
                                       imageURL="/resources/refresh.png"
                                       styleClass="refreshCasesButton"
                                       toolTip="Refresh Active Cases Table"
                                       text=""/>


                        </ui:panelLayout>
                        <ui:panelLayout binding="#{SessionBean.messagePanel}"
                                        id="msgPanel"
                                        panelLayout="flow"/>
                        </center>

                        <ui:hiddenField binding="#{ActiveCases.hdnRowIndex}" id="hdnRowIndex"/>

                    </ui:form>

                    <ui:script>
                        addOnclickToDatatableRows();
                    </ui:script>

                </ui:body>
            </ui:html>
        </ui:page>
    </f:view>
</jsp:root>
