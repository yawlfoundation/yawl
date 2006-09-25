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
import org.hibernate.cfg.Environment;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.ConstraintViolationException;

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
import au.edu.qut.yawl.events.YWorkItemEvent;
import au.edu.qut.yawl.exceptions.Problem;
import au.edu.qut.yawl.persistence.dao.restrictions.Restriction;
import au.edu.qut.yawl.persistence.dao.restrictions.RestrictionCriterionConverter;
import au.edu.qut.yawl.persistence.dao.restrictions.Unrestricted;

import au.edu.qut.yawl.persistence.YAWLTransactionAdvice;

public class DelegatedCustomSpringDAO extends AbstractDelegatedDAO {
	private static final Log LOG = LogFactory.getLog( DelegatedCustomSpringDAO.class );
	
	private static SessionFactory sessionFactory;
	private static AnnotationConfiguration cfg;
	private static Session session;
	private static Class[] classes = new Class[] {
						KeyValue.class,
						YInternalCondition.class,
						YFlow.class,
						YMultiInstanceAttributes.class,
						YCompositeTask.class,
						YAtomicTask.class,
						YTask.class,
						YInputCondition.class,
						YOutputCondition.class,
						YCondition.class,
						YExternalNetElement.class,
						YAWLServiceReference.class,
						YAWLServiceGateway.class,
						YVariable.class,
						YParameter.class,
						YDecomposition.class,
						YNet.class,
						YCaseData.class,
						YMetaData.class,
						YSpecification.class,
						YNetRunner.class,
						YWorkItem.class,
						YWorkItemID.class,
						YIdentifier.class,
						Event.class
				};
	
	private synchronized static void initializeSessions() {
		//if ( sessionFactory != null ) sessionFactory.close();
		try {
			AnnotationConfiguration config = (AnnotationConfiguration) new AnnotationConfiguration()
//	        .setProperty(Environment.USE_SQL_COMMENTS, "false")
//	        .setProperty(Environment.SHOW_SQL, "false")
//	        .setProperty(Environment.DIALECT, "org.hibernate.dialect.PostgreSQLDialect")
//	        .setProperty(Environment.DRIVER, "org.postgresql.Driver")
//	        .setProperty(Environment.URL, "jdbc:postgresql://localhost/dean2")
//	        .setProperty(Environment.USER, "capsela")
//	        .setProperty(Environment.PASS, "capsela")

			.setProperty(Environment.HBM2DDL_AUTO, "create")
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
		sessionFactory = YAWLTransactionAdvice.getFactory();
		
		
		if (sessionFactory==null) {
			initializeSessions();
		} else {
			session = YAWLTransactionAdvice.getSession();
		}
		
		if (session==null) {
			session = sessionFactory.openSession();
		}
		
		return session;
	}

	public DelegatedCustomSpringDAO() {
		addType( YSpecification.class, new SpecificationHibernateDAO() );
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

				Type persistedObject = (Type) session.get( object.getClass(), (Serializable) getKey( object ) );

				session.delete( persistedObject );


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
					try {
						if( sessionFactory!=null ) {
							sessionFactory.close();
							sessionFactory=null;
						}
					}
					catch( Exception ignore ) {
						LOG.error(ignore);
					}
					return false;
				}
		}

		public Type retrieve( Class type, Object key ) {
			Session session = null;
			try {
				Type retval;
				session = openSession();

				retval = (Type) session.get( type, (Serializable) key );

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
				try {
					if (sessionFactory!=null) {
						sessionFactory.close();
						sessionFactory=null;
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

	            Criteria query = session.createCriteria( type );
	            
	            if( ! ( restriction instanceof Unrestricted ) ) {
	            	query.add( RestrictionCriterionConverter.convertRestriction( restriction ) );
	            }
	            
	            List tmp = query.list();
	            Set set = new HashSet( tmp );
				List<Type> retval = new ArrayList<Type>( set );


				return retval;
			}
			catch (Exception e) {
				e.printStackTrace();
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
				try {
					if (sessionFactory!=null) {
						sessionFactory.close();
						sessionFactory=null;
					}
				}
				catch( Exception ignore ) {
					LOG.error( ignore );
				}
				return null;
			}
		}

		public void save( Type object ) {
			Session session = null;
			try {
				preSave( object );

				session = openSession();

				session.saveOrUpdate( object );
				LOG.debug( "Persisting " + getKey( object ) );

			}
			catch( HibernateException e2 ) {
				// Should be thrown
				LOG.error( (Throwable) e2 );
				//if (e2 instanceof ConstraintViolationException) {
					e2.printStackTrace();
				//}
				try {
					Thread.sleep(100000);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			catch( Exception e ) {
				LOG.error( e );
				//e.printStackTrace();
				try {
					if( session != null && session.isOpen() ) {
						if( session.isConnected() ) session.connection().rollback();
						session.close();
					}
				}
				catch( Exception ignore ) {
					LOG.error( ignore );
				}
				try {
					if( sessionFactory != null ) {
						sessionFactory.close();
						sessionFactory = null;
					}
				}
				catch (Exception ignore) {
					LOG.error(ignore);
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
}
