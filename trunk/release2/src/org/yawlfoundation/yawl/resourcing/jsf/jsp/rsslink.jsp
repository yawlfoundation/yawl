<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="1.2"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:jsp="http://java.sun.com/JSP/Page"
          xmlns:ui="http://www.sun.com/web/ui">
    
    <jsp:directive.page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"/>

    <f:view>
        <ui:page binding="#{rsslink.page}" id="page1">
            <ui:html binding="#{rsslink.html}" id="html1">
                <ui:head binding="#{rsslink.head}" id="head1"
                         title="Welcome to YAWL 2.0: Please Login">
                    <ui:link binding="#{rsslink.link}" id="link1"
                             url="/resources/stylesheet.css"/>
                    <ui:link binding="#{ApplicationBean.favIcon}" id="lnkFavIcon"
                             rel="shortcut icon"
                            type="image/x-icon" url="/resources/favicon.ico"/>
                </ui:head>

                <ui:body binding="#{rsslink.body}" id="body1"
                         focus="form1:txtUserName"
                         style="-rave-layout: grid">
                    <br/>
                    <ui:form binding="#{rsslink.form}" id="form1">

                        <!-- include banner -->
                        <div><jsp:directive.include file="pfHeader.jspf"/></div>

                        <center>

                            <ui:panelLayout binding="#{rsslink.pnlContainer}"
                                            id="pnlContainer"
                                            styleClass="rssLinkPanel">

                                <ui:staticText binding="#{rsslink.staticText1}"
                                               id="staticText1"
                                               styleClass="pageSubheading"
                                               style="left: 12px; top: 12px"/>

                                <ui:button action="#{rsslink.btnClose_action}"
                                           binding="#{rsslink.btnClose}"
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
