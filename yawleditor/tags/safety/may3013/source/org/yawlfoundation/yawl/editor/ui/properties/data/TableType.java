package org.yawlfoundation.yawl.editor.ui.properties.data;

/**
* @author Michael Adams
* @date 10/08/12
*/
public enum TableType {

    Net {
        public VariableTableModel getModel() { return new NetVarTableModel(); }

        public int getPreferredWidth() { return 400; }

        public String getName() { return "Net"; }
    },

    TaskInput {
        public VariableTableModel getModel() { return new TaskInputVarTableModel(); }

        public int getPreferredWidth() { return 200; }

        public String getName() { return "Task Input"; }
    },

    TaskOutput {
        public VariableTableModel getModel() { return new TaskOutputVarTableModel(); }

        public int getPreferredWidth() { return 300; }

        public String getName() { return "Task Output"; }
    };


    public abstract VariableTableModel getModel();

    public abstract int getPreferredWidth();

    public abstract String getName();
}
