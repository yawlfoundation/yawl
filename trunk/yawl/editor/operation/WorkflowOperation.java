/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package operation;

import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
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
import au.edu.qut.yawl.elements.YFlow;
import au.edu.qut.yawl.elements.YInputCondition;
import au.edu.qut.yawl.elements.YNet;
import au.edu.qut.yawl.elements.YOutputCondition;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.elements.YTask;
import au.edu.qut.yawl.elements.data.YParameter;
import au.edu.qut.yawl.elements.data.YVariable;
import au.edu.qut.yawl.util.HashBag;

import com.nexusbpm.NexusWorkflow;
import com.nexusbpm.editor.persistence.YTaskEditorExtension;
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
    
    public static String convertNameToID( String name ) {
        return name.replaceAll( " ", "_" );
    }
    
    /**
     * Creates a workflow under the given parent "folder."
     * @param parent the URI for the parent "folder" where the specification will be stored.
     * @param name the name to give the specification.
     * @return the newly created specification.
     * @throws Exception if the proxy to the parent cannot be converted into a path (shouldn't
     *                   happen).
     */
    public static YSpecification createSpecification(String parentURI, String name, String id)
    throws Exception {
        YSpecification spec = null;
        
        URI u = new URI( parentURI.replaceAll( "\\\\", "/" ) );
        spec = new YSpecification(
                new URI(
                        u.getScheme(),
                        u.getAuthority(),
                        u.getPath() + "/" + convertNameToID( id ),
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
    public static YNet createNet( String name, String id, YSpecification spec ) {
        YNet net = null;
        
        id = getAvailableDecompID( spec, convertNameToID( id ) );
        
        // the constructor throws a null pointer exception if you don't have a specification
        net = new YNet( id, getTempSpec() );
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
    public static void attachDecompositionToSpec( YDecomposition decomposition, YSpecification spec ) {
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
    
    public static YCondition createCondition(YNet net, String label) {
        return new YCondition( getAvailableNetElementID( net, "Condition" ), label, null );
    }
    
    public static void attachNetElementToNet( YExternalNetElement element, YNet net ) {
        element.setParent( net );
        // TODO net.addNetElement() would be sufficient... the other functions need refactoring
        if( element instanceof YInputCondition ) {
            net.setInputCondition( (YInputCondition) element );
        }
        else if( element instanceof YOutputCondition ) {
            net.setOutputCondition( (YOutputCondition) element );
        }
        else {
            net.addNetElement( element );
        }
    }
    
    public static void detachNetElementFromNet( YExternalNetElement element ) {
        YNet net = element.getParent();
        
        net.removeNetElement( element );
        element.setParent( null );
    }
    
    public static YFlow createFlow() {
        return new YFlow( null, null );
    }
    
    public static void attachFlowToElements( YFlow flow,
            YExternalNetElement source, YExternalNetElement target ) {
        flow.setPriorElement( source );
        flow.getPriorElement().getPostsetFlows().add( flow );
        flow.setNextElement( target );
        flow.getNextElement().getPresetFlows().add( flow );
    }
    
    public static void detachFlowFromElements( YFlow flow ) {
        if( flow.getPriorElement() != null ) {
            flow.getPriorElement().removePostsetFlow( flow );
            flow.setPriorElement( null );
        }
        if( flow.getNextElement() != null ) {
            flow.getNextElement().removePresetFlow( flow );
            flow.setNextElement( null );
        }
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
                                        varName, task.getID(), varName),
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
     * Copies a valid Nexus Service task. The original task's decomposition will
     * also be copied and the copy of the YAWL service gateway will be set as
     * the task's decomposition. The parameter <tt>vars</tt> must not be null
     * because it will be populated with local variables that need to be attached
     * to the <tt>targetNet</tt> when the task and gateway are attached.
     * @param sourceTask the original task to make a copy of.
     * @param targetNet the net that will contain the copy of the task
     * @param location the location in the net to set for the task (optional)
     * @param vars a collection that will be populated with local variables to
     *             be connected to the net.
     * @return the copy of the task.
     */
    public static YAtomicTask copyTask(
            YAtomicTask sourceTask, YNet targetNet, Point location,
            List<YVariable> vars ) {
        assert vars != null : "variable list must not be null";
        
        YSpecification targetSpec = targetNet.getParent();
        
        String taskID = WorkflowOperation.getAvailableNetElementID( targetNet, sourceTask.getID() );
        
        YAWLServiceGateway gateway = WorkflowOperation.createGateway( taskID, targetNet );
        YAtomicTask task = WorkflowOperation.createTask( taskID, sourceTask.getName(), targetNet, gateway );
        
        Set<String> names = new HashSet<String>();
        
        for( String name : sourceTask.getDecompositionPrototype().getInputParameterNames() ) {
            names.add( name );
            YParameter iparam = new YParameter( gateway, YParameter._INPUT_PARAM_TYPE );
            iparam.setDataTypeAndName( NexusWorkflow.VARTYPE_STRING, name, NexusWorkflow.XML_SCHEMA_URL );
            gateway.setInputParam( iparam );
            task.setDataBindingForInputParam(
                    WorkflowOperation.createInputBindingString(
                            targetNet.getId(),
                            name,
                            taskID,
                            name ), name );
        }
        
        for( String name : sourceTask.getDecompositionPrototype().getOutputParamNames() ) {
            names.add( name );
            YParameter oparam = new YParameter( gateway, YParameter._OUTPUT_PARAM_TYPE );
            oparam.setDataTypeAndName( NexusWorkflow.VARTYPE_STRING, name, NexusWorkflow.XML_SCHEMA_URL );
            gateway.setOutputParameter( oparam );
            task.setDataBindingForOutputExpression(
                    WorkflowOperation.createOutputBindingString(
                            targetNet.getId(),
                            taskID,
                            taskID + NexusWorkflow.NAME_SEPARATOR + name,
                            name ), taskID + NexusWorkflow.NAME_SEPARATOR + name );
        }
        
        for( String name : names ) {
            YVariable var = new YVariable( null );
            var.setDataTypeAndName(
                    NexusWorkflow.VARTYPE_STRING,
                    taskID + NexusWorkflow.NAME_SEPARATOR + name,
                    NexusWorkflow.XML_SCHEMA_URL );
            var.setInitialValue( sourceTask.getParent().getLocalVariable(
                    sourceTask.getID() + NexusWorkflow.NAME_SEPARATOR + name ).getInitialValue() );
            vars.add( var );
        }
        
        if( location != null ) {
            setBoundsOfNetElement( task, new Rectangle2D.Double(
                    location.getX() - 40, location.getY() - 35,
                    80, 70 ) );
        }
        
        return task;
    }
    
    /**
     * Sets the bounds of the given element and returns the old bounds.
     */
    public static Rectangle2D setBoundsOfNetElement( YExternalNetElement element, Rectangle2D bounds ) {
        YTaskEditorExtension editor = new YTaskEditorExtension( element );
        Rectangle2D oldBounds = editor.getBounds();
        editor.setBounds( bounds );
        return oldBounds;
    }
    
    public static Rectangle2D getBoundsOfNetElement( YExternalNetElement element ) {
        YTaskEditorExtension editor = new YTaskEditorExtension( element );
        return editor.getBounds();
    }
    
    public static YSpecification copySpecification( YSpecification original, String targetFolder )
    throws CloneNotSupportedException, URISyntaxException {
        YSpecification clone = null;
        
        clone = (YSpecification) original.clone();
        clone.setDbID( null );
        
        URI desturi = joinUris(new URI(targetFolder), new URI(clone.getID()));
        clone.setID(desturi.toString());
        
        return clone;
    }
    
    private static URI joinUris(URI parent, URI child) {
        URI retval = null;
        String text = child.getRawPath();
        int index = text.lastIndexOf("/") + 1;
        try {
            retval = new URI(parent.getScheme(), parent.getAuthority(), parent.getPath() + "/" + URLDecoder.decode(text.substring(index), "UTF-8"), child.getQuery(), child.getFragment());
            retval = retval.normalize();
        } catch (Exception e) {e.printStackTrace();}
        return retval;
    }
    
    /**
     * @return a non-colliding ID (in the context of the containing spec) by appending
     *         or altering a number at the end of the id.
     */
    public static String getAvailableDecompID(YSpecification spec, String originalID) {
        List<String> ids = new LinkedList<String>();
        for( YDecomposition decomp : spec._decompositions ) {
            ids.add( decomp.getId() );
        }
        return getAvailableID( ids, originalID );
    }
    
    /**
     * @return a non-colliding ID (in the context of the containing net) by appending
     *         or altering a number at the end of the id.
     */
    public static String getAvailableNetElementID(YNet net, String originalID) {
        List<String> ids = new LinkedList<String>();
        for( YExternalNetElement element : net.getNetElements() ) {
            ids.add( element.getID() );
        }
        return getAvailableID( ids, originalID );
    }
    
    /**
     * @return a non-colliding ID by appending or altering a number at the end of the id.
     */
    public static String getAvailableID(Collection<String> ids, String originalID) {
        HashBag<String, NameAndCounter> bag = new HashBag<String, NameAndCounter>();
        NameAndCounter id = new NameAndCounter( originalID );
        String result;
        
        for( String current : ids ) {
            NameAndCounter n = new NameAndCounter( current );
            bag.put( n.getStrippedName(), n );
        }
        
        // first try to use the unaltered ID
        if( ! ids.contains( id.toString() ) ) {
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
            
            while( ids.contains( id.toString() ) ) {
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
	 * mappings from net variables to decomposed gateway variables, and sets
     * the gateway as the task's decomposition.
	 */
	public static YAtomicTask createNexusTask(String taskID, String taskName,
			YNet net, YAWLServiceGateway gateway, NexusServiceInfo serviceInfo) {
		YAtomicTask task = new YAtomicTask( taskID, YTask._AND, YTask._AND, net );
        task.setParent( null );
		task.setName( taskName );
		task.setDecompositionPrototype( gateway );
        
        // create in/out mappings for the specific nexus service
		for (int i = 0; i < serviceInfo.getVariableNames().length; i++) {
			String varName = serviceInfo.getVariableNames()[i];
            
			task.setDataBindingForInputParam(
                    createInputBindingString(
                            net.getId(),
                            varName,
                            taskID,
                            varName ),
                    varName );
			task.setDataBindingForOutputExpression(
                    createOutputBindingString(
                            net.getId(),
                            taskID,
                            taskID + NexusWorkflow.NAME_SEPARATOR + varName,
                            varName),
                    taskID + NexusWorkflow.NAME_SEPARATOR + varName );
		}
        
        // create input and output mappings that appear on all nexus tasks
		task.setDataBindingForInputParam(
                createInputBindingString(
                        net.getId(),
                        NexusWorkflow.SERVICENAME_VAR,
                        taskID,
                        NexusWorkflow.SERVICENAME_VAR ),
                NexusWorkflow.SERVICENAME_VAR );
		task.setDataBindingForOutputExpression(
                createOutputBindingString(
                        net.getId(),
                        taskID,
                        taskID + NexusWorkflow.NAME_SEPARATOR + NexusWorkflow.STATUS_VAR,
                        NexusWorkflow.STATUS_VAR ),
				taskID + NexusWorkflow.NAME_SEPARATOR + NexusWorkflow.STATUS_VAR );
        
		return task;
	}
    
    /**
     * Creates a task in the given net, and sets the gateway as the task's decomposition.
     */
    public static YAtomicTask createTask( String taskID, String taskName, YNet net,
            YAWLServiceGateway gateway ) {
        YAtomicTask task = new YAtomicTask( taskID, YTask._AND, YTask._AND, net );
        task.setParent( null );
        task.setName( taskName );
        task.setDecompositionPrototype( gateway );
        
        return task;
    }

	/**
	 * Creates a Nexus Workflow Gateway for the specified nexus service.
	 */
	public static YAWLServiceGateway createNexusGateway(String taskID, YNet net, NexusServiceInfo serviceInfo) {
		YAWLServiceGateway gateway = new YAWLServiceGateway(
                net.getId() + NexusWorkflow.NAME_SEPARATOR + taskID,
                net.getParent() );
        gateway.setParent( null );
		YAWLServiceReference yawlService = new YAWLServiceReference(
				NexusWorkflow.LOCAL_INVOKER_URI,
                gateway);
		gateway.setYawlService(yawlService);
        
        // create in/out parameters for the specific nexus service
		for( int i = 0; i < serviceInfo.getVariableNames().length; i++ ) {
			YParameter iparam = new YParameter( gateway, YParameter._INPUT_PARAM_TYPE );
			YParameter oparam = new YParameter( gateway, YParameter._OUTPUT_PARAM_TYPE );
            
			iparam.setDataTypeAndName(
                    NexusWorkflow.VARTYPE_STRING,
                    serviceInfo.getVariableNames()[i],
                    NexusWorkflow.XML_SCHEMA_URL );
			oparam.setDataTypeAndName(
                    NexusWorkflow.VARTYPE_STRING,
                    serviceInfo.getVariableNames()[i],
                    NexusWorkflow.XML_SCHEMA_URL );
            
			gateway.setInputParam( iparam );
			gateway.setOutputParameter( oparam );
		}
        
        // create input and output parameters that appear on all nexus gateways
		YParameter iparam = new YParameter( gateway, YParameter._INPUT_PARAM_TYPE );
		YParameter oparam = new YParameter( gateway, YParameter._OUTPUT_PARAM_TYPE );
        
		iparam.setDataTypeAndName(
                NexusWorkflow.VARTYPE_STRING,
				NexusWorkflow.SERVICENAME_VAR,
                NexusWorkflow.XML_SCHEMA_URL );
		oparam.setDataTypeAndName(
                NexusWorkflow.VARTYPE_STRING,
				NexusWorkflow.STATUS_VAR,
                NexusWorkflow.XML_SCHEMA_URL );
        
		gateway.setInputParam( iparam );
		gateway.setOutputParameter( oparam );
        
		return gateway;
	}
    
    /**
     * Creates a Gateway that is intended to be used as a Nexus Workflow gateway
     * for the task with the specified id. No input or output parameters are
     * created.
     */
    public static YAWLServiceGateway createGateway( String taskID, YNet net ) {
        YAWLServiceGateway gateway = new YAWLServiceGateway(
                net.getId() + NexusWorkflow.NAME_SEPARATOR + taskID,
                net.getParent() );
        gateway.setParent( null );
        YAWLServiceReference yawlService = new YAWLServiceReference(
                NexusWorkflow.LOCAL_INVOKER_URI,
                gateway);
        gateway.setYawlService(yawlService);
        
        return gateway;
    }

	/**
	 * Creates net variables for a particular Nexus Service.
	 */
	public static List<YVariable> createNexusVariables(
            String taskID, YNet net, NexusServiceInfo serviceInfo ) {
        List<YVariable> variables = new ArrayList<YVariable>( serviceInfo.getVariableNames().length + 3 );
        
		NexusServiceData data = new NexusServiceData();

		for (int i = 0; i < serviceInfo.getVariableNames().length; i++) {
			String name = serviceInfo.getVariableNames()[i];
			String type = serviceInfo.getVariableTypes()[i];

			data.setType(name, type);

			try {
				data.set(name, serviceInfo.getInitialValues()[i]);
			} catch (IOException e) {
				e.printStackTrace( System.out );
			}
            
            YVariable var = new YVariable( null );
            var.setDataTypeAndName(
                    NexusWorkflow.VARTYPE_STRING,
                    taskID + NexusWorkflow.NAME_SEPARATOR + name,
                    NexusWorkflow.XML_SCHEMA_URL );
            var.setInitialValue( data.getEncodedValue( name ) );
            variables.add( var );
		}
		
		variables.add(
                getStringVariable(
                        taskID,
                        NexusWorkflow.SERVICENAME_VAR,
                        serviceInfo.getServiceName() ) );
		variables.add(
                getStringVariable(
                        taskID,
                        NexusWorkflow.STATUS_VAR,
						null ) );
        return variables;
	}

	/**
	 * Creates a YVariable with a name derived from the
	 * task id, a separator and the given simple var name.  
	 */
	public static YVariable getStringVariable( String taskID, String varName, String initialValue ) {
		YVariable var = new YVariable( null );
        
		var.setDataTypeAndName(
                NexusWorkflow.VARTYPE_STRING,
                taskID + NexusWorkflow.NAME_SEPARATOR + varName,
				NexusWorkflow.XML_SCHEMA_URL );
		if( initialValue != null ) {
			var.setInitialValue( initialValue );
		}
		return var;
	}
    
    public static void attachVariablesToNet( List<YVariable> variables, YNet net ) {
        for( YVariable variable : variables ) {
            variable.setParent( net );
            net.getLocalVariables().add( variable );
        }
    }
    
    public static void detachVariablesFromNet( List<YVariable> variables ) {
        for( YVariable variable : variables ) {
            ((YNet) variable.getParent()).getLocalVariables().remove( variable );
            variable.setParent( null );
        }
    }
    
    public static void detachVariablesFromNet( YNet net, List<String> varNames ) {
        for( String name : varNames ) {
            if( net.getLocalVariable( name ) != null ) {
                net.getLocalVariables().remove( net.getLocalVariable( name ) );
            }
        }
    }

	/**
	 * Creates a default input binding string useful for mapping net variables
	 * to gateways
	 * 
	 * @param net
	 * @param elementName
     * @param taskID
	 * @param variableName
	 * @return
	 */
	public static String createInputBindingString(String netID, String elementName,
			String taskID, String variableName) {
		return "<" + elementName + ">" + "{" + "/" + netID + "/" +
				taskID + NexusWorkflow.NAME_SEPARATOR + variableName + "/text()" + "}" +
                "</" + elementName + ">";
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
		return "<" + elementName + ">" + "{" + "/" +
                netID + NexusWorkflow.NAME_SEPARATOR + taskID + "/" + variableName
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

		targetTask.setDataBindingForInputParam(
                createInputBindingString(
                        net.getId(),
                        targetvarName,
                        sourceTaskId,
                        sourcevarName),
                targetvarName);
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
        NexusServiceInfo info = getNexusServiceInfoForTask( task );
        
        if( info != null ) {
            boolean isNexus = true;
            
            YNet parent = task.getParent();
            YDecomposition decomp = task.getDecompositionPrototype();
            
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
        
        return false;
    }
    
    public static NexusServiceInfo getNexusServiceInfoForTask( YAtomicTask task ) {
        String taskId = task.getID();
        if( task.getParent() != null && task.getDecompositionPrototype() != null ) {
            YNet parent = task.getParent();
            YDecomposition decomp = task.getDecompositionPrototype();
            
            String parentId = parent.getId();
            String decompId = decomp.getId();
            
            if( decompId.equals( parentId + NexusWorkflow.NAME_SEPARATOR + taskId ) ) {
                YVariable serviceNameVar = parent.getLocalVariable( taskId +
                        NexusWorkflow.NAME_SEPARATOR + NexusWorkflow.SERVICENAME_VAR );
                if( serviceNameVar != null &&
                        serviceNameVar.getInitialValue() != null ) {
                    return NexusServiceInfo.getServiceWithName( serviceNameVar.getInitialValue() );
                }
            }
        }
        return null;
    }
    
    public static boolean isGatewayANexusGateway( YAWLServiceGateway gateway ) {
        return gateway.getYawlService() != null &&
            gateway.getYawlService().getYawlServiceID() != null &&
            gateway.getYawlService().getYawlServiceID().indexOf( "/NexusServiceInvoker" ) >= 0;
    }
}
