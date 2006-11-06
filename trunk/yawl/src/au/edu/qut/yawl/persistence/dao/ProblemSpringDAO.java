/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.persistence.dao;

import au.edu.qut.yawl.exceptions.Problem;

public class ProblemSpringDAO extends AbstractSpringDAO<Problem> {
	protected void preSave( Problem object ) {}
	
	public Object getKey( Problem object ) {
		return PersistenceUtilities.getProblemDatabaseKey( object );
	}
}
