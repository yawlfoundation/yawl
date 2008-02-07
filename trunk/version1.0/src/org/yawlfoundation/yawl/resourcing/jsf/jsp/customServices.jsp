<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="1.2"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:jsp="http://java.sun.com/JSP/Page"
          xmlns:ui="http://www.sun.com/web/ui">

    <jsp:directive.page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"/>

    <f:view>
        <ui:page binding="#{customServices.page1}" id="page1">
            <ui:html binding="#{customServices.html1}" id="html1">
                <ui:head binding="#{customServices.head1}" id="head1">
                    <ui:link binding="#{customServices.link1}" id="link1"
                             url="/resources/stylesheet.css"/>

                    <ui:script binding="#{customServices.script1}" id="script1"
                               url="/resources/script.js"/>

                </ui:head>
                <ui:body binding="#{customServices.body1}" id="body1"
                         style="-rave-layout: grid"
                         focus="form1:txtName">

                    <ui:form binding="#{customServices.form1}" id="form1">

                        <!-- include banner and menu -->
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

                        <!-- Registered Services Panel -->
                        <ui:panelLayout binding="#{customServices.pnlServices}"
                                        id="pnlServices"
                                        styleClass="servicesPanel"
                                        style="height: 240px; top: 77px">

                            <ui:staticText binding="#{customServices.staticText2}"
                                           id="staticText2"
                                           styleClass="pageSubheading"
                                           style="left: 12px; top: 12px"
                                           text="Registered Services"/>
                            
                            <h:dataTable binding="#{customServices.dataTable1}"
                                         headerClass="dataTableHeader"
                                         id="dataTable1"
                                         columnClasses="servicesNameCol,
                                                        servicesURICol,
                                                        servicesDescCol,
                                                        dataTableScrollBarCol"
                                         cellpadding="3"
                                         styleClass="dataTable"
                                         value="#{SessionBean.registeredServices}"
                                         var="currentRow"
                                         width="638">

                                <h:column binding="#{customServices.colName}"
                                          id="colName">

                                    <h:outputText binding="#{customServices.colNameRows}"
                                                  id="colNameRows"
                                                  value="#{currentRow._serviceName}"/>

                                    <f:facet name="header">
                                        <h:outputText binding="#{customServices.colNameHeader}"
                                                      id="colNameHeader"
                                                      value="Name"/>
                                    </f:facet>
                                </h:column>

                                <h:column binding="#{customServices.colURI}"
                                          id="colURI">

                                    <h:outputText binding="#{customServices.colURIRows}"
                                                  id="colURIRows"
                                                  value="#{currentRow._yawlServiceID}"/>

                                    <f:facet name="header">
                                        <h:outputText binding="#{customServices.colURIHeader}"
                                                      id="colURIHeader"
                                                      value="Service URI"/>
                                    </f:facet>
                                </h:column>

                                <h:column binding="#{customServices.colDescription}"
                                          id="colDescription">
                                    <h:outputText binding="#{customServices.colDescriptionRows}"
                                                  id="colDescriptionRows"
                                                  value="#{currentRow._documentation}"/>

                                    <f:facet name="header">
                                        <h:outputText binding="#{customServices.colDescriptionHeader}"
                                                      id="colDescriptionHeader"
                                                      value="Description"/>
                                    </f:facet>
                                </h:column>

                                <h:column binding="#{customServices.colSBar}"
                                          id="colSBar"/>

                            </h:dataTable>


                            <ui:button action="#{customServices.btnRemove_action}"
                                       binding="#{customServices.btnRemove}"
                                       id="btnRemove"
                                       styleClass="servicesButton"
                                       style="left: 11px; top: 200px"
                                       text="Remove"/>
                        </ui:panelLayout>


                        <!-- Add Service Panel -->
                        <ui:panelLayout binding="#{customServices.pnlAddService}"
                                        id="pnlAddService"
                                        styleClass="servicesPanel"
                                        style="height: 210px; top: 323px">

                            <ui:staticText binding="#{customServices.staticText1}"
                                           id="staticText1"
                                           styleClass="pageSubheading"
                                           style="left: 12px; top: 12px"
                                           text="Add Service"/>

                            <ui:textField binding="#{customServices.txtName}"
                                          id="txtName"
                                          label="Name:"
                                          columns="40"
                                          style="left: 48px; top: 40px; position: absolute"/>

                            <ui:textField binding="#{customServices.txtURL}"
                                          id="txtURL"
                                          label="URI:"
                                          columns="40"
                                          style="left: 59px; top: 70px; position: absolute"/>

                            <ui:textArea binding="#{customServices.txtDescription}"
                                         id="txtDescription"
                                         label="Description:"
                                         columns="37"
                                         style="height: 110px; left: 12px; top: 100px; position: absolute"/>

                            <ui:button action="#{customServices.btnAdd_action}"
                                       binding="#{customServices.btnAdd}"
                                       id="btnAdd"
                                       styleClass="servicesButton"
                                       style="left: 11px; top: 168px"
                                       text="Add"/>

                            <ui:button action="#{customServices.btnClear_action}"
                                       binding="#{customServices.btnClear}"
                                       id="btnClear"
                                       styleClass="servicesButton"
                                       style="left: 119px; top: 168px"
                                       text="Clear"/>

                        </ui:panelLayout>

                        <ui:hiddenField binding="#{customServices.hdnRowIndex}" id="hdnRowIndex"/>

                    </ui:form>

                    <ui:script>
                        addOnclickToDatatableRows();
                    </ui:script>

                </ui:body>
            </ui:html>
        </ui:page>
    </f:view>
</jsp:root>
