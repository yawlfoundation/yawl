/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.util;

import javax.xml.soap.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

/**
 * @author Michael Adams
 * @date 24/5/17
 */
public class SoapClient {

    private static final String NS_PREFIX = "yns";       // a candidate namespace prefix

    private String _endpoint;

    public SoapClient(String endpoint) {
        _endpoint = endpoint;
    }


    /**
     * Sends a request to a web service via SOAP and returns the result
     * @return the response from the web service
     * @throws SOAPException if there's a problem connecting to the target web service
     */
    public String send(String ns, String action, List<String> argKeys, List<String> argValues)
            throws SOAPException, IOException {

        String envelope = createEnvelope(ns, action, argKeys, argValues);
        SOAPMessage message = createMessage(envelope);
        SOAPMessage response = call(message);
        return getResponseValue(response);
    }


    /**
     * Creates an envelope with the required attributes for a service call
     * @param ns the namespace
     * @param action the action
     * @param argKeys argument names (may be null)
     * @param argValues argument values
     * @return the created envelope
     * @throws SOAPException
     */
    private String createEnvelope(String ns, String action, List<String> argKeys, List<String> argValues)
            throws SOAPException {
        if (getSize(argKeys) != getSize(argValues)) {
            throw new SOAPException("argKeys has different size to argValues");
        }

        XNode root = new XNode("soapenv:Envelope");
        root.addAttribute("xmlns:soapenv", "http://schemas.xmlsoap.org/soap/envelope/");
        root.addAttribute("xmlns:" + NS_PREFIX, ns);
        root.addChild("soapenv:Header");
        XNode body = root.addChild("soapenv:Body");
        XNode binding = body.addChild(NS_PREFIX + ":" + action);
        if (argKeys != null) {
            for (int i=0; i<argKeys.size(); i++) {
                binding.addChild(argKeys.get(i), argValues.get(i));
            }
        }
        return root.toString();
    }


    /**
     * Creates a new SOAP message containing the envelope provided
     * @param envelope the envelope to send
     * @return the created message
     * @throws SOAPException
     * @throws IOException
     */
    private SOAPMessage createMessage(String envelope)
            throws SOAPException, IOException {
        MessageFactory factory = MessageFactory.newInstance();
        return factory.createMessage(new MimeHeaders(),
                new ByteArrayInputStream(envelope.getBytes(Charset.forName("UTF-8"))));
    }



    /**
     * Calls the target web service
     * @param message the message to send
     * @return the response message from the service
     * @throws SOAPException if the call fails
     */
    private SOAPMessage call(SOAPMessage message) throws SOAPException {
        SOAPConnection connection = SOAPConnectionFactory.newInstance().createConnection();
      	SOAPMessage response = connection.call(message, _endpoint);
   		connection.close();
        return response;
    }


    /**
     * Extracts the return String from the response message
     * @param response the response message received from a target web service call
     * @return the String value within the response message
     * @throws SOAPException if the message is malformed
     */
    private String getResponseValue(SOAPMessage response) throws SOAPException, IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        response.writeTo(os);
        String s = new String(os.toByteArray(),"UTF-8");
        XNode root = new XNodeParser().parse(s);
        if (root == null) {
            return null;
        }

        // drill down to return node
        XNode child = root.getChild();
        while (child != null && ! child.getName().equals("return")) {
            child = child.getChild();
        }
        
        return child != null ? child.toPrettyString() : null;
    }


    /**
     * @param l a list
     * @return the list's size, or 0 if the list is null
     */
    private int getSize(List<?> l) { return l != null ? l.size() : 0; }


    // a test
    public static void main(String argsv[]) throws Exception {
        String ns = "http://www.kayaposoft.com/enrico/ws/v1.0/";
        String action = "getPublicHolidaysForYear";
        List<String> argKeys = Arrays.asList("year", "country", "region");
        List<String> argValues = Arrays.asList("2017", "usa", "Delaware");

        SoapClient client = new SoapClient("http://kayaposoft.com/enrico/ws/v1.0/index.php");
        String result = client.send(ns, action, argKeys, argValues);
        System.out.println(result);
    }

}
