package org.yawlfoundation.yawl.resourcing.jsf.dynform;

import com.sun.rave.web.ui.component.PanelLayout;

/**
 * Author: Michael Adams
 * Creation Date: 16/03/2010
 */
public class FlatPanel extends PanelLayout {

    private static final int _defHeight = 2;

    public FlatPanel() {
        super();
    }

    public int getHeight() {
        return _defHeight;
    }

    public int getCentre() {
        return getHeight() / 2;
    }

}

