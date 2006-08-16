/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package au.edu.qut.yawl.persistence.dao;

import au.edu.qut.yawl.elements.YAWLServiceReference;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.engine.YNetRunner;
import au.edu.qut.yawl.engine.domain.YWorkItem;
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
	
	public static Object getSpecificationKey( YSpecification spec ) {
		return spec.getID();
	}
	
	public static Object getSpecificationDatabaseKey( YSpecification spec ) {
		return spec.getDbID();
	}
	
	public static Object getNetRunnerKey( YNetRunner netRunner ) {
		return netRunner.getCaseID();
	}
	
	public static Object getNetRunnerDatabaseKey( YNetRunner netRunner ) {
		return getNetRunnerKey( netRunner );
	}
	
	public static Object getProblemKey( Problem problem ) {
		return problem.getID();
	}
	
	public static Object getWorkItemKey( YWorkItem item ) {
		return item.getId();
	}
	
	public static Object getIdentifierKey( YIdentifier id ) {
		return id.getId();
	}
	
	public static Object getYAWLServiceReferenceKey( YAWLServiceReference reference ) {
		return reference.getYawlServiceID();
	}
}
