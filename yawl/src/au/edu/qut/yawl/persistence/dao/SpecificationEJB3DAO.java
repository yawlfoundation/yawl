/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.persistence.dao;
import java.io.Serializable;
import java.util.List;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import au.edu.qut.yawl.elements.YSpecification;

@Stateless
@Remote(SpecificationDAO.class)
public class SpecificationEJB3DAO implements SpecificationDAO {
	
	public List getChildren(Object o) {
		// TODO Auto-generated method stub
		return null;
	}

	@PersistenceContext
	private EntityManager manager;

	public boolean delete( YSpecification t ) {
		manager.remove( t );
		return true;
	}

	public Serializable getKey( YSpecification t ) {
		return t.getDbID();
	}

	public YSpecification retrieve( Object key ) {
		return manager.find(YSpecification.class, key);
	}

	public int save( YSpecification t ) {
		manager.merge( t );
		return 0;
	}
}
