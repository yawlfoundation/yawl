/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package au.edu.qut.yawl.persistence.dao.restrictions;

import java.io.Serializable;

/**
 * A Restriction is applied to the values in a DAO to determine which values should
 * be returned. Only those values which fit the restriction should be returned.
 * 
 * It is left to the DAO to determine how to check if the values fit the restriction
 * or not, but the class {@link RestrictionEvaluator} can be used to evaluate a
 * restriction for a single object.
 * 
 * DAOs are not required to support all types of Restriction. If a particular DAO
 * doesn't support a type of restriction, then it should throw an
 * {@link UnsupportedOperationException}.
 * 
 * @author Nathan Rose
 */
public interface Restriction extends Serializable {
}
