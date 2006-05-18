/**
 * 
 */
package au.edu.qut.yawl.elements;

import java.util.List;

import org.jdom.Element;
import org.jdom.Namespace;


public abstract class ElementConfiguration {
	ConfigurationListContainer t;
	public abstract String getDomain();
	protected final Element getRootElement() {
		List<Element> list = t.getInternalConfigurations();
		for (Element element: list) {
			String id = element.getAttributeValue("id");
			if (id.equals(getDomain())) return element; 
		}
		return null;
	}
	protected final void ensureRootElement() {
		Element current = getRootElement();
		if (current == null) {
			Element e = new Element(ConfigurationListContainer.ELEMENT_NAME
					, ConfigurationListContainer.YAWL_NAMESPACE);
			e.setAttribute(ConfigurationListContainer.IDENTIFIER_ATTRIBUTE, getDomain());
			t.getInternalConfigurations().add(e);
		}
	}
	public ElementConfiguration(ConfigurationListContainer t) {
		this.t = t;
	}
}