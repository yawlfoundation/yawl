<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="1.2"
          xmlns:f="http://java.sun.com/jsf/core"
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
        <ui:page binding="#{addInstance.page1}" id="page1">
            <ui:html binding="#{addInstance.html1}" id="html1">
                <ui:head binding="#{addInstance.head1}" id="head1">
                    <ui:link binding="#{addInstance.link1}" id="link1"
                             url="/resources/stylesheet.css"/>

                    <ui:link binding="#{ApplicationBean.favIcon}" id="lnkFavIcon"
                             rel="shortcut icon"
                            type="image/x-icon" url="/resources/favicon.ico"/>

                </ui:head>
                <ui:body binding="#{addInstance.body1}" id="body1"
                         focus="form1:txtParamVal"
                         style="-rave-layout: grid">
                    <ui:form binding="#{addInstance.form1}" id="form1">

                        <!-- include banner -->
                        <jsp:include page="pfHeader.jspf"/>

                        <center>
                            <ui:panelLayout binding="#{addInstance.pnlContainer}"
                                            id="pnlContainer"
                                            styleClass="addInstanceContainerPanel">

                                <ui:staticText binding="#{addInstance.staticHeader}"
                                               id="staticHeader"
                                               text="#{SessionBean.addInstanceHeader}"
                                               styleClass="pageHeading"
                                               style="top: 0; left: 0"/>

                                <ui:panelLayout binding="#{addInstance.pnlAddInstance}"
                                                           id="pnlAddInstance"
                                                           styleClass="addInstancePanel">

                                <ui:staticText binding="#{addInstance.staticText1}"
                                               id="staticText1"
                                               text="Please enter a valid data value for the named parameter"
                                               styleClass="pageSubheading"
                                               style="left: 8px; top: 10px"/>

                                <ui:label binding="#{addInstance.lblParam}"
                                          for="txtParamVal"
                                          id="lblParam"
                                          styleClass="addInstanceParamLabel"
                                          text="#{SessionBean.addInstanceParamNameLabelText}"/>

                                <ui:textArea binding="#{addInstance.txtParamVal}"
                                             id="txtParamVal"
                                             styleClass="addInstanceParamValue"/>

                                <ui:button action="#{addInstance.btnOK_action}"
                                           binding="#{addInstance.btnOK}"
                                           id="btnOK"
                                           styleClass="selectUserButton"
                                           style="left: 160px; top: 250px"
                                           text="Create"/>

                                <ui:button action="#{addInstance.btnCancel_action}"
                                           binding="#{addInstance.btnCancel}"
                                           id="btnCancel"
                                           immediate="true"
                                           styleClass="selectUserButton"
                                           style="left: 70px; top: 250px"
                                           text="Cancel"/>
                                </ui:panelLayout>
                            </ui:panelLayout>
                        </center>
                        <div><jsp:include page="pfFooter.jspf"/></div>
                    </ui:form>
                </ui:body>
            </ui:html>
        </ui:page>
    </f:view>
</jsp:root>
