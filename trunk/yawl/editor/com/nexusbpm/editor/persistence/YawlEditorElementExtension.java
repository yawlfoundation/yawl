/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */

package com.nexusbpm.editor.persistence;

import org.jdom.Element;
import org.jdom.Namespace;

import au.edu.qut.yawl.elements.ElementExtension;
import au.edu.qut.yawl.elements.ExtensionListContainer;

public class YawlEditorElementExtension extends ElementExtension {
	public static Namespace EDITOR_NAMESPACE = Namespace.getNamespace("http://www.nexusbpm.com");
	public static String EDITOR_ROOT_ELEMENT = "editinfo"; 
	protected static String domainString = "YAWL Editor";
	public String getDomain() {return domainString;}
	public void ensureRootElementExists() {
		super.ensureRootElementExists();
		Element root = getRootElement();
		Element child = root.getChild(EDITOR_ROOT_ELEMENT, EDITOR_NAMESPACE);
		if (child == null) {
			child = new Element(EDITOR_ROOT_ELEMENT, EDITOR_NAMESPACE);
			root.getChildren().add(child);
		}
	}
	public YawlEditorElementExtension(ExtensionListContainer t) {
		super(t);
	}
}