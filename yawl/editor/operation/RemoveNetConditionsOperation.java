package operation;

import java.util.ArrayList;
import java.util.List;

import au.edu.qut.yawl.elements.YCondition;
import au.edu.qut.yawl.elements.YDecomposition;
import au.edu.qut.yawl.elements.YExternalNetElement;
import au.edu.qut.yawl.elements.YFlow;
import au.edu.qut.yawl.elements.YNet;
import au.edu.qut.yawl.elements.YSpecification;

public class RemoveNetConditionsOperation {

    /**
     * This is a utility method that could be moved. Its entire purpose
     * is to remove the extra conditions that are inserted between each
     * task when the specification is deserialized from XML.
     * 
     * @param spec
     */
    public static void removeConditions(YSpecification spec) {
    	for (YDecomposition decomp: spec.getDecompositions()) {
    		if (decomp instanceof YNet) {
    			List<YExternalNetElement> copySet = new ArrayList<YExternalNetElement>();
    			copySet.addAll(((YNet) decomp).getNetElements());
    			for (YExternalNetElement currentElement: copySet) {
    				if (currentElement.getClass() == YCondition.class) {
    					List<YExternalNetElement> elementsBeforeCondition = currentElement.getPresetElements();
    					List<YExternalNetElement> elementsAfterCondition = currentElement.getPostsetElements();
    					if (elementsBeforeCondition.size() == 1) {
    						YExternalNetElement elementBeforeCondition = elementsBeforeCondition.get(0);
    						for (YExternalNetElement elementAfterCondition: elementsAfterCondition) {
    							for (YFlow flow: elementAfterCondition.getPresetFlows()) {
    								if (flow.getPriorElement() == currentElement) {
    									flow.setPriorElement(elementAfterCondition);
    								}
    							}
    							for (YFlow flow: elementBeforeCondition.getPostsetFlows()) {
    								if (flow.getNextElement() == currentElement) {
    									flow.setNextElement(elementAfterCondition);
    								}
    							}
    							
    						}
    					}
    					((YNet) decomp).getNetElements().remove(currentElement);
    				}
    			}
    		}
    	}
    }

}
