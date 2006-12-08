/*
 * Created on 16/02/2006
 * YAWLEditor v1.4 
 *
 * @author Moe Thandar Wyn
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package au.edu.qut.yawl.editor.analyser;

import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

import au.edu.qut.yawl.elements.YExternalNetElement;


/**
 *
 * The base class for RTransition and RPlace 
 *
 */

   
   public class RElement 
   { private String _name;
     private Map _presetFlows = new HashMap();
     private Map _postsetFlows = new HashMap();
     private String _id;
    
     //added for reduction rules code
     private Set _cancelledBySet = new HashSet();
     
     //added for reduction rules mapping
     private Set _yElementsSet = new HashSet();
     //used for reduced net mappings between yawl
     private Set _rElementsSet = new HashSet();
    
   public RElement(String id) {
	   _id = id;
		  
	  
   }
   
   public String getID()
	{
		return _id;
	}
	
   public void setName(String name){
        _name = name;
    }
    
   public String getName(){
   	 return _name;
   }
   
   public Map getPresetFlows()
   { return _presetFlows;
   }
   public Map getPostsetFlows()
   { return _postsetFlows;
   }
   
   public void setPresetFlows(Map presetFlows)
   { _presetFlows = new HashMap(presetFlows);
    
   }
    public void setPostsetFlows(Map postsetFlows)
   { _postsetFlows = new HashMap(postsetFlows);
    
   }
   
   public Set getPostsetElements() {
        Set postsetElements = new HashSet();
        Collection flowSet = _postsetFlows.values();
        for (Iterator iterator = flowSet.iterator(); iterator.hasNext();) {
            RFlow flow = (RFlow) iterator.next();
            postsetElements.add(flow.getNextElement());
        }
        return postsetElements;
  }
    
  public Set getPresetElements() {
        Set presetElements = new HashSet();
        Collection flowSet = _presetFlows.values();
        for (Iterator iterator = flowSet.iterator(); iterator.hasNext();) {
            RFlow flow = (RFlow) iterator.next();
            presetElements.add(flow.getPriorElement());
        }
        return presetElements;
    }
    
   public void setPreset(RFlow flowsInto) {
    if (flowsInto != null) {
    	// a quick fix to deal with null exception for XORsplits
    	if (flowsInto.getPriorElement() !=null)
        { _presetFlows.put(flowsInto.getPriorElement().getID(), flowsInto);
         flowsInto.getPriorElement()._postsetFlows.put(flowsInto.getNextElement().getID(), flowsInto);
        }
     }
    }

    public void setPostset(RFlow flowsInto) {
	    if (flowsInto != null) {
	    	if (flowsInto.getNextElement() !=null)
	    	{
	    	// a quick fix to deal with null exception for XORsplits
	        _postsetFlows.put(flowsInto.getNextElement().getID(), flowsInto);
	        flowsInto.getNextElement()._presetFlows.put(flowsInto.getPriorElement().getID(), flowsInto);
	        }
	   }
    }


    public RElement getPostsetElement(String id) {
/*        if (_postset.size() > 0) {
            return (YExternalNetElement) this._postset.get(id);
        } else*/ {
            return ((RFlow) this._postsetFlows.get(id)).getNextElement();
        }
    }


    /**
     * Method getPresetElement.
     * @param id
     * @return YExternalNetElement
     */
    public RElement getPresetElement(String id){
        return ((RFlow) _presetFlows.get(id)).getPriorElement();
    }

public void removePresetFlow(RFlow flowsInto){

   if (flowsInto != null) {
            _postsetFlows.remove(flowsInto.getNextElement().getID());
            flowsInto.getNextElement()._presetFlows.remove(flowsInto.getPriorElement().getID());
        }

   }

   public void removePostsetFlow(RFlow flowsInto){

   if (flowsInto != null) {
            _postsetFlows.remove(flowsInto.getNextElement().getID());
            flowsInto.getNextElement()._presetFlows.remove(flowsInto.getPriorElement().getID());
        }

   }
  
    public Set getResetMappings()
   {
   	 if (_rElementsSet != null) {
            return new HashSet(_rElementsSet);
     }
     return null;
    
   }
   
   public void addToResetMappings(RElement e){
   	_rElementsSet.add(e);
   	
   }
   public void addToResetMappings(Set elements){
   	_rElementsSet.addAll(elements);
   	
   }
   
   public Set getYawlMappings()
   {
   	 if (_yElementsSet != null) {
            return new HashSet(_yElementsSet);
     }
     return null;
    
   }
   
   public void addToYawlMappings(YExternalNetElement e){
   	_yElementsSet.add(e);
   	
   }
   public void addToYawlMappings(Set elements){
   	_yElementsSet.addAll(elements);
   	
   }
    
   public Set getCancelledBySet(){
   	if (_cancelledBySet != null) {
            return new HashSet(_cancelledBySet);
        }
        return null;
    }
   
   public void addToCancelledBySet(RTransition t){
   	if (t != null && t instanceof RTransition)
   	 {	_cancelledBySet.add(t);	
   	 }
   }
   public void removeFromCancelledBySet(RTransition t){
   	 if (t != null && t instanceof RTransition)
   	 {	
   	   _cancelledBySet.remove(t);
   	 }	
   }
   
   }
   
 

 

 
