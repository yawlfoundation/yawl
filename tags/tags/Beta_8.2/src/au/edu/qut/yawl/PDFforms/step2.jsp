<%@ page import="java.io.*"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<%@include file="head.jsp"%>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<title>PDF Form Generator - Step 2</title>
<style type="text/css">
<!--
.style1 {	font-family: Arial, Helvetica, sans-serif;
	font-weight: bold;
}
.style4 {font-family: Arial, Helvetica, sans-serif; font-weight: bold; font-size: 10; }
.style5 {font-family: Arial, Helvetica, sans-serif}
.style6 {font-family: Arial, Helvetica, sans-serif; font-size: 12px; }
.style7 {font-size: 10; font-family: Arial, Helvetica, sans-serif;}
-->
</style>
</head>
<%
int fields = Integer.parseInt(request.getParameter("fields"));
session.setAttribute("fields",""+fields);
session.setAttribute("title",request.getParameter("title").trim());
session.setAttribute("instructions", request.getParameter("instructions").trim());
session.setAttribute("author", request.getParameter("author").trim());
session.setAttribute("subject", request.getParameter("subject").trim());
session.setAttribute("fileName", request.getParameter("specid").trim()+request.getParameter("taskid").trim());
session.setAttribute("signature", request.getParameter("signature").trim());
%>
<body>
<%@ include file="banner.jsp"%>
 <p><span class="style1">STEP 2 </span></p>
 <table width="548" border="0">
   <tr class="style6">
     <td width="140"><strong>Form Title: </strong></td>
     <td width="398"><%=request.getParameter("title")%></td>
   </tr>
   <tr class="style6">
     <td><strong>Form Instructions:</strong></td>
     <td><%=request.getParameter("instructions")%></td>
   </tr>
   <tr class="style6">
     <td><strong>Author:</strong></td>
     <td><%=request.getParameter("author")%></td>
   </tr>
    <tr class="style6">
     <td><strong>Subject:</strong></td>
     <td><%=request.getParameter("subject")%></td>
   </tr>
 </table>
  <%if(request.getParameter("logo").trim().equals("")!=true){
  File f = new File(request.getParameter("logo").trim());
  if(f.exists())
  {
   session.setAttribute("logo",request.getParameter("logo").trim());
  %>
 <table width="548" border="0">
   <tr class="style6">
     <td width="140"><strong>Logo: </strong></td>
     <td width="398"><img src="<%=request.getParameter("logo").trim()%>"></td>
   </tr>
 </table>
 <%}
 else{
 out.print("Image does not exists in file location.");
 session.setAttribute("logo",null);}
 }%>
  <%if(request.getParameter("conclusion").trim().equals("")!=true){
  session.setAttribute("conclusion",request.getParameter("conclusion").trim());%>
 <table width="548" border="0">
   <tr class="style6">
     <td width="140"><strong>Optional Concluding Text: </strong></td>
     <td width="398"><%=request.getParameter("conclusion")%></td>
   </tr>
 </table>
 <%}
 else
  session.setAttribute("conclusion",null);%>
 <%if(request.getParameter("header").trim().equals("")!=true){
 session.setAttribute("header",request.getParameter("header"));%>
 <table width="548" border="0">
   <tr class="style6">
     <td width="140"><strong>Optional Header: </strong></td>
     <td width="398"><%=request.getParameter("header")%></td>
   </tr>
 </table>
 <%}else
 session.setAttribute("header",null);%>
 <%if(request.getParameter("footer").trim().equals("")!=true){
 session.setAttribute("footer",request.getParameter("footer").trim());%>
 <table width="548" border="0">
   <tr class="style6">
     <td width="141"><strong>Optional Footer: </strong></td>
     <td width="397"><%=request.getParameter("footer")%></td>
   </tr>
 </table>
 <%}else
 session.setAttribute("footer",null);%>
<%if(request.getParameter("keywords").trim().equals("")!=true){
 session.setAttribute("keywords",request.getParameter("keywords").trim());%>
 <table width="548" border="0">
   <tr class="style6">
     <td width="141"><strong>Optional keywords: </strong></td>
     <td width="397"><%=request.getParameter("keywords")%></td>
   </tr>
 </table>
 <%}else
 session.setAttribute("keywords",null);%>
<p class="style6"><strong>Enter the names of the <%=fields%> form fields. </strong></p>
<form action="generate" method="get"> 
 <table width="685" height="242" border="0" cellpadding="5">
<%for(int i=0; i<fields; i++){%>
    <tr>
      <td width="58" class="style6">Field <%=i+1%>:</td>
      <td width="494" class="style4">        <span class="style5">
        <input name="f<%=i%>" type="text" id="title" size="80">        
      </span></td>
      <td width="95"><span class="style4"><input name="fr<%=i%>" type="checkbox" id="fr<%=i%>" value="true">      
      </span><span class="style6">Read Only</span> </td>
    </tr>
<%}%>
    <tr>
      <td height="74" class="style6">&nbsp;</td>
      <td colspan="2" class="style4"><div align="right">
        <input type="submit" name="Submit" value="Next">
      </div></td>
    </tr>
 </table>
 <p>&nbsp;</p>
</form>
<%@include file="footer.jsp"%>
</body>
</html>
