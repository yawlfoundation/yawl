<%@page import="au.edu.qut.yawl.admintool.*" %>
<%@page import="au.edu.qut.yawl.admintool.model.*" %>
<%@page import="au.edu.qut.yawl.engine.interfce.*" %>

<html xmlns="http://www.w3.org/1999.xhtml">
    <head>
        <title>YAWL Administration and Monitoring Tool</title>
        <meta name="Pragma" content="no-cache"/>
        <meta name="Cache-Control" content="no-cache"/>
        <meta name="Expires" content="0"/>
        <link rel="stylesheet" href="./graphics/common.css">
    </head>
    <body>

        <script type="text/javascript">

            function DisplayAggregationFunctions() {

            s = document.forms[0].groupbyvalue.options[document.forms[0].groupbyvalue.selectedIndex].text;	

         			
   
            if (s.indexOf("No grouping")!=-1) {
	   


            document.forms[0].Owner.disabled = false;
            document.forms[0].specification.disabled = false;	

            document.forms[0].function_sc.disabled = true;

            document.forms[0].Count.disabled = true;



            if (document.forms[0].function_es!=null) {
            document.forms[0].ID.disabled = false;	
            document.forms[0].Enabled.disabled = false;	
            document.forms[0].Started.disabled = false;	
            document.forms[0].Completed.disabled = false;
            document.forms[0].Task.disabled = false;			
            document.forms[0].function_es.disabled = true;	
            document.forms[0].function_ec.disabled = true;
            } else {
	
            document.forms[0].created.disabled = false;
            document.forms[0].completed.disabled = false;
            document.forms[0].cancelled.disabled = false;

            }	
            } else {

            if (s.indexOf("Specification")!=-1) {
            document.forms[0].Owner.disabled = true;
            document.forms[0].specification.disabled = false;
            if (document.forms[0].Task!=null) {
            document.forms[0].Task.disabled = true;
            }	
            }
            if (s.indexOf("Resource")!=-1) {
            document.forms[0].specification.disabled = true;
            document.forms[0].Owner.disabled = false;
            if (document.forms[0].Task!=null) {
            document.forms[0].Task.disabled = true;
            }
            }
            if (s.indexOf("Task")!=-1) {
            document.forms[0].specification.disabled = true;
            document.forms[0].Owner.disabled = true;
            if (document.forms[0].Task!=null) {
            document.forms[0].Task.disabled = false;		
            }
            }
	
            document.forms[0].function_sc.disabled = false;
            document.forms[0].Count.disabled = false;

            if (document.forms[0].function_es!=null) {
            document.forms[0].ID.disabled = true;	
            document.forms[0].Enabled.disabled = true;	
            document.forms[0].Started.disabled = true;	
            document.forms[0].Completed.disabled = true;	
			
            document.forms[0].function_es.disabled = false;	
            document.forms[0].function_ec.disabled = false;
            } else {
            document.forms[0].created.disabled = true;
            document.forms[0].completed.disabled = true;
            document.forms[0].cancelled.disabled = true;

            }
	

            }



            return true;
            }

        </script>

        <%@include file="checkLogin.jsp" %>
        <%@include file="YAWLnavigation.jsp" %>

        <% 	
            ServletContext context = getServletContext();
            String persistOn = context.getInitParameter("EnablePersistance");
            boolean _persistanceConfiguredOn = "true".equalsIgnoreCase(persistOn);
            if (_persistanceConfiguredOn) {
                if (request.getParameter("success") != null) {
                    String bla = new String(request.getParameter("success"));
                    if (bla.compareTo("true")==0) {
        %>
        <font color="green">The chart was updated</font>
        <p/>
        <%	
            }
            }
        %>	

        <% 	
            if (request.getParameter("table") != null) {
                String bla = new String(request.getParameter("table"));
                if (bla.compareTo("true")==0) {
        %>
        <font color="green">The table was updated</font>
        <p/>

        <%	
            }
            }
        %>	

        <%
            DatabaseGatewayImpl _model = DatabaseGatewayImpl.getInstance(_persistanceConfiguredOn);


            String[] specIDs = _model.getSpecs();
            String[] caseIDs = _model.getCases();
            String[] taskIDs = _model.getTasks();
            String[] workitemIDs = _model.getWorkItems();
            String[] resourceids = _model.getLoggedResources();

            if (request.getParameter("currentquery") != null) {

        %>
        <font color="green"><%out.print(request.getParameter("currentquery"));%></font>
        <p/>

        <%	

                }

        %>

        <h2>Overview</h2>
        <p/>
        <% if (request.getParameter("success") != null) {
        %>
        <img src="http://131.181.70.9:8080/admintool/createChart" alt="Graph" border=0 />
        <% 
            }
        %>

        <% if (request.getParameter("table") != null) {
        %>

        <p><a href="createChart" target="_blank"> Click to Display Table </a></p>

        <% 
            }
        %>


        <% if (request.getParameter("success") == null) {
        %>
        Please select which chart you would like to view below.
        <% 
            }
        %>
        <p/>
        <hr/>
        <form method="post" action="http://131.181.70.9:8080/admintool/createChart" name="buildquery">
            <table width="90%" border="0" bgcolor="#eeeeee">
                <tr>
                    <td>
                        STEP 1: 
                    </td>
                </tr>

                <%
                    String queryname = request.getParameter("queryname");
                    String querytype = request.getParameter("querytype");
                    String view = request.getParameter("querygraph");
                    String querygraph = request.getParameter("querygraph");
                    String aggregate = request.getParameter("aggregate");
                %>	


                <tr>


                    <tr>
                        <td>Select Element</td>
                        <td><select name="selectelement">
                            <option <%if (querytype!=null && querytype.equals("case")) {out.print("SELECTED");}%>>Cases</option>
                            <option <%if (querytype!=null && querytype.equals("task")) {out.print("SELECTED");}%>>Work-Items</option>
                        </select>
                        </td>
                    </tr>

                    <tr>
                        <td>Select View </td>
                        <td><select name="selectgraph">
                            <option <%if (querygraph!=null && querygraph.equals("Table")) {out.print("SELECTED");}%>>Table</option>
                            <option <%if (querygraph!=null && querygraph.equals("Chart")) {out.print("SELECTED");}%>>Chart</option>
                        </select>
                        </td>
		
                    </tr>

                    <tr height=15>
                        <td><input type="submit" value="Start New Query" name="startquery"/>
                        </td>
                    </tr>




                    <%
                        if (querytype != null) {
                    %>	

                    <tr>
                        <td>
                            STEP 2: 
                        </td>
                    </tr>

                    <tr height=10>
                    </tr>	


                    <tr>
                        <td> 
                        Specification </td>
                        <td>
                            <select name="specfilter">

                                <option>--All--</option>
                                <%
                                    for (int i = 0; i < specIDs.length;i++) {
                                %><option><%
                                    out.print(specIDs[i]);
                                %></option><%
                                    }%>
                                </select>

                            </select>
                        </td>
                    </tr>
                    <tr>
                        <td>
                        Case </td>
                        <td><select name="casefilter">
                            <option>--All--</option>
                            <%
                                for (int i = 0; i < caseIDs.length;i++) {
                            %><option><%
                                out.print(caseIDs[i]);
                            %></option><%
                                }%>
                            
                        </select>
                        </td>
                    </tr>
                    <tr>
                        <td>
                        Resource </td>
                        <td><select name="resourcefilter">
                            <option>--All--</option>
                            <%
                                for (int i = 0; i < resourceids.length;i++) {
                            %><option><%
                                out.print(resourceids[i]);
                            %></option><%
                                }%>
                            
                        </select>
                        </td>
                    </tr>
                    <% if (querytype!=null && querytype.equals("task")) { %>
                    <tr>
                        <td>
                        Task </td>

                        <td><select name="taskfilter">
                            <option>--All--</option>
                            <%
                                for (int i = 0; i < taskIDs.length;i++) {
                            %><option><%
                                out.print(taskIDs[i]);
                            %></option><%
                                }%>
                            
                        </select>
                        </td>	
                    </tr>

                    <!--	<tr>
                    <td>
                    WorkItem </td>
                    <td><select name="workitemfilter">
                    <option>--All--</option>
                    <%
                        for (int i = 0; i < workitemIDs.length;i++) {
                    %><option><%
                        out.print(workitemIDs[i]);
                    %></option><%
                        }%>
                    
                    </select>
                    </td>
                    -->	<%}%> 
                </tr>

                <tr height=10>
                </tr>
                <tr>
                    <td>	
                    </td>
                    <td>	
                        -----------------------
                    </td>
                </tr>

                <tr>
                    <td>Status </td>
                    <td><select name="statusoption">
                        <option>--Select Option--</option>
                        <% if (querytype!=null && querytype.equals("task")) { %>
                        <option>Enabled</option>
                        <%}%>
                        <option>Running</option>
                        <option>Completed</option>
                        <option>Last Event</option>
                    </select>
                    </td>
                </tr>
                <tr>
                    <td>
                    </td>

                    <td>
                        <select name="timeoption">
                            <option>--Select Option--</option>
                            <option>At</option>
                            <option>After</option>
                            <option>Before</option>

                            <!--			<option>Less than</option>
                            <option>More than</option> -->
                        </select>	
                    </td>
                </tr>
                <tr>
                    <td>
                    </td>

                    <td>
                        <input type="Text" name="lengthoftime" maxlength="25" size="25">
                        <a href="javascript:cal4.popup();"><img src="img/cal.gif" width="16" height="16" border="0" alt="Click Here to Pick up the date"></a><br>

                    </td>

                    <!--		<td>
                    <select name="status_granularity">
                    <option>Seconds</option>
                    <option>Minutes</option>
                    <option>Hours</option>
                    <option>Days</option>
                    <option>Months</option>
                    <option>Years</option>
                    </select>		
                    </td> -->


	
                </tr>
	
                <tr>
                    <td>	
                    </td>
                    <td>	
                        -----------------------
                    </td>
                </tr>

                <% if (querytype!=null) { %>
                <tr>
                    <td>Interval Filter</td>
                    <td><select name="intervaloption">
                        <option>--Select Option--</option>
                        <option>Enabled - Completed</option>
                        <% if (querytype!=null && querytype.equals("task")) { %>
                        <option>Started - Completed</option>
                        <option>Enabled - Started</option>
                        <%}%>
                    </select>
                    </td>
                </tr>
                <tr>
                    <td>
                    </td>
                    <td><select name="timearea">
                        <option>More</option>
                        <option>Less</option>
                    </select>	
                    </td>
                </tr>
                <tr>
                    <td>
                    </td>	
                    <td><input type="text" name="timeforarea"/></td>
                    <td>
                </tr>
                <tr>
                    <td>
                    </td>
                    <td>
		
                        <select name="interval_granularity">
                            <option>Seconds</option>
                            <option>Minutes</option>
                            <option>Hours</option>
                            <option>Days</option>
                            <option>Percent</option>
                            <!--			<option>Months</option>
                            <option>Years</option> -->
                        </select>	
                    </td>
                    <td>

                    </td>
                </tr>
                <% } %>
                <tr>
                    <td><input type="submit" value="Add Filter" name="action"/>
                    </td>
                </tr>

                <tr>

                    <td>	
                        -----------------------
                    </td>
                </tr>


                <%
                    String filter = request.getParameter("filter");
                    if (filter !=null && filter.equals("true")) {
                %>


                <tr>
                    <td>
                        STEP 3: 
                    </td>
                </tr>




                <tr>
                    <td>
                        <%if (view.equals("Chart")) {%>
		
                        <tr height=20>
                            <td>
                                <input type="submit" value="Create chart" name="action"/>
                            </td>
                        </tr>
			
                        <tr>
                            <td>Select Graph Type</td>
                            <td><select name="graphtype">
                                <option <%if (querygraph!=null && querygraph.equals("Bar chart")) {out.print("SELECTED");}%>>Bar chart</option>
                                <option <%if (querygraph!=null && querygraph.equals("Pie chart")) {out.print("SELECTED");}%>>Pie chart</option>
                            </select>
                            </td>
                        </tr>

                        <tr>
                            <td>
                                Display Element (grouping):
                            </td>
                            <td>
                                <select name="groupbyvalue">
                                    <option>Specification</option>
                                    <option>Resource</option>
                                    <% if (querytype!=null && querytype.equals("case")) { %>
                                    <option>Case Id</option>
                                    <% } else { %>
                                    <option>Task id</option>
                                    <% } %>
                                </select>
                            </td>

                        </tr>
                        <tr>

                        </tr>
		
                        <tr>
                            <td>
                                Display Value:
                            </td>

                            <% if (querytype!=null && querytype.equals("case")) { %>

                            <td>
                                <select name="chartvalue">
                                    <option>Started-Completed</option>
                                    <option>Count</option>
                                </select>
                            </td>
                        </tr>
                        <tr>
                            <td>
                            </td>
                            <% } 
                                else { %>
                            
                            <td>
                                <select name="chartvalue">
                                    <option>Enabled-Completed</option>
                                    <option>Started-Completed</option>
                                    <option>Enabled-Started</option>			
                                    <option>Count</option>
                                </select>
                            </td>

                            <% } %>

                        </tr>
                        <tr>
                            <td>
                            </td>
                            <td>
                                <select name="function">
                                    <option>Average</option>
                                </select>	
                            </td>
                        </tr>
                        <tr>
                            <td>
                            </td>
                            <td>
                                <select name="interval_chart_granularity">
                                    <option>Seconds</option>
                                    <option>Minutes</option>
                                    <option>Hours</option>
                                    <option>Days</option>
                                    <option>Months</option>
                                    <option>Years</option>
                                </select>	
                            </td>
                        </tr>
		
                    </td>

                    <td> 



                    <% } if (view.equals("Table")) {%>
                    <!--<table width="90%" border="0" bgcolor="#dddddd">-->

                    <tr height=20 colspan=2>
                        <td><input type="submit" value="Create table" name="action"/>
                    </tr>
                        <tr>
                            <td>
                                <a> Group By: </a>
                            </td>
                            <td>
                                <% if (querytype!=null && querytype.equals("case")) { %>
                                <select name="groupbyvalue" onchange="javascript:DisplayAggregationFunctions();">
                                    <option>Specification</option>
                                    <option>Resource</option>
                                    <option>No grouping</option>
                                </select>
                                <%} else { %>
                                <select name="groupbyvalue" onchange="javascript:DisplayAggregationFunctions();">
                                    <option>Task</option>
                                    <option>Resource</option>
                                    <option>Specification</option>
                                    <option>No grouping</option>
                                </select>
                                <%} %>
                            </td>
                        </tr>

                        <% if (querytype!=null && querytype.equals("case")) { %>


                        <tr> <td>Elements:</td>
                        </tr>
                        <td><input type="checkbox" name="created" value="Y"/>Time Created</td>

                        <td><input type="checkbox" name="completed" value="Y"/>Time Completed</td>

                        <td><input type="checkbox" name="cancelled" value="Y"/>Time Cancelled</td>

                        <td><input type="checkbox" name="specification" value="Y"/>Specification</td>

                        <td><input type="checkbox" name="Owner" value="Y"/>Owner</td>

                        <td><input type="checkbox" name="Count" value="Y"/>Count</td>


                    </td>
                </tr>
	

                    <tr> <td>Interval:</td>
                    </tr>
                    <tr>
                        <td><input type="checkbox" name="interval" value="intervalSC"/>Started - Completed</td>
                    </tr>

                    <tr>
                        <td>
                            <select name="function_sc">
                                <option>Average</option>
                            </select>	
                        </td>
                    </tr>	
	
                    <tr>
                        <td>
		
                            <select name="interval_table_granularity">
                                <option>Seconds</option>
                                <option>Minutes</option>
                                <option>Hours</option>
                                <option>Days</option>
                                <option>Months</option>
                                <option>Years</option>
                            </select>	
                        </td>
                    </tr>
                    <% } 
                        else { %>
                    <tr> <td>Elements:</td></tr>

                    <tr>
                        <td><input type="checkbox" name="ID" value="Y"/>Id</td>
                        <td><input type="checkbox" name="Task" value="Y"/>Task Name</td>
                    </tr>            		
                    <tr>
                        <td><input type="checkbox" name="Enabled" value="Y"/>Time Enabled</td>
                        <td><input type="checkbox" name="Started" value="Y"/>Time Started</td>
                        <td><input type="checkbox" name="Completed" value="Y"/>Time Completed</td>            			
                        <td><input type="checkbox" name="Owner" value="Y"/>Owner</td>
                        <td><input type="checkbox" name="specification" value="Y"/>Specification</td>
                        <td><input type="checkbox" name="Count" value="Y"/>Count</td>
                    </tr>

                    <tr> <td>Interval:</td></tr>

                    <tr>            		
                        <td><input type="checkbox" name="Enabled-Completed" value="Y"/>Enabled-Completed</td
                        <td><input type="checkbox" name="Started-Completed" value="Y"/>Started-Completed</td>
                        <td><input type="checkbox" name="Enabled-Started" value="Y"/>Enabled-Started</td>
                    </tr>

                    <tr>
                        <td>
                            <select name="function_ec">
                                <option>Average</option>

                            </select>	
                        </td>
                        <td>
                            <select name="function_sc">
                                <option>Average</option>

                            </select>	
                        </td>
                        <td>
                            <select name="function_es">
                                <option>Average</option>
                            </select>	
                        </td>
                    </tr>
                    <tr>
                        <td>
			
                            <select name="interval_table_granularity">
                                <option>Seconds</option>
                                <option>Minutes</option>
                                <option>Hours</option>
                                <option>Days</option>
                                <option>Months</option>
                                <option>Years</option>
                            </select>	
                        </td>
                    </tr>
                    <% } %>


                    <!--</table>-->
                    <% } %>
                </td>
                </tr>

                <% } 

                        } %>
                

                <script language="JavaScript">
                    DisplayAggregationFunctions();
                </script>
            </table>
        </form>
        <!--
        Title: Tigra Calendar
        URL: http://www.softcomplex.com/products/tigra_calendar/
        Version: 3.2
        Date: 10/14/2002 (mm/dd/yyyy)
        Note: Permission given to use this script in ANY kind of applications if
        header lines are left unchanged.
        Note: Script consists of two files: calendar?.js and calendar.html
        -->
        <script language="JavaScript">



            var calendars = [];
            // if two digit year input dates after this year considered 20 century.
            var NUM_CENTYEAR = 30;
            // is time input control required by default
            var BUL_TIMECOMPONENT = true;
            // are year scrolling buttons required by default
            var BUL_YEARSCROLL = true;


            var RE_NUM = /^\-?\d+$/;

            var cal4 = new calendar1(document.forms['buildquery'].elements['lengthoftime']);

            cal4.year_scroll = false;
            cal4.time_comp = true;

            function calendar1(obj_target) {

            // assigning methods
            this.gen_date = cal_gen_date1;
            this.gen_time = cal_gen_time1;
            this.gen_tsmp = cal_gen_tsmp1;
            this.prs_date = cal_prs_date1;
            this.prs_time = cal_prs_time1;
            this.prs_tsmp = cal_prs_tsmp1;
            this.popup    = cal_popup1;

            // validate input parameters
            if (!obj_target)
            return;
            if (obj_target.value == null)
            return;


            this.target = obj_target;
            this.time_comp = BUL_TIMECOMPONENT;
            this.year_scroll = BUL_YEARSCROLL;
	
            // register in global collections

            this.id = calendars.length;
            calendars[0] = this;


            }

            function cal_popup1 (str_datetime) {
            if (str_datetime) {
            this.dt_current = this.prs_tsmp(str_datetime);
            }
            else {
            this.dt_current = this.prs_tsmp(this.target.value);
            this.dt_selected = this.dt_current;
            }
            if (!this.dt_current) return;

            var obj_calwindow = window.open(
            'calendar.html?datetime=' + this.dt_current.valueOf()+ '&id=' + this.id,
            'Calendar', 'width=200,height='+(this.time_comp ? 215 : 190)+
            ',status=no,resizable=no,top=200,left=200,dependent=yes,alwaysRaised=yes'
            );
            obj_calwindow.opener = window;
            obj_calwindow.focus();
            }

            // timestamp generating function
            function cal_gen_tsmp1 (dt_datetime) {
            return(this.gen_date(dt_datetime) + ' ' + this.gen_time(dt_datetime));
            }

            // date generating function
            function cal_gen_date1 (dt_datetime) {
            return (
            (dt_datetime.getDate() < 10 ? '0' : '') + dt_datetime.getDate() + "-"
            + (dt_datetime.getMonth() < 9 ? '0' : '') + (dt_datetime.getMonth() + 1) + "-"
            + dt_datetime.getFullYear()
            );
            }
            // time generating function
            function cal_gen_time1 (dt_datetime) {
            return (
            (dt_datetime.getHours() < 10 ? '0' : '') + dt_datetime.getHours() + ":"
            + (dt_datetime.getMinutes() < 10 ? '0' : '') + (dt_datetime.getMinutes()) + ":"
            + (dt_datetime.getSeconds() < 10 ? '0' : '') + (dt_datetime.getSeconds())
            );
            }

            // timestamp parsing function
            function cal_prs_tsmp1 (str_datetime) {
            // if no parameter specified return current timestamp
            if (!str_datetime)
            return (new Date());

            // if positive integer treat as milliseconds from epoch
            if (RE_NUM.exec(str_datetime))
            return new Date(str_datetime);
		
            // else treat as date in string format
            var arr_datetime = str_datetime.split(' ');
            return this.prs_time(arr_datetime[1], this.prs_date(arr_datetime[0]));
            }

            // date parsing function
            function cal_prs_date1 (str_date) {

            var arr_date = str_date.split('-');

            if (arr_date.length != 3) return cal_error ("Invalid date format: '" + str_date + "'.\nFormat accepted is dd-mm-yyyy.");
            if (!arr_date[0]) return cal_error ("Invalid date format: '" + str_date + "'.\nNo day of month value can be found.");
            if (!RE_NUM.exec(arr_date[0])) return cal_error ("Invalid day of month value: '" + arr_date[0] + "'.\nAllowed values are unsigned integers.");
            if (!arr_date[1]) return cal_error ("Invalid date format: '" + str_date + "'.\nNo month value can be found.");
            if (!RE_NUM.exec(arr_date[1])) return cal_error ("Invalid month value: '" + arr_date[1] + "'.\nAllowed values are unsigned integers.");
            if (!arr_date[2]) return cal_error ("Invalid date format: '" + str_date + "'.\nNo year value can be found.");
            if (!RE_NUM.exec(arr_date[2])) return cal_error ("Invalid year value: '" + arr_date[2] + "'.\nAllowed values are unsigned integers.");

            var dt_date = new Date();
            dt_date.setDate(1);

            if (arr_date[1] < 1 || arr_date[1] > 12) return cal_error ("Invalid month value: '" + arr_date[1] + "'.\nAllowed range is 01-12.");
            dt_date.setMonth(arr_date[1]-1);
	 
            if (arr_date[2] < 100) arr_date[2] = Number(arr_date[2]) + (arr_date[2] < NUM_CENTYEAR ? 2000 : 1900);
            dt_date.setFullYear(arr_date[2]);

            var dt_numdays = new Date(arr_date[2], arr_date[1], 0);
            dt_date.setDate(arr_date[0]);
            if (dt_date.getMonth() != (arr_date[1]-1)) return cal_error ("Invalid day of month value: '" + arr_date[0] + "'.\nAllowed range is 01-"+dt_numdays.getDate()+".");

            return (dt_date)
            }

            // time parsing function
            function cal_prs_time1 (str_time, dt_date) {

            if (!dt_date) return null;
            var arr_time = String(str_time ? str_time : '').split(':');

            if (!arr_time[0]) dt_date.setHours(0);
            else if (RE_NUM.exec(arr_time[0]))
            if (arr_time[0] < 24) dt_date.setHours(arr_time[0]);
            else return cal_error ("Invalid hours value: '" + arr_time[0] + "'.\nAllowed range is 00-23.");
            else return cal_error ("Invalid hours value: '" + arr_time[0] + "'.\nAllowed values are unsigned integers.");
	
            if (!arr_time[1]) dt_date.setMinutes(0);
            else if (RE_NUM.exec(arr_time[1]))
            if (arr_time[1] < 60) dt_date.setMinutes(arr_time[1]);
            else return cal_error ("Invalid minutes value: '" + arr_time[1] + "'.\nAllowed range is 00-59.");
            else return cal_error ("Invalid minutes value: '" + arr_time[1] + "'.\nAllowed values are unsigned integers.");

            if (!arr_time[2]) dt_date.setSeconds(0);
            else if (RE_NUM.exec(arr_time[2]))
            if (arr_time[2] < 60) dt_date.setSeconds(arr_time[2]);
            else return cal_error ("Invalid seconds value: '" + arr_time[2] + "'.\nAllowed range is 00-59.");
            else return cal_error ("Invalid seconds value: '" + arr_time[2] + "'.\nAllowed values are unsigned integers.");

            dt_date.setMilliseconds(0);
            return dt_date;
            }

            function cal_error (str_message) {
            alert (str_message);
            return null;
            }



        </script>
        <p/>
        <%
            } else {
                out.println("<a> <font color=\"red\">This page has been disabled because persistence is switched off!</font></a>");
            }
        %>


        <%@include file="footer.jsp" %>
    </body>


</html>