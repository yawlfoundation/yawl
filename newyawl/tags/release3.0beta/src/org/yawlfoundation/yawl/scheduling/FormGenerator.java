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

package org.yawlfoundation.yawl.scheduling;

import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.resourcing.rsInterface.ResourceGatewayException;
import org.yawlfoundation.yawl.scheduling.resource.ResourceServiceInterface;
import org.yawlfoundation.yawl.scheduling.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.datatype.DatatypeConfigurationException;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;


/**
 * shows custom form for configuring utilisation plan
 *
 * @author tbe
 * @version $Id: FormGenerator.java 30323 2011-05-17 10:50:07Z tbe $
 */
public class FormGenerator implements Constants {

    private static Logger _log = Logger.getLogger(FormGenerator.class);

    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private StringBuilder buffer = new StringBuilder(), bufferTop = new StringBuilder(),
            bufferBottom = new StringBuilder(), bufferBottomDebug = new StringBuilder();
    private SchedulingService ss;
    private Scheduler scheduler;
    private ResourceServiceInterface rs;
    private ConfigManager config;
    private PropertyReader _props;
    private String source;
    private Element wirElement;
    private String userName;
    private List<Element> activities;

    // used for adding new reservations/relations
    int count = 0;

    // show short form if used only for data input without reservation request to RS
    boolean isShortForm = false;
    boolean canConfirmReservations = false;
    boolean reschedulingRUPAfterEditInCustomForm;
    boolean wrapBeforeResource = true;

    public FormGenerator(HttpServletRequest request, HttpServletResponse response) {
        try {
            config = ConfigManager.getFromRequest(request);
            ss = SchedulingService.getInstance();
            scheduler = new Scheduler();
            rs = ResourceServiceInterface.getInstance();
            _props = PropertyReader.getInstance();
            this.request = request;
            this.response = response;
            session = request.getSession();
            reschedulingRUPAfterEditInCustomForm = _props.getBooleanProperty(
                 PropertyReader.SCHEDULING, "reschedulingRUPAfterEditInCustomForm");

        }
        catch (Throwable e) {
            handleError(e);
        }
    }

    public ConfigManager getConfig() { return config; }

    public String outForm() {
        try {
            outFormPriv();

            buffer.insert(0, "\r\n\r\n<script type=\"text/javascript\">\r\n" + bufferTop + "</script>");

            buffer.append("\r\n\r\n<script type=\"text/javascript\">");
            buffer.append("window.onload=function(){\r\n" + bufferBottom + "}");
            buffer.append("</script>");

            buffer.append("\r\n\r\n<!--\r\n" + bufferBottomDebug + "\r\n-->");

            return buffer.toString();
        } catch (Throwable e) {
            //_log.error("cannot create form", e);
            handleError(e);
            return null;
        }
    }

    private void outFormPriv() throws Exception {
        String reschedulingKey = request.getParameter("reschedulingKey");
        source = request.getParameter("source");
        String itemid = request.getParameter("workitem");
        String handle = request.getParameter("handle");
        String participantid = request.getParameter("participantid");

        try {
            Participant participant = rs.getParticipant(participantid);
            userName = participant.getFullName();
        }
        catch (Exception e) {
            _log.error("Cannot get username for participant id: " + participantid, e);
        }

        if (!rs.isValidUserSession(handle)) {
            _log.warn("User handle is invalid: " + handle + ", redirecting to start page...");
            redirect(source.substring(0, source.lastIndexOf("/")));
        }

        wirElement = Utils.string2Element(rs.getWorkItem(itemid, handle));

        String caseId = Utils.extractCaseId(wirElement.getChildText("caseid"));
        String taskId = wirElement.getChildText("taskid");
        String taskName = taskId.substring(0, taskId.lastIndexOf("_"));

        List<Element> children = wirElement.getChild("updateddata").getChildren();
        Element task = (children.isEmpty() ? null : children.get(0));
        if (task == null) {
            children = wirElement.getChild("data").getChildren();
            task = (children.isEmpty() ? null : children.get(0));
        }

        boolean complete = request.getParameter("Complete") != null;
        boolean completeForce = request.getParameter("CompleteForce") != null &&
                                !request.getParameter("CompleteForce").isEmpty();
        boolean save = reschedulingKey != null || request.getParameter("Save") != null;
        boolean confirmReservations = request.getParameter("ConfirmReservations") != null;
        boolean cancel = request.getParameter("Cancel") != null;
        boolean language = request.getParameter(LANGUAGE_ATTRIBUTE_NAME) != null &&
                !request.getParameter(LANGUAGE_ATTRIBUTE_NAME).isEmpty();
        isShortForm = Boolean.parseBoolean(task.getChildText("shortForm"));
        canConfirmReservations = Boolean.parseBoolean(task.getChildText("canConfirmReservations"));
        _log.debug("cancel,save,confirmReservations,complete,language,isShortForm,canConfirmReservations="
                + cancel + "," + save + "," + confirmReservations + "," + complete + "," + language + ","
                + isShortForm + "," + canConfirmReservations);

        if (cancel) {
            redirect();
        }
        else if (completeForce) {
            completeWorkitem(taskName, itemid, handle);
        }
        else if (complete || save || language || confirmReservations) {
            Case cas = ss.loadCase(caseId);
            Document rup = cas.getRUP();
            Document rupOriginal = new Document(Utils.string2Element(
                    Utils.element2String(rup.getRootElement(), false)));
            String msgDB = "customForm";
            activities = XMLUtils.getXMLObjects(rup, XMLUtils.getXPATH_Activities());

            updateRUPFromRequest(rup);

            // set times of rup if rescheduling required
            if (reschedulingRUPAfterEditInCustomForm && reschedulingKey != null &&
                    !reschedulingKey.isEmpty()) {
                String activityName = reschedulingKey.substring((XML_ACTIVITY + "_").length());
                activityName = activityName.substring(0, activityName.indexOf("_"));
                List<Element> activities = XMLUtils.getXMLObjects(rup, XMLUtils.getXPATH_Activities(activityName));
                if (!activities.isEmpty()) {
                    scheduler.setTimes(rup, activities.get(0), !isShortForm, false, null);
                }
            }

            // reschedule rup at complete of short custom form
            if (isShortForm && (complete || save || confirmReservations)) {
                scheduler.findTimeSlot(rup, !isShortForm);
            }

            // full xsd validation only, if explicit datatype validation has not found any errors
            if (!XMLUtils.hasErrors(rup.getRootElement())) {
                validateXSD(rup);
            }

            if (confirmReservations) {
                String xpath = XMLUtils.getXPATH_Activities();
                List<Element> activities = XMLUtils.getXMLObjects(rup, xpath);
                for (Element activity : activities) {
                    List<Element> reservations = activity.getChildren(XML_RESERVATION);
                    for (Element reservation : reservations) {
                        if (RESOURCE_STATUS_REQUESTED.equals(reservation.getChildText(XML_STATUSTOBE))) {
                            reservation.getChild(XML_STATUSTOBE).setText(RESOURCE_STATUS_RESERVED);
                        }
                    }
                }
            }

            try {
                ss.checkRelations(rup);
                rup = rs.saveReservations(rup, !complete && !confirmReservations, true);
            }
            catch (Exception e) {
                _log.error("Exception", e);
                XMLUtils.addErrorValue(rup.getRootElement(), !isShortForm, "msgRUPSaveRSError", e.getMessage());
            }


            boolean haveTosave = XMLUtils.different(rupOriginal.getRootElement(), rup.getRootElement());
            if (haveTosave && (complete || save || confirmReservations)) {
                try {
                    ss.saveRupToDatabase(caseId, userName, rup, msgDB);
                    haveTosave = false;
                }
                catch (Exception e) {
                    XMLUtils.addErrorValue(rup.getRootElement(), true, "msgRUPSaveDBError", e.getMessage());
                }
            } else {
                _log.debug("Don't have to save rup");
            }

            if (complete) {
                ss.startMessageTransfers(caseId, rup);
            }

            if (complete && !XMLUtils.hasErrors(rup.getRootElement())) {
                completeWorkitem(taskName, itemid, handle);
            }
            else {
                activities = XMLUtils.getXMLObjects(rup, XMLUtils.getXPATH_Activities());
                buffer.append(getForm(cas, haveTosave));

                // show javascript alert to confirm the completing of work item
                if (complete) {
                    bufferTop.append(" msgCompleteForce=\"")
                             .append(config.getLocalizedString("msgCompleteForce"))
                             .append("\";\r\n");
                    bufferBottom.append(" completeForce();\r\n");
                }
            }
        }
        else { // first call
            Case cas = ss.loadCase(caseId);
            Document rup = cas.getRUP();
            Document rupOriginal = new Document(Utils.string2Element(Utils.element2String(
                    rup.getRootElement(), false)));
            Set<String> addedActivityNames = new HashSet<String>();

            // extend RUP from model specification
            ss.extendRUPFromYAWLModel(rup, addedActivityNames);

            // extend RUP with variable from YAWL
            addedActivityNames.addAll(ss.getDiffActivityNames(rup.getRootElement(),
                    task.getChild(XML_RUP)));
            if (XMLUtils.mergeElements(rup.getRootElement(), task.getChild(XML_RUP))) {
                _log.info("extend rup for case " + caseId + " from YAWL variable: " +
                        Utils.document2String(rup, false));
            }

            // extend RUP with historical data
            ss.completeRupFromHistory(rup, addedActivityNames);

            boolean haveToSave = XMLUtils.different(rupOriginal.getRootElement(),
                    rup.getRootElement());
            validateAllElements(rup.getRootElement());
            activities = XMLUtils.getXMLObjects(rup, XMLUtils.getXPATH_Activities());
            buffer.append(getForm(cas, haveToSave));
        }
    }

    /**
     * @param taskName
     * @param itemid
     * @param handle
     * @throws IOException
     */
    private void completeWorkitem(String taskName, String itemid, String handle)
            throws IOException, ResourceGatewayException {
        Element taskUpd = new Element(taskName);
        String taskUpdStr = Utils.element2String(taskUpd, false);
        String result = rs.updateWorkItemData(itemid, taskUpdStr, handle);

        _log.debug("result: " + result);
        source += "?complete=true"; // workitem in CUI completen

        redirect();
    }

    /**
     * - remove empty 'dummy' resOrUtils
     * - set RequestType to POU, but only if SOU or EOU is not set
     */
    public void updateActivities(Document doc) throws JDOMException {
        activities = XMLUtils.getXMLObjects(doc, XMLUtils.getXPATH_Activities());
        for (Element activity : activities) {
            String xpath = XMLUtils.getXPATH_ActivityElement(
                    activity.getChildText(XML_ACTIVITYNAME),
                    "*[count(./" + XML_DUMMY + ")>0]", null);

            List<Element> resOrUtils = XMLUtils.getXMLObjects(doc, xpath);
            for (Element resOrUtil : resOrUtils) {
                activity.removeContent(resOrUtil);
            }

            Element requestType = activity.getChild(XML_REQUESTTYPE);
            if (requestType == null) {
                requestType = new Element(XML_REQUESTTYPE);
                activity.addContent(requestType);
            }
            if (requestType.getText().isEmpty()) {
                requestType.setText(UTILISATION_TYPE_PLAN);
            }
        }
    }

    /**
     * get ResourceUtilisationPlan updated with data from request
     *
     * @return
     */
    private void updateRUPFromRequest(Document doc) throws Exception {
        String xpath = XMLUtils.getXPATH_RUP();
        Element rupElement = XMLUtils.getElement(doc, xpath);

        // remove elements from rup for reinsert in next step
        for (Element activity : activities) {
            activity.removeChildren(Constants.XML_RESERVATION);
            activity.removeChildren(Constants.XML_UTILISATIONREL);
            activity.removeChildren(Constants.XML_MSGTRANSFER);
        }

        XMLUtils.removeAttributes(rupElement, XML_ERROR);
        XMLUtils.removeAttributes(rupElement, XML_WARNING);

        // build new parameter list with rup fields only
        Map<String, Object[]> params = new HashMap<String, Object[]>();
        for (String key : new ArrayList<String>(request.getParameterMap().keySet())) {
            if (!key.startsWith(XML_ACTIVITY + "_") || key.contains(XML_RESOURCE_TYPE) ||
                    key.endsWith("__sexyComboHidden")) {
                continue;
            }

            Object[] values = ((Object[]) request.getParameterMap().get(key));

            // for sexycombo plugin only: write value of key "bla__sexyCombo" to newKey "bla"
            if (key.endsWith("__sexyCombo")) {
                String newKey = key.substring(0, key.length() - "__sexyCombo".length());
                params.put(newKey, values);
            } else if (!params.containsKey(key)) {
                params.put(key, values);
            }
        }

        ArrayList<String> keys = new ArrayList<String>(params.keySet());
        final List<String> possibleActivities = Utils.parseCSV(
                _props.getSchedulingProperty("possibleActivitiesSorted"));
        Collections.sort(keys, new Comparator<String>() {
            public int compare(String a1, String a2) {
                if (a1.startsWith(XML_ACTIVITY)) {
                    a1 = a1.substring(XML_ACTIVITY.length() + 1);
                    a1 = a1.substring(0, a1.indexOf("_"));
                }
                if (a2.startsWith(XML_ACTIVITY)) {
                    a2 = a2.substring(XML_ACTIVITY.length() + 1);
                    a2 = a2.substring(0, a2.indexOf("_"));
                }
                return possibleActivities.indexOf(a1) - possibleActivities.indexOf(a2);
            }
        });

        // update RUP with input values
        for (String key : keys) {
            Object[] values = params.get(key);
            String value = (String) values[0];

            StringTokenizer st = new StringTokenizer(key, "_");
            st.nextToken(); // ignore 'Activity.'
            String activityName = st.nextToken();
            xpath = XMLUtils.getXPATH_Activities(activityName);
            Element activity = XMLUtils.getElement(doc, xpath);
            if (activity == null) {
                activity = getTemplate(XML_ACTIVITY);
                Element name = activity.getChild(XML_ACTIVITYNAME);
                name.setText(activityName);
                rupElement.addContent(activity);
            }

            Element var = null;
            String resOrUtilName = st.nextToken();
            if (resOrUtilName.equals(XML_RESERVATION) ||
                    resOrUtilName.equals(XML_UTILISATIONREL) ||
                    resOrUtilName.equals(XML_MSGTRANSFER)) {
                Integer resOrUtilIndex = Integer.valueOf(st.nextToken().substring(1)); // index, remove '#'
                xpath = XMLUtils.getXPATH_ActivityElement(activityName, resOrUtilName, resOrUtilIndex);

                Element resOrUtilElem = XMLUtils.getElement(doc, xpath);

                // insert empty 'dummy' resOrUtils, will be removed later
                while (resOrUtilElem == null) {
                    resOrUtilElem = getTemplate(resOrUtilName);
                    resOrUtilElem.addContent(new Element(XML_DUMMY));
                    activity.addContent(resOrUtilElem);

                    resOrUtilElem = XMLUtils.getElement(doc, xpath);
                }
                resOrUtilElem.removeChild(XML_DUMMY); // only if exists one

                String pathName = "";
                while (st.hasMoreTokens()) {
                    String elementName = st.nextToken();
                    pathName += elementName;
                    xpath = XMLUtils.getXPATH_ResOrUtilElement(activityName, resOrUtilName,
                            resOrUtilIndex, pathName);
                    var = XMLUtils.getElement(doc, xpath);
                    if (var == null) {
                        var = new Element(elementName);
                        resOrUtilElem.addContent(var);
                    }
                    resOrUtilElem = var;
                    pathName += "/";
                }

            } else {
                xpath = XMLUtils.getXPATH_ActivityElement(activityName, resOrUtilName, null);
                var = XMLUtils.getElement(doc, xpath);
                if (var == null) {
                    var = new Element(resOrUtilName);
                    activity.addContent(var);
                }
            }

            String cssClass = validateElement(var, new ArrayList<Element>());
            if (cssClass.equals(CSS_DATEINPUT)) {
                try {
                    Date date = Utils.string2Date(value, isShortForm ? Utils.DATE_PATTERN :
                            Utils.DATETIME_PATTERN);
                    XMLUtils.setDateValue(var, date);
                } catch (ParseException e) {
                    //_log.error("cannot parse into date" + value);
                }
            } else if (cssClass.equals(CSS_DURATIONINPUT)) {
                try {
                    value = Utils.stringMinutes2stringXMLDuration(value);
                } catch (Exception e) {
                    //_log.error("cannot parse into duration" + value);
                }
                XMLUtils.setStringValue(var, value);
            } else {
                XMLUtils.setStringValue(var, value);
            }

            validateElement(var, new ArrayList<Element>());
        }

        updateActivities(doc);
    }

    /**
     * validates element and their children recursively
     *
     * @param element
     * @return
     */
    private void validateAllElements(Element element)
            throws DatatypeConfigurationException, IOException, JDOMException {
        if (element != null) {
            validateElement(element, new ArrayList<Element>());
            for (Element child : (List<Element>) element.getChildren()) {
                validateAllElements(child);
            }
        }
    }

    /**
     * validates element and return css class and restrictions
     *
     * @param element
     * @param restrictions
     * @return
     */
    private String validateElement(Element element, List<Element> restrictions)
            throws DatatypeConfigurationException, IOException, JDOMException {
        String xpath = "//xs:element[@name='" + element.getName() + "']";
        for (Document schemaDoc : XMLUtils.getSchemaDocs()) {
            Element schemaElement = XMLUtils.getElement(schemaDoc, xpath);
            if (schemaElement != null) {
                return XMLUtils.validateElement(element, schemaElement, restrictions, !isShortForm);
            }
        }

        _log.error("cannot find element '" + element.getName() + "' in schema");
        return null;
    }

    /**
     * validates resource utilisation plan against syntax and data types of schema
     *
     * @param rup
     * @return
     */
    private void validateXSD(Document rup) {
        try {
            //validate against schema
            XMLUtils.validate(rup.getRootElement());
        } catch (Exception e) {
            _log.error("cannot validate rup", e);
            String msg = e.getMessage() == null ? "null" : e.getMessage().replaceAll("\"", "&quot;");
            XMLUtils.addErrorValue(rup.getRootElement(), !isShortForm, "msgRUPInvalid", msg);
        }
    }

    /**
     * parses the ResourceUtilisationPlan and
     * 1) generates HTML form for input of ResourceUtilisationPlan
     * 2) generates list with keys for Reservations and UtilisationRelations to check
     *
     * @param cas
     * @param haveTosave
     * @return
     */
    protected StringBuffer getForm(Case cas, boolean haveTosave) throws IOException, JDOMException {
        Document rup = cas.getRUP();
        bufferBottomDebug.append(Utils.element2String(rup.getRootElement(), true));

        StringBuffer buffer = new StringBuffer();

        bufferTop.append(" window.name = \"")
                 .append(config.getLocalizedString("titleSchedulingPage"))
                 .append("\";\r\n");

        buffer.append("\r\n\r\n<div style=\"display: none\">");
        buffer.append("<table>");
        buffer.append(getRow(getTemplate(XML_RESERVATION), XML_RESERVATION + "Template", false));
        buffer.append(getRow(getTemplate(XML_UTILISATIONREL), XML_UTILISATIONREL + "Template", false));
        buffer.append(getRow(getTemplate(XML_MSGTRANSFER), XML_MSGTRANSFER + "Template", false));
        buffer.append("</table>");
        buffer.append("</div>");

        // create form fields
        buffer.append("\r\n\r\n<form id=\"bla\" name=\"bla\" method=\"post\">");

        // @see CUI/default.jsp
        buffer.append("\r\n<div id=\"main_content\"><div>");
        //<%-- header image on the top right --%>
        buffer.append("<img src=\"images/logo_klinik.jpg\" alt=\"Krankenhaus\" style=\"float:right;margin-bottom:1em;\">");
        //<%-- the Perikles logo and language selection --%>
        buffer.append("<img src=\"images/logo-133x20.jpg\" alt=\"Perikles\">");

        // localisation
        for (int i = 0; i < LANGUAGES.length; i++) {
            String l = LANGUAGES[i];
            buffer.append(i > 0 ? "&nbsp;" : "");
            if (l.equals(config.getLanguage())) {
                buffer.append("<img src=\"images/")
                        .append(l)
                        .append("-32-16.gif\" style=\"margin:1px 0 0 6px;\"");
            } else {
                buffer.append("<input type=\"image\" src=\"images/")
                        .append(l)
                        .append("-32-16.gif\" style=\"margin:1px 0 0 10px;\" name=\"LocalePic\" id=\"LocalePic\" value=\"")
                        .append(l)
                        .append("\"");
                buffer.append(" onclick=\" document.getElementById('" + LANGUAGE_ATTRIBUTE_NAME + "').value=this.value; return saveTab();\"");
                buffer.append(" onkeyup=\" document.getElementById('" + LANGUAGE_ATTRIBUTE_NAME + "').value=this.value; return saveTab();\"");
            }
            buffer.append("/>");
        }

        buffer.append("<div id=\"userPanel\"><span class=\"ui-widget-header selected\">");
        buffer.append("<span>" + userName + "</span>");
        buffer.append(config.getLocalizedString("titleSchedulingPage"));
        buffer.append("</span></div>");
        buffer.append("<div class=\"clear\"></div>");

        // general warnings and errors
        if (XMLUtils.hasWarnings(rup.getRootElement())) {
            buffer.append("<div class=\"warningInput\"><span class=\"ui-corner-all\">");
            buffer.append(config.getLocalizedString("msgRUPWarning"));

            String warning = config.getLocalizedJSONString(XMLUtils.getWarningValue(rup.getRootElement()));
            if (warning != null) {
                buffer.append(warning);
            }
            buffer.append("</span></div>\r\n");
        }

        if (XMLUtils.hasErrors(rup.getRootElement())) {
            buffer.append("<div class=\"errorInput\"><span class=\"ui-corner-all\">");
            buffer.append(config.getLocalizedString("msgRUPError"));

            String error = config.getLocalizedJSONString(XMLUtils.getErrorValue(rup.getRootElement()));
            if (error != null) {
                buffer.append(error);
            }

            buffer.append("</span></div>\r\n");
        }

        // title of custom form
        buffer.append("<h1 title=\"'" + cas.getCaseId() + "'\">");
        buffer.append(config.getLocalizedString("case") + ": ");
        buffer.append(cas.getCaseId());
        buffer.append("</h1>");

        buffer.append("<input type=\"hidden\" name=\"" + LANGUAGE_ATTRIBUTE_NAME +
                "\" id=\"" + LANGUAGE_ATTRIBUTE_NAME + "\" value=\"\"/>\r\n\r\n");

        // rescheduling information fields
        if (!isShortForm) {
            buffer.append("<input type=\"hidden\" name=\"reschedulingKey\" id=\"reschedulingKey\" value=\"\"/>");
        }

        // prepare tabs
        buffer.append("<div id=\"tabs\">");
        buffer.append("<ul>");

        for (Element activity : activities) {
            String activityName = activity.getChildText(XML_ACTIVITYNAME);
            String clazz = "";

            if (XMLUtils.hasErrors(activity)) {
                clazz = " class=\"" + CSS_ERRORTEXT + "\"";
            } else if (XMLUtils.hasWarnings(activity)) {
                clazz = " class=\"" + CSS_WARNINGTEXT + "\"";
            }

            buffer.append("<li><a href=\"#" + activityName + "\"><span" + clazz + ">");
            buffer.append(config.getLocalizedString(activityName));
            buffer.append("</span></a></li>");
        }
        buffer.append("</ul>");

        buffer.append("<input type=\"hidden\" name=\"selectedTab\" id=\"selectedTab\" value=\"\"/>");

        String selectedTab = request.getParameter("selectedTab");
        bufferBottom.append(" $(\"#tabs\").tabs('select', " + (selectedTab == null ? 0 : selectedTab) + ");\r\n");

        // loop over activities
        for (int i = 0; i < activities.size(); i++) {
            Element activity = activities.get(i);
            String activityName = activity.getChildText(XML_ACTIVITYNAME);

            buffer.append("\r\n\r\n");
            buffer.append("<div id=\"" + activityName + "\">");

            String key = XML_ACTIVITY + "_" + activityName;
            buffer.append("<fieldset>");
            buffer.append("<legend style=\"font-weight:bold;\">" + config.getLocalizedString("general") + "</legend>");
            buffer.append("<table>");
            buffer.append("<tr>");

            Element type = activity.getChild(XML_ACTIVITYTYPE);
            Element duration = activity.getChild(XML_DURATION);
            Element from = activity.getChild(XML_FROM);
            Element to = activity.getChild(XML_TO);
            buffer.append(getInputColumn(type, type.getName(), null, key, XMLUtils.isVisibleFromSchema(type.getName())));
            buffer.append(getInputColumn(duration, duration.getName(), to, key, XMLUtils.isVisibleFromSchema(duration.getName())));
            if (isShortForm) {

                // show FROM only at first activity (if i=0)
                buffer.append(getInputColumn(from, "Date", to, key, i == 0 && XMLUtils.isVisibleFromSchema(from.getName())));
                buffer.append("<input type=\"hidden\" name=\"reschedulingKey\" id=\"reschedulingKey\" value=\"" + key + "_" + from.getName() + "\"/>");
            } else {
                buffer.append(getInputColumn(from, from.getName(), to, key, XMLUtils.isVisibleFromSchema(from.getName())));
                buffer.append(getInputColumn(to, to.getName(), from, key, XMLUtils.isVisibleFromSchema(to.getName())));
            }

            buffer.append("</tr>");
            buffer.append("</table>");
            buffer.append("</fieldset>");

            buffer.append(getListOf(activity, XML_RESERVATION, true));
            buffer.append(getListOf(activity, XML_UTILISATIONREL, !isShortForm));
            buffer.append(getListOf(activity, XML_MSGTRANSFER, !isShortForm));

            buffer.append("</div>");
        }

        bufferTop.append(" sessionHandle=\"" + ResourceServiceInterface.getInstance().getHandle() + "\";\r\n");
        bufferTop.append(" count=" + count + ";\r\n");
        bufferTop.append(" msgAjaxError=\"" + config.getLocalizedString("msgAjaxError") + "\";\r\n");
        bufferTop.append(" msgCancelWithoutSave=\"" + config.getLocalizedString("msgCancelWithoutSave") + "\";\r\n");
        bufferTop.append(" ajaxCache={};\r\n"); // cache reload bei jedem aufruf

        buffer.append("</div>");

        buffer.append("\r\n\r\n<input type=\"submit\" name=\"Cancel\" id=\"Cancel\" value=\"" + config.getLocalizedString("cancelButton") + "\"");
        buffer.append(" onclick=\"return dontSave(event);\"");
        buffer.append(" onkeyup=\"return dontSave(event);\"");
        buffer.append(" title=\"" + config.getLocalizedString("AbortAndReturn") + "\"");
        buffer.append(" class=\"abort ui-button ui-widget ui-state-default ui-button-text-only ui-corner-left ui-button-text\"");
        buffer.append("/>");

        buffer.append("<input type=\"submit\" name=\"Save\" id=\"Save\" value=\"" + config.getLocalizedString("saveButton") + "\"");
        buffer.append(haveTosave ? " disabled=false" : " disabled=true");
        buffer.append(" onclick=\"return saveTab();\"");
        buffer.append(" onkeyup=\"return saveTab();\"");
        buffer.append(" title=\"" + config.getLocalizedString("Save") + "\"");
        buffer.append(" class=\"ui-button ui-widget ui-state-default ui-button-text-only ui-button-text\"");
        buffer.append("/>");

        bufferBottom.append(" enableButton('Save', " + haveTosave + ");\r\n");

        // confirm reservations button
        if (canConfirmReservations) {
            buffer.append("<input type=\"submit\" name=\"ConfirmReservations\" id=\"ConfirmReservations\" value=\"" + config.getLocalizedString("confirmReservationsButton") + "\"");
            buffer.append(" onclick=\"return saveTab();\"");
            buffer.append(" onkeyup=\"return saveTab();\"");
            buffer.append(" title=\"" + config.getLocalizedString("confirmReservations") + "\"");
            buffer.append(" class=\"ui-button ui-widget ui-state-default ui-button-text-only ui-button-text\"");
            buffer.append("/>");
        }

        buffer.append("<input type=\"submit\" name=\"Complete\" id=\"Complete\" value=\"" + config.getLocalizedString("completeButton") + "\"");
        buffer.append(" onclick=\"return saveTab();\"");
        buffer.append(" onkeyup=\"return saveTab();\"");
        buffer.append(" title=\"" + config.getLocalizedString("CompleteAndReturn") + "\"");
        buffer.append(" class=\"ui-button ui-widget ui-state-default ui-button-text-only ui-corner-right ui-button-text\"");
        buffer.append("/>");

        buffer.append("<input type=\"hidden\" name=\"CompleteForce\" id=\"CompleteForce\" value=\"\"/>");

        buffer.append("</div>");
        buffer.append("</div>");
        buffer.append("</form>");

        return buffer;
    }


    private StringBuffer getInputColumn(Element field, String name2Show,
                                        Element dependingField, String key, boolean visible) {
        StringBuffer buffer = new StringBuffer();

        String fieldKey = key + "_" + field.getName();

        List<Element> list = field.getChildren();
        if (list.isEmpty()) { //no more children
            String n = field.getName(), style = "";
            if (!visible || n.equals(XML_ROLE) || n.equals(XML_CAPABILITY) || n.equals(XML_CATEGORY) || n.equals(XML_SUBCATEGORY)) {
                style += "display:none;"; // not visible till "enableResourceType"
            }

            buffer.append("\r\n");

            buffer.append("<td align=\"right\" valign=\"middle\"");
            buffer.append(style.isEmpty() ? "" : " style=\"" + style + "\"");
            buffer.append(">" + config.getLocalizedString(name2Show) + "</td>");

            buffer.append("<td align=\"right\" valign=\"middle\" class=\"inputTD\"");
            buffer.append(style.isEmpty() ? "" : " style=\"" + style + "\"");
            buffer.append(">");
            buffer.append(getInputHTML(field, dependingField, fieldKey));
            buffer.append("</td>");
            buffer.append("<td align=\"left\" valign=\"middle\" class=\"inputTDUnit\"");
            buffer.append(style.isEmpty() ? "" : " style=\"" + style + "\"");
            buffer.append(">");
            String unit = config.getLocalizedString(XMLUtils.getUnitFromSchema(field.getName()));
            buffer.append(unit == null ? "" : unit);
            buffer.append("</td>");

        } else {
            String resKey = fieldKey + "_" + XML_RESOURCE_TYPE;
            Boolean resourceTypeRole = null;

            if (wrapBeforeResource) {
                buffer.append("</tr><tr>");
            }

            // get resource type TODO@tbe: radiobox aus request auswerten
            for (Element childField : list) {
                String childName = childField.getName();

                if (resourceTypeRole != null) {
                    break;
                } else if (childName.equals(XML_ID)) {
                    if (childField.getText().startsWith("PA-")) {
                        resourceTypeRole = true;
                    } else if (childField.getText().startsWith("NH-")) {
                        resourceTypeRole = false;
                    }
                } else if (childName.equals(XML_ROLE) || childName.equals(XML_CAPABILITY)) {
                    if (!childField.getText().isEmpty()) {
                        resourceTypeRole = true;
                    }
                } else if (childName.equals(XML_CATEGORY) || childName.equals(XML_SUBCATEGORY)) {
                    if (!childField.getText().isEmpty()) {
                        resourceTypeRole = false;
                    }
                }
            }

            for (Element childField : list) {
                if (childField.getName().equals(XML_ID)) {
                    int levelsUp = wrapBeforeResource ? 6 : 2;

                    buffer.append("\r\n");

                    // insert radio group for human and non-human before
                    if (wrapBeforeResource) {
                        buffer.append("<td align=\"right\" valign=\"middle\"");
                        buffer.append(">" + config.getLocalizedString("resource") + "</td>");
                    }

                    buffer.append("<td align=\"right\" valign=\"bottom\" class=\"inputTD\" style=\"white-space:nowrap;\">"); // width=\"300\"

                    buffer.append("<input type=\"radio\" name=\"" + resKey + "\" id=\"" + resKey + "-human\"");
                    buffer.append(" value=\"human\"");
                    buffer.append(" onclick=\"");
                    buffer.append(" enableButton('Save', true);");
                    buffer.append(" enableResourceType(this" + Utils.copy(".parentNode", levelsUp) + ".id + '_" + XML_RESOURCE + "_" + XML_ROLE + "');");
                    buffer.append(" enableResourceType(this" + Utils.copy(".parentNode", levelsUp) + ".id + '_" + XML_RESOURCE + "_" + XML_CAPABILITY + "');");
                    buffer.append(" disableResourceType(this" + Utils.copy(".parentNode", levelsUp) + ".id + '_" + XML_RESOURCE + "_" + XML_CATEGORY + "');");
                    buffer.append(" disableResourceType(this" + Utils.copy(".parentNode", levelsUp) + ".id + '_" + XML_RESOURCE + "_" + XML_SUBCATEGORY + "');");
                    buffer.append(" actualizeDropDownBox('" + XML_RESOURCE_TYPE + "-human','" + XML_ID + "',this.id);");
                    buffer.append("\"");
                    buffer.append(">&nbsp;" + config.getLocalizedString("human"));
                    buffer.append("&nbsp;");

                    buffer.append("<input type=\"radio\" name=\"" + resKey + "\" id=\"" + resKey + "-non-human\"");
                    buffer.append(" value=\"non-human\"");
                    buffer.append(" onclick=\"");
                    buffer.append(" enableButton('Save', true);");
                    buffer.append(" disableResourceType(this" + Utils.copy(".parentNode", levelsUp) + ".id + '_" + XML_RESOURCE + "_" + XML_ROLE + "');");
                    buffer.append(" disableResourceType(this" + Utils.copy(".parentNode", levelsUp) + ".id + '_" + XML_RESOURCE + "_" + XML_CAPABILITY + "');");
                    buffer.append(" enableResourceType(this" + Utils.copy(".parentNode", levelsUp) + ".id + '_" + XML_RESOURCE + "_" + XML_CATEGORY + "');");
                    buffer.append(" enableResourceType(this" + Utils.copy(".parentNode", levelsUp) + ".id + '_" + XML_RESOURCE + "_" + XML_SUBCATEGORY + "');");
                    buffer.append(" actualizeDropDownBox('" + XML_RESOURCE_TYPE + "-non-human','" + XML_ID + "',this.id);");
                    buffer.append("\"");
                    buffer.append(">&nbsp;" + config.getLocalizedString("non-human"));

                    buffer.append("</td>");

                    if (wrapBeforeResource) {
                        buffer.append("<td align=\"left\" valign=\"middle\" class=\"inputTDUnit\"/>");
                    }

                    if (resourceTypeRole == null || resourceTypeRole) {
                        dependingField = new Element("human"); // set dependingField for ID
                        dependingField.setText("human");

                        bufferBottom.append(" enableResourceType('" + fieldKey + "_" + XML_ROLE + "');\r\n");
                        bufferBottom.append(" enableResourceType('" + fieldKey + "_" + XML_CAPABILITY + "');\r\n");
                        bufferBottom.append(" disableResourceType('" + fieldKey + "_" + XML_CATEGORY + "');\r\n");
                        bufferBottom.append(" disableResourceType('" + fieldKey + "_" + XML_SUBCATEGORY + "');\r\n");
                        bufferBottom.append(" document.getElementById('" + resKey + "-human').checked = true;\r\n");
                        bufferBottom.append(" actualizeDropDownBox('" + XML_RESOURCE_TYPE + "-human','" + XML_ID + "','" + fieldKey + "_" + XML_ID + "');\r\n");
                    } else {
                        dependingField = new Element("non-human"); // set dependingField for ID
                        dependingField.setText("non-human");

                        bufferBottom.append(" disableResourceType('" + fieldKey + "_" + XML_ROLE + "');\r\n");
                        bufferBottom.append(" disableResourceType('" + fieldKey + "_" + XML_CAPABILITY + "');\r\n");
                        bufferBottom.append(" enableResourceType('" + fieldKey + "_" + XML_CATEGORY + "');\r\n");
                        bufferBottom.append(" enableResourceType('" + fieldKey + "_" + XML_SUBCATEGORY + "');\r\n");
                        bufferBottom.append(" document.getElementById('" + resKey + "-non-human').checked = true;\r\n");
                        bufferBottom.append(" actualizeDropDownBox('" + XML_RESOURCE_TYPE + "-non-human','" + XML_ID + "','" + fieldKey + "_" + XML_ID + "');\r\n");
                    }
                }

                buffer.append(getInputColumn(childField, childField.getName(),
                        dependingField, fieldKey, XMLUtils.isVisibleFromSchema(childField.getName())));
                dependingField = childField;
            }
        }

        return buffer;
    }

    private StringBuffer getInputHTML(Element field, Element dependingField, String key) {
        StringBuffer tag1 = new StringBuffer(), tag2 = new StringBuffer(), tag3 = new StringBuffer();

        String n = field.getName();
        String value = field.getText();
        String title = "", onclick = "", onchange = "", onpropertychange = "", cssClass = "", style = "";
        String dependingFieldKey = null, dependingFieldValue = null;
        String fromFieldKey = null, durationFieldKey = null;
        String keyPart = key.substring(0, key.lastIndexOf("_"));
        boolean readonly = false;
        boolean disabled = XMLUtils.isReadonlyFromSchema(n);

        // is field no more editable (if SOU or EOU)
        Element activity = field.getParentElement();
        while (activity != null && !activity.getName().equals(XML_ACTIVITY)) {
            activity = activity.getParentElement();
        }
        boolean eou = isRequestTypeEOU(activity);
        boolean sou = eou || isRequestTypeSOU(activity);

        ArrayList<String> enumerations = new ArrayList<String>();
        try {
            ArrayList<Element> restrictions = new ArrayList<Element>();
            cssClass = validateElement(field, restrictions);
            for (Element restriction : restrictions) {
                if (restriction.getName().equals("enumeration")) {
                    enumerations.add(restriction.getAttributeValue("value"));
                }
            }

            if (cssClass == null) cssClass = "";
        } catch (Exception e) {
            _log.error("cannot get css class for element: " + n, e);
        }

        if (dependingField != null) {
            durationFieldKey = keyPart + "_" + XML_DURATION;
            fromFieldKey = keyPart + "_" + XML_FROM;
            dependingFieldValue = dependingField.getText();
            dependingFieldKey = keyPart + "_" + dependingField.getName();
        }

        // set field layout
        if (!enumerations.isEmpty() || XMLUtils.isEnumerationFromSchema(n)) {
            tag1.append("<select size=\"1\"");

            if (XMLUtils.isEnumerationFromSchema(n)) {
                if (n.equals(XML_OTHERACTIVITYNAME)) {
                    for (Element activityElem : activities) {
                        String name = activityElem.getChildText(XML_ACTIVITYNAME);
                        tag2.append("<option value=\"" + name + "\"");
                        tag2.append(name.equals(value) ? " selected=\"selected\"" : "");
                        tag2.append(">" + config.getLocalizedString(name) + "</option>");
                    }
                } else if (n.equals(XML_ACTIVITYTYPE)) {
                    try {
                        tag2.append("<option value=\"\"");
                        tag2.append("".equals(value) ? " selected=\"selected\"" : "");
                        tag2.append(">" + config.getLocalizedString("&lt;msgNewEntry&gt;") + "</option>");

                        List<String> activityTypes = ss.getActivityTypes(activity.getChildText(XML_ACTIVITYNAME), value);
                        for (String activityType : activityTypes) {
                            tag2.append("<option value=\"" + activityType + "\"");
                            tag2.append(activityType.equals(value) ? " selected=\"selected\"" : "");
                            tag2.append(">" + config.getLocalizedString(activityType) + "</option>");
                        }
                    } catch (Exception e) {
                        XMLUtils.addWarningValue(field, "msgActivityTypesWarning");
                    }

                    // activate if:
                    // 1. 'new entry' was selected or
                    // 2. 'new entry' was clicked and no more options than 'new entry' exists in selectbox
                    onchange += " comboBox(this,0);"; //
                    onpropertychange += " comboBox(this,0);";
                    onclick += " comboBox(this,1);";
                } else {
                    tag2.append("<script type=\"text/javascript\">");
                    tag2.append("writeDropDownBox('" + n + "','" + dependingFieldValue + "','" + value + "','" + key + "');");
                    tag2.append("</script>");
                }
            } else {
                for (String s : enumerations) {
                    tag2.append("<option value=\"" + s + "\"");
                    tag2.append(s.equals(value) ? " selected=\"selected\"" : "");
                    tag2.append(">" + config.getLocalizedString(s) + "</option>");
                }
            }

            tag3.append("</select>");
        } else if (cssClass.equals(CSS_DATEINPUT)) {
            try {
                Date date = Utils.string2Date(value, Utils.DATETIME_PATTERN_XML);
                value = Utils.date2String(date, isShortForm ? Utils.DATE_PATTERN : Utils.DATETIME_PATTERN);
            } catch (ParseException e) {
                //_log.error("cannot parse " + value);
            }

            tag1.append("<input type=\"text\" value=\"" + value + "\"");

            String showCalendar = " displayCalendar(document.getElementById('" + key + "'),'";
            if (isShortForm) {
                showCalendar += Utils.getJsCalendarFormat(Utils.DATE_PATTERN) + "',this);return false;";
            } else {
                showCalendar += Utils.getJsCalendarFormat(Utils.DATETIME_PATTERN) + "',this,true);return false;";
            }
            readonly = true;
            onclick += (disabled ? "" : showCalendar);

            tag3.append("</input>");
        } else if (cssClass.equals(CSS_DURATIONINPUT)) { // TODO@tbe: not only minutes, use jquery.slider
            tag1.append("<input type=\"text\" value=\"" + Utils.stringXMLDuration2stringMinutes(value) + "\"");
            tag3.append("</input>");
        } else {
            tag1.append("<input type=\"text\" value=\"" + value + "\"");
            tag3.append("</input>");
        }

        // some javascript functions for convenience
        if (n.equals(XML_FROM)) {
            if (!isShortForm) {
                onchange += " addMinutes2DateField('" + dependingFieldKey + "','" + key + "','" + durationFieldKey + "',1,false,'" + Utils.getJsCalendarFormat(Utils.DATETIME_PATTERN) + "');";
                onchange += " submitRescheduling('" + dependingFieldKey + "');";
            } else {
                onchange += " setRescheduling('" + key + "');";
            }
            disabled = disabled || sou;
        } else if (n.equals(XML_TO)) {
            if (!isShortForm) {
                onchange += " addMinutes2DateField('" + dependingFieldKey + "','" + key + "','" + durationFieldKey + "',-1,false,'" + Utils.getJsCalendarFormat(Utils.DATETIME_PATTERN) + "');";
                onchange += " submitRescheduling('" + dependingFieldKey + "');";
            }
            disabled = disabled || eou;
        } else if (n.equals(XML_ID) || n.equals(XML_ROLE) || n.equals(XML_CAPABILITY) || n.equals(XML_CATEGORY) || n.equals(XML_SUBCATEGORY)) {
            String statusFieldKey = keyPart.substring(0, keyPart.lastIndexOf("_") + 1) + XML_STATUS;
            onchange += " document.getElementById('" + statusFieldKey + "').selectedIndex=0;"; // set to "unknown"

            if (n.equals(XML_CATEGORY)) {
                onchange += " actualizeDropDownBox('" + XML_CATEGORY + "','" + XML_SUBCATEGORY + "',this.id);";
            }

            disabled = disabled || sou || eou;
        } else if (n.equals(XML_STATUS) || n.equals(XML_STATUSTOBE)) {
            disabled = disabled || sou || eou;
        } else if (n.equals(XML_DURATION)) {
            if (!isShortForm) {
                onchange += " addMinutes2DateField('" + dependingFieldKey + "','" + fromFieldKey + "','" + durationFieldKey + "',1,true,'" + Utils.getJsCalendarFormat(Utils.DATETIME_PATTERN) + "');";
                onchange += " submitRescheduling('" + dependingFieldKey + "');";
            }
            disabled = disabled || sou || eou;
        } else if (n.equals(XML_WORKLOAD)) {
            disabled = disabled || sou || eou;
        } else if (n.equals(XML_OTHERACTIVITYNAME) || n.equals(XML_MIN) || n.equals(XML_MAX)
                || n.equals(XML_THISUTILISATIONTYPE) || n.equals(XML_OTHERUTILISATIONTYPE)) {
            onchange += " setRescheduling('" + key + "');";
        }

        // show error and warning messages
        String error = config.getLocalizedJSONString(XMLUtils.getErrorValue(field));
        String warning = config.getLocalizedJSONString(XMLUtils.getWarningValue(field));

        if (error != null) {
            title = error;
            cssClass += " " + CSS_ERRORINPUT;
        } else {
            if (warning != null) {
                title = warning;
                cssClass += " " + CSS_WARNINGINPUT;
            }
        }

        if (XMLUtils.isRequiredFromSchema(n) || isShortForm) {
            cssClass += " " + CSS_REQUIRED;
        }
        tag1.append(disabled ? " disabled=\"disabled\"" : "");
        tag1.append(readonly ? " readonly=\"readonly\"" : "");

        tag1.append(" id=\"" + key + "\" name=\"" + key + "\"");
        tag1.append(title.isEmpty() ? "" : " title=\"" + title + "\"");
        tag1.append(cssClass.isEmpty() ? "" : " class=\"" + cssClass + "\"");
        tag1.append(style.isEmpty() ? "" : " style=\"" + style + "\"");
        tag1.append(" onchange=\"enableButton('Save', true);" + onchange + "\"");
        tag1.append(" onpropertychange=\"enableButton('Save', true);" + onpropertychange + "\"");
        tag1.append(onclick.isEmpty() ? "" : " onclick=\"" + onclick + "\"");
        tag1.append(">");

        return tag1.append(tag2).append(tag3);
    }

    /**
     * gets rows for each element of type reservation or utilisationRelation
     *
     * @param listElementName
     * @return
     */
    private StringBuffer getListOf(Element activity, String listElementName, boolean visible) {
        boolean disabledButton = false;
        if (listElementName.equals(XML_RESERVATION)) {
            disabledButton = isRequestTypeEOU(activity) || isRequestTypeSOU(activity);
        } else if (listElementName.equals(XML_UTILISATIONREL)) {
            disabledButton = true; // TODO@tbe: erstmal nur von einer Relation pro Aktivit�t ausgehen
        }

        StringBuffer buffer = new StringBuffer();
        String templateName = listElementName + "Template";
        String insertName = activity.getChildText(XML_ACTIVITYNAME) + "_" + templateName + "_insert";
        String key = XML_ACTIVITY + "_" + activity.getChildText(XML_ACTIVITYNAME) + "_" + listElementName + "_#";

        buffer.append("\r\n\r\n<fieldset");
        buffer.append(visible ? "" : " style=\"display: none\"");
        buffer.append(">");

        buffer.append("<legend style=\"font-weight:bold;\">" + config.getLocalizedString(listElementName + "s") + "</legend>");
        buffer.append("<table>");
        buffer.append("<tr>");
        buffer.append(getAddButton(templateName, key, insertName, disabledButton));
        buffer.append("</tr>");

        List<Element> elements = (List<Element>) activity.getChildren(listElementName);
        for (int i = 1; i <= elements.size(); i++) {
            count++;
            Element resOrUtil = elements.get(i - 1);

            buffer.append(getErrorRow(resOrUtil, key + count));
            buffer.append(getRow(resOrUtil, key + count, disabledButton));
        }

        buffer.append("\r\n<tr style=\"display: none\" id=\"" + insertName + "\"><td></td></tr>");
        buffer.append("</table>");
        buffer.append("</fieldset>");

        return buffer;
    }

    /**
     * gets row for a element of type reservation or utilisationRelation
     *
     * @param resOrUtil
     * @param key
     * @param disabledButton
     * @return
     */
    private StringBuffer getRow(Element resOrUtil, String key, boolean disabledButton) {
        StringBuffer buffer = new StringBuffer();
        String trStyle = count % 2 == 1 ? " style=\"background-color: #FFEEBB;\"" : "";
        buffer.append("\r\n\r\n<tr id=\"" + key + "\"" + trStyle + ">");
        if (wrapBeforeResource) {
            buffer.append("<td style=\"border: 2px groove threedface;\"><table><tr>");
        }

        // loop over all fields
        Element prevField = null; // if exist dependency to last field
        for (Element field : (List<Element>) resOrUtil.getChildren()) {
            buffer.append(getInputColumn(field, field.getName(), prevField, key, XMLUtils.isVisibleFromSchema(field.getName())));
            prevField = field;
        }

        buffer.append(getAddRemoveButtons(key, disabledButton, wrapBeforeResource ? 6 : 2));

        if (wrapBeforeResource) {
            buffer.append("</tr></table></td>");
        }

        buffer.append("</tr>");

        return buffer;
    }

    /**
     * gets error row for a element of type reservation or utilisationRelation
     *
     * @param element
     * @param key
     * @return
     */
    private StringBuffer getErrorRow(Element element, String key) {
        StringBuffer buffer = new StringBuffer();
        String error = config.getLocalizedJSONString(XMLUtils.getErrorValue(element));
        if (error != null) {
            buffer.append("<tr id=\"" + key + "_error\"><td colspan=\"" + (getColspan(element) * 2 + 1) + "\" style=\"text-align:left;\">");
            buffer.append("<div class=\"" + CSS_ERRORTEXT + "\">");
            buffer.append(error);
            buffer.append("</div></td></tr>");
        } else {
            String warning = config.getLocalizedJSONString(XMLUtils.getWarningValue(element));
            if (warning != null) {
                buffer.append("<tr id=\"" + key + "_warning\"><td colspan=\"" + (getColspan(element) * 2 + 1) + "\" style=\"text-align:left;\">");
                buffer.append("<div class=\"" + CSS_WARNINGTEXT + "\">");
                buffer.append(warning);
                buffer.append("</div></td></tr>");
            }
        }

        return buffer;
    }

    private int getColspan(Element element) {
        int count = element.getChildren().isEmpty() ? 1 : 0;
        for (Element e : (List<Element>) element.getChildren()) {
            count += getColspan(e);
        }
        return count;
    }

    private StringBuffer getAddButton(String templateName, String key, String insertName, boolean disabled) {
        String onclickAdd = "addCloneBeforeInsert(document.getElementById('" + templateName + "'), " +
                "'" + key + "', document.getElementById('" + insertName + "')); enableButton('Save', true); return false;";

        StringBuffer buffer = new StringBuffer();
        buffer.append("<td>");

        buffer.append("\r\n<input type=\"image\" src=\"images/plus2.png\" alt=\"" + config.getLocalizedString("addResourceButton") + "\"");

        buffer.append(" onclick=\"" + onclickAdd + "\"");
        buffer.append(" title=\"" + config.getLocalizedString("addResourceButton") + "\"");
        buffer.append(disabled ? " disabled=\"disabled\"" : "");

        buffer.append("/>");

        buffer.append("</td>");
        return buffer;
    }

    private StringBuffer getAddRemoveButtons(String key, boolean disabled, int levelsUp) {
        String onclickRemove = "removeFieldAndItsError(this" + Utils.copy(".parentNode", levelsUp) + "); enableButton('Save', true); return false;";

        StringBuffer buffer = new StringBuffer();
        buffer.append("<td style=\"text-align: right;\">");
        buffer.append("\r\n\r\n<input type=\"image\" src=\"images/minus2.png\" alt=\"" + config.getLocalizedString("removeButton") + "\"");
        buffer.append(" onclick=\"" + onclickRemove + "\"");
        buffer.append(" title=\"" + config.getLocalizedString("removeButton") + "\"");
        buffer.append(disabled ? " disabled=\"disabled\"" : "");
        buffer.append("/>");

        buffer.append("</td>");
        return buffer;
    }

    private String getRequestType(Element activity) {
        return activity == null ? null : activity.getChildText(XML_REQUESTTYPE);
    }

    private boolean isRequestTypeSOU(Element activity) {
        String requestType = getRequestType(activity);
        return requestType != null && requestType.equals(UTILISATION_TYPE_BEGIN);
    }

    private boolean isRequestTypeEOU(Element activity) {
        String requestType = getRequestType(activity);
        return requestType != null && requestType.equals(UTILISATION_TYPE_END);
    }

    /**
     * TODO@tbe: can we generate this templates from XSD?
     *
     * @param elementName
     * @return
     */
    public static Element getTemplate(String elementName) {
        Element elem;
        if (XML_ACTIVITY.equals(elementName)) {
            elem = Utils.string2Element("<Activity><ActivityName/><ActivityType/><Duration/><From/><To/></Activity>");
        } else if (XML_RESERVATION.equals(elementName)) {
            elem = Utils.string2Element("<Reservation><StatusToBe/><Status/><Workload/><Resource>" +
                    "<Id/><Role/><Capability/><Category/><SubCategory/></Resource><ReservationId/></Reservation>");
        } else if (XML_UTILISATIONREL.equals(elementName)) {
            elem = Utils.string2Element("<UtilisationRelation><ThisUtilisationType/>" +
                    "<OtherUtilisationType/><OtherActivityName/><Min/><Max/></UtilisationRelation>");
        } else if (XML_MSGTRANSFER.equals(elementName)) {
            elem = Utils.string2Element("<" + XML_MSGTRANSFER + "><" + XML_MSGDURATION + "/><" + XML_MSGREL + "/>" +
                    "<" + XML_MSGUTILISATIONTYPE + "/><" + XML_MSGTO + "/><" + XML_MSGBODY + "/></" + XML_MSGTRANSFER + ">");
        } else {
            _log.warn("element " + elementName + " has no template");
            return new Element(elementName);
        }

        XMLUtils.setDefaults(elem);
        return elem;
    }

    /**
     * redirect to source
     */
    private void redirect(String source) throws IOException {
        session.removeAttribute("ExceptionMsg");
        session.removeAttribute("Exception");
        response.sendRedirect(response.encodeURL(source));
    }

    /**
     * redirect back to the worklist
     */
    private void redirect() throws IOException {
        redirect(source);
    }

    /**
     * shows error page
     */
    private void handleError(Throwable t) {
        try {
            _log.error("show error page: ", t);
            session.setAttribute("ErrorPageTitle", config.getLocalizedString("titleErrorPage"));
            session.setAttribute("Exception", t);

            String errorMsg = config.getLocalizedString("msgErrorPage");
            if (wirElement != null && wirElement.getName().equals("failure")) {
                String yawlErrorMsg = config.getLocalizedString(wirElement.getText());
                errorMsg += yawlErrorMsg.isEmpty() ? "" : ": " + yawlErrorMsg;
            }
            session.setAttribute("ExceptionMsg", errorMsg);

            String url = PropertyReader.getInstance()
                    .getYAWLProperty("WorkQueueGatewayClient.backEndURI");
            url = url.substring(0, url.indexOf("resourceService/") + 16);
            session.setAttribute("ErrorPageLoginText", config.getLocalizedString("msgErrorPageLoginText"));
            session.setAttribute("ErrorPageLoginLink", url + "faces/Login.jsp");
            session.setAttribute("ErrorPageWorkqueueText", config.getLocalizedString("msgErrorPageWorkqueueText"));
            session.setAttribute("ErrorPageWorkqueueLink", url + "faces/userWorkQueues.jsp");
        } catch (Throwable e) {
            _log.error("cannot configure error page", e);
        }

        try {
            response.sendRedirect(response.encodeURL("Error.jsp"));
        } catch (Throwable e) {
            _log.error("cannot find error page", e);
        }
    }

}
