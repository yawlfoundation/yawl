

package org.yawlfoundation.yawl.editor.ui.specification;

public interface ProcessConfigurationModelListener {

    public abstract void processConfigurationModelStateChanged(
            ProcessConfigurationModel.PreviewState previewState,
            ProcessConfigurationModel.ApplyState applyState);    
}