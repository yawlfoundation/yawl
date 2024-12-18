package org.yawlfoundation.yawl.resourcing.util;

import com.courier.api.Courier;
import com.courier.api.requests.SendMessageRequest;
import com.courier.api.resources.send.types.*;
import com.courier.api.types.SendMessageResponse;
import com.mailersend.sdk.MailerSend;
import com.mailersend.sdk.MailerSendResponse;
import com.mailersend.sdk.emails.Email;
import com.mailersend.sdk.exceptions.MailerSendException;
import org.simplejavamail.mailer.config.TransportStrategy;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.resourcing.WorkQueue;
import org.yawlfoundation.yawl.resourcing.client.MailClient;
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.util.MailSettings;
import org.yawlfoundation.yawl.util.StringUtil;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Michael Adams
 * @date 14/12/2024
 */
public class Emailer {

    public enum Provider { Courier, MailerSend, Custom, None }

    private Provider _provider = Provider.None;
    private String _uid;
    private Properties _props = new Properties();
    private static Emailer INSTANCE;

    private Emailer() { }

    
    public static Emailer getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Emailer();
        }
        return INSTANCE;
    }


    public void loadProperties(InputStream stream) throws IOException {
        try {
            _props.load(stream);

            setProvider(_props.getProperty("provider"));
        }
        catch (Exception e) {
            throw new IOException("Failed to load mail send properties");
        }
    }


    public void sendNotification(MailClient mailer, Participant p,
                                 WorkItemRecord wir, int queue) throws IOException {
        if (StringUtil.isNullOrEmpty(p.getEmail())) {
           throw new IOException("No email address defined for participant");
        }
 //       new Thread(new Runnable() {
 //           @Override
 //           public void run() {

                Map<String, Object> data = mapData(p, wir, queue);
                try {
                    switch (_provider) {
                        case Courier:
                            sendViaCourier(p.getEmail(), data); break;
                        case MailerSend:
                            sendViaMailer(p.getEmail(), data); break;
                        case Custom:
                            sendViaCustom(mailer, p, data);
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
 //           }
 //       }).start();
    }

    
    public void sendViaCustom(MailClient mailer, Participant p, Map<String, Object> data)
            throws IOException {
        MailSettings settings = new MailSettings();
        settings.host = _props.getProperty("host");
        settings.port = StringUtil.strToInt(_props.getProperty("port"), 25);

        String strategy = _props.getProperty("transportStrategy");
        if (strategy != null) {
            settings.strategy = TransportStrategy.valueOf(strategy);
        }

        settings.user = _props.getProperty("user");
        settings.password = _props.getProperty("password");
        settings.fromName = _props.getProperty("fromName");
        settings.fromAddress = _props.getProperty("fromAddress");
        settings.toName = p.getFullName();
        settings.toAddress = p.getEmail();
        settings.subject = "YAWL Worklist Notification";
        settings.content = parseContent(data);

        mailer.sendMail(settings, mailer.getHandle());
    }


    private void setProvider(String providerStr) {
        if (StringUtil.isNullOrEmpty(providerStr)) {
            _provider = Provider.None;
        }
        else if (providerStr.equalsIgnoreCase("Courier")) {
            _provider = Provider.Courier;
        }
        else if (providerStr.equalsIgnoreCase("Mailer")) {
            _provider = Provider.MailerSend;
        }
        else if (providerStr.equalsIgnoreCase("Custom")) {
            _provider = Provider.Custom;
        }
    }

    private String getToken() {
        String uid = _props.getProperty("uid");
        if (StringUtil.isNullOrEmpty(uid)) {
            return uid;
        }
        else {
            switch (_provider) {
                case Courier:
                    return "pk_prod_" + uid;
                case MailerSend:
                    return "mlsn." + uid;
                default:
                    return uid;
            }
        }
    }

    private void sendViaCourier(String emailAddr, Map<String, Object> dataMap) {

        Courier courier = Courier.builder().authorizationToken(getToken()).build();

        MessageRecipient recipient = MessageRecipient.of(Recipient.of(
                UserRecipient.builder().email(emailAddr).build()));

        SendMessageResponse resp = courier.send(SendMessageRequest.builder()
                .message(Message.of(
                        TemplateMessage.builder()
                                .template("88MQ4MED5N4ZKTK0K7HJZ3F14X03")
                                .data(dataMap)
                                .to(recipient).build()
                )).build());

        resp.toString();
    }


    private void sendViaMailer(String emailAddr, Map<String, Object> dataMap) throws IOException {
        String fromAddress = _props.getProperty("fromAddress");
        if (fromAddress == null) {
            throw new IOException("Missing 'from address' in properties");
        }

        Email email = new Email();
        email.setFrom("yawl.notifications", fromAddress);
        email.addRecipient(String.valueOf(dataMap.get("fullName")), emailAddr);
        email.setSubject("YAWL Worklist Notification");
        email.setHtml(parseContent(dataMap));

        try {
            MailerSend sender = new MailerSend();
            sender.setToken(getToken());
            MailerSendResponse response = sender.emails().send(email);
        }
        catch (MailerSendException e) {
           e.printStackTrace();
        }
    }


    private Map<String, Object> mapData(Participant p, WorkItemRecord wir, int queue) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", wir.getID());
        map.put("caseid", wir.getCaseID());
        map.put("taskid", wir.getTaskID());
        map.put("specName", wir.getSpecURI());
        map.put("specVersion", wir.getSpecVersion());
        map.put("enabledTime", wir.getEnablementTime());
        map.put("status", queue == WorkQueue.ALLOCATED ? "allocated" : "offered");
        map.put("doco", wir.getDocumentation());
        map.put("netid", wir.getNetID());
        map.put("expires", wir.getTimerExpiry());
        map.put("firstName", p.getFirstName());
        map.put("lastName", p.getLastName());
        map.put("fullName", p.getFullName());
        map.put("offeredCount", String.valueOf(p.getWorkQueues().getQueueSize(WorkQueue.OFFERED)));
        map.put("allocatedCount", String.valueOf(p.getWorkQueues().getQueueSize(WorkQueue.ALLOCATED)));
        map.put("startedCount", String.valueOf(p.getWorkQueues().getQueueSize(WorkQueue.STARTED)));
        map.put("suspendedCount", String.valueOf(p.getWorkQueues().getQueueSize(WorkQueue.SUSPENDED)));
        return map;
    }


    private String parseContent(Map<String, Object> dataMap) {
        String content = _props.getProperty("content");
        for (String key : dataMap.keySet()) {
            String param = "\\{" + key + "}";
            content = content.replaceAll(param, String.valueOf(dataMap.get(key)));
        }
        return content;
    }

    
    private String dateTimeString(long time) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(time);
    }
}
