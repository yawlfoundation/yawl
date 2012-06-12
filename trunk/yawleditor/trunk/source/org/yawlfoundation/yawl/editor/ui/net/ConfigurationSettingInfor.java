/**
 * Created by Jingxin XU on 12/01/2010
 * 
 */

package org.yawlfoundation.yawl.editor.ui.net;

import org.yawlfoundation.yawl.editor.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.specification.ProcessConfigurationModel;

import java.util.prefs.Preferences;

public class ConfigurationSettingInfor {

  protected Preferences prefs = Preferences.userNodeForPackage(YAWLEditor.class);

	private boolean newElementsConfigurable;
	private boolean applyAutoGreyOut;
	private boolean allowBlockingInputPorts;
	private boolean allowChangingDefaultConfiguration;
	
	public ConfigurationSettingInfor(){
		newElementsConfigurable = prefs.getBoolean("ProcessConfigNewElementsConfigurable", false);
		applyAutoGreyOut = prefs.getBoolean("ProcessConfigAutoGrayout", false);
		allowBlockingInputPorts = prefs.getBoolean("ProcessConfigBlockingInputPorts", true);
		allowChangingDefaultConfiguration = prefs.getBoolean("ProcessConfigAllowDefaultConfigChanges", true);
    publishState(applyAutoGreyOut);
  }


	public boolean isNewElementsConfigurable() {
		return newElementsConfigurable;
	}

	public void setNewElementsConfigurable(boolean setting) {
		newElementsConfigurable = setting;
    prefs.putBoolean("ProcessConfigNewElementsConfigurable", setting);
	}


	public boolean isApplyAutoGreyOut() {
		return applyAutoGreyOut;
	}

    public void setApplyAutoGreyOut(boolean selected) {
        if (applyAutoGreyOut != selected) {
            applyAutoGreyOut = selected;
            prefs.putBoolean("ProcessConfigAutoGrayout", selected);
            publishState(selected);
        }
    }


	public boolean isAllowBlockingInputPorts() {
		return allowBlockingInputPorts;
	}

	public void setAllowBlockingInputPorts(boolean setting) {
		allowBlockingInputPorts = setting;
    prefs.putBoolean("ProcessConfigBlockingInputPorts", setting);
	}


	public boolean isAllowChangingDefaultConfiguration() {
		return allowChangingDefaultConfiguration;
	}

	public void setAllowChangingDefaultConfiguration(boolean setting) {
		allowChangingDefaultConfiguration = setting;
      prefs.putBoolean("ProcessConfigAllowDefaultConfigChanges", setting);
	}


    private void publishState(boolean selected) {
        // let the menus know
        ProcessConfigurationModel.PreviewState state = selected ?
                ProcessConfigurationModel.PreviewState.AUTO :
                ProcessConfigurationModel.PreviewState.OFF;
        ProcessConfigurationModel.getInstance().setPreviewState(state);
    }

	
}
