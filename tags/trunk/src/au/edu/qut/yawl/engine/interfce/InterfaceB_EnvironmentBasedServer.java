/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.engine.interfce;

import au.edu.qut.yawl.elements.data.YParameter;
import au.edu.qut.yawl.worklist.model.Marshaller;
import au.edu.qut.yawl.worklist.model.WorkItemRecord;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.StringTokenizer;


/**
 * 
 * @author Lachlan Aldred
 * Date: 23/01/2004
 * Time: 13:26:04
 * 
 */
public class InterfaceB_EnvironmentBasedServer extends HttpServlet {
    private InterfaceBWebsideController _controller;
    private static final boolean _debug = false;


    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        ServletContext context = servletConfig.getServletContext();
        String controllerClassName =
                context.getInitParameter("InterfaceBWebSideController");
        //If you need to get through an auth proxy firewall and have configured it in the
        //web.xml file they will be retrieved for use.
        String userName = context.getInitParameter("UserName");
        String password = context.getInitParameter("Password");
        String proxyHost = context.getInitParameter("ProxyHost");
        String proxyPort = context.getInitParameter("ProxyPort");
        try {
            Class controllerClass = Class.forName(controllerClassName);
            _controller = (InterfaceBWebsideController) controllerClass.newInstance();
            //here the URL of the YAWL Engine get retrieved from the web.xml file.
            String engineBackendAddress = context.getInitParameter("InterfaceB_BackEnd");
            _controller.setUpInterfaceBClient(engineBackendAddress);
            _controller.setRemoteAuthenticationDetails(
                    userName, password, proxyHost, proxyPort);
            context.setAttribute("controller", _controller);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        if (_debug) {
            System.out.println("\nInterfaceB_EnvironmentBasedServer " +
                    "request.getRequestURL = " + request.getRequestURL());
            System.out.println("InterfaceB_EnvironmentBasedServer::doGet() request.parameters = ");
            Enumeration paramNms = request.getParameterNames();
            while (paramNms.hasMoreElements()) {
                String name = (String) paramNms.nextElement();
                System.out.println("\trequest.getParameter(" + name + ") = " +
                        request.getParameter(name));
            }
        }
        if (request.getParameter("action") != null &&
                request.getParameter("action").equals("ParameterInfoRequest")) {

            YParameter[] params = _controller.describeRequiredParams();

            response.setContentType("text/xml");
            PrintWriter outputWriter = response.getWriter();
            StringBuffer output = new StringBuffer();

            output.append("<params>");
            for (int i = 0; i < params.length; i++) {
                YParameter param = params[i];
                output.append(param.toXML());
            }
            output.append("</params>");

            outputWriter.write(output.toString());
            outputWriter.flush();
            outputWriter.close();
        } else {
            _controller.doGet(request, response);
        }
    }


    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter outputWriter = ServletUtils.prepareResponse(response);
        StringBuffer output = new StringBuffer();
        output.append("<response>");
        output.append(processPostQuery(request));
        output.append("</response>");
        outputWriter.write(output.toString());
        outputWriter.flush();
        outputWriter.close();

    }


    private String processPostQuery(HttpServletRequest request) {
        if (_debug) {
            System.out.println("\nInterfaceB_Server_WebSide::doPost() " +
                    "request.getRequestURL = " + request.getRequestURL());
            System.out.println("InterfaceB_EnvironmentBasedServer::doPost() request.parameters = ");
            Enumeration paramNms = request.getParameterNames();
            while (paramNms.hasMoreElements()) {
                String name = (String) paramNms.nextElement();
                System.out.println("\trequest.getParameter(" + name + ") = " +
                        ServletUtils.urlDecodeData(request.getParameter(name)));
            }
        }
        StringTokenizer tokens = new StringTokenizer(request.getRequestURI(), "/");
        String secondLastPartOfPath = null;
        String lastPartOfPath = null;
        String temp = null;

        while (tokens.hasMoreTokens()) {
            if (temp != null) {
                secondLastPartOfPath = temp;
            }
            temp = tokens.nextToken();
            if (!tokens.hasMoreTokens() && temp != null) {
                lastPartOfPath = temp;
            }
        }
        String action = ServletUtils.urlDecodeData(request.getParameter("action"));
        String workItemXML = ServletUtils.urlDecodeData(request.getParameter("workItem"));
        if ("handleEnabledItem".equals(action)) {
            WorkItemRecord workItem = Marshaller.unmarshalWorkItem(workItemXML);
            _controller.handleEnabledWorkItemEvent(workItem);
        } else if ("cancelWorkItem".equals(action)) {
            WorkItemRecord workItem = Marshaller.unmarshalWorkItem(workItemXML);
            _controller.handleCancelledWorkItemEvent(workItem);
        } else if (InterfaceB_EngineBasedClient.ANNOUNCE_COMPLETE_CASE_CMD.equals(action)) {
            String caseID = ServletUtils.urlDecodeData(request.getParameter("caseID"));
            String casedata = ServletUtils.urlDecodeData(request.getParameter("casedata"));
            _controller.handleCompleteCaseEvent(caseID, casedata);
        }
        if (_debug) {
        }
        return "<success/>";
    }


}
