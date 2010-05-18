package org.yawlfoundation.yawl.editor.net;

import org.yawlfoundation.yawl.editor.YAWLEditor;
import org.yawlfoundation.yawl.editor.analyser.AnalysisDialog;
import org.yawlfoundation.yawl.editor.elements.model.CPort;
import org.yawlfoundation.yawl.editor.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.foundations.FileUtilities;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.prefs.Preferences;

public class ServiceAutomatonTree {

    //this map records the position of the node with a specified ID in the array list
    private HashMap<String, Integer> positionMap = new HashMap<String, Integer>();
    private ArrayList<Node> nodes = new ArrayList<Node>();
    private ArrayList<YAWLTask> tasks = new ArrayList<YAWLTask>();
    private NetGraph net;
    private String CurrentState = ""; // record the Id of the node with current state
    private String wendyMessage = "";
    private String wendyResult;
    private AnalysisDialog msgDialog = new AnalysisDialog("Net");


    private static final Preferences prefs = Preferences.userNodeForPackage(YAWLEditor.class);
    private String path = prefs.get("WendyFilePath", FileUtilities.getHomeDir() + "wendy");

    public ServiceAutomatonTree(NetGraph net){
        this.net = net;
        if (isWendyInstalled()) {
            PetriNet petri = new PetriNet(net.getNetModel(), path);
            tasks = petri.getTasks();
            if (petri.checkValidate()) {
                petri.saveOWFNfile();
                try {
                    if (UsingWendyandConfigurator()) {
                        generateNodes();
                    }
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            else{
                JOptionPane.showMessageDialog( YAWLEditor.getInstance(),
                        "Can't convert the diagram into a petri net," +
                        "because the net contains the following things: 1, OR-join  " +
                        "2, OR-Split  3, Cancellation Set",
                    "Check for Correctness Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        else {
            JOptionPane.showMessageDialog( YAWLEditor.getInstance(),
                    "Unable to locate installation of Wendy for checking process " +
                    "configuration correctness.",
                    "Check for Correctness Error", JOptionPane.ERROR_MESSAGE);
        }

        //testGeneratingTree(); TO PRINT DEBUG DATA
    }


    private boolean isWendyInstalled() {
        return new File(path + "/wendy.conf").exists();
    }

    
    private boolean UsingWendyandConfigurator() throws InterruptedException {

        try {
            long startTime = System.currentTimeMillis();
            File test_allow = new File(path + "/test_allow.txt");
            File result = new File(path + "/result.txt");
            if (test_allow.exists()) {
                test_allow.delete();
            }
            if (result.exists()) {
                result.delete();
            }

            //  "/myCommands.bat"

            ProcessBuilder builder = new ProcessBuilder("./commands.sh");
            builder.directory(new File(path));
            builder.redirectErrorStream(true);
            Process generate = builder.start();

            // get the result of the process execution
            StringWriter out = new StringWriter(32);
            InputStream is = generate.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            char[] buffer = new char[32];
            int count;

            while ((count = isr.read(buffer)) > 0) {
               out.write(buffer, 0, count);
               msgDialog.write(out.toString());
            }

            isr.close();
            msgDialog.finished();
            
            // set and return the output
            wendyResult = out.toString();
            return true;
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean checkResult(){
        boolean flag = false;
        BufferedReader in;
        try {
            in = new BufferedReader(new FileReader(path + "/result.txt"));
            String begin="";
            begin = in.readLine();
            while(!begin.contains("wendy: net is controllable:")){
                if(in.readLine() != null){
                    begin = in.readLine();
                } else{
                    break;
                }
            }
            if(begin.contains("YES")){
                flag = true;
            }
            return flag;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

    }
    private void generateNodes() {
        try {
            final int BUF_SIZE = 8192;
            StringWriter out = new StringWriter(BUF_SIZE);
            JOptionPane.showMessageDialog( YAWLEditor.getInstance(),"Please wait while Wendy generates the service automaton for this model.");
            File file = new File(path + "/test_allow.sa");
            File test = new File(path + "/test.owfn");
            while(test.exists()){
            }
            if(!checkResult()){
                JOptionPane.showMessageDialog( YAWLEditor.getInstance(),"The correctness of the configuration cannot be checked due to this net not being sound.\n Please correct the net first.");
                return;
            }
            BufferedReader in = new BufferedReader(new FileReader(path + "/test_allow.sa"));
            String begin = in.readLine();
            while(!begin.equals("NODES")){
                begin = in.readLine();
            }
            String line = in.readLine();

            while( line != null ){
                if(!line.contains("->")){
                    Node node = new Node();
                    if(line.contains(":")){
                        int pos = line.indexOf(":");
                        node.ID = line.substring(2,pos-1);
                    }else{
                        node.ID = line.substring(2);
                    }
                    if(line.contains("INITIAL")){
                        node.isInitial = true;
                        this.CurrentState = node.ID;
                    }else if(line.contains("FINAL")){
                        node.isFinal = true;
                    }
                    line = in.readLine();
                    if(line != null){
                        boolean flag = line.contains("->");
                        while(flag){
                            int position = line.indexOf("->");
                            String operation = "";
                            String nextID = "";
                            operation = line.substring(4,position-1);
                            nextID = line.substring(position+3);
                            Operation blockPort = new Operation();
                            blockPort.myOperation = operation;
                            blockPort.myNode = nextID;
                            node.sucessors.add(blockPort);
                            line = in.readLine();
                            if(line != null){
                                flag = line.contains("->");
                            } else {
                                flag = false;
                            }
                        }
                    }
                    this.nodes.add(node);
                } else{
                    line = in.readLine();
                }
            }
            in.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        matchIDs();
        generateFormerOperation();
        generateShortestPathPreceder();
        generateShortestPathSucessor();
    }

    private void matchIDs(){
        for(int i=0; i< nodes.size(); i++){
            this.positionMap.put(nodes.get(i).ID,i);
        }
    }

    public boolean ProcessCorrectnessCheckingForBlock(YAWLTask task, String type, int portID){
        boolean flag = false;
        Node node = this.nodes.get(this.positionMap.get(this.CurrentState));
        String blockPort = "";
        if(task.getDecomposition() == null){
            blockPort = "block_"+task.getEngineId()+type+portID;
        } else {
            blockPort = "block_"+task.getDecomposition().getLabel()+type+portID;
        }
        for(int i=0; i<node.sucessors.size(); i++){
            if(node.sucessors.get(i).myOperation.equals(blockPort)){
                flag = true;
                break;
            }
        }
        return flag;
    }

    public void changeCurrentStateAfterBlock(YAWLTask task, String type, int portID){

        Node node = this.nodes.get(this.positionMap.get(this.CurrentState));
        String blockPort = "";
        if(task.getDecomposition() == null){
            blockPort = "block_"+task.getEngineId()+type+portID;
        } else {
            blockPort = "block_"+task.getDecomposition().getLabel()+type+portID;
        }
        for(int i=0; i<node.sucessors.size(); i++){
            if(node.sucessors.get(i).myOperation.equals(blockPort)){
                this.CurrentState = node.sucessors.get(i).myNode;
                //System.out.println("the current state is "+this.CurrentState);
                break;
            }
        }
        AutomaticallyBlockOthers();
    }

    public boolean ProcessCorrectnessCheckingForActivate(YAWLTask task, String type, int portID){
        boolean flag = false;
        Node node = this.nodes.get(this.positionMap.get(this.CurrentState));
        String blockPort = "";
        if(task.getDecomposition() == null){
            blockPort = "block_"+task.getEngineId()+type+portID;
        } else {
            blockPort = "block_"+task.getDecomposition().getLabel()+type+portID;
        }
        for(int i=0; i<node.preceders.size(); i++){
            if(node.preceders.get(i).myOperation.equals(blockPort)){
                flag = true;
                break;
            }
        }
        return flag;
    }

    public void changeCurrentStateAfterActivate(YAWLTask task, String type, int portID){

        Node node = this.nodes.get(this.positionMap.get(this.CurrentState));
        String blockPort = "";
        if(task.getDecomposition() == null){
            blockPort = "block_"+task.getEngineId()+type+portID;
        } else {
            blockPort = "block_"+task.getDecomposition().getLabel()+type+portID;
        }
        for(int i=0; i<node.preceders.size(); i++){
            if(node.preceders.get(i).myOperation.equals(blockPort)){
                this.CurrentState = node.preceders.get(i).myNode;
                break;
            }
        }
        AutomaticallyActivateOthers();
    }


    private void AutomaticallyBlockOthers(){
        String message = "";
        Node node = this.nodes.get(this.positionMap.get(this.CurrentState));
        while(node.shortestPathSucessor > 0){
            message = message + BlockAPort(node.preferSucessor.myOperation)+"\n";
            this.CurrentState = node.preferSucessor.myNode;
            node = this.nodes.get(this.positionMap.get(this.CurrentState));
        }
        if(message != ""){
            JOptionPane.showMessageDialog( YAWLEditor.getInstance(),message);
        }
    }

    private void AutomaticallyActivateOthers(){
        String message = "";
        Node node = this.nodes.get(this.positionMap.get(this.CurrentState));
        while(node.shortestPathPreceder > 0){
            message = message + ActivateAPort(node.preferPreceder.myOperation)+"\n";
            this.CurrentState = node.preferPreceder.myNode;
            node = this.nodes.get(this.positionMap.get(this.CurrentState));
            //System.out.println("the current state is "+this.CurrentState);
        }
        if(message != ""){
            JOptionPane.showMessageDialog( YAWLEditor.getInstance(),message);
        }
    }

    private String BlockAPort(String operations) {
        String message = "";
        if(operations.contains("INPUT")){
            String taskID = operations.substring(operations.indexOf("block_")+6, operations.indexOf("INPUT"));
            int portID = Integer.parseInt(operations.substring(operations.indexOf("INPUT")+5));
            YAWLTask task = getTheTask(taskID);
            if(task != null){
                task.getInputCPorts().get(portID).setConfigurationSetting(CPort.BLOCKED);
            }
            message = "The Task "+ taskID +"'s input port "+portID+" has been automatically blocked";
        } else if (operations.contains("OUTPUT")){
            String taskID = operations.substring(operations.indexOf("block_")+6, operations.indexOf("OUTPUT"));
            int portID = Integer.parseInt(operations.substring(operations.indexOf("OUTPUT")+6));
            YAWLTask task = getTheTask(taskID);
            if(task != null){
                task.getOutputCPorts().get(portID).setConfigurationSetting(CPort.BLOCKED);
            }
            message = "The Task "+ taskID +"'s output port "+portID+" has been automatically blocked";
        }
        return message;
    }

    private String ActivateAPort(String operations) {
        String message = "";
        if(operations.contains("INPUT")){
            String taskID = operations.substring(operations.indexOf("block_")+6, operations.indexOf("INPUT"));
            int portID = Integer.parseInt(operations.substring(operations.indexOf("INPUT")+5));
            YAWLTask task = getTheTask(taskID);
            if(task != null){
                task.getInputCPorts().get(portID).setConfigurationSetting(CPort.ACTIVATED);
            }
            message = "The Task "+ taskID +"'s input port "+portID+" has been automatically activated";
        } else if (operations.contains("OUTPUT")){
            String taskID = operations.substring(operations.indexOf("block_")+6, operations.indexOf("OUTPUT"));
            int portID = Integer.parseInt(operations.substring(operations.indexOf("OUTPUT")+6));
            YAWLTask task = getTheTask(taskID);
            if(task != null){
                task.getOutputCPorts().get(portID).setConfigurationSetting(CPort.ACTIVATED);
            }
            message = "The Task "+ taskID +"'s output port "+portID+" has been automatically activated";
        }
        return message;
    }

    private YAWLTask getTheTask(String taskID){
        for(YAWLTask task : this.tasks ){
            if(task.getDecomposition() == null){
                if (task.getEngineId().equals(taskID)){
                    return task;
                }
            } else {
                if(task.getDecomposition().getLabel().equals(taskID)){
                    return task;
                }
            }
        }
        return null;
    }

    public void testGeneratingTree(){
        for(Node node:nodes){
            if(node.shortestPathSucessor == -2){
                System.out.println("ID"+node.ID);
            }
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
        }
    }


    public String getCurrentState() {
        return CurrentState;
    }

    public void setCurrentState(String currentState) {
        CurrentState = currentState;
    }

    private void generateFormerOperation(){
        for(Node node : this.nodes){
            for(Operation blockPort : node.sucessors){
                Operation formerBlockPort = new Operation();
                formerBlockPort.myNode = node.ID;
                formerBlockPort.myOperation = blockPort.myOperation;
                this.nodes.get(this.positionMap.get(blockPort.myNode)).preceders.add(formerBlockPort);
            }
        }
    }

    public boolean canApplyConfiguration(){
        boolean flag = false;
        Node node = this.nodes.get(this.positionMap.get(this.CurrentState));
        for(Operation element : node.sucessors){
            if (element.myOperation.equals("start")){
                flag = true;
                break;
            }
        }
        return flag;
    }

    private void generateShortestPathSucessor(){

        int count = 0;

        for(Node node : this.nodes){
            if(node.isFinal){
                count++;
            } else {
                for(Operation oper : node.sucessors){
                    if(oper.myOperation.equals("start")){
                        node.shortestPathSucessor = 0;
                        count++;
                        break;
                    }
                }
            }
        }

        while(count < this.nodes.size()){
            for(Node node : this.nodes){
                if( (!node.isFinal) && node.shortestPathSucessor == -1){
                    for(Operation oper : node.sucessors){
                        int i = this.nodes.get(this.positionMap.get(oper.myNode)).shortestPathSucessor;
                        if(i > -1){
                            if(node.shortestPathSucessor == -1){
                                node.shortestPathSucessor = i+1;
                                node.preferSucessor = oper;
                            } else
                            if( i+1 < node.shortestPathSucessor){
                                node.shortestPathSucessor = i+1;
                                node.preferSucessor = oper;
                            }
                        }
                    }
                    if(node.shortestPathSucessor != -1){
                        count++;
                    }
                }
            }
        }
    }

    private void generateShortestPathPreceder(){

        int count = 0;

        for(Node node : this.nodes){
            if(node.isFinal){
                count++;
            } else {
                for(Operation oper : node.sucessors){
                    if(oper.myOperation.equals("start")){
                        node.shortestPathPreceder = 0;
                        count++;
                        break;
                    }
                }
            }
        }

        while(count < this.nodes.size()){
            for(Node node : this.nodes){
                if( (!node.isFinal) && node.shortestPathPreceder == -1){
                    for(Operation oper : node.preceders){
                        int i = this.nodes.get(this.positionMap.get(oper.myNode)).shortestPathPreceder;
                        if(i > -1){
                            if(node.shortestPathPreceder == -1){
                                node.shortestPathPreceder = i+1;
                                node.preferPreceder = oper;
                            } else if( i+1 < node.shortestPathPreceder){
                                node.shortestPathPreceder = i+1;
                                node.preferPreceder = oper;
                            }
                        }
                    }
                    if(node.shortestPathPreceder != -1){
                        count++;
                    }
                }
            }
            System.out.println("the count number : "+count);
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

}
