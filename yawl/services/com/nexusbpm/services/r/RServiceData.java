/**
 * 
 */
package com.nexusbpm.services.r;

import java.io.IOException;

import org.rosuda.JRclient.RSession;

import com.nexusbpm.services.data.NexusServiceData;
import com.nexusbpm.services.data.Variable;
import com.nexusbpm.services.sql.SqlServiceData.Column;

/**
 * @author Matthew Sandoz
 *
 */
public class RServiceData extends NexusServiceData {

	//@TODO Dont forget unique ID column (Where will it go?)
	public enum Column {
		SESSION_IN("sessionIn", Variable.TYPE_OBJECT), 
		SESSION_OUT("sessionOut", Variable.TYPE_OBJECT),
		KEEP_SESSION("keepSession", Variable.TYPE_TEXT),
		SERVER("serverAddress", Variable.TYPE_TEXT), 
		CODE("code", Variable.TYPE_TEXT), 
		OUTPUT("output", Variable.TYPE_TEXT), 
		ERROR("error", Variable.TYPE_TEXT);
		private String name, type;
		Column(String name, String type) {this.name = name;this.type = type;}
		public String getName() {return name;}
		public String getType() {return type;}
		public String toString() {return name;}
	}

	public RServiceData() {
		super.initList();
		for (Column column: Column.values()) {
			variable.add(new Variable(column.getName(), column.getType(), null));
		}
	}

	public RServiceData(NexusServiceData data) throws ClassNotFoundException {
		this();
		for (Column column: Column.values()) {
			getVariable(column.getName()).setValue(data.getVariable(column.getName()).getValue());
		}
	}
	
	public String getCode() {
		return getPlain(Column.CODE.toString());
	}

	public void setCode(String code) {
		setPlain(Column.CODE.toString(), code);
	}

	public String getError() {
		return getPlain(Column.ERROR.toString());
	}

	public void setError(String error) {
		setPlain(Column.ERROR.toString(), error);
	}

	public String getOutput() {
		return getPlain(Column.OUTPUT.toString());
	}

	public void setOutput(String output) {
		setPlain(Column.OUTPUT.toString(), output);
	}

	public String getServer() {
		return getPlain(Column.SERVER.toString());
	}

	public void setServer(String server) {
		setPlain(Column.SERVER.toString(), server);
	}

	public RSession getSessionIn() throws IOException, ClassNotFoundException {
		return (RSession) getObject(Column.SESSION_IN.toString());
	}

	public void setSessionIn(RSession sessionIn) throws IOException {
		setObject(Column.SESSION_IN.toString(), sessionIn);
	}

	public RSession getSessionOut() throws IOException, ClassNotFoundException {
		return (RSession) getObject(Column.SESSION_OUT.toString());
	}

	public void setSessionOut(RSession sessionOut) throws IOException {
		setObject(Column.SESSION_OUT.toString(), sessionOut);
	}

	public Boolean getKeepSession() {
		return Boolean.parseBoolean(getPlain(Column.KEEP_SESSION.toString()));
	}

	public void setKeepSession(Boolean keepSession) {
		setPlain(Column.SERVER.toString(), keepSession.toString());
	}

}
