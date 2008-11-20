<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="1.2"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:jsp="http://java.sun.com/JSP/Page"
          xmlns:ui="http://www.sun.com/web/ui">
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

                                <ui:staticText binding="#{addInstance.staticText1}"
                                               id="staticText1"
                                               text="Please enter a unique value for the parameter below"
                                               styleClass="pageSubheading"
                                               style="left: 0; top: 50px"/>

                                <ui:label binding="#{addInstance.lblParam}"
                                          for="txtParamVal"
                                          id="lblParam"
                                          style="left: 0; top: 100px; position: absolute;"
                                          text="#{SessionBean.addInstanceParamName}"/>

                                <ui:textArea binding="#{addInstance.txtParamVal}"
                                             id="txtParamVal"
                                             style="left: 0; height: 150px; top: 120px; position: absolute; width: 300px"/>

                                <ui:button action="#{addInstance.btnOK_action}"
                                           binding="#{addInstance.btnOK}"
                                           id="btnOK"
                                           styleClass="selectUserButton"
                                           style="left: 177px; top: 300px"
                                           text="Create"/>

                                <ui:button action="#{addInstance.btnCancel_action}"
                                           binding="#{addInstance.btnCancel}"
                                           id="btnCancel"
                                           immediate="true"
                                           styleClass="selectUserButton"
                                           style="left: 53px; top: 300px"
                                           text="Cancel"/>
                            </ui:panelLayout>
                        </center>
                    </ui:form>
                </ui:body>
            </ui:html>
        </ui:page>
    </f:view>
</jsp:root>
