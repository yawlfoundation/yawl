/*
 * Copyright (c) 2004-2011 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.scheduling.timer;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.yawlfoundation.yawl.scheduling.Constants;
import org.yawlfoundation.yawl.scheduling.SchedulingService;


/**
 * periodische OP-Plan-Aktualisierung: min�tliche �berpr�fung, die ergibt, dass
 * aktuelle utilisierung in andere reservierung hineinl�uft
 * 
 * @author tbe
 * @version $Id$
 *
 */
public class JobRUPCheck implements Constants, Job {

  /**
   * sets TO time of running activities to actual time
   */
	public void execute(JobExecutionContext context) {
        try {
			SchedulingService.getInstance().updateRunningRups(context.getTrigger().getName());
		}
        catch (Exception e) {
			Logger.getLogger(JobRUPCheck.class).error(
                    "Cannot execute job " + context.getTrigger().getName(), e);
		}
    }

}
