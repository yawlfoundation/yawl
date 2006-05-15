package au.edu.qut.yawl.persistence.dao;

public class FileDAOFactory extends DAOFactory {
	@Override
	public SpecificationDAO getSpecificationModelDAO() {
		return new SpecificationFileDAO();
	}
}
 