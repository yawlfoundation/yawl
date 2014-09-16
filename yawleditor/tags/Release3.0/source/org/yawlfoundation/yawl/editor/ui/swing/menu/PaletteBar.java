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
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.SpecificationState;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.SpecificationStateListener;

import javax.swing.*;
import java.awt.*;

public class PaletteBar extends JPanel implements SpecificationStateListener {

    private Palette palette;


    public PaletteBar() {
        super();
        buildInterface();
    }

    protected void buildInterface() {
        setLayout(new GridLayout(1,1));
        palette = new Palette();
        add(palette);
        Publisher.getInstance().subscribe(this);
    }


    public void refresh() {
        repaint();
    }


    // The NetMarquee Handler overrides certain GUI behaviour at times. When it is done
    // it wants to reset to the GUI behaviour driven by the control palette. The easiest
    // way to do that is just re-selecting the current selected palette item.
    public void refreshSelected() {
        palette.setSelectedState(palette.getSelectedState());
    }

    public Palette.SelectionState getState() {
        return palette.getSelectedState();
    }

    public Palette getPalette() {
        return palette;
    }


    public void setEnabled(boolean enabled) {
        palette.setEnabled(enabled);
        super.setEnabled(enabled);
    }

    public void specificationStateChange(SpecificationState state) {
        switch(state) {
            case NoNetsExist: {
                palette.setSelectedState(Palette.SelectionState.MARQUEE);
                setEnabled(false);
                break;
            }
            case NoNetSelected: {
                setEnabled(false);
                break;
            }
            case NetSelected: {
                setEnabled(true);
                break;
            }
        }
    }
}



