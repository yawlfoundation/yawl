<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="1.2" xmlns:f="http://java.sun.com/jsf/core" xmlns:h="http://java.sun.com/jsf/html" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:ui="http://www.sun.com/web/ui">
    <jsp:directive.page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"/>
    <f:view>
        <ui:page binding="#{selectUser.page1}" id="page1">
            <ui:html binding="#{selectUser.html1}" id="html1">
                <ui:head binding="#{selectUser.head1}" id="head1">
                    <ui:link binding="#{selectUser.link1}" id="link1"
                             url="/resources/stylesheet.css"/>
                </ui:head>
                <ui:body binding="#{selectUser.body1}" id="body1"
                         style="-rave-layout: grid">
                    <ui:form binding="#{selectUser.form1}" id="form1">

                        <ui:panelLayout binding="#{SessionBean.topPanel}"
                                        id="topPanel"
                                        panelLayout="flow"
                                        styleClass="topPanel"/>
                        <div style="left: 0px; top: 0px; position: absolute">
                             <jsp:directive.include file="pfHeader.jspf"/>
                        </div>  

                        <ui:listbox binding="#{selectUser.lbxUserList}"
                                    id="lbxUserList"
                                    items="#{SessionBean.selectUserListOptions}"
                                    selected="#{SessionBean.selectUserListChoice}"
                                    style="border: 2px solid blue; height: 150px; left: 102px; top: 180px; position: absolute; width: 210px"/>

                        <ui:staticText binding="#{selectUser.staticText1}"
                                       id="staticText1"
                                       text="#{SessionBean.userListFormHeaderText}"
                                       styleClass="pageSubheading"
                                       style="left: 102px; top: 150px"/>

                        <ui:button action="#{selectUser.btnOK_action}"
                                   binding="#{selectUser.btnOK}"
                                   id="btnOK"
                                   styleClass="selectUserButton"
                                   style="left: 215px"
                                   text="OK"/>

                        <ui:button action="#{selectUser.btnCancel_action}"
                                   binding="#{selectUser.btnCancel}"
                                   id="btnCancel"
                                   styleClass="selectUserButton"
                                   style="left: 125px"
                                   text="Cancel"/>
                    </ui:form>
                </ui:body>
            </ui:html>
        </ui:page>
    </f:view>
</jsp:root>
