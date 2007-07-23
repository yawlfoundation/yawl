/*
 * Created on 05/10/2003
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
 */

package au.edu.qut.yawl.editor.specification;

import java.awt.Color;

import java.util.LinkedList;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;

import au.edu.qut.yawl.editor.data.Decomposition;
import au.edu.qut.yawl.editor.data.DataVariable;
import au.edu.qut.yawl.editor.data.WebServiceDecomposition;
import au.edu.qut.yawl.editor.net.NetGraphModel;
import au.edu.qut.yawl.editor.net.utilities.NetCellUtilities;
import au.edu.qut.yawl.editor.net.utilities.NetUtilities;
import au.edu.qut.yawl.editor.net.NetGraph;

import au.edu.qut.yawl.editor.YAWLEditor;
import au.edu.qut.yawl.editor.swing.undo.UndoableDecompositionLabelChange;
import au.edu.qut.yawl.editor.swing.undo.UndoableNetFrameTitleChange;
import au.edu.qut.yawl.editor.swing.undo.UndoableFontSizeChange;
import au.edu.qut.yawl.editor.swing.undo.UndoableNetDeletion;
import au.edu.qut.yawl.editor.swing.undo.UndoableNetAddition;
import au.edu.qut.yawl.editor.swing.undo.UndoableStartingNetChange;

import au.edu.qut.yawl.editor.thirdparty.engine.YAWLEngineProxy;

import au.edu.qut.yawl.editor.elements.model.SplitDecorator;
import au.edu.qut.yawl.editor.elements.model.YAWLCompositeTask;
import au.edu.qut.yawl.editor.elements.model.YAWLTask;
import au.edu.qut.yawl.editor.elements.model.YAWLMultipleInstanceTask;

import au.edu.qut.yawl.editor.foundations.XMLUtilities;

public class SpecificationModel {
  public static final int NO_NETS_EXIST     = 0;
  public static final int NETS_EXIST        = 1;
  public static final int NO_NET_SELECTED   = 2;
  public static final int SOME_NET_SELECTED = 3;

  private int netCount;
  private HashSet<NetGraphModel> nets;
  private int state;
  
  public static final int   DEFAULT_FONT_SIZE = 15;
  public static final int   DEFAULT_NET_BACKGROUND_COLOR = Color.WHITE.getRGB();
  
  public static final String DEFAULT_TYPE_DEFINITION = 
    "<schema xmlns=\"http://www.w3.org/2001/XMLSchema\">\n\n</schema>";
  
  private String dataTypeDefinition = DEFAULT_TYPE_DEFINITION;
  
  private transient LinkedList subscribers = new LinkedList();
  
  private HashSet<WebServiceDecomposition> webServiceDecompositions = new HashSet<WebServiceDecomposition>();
  private long    uniqueElementNumber = 0;
  private int     fontSize            = DEFAULT_FONT_SIZE;
  private int     defaultNetBackgroundColor =  DEFAULT_NET_BACKGROUND_COLOR;
  private String  name                = "";
  private String  description         = "No description has been given.";
  private String  id                 = "";
  private String  author              = System.getProperty("user.name");
  private String  versionNumber       = "0.1";
  private String  validFromTimestamp  = "";
  private String  validUntilTimestamp = "";
  
  private transient static final SpecificationModel INSTANCE = new SpecificationModel();
  
  public SpecificationModel() {
    reset();
  }
  
  public static SpecificationModel getInstance() {
    return INSTANCE; 
  }
  
  public void subscribe(final SpecificationModelListener subscriber) {
    subscribers.add(subscriber);
    subscriber.updateState(state);
  }

  public void publishState(final int inputState) {
    state = inputState;
    for(int i = 0; i < subscribers.size();  i++) {
      SpecificationModelListener listener = 
        (SpecificationModelListener) subscribers.get(i);
      listener.updateState(state);
    }
  }
  
  public void setState(int state) {
    this.state = state;
  }
  
  public int getState() {
    return this.state;
  }
  
  public void reset() {
    state = NO_NETS_EXIST;
    netCount = 0;
    nets = new HashSet();
    webServiceDecompositions = new HashSet();
    fontSize = DEFAULT_FONT_SIZE;
    defaultNetBackgroundColor = DEFAULT_NET_BACKGROUND_COLOR;
    setFileName("");
    setEngineFileName("");
    setDataTypeDefinition(DEFAULT_TYPE_DEFINITION);
    setUniqueElementNumber(0);
    
    setName("");
    setDescription("No description has been given.");
    setId("");
    setAuthor(System.getProperty("user.name"));
    setVersionNumber("0.1");
    setValidFromTimestamp("");
    setValidUntilTimestamp("");
    YAWLEditor.setStatusBarText("Open or create a net to begin.");
  }
  
  public void setNetCount(int netCount) {
    this.netCount = netCount;
  }
  
  public int getNetCount() {
    return this.netCount;
  }
  
  public void setNets(HashSet nets) {
    this.nets = nets;
  }

  public Set<NetGraphModel> getNets() {
    return nets;
  }
  
  public NetGraphModel getStartingNet() {
    Object[] netSetArray = nets.toArray();
    for(int i = 0; i < netSetArray.length; i++) {
      NetGraphModel thisNet = (NetGraphModel) netSetArray[i];
      if (thisNet.isStartingNet()) {
        return thisNet;
      }
    }
    return null;
  }
  
  public Set getSubNets() {
    HashSet subNets = new HashSet();
    Object[] netSetArray = nets.toArray();
    for(int i = 0; i < netSetArray.length; i++) {
      NetGraphModel thisNet = (NetGraphModel) netSetArray[i];
      if (!thisNet.isStartingNet()) {
        subNets.add(thisNet);
      }
    }
    return subNets;
  }
  
  public void setStartingNet(NetGraphModel newStartingNet) {
    NetGraphModel oldStartingNet = getStartingNet();
    oldStartingNet.setIsStartingNet(false);
    newStartingNet.setIsStartingNet(true);
    
    SpecificationUndoManager.getInstance().startCompoundingEdits();
    newStartingNet.postEdit(
        new UndoableStartingNetChange(
               newStartingNet,
               oldStartingNet
        )
    );
    SpecificationUndoManager.getInstance().stopCompoundingEdits();
  }
  

  public void addNet(NetGraphModel netModel) {
    SpecificationUndoManager.getInstance().startCompoundingEdits(netModel);

    addNetNotUndoable(netModel);
    if (getStartingNet() != null) { // can be null on specification load
      getStartingNet().postEdit(
          new UndoableNetAddition(netModel)
      );
    }
    
    SpecificationUndoManager.getInstance().stopCompoundingEdits();
  }
  
  public void addNetNotUndoable(NetGraphModel netModel) {
    if (netCount == 0) {
      netModel.setIsStartingNet(true);
    }
    if (nets.add(netModel)) {
      publishNetCountIncrement();
    }
  }
  
  public void removeNet(NetGraphModel netModel) {
    if (removeNetNotUndoable(netModel)) {
      SpecificationUndoManager.getInstance().startCompoundingEdits(netModel);

      HashSet changedTasks = resetUnfoldingCompositeTasks(netModel);
      boolean wasStartingNet = netModel.isStartingNet();
      NetGraphModel newStartingNet = selectAnotherStartingNet(netModel);

      getStartingNet().postEdit(
          new UndoableNetDeletion(netModel, 
                                  wasStartingNet,
                                  newStartingNet,
                                  changedTasks)
      );
      SpecificationUndoManager.getInstance().stopCompoundingEdits();
    }
  }
  
  public boolean removeNetNotUndoable(NetGraphModel netModel) {
    boolean removalSuccessful = false;
    removalSuccessful = nets.remove(netModel);
    if (removalSuccessful) {
      publishNetCountDecrement();
    }
    return removalSuccessful;
  }
  
  private NetGraphModel selectAnotherStartingNet(NetGraphModel netModel) {
    if (netCount > 0 && netModel.isStartingNet()) {
      netModel.setIsStartingNet(false);
      ((NetGraphModel) nets.toArray()[0]).setIsStartingNet(true);
      return (NetGraphModel) nets.toArray()[0];
    }
    return null;
  }

  private void publishNetCountIncrement() {
    final int oldNetCount = netCount;
    netCount++;
    if (oldNetCount == 0) {
      publishState(NETS_EXIST);    
    }
  }

  private void publishNetCountDecrement() {
    final int oldNetCount = netCount;
    netCount--;
    if (oldNetCount == 1)  {
        publishState(NO_NETS_EXIST);    
    }
  }
  
  public HashSet<YAWLCompositeTask> resetUnfoldingCompositeTasks(NetGraphModel netModel) {

    HashSet<YAWLCompositeTask> changedTasks = new HashSet<YAWLCompositeTask>();
    
    for (NetGraphModel net: nets) {
      for (YAWLCompositeTask compositeTaskOfNet: NetUtilities.getCompositeTasks(net)) {
        if (compositeTaskOfNet.getDecomposition() != null && 
            compositeTaskOfNet.getDecomposition().equals(netModel.getDecomposition())) {
          net.getGraph().setUnfoldingNet(compositeTaskOfNet, null);
          changedTasks.add(compositeTaskOfNet);
        }
      }
    }
    return changedTasks;
  }
  
  public void somethingSelected() {
    publishState(SOME_NET_SELECTED);  
  }
  
  public void nothingSelected() {
    if (state != NO_NETS_EXIST) {
      publishState(NO_NET_SELECTED); 
    }
  }
  
  public NetGraphModel getNetModelFromName(String name) {
    if (name == null || name.equals("")) {
      return null;
    }
    Object[] netSetArray = nets.toArray();
    for(int i = 0; i < netSetArray.length; i++) {
      NetGraphModel thisNet = (NetGraphModel) netSetArray[i];
      if (thisNet.getName().equals(name)) {
        return thisNet;
      }
    }
    return null;
  }
  
  public String getFileName() {
    return SpecificationFileModel.getInstance().getFileName();
  }
  
  public void setFileName(String fileName) {
    SpecificationFileModel.getInstance().setFileName(fileName);
  }

  public String getEngineFileName() {
    return SpecificationFileModel.getInstance().getEngineFileName();
  }
  
  public void setEngineFileName(String fileName) {
    SpecificationFileModel.getInstance().setEngineFileName(fileName);
  }
  
  public boolean isValidNewDecompositionName(String name) {
    if (name == null) {
      return false;
    }

    boolean nameIsValid = true;
    
    nameIsValid = (getNetModelFromName(name) == null) ? true : false;
    
    if (nameIsValid) {
      nameIsValid = (getDecompositionFromLabel(name) == null) ? true : false;
    }
    
    return nameIsValid;
  }
  
  public String getDataTypeDefinition() {
   return this.dataTypeDefinition;
  }
  
  public void setDataTypeDefinition(String dataTypeDefinition) {
    this.dataTypeDefinition = dataTypeDefinition;
    YAWLEngineProxy.getInstance().setDataTypeSchema(dataTypeDefinition);
  }   
  
  public boolean hasValidDataTypeDefinition() {
    return YAWLEngineProxy.getInstance().hasValidDataTypeDefinition();
  }
  
  public Set getDataTypes() {
    return YAWLEngineProxy.getInstance().getPrimarySchemaTypeNames();
  }
  
  public HashSet<WebServiceDecomposition> getWebServiceDecompositions() {
    return this.webServiceDecompositions;
  }
  
  public HashSet<WebServiceDecomposition> getUsedWebServiceDecompositions() {
    HashSet<WebServiceDecomposition> usedDecompositions = new HashSet<WebServiceDecomposition>();

    for(NetGraphModel net: nets) {
      
      for(Object taskAsObject: NetUtilities.getAllTasks(net)) {
        YAWLTask task = (YAWLTask) taskAsObject;
        if (task.getDecomposition()!= null  && 
            task.getDecomposition() instanceof WebServiceDecomposition) {
          usedDecompositions.add((WebServiceDecomposition) task.getDecomposition());
        }
      }
    }
    return usedDecompositions;
  }
  
  public void setWebServiceDecompositions(HashSet<WebServiceDecomposition> decompositions) {
    this.webServiceDecompositions = decompositions;
  }
  
  public void addWebServiceDecomposition(WebServiceDecomposition decomposition) {
    webServiceDecompositions.add(decomposition);
  }
  
  public Decomposition getDecompositionFromLabel(String label) {
    for(Decomposition decomposition: webServiceDecompositions) {
      if (decomposition == null) {
        continue;
      }
      if (decomposition.getLabel().equals(label)) {
        return decomposition;
      }
    }
    return null;
  }
  
  public boolean isValidDecompositionLabel(String label) {
    Iterator decompositionIterator = webServiceDecompositions.iterator();
    while(decompositionIterator.hasNext()) {
      Decomposition decomposition = (Decomposition) decompositionIterator.next();
      if (decomposition.getLabelAsElementName().equals(XMLUtilities.toValidXMLName(label))) {
        return false;
      }
    }
    Iterator netIterator = nets.iterator();
    while(netIterator.hasNext()) {
      NetGraphModel net = (NetGraphModel) netIterator.next();
      if (net.getDecomposition().getLabelAsElementName().equals(XMLUtilities.toValidXMLName(label))) {
        return false;
      }
    }
    return true;
  }
  
  public void propogateDecompositionLabelChange(Decomposition decomposition, String oldLabel) {
    SpecificationUndoManager.getInstance().startCompoundingEdits(getStartingNet());
    
    Iterator netIterator = nets.iterator();
    NetGraphModel netModel = null;
    
    while(netIterator.hasNext()) {
      netModel = (NetGraphModel) netIterator.next();
      NetGraph net = netModel.getGraph();
      
      if (netModel.getDecomposition().equals(decomposition)) {
        net.getFrame().setTitle(decomposition.getLabel());
        netModel.postEdit(
            new UndoableNetFrameTitleChange(
                netModel.getGraph().getFrame(), 
                oldLabel, 
                netModel.getName()
            )
          );    
      }
    }
    
    changeDecompositionInQueries(oldLabel, decomposition.getLabel());

    // post the decomposition edit to the last net.

    if (netModel != null) {
      netModel.postEdit(
        new UndoableDecompositionLabelChange(
              decomposition, 
              oldLabel, 
              decomposition.getLabel()
        )
      );    
    }

    SpecificationUndoManager.getInstance().stopCompoundingEdits();
  }
  
  public void changeDecompositionInQueries(String oldLabel, String newLabel) {
    Iterator netIterator = nets.iterator();
    NetGraphModel netModel = null;

    while(netIterator.hasNext()) {
      netModel = (NetGraphModel) netIterator.next();

      Iterator taskIterator = NetUtilities.getAllTasks(netModel).iterator();
      while(taskIterator.hasNext()) {
        YAWLTask task = (YAWLTask) taskIterator.next();
        task.getParameterLists().changeDecompositionInQueries(oldLabel,newLabel);
        if (task.hasSplitDecorator()) {
          SplitDecorator decorator = task.getSplitDecorator();
          decorator.changeDecompositionInPredicates(oldLabel, newLabel);
        }
      }
    }
  }
  
  public void changeVariableNameInQueries(DataVariable variable,
                                          String oldVariableName, 
                                          String newVariableName) {
    if (oldVariableName.equals(newVariableName)) {
      return;
    }
    
    Decomposition variableDecomposition = variable.getScope().getDecomposition();

    Iterator netIterator = nets.iterator();
    NetGraphModel netModel = null;

    while(netIterator.hasNext()) {
      netModel   = (NetGraphModel) netIterator.next();
      
      if (netModel.getDecomposition().equals(variableDecomposition)) {
        Iterator taskIterator = NetUtilities.getAllTasks(netModel).iterator();
        while(taskIterator.hasNext()) {
          YAWLTask task = (YAWLTask) taskIterator.next();
          task.getParameterLists().getInputParameters().changeVariableNameInQueries(
                oldVariableName, 
                newVariableName
          );
          if (task.hasSplitDecorator()) {
            SplitDecorator decorator = task.getSplitDecorator();
            decorator.changeVariableNameInPredicates(oldVariableName, newVariableName);
          }
        }
      }
      
      Iterator taskIterator = NetUtilities.getAllTasks(netModel).iterator();
      while(taskIterator.hasNext()) {
        YAWLTask task = (YAWLTask) taskIterator.next();
        if (task.getDecomposition() != null && 
            task.getDecomposition().equals(variableDecomposition)) {
          task.getParameterLists().getOutputParameters().changeVariableNameInQueries(
              oldVariableName, 
              newVariableName
          );
        }
      }
    }
  }
  
  public void propogateVariableDeletion(DataVariable variable) {
    Iterator netIterator = nets.iterator();
    NetGraphModel netModel = null;
    
    while(netIterator.hasNext()) {
      netModel = (NetGraphModel) netIterator.next();
      Iterator taskIterator = NetUtilities.getAllTasks(netModel).iterator();
      while(taskIterator.hasNext()) {
        YAWLTask task = (YAWLTask) taskIterator.next();
        task.getParameterLists().remove(variable);
        if (task instanceof YAWLMultipleInstanceTask) {
          YAWLMultipleInstanceTask multiInstanceTask = 
            (YAWLMultipleInstanceTask) task;
          
          if (multiInstanceTask.getMultipleInstanceVariable() != null &&
              multiInstanceTask.getMultipleInstanceVariable().equals(variable)) {
            multiInstanceTask.setMultipleInstanceVariable(null);
          }

          if (multiInstanceTask.getResultNetVariable() != null &&
              multiInstanceTask.getResultNetVariable().equals(variable)) {
            multiInstanceTask.setResultNetVariable(null);
          }
        }
      }
    }
  }
  
  public void propogateVariableSetChange(Decomposition decomposition) {
    Iterator netIterator = nets.iterator();
    NetGraphModel netModel = null;
    
    while(netIterator.hasNext()) {
      netModel = (NetGraphModel) netIterator.next();
      Iterator taskIterator = NetUtilities.getAllTasks(netModel).iterator();
      while(taskIterator.hasNext()) {
        YAWLTask task = (YAWLTask) taskIterator.next();
        if (task.getDecomposition() == null) {
          continue;
        }
        if (task.getDecomposition().equals(decomposition)) {
          task.removeInvalidParameters();    
        }

        // Only worklists are allocated to human beings. Remove any
        // resource allocations made to people if the task no
        // longer is done by a person.
        
        if (!task.getDecomposition().invokesWorklist()) {
          task.setAllocationResourceMapping(null);
        }
      }
    }
  }

  public void undoableSetFontSize(int oldSize, int newSize) {
    
    if (oldSize == newSize) {
      return;
    }
    
    SpecificationUndoManager.getInstance().startCompoundingEdits(getStartingNet());

    setFontSize(newSize);
    
    NetGraphModel netModel = null;
    
    Iterator netIterator = nets.iterator();
    while(netIterator.hasNext()) {
      netModel = (NetGraphModel) netIterator.next();
      NetGraph net = netModel.getGraph();
      NetCellUtilities.propogateFontChangeAcrossNet(
          net, 
          net.getFont().deriveFont((float) newSize)
     );
    }

    // post the font size edit to the last net.
    
    if (netModel != null) {
      netModel.postEdit(
        new UndoableFontSizeChange(
            oldSize,
            newSize
        )
      );
    }
    
    SpecificationUndoManager.getInstance().stopCompoundingEdits();
  }
  
  public void setFontSize(int size) {
    this.fontSize = size;
  }
  
  public int getFontSize() {
    return this.fontSize;
  }
  
  public void showAntiAliasing(boolean antiAliased) {
    NetGraphModel netModel = null;

    Iterator netIterator = nets.iterator();
    while(netIterator.hasNext()) {
      netModel = (NetGraphModel) netIterator.next();
      NetGraph net = netModel.getGraph();
      net.setAntiAliased(antiAliased);
    }
  }
  
  public void setDefaultNetBackgroundColor(int color) {
    defaultNetBackgroundColor = color;
  }
  
  public int getDefaultNetBackgroundColor() {
    return this.defaultNetBackgroundColor;
  }

  public void showNetGrid(boolean gridVisible) {
    NetGraphModel netModel = null;

    Iterator netIterator = nets.iterator();
    while(netIterator.hasNext()) {
      netModel = (NetGraphModel) netIterator.next();
      NetGraph net = netModel.getGraph();
      net.setGridVisible(gridVisible);
    }
  }
  
  public void refreshNetViews() {
    for (NetGraphModel net : nets) {
      net.getGraph().repaint();      
    }
  }


  public void setName(String name) {
    this.name = name;
  }
  
  public String getName() {
    return this.name;
  }

  public void setDescription(String description) {
    this.description = description;
  }
  
  public String getDescription() {
    return this.description;
  }
  
  public void setId(String id) {
    this.id = id;
  }
  
  public String getId() {
    if (this.id == null || this.id.trim().equals("")) {
      this.id = XMLUtilities.fileNameToURI(this.getFileName());
    }
    return this.id;
  }
  
  public void setAuthor(String author) {
    this.author = author;
  }
  
  public String getAuthor() {
    return this.author;
  }

  public void setVersionNumber(String versionNumber) {
    this.versionNumber = versionNumber;
  }
  
  public String getVersionNumber() {
    return this.versionNumber;
  }
  
  public void setValidFromTimestamp(String timestamp) {
    this.validFromTimestamp = timestamp;
  }
  
  public String getValidFromTimestamp() {
    return this.validFromTimestamp;
  }

  public void setValidUntilTimestamp(String timestamp) {
    this.validUntilTimestamp = timestamp;
  }
  
  public String getValidUntilTimestamp() {
    return this.validUntilTimestamp;
  }

  
  public void setUniqueElementNumber(long elementNumber) {
    this.uniqueElementNumber = elementNumber;
  }
  
  public long getUniqueElementNumber() {
    this.uniqueElementNumber++;
    return this.uniqueElementNumber;
  }
}
