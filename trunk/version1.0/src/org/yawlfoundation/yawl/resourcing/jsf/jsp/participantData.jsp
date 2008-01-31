<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="1.2"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:jsp="http://java.sun.com/JSP/Page"
          xmlns:ui="http://www.sun.com/web/ui">

    <jsp:directive.page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"/>
    
    <f:view>
        <ui:page binding="#{participantData.page1}" id="page1">
            <ui:html binding="#{participantData.html1}" id="html1">
                <ui:head binding="#{participantData.head1}" id="head1">
                    <ui:link binding="#{participantData.link1}" id="link1"
                             url="/resources/stylesheet.css"/>
                </ui:head>
                <ui:body binding="#{participantData.body1}" id="body1" style="-rave-layout: grid">
                    <ui:form binding="#{participantData.form1}" id="form1">

                        <ui:panelLayout binding="#{SessionBean.topPanel}"
                                        id="topPanel"
                                        panelLayout="flow"
                                        styleClass="topPanel"/>
                        <div style="left: 0px; top: 0px; position: absolute">
                             <jsp:directive.include file="pfHeader.jspf"/>
                        </div>

                        <div style="left: 0px; top: 72px; position: absolute">
                            <jsp:directive.include file="pfMenu.jspf"/>
                        </div>

                        <!-- Select Participant Panel -->
                        <ui:panelLayout binding="#{participantData.pnlSelectUser}"
                                        id="pnlSelectUser"
                                        style="top: 74px; height: 34px; left: 124px"
                                        styleClass="orgDataPanel">

                            <ui:dropDown binding="#{participantData.cbbParticipants}"
                                         id="cbbParticipants"
                                         items="#{SessionBean.orgDataParticipantList}"
                                         onChange="common_timeoutSubmitForm(this.form, 'cbbParticipants');"

                                         style="left: 102px; top: 6px; position: absolute; width: 288px"
                                         valueChangeListener="#{participantData.cbbParticipants_processValueChange}"/>

                            <ui:label binding="#{participantData.label1}"
                                      id="label1" style="left: 18px; top: 12px; position: absolute"
                                      text="Participant:"/>
                        </ui:panelLayout>



                        <ui:panelLayout binding="#{participantData.pnlPrivileges}"
                                        id="pnlPrivileges"
                                        styleClass="orgDataPanel"
                                        style="height: 333px; left: 535px; top: 74px; width: 255px">

                            <ui:staticText binding="#{participantData.staticText1}"
                                           id="staticText1"
                                           styleClass="pageSubheading"
                                           style="left: 30px; top: 12px"
                                           text="Privileges"/>

                            <ui:checkbox binding="#{participantData.cbxChooseItemToStart}"
                                         id="cbxChooseItemToStart"
                                         label="Choose Which Work Item to Start"
                                         styleClass="orgDataPrivCheckBox"
                                         selected="false"
                                         style="top: 40px"/>

                            <ui:checkbox binding="#{participantData.cbxStartConcurrent}"
                                         id="cbxStartConcurrent"
                                         label="Start Work Items Concurrently"
                                         styleClass="orgDataPrivCheckBox"
                                         selected="false"
                                         style="top: 69px"/>

                            <ui:checkbox binding="#{participantData.cbxReorderItems}"
                                         id="cbxReorderItems"
                                         label="Reorder Work Items"
                                         styleClass="orgDataPrivCheckBox"
                                         selected="false"
                                         style="top: 98px"/>

                            <ui:checkbox binding="#{participantData.cbxViewAllOffered}"
                                         id="cbxViewAllOffered"
                                         label="View All Offered Work items"
                                         styleClass="orgDataPrivCheckBox"
                                         selected="false"
                                         style="top: 127px"/>

                            <ui:checkbox binding="#{participantData.cbxViewAllAllocated}"
                                         id="cbxViewAllAllocated"
                                         label="View All Allocated Work Items"
                                         styleClass="orgDataPrivCheckBox"
                                         selected="false"
                                         style="top: 156px"/>

                            <ui:checkbox binding="#{participantData.cbxViewAllExecuting}"
                                         id="cbxViewAllExecuting"
                                         label="View All Executing Work Items"
                                         styleClass="orgDataPrivCheckBox"
                                         selected="false"
                                         style="top: 185px"/>

                            <ui:checkbox binding="#{participantData.cbxViewTeamItems}"
                                         id="cbxViewTeamItems"
                                         label="View All Work Items of Team"
                                         styleClass="orgDataPrivCheckBox"
                                         selected="false"
                                         style="top: 214px"/>

                            <ui:checkbox binding="#{participantData.cbxViewOrgGroupItems}"
                                         id="cbxViewOrgGroupItems"
                                         label="View All Work items of Org Group"
                                         styleClass="orgDataPrivCheckBox"
                                         selected="false"
                                         style="top: 243px"/>

                            <ui:checkbox binding="#{participantData.cbxChainItems}"
                                         id="cbxChainItems"
                                         label="Chain Work Item Execution"
                                         styleClass="orgDataPrivCheckBox"
                                         selected="false"
                                         style="top: 272px"/>

                            <ui:checkbox binding="#{participantData.cbxManageCases}"
                                         id="cbxManageCases"
                                         label="Manage Cases"
                                         styleClass="orgDataPrivCheckBox"
                                         selected="false"
                                         style="top: 301px"/>
                        </ui:panelLayout>


                        <ui:panelLayout binding="#{participantData.pnlUserDetails}"
                                        id="pnlUserDetails"
                                        styleClass="orgDataPanel"
                                        style="height: 293px; top: 114px; left: 124px">

                            <ui:textField binding="#{participantData.txtFirstName}"
                                          columns="40"
                                          id="txtFirstName"
                                          label="First Name:"
                                          style="left: 18px; top: 12px; position: absolute"/>

                            <ui:textField binding="#{participantData.txtLastName}"
                                          columns="40"
                                          id="txtLastName"
                                          label="Last Name:"
                                          style="left: 18px; top: 48px; position: absolute"/>

                            <ui:textField binding="#{participantData.txtUserID}"
                                          columns="20" id="txtUserID"
                                          label="User ID:"
                                          style="left: 37px; top: 84px; position: absolute"/>

                            <ui:checkbox binding="#{participantData.cbxAdmin}"
                                         id="cbxAdmin"
                                         label="Administrator"
                                         labelLevel="2"
                                         style="left: 270px; top: 84px; position: absolute"/>

                            <ui:textArea binding="#{participantData.txtDesc}"
                                         columns="40"
                                         id="txtDesc"
                                         label="Description:"
                                         rows="4"
                                         style="left: 11px; top: 126px; position: absolute"/>

                            <ui:textArea binding="#{participantData.txtNotes}"
                                         columns="40"
                                         id="txtNotes"
                                         label="Notes:"
                                         rows="4"
                                         style="left: 43px; top: 210px; position: absolute"/>
                        </ui:panelLayout>


                        <ui:tabSet binding="#{participantData.tabSetAttributes}"
                                   id="tabSetAttributes"
                                   selected="tabRoles"
                                   style="border: 2px solid gray; height: 200px; left: 125px; top: 415px; position: absolute; width: 405px">

                            <ui:tab binding="#{participantData.tabRoles}"
                                    action="#{participantData.tabRoles_action}"
                                    id="tabRoles"
                                    text="Roles">

                                <ui:panelLayout
                                        binding="#{participantData.tabPanelRole}"
                                        id="tabPanelRole"
                                        styleClass="orgDataTabPanel"/>
                            </ui:tab>

                            <ui:tab binding="#{participantData.tabPosition}"
                                    action="#{participantData.tabPosition_action}"
                                    id="tabPosition"
                                    text="Positions">

                                <ui:panelLayout binding="#{participantData.tabPanelPosition}"
                                                id="tabPanelPosition"
                                                styleClass="orgDataTabPanel"/>
                            </ui:tab>

                            <ui:tab binding="#{participantData.tabCapability}"
                                    action="#{participantData.tabCapability_action}"
                                    id="tabCapability"
                                    text="Capabilities">

                                <ui:panelLayout binding="#{participantData.tabPanelCapability}"
                                                id="tabPanelCapability"
                                                styleClass="orgDataTabPanel"/>
                            </ui:tab>

                        </ui:tabSet>


                        <ui:button action="#{participantData.btnSave_action}"
                                   binding="#{participantData.btnSave}"
                                   id="btnSave"
                                   styleClass="orgDataButton"
                                   style="top: 506px"
                                   text="Save"/>

                        <ui:button action="#{participantData.btnReset_action}"
                                   binding="#{participantData.btnReset}"
                                   id="btnReset"
                                   styleClass="orgDataButton"
                                   style="top: 556px"
                                   text="Reset"/>
                        
                        <ui:button action="#{participantData.btnEditStructure_action}"
                                   binding="#{participantData.btnEditStructure}"
                                   id="btnEditStructure"
                                   styleClass="orgDataButton"
                                   style="top: 456px"
                                   text="Edit Structure..."/>

                        <div style="left: 123px; top: 458px; position: absolute">
                            <jsp:directive.include file="pfAddRemove.jspf"/>
                        </div>
                    
                    </ui:form>
                </ui:body>
            </ui:html>
        </ui:page>
    </f:view>
</jsp:root>
