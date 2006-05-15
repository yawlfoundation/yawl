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
                        <a href="http://www.apache.org/licenses/LICENSE-2.0.html">YAWL licence</a>.
                        </td>
                    </tr>
                </table>
				</td>
			</tr>
		</table>
        <%
        }catch(Exception e){
            if(e instanceof IOException){
        %>
            <h3>Connection Problem</h3>
            <font color="red">
            <p>Error: <%= e.getMessage() %></p>

            <p>Check the YAWL installation manual for trouble shooting tips.</p>
            </font>
        <%
            } else {
        %>
            <h3>An exception was generated</h3>
            <p><font color="red">Error: <%= e.getMessage() %></font></p>
            <%
                e.printStackTrace();
            %>

        <%
            }
        }
        %>