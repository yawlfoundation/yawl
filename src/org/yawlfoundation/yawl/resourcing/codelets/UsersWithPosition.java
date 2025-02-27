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
import org.yawlfoundation.yawl.resourcing.resource.Participant;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Paul Tyson <paul.tyson@oberontech.com>
 */
public class UsersWithPosition extends AbstractCodelet {

	public UsersWithPosition() {
		super();
		setDescription("This codelet gets the userids of the users with specified position name.<br> "
				+ "Input: positionname (string type).<br>"
				+ "Output: userids (string type, comma-separated list of userids)");
	}

	public Element execute(Element inData, List<YParameter> inParams, List<YParameter> outParams)
			throws CodeletExecutionException {
		ResourceManager rm = ResourceManager.getInstance();
		setInputs(inData, inParams, outParams);
		String positionName = getValue("positionname");
		Set<Participant> participants = Optional
				.ofNullable(rm.getOrgDataSet().getParticipantsWithPosition(positionName))
				.orElse(Collections.emptySet());
		String userids = participants.stream().map(p -> p.getUserID()).collect(Collectors.joining(","));
		setParameterValue("userids", userids);
		return getOutputData();
	}

	public List<YParameter> getRequiredParams() {
		List<YParameter> params = new ArrayList<YParameter>();

		YParameter param = new YParameter(null, YParameter._INPUT_PARAM_TYPE);
		param.setDataTypeAndName("string", "positionname", XSD_NAMESPACE);
		param.setDocumentation("The name of a position.");
		params.add(param);

		param = new YParameter(null, YParameter._OUTPUT_PARAM_TYPE);
		param.setDataTypeAndName("string", "userids", XSD_NAMESPACE);
		param.setDocumentation("Comma-separated list of user ids having this position.");
		params.add(param);

		return params;
	}

}
