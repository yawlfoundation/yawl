/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package au.edu.qut.yawl.elements;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;
import au.edu.qut.yawl.elements.data.YParameter;
import au.edu.qut.yawl.elements.data.YVariable;

/**
 * @author Nathan Rose
 */
public class TestDeepClone extends TestCase {
    private YSpecification specification;

    public TestDeepClone( String name ) {
        super( name );
    }

    public void setUp() {
        specification = new YSpecification( "uri" );
        specification.setBetaVersion( YSpecification._Beta7_1 );
        
        YAWLServiceGateway gateway1 = new YAWLServiceGateway( "gateway_one", specification );
        specification.getDecompositions().add( gateway1 );
        gateway1.setYawlService( new YAWLServiceReference(
                "http://localhost:8080/NexusServiceInvoker/", gateway1 ) );
        YParameter param1 = new YParameter( gateway1, YParameter._INPUT_PARAM_TYPE );
        param1.setDataTypeAndName( "String", "foo", null );
        gateway1.setInputParam( param1 );
        
        YAWLServiceGateway gateway2 = new YAWLServiceGateway( "gateway_two", specification );
        specification.getDecompositions().add( gateway2 );
        gateway2.setYawlService( new YAWLServiceReference(
                "http://localhost:8080/NexusServiceInvoker/", gateway2 ) );
        YParameter param2 = new YParameter( gateway2, YParameter._OUTPUT_PARAM_TYPE );
        param2.setDataTypeAndName( "String", "bar", null );
        gateway2.setOutputParameter( param2 );
        
        YNet rootNet = new YNet( "root", specification );
        specification.setRootNet( rootNet );
        
        YNet net = new YNet( "net", specification );
        specification.getDecompositions().add( net );
        
        YInputCondition input = new YInputCondition( "input", rootNet );
        rootNet.setInputCondition( input );
        
        YOutputCondition output = new YOutputCondition( "output", rootNet );
        rootNet.setOutputCondition( output );
        
        YAtomicTask task1 = createAtomicTask( "task1", rootNet, gateway1 );
        task1.setDataBindingForInputParam( "<foo>blah</foo>", "foo" );
        
        YCompositeTask task2 = new YCompositeTask( "task2", YTask._AND, YTask._AND, rootNet );
        rootNet.getNetElements().add( task2 );
        task2.setDecompositionPrototype( net );
        
        YAtomicTask task3 = createAtomicTask( "task3", rootNet, null );
        
        YAtomicTask task4 = createAtomicTask( "task4", rootNet, gateway2 );
        
        YAtomicTask task5 = createAtomicTask( "task5", net, gateway2 );
        
        YOutputCondition output2 = new YOutputCondition( "output2", net );
        net.setOutputCondition( output2 );
        
        YInputCondition input2 = new YInputCondition( "input2", net );
        net.setInputCondition( input2 );
        
        createFlow( input, task1 );
        createFlow( task1, task2 );
        createFlow( task1, task3 );
        createFlow( task2, task4 );
        createFlow( task3, task4 );
        
        createFlow( input2, task5 );
        createFlow( task5, output2 );
    }
    
    private void createFlow( YExternalNetElement e1, YExternalNetElement e2 ) {
        YFlow flow = new YFlow( e1, e2 );
        e1.getPostsetFlows().add( flow );
        e2.getPresetFlows().add( flow );
    }
    
    private YAtomicTask createAtomicTask( String id, YNet net, YDecomposition decomposition ) {
        YAtomicTask task = new YAtomicTask( id, YTask._AND, YTask._AND, net );
        net.getNetElements().add( task );
        task.setDecompositionPrototype( decomposition );
        return task;
    }

    public void testCloneAccuracy() {
        YSpecification clone = specification.deepClone();
        
        assertNotNull( clone );
        
        assertNotNull( clone.getID() );
        assertTrue( "orig id:" + specification.getID() + "\nclone id:" + clone.getID(),
                clone.getID().equals( specification.getID() ) );
        
        assertNotNull( clone.getBetaVersion() );
        assertTrue(
                "orig version:" + specification.getBetaVersion() +
                "\nclone version:" + clone.getBetaVersion(),
                clone.getBetaVersion().equals( specification.getBetaVersion() ) );
        
        assertNotNull( clone.getDecompositions() );
        
        assertTrue(
                "orig decomps:" + specification.getDecompositions().size() +
                "\nclone decomps:" + clone.getDecompositions().size(),
                specification.getDecompositions().size() == clone.getDecompositions().size() );
        
        for( YDecomposition decomp : specification.getDecompositions() ) {
            YDecomposition cloneDecomp = clone.getDecomposition( decomp.getId() );
            
            assertNotNull( cloneDecomp );
            assertNotNull( cloneDecomp.getParent() );
            
            assertNotNull( cloneDecomp.getId() );
            assertTrue( decomp.getId() + "\n" + cloneDecomp.getId(),
                    decomp.getId().equals( cloneDecomp.getId() ) );
            
            if( decomp.getName() != null ) {
                assertNotNull( cloneDecomp.getName() );
                assertTrue( decomp.getName() + "\n" + cloneDecomp.getName(),
                        decomp.getName().equals( cloneDecomp.getName() ) );
            }
            else {
                assertNull( cloneDecomp.getName() );
            }
            
            if( decomp.getDocumentation() != null ) {
                assertNotNull( cloneDecomp.getDocumentation() );
                assertTrue( decomp.getDocumentation() + "\n--------\n" + cloneDecomp.getDocumentation(),
                        decomp.getDocumentation().equals( cloneDecomp.getDocumentation() ) );
            }
            else {
                assertNull( cloneDecomp.getDocumentation() );
            }
            
            assertNotNull( cloneDecomp.getInputParameters() );
            assertTrue( decomp.getInputParameters().size() + " : " + cloneDecomp.getInputParameters().size(),
                    decomp.getInputParameters().size() == cloneDecomp.getInputParameters().size() );
            
            for( YParameter param : decomp.getInputParameters() ) {
                YParameter cloneParam =
                    getParameterNamed( param.getName(), cloneDecomp.getInputParameters() );
                
                compareParameters( param, cloneParam );
            }
            
            assertNotNull( cloneDecomp.getOutputParameters() );
            assertTrue( decomp.getOutputParameters().size() + " : " + cloneDecomp.getOutputParameters().size(),
                    decomp.getOutputParameters().size() == cloneDecomp.getOutputParameters().size() );
            
            for( YParameter param : decomp.getOutputParameters() ) {
                YParameter cloneParam =
                    getParameterNamed( param.getName(), cloneDecomp.getOutputParameters() );
                
                compareParameters( param, cloneParam );
            }
            
            if( decomp instanceof YAWLServiceGateway ) {
                compareGateways( (YAWLServiceGateway) decomp, (YAWLServiceGateway) cloneDecomp );
            }
            else if( decomp instanceof YNet ) {
                compareNets( (YNet) decomp, (YNet) cloneDecomp );
            }
        }
    }
    
    private void compareParameters( YParameter param, YParameter cloneParam ) {
        compareVariables( param, cloneParam );
        
        assertTrue( "" + param.isMandatory(), param.isMandatory() == cloneParam.isMandatory() );
    }
    
    private void compareVariables( YVariable var, YVariable cloneVar ) {
        assertNotNull( cloneVar );
        assertNotNull( cloneVar.getDecomposition() );
        
        assertNotNull( cloneVar.getDataTypeName() );
        assertTrue( var.getDataTypeName() + "\n" + cloneVar.getDataTypeName(),
                var.getDataTypeName().equals( cloneVar.getDataTypeName() ) );
        
        if( var.getInitialValue() != null ) {
            assertNotNull( cloneVar.getInitialValue() );
            assertTrue( var.getInitialValue() + "\n-----\n" + cloneVar.getInitialValue(),
                    var.getInitialValue().equals( cloneVar.getInitialValue() ) );
        }
        else {
            assertNull( cloneVar.getInitialValue() );
        }
        
        if( var.getDataTypeNameSpace() != null ) {
            assertNotNull( cloneVar.getDataTypeNameSpace() );
            assertTrue( var.getDataTypeNameSpace() + "\n" + cloneVar.getDataTypeNameSpace(),
                    var.getDataTypeNameSpace().equals( cloneVar.getDataTypeNameSpace() ) );
        }
        else {
            assertNull( cloneVar.getDataTypeNameSpace() );
        }
        
        if( var.getDocumentation() != null ) {
            assertNotNull( cloneVar.getDocumentation() );
            assertTrue( var.getDocumentation() + "\n-----\n" + cloneVar.getDocumentation(),
                    var.getDocumentation().equals( cloneVar.getDocumentation() ) );
        }
        else {
            assertNull( cloneVar.getDocumentation() );
        }
    }
    
    private YVariable getVariableNamed( String name, List<YVariable> variables ) {
        for( YVariable var : variables ) {
            assertNotNull( var );
            assertNotNull( "" + var, var.getName() );
            if( var.getName().equals( name ) )
                return var;
        }
        return null;
    }
    
    private YParameter getParameterNamed( String name, List<YParameter> params ) {
        for( YParameter param : params ) {
            assertNotNull( param );
            assertNotNull( "" + param, param.getName() );
            if( param.getName().equals( name ) ) {
                return param;
            }
        }
        return null;
    }
    
    private void compareGateways( YAWLServiceGateway orig, YAWLServiceGateway clone ) {
        assertTrue( orig.getEnablementParameters().size() + " : " + clone.getEnablementParameters().size(),
                orig.getEnablementParameters().size() == clone.getEnablementParameters().size() );
        for( YParameter param : orig.getEnablementParameters() ) {
            YParameter cloneParam =
                getParameterNamed( param.getName(), clone.getEnablementParameters() );
            
            compareParameters( param, cloneParam );
        }
        
        if( orig.getYawlService() != null ) {
            assertNotNull( clone.getYawlService() );
            
            YAWLServiceReference os = orig.getYawlService();
            YAWLServiceReference cs = clone.getYawlService();
            
            assertNotNull( cs.getYawlServiceGateway() );
            
            if( os.getDocumentation() != null ) {
                assertNotNull( cs.getDocumentation() );
                assertTrue( os.getDocumentation() + "\n" + cs.getDocumentation(),
                        os.getDocumentation().equals( cs.getDocumentation() ) );
            }
            else {
                assertNull( cs.getDocumentation() );
            }
            
            if( os.getYawlServiceID() != null ) {
                assertNotNull( cs.getYawlServiceID() );
                assertTrue( os.getYawlServiceID() + "\n" + cs.getYawlServiceID(),
                        os.getYawlServiceID().equals( cs.getYawlServiceID() ) );
            }
            else {
                assertNull( cs.getYawlServiceID() );
            }
        }
        else {
            assertNull( clone.getYawlService() );
        }
    }
    
    private void compareNets( YNet net, YNet clone ) {
        assertNotNull( clone.getLocalVariables() );
        assertTrue( net.getLocalVariables().size() + " : " + clone.getLocalVariables().size(),
                net.getLocalVariables().size() == clone.getLocalVariables().size() );
        
        for( YVariable variable : net.getLocalVariables() ) {
            YVariable cloneVar =
                getVariableNamed( variable.getName(), clone.getLocalVariables() );
            
            compareVariables( variable, cloneVar );
        }
        
        assertNotNull( clone.getRootNet() );
        assertTrue( net.getRootNet(), net.getRootNet().equals( clone.getRootNet() ) );
        
        if( net.getInputCondition() == null ) {
            assertNull( clone.getInputCondition() );
        }
        else {
            assertNotNull( clone.getInputCondition() );
        }
        
        if( net.getOutputCondition() == null ) {
            assertNull( clone.getOutputCondition() );
        }
        else {
            assertNotNull( clone.getOutputCondition() );
        }
        
        assertNotNull( clone.getNetElements() );
        assertTrue( net.getNetElements().size() + " : " + clone.getNetElements().size(),
                net.getNetElements().size() == clone.getNetElements().size() );
        
        for( YExternalNetElement element : net.getNetElements() ) {
            YExternalNetElement cloneElement = clone.getNetElement( element.getID() );
            compareNetElements( element, cloneElement );
        }
    }
    
    private void compareNetElements( YExternalNetElement element, YExternalNetElement cloneElement ) {
        assertNotNull( cloneElement );
        assertNotNull( cloneElement.getID() );
        
        assertNotNull( cloneElement.getParent() );
        
        if( element.getDocumentation() != null ) {
            assertNotNull( cloneElement.getDocumentation() );
            assertTrue( element.getDocumentation() + "\n-----\n" + cloneElement.getDocumentation(),
                    element.getDocumentation().equals( cloneElement.getDocumentation() ) );
        }
        else {
            assertNull( cloneElement.getDocumentation() );
        }
        
        if( element.getName() != null ) {
            assertNotNull( cloneElement.getName() );
            assertTrue( element.getName() + "\n" + cloneElement.getName(),
                    element.getName().equals( cloneElement.getName() ) );
        }
        else {
            assertNull( cloneElement.getName() );
        }
        
        assertNotNull( cloneElement.getPostsetFlows() );
        assertNotNull( cloneElement.getPresetFlows() );
        
        assertTrue( element.getPostsetFlows().size() + " : " + cloneElement.getPostsetFlows().size(),
                element.getPostsetFlows().size() == cloneElement.getPostsetFlows().size() );
        assertTrue( element.getID() + " " + element.getName() + "  " +
                cloneElement.getID() + " " + cloneElement.getName() + "\n" +
                element.getPresetFlows().size() + " : " + cloneElement.getPresetFlows().size(),
                element.getPresetFlows().size() == cloneElement.getPresetFlows().size() );
        
        Set<String> names = new HashSet<String>();
        Set<String> cloneNames = new HashSet<String>();
        
        for( YFlow flow : element.getPostsetFlows() ) {
            names.add( flow.getPriorElement().getID() );
            names.add( flow.getNextElement().getID() );
        }
        for( YFlow flow : cloneElement.getPostsetFlows() ) {
            assertNotNull( flow.getPriorElement() );
            assertNotNull( flow.getNextElement() );
            cloneNames.add( flow.getPriorElement().getID() );
            cloneNames.add( flow.getNextElement().getID() );
        }
        
        assertTrue( names.size() + " : " + cloneNames.size(),
                names.size() == cloneNames.size() );
        for( String name : names ) {
            assertTrue( name, cloneNames.contains( name ) );
        }
        
        names.clear();
        cloneNames.clear();
        
        for( YFlow flow : element.getPresetFlows() ) {
            names.add( flow.getPriorElement().getID() );
            names.add( flow.getNextElement().getID() );
        }
        for( YFlow flow : cloneElement.getPresetFlows() ) {
            assertNotNull( flow.getPriorElement() );
            assertNotNull( flow.getNextElement() );
            cloneNames.add( flow.getPriorElement().getID() );
            cloneNames.add( flow.getNextElement().getID() );
        }
        
        assertTrue( names.size() + " : " + cloneNames.size(),
                names.size() == cloneNames.size() );
        for( String name : names ) {
            assertTrue( name, cloneNames.contains( name ) );
        }
        
        if( element instanceof YTask ) {
            YTask task = (YTask) element;
            YTask cloneTask = (YTask) cloneElement;
            
            if( task.getDecompositionPrototype() != null ) {
                assertNotNull( cloneTask.getDecompositionPrototype() );
                assertTrue( task.getDecompositionPrototype().getId() + "\n" +
                        cloneTask.getDecompositionPrototype().getId(),
                        task.getDecompositionPrototype().getId().equals(
                                cloneTask.getDecompositionPrototype().getId() ) );
            }
            else {
                assertNull( cloneTask.getDecompositionPrototype() );
            }
        }
    }
    
    /**
     * Tests to make sure that the clone does not contain any of the original
     * specification's objects.
     */
    public void testCloneDeepness() {
        YSpecification clone = specification.deepClone();
        
        assertNotNull( clone );
        
        List<Object> origObjects = new ArrayList<Object>( 50 );
        List<Object> cloneObjects = new ArrayList<Object>( 50 );
        
        collectObjects( specification, origObjects );
        collectObjects( clone, cloneObjects );
        
        assertTrue( "orig:" + origObjects.size() + "\t clone:" + cloneObjects.size(),
                origObjects.size() == cloneObjects.size() );
        
        for( Object o : origObjects ) {
            System.out.println( "" + o );
            assertNotNull( o );
            for( Object o2 : cloneObjects ) {
                assertFalse( o.getClass().getName() + "\t:\t" + o, o == o2 );
            }
        }
    }
    
    private void collectObjects( YSpecification spec, Collection<Object> set ) {
        assertNotNull( spec );
        set.add( spec );
        if( spec.getMetaData() != null ) {
            set.add( spec.getMetaData() );
        }
        for( YDecomposition decomp : spec.getDecompositions() ) {
            collectObjects( decomp, set );
        }
    }
    
    private void collectObjects( YDecomposition decomp, Collection<Object> set ) {
        assertNotNull( decomp );
        set.add( decomp );
        for( YParameter param : decomp.getInputParameters() ) {
            assertNotNull( param );
            set.add( param );
        }
        for( YParameter param : decomp.getOutputParameters() ) {
            assertNotNull( param );
            set.add( param );
        }
        if( decomp instanceof YNet ) {
            YNet net = (YNet) decomp;
            for( YVariable variable : net.getLocalVariables() ) {
                assertNotNull( variable );
                set.add( variable );
            }
            for( YExternalNetElement element : net.getNetElements() ) {
                collectObjects( element, set );
            }
        }
        else if( decomp instanceof YAWLServiceGateway ) {
            YAWLServiceGateway gateway = (YAWLServiceGateway) decomp;
            if( gateway.getYawlService() != null ) {
                set.add( gateway.getYawlService() );
            }
        }
    }
    
    private void collectObjects( YExternalNetElement element, Collection<Object> set ) {
        assertNotNull( element );
        set.add( element );
        for( YFlow flow : element.getPostsetFlows() ) {
            assertNotNull( flow );
            set.add( flow );
        }
    }
}
