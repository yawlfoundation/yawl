package com.nexusbpm.services.sql;

import junit.framework.TestCase;

public class SqlComponentTest extends TestCase {

	SqlServiceData data;
	
	public SqlComponentTest() {
		data = new SqlServiceData();
		data.setJdbcClass("org.postgresql.Driver");
		data.setJdbcUri("jdbc:postgresql:yawl");
		data.setUserName("postgres");
		data.setPassword("admin");
		data.setSqlCode("select * from yspecification");		
	}
	
	public void testRun() throws Exception{
		InternalSqlService c = new InternalSqlService();
		SqlServiceData outputData = (SqlServiceData) c.execute(data);
		System.out.println(outputData.getOutput());
		System.out.println(outputData.getError());
		System.out.println(outputData.getTableUri());
	}

}
