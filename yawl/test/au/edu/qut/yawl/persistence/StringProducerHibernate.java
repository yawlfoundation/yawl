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

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Environment;

import au.edu.qut.yawl.elements.KeyValue;
import au.edu.qut.yawl.elements.YAWLServiceGateway;
import au.edu.qut.yawl.elements.YAWLServiceReference;
import au.edu.qut.yawl.elements.YAtomicTask;
import au.edu.qut.yawl.elements.YCompositeTask;
import au.edu.qut.yawl.elements.YCondition;
import au.edu.qut.yawl.elements.YDecomposition;
import au.edu.qut.yawl.elements.YExternalNetElement;
import au.edu.qut.yawl.elements.YFlow;
import au.edu.qut.yawl.elements.YInputCondition;
import au.edu.qut.yawl.elements.YMetaData;
import au.edu.qut.yawl.elements.YMultiInstanceAttributes;
import au.edu.qut.yawl.elements.YNet;
import au.edu.qut.yawl.elements.YOutputCondition;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.elements.YTask;
import au.edu.qut.yawl.elements.data.YParameter;
import au.edu.qut.yawl.elements.data.YVariable;
import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.elements.state.YInternalCondition;
import au.edu.qut.yawl.engine.YNetRunner;
import au.edu.qut.yawl.engine.domain.YCaseData;
import au.edu.qut.yawl.engine.domain.YWorkItem;
import au.edu.qut.yawl.engine.domain.YWorkItemID;
import au.edu.qut.yawl.events.Event;
import au.edu.qut.yawl.events.YCaseEvent;
import au.edu.qut.yawl.events.YDataEvent;
import au.edu.qut.yawl.events.YWorkItemEvent;
import au.edu.qut.yawl.persistence.dao.DelegatedHibernateDAO;
import au.edu.qut.yawl.unmarshal.YMarshal;


public class StringProducerHibernate implements StringProducer  {
	private static DelegatedHibernateDAO dao;
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
		dao = new DelegatedHibernateDAO();
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
