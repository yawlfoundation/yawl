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
package org.chiba.adapter.web;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.log4j.Category;
import org.chiba.adapter.InteractionHandler;
import org.chiba.xml.xforms.ChibaBean;
import org.chiba.xml.xforms.config.Config;
import org.chiba.xml.xforms.events.EventFactory;
import org.chiba.xml.xforms.exception.XFormsException;
import org.chiba.xml.xforms.ui.Repeat;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * Default implementation for handling http servlet requests.
 *
 * @author joern turner
 * @version $Id: HttpRequestHandler.java,v 1.12 2004/12/07 15:19:05 joernt Exp $
 */
public class HttpRequestHandler implements InteractionHandler {
    private static final Category LOGGER = Category.getInstance(HttpRequestHandler.class);
    public static final String DATA_PREFIX_PROPERTY = "chiba.web.dataPrefix";
    public static final String TRIGGER_PREFIX_PROPERTY = "chiba.web.triggerPrefix";
    public static final String SELECTOR_PREFIX_PROPERTY = "chiba.web.selectorPrefix";
    public static final String REMOVE_UPLOAD_PREFIX_PROPERTY = "chiba.web.removeUploadPrefix";
    public static final String DATA_PREFIX_DEFAULT = "d_";
    public static final String TRIGGER_PREFIX_DEFAULT = "t_";
    public static final String SELECTOR_PREFIX_DEFAULT = "s_";
    public static final String REMOVE_UPLOAD_PREFIX_DEFAULT = "ru_";

    private ChibaBean chibaBean;

    private String dataPrefix;
    private String selectorPrefix;
    private String triggerPrefix;
    private String removeUploadPrefix;
    private String uploadRoot;


    public HttpRequestHandler(ChibaBean chibaBean) {
        this.chibaBean = chibaBean;
    }

    /**
     * executes this handler.
     *
     * @throws XFormsException
     */
    public void execute() throws XFormsException {
        HttpServletRequest request = (HttpServletRequest) this.chibaBean.getContext().get(ServletAdapter.HTTP_SERVLET_REQUEST);

        String contextRoot = request.getSession().getServletContext().getRealPath("");
        if (contextRoot == null) {
            contextRoot = request.getSession().getServletContext().getRealPath(".");
        }

        String uploadDir = (String) this.chibaBean.getContext().get(ServletAdapter.HTTP_UPLOAD_DIR);
        this.uploadRoot = new File(contextRoot, uploadDir).getAbsolutePath();

        handleRequest(request);
    }

    /**
     * checks whether we have multipart or urlencoded request and processes it accordingly. After updating
     * the data, a reacalculate, revalidate refresh sequence is fired and the found trigger is executed.
     *
     * @param request Servlet request
     * @throws org.chiba.xml.xforms.exception.XFormsException
     *          todo: implement action block behaviour
     */
    protected void handleRequest(HttpServletRequest request) throws XFormsException {
        String trigger = null;

        // Check that we have a file upload request
        boolean isMultipart = FileUpload.isMultipartContent(request);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("request isMultipart: " + isMultipart);
            LOGGER.debug("base URI: " + this.chibaBean.getBaseURI());
            LOGGER.debug("user agent: " + request.getHeader("User-Agent"));
        }

        if (isMultipart) {
            trigger = processMultiPartRequest(request, trigger);
        } else {
            trigger = processUrlencodedRequest(request, trigger);
        }

        // finally activate trigger if any
        if (trigger != null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("trigger '" + trigger + "'");
            }

            this.chibaBean.dispatch(trigger, EventFactory.DOM_ACTIVATE);
        }
    }

    /**
     * @param request Servlet request
     * @param trigger Trigger control
     * @return the calculated trigger
     * @throws XFormsException If an error occurs
     */
    protected String processMultiPartRequest(HttpServletRequest request, String trigger) throws XFormsException {
        DiskFileUpload upload = new DiskFileUpload();

        String encoding = request.getCharacterEncoding();
        if (encoding == null) {
            encoding = "ISO-8859-1";
        }

        upload.setRepositoryPath(this.uploadRoot);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("root dir for uploads: " + this.uploadRoot);
        }

        List items;
        try {
            items = upload.parseRequest(request);
        } catch (FileUploadException fue) {
            throw new XFormsException(fue);
        }

        Map formFields = new HashMap();
        Iterator iter = items.iterator();
        while (iter.hasNext()) {
            FileItem item = (FileItem) iter.next();
            String itemName = item.getName();
            String fieldName = item.getFieldName();
            String id = fieldName.substring(Config.getInstance().getProperty("chiba.web.dataPrefix").length());

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Multipart item name is: " + itemName
                        + " and fieldname is: " + fieldName
                        + " and id is: " + id);
                LOGGER.debug("Is formfield: " + item.isFormField());
                LOGGER.debug("Content: " + item.getString());
            }

            if (item.isFormField()) {

                // check for upload-remove action
                if (fieldName.startsWith(getRemoveUploadPrefix())) {
                  id = fieldName.substring(getRemoveUploadPrefix().length());
                  // if data is null, file will be removed ...
                  // TODO: remove the file from the disk as well
                  chibaBean.updateControlValue(id, "", "", null);
                  continue;
                }

                // It's a field name, it means that we got a non-file
                // form field. Upload is not required. We must treat it as we
                // do in processUrlencodedRequest()
                processMultipartParam(formFields, fieldName, item, encoding);
            } else {

                String uniqueFilename = new File(
                    getUniqueParameterName("file"),
                    new File(itemName).getName()).getPath();

                File savedFile = new File(this.uploadRoot, uniqueFilename);

                byte[] data = null;

                data = processMultiPartFile(item, id, savedFile, encoding, data);

                // if data is null, file will be removed ...
                // TODO: remove the file from the disk as well
                chibaBean.updateControlValue(id, item.getContentType(),
                    itemName, data);
            }
            
            // handle regular fields
            if (formFields.size() > 0) {

                Iterator it = formFields.keySet().iterator();
                while(it.hasNext()) {

                    fieldName = (String)it.next();
                    String [] values = (String[]) formFields.get(fieldName);
                    
                    // [1] handle data
                    handleData(fieldName, values);

                    // [2] handle selector
                    handleSelector(fieldName, values[0]);

                    // [3] handle trigger
                    trigger = handleTrigger(trigger, fieldName);
                }
            }
        }

        return trigger;
    }

    protected String processUrlencodedRequest(HttpServletRequest request, String trigger) throws XFormsException {
        // iterate request parameters
        Enumeration names = request.getParameterNames();
        while (names.hasMoreElements()) {
            String paramName = names.nextElement().toString();
            String[] values = request.getParameterValues(paramName);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(this + " parameter-name: " + paramName);
                for (int i = 0; i < values.length; i++) {
                    LOGGER.debug(this + " value: " + values[i]);
                }
            }

            // [1] handle data
            handleData(paramName, values);

            // [2] handle selector
            handleSelector(paramName, values[0]);

            // [3] handle trigger
            trigger = handleTrigger(trigger, paramName);
        }
        return trigger;
    }

    /**
     * @param name
     * @throws XFormsException
     */
    protected void handleData(String name, String[] values)
            throws XFormsException {
        if (name.startsWith(getDataPrefix())) {
            String id = name.substring(getDataPrefix().length());

            // assemble new control value
            String newValue;

            if (values.length > 1) {
                StringBuffer buffer = new StringBuffer(values[0]);

                for (int i = 1; i < values.length; i++) {
                    buffer.append(" ").append(values[i]);
                }

                newValue = buffer.toString().trim();
            } else {
                newValue = values[0];
            }

            this.chibaBean.updateControlValue(id, newValue);
        }
    }

    /**
     * @param name
     * @throws XFormsException
     */
    protected void handleSelector(String name, String value) throws XFormsException {
        if (name.startsWith(getSelectorPrefix())) {
            int separator = value.lastIndexOf(':');

            String id = value.substring(0, separator);
            int index = Integer.valueOf(value.substring(separator + 1)).intValue();

            Repeat repeat = (Repeat) this.chibaBean.getContainer().lookup(id);
            repeat.setIndex(index);
        }
    }

    protected String handleTrigger(String trigger, String name) {
        if ((trigger == null) && name.startsWith(getTriggerPrefix())) {
            String parameter = name;
            int x = parameter.lastIndexOf(".x");
            int y = parameter.lastIndexOf(".y");

            if (x > -1) {
                parameter = parameter.substring(0, x);
            }

            if (y > -1) {
                parameter = parameter.substring(0, y);
            }

            // keep trigger id
            trigger = name.substring(getTriggerPrefix().length());
        }
        return trigger;
    }

    private byte[] processMultiPartFile(FileItem item, String id, File savedFile, String encoding, byte[] data)
            throws XFormsException {
        // some data uploaded ...
        if (item.getSize() > 0) {

            if (chibaBean.storesExternalData(id)) {

                // store data to file and create URI
                try {
                    savedFile.getParentFile().mkdir();
                    item.write(savedFile);
                } catch (Exception e) {
                    throw new XFormsException(e);
                }
                // content is URI in this case
                try {
                    data = savedFile.toURI().toString().getBytes(encoding);
                } catch(UnsupportedEncodingException e) {
                    throw new XFormsException(e);
                }

            } else {
                // content is the data
                data = item.get();
            }
        }
        return data;
    }

    private void processMultipartParam(Map formFields, String fieldName, FileItem item, String encoding) throws XFormsException {
        String values[] = (String[]) formFields.get(fieldName);
        String formFieldValue = null;
        try {
            formFieldValue = item.getString(encoding);
        } catch (UnsupportedEncodingException e) {
            throw new XFormsException(e.getMessage(), e);
        }

        if (values == null) {
            formFields.put(fieldName, new String[]{ formFieldValue });
        } else {
            // not very effective, but not many duplicate values
            // expected either ...
            String [] tmp = new String[values.length+1];
            System.arraycopy(values, 0, tmp, 0, values.length);
            tmp[values.length] = formFieldValue;
            formFields.put(fieldName, tmp);
        }
    }


    /**
     * returns the prefix which is used to identify trigger parameters.
     *
     * @return the prefix which is used to identify trigger parameters
     */
    protected final String getTriggerPrefix() {
        if (this.triggerPrefix == null) {
            try {
                this.triggerPrefix =
                        Config.getInstance().getProperty(TRIGGER_PREFIX_PROPERTY, TRIGGER_PREFIX_DEFAULT);
            } catch (Exception e) {
                this.triggerPrefix = TRIGGER_PREFIX_DEFAULT;
            }
        }

        return this.triggerPrefix;
    }

    protected final String getDataPrefix() {
        if (this.dataPrefix == null) {
            try {
                this.dataPrefix = Config.getInstance().getProperty(DATA_PREFIX_PROPERTY, DATA_PREFIX_DEFAULT);
            } catch (Exception e) {
                this.dataPrefix = DATA_PREFIX_DEFAULT;
            }
        }

        return this.dataPrefix;
    }

    protected final String getRemoveUploadPrefix() {
        if (this.removeUploadPrefix == null) {
            try {
                this.removeUploadPrefix = Config.getInstance().getProperty(REMOVE_UPLOAD_PREFIX_PROPERTY, REMOVE_UPLOAD_PREFIX_DEFAULT);
            } catch (Exception e) {
                this.removeUploadPrefix = REMOVE_UPLOAD_PREFIX_DEFAULT;
            }
        }

        return this.removeUploadPrefix;
    }


    private String getUniqueParameterName(String prefix) {
        return prefix + Integer.toHexString((int) (Math.random() * 10000));
    }

    /**
     * returns the configured prefix which identifies 'selector' parameters. These are used to transport
     * the state of repeat indices via http.
     *
     * @return the prefix for selector parameters from the configuration
     */
    public final String getSelectorPrefix() {
        if (this.selectorPrefix == null) {
            try {
                this.selectorPrefix =
                        Config.getInstance().getProperty(SELECTOR_PREFIX_PROPERTY,
                                SELECTOR_PREFIX_DEFAULT);
            } catch (Exception e) {
                this.selectorPrefix = SELECTOR_PREFIX_DEFAULT;
            }
        }

        return this.selectorPrefix;
    }

    /**
     * Get the value of chibaBean.
     *
     * @return the value of chibaBean
     */
    public ChibaBean getChibaBean() {
	return this.chibaBean;
    }

}

// end of class


