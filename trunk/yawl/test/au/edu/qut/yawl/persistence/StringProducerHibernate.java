/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.persistence;

import java.io.File;
import java.util.List;

import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.persistence.dao.DAO;
import au.edu.qut.yawl.persistence.dao.DAOFactory;
import au.edu.qut.yawl.persistence.dao.DAOFactory.PersistenceType;
import au.edu.qut.yawl.unmarshal.YMarshal;


public class StringProducerHibernate implements StringProducer  {
	private static DAO dao;
	private static StringProducer INSTANCE = null;
	

	public static StringProducer getInstance() {
		return StringProducerHibernate.getInstance(true);
	}
	
	public static StringProducer getInstance(boolean deleteAfterRun) {
		if (INSTANCE == null) 
			INSTANCE = new StringProducerHibernate();
		return INSTANCE;
	}
	
	public StringProducerHibernate() {
		dao = DAOFactory.getDAO( PersistenceType.SPRING );
	}

	public String getXMLString( String fileName, boolean isXmlFileInPackage ) throws Exception {
		String marshalledSpecs = null;
        File specificationFile = getTranslatedFile(fileName, isXmlFileInPackage);
        List specifications = YMarshal.unmarshalSpecifications(specificationFile.getAbsolutePath());
        YSpecification inputSpec = (YSpecification) specifications.iterator().next();
        
		// Create spec
		dao.save(inputSpec);
		Long specID = inputSpec.getDbID();

		// Read spec
		YSpecification outputSpec = (YSpecification) dao.retrieve(YSpecification.class, specID);
        marshalledSpecs = YMarshal.marshal(outputSpec);
        return marshalledSpecs;
	}

	public YSpecification getSpecification(String fileName, boolean isXmlFileInPackage) throws Exception {
	        File specificationFile = getTranslatedFile(fileName, isXmlFileInPackage);
	        List specifications = YMarshal.unmarshalSpecifications(specificationFile.getAbsolutePath());
	        YSpecification inputSpec = (YSpecification) specifications.iterator().next();
	        YSpecification outputSpec = null;
				// Create spec
				dao.save(inputSpec);
				Long specID = inputSpec.getDbID();
	
				// Read spec
				outputSpec = (YSpecification) dao.retrieve(YSpecification.class, specID);
        return outputSpec;
	}
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
