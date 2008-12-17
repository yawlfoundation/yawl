<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="1.2" xmlns:f="http://java.sun.com/jsf/core" xmlns:h="http://java.sun.com/jsf/html" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:ui="http://www.sun.com/web/ui">
    <jsp:directive.page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"/>
    <f:view>
        <ui:page binding="#{caseMgt.page1}" id="page1">
            <ui:html binding="#{caseMgt.html1}" id="html1">
                <ui:head binding="#{caseMgt.head1}" id="head1"
                         title="YAWL 2.0 Case Management">

                    <ui:link binding="#{caseMgt.link1}" id="link1"
                             url="/resources/stylesheet.css"/>

                    <ui:link binding="#{ApplicationBean.favIcon}" id="lnkFavIcon"
                             rel="shortcut icon"
                            type="image/x-icon" url="/resources/favicon.ico"/>

                    <ui:script binding="#{SessionBean.script}" id="script1"
                               url="/resources/script.js"/>

                </ui:head>
                <ui:body binding="#{caseMgt.body1}" id="body1"
                         style="-rave-layout: grid">
                    <ui:form binding="#{caseMgt.form1}" id="form1">

                        <!-- include banner -->
                        <div><jsp:directive.include file="pfHeader.jspf"/></div>

                        <div style="top: 20px; position: relative">
                            <jsp:directive.include file="pfMenubar.jspf"/>
                        </div>
                        <center>

                        <ui:panelLayout binding="#{caseMgt.pnlContainer}"
                                        id="pnlContainer"
                                        styleClass="caseMgtContainerPanel">

                        <!-- Upload Panel -->
                        <ui:panelLayout binding="#{caseMgt.layoutPanel1}"
                                        id="layoutPanel1"
                                        styleClass="caseMgtPanel"
                                        style="position: absolute; height: 120px">

                            <ui:staticText binding="#{caseMgt.staticText1}"
                                           id="staticText1"
                                           styleClass="pageSubheading"
                                           style="left: 12px; top: 12px"
                                           text="Upload Specification"/>

                            <ui:upload binding="#{caseMgt.fileUpload1}"
                                       columns="60" id="fileUpload1"
                                       styleClass="fileUpload"
                                       immediate="true"
                                       style="left: 10px; top: 40px"
                                       valueChangeListener="#{caseMgt.fileUpload1_processValueChange}"/>

                            <ui:button action="#{caseMgt.btnUpload_action}"
                                       binding="#{caseMgt.btnUpload}"
                                       id="btnUpload"
                                       style="left: 11px; top: 81px" 
                                       styleClass="caseMgtButton"
                                       text="Upload File"/>
                        </ui:panelLayout>

                        <!-- Loaded Specs Panel -->
                        <ui:panelLayout binding="#{caseMgt.layoutPanel2}"
                                        id="layoutPanel2"
                                        styleClass="caseMgtPanel"
                                        style="position: absolute; height: 270px; top: 123px">

                            <ui:button action="#{caseMgt.btnLaunch_action}"
                                       binding="#{caseMgt.btnLaunch}"
                                       id="btnLaunch"
                                       styleClass="caseMgtButton"
                                       style="left: 11px; top: 230px"
                                       text="Launch Case"/>

                            <ui:staticText binding="#{caseMgt.staticText2}"
                                           id="staticText2"
                                           styleClass="pageSubheading"
                                           style="left: 12px; top: 12px"
                                           text="Loaded Specifications"/>

                            <ui:panelGroup binding="#{caseMgt.pnlGroup}"
                                            id="pnlGroup"
                                            style="position: absolute"
                                            styleClass="tablePnlGroup">

                            <h:dataTable binding="#{caseMgt.dataTable1}"
                                         headerClass="dataTableHeader"
                                         id="dataTable1"
                                         cellpadding="3"
                                         styleClass="dataTable"
                                         columnClasses="specsNameCol,
                                                        specsVersCol,
                                                        specsDescCol,
                                                        dataTableScrollBarCol"                                    
                                         value="#{SessionBean.loadedSpecs}"
                                         var="currentRow"
                                         width="570">

                                <h:column binding="#{caseMgt.colName}"
                                          id="colName">

                                    <h:outputText binding="#{caseMgt.colNameRows}"
                                                  id="colNameRows"
                                                  value="#{currentRow.ID}"/>

                                </h:column>

                                <h:column binding="#{caseMgt.colVersion}"
                                          id="colVersion">
                                    <h:outputText binding="#{caseMgt.colVersionRows}"
                                                  id="colVersionRows"
                                                  value="#{currentRow.specVersion}"/>

                                </h:column>

                                <h:column binding="#{caseMgt.colDescription}"
                                          id="colDescription">
                                    <h:outputText binding="#{caseMgt.colDescriptionRows}"
                                                  id="colDescriptionRows"
                                                  value="#{currentRow.documentation}"/>

                                </h:column>

                                <h:column binding="#{caseMgt.colSBar}"
                                          id="colSBar"/>

                            </h:dataTable>
                            </ui:panelGroup>

                            <ui:button action="#{caseMgt.btnUnload_action}"
                                       binding="#{caseMgt.btnUnload}"
                                       id="btnUnload"
                                       styleClass="caseMgtButton"
                                       style="left: 119px; top: 230px"
                                       text="Unload Spec"/>
                        </ui:panelLayout>

                        <!-- Running Cases Panel -->
                        <ui:panelLayout binding="#{caseMgt.layoutPanel3}"
                                        id="layoutPanel3"
                                        styleClass="caseMgtPanel"
                                        style="height: 194px; top: 396px">
                            
                            <ui:button binding="#{SessionBean.btnRefresh}"
                                       action="#{caseMgt.btnRefresh_action}"
                                       id="btnRefresh"
                                       imageURL="/resources/refresh.png"
                                       styleClass="refreshCasesButton"
                                       toolTip="Refresh Running Cases"
                                       text=""/>

                            <ui:button action="#{caseMgt.btnCancelCase_action}"
                                       binding="#{caseMgt.btnCancelCase}"
                                       id="btnCancelCase"
                                       styleClass="caseMgtButton"
                                       style="left: 11px; top: 156px"
                                       text="Cancel Case"/>

                            <ui:button action="#{caseMgt.btnRaiseException_action}"
                                       binding="#{caseMgt.btnRaiseException}"
                                       id="btnRaiseException"
                                       styleClass="caseMgtButton"
                                       style="left: 119px; top: 156px"
                                       visible="false"
                                       text="Raise Exception"/>

                            <ui:button action="#{caseMgt.btnRejectWorklet_action}"
                                       binding="#{caseMgt.btnRejectWorklet}"
                                       id="btnRejectWorklet"
                                       styleClass="caseMgtButton"
                                       style="left: 227px; top: 156px"
                                       visible="false"
                                       text="Reject Worklet"/>

                            <ui:listbox binding="#{caseMgt.lbxRunningCases}"
                                        id="lbxRunningCases"
                                        items="#{SessionBean.runningCaseListOptions}"
                                        selected="#{SessionBean.runningCaseListChoice}"
                                        styleClass="caseMgtListbox"/>

                            <ui:staticText binding="#{caseMgt.staticText3}"
                                           id="staticText3"
                                           styleClass="pageSubheading"
                                           style="left: 12px; top: 12px"
                                           text="Running Cases"/>
                        </ui:panelLayout>

                        </ui:panelLayout>
                        <ui:panelLayout binding="#{SessionBean.messagePanel}"
                                        id="msgPanel"
                                        panelLayout="flow"/>
                        </center>

                        <ui:hiddenField binding="#{caseMgt.hdnRowIndex}" id="hdnRowIndex"/>

                    </ui:form>

                    <ui:script>
                        addOnclickToDatatableRows();
                    </ui:script>

                </ui:body>
            </ui:html>
        </ui:page>
    </f:view>
</jsp:root>
