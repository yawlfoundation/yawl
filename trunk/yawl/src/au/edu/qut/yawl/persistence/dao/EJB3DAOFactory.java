package au.edu.qut.yawl.persistence.dao;

public class EJB3DAOFactory extends DAOFactory {
	@Override
	public SpecificationDAO getSpecificationModelDAO() {
		return new SpecificationEJB3DAO();
	}
}
 