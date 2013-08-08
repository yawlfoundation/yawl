/**
 * Created by Jingxin XU on 12/01/2010
 * 
 */

package org.yawlfoundation.yawl.editor.ui.net;

import org.yawlfoundation.yawl.editor.ui.specification.ProcessConfigurationModel;
import org.yawlfoundation.yawl.editor.ui.util.UserSettings;

public class ConfigurationSettingInfo {

	private boolean newElementsConfigurable;
	private boolean applyAutoGreyOut;
	private boolean allowBlockingInputPorts;
	private boolean allowChangingDefaultConfiguration;
	
	public ConfigurationSettingInfo() {
		newElementsConfigurable = UserSettings.getConfigurableNewElements();
		applyAutoGreyOut = UserSettings.getConfigurableAutoGreyout();
		allowBlockingInputPorts = UserSettings.getConfigurableBlockingInputPorts();
		allowChangingDefaultConfiguration = UserSettings.getConfigurableAllowDefaultChanges();
    publishState(applyAutoGreyOut);
  }


	public boolean isNewElementsConfigurable() {
		return newElementsConfigurable;
	}

	public void setNewElementsConfigurable(boolean setting) {
		newElementsConfigurable = setting;
        UserSettings.setConfigurableNewElements(setting);
	}


	public boolean isApplyAutoGreyOut() {
		return applyAutoGreyOut;
	}

    public void setApplyAutoGreyOut(boolean selected) {
        if (applyAutoGreyOut != selected) {
            applyAutoGreyOut = selected;
            UserSettings.setConfigurableAutoGreyout(selected);
            publishState(selected);
        }
    }


	public boolean isAllowBlockingInputPorts() {
		return allowBlockingInputPorts;
	}

	public void setAllowBlockingInputPorts(boolean setting) {
		allowBlockingInputPorts = setting;
        UserSettings.setConfigurableBlockingInputPorts(setting);
	}


	public boolean isAllowChangingDefaultConfiguration() {
		return allowChangingDefaultConfiguration;
	}

	public void setAllowChangingDefaultConfiguration(boolean setting) {
		allowChangingDefaultConfiguration = setting;
        UserSettings.setConfigurableAllowDefaultChanges(setting);
	}


    private void publishState(boolean selected) {
        // let the menus know
        ProcessConfigurationModel.PreviewState state = selected ?
                ProcessConfigurationModel.PreviewState.AUTO :
                ProcessConfigurationModel.PreviewState.OFF;
        ProcessConfigurationModel.getInstance().setPreviewState(state);
    }

	
}
