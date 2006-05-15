package au.edu.qut.yawl.persistence.dao;


public class MemoryDAOFactory extends DAOFactory {
	@Override
	public SpecificationDAO getSpecificationModelDAO() {
		return new SpecificationMemoryDAO();
	}
}
 