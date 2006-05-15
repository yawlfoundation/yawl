<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:import href="simplelayout.xsl"/>

    <xsl:output method="html" encoding="ISO-8859-1" omit-xml-declaration="yes"/>
    <xsl:param name="upDate" select="'00.00.00'"/>

    <xsl:template match="page">
    	<html>
    		<head>
                <title><xsl:value-of select="@title"/></title>
                <link rel="stylesheet" type="text/css" href="web/styles/chiba-styles.css"/>
    		</head>
        	<body bgcolor="#aabbdd">
                <xsl:apply-templates />
                <div align="right"><xsl:value-of disable-output-escaping="yes" select="'&amp;copy;'"/> 2003 Chiba Project</div>
                <div align="right">last update: <xsl:value-of select="string($upDate)"/> GMT+1</div>
                <div align="right">author: <a href="mailto:joernt@users.sourceforge.net">joernt</a>/<a href="mailto:unl@users.sourceforge.net">unl</a></div>
    		</body>
    	</html>
    </xsl:template>

</xsl:stylesheet>
