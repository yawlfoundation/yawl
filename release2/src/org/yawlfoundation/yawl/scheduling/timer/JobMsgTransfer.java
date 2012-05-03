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

import org.apache.log4j.Logger;
import org.jdom2.Element;
import org.yawlfoundation.yawl.scheduling.Constants;
import org.yawlfoundation.yawl.scheduling.SchedulingService;
import org.yawlfoundation.yawl.scheduling.util.PropertyReader;
import org.yawlfoundation.yawl.scheduling.util.Utils;

import java.util.Date;


/**
 * periodische OP-Plan-Aktualisierung: min�tliche �berpr�fung, die ergibt, dass
 * aktuelle utilisierung in andere reservierung hineinl�uft
 * 
 * @author tbe
 * @version $Id$
 *
 */
public class JobMsgTransfer implements Runnable, Constants {

    private String name;
    private Element msgTransfer;
    private String caseId;
    private Date startTime;

    public JobMsgTransfer(int i, Element msgTransfer, String caseId, Date startTime) {
        this.name = "MsgTransfer-" + i;
        this.msgTransfer = msgTransfer;
        this.caseId = caseId;
        this.startTime = startTime;
    }

    public void run() {
        try {
            String msgTO = msgTransfer.getChildText(XML_MSGTO);
            String msgBODY = msgTransfer.getChildText(XML_MSGBODY) + ", Time: " +
                    Utils.date2String(startTime, Utils.DATETIME_PATTERN);
            String address = PropertyReader.getInstance()
                .getSchedulingProperty(msgTO + ".Address");
            String addressType = PropertyReader.getInstance()
                .getSchedulingProperty(msgTO + ".AddressType");
            SchedulingService.getInstance().sendPushMessage(address, addressType, msgBODY, caseId);
        }
        catch (Exception e) {
            Logger.getLogger(JobMsgTransfer.class).error("Cannot execute job " + name, e);
        }
    }

    
}
