package org.yawlfoundation.yawl.editor.actions.net;

import org.yawlfoundation.yawl.editor.net.NetGraph;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class CheckProcessCorrectness extends YAWLSelectedNetAction{
	 private NetGraph net;
	 {
		    putValue(Action.SHORT_DESCRIPTION, "Check Configuration Correctness");
		    putValue(Action.NAME, "Check Configuration Correctness");
		    putValue(Action.LONG_DESCRIPTION, "Check Configuration Correctness");
        putValue(Action.SMALL_ICON, getPNGIcon("tick"));

		  }
	 
	 private boolean selected = false;

	 public void actionPerformed(ActionEvent event) {
		 selected = !selected;
		 this.net = getGraph();
		 if(selected){
			 this.net.createServiceAutonomous();
//			 ServiceAutomatonTree tree = new ServiceAutomatonTree (this.net);
//			 tree.testGeneratingTree();
		 }else{
			 this.net.setServiceAutonomous(null);
		 }
	 }

	 
}
