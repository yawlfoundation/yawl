<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
    xmlns:xhtml="http://www.w3.org/2002/06/xhtml2"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xforms="http://www.w3.org/2002/xforms"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:chiba="http://chiba.sourceforge.net/2003/08/xforms"
    exclude-result-prefixes="xhtml xforms chiba xlink">

    <xsl:include href="html-form-controls.xsl"/>


    <!-- ####################################################################################################### -->
    <!-- This stylesheet transcodes a XTHML2/XForms input document to HTML 4.0.                                  -->
    <!-- It serves as a reference for customized stylesheets which may import it to overwrite specific templates -->
    <!-- or completely replace it.                                                                               -->
    <!-- This is the most basic transformator for HTML browser clients and assumes support for HTML 4 tagset     -->
    <!-- but does NOT rely on javascript.                                                                        -->
    <!-- author: joern turner                                                                                    -->
    <!-- ####################################################################################################### -->

    <!-- ### this url will be used to build the form action attribute ### -->
    <xsl:param name="action-url" select="'http://localhost:8080/YAWLXForms-0.1/XFormsServlet'"/>

    <!-- ### the CSS stylesheet to use ### -->
    <xsl:variable name="default-css" select="'styles/yawl.css'"/>
    <xsl:variable name="mozilla-css" select="'styles/yawl.css'"/>
    <xsl:variable name="ie-css" select="'styles/yawl.css'"/>

    <!-- ### signals the phase of processing (init|submit) ### -->
    <xsl:param name="phase" select="'false'"/>

    <xsl:param name="form-id" select="'chiba-form'"/>
    <xsl:param name="form-name" select="'YAWL XForms Processor'"/>
    <xsl:param name="debug-enabled" select="'false'"/>
    <!-- ### specifies the parameter prefix for repeat selectors ### -->
    <xsl:param name="selector-prefix" select="'s_'"/>

    <!-- ### contains the full user-agent string as received from the servlet ### -->
    <xsl:param name="user-agent" select="'default'"/>

    <xsl:param name="scripted" select="'false'"/>

    <!-- ### checks, whether this form uses uploads. Used to set form enctype attribute ### -->
    <xsl:variable name="uses-upload" select="boolean(//*/xforms:upload)"/>


    <xsl:output method="html" version="4.0" encoding="ISO-8859-1" indent="yes"
        doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN"/>

    <!-- ### transcodes the XHMTL namespaced elements to HTML ### -->
    <xsl:namespace-alias stylesheet-prefix="xhtml" result-prefix="#default"/>

    <xsl:preserve-space elements="*"/>
    <xsl:strip-space elements="xforms:action"/>

    <!-- ####################################################################################################### -->
    <!-- ##################################### TEMPLATES ####################################################### -->
    <!-- ####################################################################################################### -->

    <xsl:template match="xhtml:head">
        <xsl:if test="$debug-enabled='true'">
            <xsl:message>head</xsl:message>
        </xsl:if>
        <head>
            <title>
                <xsl:value-of select="$form-name"/>
            </title>
            <xsl:call-template name="getCSS"/>
        </head>
    </xsl:template>

    <!-- copy unmatched mixed markup, comments, whitespace, and text -->
    <!-- ### copy elements from the xhtml2 namespace to html (without any namespace) by re-creating the     ### -->
    <!-- ### elements. Other Elements are just copied.                                                      ### -->
    <xsl:template match="*|@*|text()">
        <xsl:choose>
            <xsl:when test="namespace-uri(.)='http://www.w3.org/2002/06/xhtml2'">
                <xsl:element name="{local-name(.)}">
                    <xsl:apply-templates select="*|@*|text()"/>
                </xsl:element>
            </xsl:when>
            <xsl:otherwise>
                <xsl:copy>
                    <xsl:apply-templates select="*|@*|text()"/>
                </xsl:copy>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="xhtml:html">
        <xsl:if test="$debug-enabled='true'">
            <xsl:message>html</xsl:message>
        </xsl:if>
        <html>
            <xsl:apply-templates/>
        </html>
    </xsl:template>

    <xsl:template match="xhtml:link">
        <xsl:copy-of select="."/>
    </xsl:template>

    <xsl:template match="xhtml:body">
        <xsl:if test="$debug-enabled='true'">
            <xsl:message>body</xsl:message>
        </xsl:if>
        <xsl:copy-of select="@*"/>
        <body>
        <!-- Include YAWL Banner Information -->
		<br></br>
		<table width="100%" border="0" cellspacing="0" cellpadding="0" background="./graphics/bg01.gif">
			<tr>
                <!--  width="116" height="55"  -->
                <td valign="top" width="35%"><a
				    href="http://www.fit.qut.edu.au/"><img
				    src="./graphics/qut_logo.gif"
				    alt="QUT Faculty of Information Technology" border="0"/></a>
                </td>

                <!-- height="55"  -->
                <td align="center" valign="bottom" >
                    <OBJECT classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000"
                    codebase=
                    "http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=6,0,0,0"
                    WIDTH="209" HEIGHT="47" id="yawl" ALIGN="center">
                        <PARAM NAME="movie" VALUE="./graphics/yawl.swf" />
                        <PARAM NAME="quality" VALUE="high" />
                    </OBJECT>
                </td>

				<td width="30%" align="right" valign="top">
                    <img
				    src="./graphics/banner_image.gif"
				    alt="Centre for Information Technology and Innovation"/>
                </td>
			</tr>

			<tr>
                <td bgcolor="#6699cc"/>
				<td bgcolor="#6699cc" align="center">
					<img src="./graphics/subtext.jpg"/>
				</td>
                <td bgcolor="#6699cc"/>
			</tr>
		</table>

		<!-- Start Navigation Banner -->
		<table width="100%" border="0" bgcolor="#ffffff">
			<tr align="center">
				<td>
					<table border="0" cellspacing="0" cellpadding="0" width="727">
						<tr align="center">
							<td width="1"  height="3" bgcolor="#336699"/>
							<td width="120" bgcolor="#336699"/>
							<td width="1" bgcolor="#336699"/>
                            <td width="120" bgcolor="#336699"/>
							<td width="1" bgcolor="#336699"/>
							<td width="120" bgcolor="#336699"/>
							<td width="1" bgcolor="#336699"/>
							<td width="120" bgcolor="#336699"/>
							<td width="1" bgcolor="#336699"/>
							<td width="120" bgcolor="#336699"/>
							<td width="1" bgcolor="#336699"/>
							<td width="120" bgcolor="#336699"/>
							<td width="1" bgcolor="#336699"/>
						</tr>
                        
						<tr>
							<td bgcolor="#336699" height="20"/>
							<td align="center" valign="middle" bgcolor="#ffffff">
								<a href="http://www.citi.qut.edu.au/yawl" class="level3menu">YAWL Home</a>
							</td>
							<td bgcolor="#336699"/>
                            <td align="center" valign="middle" bgcolor="#ffffff">
                                <a class="level3menu">Administrate</a>
                                <!-- <xsl:element name="a">
                                    <xsl:attribute name="href"><xsl:value-of select="$worklist"/>/worklist/admin</xsl:attribute>
                                    <xsl:attribute name="class">level3menu</xsl:attribute>
                                    <xsl:text>Administrate</xsl:text>
                                </xsl:element> -->
							</td>
                            <td bgcolor="#336699"/>
							<td align="center" valign="middle" bgcolor="#ffffff">
								<a class="level3menu">Workflow Specifications</a>
							</td>
							<td bgcolor="#336699"/>
							<td align="center" valign="middle" bgcolor="#ffffff">
								<a class="level3menu">Available Work</a>
							</td>
							<td bgcolor="#336699"/>
							<td align="center" valign="middle" bgcolor="#ffffff">
								<a class="level3menu">Checked Out Work</a>
							</td>
							<td bgcolor="#336699"/>
							<td align="center" valign="middle" bgcolor="#ffffff">
								<a class="level3menu">Logout</a>
							</td>
							<td bgcolor="#336699"/>
						</tr>
						<tr>
							<td bgcolor="#336699" height="1"/>
							<td bgcolor="#336699"/>
							<td bgcolor="#336699"/>
							<td bgcolor="#336699"/>
							<td bgcolor="#336699"/>
							<td bgcolor="#336699"/>
							<td bgcolor="#336699"/>
							<td bgcolor="#336699"/>
							<td bgcolor="#336699"/>
							<td bgcolor="#336699"/>
							<td bgcolor="#336699"/>
							<td bgcolor="#336699"/>
							<td bgcolor="#336699"/>
						</tr>
					</table>


            


	    <table border="0">
                <tr>
                    <td width="33"></td>
                    <td>
                        <xsl:element name="form">
                            <xsl:attribute name="name">
                                <xsl:value-of select="$form-id"/>
                            </xsl:attribute>
                            <xsl:attribute name="action">
                                <xsl:value-of select="$action-url"/>
                            </xsl:attribute>
                            <xsl:attribute name="method">POST</xsl:attribute>
                            <xsl:attribute name="enctype">application/x-www-form-urlencoded</xsl:attribute>
                            <xsl:if test="$uses-upload">
                                <xsl:attribute name="enctype">multipart/form-data</xsl:attribute>
                            </xsl:if>
                            <xsl:if test="$scripted='true'">
                                <xsl:attribute name="onsubmit">javascript:submit();</xsl:attribute>
                            </xsl:if>

                            <!-- provide a first submit which does not map to any xforms:trigger -->
                            <input type="image" name="dummy" style="width:0pt;height:0pt;" value="dummy"/>
                            <xsl:apply-templates/>
                        </xsl:element>
                    </td>
                </tr>
                <tr>
                    <td></td>
                    <td>
                        <table width="100%" border="0">
                            <tr>
                                <td>
                                    <span class="legend">
                                        <span style="color:red">*</span> - required |
                                        <b>?</b> - help
                                    </span>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
            </table>
                
		<br/>
                <br/>
                <br/>
                <hr/>
                <table>
                    <tr align="center">
                        <td>
			YAWL is distributed under the
                        <a href="http://www.citi.qut.edu.au/yawl/downloads.jsp">YAWL licence</a>.
                        </td>
                    </tr>
                </table>
</td>
</tr>
</table>
        </body>
    </xsl:template>

    <xsl:template match="xhtml:span">
        <span>
            <xsl:copy-of select="@xhtml:class"/>
            <xsl:apply-templates/>
        </span>
    </xsl:template>

    <!-- ### skip explicitely disabled control ### -->
<!--    <xsl:template match="*[chiba:data/@chiba:enabled='false']">-->
<!--        <xsl:if test="$debug-enabled='true'">-->
<!--            <xsl:message>skipping disabled control</xsl:message>-->
<!--        </xsl:if>-->
<!--    </xsl:template>-->

    <!-- ### skip chiba:data elements ### -->
    <xsl:template match="chiba:data"/>

    <!-- ### skip model section ### -->
    <xsl:template match="xforms:model"/>

    <!-- ####################################################################################################### -->
    <!-- #################################### GROUPS ########################################################### -->
    <!-- ####################################################################################################### -->

    <!--
    processing of groups and repeats is handled with a computational pattern (as mentioned in Michael Kay's XSLT
    Programmers Reference) in this stylesheet, that means that when a group or repeat is found its children will
    be processed with for-each. this top-down approach seems to be more adequate for transforming XForms markup
    than to follow a rule-based pattern. Also note that whenever nodesets of XForms controls are processed the
    call template 'buildControl' is used to handle the control. In contrast to apply-templates a call-template
    preserves the position() of the control inside its parent nodeset and this can be valuable information for
    annotating controls with CSS classes that refer to their parent.
    -->
    <!-- ###################################### MINIMAL GROUP ################################################## -->
    <!-- handle 'minimal' group - this is the default for groups and only annotates CSS to labels + controls and
    outputs them in a kind of flow-layout -->
    <xsl:template match="xforms:group[@xforms:appearance='minimal']">
        <xsl:if test="$debug-enabled='true'">
            <xsl:message>found minimal group</xsl:message>
        </xsl:if>

        <!-- ***** ignore this group if not enabled ***** -->
<!--        <xsl:if test="not(chiba:data/@chiba:enabled='false')">-->

            <xsl:variable name="group-css">
                <xsl:call-template name="assembleClasses"/>
            </xsl:variable>

            <div class="{concat('minimal-group',$group-css)}" id="{@id}">
                <xsl:copy-of select="@class"/>

                <xsl:for-each select="*">
                    <xsl:choose>

                        <!-- **** handle group label ***** -->
                        <xsl:when test="self::xforms:label" xmlns:xforms="http://www.w3.org/2002/xforms">
                            <xsl:if test="$debug-enabled='true'">
                                <xsl:message>handling group label ...</xsl:message>
                            </xsl:if>
                            <span class="minimal-group-label label">
                                <xsl:apply-templates select="."/>
                            </span>
                        </xsl:when>

                        <!-- **** handle group alert ***** -->
                        <xsl:when test="self::xforms:alert" xmlns:xforms="http://www.w3.org/2002/xforms">
                            <xsl:apply-templates select="xforms:alert"/>
                        </xsl:when>

                        <!-- **** handle sub group ***** -->
                        <xsl:when test="self::xforms:group" xmlns:xforms="http://www.w3.org/2002/xforms">
                            <xsl:if test="$debug-enabled='true'">
                                <xsl:message>found group</xsl:message>
                            </xsl:if>
                            <xsl:apply-templates select="."/>
                        </xsl:when>

                        <!-- **** handle repeat ***** -->
                        <xsl:when test="self::xforms:repeat" xmlns:xforms="http://www.w3.org/2002/xforms">
                            <xsl:if test="$debug-enabled='true'">
                                <xsl:message>found repeat</xsl:message>
                            </xsl:if>
                            <xsl:apply-templates select="."/>
                        </xsl:when>

                        <!-- **** handle switch ***** -->
                        <xsl:when test="self::xforms:switch" xmlns:xforms="http://www.w3.org/2002/xforms">
                            <xsl:if test="$debug-enabled='true'">
                                <xsl:message>found switch</xsl:message>
                            </xsl:if>
                            <xsl:apply-templates select="."/>
                        </xsl:when>

                        <!-- **** handle trigger + submit ***** -->
                        <xsl:when test="self::xforms:trigger or self::xforms:submit" xmlns:xforms="http://www.w3.org/2002/xforms">
                            <xsl:if test="$debug-enabled='true'">
                                <xsl:message>handling trigger:
                                    <xsl:value-of select="xforms:label"/>
                                </xsl:message>
                            </xsl:if>
                            <xsl:variable name="css">
                                <xsl:call-template name="assembleClasses"/>
                            </xsl:variable>

                            <xsl:variable name="css"><xsl:call-template name="assembleClasses"/></xsl:variable>
                            <span class="{concat(local-name(),$css)}" id="{@id}">
                                <xsl:call-template name="buildControl"/>
                            </span>
                        </xsl:when>

                        <!-- **** handle xforms control ***** -->
                        <xsl:when test="self::xforms:*" xmlns:xforms="http://www.w3.org/2002/xforms">

<!--                            <xsl:if test="chiba:data/@chiba:enabled='true'">-->
                                <xsl:if test="$debug-enabled='true'">
                                    <xsl:message>handling control label</xsl:message>
                                    <xsl:message>
                                        <xsl:value-of select="name()"/>-
                                        <xsl:value-of select="xforms:label"/>
                                    </xsl:message>
                                </xsl:if>

                                <xsl:variable name="css"><xsl:call-template name="assembleClasses"/></xsl:variable>
                                <xsl:variable name="label-class"><xsl:call-template name="labelClasses"/></xsl:variable>
                                <span id="{@id}" class="{concat(local-name(),$css)}">
                                    <span id="{@id}-label" class="{$label-class}">
                                        <xsl:apply-templates select="xforms:label"/>
                                    </span>
                                    <xsl:if test="$debug-enabled='true'">
                                        <xsl:message>handling control</xsl:message>
                                        <xsl:message>
                                            <xsl:value-of select="name()"/>
                                        </xsl:message>
                                    </xsl:if>
                                    <xsl:call-template name="buildControl"/>
                                </span>
<!--                            </xsl:if>-->
                        </xsl:when>

                        <!-- **** handle chiba:data element ***** -->
                        <xsl:when test="self::chiba:data" xmlns:chiba="http://chiba.sourceforge.net/2003/08/xforms">
                            <xsl:if test="$debug-enabled='true'">
                                <xsl:message>ignoring chiba data element</xsl:message>
                            </xsl:if>
                        </xsl:when>

                        <!-- **** handle all other ***** -->
                        <xsl:otherwise>
                            <xsl:if test="$debug-enabled='true'">
                                <xsl:message>handling element:
                                    <xsl:value-of select="name()"/>
                                </xsl:message>
                            </xsl:if>
                            <xsl:copy>
                                <xsl:copy-of select="@*"/>
                                <xsl:apply-templates/>
                            </xsl:copy>
                        </xsl:otherwise>

                    </xsl:choose>
                </xsl:for-each>
            </div>
<!--        </xsl:if>-->
    </xsl:template>


    <!-- ###################################### COMPACT GROUP ################################################## -->
    <xsl:template match="xforms:group[@xforms:appearance='compact']">
        <xsl:if test="$debug-enabled='yes'">
            <xsl:message>found compact group
                <xsl:value-of select="xforms:label"/>...
            </xsl:message>
        </xsl:if>

        <!-- ***** ignore this group if not enabled ***** -->
<!--        <xsl:if test="not(chiba:data/@chiba:enabled='false')">-->

            <xsl:variable name="id" select="@id"/>
            <xsl:variable name="control-count" select="count(./*/xforms:label)"/>
            <xsl:variable name="group-css"><xsl:call-template name="assembleClasses"/></xsl:variable>
            <table class="{concat('compact-group',$group-css)}" id="{$id}">
                <!-- ***** build caption with column labels ***** -->
                <tr>
                    <td colspan="{$control-count}" class="compact-group-label">
                        <xsl:apply-templates select="xforms:label"/>
                    </td>
                </tr>
                <tr>
                    <xsl:for-each select="./*/xforms:label">
                        <xsl:variable name="label-class"><xsl:call-template name="labelClasses"/></xsl:variable>
                        <td id="{../@id}-label" class="{$label-class}">
                            <xsl:apply-templates select="self::node()[not(name(..)='xforms:trigger' or name(..)='xforms:submit')]"/>
                        </td>
                    </xsl:for-each>
                </tr>
                <tr>
                    <xsl:for-each select="*">
                        <xsl:choose>

                            <!-- **** handle group label ***** -->
                            <xsl:when test="self::xforms:label" xmlns:xforms="http://www.w3.org/2002/xforms">
                                <xsl:if test="$debug-enabled='true'">
                                    <xsl:message>ignoring group label ...</xsl:message>
                                </xsl:if>
                            </xsl:when>

                            <!-- **** handle group alert ***** -->
                            <xsl:when test="self::xforms:alert" xmlns:xforms="http://www.w3.org/2002/xforms">
                                <xsl:apply-templates select="xforms:alert"/>
                            </xsl:when>

                            <!-- **** handle sub group ***** -->
                            <xsl:when test="self::xforms:group" xmlns:xforms="http://www.w3.org/2002/xforms">
                                <td colspan="{$control-count}">
                                    <xsl:apply-templates select="."/>
                                </td>
                            </xsl:when>

                            <!-- **** handle repeat ***** -->
                            <xsl:when test="self::xforms:repeat" xmlns:xforms="http://www.w3.org/2002/xforms">
                                <td colspan="{$control-count}">
                                    <xsl:apply-templates select="."/>
                                </td>
                            </xsl:when>

                            <!-- **** handle switch ***** -->
                            <xsl:when test="self::xforms:switch" xmlns:xforms="http://www.w3.org/2002/xforms">
                                <xsl:if test="$debug-enabled='true'">
                                    <xsl:message>found switch</xsl:message>
                                </xsl:if>
                                <td colspan="{$control-count}">
                                    <xsl:apply-templates select="."/>
                                </td>
                            </xsl:when>

                            <!-- **** handle trigger + submit ***** -->
                            <xsl:when test="self::xforms:trigger or self::xforms:submit" xmlns:xforms="http://www.w3.org/2002/xforms">
                                <xsl:if test="$debug-enabled='true'">
                                    <xsl:message>handling trigger:
                                        <xsl:value-of select="xforms:label"/>
                                    </xsl:message>
                                </xsl:if>
                                <xsl:variable name="css"><xsl:call-template name="assembleClasses"/></xsl:variable>
                                <td class="{concat(local-name(),$css)}" id="{@id}">
<!--                                    <xsl:apply-templates select="."/>-->
                                    <xsl:call-template name="buildControl"/>
                                </td>
                            </xsl:when>

                            <!-- **** handle xforms control ***** -->
                            <xsl:when test="self::xforms:*" xmlns:xforms="http://www.w3.org/2002/xforms">
<!--                                <xsl:if test="not(chiba:data/@chiba:enabled='false')">-->
                                    <xsl:variable name="css"><xsl:call-template name="assembleClasses"/></xsl:variable>
                                    <td id="{@id}" class="{concat(local-name(),$css)}">
                                        <xsl:if test="$debug-enabled='true'">
                                            <xsl:message>handling control</xsl:message>
                                            <xsl:message>
                                                <xsl:value-of select="name()"/>
                                            </xsl:message>
                                        </xsl:if>
                                        <xsl:call-template name="buildControl"/>
                                    </td>
<!--                                </xsl:if>-->
                            </xsl:when>

                            <!-- **** handle chiba:data element ***** -->
                            <xsl:when test="self::chiba:data" xmlns:xforms="http://chiba.sourceforge.net/2003/08/xforms">
                                <xsl:if test="$debug-enabled='true'">
                                    <xsl:message>ignoring chiba data element</xsl:message>
                                </xsl:if>
                            </xsl:when>

                            <!-- **** handle all other ***** -->
                            <xsl:otherwise>
                                <xsl:if test="$debug-enabled='true'">
                                    <xsl:message>handling element:
                                        <xsl:value-of select="name()"/>
                                    </xsl:message>
                                </xsl:if>
                                <xsl:copy>
                                    <xsl:copy-of select="@*"/>
                                    <xsl:apply-templates/>
                                </xsl:copy>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:for-each>
                </tr>
            </table>
<!--        </xsl:if>-->
    </xsl:template>

    <!-- ###################################### FULL GROUP ################################################## -->
    <!-- handle group with apprearance 'full' - will render controls in a two-column table with labels on
    the left side. -->
    <xsl:template match="xforms:group" name="full-group">
        <xsl:if test="$debug-enabled='true'">
            <xsl:message>found full group (the default)
                <xsl:value-of select="xforms:label"/>...
            </xsl:message>
        </xsl:if>

        <!-- ***** ignore this group if not enabled ***** -->
<!--        <xsl:if test="not(chiba:data/@chiba:enabled='false')">-->

            <xsl:variable name="id" select="@id"/>
            <xsl:variable name="group-css"><xsl:call-template name="assembleClasses"/></xsl:variable>
            <table class="{concat('full-group',$group-css)}" id="{$id}" border="0">
                <!-- handling group children -->
                <xsl:for-each select="*">
                    <xsl:choose>

                        <!-- ***** build caption with column labels ***** -->
                        <xsl:when test="self::xforms:label" xmlns:xforms="http://www.w3.org/2002/xforms">
                            <xsl:if test="$debug-enabled='true'">
                                <xsl:message>handling group label ...</xsl:message>
                            </xsl:if>
                            <tr>
                                <td colspan="2" id="{@id}" class="full-group-label label">
                                    <xsl:apply-templates select="."/>
                                </td>
                            </tr>
                        </xsl:when>

                        <!-- **** handle group alert ***** -->
                        <xsl:when test="self::xforms:alert" xmlns:xforms="http://www.w3.org/2002/xforms">
                            <xsl:apply-templates select="xforms:alert"/>
                        </xsl:when>

                        <!-- **** handle sub group ***** -->
                        <xsl:when test="self::xforms:group" xmlns:xforms="http://www.w3.org/2002/xforms">
                            <tr>
                                <td colspan="2">
                                    <xsl:apply-templates select="."/>
                                </td>
                            </tr>
                        </xsl:when>

                        <!-- **** handle repeat ***** -->
                        <xsl:when test="self::xforms:repeat" xmlns:xforms="http://www.w3.org/2002/xforms">
                            <tr>
                                <td colspan="2">
                                    <xsl:apply-templates select="."/>
                                </td>
                            </tr>
                        </xsl:when>

                        <!-- **** handle switch ***** -->
                        <xsl:when test="self::xforms:switch" xmlns:xforms="http://www.w3.org/2002/xforms">
                            <xsl:if test="$debug-enabled='true'">
                                <xsl:message>found switch</xsl:message>
                            </xsl:if>
                            <tr>
                                <td colspan="2">
                                    <xsl:apply-templates select="."/>
                                </td>
                            </tr>
                        </xsl:when>

                        <!-- **** handle trigger + submit ***** -->
                        <xsl:when test="self::xforms:trigger or self::xforms:submit" xmlns:xforms="http://www.w3.org/2002/xforms">
                            <xsl:if test="$debug-enabled='true'">
                                <xsl:message>handling trigger:
                                    <xsl:value-of select="xforms:label"/>
                                </xsl:message>
                            </xsl:if>
                            <tr>
                                <xsl:variable name="css"><xsl:call-template name="assembleClasses"/></xsl:variable>
                                <td class="{concat(local-name(),$css)}" id="{@id}" colspan="2">
                                    <xsl:call-template name="buildControl"/>
                                </td>
                            </tr>
                        </xsl:when>

                        <!-- **** handle xforms control ***** -->
                        <xsl:when test="self::xforms:*" xmlns:xforms="http://www.w3.org/2002/xforms">
                            <xsl:if test="$debug-enabled='true'">
                                <xsl:message>element ->
                                    <xsl:value-of select="name(.)"/>
                                </xsl:message>
                            </xsl:if>

<!--                            <xsl:if test="not(chiba:data/@chiba:enabled='false')">-->
                                <tr>
                                    <xsl:variable name="label-class"><xsl:call-template name="labelClasses"/></xsl:variable>
                                    <td id="{@id}-label" class="{$label-class}">
                                        <xsl:if test="$debug-enabled='true'">
                                            <xsl:message>handling control label</xsl:message>
                                            <xsl:message>
                                                <xsl:value-of select="name()"/>-
                                                <xsl:value-of select="xforms:label"/>
                                            </xsl:message>
                                        </xsl:if>
                                        <xsl:apply-templates select="xforms:label"/>
                                    </td>

                                    <xsl:variable name="css"><xsl:call-template name="assembleClasses"/></xsl:variable>
                                    <td id="{@id}" class="{concat(local-name(),$css)}">
                                        <xsl:if test="$debug-enabled='true'">
                                            <xsl:message>handling control</xsl:message>
                                            <xsl:message>
                                                <xsl:value-of select="name()"/>
                                            </xsl:message>
                                        </xsl:if>
                                        <xsl:call-template name="buildControl"/>
                                    </td>
                                </tr>
<!--                            </xsl:if>-->
                        </xsl:when>

                        <!-- **** handle chiba:data element ***** -->
                        <xsl:when test="self::chiba:data" xmlns:xforms="http://chiba.sourceforge.net/2003/08/xforms">
                            <xsl:if test="$debug-enabled='true'">
                                <xsl:message>ignoring chiba data element</xsl:message>
                            </xsl:if>
                        </xsl:when>

                        <!-- **** handle all other ***** -->
                        <xsl:otherwise>
                            <xsl:if test="$debug-enabled='true'">
                                <xsl:message>handling element:
                                    <xsl:value-of select="name()"/>
                                </xsl:message>
                            </xsl:if>
                            <xsl:copy>
                                <xsl:copy-of select="@*"/>
                                <xsl:apply-templates/>
                            </xsl:copy>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:for-each>
            </table>
<!--        </xsl:if>-->
    </xsl:template>


    <!-- ######################################################################################################## -->
    <!-- ####################################### REPEAT ######################################################### -->
    <!-- ######################################################################################################## -->

    <!-- ### handle repeat with 'minimal' appearance ### -->
    <xsl:template match="xforms:repeat">
        <xsl:if test="$debug-enabled='true'">
            <xsl:message>found minimal repeat
                <xsl:value-of select="xforms:label"/>...
            </xsl:message>
        </xsl:if>

        <xsl:variable name="group-css"><xsl:call-template name="assembleClasses"/></xsl:variable>
        <div class="{concat('minimal-repeat',$group-css)}" id="{@id}">
            <xsl:if test="$scripted='true'">
                <!-- clone repeat prototype -->
                <span class="repeat-prototype">
                    <xsl:for-each select="chiba:data/xforms:group[@chiba:transient]/*">
                        <xsl:variable name="css"><xsl:call-template name="assembleClasses"/></xsl:variable>
                        <xsl:variable name="label-class"><xsl:call-template name="labelClasses"/></xsl:variable>
                        <span id="{@id}" class="{concat(local-name(),$css)}">
                            <span id="{@id}-label" class="{$label-class}">
                                <xsl:apply-templates select="./xforms:label"/>
                            </span>

                            <xsl:call-template name="buildControl"/>
                        </span>
                    </xsl:for-each>
                </span>
            </xsl:if>

            <xsl:variable name="outermost-id" select="ancestor-or-self::xforms:repeat/@id" xmlns:xforms="http://www.w3.org/2002/xforms"/>
            <xsl:variable name="repeat-id" select="@id"/>

            <!-- ***** loop repeat entries ***** -->
            <xsl:for-each select="xforms:group[@chiba:transient]">
                <xsl:if test="$debug-enabled='true'">
                    <xsl:message>found
                        <xsl:value-of select="name()"/>...
                    </xsl:message>
                    <xsl:message>found
                        <xsl:value-of select="xforms:label"/>...
                    </xsl:message>
                </xsl:if>

                <xsl:choose>
                    <xsl:when test="@chiba:selected='true'">
                        <span class="repeat-item repeat-index">
                            <xsl:if test="not($scripted='true')">
                                <span class="minimal-repeat-selector">
                                    <input type="radio" name="{$selector-prefix}{$outermost-id}" value="{$repeat-id}:{@chiba:position}" checked="checked"/>
                                </span>
                            </xsl:if>
                            <xsl:for-each select="*">
                                <xsl:variable name="css"><xsl:call-template name="assembleClasses"/></xsl:variable>
                                <xsl:variable name="label-class"><xsl:call-template name="labelClasses"/></xsl:variable>
                                <span id="{@id}" class="{concat(local-name(),$css)}">
                                    <span id="{@id}-label" class="{$label-class}">
                                        <xsl:apply-templates select="./xforms:label"/>
                                    </span>

                                    <xsl:call-template name="buildControl"/>
                                </span>
                            </xsl:for-each>
                        </span>
                    </xsl:when>
                    <xsl:otherwise>
                        <span class="repeat-item">
                            <xsl:if test="not($scripted='true')">
                                <span class="minimal-repeat-selector">
                                    <input type="radio" name="{$selector-prefix}{$outermost-id}" value="{$repeat-id}:{@chiba:position}"/>
                                </span>
                            </xsl:if>
                            <xsl:for-each select="*">
                                <xsl:variable name="css"><xsl:call-template name="assembleClasses"/></xsl:variable>
                                <xsl:variable name="label-class"><xsl:call-template name="labelClasses"/></xsl:variable>
                                <span id="{@id}" class="{concat(local-name(),$css)}">
                                    <span id="{@id}-label" class="{$label-class}">
                                        <xsl:apply-templates select="./xforms:label"/>
                                    </span>

                                    <xsl:call-template name="buildControl"/>
                                </span>
                            </xsl:for-each>
                        </span>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
        </div>
    </xsl:template>

    <!-- ### handle repeat with 'compact' appearance ### -->
    <xsl:template match="xforms:repeat[@xforms:appearance='compact']" priority="1">
        <xsl:if test="$debug-enabled='true'">
            <xsl:message>found compact repeat
                <xsl:value-of select="xforms:label"/>...
            </xsl:message>
        </xsl:if>

        <xsl:variable name="repeat" select="."/>
        <xsl:variable name="group-css"><xsl:call-template name="assembleClasses"/></xsl:variable>
        <table id="{@id}" class="{concat('compact-repeat',$group-css)}" border="0">
            <xsl:if test="$scripted='true'">
                <!-- clone repeat prototype -->
                <tr class="repeat-prototype">
                    <xsl:for-each select="chiba:data/xforms:group[@chiba:transient]/*">
                        <xsl:variable name="css"><xsl:call-template name="assembleClasses"/></xsl:variable>
                        <td class="{concat(local-name(),$css)}">
                            <xsl:call-template name="buildControl"/>
                        </td>
                    </xsl:for-each>
                </tr>
            </xsl:if>
            <tr class="compact-repeat-label">
                <xsl:if test="not($scripted='true')">
                    <!-- ***** build empty selector cell ***** -->
                    <td>&#160;</td>
                </xsl:if>
                <!-- ***** build header ***** -->
                <xsl:for-each select="chiba:data/xforms:group[1]/*/xforms:label">
                    <xsl:variable name="label-class"><xsl:call-template name="labelClasses"/></xsl:variable>
                    <td id="{../@id}-label" class="{$label-class}">
                        <xsl:apply-templates select="self::node()[not(name(..)='xforms:trigger' or name(..)='xforms:submit')]"/>
                    </td>
                </xsl:for-each>
            </tr>

            <xsl:variable name="outermost-id" select="ancestor-or-self::xforms:repeat/@id" xmlns:xforms="http://www.w3.org/2002/xforms"/>
            <xsl:variable name="repeat-id" select="@id"/>

            <xsl:for-each select="xforms:group[@chiba:transient]">
                <xsl:choose>
                    <xsl:when test="@chiba:selected='true'">
                        <tr class="repeat-item repeat-index">
                            <xsl:if test="not($scripted='true')">
                                <td class="selector-cell">
                                    <input type="radio" name="{$selector-prefix}{$outermost-id}" value="{$repeat-id}:{@chiba:position}" checked="checked"/>
                                </td>
                            </xsl:if>
                            <xsl:for-each select="*">
                                <xsl:variable name="css"><xsl:call-template name="assembleClasses"/></xsl:variable>
                                <td id="{@id}" class="{concat(local-name(),$css)}">
                                    <xsl:call-template name="buildControl"/>
                                </td>
                            </xsl:for-each>
                        </tr>
                    </xsl:when>
                    <xsl:otherwise>
                        <tr class="repeat-item">
                            <xsl:if test="not($scripted='true')">
                                <td class="selector-cell">
                                    <input type="radio" name="{$selector-prefix}{$outermost-id}" value="{$repeat-id}:{@chiba:position}"/>
                                </td>
                            </xsl:if>
                            <xsl:for-each select="*">
                                <xsl:variable name="css"><xsl:call-template name="assembleClasses"/></xsl:variable>
                                <td id="{@id}" class="{concat(local-name(),$css)}">
                                    <xsl:call-template name="buildControl"/>
                                </td>
                            </xsl:for-each>
                        </tr>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
        </table>
    </xsl:template>

    <!-- ### handle repeat with 'full' appearance ### -->
    <xsl:template match="xforms:repeat[@xforms:appearance='full']">
        <xsl:if test="$debug-enabled='true'">
            <xsl:message>found full repeat
                <xsl:value-of select="xforms:label"/>...
            </xsl:message>
        </xsl:if>

        <xsl:variable name="repeat" select="."/>
        <xsl:variable name="group-css"><xsl:call-template name="assembleClasses"/></xsl:variable>

        <table class="{concat('full-repeat',$group-css)}" id="{@id}">
            <xsl:if test="$scripted='true'">
                <!-- clone repeat prototype -->
                <tr class="repeat-prototype">
                    <xsl:for-each select="chiba:data/xforms:group[@chiba:transient]">
                        <td>
                            <xsl:call-template name="full-group"/>
                        </td>
                    </xsl:for-each>
                </tr>
            </xsl:if>

            <xsl:variable name="outermost-id" select="ancestor-or-self::xforms:repeat/@id" xmlns:xforms="http://www.w3.org/2002/xforms"/>
            <xsl:variable name="repeat-id" select="@id"/>

            <!-- ***** loop repeat entries ***** -->
            <xsl:for-each select="xforms:group[@chiba:transient]">
                <xsl:choose>
                    <xsl:when test="@chiba:selected='true'">
                        <tr class="repeat-item repeat-index">
                            <xsl:if test="not($scripted='true')">
                                <td class="selector-cell">
                                    <input type="radio" name="{$selector-prefix}{$outermost-id}" value="{$repeat-id}:{@chiba:position}" checked="checked"/>
                                </td>
                            </xsl:if>
                            <td>
                                <xsl:call-template name="full-group"/>
                            </td>
                        </tr>
                    </xsl:when>
                    <xsl:otherwise>
                        <tr class="repeat-item">
                            <xsl:if test="not($scripted='true')">
                                <td class="selector-cell">
                                    <input type="radio" name="{$selector-prefix}{$outermost-id}" value="{$repeat-id}:{@chiba:position}"/>
                                </td>
                            </xsl:if>
                            <td>
                                <xsl:call-template name="full-group"/>
                            </td>
                        </tr>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
        </table>
    </xsl:template>

    <!-- ### handle repeats attribute on foreign elements ### -->
    <xsl:template match="*[@xforms:repeat-bind]|*[@xforms:repeat-nodeset]">
        <xsl:apply-templates/>
    </xsl:template>

    <!-- ######################################################################################################## -->
    <!-- transient groups are created for clean handling of repeats; each each repeat entry will we wrapped into  -->
    <!-- a transient group. These can be used to detect the boundaries of a repeated item including its mixed     -->
    <!-- content. This template mainly handles the correct setting of the selected entry.                         -->
    <!-- ######################################################################################################## -->

    <xsl:template match="xforms:group[@chiba:transient='true']" priority="1">
        <xsl:if test="$debug-enabled='true'">
            <xsl:message>transient group</xsl:message>
        </xsl:if>

        <xsl:variable name="repeat-id" select="../@id"/>
        <xsl:variable name="repeat-index" select="../@chiba:index"/>
        <xsl:variable name="repeat-position" select="@chiba:position"/>
        <xsl:variable name="selected" select="boolean($repeat-index=$repeat-position)"/>

        <xsl:if test="$debug-enabled='true'">
            <xsl:message>repeat id:
                <xsl:value-of select="../@id"/>
            </xsl:message>
        </xsl:if>
        <xsl:choose>
            <xsl:when test="$repeat-index=$repeat-position">
                <span class="repeat-selected">
                    <span class="repeat-selector">
                        <input type="radio" name="{$selector-prefix}{$repeat-id}" value="{$repeat-position}" checked="checked"/>
                    </span>
                    <xsl:apply-templates/>
                </span>
            </xsl:when>
            <xsl:otherwise>
                <span class="repeat-deselected">
                    <span class="repeat-selector">
                        <input type="radio" name="{$selector-prefix}{$repeat-id}" value="{$repeat-position}"/>
                    </span>
                    <xsl:apply-templates/>
                </span>
            </xsl:otherwise>
        </xsl:choose>

    </xsl:template>

    <!-- ######################################################################################################## -->
    <!-- ####################################### SWITCH ######################################################### -->
    <!-- ######################################################################################################## -->

    <!-- ### handle xforms:switch ### -->
    <xsl:template match="xforms:switch">
        <xsl:if test="$debug-enabled='true'">
            <xsl:message>handling switch</xsl:message>
        </xsl:if>
        <xsl:apply-templates/>
    </xsl:template>

    <!-- ### handle selected xforms:case ### -->
    <xsl:template match="xforms:case[@xforms:selected='true']">
        <xsl:if test="$debug-enabled='true'">
            <xsl:message>handling selected case</xsl:message>
        </xsl:if>
        <xsl:apply-templates/>
    </xsl:template>

    <!-- ### skip unselected xforms:case ### -->
    <xsl:template match="xforms:case">
        <xsl:if test="$debug-enabled='true'">
            <xsl:message>handling unselected case</xsl:message>
        </xsl:if>
    </xsl:template>


    <!-- ######################################################################################################## -->
    <!-- #####################################  CONTROLS ######################################################## -->
    <!-- ######################################################################################################## -->

    <!-- ### handle xforms:input ### -->
    <xsl:template match="xforms:input">
        <xsl:call-template name="buildControl"/>
    </xsl:template>

    <!-- ### handle xforms:output ### -->
    <xsl:template match="xforms:output">
        <xsl:call-template name="buildControl"/>
    </xsl:template>

    <!-- ### handle xforms:range ### -->
    <xsl:template match="xforms:range">
        <xsl:call-template name="buildControl"/>
    </xsl:template>

    <!-- ### handle xforms:secret ### -->
    <xsl:template match="xforms:secret">
        <xsl:call-template name="buildControl"/>
    </xsl:template>

    <!-- ### handle xforms:select ### -->
    <xsl:template match="xforms:select">
        <xsl:call-template name="buildControl"/>
    </xsl:template>

    <!-- ### handle xforms:select1 ### -->
    <xsl:template match="xforms:select1">
        <xsl:call-template name="buildControl"/>
    </xsl:template>

    <!-- ### handle xforms:submit ### -->
    <xsl:template match="xforms:submit">
        <xsl:call-template name="buildControl"/>
    </xsl:template>

    <!-- ### handle xforms:trigger ### -->
    <xsl:template match="xforms:trigger">
        <xsl:call-template name="buildControl"/>
    </xsl:template>

    <!-- ### handle xforms:textarea ### -->
    <xsl:template match="xforms:textarea">
        <xsl:call-template name="buildControl"/>
    </xsl:template>

    <!-- ### handle xforms:upload ### -->
    <xsl:template match="xforms:upload">
        <xsl:call-template name="buildControl"/>
    </xsl:template>

    <!-- ### this template must be provided since its called from the imported html-form-controls.xsl ### -->
<!--
    <xsl:template name="eval-actions">
        <xsl:if test="xforms:insert|xforms:delete">
            <xsl:attribute name="onClick">javascript:
                <xsl:apply-templates select="xforms:insert|xforms:delete"/>return true;
            </xsl:attribute>
        </xsl:if>
    </xsl:template>
-->

    <!-- ### triggers must handle their label themselves ### -->
    <xsl:template match="xforms:trigger/xforms:label"/>

    <!-- ### handle label ### -->
    <xsl:template match="xforms:label">
        <xsl:variable name="group-id" select="ancestor::xforms:group[1]/@id"/>
        <xsl:variable name="img" select="@xforms:src"/>

        <xsl:choose>
            <xsl:when test="name(..)='xforms:group'">
                <xsl:apply-templates/>
            </xsl:when>
            <xsl:when test="name(..)='xforms:item'">
                <span id="{@id}" class="label">
                    <xsl:apply-templates/>
                </span>
            </xsl:when>
            <xsl:when test="boolean($img) and ( contains($img,'.gif') or contains($img,'.jpg') or contains($img,'.png') )">
                <img src="{$img}" id="{@id}-label" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- ### handle hint ### -->
    <xsl:template match="xforms:hint">
        <!--  already handled by individual controls in html-form-controls.xsl -->
    </xsl:template>

    <!-- ### handle help ### -->
    <!-- ### only reacts on help elements with a 'src' attribute and interprets it as html href ### -->
    <xsl:template match="xforms:help">
        ?
        <!--
                <span style="font-color:blue;vertical-align:top;padding-left:2px;font-weight:bold">
                    <xsl:if test="@xforms:src">
                        <a href="{@xforms:src}">?</a>
                    </xsl:if>
                </span>
        -->
        <!--        <img src="images/kasten_blau.gif"/>-->
        <!-- this implementation renders a button to display a javascript message -->
        <!--        <img src="images/help.gif" onClick="javascript:xf_help('{normalize-space(.)}');return true;"/>-->
    </xsl:template>

    <!-- ### handle explicitely enabled alert ### -->
    <!--    <xsl:template match="xforms:alert[../chiba:data/@chiba:valid='false']">-->
    <xsl:template match="xforms:alert">

<!--        <xsl:if test="../chiba:data/@chiba:valid='false'">-->
            <span id="{@id}" class="alert">
                <xsl:value-of select="."/>
            </span>
<!--        </xsl:if>-->

    </xsl:template>

    <!-- ### handle extensions ### -->
    <xsl:template match="xforms:extension">
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="chiba:selector">
    </xsl:template>


    <!-- ########################## ACTIONS ####################################################### -->
    <!-- these templates serve no real purpose here but are shown for reference what may be over-   -->
    <!-- written by customized stylesheets importing this one. -->
    <!-- ########################## ACTIONS ####################################################### -->

    <!-- action nodes are simply copied to output without any modification -->
    <xsl:template match="xforms:action">
        <xsl:apply-templates/>
    </xsl:template>
    <xsl:template match="xforms:dispatch"/>
    <xsl:template match="xforms:rebuild"/>
    <xsl:template match="xforms:recalculate"/>
    <xsl:template match="xforms:revalidate"/>
    <xsl:template match="xforms:refresh"/>
    <xsl:template match="xforms:setfocus"/>
    <xsl:template match="xforms:load"/>
    <xsl:template match="xforms:setvalue"/>
    <xsl:template match="xforms:send"/>
    <xsl:template match="xforms:reset"/>
    <xsl:template match="xforms:message"/>
    <xsl:template match="xforms:toggle"/>
    <xsl:template match="xforms:insert"/>
    <xsl:template match="xforms:delete"/>
    <xsl:template match="xforms:setindex"/>


    <!-- ####################################################################################################### -->
    <!-- #####################################  HELPER TEMPLATES '############################################## -->
    <!-- ####################################################################################################### -->

    <xsl:template name="buildControl">
        <xsl:if test="$debug-enabled='true'">
            <xsl:message>buildControl:
                <xsl:value-of select="name(.)"/>
            </xsl:message>
        </xsl:if>
        <xsl:choose>
            <xsl:when test="local-name()='input'">
                <xsl:call-template name="input"/>
                <xsl:apply-templates select="xforms:help"/>
                <xsl:apply-templates select="xforms:alert"/>
            </xsl:when>
            <xsl:when test="local-name()='output'">
                <xsl:call-template name="output"/>
                <xsl:apply-templates select="xforms:help"/>
                <xsl:apply-templates select="xforms:alert"/>
            </xsl:when>
            <xsl:when test="local-name()='range'">
                <xsl:apply-templates select="xforms:help"/>
                <xsl:apply-templates select="xforms:alert"/>
            </xsl:when>
            <xsl:when test="local-name()='secret'">
                <xsl:call-template name="secret"/>
                <xsl:apply-templates select="xforms:help"/>
                <xsl:apply-templates select="xforms:alert"/>
            </xsl:when>
            <xsl:when test="local-name()='select'">
                <xsl:call-template name="select"/>
                <xsl:apply-templates select="xforms:help"/>
                <xsl:apply-templates select="xforms:alert"/>
            </xsl:when>
            <xsl:when test="local-name()='select1'">
                <xsl:call-template name="select1"/>
                <xsl:apply-templates select="xforms:help"/>
                <xsl:apply-templates select="xforms:alert"/>
            </xsl:when>
            <xsl:when test="local-name()='submit'">
                <xsl:call-template name="submit"/>
                <xsl:apply-templates select="xforms:help"/>
                <xsl:apply-templates select="xforms:alert"/>
            </xsl:when>
            <xsl:when test="local-name()='trigger'">
                <xsl:call-template name="trigger"/>
                <xsl:apply-templates select="xforms:help"/>
                <xsl:apply-templates select="xforms:alert"/>
            </xsl:when>
            <xsl:when test="local-name()='textarea'">
                <xsl:call-template name="textarea"/>
                <xsl:apply-templates select="xforms:help"/>
                <xsl:apply-templates select="xforms:alert"/>
            </xsl:when>
            <xsl:when test="local-name()='upload'">
                <xsl:call-template name="upload"/>
                <xsl:apply-templates select="xforms:help"/>
                <xsl:apply-templates select="xforms:alert"/>
            </xsl:when>
            <xsl:when test="local-name()='repeat'">
                <xsl:apply-templates select="."/>
            </xsl:when>
            <xsl:when test="local-name()='group'">
                <xsl:apply-templates select="."/>
                <xsl:apply-templates select="xforms:help"/>
                <xsl:apply-templates select="xforms:alert"/>
            </xsl:when>
            <xsl:when test="local-name()='switch'">
                <xsl:apply-templates select="."/>
            </xsl:when>
        </xsl:choose>
    </xsl:template>

    <!-- chooses the CSS stylesheet to use -->
    <xsl:template name="getCSS">
        <xsl:if test="$debug-enabled='true'">
            <xsl:message>user agent:
                <xsl:value-of select="$user-agent"/>
            </xsl:message>
        </xsl:if>

        <xsl:choose>
            <!-- if there's a stylesheet linked plainly, then take this stylesheet. -->
            <xsl:when test="xhtml:link">
                <link rel="stylesheet" type="text/css" href="{xhtml:link/@href}"/>
            </xsl:when>
            <!-- if there's an chiba:style statement block....
            Attention: the user-agent must match (case-sensitive) to output a link element. Otherwise no stylesheet will be linked.
            -->
            <xsl:when test="chiba:style">
                <xsl:choose>
                    <xsl:when test="contains($user-agent,'IE')">
                        <link rel="stylesheet" type="text/css" href="{.//chiba:useragent[@test='IE']/@href}"/>
                    </xsl:when>
                    <xsl:when test="contains($user-agent,'Mozilla')">
                        <link rel="stylesheet" type="text/css" href="{.//chiba:useragent[@test='Mozilla']/@href}"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <link rel="stylesheet" type="text/css" href="{$default-css}"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <!--  if nothings present standard stylesheets for Mozilla and IE are choosen. -->
            <xsl:otherwise>
                <xsl:choose>
                    <xsl:when test="contains($user-agent,'IE')">
                        <link rel="stylesheet" type="text/css" href="{$ie-css}"/>
                    </xsl:when>
                    <xsl:when test="contains($user-agent,'Mozilla')">
                        <link rel="stylesheet" type="text/css" href="{$mozilla-css}"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <link rel="stylesheet" type="text/css" href="{$default-css}"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- ***** builds a string containing the correct css-classes reflecting UI-states like
    readonly/readwrite, enabled/disabled, valid/invalid ***** -->
    <xsl:template name="assembleClasses">

        <!-- only execute if there's a data element which is e.g. not the case for unbound groups -->
        <xsl:if test="chiba:data">
            <xsl:variable name="valid">
                <xsl:choose>
                    <xsl:when test="string-length(chiba:data) = 0 and chiba:data/@chiba:visited='false'">valid</xsl:when>
                    <xsl:when test="chiba:data/@chiba:valid='true'">valid</xsl:when>
                    <xsl:otherwise>invalid</xsl:otherwise>
                </xsl:choose>
            </xsl:variable>

            <xsl:variable name="readonly">
                <xsl:choose>
                    <xsl:when test="chiba:data/@chiba:readonly='true'">readonly</xsl:when>
                    <xsl:otherwise>readwrite</xsl:otherwise>
                </xsl:choose>
            </xsl:variable>

            <xsl:variable name="required">
                <xsl:choose>
                    <xsl:when test="chiba:data/@chiba:required='true'">required</xsl:when>
                    <xsl:otherwise>optional</xsl:otherwise>
                </xsl:choose>
            </xsl:variable>

            <xsl:variable name="enabled">
                <xsl:choose>
                    <xsl:when test="chiba:data/@chiba:enabled='true'">enabled</xsl:when>
                    <xsl:otherwise>disabled</xsl:otherwise>
                </xsl:choose>
            </xsl:variable>

            <xsl:value-of select="concat(' ',$valid,' ',$readonly,' ',$required, ' ', $enabled)"/>
        </xsl:if>
    </xsl:template>

    <xsl:template name="labelClasses">

        <!-- only execute if there's a data element which is e.g. not the case for unbound groups -->
        <xsl:choose>
            <xsl:when test="chiba:data">
                <xsl:variable name="enabled">
                    <xsl:choose>
                        <xsl:when test="chiba:data/@chiba:enabled='true'">enabled</xsl:when>
                        <xsl:otherwise>disabled</xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <xsl:value-of select="concat('label ',$enabled)"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="'label'"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="selectorName">
        <xsl:variable name="repeat-id" select="ancestor-or-self::xforms:repeat/@id" xmlns:xforms="http://www.w3.org/2002/xforms"/>
        <xsl:value-of select="concat($selector-prefix, $repeat-id)"/>
    </xsl:template>

</xsl:stylesheet>
