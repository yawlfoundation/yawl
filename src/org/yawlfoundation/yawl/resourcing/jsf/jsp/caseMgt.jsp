<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="1.2" xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:jsp="http://java.sun.com/JSP/Page"
          xmlns:ui="http://www.sun.com/web/ui">

<!--
  ~ Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
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
        <ui:page binding="#{caseMgt.page1}" id="page1">
            <ui:html binding="#{caseMgt.html1}" id="html1">
                <ui:head binding="#{caseMgt.head1}" id="head1"
                         title="YAWL #{ApplicationBean.yawlVersion} Case Management">

                    <ui:link binding="#{caseMgt.link1}" id="link1"
                             url="/resources/stylesheet.css"/>

                    <ui:link binding="#{ApplicationBean.favIcon}" id="lnkFavIcon"
                             rel="shortcut icon"
                            type="image/x-icon" url="/resources/favicon.ico"/>

                    <ui:script binding="#{SessionBean.script}" id="script1"
                               url="/resources/script.js"/>

                </ui:head>
                <ui:body binding="#{caseMgt.body1}" id="body1"
                         style="-rave-layout: grid">
                    <ui:form binding="#{caseMgt.form1}" id="form1">

                        <!-- include banner -->
                        <div><jsp:directive.include file="pfHeader.jspf"/></div>

                        <div style="top: 20px; position: relative">
                            <jsp:directive.include file="pfMenubar.jspf"/>
                        </div>
                        <center>

                        <ui:panelLayout binding="#{caseMgt.pnlContainer}"
                                        id="pnlContainer"
                                        style="#{SessionBean.outerPanelTop}"
                                        styleClass="caseMgtContainerPanel">

                        <!-- Upload Panel -->
                        <ui:panelLayout binding="#{caseMgt.layoutPanel1}"
                                        id="layoutPanel1"
                                        styleClass="caseMgtPanel"
                                        style="position: absolute; height: 120px">

                            <ui:staticText binding="#{caseMgt.staticText1}"
                                           id="staticText1"
                                           styleClass="pageSubheading"
                                           style="left: 12px; top: 12px"
                                           text="Upload Specification"/>

                            <ui:upload binding="#{caseMgt.fileUpload1}"
                                       columns="60" id="fileUpload1"
                                       styleClass="fileUpload"
                                       immediate="true"
                                       style="left: 10px; top: 40px"
                                       valueChangeListener="#{caseMgt.fileUpload1_processValueChange}"/>

                            <ui:button action="#{caseMgt.btnUpload_action}"
                                       binding="#{caseMgt.btnUpload}"
                                       id="btnUpload"
                                       style="left: 11px; top: 81px" 
                                       styleClass="caseMgtButton"
                                       text="Upload File"/>
                        </ui:panelLayout>

                        <!-- Loaded Specs Panel -->
                        <ui:panelLayout binding="#{caseMgt.layoutPanel2}"
                                        id="layoutPanel2"
                                        styleClass="caseMgtPanel"
                                        style="position: absolute; height: 270px; top: 123px">

                            <ui:button action="#{caseMgt.btnLaunch_action}"
                                       binding="#{caseMgt.btnLaunch}"
                                       id="btnLaunch"
                                       styleClass="caseMgtButton"
                                       style="left: 11px; top: 230px"
                                       text="Launch Case"/>

                            <ui:staticText binding="#{caseMgt.staticText2}"
                                           id="staticText2"
                                           styleClass="pageSubheading"
                                           style="left: 12px; top: 12px"
                                           text="#{SessionBean.loadedSpecsCaption}"/>

                            <ui:panelGroup binding="#{caseMgt.pnlGroup}"
                                            id="pnlGroup"
                                            style="position: absolute"
                                            styleClass="tablePnlGroup">

                            <h:dataTable binding="#{caseMgt.dataTable1}"
                                         headerClass="dataTableHeader"
                                         id="dataTable1"
                                         cellpadding="3"
                                         styleClass="dataTable"
                                         columnClasses="specsNameCol,
                                                        specsVersCol,
                                                        specsDescCol,
                                                        dataTableScrollBarCol"                                    
                                         value="#{SessionBean.loadedSpecs}"
                                         var="currentRow"
                                         width="570">

                                <h:column binding="#{caseMgt.colName}"
                                          id="colName">

                                    <h:outputText binding="#{caseMgt.colNameRows}"
                                                  id="colNameRows"
                                                  value="#{currentRow.specURI}"/>

                                </h:column>

                                <h:column binding="#{caseMgt.colVersion}"
                                          id="colVersion">
                                    <h:outputText binding="#{caseMgt.colVersionRows}"
                                                  id="colVersionRows"
                                                  value="#{currentRow.specVersion}"/>

                                </h:column>

                                <h:column binding="#{caseMgt.colDescription}"
                                          id="colDescription">
                                    <h:outputText binding="#{caseMgt.colDescriptionRows}"
                                                  id="colDescriptionRows"
                                                  value="#{currentRow.documentation}"/>

                                </h:column>

                                <h:column binding="#{caseMgt.colSBar}"
                                          id="colSBar"/>

                            </h:dataTable>
                            </ui:panelGroup>

                            <ui:button action="#{caseMgt.btnLaunchDelayed_action}"
                                           binding="#{caseMgt.btnLaunchDelayed}"
                                           id="btnLaunchDelayed"
                                           styleClass="caseMgtButton"
                                           style="left: 128px; top: 230px"
                                           text="Launch Later"/>

                            <ui:button action="#{caseMgt.btnUnload_action}"
                                       binding="#{caseMgt.btnUnload}"
                                       id="btnUnload"
                                       styleClass="caseMgtButton"
                                       style="left: 245px; top: 230px"
                                       text="Unload Spec"/>

                            <ui:button action="#{caseMgt.btnGetInfo_action}"
                                       binding="#{caseMgt.btnGetInfo}"
                                       id="btnGetInfo"
                                       styleClass="caseMgtButton"
                                       style="left: 362px; top: 230px"
                                       text="Get Info"/>

                            <ui:button action="#{caseMgt.btnDownloadLog_action}"
                                       binding="#{caseMgt.btnDownloadLog}"
                                       id="btnDownloadLog"
                                       styleClass="caseMgtButton"
                                       style="left: 479px; top: 230px"
                                       text="Download Log"/>
                        </ui:panelLayout>

                        <!-- Running Cases Panel -->
                        <ui:panelLayout binding="#{caseMgt.layoutPanel3}"
                                        id="layoutPanel3"
                                        styleClass="caseMgtPanel"
                                        style="height: 194px; top: 396px">
                            
                            <ui:button binding="#{SessionBean.btnRefresh}"
                                       action="#{caseMgt.btnRefresh_action}"
                                       id="btnRefresh"
                                       imageURL="/resources/refresh.png"
                                       styleClass="refreshCasesButton"
                                       toolTip="Refresh Running Cases"
                                       text=""/>

                            <ui:button action="#{caseMgt.btnCancelCase_action}"
                                       binding="#{caseMgt.btnCancelCase}"
                                       id="btnCancelCase"
                                       styleClass="caseMgtButton"
                                       style="left: 11px; top: 156px"
                                       text="Cancel Case"/>

                            <ui:button action="#{caseMgt.btnRaiseException_action}"
                                       binding="#{caseMgt.btnRaiseException}"
                                       id="btnRaiseException"
                                       styleClass="caseMgtButton"
                                       style="left: 129px; top: 156px"
                                       visible="#{ApplicationBean.exceptionServiceEnabled}"
                                       text="Raise Exception"/>

                            <ui:button action="#{caseMgt.btnRejectWorklet_action}"
                                       binding="#{caseMgt.btnRejectWorklet}"
                                       id="btnRejectWorklet"
                                       styleClass="caseMgtButton"
                                       style="left: 247px; top: 156px"
                                       visible="#{ApplicationBean.exceptionServiceEnabled}"
                                       text="Reject Worklet"/>

                            <ui:button action="#{caseMgt.btnWorkletAdmin_action}"
                                        binding="#{caseMgt.btnWorkletAdmin}"
                                        id="btnWorkletAdmin"
                                        styleClass="caseMgtButton"
                                        style="left: 365px; top: 156px"
                                        visible="#{ApplicationBean.exceptionServiceEnabled}"
                                        text="Worklet Admin"/>

                            <ui:listbox binding="#{SessionBean.lbxRunningCases}"
                                        id="lbxRunningCases"
                                        items="#{SessionBean.runningCaseListOptions}"
                                        selected="#{SessionBean.runningCaseListChoice}"
                                        onKeyPress="return disableEnterKey(event);"
                                        styleClass="caseMgtListbox"/>

                            <ui:staticText binding="#{caseMgt.staticText3}"
                                           id="staticText3"
                                           styleClass="pageSubheading"
                                           style="left: 12px; top: 12px"
                                           text="#{SessionBean.runningCasesCaption}"/>
                        </ui:panelLayout>


                        <!-- delayed launch popup panel -->
                        <ui:panelLayout binding="#{SessionBean.pnlUploadBlockout}"
                                        id="delayedBlockoutPanel"
                                        styleClass="transPanel"
                                        visible="#{SessionBean.delayedLaunchPanelVisible}" />

                        <ui:panelLayout binding="#{caseMgt.pnlDelayed}"
                                        id="pnlDelayed"
                                        styleClass="delayedLaunchPanel"
                                        style="position:absolute;"
                                        visible="#{SessionBean.delayedLaunchPanelVisible}" >

                            <ui:staticText binding="#{caseMgt.stDelayedHeader}"
                                           id="delayedHeader"
                                           styleClass="pageSubheading"
                                           style="position:absolute; left: 12px; top: 12px"
                                           text="Delay Case Launch Until:"/>

                            <ui:radioButton binding="#{caseMgt.rbTime}"
                                            id="rbTime"
                                            name="rButtonGroup"
                                            styleClass="delayedLaunchRadioButton"
                                            style="top: 45px"
                                            selected="#{SessionBean.delayTimeRBSelected}"
                                            onClick="common_timeoutSubmitForm(this.form, 'rbTime');"
                                            label="After a number of seconds" />

                            <ui:radioButton binding="#{caseMgt.rbDuration}"
                                               id="rbDuration"
                                               name="rButtonGroup"
                                               styleClass="delayedLaunchRadioButton"
                                               style="top: 75px"
                                               selected="#{SessionBean.delayDurationRBSelected}"
                                               onClick="common_timeoutSubmitForm(this.form, 'rbDuration');"
                                               label="After a duration" />

                            <ui:radioButton binding="#{caseMgt.rbDate}"
                                            id="rbDate"
                                            name="rButtonGroup"
                                            styleClass="delayedLaunchRadioButton"
                                            style="top: 105px"
                                            selected="#{SessionBean.delayDateRBSelected}"
                                            onClick="common_timeoutSubmitForm(this.form, 'rbDate');"
                                            label="At an exact date and time" />

                            <ui:label binding="#{caseMgt.lblDelayValue}"
                               for="txtDelayValue"
                               id="lblDelayValue"
                               style="left: 17px; top: 143px; position: absolute"
                               text="#{caseMgt.lblValueText}"/>

                            <ui:label binding="#{caseMgt.lblDelayValueError}"
                               id="lblDelayValueError"
                               styleClass="delayValueError"
                               text="#{SessionBean.delayValueError}"/>

                            <ui:textField binding="#{caseMgt.txtDelayValue}"
                                   id="txtDelayValue"
                                   styleClass="orgDataTextField"
                                   style="left: 85px; top: 140px; width: 184px"
                                   onKeyPress="return disableEnterKey(event);"
                                   trim="false"/>

                            <ui:button action="#{caseMgt.btnDelayOK_action}"
                                       binding="#{caseMgt.btnDelayOK}"
                                       id="btnDelayOK"
                                       style="left: 35px; top: 195px"
                                       styleClass="caseMgtButton"
                                       text="OK"/>

                            <ui:button action="#{caseMgt.btnDelayCancel_action}"
                                       binding="#{caseMgt.btnDelayCancel}"
                                       id="btnDelayCancel"
                                       style="left: 155px; top: 195px"
                                       styleClass="caseMgtButton"
                                       text="Cancel"/>

                        </ui:panelLayout>

                            <div><jsp:include page="pfMsgPanel.jspf"/></div>

                        </ui:panelLayout>

                        </center>

                        <ui:hiddenField binding="#{caseMgt.hdnRowIndex}" id="hdnRowIndex"/>

                        <div><jsp:include page="pfFooter.jspf"/></div>

                    </ui:form>

                    <ui:script>
                        addOnclickToDatatableRows();
                    </ui:script>

                </ui:body>
            </ui:html>
        </ui:page>
    </f:view>
</jsp:root>
