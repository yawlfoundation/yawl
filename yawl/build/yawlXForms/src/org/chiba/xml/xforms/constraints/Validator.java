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
package org.chiba.xml.xforms.constraints;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.log4j.Category;
import org.apache.xerces.impl.dv.InvalidDatatypeValueException;
import org.apache.xerces.impl.dv.ValidatedInfo;
import org.apache.xerces.impl.dv.ValidationContext;
import org.apache.xerces.impl.dv.XSSimpleType;
import org.apache.xerces.impl.validation.ValidationState;
import org.apache.xerces.xs.XSConstants;
import org.chiba.xml.xforms.Instance;
import org.chiba.xml.xforms.LocalValue;
import org.chiba.xml.xforms.Model;
import org.chiba.xml.xforms.NamespaceCtx;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import java.util.Iterator;
import java.util.Map;

/**
 * Validates instance data items.
 *
 * @author Ulrich Nicolas Liss&eacute;
 * @version $Id$
 */
public class Validator {

    private static Category LOGGER = Category.getInstance(Validator.class);

    private Model model;
    private Map datatypes;

    /**
     * Creates a new Validator.
     */
    public Validator() {
        // NOP
    }

    /**
     * Returns the Model of this Validator.
     *
     * @return the Model of this Validator.
     */
    public Model getModel() {
        return this.model;
    }

    /**
     * Sets the Model of this Validator.
     *
     * @param model the Model of this Validator.
     */
    public void setModel(Model model) {
        this.model = model;
    }

    /**
     * Returns the Schema Datatype definitions of this Validator.
     *
     * @return the Schema Datatype definitions of this Validator.
     */
    public Map getDatatypes() {
        return this.datatypes;
    }

    /**
     * Sets the Schema Datatype definitions of this Validator.
     *
     * @param datatypes the Schema Datatype definitions of this Validator.
     */
    public void setDatatypes(Map datatypes) {
        this.datatypes = datatypes;
    }

    /**
     * Checks wether the specified Schema Datatype definition is known by this
     * Validator.
     *
     * @param name the name of the Schema Datatype definition.
     * @return <code>true</code> if the specified Schema Datatype definition is
     *         known, otherwise <code>false</code>.
     */
    public boolean isKnown(String name) {
        if (this.datatypes == null) {
            return false;
        }

        String expandedName = getExpandedName(this.model.getElement(), name);
        return this.datatypes.get(expandedName) != null;
    }

    /**
     * Checks wether the specified Schema Datatype definition is supported by
     * this Validator.
     *
     * @param name the name of the Schema Datatype definition.
     * @return <code>true</code> if the specified Schema Datatype definition is
     *         supported, otherwise <code>false</code>.
     */
    public boolean isSupported(String name) {
        String expandedName = getExpandedName(this.model.getElement(), name);

        if (expandedName.equals("duration") || expandedName.equals("{" + NamespaceCtx.XMLSCHEMA_NS + "}duration")) {
            return false;
        }
        if (expandedName.equals("ENTITY") || expandedName.equals("{" + NamespaceCtx.XMLSCHEMA_NS + "}ENTITY")) {
            return false;
        }
        if (expandedName.equals("ENTITIES") || expandedName.equals("{" + NamespaceCtx.XMLSCHEMA_NS + "}ENTITIES")) {
            return false;
        }
        if (expandedName.equals("NOTATION") || expandedName.equals("{" + NamespaceCtx.XMLSCHEMA_NS + "}NOTATION")) {
            return false;
        }

        return true;
    }

    /**
     * Checks wether the Schema Datatype definition specified by the
     * <code>restriction</code> parameter is derived by restriction from the
     * Schema Datatype definition specified by the <code>base</code> parameter.
     *
     * @param base the restriction of the base Schema Datatype definition.
     * @param restriction the restriction of the restricted Schema Datatype
     * definition.
     * @return <code>true</code> if both specified Schema Datatype definitions
     *         are known by this Validator and the second definition is derived
     *         by restriction from the first, otherwise <code>false</code>.
     */
    public boolean isRestricted(String base, String restriction) {
        if (this.datatypes == null) {
            return false;
        }

        String baseName = getExpandedName(this.model.getElement(), base);
        XSSimpleType baseType = (XSSimpleType) this.datatypes.get(baseName);
        if (baseType == null) {
            return false;
        }

        String restrictionName = getExpandedName(this.model.getElement(), restriction);
        XSSimpleType restrictionType = (XSSimpleType) this.datatypes.get(restrictionName);
        if (restrictionType == null) {
            return false;
        }

        return restrictionType.derivedFromType(baseType, XSConstants.DERIVATION_RESTRICTION);
    }

    /**
     * Validates the specified instance data nodes.
     *
     * @param instance the instance to be validated.
     * @return <code>true</code> if all relevant instance data nodes are valid
     *         regarding in terms of their <code>constraint</code> and
     *         <code>required</code> properties, otherwise <code>false</code>.
     */
    public boolean validate(Instance instance) {
        // validate all instance nodes
        return validate(instance, "/");
    }

    /**
     * Validates the specified instance data node and its descendants.
     *
     * @param instance the instance to be validated.
     * @param path an xpath denoting an arbitrary subtre of the instance.
     * @return <code>true</code> if all relevant instance data nodes are valid
     *         regarding in terms of their <code>constraint</code> and
     *         <code>required</code> properties, otherwise <code>false</code>.
     */
    public boolean validate(Instance instance, String path) {
        // initialize
        boolean result = true;
        String expressionPath = path;

        if (!path.endsWith("/")) {
            expressionPath = expressionPath + "/";
        }

        // set expression path to contain the specified path and its subtree
        expressionPath = expressionPath + "descendant-or-self::*";

        // evaluate expression path
        JXPathContext context = instance.getInstanceContext();
        Iterator iterator = context.iteratePointers(expressionPath);
        Pointer locationPointer;
        String locationPath;

        while (iterator.hasNext()) {
            locationPointer = (Pointer) iterator.next();
            locationPath = locationPointer.asPath();
            Element element = (Element) locationPointer.getNode();

            // validate element node against type
            String type = element.getAttributeNS(NamespaceCtx.XMLSCHEMA_INSTANCE_NS, "type");
            result &= validateNode(instance, locationPath, type);

            // handle attributes explicitely since JXPath has
            // seriuos problems with namespaced attributes
            NamedNodeMap attributes = element.getAttributes();

            for (int index = 0; index < attributes.getLength(); index++) {
                Attr attr = (Attr) attributes.item(index);

                if (isInstanceAttribute(attr)) {
                    // validate attribute node
                    result &= validateNode(instance, locationPath + "/@" + attr.getNodeName());
                }
            }
        }

        return result;
    }

    private boolean validateNode(Instance instance, String path) {
        // validate node
        return validateNode(instance, path, null);
    }

    private boolean validateNode(Instance instance, String path, String type) {
        // lookup
        Model model = instance.getModel();
        LocalValue localValue = instance.getModelItem(path);
        String value = localValue.getValue();
        String datatype = localValue.getDatatype();

        // compute *type-valid*
        boolean typeValid = true;
        if (type != null && type.length() > 0) {
            typeValid = checkDatatype(model, type, value);
        }

        if (datatype == null || datatype.length() == 0) {
            datatype = "string";
        }

        typeValid &= checkDatatype(model, datatype, value);
        localValue.setDatatypeValid(typeValid);

        // compute *required-valid*
        boolean requiredValid = value.length() > 0;
        localValue.setRequiredValid(requiredValid);

        // propagate calculation/validation changes
        localValue.notifyListeners();

        // compute validation result
        boolean result = (!localValue.isEnabled()) || localValue.isValid();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("computed " + (result
                    ? "valid"
                    : "invalid") + " node " + path);
        }

        return result;
    }

    private boolean checkDatatype(Model model, String type, String value) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("check datatype: checking '" + value + "' against '" + type + "'");
        }

        String expandedName = getExpandedName(model.getElement(), type);
        XSSimpleType simpleType = (XSSimpleType) this.datatypes.get(expandedName);
        ValidatedInfo validatedInfo = new ValidatedInfo();
        ValidationState validationState = new ValidationState();
        validationState.setFacetChecking(true);
        validationState.setExtraChecking(false);

        try {
            simpleType.validate(value, (ValidationContext) validationState, validatedInfo);
        } catch (InvalidDatatypeValueException e) {
            return false;
        }

        return true;
    }

    private String getExpandedName(Element context, String name) {
        int separator = name.indexOf(':');
        String prefix = separator > -1 ? name.substring(0, separator) : null;
        String localName = separator > -1 ? name.substring(separator + 1) : name;
        String namespaceURI = prefix != null ? NamespaceCtx.getNamespaceURI(context, prefix) : null;
        String expandedName = namespaceURI != null ? "{" + namespaceURI + "}" + localName : localName;

        return expandedName;
    }

    private static boolean isInstanceAttribute(Attr attr) {
        if (attr.getNamespaceURI() == null) {
            return true;
        }

        if (NamespaceCtx.XML_NS.equals(attr.getNamespaceURI())) {
            return false;
        }

        if (NamespaceCtx.XMLNS_NS.equals(attr.getNamespaceURI())) {
            return false;
        }

        if (NamespaceCtx.XMLSCHEMA_INSTANCE_NS.equals(attr.getNamespaceURI())) {
            return false;
        }

        return true;
    }

}

// end of class
