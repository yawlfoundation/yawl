package org.chiba.adapter.flux.test;
import junit.framework.TestCase;
import org.chiba.adapter.ChibaAdapter;
import org.chiba.adapter.ChibaEvent;
import org.chiba.adapter.DefaultChibaEventImpl;
import org.chiba.adapter.flux.EventLog;
import org.chiba.adapter.flux.FluxAdapter;
import org.chiba.xml.util.DOMUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by IntelliJ IDEA.
 * User: joernturner
 * Date: Oct 17, 2005
 * Time: 9:42:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class FluxAdapterTest extends TestCase{
    private ChibaAdapter fluxAdapter;

    protected void setUp() throws Exception {
        Document doc = getXmlResource("actions.xhtml");
        this.fluxAdapter = new FluxAdapter();
        fluxAdapter.setXForms(doc);
        fluxAdapter.init();
    }

    protected void tearDown() throws Exception {
        this.fluxAdapter.shutdown();
        this.fluxAdapter=null;
    }

    public void testDispatch() throws Exception{
        ChibaEvent event = new DefaultChibaEventImpl();
        //event.initEvent("SETVALUE","input1","foo");
        event.initEvent("flux-action-event","btn1",null);

        //fluxAdapter.dispatch(event);
        //EventLog eventLog = (EventLog) fluxAdapter.getContextParam("EVENT-LOG");
        //Document events =  eventLog.getLog();
        //DOMUtil.prettyPrintDOM(events);

        EventLog eventLog;
//        Document events;
        Element events;

        for(int i=0; i<1000;i++){
            fluxAdapter.dispatch(event);
            eventLog = (EventLog) fluxAdapter.getContextParam("EVENT-LOG");
            events =  eventLog.getLog();
            DOMUtil.prettyPrintDOM(events);
        }


    }


    private Document getXmlResource(String fileName) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);

        // Create builder.
        DocumentBuilder builder = factory.newDocumentBuilder();

        // Parse files.
        return builder.parse(getClass().getResourceAsStream(fileName));
    }


}
