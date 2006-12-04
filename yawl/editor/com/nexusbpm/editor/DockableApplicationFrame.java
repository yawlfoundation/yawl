package com.nexusbpm.editor;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTextArea;
import javax.swing.JTree;

import org.flexdock.docking.Dockable;
import org.flexdock.docking.DockableFactory;
import org.flexdock.docking.DockingConstants;
import org.flexdock.docking.DockingManager;
import org.flexdock.docking.event.DockingEvent;
import org.flexdock.docking.event.DockingListener;
import org.flexdock.perspective.LayoutSequence;
import org.flexdock.perspective.Perspective;
import org.flexdock.perspective.PerspectiveFactory;
import org.flexdock.plaf.PlafManager;
import org.flexdock.view.View;

public class DockableApplicationFrame extends JFrame implements DockableFactory {
	public static final String PERSISTANCE_FILE = "NexusEditorPreferences.xml";
	public static final String PERSPECTIVE_ID = "NexusEditorPerspective";

	protected static final Logger logger = Logger.getLogger(DockableApplicationFrame.class.getName());

	protected Map dockComps = new HashMap();
	protected Map dockables = new HashMap();
	public static final String DAO = "Memory";
	public static final String FILE_DAO = "File";
	public static final String REMOTE_DAO= "Remote";
	public static final String EDITOR = "Editor";
	public static final String LOGS = "Logs";
	public static final String PALETTE = "Palette";

	protected String views[] = { DAO, EDITOR, LOGS, PALETTE, FILE_DAO, REMOTE_DAO};

	/** Creates a new instance of Main */
	public DockableApplicationFrame(String title) {
		super(title);
//		setDefaultCloseOperation(EXIT_ON_CLOSE);
//		Viewport port = new Viewport();
//		DockingManager.setDockableFactory(this);
//		DockingManager.setMainDockingPort(this, PALETTE);
//		PerspectiveManager.setFactory(new MyPrespectiveFactory());
//		PerspectiveManager.getInstance().setCurrentPerspective(PERSPECTIVE_ID,
//				true);
//		PersistenceHandler persister = FilePersistenceHandler
//				.createDefault(PERSISTANCE_FILE);
//		PerspectiveManager.setPersistenceHandler(persister);
//		EffectsManager.setPreview(new GhostPreview());
//		try {
//			DockingManager.loadLayoutModel();
//		} catch (Exception e) {
//			logger.fine("Docking layout not loaded.");
//		}
//		//DockingManager.setAutoPersist(true);
//		port.setPreferredSize(new Dimension(640, 480));
//		getContentPane().add(port);
//		DockingManager.restoreLayout();
	}
	protected JCheckBoxMenuItem createViewMenuItem(String id) {
		Dockable d = getDockable(id);
		boolean shown = DockingManager.isDocked(d);
		JCheckBoxMenuItem mi = new JCheckBoxMenuItem(id, shown);
		mi.addActionListener(new ViewCheckMenuItemListener(d));
		d.addDockingListener(new ViewCheckDockingListener(mi));
		return mi;
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		DockableApplicationFrame m = new DockableApplicationFrame("ApplicationName");
		Preferences prefs = Preferences.userNodeForPackage(DockableApplicationFrame.class);
		int w = prefs.getInt("frame.width", 640);
		int h = prefs.getInt("frame.height", 480);
		m.setSize(w, h);
		m.setVisible(true);
	}

	public void saveSize() {
		Preferences prefs = Preferences.userNodeForPackage(DockableApplicationFrame.class);
		prefs.putInt("frame.width", getWidth());
		prefs.putInt("frame.height", getHeight());		
	}

	protected Component createComponent(String id) {
		Container comp = null;
		boolean closable = false;
		boolean pinnable = true;
		if (EDITOR.equals(id)) {
			comp = new JLabel("Editor");
		} else if (DAO.equals(id)) {
			comp = new JTree();
		} else if (PALETTE.equals(id)) {
			comp = new JMenuBar();
		} else if (LOGS.equals(id)) {
			comp = new JTextArea("Things are happening");
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

	public Component getDockableComponent(String dockableId) {
		Component comp = (Component) dockComps.get(dockableId);
		if (comp == null) {
			comp = createComponent(dockableId);
			if (comp != null)
				dockComps.put(dockableId, comp);
		}
		return comp;
	}

	public Dockable getDockable(String dockableId) {
		Dockable d = (Dockable) dockables.get(dockableId);
		if (d == null) {
			Component comp = getDockableComponent(dockableId);
			if (comp != null) {
				d = DockingManager.getDockable(comp);
				if (d != null) {
					dockables.put(dockableId, d);
				}
			}
		}
		if (dockableId.equals(PALETTE)) {
			d.getDockingProperties().setTerritoryBlocked(
					DockingConstants.CENTER_REGION, true);
		}
		return d;
	}

	public static class MyPerspectiveFactory implements PerspectiveFactory {
		public Perspective getPerspective(String persistentId) {
			Perspective perspective = null;
			if (PERSPECTIVE_ID.equals(persistentId)) {
				perspective = new Perspective(PERSPECTIVE_ID, "default");
				LayoutSequence seq = perspective.getInitialSequence(true);

				seq.add(EDITOR);
				seq.add(DAO, EDITOR, DockingConstants.WEST_REGION, 0.25f);
				seq.add(FILE_DAO, DAO, DockingConstants.SOUTH_REGION, 0.5f);
				seq.add(REMOTE_DAO, FILE_DAO, DockingConstants.CENTER_REGION, 1.0f);
				seq.add(LOGS, EDITOR, DockingConstants.SOUTH_REGION, .05f);
				seq.add(PALETTE, EDITOR, DockingConstants.EAST_REGION, 0.05f);
			}

			return perspective;
		}

	}

	private static class ThemeChanger implements ActionListener {
		/*String id;
		 public ThemeChanger(String id){
		 this.id = id;
		 }*/

		public void actionPerformed(ActionEvent e) {
			JMenuItem mi = (JMenuItem) e.getSource();
			PlafManager.setPreferredTheme(mi.getText());
		}
	}

	private static class ViewCheckMenuItemListener implements ActionListener {
		private Dockable d;

		public ViewCheckMenuItemListener(Dockable d) {
			this.d = d;
		}

		public void actionPerformed(ActionEvent e) {
			if (DockingManager.isDocked(d)) {
				DockingManager.undock(d);
			} else {
				DockingManager.display(d);
			}
		}

	}

	private static class ViewCheckDockingListener implements DockingListener {
		JCheckBoxMenuItem mi;

		public ViewCheckDockingListener(JCheckBoxMenuItem mi) {
			this.mi = mi;
		}

		public void dockingComplete(DockingEvent evt) {
			mi.setSelected(true);
		}

		public void dockingCanceled(DockingEvent evt) {
		}

		public void dragStarted(DockingEvent evt) {
		}

		public void dropStarted(DockingEvent evt) {
		}

		public void undockingComplete(DockingEvent evt) {
			mi.setSelected(false);
		}

		public void undockingStarted(DockingEvent evt) {
		}

	}

	private class MyWindowListener extends WindowAdapter {
		public void windowOpened(WindowEvent e) {
			DockingManager.setAutoPersist(true);
		}

		public void windowClosing(WindowEvent e) {
			saveSize();
		}

	}
}
