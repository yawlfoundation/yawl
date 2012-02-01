<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="1.2"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:jsp="http://java.sun.com/JSP/Page"
          xmlns:ui="http://www.sun.com/web/ui">

<!--
  ~ Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
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
        <ui:page binding="#{customServices.page1}" id="page1">
            <ui:html binding="#{customServices.html1}" id="html1">
                <ui:head binding="#{customServices.head1}" id="head1"
                         title="YAWL 2.2 Service Management">
                    <ui:link binding="#{customServices.link1}" id="link1"
                             url="/resources/stylesheet.css"/>

                    <ui:link binding="#{ApplicationBean.favIcon}" id="lnkFavIcon"
                             rel="shortcut icon"
                            type="image/x-icon" url="/resources/favicon.ico"/>
                    
                    <ui:script binding="#{SessionBean.script}" id="script1"
                               url="/resources/script.js"/>

                </ui:head>
                <ui:body binding="#{customServices.body1}" id="body1"
                         style="-rave-layout: grid">

                    <ui:form binding="#{customServices.form1}" id="form1">

                        <!-- include banner -->
                        <jsp:include page="pfHeader.jspf"/>

                        <div style="top: 20px; position: relative">
                            <jsp:directive.include file="pfMenubar.jspf"/>
                        </div>
                        <center>
                        <ui:panelLayout binding="#{customServices.pnlContainer}"
                                        id="pnlContainer"
                                        style="#{SessionBean.outerPanelTop}"
                                        styleClass="servicesContainerPanel">

                        <!-- Registered Services Panel -->
                        <ui:panelLayout binding="#{customServices.pnlServices}"
                                        id="pnlServices"
                                        styleClass="servicesPanel"
                                        style="position: absolute; height: 270px; top: 0px">

                            <ui:staticText binding="#{customServices.staticText2}"
                                           id="staticText2"
                                           styleClass="pageSubheading"
                                           style="left: 12px; top: 12px"
                                           text="Registered Services"/>

                            <ui:panelGroup binding="#{customServices.pnlGroup}"
                                            id="cspnlGroup"
                                            style="position: absolute; width: 635px"
                                            styleClass="tablePnlGroup">

                            <h:dataTable binding="#{customServices.dataTable1}"
                                         headerClass="dataTableHeader"
                                         id="dataTable1"
                                         columnClasses="servicesNameCol,
                                                        servicesURICol,
                                                        servicesDescCol,
                                                        dataTableScrollBarCol"
                                         cellpadding="3"
                                         styleClass="dataTable"
                                         value="#{ApplicationBean.registeredServices}"
                                         var="currentRow"
                                         width="638">

                                <h:column binding="#{customServices.colName}"
                                          id="colName">

                                    <h:outputText binding="#{customServices.colNameRows}"
                                                  id="colNameRows"
                                                  value="#{currentRow._serviceName}"/>

                                </h:column>

                                <h:column binding="#{customServices.colURI}"
                                          id="colURI">

                                    <h:outputText binding="#{customServices.colURIRows}"
                                                  id="colURIRows"
                                                  value="#{currentRow._yawlServiceID}"/>

                                </h:column>

                                <h:column binding="#{customServices.colDescription}"
                                          id="colDescription">
                                    <h:outputText binding="#{customServices.colDescriptionRows}"
                                                  id="colDescriptionRows"
                                                  value="#{currentRow._documentation}"/>

                                </h:column>

                                <h:column binding="#{customServices.colSBar}"
                                          id="colSBar"/>

                            </h:dataTable>
                            </ui:panelGroup>

                            <ui:button action="#{customServices.btnRemove_action}"
                                       binding="#{customServices.btnRemove}"
                                       id="btnRemoveService"
                                       styleClass="servicesButton"
                                       style="left: 11px; top: 230px"
                                       text="Remove"/>
                        </ui:panelLayout>


                        <!-- Add Service Panel -->
                        <ui:panelLayout binding="#{customServices.pnlAddService}"
                                        id="pnlAddService"
                                        styleClass="servicesPanel"
                                        style="height: 240px; top: 273px">

                            <ui:staticText binding="#{customServices.staticText1}"
                                           id="staticText1"
                                           styleClass="pageSubheading"
                                           style="left: 12px; top: 12px"
                                           text="Add Service"/>

                            <ui:label binding="#{customServices.lblName}"
                                      for="txtName"
                                      id="lblName"
                                      style="left: 12px; top: 40px; position: absolute"
                                      text="Name:"/>                            

                            <ui:label binding="#{customServices.lblPassword}"
                                      for="txtPassword"
                                      id="lblPassword"
                                      style="left: 12px; top: 70px; position: absolute"
                                      text="Password:"/>

                            <ui:label binding="#{customServices.lblConfirmPassword}"
                                      for="txtConfirmPassword"
                                      id="lblConfirmPassword"
                                      style="left: 302px; top: 70px; position: absolute"
                                      text="Confirm Password:"/>

                            <ui:label binding="#{customServices.lblURL}"
                                      for="txtURL"
                                      id="lblURL"
                                      style="left: 12px; top: 100px; position: absolute"
                                      text="URI:"/>

                            <ui:label binding="#{customServices.lblDesc}"
                                      for="txtDescription"
                                      id="lblDesc"
                                      style="left: 12px; top: 130px; position: absolute"
                                      text="Description:"/>

                            <ui:textField binding="#{customServices.txtName}"
                                          id="txtName"
                                          style="left: 100px; top: 40px; width: 200px; position: absolute"
                                          onKeyPress="return disableEnterKey(event);"/>

                            <!--=====================================================-->

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

                            <ui:passwordField binding="#{customServices.txtPassword}"
                                          id="txtPassword"
                                          style="left: 100px; top: 70px; width: 170px; position: absolute"
                                          onKeyPress="return disableEnterKey(event);"/>

                            <ui:passwordField binding="#{customServices.txtConfirmPassword}"
                                          id="txtConfirmPassword"
                                          style="left: 430px; top: 70px; width: 170px; position: absolute"
                                          onKeyPress="return disableEnterKey(event);"/>

                            <ui:textField binding="#{customServices.txtURL}"
                                          id="txtURL"
                                          style="left: 100px; top: 100px; width: 500px; position: absolute"
                                          onKeyPress="return disableEnterKey(event);"/>

                            <ui:textArea binding="#{customServices.txtDescription}"
                                         id="txtDescription"
                                         style="height: 50px; left: 100px; top: 130px; width: 505px; position: absolute"/>

                            <ui:button action="#{customServices.btnAdd_action}"
                                       binding="#{customServices.btnAdd}"
                                       id="btnAddService"
                                       styleClass="servicesButton"
                                       style="left: 11px; top: 198px"
                                       text="Add"/>

                            <ui:button action="#{customServices.btnClear_action}"
                                       binding="#{customServices.btnClear}"
                                       id="btnClearService"
                                       styleClass="servicesButton"
                                       style="left: 119px; top: 198px"
                                       text="Clear"/>

                        </ui:panelLayout>

                            <div><jsp:include page="pfMsgPanel.jspf"/></div>

                        </ui:panelLayout>
                       </center>

                        <ui:hiddenField binding="#{customServices.hdnRowIndex}" id="hdnRowIndex"/>

                        <div><jsp:include page="pfFooter.jspf"/></div>

                    </ui:form>

                    <ui:script>
                        addOnclickToDatatableRows();
                    </ui:script>

                </ui:body>
            </ui:html>
        </ui:page>
    </f:view>
</jsp:root>
