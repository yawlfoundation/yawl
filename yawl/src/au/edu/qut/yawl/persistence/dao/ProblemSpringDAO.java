package au.edu.qut.yawl.persistence.dao;

import au.edu.qut.yawl.exceptions.Problem;

public class ProblemSpringDAO extends AbstractSpringDAO<Problem> {
	protected void preSave( Problem object ) {}
	
	public Object getKey( Problem object ) {
		return PersistenceUtilities.getProblemDatabaseKey( object );
	}
}
