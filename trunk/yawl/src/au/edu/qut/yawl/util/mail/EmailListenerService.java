package au.edu.qut.yawl.util.mail;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.exceptions.YAWLException;

/**
 * .
 *
 * @author Felix L J Mayer
 * @version $Revision: 1.21 $
 * @created Dec 21, 2004
 * @jmx:mbean name="capsela:service=EmailListenerService"
 * extends="org.jboss.system.ServiceMBean"
 */
public class EmailListenerService
/*extends ServiceMBeanSupport implements EmailListenerServiceMBean*/ {
	// FIXME: XXX TODO this class needs to extend the appropriate MBean class

    private final static Log LOG = LogFactory.getLog(EmailListenerService.class);
    
    public static final String SUBJECT_PREFIX = "RUN[";
    public static final String SUBJECT_POSTFIX = "]";
    
    private Thread _listenerThread = null;
    private EmailListener _emailListener = null;
    
    /**
     * .
     *
     * @param args
     * @throws Exception
     */
    static public void main(String[] args) throws Exception {
        EmailListenerService emailService = new EmailListenerService();
        emailService.startService();
    }//main()

    /**
     * .
     */
    public EmailListenerService() {
        super();
    }//EmailListenerService()

    /**
     * Create the service, do expensive operations etc.
     *
     * @throws Exception
     * @see org.jboss.system.ServiceMBeanSupport#createService()
     * @see org.jboss.system.Service#create()
     */
    protected void createService() throws Exception {
        LOG.debug("createService()");
    }//createService()

    /**
     * Start the service, create is already called.
     *
     * @throws Exception
     * @see org.jboss.system.ServiceMBeanSupport#startService()
     * @see org.jboss.system.Service#start()
     */
    protected void startService() throws Exception {
    	// TODO do we have a development mode in YAWL?
//        if (Helper.isDevelopmentMode()) {
//            LOG.warn("Cannot start email listener service because we are in development mode!");
//            return;
//        }
    	// TODO do we need to not start under certain circumstances in YAWL like in Capsela?
//        if (!EngineManager.isStarted()) {
//            LOG.warn("Cannot start when EngineManager is not started.");
//            return;
//        }//if
//        else {
            LOG.debug("startService()");
//        }//else
//        final EmailSender emailSender = new EmailSender();
        // TODO would rather have it the same here and in SchedulerService
        _emailListener = new EmailListener() {
            protected boolean processMessage(Message message) throws YAWLException {
                String from = null;
                String subject = null;
                String body = null;
                boolean processed = false;
                try {
                    subject = message.getSubject().trim();
                    if (!subject.endsWith(SUBJECT_POSTFIX) ||
                            !(subject.startsWith(SUBJECT_PREFIX.toLowerCase())
                            || subject.startsWith(SUBJECT_PREFIX.toUpperCase()))) {
                        // The message is not for us, so we don't process it.
                        return false;
                    }//if
                    from = "<unknown>";
                    if (message.getFrom() != null) {
                        StringBuffer buffer = new StringBuffer();
                        Address[] addresses = message.getFrom();
                        for (int i = 0; i < addresses.length; i++) {
                            buffer.append(addresses[i].toString().trim());
                            if (i < addresses.length - 1) buffer.append(";");
                        }//for
                        from = buffer.toString();
                    }//if
                    processed = true;
                    // Get body from message.
                    Object content = message.getContent();
                    if (content instanceof Multipart) {
                        body = ((Multipart) content).getBodyPart(0).getContent().toString();
                    }//if
                    else if (content instanceof Part) {
                        body = ((Part) content).getContent().toString();
                    }//else if
                    else if (content instanceof String){
                    	body = (String) content;
                    } else {
                        handleError(from, subject, body,
                                "The message content could not be processed:" + content);
                        return processed;
                    }//else
                    YSpecification specification = getSpecification( subject );
//                    // Load the component.
//                    String componentPath = subject.substring(SUBJECT_PREFIX.length(),
//                            subject.length() - SUBJECT_POSTFIX.length());
//                    Component component = ComponentType.fromPath(componentPath, true);
//                    // Initialize collections to prevent the update needed flag from being
//                    // set by automatic initialization. Could also just set the flag?
//                    if (component != null) PersistenceManager.instance().initialize(component, 0, null);
//                    String[] pathSegments = componentPath.split("/");
//                    if (component == null || pathSegments.length < 1
//                            || !component.getName().equals(pathSegments[pathSegments.length - 1])) {
//                        handleError(from, subject, body, "The component with the path '"
//                                + componentPath + "' could not be found." + Helper.LF
//                                + (component == null ? "" : "The last match was '"
//                                + component.getName() + "'."));
//                        return processed;
//                    }//if
                    // Parse attribute values from message body:
                    // one name/value pair per line separated by '='.
                    Map<String, Object> attributes = new HashMap<String, Object>();
                    String[] pairs = body.split("\n");
                    for (int i = 0; i < pairs.length; i++) {
                        String pair = pairs[i].trim();
                        if (pair.length() == 0) continue;
                        String[] segments = pair.split("=", 2);
                        if (segments.length != 2) {
                            LOG.debug("Invalid name/value pair: " + pairs[i]);
                            continue;
                        }//if
                        String name = segments[0].trim();
                        String value = segments[1].trim();
//                        if (!component.hasAttribute(name)) {
//                            handleError(from, subject, body,
//                                    "The component does not have an attribute named '" + name + "'.");
//                            return processed;
//                        }//if
//                        Class attributeClass = component.attributeClass(name);
//                        if (ComponentAttribute.class.isAssignableFrom(attributeClass)
//                                && attributeClass != ScalarAttribute.class) {
//                            handleError(from, subject, body,
//                                    "The attribute named '" + name + "' is not a scalar.");
//                            return processed;
//                        }//if
//                        Object convertedValue = value;
//                        if (attributeClass != ScalarAttribute.class) {
//                            convertedValue = Helper.convertPrimitive(attributeClass, value);
//                            if (value.length() > 0 && convertedValue == null) {
//                                handleError(from, subject, body,
//                                        "The value '" + value + "' for the attribute '" + name
//                                        + "' cannot be converted to its native type.");
//                                return processed;
//                            }//if
//                        }//if
//                        attributes.put(name, convertedValue);
                        attributes.put(name, value);
                    }//for
                    String attributeErrors = validateAttributes( specification, attributes );
                    if( attributeErrors != null && attributeErrors.length() > 0 ) {
                    	handleError(from, subject, body,
                    			"The following error(s) occured validating the data attributes:\n"
                    			+ attributeErrors );
                    	return processed;
                    }
                    startCase( specification, attributes );
//                    Component instance = EngineManager.instance().createInstance(component);
//                    for (Iterator<Map.Entry<String, Object>> i = attributes.entrySet().iterator(); i.hasNext();) {
//                        Map.Entry<String, Object> entry = i.next();
//                        instance.setAttribute(entry.getKey(), entry.getValue());
//                    }//for
//                    instance.setCallerEmail(from);
//                    PersistenceManager.instance().commit(true);
//                    EngineManager.instance().submitInstance(instance,
//                            Identifier.INVALID_ID, from, subject, body);
                    return processed;
                }//try
                catch (MessagingException e) {
                    handleError(from, subject, body,
                            "A serious error occured when processing your message:\n"
                            + stacktrace(e));
                    return processed;
                }//catch
                catch (IOException e) {
                    handleError(from, subject, body,
                            "A serious error occured when processing your message:\n"
                            + stacktrace(e));
                    return processed;
                }//catch
                catch (Throwable t) {
                    handleError(from, subject, body,
                            "A serious error occured when processing your message:\n"
                            + stacktrace(t));
                    return processed;
                }//catch
            }//processMessage()

            public void handleError(String to, String subject, String body, String message)
                    throws YAWLException {
                LOG.error("ERROR with " + subject + " from " + to + ": " + message
                        + "\nORIGINAL MESSAGE:\n" + body);
                // TODO send an email back with the errors?
//                emailSender.send(to, "ERROR with " + subject, message + Helper.LF +
//                        "ORIGINAL MESSAGE:\n" + body);
            }//handleError()
            
            private YSpecification getSpecification(String subject) {
            	/* TODO empty implementation for now
            	 * should: get the specification indicated by the subject of the email
            	 */
            	return null;
            }
            
            private String validateAttributes( YSpecification spec, Map<String, Object> attributes ) {
            	/* TODO empty implementation for now
            	 * should: check the given attributes against the given spec, and if there are any
            	 * errors return them in the string. The YAWL standard for indicating no errors
            	 * in validation is to return an empty string.
            	 */
            	return null;
            }
            
            private void startCase( YSpecification templateSpec, Map<String, Object> attributes ) {
            	/* TODO empty implementation for now
            	 * should: create a case (instance) from the template specification, set
            	 * the attributes, and start running the case.
            	 */
            }
            
            private String stacktrace( Throwable t ) {
            	StringWriter sw = new StringWriter();
            	PrintWriter pw = new PrintWriter( sw );
            	t.printStackTrace( pw );
            	pw.flush();
            	pw.close();
            	return sw.toString();
            }
            
        };//new EmailListener()
        _listenerThread = new Thread(_emailListener, "EmailListener Service");
        _listenerThread.start();
    }//startService()

    /**
     * Stop the service.
     *
     * @throws Exception
     * @see org.jboss.system.ServiceMBeanSupport#stopService()
     * @see org.jboss.system.Service#stop()
     */
    protected void stopService() throws Exception {
        LOG.debug("stopService()");
        if (_emailListener != null) _emailListener.setRunning(false);
    }//stopService()

    /**
     * Destroy the service, tear down.
     *
     * @throws Exception
     * @see org.jboss.system.ServiceMBeanSupport#destroyService()
     * @see org.jboss.system.Service#destroy()
     */
    protected void destroyService() throws Exception {
        LOG.debug("destroyService()");
    }//destroyService()
}//EmailListenerService
