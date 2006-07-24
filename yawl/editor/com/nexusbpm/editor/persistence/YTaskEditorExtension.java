/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */

package com.nexusbpm.editor.persistence;

import java.awt.geom.Rectangle2D;

import org.jdom.Element;

import au.edu.qut.yawl.elements.YExternalNetElement;

public class YTaskEditorExtension extends YawlEditorElementExtension{
	public YTaskEditorExtension(YExternalNetElement t) {super(t);}
	public static String EDITOR_RECTANGLE_ELEMENT = "rectangle"; 
	public static String X1_ATTRIBUTE = "x1";
	public static String Y1_ATTRIBUTE = "y1";
	public static String X2_ATTRIBUTE = "x2";
	public static String Y2_ATTRIBUTE = "y2";
	public Rectangle2D getBounds() {
        ensureRootElementExists();
		double x1 = getRectangleAttribute(X1_ATTRIBUTE);
		double y1 = getRectangleAttribute(Y1_ATTRIBUTE);
		double x2 = getRectangleAttribute(X2_ATTRIBUTE);
		double y2 = getRectangleAttribute(Y2_ATTRIBUTE);
		 return new Rectangle2D.Double(x1, y1, x2, y2);
	}
    private double getRectangleAttribute(String attribute) {
        if( ( getRootElement()
                .getChild(EDITOR_ROOT_ELEMENT, EDITOR_NAMESPACE) != null )
                &&
                ( getRootElement()
                .getChild(EDITOR_ROOT_ELEMENT, EDITOR_NAMESPACE)
                .getChild(EDITOR_RECTANGLE_ELEMENT, EDITOR_NAMESPACE) != null )
                &&
                ( getRootElement()
                .getChild(EDITOR_ROOT_ELEMENT, EDITOR_NAMESPACE)
                .getChild(EDITOR_RECTANGLE_ELEMENT, EDITOR_NAMESPACE)
                .getAttributeValue(attribute) != null ) ) {
            return Double.parseDouble(
                    getRootElement()
                    .getChild(EDITOR_ROOT_ELEMENT, EDITOR_NAMESPACE)
                    .getChild(EDITOR_RECTANGLE_ELEMENT, EDITOR_NAMESPACE)
                    .getAttributeValue(attribute));
        }
        else
            return 0;
    }
	public void setBounds(Rectangle2D bounds) {
		this.ensureRootElementExists();
		Element editinfo = getRootElement().getChild(EDITOR_ROOT_ELEMENT, EDITOR_NAMESPACE);
		Element boundsElement = editinfo.getChild(EDITOR_RECTANGLE_ELEMENT, EDITOR_NAMESPACE);
		if (boundsElement == null) {
			boundsElement = new Element(EDITOR_RECTANGLE_ELEMENT, EDITOR_NAMESPACE);
			editinfo.getChildren().add(boundsElement);
		}
		boundsElement.setAttribute(X1_ATTRIBUTE, String.valueOf(bounds.getX()));
		boundsElement.setAttribute(Y1_ATTRIBUTE, String.valueOf(bounds.getY()));
		boundsElement.setAttribute(X2_ATTRIBUTE, String.valueOf(bounds.getX()));
		boundsElement.setAttribute(Y2_ATTRIBUTE, String.valueOf(bounds.getY()));
	}
	
}