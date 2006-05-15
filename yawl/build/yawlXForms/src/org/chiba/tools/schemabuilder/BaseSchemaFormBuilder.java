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
 *    However, you may distribute this Package in aggxmlregate with other (possibly commercial) programs as
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
import org.chiba.util.StringUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import javax.xml.transform.Source;
import java.util.Vector;

/*
 * Search for TODO for things remaining to-do in this implementation.
 *
 * TODO: i18n/l10n of messages, hints, captions. Possibly leverage org.chiba.i18n classes.
 * TODO: When Chiba supports itemset, use schema keyref and key constraints for validation.
 * TODO: Add support for default and fixed values.
 * TODO: Add support for use=prohibited.
 */

/**
 * A concrete base implementation of the SchemaFormBuilder interface allowing
 * an XForm to be automatically generated for an XML Schema definition.
 *
 * @author Brian Dueck
 * @version $Id$
 */
public class BaseSchemaFormBuilder
        extends AbstractSchemaFormBuilder
        implements SchemaFormBuilder {
	
    private static Category cat = Category.getInstance(BaseSchemaFormBuilder.class);
	
    /**
     * Creates a new instance of BaseSchemaForBuilder
     */
    public BaseSchemaFormBuilder(String rootTagName) {
        super(rootTagName);
    }

    /**
     * Creates a new BaseSchemaFormBuilder object.
     *
     * @param rootTagName    __UNDOCUMENTED__
     * @param instanceSource __UNDOCUMENTED__
     * @param action         __UNDOCUMENTED__
     * @param submitMethod   __UNDOCUMENTED__
     * @param wrapper        __UNDOCUMENTED__
     * @param stylesheet     __UNDOCUMENTED__
     */
    public BaseSchemaFormBuilder(String rootTagName,
                                 Source instanceSource,
                                 String action,
                                 String submitMethod,
                                 WrapperElementsBuilder wrapper,
                                 String stylesheet,
                                 String base,
                                 boolean userSchemaTypes) {
        super(rootTagName,
                instanceSource,
                action,
                submitMethod,
                wrapper,
                stylesheet,
                base,
                userSchemaTypes);
    }

    /**
     * Creates a new BaseSchemaFormBuilder object.
     *
     * @param rootTagName  __UNDOCUMENTED__
     * @param instanceHref __UNDOCUMENTED__
     * @param action       __UNDOCUMENTED__
     * @param submitMethod __UNDOCUMENTED__
     * @param wrapper      __UNDOCUMENTED__
     * @param stylesheet   __UNDOCUMENTED__
     */
    public BaseSchemaFormBuilder(String rootTagName,
                                 String instanceHref,
                                 String action,
                                 String submitMethod,
                                 WrapperElementsBuilder wrapper,
                                 String stylesheet,
                                 String base,
                                 boolean userSchemaTypes) {
        super(rootTagName,
                instanceHref,
                action,
                submitMethod,
                wrapper,
                stylesheet,
                base,
                userSchemaTypes);
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param text __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public String createCaption(String text) {
        return StringUtil.capitalizeIdentifier(text);
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param attribute __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public String createCaption(XSAttributeDeclaration attribute) {
        // TODO: Improve i18n/l10n of caption - may have to use
        //       a custom <appinfo> element in the XML Schema to do this.
        //
        return createCaption(attribute.getName());
    }

    public String createCaption(XSAttributeUse attribute) {
        // TODO: Improve i18n/l10n of caption - may have to use
        //       a custom <appinfo> element in the XML Schema to do this.
        //
        return createCaption(attribute.getAttrDeclaration().getName());
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param element __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public String createCaption(XSElementDeclaration element) {
        // TODO: Improve i18n/l10n of caption - may have to use
        //       a custom <appinfo> element in the XML Schema to do this.
        //
        return createCaption(element.getName());
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param element __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public String createCaption(XSObject element) {
        // TODO: Improve i18n/l10n of caption - may have to use
        //       a custom <appinfo> element in the XML Schema to do this.
        //
        if (element instanceof XSElementDeclaration) {
            return createCaption(((XSElementDeclaration) element).getName());
        } else if (element instanceof XSAttributeDeclaration) {
            return createCaption(((XSAttributeDeclaration) element).getName());
        } else if (element instanceof XSAttributeUse) {
            return createCaption(((XSAttributeUse) element).getAttrDeclaration().getName());
        } else
            LOGGER.warn("WARNING: createCaption: element is not an attribute nor an element: "
                    + element.getClass().getName());

        return null;
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param xForm       __UNDOCUMENTED__
     * @param caption     __UNDOCUMENTED__
     * @param controlType __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public Element createControlForAnyType(Document xForm,
                                           String caption,
                                           XSTypeDefinition controlType) {
        Element control =
                xForm.createElementNS(XFORMS_NS, getXFormsNSPrefix() + "textarea");
        this.setXFormsId(control);
        control.setAttributeNS(CHIBA_NS, getChibaNSPrefix() + "height", "3");

        //label
        Element captionElement =
                (Element) control.appendChild(xForm.createElementNS(XFORMS_NS,
                        getXFormsNSPrefix() + "label"));
        this.setXFormsId(captionElement);
        captionElement.appendChild(xForm.createTextNode(caption));

        return control;
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param xForm       __UNDOCUMENTED__
     * @param caption     __UNDOCUMENTED__
     * @param controlType __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public Element createControlForAtomicType(Document xForm,
                                              String caption,
                                              XSSimpleTypeDefinition controlType) {
        Element control;

        //remove while select1 do not work correctly in repeats
        if ((controlType.getName() != null)
                && controlType.getName().equals("boolean")) {
            control =
                    xForm.createElementNS(XFORMS_NS,
                            getXFormsNSPrefix() + "select1");
            control.setAttributeNS(XFORMS_NS,
                    getXFormsNSPrefix() + "appearance",
                    "full");
            this.setXFormsId(control);

            Element item_true =
                    xForm.createElementNS(XFORMS_NS, getXFormsNSPrefix() + "item");
            this.setXFormsId(item_true);
            Element label_true =
                    xForm.createElementNS(XFORMS_NS, getXFormsNSPrefix() + "label");
            this.setXFormsId(label_true);
            Text label_true_text = xForm.createTextNode("true");
            label_true.appendChild(label_true_text);
            item_true.appendChild(label_true);

            Element value_true =
                    xForm.createElementNS(XFORMS_NS, getXFormsNSPrefix() + "value");
            this.setXFormsId(value_true);
            Text value_true_text = xForm.createTextNode("true");
            value_true.appendChild(value_true_text);
            item_true.appendChild(value_true);
            control.appendChild(item_true);

            Element item_false =
                    xForm.createElementNS(XFORMS_NS, getXFormsNSPrefix() + "item");
            this.setXFormsId(item_false);
            Element label_false =
                    xForm.createElementNS(XFORMS_NS, getXFormsNSPrefix() + "label");
            this.setXFormsId(label_false);
            Text label_false_text = xForm.createTextNode("false");
            label_false.appendChild(label_false_text);
            item_false.appendChild(label_false);

            Element value_false =
                    xForm.createElementNS(XFORMS_NS, getXFormsNSPrefix() + "value");
            this.setXFormsId(value_false);
            Text value_false_text = xForm.createTextNode("false");
            value_false.appendChild(value_false_text);
            item_false.appendChild(value_false);
            control.appendChild(item_false);
        } else {
            control =
                    xForm.createElementNS(XFORMS_NS, getXFormsNSPrefix() + "input");
            this.setXFormsId(control);
        }

        //label
        Element captionElement =
                (Element) control.appendChild(xForm.createElementNS(XFORMS_NS,
                        getXFormsNSPrefix() + "label"));
        this.setXFormsId(captionElement);
        captionElement.appendChild(xForm.createTextNode(caption));

        return control;
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param xForm       __UNDOCUMENTED__
     * @param controlType __UNDOCUMENTED__
     * @param caption     __UNDOCUMENTED__
     * @param bindElement __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public Element createControlForEnumerationType(Document xForm,
                                                   XSSimpleTypeDefinition controlType,
                                                   String caption,
                                                   Element bindElement) {
        // TODO: Figure out an intelligent or user determined way to decide between
        // selectUI style (listbox, menu, combobox, radio) (radio and listbox best apply)
        // Possibly look for special appInfo section in the schema and if not present default to comboBox...
        //
        // For now, use radio if enumValues < DEFAULT_LONG_LIST_MAX_SIZE otherwise use combobox
        //
        StringList enumFacets = controlType.getLexicalEnumeration();
        int nbFacets = enumFacets.getLength();
        if (nbFacets > 0) {
            Vector enumValues = new Vector();

            Element control =
                    xForm.createElementNS(XFORMS_NS,
                            getXFormsNSPrefix() + "select1");
            this.setXFormsId(control);

            //label
            Element captionElement1 =
                    (Element) control.appendChild(xForm.createElementNS(XFORMS_NS,
                            getXFormsNSPrefix() + "label"));
            this.setXFormsId(captionElement1);
            captionElement1.appendChild(xForm.createTextNode(caption));

            Element choices =
                    xForm.createElementNS(XFORMS_NS,
                            getXFormsNSPrefix() + "choices");
            this.setXFormsId(choices);

            for (int i = 0; i < nbFacets; i++) {
                String facet = enumFacets.item(i);
                enumValues.add(facet);
            }

            if (nbFacets
                    < Long.parseLong(getProperty(SELECTONE_LONG_LIST_SIZE_PROP))) {
                control.setAttributeNS(XFORMS_NS,
                        getXFormsNSPrefix() + "appearance",
                        getProperty(SELECTONE_UI_CONTROL_SHORT_PROP));
            } else {
                control.setAttributeNS(XFORMS_NS,
                        getXFormsNSPrefix() + "appearance",
                        getProperty(SELECTONE_UI_CONTROL_LONG_PROP));

                // add the "Please select..." instruction item for the combobox
                // and set the isValid attribute on the bind element to check for the "Please select..."
                // item to indicate that is not a valid value
                //
                {
                    String pleaseSelect = "[Select1 " + caption + "]";
                    Element item =
                            xForm.createElementNS(XFORMS_NS,
                                    getXFormsNSPrefix() + "item");
                    this.setXFormsId(item);
                    choices.appendChild(item);

                    Element captionElement =
                            xForm.createElementNS(XFORMS_NS,
                                    getXFormsNSPrefix() + "label");
                    this.setXFormsId(captionElement);
                    item.appendChild(captionElement);
                    captionElement.appendChild(xForm.createTextNode(pleaseSelect));

                    Element value =
                            xForm.createElementNS(XFORMS_NS,
                                    getXFormsNSPrefix() + "value");
                    this.setXFormsId(value);
                    item.appendChild(value);
                    value.appendChild(xForm.createTextNode(pleaseSelect));

                    // not(purchaseOrder/state = '[Choose State]')
                    //String isValidExpr = "not(" + bindElement.getAttributeNS(XFORMS_NS,"nodeset") + " = '" + pleaseSelect + "')";
                    // ->no, not(. = '[Choose State]')
                    String isValidExpr = "not( . = '" + pleaseSelect + "')";

                    //check if there was a constraint
                    String constraint =
                            bindElement.getAttributeNS(XFORMS_NS, "constraint");

                    if ((constraint != null) && !constraint.equals("")) {
                        constraint = constraint + " && " + isValidExpr;
                    } else {
                        constraint = isValidExpr;
                    }

                    bindElement.setAttributeNS(XFORMS_NS,
                            getXFormsNSPrefix() + "constraint",
                            constraint);
                }
            }

            control.appendChild(choices);

            addChoicesForSelectControl(xForm, choices, enumValues);

            return control;
        } else {
            return null;
        }
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param xForm       __UNDOCUMENTED__
     * @param listType    __UNDOCUMENTED__
     * @param caption     __UNDOCUMENTED__
     * @param bindElement __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public Element createControlForListType(Document xForm,
                                            XSSimpleTypeDefinition listType,
                                            String caption,
                                            Element bindElement) {
        XSSimpleTypeDefinition controlType = listType.getItemType();

        StringList enumFacets = controlType.getLexicalEnumeration();
        int nbFacets = enumFacets.getLength();
        if (nbFacets > 0) {
            Element control =
                    xForm.createElementNS(XFORMS_NS,
                            getXFormsNSPrefix() + "select");
            this.setXFormsId(control);

            //label
            Element captionElement =
                    (Element) control.appendChild(xForm.createElementNS(XFORMS_NS,
                            getXFormsNSPrefix() + "label"));
            this.setXFormsId(captionElement);
            captionElement.appendChild(xForm.createTextNode(caption));

            Vector enumValues = new Vector();
            for (int i = 0; i < nbFacets; i++) {
                String facet = enumFacets.item(i);
                enumValues.add(facet);
            }

            // TODO: Figure out an intelligent or user determined way to decide between
            // selectUI style (listbox, menu, combobox, radio) (radio and listbox best apply)
            // Possibly look for special appInfo section in the schema and if not present default to checkBox...
            //
            // For now, use checkbox if there are < DEFAULT_LONG_LIST_MAX_SIZE items, otherwise use long control
            //
            if (enumValues.size()
                    < Long.parseLong(getProperty(SELECTMANY_LONG_LIST_SIZE_PROP))) {
                control.setAttributeNS(XFORMS_NS,
                        getXFormsNSPrefix() + "appearance",
                        getProperty(SELECTMANY_UI_CONTROL_SHORT_PROP));
            } else {
                control.setAttributeNS(XFORMS_NS,
                        getXFormsNSPrefix() + "appearance",
                        getProperty(SELECTMANY_UI_CONTROL_LONG_PROP));
            }

            Element choices =
                    xForm.createElementNS(XFORMS_NS,
                            getXFormsNSPrefix() + "choices");
            this.setXFormsId(choices);
            control.appendChild(choices);

            addChoicesForSelectControl(xForm, choices, enumValues);

            return control;
        } else {
            return null;
        }
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param xForm __UNDOCUMENTED__
     * @param node  __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public Element createHint(Document xForm, XSObject node) {
        XSAnnotation annotation = null;
        if (node instanceof XSElementDeclaration)
            annotation = ((XSElementDeclaration) node).getAnnotation();
        else if (node instanceof XSAttributeDeclaration)
            annotation = ((XSAttributeDeclaration) node).getAnnotation();
        else if (node instanceof XSAttributeUse)
            annotation =
                    ((XSAttributeUse) node).getAttrDeclaration().getAnnotation();

        if (annotation != null)
            return addHintFromDocumentation(xForm, annotation);
        else
            return null;
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param bindElement __UNDOCUMENTED__
     */
    public void endBindElement(Element bindElement) {
        return;
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param controlElement __UNDOCUMENTED__
     * @param controlType    __UNDOCUMENTED__
     */
    public void endFormControl(Element controlElement,
                               XSTypeDefinition controlType,
                               int minOccurs,
                               int maxOccurs) {
        return;
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param groupElement __UNDOCUMENTED__
     */
    public void endFormGroup(Element groupElement,
                             XSTypeDefinition controlType,
                             int minOccurs,
                             int maxOccurs,
                             Element modelSection) {
        return;
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param bindElement __UNDOCUMENTED__
     * @param controlType __UNDOCUMENTED__
     * @param minOccurs   __UNDOCUMENTED__
     * @param maxOccurs   __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public Element startBindElement(Element bindElement,
            XSTypeDefinition controlType,
            int minOccurs,
            int maxOccurs) {
		// START WORKAROUND
		// Due to a Chiba bug, anyType is not a recognized type name.
		// so, if this is an anyType, then we'll just skip the type
		// setting.
		//
		// type.getName() may be 'null' for anonymous types, so compare against
		// static string (see bug #1172541 on sf.net)
		if (!("anyType").equals(controlType.getName())) {
			Element enveloppe = bindElement.getOwnerDocument().getDocumentElement();
			String typeName = this.getXFormsTypeName(enveloppe, controlType);
			if (typeName != null && !typeName.equals(""))
				bindElement.setAttributeNS(XFORMS_NS,
						getXFormsNSPrefix() + "type",
						typeName);
		}
		
		if (minOccurs == 0) {
			bindElement.setAttributeNS(XFORMS_NS,
					getXFormsNSPrefix() + "required",
			"false()");
		} else {
			bindElement.setAttributeNS(XFORMS_NS,
					getXFormsNSPrefix() + "required",
			"true()");
		}
		
		//no more minOccurs & maxOccurs element: add a constraint if maxOccurs>1:
		//count(.) <= maxOccurs && count(.) >= minOccurs
		String minConstraint = null;
		String maxConstraint = null;
		
		if (minOccurs > 1) {
			//if 0 or 1 -> no constraint (managed by "required")
			minConstraint = "count(.) >= " + minOccurs;
		}
		
		if (maxOccurs > 1) {
			//if 1 or unbounded -> no constraint
			maxConstraint = "count(.) <= " + maxOccurs;
		}
		
		String constraint = null;
		
		if ((minConstraint != null) && (maxConstraint != null)) {
			constraint = minConstraint + " and " + maxConstraint;
		} else if (minConstraint != null) {
			constraint = minConstraint;
		} else {
			constraint = maxConstraint;
		}
		
		if ((constraint != null) && !constraint.equals("")) {
			bindElement.setAttributeNS(XFORMS_NS,
					getXFormsNSPrefix() + "constraint",
					constraint);
		}
		
			/*if (minOccurs != 1) {
			bindElement.setAttributeNS(XFORMS_NS,getXFormsNSPrefix() + "minOccurs",String.valueOf(minOccurs));
			}
			if (maxOccurs != 1) {
			bindElement.setAttributeNS(XFORMS_NS,getXFormsNSPrefix() + "maxOccurs",maxOccurs == -1 ? "unbounded" : String.valueOf((maxOccurs)));
			}*/
			return bindElement;
		}


    /**
     * __UNDOCUMENTED__
     *
     * @param controlElement __UNDOCUMENTED__
     * @param controlType    __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public Element startFormControl(Element controlElement,
                                    XSTypeDefinition controlType) {
        return controlElement;
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param groupElement  __UNDOCUMENTED__
     * @param schemaElement __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public Element startFormGroup(Element groupElement,
                                  XSElementDeclaration schemaElement) {
        //groupElement.setAttributeNS(CHIBA_NS,getChibaNSPrefix() + "box-align",getProperty(GROUP_BOX_ALIGN_PROP));
        //groupElement.setAttributeNS(CHIBA_NS,getChibaNSPrefix() + "box-orient",getProperty(GROUP_BOX_ORIENT_PROP));
        //groupElement.setAttributeNS(CHIBA_NS,getChibaNSPrefix() + "caption-width",getProperty(GROUP_CAPTION_WIDTH_PROP));
        //groupElement.setAttributeNS(CHIBA_NS,getChibaNSPrefix() + "width",getProperty(GROUP_WIDTH_PROP));
        //groupElement.setAttributeNS(CHIBA_NS,getChibaNSPrefix() + "border",getProperty(GROUP_BORDER_PROP));
        return groupElement;
    }
}

/*
   $Log$
   Revision 1.1  2006-02-27 17:28:01  maod
   *** empty log message ***

   Revision 1.15  2004/08/15 14:14:07  joernt
   preparing release...
   -reformatted sources to fix mixture of tabs and spaces
   -optimized imports on all files

   Revision 1.14  2004/07/28 22:06:23  joernt
   removed redundant declarations

   Revision 1.13  2004/07/28 21:43:28  joernt
   optimized imports

   Revision 1.12  2004/06/15 11:54:56  soframel
   added support to XMLSchema types: if the "useSchemaTypes" option is true, there will be a link to the XMLSchema on the "model" element, and the XMLSchema simple types will be used whenever possible. The targetNamespace of the schema will also be declared in the XForms document, with prefix "schema".

   + some UI ameliorations (appearance attributes, added a group for simple types that repeat, otherwise we don't see the label when the list is empty)

   Revision 1.11  2004/03/05 08:52:34  soframel
   Refactored completely the schemabuilder tool to use Xerces XMLSchema API instead of Castor's.

   Revision 1.10  2004/01/27 09:59:57  joernt
   imports

   Revision 1.9  2004/01/14 10:54:19  soframel
   Adapted the schemabuilder to modifications of chiba.
   - default generation is HTML
   - namespaces declarations changed
   - possibility to add a xml:base attribute
   - debugged constraint replacing "maxOccurs" model item property
   - adapted the test cases to these modifications, and also to previous modifications which hadn't been taken into account

   Revision 1.8  2003/11/07 00:16:34  joernt
   optimized imports

   Revision 1.7  2003/10/02 15:15:49  joernt
   applied chiba jalopy settings to whole src tree

   Revision 1.6  2003/09/08 12:48:25  joernt
   changed occurrences of xforms:id into id to clarify the examples (there's
   nothing like a xforms:id out there).
   Revision 1.5  2003/08/19 06:52:15  soframel
   boolean controls are now transformed as select1 with true/false instead of text input
   Revision 1.4  2003/08/18 23:00:46  joernt
   javadoc fixes + optimize imports
   Revision 1.3  2003/08/18 13:58:59  soframel
   adapted/debugged the builder:
   - removed chiba attributes
   - transformed minOccurs / maxOccurs into constraints when necessary
   - corrected a bug on the constraint for list types
   - removed the selectors for repeats + added the content of each repeat into a group
   - added a "stylesheet" parameter to specify the chiba:stylesheet attribute on the enveloppe
   Revision 1.2  2003/07/31 02:19:25  joernt
   optimized imports
   Revision 1.1  2003/07/12 12:22:48  joernt
   package refactoring: moved from xforms.builder
   Revision 1.1.1.1  2003/05/23 14:54:08  unl
   no message
   Revision 1.5  2003/02/12 15:15:49  soframel
   debug nested "bind" when processing an attribute
   Revision 1.4  2003/02/04 10:39:45  soframel
   transform the xsd:boolean simple type into a select1 instead of an input
   Revision 1.3  2003/01/25 22:02:03  joernt
   imports cleaned up;
   redundant casts removed
   Revision 1.2  2002/12/12 15:21:43  soframel
   Added an interface "WrapperElementsBuilder" to create the wrappers element (with 2 implementations: a base implementation, for "envelope" elements, and an XHTML implementation).
   Changed the form builder to use it, and added a "+" trigger for repeats
   Changed the ant task to leave the choice of the wrapper type.
   Revision 1.1  2002/12/11 14:50:42  soframel
   transferred the Schema2XForms generator from chiba2 to chiba1
   Revision 1.7  2002/11/08 07:30:50  soframel
   corrected a bug with the reference to the xforms:case id in xforms:toggle elements
   Revision 1.6  2002/10/30 16:31:15  soframel
   id added for every xforms element (except xforms:value), with a counter by element name
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
