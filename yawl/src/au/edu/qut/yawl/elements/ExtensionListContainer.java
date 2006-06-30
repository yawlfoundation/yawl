/**
 * 
 */
package au.edu.qut.yawl.elements;

import java.util.List;

import org.jdom.Element;
import org.jdom.Namespace;

public interface ExtensionListContainer {

	String IDENTIFIER_ATTRIBUTE="name";
	String ELEMENT_NAME = "extension";
	Namespace YAWL_NAMESPACE = Namespace.getNamespace("http://www.citi.qut.edu.au/yawl");
	
	List<Element> getInternalExtensions();
}
