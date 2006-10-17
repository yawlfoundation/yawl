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
import au.edu.qut.yawl.unmarshal.YMarshal;


public class StringProducerHibernate extends StringProducerXML {
	
	private static SessionFactory sessions;
	private static AnnotationConfiguration cfg;
	private static boolean deleteAfterRun = false;
	private Session session;
	
	private static StringProducerXML INSTANCE = null;
	

	public static StringProducerXML getInstance() {
		return StringProducerHibernate.getInstance(true);
	}
	
	public static StringProducerXML getInstance(boolean deleteAfterRun) {
//		StringProducerHibernate.deleteAfterRun = deleteAfterRun;
		if (INSTANCE == null) {
			Class[] classes = new Class[] {
					Event.class,
					KeyValue.class,
					YAtomicTask.class,
					YAWLServiceGateway.class,
					YAWLServiceReference.class,
					YCaseData.class,
					YCaseEvent.class,
					YCompositeTask.class,
					YCondition.class,
					YDataEvent.class,
					YDecomposition.class,
					YExternalNetElement.class,
					YFlow.class,
					YIdentifier.class,
					YInputCondition.class,
					YInternalCondition.class,
					YMetaData.class,
					YMultiInstanceAttributes.class,
					YNet.class,
					YNetRunner.class,
					YOutputCondition.class,
					YParameter.class,
					YSpecification.class,
					YTask.class,
					YVariable.class,
					YWorkItem.class,
					YWorkItemEvent.class,
					YWorkItemID.class
				};
			
			INSTANCE = new StringProducerHibernate();
			if ( getSessions()!=null ) {
				getSessions().close();
//				return INSTANCE;
			}
			AnnotationConfiguration config = (AnnotationConfiguration) new AnnotationConfiguration()
//	        .setProperty(Environment.USE_SQL_COMMENTS, "false")
//	        .setProperty(Environment.SHOW_SQL, "false")
//	        .setProperty(Environment.DIALECT, "org.hibernate.dialect.PostgreSQLDialect")
//	        .setProperty(Environment.DRIVER, "org.postgresql.Driver")
//	        .setProperty(Environment.URL, "jdbc:postgresql://localhost/yawl")
//	        .setProperty(Environment.USER, "postgres")
//	        .setProperty(Environment.PASS, "admin")
//			.setProperty(Environment.HBM2DDL_AUTO, "create")
//			.setProperty(Environment.HBM2DDL_AUTO, "create-drop")
			;
			setCfg( config );
	        
			for (int i=0; i<classes.length; i++) {
				getCfg().addAnnotatedClass( classes[i] );
			}
			setSessions( getCfg().buildSessionFactory( /*new TestInterceptor()*/ ) );
		}

		return INSTANCE;
	}
	
	public StringProducerHibernate() {
		super();
	}

	public Session openSession() throws HibernateException {
		session = getSessions().openSession();
		return session;
	}
	
	protected String[] getAnnotatedPackages() {
		return new String[] {};
	}

	private static void setSessions(SessionFactory sessions) {
		StringProducerHibernate.sessions = sessions;
	}

	protected static SessionFactory getSessions() {
		return sessions;
	}

	protected static void setCfg(AnnotationConfiguration cfg) {
		StringProducerHibernate.cfg = cfg;
	}

	protected static AnnotationConfiguration getCfg() {
		return cfg;
	}

	protected boolean recreateSchema() {
		return true;
	}

	@Override
	public String getXMLString( String fileName, boolean isXmlFileInPackage ) throws Exception {
		String marshalledSpecs = null;
		try {
	        File specificationFile = getTranslatedFile(fileName, isXmlFileInPackage);
	        List specifications = YMarshal.unmarshalSpecifications(specificationFile.getAbsolutePath());
	        YSpecification inputSpec = (YSpecification) specifications.iterator().next();
	        
			// Create spec
			Transaction tx;
			session = openSession();
			tx = session.beginTransaction();
			session.persist(inputSpec);
			tx.commit();
			Long specID = inputSpec.getDbID();
			session.close();

			// Read spec
			session = openSession();
			tx = session.beginTransaction();
			YSpecification outputSpec = (YSpecification) session.get(YSpecification.class, specID);
	        marshalledSpecs = YMarshal.marshal(outputSpec);

	        // Delete spec
	        if (deleteAfterRun) {
	        	session.delete(outputSpec);
	        }
			tx.commit();
//			session.close();
//			
//			if ( session!=null && session.isOpen() ) {
//				if ( session.isConnected() ) session.connection().rollback();
//				session.close();
//				session = null;
//				throw new Exception("unclosed session");
//			}
//			else {
//				session=null;
//			}
		}
		catch (Exception e) {
			try {
				if ( session!=null && session.isOpen() ) {
					if ( session.isConnected() ) session.connection().rollback();
					session.close();
				}
			}
			catch (Exception ignore) {}
//			try {
//				if (sessions!=null) {
//					sessions.close();
//					sessions=null;
//				}
//			}
//			catch (Exception ignore) {}
			throw e;
		}

        return marshalledSpecs;
	}

	@Override
	public YSpecification getSpecification(String fileName, boolean isXmlFileInPackage) throws Exception {
	        File specificationFile = getTranslatedFile(fileName, isXmlFileInPackage);
	        List specifications = YMarshal.unmarshalSpecifications(specificationFile.getAbsolutePath());
	        YSpecification inputSpec = (YSpecification) specifications.iterator().next();
	        YSpecification outputSpec = null;
			try {
				// Create spec
				Transaction tx;
				session = openSession();
				tx = session.beginTransaction();
				session.persist(inputSpec);
				tx.commit();
				Long specID = inputSpec.getDbID();
				session.close();
	
				// Read spec
				session = openSession();
				tx = session.beginTransaction();
				outputSpec = (YSpecification) session.get(YSpecification.class, specID);
				
		        // Delete spec
		        if (deleteAfterRun) {
		        	session.delete(outputSpec);
			}
				tx.commit();
//				session.close();
				
			}
			catch (Exception e) {
				try {
					if ( session!=null && session.isOpen() ) {
						if ( session.isConnected() ) session.connection().rollback();
						session.close();
					}
				}
				catch (Exception ignore) {}
				try {
					if (sessions!=null) {
						sessions.close();
						sessions=null;
					}
				}
				catch (Exception ignore) {}
				throw e;
			}
        return outputSpec;
	}

}
