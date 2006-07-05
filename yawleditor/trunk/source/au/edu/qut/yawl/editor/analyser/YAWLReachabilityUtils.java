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

import au.edu.qut.yawl.elements.*;
import au.edu.qut.yawl.exceptions.*;
import au.edu.qut.yawl.elements.state.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * This class is used to determine reachability set for a YAWL net.
 *
 */
public class YAWLReachabilityUtils{
     YSetOfMarkings endMarkings = new YSetOfMarkings();
     int maxNumMarkings = 1000;
     YNet _yNet;
     YSetOfMarkings RS;
     Set firedTasks = new HashSet(100); 
     Set orJoins = new HashSet(10);
     Map ojMarkingsMap = new HashMap(10);
     
     public YAWLReachabilityUtils(YNet net){
     	_yNet = net;
     }
     
     
     /**
      * It returns whether the set of markings contains markings that marks
      * all input places of OJ and not smaller AND-join - 0
      * only one input place of OJ and not bigger 1
      * more than one input place - 3 and 4
      */
     private int checkORjoinStatus(YSetOfMarkings RS,Set presetOJ)
     { 
      List  preSetList = new LinkedList(presetOJ);
       YMarking orJoinANDMarking = new YMarking(preSetList);
       YSetOfMarkings orJoinXORMarkings = new YSetOfMarkings();
       for(Iterator ip = presetOJ.iterator(); ip.hasNext();)
       {  YExternalNetElement e = (YExternalNetElement) ip.next();
          List eList = new LinkedList();
          eList.add(e);
          YMarking m = new YMarking(eList);
          orJoinXORMarkings.addMarking(m);
       } 
       
       //check if RS contains all marked preset and not smaller
       if (RS.containsBiggerEqual(orJoinANDMarking)){ 
         if (!containsLessThanMarking(RS,orJoinANDMarking,presetOJ)) {
           return 0;
         }
         return 3;
       }
        //otherwise check one each
        for (Iterator i=orJoinXORMarkings.getMarkings().iterator();i.hasNext();)
        {   YMarking orJoinXORMarking = (YMarking) i.next();
        	  boolean isXOR = checkXOR(RS,orJoinXORMarking,presetOJ);
        	  if (!isXOR)
        	  {
        		return 4;
        	  }
        }
        return 1;
     } 
     
     /**
      * returns true if there is a smaller marking M[preset] < orJoinANDMarking.
      *
      */
       private boolean containsLessThanMarking(YSetOfMarkings RS, YMarking orJoinANDMarking,Set presetOJ)
       {
      
       for(Iterator i = RS.getMarkings().iterator(); i.hasNext();)
       { YMarking m = (YMarking) i.next();
         List locations = new LinkedList(m.getLocations());
         locations.retainAll(presetOJ);
         //only for markings that mark preset of OJ.
         if (locations.size() >0)
         { 
         YMarking mp = new YMarking(locations);
         if (!mp.isBiggerThanOrEqual(orJoinANDMarking))
	       	 { return true;
	       	 }
       	 } //endif
       } //endfor	 
       return false;
      }

     /**
      * returns ture if no marking bigger than XOR is found in RS.
      */
     private boolean checkXOR(YSetOfMarkings RS,YMarking orJoinXORMarking,Set presetOJ)
       {
     
       for(Iterator i = RS.getMarkings().iterator(); i.hasNext();)
       { YMarking m = (YMarking) i.next();
         List locations = m.getLocations();
         locations.retainAll(presetOJ);
         YMarking mp = new YMarking(locations);
         if (mp.isBiggerThan(orJoinXORMarking))
       	 { return false;
       	 }
       }
       return true;
      }
      
     /**
      * Used to detect whether OR-joins should be replaced by XOR or AND.
      * returns a message.
      *
      */ 
   	 public String checkUnnecessaryORJoins() throws Exception
	 {  String msg="";
	    String xor="";
	    String and="";
	    boolean changeToAND = false;
	    boolean changeToXOR = false;
	    if (RS == null)
	    {
	    List iLocation = new LinkedList();
        iLocation.add(_yNet.getInputCondition());
        YMarking Mi = new YMarking(iLocation); 
        
        RS = getReachableMarkings(Mi);
        }
        
     //   System.out.println("Number of orJoins:"+orJoins.size());
        //first identify all OR-joins
        for(Iterator i = orJoins.iterator(); i.hasNext();)
        { YTask orJoin = (YTask) i.next();
          Set   preSet = orJoin.getPresetElements();
          YSetOfMarkings ojMarkings = (YSetOfMarkings) ojMarkingsMap.get(orJoin.getID());
          if (ojMarkings != null)
          {
          int  status = checkORjoinStatus(ojMarkings,preSet);
    //      System.out.println("Status:"+status);
          if (status == 0)
          { changeToAND = true;
            and += orJoin.getID()+" ";
          }
          else if (status == 1)
          { changeToXOR = true;
            xor += orJoin.getID()+" ";
          }
          } 
        }
        
         
       if(changeToAND)
       {
       	 xor = "OR-join task(s) " + and +" in the net "+_yNet.getID() 
       	      +" could be modelled as AND-join tasks."; 
       	 msg = formatXMLMessage(xor,true);    
       } 
       if (changeToXOR)
       {
       	 and += "OR-join task(s) " + xor +" in the net "+_yNet.getID() 
       	      +" could be modelled as XOR-join tasks."; 
       	 msg += formatXMLMessage(and,true); 
       }
       if (!changeToAND && !changeToXOR)
       { msg = "The net "+_yNet.getID()+" has no unnecessary OR-joins.";
         msg = formatXMLMessage(msg,true); 
	    } 
	     
       
	   return msg;	
	   
	 }

     /**
      * The method to check soundness property of YNets using 
      * reachability analysis.
      * returns messages.
      * note: this method also generates endMarkings, orJoins and orJoinsMarkingsMap
      *
      */
	 public String checkSoundness() throws Exception
	 {
	 	String msg = "";  
        
        List iLocation = new LinkedList();
        iLocation.add(_yNet.getInputCondition());
        List oLocation = new LinkedList();
        oLocation.add(_yNet.getOutputCondition());
	   	
	   	YMarking Mi = new YMarking(iLocation); 
	   	YMarking Mo = new YMarking(oLocation); 	
	   	
	   	boolean optionToComplete = true;
	   	boolean properCompletion = true;
	   	boolean noDeadTasks = true;
	   	
	   	
	    RS = getReachableMarkings(Mi);
	    
	  /* 	System.out.println("RS"+RS.size());
	   	System.out.println("endMarkings"+endMarkings.size());
	   	
	   	for (Iterator i = RS.getMarkings().iterator(); i.hasNext();)
	    {   YMarking m = (YMarking) i.next();
	      	System.out.println(printMarking(m));
	   	}
	 */  	
	     String omsg = "";
        //To check whether exact marking Mo=o is reachable.
   	   	if (RS.contains(Mo))
   	   	{ omsg = "The net "+_yNet.getID()+" has an option to complete. The final marking is reachable from the initial marking.";
	    }
   	    else
   	    { omsg = "The net "+_yNet.getID()+" does not have an option to complete. The final marking is not reachable from the initial marking.";
   	      optionToComplete = false;
   	    }
   	 
   	  msg += formatXMLMessage(omsg,optionToComplete);
   	  
   	  
   	  //Check the end markings for other end markings. 
   	  String pmsg = "";
   	  for (Iterator i = endMarkings.getMarkings().iterator(); i.hasNext();)
	     {
	       YMarking currentM = (YMarking) i.next();	
	       if (currentM.isBiggerThan(Mo))
	       { pmsg +="The net "+_yNet.getID()+" does not have proper completion. A marking larger than the final marking is reachable."+ printMarking(currentM);
	         properCompletion = false;
	       }
	       else if (!currentM.equivalentTo(Mo))
	       { pmsg +="The net "+_yNet.getID()+" can deadlock at marking:"+ printMarking(currentM);
	         optionToComplete = false;
	       }
	     }
	  
	   if (pmsg == "")
	   {
	   	pmsg = "The net has proper completion.";
	   }
	   else
	   msg += formatXMLMessage(pmsg,properCompletion);
	   
	   
	   String dmsg = "";
	    for (Iterator i = _yNet.getNetElements().values().iterator(); i.hasNext();)
	     { 
	      YExternalNetElement nextElement = (YExternalNetElement) i.next();
	       
	       if(nextElement instanceof YTask)
	       { 
	       
	       YTask t = (YTask) nextElement;
	  /*     Set preSet = t.getPresetElements();
	       List preList = new Vector(preSet);
	       YMarking currentM = new YMarking(preList); 	
	   	   if (!RS.containsBiggerEqual(currentM))
	   	   {
	   */   if (!firedTasks.contains(t))
	        {
	        dmsg += t.getID()+" ";
	   	   	noDeadTasks = false;
	   	   }
	      }   
	     } 
	 
	  if (dmsg == "")
	    { dmsg = "The net "+_yNet.getID()+" has no dead tasks.";
	    } 
	    else
	    { dmsg = "The net "+_yNet.getID()+" has dead tasks:" + dmsg;
	    }
	     
	   msg += formatXMLMessage(dmsg,noDeadTasks);
	   	   
	   
	   //To display message regarding soundness property.
	   String smsg = "";
	   boolean isSound = true;
	   if (optionToComplete && properCompletion && noDeadTasks)
	   { 
	      smsg = "The net "+_yNet.getID() +" satisfies the soundness property.";
	   }
	   else
	   { smsg = "The net "+_yNet.getID() +" does not satisfy the soundness property.";
	     isSound = false; 
	   }
	  msg += formatXMLMessage(smsg,isSound);
   	 return msg;
   	 }
   	 
   	private YSetOfMarkings getReachableMarkings(YMarking M) throws Exception{
	 	
	YSetOfMarkings RS = new YSetOfMarkings();
    YSetOfMarkings visitingPS = getImmediateSuccessors(M); 
    
    do
    {   
        RS.addAll(visitingPS);
        
        if(RS.size() > maxNumMarkings)
        { throw new Exception("Reachability analysis cannot be completed."+
        "The net "+_yNet.getID() +"might have infinite state space."+ maxNumMarkings);
        }
        
        visitingPS = getImmediateSuccessors(visitingPS);
     		    	
    }while (visitingPS.size() > 0);
            
    return RS;	
    }
    
    /**
     * return successor markings from a set of markings.
     *
     *
     */
    private YSetOfMarkings getImmediateSuccessors(YSetOfMarkings markings)
    {
    YSetOfMarkings successors = new YSetOfMarkings();
    for (Iterator i = markings.getMarkings().iterator(); i.hasNext();)
    {
       YMarking currentM = (YMarking) i.next();	
       YSetOfMarkings post = getImmediateSuccessors(currentM);
       if(post.size() > 0)
       { successors.addAll(post);
       }
       else
      {
      	endMarkings.addMarking(currentM);
      }	
     }  
    return successors;
    }
    
    
    /**
     * return successor markings from a marking.
     *
     *
     */
    private YSetOfMarkings getImmediateSuccessors(YMarking currentM)
    {
    YSetOfMarkings successors = new YSetOfMarkings();
    for (Iterator i = _yNet.getNetElements().values().iterator(); i.hasNext();)
    { YExternalNetElement ele = (YExternalNetElement) i.next();
      if (ele instanceof YTask)  
      {  YTask t = (YTask) ele;
      	if (isForwardEnabled(currentM,t))
	     {  successors.addAll(getNextMarkings(currentM,t));
	        firedTasks.add(t);
	     }
      }
    } 
    return successors;
   }
    
     private YSetOfMarkings getNextMarkings(YMarking currentM,YTask t)
     { YSetOfMarkings successors = new YSetOfMarkings();
       Set preset = new HashSet(t.getPresetElements());
       Set postset = new HashSet(t.getPostsetElements());
       Set cancelset = new HashSet(t.getRemoveSet());
       
       int splitType = t.getSplitType();
       int joinType =  t.getJoinType();
       List locations = new LinkedList(currentM.getLocations());
       switch(joinType){
       	case YTask._AND:
       	{   
       	    for(Iterator i= preset.iterator();i.hasNext();)
       	    { YExternalNetElement ele = (YExternalNetElement)i.next();
       	      locations.remove(ele);
       	  	}
       	  	YMarking M = new YMarking(locations);
       	  	successors.addMarking(M);
       	}
       	break;
       	case YTask._OR:
       	{
       	    for(Iterator i=preset.iterator();i.hasNext();)
       	    { YExternalNetElement ele = (YExternalNetElement) i.next();
       	      locations.remove(ele);
       	  	}
       	  	YMarking M = new YMarking(locations);
       	  	successors.addMarking(M);
       	}
       	break;
       	case YTask._XOR:
       	{
       	    for(Iterator i=preset.iterator();i.hasNext();)
       	    { YExternalNetElement ele = (YExternalNetElement)i.next();
       	      locations.remove(ele);
       	      YMarking M = new YMarking(locations);
       	      successors.addMarking(M);
       	      locations.add(ele);
       	  	}
      
       	}
       	break;
       }
  //     System.out.println("No.of Successors. after join for task"+t.getID()+successors.size());
       
       //Remove tokens from cancellation region
       if (cancelset.size()>0)
       { YSetOfMarkings temp = new YSetOfMarkings();
         for (Iterator c = successors.getMarkings().iterator(); c.hasNext();)
    	 {
       		YMarking M = (YMarking) c.next();
       		List slocations = new LinkedList(M.getLocations());
       		for(Iterator i= cancelset.iterator();i.hasNext();)
       	    { YExternalNetElement ele = (YExternalNetElement)i.next();
       	      slocations.remove(ele);
       	  	}
       	  	YMarking Mt = new YMarking(slocations);
       	  	temp.addMarking(Mt);	
       
         }
       successors.removeAll();
       successors.addAll(temp);
       }
   //    System.out.println("No.of Successors. after cancel for task"+t.getID()+successors.size());
       

        switch(splitType){
       	case YTask._AND:
       	{     	  	    	
       	  YSetOfMarkings temp = new YSetOfMarkings();
          for (Iterator i = successors.getMarkings().iterator(); i.hasNext();)
    	  {
       		YMarking M = (YMarking) i.next();
       		List slocations = new LinkedList(M.getLocations());
       		for(Iterator ip = postset.iterator();ip.hasNext();)
       	    { YExternalNetElement ele = (YExternalNetElement) ip.next();
       	      slocations.add(ele);
       	  	}
       	  	YMarking Mt = new YMarking(slocations);
       	  	temp.addMarking(Mt);	
       
         }
       	successors.removeAll();
       	successors.addAll(temp);
       	
       	}
       	break;
       	case YTask._OR:
       	{ //generate combinations
       	  Set xSubSet = new HashSet();
	      for (int i=1; i <= postset.size(); i++)
	      {  Set subSet = generateCombination(postset,i);
	         xSubSet.addAll(subSet);
	      }
		      
	      YSetOfMarkings temp = new YSetOfMarkings();
	       for (Iterator i = successors.getMarkings().iterator(); i.hasNext();)
	       {  YMarking M = (YMarking) i.next();
	       	 for(Iterator s = xSubSet.iterator(); s.hasNext();)
	         {  Set subSet = (HashSet) s.next();
	            List slocations = new LinkedList(M.getLocations());
	            slocations.addAll(subSet);
	            YMarking Mt = new YMarking(slocations);
	           temp.addMarking(Mt);
       	      } //end for xsubset
       	   } //end for successors
       	 
	      successors.removeAll();
	      successors.addAll(temp);    		
       	}
       	
       	break;
       	case YTask._XOR:
       	{
       	  YSetOfMarkings temp = new YSetOfMarkings();
          for (Iterator i = successors.getMarkings().iterator(); i.hasNext();)
    	  {
       		YMarking M = (YMarking) i.next();
       	    List slocations = new LinkedList(M.getLocations());
       		
       		for(Iterator ip= postset.iterator();ip.hasNext();)
       	    { YExternalNetElement ele = (YExternalNetElement) ip.next();
       	      slocations.add(ele);
       	      YMarking Mt = new YMarking(slocations);
       	  	  temp.addMarking(Mt);
       	      slocations.remove(ele);
       	  	}
        }
       	successors.removeAll();
       	successors.addAll(temp);
       	}
       	break;
       }
   
   //   System.out.println("No.of Successors. after split for task"+t.getID()+successors.size());
     return successors;
     	
     	
     }
      
  
     private boolean isForwardEnabled(YMarking currentM, YTask t)
    {   Set preSet = t.getPresetElements();
          
        int joinType = t.getJoinType();
          switch (joinType) {
            case YTask._AND:
                {   List eleList = new LinkedList();
                 	eleList.addAll(preSet);
                	YMarking m = new YMarking(eleList);
                	return currentM.isBiggerThanOrEqual(m);
                }	 
            case YTask._OR:
                {   //use for unnecessary OR-joins
                    orJoins.add(t); 
                    YIdentifier id = convertMarkingToIdentifier(currentM);
                    boolean isOJEnabled = _yNet.orJoinEnabled(t,id);
                    //use for unnecessary OR-join enabling markingss.
                    if (isOJEnabled) 
                    { 
                      YSetOfMarkings ojMarkings = (YSetOfMarkings) ojMarkingsMap.get(t.getID());
                      if (ojMarkings == null)
                      { ojMarkings = new YSetOfMarkings();
                      }
                      ojMarkings.addMarking(currentM);
                      ojMarkingsMap.put(t.getID(),ojMarkings);
                    }
                    return isOJEnabled;
                }
            case YTask._XOR:
                for (Iterator i = preSet.iterator(); i.hasNext();) {
                	YExternalNetElement ele = (YExternalNetElement) i.next();
                	List eleList = new LinkedList();
                	eleList.add(ele);
                	YMarking m = new YMarking(eleList);
                	if (currentM.isBiggerThanOrEqual(m)) {
                	
                        return true;
                    }
                }
                return false;
            default:
                return false;
            }
   
    }

  /**
     * This method is used to generate combinations of markings for 
     * comparison. 
     */
   	private Set generateCombination(Set netElements,int size){
    
    Set subSets = new HashSet();
	Object[] elements = netElements.toArray();
	int[] indices;
	CombinationGenerator x = new CombinationGenerator(elements.length, size);
    while (x.hasMore ()) {
	  Set combsubSet = new HashSet();
	  indices = x.getNext ();
	  for (int i = 0; i < indices.length; i++) { 
	    combsubSet.add(elements[indices[i]]);
	  }
	  subSets.add(combsubSet);
	}
	return subSets;
} 

	/**
	 * used for formatting xml messages.
	 * Message could be a warning or observation. 
	 */ 
     private String formatXMLMessage(String msg,boolean isObservation)
	 { 
	  
	   String xmlHeader = "";
	   String xmlFooter = "";
	  if (isObservation)	
	  {
	      xmlHeader="<observation>";
		  xmlFooter="</observation>";
		  
	  }
	  else
	  {
		  xmlHeader="<warning>";
		  xmlFooter="</warning>";
	  } 	 
	  	return xmlHeader + msg + xmlFooter;
	 	
	 }
	 
	 
	 //A convinent method to convert a marking that we have into an identifer object.
	 private YIdentifier convertMarkingToIdentifier(YMarking m)
	 {
	 	YIdentifier id = new YIdentifier();
	 	List locations = new LinkedList(m.getLocations());
	 	
	 	try{
	 		for (Iterator i= locations.iterator();i.hasNext();)
		{  YExternalNetElement e = (YExternalNetElement) i.next();
	   	   if (e instanceof YTask)
	   	   {  YTask t = (YTask) e;
	   	      id.addLocation(null,t);
	   	   }
	       else if (e instanceof YCondition)
	   	   {  YCondition c = (YCondition) e;
	   	      id.addLocation(null,c);
	   	   }
		}
	 	}
	 	catch(YPersistenceException e)
	 	{
	 		//do nothing;
	 		
	 	}
	 
	 	return id;
	 }
	 
	 
	private String printMarking(YMarking m){
  
    String printM = "";
    List mPlaces = m.getLocations();
	for (Iterator i= mPlaces.iterator();i.hasNext();)
	{  YExternalNetElement e = (YExternalNetElement) i.next();
	   printM += e.getID()+"+";
	       
	}
	//To remove the last +
    return printM.substring(0,printM.length()-1);
    }	
}	 	
