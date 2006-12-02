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
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;
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
import com.nexusbpm.editor.logger.CapselaLogPanel;
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
public class WorkflowEditor extends JFrame implements MessageListener {
    

	private final static int DEFAULT_CLIENT_HEIGHT = 692;
	private final static int DEFAULT_CLIENT_WIDTH = 800;
    private final static int DEFAULT_COMMAND_STACK_SIZE = 20;
    private final static int DEFAULT_COMPONENTS_HEIGHT = 692;
    
	private final static int DEFAULT_COMPONENTS_WIDTH = 170;
	private static CommandExecutor executor;
	private static final Log LOG = LogFactory.getLog( WorkflowEditor.class );
    private static WorkflowEditor singleton = null;
	private static NexusSplashScreen splash;
    private JFrame _componentsFrame;
    /** The panel that contains the component tree split panes. */
    private JPanel componentTreesPanel; 

    private JPanel desktopAndStatusPanel;
    private JDesktopPane desktopPane;
    private JPanel fileDaoPanel;
    private JPanel memoryDaoPanel;
    private JPanel remoteDaoPanel;
    private JInternalFrame selectedInternalFrame;

    /**
     * Creates new form WorkflowEditor 
     */
    private WorkflowEditor() {
        // we need to set the singleton instance before the constructor returns so the constructor
        // can setup the singleton command executor
        WorkflowEditor.singleton = this;

		initComponents();
        
        this.pack();
    	this.setSize(DEFAULT_CLIENT_WIDTH,DEFAULT_CLIENT_HEIGHT);
        this.setLocationRelativeTo( null );
        _componentsFrame.setLocation(
                (int)( this.getLocation().getX() - DEFAULT_COMPONENTS_WIDTH ),
                (int) this.getLocation().getY() );
    }
    public void addInternalFrameMenuItem( CapselaInternalFrame frame ) {
        assert frame != null : "attempting to add menu item for null frame!";
        WorkflowMenuBar bar = (WorkflowMenuBar) this.getJMenuBar();
        JMenuItem newMenuItem = bar.addWindowItem(frame);
        newMenuItem.setAction( new WindowFocusAction( frame ) );
        setSelectedInternalFrameMenuItem( frame );
    }
    
    public void setSelectedInternalFrameMenuItem(JInternalFrame frame) {
    	WorkflowEditor.getInstance().setTitle("Nexus Editor - " + frame.getTitle());
    }
    
    public JDesktopPane getDesktopPane() {
		return desktopPane;
	}
    public JPanel getFileDaoPanel() {
		if (fileDaoPanel == null) {
			DAO filedao = DAOFactory.getDAO(PersistenceType.FILE);
			DataContext filedc = new DataContext(filedao, EditorDataProxy.class);

			File fileRootObject = new File(new File(".").getAbsoluteFile()
					.toURI().normalize());
			DatasourceRoot fileRoot = new DatasourceRoot(fileRootObject);
			EditorDataProxy filedp = (EditorDataProxy) filedc.createProxy(
					fileRoot, null);
			filedc.attachProxy(filedp, fileRoot, null);
			// SharedNode fileRootNode = new SharedNode(filedp, o);
			SharedNode fileRootNode = filedp.getTreeNode();

			SharedNodeTreeModel fileTreeModel = new SharedNodeTreeModel(
					fileRootNode);
			fileRootNode.setTreeModel(fileTreeModel);

			STree fileComponentListTree = new STree(fileTreeModel);
			fileComponentListTree.setShowsRootHandles(false);
			fileComponentListTree.setRootVisible(true);
			fileComponentListTree.setRowHeight(26);

			fileDaoPanel = new TreePanel(fileComponentListTree, true);
		}
		return fileDaoPanel;

	}
    
    public JPanel getMemoryDaoPanel() {
    	if (memoryDaoPanel == null) {
        DAO memdao = DAOFactory.getDAO( PersistenceType.MEMORY );
        DataContext memdc = new DataContext(memdao, EditorDataProxy.class);
        
        DatasourceRoot virtualRoot = new DatasourceRoot("virtual://memory/home/");
        EditorDataProxy memdp = (EditorDataProxy) memdc.createProxy(virtualRoot, null);
        memdc.attachProxy(memdp, virtualRoot, null);
        
//        SharedNode memRootNode = new SharedNode(memdp, o);
        SharedNode memRootNode = memdp.getTreeNode();
        
        SharedNodeTreeModel memTreeModel = new SharedNodeTreeModel(memRootNode);
        memRootNode.setTreeModel(memTreeModel);
        
        STree memoryComponentListTree = new STree(memTreeModel);
        memoryComponentListTree.setShowsRootHandles(false);
        memoryComponentListTree.setRootVisible(true);
        memoryComponentListTree.setRowHeight(26);
        
        
        /////////////////////////////////////////////////
        // create the top component pane (memory context)
        memoryDaoPanel = new TreePanel( memoryComponentListTree, true );
    	}
    	return memoryDaoPanel;
    	
    }
    public JPanel getRemoteDaoPanel() {
    	if (remoteDaoPanel == null) {
    		STree hibernateComponentListTree = null;
    		try {
    			DAO hibernatedao = (DAO) BootstrapConfiguration.getInstance().getApplicationContext().getBean("yawlEngineDao");
	            DataContext hibdc = new DataContext(hibernatedao, EditorDataProxy.class);
	            
	            DatasourceRoot hibernateRoot = new DatasourceRoot("YawlEngine://home/");
	            EditorDataProxy hibdp = (EditorDataProxy) hibdc.createProxy(hibernateRoot, null);
	            hibdc.attachProxy(hibdp, hibernateRoot, null);
	            SharedNode hibernateRootNode = hibdp.getTreeNode();
            
	            SharedNodeTreeModel hibernateTreeModel = new SharedNodeTreeModel(hibernateRootNode);
	            hibernateRootNode.setTreeModel(hibernateTreeModel);
	            
	            hibernateComponentListTree = new STree(hibernateTreeModel);
	            hibernateComponentListTree.setShowsRootHandles(false);
	            hibernateComponentListTree.setRootVisible(true);
	            hibernateComponentListTree.setRowHeight(26);
    		} catch( Exception e ) {
	            LOG.error( "Error connecting to database!", e );
	            hibernateComponentListTree = null;
	        }
    		if( hibernateComponentListTree != null ) {
	            try {
	                remoteDaoPanel = new TreePanel( hibernateComponentListTree, true );
	            }
	            catch( Exception e ) {
	                LOG.error( "Error displaying database component list!", e );
	                remoteDaoPanel = new JPanel();
	            }
    		}
    		else {
    			remoteDaoPanel = new JPanel();
    		}    	
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
			LOG.warn(sb.toString());
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
    
//    public void setSelectedInternalFrameMenuItem( CapselaInternalFrame frame ) {
//        for( CapselaInternalFrame key : windowItems.keySet() ) {
//            windowItems.get( key ).setSelected( key == frame );
//        }
//    }
//
    /**
	 * This method is called from within the constructor to initialize the form.
	 */
    private void initComponents() {
	    JFrame.setDefaultLookAndFeelDecorated(true);
		try {
	        UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
	    }
        catch (Exception e) {
            e.printStackTrace();
        }
        
        WorkflowMenuBar bar = new WorkflowMenuBar();
        setJMenuBar(bar);
        bar.setAction(bar.getExitMenuItem(), new ExitAction());
        bar.setAction(bar.getScheduledEventsMenuItem(), new OpenSchedulingCalendarAction());
        bar.setAction(bar.getPreferencesMenuItem(), new EditPreferencesAction());
        bar.setAction(bar.getRedoMenuItem(), new RedoAction());
        bar.setAction(bar.getUndoMenuItem(), new UndoAction());
        //////////////////////////////////
        // setup the component tree panels
        componentTreesPanel = new JPanel();
        componentTreesPanel.setLayout(new BorderLayout());
        
        // create the top split pane
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
        
        JPanel desktopPanel = new JPanel();
        desktopPanel.setLayout(new java.awt.GridLayout(1, 0));
        desktopPanel.add(desktopScrollPane);
        
        
        //////////////////////
        // setup the log panel
        CapselaLogPanel logPanel = new CapselaLogPanel();
        
        
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
        setTitle("NexusWorkflow Process Editor");
        setCursor(new Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setForeground(Color.lightGray);
        setName("NexusWorkflow Process Editor");
        this.setIconImage(ApplicationIcon.getIcon("NexusFrame.window_icon", ApplicationIcon.LARGE_SIZE).getImage());
        
        getContentPane().add(componentEditorSplitPane, BorderLayout.CENTER);
        JmsClient c = (JmsClient) BootstrapConfiguration.getInstance().getApplicationContext().getBean("jmsClient");
		try {
			c.start();
			c.attachListener(this);
		} catch(Exception e) {
			LOG.error(e);
		}
        
        pack();
        
        
        /////////////////////////
        // setup components panel
//        SpecificationDAO componentsDAO = DAOFactory.getDAOFactory(DAOFactory.Type.MEMORY).getSpecificationModelDAO();
        DAO componentsDAO = DAOFactory.getDAO( PersistenceType.MEMORY );
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
        componentsListTree.setRootVisible(true);
        componentsListTree.setRowHeight(26);
        
        
        
        
        /////////////////////////
        // setup components frame
        _componentsFrame = new JFrame( "Components" );
        _componentsFrame.setIconImage( ApplicationIcon.getIcon( "NexusFrame.window_icon", ApplicationIcon.LARGE_SIZE ).getImage() );
        TreePanel componentsTreePanel = new TreePanel( componentsListTree, false );
        _componentsFrame.getContentPane().add( componentsTreePanel );
        _componentsFrame.setSize( DEFAULT_COMPONENTS_WIDTH, DEFAULT_COMPONENTS_HEIGHT );
        _componentsFrame.setDefaultCloseOperation( WindowConstants.DO_NOTHING_ON_CLOSE );
        _componentsFrame.setVisible( true );
        
        componentsTreeModel.nodeStructureChanged( componentsRootNode );
    }

	private void openSchedulingCalendar() {
    	try {
	    	try {
				openEditor( SchedulerCalendar.createCalendar(), null );
			}
			catch( SchedulerException e ) {
				LOG.error( "Error connecting to remote scheduler!", e );
				// TODO remove the testing calendar later
				LOG.warn( "OPENING TESTING CALENDAR" );
				openEditor( SchedulerCalendar.createTestCalendar(), null );
			}
    	}
		catch( Exception e ) {
			LOG.error( "Error opening scheduling calendar!", e );
		}
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
            createSpec.execute();
            
            SharedNode specNode = (SharedNode) root.getChildAt( 0 );
            assert specNode.getProxy().getData() instanceof YSpecification : "invalid child of root";
            
            Command createNet = new CreateNetCommand( specNode.getProxy(), "components net", model );
            createNet.execute();
            
            SharedNode netNode = (SharedNode) specNode.getChildAt( 0 );
            assert netNode.getProxy().getData() instanceof YNet : "invalid child of specification";
            
            for( NexusServiceInfo info : Arrays.asList( NexusServiceInfo.SERVICES ) ) {
                Command createComponent = new CreateNexusComponentCommand(
                        netNode.getProxy(),
                        info.getServiceName(),
                        info.getServiceName(),
                        info,
                        model );
                createComponent.execute();
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

	/**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        try {java.awt.EventQueue.invokeAndWait(new Runnable() {public void run() {
	    	WorkflowEditor.splash = new NexusSplashScreen();
	    	WorkflowEditor.splash.setVisible(true);
	    }});} catch (Exception e) {e.printStackTrace();		}
    	splash.update(5, "Initializing Application");
		PropertyConfigurator.configure( WorkflowEditor.class.getResource( "client.logging.properties" ) );
    	splash.update(35, "Loading Configuration");
		BootstrapConfiguration.setInstance(NexusClientConfiguration.getInstance());
    	splash.update(75, "Starting Client");
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                WorkflowEditor.getInstance().setVisible(true);
                splash.setVisible(false);
                splash.dispose();
            }
        });
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
			WorkflowEditor.this.openSchedulingCalendar();
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
			ConfigurationDialog dialog = new ConfigurationDialog(null, p);
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
	};
	}
