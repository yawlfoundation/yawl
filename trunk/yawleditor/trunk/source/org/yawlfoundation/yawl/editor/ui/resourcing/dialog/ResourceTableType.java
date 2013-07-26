package org.yawlfoundation.yawl.editor.ui.resourcing.dialog;

import org.yawlfoundation.yawl.editor.ui.resourcing.dialog.tablemodel.*;

import java.awt.*;

/**
* @author Michael Adams
* @date 10/08/12
*/
public enum ResourceTableType {

    Participant {
        public AbstractResourceTableModel getModel() {
            return new ParticipantTableModel();
        }

        public String getName() { return "Participants"; }
    },

    Role {
        public AbstractResourceTableModel getModel() {
            return new RoleTableModel();
        }

        public String getName() { return "Roles"; }
    },

    NetParam {
        public AbstractResourceTableModel getModel() {
            return new NetParamTableModel();
        }

        public String getName() { return "Net Parameters"; }
    },

    NonHumanResource {
        public AbstractResourceTableModel getModel() {
            return new NonHumanResourceTableModel();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(320, 230);
        }

        public String getName() { return "Non-human Resources"; }
    },

    NonHumanResourceCategory {
        public AbstractResourceTableModel getModel() {
            return new NonHumanResourceCategoryTableModel();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(320, 230);
        }

        public String getName() { return "Non-human Resource Categories"; }
    };



    private static int DEFAULT_WIDTH = 200;

    private static int DEFAULT_HEIGHT = 150;

    private static Dimension DEFAULT_SIZE = new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);


    public Dimension getPreferredSize() { return DEFAULT_SIZE; }


    public abstract AbstractResourceTableModel getModel();

    public abstract String getName();
}
