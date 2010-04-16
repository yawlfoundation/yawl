<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="1.2" xmlns:f="http://java.sun.com/jsf/core" xmlns:h="http://java.sun.com/jsf/html" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:ui="http://www.sun.com/web/ui">
    <jsp:directive.page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"/>
    <f:view>
        <ui:page binding="#{selectUser.page1}" id="page1">
            <ui:html binding="#{selectUser.html1}" id="html1">
                <ui:head binding="#{selectUser.head1}" id="head1">
                    <ui:link binding="#{selectUser.link1}" id="link1"
                             url="/resources/stylesheet.css"/>

                    <ui:link binding="#{ApplicationBean.favIcon}" id="lnkFavIcon"
                             rel="shortcut icon"
                            type="image/x-icon" url="/resources/favicon.ico"/>

                </ui:head>                               
                <ui:body binding="#{selectUser.body1}" id="body1"
                         style="-rave-layout: grid">
                    <ui:form binding="#{selectUser.form1}" id="form1">

                        <!-- include banner -->
                        <div><jsp:directive.include file="pfHeader.jspf"/></div>

                        <center>
                            <ui:panelLayout binding="#{selectUser.pnlContainer}"
                                            id="pnlContainer"
                                            styleClass="selectUserContainerPanel">

                        <ui:listbox binding="#{SessionBean.lbxUserList}"
                                    id="lbxUserList"
                                    items="#{SessionBean.selectUserListOptions}"
                                    
                                    styleClass="selectUserListbox"/>

                        <ui:staticText binding="#{selectUser.staticText1}"
                                       id="staticText1"
                                       text="#{SessionBean.userListFormHeaderText}"
                                       styleClass="pageSubheading"
                                       style="left: 15px; top: 10px"/>

                        <ui:button action="#{selectUser.btnOK_action}"
                                   binding="#{selectUser.btnOK}"
                                   id="btnOK"
                                   styleClass="selectUserButton"
                                   style="left: 148px"
                                   text="OK"/>

                        <ui:button action="#{selectUser.btnCancel_action}"
                                   binding="#{selectUser.btnCancel}"
                                   id="btnCancel"
                                   immediate="true"
                                   styleClass="selectUserButton"
                                   style="left: 58px"
                                   text="Cancel"/>

                              </ui:panelLayout>  <!-- pnlContainer -->
                            </center>
                    </ui:form>
                </ui:body>
            </ui:html>
        </ui:page>
    </f:view>
</jsp:root>
