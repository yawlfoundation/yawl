/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.elements;

import org.yawlfoundation.yawl.elements.state.YIdentifier;
import org.yawlfoundation.yawl.engine.YPersistenceManager;
import org.yawlfoundation.yawl.exceptions.YPersistenceException;
import org.yawlfoundation.yawl.exceptions.YStateException;

import java.util.List;

/**
 * 
 * This interface expresses the ability to hold tokens (Identifiers), nothing more.
 * @author Lachlan Aldred
 * Date: 17/04/2003
 * Time: 10:21:30
 * 
 */
public interface YConditionInterface {
    /**
     * @param identifier
     * @return true iff this contains identifier.
     */
    public boolean contains(YIdentifier identifier);

    /**
     * @return true iff this contains one or more identifier.
     */
    public boolean containsIdentifier();

    /**
     * Get the number of identifier matching the the one passed in.
     * @param identifier
     * @return the number of equal identifiers in this.
     */
    public int getAmount(YIdentifier identifier);

    /**
     * @return a List of the identifiers in the condition numbering 1 or more.
     */
    public List getIdentifiers();

    /**
     * Removes one YIdentifier from this condition.  If there are none
     * inside then make no change to the state of this.
     */
    public YIdentifier removeOne(YPersistenceManager pmgr) throws RuntimeException, YPersistenceException;

    /**
     * Removes one YIdentifier equal to identifier from the condition. If there are none
     * inside then make no change to the state of this.
     * @param identifier
     */
    public void removeOne(YPersistenceManager pmgr, YIdentifier identifier) throws YPersistenceException;

    /**
     * Remove from this amount YIdentifiers equal to identifier.
     * @param identifier
     * @param amount the amount to remove.
     * @throws YStateException iff amount is greater than the number of YIdentifiers
     * held inside this, and further more no change will be made to the state of this.
     */
    public void remove(YPersistenceManager pmgr, YIdentifier identifier, int amount) throws YStateException, YPersistenceException;

    /**
     * Removes all the YIdentifiers equal to identifier.
     * @param identifier
     */
    public void removeAll(YPersistenceManager pmgr, YIdentifier identifier) throws YPersistenceException;


    /**
     * Removes all the YIdentifiers equal to identifier.
     */
    public void removeAll(YPersistenceManager pmgr) throws YPersistenceException;


    /**
     * Adds an identifier to the collection.
     * @param id
     */
    public void add(YPersistenceManager pmgr, YIdentifier id) throws YPersistenceException;


}
