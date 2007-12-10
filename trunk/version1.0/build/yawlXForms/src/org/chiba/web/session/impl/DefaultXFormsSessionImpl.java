package org.chiba.web.session.impl;

import org.apache.log4j.Logger;
import org.chiba.adapter.ui.UIGenerator;
import org.chiba.web.WebAdapter;
import org.chiba.web.session.XFormsSession;
import org.chiba.web.session.XFormsSessionManager;

import java.util.HashMap;
import java.util.Map;

/**
 * encapsulates the objects needed by a Chiba form session.
 *
 * @author joern turner</a>
 * @version $Id: DefaultXFormsSessionImpl.java,v 1.2 2006/09/29 23:55:01 unl Exp $
 *
 * todo: should use something different than simple timestamps as session keys for security reasons.
 * todo: These are easily guessable so a malicious program might easily get access to open sessions.
 *
 */
public class DefaultXFormsSessionImpl implements XFormsSession {
    private static final Logger LOGGER = Logger.getLogger(DefaultXFormsSessionImpl.class);

    private WebAdapter adapter;
    private UIGenerator uiGenerator;
    private String key;
//    public static final String ADAPTER_PREFIX = "A";
//    public static final String UIGENERATOR_PREFIX = "U";
    private Map properties;
    private XFormsSessionManager xformsSessionManager;
    private long lastUseTime = 0L;

    public DefaultXFormsSessionImpl(XFormsSessionManager manager) {
        this.lastUseTime = System.currentTimeMillis();
        this.key = "" + this.lastUseTime;
        this.xformsSessionManager = manager;
        this.properties = new HashMap();

    }

    public Object getProperty(String propertyId) {
        this.lastUseTime = System.currentTimeMillis();
        return this.properties.get(propertyId);
    }

    public void setProperty(String id, Object property) {
        this.lastUseTime = System.currentTimeMillis();
        this.properties.put(id, property);
    }

    public void removeProperty(String id) {
        this.lastUseTime = System.currentTimeMillis();
        this.properties.remove(id);
    }

    public String getKey() {
        this.lastUseTime = System.currentTimeMillis();
        return this.key;
    }

    public WebAdapter getAdapter() {
        this.lastUseTime = System.currentTimeMillis();
        return adapter;
    }

    public void setAdapter(WebAdapter adapter) {
        this.lastUseTime = System.currentTimeMillis();
        this.adapter = adapter;
    }

    public long getLastUseTime() {
        return this.lastUseTime;
    }

    public XFormsSessionManager getManager() {
        return this.xformsSessionManager;
    }



/*
    public UIGenerator getUIGenerator() {
        return uiGenerator;
    }

    public void setUIGenerator(UIGenerator uiGenerator) {
        this.uiGenerator = uiGenerator;
    }
*/

}
