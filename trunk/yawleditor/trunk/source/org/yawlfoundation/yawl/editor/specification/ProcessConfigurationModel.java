
package org.yawlfoundation.yawl.editor.specification;

import java.util.LinkedList;


public class ProcessConfigurationModel {

    public enum PreviewState { OFF, ON, AUTO }

    public enum ApplyState { OFF, ON }

    private PreviewState previewState;
    private ApplyState applyState;

    private LinkedList<ProcessConfigurationModelListener> subscribers =
            new LinkedList<ProcessConfigurationModelListener>();

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

    public PreviewState getPreviewState() { return previewState; }

    public ApplyState getApplyState() { return applyState; }
    

    private void publishState() {
        for (ProcessConfigurationModelListener subscriber : subscribers) {
            subscriber.processConfigurationModelStateChanged(previewState, applyState);
        }
    }

}