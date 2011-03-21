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
        <ui:page binding="#{rssFormViewer.page}" id="page1">
            <ui:html binding="#{rssFormViewer.html}" id="html1">
                <ui:head binding="#{rssFormViewer.head}" id="head1"
                         title="YAWL 2.2: RSS Form Handler">
                    <ui:link binding="#{rssFormViewer.link}" id="link1"
                             url="/resources/stylesheet.css"/>
                    <ui:link binding="#{ApplicationBean.favIcon}" id="lnkFavIcon"
                             rel="shortcut icon"
                            type="image/x-icon" url="/resources/favicon.ico"/>
                </ui:head>

                <ui:body binding="#{rssFormViewer.body}" id="body1"
                         focus="form1:btnClose"
                         style="-rave-layout: grid">
                    <br/>
                    <ui:form binding="#{rssFormViewer.form}" id="form1">

                        <!-- include banner -->
                        <div><jsp:directive.include file="pfRSSHeader.jspf"/></div>

                        <center>

                            <ui:panelLayout binding="#{rssFormViewer.pnlContainer}"
                                            id="pnlContainer"
                                            styleClass="rssFormViewerPanel">

                                <ui:staticText binding="#{rssFormViewer.staticText1}"
                                               id="staticText1"
                                               styleClass="rssMessage"
                                               style="top: 12px"/>

                                <ui:button action="#{rssFormViewer.btnClose_action}"
                                           binding="#{rssFormViewer.btnClose}"
                                           id="btnClose"
                                           primary="true"
                                           styleClass="rssCloseButton"
                                           onClick="return window.close();"
                                           text="Close Window"/>

                            </ui:panelLayout>

                        </center>
                    </ui:form>
                </ui:body>
            </ui:html>
        </ui:page>
    </f:view>
</jsp:root>
