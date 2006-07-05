/*
 * Created on 5/02/2005
 * YAWLEditor v1.01-1 
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
 */

package au.edu.qut.yawl.editor.swing.specification;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.border.CompoundBorder;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import au.edu.qut.yawl.editor.specification.SpecificationFileModel;
import au.edu.qut.yawl.editor.specification.SpecificationFileModelListener;
import au.edu.qut.yawl.editor.swing.JSingleSelectTable;
import au.edu.qut.yawl.editor.YAWLEditor;

public class ProblemMessagePanel extends JPanel  implements SpecificationFileModelListener{
  private JScrollPane problemScrollPane;
  private static MessageTable problemResultsTable = buildProblemMessageTable();
  
  private TitledBorder titledBorder = new TitledBorder("Problems identified");
  
  public static final ProblemMessagePanel 
    INSTANCE = new ProblemMessagePanel();
  
  public static ProblemMessagePanel getInstance() {
    return INSTANCE;
  }
  
  private ProblemMessagePanel() {
    super();

    buildContent();
    addResizeListener();
    SpecificationFileModel.getInstance().subscribe(this);
  }
  
  private void buildContent() {
    setLayout(new BorderLayout());
    
    problemScrollPane = new JScrollPane(problemResultsTable);
    
    add(problemScrollPane, BorderLayout.CENTER);
    setBorder(new CompoundBorder(
        titledBorder,
        new EmptyBorder(0,5,5,5)));
  }
  
  private void setProblemTitle(String title) {
    titledBorder.setTitle(title);
  }
  
  private void addResizeListener() {
    problemScrollPane.addComponentListener(
        new ComponentAdapter() {
          public void componentMoved(ComponentEvent event) {
            // don't care
          }
          
          public void componentResized(ComponentEvent event) {
            getProblemResultsTable().resizeProblemColumnIfNecessary(
                (int) problemScrollPane.getViewport().getVisibleRect().getWidth()
            );
          } 
        }
    );
  }
  
  public void setProblemList(String title, List problemList) {
    this.setProblemTitle(title);
    problemResultsTable.reset();

    populateProblemListTable(problemList);

    getProblemResultsTable().resizeProblemColumnIfNecessary(
        (int) problemScrollPane.getViewport().getVisibleRect().getWidth()
    );

    if (isVisible()) {
      repaint();
    } else {
      setVisible(true);
    }
  }
  
  private void populateProblemListTable(List problemList) {
    for(int i = 0; i < problemList.size(); i++) {
      String problem = (String) problemList.get(i);
      
      problemResultsTable.addMessage(problem);
    }
    readjustProblemTableSize();
  }

  public MessageTable getProblemResultsTable() {
    return problemResultsTable;
  }
  
  private void readjustProblemTableSize() {
    problemResultsTable.setPreferredScrollableViewportSize(
        problemResultsTable.getPreferredSize()
    );
    YAWLEditor.getInstance().showVerificationDetail();
  }
  
  private static MessageTable buildProblemMessageTable() {
    MessageTable table = new MessageTable();
    return table;     
  }
  
  public void specificationFileModelStateChanged(int state) {
    switch(state) {
      case SpecificationFileModel.IDLE: {
        problemResultsTable.reset();
        break;
      }
      default: {
        break;
      }
    }
  }
}

class MessageTable extends JSingleSelectTable {
  
  private int previousLargestWidth = 0;
  
  public MessageTable() {
    super();
    setModel(new MessageTableModel());
    this.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

    setMinimumSize(
        new Dimension(
            (int) getPreferredSize().getWidth(),
            getRowHeight()
        )    
      );
  }
  
  public void addMessage(String message) {
    int currentMessageWidth = getMessageWidth(message);
    
    getMessageModel().addMessage(message);

    if (currentMessageWidth > previousLargestWidth) {
      previousLargestWidth = currentMessageWidth;
      resizeProblemColumnIfNecessary(getWidth());
    }
  }
  
  public void resizeProblemColumnIfNecessary(int outerComponentWidth) {
    getColumnModel().getColumn(0).setMinWidth(
        Math.max(previousLargestWidth, outerComponentWidth));
    getColumnModel().getColumn(0).setMaxWidth(
        Math.max(previousLargestWidth, outerComponentWidth));
    getColumnModel().getColumn(0).setPreferredWidth(
        Math.max(previousLargestWidth, outerComponentWidth));
    setMinimumSize(
      new Dimension(
          (int) getPreferredSize().getWidth(),
          getRowHeight()
      )    
    );
  }
  
  private MessageTableModel getMessageModel() {
    return (MessageTableModel) getModel();
  }
  
  public void reset() {
    setModel(new MessageTableModel());
    previousLargestWidth = 0;
  }
  
  private int getMessageWidth(String message) {
    return getFontMetrics(getFont()).stringWidth(message) + 5;
  }
  
  public int getMessageHeight() {
    return getFontMetrics(getFont()).getHeight();
  }
  
  public Component prepareRenderer(TableCellRenderer renderer,
      int row, 
      int col) {

    JComponent component = (JComponent) super.prepareRenderer(renderer, row, col);
    JLabel componentAsLabel = (JLabel) component;
    componentAsLabel.setHorizontalAlignment(JLabel.LEFT);
    return component;
  }
}


class MessageTableModel extends AbstractTableModel {
  private LinkedList messages = new LinkedList();
  
  private static final String[] COLUMN_LABELS = { 
    "Problem"
  };
  
  public static final int PROBLEM_COLUMN          = 0;

  public int getColumnCount() {
    return COLUMN_LABELS.length;
  }

  public String getColumnName(int columnIndex) {
    return null;
  }
  
  public int getRowCount() {
    if (messages != null) {
      return messages.size();
    }
    return 0;
  }

  public Object getValueAt(int row, int col) {
    switch (col) {
      case PROBLEM_COLUMN:  {
        return messages.get(row);
      }
    }
    return null;
  }

  public void addMessage(String message) {
    messages.add(message);
    this.fireTableRowsInserted(getRowCount() - 1, getRowCount() - 1);
  }
}
