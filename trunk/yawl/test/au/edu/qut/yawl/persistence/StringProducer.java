package au.edu.qut.yawl.persistence;

import java.io.File;

import au.edu.qut.yawl.elements.YSpecification;

public interface StringProducer {

	public abstract String getXMLString(String fileName,
			boolean isXmlFileInPackage) throws Exception;

	public abstract YSpecification getSpecification(String fileName,
			boolean isXmlFileInPackage) throws Exception;

	public abstract File getTranslatedFile(String originalName,
			boolean isXmlFileInPackage);

}