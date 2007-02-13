/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package au.edu.qut.yawl.persistence.dao;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;

import au.edu.qut.yawl.admintool.model.HumanResource;
import au.edu.qut.yawl.elements.SpecVersion;
import au.edu.qut.yawl.elements.YAWLServiceReference;
import au.edu.qut.yawl.elements.YAtomicTask;
import au.edu.qut.yawl.elements.YCompositeTask;
import au.edu.qut.yawl.elements.YExternalNetElement;
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
import au.edu.qut.yawl.persistence.DatasourceFolder;
import au.edu.qut.yawl.persistence.dao.restrictions.PropertyRestriction;
import au.edu.qut.yawl.persistence.dao.restrictions.Restriction;
import au.edu.qut.yawl.persistence.dao.restrictions.PropertyRestriction.Comparison;

public class DelegatedSpringDAO extends AbstractDelegatedDAO {
	private static final Log LOG = LogFactory.getLog( DelegatedSpringDAO.class );
	
	private SessionFactory sessionFactory;
	
	public DelegatedSpringDAO() { 
		addType( YSpecification.class, new SpecificationSpringDAO() );
		addType( SpecVersion.class, new SpecVersionSpringDAO() );
		addType( YNetRunner.class, new NetRunnerSpringDAO() );
		addType( Problem.class, new ProblemSpringDAO() );
		addType( YWorkItem.class, new WorkItemSpringDAO() );
		addType( YIdentifier.class, new IdentifierSpringDAO() );
        addType( IdentifierSequence.class, new IdentifierSequenceSpringDAO() );
		addType( YAWLServiceReference.class, new YAWLServiceReferenceSpringDAO() );

		addType( YWorkItemEvent.class, new YWorkItemEventSpringDAO() );
		addType( YDataEvent.class, new YDataEventSpringDAO() );
		addType( YCaseEvent.class, new YCaseEventSpringDAO() );
		addType( YAtomicTask.class, new YExternalNetElementSpringDAO() );
		addType( YCompositeTask.class, new YExternalNetElementSpringDAO() );
		
		addType( HumanResource.class, new HumanResourceDAO() );
	}
	
	private class IdentifierSpringDAO extends AbstractSpringDAO<YIdentifier> {
		protected void preSave( YIdentifier item ) throws YPersistenceException {
            if( item.getId() == null ) {
                Restriction restriction = new PropertyRestriction("sequence", Comparison.EQUAL, "sequence");
                List sequences = DelegatedSpringDAO.this.retrieveByRestriction(
                        IdentifierSequence.class, restriction );
                
                int value = 1;
                IdentifierSequence sequence = new IdentifierSequence( "sequence" );
                
                if( sequences.size() > 0 ) {
                    sequence = (IdentifierSequence) sequences.get( 0 );
                    value = sequence.getValue().intValue() + 1;
                }
                item.setId( String.valueOf( value ) );
                sequence.setValue( Long.valueOf( value ) );
                
                DelegatedSpringDAO.this.save( sequence );
            }
        }
		
		public Object getKey( YIdentifier item ) {
			return PersistenceUtilities.getIdentifierDatabaseKey( item );
		}
	}
	
	private class IdentifierSequenceSpringDAO extends AbstractSpringDAO<IdentifierSequence> {
        protected void preSave( IdentifierSequence sequence ) {}
        public Object getKey( IdentifierSequence sequence ) {
            return PersistenceUtilities.getIdentifierSequenceDatabaseKey( sequence );
        }
    }

    private class SpecVersionSpringDAO extends AbstractSpringDAO<SpecVersion> {
        protected void preSave(SpecVersion object) {}

        public Object getKey(SpecVersion object) {
            return object.getSpecURI();
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
	
	private class YAWLServiceReferenceSpringDAO extends AbstractSpringDAO<YAWLServiceReference> {
		protected void preSave( YAWLServiceReference item ) {}
		
		public Object getKey( YAWLServiceReference item ) {
			return PersistenceUtilities.getYAWLServiceReferenceDatabaseKey( item );
		}
	}
	
	private class YWorkItemEventSpringDAO extends AbstractSpringDAO<YWorkItemEvent> {
		protected void preSave( YWorkItemEvent item ) {}
		
		public Object getKey( YWorkItemEvent item ) {
			return item.getId();
		}
	}

	private class YExternalNetElementSpringDAO extends AbstractSpringDAO<YExternalNetElement> {
		protected void preSave( YExternalNetElement item ) {}
		
		public Object getKey( YExternalNetElement item ) {
			return item.getDbID();
		}
	}
	
	private class YCaseEventSpringDAO extends AbstractSpringDAO<YCaseEvent> {
		protected void preSave( YCaseEvent item ) {}
		
		public Object getKey( YCaseEvent item ) {
			return item.getIdentifier();
		}
	}
	
	private class YDataEventSpringDAO extends AbstractSpringDAO<YDataEvent> {
		protected void preSave( YDataEvent item ) {}
		
		public Object getKey( YDataEvent item ) {
			return item.getId();
		}
	}
	
	private class HumanResourceDAO extends AbstractSpringDAO<HumanResource> {
		protected void preSave( HumanResource item ) {}
		
		public Object getKey( HumanResource item ) {
			return item.getRsrcID();
		}
	}
	
	private class SpecificationSpringDAO extends AbstractSpringDAO<YSpecification> {
		protected void preSave( YSpecification spec ) throws YPersistenceException {
			try {
				spec.setID( new URI( spec.getID() ).toASCIIString() );
				if( spec.getVersion() == null ) {
                    setVersion( spec );
                }
			}
			catch( URISyntaxException e ) {
				LOG.error( e );
			}
		}
		
		private void setVersion(YSpecification spec) throws YPersistenceException {
	        String uriString = spec.getID();
	        
	        SpecVersion specVersion = (SpecVersion)
                DelegatedSpringDAO.this.retrieve( SpecVersion.class, uriString );
	        
	        int nextVersion = 1;
	        
	        if( specVersion == null ) {
                specVersion = new SpecVersion( uriString, Integer.valueOf( nextVersion ) );
	        }
            else {
                nextVersion = specVersion.getHighestVersion().intValue() + 1;
            }
	        
	        spec.setVersion( Integer.valueOf( nextVersion ) );
	        specVersion.setHighestVersion( Integer.valueOf( nextVersion ) );
	        
	        DelegatedSpringDAO.this.save( specVersion );
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

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
		for( DAO dao : typeMap.values() ) {
			((AbstractSpringDAO)dao).setSessionFactory(sessionFactory);
		}
	}
	
	
}
