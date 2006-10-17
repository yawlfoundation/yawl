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
