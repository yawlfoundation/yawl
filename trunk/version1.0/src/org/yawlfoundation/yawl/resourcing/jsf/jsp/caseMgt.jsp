<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="1.2" xmlns:f="http://java.sun.com/jsf/core" xmlns:h="http://java.sun.com/jsf/html" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:ui="http://www.sun.com/web/ui">
    <jsp:directive.page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"/>
    <f:view>
        <ui:page binding="#{caseMgt.page1}" id="page1">
            <ui:html binding="#{caseMgt.html1}" id="html1">
                <ui:head binding="#{caseMgt.head1}" id="head1">
                    <ui:link binding="#{caseMgt.link1}" id="link1"
                             url="/resources/stylesheet.css"/>

                    <ui:link binding="#{ApplicationBean.favIcon}" id="lnkFavIcon"
                             rel="shortcut icon"
                            type="image/x-icon" url="/resources/favicon.ico"/>

                    <ui:script binding="#{SessionBean.script1}" id="script1"
                               url="/resources/script.js"/>

                </ui:head>
                <ui:body binding="#{caseMgt.body1}" id="body1"
                         style="-rave-layout: grid">
                    <ui:form binding="#{caseMgt.form1}" id="form1">

                        <ui:panelLayout binding="#{SessionBean.topPanel}"
                                        id="topPanel"
                                        panelLayout="flow"
                                        styleClass="topPanel"/>
                        <div style="left: 0px; top: 0px; position: absolute">
                             <jsp:directive.include file="pfHeader.jspf"/>
                        </div>                        

                        <div style="left: 0px; top: 72px; position: absolute">
                            <jsp:directive.include file="pfMenu.jspf"/>
                        </div>

                        <!-- Upload Panel -->
                        <ui:panelLayout binding="#{caseMgt.layoutPanel1}"
                                        id="layoutPanel1"
                                        styleClass="caseMgtPanel"
                                        style="height: 120px; top: 77px">

                            <ui:staticText binding="#{caseMgt.staticText1}"
                                           id="staticText1"
                                           styleClass="pageSubheading"
                                           style="left: 12px; top: 12px"
                                           text="Upload Specification"/>

                            <ui:upload binding="#{caseMgt.fileUpload1}"
                                       columns="70" id="fileUpload1"
                                       styleClass="fileUpload"
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
                                        style="height: 240px; top: 204px">

                            <ui:button action="#{caseMgt.btnLaunch_action}"
                                       binding="#{caseMgt.btnLaunch}"
                                       id="btnLaunch"
                                       styleClass="caseMgtButton"
                                       style="left: 11px; top: 200px"
                                       text="Launch Case"/>

                            <ui:staticText binding="#{caseMgt.staticText2}"
                                           id="staticText2"
                                           styleClass="pageSubheading"
                                           style="left: 12px; top: 12px"
                                           text="Loaded Specifications"/>

                            <h:dataTable binding="#{caseMgt.dataTable1}"
                                         headerClass="dataTableHeader"
                                         id="dataTable1"
                                         cellpadding="3"
                                         styleClass="dataTable"
                                         columnClasses="specsNameCol,
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

                                    <f:facet name="header">
                                        <h:outputText binding="#{caseMgt.colNameHeader}"
                                                      id="colNameHeader"
                                                      value="Name"/>
                                    </f:facet>
                                </h:column>


                                <h:column binding="#{caseMgt.colDescription}"
                                          id="colDescription">
                                    <h:outputText binding="#{caseMgt.colDescriptionRows}"
                                                  id="colDescriptionRows"
                                                  value="#{currentRow.documentation}"/>

                                    <f:facet name="header">
                                        <h:outputText binding="#{caseMgt.colDescriptionHeader}"
                                                      id="colDescriptionHeader"
                                                      value="Description"/>
                                    </f:facet>
                                </h:column>

                                <h:column binding="#{caseMgt.colSBar}"
                                          id="colSBar"/>

                            </h:dataTable>


                            <ui:button action="#{caseMgt.btnUnload_action}"
                                       binding="#{caseMgt.btnUnload}"
                                       id="btnUnload"
                                       styleClass="caseMgtButton"
                                       style="left: 119px; top: 200px"
                                       text="Unload Spec"/>
                        </ui:panelLayout>

                        <!-- Running Cases Panel -->
                        <ui:panelLayout binding="#{caseMgt.layoutPanel3}"
                                        id="layoutPanel3"
                                        styleClass="caseMgtPanel"
                                        style="height: 194px; top: 451px">
                            
                            <ui:button action="#{caseMgt.btnCancelCase_action}"
                                       binding="#{caseMgt.btnCancelCase}"
                                       id="btnCancelCase"
                                       styleClass="caseMgtButton"
                                       style="left: 11px; top: 156px"
                                       text="Cancel Case"/>

                            <ui:listbox binding="#{caseMgt.lbxRunningCases}"
                                        id="lbxRunningCases"
                                        items="#{SessionBean.runningCaseListOptions}"
                                        rows="8"
                                        selected="#{SessionBean.runningCaseListChoice}"
                                        styleClass="caseMgtListbox"/>

                            <ui:staticText binding="#{caseMgt.staticText3}"
                                           id="staticText3"
                                           styleClass="pageSubheading"
                                           style="left: 12px; top: 12px"
                                           text="Running Cases"/>
                        </ui:panelLayout>

                        <ui:messageGroup binding="#{caseMgt.msgBox}"
                                         id="msgBox"
                                         showGlobalOnly="true"
                                         style="left: 126px; top: 642px; position: absolute; width: 600px"/>

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
