/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package au.edu.qut.yawl.persistence.dao;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.edu.qut.yawl.elements.SpecVersion;
import au.edu.qut.yawl.elements.YAWLServiceReference;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.elements.state.IdentifierSequence;
import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.engine.YNetRunner;
import au.edu.qut.yawl.engine.domain.YWorkItem;
import au.edu.qut.yawl.events.YCaseEvent;
import au.edu.qut.yawl.events.YDataEvent;
import au.edu.qut.yawl.events.YWorkItemEvent;
import au.edu.qut.yawl.exceptions.Problem;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.persistence.dao.restrictions.Restriction;
import au.edu.qut.yawl.persistence.dao.restrictions.RestrictionEvaluator;
import au.edu.qut.yawl.persistence.dao.restrictions.Unrestricted;

public class DelegatedMemoryDAO extends AbstractDelegatedDAO {
	private static final Log LOG = LogFactory.getLog( DelegatedMemoryDAO.class );

	public DelegatedMemoryDAO() {
		addType( YSpecification.class, new SpecificationMemoryDAO() );
		addType( SpecVersion.class, new SpecVersionMemoryDAO() );
		addType( YNetRunner.class, new NetRunnerMemoryDAO() );
		addType( Problem.class, new ProblemMemoryDAO() );
		addType( YWorkItem.class, new WorkItemMemoryDAO() );
		addType( YIdentifier.class, new IdentifierMemoryDAO() );
        addType( IdentifierSequence.class, new IdentifierSequenceMemoryDAO() );
		addType( YAWLServiceReference.class, new YAWLServiceReferenceMemoryDAO() );
		
		addType( YWorkItemEvent.class, new YWorkItemEventDAO() );
		addType( YDataEvent.class, new YDataEventDAO() );
		addType( YCaseEvent.class, new YCaseEventDAO() );
	}
	
	private abstract class AbstractMemoryDAO<Type> implements DAO<Type> {
		protected abstract void preSave( Type object );
		protected Map<Object, Type> objects = new HashMap<Object, Type>();
		protected BeanInfo typeInfo;
		
		protected void initProperties( Class type ) {
			if( typeInfo == null ) {
				try {
					typeInfo = Introspector.getBeanInfo( type );
				}
				catch( Exception e ) {
					LOG.error( e );
				}
			}
		}

		public final void delete( Type object ) throws YPersistenceException {
			objects.remove( getKey( object ) );
		}

		public final Type retrieve( Class type, Object key ) {
			return objects.get( key );
		}
		
		public final List<Type> retrieveByRestriction( Class type, Restriction restriction ) throws YPersistenceException {
			if( restriction instanceof Unrestricted ) {
				return new ArrayList<Type>( objects.values() );
			}
			initProperties( type );
			List<Type> retval = new ArrayList<Type>();
			for( Type object : objects.values() ) {
                try {
                    if( RestrictionEvaluator.passesRestriction( object, restriction, typeInfo ) ) {
                        retval.add( object );
                    }
                }
                catch( Exception e ) {
                    throw new YPersistenceException(
                            "Retrieve by restriction error " + type + " " + restriction, e );
                }
			}
			return retval;
		}
		
		public final void save( Type object ) throws YPersistenceException {
			preSave( object );
			objects.put( getKey( object ), object );
		}
		
		public List getChildren( Object object ) {
			return new ArrayList();
		}
	}

	private class SpecificationMemoryDAO extends AbstractMemoryDAO<YSpecification> {
        long next = 1;
		protected void preSave( YSpecification spec ) {
            if( spec.getDbID() == null ) {
                spec.setDbID( Long.valueOf( next ) );
                next += 1;
            }
        }
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

    private class SpecVersionMemoryDAO extends AbstractMemoryDAO<SpecVersion> {

        public Object getKey(SpecVersion object) {
            return object.getSpecURI();
        }

        protected void preSave(SpecVersion object) { }
    }

    private class NetRunnerMemoryDAO extends AbstractMemoryDAO<YNetRunner> {
		long maxID = 0;
		protected void preSave( YNetRunner object ) {
			if( object.getId() == null ) {
				object.setId( Long.valueOf( maxID++ ) );
			}
		}
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
        int id = 1;
		protected void preSave( YIdentifier item ) {
            if( item.getId() == null ) {
                item.setId( String.valueOf( id++ ) );
            }
		}
		public Object getKey( YIdentifier item ) {
			return PersistenceUtilities.getIdentifierKey( item );
		}
	}
    
    private class IdentifierSequenceMemoryDAO extends AbstractMemoryDAO<IdentifierSequence> {
        protected void preSave( IdentifierSequence item ) {
        }
        public Object getKey( IdentifierSequence item ) {
            return PersistenceUtilities.getIdentifierSequenceKey( item );
        }
    }
	
	private class YAWLServiceReferenceMemoryDAO extends AbstractMemoryDAO<YAWLServiceReference> {
		protected void preSave( YAWLServiceReference item ) {
		}
		public Object getKey( YAWLServiceReference item ) {
			return PersistenceUtilities.getYAWLServiceReferenceKey( item );
		}
	}
	
	private class YWorkItemEventDAO extends AbstractMemoryDAO<YWorkItemEvent> {
		protected void preSave( YWorkItemEvent item ) {}
		
		public Object getKey( YWorkItemEvent item ) {
			return item.getId();
		}
	}
	
	private class YCaseEventDAO extends AbstractMemoryDAO<YCaseEvent> {
		protected void preSave( YCaseEvent item ) {}
		
		public Object getKey( YCaseEvent item ) {
			return item.getIdentifier();
		}
	}
	
	private class YDataEventDAO extends AbstractMemoryDAO<YDataEvent> {
		protected void preSave( YDataEvent item ) {}
		
		public Object getKey( YDataEvent item ) {
			return item.getId();
		}
	}
}
