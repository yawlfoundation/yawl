package org.yawlfoundation.yawl.editor.actions.element;

import org.yawlfoundation.yawl.editor.YAWLEditor;
import org.yawlfoundation.yawl.editor.actions.YAWLBaseAction;
import org.yawlfoundation.yawl.editor.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.net.NetGraph;
import org.yawlfoundation.yawl.editor.swing.TooltipTogglingWidget;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

public class CancellationRegionConfigurationAction extends YAWLBaseAction
        implements TooltipTogglingWidget {

	{
	    putValue(Action.SHORT_DESCRIPTION, getDisabledTooltipText());
	    putValue(Action.NAME, "Cancellation Region...");
	    putValue(Action.LONG_DESCRIPTION, "Configure the cancellation region for this task.");
      putValue(Action.SMALL_ICON, getPNGIcon("comment_delete"));
	    
	  }
	
	private YAWLTask task;
	private NetGraph net;
	
	
	public CancellationRegionConfigurationAction(YAWLTask task){
		this.task = task;
		this.net = this.getGraph();
	}
	
	public void actionPerformed(ActionEvent event) {  
	  	final YAWLTask task = this.task;
	 	  final NetGraph net = this.net;
		  java.awt.EventQueue.invokeLater(new Runnable() {
	        public void run() {
	            ConfigureCancellationSetJDialog dialog =
                      new ConfigureCancellationSetJDialog(new javax.swing.JFrame(), true, task, net);
	            dialog.setLocationRelativeTo(YAWLEditor.getInstance());
	            dialog.setPreferredSize(new Dimension(200, 100));
              dialog.setResizable(false);
	            dialog.setVisible(true);
	        }
	    });
}

	public String getDisabledTooltipText() {
		return null;
	}


	public String getEnabledTooltipText() {
		return "Configure the cancellation region for this task.";
	}
	

	
	private class ConfigureCancellationSetJDialog extends JDialog implements ActionListener {
		
		private YAWLTask task;
		private NetGraph net;
	    
	    /** Creates new form ConfigureCancellationSetJDialog */
	    public ConfigureCancellationSetJDialog(java.awt.Frame parent, boolean modal,
                                             YAWLTask task, NetGraph net) {
	    	super(parent, modal);
	    	this.task = task;
	    	this.net = net;  
	    	this.setTitle("");
	      initComponents();
	    }
	    
	    private void initComponents() {
          JRadioButton rbActivate = new JRadioButton("Activate");
          rbActivate.setMnemonic(KeyEvent.VK_A);
          rbActivate.setActionCommand("activate");
          rbActivate.addActionListener(this);
          rbActivate.setSelected(true);

          JRadioButton rbBlock = new JRadioButton("Block");
          rbBlock.setMnemonic(KeyEvent.VK_B);
          rbBlock.setActionCommand("block");
          rbBlock.addActionListener(this);

          ButtonGroup group = new ButtonGroup();
          group.add(rbActivate);
          group.add(rbBlock);

          JPanel panel = new JPanel(new GridLayout(0, 1));
          panel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
          panel.add(rbActivate);
          panel.add(rbBlock);

          getContentPane().setLayout(new BorderLayout());
          add(panel, BorderLayout.CENTER);
          setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
          pack();
      }

    public void actionPerformed(ActionEvent e) {
        task.setCancellationSetEnable(e.getActionCommand().equals("enable"));
    }

  }
	
}
