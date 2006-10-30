/* 
* This file is made available under the terms of the LGPL licence. 
* This licence can be retreived from http://www.gnu.org/copyleft/lesser.html. 
* The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of 
* individuals and organisations who are commited to improving workflow technology. 
* 
*/ 
package au.edu.qut.yawl.elements; 

import javax.persistence.Column; 
import javax.persistence.Entity; 
import javax.persistence.Id; 

/** 
* Stores the specification and its highest version. 
* Author Lachlan Aldred 
* Date: 24/10/2006 
* Time: 14:45:48 
*/ 
@Entity 
public class SpecVersion { 
   private String _specURI; 
   private Integer _highestVersion; 


   public SpecVersion(String specURI, Integer highestVersion) { 
       _specURI = specURI; 
       _highestVersion = highestVersion; 
   } 

   /** 
    * @return the id 
    */ 
   @Column(name="uri") 
   @Id 
   public String getSpecURI() { 
       return _specURI; 
   } 


   /** 
    * Method inserted for hibernate. 
    * @param id 
    */ 
   @SuppressWarnings({"UNUSED_SYMBOL"}) 
   private void setSpecURI( String id ) { 
       _specURI = id; 
   } 

   @Column(name="highestversion") 
   public Integer getHighestVersion() { 
       return _highestVersion; 
   } 

   @SuppressWarnings({"UNUSED_SYMBOL"}) 
   public void setHighestVersion(Integer _highestVersion) { 
       this._highestVersion = _highestVersion; 
   } 

   public String toString() { 
       return _specURI + ":" + _highestVersion; 
   } 
} 
