/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package au.edu.qut.yawl.worklist.model;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import java.io.StringReader;
import java.util.List;


 /**
 * 
 * @author Lachlan Aldred
 * Date: 4/05/2004
 * Time: 18:07:24
 * 
 */
public class WorkListGUIUtils {
    static SAXBuilder _builder = new SAXBuilder();


    public static String convertUploadErrorMsg(String errorMsg) {
        StringBuffer html = new StringBuffer();
        try {
            Document doc;
            //System.out.println("errorMsg = " + errorMsg); //MLR (02/11/07) code merge: this line was added by M2 for debugging purposes and has been commented out
            doc = _builder.build(new StringReader(errorMsg));
            List errors = doc.getRootElement().getChildren();
            for (int i = 0; i < errors.size(); i++) {
                html.append("<tr><td><pre>");
                Element errorElem = (Element) errors.get(i);
                Element srcEl = errorElem.getChild("src");
                if (srcEl != null) {
                    String src;
                    src = srcEl.getText();
                    html.append("Source:\n");
                    html.append("       " + src);
                }
                html.append("</pre></td><td><pre>");
                XMLOutputter outputter = new XMLOutputter();
                String message = outputter.outputString(errorElem.getChild("message"));
                message = convertToEscapes(message);

                html.append(message);
                html.append("</pre></td></tr>");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return html.toString();
    }

    public static String convertToEscapes(String message) {
        message = message.replaceAll("<", "&lt;");
        message = message.replaceAll(">", "&gt;");
        return message;
    }


    public static String removeFailureTags(String failureMessage) {
        return failureMessage.substring(17, failureMessage.length() - 19);
    }
}
