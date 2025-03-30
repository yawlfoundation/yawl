/*
 * Copyright (c) 2004-2025 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.resourcing.codelets;

import org.jdom2.Element;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.resourcing.ResourceManager;
import org.yawlfoundation.yawl.resourcing.resource.Capability;
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.resourcing.resource.Position;
import org.yawlfoundation.yawl.resourcing.resource.Role;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Paul Tyson <paul.tyson@oberontech.com> 2025-01-08
 */
public class ParticipantInfo extends AbstractCodelet {

	public ParticipantInfo() {
		super();
		setDescription("This codelet gets properties of the participant with the given user id.<br> "
				+ "Multi-valued property values are separated by the value of input parameter, 'delimiter' (default ',').<br> "
				+ "Input: userid , delimiter.<br>" + "Output: userid, firstname, lastname, fullname, email, "
				+ "emailOnAllocation, emailOnOffer, isAdministrator, description, notes, "
				+ "positions, roles, capabilities");
	}

	@Override
	public Element execute(Element inData, List<YParameter> inParams, List<YParameter> outParams)
			throws CodeletExecutionException {
		ResourceManager rm = ResourceManager.getInstance();
		setInputs(inData, inParams, outParams);
		String userid = getValue("userid");
		String delimiter = ",";
		try {
			delimiter = getValue("delimiter");
		}
		catch (CodeletExecutionException cee) {
			; // no-op
		}

		Participant p = rm.getParticipantFromUserID(userid);
		boolean haveParticipant = Optional.ofNullable(p).isPresent();
		setParameterValue("userid", haveParticipant ? p.getUserID() : "");
		setParameterValue("firstname", haveParticipant ? p.getFirstName() : "");
		setParameterValue("lastname", haveParticipant ? p.getLastName() : "");
		setParameterValue("fullname", haveParticipant ? p.getFullName() : "");
		setParameterValue("email", haveParticipant ? p.getEmail() : "");
		setParameterValue("emailOnAllocation", Boolean.toString(haveParticipant ?
				p.isEmailOnAllocation() : false));
		setParameterValue("emailOnOffer", Boolean.toString(haveParticipant ?
				p.isEmailOnOffer() : false));
		setParameterValue("isAdministrator", Boolean.toString(haveParticipant ?
				p.isAdministrator() : false));
		setParameterValue("description", haveParticipant ? p.getDescription() : "");
		setParameterValue("notes", haveParticipant ? p.getNotes() : "");
		setParameterValue("positions", haveParticipant ?
				p.getPositions().stream().map(Position::getName)
						.collect(Collectors.joining(delimiter))
						: "");
		setParameterValue("roles", haveParticipant ?
				p.getRoles().stream().map(Role::getName)
						.collect(Collectors.joining(delimiter))
						: "");
		setParameterValue("capabilities", haveParticipant ?
				p.getCapabilities().stream().map(Capability::getName)
						.collect(Collectors.joining(delimiter))
						: "");

		return getOutputData();
	}

	
	@Override
	public List<YParameter> getRequiredParams() {
		List<YParameter> params = new ArrayList<YParameter>();

		YParameter param = new YParameter(null, YParameter._INPUT_PARAM_TYPE);
		param.setDataTypeAndName("string", "userid", XSD_NAMESPACE);
		param.setDocumentation("The userid of a participant");
		params.add(param);

		param = new YParameter(null, YParameter._INPUT_PARAM_TYPE);
		param.setDataTypeAndName("string", "delimiter", XSD_NAMESPACE);
		param.setDocumentation("The character sequence to delimit items in list.");
		params.add(param);

		param = new YParameter(null, YParameter._OUTPUT_PARAM_TYPE);
		param.setDataTypeAndName("string", "userid", XSD_NAMESPACE);
		param.setDocumentation("The userid of the given participant");
		params.add(param);

		param = new YParameter(null, YParameter._OUTPUT_PARAM_TYPE);
		param.setDataTypeAndName("string", "firstname", XSD_NAMESPACE);
		param.setDocumentation("The first name of the given participant");
		params.add(param);

		param = new YParameter(null, YParameter._OUTPUT_PARAM_TYPE);
		param.setDataTypeAndName("string", "lastname", XSD_NAMESPACE);
		param.setDocumentation("The last name of the given participant");
		params.add(param);

		param = new YParameter(null, YParameter._OUTPUT_PARAM_TYPE);
		param.setDataTypeAndName("string", "fullname", XSD_NAMESPACE);
		param.setDocumentation("The full name of the given participant");
		params.add(param);

		param = new YParameter(null, YParameter._OUTPUT_PARAM_TYPE);
		param.setDataTypeAndName("string", "email", XSD_NAMESPACE);
		param.setDocumentation("The email address of the given participant");
		params.add(param);

		param = new YParameter(null, YParameter._OUTPUT_PARAM_TYPE);
		param.setDataTypeAndName("boolean", "emailOnAllocation", XSD_NAMESPACE);
		param.setDocumentation("Whether or not to send email when workitem is allocated to given participant");
		params.add(param);

		param = new YParameter(null, YParameter._OUTPUT_PARAM_TYPE);
		param.setDataTypeAndName("boolean", "emailOnOffer", XSD_NAMESPACE);
		param.setDocumentation("Whether or not to send email when workitem is offered to given participant");
		params.add(param);

		param = new YParameter(null, YParameter._OUTPUT_PARAM_TYPE);
		param.setDataTypeAndName("boolean", "isAdministrator", XSD_NAMESPACE);
		param.setDocumentation("Indicates if given participant is an administrator.");
		params.add(param);

		param = new YParameter(null, YParameter._OUTPUT_PARAM_TYPE);
		param.setDataTypeAndName("string", "description", XSD_NAMESPACE);
		param.setDocumentation("The description of the given participant.");
		params.add(param);

		param = new YParameter(null, YParameter._OUTPUT_PARAM_TYPE);
		param.setDataTypeAndName("string", "notes", XSD_NAMESPACE);
		param.setDocumentation("Notes about the given participant.");
		params.add(param);

		param = new YParameter(null, YParameter._OUTPUT_PARAM_TYPE);
		param.setDataTypeAndName("string", "positions", XSD_NAMESPACE);
		param.setDocumentation(
				"Names of the positions held by the given participant, separated by the delimiter string.");
		params.add(param);

		param = new YParameter(null, YParameter._OUTPUT_PARAM_TYPE);
		param.setDataTypeAndName("string", "roles", XSD_NAMESPACE);
		param.setDocumentation("Names of the roles of the given participant, separated by the delimiter string.");
		params.add(param);

		param = new YParameter(null, YParameter._OUTPUT_PARAM_TYPE);
		param.setDataTypeAndName("string", "capabilities", XSD_NAMESPACE);
		param.setDocumentation(
				"Names of the capabilities of the given participant, separated by the delimiter string.");
		params.add(param);

		return params;
	}

}
