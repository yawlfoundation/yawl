<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:param name="today" select="today"/>

<!--
    Copyright  2002,2004 The Apache Software Foundation
   
     Licensed under the Apache License, Version 2.0 (the "License");
     you may not use this file except in compliance with the License.
     You may obtain a copy of the License at
   
         http://www.apache.org/licenses/LICENSE-2.0
   
     Unless required by applicable law or agreed to in writing, software
     distributed under the License is distributed on an "AS IS" BASIS,
     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     See the License for the specific language governing permissions and
     limitations under the License.
   
-->

<xsl:output method="html" indent="yes"  encoding="US-ASCII"/>

<xsl:template match="JarAnalyzer">
    <html>
    <head>
        <title>JarAnalyzer Analysis</title>
        
    <style type="text/css">
      body {
        font:normal 68% verdana,arial,helvetica;
        color:#000000;
      }
      
     table.gray {
		  background:#eeeeee
      }
      table tr td, tr th {
          font-size: 68%;
      }
      table.details tr th{
        font-weight: bold;
        text-align:left;
        background:#a6caf0;
      }
      table.details tr td{
        background:#eeeee0;
      }
      
      p {
        line-height:1.5em;
        margin-top:0.5em; margin-bottom:1.0em;
        margin-left:2em;
        margin-right:2em;
      }
      h1 {
        margin: 0px 0px 5px; font: 165% verdana,arial,helvetica
      }
      h2 {
        margin-top: 1em; margin-bottom: 0.5em; font: bold 125% verdana,arial,helvetica
      }
      h3 {
        margin-bottom: 0.5em; font: bold 115% verdana,arial,helvetica
      }
      h4 {
        margin-bottom: 0.5em; font: bold 100% verdana,arial,helvetica
      }
      h5 {
        margin-bottom: 0.5em; font: bold 100% verdana,arial,helvetica
      }
      h6 {
        margin-bottom: 0.5em; font: bold 100% verdana,arial,helvetica
      }
      .Error {
        font-weight:bold; color:red;
      }
      .Failure {
        font-weight:bold; color:purple;
      }
      .Properties {
        text-align:right;
      }
      </style>
        
        
    </head>
    <body>
    
    <h1><a name="top">JarAnalyzer Analysis</a></h1>
	<p align="right">Run with <a href="http://www.kirkk.com/main/Main/JarAnalyzer">JarAnalyzer</a> on <xsl:value-of select="$today"/></p>
    <hr size="2" />
    
    <table width="100%"><tr><td>
    <a name="NVsummary"><h2>Summary</h2></a>
    </td><td align="right">
    [<a href="#NVsummary">summary</a>]
    [<a href="#NVjars">jars</a>]
    [<a href="#NVcycles">cycles</a>]
    [<a href="#NVexplanations">explanations</a>]
    </td></tr></table>
      <table width="100%" class="details">
        <tr>
            <th>Jar Name</th>
            <th>Total Classes</th>
            <th><a href="#EXclassnumber">Abstract Classes</a></th>
            <th><a href="#EXpackagenumber">Packages</a></th>
            <th><a href="#EXabstractness">Abstractness</a></th>
            <th><a href="#EXefferent">Efferent</a></th>
            <th><a href="#EXafferent">Afferent</a></th>
            <th><a href="#EXinstability">Instability</a></th>
            <th><a href="#EXdistance">Distance</a></th>
            
        </tr>
    <xsl:for-each select="./Jars/Jar">
        <xsl:if test="count(error) = 0">
            <tr>
                <td align="left">
                    <a>
                    <xsl:attribute name="href">#PK<xsl:value-of select="@name"/>
                    </xsl:attribute>
                    <xsl:value-of select="@name"/>
                    </a>
                </td>
                <td align="right"><xsl:value-of select="Summary/Statistics/ClassCount"/></td>
                <td align="right"><xsl:value-of select="Summary/Statistics/AbstractClassCount"/></td>
                <td align="right"><xsl:value-of select="Summary/Statistics/PackageCount"/></td>
                <td align="right"><xsl:value-of select="Summary/Metrics/Abstractness"/></td>
                <td align="right"><xsl:value-of select="Summary/Metrics/Efferent"/></td>
                <td align="right"><xsl:value-of select="Summary/Metrics/Afferent"/></td>
                <td align="right"><xsl:value-of select="Summary/Metrics/Instability"/></td>
                <td align="right"><xsl:value-of select="Summary/Metrics/Distance"/></td>
                

            </tr>
        </xsl:if>
    </xsl:for-each>
    <xsl:for-each select="./Jars/Jar">
        <xsl:if test="count(error) &gt; 0">
            <tr>
                <td align="left">
                    <xsl:value-of select="@name"/>
                </td>
                <td align="left" colspan="8"><xsl:value-of select="error"/></td>
            </tr>
        </xsl:if>
    </xsl:for-each>
    </table>
    
     <table width="100%"><tr><td>
    <a name="NVjars"><h2>Jars</h2></a>
    </td><td align="right">
    [<a href="#NVsummary">summary</a>]
    [<a href="#NVjars">jars</a>]
    [<a href="#NVcycles">cycles</a>]
    [<a href="#NVexplanations">explanations</a>]
    </td></tr></table>
    
    <xsl:for-each select="./Jars/Jar">
        <xsl:if test="count(error) = 0">
            <h3><a><xsl:attribute name="name">PK<xsl:value-of select="@name"/></xsl:attribute>
            <xsl:value-of select="@name"/></a></h3>
            
            <table width="100%" class="gray"><tr>
                <td><a href="#EXafferent">Afferent Couplings</a>: <xsl:value-of select="Summary/Metrics/Afferent"/></td>
                <td><a href="#EXefferent">Efferent Couplings</a>: <xsl:value-of select="Summary/Metrics/Efferent"/></td>
                <td><a href="#EXabstractness">Abstractness</a>: <xsl:value-of select="Summary/Metrics/Abstractness"/></td>
                <td><a href="#EXinstability">Instability</a>: <xsl:value-of select="Summary/Metrics/Instability"/></td>
                <td><a href="#EXdistance">Distance</a>: <xsl:value-of select="Summary/Metrics/Distance"/></td>
            </tr></table>
            
            <table width="100%" class="details">
                <tr>
                    <th>Uses Jars</th>
                    <th>Used by Jars</th>
                    <th>Cycles With</th>
                </tr>
                <tr>
                    <td valign="top" width="33%">
                        <xsl:if test="count(Summary/OutgoingDependencies/Jar)=0">
                            <i>None</i>
                        </xsl:if>
                        <xsl:for-each select="Summary/OutgoingDependencies/Jar">
                             <a>
								<xsl:attribute name="href">#PK<xsl:value-of select="node()"/>
								</xsl:attribute>
								<xsl:value-of select="node()"/><br/>
							</a>
                        </xsl:for-each>
                    </td>
                     <td valign="top" width="33%">
                        <xsl:if test="count(Summary/IncomingDependencies/Jar)=0">
                            <i>None</i>
                        </xsl:if>
                        <xsl:for-each select="Summary/IncomingDependencies/Jar">
                            <a>
								<xsl:attribute name="href">#PK<xsl:value-of select="node()"/>
								</xsl:attribute>
								<xsl:value-of select="node()"/><br/>
							</a>
                        </xsl:for-each>
                    </td>
                    <td valign="top" width="33%">
                        <xsl:if test="count(Summary/Cycles/Cycle)=0">
                            <i>None</i>
                        </xsl:if>
                        <xsl:for-each select="Summary/Cycles/Cycle">
                            <a>
								<xsl:attribute name="href">#PK<xsl:value-of select="node()"/>
								</xsl:attribute>
								<xsl:value-of select="node()"/><br/>
							</a>
                            <br/>
                        </xsl:for-each>
                    </td>
                </tr>
            </table>
            
            <table width="100%" class="details">
                 <tr>
                    <th>Packages within jar</th> 
                    <th><a href="#EXunresolved">Unresolved Packages</a></th> 
                </tr>
                <tr>
                <td valign="top" width="26%">
                    <xsl:if test="count(Summary/Packages/Package)=0">
                            <i>None</i>
                        </xsl:if>
                        <xsl:for-each select="Summary/Packages/Package">
                            <xsl:value-of select="node()"/><br/>
                        </xsl:for-each>
                    </td>
                <td valign="top" width="26%">
                        <xsl:if test="count(Summary/UnresolvedDependencies/Package)=0">
                            <i>None</i>
                        </xsl:if>
                        <xsl:for-each select="Summary/UnresolvedDependencies/Package">
                            <a>
                                <xsl:value-of select="node()"/>
                            </a><br/>
                        </xsl:for-each> 
                    </td> 
                </tr>
                
              </table>
        </xsl:if>
    </xsl:for-each>
    
    <table width="100%"><tr><td>
    <a name="NVcycles"><h2>Cycles</h2></a>
    </td><td align="right">
    [<a href="#NVsummary">summary</a>]
    [<a href="#NVjars">jars</a>]
    [<a href="#NVcycles">cycles</a>]
    [<a href="#NVexplanations">explanations</a>]
    </td></tr></table>
    
    <xsl:for-each select="./Jars/Jar">
        <h3><xsl:value-of select="@name"/> has cycles with</h3><p>
         <xsl:if test="count(Summary/Cycles/Cycle)=0">
			 <i>None</i>
         </xsl:if>
        <xsl:for-each select="Summary/Cycles/Cycle">
               <xsl:value-of select="node()"/>
              <br/>
        </xsl:for-each></p>
    </xsl:for-each>

 <hr size="2" />    
    <table width="100%"><tr><td>
    <a name="NVexplanations"><h2>Explanations</h2></a>
    </td><td align="right">
    [<a href="#NVsummary">summary</a>]
    [<a href="#NVjars">jars</a>]
    [<a href="#NVcycles">cycles</a>]
    [<a href="#NVexplanations">explanations</a>]
    </td></tr></table>
    
    <p>The following explanations are for quick reference. More detailed information can be found in the <a href="http://www.kirkk.com/main/Main/JarAnalyzer">JarAnalyzer documentation</a>.</p>
    
    <h3><a name="EXclassnumber">Number of Classes</a></h3>
        <p>The number of concrete and abstract classes (and interfaces) in the jar is an indicator of the extensibility of the jar.</p>
       <h3><a name="EXpackagenumber">Number of Packages</a></h3>
        <p>The number of packages in the jar.</p> 
         <h3><a name="EXafferent">Afferent Couplings</a></h3>
        <p>The number of other jars that depend upon classes within the jar is an indicator of the jar's responsibility. </p>
    <h3><a name="EXefferent">Efferent Couplings</a></h3>
        <p>The number of other jars that the classes in the jar depend upon is an indicator of the jar's independence. </p>
    <h3><a name="EXabstractness">Abstractness</a></h3> 
        <p>The ratio of the number of abstract classes (and interfaces) in the analyzed jar to the total number of classes in the analyzed jar. </p>
        <p>The range for this metric is 0 to 1, with A=0 indicating a completely concrete jar and A=1 indicating a completely abstract jar. </p>
    <h3><a name="EXinstability">Instability</a></h3>
        <p>The ratio of efferent coupling (Ce) to total coupling (Ce / (Ce + Ca)). This metric is an indicator of the jar's resilience to change. </p>
        <p>The range for this metric is 0 to 1, with I=0 indicating a completely stable jar and I=1 indicating a completely instable jar. </p>
    <h3><a name="EXdistance">Distance</a></h3>
        <p>The perpendicular distance of a jar from the idealized line A + I = 1. This metric is an indicator of the jar's balance between abstractness and stability. </p>
        <p>A jar squarely on the main sequence is optimally balanced with respect to its abstractness and stability. Ideal jars are either completely abstract and stable (x=0, y=1) or completely concrete and instable (x=1, y=0). </p>
        <p>The range for this metric is 0 to 1, with D=0 indicating a jar that is coincident with the main sequence and D=1 indicating a jar that is as far from the main sequence as possible. </p>
         <h3><a name="EXunresolved">Unresolved Packages</a></h3>
         <p>Packages not found in any of the jars analyzed. These can be filtered from output by specifying the packages to exlude in the Filter.properties file. Conversely, you can include jars containing these packages in the directory being analyzed.</p>
         <p>These packages are excluded from all calculations and adding the jars containing these packages will result in modified metrics.</p>
    </body>
    </html>
</xsl:template>

</xsl:stylesheet>
