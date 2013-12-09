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

package org.yawlfoundation.yawl.editor.ui.resourcing;

import org.yawlfoundation.yawl.editor.ui.resourcing.tablemodel.*;

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



    private static final int DEFAULT_WIDTH = 200;

    private static final int DEFAULT_HEIGHT = 150;

    private static final Dimension DEFAULT_SIZE = new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);


    public Dimension getPreferredSize() { return DEFAULT_SIZE; }


    public abstract AbstractResourceTableModel getModel();

    public abstract String getName();
}
