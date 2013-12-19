/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Created by Jingxin XU on 12/01/2010
 * 
 */

package org.yawlfoundation.yawl.configuration.net;

import org.yawlfoundation.yawl.configuration.ProcessConfigurationModel;
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
