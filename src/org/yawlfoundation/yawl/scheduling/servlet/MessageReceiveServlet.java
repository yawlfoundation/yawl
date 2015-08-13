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

package org.yawlfoundation.yawl.scheduling.servlet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.yawlfoundation.yawl.resourcing.resource.nonhuman.NonHumanCategory;
import org.yawlfoundation.yawl.scheduling.ConfigManager;
import org.yawlfoundation.yawl.scheduling.Constants;
import org.yawlfoundation.yawl.scheduling.SchedulingException;
import org.yawlfoundation.yawl.scheduling.SchedulingService;
import org.yawlfoundation.yawl.scheduling.lanes.LaneImporter;
import org.yawlfoundation.yawl.scheduling.lanes.LaneProducer;
import org.yawlfoundation.yawl.scheduling.resource.ResourceServiceInterface;
import org.yawlfoundation.yawl.scheduling.timer.JobTimer;
import org.yawlfoundation.yawl.scheduling.util.Utils;
import org.yawlfoundation.yawl.scheduling.util.XMLUtils;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;


/**
 * receive messages about status change of resources from calendar manager
 * 
 * @author tbe
 * @version $Id: MessageReceiveServlet.java 24077 2010-11-22 13:17:30Z tbe $
 */
public class MessageReceiveServlet extends HttpServlet implements Constants {

	private static Logger _log = LogManager.getLogger(MessageReceiveServlet.class);


	/**
	 * service() - accept request and produce response
	 * 
	 * @param request The HTTP request
	 * @param response The HTTP response
	 */
	public void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
		try	{
			response.setCharacterEncoding("UTF-8");
			ServletOutputStream out = response.getOutputStream();

			if (request.getParameterMap().isEmpty()) {
				_log.warn("ignoring request without parameters");
				return;
			}
			_log.debug(Utils.getLogRequestParameters(request.getParameterMap()));

			if (request.getParameter("action") == null)	{
				response.setContentType("text/xml; charset=UTF-8");
				out.print(handleRSRequest(request));
			}
			else {
				response.setContentType("text/plain; charset=UTF-8");
				out.print(handleCUIRequest(request));
			}
		}
		catch (Exception e) {
			_log.error("cannot handle request", e);
		}
	}


	private String handleRSRequest(HttpServletRequest request) {
		String action = null;

		try	{
			if (! ResourceServiceInterface.getInstance().checkConnection(
                        request.getParameter("sessionHandle"))) {
				throw new SchedulingException("msgSessionTimeout");
			}

			if ((action = request.getParameter("StatusChange")) != null) {
				Element statusChange = Utils.string2Element(action);
				XMLUtils.validate(statusChange);

				String caseId = statusChange.getChildText(XML_CASEID);
				String activityName = statusChange.getChildText(XML_ACTIVITYNAME);
				Long reservationId = XMLUtils.getLongValue(
                        statusChange.getChild(XML_RESERVATIONID), true);
				String statusNew = statusChange.getChildText("NewStatus");

				SchedulingService.getInstance().reservationStatusChange(
                        caseId, activityName, reservationId, statusNew);
			}
			else {
				throw new IOException("missing action");
			}
			return "<success/>";
		}
		catch (SchedulingException e) {
			_log.warn("cannot handle action: " + action + ", " + e.getMessage());
			return "<failure>" + e.getMessage() + "</failure>";
		}
		catch (Exception e)	{
			_log.error("cannot handle action: " + action, e);
			return "<failure>" + e.getMessage() + "</failure>";
		}
	}

	/**
	 * Handles requests from the OR Schedule user interface
	 * 
	 * @param request
	 * @return
	 * @throws Exception
	 */
	private String handleCUIRequest(HttpServletRequest request) throws Exception {
		Map<String, Object> responseMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		String action = null;
        ResourceServiceInterface rs = ResourceServiceInterface.getInstance();
        ConfigManager config = ConfigManager.getFromRequest(request);

		try	{
			String userName = request.getParameter("userName");
			if (! (action.equals("login") ||
                    rs.isValidSession(userName, request.getParameter("sessionHandle")))) {
				throw new SchedulingException("msgSessionTimeout");
			}

            action = request.getParameter("action");
			if (action.equals("login"))	{
				responseMap.put("loginResponse", paramsMap);
				String password = request.getParameter("password");
				try	{
					paramsMap.put("sessionHandle", rs.getUserSessionHandle(userName, password));
				}
				catch (IOException e) {
					throw new SchedulingException("msgLoginFailed");
				}

				paramsMap.put("userName", rs.getFullNameForUserID(userName));
				paramsMap.put("isEditor", true);

				Map<String, String> map = rs.getRoleIdentifiers();
				for (NonHumanCategory nhr : rs.getNonHumanCategories()) {
					map.put(nhr.getID(), nhr.getName());
				}
				List<Map<String, String>> catsAndRoles = new ArrayList<Map<String, String>>();
				for (String id : map.keySet()) {
					Map<String, String> catOrRole = new HashMap<String, String>();
					catOrRole.put("id", id);
					catOrRole.put("name", map.get(id));
					catsAndRoles.add(catOrRole);
				}

				// sort catsAndRoles by title with OP-Saal at beginning
				Collections.sort(catsAndRoles, new Comparator<Map<String, String>>() {
					public int compare(Map<String, String> m1, Map<String, String> m2) {
						if (m1.get("name").equals("OP-Saal")) {
							return -1;
						}
						else if (m2.get("name").equals("OP-Saal")) {
							return 1;
						}
						else {
							return m1.get("name").compareTo(m2.get("name"));
						}
					}
				});

				paramsMap.put("groups", catsAndRoles);
			}
			else if (action.equals("lanes")) {
				responseMap.put("laneResponse", paramsMap);
				String categoryOrRoleId = request.getParameter("group");
				LaneProducer lp = new LaneProducer(categoryOrRoleId, config);
				paramsMap.put("dates", lp.getLanes(request.getParameterValues("dates[]")));
			}
			else if (action.equals("lastUpdate")) {
                SchedulingService ss = SchedulingService.getInstance();
				responseMap.put("lastUpdate", paramsMap);
				paramsMap.put("timestamp", ss.getLastSaveTime());
				paramsMap.put("message", ss.getLastSaveMsg());
			}
			else if (action.equals("processUnitUpdate")) {
				responseMap.put("processUnitUpdate", paramsMap);
				String caseId = request.getParameter("id");
				String start = request.getParameter("start");
				String categoryOrRoleId = request.getParameter("group");
				String resourceId = request.getParameter("lane");

				LaneImporter li = new LaneImporter(caseId, config,
                        request.getParameter("sessionHandle"));
				li.updateRUP(start, categoryOrRoleId, resourceId);

				paramsMap.put("reload", li.reload);
				paramsMap.put("conflict", li.hasErrors);
				paramsMap.put("conflictMessage", li.errors);
			}
			else {
				throw new IOException("msgActionFailed");
			}

			paramsMap.put("success", true);
		}
		catch (SchedulingException e) {
			_log.warn("cannot handle action: " + action + ", " + e.getMessage());
			paramsMap.clear();
			paramsMap.put("success", false);
			paramsMap.put("message", config.getLocalizedString(e.getMessage()));
		}
		catch (Exception e)	{
			_log.error("cannot handle action: " + action, e);
			paramsMap.clear();
			paramsMap.put("success", false);
			paramsMap.put("message", config.getLocalizedString("msgTechnicalError"));
		}

		return Utils.getJSON(responseMap);
	}


    public void destroy() {
        JobTimer.shutdown();
    }
}
