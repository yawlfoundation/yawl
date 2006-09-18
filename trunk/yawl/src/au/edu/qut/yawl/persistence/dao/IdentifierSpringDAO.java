package au.edu.qut.yawl.persistence.dao;

import au.edu.qut.yawl.elements.state.YIdentifier;

public class IdentifierSpringDAO extends AbstractSpringDAO<YIdentifier> {
	protected void preSave( YIdentifier item ) {}
	
	public Object getKey( YIdentifier item ) {
		return PersistenceUtilities.getIdentifierDatabaseKey( item );
	}
}
