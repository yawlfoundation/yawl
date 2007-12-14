/*
 * Created on 09/10/2003
 * YAWLEditor v1.0 
 *
 * @author Lindsay Bradford
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

package org.yawlfoundation.yawl.editor.swing.menu;


import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.util.LinkedList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.TransferHandler;

import javax.swing.border.EmptyBorder;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import org.jgraph.event.GraphSelectionEvent;

import java.io.File;

import org.yawlfoundation.yawl.editor.YAWLEditor;

import org.yawlfoundation.yawl.editor.elements.model.VertexContainer;
import org.yawlfoundation.yawl.editor.elements.model.YAWLAtomicTask;
import org.yawlfoundation.yawl.editor.elements.model.YAWLVertex;

import org.yawlfoundation.yawl.editor.foundations.FileUtilities;
import org.yawlfoundation.yawl.editor.foundations.ResourceLoader;

import org.yawlfoundation.yawl.editor.specification.SpecificationModelListener;
import org.yawlfoundation.yawl.editor.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.specification.SpecificationSelectionListener;
import org.yawlfoundation.yawl.editor.specification.SpecificationSelectionSubscriber;

import org.yawlfoundation.yawl.editor.swing.JUtilities;
import org.yawlfoundation.yawl.editor.swing.YAWLEditorDesktop;
import org.yawlfoundation.yawl.editor.swing.menu.ControlFlowPalette.SelectionState;

public class Palette extends JPanel implements SpecificationModelListener {

  private static final long serialVersionUID = 1L;

  private static final ControlFlowPalette CONTROL_FLOW_PALETTE = new ControlFlowPalette();
  private static final TaskTemplatePalette TASK_TEMPLATE_PALETTE = new TaskTemplatePalette();
  private static final SingleTaskPalette SINGLE_TASK_PALETTE = new SingleTaskPalette();
  
  private static final Palette INSTANCE = new Palette();

  public static Palette getInstance() {
    return INSTANCE;
  }  
    
  private Palette() {
    super();
    buildInterface();
  }  

  protected void buildInterface() {
    
    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();

    setLayout(gbl);

    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.insets = new Insets(3,5,0,5);
    
    add(CONTROL_FLOW_PALETTE, gbc);

    gbc.gridy++;

    gbc.weightx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    add(TASK_TEMPLATE_PALETTE, gbc);

    gbc.fill = GridBagConstraints.NONE;
    gbc.anchor = GridBagConstraints.CENTER;
    gbc.weightx = 0;
    gbc.gridy++;

    add(SINGLE_TASK_PALETTE, gbc);
    
    gbc.gridy++;
    gbc.weighty = 1;
    gbc.weightx = 1;
    gbc.fill = GridBagConstraints.BOTH;

    add(Box.createVerticalGlue(),gbc);

    // split panes will go no further than the minimum size of a compoment.
    // As we want the splitpane to potentially cover the entire edit space,
    // we ensure that this component has no minimum height.
    
    setMinimumSize(
        new Dimension(
            (int) getPreferredSize().getWidth(),
            0
        )
    );
    
    SpecificationModel.getInstance().subscribe(this);
    
    CONTROL_FLOW_PALETTE.subscribeForSelectionStateChanges(
        TASK_TEMPLATE_PALETTE    
    );
  }
  
  
  public void refresh() {
    repaint();        
  } 
  
  public void refreshSelected() {
    /*
     * The NetMarquee Handler overrides certain
     * GUI behaviour at times. When it is done
     * it wants to reset to the GUI behaviour 
     * driven by the control palette. The easiest
     * way to do that is just re-selecting the current
     * selected palette item.
     */
    
    CONTROL_FLOW_PALETTE.setSelectedState(
      CONTROL_FLOW_PALETTE.getSelectedState()    
    );
  }
  
  public ControlFlowPalette.SelectionState getControlFlowPaletteState() {
    return CONTROL_FLOW_PALETTE.getSelectedState();
  }
  
  public ControlFlowPalette getControlFlowPalette() {
    return CONTROL_FLOW_PALETTE;
  }
  
  public String getSelectedAtomicTaskIconPath() {
    return TASK_TEMPLATE_PALETTE.getAtomicTaskIconPath();
  }
  
  public void doPostBuildProcessing() {
    SpecificationModel.getInstance().subscribe(this);   
  }
  
  public void setEnabled(boolean enabled) {
    CONTROL_FLOW_PALETTE.setEnabled(enabled);
    TASK_TEMPLATE_PALETTE.setEnabled(enabled);
    TASK_TEMPLATE_PALETTE.setVisible(enabled);
    SINGLE_TASK_PALETTE.setVisible(enabled);
    SINGLE_TASK_PALETTE.setEnabled(enabled);
    super.setEnabled(enabled);
  }
  
  public void receiveSpecificationModelNotification(SpecificationModel.State state) {
    switch(state) {
      case NO_NETS_EXIST: {
        CONTROL_FLOW_PALETTE.setSelectedState(
            ControlFlowPalette.SelectionState.MARQUEE
        );
        setEnabled(false);
        YAWLEditor.setStatusBarText(
            "Open or create a specification to begin."
        );     
        break;    
      }
      case NETS_EXIST: {
        YAWLEditor.setStatusBarText(
            "Select a net to continue editing it."
        );     
        break;    
      }
      case NO_NET_SELECTED: {
        YAWLEditor.setStatusBarText(
            "Select a net to continue editing it."
        );     
        setEnabled(false);
        break;
      }
      case SOME_NET_SELECTED: {
        YAWLEditor.setStatusBarText(
            "Use the palette toolbar to edit the selected net."
        );     
        setEnabled(true);
        break;
      }
      default: {
        assert false : "Invalid state passed to receiveSpecificationModelNotification()";   
      }    
    }
  }
}

class TaskTemplatePalette extends JPanel implements ControlFlowPaletteListener, SpecificationSelectionSubscriber  {

  private static final long serialVersionUID = 1L;
  
  private TaskIconTree taskTemplateTree;
  private JScrollPane taskTemplateScroller;

  private boolean atomicTaskSelectedOnControlFlowPalette = false;
  private boolean nothingSelected = true;
  private boolean atomicTaskSelected = false;
  
  private static final int ROW_HEIGHT = 10;
  
  public TaskTemplatePalette() {
    buildInterface();
    bindDragAndDropComponents();
    
    SpecificationSelectionListener.getInstance().subscribe(
        this,
        new int[] { 
          SpecificationSelectionListener.STATE_SINGLE_TASK_SELECTED,          
          SpecificationSelectionListener.STATE_NO_ELEMENTS_SELECTED,
          SpecificationSelectionListener.STATE_ONE_OR_MORE_ELEMENTS_SELECTED
        }
    );
   setEnabled(false);
  }

  protected void buildInterface() {
    
    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();

    setLayout(gbl);
    
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weightx = 1;
    gbc.insets = new Insets(4,0,3,0);
    gbc.fill = GridBagConstraints.HORIZONTAL;
    add(buildTaskTree(),gbc);
  }

  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    taskTemplateScroller.setEnabled(enabled);
  }
  
  public String getAtomicTaskIconPath() {
    return taskTemplateTree.getSelectedAtomicTaskIconPath();
  }
  
  private JScrollPane buildTaskTree() {
    taskTemplateTree = new TaskIconTree();
    
    taskTemplateScroller = new JScrollPane(taskTemplateTree);
    
    taskTemplateScroller.setPreferredSize(
        new Dimension(
            (int) taskTemplateScroller.getPreferredSize().getWidth(),
            taskTemplateTree.getFontMetrics(
              taskTemplateTree.getFont()  
            ).getHeight() * ROW_HEIGHT
        )
    );
    
    return taskTemplateScroller;
  }

  private void bindDragAndDropComponents() {
    setTransferHandler(new TransferHandler("text"));
  }

  public void controlFlowPaletteStateChanged(SelectionState selectionState) {
    switch(selectionState) {
      case ATOMIC_TASK: case MULTIPLE_ATOMIC_TASK: {
        atomicTaskSelectedOnControlFlowPalette = true;
        break;
      }
      default: {
        atomicTaskSelectedOnControlFlowPalette = false;
        break;
      }
    }
    setEnabledIfAppropriate();
  }
  
 
  private void setEnabledIfAppropriate() {
    if (nothingSelected) {
      if (atomicTaskSelectedOnControlFlowPalette) {
        setEnabled(true);
      } else { // something else selected on palette
        setEnabled(false);
      }
    } else if (atomicTaskSelected) {
      setEnabled(true);
    } else {
      setEnabled(false);
    }
  }
  
  public void receiveGraphSelectionNotification(int state, GraphSelectionEvent event) {
    switch(state) {
      case SpecificationSelectionListener.STATE_SINGLE_TASK_SELECTED: {
        nothingSelected = false;
        Object cell = event.getCell();
        if (cell instanceof VertexContainer) {
          cell = ((VertexContainer) cell).getVertex(); 
        }
        if (cell instanceof YAWLAtomicTask) {
          atomicTaskSelected = true;
        } else {
          atomicTaskSelected = false;
        }
        break;
      }
      case SpecificationSelectionListener.STATE_NO_ELEMENTS_SELECTED: {
        nothingSelected = true;
        atomicTaskSelected = false;
        break;
      }
      default: {
        nothingSelected = false;
        atomicTaskSelected = false;
        break;
      }
    }
    setEnabledIfAppropriate();
  }
}


class TaskIconTree extends JTree implements SpecificationSelectionSubscriber {
  
  private static final long serialVersionUID = 1L;
  
  public TaskIconTreeModel getTaskIconTreeModel() {
    return (TaskIconTreeModel) getModel();
  }
  
  public TaskIconTree() {
    super();
    setModel(new TaskIconTreeModel());
    buildInterface();
    
    this.selectionModel = this.getSelectionModel();
    
    SpecificationSelectionListener.getInstance().subscribe(
        this,
        new int[] { 
          SpecificationSelectionListener.STATE_SINGLE_TASK_SELECTED
        }
    );
    
    addTreeSelectionListener(
        new TreeSelectionListener() {
          public void valueChanged(TreeSelectionEvent e) {
            if (!(getLastSelectedPathComponent() instanceof TaskIconTreeNode)) {
              return;  // don't care if it's not a TaskIconTreeNode
            }
            
            if (YAWLEditorDesktop.getInstance().getSelectedGraph() == null || 
                YAWLEditorDesktop.getInstance().getSelectedGraph().getSelectionCell() == null) {
              return;  // don't care if we don't have a selected cell to change an icon on.
            }
            
            TaskIconTreeNode iconNode = (TaskIconTreeNode) getLastSelectedPathComponent();
            
            Object cell = YAWLEditorDesktop.getInstance().getSelectedGraph().getSelectionCell();
            if (cell instanceof VertexContainer) {
              cell = ((VertexContainer) cell).getVertex();
            }
            if (cell instanceof YAWLAtomicTask) {
              YAWLVertex vertex= (YAWLVertex) cell;
              YAWLEditorDesktop.getInstance().getSelectedGraph().setVertexIcon(
                  vertex, iconNode.getRelativeIconPath()
              );
            }
          }
        }
    );
    
    selectDefaultNode();
  }
  
  public TaskIconTreeNode getDefaultNode() {
    return getTaskIconTreeModel().getDefaultNode();
  }
  
  public void selectDefaultNode() {
    if (getDefaultNode() == null) {
      return;
    }
    setSelectionPath(
      new TreePath(getDefaultNode().getPath())
    );
  }
  
  public boolean isTaskPaletteNodeSelected() {
    if (getLastSelectedPathComponent() != null && 
        getLastSelectedPathComponent() instanceof TaskIconTreeNode) {
      return true;
    }
    return false;
  }
  
  private TaskIconTreeNode getSelectedTaskPaletteNode() {
    assert isTaskPaletteNodeSelected() : "Node selected is not of type TaskPaletteTreeNode";
    return (TaskIconTreeNode) getLastSelectedPathComponent();
  }
  
  public String getSelectedAtomicTaskIconPath() {
    if (isTaskPaletteNodeSelected()){
      return getSelectedTaskPaletteNode().getRelativeIconPath();
    }
    return null;
  }
  
  private void buildInterface() {
    setBorder(new EmptyBorder(4,3,3,4));

    getSelectionModel().setSelectionMode(
        TreeSelectionModel.SINGLE_TREE_SELECTION    
    );

    setCellRenderer(
        new TaskIconTreeNodeRenderer()
    );
    
    /*  Drag end of D&D behaviour. Drop is causing me grief . Edited out for the time being.
    addMouseListener(
        new MouseAdapter() {
          public void mousePressed(MouseEvent event) {
              TaskIconTree eventSource = (TaskIconTree)event.getSource();
      
              if (eventSource.isTaskPaletteNodeSelected()) {
                eventSource.getTransferHandler().exportAsDrag(
                    eventSource, 
                    event, 
                    TransferHandler.COPY
                );

                System.out.println(
                    "Dragging item: " + 
                    eventSource.getSelectionPath().getLastPathComponent().toString()
                );
              }
          }
          
        }
    );*/
  }

  public void receiveGraphSelectionNotification(int state, GraphSelectionEvent event) {
    Object cell = event.getCell();
    if (cell instanceof VertexContainer) {
      cell = ((VertexContainer) cell).getVertex(); 
    }
    if (!(cell instanceof YAWLAtomicTask)) {
      return;
    }

    YAWLAtomicTask task = (YAWLAtomicTask) cell;

    switch(state) {
    
      case SpecificationSelectionListener.STATE_SINGLE_TASK_SELECTED: {
        selectNodeWithIconPath(
            task.getIconPath()
        );
        break;
      }
    }
  }
  
  public void selectNodeWithIconPath(String iconPath) {
    getSelectionModel().setSelectionPath(
      new TreePath(
          getTaskIconTreeModel().getNodeWithIconPath(
              iconPath
          ).getPath()
      )    
    );
  }

  class TaskIconTreeNodeRenderer extends DefaultTreeCellRenderer {
    
    private static final long serialVersionUID = 1L;
    
    public Component getTreeCellRendererComponent(
                        JTree tree,
                        Object value,
                        boolean sel,
                        boolean expanded,
                        boolean leaf,
                        int row,
                        boolean hasFocus) {

        super.getTreeCellRendererComponent(
                        tree, value, sel,
                        expanded, leaf, row,
                        hasFocus);
        
        if (value == null) {
          return null;
        }
        
        if (value instanceof TaskIconTreeNode) {
            setIcon(((TaskIconTreeNode) value).getIcon());
        }

        return this;
    }
  }
}


class TaskIconTreeModel extends DefaultTreeModel {
  
  private static final long serialVersionUID = 1L;
  
  /*
   * Recursing through the tree is failing oddly and non-deterministically.  I've decided
   * to implement a flat index of nodes and use that for finding the nodes I'm interested in.
   */
  
  private LinkedList<TaskIconTreeNode> iconNodes = new LinkedList<TaskIconTreeNode>();
    
  protected static String getInternalIconPathByName(String iconName) {
    if (iconName == null) {
      return null;
    }
    return "/org/yawlfoundation/yawl/editor/resources/taskicons/" + iconName + ".png";
  }
  
  public TaskIconTreeNode getDefaultNode() {
    for(TaskIconTreeNode node: iconNodes) {
      if (node.isDefault()) {
        return node;
      }
    }
    return null;
  }
  

  /**
   * Attempts to find the tree node with the given icon path. If it fails, it will
   * sipply the default node instead.
   * @param iconPath
   * @return 
   */
  
  public TaskIconTreeNode getNodeWithIconPath(String iconPath) {
    for(TaskIconTreeNode node : iconNodes) {
      if (node.getRelativeIconPath() != null && 
          node.getRelativeIconPath().equals(iconPath)) {
        return node;
      }
    }
    return getDefaultNode();
  }

  private final DefaultMutableTreeNode buildIconTree() {
    DefaultMutableTreeNode rootIconNode = new DefaultMutableTreeNode("Task Icon");

    add(rootIconNode,createNoIconNode());
    add(rootIconNode,createManualIconNodes());
    add(rootIconNode,createAutomaticIconNode());
    add(rootIconNode,createRoutingIconNodes());
    add(rootIconNode,createPluginIconNodes());
    
    return rootIconNode;
  }

  /**
   * Adds newNode to parentNode, then indexes newNode for searching later.
   * @param parentNode
   * @param newNode
   */
  
  public void add(DefaultMutableTreeNode parentNode, TaskIconTreeNode newNode) {
    parentNode.add(newNode);
    iconNodes.add(newNode);  
  }

  private TaskIconTreeNode createInternalIconNode(String title, String fileName) {
    return createIconNode(title, getInternalIconPathByName(fileName), true);
  }

  private TaskIconTreeNode createExternalIconNode(String title, String fileName) {
    return createIconNode(title, fileName, false);
  }
  
  private TaskIconTreeNode createIconNode(String title, String fileName, boolean internal) {
    TaskIconTreeNode node = 
      new TaskIconTreeNode(
          title, 
          fileName,
          internal
      );
    
    return node;
  }

  
  private TaskIconTreeNode createNoIconNode() {
    TaskIconTreeNode noIconNode = createInternalIconNode("No Icon", null);
    noIconNode.setDefault(true);
    return noIconNode;
  }
  
  private TaskIconTreeNode createManualIconNodes() {
    TaskIconTreeNode manualNode = createInternalIconNode("Manual", "Manual");
    
    add(manualNode, createInternalIconNode("Pair", "Pair"));
    add(manualNode, createInternalIconNode("Group", "Group"));
    add(manualNode, createInternalIconNode("Inspect", "Inspect"));
    add(manualNode, createInternalIconNode("Validate", "Validate"));
    add(manualNode, createInternalIconNode("Schedule", "Schedule"));
    add(manualNode, createInternalIconNode("File", "File"));
    add(manualNode, createInternalIconNode("PDA", "BlackPDA"));

    return manualNode;
  }

  private TaskIconTreeNode createAutomaticIconNode() {
    TaskIconTreeNode automaticNode = createInternalIconNode("Automated", "Automatic");

    add(automaticNode, createInternalIconNode("Automatic", "AutomaticOne"));
    add(automaticNode, createInternalIconNode("Automatic", "AutomaticTwo"));
    add(automaticNode, createInternalIconNode("Timer", "Timer"));
    add(automaticNode, createInternalIconNode("Print", "Print"));
    
    return automaticNode;
  }
  
  private TaskIconTreeNode createRoutingIconNodes() {
    TaskIconTreeNode routingNode =  createInternalIconNode("Routing", "RoutingTask");

    add(routingNode, createInternalIconNode("Question", "QuestionOne"));
    add(routingNode, createInternalIconNode("Question", "QuestionTwo"));
    add(routingNode, createInternalIconNode("Dangerous", "Dangerous"));
    add(routingNode, createInternalIconNode("Exception", "Exception"));

    return routingNode;
  }
  
  private TaskIconTreeNode createPluginIconNodes() {
    TaskIconTreeNode pluginNode = createInternalIconNode("Plugin", "Plugin");

    recurseNodeForPluginIcons(
        pluginNode, 
        new File(FileUtilities.ABSOLUTE_TASK_ICON_PATH)
    );
    
    return pluginNode;
  }

  private void recurseNodeForPluginIcons(TaskIconTreeNode rootNode, File rootDirectory) {
    
    if (!rootDirectory.exists() || !rootDirectory.isDirectory() || !rootDirectory.canRead()) {
      return;
    }
     
     File[] filesInDirectory = rootDirectory.listFiles();

     for(File file: filesInDirectory) {
       if (file.isDirectory()) {
         
         TaskIconTreeNode dirNode = createExternalIconNode(file.getName(), null);
         
         add(rootNode, dirNode);
         
         recurseNodeForPluginIcons(dirNode, file);
       } else if(file.getName().toLowerCase().endsWith("png") ) {
         add(rootNode, 
             createExternalIconNode(
                 FileUtilities.stripFileExtension(
                     file.getName()
                 ),
                 FileUtilities.getRelativeTaskIconPath(
                     file.getPath()
                 )
             )
         );
       }
     }
  }
  

  public TaskIconTreeModel() {
    super(null);
    setRoot(
        buildIconTree()
    );
  }
}

class TaskIconTreeNode extends DefaultMutableTreeNode {

  private static final long serialVersionUID = 1L;
  
  private Icon nodeIcon;
  private String relativeIconPath;
  
  private boolean isInternal = true;
  
  private boolean isDefault = false;
  
  public TaskIconTreeNode(Object userObject) {
    super(userObject);
    setInternal(false);
  }
  
  public TaskIconTreeNode(Object userObject, String relativeIconPath, boolean isInternal) {
    super(userObject);
    setInternal(isInternal);
    setIconPath(relativeIconPath);
  }
  
  public void setIconPath(String relativeIconPath) {
    this.relativeIconPath = relativeIconPath;
    if (relativeIconPath == null) {
      return;
    }

    if (isInternal()) {
      setIcon(
          ResourceLoader.getImageAsIcon(
              relativeIconPath
          )    
      );
    } else {
      setIcon(
          ResourceLoader.getExternalImageAsIcon(
              FileUtilities.getAbsoluteTaskIconPath(
                  relativeIconPath
              )
          )    
      );
    }
  }

  public String getRelativeIconPath() {
    return this.relativeIconPath;
  }
  
  public void setInternal(boolean isInternal) {
    this.isInternal = isInternal;
  }
  
  public boolean isInternal() {
     return isInternal;
  }

  private void setIcon(Icon nodeIcon) {
    this.nodeIcon = nodeIcon;
  }
  
  public Icon getIcon() {
    return nodeIcon;
  }
  
  public boolean isDefault() {
    return isDefault;
  }
  
  public void setDefault(boolean theDefault) {
    this.isDefault = theDefault;
  }
}
