package com.nexusbpm.services.util;

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
	
	public static String unmarshal(String expression) {
        if (expression != null) {
        	expression = expression.replaceAll("&apos;", "'");
        	expression = expression.replaceAll("&qout;", "\"");
        	expression = expression.replaceAll("&gt;", ">");
        	expression = expression.replaceAll("&lt;", "<");
            expression = expression.replaceAll("&amp;", "&");
        }
        return expression;
    }
	
	public static String marshal(String expression) {
        if (expression != null) {
            expression = expression.replaceAll("&", "&amp;");
            expression = expression.replaceAll("<", "&lt;");
            expression = expression.replaceAll(">", "&gt;");
            expression = expression.replaceAll("\"", "&qout;");
            expression = expression.replaceAll("'", "&apos;");
        }
        return expression;
    }
	
	public static void main(String[] args) {
//		String test = "<root>\n\t" +
//			"<child1>some text for child 1...\n\t</child1>\n\t" +
//			"<child2 attr=\"value\"/>\n\t" +
//			"<child3 at1=\"val1\" atr2=\"val2\">txt</child3>\n" +
//			"</root>";
//		String pre = test;
//		System.out.println( "pre:\n" + test );
//		test = flattenXML( test );
//		System.out.println( "post:\n" + test );
//		test = unflattenXML( test );
//		System.out.println( "post, post:\n" + test );
//		System.out.println( "post == pre:" + pre.equals( test ) );
		String test = "here's something & something else";
		System.out.println(test);
		test = wrap( "a", test );
		System.out.println( test );
		System.out.println( unwrap( test ) );
		test = wrap( "b", test );
		System.out.println(test);
		System.out.println( unwrap( test ) );
		System.out.println( unwrap( unwrap( test ) ) );
	}
	
	public static String wrap( String name, String value ) {
		return "<" + name + ">" + marshal( value ) + "</" + name + ">";
	}
	
	public static String unwrap( String xml ) {
		String ret = xml.substring( xml.indexOf( ">" ) + 1 );
		ret = ret.substring( 0, ret.lastIndexOf( "</" ) );
		return XmlUtil.unmarshal( ret );
	}
}
