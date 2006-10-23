package au.edu.qut.yawl.events;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import junit.framework.TestCase;

public class HsqlTest extends TestCase {

	Connection conn;
	
	protected void setUp() throws Exception {
		super.setUp();
		Class.forName("org.hsqldb.jdbcDriver").newInstance();
		conn = DriverManager.getConnection("jdbc:hsqldb:mem:openjms", "sa", "");
		String setupDb = readStreamAsString(this.getClass().getResourceAsStream("/create_hsql.sql"));
		PreparedStatement s = conn.prepareStatement(setupDb);
		s.execute();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		conn.createStatement().execute("SHUTDOWN");
	}

    private static String readStreamAsString(InputStream file)
    throws java.io.IOException{
        StringBuffer fileData = new StringBuffer(1000);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(file));
        char[] buf = new char[1024];
        int numRead=0;
        while((numRead=reader.read(buf)) != -1){
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
            buf = new char[1024];
        }
        reader.close();
        return fileData.toString();
    }	
    
    public void testX() {
    	try {
			assertNull(conn.getCatalog());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public void testCreated() {
    	try {
			ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM messages");
			int cc = rs.getMetaData().getColumnCount();
			assertEquals(7, cc);			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }
	
}
