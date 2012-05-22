package org.yawlfoundation.yawl.editor.net;

import org.jdesktop.swingworker.SwingWorker;
import org.yawlfoundation.yawl.editor.YAWLEditor;
import org.yawlfoundation.yawl.editor.swing.AnalysisDialog;
import org.yawlfoundation.yawl.editor.elements.model.CPort;
import org.yawlfoundation.yawl.editor.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.foundations.FileUtilities;
import org.yawlfoundation.yawl.editor.specification.ProcessConfigurationModel;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.prefs.Preferences;

public class ServiceAutomatonTree implements PropertyChangeListener {

    private static final Preferences prefs = Preferences.userNodeForPackage(YAWLEditor.class);

    //this map records the position of the node with a specified ID in the array list
    private HashMap<String, Integer> positionMap;
    private ArrayList<Node> nodes;
    private ArrayList<YAWLTask> tasks;
    private String currentState;       // record the Id of the node with current state
    private AnalysisDialog msgDialog;
    private WendyRunner runner;
    private String path;

    public ServiceAutomatonTree(NetGraph net) {
        init();
        String errMsg = null;
        if (isWendyInstalled()) {
            PetriNet petri = new PetriNet(net.getNetModel(), path);
            tasks = petri.getTasks();
            if (petri.checkValidate()) {
                petri.saveOWFNfile();
                msgDialog = new AnalysisDialog("Net");
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
        path = prefs.get("WendyFilePath", FileUtilities.getHomeDir() + "wendy");
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
        return (task.getDecomposition() != null) ? task.getDecomposition().getLabel() :
                task.getEngineId();
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
            YAWLTask task = getTheTask(taskID);
            if (task != null) {
                task.getInputCPorts().get(portID).setConfigurationSetting(CPort.BLOCKED);
            }
            message = "The Task "+ taskID +"'s input port "+portID+" has been automatically blocked";
        }
        else if (operations.contains("OUTPUT")){
            String taskID = operations.substring(operations.indexOf("block_")+6,
                    operations.indexOf("OUTPUT"));
            int portID = Integer.parseInt(operations.substring(operations.indexOf("OUTPUT")+6));
            YAWLTask task = getTheTask(taskID);
            if (task != null) {
                task.getOutputCPorts().get(portID).setConfigurationSetting(CPort.BLOCKED);
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
            YAWLTask task = getTheTask(taskID);
            if (task != null) {
                task.getInputCPorts().get(portID).setConfigurationSetting(CPort.ACTIVATED);
            }
            message = "The Task "+ taskID +"'s input port "+portID+" has been automatically activated";
        }
        else if (operations.contains("OUTPUT")) {
            String taskID = operations.substring(operations.indexOf("block_")+6,
                    operations.indexOf("OUTPUT"));
            int portID = Integer.parseInt(operations.substring(operations.indexOf("OUTPUT")+6));
            YAWLTask task = getTheTask(taskID);
            if (task != null) {
                task.getOutputCPorts().get(portID).setConfigurationSetting(CPort.ACTIVATED);
            }
            message = "The Task "+ taskID +"'s output port "+portID+" has been automatically activated";
        }
        return message;
    }

    private YAWLTask getTheTask(String taskID){
        for (YAWLTask task : tasks) {
            if (task.getDecomposition() == null) {
                if (task.getEngineId().equals(taskID)) {
                    return task;
                }
            }
            else {
                if (task.getDecomposition().getLabel().equals(taskID)){
                    return task;
                }
            }
        }
        return null;
    }

    public void testGeneratingTree(){
//        for(Node node:nodes){
//            if(node.shortestPathSucessor == -2){
//                System.out.println("ID"+node.ID);
//            }
/*			System.out.println("sucessors");
			for(int i=0; i< node.sucessors.size();i++){
				System.out.println(node.sucessors.get(i).myOperation+"->"+node.sucessors.get(i).myNode);
			}
			System.out.println("preceder");
			for(int i=0; i< node.preceders.size();i++){
				System.out.println(node.preceders.get(i).myOperation+"->"+node.preceders.get(i).myNode);
		}
			System.out.println("Mapping id "+this.positionMap.get(node.ID));
			if(node.isFinal){
				System.out.println("This is the final state");
			}else if (node.isInitial){
				System.out.println("This is the initial state");
	}
			if(!node.isFinal){
				System.out.println("steps needed "+node.shortestPathPreceder);
				if(node.preferPreceder != null){
					System.out.println("my prefer preceder is "+ node.preferPreceder.myOperation+"->"+ node.preferPreceder.myNode);
				}
				System.out.println("steps needed "+node.shortestPathSucessor);
				if(node.preferSucessor != null){
				System.out.println("my prefer sucessor is "+ node.preferSucessor.myOperation +"->"+node.preferSucessor.myNode);
				}
			}
			System.out.println();
		}*/
//        }
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
        public ArrayList<Operation> preceders = new ArrayList<Operation>();
        public ArrayList<Operation> sucessors = new ArrayList<Operation>();
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
