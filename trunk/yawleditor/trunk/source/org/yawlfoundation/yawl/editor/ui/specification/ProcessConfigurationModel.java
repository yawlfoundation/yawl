
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

package org.yawlfoundation.yawl.editor.ui.specification;

import java.util.LinkedList;


public class ProcessConfigurationModel {

    public enum PreviewState { OFF, ON, AUTO }

    public enum ApplyState { OFF, ON }

    private PreviewState previewState;
    private ApplyState applyState;
    private final LinkedList<ProcessConfigurationModelListener> subscribers;

    private static ProcessConfigurationModel INSTANCE ;

    private ProcessConfigurationModel() {
        subscribers = new LinkedList<ProcessConfigurationModelListener>();
        previewState = PreviewState.OFF;
        applyState = ApplyState.OFF;
    }

    public static ProcessConfigurationModel getInstance() {
        if (INSTANCE == null) INSTANCE = new ProcessConfigurationModel();
        return INSTANCE;
    }


    public void subscribe(ProcessConfigurationModelListener subscriber) {
        subscribers.add(subscriber);
        subscriber.processConfigurationModelStateChanged(previewState, applyState);
    }


    public void setApplyState(ApplyState state) {
        if (applyState != state) {
            applyState = state;
            publishState();
        }
    }


    public void setPreviewState(PreviewState state) {
        if (previewState != state) {
            previewState = state;
            publishState();
        }
    }


    public void togglePreviewState() {
        if (previewState != PreviewState.OFF) {
            PreviewState temp = previewState;
            previewState = PreviewState.OFF;
            publishState();
            previewState = temp;
            publishState();
        }
    }


    public void reset() {
        if (previewState != PreviewState.AUTO) previewState = PreviewState.OFF;
        applyState = ApplyState.OFF;
        publishState();
    }


    public void refresh() {
        publishState();
    }

    public PreviewState getPreviewState() { return previewState; }

    public ApplyState getApplyState() { return applyState; }
    

    private void publishState() {
        for (ProcessConfigurationModelListener subscriber : subscribers) {
            subscriber.processConfigurationModelStateChanged(previewState, applyState);
        }
    }

}