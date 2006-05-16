<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:import href="simplelayout.xsl"/>

    <xsl:output method="html" encoding="ISO-8859-1" omit-xml-declaration="yes"/>
    <xsl:param name="upDate" select="'00.00.00'"/>

    <xsl:template match="page">
    	<html>
    		<head>
                <title><xsl:value-of select="@title"/></title>
                <link rel="stylesheet" type="text/css" href="styles/chiba-styles.css"/>
    		</head>
        	<body bgcolor="#aabbdd">
                <table cellpadding="0" cellspacing="0" border="0" width="100%">
                    <tbody>
                        <tr>
                            <td valign="Top" width="15%"><a href="http://chiba.sourceforge.net"><img src="images/chiba50t.gif" border="0" vspace="0" alt="Chiba Logo" width="113" height="39" /></a><br/></td>
                            <td valign="Middle">
                                <font face="sans-serif">
                                    <div align="Right">
                                    &lt;<a href="index.html">Home</a>/&gt;
                                    &lt;<a href="installation.html">Installation</a>/&gt;
                                    &lt;<a href="features.html">Status</a>/&gt;
                                    &lt;<a href="api/index.html">Javadoc</a>/&gt;
                                    &lt;<a href="test/index.html">Test Results</a>&gt;
                                    &lt;<a href="http://sourceforge.net/mail?group_id=20274">Mailinglist</a>/&gt;
                                    &lt;<a href="http://sourceforge.net/project/showfiles.php?group_id=20274">Download</a>/&gt;<br/>
                                    </div>
                                </font>
                            </td>
                        </tr>
                    </tbody>
                </table>
                <hr width="100%" size="2"/>
                <xsl:choose>
                    <xsl:when test="/page/@id='donation'">
                        <table border="0">
                            <tr>
                                <td align="center" valign="top" class="donate">
                                        <img src="images/x-click-but04.gif" disabled="true" />
                                    <span id="donation-text">
                                    Support Chiba!
                                    </span>

                                </td>
                                <td class="content-area" valign="top">
                                    <xsl:apply-templates/>
                                </td>
                            </tr>
                        </table>
                    </xsl:when>
                    <xsl:otherwise>
                        <table border="0">
                            <tr>
                                <td align="center" valign="top" class="donate">
                                        <a href="donate.html"><img class="donation-button" src="images/x-click-but04.gif" /></a>
                                    <span id="donation-text">
                                    Support Chiba!
                                    </span>

                                </td>
                                <td class="content-area" valign="top">
                                    <xsl:apply-templates/>
                                </td>
                            </tr>
                        </table>
                    </xsl:otherwise>
                </xsl:choose>
                <hr width="100%" size="2"/>
                <font face="sans-serif" size="-1">
                <center>
                    Hosted by <a href="http://sourceforge.net"><img alt="SourceForge Logo" border="0" height="31" src="http://sourceforge.net/sflogo.php?group_id=23211&amp;type=1" width="88"/></a>
                </center>
                    <div align="right"><xsl:value-of disable-output-escaping="yes" select="'&amp;copy;'"/> 2003, 2004 Chiba Project</div>
                    <div align="right">last update: <xsl:value-of select="string($upDate)"/> GMT+1</div>
                    <div align="right">author: <a href="mailto:joernt@users.sourceforge.net">joernt</a>/<a href="mailto:unl@users.sourceforge.net">unl</a></div>
                </font>
    		</body>
    	</html>
    </xsl:template>

</xsl:stylesheet>
