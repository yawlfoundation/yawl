package com.nexusbpm.editor;

import au.edu.qut.yawl.elements.YExternalNetElement;
import au.edu.qut.yawl.elements.YInputCondition;
import au.edu.qut.yawl.elements.YNet;
import au.edu.qut.yawl.elements.YOutputCondition;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.persistence.dao.DAO;
import au.edu.qut.yawl.persistence.dao.DAOFactory;
import au.edu.qut.yawl.persistence.dao.DatasourceRoot;
import au.edu.qut.yawl.persistence.dao.DAOFactory.PersistenceType;
import au.edu.qut.yawl.persistence.managed.DataContext;
import au.edu.qut.yawl.persistence.managed.DataProxy;
import au.edu.qut.yawl.util.configuration.BootstrapConfiguration;

import com.nexusbpm.command.Command;
import com.nexusbpm.command.CreateFlowCommand;
import com.nexusbpm.command.CreateNetCommand;
import com.nexusbpm.command.CreateNexusComponentCommand;
import com.nexusbpm.command.CreateSpecificationCommand;
import com.nexusbpm.editor.persistence.EditorDataProxy;
import com.nexusbpm.editor.tree.SharedNode;
import com.nexusbpm.editor.tree.SharedNodeTreeModel;
import com.nexusbpm.services.NexusServiceInfo;

public class CommandBasedNexusSpecificationFactory {

	public static YSpecification getInstance() {
		DAOFactory factory = (DAOFactory) BootstrapConfiguration.getInstance().getApplicationContext().getBean("daoFactory");
        DAO componentsDAO = factory.getDAO( PersistenceType.MEMORY );
        DataContext context = new DataContext(componentsDAO, EditorDataProxy.class);
        
        DatasourceRoot componentsRoot = new DatasourceRoot("virtual://components/");
        EditorDataProxy componentsRootProxy =
            (EditorDataProxy) context.createProxy(componentsRoot, null);
        context.attachProxy(componentsRootProxy, componentsRoot, null);
        
//        SharedNode componentsRootNode = new SharedNode(componentsRootProxy, o);
        SharedNode root = componentsRootProxy.getTreeNode();
        
        SharedNodeTreeModel componentsTreeModel = new SharedNodeTreeModel(root,true);
        root.setTreeModel(componentsTreeModel);
        
        SharedNode newRoot = root;
        try {
            SharedNodeTreeModel model = (SharedNodeTreeModel) root.getTreeModel();
            DataProxy proxy = root.getProxy();
            
            Command createSpec = new CreateSpecificationCommand( root.getProxy(), "testspec", model );
            WorkflowEditor.getExecutor().executeCommand(createSpec).get();
            
            SharedNode specNode = (SharedNode) root.getChildAt( 0 );
            assert specNode.getProxy().getData() instanceof YSpecification : "invalid child of root";
            
            Command createNet = new CreateNetCommand( specNode.getProxy(), "testnet", model );
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
            YNet net = (YNet) netNode.getProxy().getData();
            YInputCondition start = (YInputCondition) net.getNetElement("start"); 
            YOutputCondition end = (YOutputCondition) net.getNetElement("end"); 
            YExternalNetElement jython = (YExternalNetElement) net.getNetElement("Jython");
            YExternalNetElement emailSender = (YExternalNetElement) net.getNetElement("EmailSender");
            Command createFlowCommand = new CreateFlowCommand(context.getDataProxy(start), context.getDataProxy(jython), null);
            WorkflowEditor.getExecutor().executeCommand(createFlowCommand).get();
            createFlowCommand = new CreateFlowCommand(context.getDataProxy(jython), context.getDataProxy(emailSender), null);
            WorkflowEditor.getExecutor().executeCommand(createFlowCommand).get();
            createFlowCommand = new CreateFlowCommand(context.getDataProxy(emailSender), context.getDataProxy(end), null);
            WorkflowEditor.getExecutor().executeCommand(createFlowCommand).get();
            newRoot = netNode;
        }
        catch( Exception e ) {
            e.printStackTrace( System.out );
        }
        YSpecification spec = (YSpecification) ((SharedNode) root.getChildAt( 0 )).getProxy().getData();
        return spec;
	}
	
}
