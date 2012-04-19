

package org.yawlfoundation.yawl.editor.specification;

public interface ProcessConfigurationModelListener {

    public abstract void processConfigurationModelStateChanged(
            ProcessConfigurationModel.PreviewState previewState,
            ProcessConfigurationModel.ApplyState applyState);    
}