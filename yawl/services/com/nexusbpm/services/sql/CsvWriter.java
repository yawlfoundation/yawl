package com.nexusbpm.services.sql;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * According to http://tcllib.sourceforge.net/doc/csv.html#SECTid1405
 * The format of regular CSV files is specified as
 * 1. Each record of a csv file (comma-separated values, as exported e.g. by Excel) is a set of ASCII values separated by ",". For other languages it may be ";" however, although this is not important for this case as the functions provided here allow any separator character.
 * 2. If and only if a value contains itself the separator ",", then it (the value) has to be put between "". If the value does not contain the separator character then quoting is optional.
 * 3. If a value contains the character ", that character is represented by "".
 * 4. The output string "" represents the value ". In other words, it is assumed that it was created through rule 3, and only this rule, i.e. that the value was not quoted.
 */
public class CsvWriter {

	private static final Log LOG = LogFactory.getLog( CsvWriter.class );

	private BufferedWriter writer;
	
	public CsvWriter( OutputStream os) throws Exception {
		this.writer = new BufferedWriter(new OutputStreamWriter(os));
	}

	public void flushAndClose() throws IOException{
		writer.flush();
		writer.close();
	}
	
	public void writeColumnHeader(List<String> columns) throws IOException{
		for( int col = 0; col < columns.size(); col++ ) {
			if( col > 0 ) {
				writer.write( "," );
			}
			writer.write( columns.get(col) );
		}
		writer.newLine();	
	}
	
	public void writeRow(List<Object> values) throws Exception {
		Object object = null;
		for( int col = 0; col < values.size(); col++ ) {
			if( col > 0 ) {
				writer.write( "," );
			}
			writer.write( formatObject( values.get(col)) );
			if( null == object ) {
				object = "null";
			}
		}
		writer.newLine();
	}

	/**
	 * Format an object for CSV.  Essentially this means replacing all " with ""
	 * and wrapping the string in quotes if it contains an embedded comma.  Also if the object
	 * is null the string is replaced with the text "null".
	 *
	 * @param object
	 * @return CSV formatted string
	 */
	public String formatObject( Object object ) {
		String ret;

		if( null != object ) {
			String tmp = object.toString();
			int quoteIndex = tmp.indexOf( '\"' );
			int commaIndex = tmp.indexOf( ',' );
			if( quoteIndex >= 0 ) {
				tmp = tmp.replaceAll( "\"", "\"\"" );
			}
			if( commaIndex >= 0 || (quoteIndex >= 0 && tmp.length() > 2) ) {
				ret = "\"" + tmp + "\"";
			}
			else
				ret = tmp;
		}
		else
			ret = "null";

		return ret;
	}
}
