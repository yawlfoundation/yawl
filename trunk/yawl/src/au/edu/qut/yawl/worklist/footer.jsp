<%@ page import="org.jdom.JDOMException,
                 java.io.IOException"%>
<br/>
<br/>
<br/>
<hr/>
<table>
    <tr>
        <td>
            YAWL is distributed under the
            <a href="http://www.gnu.org/copyleft/lesser.html">LGPL</a>.
        </td>
    </tr>
</table>
</td>
</tr>
</table>
<%
    }catch(Exception e){
%>
</table>
<%
    if(e instanceof IOException){
%>
<font color="red">
    <h5>Connection Problem</h5>
    <p>Error: <%= e.getMessage() %></p>

    <p>Check the YAWL installation manual for trouble shooting tips.</p>
</font>
<%
} else if (e instanceof JDOMException){
    e.printStackTrace();
%>
<font color="red">
    <h5>Data Format Problem</h5>
    <p>Error: <%= e.getMessage() %></p>

    <p>The worklist application failed receive information in an expected format from
        the engine.</p>
</font>
<%
} else {
%>
<font color="red"><h5>An exception was generated</h5></font>
<p><font color="red">Error: <%= e.getMessage() != null ? e.getMessage(): "see log files" %></font></p>
<%
    e.printStackTrace();
//                RequestDispatcher rd = application.getRequestDispatcher("/availableWork");
//                rd.forward(request, response);
%>

<%
        }
    }
        %>