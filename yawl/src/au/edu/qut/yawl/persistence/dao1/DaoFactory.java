/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.persistence.dao1;


/**
 * Single class that everyone who wants to get a DAO object should call.  
 * 
 * @author Dean Mao
 * @created Oct 27, 2005
 */
public class DaoFactory {

	private static boolean PERSISTENT = false;
	
	// TODO remove this later in proper transient dao construction
	private static TransientDao TDAO = new TransientDao();
	
	public DaoFactory() {
		super();
	}
	
	public static boolean isJournalising() {
		return PERSISTENT;
	}
	
	public static void setJournalising(boolean journalising) {
		PERSISTENT = journalising;
	}
	
	public static Dao createDao(Object obj) {
		return TDAO;
	}
	
	public static YDao createYDao() {
		return TDAO;
	}
	
	public static Dao createDao() {
		return TDAO;
	}
	
//	public static HibernatePersistentDao createHibernateDao() {
//		return null;
//	}
}
