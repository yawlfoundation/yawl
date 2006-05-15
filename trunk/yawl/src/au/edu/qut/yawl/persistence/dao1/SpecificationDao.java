/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.persistence.dao1;

import java.util.Set;

import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.exceptions.YPersistenceException;

/**
 * Interface to replace the specification HashMap in YEngine
 * 
 * @author Dean Mao
 * @created Oct 27, 2005
 */
public interface SpecificationDao {
	/**
	 * Stores specification in system
	 * 
	 * @param specification
	 * @return true if specification can be stored, false if the specification is already stored
	 * @throws YPersistenceException
	 */
	public boolean storeSpecification(YSpecification specification) throws YPersistenceException;
	/**
	 * Will load the specification from the loaded or unloaded sets.
	 * 
	 * @param specID
	 * @return
	 * @throws YPersistenceException
	 */
	public YSpecification loadSpecification(String specID) throws YPersistenceException;
	/**
	 * checks to see if the specification is loaded in the system.
	 * 
	 * @param specID of the specification
	 * @return true if the specification exists loaded in the system, false if not
	 * @throws YPersistenceException
	 */
	public boolean existsLoadedSpecification(String specID) throws YPersistenceException;
	/**
	 * checks to see if the specification is unloaded in the system.
	 * 
	 * @param specID of the specification
	 * @return true if the specification exists unloaded in the system, false if not
	 * @throws YPersistenceException
	 */
	public boolean existsUnloadedSpecification(String specID) throws YPersistenceException;
	/**
	 * Loads a set of specification IDs (Strings) that are loaded in the system
	 * 
	 * @return <code>Set&lt;String&gt;</code> of specification ids 
	 * @throws YPersistenceException
	 */
	public Set loadSpecificationIDs() throws YPersistenceException;
}
