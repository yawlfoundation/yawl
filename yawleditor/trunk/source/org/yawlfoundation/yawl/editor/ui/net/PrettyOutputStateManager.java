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

package org.yawlfoundation.yawl.editor.ui.net;

public class PrettyOutputStateManager {
    private final NetGraph net;

    private double rememberedScale;
    private boolean rememberedGridVisibility;
    private boolean rememberedPortVisibility;
    private Object[] rememberedSelectionSet;

    public PrettyOutputStateManager(NetGraph net) {
        this.net = net;
    }

    public void makeGraphOutputReady() {
        rememberCurrentNetGraphState();
        makeStateOfNetGraphOutputFriendly();
    }

    private void rememberCurrentNetGraphState() {
        rememberedScale = net.getScale();
        rememberedGridVisibility = net.isGridVisible();
        rememberedPortVisibility = net.isPortsVisible();
        rememberedSelectionSet =
                net.getSelectionModel().getSelectionCells();
    }

    private void makeStateOfNetGraphOutputFriendly() {
        net.setScale(1);
        net.setGridVisible(false);
        net.setPortsVisible(false);
        net.getSelectionModel().clearSelection();
    }

    public void revertNetGraphToPreviousState() {
        net.setScale(rememberedScale);
        net.setGridVisible(rememberedGridVisibility);
        net.setPortsVisible(rememberedPortVisibility);
        net.getSelectionModel().addSelectionCells(rememberedSelectionSet);
    }
}
