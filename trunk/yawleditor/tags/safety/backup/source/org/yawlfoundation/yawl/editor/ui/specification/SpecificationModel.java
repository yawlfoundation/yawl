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

package org.yawlfoundation.yawl.editor.ui.specification;

import org.yawlfoundation.yawl.editor.core.YConnector;
import org.yawlfoundation.yawl.editor.core.YSpecificationHandler;
import org.yawlfoundation.yawl.editor.core.identity.ElementIdentifiers;
import org.yawlfoundation.yawl.editor.core.identity.EngineIdentifier;
import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.data.DataSchemaValidator;
import org.yawlfoundation.yawl.editor.ui.data.DataVariable;
import org.yawlfoundation.yawl.editor.ui.data.Decomposition;
import org.yawlfoundation.yawl.editor.ui.data.WebServiceDecomposition;
import org.yawlfoundation.yawl.editor.ui.elements.model.SplitDecorator;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLCompositeTask;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLMultipleInstanceTask;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.ui.engine.AnalysisResultsParser;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.net.NetGraphModel;
import org.yawlfoundation.yawl.editor.ui.net.utilities.NetUtilities;
import org.yawlfoundation.yawl.editor.ui.properties.PropertiesLoader;
import org.yawlfoundation.yawl.editor.ui.resourcing.InvalidResourceReference;
import org.yawlfoundation.yawl.editor.ui.resourcing.ResourceMapping;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.Publisher;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.SpecificationState;
import org.yawlfoundation.yawl.editor.ui.swing.specification.ProblemMessagePanel;
import org.yawlfoundation.yawl.editor.ui.swing.undo.*;
import org.yawlfoundation.yawl.editor.ui.util.LogWriter;
import org.yawlfoundation.yawl.editor.ui.util.UserSettings;
import org.yawlfoundation.yawl.elements.YSpecVersion;
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.resourcing.resource.Role;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class SpecificationModel {

    private int netCount;
  private Set<NetGraphModel> nets;

  public static final int   DEFAULT_FONT_SIZE = 15;
  public static final int   DEFAULT_NET_BACKGROUND_COLOR = Color.WHITE.getRGB();
    public static final String DEFAULT_WORKLIST_LABEL = "Default Engine Worklist";
    public static final String ENGINE_WORKLIST_NAME = "DefaultWorklist";

  public static final String DEFAULT_TYPE_DEFINITION = 
    "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n\n</xs:schema>";
  
  private String dataTypeDefinition = DEFAULT_TYPE_DEFINITION;

    private static boolean _loadInProgress = false;

    private static YSpecificationHandler _specificationHandler = new YSpecificationHandler();

    public static YSpecificationHandler getHandler() { return _specificationHandler; }

    public void loadFromFile(String fileName) throws IOException {
        _specificationHandler.load(fileName);
        reset();
    }


  private Set<WebServiceDecomposition> webServiceDecompositions;
  private ElementIdentifiers uniqueIdentifiers;
  private int     fontSize;
  private int     defaultNetBackgroundColor;
  private Color   defaultVertexBackground;
  private YSpecVersion versionNumber;
  private YSpecVersion prevVersionNumber;
  private boolean _versionChanged;
  private DataSchemaValidator _schemaValidator;
    private PropertiesLoader _propertiesLoader;


  private static final SpecificationModel INSTANCE = new SpecificationModel();
  
  private SpecificationModel() {
      nets = new HashSet<NetGraphModel>();
      webServiceDecompositions = new HashSet<WebServiceDecomposition>();
      uniqueIdentifiers = _specificationHandler.getIdentifiers();
      _propertiesLoader = new PropertiesLoader(this);
      reset();
  }
  
  public static SpecificationModel getInstance() {
    return INSTANCE; 
  }

    private Publisher getPublisher() { return Publisher.getInstance(); }
  
  public void reset() {
    netCount = 0;
    nets.clear();
    webServiceDecompositions.clear();
    uniqueIdentifiers.clear();
      _schemaValidator = new DataSchemaValidator();
    fontSize = DEFAULT_FONT_SIZE;
    defaultNetBackgroundColor = DEFAULT_NET_BACKGROUND_COLOR;
    defaultVertexBackground = getPreferredVertexBackground();
    setDataTypeDefinition(DEFAULT_TYPE_DEFINITION);

    setVersionNumber(new YSpecVersion("0.0"));
    YAWLEditor.setStatusBarText("Open or create a net to begin.");
      getPublisher().setSpecificationState(SpecificationState.NoNetsExist);
    prevVersionNumber = null;
      setVersionChanged(false);
  }

  private Color getPreferredVertexBackground() {
      return new Color(UserSettings.getSettings().getInt(
              "PREFERRED_VERTEX_BACKGROUND_COLOR", Color.WHITE.getRGB()));
  }

    private void setPreferredVertexBackground(Color color) {
        UserSettings.getSettings().putInt(
                "PREFERRED_VERTEX_BACKGROUND_COLOR", color.getRGB());
    }


    public void setLoadInProgress(boolean inProgress) { _loadInProgress = inProgress; }

    public boolean isLoadInProgress() { return _loadInProgress; }


  public Set<NetGraphModel> getNets() {
    return nets;
  }
  
  public Set<NetGraphModel> getSortedNets() {
      return new TreeSet<NetGraphModel>(nets);
  }
  
  public NetGraphModel getStartingNet() {
      for (NetGraphModel net : nets) {
          if (net.isStartingNet()) {
              return net;
          }
      }
      return null;
  }
  
  public Set<NetGraphModel> getSubNets() {
      Set<NetGraphModel> subNets = new HashSet<NetGraphModel>();
      for (NetGraphModel net : nets) {
          if (! net.isStartingNet()) {
              subNets.add(net);
          }
      }
      return subNets;
  }

  public NetGraphModel getNet(String id) {
      for (NetGraphModel net : nets) {
          if (net.getName().equals(id)) return net ;
      }
      return null;
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
      getPublisher().publishState(SpecificationState.NetDetailChanged);
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
    if (nets.isEmpty()) {
      netModel.setIsStartingNet(true);
    }
    if (nets.add(netModel)) {
        _propertiesLoader.setGraph(netModel.getGraph());
        getPublisher().publishAddNetEvent();
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
    boolean removalSuccessful = nets.remove(netModel);
    if (removalSuccessful) {
        getPublisher().publishRemoveNetEvent(nets.isEmpty());
    }
    return removalSuccessful;
  }
  
  private NetGraphModel selectAnotherStartingNet(NetGraphModel netModel) {
    if (! nets.isEmpty() && netModel.isStartingNet()) {
      netModel.setIsStartingNet(false);
      ((NetGraphModel) nets.toArray()[0]).setIsStartingNet(true);
      return (NetGraphModel) nets.toArray()[0];
    }
    return null;
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
      getPublisher().publishState(SpecificationState.NetSelected);
  }
  
  public void nothingSelected() {
    if (getPublisher().getSpecificationState() != SpecificationState.NoNetsExist) {
        getPublisher().publishState(SpecificationState.NoNetSelected);
    }
  }
  
  public String getFileName() {
    return _specificationHandler.getFileName();
  }
  
  public void setFileName(String fileName) {
     _specificationHandler.setFileName(fileName);
  }

  
  public boolean isValidNewDecompositionName(String name) {
    return (name != null) &&
           (SpecificationUtilities.getNetModelFromName(name) == null) &&
           (getDecompositionFromLabel(name) == null);
    }

  
  public String getDataTypeDefinition() {
      return (dataTypeDefinition.contains("\n")) ? dataTypeDefinition :
             DEFAULT_TYPE_DEFINITION ;
  }
  
  public void setDataTypeDefinition(String dataTypeDefinition) {
    this.dataTypeDefinition = dataTypeDefinition;
    _schemaValidator.setDataTypeSchema(dataTypeDefinition);
  }   
  
  public boolean hasValidDataTypeDefinition() {
    return _schemaValidator.hasValidDataTypeDefinition();
  }
  
  public Set getDataTypes() {
    return _schemaValidator.getPrimarySchemaTypeNames();
  }

  public boolean isDefinedTypeName(String typeName) {
      return _schemaValidator.isDefinedTypeName(typeName);
  }
  
  public Set<WebServiceDecomposition> getWebServiceDecompositions() {
    return this.webServiceDecompositions;
  }
  
  public void setWebServiceDecompositions(HashSet<WebServiceDecomposition> decompositions) {
    this.webServiceDecompositions = decompositions;
  }
  
  public void addWebServiceDecomposition(WebServiceDecomposition decomposition) {
    webServiceDecompositions.add(decomposition);
  }

    public void removeWebServiceDecomposition(WebServiceDecomposition decomposition) {
      webServiceDecompositions.remove(decomposition);
    }

    public void removeWebServiceDecomposition(String label) {
        WebServiceDecomposition decomposition =
                (WebServiceDecomposition) getDecompositionFromLabel(label);
        if (decomposition != null) {
            removeWebServiceDecomposition(decomposition);
        }
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
      getPublisher().publishState(SpecificationState.NetDetailChanged);
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
      while (taskIterator.hasNext()) {
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
          task.setResourceMapping(null);
        }
      }
    }
  }


  public void checkResourcingObjects() {
    if (YConnector.isResourceConnected()) {

      // get live object id lists from resource service
      List<String> pidList = YConnector.getParticipantIDs();
      List<String> ridList = YConnector.getRoleIDs();
      List<InvalidResourceReference> badRefs = new ArrayList<InvalidResourceReference>();

      Iterator netIterator = nets.iterator();
      while (netIterator.hasNext()) {
        NetGraphModel netModel = (NetGraphModel) netIterator.next();
        Iterator taskIterator = NetUtilities.getAllTasks(netModel).iterator();
        while (taskIterator.hasNext()) {
          YAWLTask task = (YAWLTask) taskIterator.next();
          ResourceMapping map = task.getResourceMapping();
          if (map != null) {
            map.cleanDistributionLists();  

            List<Participant> pList = map.getBaseUserDistributionList();
            if (pList != null) {
              for (Participant p : pList) {
                if (! pidList.contains(p.getID())) {
                  badRefs.add(new InvalidResourceReference(netModel, task, p));
                }
              }
            }

            List<Role> rList = map.getBaseRoleDistributionList();
            if (rList != null) {
              for (Role r : rList) {
                if (! ridList.contains(r.getID())) {
                  badRefs.add(new InvalidResourceReference(netModel, task, r));
                }
              }
            }    
          }
        }
      }
      if (! badRefs.isEmpty()) {
          List<String> msgList = new ArrayList<String>();
          for (InvalidResourceReference ref : badRefs) {
              msgList.add(ref.getMessage());
              ref.removeFromDistributionList();
          }
          ProblemMessagePanel.getInstance()
                .setProblemList("Invalid Resource References", msgList);
      }
    }
  }

  public void undoableSetFontSize(int oldSize, int newSize) {
    
    if (oldSize == newSize) {
      return;
    }
    
    SpecificationUndoManager.getInstance().startCompoundingEdits(getStartingNet());

    NetGraphModel netModel = null;
    
    SpecificationUtilities.setSpecificationFontSize(
        this, 
        newSize
    );
    
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
  
  
  public void setDefaultNetBackgroundColor(int color) {
    defaultNetBackgroundColor = color;
  }
  
  public int getDefaultNetBackgroundColor() {
    return this.defaultNetBackgroundColor;
  }

    public void setDefaultVertexBackgroundColor(Color color) {
      defaultVertexBackground = color;
      setPreferredVertexBackground(color);
    }

    public Color getDefaultVertexBackgroundColor() {
      return this.defaultVertexBackground;
    }


  public void setVersionNumber(YSpecVersion version) {
      if (versionNumber != null) {
          _versionChanged = (! versionNumber.equals(version));
          if (_versionChanged) {
              prevVersionNumber = versionNumber;
              rationaliseUniqueIdentifiers();
          }
      }
      versionNumber = version;
  }
  
  public YSpecVersion getVersionNumber() {
    return this.versionNumber;
  }

  public void setVersionChanged(boolean b) {
      _versionChanged = b;
      if (! _versionChanged) prevVersionNumber = null;
  }

  public YSpecVersion getPreviousVersionNumber() { return prevVersionNumber; }  

  public boolean isVersionChanged() { return _versionChanged; }

  
    public EngineIdentifier getUniqueIdentifier(String label) {
        return uniqueIdentifiers.getIdentifier(label);
    }

    public EngineIdentifier ensureUniqueIdentifier(EngineIdentifier engineID) {
        return uniqueIdentifiers.ensureUniqueness(engineID);
    }
    
    public void removeUniqueIdentifier(EngineIdentifier engineID) {
        uniqueIdentifiers.removeIdentifier(engineID);
    }

    public void rationaliseUniqueIdentifiers() {
        uniqueIdentifiers.rationalise(nets);
    }


    public List<String> analyse() {
        try {
            return new AnalysisResultsParser().getAnalysisResults(this);
        }
        catch (NoClassDefFoundError e) {
            JOptionPane.showMessageDialog(null,
                    "The attempt to analyse this specification failed.\n " +
                    "Please see the log for details", "Save File Error",
                    JOptionPane.ERROR_MESSAGE);
            LogWriter.error("The attempt to analyse the specification failed", e);
            return Collections.emptyList();
        }
    }


    public DataSchemaValidator getSchemaValidator() {
        return _schemaValidator;
    }

}
