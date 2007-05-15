/**
 * 
 */
package com.nexusbpm.services.sql;

import java.io.IOException;

import org.rosuda.JRclient.RSession;

import com.nexusbpm.services.data.NexusServiceData;
import com.nexusbpm.services.data.Variable;

/**
 * @author Matthew Sandoz
 *
 */
public class SqlServiceData extends NexusServiceData {

	//@TODO Dont forget unique ID column (Where will it go?)
	public enum Column {
		JDBC_DRIVER_CLASS("jdbcDriverClass", Variable.TYPE_TEXT), 
		JDBC_URI("jdbcUri", Variable.TYPE_TEXT),
		USER_NAME("userName", Variable.TYPE_TEXT),
		PASSWORD("password", Variable.TYPE_TEXT), 
		SQL_CODE("sqlCode", Variable.TYPE_TEXT),
		STATEMENT_TYPE("statementType", Variable.TYPE_TEXT),
		TABLE_URI("tableUri", Variable.TYPE_TABLE_URI),
		RECORD_COUNT("recordCount", Variable.TYPE_INT),
		OUTPUT("output", Variable.TYPE_TEXT), 
		ERROR("error", Variable.TYPE_TEXT);
		private String name, type;
		Column(String name, String type) {this.name = name;this.type = type;}
		public String getName() {return name;}
		public String getType() {return type;}
		public String toString() {return name;}
	}

	public SqlServiceData() {
		super.initList();
		for (Column column: Column.values()) {
			variable.add(new Variable(column.getName(), column.getType(), null));
		}
	}

	public SqlServiceData(NexusServiceData data) throws IOException, ClassNotFoundException {
		this();
		for (Variable var: data.getVariables()) {
			Variable myVar = this.getOrCreateVariable(var.getName());
			myVar.setType(var.getType());
			myVar.setValue(var.getValue());
		}
	}
//prototypes for copying	
//		return Boolean.parseBoolean(getPlain(Column.KEEP_SESSION.toString()));
//		setPlain(Column.SERVER.toString(), keepSession.toString());

	public String getError() {
		return getPlain(Column.ERROR.getName());
	}

	public void setError(String error) {
		setPlain(Column.ERROR.toString(), error);
	}

	public String getJdbcDriverClass() {
		return getPlain(Column.JDBC_DRIVER_CLASS.getName());
	}

	public void setJdbcClass(String jdbcClass) {
		setPlain(Column.JDBC_DRIVER_CLASS.toString(), jdbcClass);
	}

	public String getJdbcUri() {
		return getPlain(Column.JDBC_URI.getName());
	}

	public void setJdbcUri(String jdbcUri) {
		setPlain(Column.JDBC_URI.toString(), jdbcUri);
	}

	public String getOutput() {
		return getPlain(Column.OUTPUT.getName());
	}

	public void setOutput(String output) {
		setPlain(Column.OUTPUT.toString(), output);
	}

	public String getPassword() {
		return getPlain(Column.PASSWORD.getName());
	}

	public void setPassword(String password) {
		setPlain(Column.PASSWORD.toString(), password);
	}

	public String getRecordCount() {
		return getPlain(Column.RECORD_COUNT.getName());
	}

	public void setRecordCount(String recordCount) {
		setPlain(Column.RECORD_COUNT.toString(), recordCount);
	}

	public String getSqlCode() {
		return getPlain(Column.SQL_CODE.getName());
	}

	public void setSqlCode(String sqlCode) {
		setPlain(Column.SQL_CODE.toString(), sqlCode);
	}

	public String getStatementType() {
		return getPlain(Column.STATEMENT_TYPE.getName());
	}

	public void setStatementType(String statementType) {
		setPlain(Column.STATEMENT_TYPE.toString(), statementType);
	}

	public String getTableUri() {
		return getPlain(Column.TABLE_URI.getName());
	}

	public void setTableUri(String tableUri) {
		setPlain(Column.TABLE_URI.toString(), tableUri);
	}

	public String getUserName() {
		return getPlain(Column.USER_NAME.getName());
	}

	public void setUserName(String userName) {
		setPlain(Column.USER_NAME.toString(), userName);
	}

}
