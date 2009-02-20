package org.chiba.web;

import org.apache.log4j.Logger;
import org.chiba.adapter.AbstractChibaAdapter;
import org.chiba.adapter.ChibaEvent;
import org.chiba.web.flux.FluxAdapter;
import org.chiba.web.servlet.HttpRequestHandler;
import org.chiba.web.session.XFormsSession;
import org.chiba.xml.dom.DOMUtil;
import org.chiba.xml.events.ChibaEventNames;
import org.chiba.xml.events.XMLEvent;
import org.chiba.xml.ns.NamespaceConstants;
import org.chiba.xml.xforms.exception.XFormsException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

import javax.xml.transform.TransformerException;

/**
 * Superclass for Adapters used in web applications. Does minimal event listening on the processor and provides
 * a common base to build webadapers.
 *
 * @author Joern Turner
 * @version $Id: WebAdapter.java,v 1.6 2007/02/16 00:47:35 joernt Exp $
 * @see org.chiba.web.flux.FluxAdapter
 * @see org.chiba.web.servlet.ServletAdapter
 *
 */
public class WebAdapter extends AbstractChibaAdapter implements EventListener {
    /**
     * Defines the key for accessing (HTTP) session ids.
     */
    public static final String SESSION_ID = "chiba.session.id";

    private static final Logger LOGGER = Logger.getLogger(FluxAdapter.class);
    protected EventTarget root;
    protected XFormsSession xformsSession;
    protected HttpRequestHandler httpRequestHandler;
    protected XMLEvent exitEvent = null;
    public static final String USERAGENT = "useragent";
    public static final String REQUEST_URI = "requestURI";

    public WebAdapter() {
        this.chibaBean = createXFormsProcessor();
    }


    public void setXFormsSession(XFormsSession xFormsSession) {
        this.xformsSession = xFormsSession;
    }

    public void setExitEvent(XMLEvent event){
        this.exitEvent = event;
    }

    /**
     * initialize the Adapter. This is necessary cause often the using
     * application will need to configure the Adapter before actually using it.
     *
     * @throws org.chiba.xml.xforms.exception.XFormsException
     */
    public void init() throws XFormsException {
        // get docuent root as event target in order to capture all events
        this.root = (EventTarget) this.chibaBean.getXMLContainer().getDocumentElement();

        // interaction events my occur during init so we have to register before
        this.root.addEventListener(ChibaEventNames.LOAD_URI, this, true);
        this.root.addEventListener(ChibaEventNames.RENDER_MESSAGE, this, true);
        this.root.addEventListener(ChibaEventNames.REPLACE_ALL, this, true);

        configureSession();
        
        // init processor
        this.chibaBean.init();

    }

    private void configureSession() throws XFormsException {
        Document hostDocument = this.chibaBean.getXMLContainer();
        Element root = hostDocument.getDocumentElement();
        Element keepAlive = DOMUtil.findFirstChildNS(root, NamespaceConstants.CHIBA_NS,"keepalive");
        if(keepAlive != null){
            String pulse = keepAlive.getAttributeNS(null,"pulse");
            if(!(pulse == null || pulse.equals(""))){
                xformsSession.setProperty(XFormsSession.KEEPALIVE_PULSE,pulse);
            }
        }
    }

    public XMLEvent checkForExitEvent() {
        return this.exitEvent;
    }

    /**
     * Dispatch a ChibaEvent to trigger some XForms processing such as updating
     * of values or execution of triggers.
     *
     * @param event an application specific event
     * @throws org.chiba.xml.xforms.exception.XFormsException
     * @see org.chiba.adapter.DefaultChibaEventImpl
     */
    public void dispatch(ChibaEvent event) throws XFormsException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Event " + event.getEventName() + " dispatched");
            LOGGER.debug("Event target: " + event.getId());
            try {
                if(this.chibaBean != null){
                    DOMUtil.prettyPrintDOM(this.chibaBean.getXMLContainer(),System.out);
                }
            } catch (TransformerException e) {
                throw new XFormsException(e);
            }
        }

    }

    /**
     * listen to processor and add a DefaultChibaEventImpl object to the
     * EventQueue.
     *
     * @param event the handled DOMEvent
     */
    public void handleEvent(Event event) {
        if(LOGGER.isDebugEnabled()){
            LOGGER.debug("handleEvent: " + event.getType());
        }
    }

    /**
     * terminates the XForms processing. right place to do cleanup of
     * resources.
     *
     * @throws org.chiba.xml.xforms.exception.XFormsException
     */
    public void shutdown() throws XFormsException {
        // shutdown processor
        if(this.chibaBean != null){
            this.chibaBean.shutdown();
            this.chibaBean = null;
        }

        // deregister for interaction events if any
        if(this.root != null){
            this.root.removeEventListener(ChibaEventNames.LOAD_URI, this, true);
            this.root.removeEventListener(ChibaEventNames.RENDER_MESSAGE, this, true);
            this.root.removeEventListener(ChibaEventNames.REPLACE_ALL, this, true);
            this.root = null;
        }
    }

    protected HttpRequestHandler getHttpRequestHandler() {
        if (this.httpRequestHandler == null) {
            this.httpRequestHandler = new HttpRequestHandler(this.chibaBean);
            this.httpRequestHandler.setUploadRoot(this.uploadDestination);
            this.httpRequestHandler.setSessionKey(this.xformsSession.getKey());
        }

        return this.httpRequestHandler;
    }
}
