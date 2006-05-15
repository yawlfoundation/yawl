/*
 *
 *    Artistic License
 *
 *    Preamble
 *
 *    The intent of this document is to state the conditions under which a Package may be copied, such that
 *    the Copyright Holder maintains some semblance of artistic control over the development of the
 *    package, while giving the users of the package the right to use and distribute the Package in a
 *    more-or-less customary fashion, plus the right to make reasonable modifications.
 *
 *    Definitions:
 *
 *    "Package" refers to the collection of files distributed by the Copyright Holder, and derivatives
 *    of that collection of files created through textual modification.
 *
 *    "Standard Version" refers to such a Package if it has not been modified, or has been modified
 *    in accordance with the wishes of the Copyright Holder.
 *
 *    "Copyright Holder" is whoever is named in the copyright or copyrights for the package.
 *
 *    "You" is you, if you're thinking about copying or distributing this Package.
 *
 *    "Reasonable copying fee" is whatever you can justify on the basis of media cost, duplication
 *    charges, time of people involved, and so on. (You will not be required to justify it to the
 *    Copyright Holder, but only to the computing community at large as a market that must bear the
 *    fee.)
 *
 *    "Freely Available" means that no fee is charged for the item itself, though there may be fees
 *    involved in handling the item. It also means that recipients of the item may redistribute it under
 *    the same conditions they received it.
 *
 *    1. You may make and give away verbatim copies of the source form of the Standard Version of this
 *    Package without restriction, provided that you duplicate all of the original copyright notices and
 *    associated disclaimers.
 *
 *    2. You may apply bug fixes, portability fixes and other modifications derived from the Public Domain
 *    or from the Copyright Holder. A Package modified in such a way shall still be considered the
 *    Standard Version.
 *
 *    3. You may otherwise modify your copy of this Package in any way, provided that you insert a
 *    prominent notice in each changed file stating how and when you changed that file, and provided that
 *    you do at least ONE of the following:
 *
 *        a) place your modifications in the Public Domain or otherwise make them Freely
 *        Available, such as by posting said modifications to Usenet or an equivalent medium, or
 *        placing the modifications on a major archive site such as ftp.uu.net, or by allowing the
 *        Copyright Holder to include your modifications in the Standard Version of the Package.
 *
 *        b) use the modified Package only within your corporation or organization.
 *
 *        c) rename any non-standard executables so the names do not conflict with standard
 *        executables, which must also be provided, and provide a separate manual page for each
 *        non-standard executable that clearly documents how it differs from the Standard
 *        Version.
 *
 *        d) make other distribution arrangements with the Copyright Holder.
 *
 *    4. You may distribute the programs of this Package in object code or executable form, provided that
 *    you do at least ONE of the following:
 *
 *        a) distribute a Standard Version of the executables and library files, together with
 *        instructions (in the manual page or equivalent) on where to get the Standard Version.
 *
 *        b) accompany the distribution with the machine-readable source of the Package with
 *        your modifications.
 *
 *        c) accompany any non-standard executables with their corresponding Standard Version
 *        executables, giving the non-standard executables non-standard names, and clearly
 *        documenting the differences in manual pages (or equivalent), together with instructions
 *        on where to get the Standard Version.
 *
 *        d) make other distribution arrangements with the Copyright Holder.
 *
 *    5. You may charge a reasonable copying fee for any distribution of this Package. You may charge
 *    any fee you choose for support of this Package. You may not charge a fee for this Package itself.
 *    However, you may distribute this Package in aggregate with other (possibly commercial) programs as
 *    part of a larger (possibly commercial) software distribution provided that you do not advertise this
 *    Package as a product of your own.
 *
 *    6. The scripts and library files supplied as input to or produced as output from the programs of this
 *    Package do not automatically fall under the copyright of this Package, but belong to whomever
 *    generated them, and may be sold commercially, and may be aggregated with this Package.
 *
 *    7. C or perl subroutines supplied by you and linked into this Package shall not be considered part of
 *    this Package.
 *
 *    8. The name of the Copyright Holder may not be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 *    9. THIS PACKAGE IS PROVIDED "AS IS" AND WITHOUT ANY EXPRESS OR IMPLIED
 *    WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF
 *    MERCHANTIBILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 *
 */
package org.chiba.connectors.smtp;

import org.apache.log4j.Category;
import org.chiba.xml.xforms.Submission;
import org.chiba.xml.xforms.connector.AbstractConnector;
import org.chiba.xml.xforms.connector.SubmissionHandler;
import org.chiba.xml.xforms.exception.XFormsException;
import org.w3c.dom.Node;

import javax.mail.Authenticator;
import javax.mail.Message.RecipientType;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * The SMTP submission driver serializes and submits instance data over SMTP (internet mail).
 * <p/>
 * Currently, the driver only supports the <code>post</code> submission method and the replace mode <code>none</code>.
 * Support for <code>form-data-post</code> and <code>urlencoded-post</code> as suggested in <a
 * href="http://www.w3.org/TR/2002/CR-xforms-20021112#submit-options">11.2 Submission Options</a> is on the way.
 * <p/>
 * The driver requires the additional information about the SMTP server to use, the mail subject, and the sender. This
 * information has to be provided in the query part of the submission's <code>action</code> URI. If you want the driver
 * to authenticate a user with the SMTP server, just provide a <code>username</code> and a <code>password</code>.
 * Support for other mail header fields like <code>cc</code> may be added later.
 * <p/>
 * Be careful when writing the submission's <code>action</code> URI: First, the contents of the query part have to be
 * URL-encoded, then you have to replace all <code>&amp;</code>'s with their corresponding XML entity
 * <code>&amp;amp;</code> in order to keep the XML well-formed.
 * <p/>
 * Here is an illustrating example:
 * <pre>
 * &lt;xforms:submission id='smtp' xforms:action='mailto:nn@nowhere.no?server=smtp.nowhere.no&amp;amp;sender=xforms@nowhere.no&amp;amp;subject=instance%20data'
 * /&gt;
 * </pre>
 * The same example enforcing authentication:
 * <pre>
 * &lt;xforms:submission id='smtp-auth' xforms:action='mailto:nn@nowhere.no?server=smtp.nowhere.no&amp;amp;sender=xforms@nowhere.no&amp;amp;subject=instance%20data&amp;amp;username=xforms&amp;amp;password=shhh'
 * /&gt;
 * </pre>
 * Since mail accounts are personal data, there is no example form demonstrating SMTP submission.
 *
 * @author Ulrich Nicolas Liss&eacute;
 * @version $Id$
 */
public class SMTPSubmissionHandler extends AbstractConnector implements SubmissionHandler {
    /**
     * The logger.
     */
    private static final Category LOGGER = Category.getInstance(SMTPSubmissionHandler.class);

    /**
     * Serializes and submits the given instance data over the <code>mailto</code> protocol.
     *
     * @param submission the submission issuing the request.
     * @param instance   the instance data to be serialized and submitted.
     * @return <code>null</code>.
     * @throws XFormsException if any error occurred during submission.
     */
    public Map submit(Submission submission, Node instance) throws XFormsException {

        if (!submission.getReplace().equals("none")) {
            throw new XFormsException("submission mode '" + submission.getReplace() + "' not supported");
        }

        try {
            String mediatype = "application/xml";
            if (submission.getMediatype() != null) {
                mediatype = submission.getMediatype();
            }

            String encoding = getDefaultEncoding();
            if (submission.getEncoding() != null) {
                encoding = submission.getEncoding();
            }

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            serialize(submission, instance, stream);

            /*
             * Some extension mechanism here could be handy
             */
            String method = submission.getMethod();
            if (method.equals("post")) {
                send(getURI(), stream.toByteArray(), encoding, mediatype);
            } else if (method.equals("multipart-post")) {
                send(getURI(), stream.toByteArray(), encoding, "multipart/related");
            } else if (method.equals("form-data-post")) {
                send(getURI(), stream.toByteArray(), encoding, "multipart/form-data");
            } else if (method.equals("url-encoded-post")) {
                send(getURI(), stream.toByteArray(), encoding, "application/x-www-form-urlencoded");
            } else {
                // Note: user has to provide mediatype in submission element otherwise this will
                // be probably wrong type (application/xml) ...
                send(getURI(), stream.toByteArray(), encoding, mediatype);
            }
        } catch (Exception e) {
            throw new XFormsException(e);
        }

        return null;
    }

    private void send(String uri, byte[] data, String encoding, String mediatype) throws Exception {
        URL url = new URL(uri);
        String recipient = url.getPath();

        String server = null;
        String port = null;
        String sender = null;
        String subject = null;
        String username = null;
        String password = null;

        StringTokenizer headers = new StringTokenizer(url.getQuery(), "&");

        while (headers.hasMoreTokens()) {
            String token = headers.nextToken();

            if (token.startsWith("server=")) {
                server = URLDecoder.decode(token.substring("server=".length()));

                continue;
            }

            if (token.startsWith("port=")) {
                port = URLDecoder.decode(token.substring("port=".length()));

                continue;
            }

            if (token.startsWith("sender=")) {
                sender = URLDecoder.decode(token.substring("sender=".length()));

                continue;
            }

            if (token.startsWith("subject=")) {
                subject = URLDecoder.decode(token.substring("subject=".length()));

                continue;
            }

            if (token.startsWith("username=")) {
                username = URLDecoder.decode(token.substring("username=".length()));

                continue;
            }

            if (token.startsWith("password=")) {
                password = URLDecoder.decode(token.substring("password=".length()));

                continue;
            }
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("smtp server '" + server + "'");

            if (username != null) {
                LOGGER.debug("smtp-auth username '" + username + "'");
            }

            LOGGER.debug("mail sender '" + sender + "'");
            LOGGER.debug("subject line '" + subject + "'");
        }

        Properties properties = System.getProperties();
        properties.put("mail.debug", String.valueOf(LOGGER.isDebugEnabled()));
        properties.put("mail.smtp.from", sender);
        properties.put("mail.smtp.host", server);
        
        if (port != null) {
            properties.put("mail.smtp.port", port);
        }

        if (username != null) {
            properties.put("mail.smtp.auth", String.valueOf(true));
            properties.put("mail.smtp.user", username);
        }

        Session session = Session.getInstance(properties, new SMTPAuthenticator(username, password));

        MimeMessage message = null;
        if (mediatype.startsWith("multipart/")) {
            message = new MimeMessage(session, new ByteArrayInputStream(data));
        } else {
            message = new MimeMessage(session);
            if (mediatype.toLowerCase().indexOf("charset=") == -1) {
                mediatype += "; charset=\"" + encoding + "\"";
            }
            message.setText(new String(data, encoding), encoding);
            message.setHeader("Content-Type", mediatype);
        }

        message.setRecipient(RecipientType.TO, new InternetAddress(recipient));
        message.setSubject(subject);
        message.setSentDate(new Date());

        Transport.send(message);
    }

    private class SMTPAuthenticator extends Authenticator {
        private PasswordAuthentication authentication = null;

        /**
         * Creates a new SMTPAuthenticator object.
         *
         * @param user     user name
         * @param password password
         */
        public SMTPAuthenticator(String user, String password) {
            this.authentication = new PasswordAuthentication(user, password);
        }

        /**
         * __UNDOCUMENTED__
         *
         * @return __UNDOCUMENTED__
         */
        protected PasswordAuthentication getPasswordAuthentication() {
            return this.authentication;
        }
    }
}

//end of class
