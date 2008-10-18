<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="1.2"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:jsp="http://java.sun.com/JSP/Page"
          xmlns:ui="http://www.sun.com/web/ui">

    <jsp:directive.page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"/>

    <f:view>
        <ui:page binding="#{viewProfile.page1}" id="page1">
            <ui:html binding="#{viewProfile.html1}" id="html1">

                <ui:head binding="#{viewProfile.head1}" id="head1"
                         title="YAWL 2.0: View My Profile">
                    <ui:link binding="#{viewProfile.link1}" id="link1"
                             url="/resources/stylesheet.css"/>

                    <ui:link binding="#{ApplicationBean.favIcon}" id="lnkFavIcon"
                             rel="shortcut icon"
                            type="image/x-icon" url="/resources/favicon.ico"/>

                    <ui:script binding="#{SessionBean.script}" id="script1"
                               url="/resources/script.js"/>

                </ui:head>

                <ui:body binding="#{viewProfile.body1}" id="body1"
                         style="-rave-layout: grid">

                    <ui:form binding="#{viewProfile.form1}" id="form1">

                        <!-- include banner -->
                        <jsp:include page="pfHeader.jspf"/>

                        <div style="top: 20px; position: relative">
                            <jsp:directive.include file="pfMenubar.jspf"/>
                        </div>
                        <center>
                            <ui:panelLayout binding="#{viewProfile.pnlContainer}"
                                            id="pnlContainer"
                                            panelLayout="flow"
                                            styleClass="vpcontainerPanel">


                                 <!-- View Profile Panel -->
                                 <ui:panelLayout binding="#{viewProfile.pnlProfile}"
                                                 id="pnlSelectUser"
                                                 style="height: 220px; width: 265px"
                                                 styleClass="orgDataPanel">

                                       <ui:label binding="#{viewProfile.lblName}"
                                                 id="lblName"
                                                 style="left: 18px; top: 12px; position: absolute"
                                                 for="txtName"
                                                 text="Name:"/>

                                       <ui:textField binding="#{viewProfile.txtName}"
                                                     id="txtName"
                                                     disabled="true"
                                                     style="left: 102px; top: 12px; width: 145px; position: absolute"/>

                                       <ui:label binding="#{viewProfile.lblUserID}"
                                                 id="lblUserID"
                                                 style="left: 18px; top: 48px; position: absolute"
                                                 for="txtUserID"
                                                 text="UserID:"/>

                                       <ui:textField binding="#{viewProfile.txtUserID}"
                                                     id="txtUserID"
                                                     disabled="true"
                                                     style="left: 102px; top: 48px; width: 145px; position: absolute"/>

                                       <ui:label binding="#{viewProfile.lblRoles}"
                                                 id="lblRoles"
                                                 style="left: 18px; top: 84px; position: absolute"
                                                 for="cbbRoles"
                                                 text="Roles:"/>

                                       <ui:dropDown binding="#{viewProfile.cbbRoles}"
                                                    id="cbbRoles"
                                                    style="left: 102px; top: 84px; position: absolute; width: 150px"/>

                                       <ui:label binding="#{viewProfile.lblPositions}"
                                                 id="lblPositions"
                                                 style="left: 18px; top: 120px; position: absolute"
                                                 for="cbbPositions"
                                                 text="Positions:"/>

                                       <ui:dropDown binding="#{viewProfile.cbbPositions}"
                                                    id="cbbPositions"
                                                    style="left: 102px; top: 120px; position: absolute; width: 150px"/>

                                       <ui:label binding="#{viewProfile.lblCapabilities}"
                                                 id="lblCapabilities"
                                                 style="left: 18px; top: 156px; position: absolute"
                                                 for="cbbCapabilities"
                                                 text="Capabilities:"/>

                                       <ui:dropDown binding="#{viewProfile.cbbCapabilities}"
                                                    id="cbbCapabilities"
                                                    style="left: 102px; top: 156px; position: absolute; width: 150px"/>

                                       <ui:checkbox binding="#{viewProfile.cbxAdmin}"
                                                    id="cbxAdmin"
                                                    label="Administrator"
                                                    disabled="true"
                                                    labelLevel="2"
                                                    style="left: 102px; top: 192px; position: absolute"/>

                                 </ui:panelLayout>


                                 <!-- Piled Tasks -->

                                 <ui:panelLayout binding="#{viewProfile.pnlPiled}"
                                                 id="pnlPiled"
                                                 styleClass="orgDataPanel"
                                                 style="right: 0; height: 185px; width: 270px">

                                     <ui:staticText binding="#{viewProfile.sttPiled}"
                                                    id="sttPiled"
                                                    styleClass="pageSubheading"
                                                    style="left: 12px; top: 12px"
                                                    text="Piled Tasks"/>

                                     <ui:listbox binding="#{viewProfile.lbxPiled}"
                                                 id="lbxPiled"
                                                 items="#{SessionBean.piledTasks}"
                                                 style="height: 99px; left: 12px; top: 35px; position: absolute; width: 245px"/>

                                     <ui:button action="#{viewProfile.btnUnpile_action}"
                                                binding="#{viewProfile.btnUnpile}"
                                                id="btnUnpile"
                                                styleClass="orgDataButton"
                                                style="left: 85px; top: 145px"
                                                toolTip="Stop piling the selected task"
                                                text="Unpile"/>

                                 </ui:panelLayout>



<!-- Password Panel -->

<ui:panelLayout binding="#{viewProfile.pnlNewPassword}"
                id="pnlNewPassword"
                styleClass="orgDataPanel"
                style="height: 150px; top: 224px; width: 265px">

    <ui:staticText binding="#{viewProfile.sttPassword}"
                   id="sttPassword"
                   styleClass="pageSubheading"
                   style="left: 12px; top: 12px"
                   text="Change Password"/>

    <ui:label binding="#{viewProfile.lblNewPassword}"
              id="lblNewPassword"
              style="left: 18px; top: 40px; position: absolute"
              for="txtNewPassword"
              text="New:"/>

    <ui:passwordField binding="#{viewProfile.txtNewPassword}"
                      id="txtNewPassword"
                      style="left: 102px; top: 40px; width: 145px; position: absolute"/>

    <ui:label binding="#{viewProfile.lblConfirmPassword}"
              id="lblConfirmPassword"
              style="left: 18px; top: 76px; position: absolute"
              for="txtConfirmPassword"
              text="Confirm:"/>

    <ui:passwordField binding="#{viewProfile.txtConfirmPassword}"
                      id="txtConfirmPassword"
                      style="left: 102px; top: 76px; width: 145px; position: absolute"/>

    <ui:button action="#{viewProfile.btnSavePassword_action}"
               binding="#{viewProfile.btnSavePassword}"
               id="btnSavePassword"
               styleClass="orgDataButton"
               style="left: 75px; top: 110px"
               toolTip="Save the new password"
               text="Save"/>

</ui:panelLayout>


                             <!-- Chained Cases -->

                             <ui:panelLayout
                                 binding="#{viewProfile.pnlChained}"
                                 id="pnlChained"
                                 styleClass="orgDataPanel"
                                 style="right:0; height: 185px; top: 189px; width: 270px">

                                 <ui:staticText
                                         binding="#{viewProfile.sttChained}"
                                         id="sttChained"
                                         styleClass="pageSubheading"
                                         style="left: 12px; top: 12px"
                                         text="Chained Cases"/>

                                 <ui:listbox
                                         binding="#{viewProfile.lbxChained}"
                                         id="lbxChained"
                                         items="#{SessionBean.chainedCases}"
                                         style="height: 100px; left: 12px; top: 35px; position: absolute; width: 245px"/>

                                 <ui:button
                                         action="#{viewProfile.btnUnchain_action}"
                                         binding="#{viewProfile.btnUnchain}"
                                         id="btnUnchain"
                                         styleClass="orgDataButton"
                                         style="left: 85px; top: 145px"
                                         toolTip="Stop chaining workitems for the selected case"
                                         text="Unchain"/>

                                </ui:panelLayout>

                                <ui:panelLayout
                                        binding="#{SessionBean.messagePanel}"
                                        id="msgPanel"
                                        panelLayout="flow"/>

                            </ui:panelLayout>  <!-- pnlContainer -->

                        </center>
                    </ui:form>
                </ui:body>
            </ui:html>
        </ui:page>
    </f:view>
</jsp:root>
