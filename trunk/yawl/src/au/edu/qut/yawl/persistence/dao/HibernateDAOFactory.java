package au.edu.qut.yawl.persistence.dao;

public class HibernateDAOFactory extends DAOFactory {
	@Override
	public SpecificationDAO getSpecificationModelDAO() {
		return new SpecificationHibernateDAO();
	}
}
 