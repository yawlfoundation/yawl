/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.persistence.dao1;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import au.edu.qut.yawl.elements.YAWLServiceReference;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.engine.YSpecFile;
import au.edu.qut.yawl.engine.domain.YWorkItem;
import au.edu.qut.yawl.exceptions.YPersistenceException;

/**
 * TransientDao is a Dao type that does not persist any data.  It does not have any
 * notion of transaction context.
 * 
 * @author Dean Mao
 * @created Oct 27, 2005
 */
public class TransientDao extends YDao {

	public TransientDao() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	// SpecificationDao implementations
    private Map<String, YSpecification> _specifications = new HashMap<String, YSpecification>();
    private Map<String, YSpecification> _unloadedSpecifications = new HashMap<String, YSpecification>();
	
	public boolean existsLoadedSpecification( String specID ) throws YPersistenceException {
		return _specifications.containsKey(specID);
	}
	public boolean existsUnloadedSpecification( String specID ) throws YPersistenceException {
		return _unloadedSpecifications.containsKey(specID);
	}
	public YSpecification loadSpecification( String specID ) throws YPersistenceException {
		return _specifications.get(specID);
	}
	public Set<String> loadSpecificationIDs() throws YPersistenceException {
		return new HashSet<String>(_specifications.keySet());
	}
	public boolean storeSpecification( YSpecification specification ) throws YPersistenceException {
		if( !_specifications.containsKey( specification.getID() ) ) {
			_specifications.put( specification.getID(), specification );
			return true;
		}
		else {
			return false;
		}
	}

	public void commitTransaction() throws YPersistenceException {
		// Nothing to do for non-persistent storage
	}


	public void create( Serializable obj ) throws YPersistenceException {
		// TODO Auto-generated method stub
	}


	public void delete( Serializable obj ) throws YPersistenceException {
		if (obj instanceof YWorkItem) {
//			_idStringToWorkItemsMap.remove(((YWorkItem) obj).getIDString());
		} else if (obj instanceof YSpecFile) {
			// Do nothing?
		} else if (obj instanceof YAWLServiceReference) {
			// Do nothing?
		}
	}


	public void rollbackTransaction() throws YPersistenceException {
		// Nothing to do for non-persistent storage
	}


	public void startTransaction() throws YPersistenceException {
		// Nothing to do for non-persistent storage
	}


	public void update( Serializable obj ) throws YPersistenceException {
		// Nothing to do for non-persistent storage
	}

}
