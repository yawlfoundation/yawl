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

import org.yawlfoundation.yawl.editor.YAWLEditor;
import org.yawlfoundation.yawl.editor.core.api.YEditorSpecification;
import org.yawlfoundation.yawl.editor.ui.client.YConnector;
import org.yawlfoundation.yawl.editor.ui.data.DataSchemaValidator;
import org.yawlfoundation.yawl.editor.ui.data.DataVariable;
import org.yawlfoundation.yawl.editor.ui.data.Decomposition;
import org.yawlfoundation.yawl.editor.ui.data.WebServiceDecomposition;
import org.yawlfoundation.yawl.editor.ui.elements.model.*;
import org.yawlfoundation.yawl.editor.ui.foundations.LogWriter;
import org.yawlfoundation.yawl.editor.ui.foundations.XMLUtilities;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.net.NetGraphModel;
import org.yawlfoundation.yawl.editor.ui.net.utilities.NetUtilities;
import org.yawlfoundation.yawl.editor.ui.resourcing.InvalidResourceReference;
import org.yawlfoundation.yawl.editor.ui.resourcing.ResourceMapping;
import org.yawlfoundation.yawl.editor.ui.resourcing.ResourcingParticipant;
import org.yawlfoundation.yawl.editor.ui.resourcing.ResourcingRole;
import org.yawlfoundation.yawl.editor.ui.swing.specification.ProblemMessagePanel;
import org.yawlfoundation.yawl.editor.ui.swing.undo.*;
import org.yawlfoundation.yawl.editor.ui.engine.AnalysisResultsParser;
import org.yawlfoundation.yawl.elements.YSpecVersion;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.prefs.Preferences;

public class SpecificationModel {
  
  public static enum State {
    NO_NETS_EXIST,
    NETS_EXIST,
    NO_NET_SELECTED,
    SOME_NET_SELECTED,
    NET_DETAIL_CHANGED
  }
  
  private int netCount;
  private HashSet<NetGraphModel> nets;
  private State state;
  
  public static final int   DEFAULT_FONT_SIZE = 15;
  public static final int   DEFAULT_NET_BACKGROUND_COLOR = Color.WHITE.getRGB();

  public static final String DEFAULT_TYPE_DEFINITION = 
    "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n\n</xs:schema>";
  
  private String dataTypeDefinition = DEFAULT_TYPE_DEFINITION;

    private static YEditorSpecification _specification = new YEditorSpecification();

    public static YEditorSpecification getSpec() { return _specification; }

    public void loadFromFile(String fileName) throws IOException {
        _specification.load(fileName);
        reset();
    }

  /**
   * A mapping of possible selection states against subscribers that care to receive
   * notifications of a particular state.
   */
  private transient HashMap<State,LinkedList<SpecificationModelListener>> 
      stateSubscriberMap = new HashMap<State,LinkedList<SpecificationModelListener>>();

  private HashSet<WebServiceDecomposition> webServiceDecompositions;
  private ElementIdentifiers uniqueIdentifiers;
  private int     fontSize;
  private int     defaultNetBackgroundColor;
  private Color   defaultVertexBackground;
  private String  name;
  private String  description;
  private String  id;
  private String  uniqueID = "UID_" + UUID.randomUUID().toString();
  private String  author;
  private YSpecVersion versionNumber;
  private YSpecVersion prevVersionNumber;
  private String  validFromTimestamp;
  private String  validUntilTimestamp;
  private boolean _versionChanged;
  private DataSchemaValidator _schemaValidator;


  private transient static final SpecificationModel INSTANCE = new SpecificationModel();
  
  public SpecificationModel() {
      nets = new HashSet<NetGraphModel>();
      webServiceDecompositions = new HashSet<WebServiceDecomposition>();
      uniqueIdentifiers = new ElementIdentifiers();
      reset();
  }
  
  public static SpecificationModel getInstance() {
    return INSTANCE; 
  }
  
  /**
   * A convenience legacy method that subscribes the specified 
   * object to set of more coarse-grained specification model events. 
   * Namely {State.NO_NETS_EXIST},  {State.NETS_EXIST},
   * {State.NO_NET_SELECTED}, {SOME_NET_SELECTED}.
   * @param subscriber
   * @see SpecificationModelListener
   */
  
  public void subscribe(final SpecificationModelListener subscriber) {
    subscribe(
        subscriber, 
        new State[] {
            State.NO_NETS_EXIST,
            State.NETS_EXIST,
            State.NO_NET_SELECTED,
            State.SOME_NET_SELECTED,
        }
    );
  }

  /**
   * Allows a subscriber to begin receiviing callback notifications for changes of
   * state that the subscriber has defined as being important to it. The subscriber
   * must implement the interface {@link SpecificationModelListener}.
   * @param subscriber
   * @param statesOfInterest
   * @see SpecificationModelListener
   * @see State
   */
  
  public void subscribe(final SpecificationModelListener subscriber, State[] statesOfInterest) {
    for(State stateOfInterest: statesOfInterest) {
      LinkedList<SpecificationModelListener> stateSubscribers = stateSubscriberMap.get(stateOfInterest);
      
      if (stateSubscribers == null) {
        stateSubscribers = new LinkedList<SpecificationModelListener>();
        stateSubscriberMap.put(stateOfInterest, stateSubscribers);
      }
      stateSubscribers.add(subscriber);
      if (stateOfInterest == state) {
        subscriber.receiveSpecificationModelNotification(state);
      }
    }
  }
  
  private void publishState(final State state) {
    LinkedList<SpecificationModelListener> stateSubscribers =  stateSubscriberMap.get(state);
    if (stateSubscribers == null) {
      return;
    }
    for(SpecificationModelListener subscriber : stateSubscribers) {
      subscriber.receiveSpecificationModelNotification(state);
    }
  }
  
  public void setState(State state) {
    this.state = state;
    publishState(state);
  }
  
  public State getState() {
    return this.state;
  }
  
  public void reset() {
    netCount = 0;
    nets.clear();
    webServiceDecompositions.clear();
    uniqueIdentifiers.clear();
      _schemaValidator = new DataSchemaValidator();
    fontSize = DEFAULT_FONT_SIZE;
    defaultNetBackgroundColor = DEFAULT_NET_BACKGROUND_COLOR;
    defaultVertexBackground = getPreferredVertexBackground();
      setFileName("");
    setEngineFileName("");
    setDataTypeDefinition(DEFAULT_TYPE_DEFINITION);

    setName("");
    setDescription("No description has been given.");
    setId("");
    setAuthor(System.getProperty("user.name"));
    setVersionNumber(new YSpecVersion("0.0"));
    setValidFromTimestamp("");
    setValidUntilTimestamp("");
    YAWLEditor.setStatusBarText("Open or create a net to begin.");
    setState(State.NO_NETS_EXIST);
    prevVersionNumber = null;
      setVersionChanged(false);
  }

  private Color getPreferredVertexBackground() {
      Preferences prefs = Preferences.userNodeForPackage(YAWLEditor.class);
      int preferredColor = prefs.getInt("PREFERRED_VERTEX_BACKGROUND_COLOR",
                                         Color.WHITE.getRGB());
      return new Color(preferredColor);
  }

    private void setPreferredVertexBackground(Color color) {
        Preferences prefs = Preferences.userNodeForPackage(YAWLEditor.class);
        prefs.putInt("PREFERRED_VERTEX_BACKGROUND_COLOR", color.getRGB());
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
  
  public SortedSet<NetGraphModel> getSortedNets() {
    TreeSet<NetGraphModel> sortedNets = new TreeSet<NetGraphModel>();
    sortedNets.addAll(getNets());
    return sortedNets;
  }
  
  public NetGraphModel getStartingNet() {
    Object[] netSetArray = nets.toArray();
      for (Object aNetSetArray : netSetArray) {
          NetGraphModel thisNet = (NetGraphModel) aNetSetArray;
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
    publishState(State.NET_DETAIL_CHANGED);
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
    boolean removalSuccessful = nets.remove(netModel);
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
      setState(State.NETS_EXIST);    
    }
    publishState(State.NET_DETAIL_CHANGED);
  }

  private void publishNetCountDecrement() {
    final int oldNetCount = netCount;
    netCount--;
    if (oldNetCount == 1)  {
        setState(State.NO_NETS_EXIST);    
    }
    publishState(State.NET_DETAIL_CHANGED);
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
    publishState(State.SOME_NET_SELECTED);  
  }
  
  public void nothingSelected() {
    if (state != State.NO_NETS_EXIST) {
      publishState(State.NO_NET_SELECTED); 
    }
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
    return (name != null) &&
           (SpecificationUtilities.getNetModelFromName(this, name) == null) &&
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
  
  public HashSet<WebServiceDecomposition> getWebServiceDecompositions() {
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
    this.publishState(State.NET_DETAIL_CHANGED);
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

            List<ResourcingParticipant> pList = map.getBaseUserDistributionList();
            if (pList != null) {
              for (ResourcingParticipant p : pList) {
                if (! pidList.contains(p.getId())) {
                  badRefs.add(new InvalidResourceReference(netModel, task, p));
                }
              }
            }

            List<ResourcingRole> rList = map.getBaseRoleDistributionList();
            if (rList != null) {
              for (ResourcingRole r : rList) {
                if (! ridList.contains(r.getId())) {
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

  public String getUniqueID() { return uniqueID; }

  public void setUniqueID(String id) { uniqueID = id; }
    
  
  public void setAuthor(String author) {
    this.author = author;
  }
  
  public String getAuthor() {
    return this.author;
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
