<%@ page import="org.jdom.JDOMException,
                 java.io.IOException"%>
                <%--
  ~ Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
  ~ The YAWL Foundation is a collaboration of individuals and
  ~ organisations who are committed to improving workflow technology.
  ~
  ~ This file is part of YAWL. YAWL is free software: you can
  ~ redistribute it and/or modify it under the terms of the GNU Lesser
  ~ General Public License as published by the Free Software Foundation.
  ~
  ~ YAWL is distributed in the hope that it will be useful, but WITHOUT
  ~ ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
  ~ or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
  ~ Public License for more details.
  ~
  ~ You should have received a copy of the GNU Lesser General Public
  ~ License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
  --%>

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