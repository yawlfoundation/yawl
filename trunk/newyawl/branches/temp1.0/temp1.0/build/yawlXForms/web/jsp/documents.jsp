<html>
<head>
	<title>Dokumente</title>
    <link rel="stylesheet" type="text/css" href="../styles/chiba-styles.css"/>
</head>
<%@ page import="java.io.*"%>
<%@ page import="java.util.*"%>
<%@ page import="java.text.*"%>
<%@ page import="org.apache.log4j.Category" %>
<%@ page session="true"%>

   <body bgcolor="#aabbdd" text="black" link="black" vlink="black" alink="orange">
        <table cellpadding="0" cellspacing="0" border="0" width="100%">
            <tbody>
                <tr>
                    <td valign="Top" width="15%"><a href="http://chiba.sourceforge.net"><img src="../images/chiba50t.gif" border="0" vspace="0" alt="Chiba Logo" width="113" height="39" /></a><br/></td>
                    <td valign="Middle">
                        <font face="sans-serif">
                            <div align="Right">
                            &lt;<a href="../index.html">Home</a>/&gt;
                            &lt;<a href="../installation.html">Installation</a>/&gt;
                            &lt;Samples/&gt;
                            &lt;<a href="../features.html">Status</a>/&gt;
                            &lt;<a href="../api/index.html">Javadoc</a>/&gt;
                            &lt;<a href="http://sourceforge.net/mail?group_id=20274">Mailinglist</a>/&gt;
                            &lt;<a href="http://sourceforge.net/project/showfiles.php?group_id=20274">Download</a>/&gt;<br/>
                            </div>
                        </font>
                    </td>
                </tr>
            </tbody>
        </table>
        <hr width="100%" size="2"/>
        <a href="forms.jsp">Forms</a> | Documents

	<table bgcolor="#aabbdd" width="100%" height="85%" cellpadding="10" cellspacing="0" border="0">
	<tr>

	<td valign="top">
<%!
    static Category cat = Category.getInstance("org.chiba.web.jsp"); 

	String chibaRoot=null;
	String docs=null;
	String forms=null;
	String rootDir=null;
	
	public void jspInit(){
		
		// +++ read general parameters from web.xml
		chibaRoot=getServletConfig().getServletContext().getRealPath("");
		docs=(String)getServletConfig().getServletContext().getInitParameter("chiba.docs");
		forms=(String)getServletConfig().getServletContext().getInitParameter("chiba.forms");
		if(chibaRoot==null||docs==null||forms==null)
		{
			//no better way yet
			throw new Error("Configuratin incomplete. Please check the <br>chiba.doc.root and chiba.forms.root-parameters in web.xml");
		}		
		cat.debug("\ncreatex.jsp: init : chibaRoot: " + chibaRoot);
		cat.debug("createx.jsp: init : docs: " + docs);
		cat.debug("createx.jsp: init : forms: " + forms);
		
		rootDir = chibaRoot + "/";
	}
	
%>

<%
    String fileName=null;
	String uri=request.getQueryString();
	String readDir=null;

	
	if (uri == null) {
		uri = docs;
		readDir = rootDir + docs;
	}else{
		readDir = rootDir + uri;
	}
	cat.debug("URI: " + uri);
	cat.debug("Read dir: " + readDir);
%>

<table width="100%" border="0" cellpadding="0" cellspacing="5"><tr><td>
<font face="sans-serif">
	Path: /<%=uri%>
<font>
</td></tr></table>
<table border="0" cellspacing="1" cellpadding="2" width="100%">

	<tr bgcolor="gray">
		<td width="50%">
		<font color="#CCCCFF" face="sans-serif" size="-1">		
			Name
		</font>
		</td>
		
		<td>
		<font color="#CCCCFF" face="sans-serif" size="-1">
			last update
		</font>
		</td>
		<!--
		<td align="center" width="1%">
		<font color="lightgrey" face="sans-serif" size="-1">
			HISTORY
		</font>
		</td>
		
		<td width="1%">
		<font color="lightgrey" face="sans-serif" size="-1">
			HTML
		</font>
		</td>
		
		<td width="1%">
		<font color="lightgrey" face="sans-serif" size="-1">
			PDF
		</font>
		</td>
		-->
	</tr>
	<%
	
	//list files from documents directory
	
	File root=new File(readDir);
    if (!root.exists()) {
        root.mkdirs();
    }
	String[] files=root.list();
	cat.debug("Documents.jsp: files: " + files.length);
	String bgColor="#DCDCDC";
	String s;
	File f=null;
	String up=null;
	if (files!=null)
	{
		if(uri.indexOf("/")!=-1){
			up=uri.substring(0,uri.lastIndexOf("/"));
			%>
			<tr bgcolor="#DDDDDD">
				<td valign="middle" colspan="5">		
				<font face="sans-serif" size="-1">
				 <a href="documents.jsp?<%=up%>">
					<img src="../images/folder.gif" border="0" width="20" height="20" align="left">..
				</a>
				
				</font>
				</td>			
			</tr>				
			<%
		}

		for(int i=0;i< files.length;i++){
			File aFile=new File( files[i]);
			f=new File(readDir + "/" + aFile.getName());	
			
			if(f.isDirectory()){
			%>
					
				<tr bgcolor="#DDDDDD">
					<td valign="middle" colspan="5">		
					<font face="sans-serif" size="-1">
					<a href="documents.jsp?<%=uri%>/<%=aFile.getName()%>">
						<img src="../images/folder.gif" border="0" width="20" height="20" align="left"><%=aFile.getName()%>
					</a>
					</font>
					</td>
			
				</tr>
			<%
			}
		}
	}
	root=new File(readDir);
	files=root.list();
	cat.debug ("Documents.jsp: files: " + files.length);

	if (files!=null)
	{
		for(int i=0;i< files.length;i++){
			File aFile=new File( files[i]);
			f=new File(readDir + "/" + aFile.getName());	


			if(!(f.isDirectory())){
	
			%>
				<tr bgcolor="#DCDCDC">
					<td valign="middle">		
					<font face="sans-serif" size="-1">
<%--					<a href="startsession.jsp?<%=uri%>/<%=aFile.getName()%>">--%>
						<img src="../images/text.gif" border="0" width="20" height="20" align="left"><%=aFile.getName()%>
<%--					</a>--%>
					</font>
					</td>
	
							
					<td>
					<font face="sans-serif" size="-1">
						<%= ""+ DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.MEDIUM).format(new Date(f.lastModified())) %>
					</font>
					</td>
					
<%--
					<td align="center">
						<img src="../images/unknown.gif" width="20" height="20" border="0">
					</font>
					</td>
					
					<td align="center" valign="middle">
					<a href="showpage.jsp?<%=uri%>/<%=aFile.getName()%>" target="top">
						<img src="../images/html.gif" border="0" width="20" heigth="20"></a>
					</font>
					</td>
					
					<td align="center">
						<img src="../images/pdf.gif" width="20" heigth="20" border=0>
					</td>
--%>
				</tr>
				<% 
				}
			}
	}
	%>

</table>
</td></tr></table>

<p align="right"><font size="2">&copy; 2003, Chiba</font>
</body>
</html>
