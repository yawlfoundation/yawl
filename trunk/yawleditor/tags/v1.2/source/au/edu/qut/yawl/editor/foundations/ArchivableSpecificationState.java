/*
 * Created on 27/02/2004
 * YAWLEditor v1.0 
 *
 * @author Lindsay Bradford
 * 
 * Copyright (C) 2003 Queensland University of Technology
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

import java.util.HashSet;

import au.edu.qut.yawl.editor.net.NetGraphModel;
import au.edu.qut.yawl.editor.specification.SpecificationModel;
import au.edu.qut.yawl.editor.swing.YAWLEditorDesktop;

public class ArchivableSpecificationState implements Serializable {

  private transient SpecificationModel specificationModel;
  private HashSet nets = new HashSet();
  private HashSet decompositions;
  private Dimension size;
  private String dataTypeDefinition;
  private int    fontSize;

  public ArchivableSpecificationState() {}

  public ArchivableSpecificationState(SpecificationModel specificationModel) {
    this.specificationModel = specificationModel;
    
    collateNetData();
    setSize(YAWLEditorDesktop.getInstance().getPreferredSize());
    setDataTypeDefinition(
        XMLUtilities.quoteXML(
            specificationModel.getDataTypeDefinition()
        )
    );
    setDecompositions((HashSet) specificationModel.getUsedDecompositions());
    setFontSize(specificationModel.getFontSize());
  }
  
  private void collateNetData() {
    Object[] netArray = specificationModel.getNets().toArray();
    
    for(int i=0; i < netArray.length; i++) {
      NetGraphModel netModel = (NetGraphModel) netArray[i];
      ArchivableNetState archivableNet = new ArchivableNetState(netModel);
      nets.add(archivableNet);
    }
  }
  
  public void setNets(HashSet nets) {
    this.nets = nets;
  }
  
  public HashSet getNets() {
    return this.nets;
  }
  
  public void setSize(Dimension size) {
  	this.size = size;
  }
  
  public Dimension getSize() {
  	return this.size;
  }
  
  public void setDataTypeDefinition(String dataTypeDefinition) {
    this.dataTypeDefinition = dataTypeDefinition;
  }
  
  public String getDataTypeDefinition() {
    return dataTypeDefinition;
  }
  
  public void setDecompositions(HashSet decompositions) {
    this.decompositions = decompositions;
  }
  
  public HashSet getDecompositions() {
    return this.decompositions;
  }
  
  public void setFontSize(int fontSize) {
    this.fontSize = fontSize;
  }
  
  public int getFontSize() {
    return this.fontSize;
  }
}
