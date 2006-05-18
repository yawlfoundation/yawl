/**
 * 
 */
package au.edu.qut.yawl.elements;

import java.util.List;

import org.jdom.Element;
import org.jdom.Namespace;

public interface ConfigurationListContainer {

	public static String IDENTIFIER_ATTRIBUTE="id";
	public static String ELEMENT_NAME = "configuration";
	public static Namespace YAWL_NAMESPACE = Namespace.getNamespace("configuration");
	
	public List<Element> getInternalConfigurations();
}
