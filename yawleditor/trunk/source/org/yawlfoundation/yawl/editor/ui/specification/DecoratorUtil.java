/*
 * Copyright (c) 2004-2014 The YAWL Foundation. All rights reserved.
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

import org.yawlfoundation.yawl.editor.ui.elements.model.Decorator;
import org.yawlfoundation.yawl.editor.ui.elements.model.JoinDecorator;
import org.yawlfoundation.yawl.editor.ui.elements.model.SplitDecorator;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.ui.net.NetGraphModel;
import org.yawlfoundation.yawl.editor.ui.net.utilities.NetUtilities;
import org.yawlfoundation.yawl.elements.YTask;

/**
 * @author Michael Adams
 * @date 10/10/2014
 */
public class DecoratorUtil {

    public static void setTaskDecorators(YTask yTask, YAWLTask task, NetGraphModel model) {
        model.setJoinDecorator(task, engineToEditorJoin(yTask),
                JoinDecorator.getDefaultPosition());
        model.setSplitDecorator(task, engineToEditorSplit(yTask),
                SplitDecorator.getDefaultPosition());
    }


    public static int engineToEditorJoin(YTask engineTask) {
        switch (engineTask.getJoinType()) {
            case YTask._AND : return Decorator.AND_TYPE;
            case YTask._OR  : return Decorator.OR_TYPE;
            case YTask._XOR : return Decorator.XOR_TYPE;
        }
        return Decorator.XOR_TYPE;
    }


    public static int engineToEditorSplit(YTask engineTask) {
        switch (engineTask.getSplitType()) {
            case YTask._AND : return Decorator.AND_TYPE;
            case YTask._OR  : return Decorator.OR_TYPE;
            case YTask._XOR : return Decorator.XOR_TYPE;
        }
        return Decorator.AND_TYPE;
    }


    public static void removeUnnecessaryDecorators(NetGraphModel model) {
        for (YAWLTask task : NetUtilities.getAllTasks(model)) {
            if (task.hasJoinDecorator() && task.getIncomingFlowCount() < 2) {
                model.setJoinDecorator(task,
                        JoinDecorator.NO_TYPE, JoinDecorator.NOWHERE);
            }
            if (task.hasSplitDecorator() && task.getOutgoingFlowCount() < 2) {
                model.setSplitDecorator(task,
                        SplitDecorator.NO_TYPE, SplitDecorator.NOWHERE);
            }
        }
    }

}
