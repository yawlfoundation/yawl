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
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import au.edu.qut.yawl.elements.YAWLServiceReference;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.engine.YNetRunner;
import au.edu.qut.yawl.engine.domain.YWorkItem;
import au.edu.qut.yawl.exceptions.Problem;

public class DelegatedSpringDAO extends AbstractDelegatedDAO {
	private static final Log LOG = LogFactory.getLog( DelegatedSpringDAO.class );

	public DelegatedSpringDAO() {
		addType( YSpecification.class, new SpecificationSpringDAO() );
		addType( YNetRunner.class, new NetRunnerSpringDAO() );
		addType( Problem.class, new ProblemSpringDAO() );
		addType( YWorkItem.class, new WorkItemSpringDAO() );
		addType( YIdentifier.class, new IdentifierSpringDAO() );
		addType( YAWLServiceReference.class, new YAWLServiceReferenceSpringDAO() );
	}
	
	public List getChildren( Object object ) {
		return getDAOForType( YSpecification.class ).getChildren( object );
	}

	private abstract class AbstractSpringDAO<Type> extends HibernateDaoSupport implements DAO<Type> {
		/**
		 * Hook for subclassers to take care of any operation necessary before the object is saved.
		 */
		protected abstract void preSave( Type object );
		
		public final void save( Type object ) {
			preSave( object );
			getHibernateTemplate().merge( object );
		}
		
		public final Type retrieve( Class type, Object key ) {
			return (Type) getHibernateTemplate().get( type, (Serializable) key );
		}
		
		public List<Type> retrieveAll( Class type ) {
			// TODO
			return new ArrayList<Type>();
		}
		
		public final boolean delete( Type object ) {
			Type persistedObject = (Type) getHibernateTemplate().get(
					object.getClass(), (Serializable) getKey( object ) );
			assert persistedObject != null : "attempting to delete object that wasn't persisted";
			// TODO which way do we want this?
//			if( persistedObject == null ) {
//				return false;
//			}
			getHibernateTemplate().delete( persistedObject );
			return true;
		}
		
		public List getChildren( Object object ) {
			return new ArrayList();
		}
	}

	private class SpecificationSpringDAO extends AbstractSpringDAO<YSpecification> {
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
	            
	            List tmp = getHibernateTemplate().find( "from YSpecification spec where spec.ID like ?", filter + "%");
	            
//	            Set traversal = new HashSet( tmp );
	            
	            for( Object o : tmp ) {
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

	private class NetRunnerSpringDAO extends AbstractSpringDAO<YNetRunner> {
		protected void preSave( YNetRunner object ) {}
		
		public Object getKey( YNetRunner object ) {
			return PersistenceUtilities.getNetRunnerDatabaseKey( object );
		}
	}
	
	private class ProblemSpringDAO extends AbstractSpringDAO<Problem> {
		protected void preSave( Problem object ) {}
		
		public Object getKey( Problem object ) {
			return PersistenceUtilities.getProblemDatabaseKey( object );
		}
	}
	
	private class WorkItemSpringDAO extends AbstractSpringDAO<YWorkItem> {
		protected void preSave( YWorkItem item ) {}
		
		public Object getKey( YWorkItem item ) {
			return PersistenceUtilities.getWorkItemDatabaseKey( item );
		}
	}
	
	private class IdentifierSpringDAO extends AbstractSpringDAO<YIdentifier> {
		protected void preSave( YIdentifier item ) {}
		
		public Object getKey( YIdentifier item ) {
			return PersistenceUtilities.getIdentifierDatabaseKey( item );
		}
	}
	
	private class YAWLServiceReferenceSpringDAO extends AbstractSpringDAO<YAWLServiceReference> {
		protected void preSave( YAWLServiceReference item ) {}
		
		public Object getKey( YAWLServiceReference item ) {
			return PersistenceUtilities.getYAWLServiceReferenceDatabaseKey( item );
		}
	}
}
