/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package com.nexusbpm.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.UIManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Level;
import org.apache.log4j.PropertyConfigurator;
import org.flexdock.docking.DockingManager;
import org.flexdock.docking.drag.effects.EffectsManager;
import org.flexdock.docking.drag.preview.GhostPreview;
import org.flexdock.perspective.PerspectiveManager;
import org.flexdock.perspective.persist.FilePersistenceHandler;
import org.flexdock.perspective.persist.PersistenceHandler;
import org.flexdock.view.View;
import org.flexdock.view.Viewport;
import org.quartz.SchedulerException;

import au.edu.qut.yawl.elements.YNet;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.persistence.dao.DAO;
import au.edu.qut.yawl.persistence.dao.DAOFactory;
import au.edu.qut.yawl.persistence.dao.DatasourceRoot;
import au.edu.qut.yawl.persistence.dao.DAOFactory.PersistenceType;
import au.edu.qut.yawl.persistence.managed.DataContext;
import au.edu.qut.yawl.persistence.managed.DataProxy;
import au.edu.qut.yawl.util.configuration.BootstrapConfiguration;

import com.l2fprod.common.propertysheet.Property;
import com.nexusbpm.command.Command;
import com.nexusbpm.command.CommandExecutor;
import com.nexusbpm.command.CreateNetCommand;
import com.nexusbpm.command.CreateNexusComponentCommand;
import com.nexusbpm.command.CreateSpecificationCommand;
import com.nexusbpm.editor.configuration.ConfigurationDialog;
import com.nexusbpm.editor.configuration.NexusClientConfiguration;
import com.nexusbpm.editor.desktop.CapselaInternalFrame;
import com.nexusbpm.editor.desktop.DesktopPane;
import com.nexusbpm.editor.editors.ComponentEditor;
import com.nexusbpm.editor.editors.DataTransferEditor;
import com.nexusbpm.editor.editors.schedule.SchedulerCalendar;
import com.nexusbpm.editor.icon.ApplicationIcon;
import com.nexusbpm.editor.logger.CapselaLog;
import com.nexusbpm.editor.logger.CapselaLogPanel;
import com.nexusbpm.editor.logger.LogRecordI;
import com.nexusbpm.editor.logger.LogRecordVO;
import com.nexusbpm.editor.persistence.EditorDataProxy;
import com.nexusbpm.editor.tree.STree;
import com.nexusbpm.editor.tree.SharedNode;
import com.nexusbpm.editor.tree.SharedNodeTreeModel;
import com.nexusbpm.editor.util.JmsClient;
import com.nexusbpm.services.NexusServiceInfo;

/**
 * 
 * @author  Matthew Sandoz
 * @author Nathan Rose
 */
public class WorkflowEditor extends DockableApplicationFrame implements MessageListener {

	private static final long serialVersionUID = 1L;
	private final static int DEFAULT_CLIENT_HEIGHT = 692;
	private final static int DEFAULT_CLIENT_WIDTH = 800;
    private final static int DEFAULT_COMMAND_STACK_SIZE = 20;

	private static CommandExecutor executor;
	private static final Log LOG = LogFactory.getLog( WorkflowEditor.class );
    private static WorkflowEditor singleton = null;
	private static NexusSplashScreen splash;
    /** The panel that contains the component tree split panes. */
    private JPanel componentTreesPanel; 

    private JPanel desktopAndStatusPanel;
    private JDesktopPane desktopPane;
    protected static JPanel fileDaoPanel;
    protected static JPanel memoryDaoPanel;
    protected static JPanel remoteDaoPanel;
    protected CapselaLogPanel logPanel;
    protected TreePanel componentsTreePanel;
    protected JPanel desktopPanel;
    /**
     * Creates new form WorkflowEditor 
     */
    private WorkflowEditor() {
    	super("Nexus Editor");
        // we need to set the singleton instance before the constructor returns so the constructor
        // can setup the singleton command executor
        WorkflowEditor.singleton = this;

		initComponents();
        
        this.pack();
    	this.setSize(DEFAULT_CLIENT_WIDTH,DEFAULT_CLIENT_HEIGHT);
        this.setLocationRelativeTo( null );
    }
    public void addInternalFrameMenuItem( CapselaInternalFrame frame ) {
        assert frame != null : "attempting to add menu item for null frame!";
        WorkflowMenuBar bar = (WorkflowMenuBar) this.getJMenuBar();
        JMenuItem newMenuItem = bar.addWindowItem(frame);
        newMenuItem.setAction( new WindowFocusAction( frame ) );
        setSelectedInternalFrameMenuItem( frame );
    }
    
    public void setSelectedInternalFrameMenuItem(JInternalFrame frame) {
    	setTitle("Nexus Editor - " + frame.getTitle());
    }
    
    public JDesktopPane getDesktopPane() {
		return desktopPane;
	}
    public static JPanel getFileDaoPanel() {
		if (fileDaoPanel == null) {
			fileDaoPanel = DaoPanelFactory.getFileDaoPanel();
		}
		return fileDaoPanel;
	}

    public static JPanel getMemoryDaoPanel() {
		if (memoryDaoPanel == null) {
			memoryDaoPanel = DaoPanelFactory.getMemoryDaoPanel();
		}
		return memoryDaoPanel;
	}

    public static JPanel getRemoteDaoPanel() {
    	if (remoteDaoPanel == null) {
    		remoteDaoPanel = DaoPanelFactory.getRemoteDaoPanel();
    	}
        return remoteDaoPanel;
    }

    public void onMessage(Message o) {
		ObjectMessage om = (ObjectMessage) o;
		try {
			Enumeration e = om.getPropertyNames();
			StringBuilder sb = new StringBuilder("Message: {");
			while (e.hasMoreElements()) {
				String name = e.nextElement().toString();
				String value = om.getStringProperty(name);
				sb.append(name + ":" + value + " ");
			}
			sb.append("}");
			LogRecordI record = new LogRecordVO(
					Level.INFO_INT,  
					LogRecordVO.SOURCE_ENGINE, 
					new Date().getTime(), 
					1, 
					sb.toString()
			); 
			CapselaLog.log(record);
		} catch (JMSException e) {
			e.printStackTrace();
		}
    }
    /**
     * Opens an editor centered at the given location or maximized if the location
     * is null.
     */
    public void openComponentEditor( EditorDataProxy proxy, Point location ) throws Exception {
        CapselaInternalFrame editor = proxy.getEditor();
        openEditor( editor, location );
    }
    public void openDataEditor( EditorDataProxy proxy, Point location ) throws Exception {
        ComponentEditor editor = new DataTransferEditor();
        
        editor.resetTitle( proxy );
        editor.addInternalFrameListener( proxy.getInternalFrameListener( editor ) );
        
        openEditor( editor, location );
    }
    public void openEditor( CapselaInternalFrame editor, Point location ) throws Exception {
        if (editor != null && !editor.isVisible()) {
            JDesktopPane desktop = getDesktopPane();
            
            editor.pack();
            int locx = 0;
            int locy = 0;
            if( location != null ) {
                locx = (int)location.getX();
                locy = (int)location.getY();
            }
            int width = desktop.getWidth();
            int height = desktop.getHeight();
            
            if( width > 450 ) width = 450;
            if( height > 400 ) height = 400;
            
            int x = (int)( locx - width / 2 );
            int y = (int)( locy - height / 2 );
            
            if( x < 0 ) x = 0;
            else if( ( x + width ) > desktop.getWidth() ) x = desktop.getWidth() - width;
            if( y < 0 ) y = 0;
            else if( ( y + height ) > desktop.getHeight() ) y = desktop.getHeight() - height;
            
            editor.setSize(width, height);
            editor.setLocation(x, y);
            
            editor.setVisible(true);
            
            getDesktopPane().add(editor);
            
            editor.setSelected( true );
            editor.toFront();
            editor.setMaximum( location == null );
        }
        else if( editor != null ) {
            editor.toFront();
            editor.setSelected( true );
        }
    }
    
    /**
	 * This method is called from within the constructor to initialize the form.
	 */
    private void initComponents() {
       
        WorkflowMenuBar bar = new WorkflowMenuBar();
        setJMenuBar(bar);
        bar.setAction(bar.getExitMenuItem(), new ExitAction());
        bar.setAction(bar.getWorkflowScheduleMenuItem(), new OpenSchedulingCalendarAction());
        bar.setAction(bar.getPreferencesMenuItem(), new EditPreferencesAction());
        bar.setAction(bar.getRedoMenuItem(), new RedoAction());
        bar.setAction(bar.getUndoMenuItem(), new UndoAction());
        //////////////////////////////////
        // setup the component tree panels
        componentTreesPanel = new JPanel();
        componentTreesPanel.setLayout(new BorderLayout());
        
        // create the split panes
        JSplitPane componentTreesTopSplitPane = new JSplitPane();
        componentTreesTopSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        
        if( ! ( getRemoteDaoPanel() instanceof TreePanel ) ) {
            // only 2 panels since not connected to hibernate
            componentTreesTopSplitPane.setDividerLocation(300);
            componentTreesTopSplitPane.setTopComponent(getMemoryDaoPanel());
            componentTreesTopSplitPane.setBottomComponent(getFileDaoPanel());
        }
        else {
            // all 3 panels since we're connected to hibernate
            JSplitPane componentTreesBottomSplitPane = new JSplitPane();
            componentTreesBottomSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
            
            componentTreesTopSplitPane.setDividerLocation(200);
            componentTreesTopSplitPane.setTopComponent(getMemoryDaoPanel());
            componentTreesTopSplitPane.setBottomComponent(componentTreesBottomSplitPane);
            
            componentTreesBottomSplitPane.setDividerLocation(200);
            componentTreesBottomSplitPane.setTopComponent(getFileDaoPanel());
            componentTreesBottomSplitPane.setBottomComponent(getRemoteDaoPanel());
        }
        
        componentTreesPanel.add(componentTreesTopSplitPane);
        
        
        //////////////////////////
        // setup the desktop panel
        desktopPane = new DesktopPane();
        desktopPane.setBackground(new Color(135, 145, 161));
        
        JScrollPane desktopScrollPane = new javax.swing.JScrollPane();
        desktopScrollPane.setViewportView(desktopPane);
        
        desktopPanel = new JPanel();
        desktopPanel.setLayout(new java.awt.GridLayout(1, 0));
        desktopPanel.add(desktopScrollPane);
        
        
        //////////////////////
        // setup the log panel
        logPanel = new CapselaLogPanel();
        
        
        ////////////////////////////////////
        // setup the desktop/log split panel
        JSplitPane desktopLogSplitPane = new JSplitPane();
        
        desktopAndStatusPanel = new javax.swing.JPanel();
        desktopAndStatusPanel.setLayout(new java.awt.GridLayout(1, 0));

        desktopLogSplitPane.setDividerLocation(480);
        desktopLogSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        desktopLogSplitPane.setRequestFocusEnabled(false);
        
        desktopLogSplitPane.setTopComponent(desktopPanel);
        desktopLogSplitPane.setBottomComponent(logPanel);

        desktopAndStatusPanel.add(desktopLogSplitPane);
        
        
        ////////////////////////////
        // setup the main split pane
        JSplitPane componentEditorSplitPane = new JSplitPane();
        
        componentEditorSplitPane.setDividerLocation(215);
        componentEditorSplitPane.setLeftComponent(componentTreesPanel);
        componentEditorSplitPane.setRightComponent(desktopAndStatusPanel);
        
        
        
        ////////////////////////////////////////////////////
        // final setup of the main window/setup misc options
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Nexus Process Editor");
        setCursor(new Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setForeground(Color.lightGray);
        setName("Nexus Process Editor");
        this.setIconImage(ApplicationIcon.getIcon("NexusFrame.window_icon", ApplicationIcon.LARGE_SIZE).getImage());
        
//        getContentPane().add(componentEditorSplitPane, BorderLayout.CENTER);
        JmsClient c = (JmsClient) BootstrapConfiguration.getInstance().getApplicationContext().getBean("jmsClient");
		try {
			c.start();
			c.attachListener(this);
		} catch(Exception e) {
			LOG.error(e);
		}
		
        /////////////////////////
        // setup components panel
//        SpecificationDAO componentsDAO = DAOFactory.getDAOFactory(DAOFactory.Type.MEMORY).getSpecificationModelDAO();
        DAOFactory factory = (DAOFactory) BootstrapConfiguration.getInstance().getApplicationContext().getBean( "daoFactory" );
        DAO componentsDAO = factory.getDAO(PersistenceType.MEMORY); 
        DataContext componentsContext = new DataContext(componentsDAO, EditorDataProxy.class);
        
        DatasourceRoot componentsRoot = new DatasourceRoot("virtual://components/");
        EditorDataProxy componentsRootProxy =
            (EditorDataProxy) componentsContext.createProxy(componentsRoot, null);
        componentsContext.attachProxy(componentsRootProxy, componentsRoot, null);
        
//        SharedNode componentsRootNode = new SharedNode(componentsRootProxy, o);
        SharedNode componentsRootNode = componentsRootProxy.getTreeNode();
        
        SharedNodeTreeModel componentsTreeModel = new SharedNodeTreeModel(componentsRootNode,true);
        componentsRootNode.setTreeModel(componentsTreeModel);
        
        componentsRootNode = setupComponentsList( componentsRootNode );
        
        componentsTreeModel.setRoot( componentsRootNode );
        
        STree componentsListTree = new STree(componentsTreeModel);
        componentsListTree.setShowsRootHandles(false);
        componentsListTree.setRowHeight(26);
        
        		
	       /////////////////////////
        // setup components frame
        componentsTreePanel = new TreePanel(componentsListTree, true);
        componentsTreeModel.nodeStructureChanged( componentsRootNode );

        //Dockable stuff start
//		setDefaultCloseOperation(EXIT_ON_CLOSE);
		Viewport port = new Viewport();
		DockingManager.setDockableFactory(this);
		DockingManager.setMainDockingPort(this, PALETTE);
		PerspectiveManager.setFactory(new MyPerspectiveFactory());
		PerspectiveManager.getInstance().setCurrentPerspective(PERSPECTIVE_ID,
				true);
		PersistenceHandler persister = FilePersistenceHandler
				.createDefault(PERSISTANCE_FILE);
		PerspectiveManager.setPersistenceHandler(persister);
		EffectsManager.setPreview(new GhostPreview());
		try {
			DockingManager.loadLayoutModel();
		} catch (Exception e) {
			logger.fine("Docking layout not loaded.");
		}
		//DockingManager.setAutoPersist(true);
		port.setPreferredSize(new Dimension(640, 480));
		getContentPane().add(port);
		DockingManager.restoreLayout();
		//Dockable stuff end

		pack();
     }

	protected Component createComponent(String id) {
		Container comp = null;
		boolean closable = false;
		boolean pinnable = true;
		if (EDITOR.equals(id)) {
			comp = desktopPanel;
		} else if (DAO.equals(id)) {
			comp = memoryDaoPanel;
		} else if (FILE_DAO.equals(id)) {
			comp = fileDaoPanel;
		} else if (REMOTE_DAO.equals(id)) {
			comp = remoteDaoPanel;
		} else if (PALETTE.equals(id)) {
			comp = componentsTreePanel;
		} else if (LOGS.equals(id)) {
			comp = logPanel;
		} else {
			comp = new JButton(id);
		}
		View view = new View(id, id);
		// The order of actions matter. Close should be leftmost.
		if (closable)
			view.addAction(View.CLOSE_ACTION);
		if (pinnable)
			view.addAction(View.PIN_ACTION);
		view.setContentPane(comp);
		return view;
	}
    
	public void removeInternalFrameMenuItem(JInternalFrame frame) {
        WorkflowMenuBar bar = (WorkflowMenuBar) this.getJMenuBar();
        bar.removeWindowItem(frame);
	}
	
    private SharedNode setupComponentsList( SharedNode root ) {
        SharedNode newRoot = root;
        try {
            SharedNodeTreeModel model = (SharedNodeTreeModel) root.getTreeModel();
            DataProxy proxy = root.getProxy();
            DataContext context = proxy.getContext();
            
            Command createSpec = new CreateSpecificationCommand( root.getProxy(), "testspec", model );
            WorkflowEditor.getExecutor().executeCommand(createSpec).get();
            
            SharedNode specNode = (SharedNode) root.getChildAt( 0 );
            assert specNode.getProxy().getData() instanceof YSpecification : "invalid child of root";
            
            Command createNet = new CreateNetCommand( specNode.getProxy(), "components net", model );
            WorkflowEditor.getExecutor().executeCommand(createNet).get();
            
            SharedNode netNode = (SharedNode) specNode.getChildAt( 0 );
            assert netNode.getProxy().getData() instanceof YNet : "invalid child of specification";
            
            for( NexusServiceInfo info : NexusServiceInfo.SERVICES ) {
                Command createComponent = new CreateNexusComponentCommand(
                        netNode.getProxy(),
                        info.getServiceName(),
                        info.getServiceName(),
                        info,
                        model );
                WorkflowEditor.getExecutor().executeCommand(createComponent).get();
            }
            
            newRoot = netNode;
        }
        catch( Exception e ) {
            e.printStackTrace( System.out );
        }
        return newRoot;
    }
    public static CommandExecutor getExecutor() {
        if( executor == null ) {
            executor = new CommandExecutor(DEFAULT_COMMAND_STACK_SIZE);
            executor.addCommandCompletionListener( (WorkflowMenuBar) getInstance().getJMenuBar() );
        }
		return executor;
	}
    
	public static WorkflowEditor getInstance() {
    	if (WorkflowEditor.singleton == null) {
    		WorkflowEditor.singleton = new WorkflowEditor();
    	}
    	return singleton;
    }
    
    public static void setExecutor(CommandExecutor executor) {
		WorkflowEditor.executor = executor;
	}
    

	private class WindowFocusAction extends AbstractAction {
        private CapselaInternalFrame frame;
        private WindowFocusAction( CapselaInternalFrame frame ) {
            this.frame = frame;
        }
        public void actionPerformed( ActionEvent e ) {
            frame.toFront();
            try {
                frame.setSelected( true );
            }
            catch( Exception ex ) {
                // ignore
            }
        }
    }
	private class UndoAction  extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			getExecutor().undo();
		}
	};
	
	private class RedoAction  extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			getExecutor().redo();
		}
	};

	private class ExitAction  extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			System.exit(0);
		}
	};
	
	private class OpenSchedulingCalendarAction  extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
	    	try {
		    	try {
					openEditor( SchedulerCalendar.createCalendar(), null );
				}
				catch( SchedulerException e1 ) {
					WorkflowEditor.LOG.error( "Error connecting to remote scheduler!", e1 );
					// TODO remove the testing calendar later
					LOG.warn( "OPENING TESTING CALENDAR" );
					openEditor( SchedulerCalendar.createTestCalendar(), null );
				}
	    	}
			catch( Exception e2 ) {
				WorkflowEditor.LOG.error( "Error opening scheduling calendar!", e2 );
			}
		}
	};

	private class EditPreferencesAction  extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			NexusClientConfiguration config = (NexusClientConfiguration) BootstrapConfiguration
			.getInstance();
			Properties p = null;
			try {
				p = config.getProperties();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			ConfigurationDialog dialog = new ConfigurationDialog(WorkflowEditor.this, p);
			boolean shouldSave = dialog.ask();
			if (shouldSave) {
				Property[] pa = dialog.getProperties();
				for (Property prop : pa) {
					p.setProperty(prop.getName(), prop.getValue().toString());
				}
				try {
					config.saveProperties();
				} catch (IOException ex) {
					JOptionPane.showMessageDialog(null,
							"Unable to save configuration due to "
									+ ex.getMessage() + ".",
							"Error", JOptionPane.ERROR_MESSAGE);
				}
			}	
		}
	}


	/**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
	    JFrame.setDefaultLookAndFeelDecorated(true);
		try {
	        UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
	    }
        catch (Exception e) {}
     	WorkflowEditor.splash = new NexusSplashScreen();
    	WorkflowEditor.splash.setVisible(true);
    	splash.updateRelative(0, "initializing client");
		PropertyConfigurator.configure( WorkflowEditor.class.getResource( "client.logging.properties" ) );
    	splash.updateRelative(10, "loading configuration");
		BootstrapConfiguration.setInstance(NexusClientConfiguration.getInstance());
    	WorkflowEditor.getMemoryDaoPanel();
    	splash.updateRelative(10, "connecting to server");
    	WorkflowEditor.getRemoteDaoPanel();
    	splash.updateRelative(20, "parsing existing workflow files");
    	WorkflowEditor.getFileDaoPanel();
    	splash.updateRelative(30, "starting client");
        WorkflowEditor.getInstance();
    	splash.updateRelative(20, "starting client");
        WorkflowEditor.getInstance().setVisible(true);
        splash.setVisible(false);
        splash.setVisible(true);
    	splash.updateRelative(10, "started");
        try {Thread.sleep(1500);} catch (Exception e) {}
        splash.setVisible(false);
        splash.dispose();
    }
	
}
