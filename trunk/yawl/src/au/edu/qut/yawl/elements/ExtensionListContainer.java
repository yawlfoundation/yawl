/**
 * 
 */
package au.edu.qut.yawl.elements;

import java.util.List;

import org.jdom.Element;
import org.jdom.Namespace;

public interface ExtensionListContainer {

	public static String IDENTIFIER_ATTRIBUTE="name";
	public static String ELEMENT_NAME = "extension";
	public static Namespace YAWL_NAMESPACE = Namespace.getNamespace("http://www.citi.qut.edu.au/yawl");
	
	public List<Element> getInternalExtensions();
}
