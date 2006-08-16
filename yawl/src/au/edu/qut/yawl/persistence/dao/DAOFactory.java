/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package au.edu.qut.yawl.persistence.dao;

public class DAOFactory {
	private DAOFactory() {}
	
	public enum PersistenceType {EJB3, FILE, HIBERNATE, MEMORY, SPRING};
	
	public static DAO getDAO( PersistenceType type ) {
		switch( type ) {
			case EJB3:
				return new DelegatedEJB3DAO();
			case FILE:
				return new DelegatedFileDAO();
			case HIBERNATE:
				return new DelegatedHibernateDAO();
			case SPRING:
				return new DelegatedSpringDAO();
			case MEMORY:
			default:
				return new DelegatedMemoryDAO();
		}
	}
}
