package org.yawlfoundation.yawl.wsif;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;

import javax.swing.*;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.yawlfoundation.yawl.engine.interfce.AuthenticationConfig;

/**
 /**
 * 
 * @author Lachlan Aldred
 * Date: 16/03/2004
 * Time: 12:31:17
 * 
 */
public class TestWSIFInvoker extends TestCase {
    private static AuthenticationConfig _authconfig;
    private SAXBuilder _builder;

    public TestWSIFInvoker(String name) {
        super(name);
    }

    public void setUp() {
        setUpAuth();
        _builder = new SAXBuilder();
    }

    public void testNewWay() {
        HashMap map = null;
        try {
            Document doc = _builder.build(new StringReader("<data><input>33040</input></data>"));
            map = WSIFInvoker.invokeMethod(
                    "http://www.xmethods.net/sd/2001/TemperatureService.wsdl",
                    "", "getTemp",
                    doc.getRootElement(),
                    _authconfig);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertTrue(map.size() == 1);
        assertTrue(map.toString(), map.containsKey("return"));
    }


    public void testGetQuote() {
        HashMap map = null;
        try {
            Document doc = _builder.build(new StringReader("<data><input>NAB.AX</input></data>"));
            map = WSIFInvoker.invokeMethod(
                    "http://services.xmethods.net/soap/urn:xmethods-delayed-quotes.wsdl",
                    "", "getQuote",
                    doc.getRootElement(),
                    _authconfig);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        System.out.println("map = " + map);
        assertTrue(map.size() == 1);
        assertTrue(map.toString(), map.containsKey("Result"));
    }


    public void testGetBookPrice() {
        HashMap map = null;
        try {
            Document doc = _builder.build(
                    new StringReader("<data><input>0-201-61641-6</input></data>"));//A book about XP
            map = WSIFInvoker.invokeMethod(
                    "http://www.xmethods.net/sd/2001/BNQuoteService.wsdl",
                    "", "getPrice",
                    doc.getRootElement(),
                    _authconfig);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        System.out.println("map = " + map);
        assertTrue(map.size() == 1);
        assertTrue(map.toString(), map.containsKey("return"));
        Float result =  (Float) map.get("return");
        assertTrue(map.toString(), "29.95".equals(result.toString()));
    }


    public static Map invokeOldWay(String [] args){
        //taken from the command line version of clients.DynamicInvoker
        if (args.length < 2) {
            usage();
        }
        String wsdlLocation = args.length > 0 ? args[0] : null;
        String operationKey = args.length > 1 ? args[1] : null;
        String portName = null;
        String operationName = null;
        String inputName = null;
        String outputName = null;
        //break down the operation name
        StringTokenizer st = new StringTokenizer(operationKey, ":");
        int tokens = st.countTokens();
        int specType = 0;
        if (tokens == 2) {
            specType = operationKey.endsWith(":") ? 1 : 2;
        } else if (tokens != 1 && tokens != 3) {
            usage();
        }
        while (st.hasMoreTokens()) {
            if (operationName == null){
                operationName = st.nextToken();
            }
            else if (inputName == null && specType != 2){
                inputName = st.nextToken();
            }
            else if (outputName == null){
                outputName = st.nextToken();
            }
            else{
                break;
            }
        }
        try {
            portName =
                    operationName.substring(operationName.indexOf("(") + 1, operationName.indexOf(")"));
            operationName = operationName.substring(0, operationName.indexOf("("));
        } catch (Exception ignored) {
        }
        String protocol = args.length > 2 ? args[2] : "";
        int shift = 2;
        if (protocol.equals("soap") || protocol.equals("axis")) {
            shift = 3;
        }
        HashMap map = null;
        try {
            map = WSIFInvoker.invokeMethod(
                                    wsdlLocation,
                                    operationName,
                                    inputName,
                                    outputName,
                                    portName,
                                    protocol,
                                    args,
                                    shift,
                                    _authconfig);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        return null;
    }


    public static void usage() {
        System.err.println(
                "Usage: java "
                + WSIFInvoker.class.getName()
                + " wsdlLocation "
                + "operationName[(portName)]:[inputMessageName]:[outputMessageName] "
                + "[soap|axis] [argument1 ...]");
        System.exit(1);
    }



    public static void main(String args[]) {
        if(args.length >= 2){
            setUpAuth();
            Map result = invokeOldWay(args);
            System.out.println("result = " + result);
        } else {
            TestRunner runner = new TestRunner();
            runner.doRun(suite());
            System.exit(0);
        }
    }

    private static void setUpAuth() {
        _authconfig = AuthenticationConfig.getInstance();
        if(_authconfig.getPassword() == null){
            String userName = JOptionPane.showInputDialog("Enter UserName");
            String password = JOptionPane.showInputDialog("Enter Password");
            _authconfig.setProxyAuthentication(userName, password, "proxy.yawlfoundation.org", "3128");
        }
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(TestWSIFInvoker.class);
        return suite;
    }
}