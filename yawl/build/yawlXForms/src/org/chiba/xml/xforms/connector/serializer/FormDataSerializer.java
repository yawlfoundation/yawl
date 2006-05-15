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
package org.chiba.xml.xforms.connector.serializer;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.xerces.dom.ElementImpl;
import org.chiba.xml.xforms.ModelItem;
import org.chiba.xml.xforms.Submission;
import org.chiba.xml.xforms.connector.InstanceSerializer;
import org.chiba.xml.xforms.exception.XFormsException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.*;
import java.util.Random;

/**
 * Serialize instance as multipart/form-data type.
 * <p/>
 * TODO:
 * <p/>
 * - CDATA sections, what to do with them, in specs only
 * TEXT_NODEs are covered, should be ignored ?
 *
 * @author Peter Mikula <peter.mikula@digital-artefacts.fi>
 */
public class FormDataSerializer implements InstanceSerializer {

    /**
     * Serialize instance into multipart/form-data stream as defined in
     * http://www.w3.org/TR/xforms/slice11.html#serialize-form-data
     *
     * @param submission
     * @param instance
     * @param stream
     * @param defaultEncoding
     * @throws Exception on error
     */
    public void serialize(Submission submission, Node instance,
                          OutputStream stream, String defaultEncoding) throws Exception {
        // sanity checks
        if (instance == null)
            return;

        switch (instance.getNodeType()) {

            case Node.ELEMENT_NODE:
                break;

            case Node.DOCUMENT_NODE:
                instance = ((Document) instance).getDocumentElement();
                break;

            default:
                return;
        }

        String encoding = defaultEncoding;
        if (submission.getEncoding() != null) {
            encoding = submission.getEncoding();
        }
        
        // generate boundary
        Random rnd = new Random(System.currentTimeMillis());
        String boundary = DigestUtils.md5Hex(getClass().getName() + rnd.nextLong());

        // serialize the instance
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(bos, encoding)));
        serializeElement(writer, (Element) instance, boundary, encoding);
        writer.print("\r\n--" + boundary + "--");
        writer.flush();
        
        // write to the stream
        String header = "Content-Type: multipart/form-data;\r\n"
                + "\tcharset=\"" + encoding + "\";\r\n"
                + "\tboundary=\"" + boundary + "\";\r\n"
                + "Content-Length: " + bos.size() + "\r\n\r\n";
        stream.write(header.getBytes(encoding));
        bos.writeTo(stream);
    }

    protected void serializeElement(PrintWriter writer, Element element,
                                    String boundary, String charset) throws Exception {
        /* The specs http://www.w3.org/TR/2003/REC-xforms-20031014/slice11.html#serialize-form-data
         *
         *     Each element node is visited in document order.
         *
         *     Each element that has exactly one text node child is selected 
         *     for inclusion.
         *
         *     Element nodes selected for inclusion are as encoded as 
         *     Content-Disposition: form-data MIME parts as defined in 
         *     [RFC 2387], with the name parameter being the element local name.
         *
         *     Element nodes of any datatype populated by upload are serialized 
         *     as the specified content and additionally have a 
         *     Content-Disposition filename parameter, if available.
         *
         *     The Content-Type must be text/plain except for xsd:base64Binary, 
         *     xsd:hexBinary, and derived types, in which case the header 
         *     represents the media type of the attachment if known, otherwise 
         *     application/octet-stream. If a character set is applicable, the 
         *     Content-Type may have a charset parameter.
         *
         */
        String nodeValue = null;
        boolean isCDATASection = false;
        boolean includeTextNode = true;

        NodeList list = element.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node n = list.item(i);
            switch (n.getNodeType()) {

                /* CDATA sections are not mentioned ... ignore for now
                case Node.CDATA_SECTION_NODE:
                    isCDATASection = true;
                 */
                    
                case Node.TEXT_NODE:
                    if (includeTextNode == true) {
                        if (nodeValue != null) {
                            /* only one text node allowed by specs */
                            includeTextNode = false;
                        } else {
                            nodeValue = n.getNodeValue();
                        }
                    }
                    break;

                    /* Real ambiguity in specs, what if there's one text node and
                     * n elements ? Let's assume if there is an element, ignore the
                     * text nodes
                     */
                case Node.ELEMENT_NODE:
                    includeTextNode = false;
                    serializeElement(writer, (Element) n, boundary, charset);
                    break;

                default:
                    // ignore comments and other nodes...
            }
        }

        if (nodeValue != null && includeTextNode) {

            Object object = ((ElementImpl) element).getUserData();
            if (!(object instanceof ModelItem)) {
                throw new XFormsException("Unknown instance data format.");
            }
            ModelItem item = (ModelItem) object;

            writer.print("\r\n--" + boundary);

            String name = element.getLocalName();
            if (name == null) {
                name = element.getNodeName();
            }
            
            // mediatype tells about file upload
            if (item.getMediatype() != null) {
                writer.print("\r\nContent-Disposition: form-data; name=\""
                        + name + "\";");
                if (item.getFilename() != null) {
                    File file = new File(item.getFilename());
                    writer.print(" filename=\"" + file.getName() + "\";");
                }
                writer.print("\r\nContent-Type: " + item.getMediatype());

            } else {
                writer.print("\r\nContent-Disposition: form-data; name=\""
                        + name + "\";");
                writer.print("\r\nContent-Type: text/plain; charset=\""
                        + charset + "\";");
            }

            String encoding = "8bit";
            if ("base64Binary".equalsIgnoreCase(item.getDatatype())) {
                encoding = "base64";
            } else if ("hexBinary".equalsIgnoreCase(item.getDatatype())) {
                // recode to base64 because of MIME
                nodeValue = new String(Base64.encodeBase64(Hex.decodeHex(nodeValue.toCharArray()), true));
                encoding = "base64";
            }
            writer.print("\r\nContent-Transfer-Encoding: " + encoding);
            writer.print("\r\n\r\n" + nodeValue);
        }

        writer.flush();
    }
}

//end of class


