/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.testService;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.jdom.Element;
import org.yawlfoundation.yawl.cost.data.ExpressionParser;
import org.yawlfoundation.yawl.cost.interfce.CostGatewayClient;
import org.yawlfoundation.yawl.documentStore.DocumentStoreClient;
import org.yawlfoundation.yawl.documentStore.YDocument;
import org.yawlfoundation.yawl.elements.YAWLServiceReference;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.SpecificationData;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.engine.interfce.interfaceA.InterfaceA_EnvironmentBasedClient;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceBWebsideController;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceB_EngineBasedClient;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceB_EnvironmentBasedClient;
import org.yawlfoundation.yawl.engine.interfce.interfaceE.YLogGatewayClient;
import org.yawlfoundation.yawl.logging.XESTimestampComparator;
import org.yawlfoundation.yawl.resourcing.ResourceManager;
import org.yawlfoundation.yawl.resourcing.ResourceMap;
import org.yawlfoundation.yawl.resourcing.TaskPrivileges;
import org.yawlfoundation.yawl.resourcing.WorkQueue;
import org.yawlfoundation.yawl.resourcing.allocators.AbstractAllocator;
import org.yawlfoundation.yawl.resourcing.constraints.AbstractConstraint;
import org.yawlfoundation.yawl.resourcing.datastore.eventlog.ResourceEvent;
import org.yawlfoundation.yawl.resourcing.filters.AbstractFilter;
import org.yawlfoundation.yawl.resourcing.interactions.AbstractInteraction;
import org.yawlfoundation.yawl.resourcing.interactions.AllocateInteraction;
import org.yawlfoundation.yawl.resourcing.interactions.OfferInteraction;
import org.yawlfoundation.yawl.resourcing.interactions.StartInteraction;
import org.yawlfoundation.yawl.resourcing.jsf.dynform.DynTextParser;
import org.yawlfoundation.yawl.resourcing.resource.*;
import org.yawlfoundation.yawl.resourcing.rsInterface.*;
import org.yawlfoundation.yawl.schema.XSDType;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

/**
 * Created by IntelliJ IDEA. User: Default Date: 17/09/2007 Time: 17:48:13 To change this
 * template use File | Settings | File Templates.
 */
public class TestService extends InterfaceBWebsideController {

    Logger _log = Logger.getLogger(this.getClass());

    public TestService() { super(); }

    public void handleEnabledWorkItemEvent(WorkItemRecord enabledWorkItem) {
        String utf8Test = "“Iñtërnâtiônàlizætiøn”";
    }


    public void handleCancelledWorkItemEvent(WorkItemRecord workItemRecord) {

    }


    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
 //       String url = "http://localhost:8080/testService/gateway";
     //   execJSP(url) ;

        response.setContentType("text/html");
        PrintWriter outputWriter = response.getWriter();
        StringBuilder output = new StringBuilder();

        output.append("<html><head><title>YAWL Test Service Welcome Page</title>")
              .append("</head><body><H3>Test Output</H3><p><pre>");

   //     output.append(doOrgDataRefreshTest()) ;
  //      output.append(doGetSpec4Case()) ;
  //      output.append(skipItem());
  //      output.append(testCostModel());
//        output.append(testgetexpiry());
//        output.append(testExprParser());
 //         output.append(testDocStore());
 //       output.append(testConversion());
  //      output.append(testDocumentType());
 //       output.append(doResourceServiceGatewayTest()) ;
   //    output.append(createDummyOrgData());
    //     output.append(doLogGatewayTest()) ;
    //   output.append(doWorkQueueGatewayTest()) ;
    //    output.append(ibTest());
   //     output.append(doRandomTest()) ;
   //     output.append(doGetParticipantsTest()) ;
   //       output.append(controllerTest());
    //    output.append(stressTest());
    //    output.append(wqTest());
   //     output.append(xsdTest());
   //     output.append(getCaseState());
   //     output.append(testSummaries());
   //     output.append(getEngineParametersForRegisteredService());
  // output.append(testDynMultiCompTaskNewInst()) ;
  //       output.append(testJU()) ;
   //     output.append(testGateway());
  //      output.append(getTaskParametersInOrder());
  //      output.append(testClientConnect());
  //      output.append(testXNode());
  //      output.append(testXNodeParser());
    //    output.append(testXNodeComparator());
   //     output.append(testDynTextParser());
   //     output.append(testGetSpecID());
   //     output.append(getTaskPrivileges());
   //     output.append(getDistributionSet());
    //    output.append(testResourceLogGateway());
    //    output.append(testMaxCases());
    //   output.append(testSpecdata());
        output.append(findOutWhatLifeIsAllAbout());
         output.append("</pre></p></body></html>");
         outputWriter.write(output.toString());
         outputWriter.flush();
         outputWriter.close();
    }


      public String findOutWhatLifeIsAllAbout() {
        int meaning = 0;
        for (int i = 0; i < 10; i++) {
          for (int j = 0; j < 20; j++) {
            for (int k = 0; k < 300; k++) {
              for (int m = 0; m < 7000; m++) {
                meaning += Math.random() + 1;
              }
            }
          }
        }
        return String.valueOf(meaning).replaceAll("0*$", "");
      }


    private void prn(String s) { System.out.println(s) ; }

    private void prnx(String s) { System.out.println(
            JDOMUtil.formatXMLString(s)) ; }


    public String execJSP(String urlStr) throws IOException {

    // create connection
    URL url = new URL(urlStr);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setDoInput(true);

     //retrieve reply
     String result = getReply(url.openStream());
     connection.disconnect();

     return result;
}


private static String getReply(InputStream is) throws IOException {
    InputStreamReader isr = new InputStreamReader(is);
    StringWriter out = new StringWriter(8192);
    char[] buffer = new char[8192];
    int count;

    while ((count = isr.read(buffer)) > 0)
        out.write(buffer, 0, count);

    isr.close();
    return out.toString();
}

    private String getCaseState() {
        try {
            String handle = connect("admin", "YAWL");
            String s = _interfaceBClient.getCaseState("130960", handle);
            prn(s);
            return s;
        }
        catch (IOException ioe) {
        return ""; }
    }


    public String testXNode() {
        XNode root = new XNode("toplevel");
        root.addChild("childwithtext", "the text");
        root.addChild("emptychild");
        root.addAttribute("key", "value");
        root.addAttribute("thing", "other");
        XNode child = root.addChild("childwithchildren");
        child.addChild("one", "one text");
        child.addChild("two", "two text");
        XNode three = child.addChild("three");
        three.addAttribute("threes", "thing");
        System.out.println(root.toString());
        System.out.println();
        System.out.println(root.toPrettyString());
        System.out.println();
        System.out.println("root depth: " + root.getDepth());
        System.out.println("child depth: " + child.getDepth());
        return "success";
    }

    public String testXNodeParser() {
        String s = "<thing>the text</thing>";
        String t = "<single this=\"7\" that=\"banana\"/>";
        String u = "<single this=\"7\" that=\"banana\">the text</single>";
        String w = "<very_simple/>";
        String x = "<root><child>the text</child></root>";
        String y = "<root><child this=\"7\" that=\"banana\" /></root>";
        String z = "<root><child /></root>";
        String a = "<root><child><gc><ggc>the text</ggc></gc></child></root>";
        XNodeParser xnp = new XNodeParser();
        XNode node = xnp.parse(s);
        System.out.println(node.toString());
        node = xnp.parse(t);
        System.out.println(node.toString());
        node = xnp.parse(u);
        System.out.println(node.toString());
        node = xnp.parse(w);
        System.out.println(node.toString());
        node = xnp.parse(x);
        System.out.println(node.toString());
        System.out.println(node.toPrettyString());
        node = xnp.parse(y);
        System.out.println(node.toString());
        node = xnp.parse(z);
        System.out.println(node.toString());
        System.out.println(node.toPrettyString());
        node = xnp.parse(a);
        System.out.println(node.toString());
        System.out.println(node.toPrettyString(2));
        System.out.println(node.getChild("child").toString());
        System.out.println(node.getChild("child").toPrettyString(2));
        System.out.println(node.getChild("child").getChild("gc").toString());
        System.out.println(node.getChild("child").getChild("gc").toPrettyString(1));
        return "";
    }
    
    
    public String testCostModel() {
        Properties p = System.getProperties();
        for (String s : p.stringPropertyNames()) prn(s);

        FileInputStream inputStream = null;
        YSpecificationID specID = new YSpecificationID(
                "UID_d9f84d47-7e25-4b08-bcb6-1e07ab28ce07", "0.1", "costTest");
        try {
            inputStream = new FileInputStream(
                    "/Users/adamsmj/Documents/temp/anew/costtest.xml");
            String content = IOUtils.toString(inputStream);
            inputStream.close();
            if (content != null) {
                CostGatewayClient client =
                        new CostGatewayClient("http://localhost:8080/costService/gateway");
                String handle = client.connect("admin", "YAWL");
                client.importModel(content, handle);
                String log = client.getAnnotatedLog(specID, true, handle);
                prn(log);
    
                Set<String> resources = new HashSet<String>();
                resources.add("dummyid");
                resources.add("PA-63c56311-307c-464a-b47d-4251338f08f3");
                String map =
                        client.getResourceCosts(specID, "A", resources, handle);
                prnx(map);
            }
        }
        catch (IOException ioe) {
            return "ERROR: " + ioe.getMessage();
        }
        
        
        
        return "OK";

//        CostService service = new CostService();
//        service.importModel(xml);
//        CostModel model = new CostModel(new XNodeParser(true).parse(xml));
//        prn(model.toXML());
    }
    

    private String getCaseXML(String caseID) throws IOException {
        InterfaceB_EnvironmentBasedClient client =
                new InterfaceB_EnvironmentBasedClient("http://localhost:8080/yawl/ib");
        String handle = client.connect("admin", "YAWL");
        return client.getCaseData(caseID, handle);
    }
    
    private String testgetexpiry()  throws IOException {
        InterfaceB_EnvironmentBasedClient client =
                new InterfaceB_EnvironmentBasedClient("http://localhost:8080/yawl/ib");
        String handle = client.connect("admin", "YAWL");
        long time = client.getWorkItemExpiryTime("130946:3_Test_Rash", handle);
        return "";
    }
    
    
    private String testConversion() {
        try { 
            byte[] bytes = "IÃ±tÃ«rnÃ¢tiÃ´nÃ lizÃ¦tiÃ¸n".getBytes("ISO-8859-1");
            String s = new String(bytes, "UTF-8");
            return s;
        }
        catch (UnsupportedEncodingException uee) {
            //
        }
        return "what?";
    }
    
    
    private String testDocStore() throws IOException {
        YDocument doc = new YDocument("12345", -1, (InputStream) null);
//        doc.setDocument("/Users/adamsmj/Desktop/ARTICLE 19.docx");
        DocumentStoreClient c = new DocumentStoreClient("http://localhost:8080/documentStore/");
        String handle = c.connect("admin", "YAWL");
    //    return c.putDocument(doc, handle);
        prn("Connected: " + c.checkConnection(handle));
        doc.setId(1687538);
        c.getDocument(doc, handle);
        doc.writeToFile("/Users/adamsmj/Desktop/ARTICLE 22.docx");
        prn(c.removeDocument(1687535, handle));
        prn(c.clearCase("12345", handle));
        return "OK";
    }
    
    
    private String testExprParser() {
        Map<String, String> m = new Hashtable<String, String>();
        m.put("POrder/Order/Line/Quantity", "15");
        String e = "(0.1 * (0.2 + 4))";
        ExpressionParser xp = new ExpressionParser(e, m);
        double d = -1;
        try {
            d = xp.evaluate();
        }
        catch (NumberFormatException nfe) {
            return nfe.getMessage();
        }
        return String.valueOf(d);
    } 
    
    
    public String testDocumentType() {
        String dir = "/Users/adamsmj/Desktop/ARTICLE 19.docx";
        String fName = "ARTICLE 19.docx";
        File f = new File(dir);
        try {
            byte[] nameArray = fName.getBytes("UTF-8");
            byte[] bytes = FileUtils.readFileToByteArray(f);
            int nameSize = nameArray.length;
        //    DataOutputStream dos = new DataOutputStream()
            File g = new File("/Users/adamsmj/Desktop/ARTICLE 20.docx");
            OutputStream os = new FileOutputStream(g);
            IOUtils.write(bytes, os);
        }
        catch (IOException ioe) {
            return "ERROR";
        }
        return "OK";
    }

    private String skipItem() throws IOException {
        InterfaceB_EnvironmentBasedClient client =
                new InterfaceB_EnvironmentBasedClient("http://localhost:8080/yawl/ib");
        String handle = client.connect("admin", "YAWL");
        return client.skipWorkItem("75:A_3", handle);
    }


    public String testXNodeComparator() {
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(
                    "/Users/adamsmj/Library/Mail Downloads/Emergency0.6.xes-1.xml");
            String content = IOUtils.toString(inputStream);
            inputStream.close();
            if (content != null) {
                XNode root = new XNodeParser().parse(content);
                for (XNode trace : root.getChildren("trace")) {
                    _log.error(System.currentTimeMillis());
                    trace.sort(new XESTimestampComparator());
                    _log.error(System.currentTimeMillis());
                }
                FileOutputStream outputStream = new FileOutputStream(
                        "/Users/adamsmj/Library/Mail Downloads/Emergency0.6.xes-2.xml");
                IOUtils.write(root.toPrettyString(), outputStream);
            }
        }
        catch (IOException ioe) {
            return "ERROR";
        }
        return "OK";
    }



    private String testGetSpecID() {
        YSpecificationID specID = getSpecificationID("842") ;
        if (specID != null) return specID.toString();
        return "";
    }


    private YSpecificationID getSpecificationID(String caseID) {
        String resURL = "http://localhost:8080/resourceService/workqueuegateway";
        WorkQueueGatewayClientAdapter client = new WorkQueueGatewayClientAdapter(resURL);
        String engineHandle = client.connect("admin", "YAWL");

        try {
            Set<SpecificationData> specList = client.getSpecList(engineHandle);
            for (SpecificationData specData : specList) {
                YSpecificationID specID = specData.getID();
                String cases = client.getRunningCases(specID, engineHandle);
                Element caseElem = JDOMUtil.stringToElement(cases);
                for (Object child : caseElem.getChildren()) {
                    String childID = ((Element) child).getText();
                    if (caseID.equals(childID)) {
                        return specID;
                    }
                }
            }
        }
        catch (IOException ioe) {
              ioe.printStackTrace();
        }
        return null;
    }


    private String getTaskPrivileges() {
        String resURL = "http://localhost:8080/resourceService/workqueuegateway";
        WorkQueueGatewayClientAdapter client = new WorkQueueGatewayClientAdapter(resURL);
        String engineHandle = client.connect("admin", "YAWL");

        try {
            TaskPrivileges tp = client.getTaskPrivileges("899:T1_3", engineHandle);
            return tp.toString();
        }
        catch (Exception ioe) {
              ioe.printStackTrace();
        }
        return null;
    }

    private String getDistributionSet() {
        String resURL = "http://localhost:8080/resourceService/workqueuegateway";
        WorkQueueGatewayClientAdapter client = new WorkQueueGatewayClientAdapter(resURL);
        String engineHandle = client.connect("admin", "YAWL");

        try {
            Set<Participant> set = client.getDistributionSet("918.1:Create_Purchase_Order_104", engineHandle);
            return set.toString();
        }
        catch (Exception ioe) {
              ioe.printStackTrace();
        }
        return null;
    }

    private String testResourceLogGateway() {
        String resURL = "http://localhost:8080/resourceService/logGateway";
        ResourceLogGatewayClient client = new ResourceLogGatewayClient(resURL);

        try {
            String engineHandle = client.connect("admin", "YAWL");
            String result = client.getAllResourceEvents(engineHandle);
            List<ResourceEvent> eventList = new ArrayList<ResourceEvent>();
            Element events = JDOMUtil.stringToElement(result);
            if (events != null) {
                for (Object event : events.getChildren()) {
                    eventList.add(new ResourceEvent((Element) event));
                }
            }
            prn(result);
        }
        catch (IOException ioe) {
              ioe.printStackTrace();
        }
        return "";
    }


    private String testMaxCases() {
        Runtime runtime = Runtime.getRuntime();
        int nbrCases = 1000;
//        YSpecificationID specID = new YSpecificationID(
//                "UID_2d142041-0d1e-4cf3-b674-80c4a2477aac", "0.2", "maxCaseTester");
        YSpecificationID specID = new YSpecificationID(
                   "UID_f4c0454c-5a82-49a6-8a96-5a5eb1c32613", "0.2", "maxCaseTester");

        YSpecificationID stressID = new YSpecificationID(
                "UID_474cc3db-68d1-4488-8de5-c3722cf47965", "0.1", "stresstestWithEmptyTasks");
        String caseParams = "<Net><M>The rain in Spain</M></Net>";
        String template = "Cases: %d\tElapsed (ms): %d\tAvg (ms): %d\tMem Free: %d\tMem Alloc: %d\tMem Max: %d";
        String resURL = "http://localhost:8080/resourceService/workqueuegateway";
        WorkQueueGatewayClientAdapter client = new WorkQueueGatewayClientAdapter(resURL);
        long start = System.currentTimeMillis();
        try {
            String handle = client.connect("admin", "YAWL");
            long substart = start;
  //          while(true) {
            for (int i=0; i <= nbrCases; i++) {
                if (i % 10 == 0) {
                   long now = System.currentTimeMillis();
                    System.out.println(String.format(template, i, now - substart, (now-substart)/10,
                            runtime.freeMemory(), runtime.totalMemory(), runtime.maxMemory()));
                    substart = now;
                }
//                    if (i % 200 == 0) {
//                        client.launchCase(stressID, null, handle);
//                    }
                client.launchCase(specID, caseParams, handle);
            }
//                Thread.sleep(60000);
//                String cases = client.getRunningCases(specID, handle);
//                XNode casesNode = new XNodeParser().parse(cases);
//                for (XNode aCase : casesNode.getChildren()) {
//                    client.cancelCase(aCase.getText(), handle);
//                    System.out.println("Cancelled case: " + aCase.getText());
//        }
//                System.out.println("********* LOOP COMPLETE *************");
  //          }
        }
        catch (Exception ioe) {
            return "fail";
        }
        long diff = (System.currentTimeMillis() - start);
        return "Total elapsed (ms): " +  (System.currentTimeMillis() - start) +
               "\tAvg (ms): " + (diff / nbrCases);
    }


    private String testSpecdata() {
//        YSpecificationID specID = new YSpecificationID(
//                "UID_f4c0454c-5a82-49a6-8a96-5a5eb1c32613", "0.2", "maxCaseTester");
        YSpecificationID specID = new YSpecificationID(
                "UID_f4c0454c-5a82-49a6-8a96-5a5eb1c32613", "0.2", "maxCaseTester");
        String resURL = "http://localhost:8080/resourceService/workqueuegateway";
        WorkQueueGatewayClientAdapter client = new WorkQueueGatewayClientAdapter(resURL);
        String handle = client.connect("admin", "YAWL");
        try {
//            Set<SpecificationData> data = client.getSpecList(handle);
//            for (SpecificationData s : data) {
//                System.out.println(s);
//            }

//            Participant p = client.getParticipantFromUserID("th", handle);
//            prn(p.toString());

            prn(client.getCaseDataSchema(specID, handle));
        }
        catch (Exception e) {
            //
        }
        return "done";
    }

    private String testSummaries() {
        String result = "";
        try {
            String handle = connect("admin", "YAWL");
            result = _interfaceBClient.getCaseInstanceSummary(handle);
            System.out.println(result);
            result = _interfaceBClient.getWorkItemInstanceSummary("148", handle);
            System.out.println(result);

        }
        catch (IOException ioe) { }
        return result;

    }

//    private String launchCase() {
//
//    }

    private String getEngineParametersForRegisteredService() {
      String sessionID;
      InterfaceA_EnvironmentBasedClient clientInterfaceA =
              new InterfaceA_EnvironmentBasedClient("http://localhost:8080/yawl/ia");

      try {
        sessionID = clientInterfaceA.connect("admin", "YAWL");
      }
      catch (Exception e) {
        return "<failure>Exception attempting to connect to Engine.</failure>";
      }


       LinkedList dataVariableList = new LinkedList();

       YAWLServiceReference registeredService = null;

       Set services = clientInterfaceA.getRegisteredYAWLServices(sessionID);

         Iterator servicesIterator = services.iterator();
         while(servicesIterator.hasNext()) {
           YAWLServiceReference serviceReference = (YAWLServiceReference) servicesIterator.next();

           if (serviceReference.getURI().equals("http://localhost:8080/yawlWSInvoker/")) {
             registeredService = serviceReference;
             break;
           }
         }
         if (registeredService == null) {
           return null;
         }

         YParameter[] engineParametersForService;

         try {
             engineParametersForService = new InterfaceB_EngineBasedClient().getRequiredParamsForService(
                 registeredService
             );
         } catch (Exception ioe) {
           return null;
         }


         for(int i = 0; i < engineParametersForService.length; i++) {
             System.out.println(engineParametersForService[i].getName());
         }
        return "";
    }


    private String testDynTextParser() {
        String dataStr = "<wi><value>true</value></wi>";
        Element data = JDOMUtil.stringToElement(dataStr);
        String text = "2 + 3 equals ${2+3}";
        DynTextParser parser = new DynTextParser(data);
        String result = parser.parse(text);
        text = "The meaning of LUE is ${/wi/value/text()}";
        result += '\n' + parser.parse(text);
        text = "${if (boolean(/wi/value/text())) then 'it is true' else 'it is false'}";
        result += '\n' + parser.parse(text);
        text = "::: this is     a very    strange ..${/wi/value/text()}.@.    string";
        result += '\n' + parser.parse(text);
        return result;
    }


    private String doGetSpec4Case() {
        try {
            String handle = _interfaceBClient.connect("admin", "YAWL");
            prn(_interfaceBClient.getSpecificationForCase("3", handle));
        }
        catch (IOException ioe) {
            return "bad";
        }
        return "good";

    }

    private String testDynMultiCompTaskNewInst() {
        String result = "bad";
        try {
            String handle = _interfaceBClient.connect("admin", "YAWL");
            result = _interfaceBClient.createNewInstance("177:RunList_3", "newBob", handle);
        }
        catch (IOException ioe) { //
        }
        return result;
    }

//    private String testLogMiner() {
//        String resURL = "http://localhost:8080/resourceService/workqueuegateway";
//        WorkQueueGatewayClientAdapter resClient = new WorkQueueGatewayClientAdapter(resURL);
//        String handle = resClient.connect("admin", "YAWL");
//        try {
//            return resClient.getWorkItemDurationsForParticipant(
//                new YSpecificationID("bbb"),
//                "abe�en", "PA-ec2ddfbf-0a73-4fc7-ab95-bed876c145c5", handle);
//        }
//        catch (IOException ioe) {
//            return "io exception";
//        }
//    }

    public String testClientConnect() {
        String handle = null;
        try {
            handle = this.connect("editor", "YAWL");
        }
        catch (IOException ioe) {
            //
        }
        return handle;
    }

        public synchronized String getTaskParametersInOrder() {
        String resURL = "http://localhost:8080/resourceService/workqueuegateway";
        WorkQueueGatewayClientAdapter client = new WorkQueueGatewayClientAdapter(resURL);
        String engineHandle = client.connect("admin", "YAWL");
        String workItemRecordId = "596.2:Task2_4";
        try {
            List<YParameter> taskParameters =
                new ArrayList<YParameter>(client.getWorkItemParameters(workItemRecordId, engineHandle));
            List<YParameter> outputOnlyParams =
                new ArrayList<YParameter>(client.getWorkItemOutputOnlyParameters(workItemRecordId, engineHandle));

        Collections.sort(taskParameters);
        Collections.sort(outputOnlyParams);

        taskParameters.addAll(outputOnlyParams);

        //make the order of components
        String[] components = new String[taskParameters.size()];
        int order = 0;
        for (YParameter childParam : taskParameters) {
            String name = childParam.getName();
            System.out.println(" The order is " + order + " the name is " + name);
            components[order++] = name;
        }//fend
        }
        catch (Exception e) {
            //
        }
        return "";
    }//fend


    private String testGateway() {
        WorkQueueGatewayClient adapter =
               new WorkQueueGatewayClient("http://localhost:8080/resourceService/workqueuegateway");
        try {
            String handle = adapter.connect("admin", "YAWL");
            String xml = adapter.getWorkItemParameters("475:thetask_3", handle);
            Set<YParameter> set = new ResourceMarshaller().parseWorkItemParams(xml);
            System.out.println(xml);
        }
        catch (IOException ioe) {
          //  return "";
        }

//            Participant p = adapter.getParticipantFromUserID("th", handle);
//            QueueSet set = adapter.getAdminQueues(handle);
//            Set<WorkItemRecord> unofferedItems = set.getQueuedWorkItems(WorkQueue.UNOFFERED);
//   //         Set<WorkItemRecord> allocatedItems = adapter.getQueuedWorkItems(p.getID(),
//    //                WorkQueue.ALLOCATED, handle);
////            for (WorkItemRecord item : unofferedItems) {
////                adapter.startItem(p.getID(), item.getID(), handle);
////            }
//            if (unofferedItems == null) return "";
//        WorkItemRecord wir = unofferedItems.iterator().next();
          return "";
    }
    

    private String doGetParticipantsTest() {
        String resURL = "http://localhost:8080/resourceService/gateway";
        ResourceGatewayClientAdapter resClient = new ResourceGatewayClientAdapter(resURL) ;
        String handle = resClient.connect("admin", "YAWL");

        try {
            List<String> pSet = resClient.getAllParticipantNames(handle);
            for (String s : pSet) {
                System.out.println(s);
            }
        }
        catch (Exception ioe) {}
        return "";
    }

    private String doRandomTest() {
        int max = 11 ;
        int count = 1000 ;
        for (int i=0; i<count; i++) prn("" + new Random().nextInt(max-1)) ;
        return "";
    }

    private String xsdTest() {
        XSDType.getInstance();
        return "";
    }


    private String wqTest() {
        WorkQueueGatewayClientAdapter c = new
        WorkQueueGatewayClientAdapter("http://localhost:8080/resourceService/workqueuegateway");
        String handle = c.connect("admin", "YAWL");
        prn("handle = " + handle);

        try {
        System.out.println("WQG Result:");
        System.out.println(c.getCaseData("77", handle));

        String sh2 = connect("admin", "YAWL");
        System.out.println("IBC Result:");
        System.out.println(_interfaceBClient.getCaseData("77",sh2));

            String sh3 = c.userlogin("stephan","stephan"); 
            System.out.println(c.isValidUserSession(sh3));

            String resourceId = c.getParticipantFromUserID("stephan", handle).getID();
            Set<WorkItemRecord> offered =
c.getQueuedWorkItems(resourceId,WorkQueue.OFFERED,handle);
Set<WorkItemRecord> allocated=
c.getQueuedWorkItems(resourceId,WorkQueue.ALLOCATED,handle);
Set<WorkItemRecord> started=
c.getQueuedWorkItems(resourceId,WorkQueue.STARTED,handle);
Set<WorkItemRecord> suspended=
c.getQueuedWorkItems(resourceId,WorkQueue.SUSPENDED,handle);

//        if (c.checkConnection(handle)) {
//            try {
//                prn(c.getRunningCases("Order_Fulfilment.ywl", "0.11", handle));
//                prn(c.getSpecList(handle).toString());
//                prn(c.getSpecData("Order_Fulfilment.ywl", "0.11", handle).toString());
//                prn(c.getCaseData("70", handle));
//                prn(c.getRegisteredServices(handle).toString());
//                prn(c.cancelCase("70", handle));
//                prn(c.unloadSpecification("Order_Fulfilment.ywl", "0.11", handle));
            }
            catch (Exception e) {
                return "error";
            }
 //       }
        return "success";
    }


    private String doLogGatewayTest() throws IOException {
        YLogGatewayClient logClient = new YLogGatewayClient(
                                         "http://localhost:8080/yawl/logGateway") ;
        String handle = logClient.connect("admin", "YAWL") ;

//        prnx(logClient.getCompleteCaseLog("579", handle));

        // test all methods
/*        prn("getAllSpecifications:");
        prnx(logClient.getAllSpecifications(handle));

        prn("");
        prn("getNetInstancesOfSpecification - specID:");
        prnx(logClient.getNetInstancesOfSpecification(
                "UID_89217a2b-03db-4ac1-82ce-831c12210854", "1.24", "Sample", handle)) ;

        prn("");
        prn("getNetInstancesOfSpecification - key:");
        prnx(logClient.getNetInstancesOfSpecification(16690, handle)) ;
*/
        prn("");
        prn("getCompleteCaseLogsForSpecification - key:");
        prnx(logClient.getCompleteCaseLogsForSpecification(17602, handle)) ;

/*        prn("");
        prn("getCaseEvents - caseID:");
        prnx(logClient.getCaseEvents("579", handle)) ;

        prn("");
        prn("getCaseEvents - key:");
        prnx(logClient.getCaseEvents(17050, handle)) ;

        prn("");
        prn("getDataForEvent:") ;
        prnx(logClient.getDataForEvent(17006, handle)) ;

        prn("");
        prn("getDataTypeForDataItem:");
        prnx(logClient.getDataTypeForDataItem(16703, handle));

        prn("");
        prn("getTaskInstancesForCase:");
        prnx(logClient.getTaskInstancesForCase("579", handle)) ;

        prn("");
        prn("getTaskInstancesForTask:");
        prnx(logClient.getTaskInstancesForTask(16947, handle)) ;

        prn("");
        prn("getCaseEvent:");
        prnx(logClient.getCaseEvent("579", "CaseStart", handle)) ;

        prn("");
        prn("getAllCasesStartedByService:");
        prnx(logClient.getAllCasesStartedByService(null, handle)) ;

        prn("");
        prn("getAllCasesCancelledByService:");
        prnx(logClient.getAllCasesCancelledByService("DefaultWorklist", handle)) ;  */

        return "" ;
    }


    /*********************************************************************************/

    private String doWorkQueueGatewayTest() throws IOException {

        // STEP 1: get required data from resource service (via resource gateway)
        String resURL = "http://localhost:8080/resourceService/workqueuegateway";
        WorkQueueGatewayClientAdapter resClient = new WorkQueueGatewayClientAdapter(resURL);
        

//        String handle = resClient.connect("admin", "YAWL");
//
//        Participant p = resClient.getParticipantFromUserID("AdamsJ", handle);
//
//        Set<WorkItemRecord> set = resClient.getQueuedWorkItems(p.getID(), WorkQueue.OFFERED, handle) ;
//
//        System.out.println(set);

        String id = "3.1:5_Admit";
        String data = "<Admit><Weight>85</Weight><DiastolicBP>80</DiastolicBP><Sex>M</Sex><PatientID>122345</PatientID><Height>1.8</Height><HeartRate>72</HeartRate><SystolicBP>120</SystolicBP><Name>dkdkd</Name><Age>21</Age></Admit>";
        String handle = "8445744700114409003";
        String result;
        try {
            result = resClient.updateWorkItemData(id, data, handle);
        }
        catch (ResourceGatewayException rge) {
            result = rge.getMessage();
        }

        return result;
    }

    private String doOrgDataRefreshTest() throws IOException {
        String resURL = "http://localhost:8080/resourceService/gateway";
        ResourceGatewayClient resClient = new ResourceGatewayClient(resURL) ;

        // get full sets of filters, constraints and allocators
        String handle = resClient.connect("admin", "YAWL");

        resClient.refreshOrgDataSet(handle);
        return "ok";
    }


    private String doResourceServiceGatewayTest() throws IOException {
        /******* TEST CODE STARTS HERE ***********************************************/

        // testing the resourceService gateway

        // STEP 1: get required data from resource service (via resource gateway)
        String resURL = "http://localhost:8080/resourceService/gateway";
        ResourceGatewayClientAdapter resClient = new ResourceGatewayClientAdapter(resURL) ;

        // get full sets of filters, constraints and allocators
        String handle = resClient.connect("admin", "YAWL");
        try {
        // get full sets of Org Data
        List participants = resClient.getParticipants(handle);
        List roles = resClient.getRoles(handle);


        // not used here - shown for example only
        List capabilities = resClient.getCapabilities(handle);
        List positions = resClient.getPositions(handle);
        List orgGroups = resClient.getOrgGroups(handle);

        List constraints = resClient.getConstraints(handle);

        List filters = resClient.getFilters(handle);
        List allocators = resClient.getAllocators(handle);

        System.out.println("CONSTRAINTS");
        Iterator it = constraints.iterator();
        while (it.hasNext()) {
            AbstractConstraint ac = (AbstractConstraint) it.next() ;

            System.out.println("Name: " + ac.getName());
            System.out.println("DisplayName: " + ac.getDisplayName());
            System.out.println("Desc: " + ac.getDescription());
        }

        System.out.println("ALLOCATORS");
        it = allocators.iterator();
        while (it.hasNext()) {
            AbstractAllocator ac = (AbstractAllocator) it.next() ;
            System.out.println("Name: " + ac.getName());
            System.out.println("DisplayName: " + ac.getDisplayName());
            System.out.println("Desc: " + ac.getDescription());
        }

        System.out.println("FILTERS");    
        it = filters.iterator();
        while (it.hasNext()) {
            AbstractFilter ac = (AbstractFilter) it.next() ;
            System.out.println("Name: " + ac.getName());
            System.out.println("DisplayName: " + ac.getDisplayName());
            System.out.println("Desc: " + ac.getDescription());
        }


        // STEP 2: Present data to designer - designer makes choices
        //         We'll simulate some choices here - assume designer chooses the
        //         first item in each set

        // choose one participant and one role
        Participant p = (Participant) participants.get(0);
        String participantID = p.getID() ;

        Role r = (Role) roles.get(0);
        String roleID = r.getID();

        // take the first selector from each and create a (relevant) generic selector
        AbstractFilter gf = (AbstractFilter) filters.get(0);
        String name = gf.getName();
        String fName = gf.getDisplayName() ;
        gf.addParam("fparam", roleID);

        AbstractConstraint gc = (AbstractConstraint) constraints.get(0);
        String cName = gc.getDisplayName();
        gc.addParam("cparam", "12");

        AbstractAllocator ga = (AbstractAllocator) allocators.get(0);
        String aName = ga.getDisplayName() ;
        ga.addParam("aparam", "qwerty");


        // STEP 3: Instantiate the resource objects

        // set up offers
        OfferInteraction offer = new OfferInteraction(AbstractInteraction.SYSTEM_INITIATED);
        offer.addParticipantUnchecked(participantID);
        offer.addRoleUnchecked(roleID);
        offer.addInputParam("aParamName", OfferInteraction.USER_PARAM) ;
        offer.addFilter(gf) ;
        offer.addConstraint(gc) ;
        offer.setFamiliarParticipantTask("famTask18");

        // set up allocator
        AllocateInteraction allocate = new AllocateInteraction(
                                                   AbstractInteraction.SYSTEM_INITIATED);
        allocate.setAllocator(ga);

        // set up start
        StartInteraction start = new StartInteraction(AbstractInteraction.SYSTEM_INITIATED);

        // add some user-task privileges
        TaskPrivileges tp = new TaskPrivileges();
        tp.addParticipantToPrivilegeUnchecked(participantID, TaskPrivileges.CAN_DEALLOCATE);
        tp.addParticipantToPrivilegeUnchecked(participantID, TaskPrivileges.CAN_SKIP);


        // STEP 4: Build the task's ResourceMap with the above objects
        //         There are two ways of doing this:

        // 4(a): Create a ResourceMap, set it up and assign it to the YTask

        // ResourceMap constructor expects a reference to the taskID
        ResourceMap rMap = new ResourceMap("task_23") ;

        // OR 4(b): get a reference to the task's ResourceMap (commented example below)

        /** YTask task = new YAtomicTask() ;  */

        // boolean true will create a new ResourceMap inside the task (if it doesn't
        // already exist)
        /** ResourceMap rMap = task.getResourceMap(true) ; */


        // add objects created above to the resource map
        rMap.setOfferInteraction(offer);
        rMap.setAllocateInteraction(allocate);
        rMap.setStartInteraction(start);
        rMap.setTaskPrivileges(tp);


        // STEP 5: Calling the YTask.toXML() will also build the resourcing stuff
        /** task.setResourceMap(rMap);  */

        // out to xml
      //  String xmlout = rMap.toXML() ;

        // this is here to test the output to a file
       /**  Document doc = JDOMUtil.stringToDocument(xmlout);
         JDOMUtil.documentToFile(doc, "c:/temp/resourcingout.xml"); */
            return rMap.toXML() ;
        }
        catch (Exception e) {
            return "";
        }
    }
        /******* TEST CODE ENDS HERE *************************************************/

        private String createDummyOrgData() {

            int HOW_MANY_PARTICIPANTS_TO_CREATE = 20;

            ResourceManager rm = ResourceManager.getInstance();
            rm.setPersisting(true);
            rm.initOrgDataSource("HibernateImpl", -1);
            Random rand = new Random();

            String[] f = {"Alex", "Bill", "Carol", "Diane", "Errol", "Frank", "George",
                    "Hilary", "Irene", "Joanne"};
            String[] l = {"Smith", "Jones", "Brown", "Black", "Roberts", "Lewis", "Johns",
                    "Green", "Gold", "Davies"};

            Role r2 = new Role("a larger role");
            r2.setPersisting(true);
            Role r = new Role("a shared role");
            r.setPersisting(true);

            rm.getOrgDataSet().addRole(r2);
            rm.getOrgDataSet().addRole(r);
            r.setOwnerRole(r2);

            OrgGroup o = new OrgGroup("mega", OrgGroup.GroupType.DIVISION, null, "mega");
            o.setPersisting(true);
            rm.getOrgDataSet().addOrgGroup(o);

            OrgGroup o2 = new OrgGroup("minor", OrgGroup.GroupType.TEAM, o, "minor");
            o2.setPersisting(true);
            rm.getOrgDataSet().addOrgGroup(o2);

            Position po = new Position("a position");
            po.setPersisting(true);
            Position p2 = new Position("manager");
            p2.setPersisting(true);
            rm.getOrgDataSet().addPosition(p2);
            rm.getOrgDataSet().addPosition(po);
            po.setReportsTo(p2);
            po.setOrgGroup(o2);
            p2.setOrgGroup(o2);

            Capability c = new Capability("a capability", "some description", true);
            rm.getOrgDataSet().addCapability(c);


            for (int i = 0; i < HOW_MANY_PARTICIPANTS_TO_CREATE; i++) {
                String first = f[rand.nextInt(10)];
                String last = l[rand.nextInt(10)];
                String user = last + first.substring(0, 1);
                Participant p = new Participant(last, first, user, true);
                rm.addParticipant(p);

                p.setAdministrator(rand.nextBoolean());
                p.setPassword("apple");

                p.addPosition(po);
                p.addCapability(c);
                p.addRole(r);
                p.getUserPrivileges().allowAll();

            }

            return "Successfully created dummy org data";
        }


    private String ibTest() {

        InterfaceB_EnvironmentBasedClient yawl =
                new InterfaceB_EnvironmentBasedClient( "http://localhost:8080/yawl/ib" );
        try {
            String session = yawl.connect("admin", "YAWL");
            System.out.println( "session = "+session);
        } catch (IOException e) {
            e.printStackTrace();
        }


//        try {
//            String handle = _interfaceBClient.connect("admin", "YAWL");
//            String doc = _interfaceBClient.getCaseData("2", handle);
//            System.out.println(doc);
//        }
//        catch (Exception e) {}
        return "";
    }

    /********************************************************************************/

    private String stressTest() {
        // note: make sure "_stressTest.xml" is loaded in engine

        int numberOfCasesToStart = 50, i = 0;
        String obs = "http://localhost:8080/testService/ib";
        String result;

    //    _log.setLevel(Level.TRACE);

        try {
            String handle = _interfaceBClient.connect("admin", "YAWL");

            for (i=0; i<numberOfCasesToStart; i++) {
                result = _interfaceBClient.launchCase("StressTest", null, handle, obs);
                _log.trace("Case Started: " + result + ", case count: " + (i+1));
            }
        }
        catch (IOException ioe) {
            _log.error("IOException connecting to Engine.");
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return "Completed " + i + " case starts.";
    }


    public void handleCompleteCaseEvent(String caseID, String casedata) {
        _log.trace("Case Completed: " + caseID);
    }

    /*****************************************************************************/



    private String controllerTest() {
        Controller c = new Controller();
        c.getAllResources();
        return "";
    }

    class Controller extends WorkQueueGatewayClientAdapter{

 //       private static Controller instance;

private Controller() {
    super("http://localhost:8080/resourceService/workqueuegateway");
}

//public static Controller getInstance() {
//    if (instance == null)
//      instance = new Controller();
//    return (instance);
//  }
public String[] getAllResources()
  {
    try {
        String sessionHandler = connect("admin","YAWL");
//        if(WorkQueueGatewayClient.successful(sessionHandler)){
//            System.out.println("OK - Session:"+sessionHandler);
//        }
        Set<Participant> participantSet;
        String[] retValue;
        try {
            participantSet = getAllParticipants(sessionHandler);
        }
        catch (ResourceGatewayException rge) {
            retValue = new String[1];
            retValue[0] = rge.getMessage();
            return retValue;
        }

        Iterator iter = participantSet.iterator();
        retValue = new String[participantSet.size()];
        int i = 0;
        while (iter.hasNext()) {
            Object obj = iter.next();
            Participant p = (Participant) obj;
            retValue[i] = p.getFullName();
            i++;
        }
        disconnect(sessionHandler);
        return retValue;
    } catch (IOException e) {
        e.printStackTrace();
        return null;
    }
  }
}

}
