package com.nexusbpm.services.jython;

import java.io.IOException;
import java.io.StringReader;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;


public class XmlUtil {
	
	private XmlUtil() {};
	
	public static Document xmlToDocument( String xml ) throws JDOMException, IOException {
		SAXBuilder builder = new SAXBuilder();
		return builder.build(new StringReader(xml));
	}
	
	public static String unflattenXML( String flatXML ) {
		return flatXML.replaceAll( "&lt;", "<" ).replaceAll( "&gt;", ">" );
	}
	
	public static String flattenXML( String xml ) {
		return xml.replaceAll( "<", "&lt;" ).replaceAll( ">", "&gt;" );
	}
}
