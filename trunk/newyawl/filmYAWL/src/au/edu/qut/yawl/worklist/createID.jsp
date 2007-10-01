<%@ page contentType="text/html; charset=iso-8859-1" language="java" errorPage="" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<title>Generate Digital ID</title>
<style type="text/css">
<!--
.style1 {
	font-family: Arial, Helvetica, sans-serif;
	font-weight: bold;
}
.style2 {font-family: Arial, Helvetica, sans-serif}
.style4 {font-size: 10px}
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

<%@include file="head.jsp"%>


</head>

<%@include file="banner.jsp"%>

<body>
<p align="center" class="style1">Create a Digital ID</p>
<table width="281" border="0" align="center">
  <tr>
    <td width="275"><form action="createid" method="post" name="form1" class="style2">
  <p>Username:  
    <input name="username" type="text" id="username">
</p>
  <p>Password: 
    <input name="password" type="password" id="password"> 
    <br>
    <span class="style4">(min. 6 characters)</span></p>
  <p align="right">
    <input name="Submit" type="submit" onClick="MM_validateForm('firstName','','R','lastName','','R','password','','R');return document.MM_returnValue" value="Submit">
    <input name="status" type="hidden" id="status" value="generate">
</p>
</form></td>
  </tr>
</table>
<p align="center"><strong></strong></p>

</body>
<%@include file="footer.jsp"%>
</html>
