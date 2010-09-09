/*
 * Copyright (c) 2004-2010 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.resourcing.util;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.engine.interfce.TaskInformation;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.resourcing.ResourceManager;
import org.yawlfoundation.yawl.resourcing.codelets.AbstractCodelet;
import org.yawlfoundation.yawl.resourcing.codelets.CodeletExecutionException;
import org.yawlfoundation.yawl.resourcing.codelets.CodeletFactory;

import java.text.MessageFormat;
import java.util.List;

/**
 * Executes a codelet in a separate thread, and announces its completion when done
 * @author Michael Adams
 * @date 3/09/2010
 */
public class CodeletRunner implements Runnable {

    private WorkItemRecord _wir;
    private String _codeletName;
    private TaskInformation _taskInfo;
    private AbstractCodelet _codelet;
    private boolean _init;
    private Logger _log = Logger.getLogger(this.getClass());

    public CodeletRunner(WorkItemRecord wir, TaskInformation taskInfo, boolean resume) {
        _wir = wir;
        _codeletName = wir.getCodelet();
        _taskInfo = taskInfo;
        _init = resume;
    }


    public void run() {
        Element result = null;

        try {

            // get params
            List<YParameter> inputs = _taskInfo.getParamSchema().getInputParams();
            List<YParameter> outputs = _taskInfo.getParamSchema().getOutputParams();

            // get class instance
            _codelet = CodeletFactory.getInstance(_codeletName);
            if (_codelet != null) {
                if (_init)
                    _codelet.init();
                else
                    _codelet.resume();

                result = _codelet.execute(_wir.getDataList(), inputs, outputs);
            }
        }
        catch (CodeletExecutionException e) {
            _log.error(MessageFormat.format("Exception executing codelet '{0}': {1}. " +
                    "Codelet could not be executed; default value returned for workitem '{2}'",
                    _codeletName, e.getMessage(), _wir.getID()));
        }

        // tell the RM we're done
        ResourceManager.getInstance().handleCodeletCompletion(_wir, result);
    }


    public void cancel() {
        if (_codelet != null) _codelet.cancel();
    }

    public void shutdown() {
        if (_codelet != null) _codelet.shutdown();
    }

    public boolean persist() {
        return (_codelet != null) && _codelet.getPersist() ; 
    }
}
