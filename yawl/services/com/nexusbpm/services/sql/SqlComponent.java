package com.nexusbpm.services.sql;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <b>Use case:</b><br>
 * <p>Verifies that the database address, username/password, database name, and
 * sql query are all valid such that the database can be queried by another
 * component without problems.
 * <p>Also will construct a SqlQueryAttribute given the database type, database
 * name, username, password, database address, and a query string. The
 * SqlQueryAttribute will be executed on the database to ensure correctness.
 * <p>This is mainly used to save database information and to verify the
 * correctness of a given query string.
 *
 * @author Matthew Sandoz
 * @author Dean Mao
 * @created February 19, 2003
 * @changed May 1, 2007
 * @hibernate.subclass discriminator-value="27"
 * @javabean.class name="SqlComponent"
 * displayName="Sql Component"
 */
public class SqlComponent {

	private static final long serialVersionUID = 8491891469655283586L;
	private static final Log LOG = LogFactory.getLog( SqlComponent.class );
	private SqlServiceData data;
	
	public SqlComponent( SqlServiceData data) {
		this.data = data;
	}

	public SqlServiceData run() throws Exception {
		Connection connection = null;
		Statement statement = null;
		OutputStream fos = null;
		File outputFile = null;
		try {
			Class.forName(data.getJdbcDriverClass());
			connection = java.sql.DriverManager.getConnection(data.getJdbcUri(), data.getUserName(), data.getPassword());
			statement = connection.createStatement();
			ResultSet results = statement.executeQuery( data.getSqlCode() );
			System.out.println(new File(".").getAbsolutePath());
			File parentDir = new File("taskdata");
			parentDir.mkdirs();
			outputFile = File.createTempFile("sql" + System.currentTimeMillis() + "-", ".csv", parentDir);
			fos = new FileOutputStream(outputFile);
			List<String> columnNames = new ArrayList<String>();
			for (int column = 1; column <= results.getMetaData().getColumnCount(); column++) {
				columnNames.add(results.getMetaData().getColumnName(column));
			}
			CsvWriter writer = new CsvWriter(fos);
			writer.writeColumnHeader(columnNames);
			List<Object> columnValues = new ArrayList<Object>();
			while (results.next()) {
				columnValues.clear();
				for (int column = 1; column <= results.getMetaData().getColumnCount(); column++) {
					columnValues.add(results.getObject(column));
				}
				writer.writeRow(columnValues);
			}
			writer.flushAndClose();
			String server = InetAddress.getLocalHost().getHostAddress();
			data.setTableUri("http://" + server + ":8080/" + outputFile.getPath().replace("\\", "/")); //will have to translate this
		}//try
		catch(Exception e) {
			data.setError(e.getMessage());
			e.printStackTrace();
		}
		finally {
			fos.flush();
			if (fos != null) fos.close();
		}//finally
		return data;
	}//run()
		
}//SqlComponent
