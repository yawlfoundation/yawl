<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="1.2"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:jsp="http://java.sun.com/JSP/Page"
          xmlns:ui="http://www.sun.com/web/ui">
    
    <jsp:directive.page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"/>

    <f:view>
        <ui:page binding="#{msLogin.page}" id="page1">
            <ui:html binding="#{msLogin.html}" id="html1">
                <ui:head binding="#{msLogin.head}" id="head1"
                         title="Welcome to YAWL 2.0: Please Login">
                    <ui:link binding="#{msLogin.link}" id="link1"
                             url="/resources/stylesheet.css"/>
                    <ui:link binding="#{ApplicationBean.favIcon}" id="lnkFavIcon"
                             rel="shortcut icon"
                            type="image/x-icon" url="/resources/favicon.ico"/>
                </ui:head>

                <ui:body binding="#{msLogin.body}" id="body1"
                         focus="form1:txtUserName"
                         style="-rave-layout: grid">
                    <br/>
                    <ui:form binding="#{msLogin.form}" id="form1">

                        <!-- include banner -->
                        <div><jsp:directive.include file="pfHeader.jspf"/></div>

                        <center>

                            <ui:panelLayout binding="#{msLogin.pnlContainer}"
                                            id="pnlContainer"
                                            styleClass="loginPanel">

                                <ui:label binding="#{msLogin.lblUserName}"
                                          for="txtUserName"
                                          id="lblUserName"
                                          style="top: 25px"
                                          styleClass="loginLabel"
                                          text="User Name:"/>

                                <ui:textField binding="#{msLogin.txtUserName}"
                                              id="txtUserName"
                                              style="top: 22px"
                                              styleClass="loginField"/>

                                <ui:label binding="#{msLogin.lblPassword}"
                                          for="txtPassword"
                                          style="top: 55px"
                                          styleClass="loginLabel"
                                          id="lblPassword"
                                          text="Password:"/>

                                <ui:passwordField binding="#{msLogin.txtPassword}"
                                                  id="txtPassword"
                                                  style="top: 52px"
                                                  styleClass="loginField"/>

                                <ui:button action="#{msLogin.btnLogin_action}"
                                           binding="#{msLogin.btnLogin}"
                                           id="btnLogin"
                                           primary="true"
                                           styleClass="loginButton"
                                           text="Login"/>

                            </ui:panelLayout>

                            <ui:panelLayout binding="#{SessionBean.messagePanel}"
                                            id="msgPanel"
                                            panelLayout="flow"/>
                        </center>
                    </ui:form>
                </ui:body>
            </ui:html>
        </ui:page>
    </f:view>
</jsp:root>
