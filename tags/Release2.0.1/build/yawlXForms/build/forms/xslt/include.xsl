<?xml version="1.0" encoding="UTF-8"?>
<!-- Copyright 2006 Chibacon, $Id: include.xsl,v 1.1 2006/03/09 23:45:01 joernt Exp $ -->
<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:chiba="http://chiba.sourceforge.net/xforms"
    xmlns:xalan="http://xml.apache.org/xalan"
    exclude-result-prefixes="chiba xsl">

    <xsl:param name="rootDir" select="'../'"/>

    <!--
    Simple Stylesheet to assemble XForms documents from markup found in other files.

    Syntax for includes:
    [1] <chiba:include src="[path]#[id]/>

    where [path] is the relative path to the file to be included (basedir is determined by $rootDir global var)
          [id] is some element in the file identified by [filename] that has a matching id Attribute

    [2] <chiba:include src="[path]" xpath="[xpathexpr]" />

    where [path] is the relative path to the file to be included (basedir is determined by $rootDir global var)
          [xpathexpr] is an arbitrary xpath statement to be evaluated on [filename] the included nodes

    Note: for [2] no fragment (denoted by '#') should be used and will have no effect. If an 'xpath' Attribute is
    present, the XPath evaluation will always take precedence over an eventually also existing fragment id.
    -->

    <xsl:template match="/">
        <xsl:copy>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <xsl:apply-templates />
        </xsl:copy>
    </xsl:template>

    <xsl:template match="chiba:include[@xpath]">
        <xsl:variable name="file" select="concat($rootDir,@src)"/>

        <xsl:apply-templates select="xalan:evaluate(concat('document($file)',@xpath))"/>
    </xsl:template>

    <xsl:template match="chiba:include">
        <xsl:variable name="file" select="concat($rootDir,substring-before(@src,'#'))"/>
        <xsl:variable name="fragmentId" select="substring-after(@src,'#')"/>

        <xsl:apply-templates select="document($file)//*[@id=$fragmentId]"/>
    </xsl:template>

</xsl:stylesheet>
