/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package operation;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import au.edu.qut.yawl.elements.YAWLServiceGateway;
import au.edu.qut.yawl.elements.YAWLServiceReference;
import au.edu.qut.yawl.elements.YAtomicTask;
import au.edu.qut.yawl.elements.YCondition;
import au.edu.qut.yawl.elements.YInputCondition;
import au.edu.qut.yawl.elements.YNet;
import au.edu.qut.yawl.elements.YOutputCondition;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.elements.YTask;
import au.edu.qut.yawl.elements.data.YParameter;
import au.edu.qut.yawl.elements.data.YVariable;

import com.nexusbpm.NexusWorkflow;
import com.nexusbpm.editor.persistence.EditorDataProxy;
import com.nexusbpm.services.NexusServiceInfo;
import com.nexusbpm.services.data.NexusServiceData;

/**
 * Class WorkflowOperation provides higher-level operations for manipulating
 * YAWL objects. It holds the basic building blocks for the editor commands.
 * 
 * @author Matthew Sandoz
 * @author Nathan Rose
 */
public class WorkflowOperation {
    private static YSpecification tempSpec;
    
    private static YSpecification getTempSpec() {
        if( tempSpec == null ) {
            tempSpec = new YSpecification( "temp_spec" );
        }
        return tempSpec;
    }
    
    /**
     * Creates a workflow under the given parent "folder."
     * @param parent a proxy for the parent "folder" where the specification will be stored,
     *               which may be a folder or a virtualization of a folder depending on the
     *               data context.
     * @param name the name to give the specification.
     * @return the newly created specification.
     * @throws Exception if the proxy to the parent cannot be converted into a path (shouldn't
     *                   happen).
     */
    public static YSpecification createSpecification(EditorDataProxy parent, String name)
    throws Exception {
        YSpecification spec = null;
        
        URI u = new URI( parent.getData().toString() );
        spec = new YSpecification(
                new URI(
                        u.getScheme(),
                        u.getAuthority(),
                        u.getPath() + "/" + name,
                        null,
                        null ).toString() );
        spec.setName( name );
        spec.setBetaVersion(NexusWorkflow.CURRENT_VERSION);
        spec.setDocumentation("");
        
        return spec;
    }
    
    /**
     * Creates a new net to be placed inside a specification.
     * @param name the name to give the net.
     * @return the newly created net.
     */
    public static YNet createNet(String name) {
        YNet net = null;;
        
        // the constructor throws a null pointer exception if you don't have a specification
        net = new YNet( name, getTempSpec() );
        net.setParent( null );
        net.setName( name );
        net.setDocumentation("");
        
        return net;
    }
    
    /**
     * Places the given net into the specification. If the specification has
     * no root net, then the new net is set as the root net, otherwise the
     * net is added to the specification's list of decompositions.
     * @param specificationProxy a proxy for the parent specification.
     * @param net the net to add to the specification.
     */
    public static void attachNetToSpec(EditorDataProxy specificationProxy, YNet net) {
        YSpecification spec = (YSpecification) specificationProxy.getData();
        
        net.setParent( spec );
        if( spec.getRootNet() == null ) {
            spec.setRootNet( net );
        }
        else {
            spec.setDecomposition( net );
        }
    }
    
    /**
     * Removes the net from its parent specification.
     * @param net the net to remove from its specification.
     */
    public static void detachNetFromSpec(YNet net) {
        YSpecification spec = net.getParent();
        
        spec._decompositions.remove( net );
        net.setParent( null );
    }
    
    public static YInputCondition createInputCondition() {
        return new YInputCondition( "start", "start", null );
    }
    
    public static YOutputCondition createOutputCondition() {
        return new YOutputCondition( "end", "end", null );
    }
    
    public static YCondition createCondition(String id, String label) {
        return new YCondition( id, label, null );
    }
    
    public static void attachConditionToNet(EditorDataProxy netProxy, YCondition condition) {
        YNet net = (YNet) netProxy.getData();
        
        condition.setParent( net );
        // TODO net.addNetElement() would be sufficient... the other functions need refactoring
        if( condition instanceof YInputCondition ) {
            net.setInputCondition( (YInputCondition) condition );
        }
        else if( condition instanceof YOutputCondition ) {
            net.setOutputCondition( (YOutputCondition) condition );
        }
        else {
            net.addNetElement( condition );
        }
    }
    
    public static void detachConditionFromNet(YCondition condition) {
        YNet net = condition.getParent();
        
        net.removeNetElement( condition );
        condition.setParent( null );
    }

	/**
	 * Creates a Nexus workflow task in the given net. Provides all default 
	 * mappings from net variables to decomposed gateway variables. 
	 * 
	 * @param taskID
	 * @param taskName
	 * @param net
	 * @param gateway
	 * @param serviceInfo
	 * @return
	 */
	public static YAtomicTask createTask(String taskID, String taskName,
			YNet net, YAWLServiceGateway gateway, NexusServiceInfo serviceInfo) {
		YAtomicTask retval = new YAtomicTask(taskID, YTask._AND, YTask._AND,
				net);
		retval.setName(taskName);
		retval.setDecompositionPrototype(gateway);
		for (int i = 0; i < serviceInfo.getVariableNames().length; i++) {
			String varName = serviceInfo.getVariableNames()[i];
			retval.setDataBindingForInputParam(createInputBindingString(net,
					varName, taskID + NexusWorkflow.NAME_SEPARATOR + varName),
					varName);
			retval.setDataBindingForOutputExpression(createOutputBindingString(
					net, taskID, taskID + NexusWorkflow.NAME_SEPARATOR
							+ varName, varName), taskID
					+ NexusWorkflow.NAME_SEPARATOR + varName);
		}
		retval.setDataBindingForInputParam(createInputBindingString(net,
				NexusWorkflow.SERVICENAME_VAR, taskID
						+ NexusWorkflow.NAME_SEPARATOR
						+ NexusWorkflow.SERVICENAME_VAR),
				NexusWorkflow.SERVICENAME_VAR);
		retval.setDataBindingForOutputExpression(createOutputBindingString(net,
				taskID, taskID + NexusWorkflow.NAME_SEPARATOR
						+ NexusWorkflow.STATUS_VAR, NexusWorkflow.STATUS_VAR),
				taskID + NexusWorkflow.NAME_SEPARATOR
						+ NexusWorkflow.STATUS_VAR);
		return retval;
	}

	/**
	 * Creates a Nexus Workflow Gateway reference in the given net.
	 * 
	 * @param taskID
	 * @param net
	 * @param serviceInfo
	 * @return
	 */
	public static YAWLServiceGateway createGateway(String taskID, YNet net,
			NexusServiceInfo serviceInfo) {
		YAWLServiceGateway retval = new YAWLServiceGateway(net.getId()
				+ NexusWorkflow.NAME_SEPARATOR + taskID, net.getParent());
		YAWLServiceReference yawlService = new YAWLServiceReference(
				NexusWorkflow.LOCAL_INVOKER_URI, retval);
		retval.setYawlService(yawlService);
		for (int i = 0; i < serviceInfo.getVariableNames().length; i++) {
			YParameter iparam = new YParameter(net,
					YParameter._INPUT_PARAM_TYPE);
			YParameter oparam = new YParameter(net,
					YParameter._OUTPUT_PARAM_TYPE);
			iparam.setDataTypeAndName(NexusWorkflow.VARTYPE_STRING, serviceInfo
					.getVariableNames()[i], NexusWorkflow.XML_SCHEMA_URL);
			oparam.setDataTypeAndName(NexusWorkflow.VARTYPE_STRING, serviceInfo
					.getVariableNames()[i], NexusWorkflow.XML_SCHEMA_URL);
			retval.setInputParam(iparam);
			retval.setOutputParameter(oparam);
		}
		YParameter iparam = new YParameter(net, YParameter._INPUT_PARAM_TYPE);
		YParameter oparam = new YParameter(net, YParameter._OUTPUT_PARAM_TYPE);
		iparam.setDataTypeAndName(NexusWorkflow.VARTYPE_STRING,
				NexusWorkflow.SERVICENAME_VAR, NexusWorkflow.XML_SCHEMA_URL);
		oparam.setDataTypeAndName(NexusWorkflow.VARTYPE_STRING,
				NexusWorkflow.STATUS_VAR, NexusWorkflow.XML_SCHEMA_URL);
		retval.setInputParam(iparam);
		retval.setOutputParameter(oparam);
		return retval;
	}

	/**
	 * Creates variables for a particular NexusServiceInfo (service 
	 * definition) on the given net.
	 * 
	 * @param taskID
	 * @param net
	 * @param serviceInfo
	 */
	public static void createNetVariables(String taskID, YNet net,
			NexusServiceInfo serviceInfo) {
		NexusServiceData data = new NexusServiceData();

		for (int i = 0; i < serviceInfo.getVariableNames().length; i++) {
			String name = serviceInfo.getVariableNames()[i];
			String type = serviceInfo.getVariableTypes()[i];

			data.setType(name, type);

			try {
				data.set(name, serviceInfo.getInitialValues()[i]);
			} catch (IOException e) {
				e.printStackTrace(System.out);
			}
		}
		data.marshal(net, taskID);

		List<YVariable> vars = net.getLocalVariables();
		vars.add(getStringVariable(net, taskID, NexusWorkflow.SERVICENAME_VAR,
				serviceInfo.getServiceName()));
		vars
				.add(getStringVariable(net, taskID, NexusWorkflow.STATUS_VAR,
						null));
	}

	/**
	 * Creates a YVariable on the given net with a name derived from the
	 * task id, a separator and the given simple var name.  
	 * 
	 * @param net
	 * @param taskID
	 * @param varName
	 * @param initialValue
	 * @return
	 */
	public static YVariable getStringVariable(YNet net, String taskID,
			String varName, String initialValue) {
		YVariable var;
		var = new YVariable(net);
		var.setDataTypeAndName(NexusWorkflow.VARTYPE_STRING, taskID
				+ NexusWorkflow.NAME_SEPARATOR + varName,
				NexusWorkflow.XML_SCHEMA_URL);
		if (initialValue != null) {
			var.setInitialValue(initialValue);
		}
		return var;
	}

	/**
	 * Creates a default input binding string useful for mapping net variables
	 * to gateways
	 * 
	 * @param net
	 * @param elementName
	 * @param variableName
	 * @return
	 */
	public static String createInputBindingString(YNet net, String elementName,
			String variableName) {
		return "<" + elementName + ">" + "{" + "/" + net.getId() + "/"
				+ variableName + "/text()" + "}" + "</" + elementName + ">";
	}

	/**
	 * 
	 * Creates a default input binding string useful for mapping net variables
	 * to gateways
	 * 
	 * @param net
	 * @param taskID
	 * @param elementName
	 * @param variableName
	 * @return
	 */
	public static String createOutputBindingString(YNet net, String taskID,
			String elementName, String variableName) {
		return "<" + elementName + ">" + "{" + "/" + net.getId()
				+ NexusWorkflow.NAME_SEPARATOR + taskID + "/" + variableName
				+ "/text()" + "}" + "</" + elementName + ">";
	}

	/**
	 * Changes the existing input mapping in a task referred to by the target 
	 * variable to refer to a variable from another task.
	 * 
	 * @param source variable
	 * @param target variable
	 */
	public static void remapInputVariable(YVariable source, YVariable target) {
		YTask sourceTask = getNexusTask(source);
		YTask targetTask = getNexusTask(target);
		YNet net = sourceTask.getParent();
		String sourceTaskId = sourceTask.getID();
		String sourcevarName = getNexusSimpleVarName(source);
		String targetvarName = getNexusSimpleVarName(target);

		targetTask.setDataBindingForInputParam(createInputBindingString(net,
				targetvarName, sourceTaskId + NexusWorkflow.NAME_SEPARATOR
						+ sourcevarName), targetvarName);
	}

	/**
	 * Parses a Nexus net variable and attempts to discern what task
	 * it belongs to.
	 * 
	 * @param var
	 * @return
	 */
	public static YTask getNexusTask(YVariable var) {
		YTask retval = null;
		YNet net = (YNet) var.getParent();
		int separatorAt = var.getName().indexOf(NexusWorkflow.NAME_SEPARATOR);
		if (separatorAt != -1) {
			String id = var.getName().substring(0, separatorAt);
			retval = (YTask) net.getNetElement(id);
		}
		return retval;
	}

	/**
	 * Parses a Nexus net variable and attempts to discern the simple
	 * name of the variable.
	 * 
	 * @param var
	 * @return
	 */
	public static String getNexusSimpleVarName(YVariable var) {
		String retval = null;
		int separatorAt = var.getName().indexOf(NexusWorkflow.NAME_SEPARATOR);
		if (separatorAt != -1) {
			retval = var.getName().substring(
					separatorAt + NexusWorkflow.NAME_SEPARATOR.length());
		}
		return retval;
	}

}
