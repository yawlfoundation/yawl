/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.engine;

import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.exceptions.YStateException;
import au.edu.qut.yawl.exceptions.YPersistenceException;

import java.util.Set;
import java.util.Iterator;

/**
 * @author Lachlan Aldred
 * Date: 26/11/2004
 * Time: 17:42:40
 */
public class EngineClearer {
    public static void clear(YEngine engine) throws YPersistenceException {
        while (engine.getSpecIDs().iterator().hasNext()) {
            String specID = (String) engine.getSpecIDs().iterator().next();
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
