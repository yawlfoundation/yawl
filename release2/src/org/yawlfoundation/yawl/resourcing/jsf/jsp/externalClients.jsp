<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="1.2"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:jsp="http://java.sun.com/JSP/Page"
          xmlns:ui="http://www.sun.com/web/ui">

<!--
  ~ Copyright (c) 2004-2010 The YAWL Foundation. All rights reserved.
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
        <ui:page binding="#{externalClients.page1}" id="page1">
            <ui:html binding="#{externalClients.html1}" id="html1">
                <ui:head binding="#{externalClients.head1}" id="head1"
                         title="YAWL 2.1 External Client Management">
                    <ui:link binding="#{externalClients.link1}" id="link1"
                             url="/resources/stylesheet.css"/>

                    <ui:link binding="#{ApplicationBean.favIcon}" id="lnkFavIcon"
                             rel="shortcut icon"
                            type="image/x-icon" url="/resources/favicon.ico"/>
                    
                    <ui:script binding="#{SessionBean.script}" id="script1"
                               url="/resources/script.js"/>

                </ui:head>
                <ui:body binding="#{externalClients.body1}" id="body1"
                         style="-rave-layout: grid"
                         focus="form1:txtName">

                    <ui:form binding="#{externalClients.form1}" id="form1">

                        <!-- include banner -->
                        <jsp:include page="pfHeader.jspf"/>

                        <div style="top: 20px; position: relative">
                            <jsp:directive.include file="pfMenubar.jspf"/>
                        </div>
                        <center>
                        <ui:panelLayout binding="#{externalClients.pnlContainer}"
                                        id="pnlContainer"
                                        styleClass="externalClientsContainerPanel">

                        <!-- Registered Services Panel -->
                        <ui:panelLayout binding="#{externalClients.pnlServices}"
                                        id="pnlServices"
                                        styleClass="servicesPanel"
                                        style="position: absolute; height: 270px; top: 0px">

                            <ui:staticText binding="#{externalClients.staticText2}"
                                           id="staticText2"
                                           styleClass="pageSubheading"
                                           style="left: 12px; top: 12px"
                                           text="Registered Client Application Accounts"/>

                            <ui:panelGroup binding="#{externalClients.pnlGroup}"
                                            id="cspnlGroup"
                                            style="position: absolute; width: 635px"
                                            styleClass="tablePnlGroup">

                            <h:dataTable binding="#{externalClients.dataTable1}"
                                         headerClass="dataTableHeader"
                                         id="dataTable1"
                                         columnClasses="externalClientNameCol,
                                                        externalClientDescCol,
                                                        dataTableScrollBarCol"
                                         cellpadding="3"
                                         styleClass="dataTable"
                                         value="#{ApplicationBean.externalClients}"
                                         var="currentRow"
                                         width="638">

                                <h:column binding="#{externalClients.colName}"
                                          id="colName">

                                    <h:outputText binding="#{externalClients.colNameRows}"
                                                  id="colNameRows"
                                                  value="#{currentRow._userid}"/>

                                </h:column>

                                <h:column binding="#{externalClients.colDescription}"
                                          id="colDescription">
                                    <h:outputText binding="#{externalClients.colDescriptionRows}"
                                                  id="colDescriptionRows"
                                                  value="#{currentRow._documentation}"/>

                                </h:column>

                                <h:column binding="#{externalClients.colSBar}"
                                          id="colSBar"/>

                            </h:dataTable>
                            </ui:panelGroup>

                            <ui:button action="#{externalClients.btnEdit_action}"
                                       binding="#{externalClients.btnEdit}"
                                       id="btnEdit"
                                       styleClass="servicesButton"
                                       style="left: 11px; top: 230px"
                                       text="Edit"/>

                            <ui:button action="#{externalClients.btnRemove_action}"
                                       binding="#{externalClients.btnRemove}"
                                       id="btnRemove"
                                       styleClass="servicesButton"
                                       style="left: 119px; top: 230px"
                                       text="Remove"/>

                        </ui:panelLayout>


                        <!-- Add/Edit Account Panel -->
                        <ui:panelLayout binding="#{externalClients.pnlAddService}"
                                        id="pnlAddService"
                                        styleClass="servicesPanel"
                                        style="height: 210px; top: 273px">

                            <ui:staticText binding="#{externalClients.staticText1}"
                                           id="staticText1"
                                           styleClass="pageSubheading"
                                           style="left: 12px; top: 12px"
                                           text="#{externalClients.addPanelHeading}"/>

                            <ui:label binding="#{externalClients.lblName}"
                                      for="txtName"
                                      id="lblName"
                                      style="left: 12px; top: 40px; position: absolute"
                                      text="Name:"/>                            

                            <ui:label binding="#{externalClients.lblPassword}"
                                      for="txtPassword"
                                      id="lblPassword"
                                      style="left: 12px; top: 70px; position: absolute"
                                      text="Password:"/>

                            <ui:label binding="#{externalClients.lblConfirmPassword}"
                                      for="txtConfirmPassword"
                                      id="lblConfirmPassword"
                                      style="left: 302px; top: 70px; position: absolute"
                                      text="Confirm Password:"/>

                            <ui:label binding="#{externalClients.lblDesc}"
                                      for="txtDescription"
                                      id="lblDesc"
                                      style="left: 12px; top: 100px; position: absolute"
                                      text="Description:"/>


                            <ui:textField binding="#{externalClients.txtName}"
                                          id="txtName"
                                          style="left: 100px; top: 40px; width: 200px; position: absolute"/>
 
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
                            
                            <ui:passwordField binding="#{externalClients.txtPassword}"
                                          id="txtPassword"
                                          style="left: 100px; top: 70px; width: 170px; position: absolute"/>

                            <ui:passwordField binding="#{externalClients.txtConfirmPassword}"
                                           id="txtConfirmPassword"
                                           style="left: 430px; top: 70px; width: 170px; position: absolute"/>

                            <ui:textArea binding="#{externalClients.txtDescription}"
                                         id="txtDescription"
                                         style="height: 50px; left: 100px; top: 100px; width: 505px; position: absolute"/>

                            <ui:button action="#{externalClients.btnAdd_action}"
                                       binding="#{externalClients.btnAdd}"
                                       id="btnAddService"
                                       styleClass="servicesButton"
                                       style="left: 11px; top: 168px"
                                       text="#{externalClients.btnAddText}"/>

                            <ui:button action="#{externalClients.btnClear_action}"
                                       binding="#{externalClients.btnClear}"
                                       id="btnClearService"
                                       styleClass="servicesButton"
                                       style="left: 119px; top: 168px"
                                       text="Clear"/>

                        </ui:panelLayout>

                           <div><jsp:include page="pfMsgPanel.jspf"/></div>

                        </ui:panelLayout>


                        </center>

                        <ui:hiddenField binding="#{externalClients.hdnRowIndex}" id="hdnRowIndex"/>

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
