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

/**
 * Created by Jingxin XU on 04/02/2010
 *
 */

package org.yawlfoundation.yawl.editor.ui.net;

import org.yawlfoundation.yawl.editor.ui.configuration.CPort;
import org.yawlfoundation.yawl.editor.ui.elements.model.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class PetriNet {

    private final NetGraphModel model;

    private InputCondition inputCondition;
    private OutputCondition outputCondition;

    private final ArrayList<YAWLFlowRelation> flows = new ArrayList<YAWLFlowRelation>();
    private final ArrayList<Condition> conditions = new ArrayList<Condition>();
    private ArrayList<YAWLTask> tasks = new ArrayList<YAWLTask>();

    private final ArrayList<Integer> places;
    private final ArrayList<String> roles;
    private final ArrayList<Transition> transitions;
    private int initialMarking;
    private int finalCondition;

    private final HashMap<Condition, Integer> conditionToPlace = new HashMap<Condition, Integer>();
    private final HashMap<YAWLFlowRelation, Integer> flowToSourcePlace = new HashMap<YAWLFlowRelation, Integer>();
    private final HashMap<YAWLFlowRelation, Integer> flowToTargetPlace = new HashMap<YAWLFlowRelation, Integer>();

    private int placeID;
    private final String path;

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
            if (flow.getSourceVertex() instanceof Condition) {
                flowToSourcePlace.put(flow, conditionToPlace.get(flow.getSourceVertex()));
                flowToTargetPlace.put(flow, null);
            }
            else if (flow.getTargetVertex() instanceof Condition) {
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
                        t.role = task.getID() + "INPUT" + port.getID();
                    }
                    else {
                        t.role = task.getDecomposition().getID() + "INPUT" + port.getID();
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
                        t.role = task.getID() + "OUTPUT" + port.getID();
                    }
                    else {
                        t.role = task.getDecomposition().getID() + "OUTPUT" + port.getID();
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
            File file = new File(path + "/temp.owfn");
            FileWriter out = new FileWriter(file);
            out.write("PLACE\n\n");

            StringBuilder roles = new StringBuilder("ROLES ");
            if (getRoles().isEmpty()) {
                roles.append(" ;");
            }
            else {
                for (int i=0; i < getRoles().size()-1 ; i++) {
                    roles.append(getRoles().get(i)).append(",");
                }
                roles.append(getRoles().get(getRoles().size() -1)).append(";");
            }
            out.write(roles.toString());
            out.append("\n\n");

            StringBuilder place = new StringBuilder("INTERNAL ");
            for (int i= 0; i < places.size() -1; i++) {
                place.append("p").append(places.get(i)).append(",");
            }
            place.append("p").append(places.get(places.size() -1)).append(";");
            out.write(place.toString());
            out.append("\n\n");

            String initialMarking = "INITIALMARKING ";
            initialMarking += "p" + this.initialMarking + ";";
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
        public final ArrayList<Integer> consumePlaces;
        public final ArrayList<Integer> producePlaces;
        public Transition(){
            this.role= null;
            this.consumePlaces = new ArrayList<Integer>();
            this.producePlaces = new ArrayList<Integer>();
        }
    }
    
}
