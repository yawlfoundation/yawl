
<html>
<head>
	<title>Dokumente</title>
    <link rel="stylesheet" type="text/css" href="../styles/chiba-styles.css"/>
    <style type="text/css">
      td{padding:2px;}
    </style>

</head>
<%@ page import="java.io.*"%>
<%@ page import="java.util.*"%>
<%@ page import="java.text.*"%>
<%@ page import="org.apache.log4j.Category" %>
<%@ page session="true"%>

   <body text="black" link="black" vlink="black" alink="orange">
        <table cellpadding="0" cellspacing="0" border="0" width="100%">
            <tbody>
                <tr>
                    <td valign="Top" width="15%"><a href="http://chiba.sourceforge.net"><img src="<%=request.getContextPath()%>/forms/images/chiba50t.gif" border="0" vspace="0" alt="Chiba Logo" width="113" height="39" /></a><br/></td>
                    <td valign="Middle">
                        <div align="Right">
                        </div>
                    </td>
                </tr>
            </tbody>
        </table>
        <hr width="100%" size="2"/>
<%--
	<br>
	
	<table width="100%" border="0" cellpadding="0" cellspacing="0">
		<tr>
			
			<td>
				<font face="sans-serif" color="black">
					<b><a href="../index.html">Home</a> | Forms | <a href="documents.jsp">Documents</a></b>
				</font>
			</td>
			<td align="right" valign="top">
				<a href="../index.html"><img src="<%=request.getContextPath()%>/forms/images/chiba50t.gif" vspace="3" hspace="0" border="0"></a></td></tr>
	</table>
--%>

	<table width="100%" height="85%" cellpadding="10" cellspacing="0" border="0">
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
        if (chibaRoot == null) {
            chibaRoot = getServletConfig().getServletContext().getRealPath(".");
        }
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
		uri = forms;
		readDir = rootDir + forms;
	}else{
		readDir = rootDir + uri;
	}
	cat.debug("URI: " + uri);
	cat.debug("Read dir: " + readDir);
%>

<table width="100%" border="0" cellpadding="0" cellspacing="5"><tr><td>
Path: /<%=uri%>
</td></tr></table>
<table style="border:thin solid orange;" cellspacing="1" cellpadding="2" width="100%">

	<tr bgcolor="#faeeaa">
		<td align="left" width="10%">
			File
		</td>

		<td align="center" width="5%">
			plain HTML
		</td>

		<td width="5%" align="center">
			scripted HTML
		</td>

        <td>
            Source
        </td>
		<td>
            last update
		</td>
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
			<tr bgcolor="#FCF6D3" style="border:thin solid orange;">
				<td valign="middle" colspan="5">
				<a href="forms.jsp?<%=up%>">
					<img src="<%=request.getContextPath()%>/forms/images/folder.gif" border="0" width="20" height="20" align="left">..
				</a>
				</td>
			</tr>				
			<%
		}

		for(int i=0;i< files.length;i++){
			File aFile=new File( files[i]);
			f=new File(readDir + "/" + aFile.getName());	
			
			if(f.isDirectory()){
			%>
					
				<tr bgcolor="#FCF6D3">
					<td valign="middle" colspan="5">
					<a href="forms.jsp?<%=uri%>/<%=aFile.getName()%>">
						<img src="<%=request.getContextPath()%>/forms/images/folder.gif" border="0" width="20" height="20" align="left"><%=aFile.getName()%>
					</a>
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
				<tr bgcolor="#FCF6D3">

                    <td>
                        <%=aFile.getName()%>
                    </td>
                    <td align="center" valign="middle">
                        <a href="<%=request.getContextPath()%>/XFormsServlet?form=/<%=uri%>/<%=aFile.getName()%>">
                            <img src="<%=request.getContextPath()%>/forms/images/text.gif" border="0" width="20" height="20" align="center">
                        </a>
					</td>

                    <td align="center" valign="middle">
                        <a href="<%=request.getContextPath()%>/XFormsServlet?form=/<%=uri%>/<%=aFile.getName()%>"
                            onclick="this.href=this.href + '&JavaScript=enabled'">
                            <img src="<%=request.getContextPath()%>/forms/images/text.gif" border="0" width="20" height="20" align="center">
                        </a>
					</td>

                    <td>
                            <a href="<%=request.getContextPath()%>/<%=uri%>/<%=aFile.getName()%>">source</a>
                    </td>
					<td>
                            <%= ""+ DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.MEDIUM).format(new Date(f.lastModified())) %>
					</td>
					
				</tr>
				<% 
				}
			}
	}
	%>

</table>
</td></tr></table>

<p align="right" style="font-size:8pt;">&copy; 2003-2005 Chiba</p>
</body>
</html>
