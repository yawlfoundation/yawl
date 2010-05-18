/**
 * Created by Jingxin XU on 04/02/2010
 *
 */

package org.yawlfoundation.yawl.editor.net;

import org.yawlfoundation.yawl.editor.elements.model.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class PetriNet {

    private NetGraphModel model;

    private InputCondition inputCondition;
    private OutputCondition outputCondition;

    private ArrayList<YAWLFlowRelation> flows = new ArrayList<YAWLFlowRelation>();
    private ArrayList<Condition> conditions = new ArrayList<Condition>();
    private ArrayList<YAWLTask> tasks = new ArrayList<YAWLTask>();

    private ArrayList<Integer> places;
    private ArrayList<String> roles;
    private ArrayList<Transition> transitions;
    private int initialMarking;
    private int finalCondition;

    private HashMap<YAWLCondition, Integer> conditionToPlace = new HashMap<YAWLCondition, Integer>();
    private HashMap<YAWLFlowRelation, Integer> flowToSourcePlace = new HashMap<YAWLFlowRelation, Integer>();
    private HashMap<YAWLFlowRelation, Integer> flowToTargetPlace = new HashMap<YAWLFlowRelation, Integer>();

    private int placeID;
    private String path;

    public PetriNet(NetGraphModel model, String path) {
        places = new  ArrayList<Integer>();
        roles = new ArrayList<String>();
        transitions = new ArrayList<Transition>();
        this.model = model;
        placeID = 0;
        this.path = path;
        parseModel();
        InitializeConditionToPlaceMap();
        InitializeFlowToPlaceMap();
        generateTransitions();
    }

    private void parseModel() {
        Object[] cells = NetGraphModel.getRoots(model);
        for (int i=0; i < cells.length; i++) {
            if (cells[i] instanceof VertexContainer) {
                cells[i] = ((VertexContainer) cells[i]).getVertex();
            }
            if (cells[i] instanceof InputCondition) {
                inputCondition = (InputCondition) cells[i];
            }
            else if (cells[i] instanceof OutputCondition) {
                outputCondition = (OutputCondition) cells[i];
            }
            else if (cells[i] instanceof Condition) {
                conditions.add((Condition) cells[i]);
            }
            else if (cells[i] instanceof YAWLTask) {
                tasks.add((YAWLTask) cells[i]);
            }
            else if (cells[i] instanceof YAWLFlowRelation) {
                flows.add((YAWLFlowRelation) cells[i]);
            }
        }
    }

    private void InitializeConditionToPlaceMap() {
        for (int i=0; i<conditions.size(); i++) {
            places.add(i);
            conditionToPlace.put(conditions.get(i), i);
            placeID++;
        }
        initialMarking = conditions.size();
        places.add(initialMarking);
        conditionToPlace.put(inputCondition, initialMarking);
        placeID++;

        finalCondition = initialMarking + 1;
        places.add(finalCondition);
        conditionToPlace.put(outputCondition, finalCondition);
        placeID++;
    }

    private void InitializeFlowToPlaceMap() {
        for (YAWLFlowRelation flow : flows) {
            if (flow.getSourceVertex() instanceof YAWLCondition) {
                flowToSourcePlace.put(flow, conditionToPlace.get(flow.getSourceVertex()));
                flowToTargetPlace.put(flow, null);
            }
            else if (flow.getTargetVertex() instanceof YAWLCondition){
                flowToTargetPlace.put(flow, conditionToPlace.get(flow.getTargetVertex()));
                flowToSourcePlace.put(flow, null);
            }
            else {
                places.add(placeID);
                flowToSourcePlace.put(flow, placeID);
                flowToTargetPlace.put(flow, placeID);
                placeID++;
            }
        }
    }

    private void generateTransitions() {
        for (YAWLTask task : tasks) {
            if (!task.isConfigurable()) {
                Transition t1 = new Transition();
                for (YAWLFlowRelation flow : task.getIncomingFlows()) {
                    if (flowToSourcePlace.get(flow) != null) {
                        t1.consumePlaces.add(flowToSourcePlace.get(flow));
                    }
                }
                for (YAWLFlowRelation flow : task.getOutgoingFlows()) {
                    if (flowToTargetPlace.get(flow) != null) {
                        t1.producePlaces.add(flowToTargetPlace.get(flow));
                    }
                }
                transitions.add(t1);
            }
            else {
                places.add(placeID);
                for (CPort port : task.getInputCPorts()) {
                    Transition t = new Transition();
                    if (task.getDecomposition() == null) {
                        t.role = task.getEngineId() + "INPUT" + port.getID();
                    }
                    else {
                        t.role = task.getDecomposition().getLabel() + "INPUT" + port.getID();
                    }
                    t.producePlaces.add(placeID);
                    for (YAWLFlowRelation flow : port.getFlows()) {
                        if (flowToSourcePlace.get(flow)!= null) {
                            t.consumePlaces.add(flowToSourcePlace.get(flow));
                        }
                    }
                    transitions.add(t);
                }
                for (CPort port : task.getOutputCPorts()) {
                    Transition t = new Transition();
                    if (task.getDecomposition() == null) {
                        t.role = task.getEngineId() + "OUTPUT" + port.getID();
                    }
                    else {
                        t.role = task.getDecomposition().getLabel() + "OUTPUT" + port.getID();
                    }
                    t.consumePlaces.add(placeID);
                    for (YAWLFlowRelation flow : port.getFlows()) {
                        if (flowToTargetPlace.get(flow)!= null){
                            t.producePlaces.add(flowToTargetPlace.get(flow));
                        }
                    }
                    this.transitions.add(t);
                }
                placeID++;
            }
        }
    }

    private List<String> getRoles() {
        List<String> roles = new ArrayList<String>();
        for (Transition t : transitions) {
            if ((t.role != null) && !roles.contains(t.role)) {
                roles.add(t.role);
            }
        }
        return roles;
    }


    public void saveOWFNfile(){
        try {
            File file = new File(path + "/test.owfn");
            FileWriter out = new FileWriter(file);
            out.write("PLACE\n\n");

            String roles = "ROLES ";
            if (getRoles().isEmpty()) {
                roles = roles + " ;";
            }
            else {
                for (int i=0; i < getRoles().size()-1 ; i++) {
                    roles += getRoles().get(i) + ",";
                }
                roles += getRoles().get(getRoles().size() -1) + ";";
            }
            out.write(roles);
            out.append("\n\n");

            String place = "INTERNAL ";
            for (int i= 0; i < places.size() -1; i++) {
                place += "p" + places.get(i) + ",";
            }
            place += "p" + places.get(places.size() -1) + ";";
            out.write(place);
            out.append("\n\n");

            String initialMarking = "INITIALMARKING ";
            initialMarking += "p" + initialMarking + ";";
            out.write(initialMarking);
            out.append("\n\n");

            String finalCondition = "FINALCONDITION (p" + this.finalCondition + "=1);";
            out.write(finalCondition);
            out.append("\n\n");

            for (int i=0; i < transitions.size(); i++) {
                Transition t = transitions.get(i);
                String transition = "TRANSITION t" + i;
                out.write(transition);
                out.append("\n");
                if (t.role != null) {
                    out.write("ROLES " + t.role + ";");
                    out.append("\n");
                }
                out.write("CONSUME ");
                for (int j=0; j < t.consumePlaces.size()-1; j++) {
                    out.write("p" + t.consumePlaces.get(j) + ",");
                }
                out.write("p"  +t.consumePlaces.get(t.consumePlaces.size() -1) + ";");
                out.append("\n");
                out.write("PRODUCE ");
                for (int j=0; j < t.producePlaces.size()-1; j++) {
                    out.write("p" + t.producePlaces.get(j) + ",");
                }
                out.write("p" + t.producePlaces.get(t.producePlaces.size() -1) + ";");
                out.append("\n\n");
            }

            out.close();
        }
        catch (IOException e) {
            System.out.println("Exception "+e.getMessage()+e.toString());
        }
    }

    public boolean checkValidate(){
        boolean flag = true;
        for (YAWLTask task : tasks) {
            if (task.hasJoinDecorator() &&
                    (task.getJoinDecorator().getType()== Decorator.OR_TYPE)) {
                flag = false;
                break;
            }

            if (task.hasSplitDecorator() &&
                    (task.getSplitDecorator().getType() == Decorator.OR_TYPE)) {
                flag = false;
                break;
            }

            if (task.hasCancellationSetMembers()) {
                flag = false;
                break;
            }
        }
        return flag;
    }

    public ArrayList<YAWLTask> getTasks() {
        return tasks;
    }

    public void setTasks(ArrayList<YAWLTask> tasks) {
        this.tasks = tasks;
    }

    private class Transition {
        public String role;
        public ArrayList<Integer> consumePlaces;
        public ArrayList<Integer> producePlaces;
        public Transition(){
            this.role= null;
            this.consumePlaces = new ArrayList<Integer>();
            this.producePlaces = new ArrayList<Integer>();
        }
    }
    
}
