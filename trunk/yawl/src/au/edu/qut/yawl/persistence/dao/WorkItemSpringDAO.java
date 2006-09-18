package au.edu.qut.yawl.persistence.dao;

import au.edu.qut.yawl.engine.domain.YWorkItem;

public class WorkItemSpringDAO extends AbstractSpringDAO<YWorkItem> {
	protected void preSave( YWorkItem item ) {}

	public Object getKey( YWorkItem item ) {
		return PersistenceUtilities.getWorkItemDatabaseKey( item );
	}
}