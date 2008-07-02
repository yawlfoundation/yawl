/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.util;

import org.jdom.*;
import org.jdom.input.SAXBuilder;
import org.jdom.output.*;

import org.apache.log4j.Logger;

import java.io.*;


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
        return doc.getRootElement();
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
        if (s == null) return s;
        return s.replaceAll("&amp;", "&")
                .replaceAll("&lt;","<")               
                .replaceAll("&gt;", ">")
                .replaceAll("&quot;","\"")
                .replaceAll("&apos;", "'") ;
    }

    /****************************************************************************/

    public static String formatXMLString(String s) {
        return elementToString(stringToElement(s));
    }

} //ends
