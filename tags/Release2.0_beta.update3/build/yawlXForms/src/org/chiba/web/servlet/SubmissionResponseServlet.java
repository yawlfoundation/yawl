// Copyright 2005 Chibacon
/*
 *
 * Artistic License
 *
 * Preamble
 *
 * The intent of this document is to state the conditions under which a
 * Package may be copied, such that the Copyright Holder maintains some
 * semblance of artistic control over the development of the package, while
 * giving the users of the package the right to use and distribute the
 * Package in a more-or-less customary fashion, plus the right to make
 * reasonable modifications.
 *
 * Definitions:
 *
 * "Package" refers to the collection of files distributed by the Copyright
 * Holder, and derivatives of that collection of files created through
 * textual modification.
 *
 * "Standard Version" refers to such a Package if it has not been modified,
 * or has been modified in accordance with the wishes of the Copyright
 * Holder.
 *
 * "Copyright Holder" is whoever is named in the copyright or copyrights
 * for the package.
 *
 * "You" is you, if you're thinking about copying or distributing this
 * Package.
 *
 * "Reasonable copying fee" is whatever you can justify on the basis of
 * media cost, duplication charges, time of people involved, and so
 * on. (You will not be required to justify it to the Copyright Holder, but
 * only to the computing community at large as a market that must bear the
 * fee.)
 *
 * "Freely Available" means that no fee is charged for the item itself,
 * though there may be fees involved in handling the item. It also means
 * that recipients of the item may redistribute it under the same
 * conditions they received it.
 *
 * 1. You may make and give away verbatim copies of the source form of the
 *    Standard Version of this Package without restriction, provided that
 *    you duplicate all of the original copyright notices and associated
 *    disclaimers.
 *
 * 2. You may apply bug fixes, portability fixes and other modifications
 *    derived from the Public Domain or from the Copyright Holder. A
 *    Package modified in such a way shall still be considered the Standard
 *    Version.
 *
 * 3. You may otherwise modify your copy of this Package in any way,
 *    provided that you insert a prominent notice in each changed file
 *    stating how and when you changed that file, and provided that you do
 *    at least ONE of the following:
 *
 *    a) place your modifications in the Public Domain or otherwise make
 *    them Freely Available, such as by posting said modifications to
 *    Usenet * or an equivalent medium, or placing the modifications on a
 *    major * archive site such as ftp.uu.net, or by allowing the Copyright
 *    Holder * to include your modifications in the Standard Version of the
 *    Package.
 *
 *    b) use the modified Package only within your corporation or *
 *    organization.
 *
 *    c) rename any non-standard executables so the names do not conflict
 *    with standard executables, which must also be provided, and provide a
 *    separate manual page for each non-standard executable that clearly
 *    documents how it differs from the Standard Version.
 *
 *    d) make other distribution arrangements with the Copyright Holder.
 *
 * 4. You may distribute the programs of this Package in object code or
 *    executable form, provided that you do at least ONE of the following:
 *
 *    a) distribute a Standard Version of the executables and library
 *    files, together with instructions (in the manual page or equivalent)
 *    on where to get the Standard Version.
 *
 *    b) accompany the distribution with the machine-readable source of the
 *    Package with your modifications.
 *
 *    c) accompany any non-standard executables with their corresponding
 *    Standard Version executables, giving the non-standard executables
 *    non-standard names, and clearly documenting the differences in manual
 *    pages (or equivalent), together with instructions on where to get the
 *    Standard Version.
 *
 *    d) make other distribution arrangements with the Copyright Holder.
 *
 * 5. You may charge a reasonable copying fee for any distribution of this
 *    Package. You may charge any fee you choose for support of this
 *    Package. You may not charge a fee for this Package itself.  However,
 *    you may distribute this Package in aggregate with other (possibly
 *    commercial) programs as part of a larger (possibly commercial)
 *    software distribution provided that you do not advertise this Package
 *    as a product of your own.
 *
 * 6. The scripts and library files supplied as input to or produced as
 *    output from the programs of this Package do not automatically fall
 *    under * the copyright of this Package, but belong to whomever
 *    generated them, * and may be sold commercially, and may be aggregated
 *    with this Package.
 *
 * 7. C or perl subroutines supplied by you and linked into this Package
 *    shall not be considered part of this Package.
 *
 * 8. The name of the Copyright Holder may not be used to endorse or
 *    promote products derived from this software without specific prior *
 *    written permission.
 *
 * 9. THIS PACKAGE IS PROVIDED "AS IS" AND WITHOUT ANY EXPRESS OR IMPLIED
 *    WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF
 *    MERCHANTIBILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 *
 */
package org.chiba.web.servlet;

import org.apache.log4j.Logger;
import org.chiba.web.session.XFormsSessionManager;
import org.chiba.web.session.XFormsSession;
import org.chiba.web.session.impl.DefaultXFormsSessionManagerImpl;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

/**
 * Returns a submission response exactly once.
 *
 * @author Ulrich Nicolas Liss&eacute;
 * @version $Id: SubmissionResponseServlet.java,v 1.1 2006/09/10 19:50:51 joernt Exp $
 */
public class SubmissionResponseServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(SubmissionResponseServlet.class);

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // lookup session
        HttpSession session = request.getSession(false);
        String key = request.getParameter("sessionKey");

        if(LOGGER.isDebugEnabled()){
            LOGGER.debug("requested session: " + key);
        }

        //todo: inherit from ChibaServlet for getXFormsSessionManager() method ? hm, weak reaon
        XFormsSessionManager sessionManager = DefaultXFormsSessionManagerImpl.getInstance();
        XFormsSession xFormsSession = sessionManager.getXFormsSession(key);

        if (session != null) {
            if(LOGGER.isDebugEnabled()){
                Enumeration keys = session.getAttributeNames();
                if(keys.hasMoreElements()){
                    LOGGER.debug("--- existing keys in session --- ");
                }
                while (keys.hasMoreElements()) {
                    String s = (String) keys.nextElement();
                    LOGGER.debug("existing sessionkey: " + s + ":" + session.getAttribute(s));
                }
            }

            // lookup attribute containing submission response map
            Map submissionResponse = (Map) xFormsSession.getProperty(ChibaServlet.CHIBA_SUBMISSION_RESPONSE);
            if (submissionResponse != null) {

                if(LOGGER.isDebugEnabled()){
                    LOGGER.debug("handling submission/@replace='all'");
                    Enumeration keys = session.getAttributeNames();
                    if(keys.hasMoreElements()){
                        LOGGER.debug("--- existing keys in session  --- ");
                        while (keys.hasMoreElements()) {
                            String s = (String) keys.nextElement();
                            LOGGER.debug("existing sessionkey: " + s + ":" + session.getAttribute(s));
                        }
                    }else{
                        LOGGER.debug("--- no keys left in session  --- ");
                    }
                }

                // copy header fields
                Map headerMap = (Map) submissionResponse.get("header");
                String name;
                String value;
                Iterator iterator = headerMap.keySet().iterator();
                while (iterator.hasNext()) {
                    name = (String) iterator.next();
                    if (name.equalsIgnoreCase("Transfer-Encoding")) {
                        // Some servers (e.g. WebSphere) may set a "Transfer-Encoding"
                        // with the value "chunked". This may confuse the client since
                        // ChibaServlet output is not encoded as "chunked", so this
                        // header is ignored.
                        continue;
                    }

                    value = (String) headerMap.get(name);
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("added header: " + name + "=" + value);
                    }

                    response.setHeader(name, value);
                }

                // copy body stream
                InputStream bodyStream = (InputStream) submissionResponse.get("body");
                OutputStream outputStream = new BufferedOutputStream(response.getOutputStream());
                for (int b = bodyStream.read(); b > -1; b = bodyStream.read()) {
                    outputStream.write(b);
                }

                // close streams
                bodyStream.close();
                outputStream.close();

                //kill XFormsSession
                sessionManager.deleteXFormsSession(xFormsSession.getKey());

                return;
            }
        }

        response.sendError(HttpServletResponse.SC_FORBIDDEN, "no submission response available");
    }

}
