package org.yawlfoundation.yawl.editor.ui.resourcing.dialog;

import javax.swing.table.AbstractTableModel;
import java.awt.*;

/**
* @author Michael Adams
* @date 10/08/12
*/
public enum ResourceTableType {

    Participant {
        public AbstractTableModel getModel() {
            return new ParticipantTableModel();
        }

        public String getName() { return "Participants"; }
    },

    Role {
        public AbstractTableModel getModel() {
            return new RoleTableModel();
        }

        public String getName() { return "Roles"; }
    },

    NetParam {
        public AbstractTableModel getModel() {
            return new NetParamTableModel();
        }

        public String getName() { return "Net Parameters"; }
    },

    Filter {
        public AbstractTableModel getModel() {
            return new FilterTableModel();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(DEFAULT_WIDTH, 100);
        }

        public String getName() { return "Filters"; }
    };


    private static int DEFAULT_WIDTH = 200;

    private static int DEFAULT_HEIGHT = 150;

    private static Dimension DEFAULT_SIZE = new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);


    public Dimension getPreferredSize() { return DEFAULT_SIZE; }


    public abstract AbstractTableModel getModel();

    public abstract String getName();
}
