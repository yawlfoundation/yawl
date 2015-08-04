/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.util;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;

import java.util.List;

/**
 * 
 * @author Lachlan Aldred
 * Date: 7/11/2003
 * Time: 17:11:59
 * 
 */
public class YDocumentCleaner {


    public static Document cleanDocument(Document oldDocument) {
        Document newDocument = new Document();
        newDocument.setRootElement(new Element(oldDocument.getRootElement().getName()));
        cleanChildren(oldDocument.getRootElement(), newDocument.getRootElement());
        return newDocument;
    }


    private static void cleanChildren(Element oldParent, Element newParent) {
        if (oldParent.getChildren().size() > 0) {
            List children = oldParent.getChildren();
            for (int i = 0; i < children.size(); i++) {
                Element child = (Element) children.get(i);
                Element newChild = new Element(child.getName());
                newParent.addContent(newChild);
                List attrList = child.getAttributes();
                for (int j = 0; j < attrList.size(); j++) {
                    Attribute attribute = (Attribute) attrList.get(j);
                    newChild.setAttribute((Attribute) attribute.clone());
                }
                cleanChildren(child, newChild);
            }
        } else {
            newParent.setText(oldParent.getText());
        }
    }
}
