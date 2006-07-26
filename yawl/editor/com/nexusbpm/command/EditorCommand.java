/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.command;

import java.net.URI;
import java.net.URLDecoder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.edu.qut.yawl.elements.YDecomposition;
import au.edu.qut.yawl.elements.YExternalNetElement;
import au.edu.qut.yawl.elements.YFlow;
import au.edu.qut.yawl.elements.YNet;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.elements.YTask;
import au.edu.qut.yawl.persistence.managed.DataContext;
import au.edu.qut.yawl.persistence.managed.DataProxy;

import com.nexusbpm.editor.tree.SharedNode;
import com.nexusbpm.editor.tree.SharedNodeTreeModel;

/**
 * This class should be refactored and perhaps moved to the operation package.
 * 
 * It includes utility methods for some of the operations we need to invoke 
 * frequently on the YAWL objects.
 * 
 * @todo break up and move the objects around. some into ops some into elements.
 * @author Matthew Sandoz
 *
 */
public class EditorCommand {
	private static final Log LOG = LogFactory.getLog(EditorCommand.class);
		
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
	
	public static void executeCopyCommand(SharedNode source, SharedNode target) {
		//the purpose of this is to use the path and context of the target
		//and the content of the source (which must at this point be a 
		//specification
		DataContext targetDataContext = target.getProxy().getContext();
		YSpecification spec = (YSpecification) source.getProxy().getData();
		try {
			YSpecification newSpec = (YSpecification) spec.clone();
			newSpec.setDbID(null);
			String dataroot = target.getProxy().getData().toString();
			URI desturi = joinUris(new URI(dataroot), new URI(spec.getID()));
			newSpec.setID(desturi.toASCIIString());
//			DataProxy dp = targetDataContext.getDataProxy(newSpec, null);
            DataProxy dp = targetDataContext.createProxy(newSpec, null);
            targetDataContext.attachProxy(dp, newSpec, null); // TODO fix null
			targetDataContext.save(dp);
			LOG.info("Copying specification " + spec.getID() + " to " + newSpec.getID());
			targetDataContext.getChildren(target.getProxy(), true);
			((SharedNodeTreeModel)target.getTreeModel()).reload(target);
		} catch (Exception e) {
			LOG.error("Error copying specification " + spec.getID(), e);
		}
	}
	
	//here there be dragons
	
	/**
	 * This marks the beginning of the grafting command utilities.
	 * 
	 * They pretty much will all rely on the "clone all" routine
	 * inside the yspec. this should ensure that there will be no 
	 * cross-wiring of child elements among different containers
	 * 
	 */
	
	public static void addDecompositionToSpecification(YSpecification spec, YDecomposition decomp){
		spec.setDecomposition(decomp);
		decomp.setParent(spec);
		//at this point, might have to regenerate mappings...
	}

	public static void addNewNetElementToExistingNet(YExternalNetElement newElement, YNet existingNet) {
		
		existingNet.addNetElement(newElement);
		newElement.setParent(existingNet);
		if (newElement instanceof YTask) {
			YTask task = (YTask) newElement;
			YDecomposition decomp = task.getDecompositionPrototype();
			//should we do something with any existing like-named decompositions?
			if (decomp != null) {
				decomp.setParent(existingNet.getParent());
				existingNet.getParent().setDecomposition(decomp);
			}
		}
	}
	
	public static void addNewNetElementsToExistingNet(
			YExternalNetElement[] elements, YNet net) {
		for (YExternalNetElement element : elements) {
			addNewNetElementToExistingNet(element, net);
		}
		for (YExternalNetElement element : elements) {
			for (YFlow flow : element.getPostsetFlows()) {
				//at least one must be the new net so they must == if both are in
				if (flow.getPriorElement().getParent() != flow.getNextElement().getParent()) {
					element.removePostsetFlow(flow);
				}
			}
			for (YFlow flow : element.getPresetFlowsAsList()) {
				if (flow.getPriorElement().getParent() != flow.getNextElement().getParent()) {
					element.removePresetFlow(flow);
				}
			}
		}
	}
	
	public static void removeElementFromExistingNet(
			YExternalNetElement element, YNet existingNet) {
		throw new UnsupportedOperationException("remove element from net");
	}
	public static void removeElementsFromExistingNet(
			YExternalNetElement[] elements, YNet existingNet) {
		throw new UnsupportedOperationException("remove elements from net");
	}
	
	public static void removeNetFromExistingSpecification(
			YNet existingNet, YSpecification spec) {
		throw new UnsupportedOperationException("remove net from specification");
	}
	
}