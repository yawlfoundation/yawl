<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="java.sql.*" errorPage="" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<%@include file="head.jsp"%>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<title>PDF Form Generator</title>
<style type="text/css">
<!--
.style1 {
	font-family: Arial, Helvetica, sans-serif;
	font-weight: bold;
}
.style3 {font-size: 12px; font-family: Arial, Helvetica, sans-serif;}
-->
</style>
<script language="JavaScript" type="text/JavaScript">
<!--
function MM_findObj(n, d) { //v4.01
  var p,i,x;  if(!d) d=document; if((p=n.indexOf("?"))>0&&parent.frames.length) {
    d=parent.frames[n.substring(p+1)].document; n=n.substring(0,p);}
  if(!(x=d[n])&&d.all) x=d.all[n]; for (i=0;!x&&i<d.forms.length;i++) x=d.forms[i][n];
  for(i=0;!x&&d.layers&&i<d.layers.length;i++) x=MM_findObj(n,d.layers[i].document);
  if(!x && d.getElementById) x=d.getElementById(n); return x;
}

function MM_validateForm() { //v4.0
  var i,p,q,nm,test,num,min,max,errors='',args=MM_validateForm.arguments;
  for (i=0; i<(args.length-2); i+=3) { test=args[i+2]; val=MM_findObj(args[i]);
    if (val) { nm=val.name; if ((val=val.value)!="") {
      if (test.indexOf('isEmail')!=-1) { p=val.indexOf('@');
        if (p<1 || p==(val.length-1)) errors+='- '+nm+' must contain an e-mail address.\n';
      } else if (test!='R') { num = parseFloat(val);
        if (isNaN(val)) errors+='- '+nm+' must contain a number.\n';
        if (test.indexOf('inRange') != -1) { p=test.indexOf(':');
          min=test.substring(8,p); max=test.substring(p+1);
          if (num<min || max<num) errors+='- '+nm+' must contain a number between '+min+' and '+max+'.\n';
    } } } else if (test.charAt(0) == 'R') errors += '- '+nm+' is required.\n'; }
  } if (errors) alert('The following error(s) occurred:\n'+errors);
  document.MM_returnValue = (errors == '');
}
//-->
</script>
</head>

<body>
<%@ include file="banner.jsp"%>
<form action="step2.jsp" method="get" enctype="multipart/form-data" name="form" id="form" onSubmit="MM_validateForm('specid','','R','title','','R','author','','R','subject','','R','instructions','','R');return document.MM_returnValue">
  <p class="style1">PDF Form Generator</p>
  <p class="style1">STEP 1 </p>
  <table width="642" height="267" border="0" cellpadding="5">
    <tr>
      <td class="style3">Specification ID: </td>
      <td><input name="specid" type="text" id="specid" size="80"></td>
    </tr>
    <tr>
      <td class="style3">Task ID: </td>
      <td><input name="taskid" type="text" id="taskid" size="80"></td>
    </tr>
    <tr>
      <td width="128" class="style3">Form Title: </td>
      <td width="316"><input name="title" type="text" id="title" size="80"></td>
    </tr>
    <tr>
      <td class="style3">Logo:</td>
      <td><input name="logo" type="file" id="logo" size="60" maxlength="300"></td>
    </tr>
    <tr>
      <td class="style3">Author:</td>
      <td><input name="author" type="text" id="author" size="80"></td>
    </tr>
    <tr>
      <td class="style3">Subject:</td>
      <td><input name="subject" type="text" id="subject" size="80"></td>
    </tr>
    <tr>
      <td class="style3">Instructions:</td>
      <td><textarea name="instructions" cols="50" id="instructions"></textarea></td>
    </tr>
    <tr>
      <td class="style3">Number of fields: </td>
      <td><select name="fields" id="fields">
        <option value="1">1</option>
        <option value="2">2</option>
        <option value="3">3</option>
        <option value="4">4</option>
        <option value="5">5</option>
        <option value="6">6</option>
        <option value="7">7</option>
        <option value="8">8</option>
        <option value="9">9</option>
        <option value="10">10</option>
      </select></td>
    </tr>
    <tr>
      <td class="style3">Include Signature field: </td>
      <td><select name="signature" id="signature">
        <option value="Yes">Yes</option>
        <option value="No" selected>No</option>
                  </select></td>
    </tr>
    <tr>
      <td class="style3">&nbsp;</td>
      <td>&nbsp;</td>
    </tr>
    <tr>
      <td class="style3"> Optional Concluding Text: </td>
      <td><input name="conclusion" type="text" id="conclusion" size="80"></td>
    </tr>
    <tr>
      <td class="style3">Optional Header: </td>
      <td><input name="header" type="text" id="header" size="80"></td>
    </tr>
    <tr>
      <td height="36" class="style3">Optional Footer:</td>
      <td><input name="footer" type="text" id="footer" size="80"></td>
    </tr>
    <tr>
      <td class="style3"> Optional Keyword(s):</td>
      <td><input name="keywords" type="text" id="keywords" size="80"></td>
    </tr>
    <tr>
      <td class="style3">&nbsp;</td>
      <td><div align="right"><span class="style1">
          <input type="submit" name="Submit" value="Next">
      </span></div></td>
    </tr>
  </table>
</form>
<%@include file="footer.jsp"%>
</body>
</html>
