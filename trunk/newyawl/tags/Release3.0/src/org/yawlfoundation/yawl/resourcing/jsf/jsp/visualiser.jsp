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
       <ui:page binding="#{visualiser.page1}" id="page1">
          <ui:html binding="#{visualiser.html1}" id="html1">
             <ui:head binding="#{visualiser.head1}" id="head1"
                      title="YAWL Work Item Visualizer">

                 <ui:link binding="#{visualiser.link1}" id="link1"
                          url="/resources/stylesheet.css"/>

                 <ui:link binding="#{ApplicationBean.favIcon}" id="lnkFavIcon"
                          rel="shortcut icon"
                          type="image/x-icon" url="/resources/favicon.ico"/>

                 <ui:script binding="#{SessionBean.script}" id="script1"
                            url="/resources/script.js"/>
             </ui:head>

             <ui:body style=" -rave-layout: grid"
                      id="body1" binding="#{visualiser.body1}">

                 <ui:form binding="#{visualiser.form1}" id="form1">

                    <!-- include banner -->
                    <jsp:include page="pfHeader.jspf"/>

                    <center>

                        <h:outputText binding="#{visualiser.outputText}"
                                      escape="false" id="outputText1"
                                      value="#{visualiser.appletHtml}"/>


                        <ui:panelLayout binding="#{visualiser.pnlContainer}"
                                        id="pnlContainer"
                                        styleClass="visualiserContainerPanel">

                            <ui:button action="#{visualiser.btnReturn_action}"
                                       binding="#{visualiser.btnReturn}"
                                       id="btnReturn"
                                       primary="true"
                                       styleClass="visualiserButton"
                                       text="Return"/>

                            <ui:button action="#{visualiser.btnView_action}"
                                       binding="#{visualiser.btnView}"
                                       id="btnView"
                                       styleClass="visualiserButton"
                                       style="left:125px"
                                       text="View/Edit"
                                       visible="false"/>

                        </ui:panelLayout>

                        <ui:panelLayout binding="#{SessionBean.messagePanel}"
                                        id="msgPanel"
                                        panelLayout="flow"/>
 
                        <ui:hiddenField binding="#{visualiser.hdnSelectedItemID}"
                                        id="hdnSelectedItemID"/>

                    </center>
                 </ui:form>
              </ui:body>
           </ui:html>
        </ui:page>
    </f:view>
</jsp:root>
