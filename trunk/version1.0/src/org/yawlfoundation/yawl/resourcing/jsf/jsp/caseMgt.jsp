<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="1.2" xmlns:f="http://java.sun.com/jsf/core" xmlns:h="http://java.sun.com/jsf/html" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:ui="http://www.sun.com/web/ui">
    <jsp:directive.page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"/>
    <f:view>
        <ui:page binding="#{caseMgt.page1}" id="page1">
            <ui:html binding="#{caseMgt.html1}" id="html1">
                <ui:head binding="#{caseMgt.head1}" id="head1">
                    <ui:link binding="#{caseMgt.link1}" id="link1" url="/resources/stylesheet.css"/>
                </ui:head>
                <ui:body binding="#{caseMgt.body1}" id="body1" style="-rave-layout: grid">
                    <ui:form binding="#{caseMgt.form1}" id="form1">
                        <div style="left: 240px; top: 36px; position: absolute">
                            <jsp:directive.include file="pfHeader.jspf"/>
                        </div>
                        <div style="left: 12px; top: 138px; position: absolute">
                            <jsp:directive.include file="pfMenu.jspf"/>
                        </div>

                        <ui:panelLayout binding="#{caseMgt.layoutPanel1}" id="layoutPanel1" panelLayout="flow" style="border: 2px solid gray; height: 120px; left: 186px; top: 138px; position: absolute; width: 600px">
                            <ui:staticText binding="#{caseMgt.staticText1}" id="staticText1"
                                style="color: blue; font-size: 14px; font-weight: bold; left: 12px; top: 6px; position: absolute" text="Upload Specification:"/>
                            <ui:upload binding="#{caseMgt.fileUpload1}" columns="70" id="fileUpload1"
                                style="border-width: 1px; border-style: solid; border-color: rgb(0, 0, 0) rgb(0, 0, 0) rgb(0, 0, 0) rgb(0, 0, 0); margin: 2px; padding: 4px; left: 10px; top: 40px; position: absolute" valueChangeListener="#{caseMgt.fileUpload1_processValueChange}"/>
                            <ui:button action="#{caseMgt.btnUpload_action}" binding="#{caseMgt.btnUpload}" id="btnUpload"
                                style="height: 30px; left: 11px; top: 81px; position: absolute; width: 90px" styleClass="" text="Upload File"/>
                        </ui:panelLayout>

                        <ui:panelLayout binding="#{caseMgt.layoutPanel2}" id="layoutPanel2" panelLayout="flow" style="border: 2px solid gray; height: 194px; left: 186px; top: 264px; position: absolute; width: 600px">
                            <ui:button action="#{caseMgt.btnLaunch_action}" binding="#{caseMgt.btnLaunch}" id="btnLaunch"
                                style="height: 30px; left: 11px; top: 156px; position: absolute" text="Launch Case"/>
                            <ui:staticText binding="#{caseMgt.staticText2}" id="staticText2"
                                style="color: blue; font-size: 14px; font-weight: bold; left: 12px; top: 12px; position: absolute" text="Loaded Specifications:"/>
                            <ui:listbox binding="#{caseMgt.lbxLoadedSpecs}" id="lbxLoadedSpec1" items="#{SessionBean.loadedSpecListOptions}"
                                rows="8" selected="#{SessionBean.loadedSpecListChoice}" style="left: 12px; top: 36px; position: absolute; width: 570px"/>
                            <ui:button action="#{caseMgt.btnUnload_action}" binding="#{caseMgt.btnUnload}" id="btnUnload"
                                style="height: 30px; left: 119px; top: 156px; position: absolute" text="Unload Spec"/>
                        </ui:panelLayout>

                        <ui:panelLayout binding="#{caseMgt.layoutPanel3}" id="layoutPanel3" panelLayout="flow" style="border: 2px solid gray; height: 194px; left: 186px; top: 468px; position: absolute; width: 600px">
                            <ui:button action="#{caseMgt.btnCancelCase_action}" binding="#{caseMgt.btnCancelCase}" id="btnCancelCase"
                                style="height: 30px; left: 11px; top: 156px; position: absolute; width: 95px" text="Cancel Case"/>
                            <ui:listbox binding="#{caseMgt.lbxRunningCases}" id="lbxRunningCases" items="#{SessionBean.runningCaseListOptions}"
                                rows="8" selected="#{SessionBean.runningCaseListChoice}" style="left: 12px; top: 30px; position: absolute; width: 570px"/>
                            <ui:staticText binding="#{caseMgt.staticText3}" id="staticText3"
                                style="color: blue; font-size: 14px; font-weight: bold; left: 12px; top: 6px; position: absolute" text="Running Cases:"/>
                        </ui:panelLayout>

                        <ui:messageGroup binding="#{caseMgt.msgBox}" id="msgBox" showGlobalOnly="true" style="left: 198px; top: 690px; position: absolute; width: 568px"/>
                    </ui:form>
                </ui:body>
            </ui:html>
        </ui:page>
    </f:view>
</jsp:root>
