/*
 * Copyright (c) 2004-2015 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.worklet.client;

import org.yawlfoundation.yawl.editor.core.connection.YConnection;
import org.yawlfoundation.yawl.engine.interfce.Interface_Client;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;
import org.yawlfoundation.yawl.worklet.support.WorkletGatewayClient;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Vector;

/**
 * @author Michael Adams
 * @date 6/03/15
 */
public class WorkletClient extends YConnection {

    private static final String DEFAULT_USERID = "editor";
    private static final String DEFAULT_PASSWORD = "yEditor";
    private static final String DEFAULT_URL = "http://localhost:8080/workletService/gateway";

    private String _userid = DEFAULT_USERID;
    private String _password = DEFAULT_PASSWORD;
    private WorkletGatewayClient _client;


    public WorkletClient() {
        super();
        try {
            setURL(DEFAULT_URL);
        }
        catch (Exception e) {}
    }

    public WorkletClient(String host, int port) throws MalformedURLException {
        super();
        setURL(new URL("http", host, port, "workletService/gateway"));
    }

    public WorkletClient(String urlStr) { super(urlStr); }

    public WorkletClient(URL url) { super(url); }



    @Override
    protected void init() { _client = new WorkletGatewayClient(); }

    @Override
    protected String getURLFilePath() {
        return null;
    }

    @Override
    protected Interface_Client getClient() { return _client; }



    public void setUserID(String id) {
        if (id != null) {
            _userid = id;
            disconnect();
        }
    }


    public void setPassword(String pw) {
        if (pw != null) {
            _password = pw;
            disconnect();
        }
    }


    public void disconnect() {
        if (_handle != null) {
            try {
                _client.disconnect(_handle);
            }
            catch (IOException ioe) {
                //
            }
        }
        _handle = null;
    }


    protected boolean connect(Interface_Client client) throws IOException {
        super.connect(client);
        if (_handle == null) {
            _handle = _client.connect(_userid, _password);
            if (! client.successful(_handle)) {
                _handle = null;
                return false;
            }
        }
        return true;
    }


    public boolean isConnected(Interface_Client client) {
        try {
            if (_handle == null) {
                return connect(client);
            }
            else {
                boolean success = _client.successful(_client.checkConnection(_handle));
                if (! success) _handle = null;
                return success;
            }
        }
        catch (IOException ioe) {
            return false;
        }
    }


    protected boolean isConnected() { return isConnected(_client); }


    /********************************************************************************/

    public Vector<String> getWorkletList() throws IOException {
        Vector<String> names = new Vector<String>();
        if (isConnected()) {
            String xml = _client.getWorkletNames(false, _handle);
            if (_client.successful(xml)) {
                XNode node = new XNodeParser().parse(xml);
                for (XNode child : node.getChildren()) {
                    names.add(child.getText());
                }
            }
            Collections.sort(names);
        }
        return names;
    }


}
