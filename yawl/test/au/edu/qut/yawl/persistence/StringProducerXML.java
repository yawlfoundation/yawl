/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.persistence;

import java.io.File;

import au.edu.qut.yawl.elements.YSpecification;

public abstract class StringProducerXML implements StringProducer {

	/* (non-Javadoc)
	 * @see au.edu.qut.yawl.persistence.StringProducer#getXMLString(java.lang.String, boolean)
	 */
	public abstract String getXMLString(String fileName, boolean isXmlFileInPackage) throws Exception;
	/* (non-Javadoc)
	 * @see au.edu.qut.yawl.persistence.StringProducer#getSpecification(java.lang.String, boolean)
	 */
	public abstract YSpecification getSpecification(String fileName, boolean isXmlFileInPackage) throws Exception;

	/* (non-Javadoc)
	 * @see au.edu.qut.yawl.persistence.StringProducer#getTranslatedFile(java.lang.String, boolean)
	 */
	public File getTranslatedFile(String originalName, boolean isXmlFileInPackage) {
		File file = null;
		if (isXmlFileInPackage) {
			file = new File(StringProducerXML.class.getResource(originalName).getFile());
		}
		else {
			file = new File(originalName);
		}
			return file;
		}
}
