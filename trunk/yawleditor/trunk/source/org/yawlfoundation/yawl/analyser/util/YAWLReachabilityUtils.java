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

import org.yawlfoundation.yawl.elements.*;
import org.yawlfoundation.yawl.elements.state.YIdentifier;
import org.yawlfoundation.yawl.elements.state.YMarking;
import org.yawlfoundation.yawl.elements.state.YSetOfMarkings;
import org.yawlfoundation.yawl.exceptions.YPersistenceException;
import org.yawlfoundation.yawl.util.StringUtil;

import java.util.*;

/**
 * This class is used to determine reachability set for a YAWL net.
 *
 */
public class YAWLReachabilityUtils{

    private final YNet _yNet;
    private final YSetOfMarkings endMarkings = new YSetOfMarkings();
    private final YSetOfMarkings VisitedMarkings = new YSetOfMarkings();
    private YSetOfMarkings RS;
    private final Set<YTask> firedTasks = new HashSet<YTask>();
    private final Set<YTask> orJoins = new HashSet<YTask>();
    private final Map<String, YSetOfMarkings> ojMarkingsMap = new HashMap<String, YSetOfMarkings>();
    private int _maxMarkings = 5000;

    private YAWLResetAnalyser _parent;

    public YAWLReachabilityUtils(YNet net) {
        _yNet =  transformNet(net);
    }

    public YAWLReachabilityUtils(YNet net, int maxMarkings) {
        this(net);
        _maxMarkings = maxMarkings;
    }

    public void setParent(YAWLResetAnalyser parent) {
        _parent = parent;
    }

    /**
     * Returns whether the set of markings contains markings that marks
     * all input places of OJ and not smaller AND-join - 0
     * only one input place of OJ and not bigger 1
     * more than one input place - 3 and 4
     */
    private int checkORjoinStatus(YSetOfMarkings RS, Set<YNetElement> presetOJ) {
        List<YNetElement> preSetList = new ArrayList<YNetElement>(presetOJ);
        YMarking orJoinANDMarking = new YMarking(preSetList);
        YSetOfMarkings orJoinXORMarkings = new YSetOfMarkings();
        for (YNetElement element : presetOJ) {
            List<YNetElement> eList = new ArrayList<YNetElement>();
            eList.add(element);
            orJoinXORMarkings.addMarking(new YMarking(eList));
        }

        //check if RS contains all marked preset and not smaller
        if (RS.containsBiggerEqual(orJoinANDMarking)) {
            return (! containsLessThanMarking(RS, orJoinANDMarking, presetOJ)) ? 0 : 3;
        }
        //otherwise check one each
        for (YMarking orJoinXORMarking : orJoinXORMarkings.getMarkings()) {
            if (! checkXOR(RS, orJoinXORMarking, presetOJ)) return 4;
        }
        return 1;
    }

    /**
     * returns true if there is a smaller marking M[preset] < orJoinANDMarking.
     *
     */
    private boolean containsLessThanMarking(YSetOfMarkings RS, YMarking orJoinANDMarking,
                                            Set<YNetElement> presetOJ) {
        for (YMarking m : RS.getMarkings()) {
            List<YNetElement> locations = new ArrayList<YNetElement>(m.getLocations());
            locations.retainAll(presetOJ);

            // only for markings that mark preset of OJ.
            if (locations.size() > 0) {
                YMarking mp = new YMarking(locations);
                if (! mp.isBiggerThanOrEqual(orJoinANDMarking)) return true;
            }
        }
        return false;
    }

    /**
     * returns true if no marking bigger than XOR is found in RS.
     */
    private boolean checkXOR(YSetOfMarkings RS, YMarking orJoinXORMarking,
                             Set<YNetElement> presetOJ) {
        for (YMarking m : RS.getMarkings()) {
            List<YNetElement> locations = new ArrayList<YNetElement>(m.getLocations());
            locations.retainAll(presetOJ);
            YMarking mp = new YMarking(locations);
            if (mp.isBiggerThan(orJoinXORMarking)) return false;
        }
        return true;
    }

    /**
     * Used to detect whether OR-joins should be replaced by XOR or AND.
     * returns a message.
     */
    public String checkUnnecessaryORJoins() throws IllegalStateException {
        String msg="";
        String xor="";
        String and="";
        boolean changeToAND = false;
        boolean changeToXOR = false;
        if (RS == null) {
            List<YNetElement> iLocation = new ArrayList<YNetElement>();
            iLocation.add(_yNet.getInputCondition());
            YMarking Mi = new YMarking(iLocation);
            RS = getReachableMarkings(Mi);
        }

        //first identify all OR-joins
        for (YTask orJoin : orJoins) {
            Set<YNetElement> preSet = new HashSet<YNetElement>(orJoin.getPresetElements());
            YSetOfMarkings ojMarkings = ojMarkingsMap.get(orJoin.getID());
            if (ojMarkings != null) {
                int status = checkORjoinStatus(ojMarkings, preSet);
                if (status == 0) {
                    changeToAND = true;
                    and += orJoin.getID()+" ";
                }
                else if (status == 1) {
                    changeToXOR = true;
                    xor += orJoin.getID()+" ";
                }
            }
        }

        if (changeToAND) {
            String xormsg = "OR-join task(s) " + and + " in the net " + _yNet.getID() +
                    " could be more simply modelled as AND-join task(s).";
            msg = formatXMLMessage(xormsg, false);
        }
        if (changeToXOR) {
            String andmsg = "OR-join task(s) " + xor +" in the net "+_yNet.getID() +
                    " could be more simply modelled as XOR-join task(s).";
            msg += formatXMLMessage(andmsg,false);
        }
        if (! (changeToAND || changeToXOR)) {
            msg = "The net " +_yNet.getID() + " satisfies the immutable OR-joins property.";
            msg = formatXMLMessage(msg,true);
        }
        return msg;
    }

    /**
     *  Check if the cancellation sets are unnecessary.
     */
    public String checkCancellationSets() throws IllegalStateException {
        List<YNetElement> iLocation = new ArrayList<YNetElement>();
        iLocation.add(_yNet.getInputCondition());
        YMarking Mi = new YMarking(iLocation);
        YSetOfMarkings RS = getReachableMarkings(Mi);
        List<YExternalNetElement> mLocation = new ArrayList<YExternalNetElement>();
        YMarking M;
        List<String> msgArray = new ArrayList<String>();
        Set<YExternalNetElement> cancelElements = new HashSet<YExternalNetElement>();
        Set<YTask> cancelByTasks = new HashSet<YTask>();
        String msg= "";
        boolean cancelWarning = false;

        for (YExternalNetElement netElement : _yNet.getNetElements().values()) {
            if (netElement instanceof YTask) {
                YTask task = (YTask) netElement;
                if (! task.getRemoveSet().isEmpty()) {
                    mLocation.clear();

                    //Try to get a c_t for task t
                    for (YExternalNetElement preElement : task.getPresetElements()) {
                        if (preElement instanceof YCondition) mLocation.add(preElement);
                    }

                    // Create a marking with one token in one of the conditions from the
                    // remove set and one token in c_t
                    for (YExternalNetElement remElement : task.getRemoveSet()) {

                        // it is possible that input and reset places can overlap
                        // now that we can be dealing with reduced nets.
                        YExternalNetElement mappedElem = findYawlMapping(remElement.getID());
                        if (mappedElem instanceof YCondition) {
                            mLocation.add(mappedElem);
                            M = new YMarking(new ArrayList<YNetElement>(mLocation));
                            if (! isCoverable(RS, M)) {
                                cancelElements.add(mappedElem);
                                cancelElements.addAll(convertToYawlMappings(mappedElem));
                                cancelByTasks.add(task);
                                cancelByTasks.addAll(convertToYawlMappingsForTasks(task));
                                cancelWarning = true;
                            }
                            mLocation.remove(mappedElem);
                        }
                    }
                }

                if (cancelWarning) {
                    msgArray.add("Element(s) " + cancelElements +
                            " should not be in the cancellation set of task(s) "
                            + cancelByTasks +".");
                }
                cancelElements.clear();
                cancelByTasks.clear();
                cancelWarning = false;
            }
        }

        if (msgArray.isEmpty()) {
            msg = "The net "+ _yNet.getID() +
                    " satisfies the irreducible cancellation regions property.";
            msg = formatXMLMessage(msg, true);
        }
        else {
            for (String rawMsg : msgArray) {
                msg += formatXMLMessage(rawMsg, false);
            }
        }
        return msg;
    }


    private boolean isCoverable(YSetOfMarkings RS, YMarking M) {
        return RS.containsBiggerEqual(M);
    }

    /**
     * The method to check soundness property of YNets using reachability analysis.
     * returns messages.
     * note: this method also generates endMarkings, orJoins and orJoinsMarkingsMap
     */
    public String checkSoundness() {
        String msg = "";

        List<YNetElement> iLocation = new ArrayList<YNetElement>();
        iLocation.add(_yNet.getInputCondition());
        List<YNetElement> oLocation = new ArrayList<YNetElement>();
        oLocation.add(_yNet.getOutputCondition());

        YMarking Mi = new YMarking(iLocation);
        YMarking Mo = new YMarking(oLocation);

        boolean optionToComplete;
        boolean properCompletion = true;
        boolean noDeadTasks = true;

        RS = getReachableMarkings(Mi);

        String omsg = "";

        //Option to complete property
        //If the set does not contain Mo then false
        if (endMarkings.size() == 1 && endMarkings.contains(Mo)) {
            optionToComplete = true;
            omsg = "The net " + _yNet.getID() + " has an option to complete.";
        }
        else {
            optionToComplete = false;
            if (!endMarkings.contains(Mo)) {
                omsg = " The final marking Mo is not reachable.";
            }

            String deadlockmsg = "";
            for (YMarking currentM : endMarkings.getMarkings()) {
                if (!currentM.isBiggerThanOrEqual(Mo)) {
                    deadlockmsg += printMarking(currentM) + " ";
                }
            }
            omsg = "The net "+_yNet.getID()+" does not have an option to complete." + omsg;
            if (deadlockmsg.length() > 0) {
                omsg = omsg + " Potential deadlocks: " + deadlockmsg;
            }
        }

        msg += formatXMLMessage(omsg,optionToComplete);

        // Check for larger end markings than o.
        String pmsg = "";
        for (YMarking currentM : RS.getMarkings()) {
            if (currentM.isBiggerThan(Mo)) {
                pmsg += printMarking(currentM) + " ";
                properCompletion = false;
            }
        }

        if (properCompletion) {
            pmsg = "The net "+_yNet.getID()+" has proper completion.";
        }
        else {
            pmsg = "The net "+_yNet.getID()+" does not have proper completion. Markings larger than Mo can be found: " +pmsg;
        }
        msg += formatXMLMessage(pmsg, properCompletion);

        String dmsg;
        Set<YTask> deadTasks = new HashSet<YTask>();
        for (YExternalNetElement nextElement : _yNet.getNetElements().values()) {
            if (nextElement instanceof YTask) {
                YTask task = (YTask) nextElement;
                if (!firedTasks.contains(task)) {
                    deadTasks.add(task);
                    // if dealing with reduced nets
                    deadTasks.addAll(convertToYawlMappingsForTasks(task));
                    noDeadTasks = false;
                }
            }
        }

        if (noDeadTasks) {
            dmsg = "The net "+_yNet.getID()+" has no dead tasks.";
        }
        else {
            dmsg = "The net "+_yNet.getID()+" has dead tasks:" + deadTasks.toString();
        }

        msg += formatXMLMessage(dmsg,noDeadTasks);


        //To display message regarding soundness property.
        String smsg;
        boolean isSound = true;
        if (optionToComplete && properCompletion && noDeadTasks) {
            smsg = "The net "+_yNet.getID() +" satisfies the soundness property.";
        }
        else {
            smsg = "The net "+_yNet.getID() +" does not satisfy the soundness property.";
            isSound = false;
        }
        msg += formatXMLMessage(smsg,isSound);

        return msg;
    }

    private YSetOfMarkings getReachableMarkings(YMarking M) throws IllegalStateException {
        YSetOfMarkings RS = new YSetOfMarkings();
        VisitedMarkings.removeAll();
        endMarkings.removeAll();

        YSetOfMarkings visitingPS = getImmediateSuccessors(M);
        RS.addMarking(M);
        VisitedMarkings.addMarking(M);

        while (!RS.containsAll(visitingPS.getMarkings())) {
            if (_parent.isCancelled()) return RS;
            RS.addAll(visitingPS);
            if (RS.size() > _maxMarkings) {
                String msg = "Reachable markings exceeds limit (" + _maxMarkings +
                        "). Possible infinite loop in the net '" + _yNet.getID() +
                        "'. Unable to complete analysis of this net.";
                _parent.announceProgressMessage(msg);
                throw new IllegalStateException(msg);
            }

            YSetOfMarkings successors = getImmediateSuccessors(visitingPS);
            visitingPS.removeAll();
            visitingPS.addAll(successors);
            _parent.announceProgressMessage("Immediate Successors: " + visitingPS.size());
        }

        _parent.announceProgressMessage("Reachability Set size: " + RS.size());
        return RS;
    }

    /**
     * return successor markings from a set of markings.
     */
    private YSetOfMarkings getImmediateSuccessors(YSetOfMarkings markings) {
        YSetOfMarkings successors = new YSetOfMarkings();
        for (YMarking currentM : markings.getMarkings()) {
            if (! VisitedMarkings.contains(currentM)) {
                YSetOfMarkings post = getImmediateSuccessors(currentM);
                VisitedMarkings.addMarking(currentM);
                if (post.size() > 0) {
                    successors.addAll(post);
                }
                else {
                    endMarkings.addMarking(currentM);
                }
            }
        }
        return successors;
    }


    /**
     * return successor markings from a marking.
     *
     *
     */
    private YSetOfMarkings getImmediateSuccessors(YMarking currentM) {
        YSetOfMarkings successors = new YSetOfMarkings();
        for (YExternalNetElement netElement : _yNet.getNetElements().values()) {
            if (netElement instanceof YTask) {
                YTask task = (YTask) netElement;
                if (isForwardEnabled(currentM, task)) {
                    YSetOfMarkings nextMs = getNextMarkings(currentM, task);
                    successors.addAll(nextMs);
                    firedTasks.add(task);
                }
            }
        }
        return successors;
    }

    /***
     * Changes made to fix the bug with XORsplit
     */
    private YSetOfMarkings getNextMarkings(YMarking currentM, YTask task) {
        YSetOfMarkings successors = new YSetOfMarkings();
        List<YNetElement> locations = new ArrayList<YNetElement>(currentM.getLocations());

        switch (task.getJoinType()){
            case YTask._AND:
            case YTask._OR: {
                for (YNetElement netElement : task.getPresetElements()) {
                    locations.remove(netElement);
                }
                successors.addMarking(new YMarking(locations));
                break;
            }
            case YTask._XOR: {
                for (YNetElement netElement : task.getPresetElements()) {
                    //if it is marked
                    if (locations.contains(netElement)) {
                        locations.remove(netElement);
                        successors.addMarking(new YMarking(locations));
                        locations.add(netElement);
                    }
                }
                break;
            }
         }

        //Remove tokens from cancellation region
        Set<YExternalNetElement> cancelset = task.getRemoveSet();
        if (cancelset.size() > 0) {
            YSetOfMarkings temp = new YSetOfMarkings();
            for (YMarking M : successors.getMarkings()) {
                List<YNetElement> slocations = M.getLocations();
                for (YExternalNetElement netElement : cancelset) {
                    slocations.remove(netElement);
                }
                temp.addMarking(new YMarking(slocations));
            }
            successors.removeAll();
            successors.addAll(temp);
        }

        switch(task.getSplitType()){
            case YTask._AND: {
                YSetOfMarkings temp = new YSetOfMarkings();
                for (YMarking M : successors.getMarkings()) {
                    List<YNetElement> slocations = new ArrayList<YNetElement>(M.getLocations());
                    for (YNetElement netElement : task.getPostsetElements()) {
                        slocations.add(netElement);
                    }
                    temp.addMarking(new YMarking(slocations));
                }
                successors.removeAll();
                successors.addAll(temp);
                break;
            }
            case YTask._OR: { //generate combinations
                YSetOfMarkings temp = new YSetOfMarkings();
                for (YMarking M : successors.getMarkings()) {
                    for (Set<YNetElement> subSet : generateCombinations(
                                            task.getPostsetElements())) {
                        List<YNetElement> slocations = new ArrayList<YNetElement>(M.getLocations());
                        slocations.addAll(subSet);
                        temp.addMarking(new YMarking(slocations));
                    }
                }
                successors.removeAll();
                successors.addAll(temp);
                break;
            }
            case YTask._XOR: {
                YSetOfMarkings temp = new YSetOfMarkings();
                for (YMarking M : successors.getMarkings()) {
                    List<YNetElement> slocations = new ArrayList<YNetElement>(M.getLocations());
                    for (YExternalNetElement netElement : task.getPostsetElements()) {
                        slocations.add(netElement);
                        temp.addMarking(new YMarking(slocations));
                        slocations.remove(netElement);
                    }
                }
                successors.removeAll();
                successors.addAll(temp);
                break;
            }
        }
        return successors;
    }

    private Set<Set<YNetElement>> generateCombinations(
            Set<YExternalNetElement> elementSet) {
        Set<Set<YNetElement>> subSets = new HashSet<Set<YNetElement>>();
        for (int i=1; i <= elementSet.size(); i++) {
            Set<Set<YNetElement>> subSet = generateCombination(elementSet, i);
            subSets.addAll(subSet);
        }
        return subSets;
    }




    private boolean isForwardEnabled(YMarking currentM, YTask task) {
        Set<YExternalNetElement> preSet = task.getPresetElements();
        switch (task.getJoinType()) {
            case YTask._AND: {
                List<YNetElement> eleList = new ArrayList<YNetElement>(preSet);
                return currentM.isBiggerThanOrEqual(new YMarking(eleList));
            }
            case YTask._OR: {   // use for unnecessary OR-joins
                orJoins.add(task);
                YIdentifier id = convertMarkingToIdentifier(currentM);
                boolean isOJEnabled = _yNet.orJoinEnabled(task,id);

                //use for unnecessary OR-join enabling markings.
                if (isOJEnabled) {
                    YSetOfMarkings ojMarkings = ojMarkingsMap.get(task.getID());
                    if (ojMarkings == null) {
                        ojMarkings = new YSetOfMarkings();
                    }
                    ojMarkings.addMarking(currentM);
                    ojMarkingsMap.put(task.getID(), ojMarkings);
                }
                return isOJEnabled;
            }
            case YTask._XOR: {
                for (YExternalNetElement netElement : preSet) {
                    List<YNetElement> eleList = new ArrayList<YNetElement>();
                    eleList.add(netElement);
                    YMarking m = new YMarking(eleList);
                    if (currentM.isBiggerThanOrEqual(m)) {
                        return true;
                    }
                }
                return false;
            }
            default: return false;
        }
    }

    /**
     * This method is used to generate combinations of markings for 
     * comparison. 
     */
    private Set<Set<YNetElement>> generateCombination(
            Set<YExternalNetElement> netElements, int count) {
        Set<Set<YNetElement>> subSets = new HashSet<Set<YNetElement>>();
        List<YNetElement> elementList = new ArrayList<YNetElement>(netElements);
        CombinationGenerator generator = new CombinationGenerator(elementList.size(), count);
        while (generator.hasMore()) {
            Set<YNetElement> combSubSet = new HashSet<YNetElement>();
            int[] indices = generator.getNext();
            for (int index : indices) {
                combSubSet.add(elementList.get(index));
            }
            subSets.add(combSubSet);
        }
        return subSets;
    }

    /**
     * used for formatting xml messages.
     * Message could be a warning or observation.
     */
    private String formatXMLMessage(String msg, boolean isObservation) {
        return StringUtil.wrap(msg, isObservation ? "observation" : "warning");
    }


    //A convenience method to convert a marking that we have into an identifier object.
    private YIdentifier convertMarkingToIdentifier(YMarking m) {
        YIdentifier id = new YIdentifier("temp");
        try {
            for (YNetElement netElement : m.getLocations()) {
                if (netElement instanceof YTask) {
                    id.addLocation(null, (YTask) netElement);
                }
                else if (netElement instanceof YCondition) {
                    id.addLocation(null, netElement);
                }
            }
        }
        catch(YPersistenceException e) {
            //do nothing;
        }
        return id;
    }


    private String printMarking(YMarking m) {
        String printM = "";
        for (YNetElement e : m.getLocations()) {
            printM += e.getID() + "+";
        }
        return printM.substring(0, printM.length()-1); // remove the last +
    }

    /**
     * This method is used to transform a YAWL net by splitting
     * tasks into two and adding a condition between them.
     *
     */
    private YNet transformNet(YNet net) {

        //for all tasks - split into two
        for (YExternalNetElement netElement : net.getNetElements().values()) {
            if (netElement instanceof YTask) {
                YTask task = (YTask) netElement;

                 //introduce a condition in between
                //change join behaviour and preset of t_start
                YTask t_start = new YAtomicTask(task.getID() + "_start",
                        task.getJoinType(), YTask._AND, net);
                YCondition condition = new YCondition("c_" + task.getID(),
                        "c" + task.getID(), net);
                task.setJoinType(YTask._XOR);
                for (YExternalNetElement preElem :  task.getPresetElements()) {
                    t_start.addPreset(new YFlow(preElem, t_start));
                    task.removePresetFlow(new YFlow(preElem, task));
                }
                t_start.addPostset(new YFlow(t_start, condition));
                task.addPreset(new YFlow(condition, task));
                net.addNetElement(t_start);
                net.addNetElement(condition);

                Set<YExternalNetElement> cancelledBySet = task.getCancelledBySet();
                if (! cancelledBySet.isEmpty()) {
                    for (YExternalNetElement cancelElem : cancelledBySet) {
                        YTask cancelTask = (YTask) cancelElem;
                        cancelTask.removeFromRemoveSet(task);
                        List<YExternalNetElement> conditionList =
                                new ArrayList<YExternalNetElement>();
                        conditionList.add(condition);
                        cancelTask.addRemovesTokensFrom(conditionList);
                    }
                }
            }
        }
        return net;
    }


    public static Set<YExternalNetElement> convertToYawlMappings(YExternalNetElement e){
        Set<YExternalNetElement> mappings = new HashSet<YExternalNetElement>(e.getYawlMappings());
        for (YExternalNetElement innerEle : mappings) {
            mappings.addAll(innerEle.getYawlMappings());
        }
        return mappings;
    }

    public static Set<YCondition> convertToYawlMappingsForConditions(YExternalNetElement e) {
        Set<YCondition> condMappings = new HashSet<YCondition>();
        for (YExternalNetElement netElement : convertToYawlMappings(e)){
            if (netElement instanceof YCondition) condMappings.add((YCondition) netElement);
        }
        return condMappings;
    }


    private static Set<YTask> convertToYawlMappingsForTasks(YExternalNetElement e) {
        Set<YTask> taskMappings = new HashSet<YTask>();
        for (YExternalNetElement netElement : convertToYawlMappings(e)) {
            if (netElement instanceof YTask) taskMappings.add((YTask) netElement);
        }
        return taskMappings;
    }

    private YExternalNetElement findYawlMapping(String id) {
        YExternalNetElement element = _yNet.getNetElement(id);
        if (element == null) {
            for (YExternalNetElement mapped : _yNet.getNetElements().values()) {
                for (YExternalNetElement inner : mapped.getYawlMappings()) {
                    if (inner.getID().equals(id)) {
                        return mapped;
                    }
                }
            }
        }
        return element;
    }


}	 	
