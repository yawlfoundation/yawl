<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="1.2" xmlns:f="http://java.sun.com/jsf/core" xmlns:h="http://java.sun.com/jsf/html" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:ui="http://www.sun.com/web/ui">
    <jsp:directive.page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"/>
    <f:view>
        <ui:page binding="#{visualiser.page1}" id="page1">
            <ui:html binding="#{visualiser.html1}" id="html1">
                <ui:head binding="#{visualiser.head1}" id="head1">
                    <ui:link binding="#{visualiser.link1}" id="link1"
                             url="/resources/stylesheet.css"/>

                    <ui:link binding="#{ApplicationBean.favIcon}" id="lnkFavIcon"
                             rel="shortcut icon"
                            type="image/x-icon" url="/resources/favicon.ico"/>

                </ui:head>                               

<ui:body style="background-color: rgb(255, 255, 255); -rave-layout: grid" id="body1"
        binding="#{visualiser.body1}">


                <jsp:plugin archive="visualiser.jar, javax.servlet.jar, jdom.jar,log4j-1.2.14.jar, resourceService.jar, saxon9.jar"
                            code="worklist/WRKLApplet.class" height="600" hspace="10"
                            type="applet" vspace="50" width="800"
                            jreversion="1.5">
                    <jsp:params>
                        <jsp:param name="user" value="th"/>
                        <jsp:param name="pass" value="apple"/>
                        <jsp:param name="urYAWL" value="http://localhost:8080/WebApplet"/>
                    </jsp:params>
                    <jsp:fallback>
   				Your browser can't display this applet. Sorry.
 		    </jsp:fallback>
                </jsp:plugin>

                 </ui:body>
            </ui:html>
        </ui:page>
    </f:view>
</jsp:root>
