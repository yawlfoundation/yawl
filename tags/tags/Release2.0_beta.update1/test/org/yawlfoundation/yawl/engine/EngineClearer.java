/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.engine;

import org.yawlfoundation.yawl.elements.state.YIdentifier;
import org.yawlfoundation.yawl.exceptions.YEngineStateException;
import org.yawlfoundation.yawl.exceptions.YPersistenceException;
import org.yawlfoundation.yawl.exceptions.YStateException;

import java.util.Iterator;
import java.util.Set;

/**
 * @author Lachlan Aldred
 * Date: 26/11/2004
 * Time: 17:42:40
 */
public class EngineClearer {
    public static void clear(YEngine engine) throws YPersistenceException, YEngineStateException {
        while (engine.getSpecIDs().iterator().hasNext()) {
            YSpecificationID specID = engine.getSpecIDs().iterator().next();
            Set caseIDs = engine.getCasesForSpecification(specID);
            for (Iterator iterator2 = caseIDs.iterator(); iterator2.hasNext();) {
                YIdentifier identifier = (YIdentifier) iterator2.next();
                engine.cancelCase(identifier);
            }
            try {
                engine.unloadSpecification(specID);
            } catch (YStateException e) {
                e.printStackTrace();
            }
        }
    }
}
