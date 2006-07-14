package au.edu.qut.yawl.persistence.jaxb;

import java.io.ByteArrayOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.w3c.dom.Element;

import au.edu.qut.yawl.elements.YAtomicTask;
import au.edu.qut.yawl.elements.YCompositeTask;
import au.edu.qut.yawl.elements.YDecomposition;
import au.edu.qut.yawl.elements.YExternalNetElement;
import au.edu.qut.yawl.elements.YFlow;
import au.edu.qut.yawl.elements.YNet;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.elements.YTask;
//import au.edu.qut.yawl.jaxb.ExpressionType;


public class PersistenceHelper {
	public static class MiDataOutput {
	
//	    protected ExpressionType formalOutputExpression;
//	    protected ExpressionType outputJoiningExpression;
	    protected String resultAppliedToLocalVariable;
	
//	    public ExpressionType getFormalOutputExpression() {
//	        return formalOutputExpression;
//	    }
//	    public void setFormalOutputExpression(ExpressionType value) {
//	        this.formalOutputExpression = value;
//	    }
//	    public ExpressionType getOutputJoiningExpression() {
//	        return outputJoiningExpression;
//	    }
//	    public void setOutputJoiningExpression(ExpressionType value) {
//	        this.outputJoiningExpression = value;
//	    }
	    public String getResultAppliedToLocalVariable() {
	        return resultAppliedToLocalVariable;
	    }
	    public void setResultAppliedToLocalVariable(String value) {
	        this.resultAppliedToLocalVariable = value;
	    }
	}
	public static class MiDataInput {
//	    protected ExpressionType expression;
//	    protected ExpressionType splittingExpression;
//	    protected String formalInputParam;
//	    public ExpressionType getExpression() {
//	        return expression;
//	    }
//	    public void setExpression(ExpressionType value) {
//	        this.expression = value;
//	    }
//	    public ExpressionType getSplittingExpression() {
//	        return splittingExpression;
//	    }
//	    public void setSplittingExpression(ExpressionType value) {
//	        this.splittingExpression = value;
//	    }
//	    public String getFormalInputParam() {
//	        return formalInputParam;
//	    }
//	    public void setFormalInputParam(String value) {
//	        this.formalInputParam = value;
//	    }
	}
//	public static void setJaxbDecompositions(YSpecification spec, List<YDecomposition> set) {
//		spec._decompositions = set;
//    	for (YDecomposition decomp: spec._decompositions) {
//    		decomp.setSpecification(spec);
////    		decomp.getInternalDataDocument().setRootElement(new org.jdom.Element(decomp.getRootDataElementName()));
//    		if (decomp instanceof YNet) {
//    			if (Boolean.parseBoolean(((YNet) decomp).getRootNet())) {
//    				spec._rootNet = ((YNet) decomp);
//    			}
//    		}
//    	}
//    	Map<String, YDecomposition> nameMap = new HashMap<String, YDecomposition>();
//    	for (YDecomposition decomp: spec.getDecompositions()) {
//    		nameMap.put(decomp.getId(), decomp);
//    	}
//    		
//    	for (YDecomposition decomp: spec.getDecompositions()) {
//    		if (decomp instanceof YNet) {
//    			for (Map.Entry<String, YExternalNetElement> entry:((YNet) decomp).getNetElements().entrySet()) {
//    				if (entry.getValue() instanceof YTask) {
//    					if (((YTask) entry.getValue()).getDecompositionPrototype() != null) {
//        					String which = ((YTask) entry.getValue()).getDecompositionPrototype().getId();
//        					((YTask) entry.getValue()).setDecompositionPrototype(nameMap.get(which));
//    					}
//    				}
//    			}
//    		}
//    	}
//    	for (YDecomposition decomp: spec.getDecompositions()) {
//    		if (decomp instanceof YNet) {
//    			Map<String, YExternalNetElement> map = new TreeMap<String, YExternalNetElement>();
//    			map.putAll(((YNet) decomp).getNetElements());
//    			for (Map.Entry<String, YExternalNetElement> entry:map.entrySet()) {
////    				for (YFlow flow: entry.getValue().getPresetFlows()) {
////    					System.out.println("AFLOW : " + flow.isDefaultFlow() + ":" + flow.getPriorElement().getID() + ":" + entry.getValue().getID());
////    				}
////    				for (YFlow flow: entry.getValue().getPostsetFlows()) {
////    					System.out.println("BFLOW : " + flow.isDefaultFlow() + ":" + entry.getValue().getID() + ":" + flow.getNextElement().getID());
////    				}
//    				if (entry.getValue() instanceof YTask) {
//    					YTask taska = ((YTask) entry.getValue());
//    					if (taska.getDecompositionPrototype() != null) {
//        					String which = taska.getDecompositionPrototype().getId();
//        					if (nameMap.get(which) instanceof YNet) {
//        						YExternalNetElement trial = ((YNet) decomp).getNetElements().get(taska.getID());
//        						YCompositeTask task = null; 
//        						if (trial == null || trial instanceof YAtomicTask) {
//            						task = new YCompositeTask(taska.getID(), taska.getJoinType(), taska.getSplitType(), taska.getParent());
//        						}
//        						else if (trial instanceof YCompositeTask){
//        							task = (YCompositeTask) trial;
//        						}        						
//        						task.setDecompositionPrototype(nameMap.get(which));
//        						Collection<YFlow> flows = taska.getPostsetFlows();
//        						for (YFlow flow: flows) {
//        							flow.setPriorElement(task);
//        						}
//        						flows = taska.getPresetFlows();
//        						for (YFlow flow: flows) {
//        							flow.setNextElement(task);
//        						}
//        						task.setPresetFlows(taska.getPresetFlows());
//        						task.setPostsetFlows(taska.getPostsetFlows());
////        						task.setCreationMode(taska.getCreationMode());
//        						task.setDataMappingsForTaskStarting(taska.getDataMappingsForTaskStarting());
//        						task.setDataMappingsForTaskCompletion(taska.getDataMappingsForTaskCompletion());
//        						task.setDocumentation(taska.getDocumentation());
//        						((YNet) decomp).getNetElements().put(task.getID(), task);
//        					}
//    					}
//    				}
//    			}
//    		}
//    	}
//	}
	public static void setAnyJaxb(YSpecification spec, List<Element> nodes) {
		try {
			com.sun.org.apache.xml.internal.serialize.XMLSerializer s = new com.sun.org.apache.xml.internal.serialize.XMLSerializer();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			s.setNamespaces(true);
			s.setOutputByteStream(System.out);
			s.serialize(nodes.get(0));
			s.setOutputByteStream(baos);
			s.serialize(nodes.get(0));
			spec.setSchema(baos.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
