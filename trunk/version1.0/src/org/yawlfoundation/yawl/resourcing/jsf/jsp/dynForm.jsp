<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="1.2"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:jsp="http://java.sun.com/JSP/Page"
          xmlns:ui="http://www.sun.com/web/ui">

    <jsp:directive.page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"/>
    <f:view>
        <ui:page binding="#{dynForm.page1}" id="page1">
            <ui:html binding="#{dynForm.html1}" id="html1">
                <ui:head binding="#{dynForm.head1}" id="head1"
                         title="#{DynFormFactory.title}">
                    <ui:link binding="#{dynForm.link1}" id="link1"
                             url="/resources/stylesheet.css"/>

                    <ui:link binding="#{ApplicationBean.favIcon}" id="lnkFavIcon"
                             rel="shortcut icon"
                            type="image/x-icon" url="/resources/favicon.ico"/>
                    
                </ui:head>
                <ui:body binding="#{dynForm.body1}" id="body1"
                         focus="#{DynFormFactory.focus}"
                         style="-rave-layout: grid">
                    
                    <ui:form binding="#{dynForm.form1}" id="form1">

                        <!-- include banner -->
                        <div><jsp:directive.include file="pfHeader.jspf"/></div>
                        
                        <ui:staticText binding="#{dynForm.txtHeader}"
                                       id="txtHeader"
                                       text="#{DynFormFactory.headerText}"
                                       styleClass="pageHeading"/>

                        <ui:panelLayout binding="#{DynFormFactory.compPanel}"
                                        id="compPanel"
                                        styleClass="dynformOuterPanel"/>

                        <ui:button action="#{dynForm.btnOK_action}"
                                   binding="#{dynForm.btnOK}"
                                   id="btnOK"
                                   styleClass="dynformButton"
                                   style="#{DynFormFactory.btnOKStyle}"/>

                        <ui:button action="#{dynForm.btnCancel_action}"
                                   binding="#{dynForm.btnCancel}"
                                   id="btnCancel"
                                   text="Cancel"
                                   styleClass="dynformButton"
                                   style="#{DynFormFactory.btnCancelStyle}"/>

                        <ui:panelLayout binding="#{SessionBean.messagePanel}"
                                        id="msgPanel"
                                        panelLayout="flow"
                                        style="top: 70px; left: 500px; position: absolute"/>

                    </ui:form>
                </ui:body>
            </ui:html>
        </ui:page>
    </f:view>
</jsp:root>