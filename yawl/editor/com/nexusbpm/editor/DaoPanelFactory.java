package com.nexusbpm.editor;

import java.io.File;

import javax.persistence.PersistenceException;
import javax.swing.JPanel;

import au.edu.qut.yawl.persistence.dao.DAO;
import au.edu.qut.yawl.persistence.dao.DAOFactory;
import au.edu.qut.yawl.persistence.dao.DatasourceRoot;
import au.edu.qut.yawl.persistence.dao.DAOFactory.PersistenceType;
import au.edu.qut.yawl.persistence.managed.DataContext;
import au.edu.qut.yawl.util.configuration.BootstrapConfiguration;

import com.nexusbpm.editor.persistence.EditorDataProxy;
import com.nexusbpm.editor.tree.STree;
import com.nexusbpm.editor.tree.SharedNode;
import com.nexusbpm.editor.tree.SharedNodeTreeModel;

public class DaoPanelFactory {

	public static JPanel getFileDaoPanel() throws PersistenceException {
		DAO filedao = DAOFactory.getDAO(PersistenceType.FILE);
		DataContext filedc = new DataContext(filedao, EditorDataProxy.class);

		File fileRootObject = new File(new File(".").getAbsoluteFile().toURI()
				.normalize());
		DatasourceRoot fileRoot = new DatasourceRoot(fileRootObject);
		EditorDataProxy filedp = (EditorDataProxy) filedc.createProxy(fileRoot,
				null);
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
		return new TreePanel(fileComponentListTree, true);
	}

	public static JPanel getMemoryDaoPanel() throws PersistenceException {
		DAO memdao = DAOFactory.getDAO(PersistenceType.MEMORY);
		DataContext memdc = new DataContext(memdao, EditorDataProxy.class);

		DatasourceRoot virtualRoot = new DatasourceRoot(
				"virtual://memory/home/");
		EditorDataProxy memdp = (EditorDataProxy) memdc.createProxy(
				virtualRoot, null);
		memdc.attachProxy(memdp, virtualRoot, null);

		SharedNode memRootNode = memdp.getTreeNode();

		SharedNodeTreeModel memTreeModel = new SharedNodeTreeModel(memRootNode);
		memRootNode.setTreeModel(memTreeModel);

		STree memoryComponentListTree = new STree(memTreeModel);
		memoryComponentListTree.setShowsRootHandles(false);
		memoryComponentListTree.setRootVisible(true);
		memoryComponentListTree.setRowHeight(26);

		/////////////////////////////////////////////////
		// create the top component pane (memory context)
		return new TreePanel(memoryComponentListTree, true);
	}

	public static JPanel getRemoteDaoPanel() throws PersistenceException {
		STree hibernateComponentListTree = null;
		TreePanel remoteDaoPanel;
		DAO hibernatedao = (DAO) BootstrapConfiguration.getInstance()
				.getApplicationContext().getBean("yawlEngineDao");
		DataContext hibdc = new DataContext(hibernatedao, EditorDataProxy.class);

		DatasourceRoot hibernateRoot = new DatasourceRoot("YawlEngine://home/");
		EditorDataProxy hibdp = (EditorDataProxy) hibdc.createProxy(
				hibernateRoot, null);
		hibdc.attachProxy(hibdp, hibernateRoot, null);
		SharedNode hibernateRootNode = hibdp.getTreeNode();

		SharedNodeTreeModel hibernateTreeModel = new SharedNodeTreeModel(
				hibernateRootNode);
		hibernateRootNode.setTreeModel(hibernateTreeModel);

		hibernateComponentListTree = new STree(hibernateTreeModel);
		hibernateComponentListTree.setShowsRootHandles(false);
		hibernateComponentListTree.setRootVisible(true);
		hibernateComponentListTree.setRowHeight(26);
		remoteDaoPanel = new TreePanel(hibernateComponentListTree, true);
		return remoteDaoPanel;
	}

}
