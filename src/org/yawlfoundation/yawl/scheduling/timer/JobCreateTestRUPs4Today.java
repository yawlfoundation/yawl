/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.yawlfoundation.yawl.scheduling.Case;
import org.yawlfoundation.yawl.scheduling.Constants;
import org.yawlfoundation.yawl.scheduling.Scheduler;
import org.yawlfoundation.yawl.scheduling.SchedulingService;
import org.yawlfoundation.yawl.scheduling.persistence.DataMapper;
import org.yawlfoundation.yawl.scheduling.util.PropertyReader;
import org.yawlfoundation.yawl.scheduling.util.Utils;
import org.yawlfoundation.yawl.scheduling.util.XMLUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;


/**
 * Utility class, may be used for test and presentation purposes. 
 * Updates the schedule daily in that all time windows are shifted to sysdate.
 * 
 * @author tbe
 * @version $Id$
 *
 */
public class JobCreateTestRUPs4Today implements Runnable, Constants {
	private static final Logger logger = LogManager.getLogger(JobCreateTestRUPs4Today.class);
	
	private SchedulingService service;
	private Scheduler scheduler;
	private DataMapper dataMapper;
    private String context;

	public JobCreateTestRUPs4Today(String context) {
		service = SchedulingService.getInstance();
		scheduler = new Scheduler();
		dataMapper = new DataMapper();
        this.context = context;
  }

  /**
   * sets TO time of running activities to actual time
   */
	public void run() {
    try {
  		List<Case> cases = dataMapper.getAllRups();
  		for (Case cas : cases) {
  			Document rup = cas.getRUP();
  			String possibleActivitiesSorted = PropertyReader.getInstance()
                .getSchedulingProperty("possibleActivitiesSorted");
  			String[] possibleActivities = Utils.parseCSV(possibleActivitiesSorted).toArray(new String[0]);
  			Element earlFrom = XMLUtils.getEarliestBeginElement(rup, possibleActivities);
            if (earlFrom == null) continue;
  			Date earlFromDate = XMLUtils.getDateValue(earlFrom, false);
  			if (earlFromDate == null) continue;
  			
  			Calendar today = Calendar.getInstance();
  			today.setTime(new Date());
  			
  			Calendar cal = Calendar.getInstance();
  			cal.setTime(earlFromDate);
  			cal.set(Calendar.YEAR, today.get(Calendar.YEAR));
  			cal.set(Calendar.MONTH, today.get(Calendar.MONTH));
  			cal.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH));

  			XMLUtils.setDateValue(earlFrom, cal.getTime());
  			
  			scheduler.setTimes(rup, earlFrom.getParentElement(), false, false, null);
  			service.optimizeAndSaveRup(rup, context, null, false);
  		}
	}
    catch (Exception e) {
		logger.error("cannot execute job " + context, e);
	}
  }
  
	
}
