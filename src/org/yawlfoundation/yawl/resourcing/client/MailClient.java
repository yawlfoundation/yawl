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

package org.yawlfoundation.yawl.resourcing.client;

import org.yawlfoundation.yawl.mailService.MailServiceClient;
import org.yawlfoundation.yawl.util.StringUtil;

import java.io.IOException;

/**
 * @author Michael Adams
 * @date 9/11/11
 */
public class MailClient extends MailServiceClient {

    private String engineLogonName;
    private String engineLogonPassword;
    private String handle;

    public MailClient() { }

    public MailClient(String uri, String userid, String password) {
        setURI(uri);
        engineLogonName = userid;
        engineLogonPassword = password;
    }


    public String getHandle() throws IOException {
        if (handle != null) {
            if (checkHandle(handle)) {
                return handle;
            }
        }
        handle = connect(engineLogonName, engineLogonPassword);
        if (successful(handle)) {
            return handle;
        }
        else throw new IOException("Error establishing session handle: " +
                StringUtil.unwrap(handle));
    }


    private boolean checkHandle(String handle) throws IOException {
        return checkConnection(handle).equalsIgnoreCase("true");
    }

}
