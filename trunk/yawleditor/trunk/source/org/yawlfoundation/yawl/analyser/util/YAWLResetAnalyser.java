/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.analyser.util;

import org.yawlfoundation.yawl.analyser.YAnalyserEvent;
import org.yawlfoundation.yawl.analyser.YAnalyserEventListener;
import org.yawlfoundation.yawl.analyser.YAnalyserEventType;
import org.yawlfoundation.yawl.analyser.YAnalyserOptions;
import org.yawlfoundation.yawl.analyser.elements.ResetWFNet;
import org.yawlfoundation.yawl.analyser.reductionrules.*;
import org.yawlfoundation.yawl.elements.YDecomposition;
import org.yawlfoundation.yawl.elements.YNet;
import org.yawlfoundation.yawl.elements.YSpecification;
import org.yawlfoundation.yawl.elements.YTask;
import org.yawlfoundation.yawl.util.StringUtil;

import java.util.Set;

public class YAWLResetAnalyser {

    private boolean _cancelled = false;
    private Set<YAnalyserEventListener> _listeners;


    public String analyse(YSpecification specification, YAnalyserOptions options,
                          Set<YAnalyserEventListener> listeners) {

        // short circuit if no reset options selected
        if (! options.isResetAnalysis()) return "";

        long startTime = System.currentTimeMillis();
        _listeners = listeners;
        announceProgressEvent(YAnalyserEventType.Init, null, null);

        StringBuilder msgBuffer = new StringBuilder(400);

        for (YDecomposition decomposition : specification.getDecompositions()) {
            if (decomposition instanceof YNet) {
                YNet yNet = (YNet) decomposition;

                // check for OR-joins before any reduction
                boolean yNetContainsORJoins = containsORjoins(yNet);

                if (options.isYawlReductionRules()) {
                    YNet reducedYNet = reduceNet(yNet);
                    if (reducedYNet != null) yNet = reducedYNet;
                }

                ResetWFNet resetNet = new ResetWFNet(yNet);
                YAWLReachabilityUtils utils = new YAWLReachabilityUtils(yNet);

                if (options.isResetReductionRules()) {
                    ResetWFNet reducedNet = reduceNet(resetNet);
                    if (reducedNet != null) resetNet = reducedNet;
                }

                resetNet.setParent(this);
                utils.setParent(this);

                if (options.isResetWeakSoundness()) {
                    msgBuffer.append(resetNet.checkWeakSoundness());
                }
                if (options.isResetSoundness()) {
                    msgBuffer.append(checkSoundness(resetNet, utils));
                }
                if (options.isResetCancellation()) {
                    msgBuffer.append(checkCancellation(resetNet, utils));
                }
                if (options.isResetOrJoin()) {
                    msgBuffer.append(checkOrJoins(resetNet, utils,
                            options.isYawlReductionRules() && yNetContainsORJoins,
                            yNet.getID()));
                }
                if (options.isResetOrjoinCycle()) {
                    msgBuffer.append(checkOrJoinCycles(yNet));
                }
            }
        }

        announceProgressMessage("Duration: " + (System.currentTimeMillis() - startTime) +
                " milliseconds");
        announceProgressEvent(YAnalyserEventType.Completed, null, null);

        return _cancelled ? StringUtil.wrap("Analysis cancelled.", "cancelled") :
                formatXMLMessageResults(msgBuffer.toString());
    }

    public void cancel() { _cancelled = true; }  // break-out flag

    public boolean isCancelled() { return _cancelled; }


    /********************************************************************************/

    private String checkSoundness(ResetWFNet resetNet, YAWLReachabilityUtils utils) {
        try {
            if (resetNet.containsORjoins()) {
                return utils.checkSoundness();
            }
            else {
                return resetNet.checkSoundness();
            }
        }
        catch (Exception e) {
           return formatXMLMessage(e.getMessage(), false);
        }
    }


    private String checkCancellation(ResetWFNet resetNet, YAWLReachabilityUtils utils) {
        try {
            if (resetNet.containsORjoins()) {
                return utils.checkCancellationSets();
            }
            else {
                return resetNet.checkCancellationSets();
            }
        }
        catch (Exception e) {
            return formatXMLMessage(e.getMessage(), false);
        }
    }


    private String checkOrJoins(ResetWFNet resetNet, YAWLReachabilityUtils utils,
                                boolean orJoinsInUnreducedNet, String netID) {
        try {
            if (resetNet.containsORjoins()) {
                return utils.checkUnnecessaryORJoins();
            }
            else {

                //It is possible that YAWL reduction rules were applied first,
                // thus removing OR-joins.
                if (orJoinsInUnreducedNet) {
                    return formatXMLMessage(
                        "There are no OR-joins in the reduced net of " + netID +
                        ". Please recheck this property with YAWL reduction rules disabled.",
                        true);
                }
                else {
                    return formatXMLMessage("There are no OR-joins in the net " + netID +
                        ".", true);
                }
            }
        }
        catch (Exception e) {
            return formatXMLMessage(e.getMessage(), false);
        }
    }


    private String checkOrJoinCycles(YNet yNet) {
        try {
            return new OrjoinInCycleUtils().checkORjoinsInCycle(yNet);
        }
        catch (Exception e) {
            return formatXMLMessage(e.getMessage(),false);
        }
    }


    private boolean containsORjoins(YNet yNet) {
        for ( YTask task : yNet.getNetTasks()) {
            if (task.getJoinType() == YTask._OR) return true;
        }
        return false;
    }


    private YNet reduceNet(YNet originalNet) {
        announceProgressMessage("# Elements in the original YAWL net (" +
                originalNet.getID() + "): " + originalNet.getNetElements().size());
        YAWLReductionRule rule;
        YNet tempReducedNet = null;
        YNet reducedNet = originalNet;
        int loop = 0;
        String rulesMsg = "";
        boolean containsORjoinCycles = new OrjoinInCycleUtils().hasORjoinsInCycle(originalNet);

        do {
            loop++;
            for (YAWLReductionRuleType ruleType : YAWLReductionRuleType.values()) {
                rule = ruleType.getRule();
                if (rule != null) {

                    // FOR rule is only applied to nets without or-join cycles
                    if ((rule instanceof FORrule) && containsORjoinCycles) continue;

                    tempReducedNet = rule.reduce(reducedNet);
                    if (tempReducedNet != null) {
                        rulesMsg += ruleType.name() + ";";
                        reducedNet = tempReducedNet;
                        break;
                    }
                }
            }
            if (tempReducedNet == null) {    // do-while break point
                loop --;
                announceProgressMessage("YAWL Reduced net " + loop + " rules: " +
                        rulesMsg + "\nReduced net size: " +
                        reducedNet.getNetElements().size());
                return reducedNet;
            }
            if (_cancelled) {
                announceProgressEvent(YAnalyserEventType.Cancelled, null, null);
                return null;
            }
        } while (true);
    }

    private ResetWFNet reduceNet(ResetWFNet originalNet) {
        announceProgressMessage("# Elements in the original reset net: " +
                originalNet.getNetElements().size());
        ResetReductionRule rule;
        ResetWFNet tempReducedNet = null;
        ResetWFNet reducedNet = new ResetWFNet(originalNet);    // a copy of the original
        reducedNet.setParent(this);
        String rulesMsg = "";
        int loop = 0;

        do {
            loop++;
            for (ResetReductionRuleType ruleType : ResetReductionRuleType.values()) {
                rule = ruleType.getRule();
                if (rule != null) {
                    tempReducedNet = rule.reduce(reducedNet);
                    if (tempReducedNet != null) {
                        rulesMsg += ruleType.name() + ";";
                        reducedNet = tempReducedNet;
                        break;
                    }
                }
            }
            if (tempReducedNet == null) {
                if (reducedNet != originalNet) {
                    loop --;
                    announceProgressMessage("Reset Reduced net " + loop + " rules: " +
                            rulesMsg + "\nReduced net size: " +
                            reducedNet.getNetElements().size());
                    return reducedNet;
                }
                else return null;
            }
            if (_cancelled) {
                announceProgressEvent(YAnalyserEventType.Cancelled, null, null);
                return null;
            }
        } while (true);
    }


    private String formatXMLMessageResults(String msg) {
        return StringUtil.wrap(msg, "resetAnalysisResults");
    }

    /**
     * used for formatting xml messages.
     * Message could be a warning or observation.
     */
    private String formatXMLMessage(String msg, boolean isObservation) {
        return StringUtil.wrap(msg, isObservation ? "observation" : "warning");
    }


    public void announceProgressMessage(String message) {
        announceProgressEvent(YAnalyserEventType.Message, null, message);
    }


    protected void announceProgressEvent(YAnalyserEventType eventType,
                                         String source, String message) {
        announceProgressEvent(new YAnalyserEvent(eventType, source, message));
    }


    protected void announceProgressEvent(YAnalyserEvent event) {
        if (_listeners != null) {
            for (YAnalyserEventListener listener : _listeners) {
                listener.yAnalyserEvent(event);
            }
        }
    }

}