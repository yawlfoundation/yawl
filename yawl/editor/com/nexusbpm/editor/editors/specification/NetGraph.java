package com.nexusbpm.editor.editors.specification;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.util.Date;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgraph.JGraph;
import org.jgraph.event.GraphLayoutCacheEvent;
import org.jgraph.event.GraphLayoutCacheListener;
import org.jgraph.event.GraphModelEvent;
import org.jgraph.event.GraphModelListener;
import org.jgraph.graph.BasicMarqueeHandler;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphLayoutCache;
import org.jgraph.graph.GraphModel;

import au.edu.qut.yawl.elements.YNet;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.events.StateEvent;
import au.edu.qut.yawl.persistence.dao.DAO;
import au.edu.qut.yawl.persistence.dao.DAOFactory;
import au.edu.qut.yawl.persistence.dao.DelegatedMemoryDAO;
import au.edu.qut.yawl.persistence.dao.DAOFactory.PersistenceType;
import au.edu.qut.yawl.persistence.managed.DataContext;
import au.edu.qut.yawl.persistence.managed.DataProxy;
import au.edu.qut.yawl.util.SpringTestConfiguration;
import au.edu.qut.yawl.util.SpringTestConfiguration.Configuration;
import au.edu.qut.yawl.util.configuration.BootstrapConfiguration;

import com.nexusbpm.command.SaveSpecificationCommand;
import com.nexusbpm.editor.CommandBasedNexusSpecificationFactory;
import com.nexusbpm.editor.WorkflowEditor;
import com.nexusbpm.editor.configuration.NexusClientConfiguration;
import com.nexusbpm.editor.editors.specification.NetGraphLayoutCache.AnimationTimer;
import com.nexusbpm.editor.persistence.EditorDataProxy;
import com.nexusbpm.editor.util.AbstractPopulatedRunnable;

public class NetGraph extends JGraph implements GraphModelListener, GraphLayoutCacheListener, ActionListener{

	public void actionPerformed(ActionEvent aue) {
		Set<DefaultGraphCell> cells = ((AnimationTimer) aue.getSource()).getCells();
		for (DefaultGraphCell cell: (Set<DefaultGraphCell>) cells) {
			Rectangle2D rectangle = NetGraph.this.getCellBounds(cell);
			if (rectangle != null) {
				SwingUtilities.invokeLater(new AbstractPopulatedRunnable(rectangle) {
					public void run() {
						NetGraph.this.repaint(((Rectangle2D)objects[0]).getBounds());
					}
				});
			}
		}
	}

	public void graphChanged(GraphModelEvent e) {
		System.out.println(e.getSource());
		System.out.println("GC" + e.getChange().getAttributes());
	}

	public void graphLayoutCacheChanged(GraphLayoutCacheEvent e) {
		System.out.println(e.getSource());
		System.out.println("GLCC" + e.getChange().getAttributes());
	}

	private static final long serialVersionUID = 1L;
	private static final Log LOG = LogFactory.getLog(NetGraph.class);

	public NetGraph(GraphLayoutCache arg0) {
		super(arg0);
	}

	public NetGraph(GraphModel arg0, BasicMarqueeHandler arg1) {
		super(arg0, arg1);
	}

	public NetGraph(GraphModel arg0, GraphLayoutCache arg1, BasicMarqueeHandler arg2) {
		super(arg0, arg1, arg2);
	}

	public NetGraph(GraphModel arg0, GraphLayoutCache arg1) {
		super(arg0, arg1);
		thread.start();
	}

	public NetGraph(GraphModel arg0) {
		super(arg0);
	}
	
	public static void main(String[] args) {
//		YSpecification spec = MockNexusSpecification.getSpecification();
		SpringTestConfiguration.setupTestConfiguration(Configuration.NEXUS_CLIENT);
		SpringTestConfiguration.startNewTestTransaction();
		YSpecification spec = CommandBasedNexusSpecificationFactory.getInstance();
		DAOFactory factory = (DAOFactory) BootstrapConfiguration.getInstance().getApplicationContext().getBean("daoFactory");
		DataContext c = new DataContext(factory.getDAO(PersistenceType.MEMORY), EditorDataProxy.class);
		try {
			DataProxy proxy = c.createProxy(spec, null);
			c.attachProxy(proxy, spec, null);
			SaveSpecificationCommand s = new SaveSpecificationCommand(proxy);
			WorkflowEditor.getExecutor().executeCommand(s).get();

		} catch (Exception e) {	e.printStackTrace(); }
		YNet net = spec.getRootNet();
		EditorDataProxy<YNet> netProxy = (EditorDataProxy) c.getDataProxy(net);
		GraphModel model = new NetGraphModel();
		NetGraphLayoutCache cache = new NetGraphLayoutCache(netProxy, model, new NetViewFactory());

		NetGraph graph = new NetGraph(model, cache);
		cache.addGraphLayoutCacheListener(graph);
		cache.addListener(graph);
		model.addGraphModelListener(graph);
		cache.insertObjectsFrom(netProxy, cache);

		graph.setSize(800, 600);
		JFrame frame = new JFrame();
		JPanel p = new JPanel();
		p.setSize(800, 600);
		p.add(graph);
		frame.add(p);
		frame.pack();
		frame.setVisible(true);
		SpringTestConfiguration.rollbackTestTransaction();		

	}

	private Thread thread = new Thread() {
		@Override
		public void run() {
			while (true) {
				Object[] cells = (Object[]) NetGraph.this.getGraphLayoutCache().getCells(false, true, false, false);
				for (Object cell: cells) {
					EditorDataProxy proxy = (EditorDataProxy)((NetCell) cell).getUserObject();
					if (!proxy.getLabel().equals("start")) {
						
					((NetCell) cell).getAttributes().put(NetCell.LABEL, new Date().toString());
					if (proxy.getState() == StateEvent.ACTIVE) {
						proxy.setState(StateEvent.EXECUTING);
					} else {
						proxy.setState(StateEvent.ACTIVE);
					}
					}
				}
				try {Thread.sleep(1000);} catch (InterruptedException e) {return;}
			}
		}
	};
	
}

