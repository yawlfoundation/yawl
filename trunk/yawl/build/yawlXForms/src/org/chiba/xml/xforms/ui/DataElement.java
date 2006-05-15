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
package org.chiba.xml.xforms.ui;

import org.apache.log4j.Category;
import org.chiba.xml.xforms.Model;
import org.chiba.xml.xforms.ModelItem;
import org.chiba.xml.xforms.ModelItemListener;
import org.chiba.xml.xforms.NamespaceCtx;
import org.chiba.xml.xforms.config.Config;
import org.chiba.xml.xforms.events.EventFactory;
import org.chiba.xml.xforms.exception.XFormsException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Manages the chiba:data Element which is created for every bound XForms
 * Control and for Container Elements such as 'group', 'repeat' and 'switch'.
 * This acts as a proxy for the status and value of the associated instance data
 * item and is created and updated by the Processor.
 * <p/>
 * This class is a convenience function of the processor and has no
 * correspondence in the XForms set of elements. It eases the generation of the
 * user interface by pulling all instance data information down to the controls
 * and thereby simplifying the task of writing XSL tranformations for the output
 * DOM.
 *
 * @author Joern Turner
 * @author Ulrich Nicolas Liss&eacute;
 * @version $Id$
 */
public class DataElement implements ModelItemListener {
    private static final Category LOGGER = Category.getInstance(DataElement.class);
    private static final String IGNORE_PROPERTY = "chiba.ui.ignoreInitialValidity";
    private static final String IGNORE_DEFAULT = "true";

    private boolean handleProperties;
    private boolean handlePath;
    private boolean handleTypes;
    private boolean handleValue;
    private BoundElement boundElement;
    private Element dataElement;

    /**
     * Creates a new data element.
     *
     * @param boundElement the owning form control or container element.
     */
    public DataElement(BoundElement boundElement) {
        this.boundElement = boundElement;
        this.handleProperties = false;
        this.handlePath = false;
        this.handleTypes = false;
        this.handleValue = false;
    }

    // configurate behaviour

    /**
     * Specifies wether the data proxy initializes and updates the attributes
     * <code>chiba:valid</code>, <code>chiba:readonly</code>,
     * <code>chiba:required</code>, and <code>chiba:enabled</code> during its
     * lifecycle.
     * <p/>
     * These attributes will not be removed by updates.
     *
     * @param handleProperties handle properties or not.
     */
    public void setHandleProperties(boolean handleProperties) {
        this.handleProperties = handleProperties;
    }

    /**
     * Specifies wether the data proxy initializes and updates the attribute
     * <code>chiba:xpath</code> during its lifecycle.
     * <p/>
     * This attribute may be removed by updates.
     *
     * @param handlePath handle path or not.
     */
    public void setHandlePath(boolean handlePath) {
        this.handlePath = handlePath;
    }

    /**
     * Specifies wether the data proxy initializes and updates the attributes
     * <code>chiba:type</code> and <code>chiba:p3ptype</code> during its
     * lifecycle.
     * <p/>
     * These attributes may be removed by updates.
     *
     * @param handleTypes handle types or not.
     */
    public void setHandleTypes(boolean handleTypes) {
        this.handleTypes = handleTypes;
    }

    /**
     * Specifies wether the data proxy initializes and updates the
     * <code>chiba:data</code> element's child node during its lifecycle.
     * <p/>
     * The child node may be removed by updates.
     *
     * @param handleValue handle value or not.
     */
    public void setHandleValue(boolean handleValue) {
        this.handleValue = handleValue;
    }

    // lifecycle

    /**
     * Creates the <code>chiba:data</code> element as a child of the bound
     * element. Then, initializes the data proxy according to its
     * configuration.
     */
    public void init() {
        // create chiba:data element
        Document document = this.boundElement.getElement().getOwnerDocument();
        this.dataElement = document.createElementNS(NamespaceCtx.CHIBA_NS, NamespaceCtx.CHIBA_PREFIX + ":data");
        this.boundElement.getElement().appendChild(this.dataElement);

        // steers alert visibility in conjunction with valid
        setAttributeValue("visited", String.valueOf(false));

        // init facets
        ModelItem item = getModelItem();
        initProperties(item);
        initOrUpdatePath(item);
        initOrUpdateTypes(item);
        initOrUpdateValue(item);
    }

    /**
     * Updates the data proxy according to its configuration.
     */
    public void update() {
        // update facets
        ModelItem item = getModelItem();
        updateProperties(item);
        initOrUpdatePath(item);
        initOrUpdateTypes(item);
        initOrUpdateValue(item);
    }

    /**
     * Disposes all resources hold by the data proxy.
     */
    public void dispose() {
        // dispose resources
        ModelItem item = getModelItem();
        disposeProperties(item);
    }

    // member access

    /**
     * Returns the associated DOM element.
     *
     * @return the associated DOM element.
     */
    public Element getElement() {
        return this.dataElement;
    }

    /**
     * Returns the datatype of the bound element.
     *
     * @return the datatype of the bound element.
     */
    public String getDatatype() {
        return this.dataElement.getAttributeNS(NamespaceCtx.CHIBA_NS, "type");
    }

    /**
     * Sets the specified attribute value. If the value is <code>null</code>,
     * the attribute will be removed.
     *
     * @param name the local name of the attribute.
     * @param value the value.
     */
    public void setAttributeValue(String name, String value) {
        if (value != null) {
            this.dataElement.setAttributeNS(NamespaceCtx.CHIBA_NS,
                    NamespaceCtx.CHIBA_PREFIX + ":" + name,
                    value);
        }
        else {
            this.dataElement.removeAttributeNS(NamespaceCtx.CHIBA_NS, name);
        }
    }

    /**
     * Appends the specified value as a text node to the data element. If the
     * value is <code>null</code>, the data element's first child node will be
     * removed.
     *
     * @param value the element's value.
     */
    public void setElementValue(String value) {
        Node child = this.dataElement.getFirstChild();

        if (value != null) {
            if (child == null) {
                child = this.dataElement.getOwnerDocument().createTextNode("");
                this.dataElement.appendChild(child);
            }

            child.setNodeValue(value);
        }
        else {
            if (child != null) {
                this.dataElement.removeChild(child);
            }
        }
    }

    /**
     * Returns the node value of the data element's first child if there is any,
     * otherwise <code>null</code>.
     *
     * @return the element's value.
     */
    public String getElementValue() {
        Node child = this.dataElement.getFirstChild();
        if (child != null) {
            return child.getNodeValue();
        }

        return null;
    }

    // implementation of 'org.chiba.xml.xforms.ModelItemListener'

    /**
     * Dispatches <code>xforms-enabled</code> / <code>xforms-disabled</code>
     * events depending on the current <code>enabled</code> state.
     *
     * @param enabled the current <code>enabled</code> state.
     */
    public void enabledChanged(boolean enabled) {
        try {
            if (enabled) {
                this.boundElement.getContainerObject().dispatch(this.boundElement.getTarget(), EventFactory.ENABLED, null);
            }
            else {
                this.boundElement.getContainerObject().dispatch(this.boundElement.getTarget(), EventFactory.DISABLED, null);
            }
        }
        catch (XFormsException e) {
            // todo: error handling
            e.printStackTrace();
        }
    }

    /**
     * Dispatches <code>xforms-readonly</code> / <code>xforms-readwrite</code>
     * events depending on the current <code>readonly</code> state.
     *
     * @param readonly the current <code>readonly</code> state.
     */
    public void readonlyChanged(boolean readonly) {
        try {
            if (readonly) {
                this.boundElement.getContainerObject().dispatch(this.boundElement.getTarget(), EventFactory.READONLY, null);
            }
            else {
                this.boundElement.getContainerObject().dispatch(this.boundElement.getTarget(), EventFactory.READWRITE, null);
            }
        }
        catch (XFormsException e) {
            // todo: error handling
            e.printStackTrace();
        }
    }

    /**
     * Dispatches <code>xforms-required</code> / <code>xforms-optional</code>
     * events depending on the current <code>required</code> state.
     *
     * @param required the current <code>required</code> state.
     */
    public void requiredChanged(boolean required) {
        try {
            if (required) {
                this.boundElement.getContainerObject().dispatch(this.boundElement.getTarget(), EventFactory.REQUIRED, null);
            }
            else {
                this.boundElement.getContainerObject().dispatch(this.boundElement.getTarget(), EventFactory.OPTIONAL, null);
            }
        }
        catch (XFormsException e) {
            // todo: error handling
            e.printStackTrace();
        }
    }

    /**
     * Dispatches <code>xforms-valid</code> / <code>xforms-invalid</code> events
     * depending on the current <code>valid</code> state.
     *
     * @param valid the current <code>valid</code> state.
     */
    public void validChanged(boolean valid) {
        try {
            if (valid) {
                this.boundElement.getContainerObject().dispatch(this.boundElement.getTarget(), EventFactory.VALID, null);
            }
            else {
                this.boundElement.getContainerObject().dispatch(this.boundElement.getTarget(), EventFactory.INVALID, null);
            }
        }
        catch (XFormsException e) {
            // todo: error handling
            e.printStackTrace();
        }
    }

    // private helper

    private ModelItem getModelItem() {
        if (this.boundElement.isBound()) {
            Model model = this.boundElement.getModel();
            String instanceId = this.boundElement.getInstanceId();
            String locationPath = this.boundElement.getLocationPath();

            return model.getInstance(instanceId).getModelItem(locationPath);
        }

        return null;
    }

    private void initOrUpdatePath(ModelItem item) {
        if (this.handlePath) {
            String xpath = null;
            if (item != null) {
                Model model = this.boundElement.getModel();
                String instanceId = this.boundElement.getInstanceId();
                String locationPath = this.boundElement.getLocationPath();

                xpath = model.getInstance(instanceId).getPointer(locationPath).asPath();
            }

            setAttributeValue("xpath", xpath);
        }
    }

    private void initProperties(ModelItem item) {
        if (this.handleProperties) {
            if (item != null) {
                // register with model item as listener
                item.register(this);

                // set the initial validity to true to prevent alerts when the
                // user views the form for the first time !!!
                boolean valid = ignoreInitialValidity()
                        ? true
                        : item.isValid();

                // set current properties
                setAttributeValue("valid", String.valueOf(valid));
                setAttributeValue("readonly", String.valueOf(item.isReadonly()));
                setAttributeValue("required", String.valueOf(item.isRequired()));
                setAttributeValue("enabled", String.valueOf(item.isEnabled()));
            }
            else {
                // set default properties and disabled
                setAttributeValue("valid", String.valueOf(true));
                setAttributeValue("readonly", String.valueOf(false));
                setAttributeValue("required", String.valueOf(false));
                setAttributeValue("enabled", String.valueOf(false));
            }
        }
        else {
            // set default properties
            setAttributeValue("valid", String.valueOf(true));
            setAttributeValue("readonly", String.valueOf(false));
            setAttributeValue("required", String.valueOf(false));
            setAttributeValue("enabled", String.valueOf(true));
        }
    }

    private void updateProperties(ModelItem item) {
        if (this.handleProperties) {
            if (item != null) {
                // re-register with model item as listener (just in case the instance
                // has been replaced by a submission)
                // todo: handle instance data lifecycle better
                item.deregister(this);
                item.register(this);

                // set current properties
                setAttributeValue("valid", String.valueOf(item.isValid()));
                setAttributeValue("readonly", String.valueOf(item.isReadonly()));
                setAttributeValue("required", String.valueOf(item.isRequired()));
                setAttributeValue("enabled", String.valueOf(item.isEnabled()));
            }
            else {
                // set default properties and disabled
                setAttributeValue("valid", String.valueOf(true));
                setAttributeValue("readonly", String.valueOf(false));
                setAttributeValue("required", String.valueOf(false));
                setAttributeValue("enabled", String.valueOf(false));
            }
        }
    }

    private void disposeProperties(ModelItem item) {
        if (this.handleProperties) {
            if (item != null) {
                item.deregister(this);
            }
        }
    }

    private void initOrUpdateTypes(ModelItem item) {
        if (this.handleTypes) {
            String datatype = null;
            String p3ptype = null;
            if (item != null) {
                datatype = item.getDatatype();

                // check for xsi:type
                if (datatype == null || datatype.length() == 0) {
                    Object node = item.getNode();
                    if (node instanceof Element) {
                        datatype = ((Element) node).getAttributeNS(NamespaceCtx.XMLSCHEMA_INSTANCE_NS, "type");
                    }
                }

                // check for defaulting
                if (datatype == null || datatype.length() == 0) {
                    datatype = "string";
                }

                p3ptype = item.getP3PType();
            }

            setAttributeValue("type", datatype);
            setAttributeValue("p3ptype", p3ptype);
        }
    }

    private void initOrUpdateValue(ModelItem item) {
        if (this.handleValue) {
            String value = null;
            if (item != null) {
                value = item.getValue();
            }

            setElementValue(value);
        }
    }

    private boolean ignoreInitialValidity() {
        try {
            return Boolean.valueOf(Config.getInstance().getProperty(IGNORE_PROPERTY, IGNORE_DEFAULT))
                    .booleanValue();
        }
        catch (Exception e) {
            return Boolean.valueOf(IGNORE_DEFAULT).booleanValue();
        }
    }
}

// end of class
