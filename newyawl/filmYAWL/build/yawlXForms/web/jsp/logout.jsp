
<html>
   <head>
      <titleChiba logout</title>
   </head>
   <%
   	session.invalidate();
   %>
   <body bgcolor="white" background="../images/wabenfade.jpg" link="darkblue" vlink="darkblue" alink="white">
   <br><br><br><br>
	<center>
	<table bgcolor="lightgrey" border="0" cellpadding="0" cellspacing="0">
		<tr>
			<td align="right" valign="top">
				<img src="../images/chiba50t.gif" vspace="3" hspace="0">
				<br><br>
			</td></tr>
			
			<tr>
				<td>
				<table bgcolor="lightgrey" cellspacing="3">
					<tr>	
						<td>&nbsp;</td>
						<td>
							<font face="sans-serif">
							<b><p>Goodbye</p>
							<a href="forms.jsp">login again</a>
							</b>
							</font>	
						</td>
					</tr>			
				</table>
				</td>
			</tr>
	</table>
	</center>
   </body>
</html>
