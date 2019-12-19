/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.scheduling;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.yawlfoundation.yawl.scheduling.persistence.DataMapper;
import org.yawlfoundation.yawl.scheduling.resource.ResourceServiceInterface;
import org.yawlfoundation.yawl.scheduling.util.PropertyReader;
import org.yawlfoundation.yawl.scheduling.util.Utils;
import org.yawlfoundation.yawl.scheduling.util.XMLUtils;

import javax.xml.datatype.Duration;
import java.util.*;


/**
 * (re)schedules RUPs automatically
 * 
 * @author tbe
 * @version $Id$
 */
public class Scheduler implements Constants
{
	private static Logger logger = LogManager.getLogger(Scheduler.class);

	private DataMapper dataMapper;
	private ResourceServiceInterface rs;
    private final boolean debug = true;

    public Scheduler() {
        dataMapper = new DataMapper();
        rs = ResourceServiceInterface.getInstance();
    }


	/**
	 * set times of activities if FROM and DURATION are given Rescheduling
	 * kollidierender RUPs, welche definiert sind als: alle RUPs welche die
	 * selben Ressourcen zur gleichen Zeit definieren wie der vorhergehend RUP.
	 * als selbe Ressourcen werden nur nicht austauschbare Ressourcen betrachtet,
	 * das sind Ressourcen mit Reservierung by Id (TODO@tbe: Reservierungen by
	 * Role oder Category k�nnten auch kollidieren, hier aber erstmal weglassen)
	 * TODO@tbe: Rescheduling von RUPs die Leerlaufzeiten haben, z.B. durch
	 * fr�heres ende einer Aktivit�t, erstmal nicht betrachten
	 * 
	 * @param rup
	 * @param activity
	 * @param withValidation
	 * @param rescheduleCollidingRUPs
	 * 
	 */
	public boolean setTimes(Document rup, Element activity, boolean withValidation,
                            boolean rescheduleCollidingRUPs, Duration defaultDuration) {
		String caseId = XMLUtils.getCaseId(rup);
		String activityName = activity.getChildText(XML_ACTIVITYNAME);
		boolean hasRescheduledRUPs = false;

		try	{
			// 1) reschedule rup
			setTimes(rup, activity, withValidation, new ArrayList<String>(), defaultDuration);

			// rescheduling colliding rups
			if (rescheduleCollidingRUPs) {
				List<Document> allCollidingRUPs = new ArrayList<Document>();
				List<String> allCollidingRUPCaseIds = new ArrayList<String>();
				allCollidingRUPCaseIds.add(caseId);

				// 2) collect all potential colliding rups and remove their
				// reservations
				List<Document> collidingRUPs = getCollidingRups(rup, allCollidingRUPCaseIds);
				while (!collidingRUPs.isEmpty()) {
					List<Document> newCollidingRUPs = new ArrayList<Document>();
					for (Document collidingRUP : collidingRUPs)	{
						String collCaseId = null;
						try	{
							collCaseId = XMLUtils.getCaseId(collidingRUP);
							if (allCollidingRUPCaseIds.contains(collCaseId)) continue;

							// remove all reservations in RS by saving rup without
							// reservations
							Map<String, List<Element>> collRes = rs.removeReservations(collidingRUP, null);
							try	{
								logger.debug("delete reservations from collidingRUP: " + collCaseId);
								collidingRUP = rs.saveReservations(collidingRUP, false, false);
							}
							finally	{
								rs.addReservations(collidingRUP, collRes);
							}

							allCollidingRUPs.add(collidingRUP);
							allCollidingRUPCaseIds.add(collCaseId);

							newCollidingRUPs.addAll(getCollidingRups(collidingRUP, allCollidingRUPCaseIds));
						}
						catch (Exception e)	{
							logger.error("cannot collect caseId: " +
                                    (collCaseId == null ? "null" : collCaseId), e);
							XMLUtils.addErrorValue(rup.getRootElement(), withValidation,
                                    "msgRescheduleError", activityName, e.getMessage());
						}
					}
					collidingRUPs = newCollidingRUPs;
				}

				if (!allCollidingRUPs.isEmpty()) {
					// 3) save rup with new times, should not conflicting with other
					// rups
					// logger.debug("----------------wait 30s and save: "+caseId);
					// Thread.sleep(30000); // FIXME@tbe: raus
					Set<String> errors = SchedulingService.getInstance().optimizeAndSaveRup(
                            rup, "reschedulingRUP", null, false);
					logger.debug("----------------save rescheduled rup caseId: " + caseId + ", errors: "
							+ Utils.toString(errors));
				}

				// 4) sort colliding rups
				Collections.sort(allCollidingRUPs, new Comparator<Document>() {
					public int compare(Document rup1, Document rup2) {
						Date earlFrom1 = XMLUtils.getEarliestBeginDate(rup1);
						Date earlFrom2 = XMLUtils.getEarliestBeginDate(rup2);
						long timeGap = earlFrom1.getTime() - earlFrom2.getTime();
						if (timeGap > 0) return 1;
						else if (timeGap < 0) return -1;
						else return 0;
					}
				});

				// 5) find new time slot for each colliding rup and save it
				for (Document collidingRUP : allCollidingRUPs) {
					String collCaseId = null;
					try	{
						collCaseId = XMLUtils.getCaseId(collidingRUP);
						if (collCaseId.equals(caseId)) continue;

						// TODO@tbe: so ist nur das suchen eines sp�teren timeslots
						// m�glich, es sollte auch
						// ein fr�herer m�glich sein, falls RUP nach vorne verschoben
						// wurde, dazu evntl.
						// from auf 00:00 Uhr setzen, dann findTimeSlot aufrufen
						hasRescheduledRUPs = findTimeSlot(collidingRUP, true) || hasRescheduledRUPs;

                        SchedulingService.getInstance().optimizeAndSaveRup(collidingRUP,
                                "reschedulingCollidingRUPs", null, false);
						logger.debug("save rescheduled colliding caseId: " + collCaseId + ", errors: "
								+ Utils.toString(XMLUtils.getErrors(collidingRUP.getRootElement())));
						logger.info("caseId: " + collCaseId + " successfully rescheduled");
					}
					catch (Exception e) {
						logger.error("cannot reschedule caseId: " +
                                (collCaseId == null ? "null" : collCaseId), e);
						XMLUtils.addErrorValue(rup.getRootElement(), withValidation,
                                "msgRescheduleError", activityName,	e.getMessage());
					}
				}
			}
		}
		catch (Exception e)	{
			logger.error("error during rescheduling caseId: " +
                    (caseId == null ? "null" : caseId), e);
			XMLUtils.addErrorValue(rup.getRootElement(), withValidation,
                    "msgRescheduleError", activityName,	e.getMessage());
		}

		return hasRescheduledRUPs;
	}

	/**
	 * set TO time of an activity depending on FROM and DURATION set FROM time of
	 * previous and following activities and recurses to set their TO times also
	 * 
	 * TODO@tbe: erstmal ohne Beachtung von Max und ohne Ausnutzung des
	 * �Gummibandes� zwischen Min und Max, d.h. es wird Min als fester Wert
	 * angenommen, ausserdem wird nur von einer Relation pro Aktivit�t
	 * ausgegangen
	 * 
	 * @param activity
	 */
	private void setTimes(Document rup, Element activity, boolean withValidation,
                          List<String> activityNamesProcessed, Duration defaultDuration) {
		String activityName = activity.getChildText(XML_ACTIVITYNAME);
		Element durationElem = activity.getChild(XML_DURATION);
		Duration duration = XMLUtils.getDurationValue(durationElem, withValidation);
		Element from = activity.getChild(XML_FROM);
		Date fromDate = XMLUtils.getDateValue(from, withValidation);
		Element to = activity.getChild(XML_TO);
		Date toDate = XMLUtils.getDateValue(to, withValidation); // can be null
		String requestType = activity.getChildText(XML_REQUESTTYPE);

		if (requestType.equals("EOU")) {    // calculate duration
			toDate = XMLUtils.getDateValue(to, withValidation);
			XMLUtils.setDurationValue(durationElem, toDate.getTime() - fromDate.getTime());
		}
		else if (fromDate == null) {
            if (toDate != null) {
		    	fromDate = new Date(toDate.getTime());
			    if (duration != null) {
				    duration.negate().addTo(fromDate);
				    XMLUtils.setDateValue(from, fromDate);
                }
			}
			else if (defaultDuration != null) {
				defaultDuration.negate().addTo(fromDate);
				XMLUtils.setDateValue(from, fromDate);
			}
		}
		else {
			toDate = new Date(fromDate.getTime());
			if (duration != null) {
				duration.addTo(toDate); // TODO@tbe: if very very long duration, to is
											// one hour to high
				XMLUtils.setDateValue(to, toDate);
			}
			else if (defaultDuration != null) {
				defaultDuration.addTo(toDate);
				XMLUtils.setDateValue(to, toDate);
			}
		}

		activityNamesProcessed.add(activityName);
		logger.debug(activityName + ", set from: " + from.getText() + ", to: " +
                to.getText() + ", duration: " + durationElem.getText());

		// set times of following activities
		String xpath = XMLUtils.getXPATH_ActivityElement(activityName, XML_UTILISATIONREL, null);
		List relations = XMLUtils.getXMLObjects(rup, xpath);
		for (Object o : relations) {
            Element relation = (Element) o;
			String otherActivityName = relation.getChildText(XML_OTHERACTIVITYNAME);
			if (activityNamesProcessed.contains(otherActivityName)) {
                continue; // activity has been processed already
			}

			Duration min = XMLUtils.getDurationValue(relation.getChild(XML_MIN), withValidation);
			List otherActivities = XMLUtils.getXMLObjects(rup, XMLUtils.getXPATH_Activities(otherActivityName));
			for (Object obj : otherActivities) {
                Element otherActivity = (Element) obj;
				Date thisDate;
				if (relation.getChildText(XML_THISUTILISATIONTYPE).equals(UTILISATION_TYPE_BEGIN)) {
					thisDate = fromDate;
				}
				else if (duration != null) {
					thisDate = toDate;
				}
				else {
					continue;
				}
				min.addTo(thisDate); // add time gap between activities

				if (relation.getChildText(XML_OTHERUTILISATIONTYPE).equals(UTILISATION_TYPE_END)) {
					Duration otherDuration = XMLUtils.getDurationValue(
                            otherActivity.getChild(XML_DURATION), withValidation);
					(otherDuration == null ? defaultDuration : otherDuration).negate().addTo(thisDate);
				}
				XMLUtils.setDateValue(otherActivity.getChild(XML_FROM), thisDate);

				setTimes(rup, otherActivity, withValidation, activityNamesProcessed, defaultDuration);
			}
		}

		// set time of previous activities if this activity is not started
		if (!requestType.equals("POU"))	{
			return;
		}
		xpath = XMLUtils.getXPATH_ActivityElement(null, XML_UTILISATIONREL, null);
		xpath += "[" + XML_OTHERACTIVITYNAME + "/text()='" + activityName + "']";
		relations = XMLUtils.getXMLObjects(rup, xpath);
		for (Object o : relations) {
            Element relation = (Element) o;
			Element otherActivity = relation.getParentElement();
			if (activityNamesProcessed.contains(otherActivity.getChildText(XML_ACTIVITYNAME))) {
				continue; // activity has been processed already
			}

			Duration min = XMLUtils.getDurationValue(relation.getChild(XML_MIN), withValidation);
			Date otherDate;
			if (relation.getChildText(XML_OTHERUTILISATIONTYPE).equals(UTILISATION_TYPE_BEGIN))	{
				fromDate = XMLUtils.getDateValue(from, withValidation);
				otherDate = fromDate;
			}
			else if (duration != null) {
				toDate = XMLUtils.getDateValue(to, withValidation);
				otherDate = toDate;
			}
			else {
				continue;
			}
			min.negate().addTo(otherDate);

			if (relation.getChildText(XML_THISUTILISATIONTYPE).equals(UTILISATION_TYPE_END)) {
				Duration otherDuration = XMLUtils.getDurationValue(otherActivity.getChild(XML_DURATION), withValidation);
				(otherDuration == null ? defaultDuration : otherDuration).negate().addTo(otherDate);
			}
			XMLUtils.setDateValue(otherActivity.getChild(XML_FROM), otherDate);

			setTimes(rup, otherActivity, withValidation, activityNamesProcessed, defaultDuration);
		}
	}

	/**
	 * find time slot for rup
	 * 
	 * @param rup
	 */
	public boolean findTimeSlot(Document rup, boolean withValidation) {
		String caseId = XMLUtils.getCaseId(rup);
		logger.debug("reschedule caseId " + caseId);
		boolean isRescheduledRUP = false;

		try	{
			// Map with activities and their list of possible offsets for adding to
			// FROM
			Map<String, List<List<Long>>> actOffsets = new HashMap<String, List<List<Long>>>();
			Date latestTO = null;

			// get availabilities for all resources of rup between from and to
			String xpath = XMLUtils.getXPATH_Activities();
			List<Element> activities = XMLUtils.getXMLObjects(rup, xpath);
			for (Element activity : activities)
			{
				String activityName = activity.getChildText(XML_ACTIVITYNAME);
				// logger.debug("activity name: " + activityName);
				// logger.debug("activity: " + Utils.object2String(activity));
				List<Element> reservations = activity.getChildren(XML_RESERVATION);
				if (reservations.isEmpty())
				{
					continue;
				}

				actOffsets.put(activityName, new ArrayList<List<Long>>());
				Date from = XMLUtils.getDateValue(activity.getChild(XML_FROM), withValidation);
				Date to = XMLUtils.getDateValue(activity.getChild(XML_TO), withValidation);

				// compute latest date to check for free timeslots
				// between earliest FROM date (of first activity) and
				// maxTimeslotPeriod hours later
				if (latestTO == null)
				{
					Calendar cal = Calendar.getInstance();
					/*
					 * latestTO = Utils.string2Date(Utils.date2String(to,
					 * Utils.DATE_PATTERN_XML), Utils.DATE_PATTERN_XML);
					 * cal.setTime(latestTO); cal.roll(Calendar.DAY_OF_MONTH, 1);
					 * latestTO = cal.getTime();
					 */
					Integer maxTimeslotPeriod =
                            PropertyReader.getInstance().getIntProperty(
                                    PropertyReader.SCHEDULING, "maxTimeslotPeriod");
					cal.setTime(from);
					cal.add(Calendar.HOUR_OF_DAY, maxTimeslotPeriod);
					// cal.roll(Calendar.HOUR_OF_DAY, maxTimeslotPeriod);
					latestTO = cal.getTime();
					// logger.debug("latestTO set to " + Utils.date2String(latestTO,
					// Utils.DATETIME_PATTERN));
				}

				long duration = to.getTime() - from.getTime();
				for (Element reservation : reservations)
				{
					Element resource = null;
					try
					{
						resource = reservation.getChild(XML_RESOURCE);
						int workload = XMLUtils.getIntegerValue(reservation.getChild(XML_WORKLOAD), withValidation);
						List<Element> timeslots = rs.getAvailabilities(resource, from, latestTO);
						for (int i = 0; i < timeslots.size(); i++)
						{
							Element timeslot = timeslots.get(i);
							Date tsFrom = XMLUtils.getDateValue(timeslot.getChild("start"), withValidation);
							Date tsTo = XMLUtils.getDateValue(timeslot.getChild("end"), withValidation);
							long tsDuration = tsTo.getTime() - tsFrom.getTime();
							long offsetMin = tsFrom.getTime() - from.getTime();
							long offsetMax = tsTo.getTime() - to.getTime();
							int tsAvailability = XMLUtils.getIntegerValue(timeslot.getChild("availability"), withValidation);
							int tsAvailabilityDiff = tsAvailability - workload;

							// try to pass activity into timeslot, if possible, add min
							// and max offset
							if (tsAvailabilityDiff >= 0 && duration <= tsDuration && offsetMin >= 0 && offsetMax >= 0)
							{
								List<Long> offset = new ArrayList<Long>();
								offset.add(offsetMin);
								offset.add(offsetMax);
								List<List<Long>> offsets = actOffsets.get(activityName);
								offsets.add(offset);
								// logger.debug("insert offset for "+activityName+", timeslot "+i+": "
								// + Utils.toString(offset));
								// TODO@tbe: ohne Speicherung des verbrauchten workloads
								// (nur relevant wenn gleiche Ressource mehrfach pro
								// Aktivit�t)
								// --> verbrauchte availabiliy (tsAvailabilityDiff) am
								// offset speichern und beachten
							}
						}
					}
					catch (Exception e)
					{
						logger.error("error get availability for resource:" + Utils.element2String(resource, true), e);
					}
				}
			}
			// logger.debug("offsets: " + Utils.toString(actOffsets));

			// get average of offsets of each activity
			List<List<Long>> offsetsAvg = new ArrayList<List<Long>>();
			for (String activityName : actOffsets.keySet())
			{
				List<List<Long>> offsets = actOffsets.get(activityName);
				if (offsetsAvg.isEmpty())
				{
					offsetsAvg.addAll(offsets);
				}
				else
				{
					xpath = XMLUtils.getXPATH_Activities(activityName);
					Element activity = (Element) XMLUtils.getXMLObjects(rup, xpath).get(0);
					Date from = XMLUtils.getDateValue(activity.getChild(XML_FROM), withValidation);
					Date to = XMLUtils.getDateValue(activity.getChild(XML_TO), withValidation);
					long duration = to.getTime() - from.getTime();

					List<List<Long>> offsetsAvgNew = new ArrayList<List<Long>>();
					for (List<Long> offset : offsets)
					{
						for (List<Long> offsetAvg : offsetsAvg)
						{
							Long offsetMin = Math.max(offset.get(0), offsetAvg.get(0));
							Long offsetMax = Math.min(offset.get(1), offsetAvg.get(1));
							long offsetDuration = offsetMax - offsetMin;
							if (offsetDuration >= duration)
							{
								List<Long> offsetAvgNew = new ArrayList<Long>();
								offsetAvgNew.add(offsetMin);
								offsetAvgNew.add(offsetMax);
								offsetsAvgNew.add(offsetAvgNew);
							}
						}
					}
					offsetsAvg = offsetsAvgNew;
				}
			}
			logger.debug("caseId: " + caseId + ", offsetsAvg: " + Utils.toString(offsetsAvg));

			// add min of first offset to each activity
			if (offsetsAvg.isEmpty())
			{
				logger.error("no timeslot available for case: " + caseId);
				XMLUtils.addErrorValue(rup.getRootElement(), withValidation, "msgTimeslotUnavailable");
				isRescheduledRUP = true;
			}
			else
			{
				long offsetAvgMin = Long.MAX_VALUE;
				for (List<Long> offsetAvg : offsetsAvg)
				{
					offsetAvgMin = Math.min(offsetAvgMin, offsetAvg.get(0));
				}
				logger.info("timeslot for case: " + caseId + " found, offset=" + offsetAvgMin + " ("
						+ Utils.date2String(new Date(offsetAvgMin), Utils.TIME_PATTERN, TimeZone.getTimeZone("GMT")) + " h)");
				if (offsetAvgMin > 0)
				{
					for (Element activity : activities)
					{
						String activityName = activity.getChildText(XML_ACTIVITYNAME);

						Element from = activity.getChild(XML_FROM);
						Date fromDate = XMLUtils.getDateValue(from, false);
						fromDate = new Date(fromDate.getTime() + offsetAvgMin);
						XMLUtils.setDateValue(from, fromDate);
						// logger.debug("set "+activityName+" from: " +
						// Utils.date2String(fromDate, Utils.DATETIME_PATTERN));

						Element to = activity.getChild(XML_TO);
						Date toDate = XMLUtils.getDateValue(to, false);
						toDate = new Date(toDate.getTime() + offsetAvgMin);
						XMLUtils.setDateValue(to, toDate);
						// logger.debug("set "+activityName+" to: " +
						// Utils.date2String(toDate, Utils.DATETIME_PATTERN));
						isRescheduledRUP = true;
					}
				}
			}
		}
		catch (Exception e)
		{
			logger.error("cannot found timeslot for case: " + caseId, e);
			XMLUtils.addErrorValue(rup.getRootElement(), withValidation, "msgTimeslotError", e.getMessage());
		}

		return isRescheduledRUP;
	}

	/**
	 * findet potentiell kollidierende RUPs, welche definiert sind als: alle RUPs
	 * welche die selben Ressourcen zur gleichen Zeit definieren wie der
	 * vorhergehend RUP. als selbe Ressourcen werden nur nicht austauschbare
	 * Ressourcen betrachtet, das sind Ressourcen mit Reservierung by Id
	 * (TODO@tbe: Reservierungen by Role oder Category k�nnten auch kollidieren,
	 * hier aber erstmal weglassen) TODO@tbe: nur Aktivit�ten beachten, welche
	 * Reservierungen haben
	 * 
	 * @param rup
	 * @return
	 */
	private List<Document> getCollidingRups(Document rup, List<String> excludedCaseIds) {

		// find earliest FROM and latest TO time of rup
		Date earliestBeginDate = XMLUtils.getEarliestBeginDate(rup);
		Date latestEndDate = XMLUtils.getLatestEndDate(rup);

        List<Case> collidingCases = dataMapper.getRupsByInterval(
                earliestBeginDate, latestEndDate, excludedCaseIds, true);
        List<Document> collidingRups = SchedulingService.getInstance().getRupList(collidingCases);

		if (debug) {
			List<String> caseIds = new ArrayList<String>();
			for (Document collidingRUP : collidingRups)	{
				caseIds.add(XMLUtils.getCaseId(collidingRUP));
			}
			logger.debug("found " + collidingRups.size() +
                    " potential colliding rups: " + Utils.getCSV(caseIds));
		}
		return collidingRups;
	}

}