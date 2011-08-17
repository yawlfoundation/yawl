/*
 * Copyright (c) 2004-2011 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.elements;

import org.yawlfoundation.yawl.elements.state.YIdentifier;
import org.yawlfoundation.yawl.engine.YPersistenceManager;
import org.yawlfoundation.yawl.exceptions.YPersistenceException;
import org.yawlfoundation.yawl.exceptions.YStateException;

import java.util.List;

/**
 * This interface expresses the ability to hold tokens (Identifiers), nothing more.
 * @author Lachlan Aldred
 * @since 0.1
 * @date 17/04/2003
 */
public interface YConditionInterface {

    /**
     * Check whether a condition contains the specified identifier.
     * @param identifier the identifier in question.
     * @return true iff this contains identifier.
     */
    public boolean contains(YIdentifier identifier);

    /**
     * Check whether a condition has at least one identifier.
     * @return true iff this contains one or more identifier.
     */
    public boolean containsIdentifier();

    /**
     * Get the number of identifiers in a condition that match the specified identifier.
     * @param identifier the identifier in question.
     * @return the number of equal identifiers in the condition.
     */
    public int getAmount(YIdentifier identifier);

    /**
     * Get all the identifiers in a condition.
     * @return a List of the identifiers in the condition.
     */
    public List<YIdentifier> getIdentifiers();

    /**
     * Remove one identifier from the condition. If there are none to remove
     * then make no change to the condition's state.
     * @param pmgr an instantiated persistence manager object.
     * @return the identifier that has been removed.
     * @throws RuntimeException if there's a problem removing the identifier.
     * @throws YPersistenceException if there's a problem persisting the change.
     */
    public YIdentifier removeOne(YPersistenceManager pmgr) throws RuntimeException, YPersistenceException;

    /**
     * Remove one identifier equal to the specified identifier from the condition.
     * If there are none to remove, or none matching the specified identifier, then
     * make no change to the condition's state.
     * @param pmgr an instantiated persistence manager object.
     * @param identifier an identifier matching the one to be removed.
     * @throws YPersistenceException if there's a problem persisting the change.
     */
    public void removeOne(YPersistenceManager pmgr, YIdentifier identifier) throws YPersistenceException;

    /**
     * Remove a specified number of identifiers equal to the specified identifier
     * from the condition.
     * @param pmgr an instantiated persistence manager object.
     * @param identifier an identifier matching the ones to be removed.
     * @param amount the number of matching identifiers to remove.
     * @throws YStateException if the amount specified is greater than the number of identifiers
     * held inside the condition, and furthermore no change will be made to the state of this.
     * @throws YPersistenceException if there's a problem persisting the change.
     */
    public void remove(YPersistenceManager pmgr, YIdentifier identifier, int amount) throws YStateException, YPersistenceException;

    /**
     * Remove all the identifiers that match the specified identifier.
     * @param pmgr an instantiated persistence manager object.
     * @param identifier an identifier matching the ones to be removed.
     * @throws YPersistenceException if there's a problem persisting the change.
     */
    public void removeAll(YPersistenceManager pmgr, YIdentifier identifier) throws YPersistenceException;

    /**
     * Remove all the identifiers in the condition.
     * @param pmgr an instantiated persistence manager object.
     * @throws YPersistenceException if there's a problem persisting the change.
     */
    public void removeAll(YPersistenceManager pmgr) throws YPersistenceException;

    /**
     * Add an identifier to the condition.
     * @param pmgr an instantiated persistence manager object.
     * @param id the identifier to add.
     * @throws YPersistenceException if there's a problem persisting the change.
     */
    public void add(YPersistenceManager pmgr, YIdentifier id) throws YPersistenceException;

}
