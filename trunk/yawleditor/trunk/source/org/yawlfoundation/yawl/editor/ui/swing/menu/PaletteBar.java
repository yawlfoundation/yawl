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



