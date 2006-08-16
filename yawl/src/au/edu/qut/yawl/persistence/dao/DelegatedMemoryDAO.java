/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package au.edu.qut.yawl.persistence.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.edu.qut.yawl.elements.YAWLServiceReference;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.engine.YNetRunner;
import au.edu.qut.yawl.engine.domain.YWorkItem;
import au.edu.qut.yawl.exceptions.Problem;

public class DelegatedMemoryDAO extends AbstractDelegatedDAO {
	private static final Log LOG = LogFactory.getLog( DelegatedMemoryDAO.class );

	public DelegatedMemoryDAO() {
		addType( YSpecification.class, new SpecificationMemoryDAO() );
		addType( YNetRunner.class, new NetRunnerMemoryDAO() );
		addType( Problem.class, new ProblemMemoryDAO() );
		addType( YWorkItem.class, new WorkItemMemoryDAO() );
		addType( YIdentifier.class, new IdentifierMemoryDAO() );
		addType( YAWLServiceReference.class, new YAWLServiceReferenceMemoryDAO() );
	}
	
	public List getChildren( Object object ) {
		return getDAOForType( YSpecification.class ).getChildren( object );
	}
	
	private abstract class AbstractMemoryDAO<Type> implements DAO<Type> {
		protected abstract void preSave( Type object );
		protected Map<Object, Type> objects = new HashMap<Object, Type>();

		public final boolean delete( Type object ) {
			return objects.remove( getKey( object ) ) != null;
		}

		public final Type retrieve( Class type, Object key ) {
			return objects.get( key );
		}
		
		public final List<Type> retrieveAll( Class type ) {
			return new ArrayList<Type>( objects.values() );
		}

		public final void save( Type object ) {
			preSave( object );
			objects.put( getKey( object ), object );
		}
		
		public List getChildren( Object object ) {
			return new ArrayList();
		}
	}

	private class SpecificationMemoryDAO extends AbstractMemoryDAO<YSpecification> {
		protected void preSave( YSpecification spec ) {}
		public Object getKey( YSpecification object ) {
			return PersistenceUtilities.getSpecificationKey( object );
		}
		
		public List getChildren( Object object ) {
			if( object == null ) throw new IllegalArgumentException( "Parent can not be null" );
			List retval = new ArrayList();
			if( object instanceof DatasourceFolder ) {
				DatasourceFolder parent = (DatasourceFolder) object;
				String path = parent.getPath();
				if( !path.endsWith( "/" ) ) {
					path = path + "/";
				}
				for( YSpecification spec : objects.values() ) {
					String id = spec.getID();
					if( id != null && id.startsWith( path ) ) {
						if( PersistenceUtilities.contains( id, path ) != null ) {
							retval.add( new DatasourceFolder(
									PersistenceUtilities.contains( id, path ), parent ) );
						}
						else {
							retval.add( spec );
						}
					}
				}
			}
			return retval;
		}
	}

	private class NetRunnerMemoryDAO extends AbstractMemoryDAO<YNetRunner> {
		protected void preSave( YNetRunner object ) {}
		public Object getKey( YNetRunner object ) {
			return PersistenceUtilities.getNetRunnerKey( object );
		}
	}
	
	private class ProblemMemoryDAO extends AbstractMemoryDAO<Problem> {
		long maxID = 0;
		protected void preSave( Problem object ) {
			if( object.getID() == null ) {
				object.setID( Long.valueOf( maxID++ ) );
			}
		}
		public Object getKey( Problem object ) {
			return PersistenceUtilities.getProblemKey( object );
		}
	}
	
	private class WorkItemMemoryDAO extends AbstractMemoryDAO<YWorkItem> {
		long maxID = 0;
		protected void preSave( YWorkItem item ) {
			if( item.getId() == null ) {
				item.setId( Long.valueOf( maxID++ ) );
			}
		}
		public Object getKey( YWorkItem item ) {
			return PersistenceUtilities.getWorkItemKey( item );
		}
	}
	
	private class IdentifierMemoryDAO extends AbstractMemoryDAO<YIdentifier> {
		protected void preSave( YIdentifier item ) {
		}
		public Object getKey( YIdentifier item ) {
			return PersistenceUtilities.getIdentifierKey( item );
		}
	}
	
	private class YAWLServiceReferenceMemoryDAO extends AbstractMemoryDAO<YAWLServiceReference> {
		protected void preSave( YAWLServiceReference item ) {
		}
		public Object getKey( YAWLServiceReference item ) {
			return PersistenceUtilities.getYAWLServiceReferenceKey( item );
		}
	}
}
