package au.edu.qut.yawl.persistence;

import au.edu.qut.yawl.elements.KeyValue;
import au.edu.qut.yawl.elements.YTask;
import au.edu.qut.yawl.elements.data.YParameter;
import au.edu.qut.yawl.elements.data.YVariable;

public class SourcedVariable {

	private YTask task;
	private String name;
	public SourcedVariable(YTask task, String name) {
		super();
		this.task = task;
		this.name = name;
	}
	
	private void nothing() {
    	Object val = task.getDataMappingsForTaskStarting().get(name);
    	YVariable netVar = task.getParent().getLocalVariable(task.getID()+ "__" + name);
    	YParameter inputParameter = null;
    	for (YParameter parm: task.getDecompositionPrototype().getInputParameters()) {
    		if (parm.getName().equals(name)) {
    			inputParameter = parm;break;
    		}
    	}
    	YParameter outputParameter = null;
    	for (YParameter parm: task.getDecompositionPrototype().getOutputParameters()) {
    		if (parm.getName().equals(name)) {
    			outputParameter = parm;break;
    		}
    	}
    	KeyValue startingMapping = task.getDataMappingsForTaskStartingSet().get(name);
    	String completionMapping = task.getDataMappingsForTaskCompletion().get(task.getID()+ "__" + name);
    	KeyValue enablementMapping = task.getDataMappingsForEnablement().get(name);

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public YTask getTask() {
		return task;
	}

	public void setTask(YTask task) {
		this.task = task;
	}
	
	
	
	
}
