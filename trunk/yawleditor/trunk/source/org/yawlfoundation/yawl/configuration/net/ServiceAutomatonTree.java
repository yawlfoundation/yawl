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

package org.yawlfoundation.yawl.configuration.net;

import org.yawlfoundation.yawl.configuration.CPort;
import org.yawlfoundation.yawl.configuration.ConfigurationSettings;
import org.yawlfoundation.yawl.configuration.ProcessConfigurationModel;
import org.yawlfoundation.yawl.configuration.element.TaskConfiguration;
import org.yawlfoundation.yawl.configuration.element.TaskConfigurationCache;
import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.net.NetGraphModel;
import org.yawlfoundation.yawl.editor.ui.swing.AnalysisDialog;
import org.yawlfoundation.yawl.editor.ui.util.FileUtilities;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ServiceAutomatonTree implements PropertyChangeListener {

    //this map records the position of the node with a specified ID in the array list
    private HashMap<String, Integer> positionMap;
    private ArrayList<Node> nodes;
    private ArrayList<YAWLTask> tasks;
    private String currentState;       // record the Id of the node with current state
    private AnalysisDialog msgDialog;
    private WendyRunner runner;
    private String path;
    private NetGraphModel graphModel;

    public ServiceAutomatonTree(NetGraph net) {
        init();
        String errMsg = null;
        if (isWendyInstalled()) {
            graphModel = net.getNetModel();
            PetriNet petri = new PetriNet(net.getNetModel(), path);
            tasks = petri.getTasks();
            if (petri.checkValidate()) {
                petri.saveOWFNfile();
                msgDialog = new AnalysisDialog("Net", YAWLEditor.getInstance());
                msgDialog.setTitle("Check Configuration Correctness");
                runner = new WendyRunner();
                runner.addPropertyChangeListener(this);
                runner.execute();
            }
            else {
                errMsg = "Can't convert the diagram into a petri net," +
                         "because the net contains the following things: 1, OR-join  " +
                         "2, OR-Split  3, Cancellation Set.";
            }
        }
        else {
            errMsg = "Unable to locate installation of Wendy for checking process " +
                     "configuration correctness.";
        }
        if (errMsg != null) {
            JOptionPane.showMessageDialog(YAWLEditor.getInstance(), errMsg,
                    "Check for Correctness Error", JOptionPane.ERROR_MESSAGE);
        }

        //testGeneratingTree(); TO PRINT DEBUG DATA
    }


    private void init() {
        positionMap = new HashMap<String, Integer>();
        nodes = new ArrayList<Node>();
        tasks = new ArrayList<YAWLTask>();
        currentState = "";
        String savedPath = ConfigurationSettings.getWendyFilePath();
        path = savedPath != null ? savedPath : FileUtilities.getHomeDir() + "wendy";
    }


    private boolean isWendyInstalled() {
        return new File(path + "/wendy.conf").exists();
    }


    private boolean checkResult(String output) {
        return output.contains("net is controllable: YES") ;
    }


    private void generateNodes() {
        try {
            BufferedReader in = new BufferedReader(new FileReader(path + "/temp.sa"));

            String begin = in.readLine();
            while (! begin.equals("NODES")) {
                begin = in.readLine();
            }

            String line = in.readLine();
            while (line != null) {
                if (! line.contains("->")) {
                    Node node = new Node();
                    node.ID = line.contains(":") ? line.substring(2, line.indexOf(":") -1)
                                                 : line.substring(2);
                    if (line.contains("INITIAL")) {
                        node.isInitial = true;
                        currentState = node.ID;
                    }
                    else if (line.contains("FINAL")) {
                        node.isFinal = true;
                    }
                    
                    line = in.readLine();
                    if (line != null) {
                        boolean flag = line.contains("->");
                        while (flag) {
                            int position = line.indexOf("->");
                            Operation blockPort = new Operation();
                            blockPort.myOperation = line.substring(4,position-1);
                            blockPort.myNode = line.substring(position+3);
                            node.sucessors.add(blockPort);
                            line = in.readLine();
                            flag = (line != null) && line.contains("->");
                        }
                    }
                    nodes.add(node);
                }
                else {
                    line = in.readLine();
                }
            }
            in.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        matchIDs();
        generateFormerOperation();
        generateShortestPathPreceder();
        generateShortestPathSucessor();
        ProcessConfigurationModel.getInstance().togglePreviewState();
    }

    private void matchIDs(){
        for (int i=0; i< nodes.size(); i++) {
            positionMap.put(nodes.get(i).ID, i);
        }
    }

    public boolean processCorrectnessCheckingForBlock(YAWLTask task, String type, int portID){
        boolean flag = false;
        Node node = nodes.get(positionMap.get(currentState));
        String blockPort =  "block_" + getTaskLabel(task) + type + portID;
        for (int i=0; i<node.sucessors.size(); i++) {
            if (node.sucessors.get(i).myOperation.equals(blockPort)) {
                flag = true;
                break;
            }
        }
        return flag;
    }


    private String getTaskLabel(YAWLTask task) {
        return (task.getDecomposition() != null) ? task.getDecomposition().getID() :
                task.getID();
    }


    public void changeCurrentStateAfterBlock(YAWLTask task, String type, int portID){
        Node node = nodes.get(positionMap.get(currentState));
        String blockPort =  "block_" + getTaskLabel(task) + type + portID;
        for (int i=0; i<node.sucessors.size(); i++) {
            if (node.sucessors.get(i).myOperation.equals(blockPort)) {
                currentState = node.sucessors.get(i).myNode;
                break;
            }
        }
        automaticallyBlockOthers();
    }


    public boolean processCorrectnessCheckingForActivate(YAWLTask task, String type, int portID){
        boolean flag = false;
        Node node = nodes.get(positionMap.get(currentState));
        String blockPort =  "block_" + getTaskLabel(task) + type + portID;
        for(int i=0; i<node.preceders.size(); i++){
            if (node.preceders.get(i).myOperation.equals(blockPort)) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    public void changeCurrentStateAfterActivate(YAWLTask task, String type, int portID){
        Node node = nodes.get(positionMap.get(currentState));
        String blockPort =  "block_" + getTaskLabel(task) + type + portID;
        for (int i=0; i<node.preceders.size(); i++) {
            if (node.preceders.get(i).myOperation.equals(blockPort)) {
                currentState = node.preceders.get(i).myNode;
                break;
            }
        }
        automaticallyActivateOthers();
    }


    private void automaticallyBlockOthers() {
        String message = "";
        Node node = nodes.get(positionMap.get(currentState));
        while (node.shortestPathSucessor > 0) {
            message = message + blockAPort(node.preferSucessor.myOperation) +"\n";
            currentState = node.preferSucessor.myNode;
            node = nodes.get(positionMap.get(currentState));
        }
        if (message.length() > 0) {
            JOptionPane.showMessageDialog(YAWLEditor.getInstance(), message);
        }
    }


    private void automaticallyActivateOthers() {
        String message = "";
        Node node = nodes.get(positionMap.get(currentState));
        while (node.shortestPathPreceder > 0) {
            message = message + activateAPort(node.preferPreceder.myOperation)+"\n";
            currentState = node.preferPreceder.myNode;
            node = nodes.get(positionMap.get(currentState));
        }
        if (message.length() > 0) {
            JOptionPane.showMessageDialog(YAWLEditor.getInstance(), message);
        }
    }

    private String blockAPort(String operations) {
        String message = "";
        if (operations.contains("INPUT")) {
            String taskID = operations.substring(operations.indexOf("block_") +6,
                    operations.indexOf("INPUT"));
            int portID = Integer.parseInt(operations.substring(operations.indexOf("INPUT")+5));
            TaskConfiguration config = getTheTask(taskID);
            if (config != null) {
                config.getInputCPorts().get(portID).setConfigurationSetting(CPort.BLOCKED);
            }
            message = "The Task "+ taskID +"'s input port "+portID+" has been automatically blocked";
        }
        else if (operations.contains("OUTPUT")){
            String taskID = operations.substring(operations.indexOf("block_")+6,
                    operations.indexOf("OUTPUT"));
            int portID = Integer.parseInt(operations.substring(operations.indexOf("OUTPUT")+6));
            TaskConfiguration config = getTheTask(taskID);
            if (config != null) {
                config.getOutputCPorts().get(portID).setConfigurationSetting(CPort.BLOCKED);
            }
            message = "The Task "+ taskID +"'s output port "+portID+" has been automatically blocked";
        }
        return message;
    }

    private String activateAPort(String operations) {
        String message = "";
        if (operations.contains("INPUT")) {
            String taskID = operations.substring(operations.indexOf("block_")+6,
                    operations.indexOf("INPUT"));
            int portID = Integer.parseInt(operations.substring(operations.indexOf("INPUT")+5));
            TaskConfiguration config = getTheTask(taskID);
            if (config != null) {
                config.getInputCPorts().get(portID).setConfigurationSetting(CPort.ACTIVATED);
            }
            message = "The Task "+ taskID +"'s input port "+portID+" has been automatically activated";
        }
        else if (operations.contains("OUTPUT")) {
            String taskID = operations.substring(operations.indexOf("block_")+6,
                    operations.indexOf("OUTPUT"));
            int portID = Integer.parseInt(operations.substring(operations.indexOf("OUTPUT")+6));
            TaskConfiguration config = getTheTask(taskID);
            if (config != null) {
                config.getOutputCPorts().get(portID).setConfigurationSetting(CPort.ACTIVATED);
            }
            message = "The Task "+ taskID +"'s output port "+portID+" has been automatically activated";
        }
        return message;
    }

    private TaskConfiguration getTheTask(String taskID){
        for (YAWLTask task : tasks) {
            if (task.getDecomposition() == null) {
                if (task.getID().equals(taskID)) {
                    return TaskConfigurationCache.getInstance().get(graphModel, task);
                }
            }
            else {
                if (task.getDecomposition().getID().equals(taskID)){
                    return TaskConfigurationCache.getInstance().get(graphModel, task);
                }
            }
        }
        return null;
    }


    public String getCurrentState() {
        return currentState;
    }

    public void setCurrentState(String currentState) {
        this.currentState = currentState;
    }

    private void generateFormerOperation() {
        for (Node node : nodes) {
            for (Operation blockPort : node.sucessors) {
                Operation formerBlockPort = new Operation();
                formerBlockPort.myNode = node.ID;
                formerBlockPort.myOperation = blockPort.myOperation;
                nodes.get(positionMap.get(blockPort.myNode)).preceders.add(formerBlockPort);
            }
        }
    }

    public boolean canApplyConfiguration() {
        boolean flag = false;
        Node node = nodes.get(positionMap.get(currentState));
        for (Operation element : node.sucessors) {
            if (element.myOperation.equals("start")){
                flag = true;
                break;
            }
        }
        return flag;
    }

    private void generateShortestPathSucessor() {
        int count = 0;
        for (Node node : nodes) {
            if (node.isFinal) {
                count++;
            }
            else {
                for (Operation oper : node.sucessors) {
                    if (oper.myOperation.equals("start")) {
                        node.shortestPathSucessor = 0;
                        count++;
                        break;
                    }
                }
            }
        }

        while (count < nodes.size()) {
            for (Node node : nodes) {
                if((! node.isFinal) && node.shortestPathSucessor == -1) {
                    for (Operation oper : node.sucessors) {
                        int i = nodes.get(positionMap.get(oper.myNode)).shortestPathSucessor;
                        if (i > -1) {
                            if (node.shortestPathSucessor == -1) {
                                node.shortestPathSucessor = i+1;
                                node.preferSucessor = oper;
                            }
                            else if (i+1 < node.shortestPathSucessor) {
                                node.shortestPathSucessor = i+1;
                                node.preferSucessor = oper;
                            }
                        }
                    }
                    if (node.shortestPathSucessor != -1) {
                        count++;
                    }
                }
            }
        }
    }

    private void generateShortestPathPreceder(){
        int count = 0;
        for (Node node : this.nodes) {
            if (node.isFinal) {
                count++;
            }
            else {
                for (Operation oper : node.sucessors) {
                    if (oper.myOperation.equals("start")) {
                        node.shortestPathPreceder = 0;
                        count++;
                        break;
                    }
                }
            }
        }

        while (count < this.nodes.size()) {
            for (Node node : this.nodes) {
                if ((!node.isFinal) && node.shortestPathPreceder == -1) {
                    for (Operation oper : node.preceders) {
                        int i = nodes.get(positionMap.get(oper.myNode)).shortestPathPreceder;
                        if (i > -1) {
                            if (node.shortestPathPreceder == -1) {
                                node.shortestPathPreceder = i+1;
                                node.preferPreceder = oper;
                            }
                            else if( i+1 < node.shortestPathPreceder) {
                                node.shortestPathPreceder = i+1;
                                node.preferPreceder = oper;
                            }
                        }
                    }
                    if (node.shortestPathPreceder != -1) {
                        count++;
                    }
                }
            }
        }
    }

    private class Node{
        public String ID;
        public final ArrayList<Operation> preceders = new ArrayList<Operation>();
        public final ArrayList<Operation> sucessors = new ArrayList<Operation>();
        public boolean isInitial = false;
        public boolean isFinal = false;
        public Operation preferPreceder = null;
        public Operation preferSucessor = null;
        public int shortestPathPreceder = -1; // if it is 0 , it is a state which we can apply configuration
        public int shortestPathSucessor = -1; // if it is 0 , it is a state which we can apply configuration

    }

    public class Operation{
        String myOperation = ""; // the operation
        String myNode = ""; // the node Id
    }

    private class WendyRunner extends SwingWorker<Void, String> {

        protected Void doInBackground() {
            try {
                List<String> cmd = new ArrayList<String>();
                cmd.add(path + "/wendy");
                cmd.add("temp.owfn");
                cmd.add("--verbose");
                cmd.add("--correctness=livelock");
                cmd.add("--sa");
                ProcessBuilder builder = new ProcessBuilder(cmd);
                builder.directory(new File(path));
                builder.redirectErrorStream(true);
                Process generate = builder.start();

                StringWriter out = new StringWriter(32);
                InputStream is = generate.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                char[] buffer = new char[32];
                int count;

                while ((count = isr.read(buffer)) > 0) {
                    out.write(buffer, 0, count);
                    publish(out.toString());
                    if (msgDialog.isCancelled()) break;
                }
                isr.close();
            }
            catch (IOException ioe) {
                publish("IOException reading Wendy output.");
            }
            return null;
        }

        protected void process(List<String> outList) {
            String out = outList.get(outList.size() - 1);
            msgDialog.setText(out);
        }

        protected void done() {
            msgDialog.finished();
        }
    }

    // listens for progress of the WendyRunner operation (ie. the completion of wendy)
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("state")) {
            if (evt.getNewValue() == SwingWorker.StateValue.DONE) {
                if (checkResult(msgDialog.getText())) {
                    runner = null;                        // no more events, please
                    generateNodes();
                }
                else {
                    JOptionPane.showMessageDialog(YAWLEditor.getInstance(),
                            "The correctness of the configuration cannot be " +
                            "checked due to this net not being sound.\n " +
                            "Please correct the net first.",
                            "Check for Correctness Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    

}
