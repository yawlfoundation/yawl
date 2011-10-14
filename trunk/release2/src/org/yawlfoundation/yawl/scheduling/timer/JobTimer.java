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
import org.jdom.Document;
import org.jdom.Element;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.yawlfoundation.yawl.scheduling.Constants;
import org.yawlfoundation.yawl.scheduling.SchedulingService;
import org.yawlfoundation.yawl.scheduling.util.PropertyReader;
import org.yawlfoundation.yawl.scheduling.util.Utils;
import org.yawlfoundation.yawl.scheduling.util.XMLUtils;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class JobTimer implements Constants {

	private static final Logger _log = Logger.getLogger(JobTimer.class);

	private static Scheduler _scheduler;
    private static final ScheduledExecutorService _executor =
           Executors.newScheduledThreadPool(1);

    private static final PropertyReader _props = PropertyReader.getInstance();

	public static void initialize()	{
		if (_scheduler == null)	{
            try	{
                SchedulerFactory schedulerFactory = new StdSchedulerFactory();
                _scheduler = schedulerFactory.getScheduler();
                _scheduler.start();
                startJobRUPCheck();

                if (_props.getBooleanProperty(PropertyReader.SCHEDULING, "createTestRUPs4Today"))
                {
                    startJobCreateTestRUPs4Today();
                }
            }
            catch (Exception e) {
                _log.error("cannot instantiate timer", e);
            }
        }
	}


    public static void shutdown() {
        _executor.shutdown();
    }

    private static void startJobRUPCheck2() throws IOException {
        long interval = _props.getLongProperty(PropertyReader.SCHEDULING, "RUPCheck.Interval");
        _executor.scheduleAtFixedRate(new Runnable() {
            public void run() {
                try {
                     SchedulingService.getInstance().updateRunningRups("RUPCheck");
                 }
                 catch (Exception e) {
                     Logger.getLogger(JobRUPCheck.class).error(
                             "Cannot execute job RUPCheck", e);
                 }
            }
        }, 0, interval, TimeUnit.MILLISECONDS);
    }

	private static void startJobRUPCheck() throws SchedulerException, IOException {
		initialize();
		String group = "RUPCheck", name = "RUPCheck";

		// Initiate JobDetail with job name, job group, and executable job class
		JobDetail jobDetail = new JobDetail(name, group, JobRUPCheck.class);
		SimpleTrigger trigger = new SimpleTrigger(name, group);

		// set its start up time
		trigger.setStartTime(new Date());

		// set the interval in ms, how often the job should run
		// trigger.setRepeatInterval(clazz.getIntervall());
		Long intervall = _props.getLongProperty(PropertyReader.SCHEDULING, name + ".Intervall");
		trigger.setRepeatInterval(intervall);

		// set the number of execution of this job. It will run x time and
		// exhaust.
		trigger.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);

		if (_scheduler.deleteJob(name, group))	{
			_log.debug("RUPCheck job deleted");
		}

		_scheduler.scheduleJob(jobDetail, trigger);
		_log.info("job " + name + " is scheduled");
	}

	public static void startJobMsgTransfer(String caseId, Document rup)
            throws SchedulerException {
		initialize();
		String group = "MsgTransfer-" + caseId;

		// delete all message transfer jobs for case
		int i = 0;
		while (_scheduler.deleteJob(String.valueOf(i++), group));

		_log.debug(i + " message transfer jobs deleted for case " + caseId);

		String xpath = XMLUtils.getXPATH_ActivityElement(null, XML_MSGTRANSFER, null);
		List<Element> msgTransfers = XMLUtils.getXMLObjects(rup, xpath);
		i = 0;
		for (Element msgTransfer : msgTransfers) {
			try	{
				_log.debug(Utils.element2String(msgTransfer, false));
				Element activity = msgTransfer.getParentElement();
				String name = String.valueOf(i);

				// Initiate JobDetail with job name, job group, and executable job class
				JobDetail jobDetail = new JobDetail(name, group, JobMsgTransfer.class);
				JobDataMap jobDataMap = new JobDataMap();
				jobDataMap.put(XML_MSGTO, msgTransfer.getChildText(XML_MSGTO));
				jobDataMap.put(XML_MSGBODY, msgTransfer.getChildText(XML_MSGBODY));
				jobDataMap.put(XML_CASEID, caseId);
				jobDetail.setJobDataMap(jobDataMap);

				SimpleTrigger trigger = new SimpleTrigger(name, group);

				// set its start up time
				Calendar cal = Calendar.getInstance();
				String msgUtilisationType = msgTransfer.getChildText(XML_MSGUTILISATIONTYPE);
				if (UTILISATION_TYPE_BEGIN.equals(msgUtilisationType)) {
					cal.setTime(XMLUtils.getDateValue(activity.getChild(XML_FROM), true));
				}
				else {
					cal.setTime(XMLUtils.getDateValue(activity.getChild(XML_TO), true));
				}
				int min = XMLUtils.getDurationValueInMinutes(
                        msgTransfer.getChild(XML_MSGDURATION), true);
				if (MSGREL_BEFORE.equals(msgTransfer.getChildText(XML_MSGREL)))	{
					min = 0 - min;
				}
				cal.add(Calendar.MINUTE, min);
				trigger.setStartTime(cal.getTime());
				trigger.setRepeatInterval(1);
				trigger.setRepeatCount(0);

				_scheduler.scheduleJob(jobDetail, trigger);
				_log.debug("message transfer job " + name + " is scheduled at "
						+ Utils.date2String(cal.getTime(), Utils.DATETIME_PATTERN) +
                        " for case " + caseId);
				i++;
			}
			catch (Exception e)	{
				XMLUtils.addErrorValue(msgTransfer, true, "msgMsgError", e.getMessage());
			}
		}

		if (i == msgTransfers.size()) {
			_log.info(i + " message transfer jobs created for case " + caseId);
		}
		else {
			throw new SchedulerException(msgTransfers.size() - i + " messages failed");
		}
	}

	private static void startJobCreateTestRUPs4Today()
            throws SchedulerException, IOException 	{
		initialize();

		String group = "CreateTestRUPs4Today", name = "CreateTestRUPs4Today";

		// Initiate JobDetail with job name, job group, and executable job class
		JobDetail jobDetail = new JobDetail(name, group, JobCreateTestRUPs4Today.class);
		SimpleTrigger trigger = new SimpleTrigger(name, group);

		// set its start up time to today, 00:00.000
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		trigger.setStartTime(cal.getTime());

		// set the interval in ms, how often the job should run, to 24h
		trigger.setRepeatInterval(24 * 60 * 60 * 1000);

		// set the number of execution of this job. It will run x time and exhaust.
		trigger.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);

		if (_scheduler.deleteJob(name, group)) {
			_log.debug("CreateTestRUPs4Today job deleted");
		}

		_scheduler.scheduleJob(jobDetail, trigger);
		_log.info("job " + name + " is scheduled");
	}

}
