/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package au.edu.qut.yawl.util;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.edu.qut.yawl.elements.KeyValue;
import au.edu.qut.yawl.elements.YAtomicTask;
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
import au.edu.qut.yawl.persistence.SourcedVariable;

/**
 * The VisitSpecificationOperation allows a caller to iterate through an
 * entire specification (spec, nets, decomps, tasks, conditions, flows)
 * or a subset and do something at each step.
 * 
 * @author Matthew Sandoz
 *
 */
public class VisitSpecificationOperation {
    private static final Log LOG = LogFactory.getLog(VisitSpecificationOperation.class);
    
    /**
     * This is a utility method that could be moved. Its purpose is to 
     * iterate through all children of a specification - nets, tasks, etc.
     * and to create a proxy for each of them.
     * 
     * Potentially useful refactors include:
     * refactor to visitor pattern
     * move method to another class
     * decompose method
     * 
     * @param spec
     * @param parent the object to be passed to the visitor as the parent of the spec
     */
    public static void visitSpecification(YSpecification spec, Object parent, Visitor v) {
		assert spec != null;
		v.visit(spec, parent, getSpecLabel(spec));
    	List<YDecomposition> decomps = spec.getDecompositions();
    	for (YDecomposition decomp: decomps) {
            assert decomp.getParent() == spec : "spec's decomp's parent was not the spec";
            visitDecomposition( decomp, v );
    	}
    }
    
    private static String getSpecLabel( YSpecification spec ) {
        String val = "";
        if( spec.getID().indexOf( "/" ) >= 0 ) {
            val = spec.getID().substring( spec.getID().lastIndexOf( "/" ) + 1 );
        }
        else {
            val = spec.getID();
        }
        val = val.trim();
        if( val.length() > 0 ) {
            return val;
        }
        else {
            return null;
        }
    }
    
    public static void visitDecomposition(YDecomposition decomp, Visitor v) {
        String label;
        if (decomp.getName() != null && decomp.getName().length() != 0) {
            label = decomp.getName();
        } else {
            label = decomp.getId();
        }
        v.visit(decomp, decomp.getParent(), label);
        if (decomp instanceof YNet) {
            YNet net = (YNet) decomp;
            for(YExternalNetElement yene: net.getNetElements()) {
                assert yene.getParent() == net : "net's element's parent was not the net";
                visitNetElement(yene, v);
            }
            for(YExternalNetElement yene: net.getNetElements()) {
                for (YFlow flow: yene.getPostsetFlows()) {
                    String to;
                    if (findType(flow.getNextElement()) == Type.CONDITION) {
                        to = "to connector";
                    } else {
                        to = "to " + getLabelFor(flow.getNextElement());
                    }
                    v.visit(flow, decomp, to);
                }
            }
        }
    }
    
    public static void visitNetElement(YExternalNetElement yene, Visitor v) {
        String label = getLabelFor(yene);
        v.visit(yene, yene.getParent(), label);
        if (yene instanceof YAtomicTask) {
        	YAtomicTask task = (YAtomicTask) yene;
            for (String name: task.getDataMappingsForTaskStarting().keySet()) {
            	SourcedVariable var = new SourcedVariable(task, name);
            	visitVariable(var, v);
            }
        }
    }

    public static void visitVariable(SourcedVariable var, Visitor v) {
    	v.visit(var, var.getTask(), var.getName());
//    	System.out.println(var.getName());
    }
    
    private static Type findType (Object o) {
    	Type retval = Type.UNKNOWN;
    	if (o instanceof YInputCondition ) {retval = Type.INPUT_CONDITION;}
    	else if (o instanceof YOutputCondition) {retval = Type.OUTPUT_CONDITION;}
    	else if (o instanceof YTask) {retval = Type.TASK;}
    	else if (o instanceof YCondition) {retval = Type.CONDITION;}
    	else if (o instanceof YFlow) {retval = Type.FLOW;}
    	else if (o instanceof YSpecification) {retval = Type.SPECIFICATION;}
    	return retval;

    }	
    
    private static String getLabelFor(YExternalNetElement element) {
    	String retval;
		if (element.getName() != null && element.getName().length() != 0) {
			retval = element.getName();
		}else {
			retval = element.getID();
		}
		return retval;    	
    }
    
    public interface Visitor {
    	void visit(Object child, Object parent, String childLabel);
    }
  
    private enum Type {UNKNOWN, INPUT_CONDITION, OUTPUT_CONDITION, TASK, CONDITION, FLOW, SPECIFICATION};
}
