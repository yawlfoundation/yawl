/**
 * 
 */
package com.nexusbpm.services.sas;

import java.io.IOException;

import org.rosuda.JRclient.RSession;

import com.nexusbpm.services.data.NexusServiceData;
import com.nexusbpm.services.data.Variable;

/**
 * @author Matthew Sandoz
 *
 */
public class SasServiceData extends NexusServiceData {

	//@TODO Dont forget unique ID column (Where will it go?)
	public enum Column {
		JDBC_CLASS("jdbcClass", Variable.TYPE_TEXT), 
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

	public SasServiceData() {
		super.initList();
		for (Column column: Column.values()) {
			variable.add(new Variable(column.getName(), column.getType(), null));
		}
	}

	public SasServiceData(NexusServiceData data) throws IOException, ClassNotFoundException {
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

	protected String getError() {
		return getPlain(Column.ERROR.getName());
	}

	protected void setError(String error) {
		setPlain(Column.ERROR.toString(), error);
	}

	protected String getJdbcClass() {
		return getPlain(Column.JDBC_CLASS.getName());
	}

	protected void setJdbcClass(String jdbcClass) {
		setPlain(Column.JDBC_CLASS.toString(), jdbcClass);
	}

	protected String getJdbcUri() {
		return getPlain(Column.JDBC_URI.getName());
	}

	protected void setJdbcUri(String jdbcUri) {
		setPlain(Column.JDBC_URI.toString(), jdbcUri);
	}

	protected String getOutput() {
		return getPlain(Column.OUTPUT.getName());
	}

	protected void setOutput(String output) {
		setPlain(Column.OUTPUT.toString(), output);
	}

	protected String getPassword() {
		return getPlain(Column.PASSWORD.getName());
	}

	protected void setPassword(String password) {
		setPlain(Column.PASSWORD.toString(), password);
	}

	protected String getRecordCount() {
		return getPlain(Column.RECORD_COUNT.getName());
	}

	protected void setRecordCount(String recordCount) {
		setPlain(Column.RECORD_COUNT.toString(), recordCount);
	}

	protected String getSqlCode() {
		return getPlain(Column.SQL_CODE.getName());
	}

	protected void setSqlCode(String sqlCode) {
		setPlain(Column.SQL_CODE.toString(), sqlCode);
	}

	protected String getStatementType() {
		return getPlain(Column.STATEMENT_TYPE.getName());
	}

	protected void setStatementType(String statementType) {
		setPlain(Column.STATEMENT_TYPE.toString(), statementType);
	}

	protected String getTableUri() {
		return getPlain(Column.TABLE_URI.getName());
	}

	protected void setTableUri(String tableUri) {
		setPlain(Column.TABLE_URI.toString(), tableUri);
	}

	protected String getUserName() {
		return getPlain(Column.USER_NAME.getName());
	}

	protected void setUserName(String userName) {
		setPlain(Column.USER_NAME.toString(), userName);
	}

}
