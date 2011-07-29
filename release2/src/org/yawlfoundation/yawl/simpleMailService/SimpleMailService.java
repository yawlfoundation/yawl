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

package org.yawlfoundation.yawl.simpleMailService;

import org.codemonkey.simplejavamail.Email;
import org.codemonkey.simplejavamail.MailException;
import org.codemonkey.simplejavamail.Mailer;
import org.jdom.Element;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceBWebsideController;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.mail.Message;
import java.io.IOException;

/**
 * A simple service that provides for status updates to the YAWL Twitter account
 *
 * @author Michael Adams
 * @date 25/07/2009
 */

public class SimpleMailService extends InterfaceBWebsideController {

    // holds a session handle to the engine
    private String _handle = null;

    private static SimpleMailService _instance;

    private final String _engineUser = "mailService";
    private final String _enginePassword = "yMail";

    private MailSettings _defaults = new MailSettings();


    private SimpleMailService() { }

    public static SimpleMailService getInstance() {
        if (_instance == null) _instance = new SimpleMailService();
        return _instance;
    }


    public void handleEnabledWorkItemEvent(WorkItemRecord wir) {
        try {

            // connect only if not already connected
            if (! connected()) _handle = connect(_engineUser, _enginePassword);

            // checkout ... process ... checkin
            wir = checkOut(wir.getID(), _handle);
            String result = sendMail(wir);
            checkInWorkItem(wir.getID(), wir.getDataList(),
                            getOutputData(wir.getTaskID(), result), null,  _handle);
        }
        catch (Exception ioe) {
            ioe.printStackTrace();
        }
    }

    // have to implement abstract method, but have no need for this event
    public void handleCancelledWorkItemEvent(WorkItemRecord workItemRecord) {  }


    // these parameters are automatically inserted (in the Editor) into a task
    // decomposition when this service is selected from the list
    public YParameter[] describeRequiredParams() {
        YParameter[] params = new YParameter[9];
        params[0] = createParameter(YParameter._INPUT_PARAM_TYPE, "string",
                "recipientName", "The name of the person to send the email to", false);
        params[1] = createParameter(YParameter._INPUT_PARAM_TYPE, "string",
                "recipientAddress", "The email address to send the email to", false);
        params[2] = createParameter(YParameter._INPUT_PARAM_TYPE, "string", "subject",
                "The subject of the email", false);
        params[3] = createParameter(YParameter._INPUT_PARAM_TYPE, "string", "content",
                "The content of the email", false);
        params[4] = createParameter(YParameter._INPUT_PARAM_TYPE, "string", "host",
                "The host mail server url (e.g. smtp.example.com)", true);
        params[5] = createParameter(YParameter._INPUT_PARAM_TYPE, "int", "port",
                "The host email server's smtp port number", true);
        params[6] = createParameter(YParameter._INPUT_PARAM_TYPE, "string", "user",
                "The user name of an account on the host email server", true);
        params[7] = createParameter(YParameter._INPUT_PARAM_TYPE, "string", "password",
                "The password of the account on the host email server", true);
        params[8] = createParameter(YParameter._OUTPUT_PARAM_TYPE, "string", "result",
                "The success or error message returned", false);
        return params;
    }

    protected void setHost(String host) { _defaults.host = host; }

    protected void setPort(int port) { if (port > -1) _defaults.port = port; }

    protected void setUser(String user) { _defaults.user = user; }

    protected void setPassword(String password) { _defaults.password = password; }

    protected void setFromName(String name) { _defaults.fromName = name; }

    protected void setFromAddress(String address) { _defaults.fromAddress = address; }

    
    //********************* PRIVATE METHODS *************************************//

    private String sendMail(WorkItemRecord wir) {
        MailSettings settings;
        try {
            settings = buildSettings(wir);
        }
        catch (MailSettingsException mse) {
            return mse.getMessage();
        }

        Email email = buildEmail(settings);
        return sendMail(email, settings);
    }


    private String sendMail(Email email, MailSettings settings) {
        try {
            new Mailer(settings.host, settings.port, settings.user, settings.password)
                    .sendMail(email);
            return "Mail successfully sent.";
        }
        catch (MailException me) {
            return me.getMessage();
        }
    }


    private MailSettings buildSettings(WorkItemRecord wir) throws MailSettingsException {
        if (wir == null) throw new MailSettingsException("Work item is null.");
        Element data = wir.getDataList();
        if (data == null) throw new MailSettingsException("Work item contains no data.");
        MailSettings settings = new MailSettings();
        settings.host = getSetting(data, "host");
        settings.port = getPort(data);
        settings.user = getSetting(data, "user");
        settings.password = getSetting(data, "password");
        settings.fromName = getSetting(data, "senderName");
        settings.fromAddress = getSetting(data, "senderAddress");
        settings.toName = getSetting(data, "recipientName");
        settings.toAddress = getSetting(data, "recipientAddress");
        settings.subject = getSetting(data, "subject");
        settings.content = getSetting(data, "content");
        return settings;
    }


    private Email buildEmail(MailSettings settings) {
        Email email = new Email();
        email.setFromAddress(settings.fromName, settings.fromAddress);
        email.addRecipient(settings.toName, settings.toAddress, Message.RecipientType.TO);
        email.setSubject(settings.subject);
        email.setText(settings.content);
        return email;
    }


    private String getSetting(Element data, String name) throws MailSettingsException {
        String setting = getDataValue(data, name);
        if (StringUtil.isNullOrEmpty(setting)) setting = _defaults.getSetting(name);
        if (StringUtil.isNullOrEmpty(setting)) throw new MailSettingsException(
                "No value for '" + name + "' supplied.");
        return setting;
    }


    private int getPort(Element data) throws MailSettingsException {
        int port = StringUtil.strToInt(getDataValue(data, "port"), -1);
        if (port < 0) port = _defaults.port;
        if (port < 0) throw new MailSettingsException("Invalid port value.");
        return port;
    }


    private String getDataValue(Element data, String name) {
        return (data != null) ? data.getChildText(name) : null;
    }


    private Element getOutputData(String taskName, String data) {
        Element output = new Element(taskName);
        Element result = new Element("result");
        result.setText(data);
        output.addContent(result);
        return output;
    }

    
    private boolean connected() throws IOException {
        return _handle != null && checkConnection(_handle);
    }


    private YParameter createParameter(int IorO, String type, String name, String doco,
                                       boolean optional) {
        YParameter param = new YParameter(null, IorO);
        param.setDataTypeAndName(type, name, XSD_NAMESPACE);
        param.setDocumentation(doco);
        param.setOptional(optional);
        return param;
    }


    private class MailSettings {
        String host = null;
        int port = 25;
        String user = null;
        String password = null;
        String fromName = null;
        String fromAddress = null;
        String toName = null;
        String toAddress = null;
        String subject = null;
        String content = null;

        String getSetting(String name) {
            if (name.equals("host")) return host;
            if (name.equals("user")) return user;
            if (name.equals("password")) return password;
            if (name.equals("senderName")) return fromName;
            if (name.equals("senderAddress")) return fromAddress;
            if (name.equals("recipientName")) return toName;
            if (name.equals("recipientAddress")) return toAddress;
            if (name.equals("subject")) return subject;
            if (name.equals("content")) return content;
            return null;
        }
    }


    private class MailSettingsException extends Exception {
        MailSettingsException(String msg) { super(msg); }
    }

}





