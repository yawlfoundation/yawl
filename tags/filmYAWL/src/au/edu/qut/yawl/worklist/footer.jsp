<%@ page import="org.jdom.JDOMException,
                 java.io.IOException"%><br/>
                <br/>
                <br/>
                <hr/>
                <table>
                    <tr>
		           		<td>
		                	YAWL is distributed under the <a href="http://www.gnu.org/copyleft/lesser.html">LGPL</a>.
		           		</td>
                    </tr>
                </table>
				</td>
			</tr>
		</table>
        <%
        }
        catch(Exception e){

            if(e instanceof IOException){
        %><h5><font color="red">Connection Problem</font></h5><br/>
            <font color="red">Error: <%= e.getMessage() %><br/>
            Check the YAWL installation manual for trouble shooting tips.
            </font>
        <%
            } 
            else if (e instanceof JDOMException){
                e.printStackTrace();
        %><h5><font color="red">Data Format Problem</font></h5><br/>
            <font color="red">Error: <%= e.getMessage() %><br/>
            The worklist application failed receive information in an expected format from the engine.
            </font>
        <%
            }
            else {
            	e.printStackTrace();
        %><h5><font color="red">An exception was generated</font></h5><br/>
            <font color="red">Error: <%= e.getMessage() != null ? e.getMessage(): "see log files" %></font>
        <%
            }
        }
        %>