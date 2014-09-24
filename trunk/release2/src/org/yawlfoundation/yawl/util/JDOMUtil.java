/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.util;

import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.ElementFilter;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaderSAX2Factory;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.yawlfoundation.yawl.schema.XSDType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;


/**
 * Some static utility methods for converting JDOM Documents and Elements
 * to Strings and files & vice versa.
 *
 *  @author Michael Adams
 *  04/07/2006
 *
 *  Last date: 22/06/08
 */

public class JDOMUtil {

    private static Logger _log = Logger.getLogger(JDOMUtil.class);
    private static SAXBuilder _builder = new SAXBuilder(
            new XMLReaderSAX2Factory(false, "org.apache.xerces.parsers.SAXParser"));


    /****************************************************************************/

    public static String documentToString(Document doc) {
        if (doc == null) return null;
        XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
        return out.outputString(doc);
    }

    /****************************************************************************/

    public static String documentToStringDump(Document doc) {
        if (doc == null) return null;
        XMLOutputter out = new XMLOutputter(Format.getCompactFormat());
        return out.outputString(doc);
    }

    /****************************************************************************/

    public static String elementToString(Element e) {
        if (e == null) return null ;
        XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
        return out.outputString(e);
    }

    /****************************************************************************/

    public static String elementToStringDump(Element e) {
        if (e == null) return null ;
        XMLOutputter out = new XMLOutputter(Format.getCompactFormat());
        return out.outputString(e);
    }

    /****************************************************************************/

    public synchronized static Document stringToDocument(String s) {
        try {
            _builder.setIgnoringBoundaryWhitespace(true);            
            return (s != null) ? _builder.build(new StringReader(s)) : null ;
        }
        catch (JDOMException jde) {
            _log.error("JDOMException converting to Document, String = " + s , jde);
        }
        catch (IOException ioe) {
            _log.error("IOException converting to Document, String = " + s, ioe);
        }
        return null ;
    }

    /****************************************************************************/

    public static Element stringToElement(String s) {
        if (s == null) return null ;
        Document doc = stringToDocument(s);
        return (doc != null) ? doc.getRootElement() : null;
    }

    /****************************************************************************/

    public static Document fileToDocument(String path) {
        return fileToDocument(new File(path));
    }


    public synchronized static Document fileToDocument(File file) {
        try {
            return (file != null && file.exists()) ? _builder.build(file) : null ;
        }
        catch (JDOMException jde) {
            _log.error("JDOMException loading file into Document, filepath = " +
                    file.getAbsolutePath(), jde);
        }
        catch (IOException ioe) {
            _log.error("IOException loading file into Document, filepath = " +
                    file.getAbsolutePath(), ioe);
        }
        return null ;
    }

    /****************************************************************************/

    /** saves a JDOM Document to a file */
    public static void documentToFile(Document doc, String path)   {
        try {
            FileOutputStream fos = new FileOutputStream(path);
            XMLOutputter xop = new XMLOutputter(Format.getPrettyFormat());
            xop.output(doc, fos);
            fos.flush();
            fos.close();
        }
        catch (IOException ioe){
            _log.error("IO Exeception in saving Document to file, filepath = " + path, ioe) ;
        }
    }

    /****************************************************************************/

    public static String getDefaultValueForType(String dataType) {
        if (dataType == null) return "null";
        else if (dataType.equalsIgnoreCase("boolean")) return "false" ;
        else if (dataType.equalsIgnoreCase("string") ||
                (! XSDType.isBuiltInType(dataType))) return "" ;
        else return "0";
    }

    /****************************************************************************/

    public static String encodeEscapes(String s) {
        if (s == null) return s;
        StringBuilder sb = new StringBuilder(s.length());
        for (char c : s.toCharArray()) {
            switch(c) {
                case '\'' : sb.append("&apos;"); break;
                case '\"' : sb.append("&quot;"); break;
                case '>'  : sb.append("&gt;"); break;
                case '<'  : sb.append("&lt;"); break;
                case '&'  : sb.append("&amp;"); break;
                default   : sb.append(c);
            }
        }
        return sb.toString();
    }


    public static String decodeEscapes(String s) {
        if ((s == null) || (s.indexOf('&') < 0)) return s;  // short circuit if no encodes
        return s.replaceAll("&lt;","<")
                .replaceAll("&gt;", ">")
                .replaceAll("&quot;","\"")
                .replaceAll("&apos;", "'")
                .replaceAll("&amp;", "&");
    }

    /****************************************************************************/

    public static Element selectElement(Document doc, String path) {
        XPathExpression<Element> expression =
                XPathFactory.instance().compile(path, new ElementFilter());
        return expression.evaluateFirst(doc);
    }


    public static String formatXMLString(String s) {
        if (s == null) return null;
        if (s.startsWith("<?xml"))
            return formatXMLStringAsDocument(s);
        else
            return formatXMLStringAsElement(s);
    }

    public static String formatXMLStringAsDocument(String s) {
        return documentToString(stringToDocument(s));
    }

    public static String formatXMLStringAsElement(String s) {
        return elementToString(stringToElement(s));
    }

    public static String strip(String s) {
        Element e = stringToElement(s);
        if (e != null) {
            s = e.getText();
        }
        return s;
    }

    public static Element stripAttributes(Element e) {
        e.setAttributes(null);
        for (Element child: e.getChildren()) {
            stripAttributes(child);      // recurse
        }
        return e;
    }

} //ends
