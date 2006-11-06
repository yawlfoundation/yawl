/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.persistence;

import java.io.File;
import java.io.FileReader;

import au.edu.qut.yawl.elements.YSpecification;

public class StringProducerRawFile extends StringProducerXML {
	private static StringProducer INSTANCE = new StringProducerRawFile();
	
	public static StringProducer getInstance() {return INSTANCE;}

	@Override
	public String getXMLString(String fileName, boolean isXmlFileInPackage) throws Exception {
		File specificationFile = getTranslatedFile(fileName, isXmlFileInPackage);
		StringBuilder builder = new StringBuilder();
		try {
			FileReader reader = new FileReader(specificationFile);
			int c;
			while ((c = reader.read()) != -1) {
				builder.append((char) c);
			}
			reader.close();
		} catch (Exception e) {
			throw new RuntimeException("file failure on " + fileName, e);
		}
		return builder.toString();	
		}

	@Override
	public YSpecification getSpecification(String fileName, boolean isXmlFileInPackage) throws Exception {
		throw new UnsupportedOperationException("not sure how to implement this...");
	}

}
