/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.elements.e2wfoj;

import java.util.*;

/**
 *
 * The base class for RTransition and RPlace 
 *
 **/

   
   public class RElement 
   { private String _name;
     private Map _presetFlows = new HashMap();
     private Map _postsetFlows = new HashMap();
     private String _id;
     
   public RElement(String id){
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
        _presetFlows.put(flowsInto.getPriorElement().getID(), flowsInto);
        flowsInto.getPriorElement()._postsetFlows.put(flowsInto.getNextElement().getID(), flowsInto);
     }
    }

    public void setPostset(RFlow flowsInto) {
	    if (flowsInto != null) {
	         _postsetFlows.put(flowsInto.getNextElement().getID(), flowsInto);
	        flowsInto.getNextElement()._presetFlows.put(flowsInto.getPriorElement().getID(), flowsInto);
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


   }
   
 

 

 
