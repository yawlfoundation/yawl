<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="1.2"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:jsp="http://java.sun.com/JSP/Page"
          xmlns:ui="http://www.sun.com/web/ui">

    <jsp:directive.page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"/>

    <f:view>
        <ui:page binding="#{Login.page1}" id="page1">
            <ui:html binding="#{Login.html1}" id="html1">
                <ui:head binding="#{Login.head1}" id="head1">
                    <ui:link binding="#{Login.link1}" id="link1"
                             url="/resources/stylesheet.css"/>
                    <ui:link binding="#{ApplicationBean.favIcon}" id="lnkFavIcon"
                             rel="shortcut icon"
                            type="image/x-icon" url="/resources/favicon.ico"/>
                </ui:head>

                <ui:body binding="#{Login.body1}" id="body1" style="-rave-layout: grid">
                    <br/>
                    <ui:form binding="#{Login.form1}" id="form1">

                        <ui:panelLayout binding="#{SessionBean.topPanel}"
                                        id="topPanel"
                                        panelLayout="flow"
                                        styleClass="topPanel"/>
                        <div style="left: 0px; top: 0px; position: absolute">
                             <jsp:directive.include file="pfHeader.jspf"/>
                        </div>

                        <ui:panelGroup binding="#{Login.groupPanel1}"
                                       id="groupPanel1"
                                       styleClass="loginPanel">

                            <h:panelGrid binding="#{Login.gridPanel1}"
                                         columns="2"
                                         id="gridPanel1"
                                         style="height: 48px" width="240">

                                <ui:label binding="#{Login.label2}"
                                          for="txtUserName"
                                          id="label2"
                                          text="User Name:"/>

                                <ui:textField binding="#{Login.txtUserName}"
                                              id="txtUserName"
                                              style="width: 120px"
                                              validator="#{Login.valLenUserName.validate}"/>
                            </h:panelGrid>

                            <h:panelGrid binding="#{Login.gridPanel2}"
                                         columns="2" id="gridPanel2"
                                         style="height: 48px" width="240">

                                <ui:label binding="#{Login.label1}"
                                          for="txtPassword"
                                          id="label1"
                                          text="Password:   "/>

                                <ui:passwordField binding="#{Login.txtPassword}"
                                                  id="txtPassword"
                                                  style="width: 120px"
                                                  validator="#{Login.valLenPassword.validate}"/>
                            </h:panelGrid>
                        </ui:panelGroup>

                        <ui:button action="#{Login.btnLogin_action}"
                                   binding="#{Login.btnLogin}"
                                   id="btnLogin"
                                   primary="true"
                                   styleClass="loginButton"
                                   text="Login"/>

                        <ui:message binding="#{Login.message1}"
                                    for="txtUserName"
                                    id="message1"
                                    showDetail="false"
                                    showSummary="true"
                                    style="left: 552px; top: 144px; position: absolute"/>

                        <ui:message binding="#{Login.message2}"
                                    for="txtPassword"
                                    id="message2"
                                    showDetail="false"
                                    showSummary="true"
                                    style="left: 552px; top: 192px; position: absolute"/>

                    </ui:form>
                </ui:body>
            </ui:html>
        </ui:page>
    </f:view>
</jsp:root>
