/*
 * Created on 16/02/2006
 * YAWLEditor v1.4 
 *
 * @author Moe Thandar Wynn
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package org.yawlfoundation.yawl.analyser.elements;

import org.yawlfoundation.yawl.analyser.util.CombinationGenerator;
import org.yawlfoundation.yawl.analyser.util.YAWLReachabilityUtils;
import org.yawlfoundation.yawl.analyser.util.YAWLResetAnalyser;
import org.yawlfoundation.yawl.elements.*;
import org.yawlfoundation.yawl.elements.state.YMarking;
import org.yawlfoundation.yawl.util.StringUtil;

import java.util.*;

/**
 *  A Reset net formalisation of a YAWL net.
 *
 **/
public final class ResetWFNet {

    private Map<String, RTransition> _transitions = new HashMap<String, RTransition>();
    private Map<String, RPlace> _places = new HashMap<String, RPlace>();
    private Map<String, YTask> _YawlOrJoins = new HashMap<String, YTask>();
    private Set<YCondition> _conditions = new HashSet<YCondition>();
    private Set<YTask> _tasks = new HashSet<YTask>();
    private RPlace _inputPlace;
    private RPlace _outputPlace;
    private String _id;
    private YAWLResetAnalyser _parent;

    private int MAX_MARKINGS = 5000;
    private final String _baseMsg;

    /**
     * Constructor for Reset net.
     *
     */
    public ResetWFNet(YNet yNet) {
        _id = yNet.getID();
        _baseMsg = "The net " + _id;
        convertToResetNet(yNet.getNetElements());
    }

    //an alternative to cloning
    public ResetWFNet(ResetWFNet rNet) {
        _transitions = new HashMap<String, RTransition>(rNet._transitions);
        _places = new HashMap<String, RPlace>(rNet._places);
        _YawlOrJoins = new HashMap<String, YTask>(rNet._YawlOrJoins);
        _id = rNet.getID();
        _baseMsg = "The net " + _id;
        _inputPlace = _places.get(rNet.getInputPlace().getID());
        _outputPlace = _places.get(rNet.getOutputPlace().getID());
        _conditions = new HashSet<YCondition>(rNet._conditions);
        _tasks = new HashSet<YTask>(rNet._tasks);
    }

    public String getID() {
        return _id;
    }

    public void setParent(YAWLResetAnalyser parent) {
        _parent = parent;
    }

    public Map<String, RElement> getNetElements() {
        Map<String, RElement> allElements = new HashMap<String, RElement>();
        allElements.putAll(_transitions);
        allElements.putAll(_places);
        return allElements;
    }

    public static Set<RElement> getPostset(Set<RElement> elements) {
        Set<RElement> postset = new HashSet<RElement>();
        for (RElement re : elements) {
            postset.addAll(re.getPostsetElements());
        }
        return postset;
    }


    public static Set<RElement> getPreset(Set<RElement> elements) {
        Set<RElement> preset = new HashSet<RElement>();
        for (RElement re : elements) {
            preset.addAll(re.getPresetElements());
        }
        return preset;
    }

    public RPlace getInputPlace() {
        return _inputPlace;
    }

    public RPlace getOutputPlace() {
        return _outputPlace;
    }

    public void setMaxMarkings(int maxMarkings) { MAX_MARKINGS = maxMarkings; }

    public void removeNetElement(RElement netElement) {
        for (RElement element : netElement.getPresetElements()) {
            element.removePostsetFlow(new RFlow(element, netElement));
        }
        for (RElement element : netElement.getPostsetElements()) {
            element.removePresetFlow(new RFlow(netElement, element));
        }

        //Need to remove from removeSet and cancelledBySet as well.
        if (netElement instanceof RTransition) {
            _transitions.remove(netElement.getID());
            RTransition transition = (RTransition) netElement;
            for (RElement element : transition.getRemoveSet()) {
                element.removeFromCancelledBySet(transition);
            }
        }
        else {
            _places.remove(netElement.getID());

            //Check if a place is part of any cancellation sets
            for (RTransition transition : netElement.getCancelledBySet()) {
                    transition.removeFromRemoveSet((RPlace) netElement);
            }
        }
    }

    private RPlace newPlace(String id, YExternalNetElement netElement) {
        RPlace place = new RPlace(id);
        addMappings(place, netElement);
        _places.put(place.getID(), place);
        return place;
    }

    private RTransition newTransition(String id, YTask task) {
        RTransition transition = new RTransition(id);
        addMappings(transition, task);
        _transitions.put(transition.getID(), transition);
        return transition;
    }

    private void addMappings(RElement element, YExternalNetElement netElement) {
        element.addToYawlMappings(netElement);
        element.addToYawlMappings(YAWLReachabilityUtils.convertToYawlMappings(netElement));
        element.addToResetMappings(element);
    }

    private void setFlowsForTransition(RTransition transition, String preID, String postID) {
        setInflowForTransition(preID, transition);
        setOutflowForTransition(postID, transition);
    }

    private void setInflowForTransition(String id, RTransition transition) {
        transition.setPreset(new RFlow(_places.get(id), transition));
    }

    private void setOutflowForTransition(String id, RTransition transition) {
        transition.setPostset(new RFlow(transition, _places.get(id)));
    }


    private void setRemoveSetForTransition(YTask task, RTransition transition) {
        Set<YExternalNetElement> removeSet = task.getRemoveSet();
        if (removeSet != null) addCancelSet(transition, removeSet);
    }

    /**
     * The method converts a YAWL net into a Reset net.
     * If there are OR-joins, they are converted to XORs
     */
    private void convertToResetNet(Map<String, YExternalNetElement> netElements) {
        for (YExternalNetElement netElement : netElements.values()) {
            if (netElement instanceof YCondition) {
                newPlace(netElement.getID(), netElement);
                _conditions.add((YCondition) netElement);

                if (netElement instanceof YInputCondition) {
                    _inputPlace = _places.get(netElement.getID());
                }
                else if (netElement instanceof YOutputCondition) {
                    _outputPlace = _places.get(netElement.getID());
                }
            }
            else if (netElement instanceof YTask) {
                newPlace("p_" + netElement.getID(), netElement);
                _tasks.add((YTask) netElement);
            }
        }

        for (YTask task : _tasks) {
            if (task.getJoinType() == YTask._AND) {
                RTransition transition = newTransition(task.getID() + "_start", task);
                for (YExternalNetElement preElement : task.getPresetElements()) {
                    setFlowsForTransition(transition, preElement.getID(), "p_" + task.getID());
                }
            }
            else  {
                for (YExternalNetElement preElement : task.getPresetElements()) {
                    RTransition transition = newTransition(task.getID() +
                            "_start^" + preElement.getID(), task);
                    setFlowsForTransition(transition, preElement.getID(), "p_" + task.getID());
                    if (task.getJoinType() == YTask._OR) {
                        _YawlOrJoins.put(task.getID(), task);
                    }
                }
            }

            if (task.getSplitType() == YTask._AND) {
                RTransition transition = newTransition(task.getID() + "_end", task);
                for (YExternalNetElement postElement : task.getPostsetElements()) {
                    setFlowsForTransition(transition, "p_" + task.getID(), postElement.getID());
                }
                setRemoveSetForTransition(task, transition);
            }
            else if (task.getSplitType() == YTask._XOR) {
                for (YExternalNetElement postElement : task.getPostsetElements()) {
                    RTransition transition = newTransition(task.getID() +"_end^" +
                            postElement.getID(), task);
                    setFlowsForTransition(transition, "p_" + task.getID(), postElement.getID());
                    setRemoveSetForTransition(task, transition);
                }
            }
            else if (task.getSplitType() == YTask._OR) {
                for (Set<YExternalNetElement> subSet : generateCombinations(
                                        task.getPostsetElements())) {
                    String t_id = "";
                    for (YExternalNetElement postElement : subSet) {
                        t_id += postElement.getID() + " ";
                    }

                    RTransition transition = newTransition(task.getID() + "_end^{"
                            + t_id + "}", task);
                    setInflowForTransition("p_" + task.getID(), transition);
                    for (YExternalNetElement postElement : subSet) {
                        setOutflowForTransition(postElement.getID(), transition);
                    }

                    setRemoveSetForTransition(task, transition);
                }
            }
        }
    }

    private Set<Set<YExternalNetElement>> generateCombinations(
            Set<YExternalNetElement> elementSet) {
        Set<Set<YExternalNetElement>> subSets = new HashSet<Set<YExternalNetElement>>();
        for (int i=1; i <= elementSet.size(); i++) {
            Set<Set<YExternalNetElement>> subSet = generateCombination(elementSet, i);
            subSets.addAll(subSet);
        }
        return subSets;
    }

    /**
     * This method is used to generate combinations of markings for 
     * comparison. 
     */
    private Set<Set<YExternalNetElement>> generateCombination(
            Set<YExternalNetElement> netElements, int count) {
        Set<Set<YExternalNetElement>> subSets = new HashSet<Set<YExternalNetElement>>();
        List<YExternalNetElement> elementList = new ArrayList<YExternalNetElement>(netElements);
        CombinationGenerator generator = new CombinationGenerator(elementList.size(), count);
        while (generator.hasMore()) {
            Set<YExternalNetElement> combSubSet = new HashSet<YExternalNetElement>();
            int[] indices = generator.getNext();
            for (int index : indices) {
                combSubSet.add(elementList.get(index));
            }
            subSets.add(combSubSet);
        }
        return subSets;
    }

    /**
     *This method is used to associate a cancellation set (RPlaces) with each RTransition.
     *This is the implementation of R.
     */
    private void addCancelSet(RTransition transition, Set<YExternalNetElement> removeSet) {
        Set<RElement> removePlaces = new HashSet<RElement>();
        RPlace place = null;

        for (YExternalNetElement element : removeSet) {
            String id = element.getID();
            if (element instanceof YCondition) {
                place = _places.get(id);
            }
            else if (element instanceof YTask) {
                place = _places.get("p_" + id);
            }
            if (place != null) removePlaces.add(place);
        }
        transition.setRemoveSet(removePlaces);
    }


    public String checkWeakSoundness() {
        String msg;

        String optionMsg;
        boolean optionToComplete = checkOptionToComplete();
        if (optionToComplete) {
            if (containsORjoins()) { //cannot decide option to complete.
                optionMsg = _baseMsg + " has one or more OR-join tasks and option to complete " +
                        "cannot be decided.";
            }
            else optionMsg = _baseMsg + " has an option to complete.";
        }
        else {
            optionMsg = _baseMsg + " does not have an option to complete.";
            optionToComplete = false;
        }
        msg = formatXMLMessage(optionMsg, optionToComplete);

        String deadTaskMsg = checkDeadTasks();
        boolean noDeadTasks = true;
        if (deadTaskMsg.equals("")) {
            if (containsORjoins()) { //cannot decide no dead tasks.
                deadTaskMsg = _baseMsg + " has one or more OR-join tasks and whether " +
                        "there are dead tasks cannot be decided.";
            }
            else deadTaskMsg =  _baseMsg + " has no dead tasks.";
        }
        else {
            noDeadTasks = false;
            deadTaskMsg = _baseMsg +" has dead tasks:" + deadTaskMsg;
        }
        msg += formatXMLMessage(deadTaskMsg,noDeadTasks);

        boolean properCompletion = true;
        if (noDeadTasks) {
            String properCompletionMsg = checkProperCompletion();
            if (properCompletionMsg.equals("")) {
                properCompletionMsg = _baseMsg + " has proper completion.";
            }
            else {
                if (containsORjoins()) { //cannot decide proper completion.
                    properCompletionMsg = _baseMsg + " has one or more OR-join tasks and " +
                            "proper completion cannot be decided.";
                }
                else {
                    properCompletionMsg = "Tokens could be left in the following " +
                            "condition(s) when the net has completed: " + properCompletionMsg;
                    properCompletion = false;
                }

            }
            msg += formatXMLMessage(properCompletionMsg,properCompletion);
        }

        String soundnessMsg;
        boolean isWeakSound = true;
        if (containsORjoins()) {
            soundnessMsg = _baseMsg + " has one or more OR-join tasks and the weak " +
                    "soundness property cannot be decided.";
        }
        else {
            if (optionToComplete && properCompletion && noDeadTasks) {
                soundnessMsg = _baseMsg + " satisfies the weak soundness property.";

            }
            else { soundnessMsg = _baseMsg + " does not satisfy the weak soundness property.";
                isWeakSound = false;
            }
        }
        msg += formatXMLMessage(soundnessMsg,isWeakSound);

        return msg;
    }

    /**
     *  To check if a marking with one token in output condition is covered from
     *  initial marking.
     *
     */

    private boolean checkOptionToComplete(){
        boolean canComplete = false;
        if (! (_inputPlace == null || _outputPlace != null)) {
            Integer tokenCount = 1;
            Map<String, Integer> iMap = new HashMap<String, Integer>();
            Map<String, Integer> oMap = new HashMap<String, Integer>();
            iMap.put(_inputPlace.getID(), tokenCount);
            oMap.put(_outputPlace.getID(), tokenCount);
            RMarking Mi = new RMarking(iMap);
            RMarking Mo = new RMarking(oMap);
            canComplete = isCoverable(Mi, Mo);
        }
        return canComplete;
    }

    /**
     *  To check if there are dead tasks in the net.
     *  Returns a list of dead tasks or an empty string.
     */

    private String checkDeadTasks() {
        Integer tokenCount = 1;
        Map<String, Integer> iMap = new HashMap<String, Integer>();
        Map<String, Integer> pMap = new HashMap<String, Integer>();
        Set<YTask> deadTasks = new HashSet<YTask>();
        RMarking Mi = new RMarking(iMap);
        String msg= "";
        boolean fireableTask = true;
        iMap.put(_inputPlace.getID(), tokenCount);

        for (YTask task : _tasks) {
            RElement element = findResetMapping("p_"+ task.getID());
            if (element != null) {
                if (element instanceof RPlace) {
                    RPlace p = (RPlace) element;
                    pMap.put(p.getID(), tokenCount);
                    RMarking Mp = new RMarking(pMap);
                    fireableTask = isCoverable(Mi, Mp);
                    pMap.remove(p.getID());
                }
                else if (element instanceof RTransition) {
                    for (RElement preElement : element.getPresetElements()) {
                        pMap.put(preElement.getID(), tokenCount);
                    }
                    RMarking Mp = new RMarking(pMap);
                    fireableTask = isCoverable(Mi, Mp);
                    pMap.clear();
                }
                if (! fireableTask) {
                    deadTasks.addAll(convertToYawlMappingsForTasks(element));
                }
            }
        }
        if (deadTasks.size() > 0) msg = deadTasks.toString();
        return msg;
    }


    public boolean containsORjoins() {
        return ! _YawlOrJoins.isEmpty();
    }


    /**
     *  To check if it is possible to have a token in the net, while there is
     *  a token in the output condition. (Note: this alone cannot determine
     *  whether the net will complete or not).
     *
     */
    private String checkProperCompletion() {
        Map<String, Integer> iMap = new HashMap<String, Integer>();
        Map<String, Integer> pMap = new HashMap<String, Integer>();
        iMap.put(_inputPlace.getID(), 1);
        pMap.put(_outputPlace.getID(), 1);
        RMarking Mi = new RMarking(iMap);
        String msg= "";

        // code using one mapping
        // changed to use all Places to fix the discriminator problem
        for (RPlace p : _places.values()) {
            if (! (p.equals(_inputPlace) || p.equals(_outputPlace))) {
                pMap.put(p.getID(), 1);
                RMarking Mp = new RMarking(pMap);
                if (isCoverable(Mi, Mp)) {
                    msg += convertToYawlMappings(p).toString();
                }
                pMap.remove(p.getID());
            }
        }
        return msg;
    }


    /**
     *  To check if the cancellation sets are unnecessary.
     *
     */
    public String checkCancellationSets() {
        Integer tokenCount = 1;
        Map<String, Integer> iMap = new HashMap<String, Integer>();
        Map<String, Integer> pMap = new HashMap<String, Integer>();
        iMap.put(_inputPlace.getID(), tokenCount);
        RMarking Mi = new RMarking(iMap);
        List<String> msgList = new ArrayList<String>();
        String msg= "";

        //Check cancellation set.
        for ( RTransition t :_transitions.values()) {
            if (t.isCancelTransition()) {
                Set<RElement> preSet = t.getPresetElements();
                pMap.clear();
                for (RElement element : preSet) {
                    pMap.put(element.getID(), tokenCount);
                }

                //it is possible that input and reset places can overlap
                //now that we can be dealing with reduced nets so we need to add them.
                for (RElement element : t.getRemoveSet()) {
                    if (preSet.contains(element)) {
                        tokenCount = 2;
                    }
                    pMap.put(element.getID(), tokenCount);
                    RMarking Mp = new RMarking(pMap);
                    if (! isCoverable(Mi, Mp)) {
                        msgList.add("Element(s)" + convertToYawlMappings(element) +
                                " should not be in the cancellation set of task(s) " +
                                convertToYawlMappingsForTasks(t) + ".");
                    }

                    if (preSet.contains(element)) {
                        tokenCount = 1;
                        pMap.put(element.getID(), tokenCount);
                    }
                    else {
                        pMap.remove(element.getID());
                    }
                } //endfor
            } //endif
        } //endfor

        if (msgList.isEmpty()) {
            msg = _baseMsg + " satisfies the irreducible cancellation regions property.";
            msg = formatXMLMessage(msg, true);
        }
        else {
            for (String rawmessage : msgList) {
                msg += formatXMLMessage(rawmessage, false);
            }
        }
        return msg;
    }


    //********************** START - isReachable methods ***************************//

    public RMarking convertToRMarking(YMarking M) {
        Set<YTask> markedTasks = new HashSet<YTask>();
        Map<String, Integer> RMap = new HashMap<String, Integer>();

        //Need to convert from YAWL to ResetNet
        for (YNetElement netElement : M.getLocations()) {
            if (netElement instanceof YCondition) {
                RPlace place = _places.get(netElement.getID());
                if (place != null) {
                    String placename = place.getID();
                    Integer tokenCount = 1;
                    if (RMap.containsKey(placename)) {
                        tokenCount = RMap.get(placename) + 1;
                    }
                    RMap.put(placename, tokenCount);
                }

            }
            else if (netElement instanceof YTask) {
                markedTasks.add((YTask) netElement);
            }

        }

        // Convert the active tasks in the marking into appropriate places
        for (YTask task : markedTasks) {
            RPlace place = _places.get("p_"+ task.getID());
            if (place != null) {
                RMap.put(place.getID(), 1);
            }
        }
        return new RMarking(RMap);
    }

    /**
     * This method uses reachable markings to check soundness property.
     * Only applicable to YAWL nets without OR-joins
     *
     */
    public String checkSoundness() {
        if (containsORjoins()) {
            return "This net has OR-joins. Soundness check cannot be performed.";
        }

        RSetOfMarkings endMarkings = new RSetOfMarkings();
        String msg = "";
        Integer tokenCount = 1;
        Map<String, Integer> iMap = new HashMap<String, Integer>();
        Map<String, Integer> oMap = new HashMap<String, Integer>();
        iMap.put(_inputPlace.getID(), tokenCount);
        oMap.put(_outputPlace.getID(), tokenCount);
        RMarking Mi = new RMarking(iMap);
        RMarking Mo = new RMarking(oMap);

        boolean optionToComplete = true;
        boolean properCompletion = true;
        boolean noDeadTasks = true;

        RSetOfMarkings reachableMarkings = getReachableMarkings(Mi, endMarkings);

        String optionMsg = "";

        //To check whether exact marking Mo=o is reachable.
        if (endMarkings.contains(Mo) && endMarkings.size() == 1) {
            optionMsg = _baseMsg + " has an option to complete.";
        }
        else {
            optionToComplete = false;
            if (! endMarkings.contains(Mo)) {
                optionMsg = " The final marking Mo is not reachable.";
            }
            optionMsg = _baseMsg + " does not have an option to complete." + optionMsg;
            String deadlockMsg = "";
            for (RMarking currentM : endMarkings.getMarkings()) {
                if (! currentM.isBiggerThanOrEqual(Mo)) {
                    deadlockMsg += printMarking(currentM) + " ";
                }
            }
            if (deadlockMsg.length() > 0) {
                optionMsg = optionMsg + " Potential deadlocks: " + deadlockMsg;
            }
        }
        msg += formatXMLMessage(optionMsg, optionToComplete);
        //Note: Error messages could contain reset markings and not yawl markings

        String properMsg = "";
        for (RMarking currentM : reachableMarkings.getMarkings()) {
            if (currentM.isBiggerThan(Mo)) {
                properMsg += printMarking(currentM)+ " ";
                properCompletion = false;
            }
        }

        if (properMsg.equals("")) {
            properMsg = _baseMsg +" has proper completion.";
        }
        else {
            properMsg = _baseMsg + " does not have proper completion. " +
                    "Markings larger than Mo can be found: " + properMsg;
        }
        msg += formatXMLMessage(properMsg, properCompletion);

        //To check if there are dead tasks
        //Changes made to make this work with reduction rules
        String deadTaskMsg = "";
        Map<String, Integer> cMap = new HashMap<String, Integer>();
        Set<YTask> deadTasks = new HashSet<YTask>();
        for (YTask t : _tasks) {
            RElement e = findResetMapping("p_"+ t.getID());
            if (e != null)
                if (e instanceof RPlace) {
                    cMap.put(e.getID(), tokenCount);
                    RMarking currentM = new RMarking(cMap);
                    if (!reachableMarkings.containsBiggerEqual(currentM)) {
                        deadTasks.addAll(convertToYawlMappingsForTasks(e));
                        noDeadTasks = false;
                    }
                    cMap.remove(e.getID());
                }

                //For reduced net, a place could map to a transition
                //In that case, check to see if a marking that marks
                // all preset places of that transition can be found
                else if (e instanceof RTransition) {
                for (RElement element : e.getPresetElements()) {
                    cMap.put(element.getID(), tokenCount);
                }
                RMarking currentM = new RMarking(cMap);
                if (!reachableMarkings.containsBiggerEqual(currentM)) {
                    deadTasks.addAll(convertToYawlMappingsForTasks(e));
                    noDeadTasks = false;
                }
                cMap.clear();
            }
        }
        if (noDeadTasks) {
            deadTaskMsg = _baseMsg + " has no dead tasks.";
        }
        else {
            deadTaskMsg = _baseMsg + " has dead tasks:" + deadTasks.toString();
        }
        msg += formatXMLMessage(deadTaskMsg, noDeadTasks);

        //To display message regarding soundness property.
        if (optionToComplete && properCompletion && noDeadTasks) {
            msg += formatXMLMessage(_baseMsg + " satisfies the soundness property.",
                    true);
        }
        else {
            msg += formatXMLMessage(_baseMsg + " does not satisfy the soundness property.",
                    false);
        }
        return msg;
    }


    /**
     * This method generates a set of reachable markings for a given net.
     */
    private RSetOfMarkings getReachableMarkings(RMarking M, RSetOfMarkings endMarkings) {
        RSetOfMarkings reachable = new RSetOfMarkings();
        RSetOfMarkings visitingPS = getImmediateSuccessors(M);
        visitingPS.addMarking(M);                                   // add Mi to RS.

        while (! reachable.containsAll(visitingPS.getMarkings())) {
            if (_parent.isCancelled()) return reachable;
            reachable.addAll(visitingPS);
            if (reachable.size() > MAX_MARKINGS) {
                _parent.announceProgressMessage(
                        "Reachable markings exceed limit (" + MAX_MARKINGS +
                        "). Possible infinite loop in the net '" + _id +
                        "'. Unable to complete analysis of this net.");
                return reachable;
            }
            visitingPS = getImmediateSuccessors(visitingPS, endMarkings);
            _parent.announceProgressMessage("Immediate successors: " + visitingPS.size());

        }
        _parent.announceProgressMessage("Reachability set size: " + reachable.size());
        return reachable;
    }

    /**
     * return successor markings from a set of markings.
     *
     *
     */
    private RSetOfMarkings getImmediateSuccessors(RSetOfMarkings markings,
                                                  RSetOfMarkings endMarkings) {
        RSetOfMarkings successors = new RSetOfMarkings();
        for (RMarking currentM : markings.getMarkings()) {
            RSetOfMarkings post = getImmediateSuccessors(currentM);
            if (post.size() > 0) {
                successors.addAll(post);
            }
            else {
                endMarkings.addMarking(currentM);
            }
        }
        return successors;
    }


    /**
     * return successor markings from a marking.
     */
    private RSetOfMarkings getImmediateSuccessors(RMarking currentM) {
        RSetOfMarkings successors = new RSetOfMarkings();
        for (RTransition t: _transitions.values()) {
            if (isForwardEnabled(currentM,t)) {
                successors.addMarking(getNextRMarking(currentM,t));
            }
        }
        return successors;
    }

    /**
     * Get an immediate successor marking from m by firing t.
     */
    private RMarking getNextRMarking(RMarking currentM, RTransition t) {
        Map<String, Integer> postmarkedPlaces =
                new HashMap<String, Integer>(currentM.getMarkedPlaces());
        Set<RElement> preSet = t.getPresetElements();
        Set<RElement> removeSet = t.getRemoveSet();
        preSet.removeAll(removeSet);

        //Remove 1 token from preSet \ R(t) **
        for (RElement netElement : preSet) {
            String netElementName = netElement.getID();
            if (postmarkedPlaces.containsKey(netElementName)) {
                int count = postmarkedPlaces.get(netElementName);
                if (count == 1) {
                    postmarkedPlaces.remove(netElementName);
                }
                else if (count > 1) {
                    postmarkedPlaces.put(netElementName, count -1);
                }
            }
            //nothing to do if postset is not marked
        }

        //Remove tokens from R(t)
        for (RElement netElement : removeSet) {
            String netElementName = netElement.getID();
            if (postmarkedPlaces.containsKey(netElementName)) {
                postmarkedPlaces.remove(netElementName);
            }
        }

        // Must be done in the correct order, remove first then add postSet
        //Add one token to postSet
        int tokenCount = 1;
        for (RElement netElement : t.getPostsetElements()) {
            String netElementName = netElement.getID();
            if (postmarkedPlaces.containsKey(netElementName)) {
                tokenCount = postmarkedPlaces.get(netElementName) + 1;
            }
            postmarkedPlaces.put(netElementName,tokenCount);
        }

        return new RMarking(postmarkedPlaces);
    }


    private boolean isForwardEnabled(RMarking currentM, RTransition t) {
        Map markedPlaces = currentM.getMarkedPlaces();

        // \bullet t >= marked(M)
        for (RElement element : t.getPresetElements()) {
            if (! markedPlaces.containsKey(element.getID())) return false;
        }
        return true;
    }

    // ************************ END - isReachable methods *******************************//


    // ************************ START - isCoverable methods ****************************//


    /**
     * This method takes two markings s and t, and check whether s'<= s is coverable 
     * from the predecessors of t.
     */
    private boolean isCoverable(RMarking s, RMarking t) {
        RSetOfMarkings tSet = new RSetOfMarkings();
        tSet.addMarking(t);
        RSetOfMarkings rm = FiniteBasisPred(tSet, new HashSet<RMarking>());
        for (RMarking x : rm.getMarkings()) {
            if (x.isLessThanOrEqual(s)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This methods returns the FiniteBasis of the Predecessors for a set of 
     * RMarkings.
     */
    private RSetOfMarkings FiniteBasisPred(RSetOfMarkings I, Set<RMarking> alreadyConsidered) {
        RSetOfMarkings K = new RSetOfMarkings();
        RSetOfMarkings Pred = new RSetOfMarkings();
        K.addAll(I);
        Pred.addAll(K);
        RSetOfMarkings Kn = getMinimalCoveringSet(getPredecessors(K, alreadyConsidered), Pred);

        while (! IsUpwardEqual(K,Kn)) {
            K.removeAll();
            K.addAll(Kn);
            Pred.removeAll();
            Pred.addAll(K);
            Kn = getMinimalCoveringSet(getPredecessors(K, alreadyConsidered), Pred);
        }
        return K;
    }


    /**
     * This method checks whether the basis of the two sets of markings are equal.
     * This is the implementation of K = Kn.
     *
     */
    private boolean IsUpwardEqual(RSetOfMarkings K, RSetOfMarkings Kn) {
        return K.equals(Kn);
    }

    /**
     * This method is called with a set of RMarkings to generate a set of predecessors.
     */
    private RSetOfMarkings getPredecessors(RSetOfMarkings markings,
                                           Set<RMarking> alreadyConsidered) {
        RSetOfMarkings predecessors = new RSetOfMarkings();
        for (RMarking M : markings.getMarkings()) {
            predecessors.addAll(getPredecessors(M, alreadyConsidered));
        }
        return getMinimalCoveringSet(predecessors);
    }

    /**
     * This method is called with a RMarking to generate a set of predecessors of this
     * marking. The method returns a finite basis for the predecessors set.(optimisation)
     */
    private RSetOfMarkings getPredecessors(RMarking M, Set<RMarking> alreadyConsidered) {
        RSetOfMarkings predecessors = new RSetOfMarkings();

        // For optimisation purpose, we keep track of which marking has been
        // considered before with getPredecessors(M).
        if (! alreadyConsidered.contains(M)) {
            for (RTransition t : _transitions.values()) {
                if (isBackwardsEnabled(M,t)) {
                    RMarking preM = getPreviousRMarking(M,t);
                    if (! preM.isBiggerThanOrEqual(M)) {
                        predecessors.addMarking(preM); // isCoverable check
                    }
                }
            }
            alreadyConsidered.add(M);
        }
        return predecessors;

    }

    /**
     * This method determines whether a transition should be backwards
     * enabled at a given RMarking. Currently, a transition
     * is not backwards enabled only if there are more tokens in the remove set
     * than tokens in the postset.
     *
     */
    private boolean isBackwardsEnabled(RMarking currentM, RTransition t) {
        Set<RElement> removeSet = t.getRemoveSet();

        // M[R(t)] <= t\bullet[R(t)]
        if (removeSet.size() > 0) {
            Set<RElement> postSet = t.getPostsetElements();
            Map<String, Integer> markedPlaces = currentM.getMarkedPlaces();
            for (RElement element : removeSet) {
                String placeName = element.getID();

                // reset place is marked and reset place is also a postset
                if (markedPlaces.containsKey(placeName) && postSet.contains(element)) {

                    // If the number of tokens in marked reset place > the postset
                    if (markedPlaces.get(placeName) > 1) return false;
                }

                // reset place is marked but it is not in the postset so should not fire.
                else return false;
            }
        }
        return true;
    }

    private RMarking getPreviousRMarking(RMarking currentM, RTransition t) {

        Map<String, Integer> premarkedPlaces  = currentM.getMarkedPlaces();
        Set<RElement> postSet = t.getPostsetElements();
        Set<RElement> preSet = t.getPresetElements();
        Set<RElement> removeSet = t.getRemoveSet();

        // Remove the marked postSet elements from marking
        // We need to make sure that only one token is removed and not all tokens.

        // Remove 1 token from postSet only if there are tokens in postSet
        postSet.removeAll(removeSet);
        for (RElement netElement : postSet) {
            String netElementName = netElement.getID();
            if (premarkedPlaces.containsKey(netElementName)) {
                int count = premarkedPlaces.get(netElementName);
                if (count == 1) {
                    premarkedPlaces.remove(netElementName);
                }
                else if (count > 1) {
                    premarkedPlaces.put(netElementName, count - 1);
                }
            }
            // nothing to do if postset is not marked
        }

        // Add one token to preSet
        preSet.removeAll(removeSet);
        int tokenCount = 1;
        for (RElement netElement : preSet) {
            String netElementName = netElement.getID();
            if (premarkedPlaces.containsKey(netElementName)) {
                tokenCount = premarkedPlaces.get(netElementName) + 1;
            }
            premarkedPlaces.put(netElementName, tokenCount);
        }

        // Add one token to R(t) if it is an input place
        // F(p,t) if p in R(t)
        removeSet.retainAll(t.getPresetElements());
        for (RElement netElement : removeSet) {
            premarkedPlaces.put(netElement.getID(), 1);
        }

        return new RMarking(premarkedPlaces);
    }


    /**
     * This method is used to generate the minimal covering set 
     * for a given set of Markings.
     */
    private RSetOfMarkings getMinimalCoveringSet(RSetOfMarkings Z) {
        RSetOfMarkings minimalSet = new RSetOfMarkings();
        minimalSet.addAll(Z);

        for (RMarking M : Z.getMarkings()) {
            RSetOfMarkings Z_inner = new RSetOfMarkings();
            Z_inner.addAll(minimalSet);
            Z_inner.removeMarking(M);
            for (RMarking M_i : Z_inner.getMarkings()) {
                if (M.isBiggerThanOrEqual(M_i)) minimalSet.removeMarking(M);
            }
        }
        return minimalSet;
    }


    private RSetOfMarkings getMinimalCoveringSet(RSetOfMarkings pbZ, RSetOfMarkings Z) {
        RSetOfMarkings minimalSet = new RSetOfMarkings();
        minimalSet.addAll(Z);
        minimalSet.addAll(pbZ);

        for (RMarking M : pbZ.getMarkings()) {
            RSetOfMarkings Z_inner = new RSetOfMarkings();
            Z_inner.addAll(minimalSet);
            Z_inner.removeMarking(M);
            for (RMarking M_i : Z_inner.getMarkings()) {
                if (M.isBiggerThanOrEqual(M_i)) {
                    minimalSet.removeMarking(M);
                }
                else if (M_i.isBiggerThanOrEqual(M)) {
                    minimalSet.removeMarking(M_i);
                }
            }
        }
        return minimalSet;
    }

    // ************************ START - isCoverable methods ****************************//

    /**
     * used for formatting xml messages.
     * Message could be a warning or observation.
     */
    private String formatXMLMessage(String msg, boolean isObservation) {
        return StringUtil.wrap(msg, isObservation ? "observation" : "warning");
    }


    private String printMarking(RMarking marking) {

        String printStr = "";
        Map<String, Integer> mPlaces = marking.getMarkedPlaces();
        for (String location : mPlaces.keySet()) {
            printStr += mPlaces.get(location) + location + "+";
        }
        //To remove the last +
        return printStr.substring(0, printStr.length()-1);
    }


    /**
     * This method is used for error messages mapping from reset
     * to yawl. If there are yawl mappings, the message returns them, otherwise
     * it returns an empty set.
     */
    public static Set<YExternalNetElement> convertToYawlMappings(RElement element){
        Set<YExternalNetElement> yawlMappings = element.getYawlMappings();
        for (RElement innerElement : element.getResetMappings()) {
            yawlMappings.addAll(innerElement.getYawlMappings());
        }
        return yawlMappings;
    }

    public static Set<YCondition> convertToYawlMappingsForConditions(RElement e){
        Set<YCondition> conditionMappings = new HashSet<YCondition>();
        for (YExternalNetElement netElement : convertToYawlMappings(e)) {
            if (netElement instanceof YCondition) {
                conditionMappings.add((YCondition) netElement);
            }
        }
        return conditionMappings;
    }


    public static Set<YTask> convertToYawlMappingsForTasks(RElement e){
        Set<YTask> taskMappings = new HashSet<YTask>();
        for (YExternalNetElement netElement : convertToYawlMappings(e)) {
            if (netElement instanceof YTask) {
                taskMappings.add((YTask) netElement);
            }
        }
        return taskMappings;
    }


    /**
     * @param id - id of an RElement to retrieve from reset net
     * it returns the element or null
     * Changes made to make it work with reduced nets
     */
    private RElement findResetMapping(String id) {

        Map<String, RElement> allElements = new HashMap<String, RElement>();
        allElements.putAll(_places);
        allElements.putAll(_transitions);
        RElement foundElement = allElements.get(id);

        if (foundElement == null) {

           //might have reset mappings
            for (RElement element : allElements.values()) {
                for (RElement inner : element.getResetMappings()) {
                    if (inner.getID().equals(id)) {
                        return element;
                    }
                }
            }
        }
        return foundElement;
    }

}
   
   
