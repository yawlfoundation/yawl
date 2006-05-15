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
package org.chiba.tools.schemabuilder;

import org.apache.log4j.Category;
import org.apache.xerces.xs.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.transform.Source;
import java.util.Properties;

/**
 * An object that implements this interface can build an XForm that conforms to
 * the elements and attributes declared in an XML Schema.
 *
 * @author Brian Dueck
 * @version $Id$
 */
public interface SchemaFormBuilder {
    /**
     * logging
     */
    public static Category LOGGER =
            Category.getInstance(SchemaFormBuilder.class);

    /**
     * XMLSchema Instance Namespace declaration
     */
    public static final String XMLSCHEMA_INSTANCE_NAMESPACE_URI = "http://www.w3.org/2001/XMLSchema-instance";

    /**
     * XMLNS Namespace declaration.
     */
    public static final String XMLNS_NAMESPACE_URI =
            "http://www.w3.org/2000/xmlns/";

    /**
     * XML Namespace declaration
     */
    public static final String XML_NAMESPACE_URI =
            "http://www.w3.org/XML/1998/namespace";

    /**
     * XForms namespace declaration.
     */
    public static final String XFORMS_NS = "http://www.w3.org/2002/xforms";

    /**
     * Chiba namespace declaration.
     */
    public static final String CHIBA_NS =
            "http://chiba.sourceforge.net/2003/08/xforms";

    /**
     * XLink namespace declaration.
     */
    public static final String XLINK_NS = "http://www.w3.org/1999/xlink";

    /**
     * XML Events namsepace declaration.
     */
    public static final String XMLEVENTS_NS = "http://www.w3.org/2001/xml-events";

    /**
     * Chiba prefix
     */
    public static final String chibaNSPrefix = "chiba:";

    /**
     * XForms prefix
     */
    public static final String xformsNSPrefix = "xforms:";

    /**
     * Xlink prefix
     */
    public static final String xlinkNSPrefix = "xlink:";

    /**
     * XMLSchema instance prefix *
     */
    public static final String xmlSchemaInstancePrefix = "xsi:";

    /**
     * XML Events prefix
     */
    public static final String xmleventsNSPrefix = "ev:";

    /**
     * __UNDOCUMENTED__
     *
     * @return __UNDOCUMENTED__
     */
    public String getAction();

    /**
     * __UNDOCUMENTED__
     *
     * @return __UNDOCUMENTED__
     */
    public String getInstanceHref();

    /**
     * __UNDOCUMENTED__
     *
     * @return __UNDOCUMENTED__
     */
    public int getInstanceMode();

    /**
     * __UNDOCUMENTED__
     *
     * @return __UNDOCUMENTED__
     */
    public Source getInstanceSource();

    /**
     * Get the current set of properties used by implementations of SchemaFormBuilder.
     *
     * @return The list of properties.
     */
    public Properties getProperties();

    /**
     * Sets the property to the specified value. If the property exists, its value is overwritten.
     *
     * @param key   The implementation defined property key.
     * @param value The value for the property.
     */
    public void setProperty(String key, String value);

    /**
     * Gets the value for the specified property.
     *
     * @param key The implementation defined property key.
     * @return The property value if found, or null if the property cannot be located.
     */
    public String getProperty(String key);

    /**
     * Gets the value for the specified property, with a default if the property cannot be located.
     *
     * @param key          The implementation defined property key.
     * @param defaultValue This value will be returned if the property does not exists.
     * @return The property value if found, or defaultValue if the property cannot be located.
     */
    public String getProperty(String key, String defaultValue);

    /**
     * Properties choosed by the user
     */
    public String getRootTagName();

    /**
     * __UNDOCUMENTED__
     *
     * @return __UNDOCUMENTED__
     */
    public String getStylesheet();

    /**
     * __UNDOCUMENTED__
     *
     * @return __UNDOCUMENTED__
     */
    public String getSubmitMethod();

    /**
     * Generate the XForm based on a user supplied XML Schema.
     *
     * @param inputURI The document source for the XML Schema.
     * @return The Document containing the XForm.
     * @throws org.chiba.tools.schemabuilder.FormBuilderException
     *          If an error occurs building the XForm.
     */
    public Document buildForm(String inputURI) throws FormBuilderException;

    /**
     * Generate the XForm based on a user supplied XML Schema returning the
     * XForm as stringified XML.
     *
     * @param inputURI The document source for the XML Schema.
     * @return The stringified XML containing the XForm.
     * @throws org.chiba.tools.schemabuilder.FormBuilderException
     *          If an error occurs building the XForm.
     */
    public String buildFormAsString(String inputURI)
            throws FormBuilderException;

    /**
     * Creates a caption for the provided text extracted from the XML Schema.
     * The implementation is responsible for reformatting the provided string to make it
     * suitable to be displayed to users of the XForm. This typically includes translating
     * XML tag name style identifiers (e.g. customerStreetAddress) into more reader friendly
     * captions (e.g. Customer Street Address).
     *
     * @param text The string value to be reformatted for use as a caption.
     * @return The caption.
     */
    public String createCaption(String text);

    /**
     * Creates a caption for the provided XML Schema attribute.
     * The implementation is responsible for providing an appropriate caption
     * suitable to be displayed to users of the XForm. This typically includes translating
     * XML tag name style identifiers (e.g. customerStreetAddress) into more reader friendly
     * captions (e.g. Customer Street Address).
     *
     * @param attribute The XML schema attribute for which a caption is required.
     * @return The caption.
     */
    public String createCaption(XSAttributeDeclaration attribute);

    /**
     * Creates a caption for the provided XML Schema element.
     * The implementation is responsible for providing an appropriate caption
     * suitable to be displayed to users of the XForm. This typically includes translating
     * XML tag name style identifiers (e.g. customerStreetAddress) into more reader friendly
     * captions (e.g. Customer Street Address).
     *
     * @param element The XML schema element for which a caption is required.
     * @return The caption.
     */
    public String createCaption(XSElementDeclaration element);

    /**
     * Creates a form control for an XML Schema any type.
     * <p/>
     * This method is called when the form builder determines a form control is required for
     * an any type.
     * The implementation of this method is responsible for creating an XML element of the
     * appropriate type to receive a value for <b>controlType</b>. The caller is responsible
     * for adding the returned element to the form and setting caption, bind, and other
     * standard elements and attributes.
     *
     * @param xForm       The XForm document.
     * @param controlType The XML Schema type for which the form control is to be created.
     * @return The element for the form control.
     */
    public Element createControlForAnyType(Document xForm,
                                           String caption,
                                           XSTypeDefinition controlType);

    /**
     * Creates a form control for an XML Schema simple atomic type.
     * <p/>
     * This method is called when the form builder determines a form control is required for
     * an atomic type.
     * The implementation of this method is responsible for creating an XML element of the
     * appropriate type to receive a value for <b>controlType</b>. The caller is responsible
     * for adding the returned element to the form and setting caption, bind, and other
     * standard elements and attributes.
     *
     * @param xForm       The XForm document.
     * @param controlType The XML Schema type for which the form control is to be created.
     * @return The element for the form control.
     */
    public Element createControlForAtomicType(Document xForm,
                                              String caption,
                                              XSSimpleTypeDefinition controlType);

    /**
     * Creates a form control for an XML Schema simple type restricted by an enumeration.
     * This method is called when the form builder determines a form control is required for
     * an enumerated type.
     * The implementation of this method is responsible for creating an XML element of the
     * appropriate type to receive a value for <b>controlType</b>. The caller is responsible
     * for adding the returned element to the form and setting caption, bind, and other
     * standard elements and attributes.
     *
     * @param xForm       The XForm document.
     * @param controlType The XML Schema type for which the form control is to be created.
     * @param caption     The caption for the form control. The caller The purpose of providing the caption
     *                    is to permit the implementation to add a <b>[Select1 .... ]</b> message that involves the caption.
     * @param bindElement The bind element for this control. The purpose of providing the bind element
     *                    is to permit the implementation to add a isValid attribute to the bind element that prevents
     *                    the <b>[Select1 .... ]</b> item from being selected.
     * @return The element for the form control.
     */
    public Element createControlForEnumerationType(Document xForm,
                                                   XSSimpleTypeDefinition controlType,
                                                   String caption,
                                                   Element bindElement);

    /**
     * Creates a form control for an XML Schema simple list type.
     * <p/>
     * This method is called when the form builder determines a form control is required for
     * a list type.
     * The implementation of this method is responsible for creating an XML element of the
     * appropriate type to receive a value for <b>controlType</b>. The caller is responsible
     * for adding the returned element to the form and setting caption, bind, and other
     * standard elements and attributes.
     *
     * @param xForm       The XForm document.
     * @param listType    The XML Schema list type for which the form control is to be created.
     * @param caption     The caption for the form control. The caller The purpose of providing the caption
     *                    is to permit the implementation to add a <b>[Select1 .... ]</b> message that involves the caption.
     * @param bindElement The bind element for this control. The purpose of providing the bind element
     *                    is to permit the implementation to add a isValid attribute to the bind element that prevents
     *                    the <b>[Select1 .... ]</b> item from being selected.
     * @return The element for the form control.
     */
    public Element createControlForListType(Document xForm,
                                            XSSimpleTypeDefinition listType,
                                            String caption,
                                            Element bindElement);

    /**
     * Creates a hint XML Schema annotated node (AttributeDecl or ElementDecl).
     * The implementation is responsible for providing an xforms:hint element for the
     * specified schemaNode suitable to be dsipalayed to users of the XForm. The caller
     * is responsible for adding the returned element to the form.
     * This typically includes extracting documentation from the element/attribute's
     * annotation/documentation elements and/or extracting the same information from the
     * element/attribute's type annotation/documentation.
     *
     * @param schemaNode The string value to be reformatted for use as a caption.
     * @return The xforms:hint element. If a null value is returned a hint is not added.
     */
    public Element createHint(Document xForm, XSObject schemaNode);

    /**
     * This method is invoked after the form builder is finished creating and processing
     * a bind element. Implementations may choose to use this method to add/inspect/modify
     * the bindElement prior to the builder moving onto the next bind element.
     *
     * @param bindElement The bind element being processed.
     */
    public void endBindElement(Element bindElement);

    /**
     * This method is invoked after the form builder is finished creating and processing
     * a form control. Implementations may choose to use this method to add/inspect/modify
     * the controlElement prior to the builder moving onto the next control.
     *
     * @param controlElement The form control element that was created.
     * @param controlType    The XML Schema type for which <b>controlElement</b> was created.
     */
    public void endFormControl(Element controlElement,
                               XSTypeDefinition controlType,
                               int minOccurs,
                               int maxOccurs);

    /**
     * __UNDOCUMENTED__
     *
     * @param groupElement __UNDOCUMENTED__
     */
    public void endFormGroup(Element groupElement,
                             XSTypeDefinition controlType,
                             int minOccurs,
                             int maxOccurs,
                             Element modelSection);

    /**
     * Reset the SchemaFormBuilder to default values.
     */
    public void reset();

    /**
     * This method is invoked after an xforms:bind element is created for the specified SimpleType.
     * The implementation is responsible for setting setting any/all bind attributes
     * except for <b>id</b> and <b>ref</b> - these have been automatically set
     * by the caller (and should not be touched by implementation of startBindElement)
     * prior to invoking startBindElement.
     * The caller automatically adds the returned element to the model section of
     * the form.
     *
     * @param bindElement The bindElement being processed.
     * @param controlType XML Schema type of the element/attribute this bind is for.
     * @param minOccurs   The minimum number of occurences for this element/attribute.
     * @param maxOccurs   The maximum number of occurences for this element/attribute.
     * @return The bind Element to use in the XForm - bindElement or a replacement.
     */
    public Element startBindElement(Element bindElement,
                                    XSTypeDefinition controlType,
                                    int minOccurs,
                                    int maxOccurs);

    /**
     * This method is invoked after the form builder creates a form control
     * via a createControlForXXX() method but prior to decorating the form control
     * with common attributes such as a caption, hint, help text elements,
     * bind attributes, etc.
     * The returned element is used in the XForm in place of controlElement.
     * Implementations may choose to use this method to substitute controlElement
     * with a different element, or perform any other processing on controlElement
     * prior to it being added to the form.
     *
     * @param controlElement The form control element that was created.
     * @param controlType    The XML Schema type for which <b>controlElement</b> was created.
     * @return The Element to use in the XForm - controlElement or a replacement.
     */
    public Element startFormControl(Element controlElement,
                                    XSTypeDefinition controlType);

    /**
     * This method is invoked after an xforms:group element is created for the specified
     * ElementDecl. A group is created whenever an element is encountered in the XML Schema
     * that contains other elements and attributes (complex types or mixed content types).
     * The caller automatically adds the returned element to the XForm.
     *
     * @param groupElement  The groupElement being processed.
     * @param schemaElement The schemaElement for the group.
     * @return The group Element to use in the XForm - groupElement or a replacement. If a null
     *         value is returned, the group is not created.
     */
    public Element startFormGroup(Element groupElement,
                                  XSElementDeclaration schemaElement);
}

/*
   $Log$
   Revision 1.1  2006-02-27 17:28:01  maod
   *** empty log message ***

   Revision 1.14  2004/08/15 16:27:22  joernt
   preparing release...
   - fixed some javadoc issues
   - updated installation and index page for website

   Revision 1.13  2004/08/15 14:14:07  joernt
   preparing release...
   -reformatted sources to fix mixture of tabs and spaces
   -optimized imports on all files

   Revision 1.12  2004/07/28 21:43:28  joernt
   optimized imports

   Revision 1.11  2004/03/24 16:17:13  soframel
   - support for inheritance corrected in schemabuilder
   - basicTest adapted to work with these corrections
   - other tests cases slightly adapted (namespaces)

   Revision 1.10  2004/03/05 08:52:34  soframel
   Refactored completely the schemabuilder tool to use Xerces XMLSchema API instead of Castor's.

   Revision 1.9  2004/01/27 09:59:57  joernt
   imports

   Revision 1.8  2004/01/14 10:54:19  soframel
   Adapted the schemabuilder to modifications of chiba.
   - default generation is HTML
   - namespaces declarations changed
   - possibility to add a xml:base attribute
   - debugged constraint replacing "maxOccurs" model item property
   - adapted the test cases to these modifications, and also to previous modifications which hadn't been taken into account

   Revision 1.7  2003/11/07 00:16:34  joernt
   optimized imports

   Revision 1.6  2003/10/02 15:15:49  joernt
   applied chiba jalopy settings to whole src tree

   Revision 1.5  2003/08/18 23:00:46  joernt
   javadoc fixes + optimize imports
   Revision 1.4  2003/08/18 13:58:59  soframel
   adapted/debugged the builder:
   - removed chiba attributes
   - transformed minOccurs / maxOccurs into constraints when necessary
   - corrected a bug on the constraint for list types
   - removed the selectors for repeats + added the content of each repeat into a group
   - added a "stylesheet" parameter to specify the chiba:stylesheet attribute on the enveloppe
   Revision 1.3  2003/08/05 08:57:21  joernt
   xforms + chiba namespaces adapted
   Revision 1.2  2003/07/31 02:19:25  joernt
   optimized imports
   Revision 1.1  2003/07/12 12:22:48  joernt
   package refactoring: moved from xforms.builder
   Revision 1.1.1.1  2003/05/23 14:54:08  unl
   no message
   Revision 1.2  2002/12/12 15:21:44  soframel
   Added an interface "WrapperElementsBuilder" to create the wrappers element (with 2 implementations: a base implementation, for "envelope" elements, and an XHTML implementation).
   Changed the form builder to use it, and added a "+" trigger for repeats
   Changed the ant task to leave the choice of the wrapper type.
   Revision 1.1  2002/12/11 14:50:42  soframel
   transferred the Schema2XForms generator from chiba2 to chiba1
   Revision 1.6  2002/11/08 07:30:50  soframel
   corrected a bug with the reference to the xforms:case id in xforms:toggle elements
   Revision 1.5  2002/10/29 16:53:53  soframel
   - removed the chiba layout parameters box-align, border, box-orient, caption-width and width
   - added parameters to the constructors and removed them from the 'buildForm' method
   Revision 1.4  2002/10/24 07:43:55  soframel
   adapted to the 08/2002 working draft
   Revision 1.3  2002/06/10 14:57:45  bdueck
   Various Schema2Form enhancements. Support for inheritence, any attribute, and xsi:type through switch element.
   Check-out basicTest.xsd and basicTest-expected.xml in
   org\chiba\xml\xforms\schema\builder\test for an example of most features.
   Revision 1.2  2002/06/04 13:28:27  bdueck
   Ported Schema2Form builder to work with Castor's XML Schema library (http://www.castor.org).
   Castor replaces the Oracle schema library used in the initial version.
   Revision 1.1  2002/05/22 22:24:34  joernt
   Brian's initial version of schema2xforms builder
 */
