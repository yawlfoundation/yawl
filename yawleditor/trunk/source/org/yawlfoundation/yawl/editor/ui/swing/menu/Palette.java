/*
 * Created on 09/10/2003
 * YAWLEditor v1.0 
 *
 * @author Lindsay Bradford
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package org.yawlfoundation.yawl.editor.ui.swing.menu;


import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.Publisher;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.SpecificationState;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.SpecificationStateListener;

import javax.swing.*;
import java.awt.*;

public class Palette extends JPanel implements SpecificationStateListener {

    private static final ControlFlowPalette CONTROL_FLOW_PALETTE = new ControlFlowPalette();

    private static final Palette INSTANCE = new Palette();

    public static Palette getInstance() {
        return INSTANCE;
    }

    private Palette() {
        super();
        buildInterface();
    }

    protected void buildInterface() {
        setLayout(new GridLayout(1,1));
        add(CONTROL_FLOW_PALETTE);
        Publisher.getInstance().subscribe(this);
    }


    public void refresh() {
        repaint();
    }


    // The NetMarquee Handler overrides certain GUI behaviour at times. When it is done
    // it wants to reset to the GUI behaviour driven by the control palette. The easiest
    // way to do that is just re-selecting the current selected palette item.
    public void refreshSelected() {
        CONTROL_FLOW_PALETTE.setSelectedState(CONTROL_FLOW_PALETTE.getSelectedState());
    }

    public ControlFlowPalette.SelectionState getControlFlowPaletteState() {
        return CONTROL_FLOW_PALETTE.getSelectedState();
    }

    public ControlFlowPalette getControlFlowPalette() {
        return CONTROL_FLOW_PALETTE;
    }


    public void setEnabled(boolean enabled) {
        CONTROL_FLOW_PALETTE.setEnabled(enabled);
        super.setEnabled(enabled);
    }

    public void specificationStateChange(SpecificationState state) {
        switch(state) {
            case NoNetsExist: {
                CONTROL_FLOW_PALETTE.setSelectedState(
                        ControlFlowPalette.SelectionState.MARQUEE
                );
                setEnabled(false);
                YAWLEditor.setStatusBarText("Open or create a specification to begin.");
                break;
            }
            case NetsExist: {
                YAWLEditor.setStatusBarText("Select a net to continue editing it.");
                break;
            }
            case NoNetSelected: {
                YAWLEditor.setStatusBarText("Select a net to continue editing it.");
                setEnabled(false);
                break;
            }
            case NetSelected: {
                YAWLEditor.setStatusBarText(
                        "Use the palette toolbar to edit the selected net.");
                setEnabled(true);
                break;
            }
        }
    }
}



