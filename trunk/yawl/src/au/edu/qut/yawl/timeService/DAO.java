package au.edu.qut.yawl.timeService;

import java.util.List;

public interface DAO {

	public List retrieveAll();
	public void delete();
	public void save(InternalRunner r);
	
}
