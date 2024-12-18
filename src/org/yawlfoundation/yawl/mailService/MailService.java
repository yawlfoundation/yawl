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

import org.jdom2.Element;
import org.simplejavamail.email.Email;
import org.simplejavamail.mailer.Mailer;
import org.simplejavamail.mailer.config.TransportStrategy;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceBWebsideController;
import org.yawlfoundation.yawl.util.MailSettings;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.mail.Message;
import java.io.IOException;

/**
 * A service that provides for emails to be sent by tasks
 *
 * @author Michael Adams
 * @date 25/07/2009
 */

public class MailService extends InterfaceBWebsideController {

    // holds a session handle to the engine
    private String _handle = null;

    private static MailService _instance;
    private final MailSettings _defaults = new MailSettings();


    private MailService() { }

    public static MailService getInstance() {
        if (_instance == null) _instance = new MailService();
        return _instance;
    }


    public void handleEnabledWorkItemEvent(WorkItemRecord wir) {
        try {

            // connect only if not already connected
            if (! connected()) _handle = connect(engineLogonName, engineLogonPassword);

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
        YParameter[] params = new YParameter[14];
        params[0] = createParameter(YParameter._INPUT_PARAM_TYPE, "string",
                "senderName", "The name of the person or system who is sending the email", false);
        params[1] = createParameter(YParameter._INPUT_PARAM_TYPE, "string",
                "senderAddress", "The email address of the person or system who is sending the email", false);
        params[2] = createParameter(YParameter._INPUT_PARAM_TYPE, "string",
                "recipientName", "The name of the person to send the email to", true);
        params[3] = createParameter(YParameter._INPUT_PARAM_TYPE, "string",
                "recipientAddress", "The email address to send the email to", false);
        params[4] = createParameter(YParameter._INPUT_PARAM_TYPE, "string",
                "CC", "The email address to CC the email to", true);
        params[5] = createParameter(YParameter._INPUT_PARAM_TYPE, "string",
                "BCC", "The email address to BCC the email to", true);
        params[6] = createParameter(YParameter._INPUT_PARAM_TYPE, "string", "subject",
                "The subject of the email", false);
        params[7] = createParameter(YParameter._INPUT_PARAM_TYPE, "string", "content",
                "The content of the email", false);
        params[8] = createParameter(YParameter._INPUT_PARAM_TYPE, "string", "host",
                "The host mail server url (e.g. smtp.example.com)", true);
        params[9] = createParameter(YParameter._INPUT_PARAM_TYPE, "int", "port",
                "The host email server's smtp port number", true);
        params[10] = createParameter(YParameter._INPUT_PARAM_TYPE, "string", "user",
                "The user name of an account on the host email server", true);
        params[11] = createParameter(YParameter._INPUT_PARAM_TYPE, "string", "password",
                "The password of the account on the host email server", true);
        params[12] = createParameter(YParameter._INPUT_PARAM_TYPE, "string", "transportStrategy",
                "The encryption required to use the host. Choose between PLAIN, SSL and TLS", true);
        params[13] = createParameter(YParameter._OUTPUT_PARAM_TYPE, "string", "result",
                "The success or error message returned", false);
        return params;
    }

    protected void setHost(String host) { _defaults.host = host; }

    protected void setPort(int port) { if (port > -1) _defaults.port = port; }

    protected void setTransportStrategy(String strategy) {
        _defaults.strategy = getTransportStrategy(strategy);
    }

    protected void setUser(String user) { _defaults.user = user; }

    protected void setPassword(String password) { _defaults.password = password; }

    protected void setFromName(String name) { _defaults.fromName = name; }

    protected void setFromAddress(String address) { _defaults.fromAddress = address; }


    protected String sendMail(String toName, String toAddress, String ccAddress,
                              String bccAddress, String subject, String content) {

        // set settings from mix of defaults and above params
        MailSettings settings = _defaults.copyOf();
        settings.toName = toName;
        settings.toAddress = toAddress;
        settings.ccAddress = ccAddress;
        settings.bccAddress = bccAddress;
        settings.subject = subject;
        settings.content = content;
        
        return sendMail(settings);
    }


    protected String sendMail(String settingsAsXml) {
        MailSettings settings = new MailSettings();
        settings.fromXML(settingsAsXml);
        if (settings.host == null) settings.host = _defaults.host;
        if (settings.port < 25) settings.port = _defaults.port;
        if (settings.strategy == null) settings.strategy = _defaults.strategy;
        if (settings.user == null) settings.user = _defaults.user;
        if (settings.password == null) settings.password = _defaults.password;
        if (settings.fromName == null) settings.fromName = _defaults.fromName;;
        if (settings.fromAddress == null) settings.fromAddress = _defaults.fromAddress;

        return sendMail(settings);
    }


    //********************* PRIVATE METHODS *************************************//

    private String sendMail(WorkItemRecord wir) {
        MailSettings settings;
        try {
            settings = buildSettings(wir);
        }
        catch (MailSettingsException mse) {
            return mse.getMessage();
        }

        return sendMail(settings);
    }


    private String sendMail(MailSettings settings) {
        return sendMail(buildEmail(settings), settings);
    }


    private String sendMail(Email email, MailSettings settings) {
        try {
            new Mailer(settings.host, settings.port, settings.user,
                    settings.password, settings.strategy)
                    .sendMail(email);
            return "Mail successfully sent.";
        }
        catch (Exception e) {
            _logger.error("Error sending mail.", e.getCause());
            return e.getMessage();
        }
    }


    private MailSettings buildSettings(WorkItemRecord wir) throws MailSettingsException {
        if (wir == null) throw new MailSettingsException("Work item is null.");
        Element data = wir.getDataList();
        if (data == null) throw new MailSettingsException("Work item contains no data.");
        MailSettings settings = new MailSettings();
        settings.host = getSetting(data, "host");
        settings.port = getPort(data);
        settings.strategy = getTransportStrategy(data);
        settings.user = getSetting(data, "user");
        settings.password = getSetting(data, "password");
        settings.fromName = getSetting(data, "senderName");
        settings.fromAddress = getSetting(data, "senderAddress");
        settings.toName = getSetting(data, "recipientName", true);
        settings.toAddress = getSetting(data, "recipientAddress");
        settings.ccAddress = getSetting(data, "CC", true);
        settings.bccAddress = getSetting(data, "BCC", true);
        settings.subject = getSetting(data, "subject");
        settings.content = getSetting(data, "content");
        return settings;
    }


    private Email buildEmail(MailSettings settings) {
        Email email = new Email();
        addRecipients(email, settings);
        email.setFromAddress(settings.fromName, settings.fromAddress);
        email.setSubject(settings.subject);
        if (settings.content.contains("<")) {
            email.setTextHTML(settings.content);
        }
        else {
            email.setText(settings.content);            // plain text
        }
        return email;
    }


    private void addRecipients(Email email, MailSettings settings) {
        addRecipients(email, settings.toName, settings.toAddress, Message.RecipientType.TO);
        addRecipients(email, null, settings.ccAddress, Message.RecipientType.CC);
        addRecipients(email, null, settings.bccAddress, Message.RecipientType.BCC);
    }


    private void addRecipients(Email email, String name, String address,
                              Message.RecipientType mailType) {
        if (! StringUtil.isNullOrEmpty(address)) {
            if (name == null) name = "";
            email.addRecipients(name, mailType, address);
        }
    }


    // settings not optional by default
    private String getSetting(Element data, String name) throws MailSettingsException {
        return getSetting(data, name, false);
    }


    private String getSetting(Element data, String name, boolean optional)
            throws MailSettingsException {
        String setting = getDataValue(data, name);
        if (StringUtil.isNullOrEmpty(setting)) setting = _defaults.getSetting(name);
        if (StringUtil.isNullOrEmpty(setting) && ! optional) throw new MailSettingsException(
                "Required value for '" + name + "' not supplied.");
        return setting;
    }


    private int getPort(Element data) throws MailSettingsException {
        int port = StringUtil.strToInt(getDataValue(data, "port"), -1);
        if (port < 0) port = _defaults.port;
        if (port < 0) throw new MailSettingsException("Invalid port value.");
        return port;
    }

    private TransportStrategy getTransportStrategy(String strategyString) {
        if (StringUtil.isNullOrEmpty(strategyString)) return _defaults.strategy;
        if ("PLAIN".equalsIgnoreCase(strategyString)) return TransportStrategy.SMTP_PLAIN;
        if ("SSL".equalsIgnoreCase(strategyString)) return TransportStrategy.SMTP_SSL;
        if ("TLS".equalsIgnoreCase(strategyString)) return TransportStrategy.SMTP_TLS;
        
        _logger.error("Unknown transport strategy ('" + strategyString + "'). " +
                "Fall back to default (SSL).");
        return null; //defaults.strategy;
    }

    private TransportStrategy getTransportStrategy(Element data) {
        return getTransportStrategy(getDataValue(data, "transportStrategy"));
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


    private static class MailSettingsException extends Exception {
        MailSettingsException(String msg) { super(msg); }
    }

}





