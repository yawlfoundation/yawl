package au.edu.qut.yawl.persistence.dao;

import au.edu.qut.yawl.engine.YNetRunner;

public class NetRunnerSpringDAO extends AbstractSpringDAO<YNetRunner> {
	protected void preSave( YNetRunner object ) {}
	
	public Object getKey( YNetRunner object ) {
		return PersistenceUtilities.getNetRunnerDatabaseKey( object );
	}
}
