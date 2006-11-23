/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package au.edu.qut.yawl.persistence.dao;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.ObjectDeletedException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.criterion.Restrictions;

import au.edu.qut.yawl.elements.*;
import au.edu.qut.yawl.elements.state.YInternalCondition;
import au.edu.qut.yawl.elements.data.YParameter;
import au.edu.qut.yawl.elements.data.YVariable;
import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.engine.YNetRunner;
import au.edu.qut.yawl.engine.domain.YCaseData;
import au.edu.qut.yawl.engine.domain.YWorkItem;
import au.edu.qut.yawl.engine.domain.YWorkItemID;
import au.edu.qut.yawl.events.Event;
import au.edu.qut.yawl.events.YCaseEvent;
import au.edu.qut.yawl.events.YDataEvent;
import au.edu.qut.yawl.events.YErrorEvent;
import au.edu.qut.yawl.events.YServiceError;
import au.edu.qut.yawl.events.YWorkItemEvent;

import au.edu.qut.yawl.exceptions.Problem;
import au.edu.qut.yawl.persistence.dao.restrictions.Restriction;
import au.edu.qut.yawl.persistence.dao.restrictions.RestrictionCriterionConverter;
import au.edu.qut.yawl.persistence.dao.restrictions.Unrestricted;
import au.edu.qut.yawl.elements.SpecVersion;

/**
 * @deprecated see DelegatedCustomSpringDAO
 */
public class DelegatedHibernateDAO extends AbstractDelegatedDAO {
	private static final Log LOG = LogFactory.getLog( DelegatedHibernateDAO.class );
	
	private static SessionFactory sessionFactory;
	private static AnnotationConfiguration cfg;
	private static Session session;
	private static Class[] classes = new Class[] {
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
		SpecVersion.class,
		YTask.class,
		YVariable.class,
		YWorkItem.class,
		YWorkItemEvent.class,
		YWorkItemID.class,
		YErrorEvent.class,
		YServiceError.class};
	
	private synchronized static void initializeSessions() {
		try {
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
//			.setProperty(Environment.HBM2DDL_AUTO, "update")
			;
			cfg = config;
	        
			for (int i=0; i<classes.length; i++) {
				cfg.addAnnotatedClass( classes[i] );
			}
			sessionFactory = cfg.buildSessionFactory();
			session = sessionFactory.openSession();
		}
		catch (Error e) {
			e.printStackTrace();
		}
	}
	
	private Session openSession() throws HibernateException {
		if (sessionFactory==null) {
			initializeSessions();
		} if (session==null || !session.isOpen()) {
			session = sessionFactory.openSession();
		}
		return session;
	}
	
	public DelegatedHibernateDAO() {
		addType( YSpecification.class, new SpecificationHibernateDAO() );
		addType( SpecVersion.class, new SpecVersionHibernateDAO() );
		addType( YNetRunner.class, new NetRunnerHibernateDAO() );
		addType( Problem.class, new ProblemHibernateDAO() );
		addType( YWorkItem.class, new WorkItemHibernateDAO() );
		addType( YIdentifier.class, new IdentifierHibernateDAO() );
		addType( YAWLServiceReference.class, new YAWLServiceReferenceHibernateDAO() );
		
		addType( YWorkItemEvent.class, new YWorkItemEventDAO() );
		addType( YDataEvent.class, new YDataEventDAO() );
		addType( YCaseEvent.class, new YCaseEventDAO() );

	}
	
	public List getChildren( Object object ) {
		return getDAOForType( YSpecification.class ).getChildren( object );
	}

	private abstract class AbstractHibernateDAO<Type> implements DAO<Type> {
		/**
		 * Hook for subclassers to take care of any operation necessary before the object is saved.
		 */
		protected abstract void preSave( Type object );
		
		public boolean delete( Type object ) {
			Session session = null;
			try {
				session = openSession();
				Transaction tx = session.beginTransaction();
				Type persistedObject = (Type) session.get( object.getClass(), (Serializable) getKey( object ) );
//				YSpecification spec = (YSpecification) session.get(YSpecification.class, (Serializable) getKey(t));
//				System.out.println(">>>>" + spec.getDbID());
//				for (YDecomposition decomp: spec.getDecompositions()) {
//					System.out.println("><" + decomp.getId() + ":" + decomp.getDbID());
//				}
				session.delete( persistedObject );
				tx.commit();
				return true;
			}
			catch( ObjectDeletedException ode ) {
				LOG.error("Deletion failure", ode);
				return false;
			}
			catch( Exception e ) {
					LOG.error( e );
					try {
						if( session!=null && session.isOpen() ) {
							if( session.isConnected() ) session.connection().rollback();
							session.close();
						}
					}
					catch( Exception ignore ) {
						LOG.error( ignore );
					}
					return false;
				}
		}

		public Type retrieve( Class type, Object key ) {
			Session session = null;
			try {
				Type retval;
				session = openSession();
				Transaction tx = session.beginTransaction();
				retval = (Type) session.get( type, (Serializable) key );
				tx.commit();
				return retval;
			}
			catch (Exception e) {
				LOG.error( e );
				try {
					if( session != null && session.isOpen() ) {
						if ( session.isConnected() ) session.connection().rollback();
						session.close();
					}
				}
				catch( Exception ignore ) {			
					LOG.error( ignore );
				}
				return null;
			}
		}
		
		public List<Type> retrieveByRestriction( Class type, Restriction restriction ) {
			Session session = null;
			try {
				session = openSession();
				Transaction tx = session.beginTransaction();
				
	            Criteria query = session.createCriteria( type );
	            
	            if( ! ( restriction instanceof Unrestricted ) ) {
	            	query.add( RestrictionCriterionConverter.convertRestriction( restriction ) );
	            }
	            
	            List tmp = query.list();
	            Set set = new HashSet( tmp );
				List<Type> retval = new ArrayList<Type>( set );
				tx.commit();
				return retval;
			}
			catch (Exception e) {
				LOG.error( e );
				try {
					if( session != null && session.isOpen() ) {
						if ( session.isConnected() ) session.connection().rollback();
						session.close();
					}
				}
				catch( Exception ignore ) {			
					LOG.error( ignore );
				}
				return new ArrayList<Type>();
			}
		}

		public void save( Type object ) {
			Session session = null;
			try {
				preSave( object );
				Transaction tx;
				session = openSession();
				tx = session.beginTransaction();
				session.saveOrUpdate( object );
				LOG.debug( "Persisting " + getKey( object ) );
				tx.commit();
			}
			catch( HibernateException e2 ) {
				LOG.error( (Throwable) e2 );
			}
			catch( Exception e ) {
				LOG.error( e );
				try {
					if( session != null && session.isOpen() ) {
						if( session.isConnected() ) session.connection().rollback();
						session.close();
					}
				}
				catch( Exception ignore ) {
					LOG.error( ignore );
				}
			}
		}
		
		public List getChildren( Object object ) {
			return new ArrayList();
		}
	}

	private class SpecificationHibernateDAO extends AbstractHibernateDAO<YSpecification> {
		protected void preSave( YSpecification spec ) {
			try {
				spec.setID( new URI( spec.getID() ).toASCIIString() );
			}
			catch( URISyntaxException e ) {
				LOG.error( e );
			}
		}
		
		public Object getKey( YSpecification object ) {
			return PersistenceUtilities.getSpecificationDatabaseKey( object );
		}

		public List getChildren(Object parent) {
	        List retval = new ArrayList();
	        String filter = "";
	        
	        if( parent instanceof DatasourceFolder ) {
	            DatasourceFolder folder = (DatasourceFolder) parent;
	            filter = folder.getPath();
	            if( ! filter.endsWith( "/" ) ) {
	                filter = filter + "/";
	            }
	            
	            Session session = openSession();
	            Criteria query = session.createCriteria(YSpecification.class)
	            	.add( Restrictions.like( "ID", filter + "%" ) );
	            
	            List tmp = query.list();
	            
	            Set traversal = new HashSet( tmp );
	            
	            for( Object o : traversal ) {
	                String id = getID( o );
	                if( id != null && id.startsWith( filter ) ) {
	                    if( PersistenceUtilities.contains( id, filter ) != null ) {
	                        retval.add( new DatasourceFolder(
	                        		PersistenceUtilities.contains( id, filter ), folder ) );
	                    } else {
	                        assert o instanceof YSpecification : "object not a specification";
	                        retval.add( o );
	                    }
	                }
	            }
	        }
	        
			LOG.debug("retrieving " + retval);
			return retval;
		}
		
		private String getID( Object object ) {
	        if( object instanceof YSpecification ) {
	            return ((YSpecification) object).getID();
	        }
	        else {
	            return object.toString();
	        }
	    }
	}

	private class NetRunnerHibernateDAO extends AbstractHibernateDAO<YNetRunner> {
		protected void preSave( YNetRunner object ) {}
		
		public Object getKey( YNetRunner object ) {
			return PersistenceUtilities.getNetRunnerDatabaseKey( object );
		}
	}
	
	private class ProblemHibernateDAO extends AbstractHibernateDAO<Problem> {
		protected void preSave( Problem object ) {}
		
		public Object getKey( Problem object ) {
			return PersistenceUtilities.getProblemDatabaseKey( object );
		}
	}
	
	private class WorkItemHibernateDAO extends AbstractHibernateDAO<YWorkItem> {
		protected void preSave( YWorkItem item ) {}
		
		public Object getKey( YWorkItem item ) {
			return PersistenceUtilities.getWorkItemDatabaseKey( item );
		}
	}
	
	private class IdentifierHibernateDAO extends AbstractHibernateDAO<YIdentifier> {
		protected void preSave( YIdentifier item ) {}
		
		public Object getKey( YIdentifier item ) {
			return PersistenceUtilities.getIdentifierDatabaseKey( item );
		}
	}
	
	private class YAWLServiceReferenceHibernateDAO extends AbstractHibernateDAO<YAWLServiceReference> {
		protected void preSave( YAWLServiceReference item ) {}
		
		public Object getKey( YAWLServiceReference item ) {
			return PersistenceUtilities.getYAWLServiceReferenceDatabaseKey( item );
		}
	}
	private class YWorkItemEventDAO extends AbstractHibernateDAO<YWorkItemEvent> {
		protected void preSave( YWorkItemEvent item ) {}
		
		public Object getKey( YWorkItemEvent item ) {
			return item.getId();
		}
	}
	
	private class YCaseEventDAO extends AbstractHibernateDAO<YCaseEvent> {
		protected void preSave( YCaseEvent item ) {}
		
		public Object getKey( YCaseEvent item ) {
			return item.getIdentifier();
		}
	}
	
	private class YDataEventDAO extends AbstractHibernateDAO<YDataEvent> {
		protected void preSave( YDataEvent item ) {}
		
		public Object getKey( YDataEvent item ) {
			return item.getId();
		}
	}

    private class SpecVersionHibernateDAO extends AbstractHibernateDAO<SpecVersion> {
		protected void preSave( SpecVersion item ) {}

		public Object getKey( SpecVersion item ) {
			return item.getSpecURI();
		}
	}

}
