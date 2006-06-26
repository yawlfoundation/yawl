/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */

package com.nexusbpm.editor.persistence;

import java.awt.geom.Point2D;

import org.jdom.Element;

import au.edu.qut.yawl.elements.YExternalNetElement;

public class YTaskEditorExtension extends YawlEditorElementExtension{
	public YTaskEditorExtension(YExternalNetElement t) {super(t);}
	public static String EDITOR_POINT_ELEMENT = "point"; 
	public static String X_ATTRIBUTE = "x";
	public static String Y_ATTRIBUTE = "y";
	public Point2D getCenterPoint() {
		double x = java.lang.Double.parseDouble(
		 getRootElement()
		.getChild(EDITOR_ROOT_ELEMENT, EDITOR_NAMESPACE)
		.getChild(EDITOR_POINT_ELEMENT, EDITOR_NAMESPACE)
		.getAttributeValue(X_ATTRIBUTE));
		double y = java.lang.Double.parseDouble(
		getRootElement()
		.getChild(EDITOR_ROOT_ELEMENT, EDITOR_NAMESPACE)
		.getChild(EDITOR_POINT_ELEMENT, EDITOR_NAMESPACE)
		.getAttributeValue(Y_ATTRIBUTE));
		 return new Point2D.Double(x, y);
	}
	public void setCenterPoint(Point2D inPoint) {
		this.ensureRootElementExists();
		Element editinfo = getRootElement().getChild(EDITOR_ROOT_ELEMENT, EDITOR_NAMESPACE);
		Element point = editinfo.getChild(EDITOR_POINT_ELEMENT, EDITOR_NAMESPACE);
		if (point == null) {
			point = new Element(EDITOR_POINT_ELEMENT, EDITOR_NAMESPACE);
			editinfo.getChildren().add(point);
		}
		point.setAttribute(X_ATTRIBUTE, String.valueOf(inPoint.getX()));
		point.setAttribute(Y_ATTRIBUTE, String.valueOf(inPoint.getY()));
	}
	
}