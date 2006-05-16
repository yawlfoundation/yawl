<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output method="html" encoding="ISO-8859-1" omit-xml-declaration="yes"/>

    <xsl:param name="upDate" select="'00.00.00'"/>

    <xsl:template match="page">
        <html>
            <head>
                <title>
                    <xsl:value-of select="@title"/>
                </title>
                <link rel="stylesheet" type="text/css" href="styles/chiba-styles.css"/>
            </head>
            <body bgcolor="#aabbdd">
                <table cellpadding="0" cellspacing="0" border="0" width="100%">
                    <tbody>
                        <tr>
                            <td valign="Top" width="15%">
                                <a href="http://chiba.sourceforge.net">
                                    <img src="images/chiba50t.gif" border="0" vspace="0" alt="Chiba Logo" width="113" height="39"/>
                                </a>
                                <br/>
                            </td>
                            <td valign="Middle">
                                <font face="sans-serif">
                                    <div align="Right">
                                        &lt;
                                        <a href="index.html">Home</a>/&gt;
                                        &lt;
                                        <a href="installation.html">Installation</a>/&gt;
                                        &lt;
                                        <a href="jsp/forms.jsp">Samples</a>/&gt;
                                        &lt;
                                        <a href="features.html">Status</a>/&gt;
                                        &lt;
                                        <a href="api/index.html">Javadoc</a>/&gt;
                                        &lt;
                                        <a href="http://sourceforge.net/mail?group_id=20274">Mailinglist</a>/&gt;
                                        &lt;
                                        <a href="http://sourceforge.net/project/showfiles.php?group_id=20274">Download</a>/&gt;
                                        <br/>
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
                                    <img src="images/x-click-but04.gif" disabled="true"/>
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
                                    <a href="donate.html">
                                        <img class="donation-button" src="images/x-click-but04.gif"/>
                                    </a>
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
                    Hosted by
                        <a href="http://sourceforge.net">
                            <img alt="SourceForge Logo" border="0" height="31" src="http://sourceforge.net/sflogo.php?group_id=23211&amp;type=1" width="88"/>
                        </a>
                    </center>
                    <div align="right">
                        <xsl:value-of disable-output-escaping="yes" select="'&amp;copy;'"/> 2003, 2004 Chiba Project
                    </div>
                    <div align="right">last update:
                        <xsl:value-of select="string($upDate)"/> GMT+1
                    </div>
                    <div align="right">author:
                        <a href="mailto:joernt@users.sourceforge.net">joernt</a>/
                        <a href="mailto:unl@users.sourceforge.net">unl</a>
                    </div>
                </font>
            </body>
        </html>
    </xsl:template>


    <xsl:template match="title">
        <span class="title">
            <xsl:value-of select="."/>
        </span>
    </xsl:template>

    <xsl:template match="subtitle">
        <span class="subtitle">
            <xsl:value-of select="."/>
        </span>
    </xsl:template>

    <xsl:template match="list">
        <ul>
            <xsl:for-each select="item">
                <li>
                    <xsl:apply-templates/>
                </li>
            </xsl:for-each>
        </ul>
    </xsl:template>

    <xsl:template match="b">
        <xsl:copy-of select="."/>
    </xsl:template>
    <xsl:template match="para">
        <div class="para" id="{@id}">
            <xsl:apply-templates/>
        </div>
    </xsl:template>

    <xsl:template match="cite">
        <blockquote>
            <i>
                <xsl:value-of disable-output-escaping="yes" select="'&amp;#132;'"/>
                <xsl:apply-templates/>
                <xsl:value-of disable-output-escaping="yes" select="'&amp;#147;'"/>
            </i>
            <xsl:value-of select="@from"/>
        </blockquote>
    </xsl:template>

    <xsl:template match="table">
        <xsl:copy-of select="."/>
    </xsl:template>

    <xsl:template match="hr">
        <xsl:copy-of select="."/>
    </xsl:template>

    <xsl:template match="a">
        <xsl:copy-of select="."/>
    </xsl:template>

    <xsl:template match="img">
        <xsl:copy-of select="."/>
    </xsl:template>

    <xsl:template match="tt">
        <xsl:copy-of select="."/>
    </xsl:template>

    <xsl:template match="form">
        <xsl:copy-of select="."/>
    </xsl:template>

    <xsl:template match="input">
        <xsl:copy-of select="."/>
    </xsl:template>

    <xsl:template match="select">
        <xsl:copy-of select="."/>
    </xsl:template>

    <xsl:template match="option">
        <xsl:copy-of select="."/>
    </xsl:template>

    <xsl:template match="center">
        <xsl:copy>
            <xsl:value-of select="."/>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="features">
        <table border="0" cellpadding="2" cellspacing="2">
            <tr>
                <td colspan="3">Legend</td>
            </tr>
            <tr>
                <td bgcolor="red">open -
                    <xsl:value-of select="count(/page/features/section/feature[@status='open'])"/>
                </td>
                <td bgcolor="yellow">partly -
                    <xsl:value-of select="count(/page/features/section/feature[@status='partly'])"/>
                </td>
                <td bgcolor="green">ready -
                    <xsl:value-of select="count(/page/features/section/feature[@status='ready'])"/>
                </td>
            </tr>
        </table>
        <br/>
        <table border="0" cellpadding="2" cellspacing="2">
            <tr align="left" valign="top">
                <td bgcolor="#999999">Chapter</td>
                <td bgcolor="#999999">Comments</td>
                <td bgcolor="#999999">Status</td>
            </tr>
            <xsl:apply-templates/>
        </table>
    </xsl:template>

    <xsl:template match="section">
        <tr align="left" valign="top">
            <td colspan="3" bgcolor="#999999">
                <xsl:value-of select="@name"/>
            </td>
        </tr>
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="feature[@status='open']">
        <xsl:call-template name="row">
            <xsl:with-param name="color" select="'red'"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="feature[@status='partly']">
        <xsl:call-template name="row">
            <xsl:with-param name="color" select="'yellow'"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="feature[@status='ready']">
        <xsl:call-template name="row">
            <xsl:with-param name="color" select="'green'"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="feature[@status='NA']">
        <xsl:call-template name="row">
            <xsl:with-param name="color" select="'silver'"/>
        </xsl:call-template>
    </xsl:template>



    <xsl:template name="row">
        <xsl:param name="color"/>
        <tr align="left" valign="top">
            <td bgcolor="#C0C0C0">
                <xsl:value-of select="name"/>
            </td>
            <td bgcolor="#C0C0C0">
                <xsl:value-of select="comment"/>
                <xsl:value-of disable-output-escaping="yes" select="'&amp;nbsp;'"/>
            </td>
            <td bgcolor="{$color}">
                <xsl:value-of select="@status"/>
            </td>
        </tr>
    </xsl:template>

</xsl:stylesheet>
