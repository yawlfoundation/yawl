/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.util;

import org.yawlfoundation.yawl.elements.YCondition;
import org.yawlfoundation.yawl.elements.YConditionInterface;
import org.yawlfoundation.yawl.elements.YNetElement;
import org.yawlfoundation.yawl.elements.YTask;
import org.yawlfoundation.yawl.elements.state.YIdentifier;
import org.yawlfoundation.yawl.elements.state.YInternalCondition;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * 
 * @author Lachlan Aldred
 * Date: 22/10/2003
 * Time: 15:25:41
 * 
 */
public class YStateInspector {

    public static String inspectState(YIdentifier parentID) {
        //###########################################################################
        //##########            BEGIN State inspection code             #############
        //###########################################################################
        Set allChildren = parentID.getDescendants();
        Set allLocations = new HashSet();
        for (Iterator childIter = allChildren.iterator(); childIter.hasNext();) {
            YIdentifier identifier = (YIdentifier) childIter.next();
            allLocations.addAll(identifier.getLocations());
        }
        StringBuffer stateText = new StringBuffer();
        for (Iterator locationsIter = allLocations.iterator(); locationsIter.hasNext();) {
            YNetElement element = (YNetElement) locationsIter.next();
            if (element instanceof YCondition) {
                stateText.append("CaseIDs in: " + element.toString() + "\r\n");
                List identifiers = ((YConditionInterface) element).getIdentifiers();
                for (Iterator idIter = identifiers.iterator(); idIter.hasNext();) {
                    YIdentifier identifier = (YIdentifier) idIter.next();
                    stateText.append("\t" + identifier.toString() + "\r\n");
                }
            } else if (element instanceof YTask) {
                stateText.append("CaseIDs in: " + element.toString() + "\r\n");
                YTask task = (YTask) element;
                for (int i = 0; i < 4; i++) {
                    YInternalCondition internalCondition = null;
                    if (i == 0) {
                        internalCondition = task.getMIActive();
                    }
                    if (i == 1) {
                        internalCondition = task.getMIEntered();
                    }
                    if (i == 2) {
                        internalCondition = task.getMIExecuting();
                    }
                    if (i == 3) {
                        internalCondition = task.getMIComplete();
                    }
                    if (internalCondition.containsIdentifier()) {
                        stateText.append("\t" + internalCondition.toString() + "\r\n");
                        List identifiers = internalCondition.getIdentifiers();
                        for (Iterator iterator = identifiers.iterator(); iterator.hasNext();) {
                            YIdentifier identifier = (YIdentifier) iterator.next();
                            stateText.append("\t\t" + identifier.toString() + "\r\n");
                        }
                    }
                }

            }
        }
        return stateText.toString();
        //###########################################################################
        //#######                  END state inspection code               ##########
        //###########################################################################
    }
}
