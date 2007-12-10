<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<title>Persistance Failure</title>
	</head>
	<body>
    <% String toolName = request.getParameter("tool"); %>
		<h3>Persistance Failure</h3>
        <p>
            <font color='red'>
                <em>
                A failure has occured whilst connecting <%= toolName %> to the
                database. Check the status of the database connection defined
                for <%= toolName %>, and perhaps restart <%= toolName %> web application.
                Further information may be found within the Tomcat log files.
                </em>
            </font>
        </p>
	</body>
</html>
