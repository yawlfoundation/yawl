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
package org.chiba.xml.xforms.connector.http;

import org.apache.log4j.Category;
import org.chiba.xml.xforms.Submission;
import org.chiba.xml.xforms.XFormsConstants;
import org.chiba.xml.xforms.connector.SubmissionHandler;
import org.chiba.xml.xforms.exception.XFormsException;
import org.chiba.adapter.ChibaAdapter;
import org.w3c.dom.Node;

import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.StringTokenizer;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * The HTTP submission handler serializes and submits instance data over HTTP/1.1.
 * <p/>
 * Currently, the driver supports all but the <code>multipart-post</code> submission methods. Maybe security
 * functionality will be added later, thus becoming a HTTPS handler.
 * <p/>
 * See the '/web/forms/action.xhtml'-form for examples how to use HTTP submission.
 *
 * @author Ulrich Nicolas Liss&eacute;
 * @version $Id$
 */
public class HTTPSubmissionHandler extends AbstractHTTPConnector implements SubmissionHandler {
    /**
     * The logger.
     */
    private static final Category LOGGER = Category.getInstance(HTTPSubmissionHandler.class);

    /**
     * Serializes and submits the specified instance data over the <code>http</code> protocol.
     *
     * @param submission the submission issuing the request.
     * @param instance   the instance data to be serialized and submitted.
     * @return a map holding the response mime-type and the response stream.
     * @throws XFormsException if any error occurred during submission.
     */
    public Map submit(Submission submission, Node instance) throws XFormsException {
        try {
            String method = submission.getMethod();

            String mediatype = "application/xml";
            if (submission.getMediatype() != null) {
                mediatype = submission.getMediatype();
            }

            String encoding = getDefaultEncoding();
            if (submission.getEncoding() != null) {
                encoding = submission.getEncoding();
            }

            // todo: remove
            setSubmissionMap(submission.getSubmissionMap());

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            serialize(submission, instance, stream);

            /*
             * Some extension mechanism here could be handy 
             */
            if (method.equals("post")) {
                post(getURI(), stream.toString(encoding), mediatype + "; charset="+encoding);
            } else if (method.equals("get")) {
                get(getURI() + "?" + stream.toString(encoding));
            } else if (method.equals("put")) {
                put(getURI(), stream.toString(encoding), mediatype + "; charset="+encoding);
            } else if (method.equals("multipart-post")) {
                
                // body comes with header
                String data = stream.toString(encoding);
                int i = data.indexOf("\n\n");
                if (i == -1) {
                    i = data.indexOf("\r\n\r\n");
                    if (i == -1) {
                        throw new XFormsException(
                            "serializer sent wrong multipart content.");                    
                    }
                    i += 2;
                }
                i += 2;
                
                // get contenttype
                String contentType = null;
                StringTokenizer tok = new StringTokenizer(
                    data.substring(0, i)
                        .replaceAll("\r","")         // remove CR
                        .replaceAll("\n ", " ")      // multiline header                        
                        .replaceAll("\n\t", " ")     // multiline header
                );
                while(tok.hasMoreTokens()) {
                    String name = tok.nextToken("\n");
                    if (name.toLowerCase().startsWith("content-type:")) {
                        contentType = name.substring("content-type:".length());                        
                        break;
                    }
                }
                post(getURI(), data.substring(i), contentType);
                
            } else if (method.equals("form-data-post")) {
                post(getURI(), stream.toString(encoding), "multipart/form-data; charset="+encoding);
            } else if (method.equals("url-encoded-post")) {
                post(getURI(), stream.toString(encoding), "application/x-www-form-urlencoded; charset="+encoding);
            } else {
                // Note: user has to provide mediatype in submission element otherwise this will
                // be probably wrong type (application/xml) ...
                post(getURI(), stream.toString(encoding), mediatype + "; charset="+encoding);
            }

            Map response = getResponseHeader();
            response.put(ChibaAdapter.SUBMISSION_RESPONSE_STREAM, getResponseBody());

            return response;
        } catch (Exception e) {
            throw new XFormsException(e);
        }
    }


}

//end of class
