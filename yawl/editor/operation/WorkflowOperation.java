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
     * @throws URISyntaxException if the parentURI and id can't be combined into a new URI.
     */
    public static YSpecification createSpecification(String parentURI, String name, String id)
    throws URISyntaxException {
        YSpecification spec = null;
        
        URI u = new URI( parentURI.replaceAll( "\\\\", "/" ) );
        spec = createSpecification( name,
                new URI(
                        u.getScheme(),
                        u.getAuthority(),
                        u.getPath() + "/" + convertNameToID( id ),
                        null,
                        null ).toString() );
        
        return spec;
    }
    
    public static YSpecification createSpecification( String name, String id ) {
        YSpecification spec = null;
        
        spec = new YSpecification( id );
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
        flow.setNextElement( target );
        flow.getPriorElement().getPostsetFlows().add( flow );
        flow.getNextElement().getPresetFlows().add( flow );
    }
    
    public static void detachFlowFromElements( YFlow flow ) {
        if( flow.getPriorElement() != null ) {
            flow.getPriorElement().removePostsetFlow( flow );
        }
        if( flow.getNextElement() != null ) {
            flow.getNextElement().removePresetFlow( flow );
        }
        flow.setPriorElement( null );
        flow.setNextElement( null );
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
    
    public static YSpecification copySpecification( YSpecification original, String targetID )
    throws CloneNotSupportedException, URISyntaxException {
        YSpecification clone = null;
        
        clone = original.deepClone();
        clone.setDbID( null );
        clone.setID( targetID );
        
        return clone;
    }
    
    public static String joinURIs( String parentURI, String childURI ) {
        try {
            return joinURIs( new URI( parentURI ), new URI( childURI ) ).toString();
        }
        catch( URISyntaxException e ) {
            throw new RuntimeException( e );
        }
    }
    
    public static URI joinURIs( URI parent, URI child ) {
        URI retval = null;
        String text = child.getRawPath();
        int index = text.lastIndexOf("/") + 1;
        try {
            retval = new URI(
                    parent.getScheme(),
                    parent.getAuthority(),
                    parent.getPath() + "/" + URLDecoder.decode(
                            text.substring(index),
                            "UTF-8"),
                    child.getQuery(),
                    child.getFragment() );
            retval = retval.normalize();
        } catch (Exception e) { throw new RuntimeException( e ); }
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
            id = new NameAndCounter( id.getStrippedName(), Integer.valueOf( num ), id.getExtension() );
            
            while( ids.contains( id.toString() ) ) {
                id = id.getNextNameAndCounter();
            }
            
            result = id.toString();
        }
        
        return result;
    }
    
    /**
     * <p>Takes a name and splits it into the actual name and the counter, for
     * example "My_Flow_2" -&gt; {"My_Flow", 2, null}. Also splits a name and extension
     * (if the extension is 4 or fewer characters), for example "My_Flow.xml"
     * -&gt; {"My_Flow", null, ".xml"} and "My_Flow_2.xml" -&gt; {"My_Flow", 2, ".xml"}.</p>
     * <p>The stripped name will never be null, but the counter and
     * extension may be null if they weren't matched.</p>
     * <strong>note:</strong> If there is a counter, then the stripped name <em>can</em> be
     * the empty string. If there is no counter, then the stripped name <em>cannot</em> be
     * the empty string (unless the entire original name is just the empty string). In other
     * words, names that start with a dot (such as ".xml") will not be considered to be just
     * an empty string and an extension, the first dot will be considered part of the
     * stripped name.
     */
    public static class NameAndCounter {
        private String _strippedName;
        private Integer _counter;
        private String _extension;
        /**
         * Constructor that splits the name into the actual name and counter.
         * @param origName the original name.
         */
        public NameAndCounter( String origName ) {
            // match the whole string
            Matcher m = Pattern.compile( "(.*)(_[1-9][0-9]*)(\\..{0,4})?" ).matcher( origName );
            Matcher m2 = Pattern.compile( "(.+?)(\\..{0,4})?" ).matcher( origName );
            if( m.matches() ) {
                // if the last part (before the extension, if any) is a number, split it
                _strippedName = m.group( 1 );
                _counter = Integer.valueOf( m.group( 2 ).substring( 1 ) );
                _extension = m.group( 3 );
            }
            else if( m2.matches() ) {
                // if the backup matcher matches it, use that one to split it
                _strippedName = m2.group( 1 );
                _counter = null;
                _extension = m2.group( 2 );
            }
            else {
                // otherwise if it can't be matched, just consider it to all be part of the name
                _strippedName = origName;
                _counter = null;
                _extension = null;
            }
        }//NameAndCounter()
        public NameAndCounter( String namePart, Integer counterPart, String extension ) {
            _strippedName = namePart;
            _counter = counterPart;
            _extension = extension;
        }
        public String getStrippedName() {
            return _strippedName;
        }//getName()
        public Integer getCounter() {
            return _counter;
        }
        public String getExtension() {
            return _extension;
        }
        public NameAndCounter getNextNameAndCounter() {
            if( _counter != null ) {
                return new NameAndCounter(
                        _strippedName,
                        Integer.valueOf( _counter.intValue() + 1 ),
                        _extension );
            }
            else {
                return new NameAndCounter( _strippedName, Integer.valueOf( 2 ), _extension );
            }
        }
        /**
         * @see Object#toString()
         */
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append( _strippedName );
            if( _counter != null ) {
                builder.append( "_" );
                builder.append( _counter.toString() );
            }
            if( _extension != null ) {
                builder.append( _extension );
            }
            
            return builder.toString();
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
                createStringVariable(
                        taskID,
                        NexusWorkflow.SERVICENAME_VAR,
                        serviceInfo.getServiceName() ) );
		variables.add(
                createStringVariable(
                        taskID,
                        NexusWorkflow.STATUS_VAR,
						null ) );
        return variables;
	}

	/**
	 * Creates a YVariable with a name derived from the
	 * task id, a separator and the given simple var name.  
	 */
	public static YVariable createStringVariable( String taskID, String varName, String initialValue ) {
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
    
    public static void attachVariablesToNet( Collection<YVariable> variables, YNet net ) {
        for( YVariable variable : variables ) {
            variable.setParent( net );
            net.getLocalVariables().add( variable );
        }
    }
    
    public static void detachVariablesFromNet( Collection<YVariable> variables ) {
        for( YVariable variable : variables ) {
            ((YNet) variable.getParent()).getLocalVariables().remove( variable );
            variable.setParent( null );
        }
    }
    
    public static void detachVariablesFromNet( YNet net, Collection<String> varNames ) {
        for( String name : varNames ) {
            if( net.getLocalVariable( name ) != null ) {
                net.getLocalVariables().remove( net.getLocalVariable( name ) );
            }
        }
    }
    
    /**
     * @param type one of the constants in the YParameter class (ie: input param, output param)
     */
    public static YParameter createStringParameter( String name, int type ) {
        YParameter param = new YParameter( null, type );
        param.setDataTypeAndName( NexusWorkflow.VARTYPE_STRING, name, NexusWorkflow.XML_SCHEMA_URL );
        return param;
    }
    
    public static void attachParametersToDecomposition( Collection<YParameter> params, YDecomposition decomp ) {
        for( YParameter param : params ) {
        	param.setDecomposition(  decomp  );
            if( param.isInput() ) {
                decomp.getInputParameters().add( param );
            }
            else if( param.isOutput() ) {
                decomp.getOutputParameters().add( param );
            }
            else if( param.isEnablement() ) {
                // TODO ????
            }
        }
    }
    
    public static void detachParametersFromDecomposition( Collection<YParameter> params ) {
        for( YParameter param : params ) {
            YDecomposition parent = param.getParent();
            if( param.isInput() ) {
                parent.getInputParameters().remove( param );
            }
            else if( param.isOutput() ) {
                parent.getOutputParameters().remove( param );
            }
            else if( param.isEnablement() ) {
                // TODO ????
            }
        }
    }
    
    public static class VariableMapping {
        public String elementID;
        public String netID;
        public String taskID;
        public String variableName;
        public VariableMapping( String mapping ) {
            // regular expression for:
            // <elementID>{/netID/taskID__variableName/text()}</elementID>
            // allowing whitespace in appropriate places.
            // note: this can fail if taskID or variableName includes a double underscore...
            Matcher m = Pattern.compile(
                    "(\\s)*<([^>]+)>(\\s)*\\{(\\s)*/([^/]+)/([^/]+)__([^/]+)/text\\(\\)(\\s)*\\}(\\s)*</\\2>(\\s*)" )
                    .matcher( mapping );
            if( m.matches() ) {
                assert m.groupCount() == 10 : "group count was " + m.groupCount();
                elementID = m.group( 2 );
                netID = m.group( 5 );
                taskID = m.group( 6 );
                variableName = m.group( 7 );
            }
            else {
                throw new IllegalArgumentException( "The mapping '" + mapping +
                        "' is not a nexus service generated mapping!" );
            }
        }//NameAndCounter()
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
