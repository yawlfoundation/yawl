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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import au.edu.qut.yawl.elements.KeyValue;
import au.edu.qut.yawl.elements.YAWLServiceGateway;
import au.edu.qut.yawl.elements.YAWLServiceReference;
import au.edu.qut.yawl.elements.YAtomicTask;
import au.edu.qut.yawl.elements.YCompositeTask;
import au.edu.qut.yawl.elements.YCondition;
import au.edu.qut.yawl.elements.YDecomposition;
import au.edu.qut.yawl.elements.YExternalNetElement;
import au.edu.qut.yawl.elements.YInputCondition;
import au.edu.qut.yawl.elements.YNet;
import au.edu.qut.yawl.elements.YOutputCondition;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.elements.YTask;
import au.edu.qut.yawl.elements.data.YParameter;
import au.edu.qut.yawl.elements.data.YVariable;
import au.edu.qut.yawl.util.HashBag;

import com.nexusbpm.NexusWorkflow;
import com.nexusbpm.services.NexusServiceInfo;
import com.nexusbpm.services.data.NexusServiceData;

/**
 * Class WorkflowOperation provides higher-level operations for manipulating
 * YAWL objects. It holds the basic building blocks for the editor commands.
 * 
 * Note: the general pattern is to return objects that are disconnected from
 * their intended parent inside the create and copy operations, so that the
 * command that is invoking the operations can control when to attach and
 * detach the objects (through other functions available in this class).
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
     * @param parent the URI for the parent "folder" where the specification will be stored.
     * @param name the name to give the specification.
     * @return the newly created specification.
     * @throws Exception if the proxy to the parent cannot be converted into a path (shouldn't
     *                   happen).
     */
    public static YSpecification createSpecification(String parentURI, String name)
    throws Exception {
        YSpecification spec = null;
        
        URI u = new URI( parentURI );
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
     * Places the given decomposition into the specification. If the given decomposition
     * is a net and the specification has no root net, then the new net is set as the
     * root net, otherwise the decomposition is added to the specification's list of
     * decompositions.
     * @param specificationProxy a proxy for the parent specification.
     * @param decomposition the decomposition to add to the specification.
     */
    public static void attachDecompositionToSpec(YSpecification spec, YDecomposition decomposition) {
        decomposition.setParent( spec );
        if( decomposition instanceof YNet && spec.getRootNet() == null ) {
            spec.setRootNet( (YNet) decomposition );
        }
        else {
            spec.setDecomposition( decomposition );
        }
    }
    
    /**
     * Removes the net from its parent specification.
     * @param net the net to remove from its specification.
     */
    public static void detachDecompositionFromSpec(YDecomposition decomp) {
        YSpecification spec = decomp.getParent();
        
        spec._decompositions.remove( decomp );
        decomp.setParent( null );
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
    
    public static void attachConditionToNet(YNet net, YCondition condition) {
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
     * This function is written to copy a nexus workflow created net to another specification,
     * but will handle other cases to some extent (such as web service gateways). Should be
     * made more general at some point in the future.
     */
    public static List<YDecomposition> copyDecomposition(YDecomposition original, YSpecification target) throws CloneNotSupportedException {
        // just clone the entire spec, then detach and return the parts that are needed
        YSpecification clonedSpec = (YSpecification) original.getParent().clone();
        
        List<YDecomposition> decomps = new LinkedList<YDecomposition>();
        
        String id = original.getId();
        String newId = getAvailableDecompID( target, id );
        boolean changeId = ! id.equals( newId );
        
        YDecomposition decomp = clonedSpec.getDecomposition( id );
        detachDecompositionFromSpec( decomp );
        decomps.add( decomp );
        
        if( changeId ) {
            decomp.setIdAndSpecification( newId, target );
            decomp.setParent( null );
        }
        
        if( decomp instanceof YNet ) {
            YNet net = (YNet) decomp;
            
            for( YExternalNetElement element : net.getNetElements() ) {
                if( element instanceof YAtomicTask ) {
                    YAtomicTask task = (YAtomicTask) element;
                    YDecomposition gateway = (YAWLServiceGateway) task.getDecompositionPrototype();
                    
                    if( gateway != null && ! decomps.contains( gateway ) ) {
                        String newGatewayId;
                        if( isTaskANexusTask( task ) ) {
                            if( changeId ) {
                                newGatewayId = newId + NexusWorkflow.NAME_SEPARATOR + task.getID();
                            }
                            else {
                                newGatewayId = gateway.getId();
                            }
                            
                            NexusServiceInfo info = NexusServiceInfo.getServiceWithName(
                                    net.getLocalVariable( task.getID() + NexusWorkflow.NAME_SEPARATOR +
                                            NexusWorkflow.SERVICENAME_VAR ).getInitialValue() );
                            for( int index = 0; index < info.getVariableNames().length; index++ ) {
                                // for each variable just remove the old mappings and put new ones in
                                task.getDataMappingsForTaskStartingSet().remove(
                                        info.getVariableNames()[ index ] );
                                for( Iterator<KeyValue> iter =
                                    task.getDataMappingsForTaskCompletionSet().iterator(); iter.hasNext(); ) {
                                    if( iter.next().getValue().equals(
                                            task.getID() + NexusWorkflow.NAME_SEPARATOR +
                                            info.getVariableNames()[ index ] ) ) {
                                        iter.remove();
                                    }
                                }
                                
                                String varName = info.getVariableNames()[ index ];
                                task.setDataBindingForInputParam(createInputBindingString(newId,
                                        varName, task.getID() + NexusWorkflow.NAME_SEPARATOR + varName),
                                        varName);
                                task.setDataBindingForOutputExpression(createOutputBindingString(
                                        newId, task.getID(), task.getID() + NexusWorkflow.NAME_SEPARATOR
                                                + varName, varName), task.getID()
                                        + NexusWorkflow.NAME_SEPARATOR + varName);
                            }
                        }
                        else {
                            newGatewayId = getAvailableDecompID( target, gateway.getId() );
                        }
                        
                        detachDecompositionFromSpec( gateway );
                        decomps.add( gateway );
                        gateway.setIdAndSpecification( newGatewayId, target );
                        gateway.setParent( null );
                    }
                }
                else if( element instanceof YCompositeTask ) {
                    YCompositeTask task = (YCompositeTask) element;
                    task.setDecompositionPrototype( null );
                }
            }
        }
        
        return decomps;
    }
    
    /**
     * @return a non-colliding ID (in the context of the containing spec) by appending
     *         or altering a number at the end of the id.
     */
    public static String getAvailableDecompID(YSpecification spec, String originalID) {
        HashBag<String, NameAndCounter> bag = new HashBag<String, NameAndCounter>();
        NameAndCounter id = new NameAndCounter( originalID );
        String result;
        
        for( YDecomposition element : spec._decompositions ) {
            NameAndCounter n = new NameAndCounter( element.getId() );
            bag.put( n.getStrippedName(), n );
        }
        
        // first try to use the unaltered ID
        if( spec.getDecomposition( id.toString() ) == null ) {
            result = id.toString();
        }
        // second try to use the ID stripped down (IE: copying from somewhere where the
        // counter was needed to somewhere where it isn't needed)
        else if( bag.get( id.getStrippedName() ) == null ) {
            result = id.getStrippedName();
        }
        // third try to find the next available name and counter pair
        else {
            int num = 2;
            if( bag.containsKey( id.getStrippedName() ) )
                num = bag.get( id.getStrippedName() ).size() + 1;
            id = new NameAndCounter( id.getStrippedName(), Integer.valueOf( num ) );
            
            while( spec.getDecomposition( id.toString() ) != null ) {
                id = id.getNextNameAndCounter();
            }
            
            result = id.toString();
        }
        
        return result;
    }
    
    /**
     * Takes a name and splits it into the actual name and the counter, for
     * example "My Flow [2]" -> {"My Flow", 2}, or "My Flow" -> {"My Flow", 0}.
     */
    private static class NameAndCounter {
        private String _strippedName;
        private Integer _counter;
        /**
         * Constructor that splits the name into the actual name and counter.
         * @param origName the original name.
         */
        private NameAndCounter( String origName ) {
            // match the string from the last underscore to the end
            Matcher m = Pattern.compile( "_[1-9][0-9]*" ).matcher(
                    ( origName.indexOf( "_" ) >= 0 ) ?
                    origName.substring( origName.lastIndexOf( "_" ) ) :
                    origName );
            if( m.matches() ) {
                // if the last part is a number, split it
                _strippedName = origName.substring( 0, origName.lastIndexOf( "_" ) );
                _counter = Integer.valueOf( origName.substring( origName.lastIndexOf( "_" ) + 1 ) );
            }
            else {
                // if the last part is a number, then it's not a name/counter pair
                _strippedName = origName;
                _counter = null;
            }
        }//NameAndCounter()
        private NameAndCounter( String namePart, Integer counterPart ) {
            _strippedName = namePart;
            _counter = counterPart;
        }
        private String getStrippedName() {
            return _strippedName;
        }//getName()
        private NameAndCounter getNextNameAndCounter() {
            if( _counter != null ) {
                return new NameAndCounter( _strippedName, Integer.valueOf( _counter.intValue() + 1 ) );
            }
            else {
                return new NameAndCounter( _strippedName, Integer.valueOf( 2 ) );
            }
        }
        /**
         * @see Object#toString()
         */
        public String toString() {
            if( _counter != null ) {
                return _strippedName + "_" + _counter.toString();
            }
            else {
                return _strippedName;
            }
        }//toString()
    }//NameAndCounter

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
			retval.setDataBindingForInputParam(createInputBindingString(net.getId(),
					varName, taskID + NexusWorkflow.NAME_SEPARATOR + varName),
					varName);
			retval.setDataBindingForOutputExpression(createOutputBindingString(
					net.getId(), taskID, taskID + NexusWorkflow.NAME_SEPARATOR
							+ varName, varName), taskID
					+ NexusWorkflow.NAME_SEPARATOR + varName);
		}
		retval.setDataBindingForInputParam(createInputBindingString(net.getId(),
				NexusWorkflow.SERVICENAME_VAR, taskID
						+ NexusWorkflow.NAME_SEPARATOR
						+ NexusWorkflow.SERVICENAME_VAR),
				NexusWorkflow.SERVICENAME_VAR);
		retval.setDataBindingForOutputExpression(createOutputBindingString(net.getId(),
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
	public static String createInputBindingString(String netID, String elementName,
			String variableName) {
		return "<" + elementName + ">" + "{" + "/" + netID + "/"
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
	public static String createOutputBindingString(String netID, String taskID,
			String elementName, String variableName) {
		return "<" + elementName + ">" + "{" + "/" + netID
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

		targetTask.setDataBindingForInputParam(createInputBindingString(net.getId(),
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
    
    /**
     * A relatively exhaustive task that makes sure a particular task is a nexus workflow task.
     */
    public static boolean isTaskANexusTask( YAtomicTask task ) {
        String taskId = task.getID();
        if( task.getParent() != null && task.getDecompositionPrototype() != null ) {
            try {
                YNet parent = task.getParent();
                YDecomposition decomp = task.getDecompositionPrototype();
                boolean isNexus = true;
                String parentId = parent.getId();
                String decompId = decomp.getId();
                isNexus = isNexus && decompId.equals( parentId + NexusWorkflow.NAME_SEPARATOR + taskId );
                String serviceName = parent.getLocalVariable( taskId +
                        NexusWorkflow.NAME_SEPARATOR + NexusWorkflow.SERVICENAME_VAR ).getInitialValue();
                NexusServiceInfo info = NexusServiceInfo.getServiceWithName( serviceName );
                
                for( int index = 0; index < info.getVariableNames().length; index++ ) {
                    String var = info.getVariableNames()[ index ];
                    isNexus = isNexus && parent.getLocalVariable(
                            taskId + NexusWorkflow.NAME_SEPARATOR + var ) != null;
                    isNexus = isNexus && task.getDataMappingsForTaskStarting().get( var ) != null;
                    isNexus = isNexus && task.getDataMappingsForTaskCompletion().get( var ) != null;
                    isNexus = isNexus && decomp.getInputParameterNames().contains( var );
                    isNexus = isNexus && decomp.getOutputParamNames().contains( var );
                }
                
                return isNexus;
            }
            catch( NullPointerException e ) {
                // null pointers happen when it tries to acces net variables that don't exist,
                // so it's not a nexus task
            }
        }
        return false;
    }
    
    public static boolean isGatewayANexusGateway( YAWLServiceGateway gateway ) {
        return gateway.getYawlService() != null &&
            gateway.getYawlService().getYawlServiceID() != null &&
            gateway.getYawlService().getYawlServiceID().indexOf( "/NexusServiceInvoker" ) >= 0;
    }
}
