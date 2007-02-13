/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package au.edu.qut.yawl.persistence.dao;

import java.io.Serializable;

import au.edu.qut.yawl.elements.SpecVersion;
import au.edu.qut.yawl.elements.YAWLServiceReference;
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

public class PersistenceUtilities {
	private PersistenceUtilities() {}
	
	public static String contains( String full, String partial ) {
		String retval = null;
		if( full.startsWith( partial ) ) {
			int x = full.lastIndexOf( "/" );
			int y = full.indexOf( "/", partial.length() + 1 );
			if( x > partial.length() ) {
				retval = full.substring( 0, y );
				if( retval.indexOf( "/" ) >= 0 ) {
					retval = retval.substring( retval.lastIndexOf( "/" ) + 1 );
				}
			}
		}
		return retval;
	}
	
    public static Serializable getDatabaseKey( Object object ) {
        if( object == null ) {
            throw new NullPointerException( "Cannot retrieve a key for null!" );
        }
        else if( object instanceof YSpecification ) {
            return getSpecificationDatabaseKey( (YSpecification) object );
        }
        else if( object instanceof YNetRunner ) {
            return getNetRunnerDatabaseKey( (YNetRunner) object );
        }
        else if( object instanceof Problem ) {
            return getProblemDatabaseKey( (Problem) object );
        }
        else if( object instanceof YWorkItem ) {
            return getWorkItemDatabaseKey( (YWorkItem) object );
        }
        else if( object instanceof YIdentifier ) {
            return getIdentifierDatabaseKey( (YIdentifier) object );
        }
        else if( object instanceof IdentifierSequence ) {
            return getIdentifierSequenceDatabaseKey( (IdentifierSequence) object );
        }
        else if( object instanceof YAWLServiceReference ) {
            return getYAWLServiceReferenceDatabaseKey( (YAWLServiceReference) object );
        }
        else if( object instanceof SpecVersion ) {
            return getSpecVersionDatabaseKey( (SpecVersion) object );
        }
        else if( object instanceof YWorkItemEvent ) {
            return getWorkItemEventDatabaseKey( (YWorkItemEvent) object ); 
        }
        else if( object instanceof YExternalNetElement ) {
            return getExternalNetElementDatabaseKey( (YExternalNetElement) object );
        }
        else if( object instanceof YCaseEvent ) {
            return getCaseEventDatabaseKey( (YCaseEvent) object );
        }
        else if( object instanceof YDataEvent ) {
            return getDataEventDatabaseKey( (YDataEvent) object );
        }
        else {
            throw new IllegalArgumentException( "Cannot retrieve key for instance of " + object.getClass().toString());
        }
    }
    
	public static Object getSpecificationKey( YSpecification spec ) {
		return spec.getID();
	}
	
	public static Serializable getSpecificationDatabaseKey( YSpecification spec ) {
		return spec.getDbID();
	}
	
	public static Object getNetRunnerKey( YNetRunner netRunner ) {
		return getNetRunnerDatabaseKey( netRunner );
	}
	
	public static Serializable getNetRunnerDatabaseKey( YNetRunner netRunner ) {
		return netRunner.getId();
	}
	
	public static Object getProblemKey( Problem problem ) {
		return getProblemDatabaseKey( problem );
	}
	
	public static Serializable getProblemDatabaseKey( Problem problem ) {
		return problem.getID();
	}
	
	public static Object getWorkItemKey( YWorkItem item ) {
		return getWorkItemDatabaseKey( item );
	}
	
	public static Serializable getWorkItemDatabaseKey( YWorkItem item ) {
		return item.getId();
	}
	
	public static Object getIdentifierKey( YIdentifier id ) {
		return getIdentifierDatabaseKey( id );
	}
	
	public static Serializable getIdentifierDatabaseKey( YIdentifier id ) {
		return id.getId();
	}
    
    public static Object getIdentifierSequenceKey( IdentifierSequence sequence ) {
        return getIdentifierSequenceDatabaseKey( sequence );
    }
    
    public static Serializable getIdentifierSequenceDatabaseKey( IdentifierSequence sequence ) {
        return sequence.getSequence();
    }
	
	public static Object getYAWLServiceReferenceKey( YAWLServiceReference reference ) {
		return getYAWLServiceReferenceDatabaseKey( reference );
	}
	
	public static Serializable getYAWLServiceReferenceDatabaseKey( YAWLServiceReference reference ) {
		return reference.getYawlServiceID();
	}
    
    public static Serializable getSpecVersionDatabaseKey( SpecVersion object ) {
        return object.getSpecURI();
    }
    
    public static Serializable getWorkItemEventDatabaseKey( YWorkItemEvent event ) {
        return event.getId();
    }
    
    public static Serializable getExternalNetElementDatabaseKey( YExternalNetElement element ) {
        return element.getDbID();
    }
    
    public static Serializable getCaseEventDatabaseKey( YCaseEvent event ) {
        return event.getIdentifier();
    }
    
    public static Serializable getDataEventDatabaseKey( YDataEvent event ) {
        return event.getId();
    }
}
