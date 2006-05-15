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
package org.chiba.xml.xforms.connector.file;

import org.apache.log4j.Category;
import org.chiba.adapter.ChibaAdapter;
import org.chiba.xml.xforms.Submission;
import org.chiba.xml.xforms.connector.AbstractConnector;
import org.chiba.xml.xforms.connector.SubmissionHandler;
import org.chiba.xml.xforms.exception.XFormsException;
import org.chiba.xml.util.DOMUtil;
import org.w3c.dom.Node;
import org.w3c.dom.Document;

import java.io.*;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * The file submission driver serializes and submits instance data to a file.
 * <p/>
 * When using the <code>put</code> submission method, the driver only supports
 * the replace mode <code>none</code>. It simply serializes the instance data to
 * the file denoted by the connector URI. When this file exists, it will be
 * overwritten silently, otherwise it will be created.
 * <p/>
 * When using the <code>get</code> submission method, the driver ignores any
 * replace mode. Furthermore, it ignores the instance data completely and
 * returns the file denoted by the connector URI as a response stream.
 *
 * @author Ulrich Nicolas Liss&eacute;
 * @version $Id$
 */
public class FileSubmissionHandler extends AbstractConnector implements SubmissionHandler {
    /**
     * The logger.
     */
    private static final Category LOGGER = Category.getInstance(FileSubmissionHandler.class);

    /**
     * Serializes and submits the specified instance data over the
     * <code>file</code> protocol.
     *
     * @param submission the submission issuing the request.
     * @param instance the instance data to be serialized and submitted.
     * @return <code>null</code>.
     * @throws XFormsException if any error occurred during submission.
     */
    public Map submit(Submission submission, Node instance) throws XFormsException {
        if (submission.getMethod().equalsIgnoreCase("get")) {
            try {
                // create uri
                URI uri = new URI(getURI());

                // use scheme specific part in order to handle UNC names
                String fileName = uri.getSchemeSpecificPart();
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("getting file '" + fileName + "'");
                }

                // create file
                File file = new File(fileName);
                InputStream inputStream;

                // check for directory
                if (file.isDirectory()) {
                    // create input stream from directory listing
                    Document document = FileURIResolver.buildDirectoryListing(file);
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    DOMUtil.prettyPrintDOM(document, outputStream);
                    inputStream = new ByteArrayInputStream(outputStream.toByteArray());
                }
                else {
                    // create file input stream
                    inputStream = new FileInputStream(new File(fileName));
                }

                Map response = new HashMap();
                response.put(ChibaAdapter.SUBMISSION_RESPONSE_STREAM, inputStream);

                return response;
            }
            catch (Exception e) {
                throw new XFormsException(e);
            }
        }

        if (submission.getMethod().equalsIgnoreCase("put")) {
            if (!submission.getReplace().equals("none")) {
                throw new XFormsException("submission mode '" + submission.getReplace() + "' not supported");
            }

            try {
                // create uri
                URI uri = new URI(getURI());

                // use scheme specific part in order to handle UNC names
                String fileName = uri.getSchemeSpecificPart();
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("putting file '" + fileName + "'");
                }

                // create output steam and serialize instance data
                FileOutputStream stream = new FileOutputStream(new File(fileName));
                serialize(submission, instance, stream);
                stream.close();
            }
            catch (Exception e) {
                throw new XFormsException(e);
            }

            return new HashMap();
        }

        throw new XFormsException("submission method '" + submission.getMethod() + "' not supported");
    }
}

// end of class
