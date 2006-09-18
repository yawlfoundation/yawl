package au.edu.qut.yawl.persistence.dao;

import au.edu.qut.yawl.elements.YAWLServiceReference;

public class YAWLServiceReferenceSpringDAO extends AbstractSpringDAO<YAWLServiceReference> {
	protected void preSave( YAWLServiceReference item ) {}
	
	public Object getKey( YAWLServiceReference item ) {
		return PersistenceUtilities.getYAWLServiceReferenceDatabaseKey( item );
	}
}
