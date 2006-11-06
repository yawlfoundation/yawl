/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package com.nexusbpm.editor.util.dbproxy;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * A row from the database.
 * Must be serializable because it is in the graph of the bean.
 */
class Row implements Serializable {
	
	public Row( int index, ResultSet resultSet ) throws SQLException {
		_key = new Integer( index );
		_data = new ArrayList();
		int columnCount = resultSet.getMetaData().getColumnCount();
		for( int j = 1; j <= columnCount; j++ ) {
			_data.add( resultSet.getObject( j ) );
		}//for
	}//Row
	
	public boolean equals( Object object ) {
		if( ! (object instanceof Row) ) return false;
		Row row = (Row) object;
		return row.index() == index();
	}//equals()
	
	private Integer _key;
	public int index() {
		return _key.intValue();
	}//index()
	public Integer key() {
		return _key;
	}//key()
	
	private List _data;
	public List data() {
		return _data;
	}//row()
	
}