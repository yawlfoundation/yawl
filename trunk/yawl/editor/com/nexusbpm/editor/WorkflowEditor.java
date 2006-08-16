package com.nexusbpm.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;

import au.edu.qut.yawl.elements.YNet;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.persistence.dao.DAO;
import au.edu.qut.yawl.persistence.dao.DAOFactory;
import au.edu.qut.yawl.persistence.dao.DatasourceRoot;
import au.edu.qut.yawl.persistence.dao.DAOFactory.PersistenceType;
import au.edu.qut.yawl.persistence.managed.DataContext;
import au.edu.qut.yawl.persistence.managed.DataProxy;

import com.nexusbpm.command.Command;
import com.nexusbpm.command.CommandExecutor;
import com.nexusbpm.command.CreateNetCommand;
import com.nexusbpm.command.CreateNexusComponentCommand;
import com.nexusbpm.command.CreateSpecificationCommand;
import com.nexusbpm.command.CommandExecutor.CommandCompletionListener;
import com.nexusbpm.command.CommandExecutor.ExecutionResult;
import com.nexusbpm.editor.desktop.CapselaInternalFrame;
import com.nexusbpm.editor.desktop.DesktopPane;
import com.nexusbpm.editor.editors.ComponentEditor;
import com.nexusbpm.editor.editors.DataTransferEditor;
import com.nexusbpm.editor.icon.ApplicationIcon;
import com.nexusbpm.editor.logger.CapselaLogPanel;
import com.nexusbpm.editor.persistence.EditorDataProxy;
import com.nexusbpm.editor.tree.STree;
import com.nexusbpm.editor.tree.SharedNode;
import com.nexusbpm.editor.tree.SharedNodeTreeModel;
import com.nexusbpm.services.NexusServiceInfo;

/**
 *
 * @author  SandozM
 * @author Nathan Rose
 */
public class WorkflowEditor extends javax.swing.JFrame {
    
	private final static int DEFAULT_CLIENT_WIDTH = 800;
	private final static int DEFAULT_CLIENT_HEIGHT = 692;
    private final static int DEFAULT_COMPONENTS_WIDTH = 170;
    private final static int DEFAULT_COMPONENTS_HEIGHT = 692;
    
	private final static int DEFAULT_COMMAND_STACK_SIZE = 20;
	private static CommandExecutor executor;
	private static WorkflowEditor singleton = null;
    
    private static final Log LOG = LogFactory.getLog( WorkflowEditor.class );
    
    private JFrame _componentsFrame;
	
    /**
     * Creates new form WorkflowEditor 
     */
    private WorkflowEditor() {
        // we need to set the singleton instance before the constructor returns so the constructor
        // can setup the singleton command executor
        WorkflowEditor.singleton = this;
		PropertyConfigurator.configure( WorkflowEditor.class.getResource( "client.logging.properties" ) );
    	initComponents();
        
        this.pack();
    	this.setSize(DEFAULT_CLIENT_WIDTH,DEFAULT_CLIENT_HEIGHT);
        this.setLocationRelativeTo( null );
        _componentsFrame.setLocation(
                (int)( this.getLocation().getX() - DEFAULT_COMPONENTS_WIDTH ),
                (int) this.getLocation().getY() );
    }
    
    public static WorkflowEditor getInstance() {
    	if (WorkflowEditor.singleton == null) {
    		WorkflowEditor.singleton = new WorkflowEditor();
    	}
    	return singleton;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     */
    private void initComponents() {
	    JFrame.setDefaultLookAndFeelDecorated(true);
		try {
	        UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
	    }
        catch (Exception e) {
            e.printStackTrace();
        }
        
        
        ///////////////////////
        // create the file menu
        fileMenu = new JMenu();
        fileMenu.setText("File");
        fileMenu.setMnemonic( KeyEvent.VK_F );
        
        openMenuItem = new JMenuItem();
        openMenuItem.setText("Open");
        fileMenu.add(openMenuItem);
        
        saveMenuItem = new JMenuItem();
        saveMenuItem.setText("Save");
        fileMenu.add(saveMenuItem);
        
        saveAsMenuItem = new JMenuItem();
        saveAsMenuItem.setText("Save As ...");
        fileMenu.add(saveAsMenuItem);
        
        exitMenuItem = new JMenuItem();
        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exitMenuItem);
        
        
        ///////////////////////
        // create the edit menu
        editMenu = new JMenu();
        editMenu.setText("Edit");
        editMenu.setMnemonic( KeyEvent.VK_E );
        
        undoMenuItem = new JMenuItem( new AbstractAction( "Undo" ) {
            public void actionPerformed( ActionEvent e ) {
                getExecutor().undo();
            }
        });
        undoMenuItem.setEnabled( false );
        editMenu.add( undoMenuItem );
        
        redoMenuItem = new JMenuItem( new AbstractAction( "Redo" ) {
            public void actionPerformed( ActionEvent e ) {
                getExecutor().redo();
            }
        });
        redoMenuItem.setEnabled( false );
        editMenu.add( redoMenuItem );
        
        editMenu.add( new JSeparator() );
        
        cutMenuItem = new JMenuItem();
        cutMenuItem.setText("Cut");
        editMenu.add(cutMenuItem);
        
        copyMenuItem = new JMenuItem();
        copyMenuItem.setText("Copy");
        editMenu.add(copyMenuItem);
        
        pasteMenuItem = new JMenuItem();
        pasteMenuItem.setText("Paste");
        editMenu.add(pasteMenuItem);
        
        deleteMenuItem = new JMenuItem();
        deleteMenuItem.setText("Delete");
        editMenu.add(deleteMenuItem);
        
        
        /////////////////////////
        // create the window menu
        windowMenu = new JMenu();
        windowMenu.setText("Window");
        windowMenu.setMnemonic( KeyEvent.VK_W );
        
        noWindowOpenItem = new JMenuItem();
        noWindowOpenItem.setText("None");
        noWindowOpenItem.setEnabled(false);
        windowMenu.add(noWindowOpenItem);
        
        windowItems = new HashMap<CapselaInternalFrame,JMenuItem>();
        
        
        ///////////////////////
        // create the help menu
        helpMenu = new JMenu();
        helpMenu.setText("Help");
        helpMenu.setMnemonic( KeyEvent.VK_H );
        
        contentsMenuItem = new JMenuItem();
        contentsMenuItem.setText("Contents");
        helpMenu.add(contentsMenuItem);
        
        aboutMenuItem = new JMenuItem();
        aboutMenuItem.setText("About");
        helpMenu.add(aboutMenuItem);
        
        
        ///////////////////////////////
        // create and set the main menu
        menuBar = new JMenuBar();
        
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(windowMenu);
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);
        
        
        ////////////////////////////
        // create the memory context
//        SpecificationDAO memdao = DAOFactory.getDAOFactory(DAOFactory.Type.MEMORY).getSpecificationModelDAO();
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
        componentList1Panel = new TreePanel( memoryComponentListTree, true );
        
        
        //////////////////////////
        // create the file context
//        SpecificationDAO filedao = DAOFactory.getDAOFactory(DAOFactory.Type.FILE).getSpecificationModelDAO();
        DAO filedao = DAOFactory.getDAO( PersistenceType.FILE );
        DataContext filedc = new DataContext(filedao, EditorDataProxy.class);
        
        File fileRootObject= new File( new File(".").getAbsoluteFile().toURI().normalize() );
        DatasourceRoot fileRoot = new DatasourceRoot(fileRootObject);
        EditorDataProxy filedp = (EditorDataProxy) filedc.createProxy(fileRoot, null);
        filedc.attachProxy(filedp, fileRoot, null);
        
//        SharedNode fileRootNode = new SharedNode(filedp, o);
        SharedNode fileRootNode = filedp.getTreeNode();
        
        SharedNodeTreeModel fileTreeModel = new SharedNodeTreeModel(fileRootNode);
        fileRootNode.setTreeModel(fileTreeModel);
        
        STree fileComponentListTree = new STree(fileTreeModel);
        fileComponentListTree.setShowsRootHandles(false);
        fileComponentListTree.setRootVisible(true);
        fileComponentListTree.setRowHeight(26);
        
        
        //////////////////////////////////////////////////
        // create the middle component pane (file context)
        componentList2Panel = new TreePanel( fileComponentListTree, true );
        
        
        ///////////////////////////////
        // create the hibernate context
        STree hibernateComponentListTree = null;
        try {
//            SpecificationDAO hibernatedao = DAOFactory.getDAOFactory(DAOFactory.Type.HIBERNATE).getSpecificationModelDAO();
        	DAO hibernatedao = DAOFactory.getDAO( PersistenceType.HIBERNATE );
            DataContext hibdc = new DataContext(hibernatedao, EditorDataProxy.class);
            
            DatasourceRoot hibernateRoot = new DatasourceRoot("hibernate://home/");
            EditorDataProxy hibdp = (EditorDataProxy) hibdc.createProxy(hibernateRoot, null);
            hibdc.attachProxy(hibdp, hibernateRoot, null);
            
//            SharedNode hibernateRootNode = new SharedNode(hibdp, o);
            SharedNode hibernateRootNode = hibdp.getTreeNode();
            
            SharedNodeTreeModel hibernateTreeModel = new SharedNodeTreeModel(hibernateRootNode);
            hibernateRootNode.setTreeModel(hibernateTreeModel);
            
            hibernateComponentListTree = new STree(hibernateTreeModel);
            hibernateComponentListTree.setShowsRootHandles(false);
            hibernateComponentListTree.setRootVisible(true);
            hibernateComponentListTree.setRowHeight(26);
        }
        catch( Exception e ) {
            LOG.error( "Error connecting to database!", e );
            hibernateComponentListTree = null;
        }
        
        
        ///////////////////////////////////////////////////////
        // create the bottom component pane (hibernate context)
        if( hibernateComponentListTree != null ) {
            try {
                componentList3Panel = new TreePanel( hibernateComponentListTree, true );
            }
            catch( Exception e ) {
                LOG.error( "Error displaying database component list!", e );
                componentList3Panel = null;
            }
        }
        else {
            componentList3Panel = null;
        }
        
        
        
        //////////////////////////////////
        // setup the component tree panels
        componentTreesPanel = new JPanel();
        componentTreesPanel.setLayout(new BorderLayout());
        
        // create the top split pane
        JSplitPane componentTreesTopSplitPane = new JSplitPane();
        componentTreesTopSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        
        if( componentList3Panel == null ) {
            // only 2 panels since not connected to hibernate
            componentTreesTopSplitPane.setDividerLocation(300);
            componentTreesTopSplitPane.setTopComponent(componentList1Panel);
            componentTreesTopSplitPane.setBottomComponent(componentList2Panel);
        }
        else {
            // all 3 panels since we're connected to hibernate
            JSplitPane componentTreesBottomSplitPane = new JSplitPane();
            componentTreesBottomSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
            
            componentTreesTopSplitPane.setDividerLocation(200);
            componentTreesTopSplitPane.setTopComponent(componentList1Panel);
            componentTreesTopSplitPane.setBottomComponent(componentTreesBottomSplitPane);
            
            componentTreesBottomSplitPane.setDividerLocation(200);
            componentTreesBottomSplitPane.setTopComponent(componentList2Panel);
            componentTreesBottomSplitPane.setBottomComponent(componentList3Panel);
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
        
//        try {
//        	Command createSpec = new CreateSpecificationCommand( memdp, "testspec" );
//        	executor.executeCommand(createSpec).get();
//            Set<DataProxy> children = memdc.getChildren( memdp, false );
//            for( DataProxy child : children ) {
//                if( child.getData() instanceof YSpecification ) {
//                    Command createNet = new CreateNetCommand( (EditorDataProxy) child, "testnet" );
//                    executor.executeCommand(createNet).get();
//                    Set<DataProxy> subchildren = child.getContext().getChildren( child, false );
//                    for( DataProxy subchild : subchildren ) {
//                        if( subchild.getData() instanceof YNet ) {
//                            Command command = new CreateConditionCommand( (EditorDataProxy) subchild,
//                                    CreateConditionCommand.TYPE_INPUT_CONDITION, null);
//                            executor.executeCommand(command).get();
//                            command = new CreateConditionCommand( (EditorDataProxy) subchild,
//                                    CreateConditionCommand.TYPE_OUTPUT_CONDITION, null);
//                            executor.executeCommand(command).get();
//                            command = new CreateNexusComponent( (EditorDataProxy) subchild,
//                                    "jython", "jython", NexusServiceInfo.getServiceWithName( "Jython" ) );
//                            executor.executeCommand(command).get();
//                            command = new CreateNexusComponent( (EditorDataProxy) subchild,
//                                    "email sender", "email_sender", NexusServiceInfo.getServiceWithName( "EmailSender" ) );
//                            executor.executeCommand(command).get();
//                            YNet net = (YNet) subchild.getData();
//                            command = new CreateFlowCommand(
//                                    (EditorDataProxy) memdc.getDataProxy( net.getInputCondition(), null ),
//                                    (EditorDataProxy) memdc.getDataProxy( net.getNetElement( "jython" ), null ) );
//                            executor.executeCommand(command).get();
//                            command = new CreateFlowCommand(
//                                    (EditorDataProxy) memdc.getDataProxy( net.getNetElement( "jython" ), null ),
//                                    (EditorDataProxy) memdc.getDataProxy( net.getNetElement( "email_sender" ), null ) );
//                            executor.executeCommand(command).get();
//                            command = new CreateFlowCommand(
//                                    (EditorDataProxy) memdc.getDataProxy( net.getNetElement( "email_sender" ), null ),
//                                    (EditorDataProxy) memdc.getDataProxy( net.getOutputCondition(), null ) );
//                            executor.executeCommand(command).get();
//                        }
//                    }
//                }
//            }
//        }
//        catch( Exception e ) {
//            e.printStackTrace( System.out );
//            System.out.flush();
//        }
        
        
        ////////////////////////////////////////////////////
        // final setup of the main window/setup misc options
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("NexusWorkflow Process Editor");
        setCursor(new Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setForeground(Color.lightGray);
        setName("NexusWorkflow Process Editor");
        this.setIconImage(ApplicationIcon.getIcon("NexusFrame.window_icon", ApplicationIcon.LARGE_SIZE).getImage());
        
        getContentPane().add(componentEditorSplitPane, BorderLayout.CENTER);
        
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

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {                                             
        System.exit(0);
    }
    
    /**
     * Opens an editor centered at the given location or maximized if the location
     * is null.
     */
    public void openComponentEditor( EditorDataProxy proxy, Point location ) throws Exception {
        CapselaInternalFrame editor = proxy.getEditor();
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
    
    public void openDataEditor( EditorDataProxy proxy, Point location ) throws Exception {
        ComponentEditor editor = new DataTransferEditor();
        
        editor.resetTitle( proxy );
        editor.addInternalFrameListener( proxy.getInternalFrameListener( editor ) );
        
        openEditor( editor, location );
    }
    
    public void addInternalFrameMenuItem( CapselaInternalFrame frame ) {
        assert frame != null : "attempting to add menu item for null frame!";
        windowMenu.remove( noWindowOpenItem );
        JMenuItem item = new JRadioButtonMenuItem();
        item.setAction( new WindowFocusAction( frame ) );
        item.setText( frame.getTitle() );
        windowMenu.add( item );
        windowItems.put( frame, item );
        setSelectedInternalFrameMenuItem( frame );
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
    
    public void setSelectedInternalFrameMenuItem( CapselaInternalFrame frame ) {
        for( CapselaInternalFrame key : windowItems.keySet() ) {
            windowItems.get( key ).setSelected( key == frame );
        }
    }
    
    public void removeInternalFrameMenuItem( CapselaInternalFrame frame ) {
        if( windowItems.containsKey( frame ) ) {
            JMenuItem item = windowItems.get( frame );
            windowMenu.remove( item );
            windowItems.remove( frame );
            if( windowItems.size() == 0 ) {
                windowMenu.add( noWindowOpenItem );
            }
        }
        else {
            LOG.warn( "Internal window was closed, but it wasn't on the window menu!" );
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                WorkflowEditor.getInstance().setVisible(true);
            }
        });
    }
    
    /**
     * Split pane that splits the entire window left to right, with
     * the trees on the left and the desktop and log on the right.
     */
//    private JSplitPane componentEditorSplitPane;
//    
//    private JSplitPane componentTreesTopSplitPane;
//    private JSplitPane componentTreesBottomSplitPane;
//    private JSplitPane desktopLogSplitPane;
    
    /** The panel that contains the component tree split panes. */
    private JPanel componentTreesPanel;
    
    private TreePanel componentList1Panel;
    private TreePanel componentList2Panel;
    private TreePanel componentList3Panel;
    
//    private JScrollPane componentList1ScrollPane;
//    private JScrollPane componentList2ScrollPane;
//    private JScrollPane componentList3ScrollPane;
    
    
//    private STree memoryComponentListTree;
//    private STree fileComponentListTree;
    
    private JPanel desktopAndStatusPanel;
    
    private JDesktopPane desktopPane;
//    private JPanel desktopPanel;
//    private JScrollPane desktopScrollPane;
//    private CapselaLogPanel logPanel;
    
    // main menu
    private JMenuBar menuBar;
    
    // top level menu options
    private JMenu fileMenu;
    private JMenu editMenu;
    private JMenu windowMenu;
    private JMenu helpMenu;
    
    // options under the file menu
    private JMenuItem openMenuItem;
    private JMenuItem saveMenuItem;
    private JMenuItem saveAsMenuItem;
    private JMenuItem exitMenuItem;
    
    // options under the edit menu
    private JMenuItem undoMenuItem;
    private JMenuItem redoMenuItem;
    private JMenuItem cutMenuItem;
    private JMenuItem copyMenuItem;
    private JMenuItem pasteMenuItem;
    private JMenuItem deleteMenuItem;
    
    // options under the window menu
    private JMenuItem noWindowOpenItem;
    private Map<CapselaInternalFrame,JMenuItem> windowItems;
    
    // options under the help menu
    private JMenuItem contentsMenuItem;
    private JMenuItem aboutMenuItem;
    
	public JDesktopPane getDesktopPane() {
		return desktopPane;
	}

	public static CommandExecutor getExecutor() {
        if( executor == null ) {
            executor = new CommandExecutor(DEFAULT_COMMAND_STACK_SIZE);
            executor.addCommandCompletionListener( getInstance().createCommandMenuUpdater() );
        }
		return executor;
	}
    
    private CommandMenuUpdater createCommandMenuUpdater() {
        return new CommandMenuUpdater();
    }
    
    private class CommandMenuUpdater implements CommandCompletionListener {
        public void commandCompleted( ExecutionResult result ) {
            WorkflowEditor.this.undoMenuItem.setEnabled( result.canUndo() );
            WorkflowEditor.this.redoMenuItem.setEnabled( result.canRedo() );
        }
    }

	public static void setExecutor(CommandExecutor executor) {
		WorkflowEditor.executor = executor;
	}
}
