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
        <ui:page binding="#{nonHumanMgt.page1}" id="page1">
            <ui:html binding="#{nonHumanMgt.html1}" id="html1">
                <ui:head binding="#{nonHumanMgt.head1}" id="head1"
                         title="YAWL 2.2: Non Human Resource Management">

                    <ui:link binding="#{nonHumanMgt.link1}" id="link1"
                             url="/resources/stylesheet.css"/>

                    <ui:link binding="#{ApplicationBean.favIcon}" id="lnkFavIcon"
                             rel="shortcut icon"
                            type="image/x-icon" url="/resources/favicon.ico"/>

                </ui:head>
                <ui:body binding="#{nonHumanMgt.body1}" id="body1"
                         style="-rave-layout: grid">
                    <ui:form binding="#{nonHumanMgt.form1}" id="form1">

                    <!-- include banner -->
                    <div><jsp:directive.include file="pfHeader.jspf"/></div>

                        <div style="top: 20px; position: relative">
                            <jsp:directive.include file="pfMenubar.jspf"/>
                        </div>
                        <center>
                        <ui:panelLayout binding="#{nonHumanMgt.pnlContainer}"
                                        id="pnlContainer"
                                        style="#{SessionBean.outerPanelTop}"
                                        styleClass="orgDataMgtContainerPanel">

                        <ui:tabSet binding="#{nonHumanMgt.tabSet}"
                                   id="tabSet"
                                   styleClass="orgDataTabSet">

                            <ui:tab action="#{nonHumanMgt.tabResources_action}"
                                    binding="#{nonHumanMgt.tabResources}"
                                    id="tabResources"
                                    style="#{SessionBean.initTabStyle}"
                                    text="Resources">

                                <ui:panelLayout binding="#{nonHumanMgt.lpResources}"
                                                id="lpResources"
                                                styleClass="orgDataTabPanel">

                                </ui:panelLayout>
                            </ui:tab>

                            <ui:tab action="#{nonHumanMgt.tabCategories_action}"
                                    binding="#{nonHumanMgt.tabCategories}"
                                    id="tabCategories"
                                    text="Categories">

                                <ui:panelLayout binding="#{nonHumanMgt.lpCategories}"
                                                id="lpCategories"
                                                styleClass="orgDataTabPanel">

                                </ui:panelLayout>
                            </ui:tab>
                        </ui:tabSet>

                        <ui:button binding="#{SessionBean.btnRefresh}"
                                   action="#{nonHumanMgt.btnRefresh_action}"
                                   id="btnRefresh"
                                   imageURL="/resources/refresh.png"
                                   styleClass="refreshOrgDataButton"
                                   toolTip="Refresh"
                                   text=""/>

                        <div style="position: absolute;">
                            <jsp:directive.include file="pfNHResources.jspf"/>
                        </div>

                            <div><jsp:include page="pfMsgPanel.jspf"/></div>
                        </ui:panelLayout>

                        </center>

                        <div><jsp:include page="pfFooter.jspf"/></div>

                        <ui:meta binding="#{nonHumanMgt.metaRefresh}"
                                 httpEquiv="refresh"
                                 id="metaRefresh" />

                    </ui:form>
                </ui:body>
            </ui:html>
        </ui:page>
    </f:view>
</jsp:root>
