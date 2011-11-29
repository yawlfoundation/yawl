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

package org.yawlfoundation.yawl.resourcing.client;

import org.yawlfoundation.yawl.documentStore.DocumentStoreClient;

import java.io.IOException;

/**
 * @author Michael Adams
 * @date 9/11/11
 */
public class DocStoreClient extends DocumentStoreClient {

    private String engineLogonName;
    private String engineLogonPassword;
    private String handle;

    public DocStoreClient() { }

    public DocStoreClient(String uri, String userid, String password) {
        setURI(uri);
        engineLogonName = userid;
        engineLogonPassword = password;
    }
    
    public String getHandle() throws IOException {
        if (handle != null) {
            if (checkConnection(handle)) {
                return handle;
            }
        }
        handle = connect(engineLogonName, engineLogonPassword);
        if (successful(handle)) {
            return handle;
        }
        else throw new IOException("Error establishing session handle");
    }

}
