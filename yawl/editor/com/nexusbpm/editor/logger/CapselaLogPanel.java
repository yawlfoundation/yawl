package com.nexusbpm.editor.logger;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Level;

import com.nexusbpm.editor.icon.ApplicationIcon;

/**
 * The log panel which displays log messages for the client.
 *
 * @author     Dean Mao
 * @created    August 11, 2003
 */
public class CapselaLogPanel extends JPanel {
  /** The log for the log panel. */ 
  protected static final Log LOG = LogFactory.getLog(CapselaLogPanel.class);

  private final static ImageIcon warningIcon = ApplicationIcon.getIcon("LogPanel.filter_warning_level");
  private final static ImageIcon infoIcon = ApplicationIcon.getIcon("LogPanel.filter_info_level");

  private JTabbedPane tabbedPane;

  private final static int DEBUG = 0;
  private final static int INFO = 1;
  private final static int WARN = 2;

  /** The color to use for a selected log. */
  protected final static Color COLOR_SELECTED = new Color(219, 234, 143);

  /**
   * Creates and initializes a log panel using a table view.
   *
   */
  public CapselaLogPanel() {
    initTableView();
  }

  /**
   * Initializes the tables for the logs, the tabbed pane to switch between the
   * tables, and the toolbar for clearing the tables or only viewing logs of
   * a particular level.
   */
  public void initTableView() {
    tabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);

    initLog(CapselaLog.getAllLog(), tabbedPane, "All");
    initLog(CapselaLog.getServerLog(), tabbedPane, "Server");
    initLog(CapselaLog.getEngineLog(), tabbedPane, "Engine");
    initLog(CapselaLog.getClientLog(), tabbedPane, "Client");
    JToolBar toolBar = new JToolBar();
    setupToolBar(toolBar);
    setLayout(new BorderLayout());
    add(tabbedPane, BorderLayout.CENTER);
    add(toolBar, BorderLayout.WEST);

  }

  /**
   * Sets up the toolbar with buttons for: clearing the log, showing only
   * warning and error level logs, and viewing all logs of all levels.
   * @param toolBar the toolbar where the buttons will be added.
   */
  public void setupToolBar(JToolBar toolBar) {
    toolBar.setOrientation(JToolBar.VERTICAL);
    JButton clearB = new JButton(ApplicationIcon.getIcon("LogPanel.clear_log_window"));
    clearB.setToolTipText("Clear Log");
    clearB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        CapselaLog.clearAll();
      }
    });
    toolBar.add(clearB);
    JButton warningB = new JButton(warningIcon);
    warningB.setToolTipText("Warning Level");
    warningB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        CapselaLog.setMode(Level.WARN);
      }
    });
    toolBar.add(warningB);
    JButton infoB = new JButton(infoIcon);
    infoB.setToolTipText("Info Level");
    infoB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        CapselaLog.setMode(Level.INFO);
      }
    });
    toolBar.add(infoB);
  }

  /**
   * Creates a new table for the given log and adds it to the given tabbed pane
   * with the given title displayed on the tab.
   * @param log the log to create a new table and tab for.
   * @param pane the tabbed pane where the new tab will be added.
   * @param title the title of the tab to add.
   */
  public void initLog(CapselaLog log, JTabbedPane pane, String title) {

    JTable table = new JTable(log);
    try { table.getColumn(CapselaLog.COLUMN_SOURCE).setPreferredWidth(50);    } catch (IllegalArgumentException e) { /* The column doesn't exist; ignore. */ }
    try { table.getColumn(CapselaLog.COLUMN_DATE).setPreferredWidth(120);     } catch (IllegalArgumentException e) { /* The column doesn't exist; ignore. */ }
    try { table.getColumn(CapselaLog.COLUMN_MESSAGE).setPreferredWidth(3000); } catch (IllegalArgumentException e) { /* The column doesn't exist; ignore. */ }

    table.setDefaultRenderer(CellModel.class, new MyTableCellRenderer());
    table.setSelectionModel(log.getSelectionModel());
    JScrollPane scrollPane = new JScrollPane(table);
    scrollPane.getViewport().setBackground(Color.WHITE);
    log.addTableModelListener(new LogTableModelListener(table, scrollPane.getVerticalScrollBar()));
    table.addMouseListener(new LogTableMouseListener(table));
    table.setShowGrid(false);
    table.setTableHeader(null);

    // Need this for the scrollbars to work correctly. If the table automatically
    // resizes itself, then for some reason the scrollpane can't determine the horizontal
    // size of the table, and never gives us a scrollbar.
    // See "http://java.sun.com/developer/qow/archive/50" for more info.
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

    pane.addTab(title, scrollPane);
  }

  /**
   * Initializes a list view for the logs and the tabbed pane to switch between
   * the lists.
   */
  public void initListView() {
    JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
    JList list1 = new JList(CapselaLog.getServerLog());
    JList list2 = new JList(CapselaLog.getEngineLog());
    JList list3 = new JList(CapselaLog.getClientLog());
    JList list4 = new JList(CapselaLog.getAllLog());

    list1.setSelectionModel(CapselaLog.getServerLog().getSelectionModel());
    list1.addListSelectionListener(new MyListSelectionListener(list1));
    list2.setSelectionModel(CapselaLog.getEngineLog().getSelectionModel());
    list2.addListSelectionListener(new MyListSelectionListener(list2));
    list3.setSelectionModel(CapselaLog.getClientLog().getSelectionModel());
    list3.addListSelectionListener(new MyListSelectionListener(list3));
    list4.setSelectionModel(CapselaLog.getAllLog().getSelectionModel());
    list4.addListSelectionListener(new MyListSelectionListener(list4));

    JScrollPane pane1 = new JScrollPane(list1, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    JScrollPane pane2 = new JScrollPane(list2, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    JScrollPane pane3 = new JScrollPane(list3, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    JScrollPane pane4 = new JScrollPane(list4, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    tabbedPane.addTab("server", pane1);
    tabbedPane.addTab("engine", pane2);
    tabbedPane.addTab("client", pane3);
    tabbedPane.addTab("all", pane4);

    JSlider slider = new JSlider(JSlider.VERTICAL, INFO, WARN, INFO);
    slider.setSnapToTicks(true);
    slider.addChangeListener(new ChangeListener() {

      public void stateChanged(ChangeEvent e) {
        int value = ((JSlider) e.getSource()).getValue();
        //_logger.fine("stateChanged");
        //_logger.fine("value: " + value);

        if (value == DEBUG) {
          CapselaLog.setMode(Level.DEBUG);
        } else if (value == WARN) {
          CapselaLog.setMode(Level.WARN);
        } else if (value == INFO) {
          CapselaLog.setMode(Level.INFO);
        }
      }
    });

    setLayout(new BorderLayout());
    add(tabbedPane, BorderLayout.CENTER);
    add(slider, BorderLayout.WEST);
  }

}

/**
 * Selection listener for the list view version of the log.
 *
 * @author     Dean Mao
 * @created    August 29, 2003
 */
class MyListSelectionListener implements ListSelectionListener {

  private JList _list;

  /**
   * Creates a selection listener for the given list.
   * @param list the list to listen to.
   */
  public MyListSelectionListener(JList list) {
    _list = list;
  }

  /**
   * Called when the value of the selection changes.
   * @param e the selection event.
   * @see ListSelectionListener#valueChanged(ListSelectionEvent)
   */
  public void valueChanged(ListSelectionEvent e) {
    //JList list = (JList) e.getSource();
    CapselaLogPanel.LOG.trace("======changed");
    final int ind = e.getLastIndex();
    if (!e.getValueIsAdjusting()) {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          CapselaLogPanel.LOG.trace("======ensuring");
          _list.ensureIndexIsVisible(ind);
        }
      });
    }

  }
}

/**
 * Cell renderer for the table view version of the log.
 *
 * @author     Dean Mao
 * @created    September 8, 2003
 */
class MyTableCellRenderer extends JTextArea implements TableCellRenderer {

  /** Color for a selected row. */
  public final static Color highlight = new Color(239, 238, 162);

  /**
   * Constructor for a table cell renderer.
   */
  public MyTableCellRenderer() {
    this.setOpaque(true);
    this.setFont(new Font("Arial", Font.PLAIN, 11));
  }

  /**
   * Returns the component used for drawing the cell. This method is used to
   * configure the renderer appropriately before drawing.
   * @param table the table that is asking the renderer to draw.
   * @param value the value of the cell to be rendered.
   * @param isSelected whether the cell is to be rendered as selected.
   * @param focused whether the cell is to be rendered as having focus.
   * @param row the row index of the cell to draw. <tt>-1</tt> for the header.
   * @param col the column index of the cell to draw.
   * @return the component used for drawing the cell.
   * @see TableCellRenderer#getTableCellRendererComponent(JTable,Object,boolean,boolean,int,int)
   */
  public Component getTableCellRendererComponent(JTable table, Object value, 
  	boolean isSelected, boolean focused, int row, int col) {
  		
    CellModel cellModel = (CellModel) value;
    if (cellModel != null) {
      Object v = cellModel.getObj();

      setForeground(cellModel.getColor());

      if (isSelected) {
        setBackground(highlight);
      } else {
        setBackground(Color.WHITE);
      }
      if (v instanceof Integer) {
        int src = ((Integer) v).intValue();
				setText( LogRecordI.SOURCES[src] );
      } else if (v instanceof Date) {
        setText(new SimpleDateFormat("MM/dd/yy hh:mm:ss aaa").format((Date) v));
      } else if (v instanceof String) {

        setText((String) v);
      } else if (v instanceof Level) {
      }
    }
    return this;
  }
}

/**
 *  Description of the Class
 *
 * @author     Dean Mao
 * @created    August 29, 2003
 */
class MyCellRenderer extends JLabel implements ListCellRenderer {

  /**
   *Constructor for the MyCellRenderer object
   */
  public MyCellRenderer() {
    setOpaque(true);
  }

  /**
   *  Description of the Method
   *
   * @param  record  Description of the Parameter
   * @return         Description of the Return Value
   */
  public String prepareText(LogRecordI record) {
    return record.toString();
  }

	/**
   *  Gets the listCellRendererComponent attribute of the MyCellRenderer object
   *
   * @param  list          Description of the Parameter
   * @param  value         Description of the Parameter
   * @param  index         Description of the Parameter
   * @param  isSelected    Description of the Parameter
   * @param  cellHasFocus  Description of the Parameter
   * @return               The listCellRendererComponent value
	 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
   */
  public synchronized Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
    setBackground(isSelected ? CapselaLogPanel.COLOR_SELECTED : Color.WHITE);
    
    LogRecordI record = (LogRecordI) value;
    int level = record.getLevel();
    try {
      if (level >= Level.ERROR_INT) {
        this.setForeground(Color.RED);
			} else if (level >= Level.WARN_INT) {
				this.setForeground(Color.YELLOW);
      } else if (level >= Level.DEBUG_INT) {
        this.setForeground(Color.GREEN);
      } else {
        this.setForeground(Color.BLACK);
      }
      /*
       *  StyledDocument doc = new DefaultStyledDocument();//this.getStyledDocument();
       *  this.setDocument(doc);
       */
      setText(prepareText((LogRecordI) value));
    } catch (Exception e) {
      System.out.println("not runtimeException i hope :######################");
      e.printStackTrace(System.out);
    }
    return this;
  }
}

/**
 *  Description of the Class
 *
 * @author     Dean Mao
 * @created    August 29, 2003
 */
class MyAllListCellRenderer extends MyCellRenderer {

  /**
   *  Description of the Method
   *
   * @param  record  Description of the Parameter
   * @return         Description of the Return Value
   */
  public String prepareText(LogRecordI record) {
    String tag = "";
   	tag = LogRecordI.SOURCES[record.getSourceType()] + "\t";

    return tag + super.prepareText(record);
  }
}
