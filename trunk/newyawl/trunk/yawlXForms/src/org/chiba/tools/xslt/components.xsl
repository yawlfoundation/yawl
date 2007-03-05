<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
    xmlns:xhtml="http://www.w3.org/2002/06/xhtml2"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xforms="http://www.w3.org/2002/xforms"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:chiba="http://chiba.sourceforge.net/2003/08/xforms"
    exclude-result-prefixes="xhtml xforms chiba xlink">


    <!-- ####################################################################################################### -->
    <!-- contains some convenience components for html authoring.                                   -->
    <!-- author: joern turner                                                                                    -->
    <!-- ####################################################################################################### -->

    <!-- ####################################### TABLE STYLES ################################################## -->
    <!-- ### convenience template to build a html table from a repeat                                        ### -->
    <!-- ### Attention: this template assumes that the repeat is not nested and uses no mixed markup ### -->
    <xsl:template match="xforms:repeat[@xforms:appearance='compact']" priority="1">
        <xsl:variable name="repeat" select="."/>
        <table class="repeat-table">
            <tr class="repeat-table-header">
                <!-- build selector cell -->
                <td class="repeat-selector-header">
                </td>
                <!-- build header -->
                <xsl:for-each select="./xforms:group[1]/*/xforms:label">
                    <td class="repeat-table-header-cell-{../@id} repeat-table-header-cell">
                        <xsl:apply-templates select="."/>
                    </td>
                </xsl:for-each>
            </tr>
            <xsl:for-each select="xforms:group[@chiba:transient]">
                <xsl:message>index:
                    <xsl:value-of select="../@chiba:index"/>
                </xsl:message>
                <xsl:message>position:
                    <xsl:value-of select="../@chiba:position"/>
                </xsl:message>

                <xsl:choose>
                    <xsl:when test="number(../@chiba:index)=number(@chiba:position)">
                        <xsl:message>*** selected ***</xsl:message>

                        <tr class="selected-repeat-row">
                            <td class="repeat-selector">
                                <span class="repeat-selector">
                                    <input type="radio" name="{$selector-prefix}{$repeat/@id}" value="{@chiba:position}" checked="true"/>
                                </span>
                            </td>
                            <!-- process repeat entry children -->
                            <xsl:for-each select="*">
                                <td class="repeat-cell">
                                    <!--                                    <xsl:apply-templates select="."/>-->
                                    <xsl:call-template name="buildControl"/>
                                </td>
                            </xsl:for-each>
                        </tr>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:message>*** unselected ***</xsl:message>

                        <tr class="unselected-repeat-row">
                            <td>
                                <span class="repeat-selector">
                                    <input type="radio" name="{$selector-prefix}{$repeat/@id}" value="{@chiba:position}"/>
                                </span>
                            </td>
                            <!-- process repeat entry children -->
                            <xsl:for-each select="*">
                                <td class="repeat-cell-./@id repeat-cell">
                                    <!--                                    <xsl:apply-templates select="."/>-->
                                    <xsl:call-template name="buildControl"/>
                                </td>
                            </xsl:for-each>
                        </tr>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
        </table>
    </xsl:template>

    <xsl:template name="buildControl">
        <xsl:message>
            <xsl:value-of select="name(.)"/>
        </xsl:message>
        <xsl:choose>
            <xsl:when test="local-name()='input'">
                <xsl:call-template name="input"/>
            </xsl:when>
            <xsl:when test="local-name()='output'">
                <xsl:value-of select="chiba:data/text()"/>
            </xsl:when>
            <xsl:when test="local-name()='range'">
            </xsl:when>
            <xsl:when test="local-name()='secret'">
                <xsl:call-template name="secret"/>
            </xsl:when>
            <xsl:when test="local-name()='select'">
                <xsl:call-template name="select"/>
            </xsl:when>
            <xsl:when test="local-name()='select1'">
                <xsl:call-template name="select1"/>
            </xsl:when>
            <xsl:when test="local-name()='submit'">
                <xsl:call-template name="submit"/>
            </xsl:when>
            <xsl:when test="local-name()='trigger'">
                <xsl:call-template name="trigger"/>
            </xsl:when>
            <xsl:when test="local-name()='textarea'">
                <xsl:call-template name="textarea"/>
            </xsl:when>
            <xsl:when test="local-name()='upload'">
                <xsl:call-template name="upload"/>
            </xsl:when>
        </xsl:choose>
    </xsl:template>

    <!-- ####################################### SWITCH STYLES ################################################## -->
    <!-- renders a switch as registercard control with one card for every case found.  -->
    <xsl:template match="xforms:switch[xforms:appearance='registercard']">
        <xsl:variable name="cases" select="count(xforms:case)"/>
        <table class="registercards">
            <!-- the register tabs -->
            <tr>
                <xsl:for-each select="xforms:case">
                    <td class="register">
                    </td>
                </xsl:for-each>
            </tr>
            <tr>
                <td colspan="{$cases}">

                </td>
            </tr>
        </table>
    </xsl:template>

    <!-- renders a switch as a wizard with previous and next buttons to navigate to the next/previous card -->
    <xsl:template match="xforms:switch[xforms:appearance='wizard']">

    </xsl:template>
</xsl:stylesheet>

