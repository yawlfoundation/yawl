/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package au.edu.qut.yawl.wsif;

import au.edu.qut.yawl.engine.interfce.AuthenticationConfig;
import org.apache.wsif.*;
import org.apache.wsif.providers.ProviderUtils;
import org.apache.wsif.providers.soap.apachesoap.WSIFDynamicProvider_ApacheSOAP;
import org.apache.wsif.util.WSIFPluggableProviders;
import org.apache.wsif.util.WSIFUtils;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import javax.wsdl.*;
import javax.xml.namespace.QName;
import java.util.*;


/**
 * @author Sanjiva Weerawarana
 * @author Alekander Slominski
 * @author Lachlan Aldred
 */

public class WSIFInvoker {
    public static HashMap invokeMethod(String wsdlLocation,
                                       String portName, String operationName,
                                       Element inputDataDoc,
                                       AuthenticationConfig authconfig) {
        System.out.println("XMLOutputter = " + new XMLOutputter(Format.getPrettyFormat()).outputString(inputDataDoc));
        System.out.println("wsdl location = " + wsdlLocation);
        System.out.println("port name = " + portName);
        System.out.println("operation name = " + operationName);

        List argsV = new ArrayList();
        for (int i = 0; i < inputDataDoc.getChildren().size(); i++) {
            Element element = (Element) inputDataDoc.getChildren().get(i);
            argsV.add(element.getText());
        }
        String[] args = new String[argsV.size()];
        argsV.toArray(args);
        return invokeMethod(
                wsdlLocation, operationName,
                null, null,
                portName, null,
                args, 0,
                authconfig);
    }


    public static HashMap invokeMethod(String wsdlLocation, String operationName,
                                       String inputName, String outputName,
                                       String portName, String protocol,
                                       String[] args, int argShift,
                                       AuthenticationConfig authconfig) {

        for (int i = 0; i < args.length; i++) {
            System.out.println("argValue: " + args[i]);
        }

        HashMap map = new HashMap();
        try {
            String serviceNS = null;
            String serviceName = null;
            String portTypeNS = null;
            String portTypeName = null;

            // The default SOAP provider is the Apache AXIS provider. If soap was specified
            // then change the Apache SOAP provider
            if ("soap".equals(protocol)) {
                WSIFPluggableProviders.overrideDefaultProvider(
                        "http://schemas.xmlsoap.org/wsdl/soap/",
                        new WSIFDynamicProvider_ApacheSOAP());
            }

            System.out.println("Reading WSDL document from '" + wsdlLocation + "'");
            Definition wsdlDefinition = null;
            if (authconfig == null) {
                return null;
            }

            String userName = authconfig.getUserName();
            String password = authconfig.getPassword();
            String proxyHost = authconfig.getProxyHost();
            String proxyPort = authconfig.getProxyPort();

            if (userName != null && userName.length() > 0
                    && password != null && password.length() > 0
                    && proxyHost != null && password.length() > 0
                    && proxyPort != null && proxyPort.length() > 0) {
                System.getProperties().put("http.proxyHost", proxyHost);
                System.getProperties().put("http.proxyPort", proxyPort);

                java.net.PasswordAuthentication pa = new java.net.PasswordAuthentication(
                        userName, password.toCharArray());
                wsdlDefinition = WSIFUtils.readWSDLThroughAuthProxy(wsdlLocation, pa);
            } else {
                wsdlDefinition = WSIFUtils.readWSDL(null, wsdlLocation);
            }
            System.out.println("Preparing WSIF dynamic invocation");

            Service service = WSIFUtils.selectService(wsdlDefinition, serviceNS, serviceName);

            Map portTypes = WSIFUtils.getAllItems(wsdlDefinition, "PortType");
            // Really there should be a way to specify the portType
            // for now just try to find one with the portName
            if (portTypes.size() > 1 && portName != null) {
                for (Iterator i = portTypes.keySet().iterator(); i.hasNext();) {
                    QName qn = (QName) i.next();
                    System.out.println("qn.getLocalPart() = " + qn.getLocalPart());
                    if (portName.equals(qn.getLocalPart())) {
                        portTypeName = qn.getLocalPart();
                        portTypeNS = qn.getNamespaceURI();
                        System.out.println("portTypeName = " + portTypeName);
                        System.out.println("portTypeNS = " + portTypeNS);
                        break;
                    }
                }
            }
            PortType portType = WSIFUtils.selectPortType(wsdlDefinition, portTypeNS, portTypeName);
            WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
            WSIFService dpf = factory.getService(wsdlDefinition, service, portType);
            WSIFMessage ctx = dpf.getContext();
            ctx.setObjectPart(WSIFConstants.CONTEXT_HTTP_PROXY_USER, authconfig.getUserName());
            ctx.setObjectPart(WSIFConstants.CONTEXT_HTTP_PROXY_PSWD, authconfig.getPassword());
            dpf.setContext(ctx);
            WSIFPort port = null;
            if (portName == null) {
                port = dpf.getPort();
            } else {
                port = dpf.getPort(portName);
            }

            if (inputName == null && outputName == null) {
                // retrieve list of operations
                List operationList = portType.getOperations();

                // try to find input and output names for the operation specified
                boolean found = false;
                for (Iterator i = operationList.iterator(); i.hasNext();) {
                    Operation op = (Operation) i.next();
                    String name = op.getName();
                    if (!name.equals(operationName)) {
                        continue;
                    }
                    if (found) {
                        throw new RuntimeException(
                                "Operation '" +
                                operationName +
                                "' is overloaded. " +
                                "Please specify the operation in the form " +
                                "'operationName:inputMessageName:outputMesssageName'" +
                                " to distinguish it");
                    }
                    found = true;
                    Input opInput = op.getInput();
                    inputName = (opInput.getName() == null) ? null : opInput.getName();
                    Output opOutput = op.getOutput();
                    outputName = (opOutput.getName() == null) ? null : opOutput.getName();
                }
            }
            WSIFOperation operation =
                    port.createOperation(operationName, inputName, outputName);
            WSIFMessage input = operation.createInputMessage();
            WSIFMessage output = operation.createOutputMessage();
            WSIFMessage fault = operation.createFaultMessage();
            // retrieve list of names and types for input and names for output
            List operationList = portType.getOperations();

            // find portType operation to prepare in/oout message w/ parts
            boolean found = false;
            String[] outNames = new String[0];
            Class[] outTypes = new Class[0];
            for (Iterator i = operationList.iterator(); i.hasNext();) {
                Operation op = (Operation) i.next();
                String name = op.getName();
                if (!name.equals(operationName)) {
                    continue;
                }
                if (found) {
                    throw new RuntimeException("overloaded operations are not supported in this sample");
                }
                found = true;

                //System.err.println("op = "+op);
                Input opInput = op.getInput();

                // first determine list of arguments
                String[] inNames = new String[0];
                Class[] inTypes = new Class[0];
                if (opInput != null) {
                    List parts = opInput.getMessage().getOrderedParts(null);
                    unWrapIfWrappedDocLit(parts, name, wsdlDefinition);
                    int count = parts.size();
                    inNames = new String[count];
                    inTypes = new Class[count];
                    retrieveSignature(parts, inNames, inTypes);
                }
                // now prepare out parameters
                for (int pos = 0; pos < inNames.length; ++pos) {
                    String arg = args[pos + argShift];
                    Object value = null;
                    Class c = inTypes[pos];
                    if (c.equals(String.class)) {
                        value = arg;
                    } else if (c.equals(Double.TYPE)) {
                        value = new Double(arg);
                    } else if (c.equals(Float.TYPE)) {
                        value = new Float(arg);
                    } else if (c.equals(Integer.TYPE)) {
                        value = new Integer(arg);
                    } else if (c.equals(Boolean.TYPE)) {
                        value = new Boolean(arg);
                    } else {
                        throw new RuntimeException("not know how to convert '" + arg + "' into " + c);
                    }
                    input.setObjectPart(inNames[pos], value);
                }
                Output opOutput = op.getOutput();
                if (opOutput != null) {
                    List parts = opOutput.getMessage().getOrderedParts(null);
                    unWrapIfWrappedDocLit(parts, name + "Response", wsdlDefinition);
                    int count = parts.size();
                    outNames = new String[count];
                    outTypes = new Class[count];
                    retrieveSignature(parts, outNames, outTypes);
                }
            }
            if (!found) {
                throw new RuntimeException(
                        "no operation "
                        + operationName
                        + " was found in port type "
                        + portType.getQName());
            }

            System.out.println("Executing operation " + operationName);
            operation.executeRequestResponseOperation(input, output, fault);

            for (int pos = 0; pos < outNames.length; ++pos) {
                String name = outNames[pos];
                map.put(name, output.getObjectPart(name));
            }

            System.getProperties().remove("http.proxyHost");
            System.getProperties().remove("http.proxyPort");
        } catch (WSDLException e) {
            e.printStackTrace();
            System.out.println("" +
                    "\n\n" +
                    "#########################################################################\n" +
                    "###################      Warning From YAWL Engine     ###################\n" +
                    "#########################################################################\n" +
                    "####                                                                     \n" +
                    "####                                                                     \n" +
                    "####            Engine failed to read the WSDL file needed to invoke     \n" +
                    "####            the service.  This is either because:                    \n" +
                    "####               a) The authenication settings are not correct.        \n" +
                    "####                    This can be checked by pointing a browser to     \n" +
                    "####                    http://localhost:8080/yawlWSInvoker and          \n" +
                    "####                    following the on screen instructions.            \n" +
                    "####               b) The WSDL at " + wsdlLocation + " is currently\n" +
                    "####                    unavailable.                                     \n" +
                    "####                                                                     \n" +
                    "####                                                                     \n" +
                    "#########################################################################\n" +
                    "#########################################################################\n" +
                    "#########################################################################\n" +
                    "\n\n");
        } catch (WSIFException e) {
            e.printStackTrace();
        }

        return map;
    }

    private static void retrieveSignature(
            List parts,
            String[] names,
            Class[] types) {
        // get parts in correct order
        for (int i = 0; i < names.length; ++i) {
            Part part = (Part) parts.get(i);
            names[i] = part.getName();
            QName partType = part.getTypeName();
            if (partType == null) {
                partType = part.getElementName();
            }
            if (partType == null) {
                throw new RuntimeException(
                        "part " + names[i] + " must have type name declared");
            }
            // only limited number of types is supported
            // cheerfully ignoring schema namespace ...
            String s = partType.getLocalPart();
            if ("string".equals(s)) {
                types[i] = String.class;
            } else if ("double".equals(s)) {
                types[i] = Double.TYPE;
            } else if ("float".equals(s)) {
                types[i] = Float.TYPE;
            } else if ("int".equals(s)) {
                types[i] = Integer.TYPE;
            } else if ("boolean".equals(s)) {
                types[i] = Boolean.TYPE;
            } else {
                throw new RuntimeException(
                        "part type " + partType + " not supported in this sample");
            }
        }
    }

    /**
     * Unwraps the top level part if this a wrapped DocLit message.
     */
    private static void unWrapIfWrappedDocLit(List parts, String operationName, Definition def) throws WSIFException {
        Part p = ProviderUtils.getWrapperPart(parts, operationName);
        if (p != null) {
            List unWrappedParts = ProviderUtils.unWrapPart(p, def);
            if (unWrappedParts != null && unWrappedParts.size() > 0) {
                parts.remove(p);
                parts.addAll(unWrappedParts);
            }
        }
    }
}


