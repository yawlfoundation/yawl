package operation;

import java.io.IOException;
import java.util.List;

import au.edu.qut.yawl.elements.YAWLServiceGateway;
import au.edu.qut.yawl.elements.YAWLServiceReference;
import au.edu.qut.yawl.elements.YAtomicTask;
import au.edu.qut.yawl.elements.YNet;
import au.edu.qut.yawl.elements.YTask;
import au.edu.qut.yawl.elements.data.YParameter;
import au.edu.qut.yawl.elements.data.YVariable;

import com.nexusbpm.NexusWorkflow;
import com.nexusbpm.services.NexusServiceInfo;
import com.nexusbpm.services.data.NexusServiceData;

public class WorkflowOperation {

	public static YAtomicTask createTask(String taskID, String taskName, YNet net,
			YAWLServiceGateway gateway, NexusServiceInfo serviceInfo) {
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


	public static void createNetVariables(String taskID, YNet net,
			NexusServiceInfo serviceInfo) {
        NexusServiceData data = new NexusServiceData();
        
        for (int i = 0; i < serviceInfo.getVariableNames().length; i++) {
            String name = serviceInfo.getVariableNames()[i];
            String type = serviceInfo.getVariableTypes()[i];
            
            data.setType( name, type );
            
            try {
                data.set( name, serviceInfo.getInitialValues()[i] );
            }
            catch( IOException e ) {
                e.printStackTrace( System.out );
            }
        }
        data.saveToTask( net, taskID );
        
		List<YVariable> vars = net.getLocalVariables();
		vars.add(getStringVariable(
                net, taskID, NexusWorkflow.SERVICENAME_VAR, serviceInfo.getServiceName()));
		vars.add(getStringVariable(
                net, taskID, NexusWorkflow.STATUS_VAR, null));
	}

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

	public static String createInputBindingString(YNet net, String elementName,
			String variableName) {
		return "<" + elementName + ">" + "{" + "/" + net.getId() + "/"
				+ variableName + "/text()" + "}" + "</" + elementName + ">";
	}

	public static String createOutputBindingString(YNet net, String taskID,
			String elementName, String variableName) {
		return "<" + elementName + ">" + "{" + "/" + net.getId()
				+ NexusWorkflow.NAME_SEPARATOR + taskID + "/" + variableName
				+ "/text()" + "}" + "</" + elementName + ">";
	}
}
