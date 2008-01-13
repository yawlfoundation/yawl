<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="1.2" xmlns:f="http://java.sun.com/jsf/core" xmlns:h="http://java.sun.com/jsf/html" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:ui="http://www.sun.com/web/ui">
    <jsp:directive.page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"/>
    <f:view>
        <ui:page binding="#{dynForm.page1}" id="page1">
            <ui:html binding="#{dynForm.html1}" id="html1">
                <ui:head binding="#{dynForm.head1}" id="head1">
                    <ui:link binding="#{dynForm.link1}" id="link1" url="/resources/stylesheet.css"/>
                </ui:head>
                <ui:body binding="#{dynForm.body1}" id="body1" style="-rave-layout: grid">
                    <ui:form binding="#{dynForm.form1}" id="form1">

                        <div style="left: 204px; top: 30px; position: absolute">
                            <jsp:directive.include file="pfHeader.jspf"/>
                        </div>
                        
                        <ui:staticText binding="#{dynForm.txtHeader}"
                                       id="txtHeader"
                                       text="#{SessionBean.dynFormHeaderText}" 
                                       styleClass="pageHeading"/>

                        <ui:panelLayout binding="#{dynForm.compPanel}"
                                        id="compPanel"
                                        panelLayout="flow"
                                        styleClass="dynformPanel"/>

                        <ui:button action="#{dynForm.btnOK_action}"
                                   binding="#{dynForm.btnOK}"
                                   id="btnOK"
                                   text="OK"
                                   styleClass="dynformButton"/>

                        <ui:button action="#{dynForm.btnCancel_action}"
                                   binding="#{dynForm.btnCancel}"
                                   id="btnCancel"
                                   text="Cancel"
                                   styleClass="dynformButton"/>
                    </ui:form>
                </ui:body>
            </ui:html>
        </ui:page>
    </f:view>
</jsp:root>