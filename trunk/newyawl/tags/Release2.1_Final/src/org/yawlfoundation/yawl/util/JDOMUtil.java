/*
 * Copyright (c) 2004-2010 The YAWL Foundation. All rights reserved.
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
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;


/**
 * Some static utility methods for coverting JDOM Documents and Elements
 * to Strings and files & vice versa.
 *
 *  @author Michael Adams
 *  04/07/2006
 *
 *  Last date: 22/06/08
 */

 public class JDOMUtil {

    private static Logger _log = Logger.getLogger("org.yawlfoundation.yawl.util.JDOMUtil");

    /****************************************************************************/

    public static String documentToString(Document doc) {
        if (doc == null) return null;
        XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
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

    public static Document stringToDocument(String s) {
        try {
           if (s == null) return null ;
           return new SAXBuilder().build(new StringReader(s));
        }
        catch (JDOMException jde) {
            _log.error("JDOMException converting to Document, String = " + s , jde);
            return null ;
        }
        catch (IOException ioe) {
            _log.error("IOException converting to Document, String = " + s, ioe);
            return null ;
        }
    }

    /****************************************************************************/

    public static Element stringToElement(String s) {
        if (s == null) return null ;
        Document doc = stringToDocument(s);
        return (doc != null) ? doc.getRootElement() : null;
    }

    /****************************************************************************/

    public static Document fileToDocument(String path) {
       try {
           if (path == null) return null ;
           return new SAXBuilder().build(new File(path));
       }
       catch (JDOMException jde) {
           _log.error("JDOMException loading file into Document, filepath = " + path , jde);
           return null ;
       }
       catch (IOException ioe) {
           _log.error("IOException loading file into Document, filepath = " + path, ioe);
           return null ;
       }
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
        else if (dataType.equalsIgnoreCase("string")) return "" ;
        else return "0";
    }

    /****************************************************************************/

    public static String encodeEscapes(String s) {
        if (s == null) return s;
        return s.replaceAll("&", "&amp;")
                .replaceAll("<", "&lt;")                
                .replaceAll(">", "&gt;")
                .replaceAll("\"", "&quot;")
                .replaceAll("'", "&apos;") ;
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

    public static String formatXMLString(String s) {
        if (s == null) return null;
        if (s.startsWith("<?xml"))
            return documentToString(stringToDocument(s));
        else
            return elementToString(stringToElement(s));
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
        for (Object o: e.getChildren()) {
            stripAttributes((Element) o);      // recurse
        }
        return e;
    }



} //ends
