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
import org.yawlfoundation.yawl.scheduling.Constants;
import org.yawlfoundation.yawl.scheduling.SchedulingService;
import org.yawlfoundation.yawl.scheduling.util.PropertyReader;
import org.yawlfoundation.yawl.scheduling.util.Utils;
import org.yawlfoundation.yawl.scheduling.util.XMLUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


public class JobTimer implements Constants {

	private static final Logger _log = LogManager.getLogger(JobTimer.class);

    private static final ScheduledExecutorService _executor =
           Executors.newScheduledThreadPool(1);

    private static final PropertyReader _props = PropertyReader.getInstance();
    
    private static Map<String, Set<ScheduledFuture<?>>> _msgTransferJobs =
            new HashMap<String, Set<ScheduledFuture<?>>>();

    private static boolean _initialised;

	public static void initialize()	{
		if (! _initialised)	{
            try {
                startJobRUPCheck();
                if (_props.getBooleanProperty(
                        PropertyReader.SCHEDULING, "createTestRUPs4Today")) {
                    startTestRUPs4Today();
                }
            }
            catch (Exception e) {
                _log.error("Cannot instantiate timer", e);
            }
            _initialised = true;
        }
	}


    public static void shutdown() {
        _msgTransferJobs.clear();
        _executor.shutdown();
    }

	public static void startJobMsgTransfer(String caseId, Document rup)
            throws JobTimerException {

		// delete all existing message transfer jobs for case
		int i = removeTransferJobs(caseId);
		_log.debug(i + " message transfer jobs deleted for case " + caseId);

		String xpath = XMLUtils.getXPATH_ActivityElement(null, XML_MSGTRANSFER, null);
		List msgTransfers = XMLUtils.getXMLObjects(rup, xpath);
		int transferredCount = 0;
		for (Object o : msgTransfers) {
            transferredCount = scheduleMsgTransfer((Element) o, caseId, transferredCount);
		}

		if (transferredCount == msgTransfers.size()) {
			_log.info(transferredCount + " message transfer jobs created for case " + caseId);
		}
		else {
			throw new JobTimerException(msgTransfers.size() - transferredCount + " messages failed");
		}
	}


    /****************************************************************************/

    private static void startJobRUPCheck() throws IOException {
        long interval = _props.getLongProperty(PropertyReader.SCHEDULING, "RUPCheck.Interval");
        _executor.scheduleAtFixedRate(new Runnable() {
            public void run() {
                try {
                     SchedulingService.getInstance().updateRunningRups("RUPCheck");
                 }
                 catch (Exception e) {
                     _log.error("Cannot execute job RUPCheck", e);
                 }
            }
        }, 0, interval, TimeUnit.MILLISECONDS);
    }


	private static void startTestRUPs4Today() throws IOException {
        long interval = 24 * 60 * 60 * 1000;   // set the interval to 24 hours, in ms
        _executor.scheduleAtFixedRate(new JobCreateTestRUPs4Today("CreateTestRUPs4Today"),
                getNextMidnight() - System.currentTimeMillis(), interval, TimeUnit.MILLISECONDS);

        // and start one for today
        new JobCreateTestRUPs4Today("CreateTestRUPs4Today").run();
	}


    private static int scheduleMsgTransfer(final Element msgTransfer, 
                                           final String caseId, final int i) {
        try	{
            _log.debug(Utils.element2String(msgTransfer, false));

            final Calendar cal = calcMsgTransferStartTime(msgTransfer);

            final ScheduledFuture<?> msgTransferJob = _executor.schedule(
                    new JobMsgTransfer(i, msgTransfer, caseId, cal.getTime()),
                    cal.getTimeInMillis(), TimeUnit.MILLISECONDS);
            addTransferJob(caseId, msgTransferJob);

            _log.debug("message transfer job " + i + " is scheduled at "
                    + Utils.date2String(cal.getTime(), Utils.DATETIME_PATTERN) +
                    " for case " + caseId);

            return i + 1;
        }
        catch (Exception e)	{
            XMLUtils.addErrorValue(msgTransfer, true, "msgMsgError", e.getMessage());
            return i;
        }
    }


    private static Calendar calcMsgTransferStartTime(Element msgTransfer) {
        Calendar cal = Calendar.getInstance();
        String msgUtilisationType = msgTransfer.getChildText(XML_MSGUTILISATIONTYPE);
        Element activity = msgTransfer.getParentElement();
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
        return cal;
    }


    private static long getNextMidnight() {
      	Calendar cal = Calendar.getInstance();
      	cal.setTime(new Date());
        cal.roll(Calendar.DATE, true);               // add 1 day to today
      	cal.set(Calendar.HOUR_OF_DAY, 0);            // set time to midnight
      	cal.set(Calendar.MINUTE, 0);
      	cal.set(Calendar.SECOND, 0);
      	cal.set(Calendar.MILLISECOND, 0);
      	return cal.getTimeInMillis();
    }
    
    private static void addTransferJob(String caseID, ScheduledFuture<?> scheduledFuture) {
        Set<ScheduledFuture<?>> futureSet = _msgTransferJobs.get(caseID);
        if (futureSet == null) {
            futureSet = new HashSet<ScheduledFuture<?>>();
            _msgTransferJobs.put(caseID, futureSet);
        }
        futureSet.add(scheduledFuture);
    }
    
    
    private static int removeTransferJobs(String caseID) {
        int removed = 0;
        Set<ScheduledFuture<?>> futureSet = _msgTransferJobs.remove(caseID);
        if (futureSet != null) {
            removed = futureSet.size();
            for (ScheduledFuture<?> future : futureSet) {
                future.cancel(true);
            }
        }
        return removed;
    }

}
