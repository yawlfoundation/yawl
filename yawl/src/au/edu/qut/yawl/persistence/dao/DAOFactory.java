package au.edu.qut.yawl.persistence.dao;


public abstract class DAOFactory {

	  // List of DAO types supported by the factory
	public enum Type {HIBERNATE, FILE, MEMORY};  

	  public abstract SpecificationDAO getSpecificationModelDAO();	
	
	  public static DAOFactory getDAOFactory(Type whichFactory) {
	    switch (whichFactory) {
	      case HIBERNATE: 
	          return new HibernateDAOFactory();
	      case FILE    : 
	          return new FileDAOFactory();      
	      default:
	    	  return new MemoryDAOFactory();
	  }
	}
}