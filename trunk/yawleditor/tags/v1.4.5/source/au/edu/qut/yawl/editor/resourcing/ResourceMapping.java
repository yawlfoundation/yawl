/*
 * Created on 10/06/2005
 * YAWLEditor v1.3 
 *
 * @author Lindsay Bradford
 * 
 * 
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
 */

package au.edu.qut.yawl.editor.resourcing;

import java.io.Serializable;

/**
 * @author Lindsay Bradford
 */

public class ResourceMapping implements Serializable, Cloneable {
   /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public static final int NO_MAPPING = 0;

   public static final int ALLOCATE_TO_ANYONE = 1;
   public static final int ALLOCATE_DIRECTLY = 2;
   public static final int ALLOCATE_TO_ROLE = 3;

   public static final int AUTHORISATION_UNNECESSARY = 4;
   public static final int AUTHORISED_DIRECTLY = 5;
   public static final int AUTHORISATION_VIA_ROLE = 6;

   private int mappingType = NO_MAPPING;
   private String identifier;
   private String label;

   public static final ResourceMapping DEFAULT_ALLOCATION = 
      new ResourceMapping(ALLOCATE_TO_ANYONE, null, null);

   public static final ResourceMapping DEFAULT_AUTHORISATION = 
     new ResourceMapping(AUTHORISATION_UNNECESSARY, null, null);
   
   public ResourceMapping() {}
   
   public ResourceMapping(int mappingType, String identifier, String label) {
     setMappingType(mappingType);
     setIdentifier(identifier);
     setLabel(identifier);
   }

   public void setMappingType(int mappingType) {
     this.mappingType = mappingType;
   }
   
   public int getMappingType() {
     return this.mappingType;
   }
   
   public void setIdentifier(String identifier) {
     this.identifier = identifier;
   }
   
   public String getIdentifier() {
     return this.identifier;
   }
    
   public void setLabel(String label) {
     this.label = label;
   }
   
   public String getLabel() {
     return this.label;
   }
   
   public Object clone() {
     ResourceMapping clone = new ResourceMapping();

     clone.setMappingType(getMappingType());
     clone.setIdentifier(getIdentifier());
     clone.setLabel(getLabel());

     return clone;
   }
   
   public String toString() {
     return super.toString() + "\n" +
     "  mappingType: " + getMappingType() + "\n" +
     "  identifier: " + getIdentifier() + "\n" +
     "  label: " + getLabel() + "\n";
   }
}
