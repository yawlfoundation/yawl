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