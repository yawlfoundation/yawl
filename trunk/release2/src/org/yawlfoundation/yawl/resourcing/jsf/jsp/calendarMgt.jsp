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
                                        style="position: absolute; height: 620px; top: 3px">

              <ui:label binding="#{calendarMgt.lblFilter}"
                  for="cbbFilter"
                  id="lblFilter"
                  style="top: 10px; left: 12px; position: absolute"
                  text="Filter:"/>

              <ui:dropDown binding="#{calendarMgt.cbbFilter}"
                     id="cbbFilter"
                     onChange="common_timeoutSubmitForm(this.form, 'cbbFilter');"
                     items="#{calendarMgt.calendarMgtFilterComboItems}"
                     valueChangeListener="#{calendarMgt.cbbFilter_processValueChange}"
                     style="left: 80px; top: 10px; position: absolute; width: 200px"/>

                            <ui:label binding="#{calendarMgt.lblResource}"
                                for="cbbResource"
                                id="lblResource"
                                style="top: 33px; left: 12px; position: absolute"
                                text="Resource:"/>

                            <ui:dropDown binding="#{calendarMgt.cbbResource}"
                                   id="cbbResource"
                                   items="#{SessionBean.calResourceOptions}"
                                   onChange="common_timeoutSubmitForm(this.form, 'cbbResource');"
                                   valueChangeListener="#{calendarMgt.cbbResource_processValueChange}"
                                   style="left: 80px; top: 33px; position: absolute; width: 200px"/>

                            <ui:calendar binding="#{calendarMgt.calComponent}"
                                         id="calendarComponent"
                                         style="position: absolute; top: 30px; left: 380px"
                                         selectedDate="#{SessionBean.selectedCalMgtDate}"
                                         columns="20"
                                         dateFormatPatternHelp=""
                                         minDate="#{SessionBean.calMgtMinDate}"
                                         maxDate="#{SessionBean.calMgtMaxDate}"/>

             <ui:button action="#{calendarMgt.btnYesterday_action}"
                   binding="#{calendarMgt.btnYesterday}"
                    id="btnYesterday"
                    styleClass="nhSubCatButton"
                    style="top: 30px; left: 342px"
                    toolTip="Previous Day"
                    noTextPadding="true"
                    mini="true"
                    imageURL="/resources/prevday.png"/>

             <ui:button action="#{calendarMgt.btnTomorrow_action}"
                   binding="#{calendarMgt.btnTomorrow}"
                    id="btnTomorrow"
                    styleClass="nhSubCatButton"
                    style="top: 30px; left: 560px;"
                    toolTip="Next Day"
                    noTextPadding="true"
                    mini="true"
                    imageURL="/resources/nextday.png"/>

                            <ui:panelGroup binding="#{calendarMgt.pnlGroup}"
                                            id="pnlGroup"
                                            style="height: 500px; top: 60px; position: absolute"
                                            styleClass="tablePnlGroup">

                            <h:dataTable binding="#{calendarMgt.dataTable1}"
                                         headerClass="dataTableHeader"
                                         id="dataTable1"
                                         cellpadding="3"
                                         style="height: 500px"
                                         styleClass="dataTable"
                                         columnClasses="specsNameCol,
                                                        specsVersCol,
                                                        specsDescCol,
                                                        dataTableScrollBarCol"                                    
                                         value="#{SessionBean.calendarRows}"
                                         var="currentRow"
                                         width="570">

                                <h:column binding="#{calendarMgt.colStartTime}"
                                          id="colStartTime">

                                    <h:outputText binding="#{calendarMgt.colStartTimeRows}"
                                                  id="colStartTimeRows"
                                                  value="#{currentRow.startTimeAsString}"/>
                                </h:column>

                                <h:column binding="#{calendarMgt.colEndTime}"
                                          id="colEndTime">

                                    <h:outputText binding="#{calendarMgt.colEndTimeRows}"
                                                  id="colWndTimeRows"
                                                  value="#{currentRow.endTimeAsString}"/>
                                </h:column>

                                <h:column binding="#{calendarMgt.colName}"
                                          id="colName">
                                    <h:outputText binding="#{calendarMgt.colNameRows}"
                                                  id="colNameRows"
                                                  value="#{currentRow.name}"/>
                                </h:column>

                                <h:column binding="#{calendarMgt.colStatus}"
                                          id="colStatus">
                                    <h:outputText binding="#{calendarMgt.colStatusRows}"
                                                  id="colStatusRows"
                                                  value="#{currentRow.status}"/>
                                </h:column>

                                <h:column binding="#{calendarMgt.colWorkload}"
                                          id="colWorkload">
                                    <h:outputText binding="#{calendarMgt.colWorkloadRows}"
                                                  id="colWorkloadRows"
                                                  value="#{currentRow.workload}"/>
                                </h:column>

                                <h:column binding="#{calendarMgt.colComment}"
                                          id="colComment">
                                    <h:outputText binding="#{calendarMgt.colCommentRows}"
                                                  id="colCommentRows"
                                                  value="#{currentRow.comment}"/>
                                </h:column>

                                <h:column binding="#{calendarMgt.colSBar}"
                                          id="colSBar"/>

                            </h:dataTable>
                            </ui:panelGroup>

                            <ui:button action="#{calendarMgt.btnAdd_action}"
                                       binding="#{calendarMgt.btnAdd}"
                                       id="btnAdd"
                                       styleClass="caseMgtButton"
                                       style="left: 11px; top: 580px"
                                       text="Add"/>

                            <ui:button action="#{calendarMgt.btnUpdate_action}"
                                       binding="#{calendarMgt.btnUpdate}"
                                       id="btnUpdate"
                                       styleClass="caseMgtButton"
                                       style="left: 129px; top: 580px"
                                       text="Update"/>

                            <ui:button action="#{calendarMgt.btnDelete_action}"
                                       binding="#{calendarMgt.btnDelete}"
                                       id="btnDelete"
                                       styleClass="caseMgtButton"
                                       style="left: 247px; top: 580px"
                                       text="Delete"/>

                            <ui:button binding="#{SessionBean.btnRefresh}"
                                       action="#{calendarMgt.btnRefresh_action}"
                                       id="btnRefresh"
                                       imageURL="/resources/refresh.png"
                                       styleClass="refreshCasesButton"
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
