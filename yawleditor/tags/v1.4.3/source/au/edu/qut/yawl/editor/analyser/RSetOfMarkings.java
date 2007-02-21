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

import java.util.Set;
import java.util.Iterator;
import java.util.HashSet;

/**
 *  Data structure for Storage of RMarkings.
 *
 **/

public class RSetOfMarkings {
    private Set _markings = new HashSet();

    
   public void addMarking(RMarking marking){
   	    if (!contains(marking)){
         _markings.add(marking);
        }	
     }   
    
   public Set getMarkings(){
        return new HashSet(_markings);
    }

    public int size() {
        return _markings.size();
    }

    public void removeAll(){
    	_markings.clear();
    }
    
    public void removeMarking(RMarking marking){
        _markings.remove(marking); 
    }
    
    public void addAll(RSetOfMarkings newmarkings){
        _markings.addAll(newmarkings.getMarkings());	
   
    }
    
    public boolean equals(RSetOfMarkings markings) {
    Set markingsToCompare = markings.getMarkings();
    if (_markings.size() != markingsToCompare.size()) {
      return false;
    }
    if (_markings.containsAll(markingsToCompare)
        && (markingsToCompare.containsAll(_markings))) {
      return true;
    }
    return false;
  }
      
    public boolean contains(RMarking m)
    {
     for (Iterator i = _markings.iterator(); i.hasNext();)
     {
       RMarking currentM = (RMarking) i.next();	
       if (currentM.equals(m))
       { return true;
       }
     }  
    	
     return false;
   	
    }
    
    public boolean containsBiggerEqual(RMarking m)
    {
     for (Iterator i = _markings.iterator(); i.hasNext();)
     {
       RMarking currentM = (RMarking) i.next();	
       if (currentM.isBiggerThanOrEqual(m))
       { return true;
       }
     }  
    return false;
   }
 }