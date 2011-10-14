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

package org.yawlfoundation.yawl.scheduling;

/**
 * 
 * @author tbe
 * @version $Id: XMLUtils.java 18313 2010-04-30 10:14:09Z tbe $
 */
public interface Constants
{
	public static final String RESOURCE_STATUS_UNCHECKED = "unchecked";
	public static final String RESOURCE_STATUS_UNKNOWN = "unknown";
	public static final String RESOURCE_STATUS_NOTAVAILABLE = "unavailable";
	public static final String RESOURCE_STATUS_AVAILABLE = "available";
	public static final String RESOURCE_STATUS_REQUESTED = "requested";
	public static final String RESOURCE_STATUS_RESERVED = "reserved";

	public static final String UTILISATION_TYPE_PLAN = "POU";
	public static final String UTILISATION_TYPE_BEGIN = "SOU";
	public static final String UTILISATION_TYPE_END = "EOU";

	public static final String ADDRESS_TYPE_IP = "IP";
	public static final String ADDRESS_TYPE_EMAIL = "EMail";
	public static final String ADDRESS_TYPE_SMS = "SMS";

	public static final String MSGREL_BEFORE = "before";
	public static final String MSGREL_AFTER = "after";

	public static final String XML_RESOURCE_TYPE = "ResourceType";

	public static final String XML_DUMMY = "DUMMY";
	public static final String XML_ERROR = "error";
	public static final String XML_WARNING = "warning";
	public static final String XML_UNIT = "unit";
	public static final String XML_UTILISATION_TYPE = "UtilisationType";

	public static final String XML_RUP_LIGHT = "ResourceUtilisationPlan_light";
	public static final String XML_RUP = "ResourceUtilisationPlan";
	public static final String XML_CASEID = "CaseId";
	public static final String XML_ACTIVITY = "Activity";
	public static final String XML_STARTTASKID = "StartTaskId";
	public static final String XML_ENDTASKID = "EndTaskId";
	public static final String XML_REQUESTTYPE = "RequestType";
	public static final String XML_ACTIVITYNAME = "ActivityName";
	public static final String XML_ACTIVITYTYPE = "ActivityType";
	public static final String XML_FROM = "From";
	public static final String XML_TO = "To";
	public static final String XML_DURATION = "Duration";
	public static final String XML_RESERVATION = "Reservation";
	public static final String XML_RESERVATIONID = "ReservationId";
	public static final String XML_STATUSTOBE = "StatusToBe";
	public static final String XML_STATUS = "Status";
	public static final String XML_RESOURCE = "Resource";
	public static final String XML_ID = "Id";
	public static final String XML_ROLE = "Role";
	public static final String XML_CAPABILITY = "Capability";
	public static final String XML_CATEGORY = "Category";
	public static final String XML_SUBCATEGORY = "SubCategory";
	public static final String XML_WORKLOAD = "Workload";
	public static final String XML_UTILISATIONREL = "UtilisationRelation";
	public static final String XML_THISUTILISATIONTYPE = "ThisUtilisationType";
	public static final String XML_OTHERACTIVITYNAME = "OtherActivityName";
	public static final String XML_OTHERUTILISATIONTYPE = "OtherUtilisationType";
	public static final String XML_MIN = "Min";
	public static final String XML_MAX = "Max";
	public static final String XML_MSGTRANSFER = "MessageTransfer";
	public static final String XML_MSGDURATION = "MsgDuration";
	public static final String XML_MSGUTILISATIONTYPE = "MsgUtilisationType";
	public static final String XML_MSGREL = "MsgRel";
	public static final String XML_MSGTO = "MsgTo";
	public static final String XML_MSGBODY = "MsgBody";

	public static final String XML_MESSAGES = "Messages";
	public static final String XML_MESSAGE_SEND = "Message";
	public static final String XML_MESSAGEPUSH_SEND = "MessagePush";
	public static final String XML_TIMESTAMP = "TimeStamp";
	public static final String XML_CHANNEL = "Channel";
	public static final String XML_ADDRESS = "Address";
	public static final String XML_ADDRESSTYPE = "AddressType";
	public static final String XML_PAYLOAD = "Payload";
	public static final String XML_TEXT = "Text";
	public static final String XML_UTILISATIONTYPE = "UtilisationType";

	public static final String XML_EVENT_RECEIVE = "Event";
	public static final String XML_FILTERMODEL = "FilterModel";

	public static final String XML_UTILISATION = "Utilisation";

	public static final String XML_RESCHEDULING = "Rescheduling";

	public static final String CSS_TEXTINPUT = "textInput";
	public static final String CSS_DATEINPUT = "dateInput";
	public static final String CSS_INTINPUT = "intInput";
	public static final String CSS_DURATIONINPUT = "durInput";
	public static final String CSS_BOOLEANINPUT = "boolInput";

	public static final String CSS_ERRORINPUT = "errorInputTD";
	public static final String CSS_ERRORTEXT = "errorText";
	public static final String CSS_WARNINGINPUT = "warningInputTD";
	public static final String CSS_WARNINGTEXT = "warningText";
	public static final String CSS_REQUIRED = "required";

	public static final String DELIMITER = " ";

	public static final String LANGUAGE_ATTRIBUTE_NAME = "language";
	public static final String LANGUAGE_DEFAULT = "en";
	public static final String[] LANGUAGES = new String[] { LANGUAGE_DEFAULT, "de" };

	/**
	 * list of chars which separates words in written language
	 */
	public static final String WORD_SEPARATORS = " .:,;+*~/\\?=})]([/{&%$��\"�!^�|<>#'�`@��";

	/**
	 * TODO@tbe: some data types are not yet supported, see comments below
	 * Unsupported data types will be handled like strings.
	 * 
	 * see www.w3schools.com/schema/schema_dtypes_string.asp
	 */
	public static final String[] XSDDatatypes_String = new String[] { "ENTITIES", "ENTITY", "ID", "IDREF", "IDREFS",
			"language", "Name", "NCName", "NMTOKEN", "NMTOKENS", "normalizedString", "QName", "string", "token", "anyURI",
			"base64Binary", "hexBinary", "NOTATION", "float", // not yet supported
			"date", "gDay", "gMonth", "gMonthDay", "gYear", "gYearMonth", "time" }; // not
																											// yet
																											// supported,
																											// because
																											// date
																											// pattern
																											// has
																											// to
																											// be
																											// defined

	public static final String[] XSDDatatypes_DateTime = new String[] { "dateTime" };

	public static final String[] XSDDatatypes_Duration = new String[] { "duration" };

	public static final String[] XSDDatatypes_Int = new String[] { "byte", "decimal", "int", "integer",
			"negativeInteger", "nonNegativeInteger", "nonPositiveInteger", "positiveInteger", "short", "unsignedInt",
			"unsignedShort", "unsignedByte" };

	public static final String[] XSDDatatypes_Long = new String[] { "long", "unsignedLong" };

	public static final String[] XSDDatatypes_Double = new String[] { "double" };

	public static final String[] XSDDatatypes_Boolean = new String[] { "boolean" };
	public final static String CSV_DELIMITER = ", ";
}
