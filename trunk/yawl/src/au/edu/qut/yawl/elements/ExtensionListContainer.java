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
import org.jdom.Namespace;

public interface ExtensionListContainer {

	String IDENTIFIER_ATTRIBUTE="name";
	String ELEMENT_NAME = "extension";
	Namespace YAWL_NAMESPACE = Namespace.getNamespace("http://www.yawl.fit.qut.edu.au/");
	
	List<Element> getInternalExtensions();
}
