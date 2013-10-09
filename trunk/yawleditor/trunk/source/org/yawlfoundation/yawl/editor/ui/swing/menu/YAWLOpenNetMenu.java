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

package org.yawlfoundation.yawl.editor.ui.swing.menu;

import org.yawlfoundation.yawl.editor.ui.specification.pubsub.Publisher;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.SpecificationStateListener;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.SpecificationState;

import javax.swing.*;

abstract class YAWLOpenNetMenu extends JMenu implements SpecificationStateListener {

    public YAWLOpenNetMenu(String title, int keyEventCode) {
        super(title);
        setMnemonic(keyEventCode);
        buildInterface();
        Publisher.getInstance().subscribe(this);
    }


    public void specificationStateChange(SpecificationState state) {
        switch (state) {
            case NoNetsExist: setEnabled(false); break;
            case NetsExist: setEnabled(true); break;
        }
    }


    protected abstract void buildInterface();

}
