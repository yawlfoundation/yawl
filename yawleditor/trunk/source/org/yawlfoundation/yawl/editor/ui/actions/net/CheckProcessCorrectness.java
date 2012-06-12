package org.yawlfoundation.yawl.editor.ui.actions.net;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class CheckProcessCorrectness extends YAWLSelectedNetAction{

	 {
		    putValue(Action.SHORT_DESCRIPTION, "Check Configuration Correctness");
		    putValue(Action.NAME, "Check Configuration Correctness");
		    putValue(Action.LONG_DESCRIPTION, "Check Configuration Correctness");
        putValue(Action.SMALL_ICON, getPNGIcon("tick"));

		  }
	 
	 private boolean selected = false;

	 public void actionPerformed(ActionEvent event) {
		 selected = !selected;
		 if (selected) {
			 getGraph().createServiceAutonomous();
		 }
     else {
			 getGraph().setServiceAutonomous(null);
		 }
	 }

	 
}
