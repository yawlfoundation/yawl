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

package org.yawlfoundation.yawl.mailService;

import org.yawlfoundation.yawl.engine.interfce.Interface_Client;
import org.yawlfoundation.yawl.util.MailSettings;
import org.yawlfoundation.yawl.util.PasswordEncryptor;

import java.io.IOException;
import java.util.Map;

/**
 * An API to be used by clients that want to send email via the mail service.
 *
 *  @author Michael Adams
 *  11/12/2024
 */

public class MailServiceClient extends Interface_Client {

    private static final String DEFAULT_URI = "http://localhost:8080/mailService/gateway";

    protected String _msURI;                   // the uri of the Mail Service Gateway

    /**
     * Constructor
     * @param uri the uri of the Mail Service Gateway
     */
    public MailServiceClient(String uri) {
        _msURI = uri ;
    }

    /**
     * Constructor - uses default uri on localhost
     */
    public MailServiceClient() {
        this(DEFAULT_URI);
    }


    /**
     * Sets the uri of the Mail Service Gateway
     * @param uri the uri to set
     */
    public void setURI(String uri)  { _msURI = uri; }


    /*******************************************************************************/

    /**
     * Connects an external entity to the mail service
     * @param userID the userid
     * @param password the corresponding password
     * @return a session handle if successful, or a failure message if not
     * @throws IOException if the service can't be reached
     */
    public String connect(String userID, String password) throws IOException {
        Map<String, String> params = prepareParamMap("connect", null);
        params.put("userid", userID);
        params.put("password", PasswordEncryptor.encrypt(password, null));
        return executeGet(_msURI, params) ;
    }


    /**
     * Check that a session handle is active
     * @param handle the session handle to check
     * @return "true" if the id is valid, "false" if otherwise
     * @throws IOException if the service can't be reached
     */
    public String checkConnection(String handle) throws IOException {
        return executeGet(_msURI, prepareParamMap("checkConnection", handle)) ;
    }


    /**
     * Disconnects an external entity from the Mail service
     * @param handle the session handle to disconnect
     * @throws IOException if the service can't be reached
     */
    public void disconnect(String handle) throws IOException {
        executePost(_msURI, prepareParamMap("disconnect", handle));
    }


    public String sendMail(String toAddress, String subject, String content, String handle)
            throws IOException {
        return sendMail(toAddress, subject, content,
                null, null, null, handle);
    }


    public String sendMail(String toAddress, String subject, String content,
                           String toName, String handle) throws IOException {
        return sendMail(toAddress, subject, content,
                toName, null, null, handle);
    }


    public String sendMail(String toAddress, String subject, String content,
                           String toName, String ccAddress, String bccAddress, String handle)
            throws IOException {
        Map<String, String> params = prepareParamMap("sendMail", handle);
        params.put("toAddress", toAddress);
        params.put("subject", subject);
        params.put("content", content);
        params.put("toName", toName);
        params.put("ccAddress", ccAddress);
        params.put("bccAddress", bccAddress);
        return executePost(_msURI, params) ;
    }


    public String sendMail(MailSettings settings, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("sendMail", handle);
           params.put("xml", settings.toXML());
           return executePost(_msURI, params) ;
    }


    public String setHost(String host, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("setHost", handle);
        params.put("host", host);
        return executePost(_msURI, params) ;
    }


    public String setPort(int port, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("setPort", handle);
        params.put("port", String.valueOf(port));
        return executePost(_msURI, params) ;
    }


    public String setSenderUserid(String userid, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("setSenderUserid", handle);
        params.put("userid", userid);
        return executePost(_msURI, params) ;
    }


    public String setSenderPassword(String password, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("setSenderPassword", handle);
        params.put("password", password);
        return executePost(_msURI, params) ;
    }


    public String setSenderName(String name, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("setSenderName", handle);
        params.put("name", name);
        return executePost(_msURI, params) ;
    }


    public String setSenderAddress(String address, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("setSenderAddress", handle);
        params.put("address", address);
        return executePost(_msURI, params) ;
    }


    public String setTransportStrategy(String strategy, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("setTransportStrategy", handle);
        params.put("strategy", strategy);
        return executePost(_msURI, params) ;
    }

}