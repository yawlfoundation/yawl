/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package au.edu.qut.yawl.persistence.dao;

import java.util.Map;

public class DAOFactory {
	private DAOFactory() {}
	
//	private static Map<PersistenceType, DAO> daoMap = new HashMap<PersistenceType, DAO>();
	
	public enum PersistenceType {EJB3, FILE, HIBERNATE, MEMORY, SPRING, HSQLDB};
	
	public Map daoMap;
	
	public DAO getDAO( PersistenceType type ) {
//		if( daoMap.get( type ) == null ) {
//			switch( type ) {
//				case EJB3:
//					daoMap.put( type, new DelegatedEJB3DAO() );
//					break;
//				case FILE:
//					// FIXME for now just return a new DAO for file DAOs
//					/* The reason for this is because the file DAO caches everything it has
//					 * loaded, and tests use the file DAO to read in test specs. Because of
//					 * the caching, hibernate tests end up re-persisting objects with the
//					 * same DB ID.
//					 * This is a hack that needs fixed.
//					 */
//					return new DelegatedFileDAO();
//					//break;
//				case HIBERNATE:
//                    throw new IllegalArgumentException("Hibernate is deprecated.");
////					daoMap.put( type, new DelegatedHibernateDAO() );
////					break;
//				case SPRING:
//					daoMap.put( type, new DelegatedCustomSpringDAO() );
//					break;
//				case MEMORY:
//				default:
//					daoMap.put( type, new DelegatedMemoryDAO() );
//			}
//		}
		return (DAO) daoMap.get( "hsql" );
	}

	public static void resetDAO(PersistenceType type) {
//		daoMap.remove(type);
	}

	public Map getDaoMap() {
		return daoMap;
	}

	public void setDaoMap(Map daoMap) {
		this.daoMap = daoMap;
	}

}
