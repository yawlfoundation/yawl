package com.nexusbpm.editor.editors.specification;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Timer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.undo.UndoableEdit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.CellView;
import org.jgraph.graph.CellViewFactory;
import org.jgraph.graph.ConnectionSet;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphLayoutCache;
import org.jgraph.graph.GraphModel;
import org.jgraph.graph.ParentMap;
import org.jgraph.graph.Port;

import au.edu.qut.yawl.elements.YExternalNetElement;
import au.edu.qut.yawl.elements.YFlow;
import au.edu.qut.yawl.elements.YNet;
import au.edu.qut.yawl.persistence.managed.DataContext;
import au.edu.qut.yawl.persistence.managed.DataProxy;

import com.nexusbpm.editor.editors.net.GraphEditor;
import com.nexusbpm.editor.persistence.EditorDataProxy;
import com.nexusbpm.editor.persistence.YTaskEditorExtension;
import com.nexusbpm.editor.util.ActionEventSupport;

public class NetGraphLayoutCache extends GraphLayoutCache implements ActionListener, PropertyChangeListener {

	private static final long serialVersionUID = 1L;
	private static final Log LOG = LogFactory.getLog( GraphEditor.class );
	private static AnimationTimer timer;
	private ActionEventSupport support; 
	
	public void actionPerformed(ActionEvent e) {
		System.out.println("Timer tick!");
		support.fireEvent();
	}

	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(NetCell.ACTIVITY_STATE)) {
			System.out.println("RECEIVED AN ACTIVITY STATE CHANGE");
			timer.getCells().add((NetCell) evt.getSource());
			support.fireEvent();
		}
	}

	public NetGraphLayoutCache(EditorDataProxy<YNet> proxy, GraphModel arg0, CellViewFactory arg1, boolean arg2) {
		super(arg0, arg1, arg2);
		init(proxy);
	}

	public NetGraphLayoutCache(EditorDataProxy<YNet> proxy, GraphModel arg0, CellViewFactory arg1, CellView[] arg2, CellView[] arg3, boolean arg4) {
		super(arg0, arg1, arg2, arg3, arg4);
		init(proxy);
	}

	public NetGraphLayoutCache(EditorDataProxy<YNet> proxy, GraphModel arg0, CellViewFactory arg1) {
		super(arg0, arg1);
		init(proxy);
	}
	
	private final void init(EditorDataProxy<YNet> proxy) {
		timer = new AnimationTimer(this);
		support = new ActionEventSupport(timer);
		insertObjectsFrom(proxy, this);
		timer.start();
	}	

	@Override
	public void insert(Object[] roots, Map attributes, ConnectionSet cs,
			ParentMap pm, UndoableEdit[] e) {
		for (Object root: roots) {
			if (root instanceof NetCell) { 
				((NetCell) root).addPropertyChangeListener(this);
			}
		}
		super.insert(roots, attributes, cs, pm, e);
	}

	@Override
	public void remove(Object[] cells) {
		for (Object cell: cells) {
			if (cell instanceof NetCell) { 
				((NetCell) cell).removePropertyChangeListener(this);
			}
		}
		super.remove(cells);
	}
	
	//initialize graph of a net from scratch...
	public static void insertObjectsFrom(EditorDataProxy<YNet> netProxy, GraphLayoutCache into) {
		YNet net = netProxy.getData();
		DataContext context = netProxy.getContext();

		Hashtable<Object, Map> attributes = new Hashtable<Object, Map>();

		ConnectionSet cs = new ConnectionSet();
		List<DefaultMutableTreeNode> cells = new ArrayList<DefaultMutableTreeNode>();

		Iterator netChildIterator = net.getNetElements().iterator();
		while( netChildIterator.hasNext() ) {
			Object c = netChildIterator.next();
			EditorDataProxy proxy = (EditorDataProxy) context.getDataProxy(c);
			NetCell cell = new NetCell(proxy, new AttributeMap());
			attributes.put( cell, new AttributeMap() );
			NetPort port = new NetPort();
			attributes.put( port, new AttributeMap() );
			cells.add(cell);
			cell.add(port);
			if (c instanceof YExternalNetElement) {
				YTaskEditorExtension ext = new YTaskEditorExtension((YExternalNetElement) c);
				Rectangle2D rect = (Rectangle2D) ext.getBounds().clone();
				GraphConstants.setBounds(cell.getAttributes(), rect);
			}
		}
		netChildIterator = net.getNetElements().iterator();
		while( netChildIterator.hasNext() ) {
			YExternalNetElement component = (YExternalNetElement) netChildIterator.next();
			Iterator flowChildIterator = component.getPostsetFlows().iterator(); 
			while( flowChildIterator.hasNext()) {
				YFlow flow = (YFlow) flowChildIterator.next();
				EditorDataProxy edgeproxy = (EditorDataProxy) context.getDataProxy(flow);
				EditorDataProxy sourceproxy = (EditorDataProxy) context.getDataProxy(flow.getPriorElement());
				EditorDataProxy sinkproxy = (EditorDataProxy) context.getDataProxy(flow.getNextElement());
				NetEdge edge = new NetEdge(edgeproxy);
				cells.add( edge );
				Port sourcePort = null;
				Port sinkPort = null;
				for (DefaultMutableTreeNode cell: cells) {
					if (cell.getUserObject() != null && cell.getUserObject() == sourceproxy) {
						sourcePort = (Port) cell.getFirstChild();
					}
					if (cell.getUserObject() != null && cell.getUserObject() == sinkproxy) {
						sinkPort = (Port) cell.getFirstChild();
					}
					if (sinkPort != null && sourcePort != null) break;
				}
				cs.connect( edge, sourcePort, sinkPort );
			}
		}

		into.insert(cells.toArray(), attributes, cs, null);
	}
	
	class AnimationTimer extends Timer {
		public Set<DefaultGraphCell> cells = new HashSet<DefaultGraphCell>();
		public AnimationTimer(ActionListener listener) {
			super(0, listener);
			this.setRepeats(true);
			this.setDelay(1000);
		}
		public Set<DefaultGraphCell> getCells() {
			return cells;
		}
		@Override
		protected void fireActionPerformed(ActionEvent e) {
			if (cells.size() > 0) {
				super.fireActionPerformed(e);
			}
		}
		
	}

	public void addListener(ActionListener listener) {
		support.addListener(listener);
	}

	public void removeListener(ActionListener listener) {
		support.removeListener(listener);
	}

	
}
