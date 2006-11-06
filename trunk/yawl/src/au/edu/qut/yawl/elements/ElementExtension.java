/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

/**
 * 
 */
package au.edu.qut.yawl.elements;

import java.util.List;

import org.jdom.Element;


public abstract class ElementExtension {
	ExtensionListContainer t;
	public abstract String getDomain();
	protected final Element getRootElement() {
		List<Element> list = t.getInternalExtensions();
		for (Element element: list) {
			String id = element.getAttributeValue(ExtensionListContainer.IDENTIFIER_ATTRIBUTE);
			if (id.equals(getDomain())) return element; 
		}
		return null;
	}
	protected void ensureRootElementExists() {
		Element current = getRootElement();
		if (current == null) {
			Element e = new Element(ExtensionListContainer.ELEMENT_NAME
					, ExtensionListContainer.YAWL_NAMESPACE);
			e.setAttribute(ExtensionListContainer.IDENTIFIER_ATTRIBUTE, getDomain());
			t.getInternalExtensions().add(e);
		}
	}
	public ElementExtension(ExtensionListContainer t) {
		this.t = t;
	}
}