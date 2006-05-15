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

package org.chiba.xml.xforms.connector;

import org.apache.log4j.Category;
import org.chiba.xml.util.DOMUtil;
import org.chiba.xml.xforms.Model;
import org.chiba.xml.xforms.NamespaceCtx;
import org.chiba.xml.xforms.exception.XFormsException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.net.URI;
import java.util.StringTokenizer;

/**
 * The SchemaValidator can validate the instance node according to its XMLSchema
 * <p>the schema(s) must be specified in the "schema" attribute of the model.</p>
 *
 * @author <a href="mailto:soframel@users.sourceforge.net">Sophie Ramel</a>
 */

public class SchemaValidator {

    /**
     * XMLSchema and XMLSchema-instance namespaces
     */
    private final static String XMLSCHEMA_INSTANCE_NS = "http://www.w3.org/2001/XMLSchema-instance";
    private final static String XMLSCHEMA_NS = "http://www.w3.org/2001/XMLSchema";

    /**
     * The logger.
     */
    private static Category LOGGER = Category.getInstance(SchemaValidator.class);

    public SchemaValidator() {
    }

    /**
     * validate the instance according to the schema specified on the model
     *
     * @return false if the instance is not valid
     */
    public boolean validateSchema(Model model, Node instance) throws XFormsException {
        boolean valid = true;
        String message;
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("SchemaValidator.validateSchema: validating instance");

        //needed if we want to load schemas from Model + set it as "schemaLocation" attribute
        String schemas = model.getElement().getAttributeNS(NamespaceCtx.XFORMS_NS, "schema");
        if (schemas != null && !schemas.equals("")) {
//		    valid=false;

            //add schemas to element
            //shouldn't it be done on a copy of the doc ?
            Element el = null;
            if (instance.getNodeType() == Node.ELEMENT_NODE)
                el = (Element) instance;
            else if (instance.getNodeType() == Node.DOCUMENT_NODE)
                el = ((Document) instance).getDocumentElement();
            else {
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug("instance node type is: " + instance.getNodeType());
            }

            String prefix = NamespaceCtx.getPrefix(el, XMLSCHEMA_INSTANCE_NS);
            //test if with targetNamespace or not
            //if more than one schema : namespaces are mandatory ! (optional only for 1)
            StringTokenizer tokenizer = new StringTokenizer(schemas, " ", false);
            String schemaLocations = null;
            String noNamespaceSchemaLocation = null;
            while (tokenizer.hasMoreElements()) {
                String token = (String) tokenizer.nextElement();
                //check that it is an URL
                URI uri = null;
                try {
                    uri = new java.net.URI(token);
                } catch (java.net.URISyntaxException ex) {
                    if (LOGGER.isDebugEnabled())
                        LOGGER.debug(token + " is not an URI");
                }

                if (uri != null) {
                    String ns;
                    try {
                        ns = this.getSchemaNamespace(uri);

                        if (ns != null && !ns.equals("")) {
                            if (schemaLocations == null)
                                schemaLocations = ns + " " + token;
                            else
                                schemaLocations = schemaLocations + " " + ns + " " + token;

                            ///add the namespace declaration if it is not on the instance?
                            //TODO: how to know with which prefix ?
                            String nsPrefix = NamespaceCtx.getPrefix(el, ns);
                            if (nsPrefix == null) { //namespace not declared !
                                LOGGER.warn("SchemaValidator: targetNamespace " + ns + " of schema " + token + " is not declared in instance: declaring it as default...");
                                el.setAttributeNS(NamespaceCtx.XMLNS_NS,
                                        NamespaceCtx.XMLNS_PREFIX,
                                        ns);
                            }
                        } else if (noNamespaceSchemaLocation == null)
                            noNamespaceSchemaLocation = token;
                        else { //we have more than one schema without namespace
                            LOGGER.warn("SchemaValidator: There is more than one schema without namespace !");
                        }
                    } catch (Exception ex) {
                        LOGGER.warn("Exception while trying to load schema: " + uri.toString() + ": " + ex.getMessage(), ex);
                        //in case there was an exception: do nothing, do not set the schema
                    }
                }
            }
            //write schemaLocations found
            if (schemaLocations != null && !schemaLocations.equals(""))
                el.setAttributeNS(XMLSCHEMA_INSTANCE_NS, prefix + ":schemaLocation", schemaLocations);
            if (noNamespaceSchemaLocation != null)
                el.setAttributeNS(XMLSCHEMA_INSTANCE_NS, prefix + ":noNamespaceSchemaLocation", noNamespaceSchemaLocation);

            //save and parse the doc
            ValidationErrorHandler handler = null;
            File f;
            try {
                //save document
                f = File.createTempFile("instance", ".xml");
                f.deleteOnExit();
                TransformerFactory trFact = TransformerFactory.newInstance();
                Transformer trans = trFact.newTransformer();
                DOMSource source = new DOMSource(el);
                StreamResult result = new StreamResult(f);
                trans.transform(source, result);
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug("Validator.validateSchema: file temporarily saved in " + f.getAbsolutePath());

                //parse it with error handler to validate it
                handler = new ValidationErrorHandler();
                SAXParserFactory parserFact = SAXParserFactory.newInstance();
                parserFact.setValidating(true);
                parserFact.setNamespaceAware(true);
                SAXParser parser = parserFact.newSAXParser();
                XMLReader reader = parser.getXMLReader();

                //validation activated
                reader.setFeature("http://xml.org/sax/features/validation", true);
                //schema validation activated
                reader.setFeature("http://apache.org/xml/features/validation/schema", true);
                //used only to validate the schema, not the instance
                //reader.setFeature( "http://apache.org/xml/features/validation/schema-full-checking", true);
                //validate only if there is a grammar
                reader.setFeature("http://apache.org/xml/features/validation/dynamic", true);

                parser.parse(f, handler);
            } catch (Exception ex) {
                LOGGER.warn("Validator.validateSchema: Exception in XMLSchema validation: " + ex.getMessage(), ex);
                //throw new XFormsException("XMLSchema validation failed. "+message);
            }

            //if no exception
            if (handler != null && handler.isValid())
                valid = true;
            else {
                message = handler.getMessage();
                //TODO: find a way to get the error message displayed
                throw new XFormsException("XMLSchema validation failed. " + message);
            }

            if (LOGGER.isDebugEnabled())
                LOGGER.debug("Validator.validateSchema: result=" + valid);

        }

        return valid;
    }

    /**
     * method to get the target namespace of a schema given by an URI:
     * it opens the schema at the given URI, anf looks for the "targetNamespace" attribute
     *
     * @param uri the URI of the schema
     * @return the targetNamespace of the schema, or null of none was found
     */
    private String getSchemaNamespace(URI uri) throws Exception {
        String ns = null;

        //load schema
        File schemaFile = new File(uri);
        Document doc = DOMUtil.parseXmlFile(schemaFile, true, false);
        if (doc != null) {
            Element schema = doc.getDocumentElement();
            ns = schema.getAttributeNS(XMLSCHEMA_NS, "targetNamespace");
            if (ns == null || ns.equals("")) //try without NS !
                ns = schema.getAttribute("targetNamespace");
        } else
            LOGGER.warn("Schema " + uri.toString() + " could not be parsed");

        if (LOGGER.isDebugEnabled())
            LOGGER.debug("SchemaValidator.getSchemaNamespace for schema " + uri.toString() + ": " + ns);

        return ns;
    }

    /**
     * SAX error handler for XMLSchema validation
     * TODO: transform Xerces error messages so that they are more "user-friendly"
     */
    class ValidationErrorHandler extends DefaultHandler {
        private boolean valid;
        private String message;

        public ValidationErrorHandler() {
            valid = true;
            message = null;
        }

        public void error(SAXParseException exception) throws SAXException {
            allErrors(exception);
        }

        public void fatalError(SAXParseException exception) throws SAXException {
            allErrors(exception);
        }

        public void warning(SAXParseException exception) throws SAXException {
            allErrors(exception);
        }

        public void allErrors(SAXParseException exception) throws SAXException {
            valid = false;
            if (message == null || message.equals(""))
                message = exception.getMessage();
            else
                message = message + "\n" + exception.getMessage();
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("validation error: " + exception.getMessage());
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }
    }

}
