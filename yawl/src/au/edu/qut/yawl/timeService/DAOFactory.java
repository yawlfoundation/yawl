package au.edu.qut.yawl.timeService;

public class DAOFactory {

	/*
	 * This method should return different
	 * storage mechanisms depending on some
	 * configuration variable in the web.xml file
	 * 
	 * */
	public static DAO getDAO() {
		return new InternalRunnerMemoryDAO();
	}
	
}
