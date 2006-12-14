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
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import au.edu.qut.yawl.elements.SpecVersion;
import au.edu.qut.yawl.elements.YAWLServiceReference;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.engine.YNetRunner;
import au.edu.qut.yawl.engine.domain.YWorkItem;
import au.edu.qut.yawl.events.YCaseEvent;
import au.edu.qut.yawl.events.YDataEvent;
import au.edu.qut.yawl.events.YWorkItemEvent;
import au.edu.qut.yawl.exceptions.Problem;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.persistence.YAWLTransactionAdvice;
import au.edu.qut.yawl.persistence.dao.restrictions.PropertyRestriction;
import au.edu.qut.yawl.persistence.dao.restrictions.Restriction;
import au.edu.qut.yawl.persistence.dao.restrictions.RestrictionCriterionConverter;
import au.edu.qut.yawl.persistence.dao.restrictions.Unrestricted;
import au.edu.qut.yawl.persistence.dao.restrictions.PropertyRestriction.Comparison;

public class DelegatedCustomSpringDAO extends AbstractDelegatedDAO {
	private static final Log LOG = LogFactory.getLog( DelegatedCustomSpringDAO.class );
	
	public DelegatedCustomSpringDAO() {
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
	
	private abstract class AbstractHibernateDAO<Type> implements DAO<Type> {
		/**
		 * Hook for subclassers to take care of any operation necessary before the object is saved.
		 */
		protected abstract void preSave( Type object ) throws YPersistenceException;
		
		public void delete( Type object ) throws YPersistenceException {
			Session session = null;
			try {
				session = YAWLTransactionAdvice.openSession();

				Type persistedObject = (Type) session.get( object.getClass(), (Serializable) getKey( object ) );

				session.delete( persistedObject );
			}
			catch( HibernateException e ) {
				throw new YPersistenceException( "Error deleting object " + object, e );
			}
		}

		public Type retrieve( Class type, Object key ) throws YPersistenceException {
			Session session = null;
			try {
				Type retval;
				session = YAWLTransactionAdvice.openSession();

				retval = (Type) session.get( type, (Serializable) key );

				return retval;
			}
			catch( HibernateException e ) {
				throw new YPersistenceException( "Error retrieving object of type '" + type +
						"' with key '" + key + "'", e );
			}
		}
		
		public List<Type> retrieveByRestriction( Class type, Restriction restriction ) throws YPersistenceException {
			Session session = null;
			try {
				session = YAWLTransactionAdvice.openSession();

	            Criteria query = session.createCriteria( type );
	            
	            if( ! ( restriction instanceof Unrestricted ) ) {
	            	query.add( RestrictionCriterionConverter.convertRestriction( restriction ) );
	            }
	            
	            List tmp = query.list();
	            Set set = new HashSet( tmp );
				List<Type> retval = new ArrayList<Type>( set );

				return retval;
			}
			catch( HibernateException e ) {
				throw new YPersistenceException(
						"Retrieve by restriction error " + type + " " + restriction, e );
			}
		}

		public void save( Type object ) throws YPersistenceException {
			Session session = null;
			try {
				preSave( object );

				session = YAWLTransactionAdvice.openSession();

				session.saveOrUpdate( object );
				LOG.debug( "Persisting " + getKey( object ) );
			}
			catch( HibernateException e ) {
				throw new YPersistenceException( "Error saving object " + object, e );
			}
		}
		
		public List getChildren( Object object ) throws YPersistenceException {
			throw new UnsupportedOperationException( "getChildren not supported for " + object);
		}
	}

	private class SpecificationHibernateDAO extends AbstractHibernateDAO<YSpecification> {
		protected void preSave( YSpecification spec ) throws YPersistenceException {
			try {
				spec.setID( new URI( spec.getID() ).toASCIIString() );
				setVersion( spec );
			}
			catch( URISyntaxException e ) {
				LOG.error( e );
			}
		}
		
		private void setVersion(YSpecification spec) throws YPersistenceException {
	        String uriString = spec.getID();
	        
	        Restriction restriction = new PropertyRestriction("specURI", Comparison.EQUAL, uriString);
	        List specVersions =
	        	DelegatedCustomSpringDAO.this.retrieveByRestriction( SpecVersion.class, restriction );
	        
	        SpecVersion specVersion;
	        int nextVersion = 1;
	        specVersion = new SpecVersion( uriString, Integer.valueOf( nextVersion ) );
	        
	        if( specVersions.size() > 0 ) {
	        	specVersion = (SpecVersion) specVersions.get( 0 );
	        	nextVersion = specVersion.getHighestVersion().intValue() + 1;
	        }
	        
	        spec.setVersion( Integer.valueOf( nextVersion ) );
	        specVersion.setHighestVersion( Integer.valueOf( nextVersion ) );
	        
	        DelegatedCustomSpringDAO.this.save( specVersion );
	    }
		
		public Object getKey( YSpecification object ) {
			return PersistenceUtilities.getSpecificationDatabaseKey( object );
		}

		public List getChildren(Object parent) throws YPersistenceException {
			try {
		        List retval = new ArrayList();
		        String filter = "";
		        
		        if( parent instanceof DatasourceFolder ) {
		            DatasourceFolder folder = (DatasourceFolder) parent;
		            filter = folder.getPath();
		            if( ! filter.endsWith( "/" ) ) {
		                filter = filter + "/";
		            }
		            
		            Session session = YAWLTransactionAdvice.openSession();
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
			catch( HibernateException e ) {
				throw new YPersistenceException( "Error retrieving children of " + parent, e );
			}
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

    private class SpecVersionHibernateDAO extends AbstractHibernateDAO<SpecVersion> {
        protected void preSave(SpecVersion object) {}

        public Object getKey(SpecVersion object) {
            return object.getSpecURI();
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
