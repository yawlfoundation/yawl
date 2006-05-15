package au.edu.qut.yawl.persistence;

import java.io.File;
import java.util.List;

import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.unmarshal.YMarshal;

public class StringProducerYAWL extends StringProducerRawFile {

	private static StringProducerXML INSTANCE = new StringProducerYAWL();
	
	public static StringProducerXML getInstance() {return INSTANCE;}

	@Override
	public String getXMLString(String fileName, boolean isXmlFileInPackage) {
		File file = getTranslatedFile(fileName, isXmlFileInPackage);
		String marshalledSpecs = "";
		try {
			List specifications = YMarshal
					.unmarshalSpecifications(file.getAbsolutePath());
			YSpecification inputSpec = (YSpecification) specifications.iterator()
					.next();
			marshalledSpecs = YMarshal.marshal(inputSpec);
		} catch (Exception e) {
			throw new RuntimeException("file failure on " + fileName, e);
		}
		return marshalledSpecs;
	}

	@Override
	public YSpecification getSpecification(String fileName, boolean isXmlFileInPackage) throws Exception {
		File file = getTranslatedFile(fileName, isXmlFileInPackage);
		YSpecification inputSpec = null;
		try {
			List specifications = YMarshal
					.unmarshalSpecifications(file.getAbsolutePath());
			inputSpec = (YSpecification) specifications.iterator()
					.next();
		} catch (Exception e) {
			throw new RuntimeException("file failure on " + fileName, e);
		}
		return inputSpec;
	}

}