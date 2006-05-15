<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xforms="http://www.w3.org/2002/xforms"
    xmlns:chiba="http://chiba.sourceforge.net/2003/08/xforms"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    exclude-result-prefixes="chiba xforms xlink xsl">

    <xsl:variable name="data-prefix" select="'d_'"/>
    <xsl:variable name="trigger-prefix" select="'t_'"/>
    <xsl:variable name="remove-upload-prefix" select="'ru_'"/>
    <xsl:param name="scripted" select="'false'"/>

    <!-- change this to your ShowAttachmentServlet -->
    <xsl:variable name="show-attachment-action" select="'http://@baseurl.host@:@baseurl.port@/chiba-@chiba-version@/ShowAttachmentServlet'"/>
    
    <!-- This stylesheet contains a collection of templates which map XForms controls to HTML controls. -->
    <xsl:output method="html" indent="yes" omit-xml-declaration="yes"/>

    <!-- ######################################################################################################## -->
    <!-- This stylesheet serves as a 'library' for HTML form controls. It contains only named templates and may   -->
    <!-- be re-used in different layout-stylesheets to create the naked controls.                                 -->
    <!-- ######################################################################################################## -->

    <!-- build input control -->
    <xsl:template name="input">

        <xsl:variable name="repeat-id" select="ancestor::*[name(.)='xforms:repeat'][1]/@id"/>
        <xsl:variable name="pos" select="position()"/>
        <xsl:variable name="id" select="@id"/>

        <xsl:if test="$debug-enabled='true'">
            <xsl:message>###repeat-id: <xsl:value-of select="$repeat-id"/></xsl:message>
            <xsl:message>###has repeat-id: <xsl:value-of select="boolean(string-length($repeat-id) > 0)"/></xsl:message>
            <xsl:message>###position: <xsl:value-of select="position()"/></xsl:message>
        </xsl:if>

        <xsl:element name="input">
            <xsl:attribute name="id">
                <xsl:value-of select="concat($id,'-value')"/>
            </xsl:attribute>
            <xsl:attribute name="name">
                <xsl:value-of select="concat($data-prefix,$id)"/>
            </xsl:attribute>
            <xsl:attribute name="type">text</xsl:attribute>
            <xsl:attribute name="value">
                <xsl:value-of select="chiba:data/text()"/>
            </xsl:attribute>
            <xsl:attribute name="title">
                <xsl:value-of select="normalize-space(xforms:hint)"/>
            </xsl:attribute>
            <xsl:if test="chiba:data/@chiba:readonly='true'">
                <xsl:attribute name="disabled">disabled</xsl:attribute>
            </xsl:if>
            <xsl:call-template name="assembleRepeatClasses">
                <xsl:with-param name="repeat-id" select="$repeat-id"/>
                <xsl:with-param name="pos" select="$pos"/>
                <xsl:with-param name="classes" select="'value'"/>
            </xsl:call-template>
            <xsl:if test="$scripted='true'">
                <xsl:attribute name="onchange">javascript:setXFormsValue('<xsl:value-of select="$id"/>');</xsl:attribute>
            </xsl:if>
        </xsl:element>

        <xsl:call-template name="handleRequired"/>
    </xsl:template>

    <!-- build image trigger / submit -->
    <xsl:template name="image-trigger">
        <xsl:element name="input">
            <xsl:variable name="id" select="@id"/>
            <xsl:variable name="repeat-id" select="ancestor::*[name(.)='xforms:repeat'][1]/@id"/>
            <xsl:attribute name="id">
                <xsl:value-of select="concat($id,'-value')"/>
            </xsl:attribute>
            <xsl:attribute name="name">
                <xsl:value-of select="concat($trigger-prefix,$id)"/>
            </xsl:attribute>
            <xsl:attribute name="type">image</xsl:attribute>
            <xsl:attribute name="value">
                <xsl:value-of select="xforms:label"/>
            </xsl:attribute>
            <xsl:attribute name="title">
                <xsl:value-of select="normalize-space(xforms:hint)"/>
            </xsl:attribute>
            <xsl:attribute name="src">
                <xsl:value-of select="xforms:label/@xlink:href"/>
            </xsl:attribute>
            <xsl:attribute name="class">value</xsl:attribute>
            <xsl:if test="chiba:data/@chiba:readonly='true'">
                <xsl:attribute name="disabled">disabled</xsl:attribute>
            </xsl:if>
            <xsl:if test="$scripted='true'">
                <xsl:attribute name="onclick">javascript:activate('<xsl:value-of select="$id"/>');</xsl:attribute>
            </xsl:if>
        </xsl:element>

    </xsl:template>

    <!-- build output -->
    <xsl:template name="output">

        <xsl:variable name="css" select="@class"/>
        <xsl:choose>
            <xsl:when test="@xforms:appearance='minimal'">
                <xsl:value-of select="chiba:data/text()"/>
            </xsl:when>
	        <xsl:when test="@xforms:appearance='image'">
	        	<xsl:element name="img">
		            <xsl:attribute name="id">
		                <xsl:value-of select="concat(@id,'-value')"/>
		            </xsl:attribute>
		            <xsl:if test="$css">
		                <xsl:attribute name="class">
		                    <xsl:value-of select="$css"/>
		                </xsl:attribute>
		            </xsl:if>
		            <xsl:attribute name="src">
			            <xsl:value-of select="chiba:data/text()"/>
			        </xsl:attribute>
	        	</xsl:element>
	        </xsl:when>
            <xsl:when test="@xforms:appearance='anchor'">
                <xsl:element name="a">
                    <xsl:attribute name="id">
                        <xsl:value-of select="concat(@id,'-value')"/>
                    </xsl:attribute>
                    <xsl:if test="$css">
                        <xsl:attribute name="class">
                            <xsl:value-of select="$css"/>
                        </xsl:attribute>
                    </xsl:if>
                    <xsl:attribute name="href">
                        <xsl:value-of select="chiba:data/text()"/>
                    </xsl:attribute>
                    <xsl:value-of select="chiba:data/text()"/>
                </xsl:element>
            </xsl:when>
	        <xsl:otherwise>
		        <xsl:element name="span">
		            <xsl:attribute name="id">
		                <xsl:value-of select="concat(@id,'-value')"/>
		            </xsl:attribute>
		            <xsl:if test="$css">
		                <xsl:attribute name="class">
		                    <xsl:value-of select="$css"/>
		                </xsl:attribute>
		            </xsl:if>
		            <xsl:value-of select="chiba:data/text()"/>
		        </xsl:element>
	        </xsl:otherwise>
	    </xsl:choose>
    </xsl:template>

    <!-- build range -->
    <xsl:template name="range">
        <xsl:variable name="repeat-id" select="ancestor::*[name(.)='xforms:repeat'][1]/@id"/>
        <xsl:variable name="pos" select="position()"/>
        <xsl:variable name="id" select="@id"/>
		<xsl:variable name="start" select="@xforms:start"/>
		<xsl:variable name="end" select="@xforms:end"/>
		<xsl:variable name="step" select="@xforms:step"/>
        <xsl:variable name="showInput">
			<xsl:choose>
				<xsl:when test="@xforms:appearance='full'">true</xsl:when>
				<xsl:otherwise>false</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>


        <xsl:if test="$debug-enabled='true'">
            <xsl:message>WARN: range not supported yet</xsl:message>
        </xsl:if>


		<xsl:element name="script">
			<xsl:attribute name="language">JavaScript</xsl:attribute>
			createSlider2('<xsl:value-of select="$form-id"/>', '<xsl:value-of select="concat($id,'-value')"/>', '<xsl:value-of select="concat($data-prefix,$id)"/>', '<xsl:value-of select="$start"/>', '<xsl:value-of select="$end"/>', '<xsl:value-of select="$step"/>', <xsl:value-of select="$showInput"/>, "", "");
			setSlider('<xsl:value-of select="concat($data-prefix,$id)"/>', '<xsl:value-of select="chiba:data/text()"/>');
        </xsl:element>

        <xsl:call-template name="handleRequired"/>
    </xsl:template>

    <!-- build secret control -->
    <xsl:template name="secret">
        <xsl:param name="maxlength"/>

        <xsl:variable name="repeat-id" select="ancestor::*[name(.)='xforms:repeat'][1]/@id"/>
        <xsl:variable name="pos" select="position()"/>
        <xsl:variable name="id" select="@id"/>

        <xsl:element name="input">
            <xsl:attribute name="id">
                <xsl:value-of select="concat($id,'-value')"/>
            </xsl:attribute>
            <xsl:attribute name="name">
                <xsl:value-of select="concat($data-prefix,$id)"/>
            </xsl:attribute>
            <xsl:attribute name="type">password</xsl:attribute>
            <xsl:attribute name="value">
                <xsl:value-of select="chiba:data/text()"/>
            </xsl:attribute>
            <xsl:attribute name="title">
                <xsl:value-of select="normalize-space(./xforms:hint)"/>
            </xsl:attribute>
            <xsl:if test="$maxlength">
                <xsl:attribute name="maxlength">
                    <xsl:value-of select="$maxlength"/>
                </xsl:attribute>
            </xsl:if>
            <xsl:if test="chiba:data/@chiba:readonly='true'">
                <xsl:attribute name="disabled">disabled</xsl:attribute>
            </xsl:if>
            <xsl:call-template name="assembleRepeatClasses">
                <xsl:with-param name="repeat-id" select="$repeat-id"/>
                <xsl:with-param name="pos" select="$pos"/>
                <xsl:with-param name="classes" select="'value'"/>
            </xsl:call-template>
            <xsl:if test="$scripted='true'">
                <xsl:attribute name="onchange">javascript:setXFormsValue('<xsl:value-of select="$id"/>');</xsl:attribute>
            </xsl:if>
        </xsl:element>

        <xsl:call-template name="handleRequired"/>
    </xsl:template>


    <xsl:template name="select1">

        <xsl:variable name="repeat-id" select="ancestor::*[name(.)='xforms:repeat'][1]/@id"/>
        <xsl:variable name="pos" select="position()"/>
        <xsl:variable name="id" select="@id"/>

        <xsl:variable name="parent" select="."/>
        <xsl:choose>
            <xsl:when test="@xforms:appearance='compact'">
                <xsl:element name="select">
                    <xsl:attribute name="id">
                        <xsl:value-of select="concat($id,'-value')"/>
                    </xsl:attribute>
                    <xsl:attribute name="name">
                        <xsl:value-of select="concat($data-prefix,$id)"/>
                    </xsl:attribute>
                    <xsl:attribute name="size">5</xsl:attribute>
                    <xsl:attribute name="title">
                        <xsl:value-of select="normalize-space(./xforms:hint)"/>
                    </xsl:attribute>

                    <xsl:call-template name="assembleRepeatClasses">
                        <xsl:with-param name="repeat-id" select="$repeat-id"/>
                        <xsl:with-param name="pos" select="$pos"/>
                        <xsl:with-param name="classes" select="'value'"/>
                    </xsl:call-template>
                    <xsl:if test="chiba:data/@chiba:readonly='true'">
                        <xsl:attribute name="disabled">disabled</xsl:attribute>
                    </xsl:if>
                    <xsl:if test="$scripted='true'">
                        <xsl:attribute name="onchange">javascript:setXFormsValue('<xsl:value-of select="$id"/>');</xsl:attribute>
                    </xsl:if>
                    <xsl:call-template name="build-items">
                        <xsl:with-param name="parent" select="$parent"/>
                    </xsl:call-template>
                </xsl:element>
                <!-- create hidden parameter for deselection -->
                <input type="hidden" name="{concat($data-prefix,$id)}" value=""/>
            </xsl:when>
            <xsl:when test="@xforms:appearance='full'">
                <xsl:call-template name="build-radiobuttons">
                    <xsl:with-param name="id" select="$id"/>
                    <xsl:with-param name="name" select="concat($data-prefix,$id)"/>
                    <xsl:with-param name="parent" select="$parent"/>
                </xsl:call-template>
                <!-- create hidden parameter for identification and deselection -->
                <input type="hidden" id="{$id}-value" name="{concat($data-prefix,$id)}" value=""/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:element name="select">
                    <xsl:attribute name="id">
                        <xsl:value-of select="concat($id,'-value')"/>
                    </xsl:attribute>
                    <xsl:attribute name="name">
                        <xsl:value-of select="concat($data-prefix,$id)"/>
                    </xsl:attribute>
                    <xsl:attribute name="size">1</xsl:attribute>
                    <xsl:attribute name="title">
                        <xsl:value-of select="normalize-space(./xforms:hint)"/>
                    </xsl:attribute>
                    <xsl:call-template name="assembleRepeatClasses">
                        <xsl:with-param name="repeat-id" select="$repeat-id"/>
                        <xsl:with-param name="pos" select="$pos"/>
                        <xsl:with-param name="classes" select="'value'"/>
                    </xsl:call-template>
                    <xsl:if test="chiba:data/@chiba:readonly='true'">
                        <xsl:attribute name="disabled">disabled</xsl:attribute>
                    </xsl:if>
                    <xsl:if test="$scripted='true'">
                        <xsl:attribute name="onchange">javascript:setXFormsValue('<xsl:value-of select="$id"/>');</xsl:attribute>
                    </xsl:if>
                    <xsl:call-template name="build-items">
                        <xsl:with-param name="parent" select="$parent"/>
                    </xsl:call-template>
                </xsl:element>
                <!-- create hidden parameter for deselection -->
                <input type="hidden" name="{concat($data-prefix,$id)}" value=""/>
            </xsl:otherwise>
        </xsl:choose>

        <xsl:call-template name="handleRequired"/>
    </xsl:template>


    <xsl:template name="select">

        <xsl:variable name="repeat-id" select="ancestor::*[name(.)='xforms:repeat'][1]/@id"/>
        <xsl:variable name="pos" select="position()"/>
        <xsl:variable name="id" select="@id"/>

        <xsl:variable name="parent" select="."/>
        <xsl:choose>
            <xsl:when test="@xforms:appearance='compact'">
                <xsl:element name="select">
                    <xsl:attribute name="id">
                        <xsl:value-of select="concat($id,'-value')"/>
                    </xsl:attribute>
                    <xsl:attribute name="name">
                        <xsl:value-of select="concat($data-prefix,$id)"/>
                    </xsl:attribute>
                    <xsl:attribute name="title">
                        <xsl:value-of select="normalize-space(./xforms:hint)"/>
                    </xsl:attribute>
                    <xsl:attribute name="multiple">true</xsl:attribute>
                    <xsl:attribute name="size">5</xsl:attribute>
                    <xsl:if test="chiba:data/@chiba:readonly='true'">
                        <xsl:attribute name="disabled">disabled</xsl:attribute>
                    </xsl:if>
                    <xsl:attribute name="class">value</xsl:attribute>
                    <xsl:call-template name="assembleRepeatClasses">
                        <xsl:with-param name="repeat-id" select="$repeat-id"/>
                        <xsl:with-param name="pos" select="$pos"/>
                        <xsl:with-param name="classes" select="'value'"/>
                    </xsl:call-template>
                    <xsl:if test="$scripted='true'">
                        <xsl:attribute name="onchange">javascript:setXFormsValue('<xsl:value-of select="$id"/>');</xsl:attribute>
                    </xsl:if>
                    <xsl:call-template name="build-items">
                        <xsl:with-param name="value" select="chiba:data/text()"/>
                        <xsl:with-param name="parent" select="$parent"/>
                    </xsl:call-template>
                </xsl:element>
                <!-- create hidden parameter for deselection -->
                <input type="hidden" name="{concat($data-prefix,$id)}" value=""/>
            </xsl:when>
            <xsl:when test="@xforms:appearance='full'">
                <xsl:call-template name="build-checkboxes">
                    <xsl:with-param name="id" select="$id"/>
                    <xsl:with-param name="name" select="concat($data-prefix,$id)"/>
                    <xsl:with-param name="parent" select="$parent"/>
                </xsl:call-template>
                <!-- create hidden parameter for identification and deselection -->
                <input type="hidden" id="{$id}-value" name="{concat($data-prefix,$id)}" value=""/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:element name="select">
                    <xsl:attribute name="id">
                        <xsl:value-of select="concat($id,'-value')"/>
                    </xsl:attribute>
                    <xsl:attribute name="name">
                        <xsl:value-of select="concat($data-prefix,$id)"/>
                    </xsl:attribute>
                    <xsl:attribute name="title">
                        <xsl:value-of select="normalize-space(./xforms:hint)"/>
                    </xsl:attribute>
                    <xsl:attribute name="multiple">true</xsl:attribute>
                    <xsl:attribute name="size">3</xsl:attribute>
                    <xsl:if test="chiba:data/@chiba:readonly='true'">
                        <xsl:attribute name="disabled">disabled</xsl:attribute>
                    </xsl:if>

                    <xsl:call-template name="assembleRepeatClasses">
                        <xsl:with-param name="repeat-id" select="$repeat-id"/>
                        <xsl:with-param name="pos" select="$pos"/>
                        <xsl:with-param name="classes" select="'value'"/>
                    </xsl:call-template>
                    <xsl:if test="$scripted='true'">
                        <xsl:attribute name="onchange">javascript:setXFormsValue('<xsl:value-of select="$id"/>');</xsl:attribute>
                    </xsl:if>
                    <xsl:call-template name="build-items">
                        <xsl:with-param name="value" select="chiba:data/text()"/>
                        <xsl:with-param name="parent" select="$parent"/>
                    </xsl:call-template>
                </xsl:element>
                <!-- create hidden parameter for deselection -->
                <input type="hidden" name="{concat($data-prefix,$id)}" value=""/>
            </xsl:otherwise>
        </xsl:choose>

        <xsl:call-template name="handleRequired"/>
    </xsl:template>

    <!-- build textarea control -->
    <xsl:template name="textarea">
        <xsl:variable name="repeat-id" select="ancestor::*[name(.)='xforms:repeat'][1]/@id"/>
        <xsl:variable name="pos" select="position()"/>
        <xsl:variable name="id" select="@id"/>

        <xsl:element name="textarea">
            <xsl:attribute name="id">
                <xsl:value-of select="concat($id,'-value')"/>
            </xsl:attribute>
            <xsl:attribute name="name">
                <xsl:value-of select="concat($data-prefix,$id)"/>
            </xsl:attribute>
            <xsl:attribute name="title">
                <xsl:value-of select="normalize-space(./xforms:hint)"/>
            </xsl:attribute>
            <xsl:if test="chiba:data/@chiba:readonly='true'">
                <xsl:attribute name="disabled">disabled</xsl:attribute>
            </xsl:if>
            <xsl:call-template name="assembleRepeatClasses">
                <xsl:with-param name="repeat-id" select="$repeat-id"/>
                <xsl:with-param name="pos" select="$pos"/>
                <xsl:with-param name="classes" select="'value'"/>
            </xsl:call-template>
            <xsl:if test="$scripted='true'">
                <xsl:attribute name="onchange">javascript:setXFormsValue('<xsl:value-of select="$id"/>');</xsl:attribute>
            </xsl:if>
            <xsl:value-of select="chiba:data/text()"/>
        </xsl:element>

        <xsl:call-template name="handleRequired"/>
    </xsl:template>

    <!-- build submit -->
    <xsl:template name="submit">
        <xsl:variable name="repeat-id" select="ancestor::*[name(.)='xforms:repeat'][1]/@id"/>
        <xsl:variable name="pos" select="position()"/>
        <xsl:variable name="id" select="@id"/>

        <xsl:element name="input">
            <xsl:attribute name="id">
                <xsl:value-of select="concat($id,'-value')"/>
            </xsl:attribute>
            <xsl:choose>
                <xsl:when test="$scripted='true'">
                    <xsl:attribute name="type">button</xsl:attribute>
                    <xsl:attribute name="onclick">javascript:activate('<xsl:value-of select="$id"/>');</xsl:attribute>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:attribute name="type">submit</xsl:attribute>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:attribute name="name">
                <xsl:value-of select="concat($trigger-prefix,$id)"/>
            </xsl:attribute>
            <xsl:attribute name="value">
                <xsl:value-of select="xforms:label"/>
            </xsl:attribute>
            <xsl:attribute name="title">
                <xsl:value-of select="normalize-space(xforms:hint)"/>
            </xsl:attribute>
            <xsl:if test="chiba:data/@chiba:readonly='true'">
                <xsl:attribute name="disabled">disabled</xsl:attribute>
            </xsl:if>
            <!--            <xsl:if test="chiba:data/@chiba:enabled='false'">-->
            <!--                <xsl:attribute name="disabled">true</xsl:attribute>-->
            <!--            </xsl:if>-->
            <xsl:call-template name="assembleRepeatClasses">
                <xsl:with-param name="repeat-id" select="$repeat-id"/>
                <xsl:with-param name="pos" select="$pos"/>
                <xsl:with-param name="classes" select="'value'"/>
            </xsl:call-template>

        </xsl:element>
    </xsl:template>

    <!-- build trigger -->
    <!-- ### please note that triggers are always submit buttons cause this stylesheet assumes no javascript ### -->
    <xsl:template name="trigger">
        <xsl:variable name="repeat-id" select="ancestor::*[name(.)='xforms:repeat'][1]/@id"/>
        <xsl:variable name="pos" select="position()"/>
        <xsl:variable name="id" select="@id"/>

        <xsl:element name="input">
            <xsl:attribute name="id">
                <xsl:value-of select="concat($id,'-value')"/>
            </xsl:attribute>
            <xsl:attribute name="name">
                <xsl:value-of select="concat($trigger-prefix,$id)"/>
            </xsl:attribute>
            <xsl:choose>
                <xsl:when test="$scripted='true'">
                    <xsl:attribute name="type">button</xsl:attribute>
                    <xsl:attribute name="onclick">javascript:activate('<xsl:value-of select="$id"/>');</xsl:attribute>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:attribute name="type">submit</xsl:attribute>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:attribute name="value">
                <xsl:value-of select="xforms:label"/>
            </xsl:attribute>
            <xsl:attribute name="title">
                <xsl:value-of select="normalize-space(xforms:hint)"/>
            </xsl:attribute>
            <xsl:call-template name="assembleRepeatClasses">
                <xsl:with-param name="repeat-id" select="$repeat-id"/>
                <xsl:with-param name="pos" select="$pos"/>
                <xsl:with-param name="classes" select="'value'"/>
            </xsl:call-template>
            <xsl:if test="chiba:data/@chiba:readonly='true'">
                <xsl:attribute name="disabled">disabled</xsl:attribute>
            </xsl:if>
            <!--            <xsl:if test="chiba:data/@chiba:enabled='false'">-->
            <!--                <xsl:attribute name="disabled">true</xsl:attribute>-->
            <!--            </xsl:if>-->
            <xsl:if test="@xforms:accesskey">
                <xsl:attribute name="accesskey">
                    <xsl:value-of select="@xforms:accesskey"/>
                </xsl:attribute>
                <xsl:attribute name="title"><xsl:value-of select="normalize-space(xforms:hint)"/> - KEY: [ALT]+<xsl:value-of select="@xforms:accesskey"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="contains(@xforms:src,'.gif') or contains(@xforms:src,'.jpg') or contains(@xforms:src,'.png')">
                <img src="{@xforms:src}" id="{@id}-label"/>
            </xsl:if>

        </xsl:element>

    </xsl:template>

    <!-- build upload control -->
    <xsl:template name="upload">
        <!-- the stylesheet using this template has to take care, that form enctype is set to 'multipart/form-data' -->
        <xsl:variable name="repeat-id" select="ancestor::*[name(.)='xforms:repeat'][1]/@id"/>
        <xsl:variable name="pos" select="position()"/>
        <xsl:variable name="id" select="@id"/>

	<xsl:choose>
	    <xsl:when test="not(xforms:filename/chiba:data/text() = '')">	      
              <span class="upload-label label">
                  <a href="{concat($show-attachment-action,'?id=',$id)}" class="upload-show">
                     <xsl:value-of select="xforms:filename/chiba:data/text()"/>
                  </a>
              </span>
	      <!-- form handler must know about the remove-upload-prefix and handle it -->
              <input type="submit" class="upload-remove value" name="{concat($remove-upload-prefix,$id)}" value="Remove"/>
	    </xsl:when>
	    <xsl:otherwise>
		<xsl:element name="input">
		    <xsl:attribute name="id">
			<xsl:value-of select="concat($id,'-value')"/>
		    </xsl:attribute>
		    <xsl:attribute name="name">
			<xsl:value-of select="concat($data-prefix,$id)"/>
		    </xsl:attribute>
		    <xsl:attribute name="type">file</xsl:attribute>
		    <xsl:attribute name="value"></xsl:attribute>
		    <xsl:attribute name="title">
			<xsl:value-of select="normalize-space(xforms:hint)"/>
		    </xsl:attribute>
		    <xsl:if test="chiba:data/@chiba:readonly='true'">
			<xsl:attribute name="disabled">disabled</xsl:attribute>
		    </xsl:if>

		    <xsl:call-template name="assembleRepeatClasses">
			<xsl:with-param name="repeat-id" select="$repeat-id"/>
			<xsl:with-param name="pos" select="$pos"/>
			<xsl:with-param name="classes" select="'value'"/>
		    </xsl:call-template>

		    <!-- Content types accepted, from mediatype xforms:upload attribute
		    to accept input attribute -->
		    <xsl:attribute name="accept">
			<xsl:value-of select="translate(normalize-space(@xforms:mediatype),' ',',')"/>
		    </xsl:attribute>
		    <xsl:if test="$scripted='true'">
			<xsl:choose>
			    <xsl:when test="@xforms:onchange">
				<xsl:attribute name="onchange">
				    <xsl:value-of select="@xforms:onchange"/>
				</xsl:attribute>
			    </xsl:when>
			    <xsl:otherwise>
				<xsl:attribute name="onchange">javascript:upload('<xsl:value-of select="$id"/>');</xsl:attribute>
			    </xsl:otherwise>
			</xsl:choose>
		    </xsl:if>
		</xsl:element>
		<xsl:if test="xforms:filename">
		    <input type="hidden" id="{xforms:filename/@id}" value="{xforms:filename/chiba:data}"/>
		</xsl:if>
		<xsl:if test="@chiba:destination">
		    <!-- create hidden parameter for destination -->
		    <input type="hidden" id="{$id}-destination" value="{@chiba:destination}"/>
		</xsl:if>
	    </xsl:otherwise>
	  </xsl:choose>

        <xsl:call-template name="handleRequired"/>
    </xsl:template>


    <!-- ######################################################################################################## -->
    <!-- ########################################## HELPER TEMPLATES FOR SELECT, SELECT1 ######################## -->
    <!-- ######################################################################################################## -->

    <xsl:template name="build-items">
        <xsl:param name="parent"/>

        <!-- add an empty item, cause otherwise deselection is not possible -->
        <option value="">
            <xsl:if test="string-length($parent/chiba:data/text()) = 0">
                <xsl:attribute name="selected">selected</xsl:attribute>
            </xsl:if>
        </option>

        <!-- todo: handle xforms:choice -->
        <xsl:variable name="items" select="$parent//xforms:item[not(ancestor::chiba:data)]"/>
        <xsl:for-each select="$items">
            <option id="{@id}-value" value="{xforms:value}" title="{xforms:hint}">
                <xsl:if test="@xforms:selected='true'">
                    <xsl:attribute name="selected">selected</xsl:attribute>
                </xsl:if>
                <xsl:value-of select="xforms:label"/>
            </option>
        </xsl:for-each>
    </xsl:template>

    <!-- overwrite/change this template, if you don't like the way labels are rendered for checkboxes -->
    <xsl:template name="build-checkboxes">
        <xsl:param name="id"/>
        <xsl:param name="name"/>
        <xsl:param name="parent"/>

        <!-- todo: handle xforms:choice -->
        <xsl:variable name="items" select="$parent//xforms:item[not(ancestor::chiba:data)]"/>
        <xsl:for-each select="$items">
            <xsl:variable name="title">
                <xsl:choose>
                    <xsl:when test="xforms:hint">
                        <xsl:value-of select="xforms:hint"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="$parent/xforms:hint"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:variable>
            <input id="{@id}-value" class="value" type="checkbox" name="{$name}" value="{xforms:value}" title="{$title}">
                <xsl:if test="$parent/chiba:data/@chiba:readonly='true'">
                    <xsl:attribute name="disabled">disabled</xsl:attribute>
                </xsl:if>
                <xsl:if test="@xforms:selected='true'">
                    <xsl:attribute name="checked">checked</xsl:attribute>
                </xsl:if>
                <xsl:if test="$scripted='true'">
                    <xsl:attribute name="onclick">javascript:setXFormsValue('<xsl:value-of select="$parent/@id"/>');</xsl:attribute>
                </xsl:if>
            </input>
            <span id="{@id}-label" class="label">
                <xsl:if test="$parent/chiba:data/@chiba:readonly='true'">
                    <xsl:attribute name="disabled">disabled</xsl:attribute>
                </xsl:if>
                <xsl:apply-templates select="xforms:label"/>
            </span>
        </xsl:for-each>
    </xsl:template>

    <!-- overwrite/change this template, if you don't like the way labels are rendered for checkboxes -->
    <xsl:template name="build-radiobuttons">
        <xsl:param name="id"/>
        <xsl:param name="name"/>
        <xsl:param name="parent"/>

        <!-- todo: handle xforms:choice -->
        <xsl:variable name="items" select="$parent//xforms:item[not(ancestor::chiba:data)]"/>
        <xsl:for-each select="$items">
            <xsl:variable name="title">
                <xsl:choose>
                    <xsl:when test="xforms:hint">
                        <xsl:value-of select="xforms:hint"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="$parent/xforms:hint"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:variable>
            <input id="{@id}-value" class="value" type="radio" name="{$name}" value="{xforms:value}" title="{$title}">
                <xsl:if test="$parent/chiba:data/@chiba:readonly='true'">
                    <xsl:attribute name="disabled">disabled</xsl:attribute>
                </xsl:if>
                <xsl:if test="@xforms:selected='true'">
                    <xsl:attribute name="checked">checked</xsl:attribute>
                </xsl:if>
                <xsl:if test="$scripted='true'">
                    <xsl:attribute name="onclick">javascript:setXFormsValue('<xsl:value-of select="$parent/@id"/>');</xsl:attribute>
                </xsl:if>
            </input>
            <span id="{@id}-label" class="label">
                <xsl:if test="$parent/chiba:data/@chiba:readonly='true'">
                    <xsl:attribute name="disabled">disabled</xsl:attribute>
                </xsl:if>
                <xsl:apply-templates select="xforms:label"/>
            </span>
        </xsl:for-each>
    </xsl:template>

    <!-- handles required/optional property -->
    <xsl:template name="handleRequired">
        <xsl:choose>
            <xsl:when test="chiba:data/@chiba:required='true'">
                <span id="{@id}-required" class="required-symbol">*</span>
            </xsl:when>
            <xsl:otherwise>
                <span id="{@id}-required" class="required-symbol"></span>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- ########## builds indexed classname for styling repeats rendered as tables ########## -->
    <xsl:template name="assembleRepeatClasses">
        <xsl:param name="repeat-id"/>
        <xsl:param name="pos"/>
        <xsl:param name="classes"/>
        <xsl:choose>
            <xsl:when test="boolean(string-length($repeat-id) > 0)">
                <xsl:attribute name="class">
                    <xsl:value-of select="concat($repeat-id,'-',$pos,' ',$classes)"/>
                </xsl:attribute>
            </xsl:when>
            <xsl:when test="boolean(string-length(@class) > 0)">
                <xsl:attribute name="class">
                    <xsl:value-of select="concat(@class, ' ',$classes)"/>
                </xsl:attribute>
            </xsl:when>
            <xsl:otherwise>
                <xsl:attribute name="class">
                    <xsl:value-of select="$classes"/>
                </xsl:attribute>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>
