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

package org.yawlfoundation.yawl.configuration.menu.action;

import org.yawlfoundation.yawl.configuration.net.NetConfiguration;
import org.yawlfoundation.yawl.configuration.net.NetConfigurationCache;
import org.yawlfoundation.yawl.editor.ui.actions.net.YAWLSelectedNetAction;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class CheckProcessCorrectness extends YAWLSelectedNetAction {

    {
        putValue(Action.SHORT_DESCRIPTION, "Check Configuration Correctness");
        putValue(Action.NAME, "Check Configuration Correctness");
        putValue(Action.LONG_DESCRIPTION, "Check Configuration Correctness");
        putValue(Action.SMALL_ICON, getPNGIcon("tick"));

    }

    private boolean selected = false;

    public void actionPerformed(ActionEvent event) {
        NetConfiguration netConfiguration = NetConfigurationCache.getInstance()
                .get(getGraph().getNetModel());
        selected = !selected;
        if (selected) {
            netConfiguration.createServiceAutonomous();
        }
        else {
            netConfiguration.setServiceAutonomous(null);
        }
    }


}
