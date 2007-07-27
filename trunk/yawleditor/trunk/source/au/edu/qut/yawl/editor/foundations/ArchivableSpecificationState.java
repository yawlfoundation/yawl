/*
 * Created on 27/02/2004
 * YAWLEditor v1.0 
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
 *
 */

package au.edu.qut.yawl.editor.foundations;

import java.io.Serializable;

import java.awt.Dimension;
import java.awt.Rectangle;

import java.util.HashSet;
import java.util.HashMap;

import au.edu.qut.yawl.editor.YAWLEditor;
import au.edu.qut.yawl.editor.net.NetGraphModel;
import au.edu.qut.yawl.editor.specification.SpecificationModel;
import au.edu.qut.yawl.editor.specification.SpecificationUtilities;
import au.edu.qut.yawl.editor.swing.YAWLEditorDesktop;

public class ArchivableSpecificationState implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private transient SpecificationModel specificationModel;
  
  /* ALL attributes of this object are to be stored in 
   * serializationProofAttributeMap, meaning we won't get problems
   * with incompatible XML serializations as we add new attributes
   * in the future. 
   */
  
  private HashMap serializationProofAttributeMap = new HashMap();

  public ArchivableSpecificationState() {
    setFontSize(15);
    setDefaultNetBackgroundColor(SpecificationModel.getInstance().DEFAULT_NET_BACKGROUND_COLOR);
    setUniqueElementNumber(0);
  }

  public ArchivableSpecificationState(SpecificationModel specificationModel) {
    this.specificationModel = specificationModel;
    
    collateNetData();
    setSize(YAWLEditorDesktop.getInstance().getPreferredSize());
    setDataTypeDefinition(
        XMLUtilities.quoteXML(
            specificationModel.getDataTypeDefinition()
        )
    );
    setName(specificationModel.getName());
    setId(specificationModel.getId());
    setDescription(specificationModel.getDescription());
    setAuthor(specificationModel.getAuthor());
    
    setVersionNumber(specificationModel.getVersionNumber());
    setValidFromTimestamp(specificationModel.getValidFromTimestamp());
    setValidUntilTimestamp(specificationModel.getValidUntilTimestamp());
    
    setDecompositions(
        SpecificationUtilities.getUsedWebServiceDecompositions(
            specificationModel
        )
    );
    
    setFontSize(specificationModel.getFontSize());
    setDefaultNetBackgroundColor(specificationModel.getDefaultNetBackgroundColor());
    setUniqueElementNumber(specificationModel.getUniqueElementNumber());
    setBounds(YAWLEditor.getInstance().getBounds());
  }
  
  public void setSerializationProofAttributeMap(HashMap map) {
    this.serializationProofAttributeMap = map;
  }
  
  public HashMap getSerializationProofAttributeMap() {
    return this.serializationProofAttributeMap;
  }
  
  private void collateNetData() {
    HashSet nets = new HashSet();
    Object[] netArray = specificationModel.getNets().toArray();
    
    for(int i=0; i < netArray.length; i++) {
      NetGraphModel netModel = (NetGraphModel) netArray[i];
      ArchivableNetState archivableNet = new ArchivableNetState(netModel);
      nets.add(archivableNet);
    }
    setNets(nets);
  }
  
  public void setNets(HashSet nets) {
    serializationProofAttributeMap.put("nets",nets);
  }
  
  public HashSet getNets() {
    return (HashSet) serializationProofAttributeMap.get("nets");
  }
  
  public void setSize(Dimension size) {
    serializationProofAttributeMap.put("size",size);
  }
  
  public Dimension getSize() {
    return (Dimension) serializationProofAttributeMap.get("size");
  }
  
  public void setDataTypeDefinition(String dataTypeDefinition) {
    serializationProofAttributeMap.put("dataTypeDefinition",dataTypeDefinition);
  }
  
  public String getDataTypeDefinition() {
    return (String) serializationProofAttributeMap.get("dataTypeDefinition");
  }
  
  public void setDecompositions(HashSet decompositions) {
    serializationProofAttributeMap.put("decompositions",decompositions);
  }
  
  public HashSet getDecompositions() {
    return (HashSet) serializationProofAttributeMap.get("decompositions");
  }
  
  public void setFontSize(int fontSize) {
    serializationProofAttributeMap.put("fontSize",new Integer(fontSize));
  }
  
  public int getFontSize() {
    return ((Integer) serializationProofAttributeMap.get("fontSize")).intValue();
  }

  public void setDefaultNetBackgroundColor(int netBackgroundColor) {
    serializationProofAttributeMap.put("defaultNetBackgroundColor",new Integer(netBackgroundColor));
  }
  
  public int getDefaultNetBackgroundColor() {
    return ((Integer) serializationProofAttributeMap.get("defaultNetBackgroundColor")).intValue();
  }
  
  public void setName(String specificationName) {
    serializationProofAttributeMap.put("name",specificationName);
  }
  
  public String getName() {
    return (String) serializationProofAttributeMap.get("name");
  }

  public void setDescription(String description) {
    serializationProofAttributeMap.put("description",description);
  }
  
  public String getDescription() {
    return (String) serializationProofAttributeMap.get("description");
  }
  
  public void setId(String id) {
    serializationProofAttributeMap.put("id",id);
  }
  
  public String getId() {
    return (String) serializationProofAttributeMap.get("id");
  }

  public void setAuthor(String author) {
    serializationProofAttributeMap.put("author",author);
  }
  
  public String getAuthor() {
    return (String) serializationProofAttributeMap.get("author");
  }

  public void setVersionNumber(String versionNumber) {
    serializationProofAttributeMap.put("versionNumber",versionNumber);
  }
  
  public String getVersionNumber() {
    return (String) serializationProofAttributeMap.get("versionNumber");
  }
  
  public void setValidFromTimestamp(String timestamp) {
    serializationProofAttributeMap.put("validFromTimestamp",timestamp);
  }
  
  public String getValidFromTimestamp() {
    return (String) serializationProofAttributeMap.get("validFromTimestamp");
  }

  public void setValidUntilTimestamp(String timestamp) {
    serializationProofAttributeMap.put("validUntilTimestamp",timestamp);
  }
  
  public String getValidUntilTimestamp() {
    return (String) serializationProofAttributeMap.get("validUntilTimestamp");
  }

  public void setUniqueElementNumber(long uniqueElementNumber) {
    serializationProofAttributeMap.put("uniqueElementNumber",new Long(uniqueElementNumber));
  }
  
  public long getUniqueElementNumber() {
    return ((Long) serializationProofAttributeMap.get("uniqueElementNumber")).longValue();
  }

  public void setBounds(Rectangle bounds) {
    serializationProofAttributeMap.put("bounds",bounds);
  }

  public Rectangle getBounds() {
    return (Rectangle) serializationProofAttributeMap.get("bounds");
  }
}
