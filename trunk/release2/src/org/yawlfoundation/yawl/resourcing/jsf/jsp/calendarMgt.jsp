<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="1.2" xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:jsp="http://java.sun.com/JSP/Page"
          xmlns:ui="http://www.sun.com/web/ui">

<!--
  ~ Copyright (c) 2004-2011 The YAWL Foundation. All rights reserved.
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
        <ui:page binding="#{calendarMgt.page1}" id="page1">
            <ui:html binding="#{calendarMgt.html1}" id="html1">
                <ui:head binding="#{calendarMgt.head1}" id="head1"
                         title="YAWL 2.2 Calendar Management">

                    <ui:link binding="#{calendarMgt.link1}" id="link1"
                             url="/resources/stylesheet.css"/>

                    <ui:link binding="#{ApplicationBean.favIcon}" id="lnkFavIcon"
                             rel="shortcut icon"
                            type="image/x-icon" url="/resources/favicon.ico"/>

                    <ui:script binding="#{SessionBean.script}" id="script1"
                               url="/resources/script.js"/>

                </ui:head>
                <ui:body binding="#{calendarMgt.body1}" id="body1"
                         style="-rave-layout: grid">
                    <ui:form binding="#{calendarMgt.form1}" id="form1">

                        <!-- include banner -->
                        <div><jsp:directive.include file="pfHeader.jspf"/></div>

                        <div style="top: 20px; position: relative">
                            <jsp:directive.include file="pfMenubar.jspf"/>
                        </div>
                        <center>

                        <ui:panelLayout binding="#{calendarMgt.pnlContainer}"
                                        id="pnlContainer"
                                        style="#{SessionBean.outerPanelTop}"
                                        styleClass="calendarMgtContainerPanel">

                        <ui:panelLayout binding="#{calendarMgt.layoutPanel2}"
                                        id="layoutPanel2"
                                        styleClass="caseMgtPanel"
                                        style="position: absolute; height: 642px; width: 595px; top: 3px">

              <ui:label binding="#{calendarMgt.lblFilter}"
                  for="cbbFilter"
                  id="lblFilter"
                  style="top: 30px; left: 292px; position: absolute"
                  text="Filter:"/>

              <ui:dropDown binding="#{calendarMgt.cbbFilter}"
                     id="cbbFilter"
                     onChange="common_timeoutSubmitForm(this.form, 'cbbFilter');"
                     items="#{calendarMgt.calendarMgtFilterComboItems}"
                     valueChangeListener="#{calendarMgt.cbbFilter_processValueChange}"
                     style="left: 360px; top: 30px; position: absolute; width: 200px"/>

                            <ui:label binding="#{calendarMgt.lblResource}"
                                for="cbbResource"
                                id="lblResource"
                                style="top: 56px; left: 292px; position: absolute"
                                text="Resource:"/>

                            <ui:dropDown binding="#{calendarMgt.cbbResource}"
                                   id="cbbResource"
                                   items="#{SessionBean.calResourceOptions}"
                                   onChange="common_timeoutSubmitForm(this.form, 'cbbResource');"
                                   valueChangeListener="#{calendarMgt.cbbResource_processValueChange}"
                                   style="left: 360px; top: 56px; position: absolute; width: 200px"/>

                            <ui:panelLayout binding="#{calendarMgt.calPanel}"
                                            id="calPanel"
                                            styleClass="caseMgtPanel"
                                            style="border: 1px solid gray; position: absolute; height: 23px; top: 50px; left: 16px; width: 239px">

                                <ui:button action="#{calendarMgt.btnYesterday_action}"
                                      binding="#{calendarMgt.btnYesterday}"
                                       id="btnYesterday"
                                       styleClass="nhSubCatButton"
                                       style="top: 0px; left: 0px;"
                                       toolTip="Previous Day"
                                       noTextPadding="true"
                                       mini="true"
                                       imageURL="/resources/prevday.png"/>

                            <ui:calendar binding="#{calendarMgt.calComponent}"
                                         id="calendarComponent"
                                         style="top: 0px; left: 40px;"
                                         selectedDate="#{SessionBean.selectedCalMgtDate}"
                                         columns="15"
                                         onChange="common_timeoutSubmitForm(this.form, 'calendarComponent');"
                                         dateFormatPatternHelp=""
                                         minDate="#{SessionBean.calMgtMinDate}"
                                         maxDate="#{SessionBean.calMgtMaxDate}"/>


             <ui:button action="#{calendarMgt.btnTomorrow_action}"
                   binding="#{calendarMgt.btnTomorrow}"
                    id="btnTomorrow"
                    styleClass="nhSubCatButton"
                    style="top: 0px; right: 0px;"
                    toolTip="Next Day"
                    noTextPadding="true"
                    mini="true"
                    imageURL="/resources/nextday.png"/>
            </ui:panelLayout>

              <ui:panelLayout binding="#{calendarMgt.upperPanel}"
                              id="upperPanel"
                              styleClass="caseMgtPanel"
                              style="height: 380px; top: 90px; left: 12px; width: 567px">

                            <ui:panelGroup binding="#{calendarMgt.pnlGroup}"
                                            id="pnlGroup"
                                            style="border: 1px solid gray; height: 330px; top: -1px; left: -1px; position: absolute"
                                            styleClass="tablePnlGroup">

                            <h:dataTable binding="#{calendarMgt.dataTable1}"
                                         headerClass="dataTableHeader"
                                         id="dataTable1"
                                         cellpadding="3"
                                         style="#{SessionBean.calDataTableStyle}"
                                         styleClass="dataTable"
                                         columnClasses="calTimeCol,
                                                        calTimeCol,
                                                        calNameCol,
                                                        calStatusCol,
                                                        calWorkloadCol,
                                                        calCommentCol,
                                                        dataTableScrollBarCol"                                    
                                         value="#{SessionBean.calendarRows}"
                                         var="currentRow"
                                         width="570">

                                <h:column binding="#{calendarMgt.colStartTime}"
                                          id="colStartTime">
                                    <f:facet name="header" >
                                         <h:commandLink value="Start"/>
                                    </f:facet>
                                    <h:outputText binding="#{calendarMgt.colStartTimeRows}"
                                                  id="colStartTimeRows"
                                                  value="#{currentRow.startTimeAsString}"/>
                                </h:column>

                                <h:column binding="#{calendarMgt.colEndTime}"
                                          id="colEndTime">
                                    <f:facet name="header" >
                                         <h:commandLink value="End"/>
                                    </f:facet>
                                    <h:outputText binding="#{calendarMgt.colEndTimeRows}"
                                                  id="colWndTimeRows"
                                                  value="#{currentRow.endTimeAsString}"/>
                                </h:column>

                                <h:column binding="#{calendarMgt.colName}"
                                          id="colName">
                                    <f:facet name="header" >
                                         <h:commandLink value="Resource"/>
                                    </f:facet>
                                    <h:outputText binding="#{calendarMgt.colNameRows}"
                                                  id="colNameRows"
                                                  value="#{currentRow.name}"/>
                                </h:column>

                                <h:column binding="#{calendarMgt.colStatus}"
                                          id="colStatus">
                                    <f:facet name="header" >
                                         <h:commandLink value="Status"/>
                                    </f:facet>
                                    <h:outputText binding="#{calendarMgt.colStatusRows}"
                                                  id="colStatusRows"
                                                  value="#{currentRow.status}"/>
                                </h:column>

                                <h:column binding="#{calendarMgt.colWorkload}"
                                          id="colWorkload">
                                    <f:facet name="header" >
                                         <h:commandLink value="Workload"/>
                                    </f:facet>
                                    <h:outputText binding="#{calendarMgt.colWorkloadRows}"
                                                  id="colWorkloadRows"
                                                  value="#{currentRow.workload}"/>
                                </h:column>

                                <h:column binding="#{calendarMgt.colComment}"
                                          id="colComment">
                                    <f:facet name="header" >
                                         <h:commandLink value="Comments"/>
                                    </f:facet>
                                    <h:outputText binding="#{calendarMgt.colCommentRows}"
                                                  id="colCommentRows"
                                                  value="#{currentRow.comment}"/>
                                </h:column>

                                <h:column binding="#{calendarMgt.colSBar}"
                                          id="colSBar"/>

                            </h:dataTable>
                            </ui:panelGroup>

                  <ui:button action="#{calendarMgt.btnUpdate_action}"
                             binding="#{calendarMgt.btnUpdate}"
                             id="btnUpdate"
                             styleClass="caseMgtButton"
                             style="left: 11px; top: 340px"
                             text="Edit"/>

                  <ui:button action="#{calendarMgt.btnDelete_action}"
                             binding="#{calendarMgt.btnDelete}"
                             id="btnDelete"
                             styleClass="caseMgtButton"
                             style="left: 129px; top: 340px"
                             text="Remove"/>

               </ui:panelLayout>

              <ui:panelLayout binding="#{calendarMgt.editPanel}"
                              id="editPanel"
                              styleClass="caseMgtPanel"
                              style="position: absolute; height: 152px; top: 480px; left: 12px; width: 567px">

                  <ui:label binding="#{calendarMgt.lblResourceName}"
                            id="lblResourceName" style="left: 12px; top: 12px; position: absolute"
                            text="Resource:"/>

                  <ui:staticText binding="#{calendarMgt.sttResourceName}"
                                 id="sttResourceName"
                                 style="left: 80px; top: 12px; position: absolute"
                                 text="#{SessionBean.calEditedResourceName}"/>

                  <ui:label binding="#{calendarMgt.lblStart}"
                            id="lblStart" style="left: 12px; top: 42px; position: absolute"
                            text="Start Time:"/>

                  <ui:label binding="#{calendarMgt.lblEnd}"
                            id="lblEnd" style="left: 170px; top: 42px; position: absolute"
                            text="End Time:"/>

                  <ui:label binding="#{calendarMgt.lblUntil}"
                            id="lblUntil" style="left: 310px; top: 42px; position: absolute"
                            text="Until:"/>

                  <ui:label binding="#{calendarMgt.lblWorkload}"
                            id="lblWorkload" style="left: 12px; top: 72px; position: absolute"
                            text="Workload (%):"/>

                  <ui:label binding="#{calendarMgt.lblComments}"
                            id="lblComments" style="left: 170px; top: 72px; position: absolute"
                            text="Comments:"/>


                  <ui:textField binding="#{calendarMgt.txtStartTime}"
                                id="txtStartTime"
                                onKeyPress="return disableEnterKey(event);"
                                style="left: 100px; top: 42px; width: 40px; position: absolute"/>

                  <ui:textField binding="#{calendarMgt.txtEndTime}"
                                id="txtEndTime"
                                onKeyPress="return disableEnterKey(event);"
                                style="left: 240px; top: 42px; width: 40px; position: absolute"/>

                  <ui:textField binding="#{calendarMgt.txtWorkload}"
                                id="txtWorkload"
                                onKeyPress="return disableEnterKey(event);"
                                style="left: 100px; top: 72px; width: 40px; position: absolute"/>

                  <ui:textField binding="#{calendarMgt.txtComments}"
                                id="txtComments"
                                onKeyPress="return disableEnterKey(event);"
                                style="left: 240px; top: 72px; width: 310px; position: absolute"/>

                            <ui:calendar binding="#{calendarMgt.calDuration}"
                                         id="calendarDuration"
                                         style="top: 42px; left: 350px; position: absolute"
                                         selectedDate="#{SessionBean.selectedDurationDate}"
                                         columns="15"
                                         dateFormatPatternHelp=""
                                         onChange="null"
                                         minDate="#{SessionBean.calMgtMinDate}"
                                         maxDate="#{SessionBean.calMgtMaxDate}"/>
                  
                  <ui:button action="#{calendarMgt.btnAdd_action}"
                             binding="#{calendarMgt.btnAdd}"
                             id="btnAdd"
                             styleClass="caseMgtButton"
                             style="left: 11px; top: 112px"
                             text="#{calendarMgt.btnAddText}"/>

                  <ui:button action="#{calendarMgt.btnClear_action}"
                             binding="#{calendarMgt.btnClear}"
                             id="btnClear"
                             styleClass="caseMgtButton"
                             style="left: 129px; top: 112px"
                             text="Clear"/>

                  <ui:checkbox binding="#{calendarMgt.cbxRepeat}"
                               id="cbxRepeat"
                               label="Repeat"
                               styleClass="orgDataPrivCheckBox"
                               selected="false"
                               style="top: 42px; left: 495px;"/>
              </ui:panelLayout>

                            <ui:button binding="#{SessionBean.btnRefresh}"
                                       action="#{calendarMgt.btnRefresh_action}"
                                       id="btnRefresh"
                                       imageURL="/resources/refresh.png"
                                       styleClass="refreshCasesButton"
                                       style="left: 572px;"
                                       toolTip="Refresh Running Cases"
                                       text=""/>
                        </ui:panelLayout>

                            <div><jsp:include page="pfMsgPanel.jspf"/></div>

                        </ui:panelLayout>

                        </center>

                        <ui:hiddenField binding="#{calendarMgt.hdnRowIndex}" id="hdnRowIndex"/>

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
