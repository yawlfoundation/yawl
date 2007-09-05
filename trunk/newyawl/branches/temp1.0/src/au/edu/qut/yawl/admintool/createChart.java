/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */



package au.edu.qut.yawl.admintool;
import java.awt.image.BufferedImage;
import java.io.PrintWriter;
import java.util.*;
import net.sf.hibernate.*;
import java.util.Calendar;


import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jfree.chart.*;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.plot.PlotOrientation;

import au.edu.qut.yawl.worklist.model.AggregatedWorkItemRecord;
import au.edu.qut.yawl.engine.interfce.InterfaceA_EnvironmentBasedClient;
import au.edu.qut.yawl.engine.YLogIdentifier;
import au.edu.qut.yawl.engine.YWorkItemEvent;
import java.io.IOException;

/**
 * @author heijens and fjellheim
 * @version 11.06.2005 added javadoc comments, changed queries because
 * of removing spaces in tablenames and columns and removed code to test output.
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class createChart extends HttpServlet{
    
    private InterfaceA_EnvironmentBasedClient iaClient = new InterfaceA_EnvironmentBasedClient("http://localhost:8080/yawl/ia");
    
    
    CommonQuery querylist = null;
    
    List columnlist = new LinkedList();
    
    String action = null;
    
    String querytype = "";
    String querystartstring = null;
    String currentquery = "";
    String currentqueryname = "";
    String charttype = "";
    
    DatabaseGatewayImpl _model = null;
    
    BufferedImage image = null;
    List parseList = null;
    
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        ServletContext context = config.getServletContext();
        String persistOnStr = context.getInitParameter("EnablePersistance");
        boolean _persistanceConfiguredOn = "true".equalsIgnoreCase(persistOnStr);
        if(_persistanceConfiguredOn) {
            System.out.println("Initializing DB connections for chart creation");
            try {
                
                _model = DatabaseGatewayImpl.getInstance(_persistanceConfiguredOn);
            } catch (HibernateException e) {
                e.printStackTrace();
            }
        }
    }
    
    
    /*
      Return a chart or a table depending on the type of request made
     */
    public void doGet(HttpServletRequest req, HttpServletResponse res){
        if (image!=null) {
            // return a picture
            res.setContentType("image/jpg");
            try {
                ChartUtilities.writeBufferedImageAsJPEG(res.getOutputStream(),image);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (parseList !=null) {
            //return a table
            
            try {
                PrintWriter out = res.getWriter();
                
                
                
                out.println("<CENTER><img src=\"./graphics/subtext.jpg\"/>");
                out.println("<table width=\"90%\" border=\"1\" bgcolor=\"#ffffff\"></CENTER>");
                
                for (int i = 0; i < parseList.size(); i++) {
                    Object[] results = (Object[]) parseList.get(i);
                    if (i==0) {
                        out.println("<tr>");
                        if (columnlist!=null && columnlist.size() > 0) {
                            for (int j = 0; j < results.length; j++) {
                                String colname = (String) columnlist.get(j);
                                out.println("<td> " + colname + "</td>");
                                
                            }
                        }
                        out.println("</tr>");
                    }
                    out.println("<tr>");
                    for (int j = 0; j < results.length; j++) {
                        if (results[j]!=null) {
                            out.println("<td> " + results[j] + "</td>");
                        }
                    }
                    out.println("</tr>");
                    
                }
                out.println("</table>");
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // return nothing...inform user
        }
        
        
    }
    
    
    /*
      Build the query
     */
    public void doPost(HttpServletRequest req, HttpServletResponse res){

            /*
             *  Find initial paramaters for analysis
                */
        
        String request = req.getParameter("startquery");
        String query = req.getParameter("query");
        String graph = req.getParameter("selectgraph");
        String spec = req.getParameter("selectspec");
        String caseid = req.getParameter("caseid");        
        String grouping = req.getParameter("groupbyvalue");                       
        
        action = req.getParameter("action");
        
        int granularity = 0;
        String time = req.getParameter("time");
        if (time!=null) {
            if (time.equals("Seconds")) {
                granularity = java.util.Calendar.SECOND;
            } else if (time.equals("Minutes")) {
                granularity = java.util.Calendar.MINUTE;
            } else if (time.equals("Hours")) {
                granularity = java.util.Calendar.HOUR_OF_DAY;
            } else if (time.equals("Days")) {
                granularity = java.util.Calendar.DATE;
            } else if (time.equals("Months")) {
                granularity = java.util.Calendar.MONTH;
            } else if (time.equals("Years")) {
                granularity = java.util.Calendar.YEAR;
            }
        }
        
        String params = "";
        
        
        
        /*
          First type of request...start new query
         */
        if (request!=null && request.equals("Start New Query")) {
            params = buildInitialQuery(req);
        }
        /*
          STEP 2 request: Add a filter
         */
        
        /*
          The WHERE option in the SQL query has not been set
         */
        
        if (action!=null && action.equals("Add Filter")) {
            params = buildQuery(req);
        }
        params = params + "&querytype="+querytype+"&querygraph="+charttype;
        
        
        /*
          Run the query and build the table/chart
          If the action is not an add filter request,
          it must be an execute query request
         */
        if (action!=null && !action.equals("Add Filter")) {
            
            columnlist = new LinkedList();
            
            List result = null;
            
            List reallyfinal = new LinkedList();
            List resultlist = null;
            for (int i = 0; i < querylist.size();i++) {
                QueryFilter filter = (QueryFilter) querylist.get(i);
                
                /*
                  If this is a case query
                 */
                if (querytype.equals("case")) {
                    checkCaseDisplayParameters(req);
                    
                    resultlist = _model.executeQueryCases(filter.query);
                    
                    /*
                      postprocessing interval
                      analysis
                     */
                    System.out.println(resultlist);
                    System.out.println(filter.intervaltime);
                    
                    List finalresult = new LinkedList();
                    
                    /*
                      The top X percent is a total of X percent from the whole list
                      finalresult gets a maximum size of X percent of the whole list
                     */
                    
                    if (resultlist!=null && filter.intervaltime > 0) {
                        int maxSize = (int) (resultlist.size() * filter.intervaltime / 100);
                        for (int j = 0; j < resultlist.size();j++) {
                            YLogIdentifier ylog = (YLogIdentifier) resultlist.get(j);
                            /*
                              Calculate intervaltime for this identifier...
                              if it is not a percentage based interval
                             */
                            ylog.setTime(ylog.getCompleted()-ylog.getCreated());
                            if (!filter.granularity.equals("Percent")) {
                                if (filter.LessThan) {
                                    if ((ylog.getCompleted()-ylog.getCreated()) < filter.intervaltime) {
                                        finalresult.add(ylog);
                                    }
                                } else {
                                    if ((ylog.getCompleted()-ylog.getCreated()) > filter.intervaltime) {
                                        finalresult.add(ylog);
                                    }
                                }
                            } else {
                                /*
                                  If it is a percentage based interval
                                  Compare to the rest of the list to see if it is
                                  within the X percent...if there is room...then ok
                                 */
                                System.out.println("MAX SIZE: " + maxSize);
                                if (finalresult.size() < maxSize) {
                                    finalresult.add(ylog);
                                } else {
                                    for (int k = 0; k < finalresult.size();k++) {
                                        YLogIdentifier old = (YLogIdentifier) finalresult.get(k);
                                        if (filter.LessThan) {
                                            if (old.getTime() > ylog.getTime()) {
                                                finalresult.remove(old);
                                                finalresult.add(ylog);
                                            }
                                        } else {
                                            if (old.getTime() < ylog.getTime()) {
                                                finalresult.remove(old);
                                                finalresult.add(ylog);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        for (int j = 0; j < finalresult.size();j++) {
                            YLogIdentifier ylog = (YLogIdentifier) finalresult.get(j);
                            //reallyfinal.add(addCaseParameters(req,ylog));
                            reallyfinal.add(ylog);
                        }
                    } else {
                        finalresult = resultlist;
                        for (int j = 0; j < finalresult.size();j++) {
                            YLogIdentifier ylog = (YLogIdentifier) finalresult.get(j);
                            ylog.setTime(ylog.getCompleted()-ylog.getCreated());
                            
                            //reallyfinal.add(addCaseParameters(req,ylog));
                            System.out.println("YLOG: " + ylog.getCreatedby());
                            reallyfinal.add(ylog);
                        }
                    }
                    
                    /*
                      This should be done if there is a grouping
                      request in the query rather than individual
                     */
                    
                    if (!grouping.equals("No grouping")) {
                        reallyfinal = calculateCaseAggregation(reallyfinal,grouping);
                    }
                    
                    /*
                      If this is a work item query
                     */
                } else {
                    checkWorkItemDisplayParameters(req);
                    boolean add = false;
                    String currentid = "";
                    String exec_query = filter.query;
                    
                    /*
                      WHY are they sorted this way??
                      Actually to keep the grouping of items
                      So that we know when we are finished with one item
                     */
                    
                    resultlist = _model.executeQueryWorkItems(exec_query+ " order by time");
                    AggregatedWorkItemRecord witem = new AggregatedWorkItemRecord();
                    
                    HashMap parent_items = new HashMap();
                    
                    YWorkItemEvent dummy_event = new YWorkItemEvent();
                    dummy_event.setIdentifier("");
                    dummy_event.setTaskid("");
                    dummy_event.setEvent("dummy");
                    resultlist.add(dummy_event);
                    /*
                      Create WorkItemRecords...
                     */
                    
                    /*
                      Rewrite into a hashMap Algorithm, which makes it order independent
                      That should fix it!!!
                     */
                    
                    HashMap eventview = new HashMap();
                    
                    for (int j = 0; j < resultlist.size();j++) {
                        
                        YWorkItemEvent event = (YWorkItemEvent) resultlist.get(j);
                        
                        witem = (AggregatedWorkItemRecord) eventview.get(event.getIdentifier()+event.getTaskid());
                        
                        if (witem==null) {
                            witem = new AggregatedWorkItemRecord();
                            witem.setCaseID(event.getIdentifier());
                            witem.setTaskID(event.getTaskid());
                            eventview.put(event.getIdentifier()+event.getTaskid(),witem);
                        }
                        
                        
                        if (event.getEvent().equals("Fired")) {
                            witem.setFiringTime(DateTransform.transform(event.getTime()));
                            
                            
                            Long parent_enablement = (Long) parent_items.get(event.getIdentifier().
                                    substring(0,event.getIdentifier().
                                    lastIndexOf("."))+
                                    event.getTaskid());
                            if (parent_enablement!=null) {
                                long enabled = parent_enablement.longValue();
                                witem.setEnablementTime(DateTransform.transform(enabled));
                                witem.setFired2Started(enabled);
                            }
                            
                        }
                        if (event.getEvent().equals("Executing")) {
                            witem.setStartTime(DateTransform.transform(event.getTime()));
                            add = true;
                            long started = event.getTime();
                            witem.setStarted2Complete(started);
                            
                        }
                        if (event.getEvent().equals("Enabled")) {
                            parent_items.put(event.getIdentifier()+event.getTaskid(),
                                    new Long(event.getTime()));
                        }
                        
                        if (event.getEvent().equals("Complete")) {
                            witem.setCompletionTime(DateTransform.transform(event.getTime()));
                            long completed = event.getTime();
                            
                            
                            long enabled = witem.getFired2Started();
                            
                            
                            long started = witem.getStarted2Complete();
                            /*
                              fired to started
                             */
                            witem.setFired2Started(started - enabled);
                            
                            /*
                              completion time (fired - complete)
                             */
                            witem.setFired2Complete(completed-enabled);
                            
                            /*
                              started to complete
                             */
                            witem.setStarted2Complete(completed-started);
                            
                            witem.setAssignedTo(event.getResource());
                            
                            
                            /*
                              Do not add the parent items and items with missing information
                             */
                            if (parent_items.get(event.getIdentifier()+event.getTaskid())==null) {
                                
                                if (enabled!=0 && completed!=0 && started!=0) {
                                    checkForAddition(filter,witem,reallyfinal);
                                }
                            }
                            
                        }
                        witem.setTaskID(event.getTaskid());
                        witem.setSpecificationID(event.getDescription());
                    }
                    
                    
                    if (filter.granularity.equals("Percent")) {
                        reallyfinal = reduceToPercentage(reallyfinal, filter);
                    }
                    
                    /*
                      calculate the aggregation according to the grouping
                     */
                    
                    if (!grouping.equals("No grouping")) {
                        reallyfinal = calculateWorkItemAggregation(reallyfinal,grouping);
                        
                        System.out.println(reallyfinal);
                        
                    }
                    
                    
                }
            }
            
            
            if (action!=null && action.equals("Create chart")) {
                
                String interval_granul = req.getParameter("interval_chart_granularity");
                int granul = 0;
                if (interval_granul.equals("Seconds")) {
                    granul = Calendar.SECOND;
                }
                if (interval_granul.equals("Minutes")) {
                    granul = Calendar.MINUTE;
                }
                if (interval_granul.equals("Hours")) {
                    granul = Calendar.HOUR_OF_DAY;
                }
                if (interval_granul.equals("Days")) {
                    granul = Calendar.DATE;
                }
                if (interval_granul.equals("Months")) {
                    granul = Calendar.MONTH;
                }
                if (interval_granul.equals("Years")) {
                    granul = Calendar.YEAR;
                }
                
                
                String charttype = req.getParameter("graphtype");
                
                System.out.println(charttype);
                
                List chartValues = new LinkedList();
                
                String xaxis = req.getParameter("groupbyvalue");
                String yaxis = req.getParameter("chartvalue");
                
                for (int i = 0; i < reallyfinal.size();i++) {
                    
                    
                    if (querytype.equals("case")) {
                        
                        YLogIdentifier ylog = (YLogIdentifier) reallyfinal.get(i);
                        System.out.println(ylog.getTime());
                        System.out.println(ylog.getSpecification());
                        Object[] o = new Object[2];
                        
                        /*
                          Select time selected
                         */
                        if (yaxis.equals("Started-Completed")) {
                            o[1] = new Integer((int)DateTransform.getTime(ylog.getTime(),granul));
                        } else if (yaxis.equals("Count")) {
                            o[1] = new Integer((int) ylog.getCount());
                        }
                        
                        /*
                          Select group by element
                         */
                        if (grouping.equals("Specification")) {
                            o[0] = ylog.getSpecification();
                        } else if (grouping.equals("Resource")) {
                            o[0] = ylog.getCreatedby();
                        } else {
                            o[0] = ylog.getIdentifier();
                        }
                        
                        System.out.println("Setting " +  grouping +  " as " + o[0] + " " + ylog.getCreatedby());
                        chartValues.add(o);
                        
                    } else {
                        AggregatedWorkItemRecord witem = (AggregatedWorkItemRecord) reallyfinal.get(i);
                        Object[] o = new Object[2];
                        
                        /*
                          Select time selected
                         */                                                
                        if (yaxis.equals("Enabled-Completed")) {
                            o[1] = new Integer(DateTransform.getTime(witem.getFired2Complete(),granul));
                        } else if (yaxis.equals("Started-Completed")) {
                            o[1] = new Integer(DateTransform.getTime(witem.getStarted2Complete(),granul));
                        } else if (yaxis.equals("Enabled-Started")) {
                            o[1] = new Integer(DateTransform.getTime(witem.getFired2Started(),granul));
                        } else if (yaxis.equals("Count")) {
                            o[1] = new Integer((int) witem.getCount());
                        }
                        
                        
                        /*
                          Select group by element
                         */
                        if (grouping.equals("Specification")) {
                            o[0] = witem.getSpecificationID();
                        } else if (grouping.equals("Resource")) {
                            o[0] = witem.getAssignedTo();
                        } else {
                            o[0] = witem.getTaskID();
                        }
                        chartValues.add(o);
                    }
                }
                
                if (charttype.equals("Bar chart")) {
                    generateBarChart(chartValues);
                } else {
                    generatePieChart(chartValues);
                }
            }
            
            
            
            if (action!=null && action.equals("Create table")) {
                System.out.println(reallyfinal);
                
                /*
                  Calculate average, maximum, minimum as well...but how
                  Need to sort the reallyfinal list by the group by value
                  and then calculate requested display value
                 */
                String groupbyvalue = req.getParameter("groupbyvalue");
                
                generateTable(reallyfinal);
                
            }
        }
        
        
        /*
          After each type of request
          Redirect back to the chartview page with the correct
          parameters
         */
        
        try {
            if (action!=null) {
                if (action.equals("Create chart")) {
                    getServletContext().getRequestDispatcher("/chartview.jsp?success=true&filter=true"+params).forward(req, res);
                } else if (action.equals("Add Filter")) {
                    getServletContext().getRequestDispatcher("/chartview.jsp?test=true"+params).forward(req, res);
                } else {
                    getServletContext().getRequestDispatcher("/chartview.jsp?table=true&filter=true"+params).forward(req, res);
                }
            } else {
                getServletContext().getRequestDispatcher("/chartview.jsp?test=true"+params).forward(req, res);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        
        
    }
    
    public void checkWorkItemDisplayParameters(HttpServletRequest req) {
        String grouping = req.getParameter("groupbyvalue");                       
        
        if (req.getParameter("ID")!=null) {
            columnlist.add("ID");
        }
        if (req.getParameter("Task")!=null) {
            columnlist.add("Task");
        }
        if (req.getParameter("Enabled")!=null) {
            columnlist.add("Enabled");
        }
        if (req.getParameter("Started")!=null) {
            columnlist.add("Started");
        }
        if (req.getParameter("Completed")!=null) {
            columnlist.add("Completed");
        }
        if (req.getParameter("Owner")!=null) {
            columnlist.add("Owner");
        }
        if (req.getParameter("Specification")!=null) {
            columnlist.add("Specification");
        }
        if (req.getParameter("Count")!=null) {
            columnlist.add("Count");
        }
        if (grouping.equals("No grouping")) {
            if (req.getParameter("Enabled-Completed")!=null) {
                columnlist.add("Enabled-Completed");
            }
            if (req.getParameter("Started-Completed")!=null) {
                columnlist.add("Started-Completed");
            }
            if (req.getParameter("Enabled-Started")!=null) {
                columnlist.add("Enabled-Started");
            }
        } else {
            if (req.getParameter("Enabled-Completed")!=null) {
                columnlist.add("Average Enabled-Completed");
            }
            if (req.getParameter("Started-Completed")!=null) {
                columnlist.add("Average Started-Completed");
            }
            if (req.getParameter("Enabled-Started")!=null) {
                columnlist.add("Average Enabled-Started");
            }        
        }
    }
    
    public void checkCaseDisplayParameters(HttpServletRequest req) {
        
        if (req.getParameter("interval")!=null) {
            String grouping = req.getParameter("groupbyvalue");                       
            if (grouping.equals("No grouping")) {
                 columnlist.add("Completion Time");
            } else {
                columnlist.add("Average Completion Time");
            }
        }
        if (req.getParameter("created")!=null) {
            columnlist.add("Created");
        }
        if (req.getParameter("completed")!=null) {
            columnlist.add("Completed");
            
        }
        if (req.getParameter("cancelled")!=null) {
            columnlist.add("Cancelled");
            
        }
        if (req.getParameter("specification")!=null) {
            columnlist.add("Specification");
        }
        if (req.getParameter("Count")!=null) {
            columnlist.add("Count");
        }
        if (req.getParameter("Owner")!=null) {
            columnlist.add("Owner");
        }
        
    }
    
    public void generateTable(List parseList) {
              
        LinkedList results = new LinkedList();
        
        List paramlist = null;
        
        HashMap map = new HashMap();
        
        for (int i = 0; i < parseList.size();i++) {
            
            paramlist = new LinkedList();
            
            if (parseList.get(i) instanceof AggregatedWorkItemRecord) {
                
                AggregatedWorkItemRecord witem = (AggregatedWorkItemRecord) parseList.get(i);
                
                if (columnlist.contains("ID")) {
                    paramlist.add(witem.getCaseID());
                }
                if (columnlist.contains("Task")) {
                    paramlist.add(witem.getTaskID());
                }
                if (columnlist.contains("Enabled")) {
                    paramlist.add(witem.getEnablementTime());
                }
                if (columnlist.contains("Started")) {
                    paramlist.add(witem.getStartTime());
                }
                if (columnlist.contains("Completed")) {
                    paramlist.add(witem.getCompletionTime());
                }
                if (columnlist.contains("Owner")) {
                    paramlist.add(witem.getAssignedTo());
                }
                if (columnlist.contains("Specification")) {
                    paramlist.add(witem.getSpecificationID());
                }
                if (columnlist.contains("Count")) {
                    paramlist.add(new Long(witem.getCount()).toString());
                }
                if (columnlist.contains("Enabled-Completed")) {
                    paramlist.add(DateTransform.convertRelativeTime(witem.getFired2Complete()));
                }
                if (columnlist.contains("Started-Completed")) {
                    paramlist.add(DateTransform.convertRelativeTime(witem.getStarted2Complete()));
                }
                if (columnlist.contains("Enabled-Started")) {
                    paramlist.add(DateTransform.convertRelativeTime(witem.getFired2Started()));
                }
                if (columnlist.contains("Average Enabled-Completed")) {
                    paramlist.add(DateTransform.convertRelativeTime(witem.getFired2Complete()));
                }
                if (columnlist.contains("Average Started-Completed")) {
                    paramlist.add(DateTransform.convertRelativeTime(witem.getStarted2Complete()));
                }
                if (columnlist.contains("Average Enabled-Started")) {
                    paramlist.add(DateTransform.convertRelativeTime(witem.getFired2Started()));
                }

            } else {
                YLogIdentifier ylog = (YLogIdentifier) parseList.get(i);
                
                if (columnlist.contains("Completion Time")) {
                    //paramlist.add(DateTransform.convertRelativeTime(ylog.getCompleted()- ylog.getCreated()));
                    
                    paramlist.add(DateTransform.convertRelativeTime(ylog.getTime()));
                }
                if (columnlist.contains("Average Completion Time")) {
                    //paramlist.add(DateTransform.convertRelativeTime(ylog.getCompleted()- ylog.getCreated()));
                    
                    paramlist.add(DateTransform.convertRelativeTime(ylog.getTime()));
                }
                if (columnlist.contains("Created")) {
                    paramlist.add(DateTransform.transform(ylog.getCreated()));
                }
                if (columnlist.contains("Completed")) {
                    paramlist.add(DateTransform.transform(ylog.getCompleted()));
                }
                if (columnlist.contains("Cancelled")) {
                    paramlist.add(DateTransform.transform(ylog.getCancelled()));
                }
                if (columnlist.contains("Specification")) {
                    paramlist.add(ylog.getSpecification());
                }

                if (columnlist.contains("Count")) {
                    paramlist.add(new Long(ylog.getCount()).toString());
                }
                if (columnlist.contains("Owner")) {
                    paramlist.add(ylog.getCreatedby());
                }
                
                
                
                
            }
            results.add(paramlist.toArray());
            
        }
        
        image = null;
        this.parseList = results;
        
        String url =  new String(getServletContext().getInitParameter("admintool")+"/chartview.jsp?table=true");
    }
    
    public void generateBarChart(List parseList){
        
        String idcase = "1";
        
        
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        try {
            for (int i = 0; i < parseList.size(); i++) {
                Object[] results = (Object[]) parseList.get(i);
                
                System.out.println(results[0] + " " + results[1]);
                
                dataset.setValue(((Integer) results[1]).intValue(),"", (String) results[0]);
            }
            
        } catch (Exception e) {
            // Should inform the user that something went wrong....
            e.printStackTrace();
        }
        
        
        JFreeChart chart =  ChartFactory.createBarChart("",
                null, null, dataset,
                PlotOrientation.VERTICAL, true, false, false);
        try {
            image = chart.createBufferedImage(120*parseList.size(),300);
            
            //ChartUtilities.saveChartAsJPEG(new File("../webapps/admintool/graphics/piegraph.jpg"), chart, 500, 300);
            System.out.println("created file");
        } catch (Exception exc){
            System.err.println("Error writing image to file");
            exc.printStackTrace();
        }
        
        
        String url =  new String(getServletContext().getInitParameter("admintool")+"/chartview.jsp?success=true");
    }
    
    public void generatePieChart(List parseList) {
        
        LinkedList nrList = new LinkedList();
        List statusList =  new LinkedList();
        String idcase = "1";
        
        try {
            
            for (int i = 0; i < parseList.size(); i++) {
                Object[] results = (Object[]) parseList.get(i);
                nrList.add(results[1]);
                statusList.add(results[0]);
            }
            
        } catch (Exception e) {
            e.printStackTrace();//nothing needs doing
        }
        
        DefaultPieDataset pieDataSet = new DefaultPieDataset();
        for (int i=0; i<nrList.size(); i++){
            Integer nrstring = (Integer) nrList.get(i);
            int nr = nrstring.intValue();
            String status = (String) statusList.get(i);
            pieDataSet.setValue(status, nr);
        }
        
        
        JFreeChart chart = ChartFactory.createPieChart3D("", pieDataSet, true, false, false);
        try {
            image = chart.createBufferedImage(500,300);
            
            System.out.println("created file");
        } catch (Exception exc){
            System.err.println("Error writing image to file");
            exc.printStackTrace();
        }
        
    }
    
    
    /*
      Check to see if this array should be added to the list
     */
    public void checkForAddition(QueryFilter filter, AggregatedWorkItemRecord witem, List reallyfinal) {
        
        long timetaken = 0;
        long old_timetaken = 0;
        if (filter.intervaloption.equals("Started - Completed")) {
            timetaken = witem.getStarted2Complete();
        } else if (filter.intervaloption.equals("Enabled - Started")) {
            timetaken = witem.getFired2Started();
        } else if (filter.intervaloption.equals("Enabled - Completed")) {
            timetaken = witem.getFired2Complete();
        }
        
        
        /*
          Select a subset of the elements from here based
          on the selected view
         */
        System.out.println("timetaken: " +  timetaken);
        System.out.println("intervaltime: " +  filter.intervaltime);
        
        if (!filter.granularity.equals("Percent")) {
            
            if (filter.LessThan) {
                if (timetaken < filter.intervaltime) {
                    if (witem!=null) {
                        reallyfinal.add(witem);
                    }
                }
            } else {
                if (timetaken > filter.intervaltime) {
                    if (witem!=null) {
                        reallyfinal.add(witem);
                    }
                }
            }
        } else {
            reallyfinal.add(witem);
        }
        
    }
    
    public List calculateWorkItemAggregation(List parseList, String grouping) {
        
        LinkedList aggregated = new LinkedList();
        HashMap map = new HashMap();
        
        for (int i = 0; i < parseList.size(); i++) {
            AggregatedWorkItemRecord witem = (AggregatedWorkItemRecord) parseList.get(i);
            
            AggregatedWorkItemRecord previous = null;
            if (grouping.equals("Specification")) {
                previous = (AggregatedWorkItemRecord) map.get(witem.getSpecificationID());
            } else if (grouping.equals("Resource")) {
                previous = (AggregatedWorkItemRecord) map.get(witem.getWhoStartedMe());
            } else {
                previous = (AggregatedWorkItemRecord) map.get(witem.getTaskID());
            }
            
            if (previous == null) {
                witem.setCount(1);
                if (grouping.equals("Specification")) {
                    map.put(witem.getSpecificationID(),witem);
                } else if (grouping.equals("Resource")) {
                    map.put(witem.getWhoStartedMe(),witem);
                } else {
                    map.put(witem.getTaskID(),witem);
                }
                
            }	else {
                
                
                previous.setFired2Complete(previous.getFired2Complete() + witem.getFired2Complete());
                previous.setStarted2Complete(previous.getStarted2Complete() + witem.getStarted2Complete());
                previous.setFired2Started(previous.getFired2Started() + witem.getFired2Started());
                
                previous.setCount(previous.getCount() + 1);
            }
            
        }
        
        Iterator it = map.values().iterator();
        while (it.hasNext()) {
            AggregatedWorkItemRecord awir = (AggregatedWorkItemRecord) it.next();
            awir.setFired2Started(awir.getFired2Started() / awir.getCount());
            awir.setFired2Complete(awir.getFired2Complete() / awir.getCount());
            awir.setStarted2Complete(awir.getStarted2Complete() / awir.getCount());
            
            
            aggregated.add(awir);
            System.out.println("Calculating Averages....!!!!!!!!! ");
        }
        
        return aggregated;
        
        
    }
    
    
    public List calculateCaseAggregation(List parseList, String grouping) {
        
        LinkedList aggregated = new LinkedList();
        HashMap map = new HashMap();
        
        for (int i = 0; i < parseList.size(); i++) {
            YLogIdentifier ylog = (YLogIdentifier) parseList.get(i);
            
            /*
              The grouping variable is used as the key
              Default is group by specification
             */
            
            
            YLogIdentifier agglog = null;
            if (grouping.equals("Specification")) {
                agglog = (YLogIdentifier) map.get(ylog.getSpecification());
            } else if (grouping.equals("Resource")) {
                agglog = (YLogIdentifier) map.get(ylog.getCreatedby());
            }  else {
                return parseList;
            }
            
            if (agglog == null) {
                /*
                  There is no element within this group yet....
                 */
                agglog = new YLogIdentifier();
                
                agglog.setSpecification(ylog.getSpecification());
                System.out.println("new grouping by: " + grouping + " " + ylog.getCreatedby());
                
                agglog.setCreatedby(ylog.getCreatedby());
                agglog.setCreated(ylog.getCreated());
                agglog.setCompleted(ylog.getCompleted());
                agglog.setCancelled(ylog.getCancelled());
                agglog.setTime(ylog.getCompleted()- ylog.getCreated());
                //agglog.setFirstCompletionTime();
                //agglog.setFirstStartTime();
                //agglog.setLastCompletionTime();
                //agglog.setLastStartTime();
                agglog.setCount(1);
                
                
                if (grouping.equals("Specification")) {
                    map.put(agglog.getSpecification(),agglog);
                } else if (grouping.equals("Resource")) {
                    map.put(agglog.getCreatedby(),agglog);
                }
                System.out.println("New spec....time is: " + agglog.getTime());
            } else {
                
                /*
                  Existing element within this group exists
                  Add to this group
                 */
                
                agglog.setTime(agglog.getTime() + (ylog.getCompleted()-ylog.getCreated()));
                agglog.setCount(agglog.getCount()+1);
            }
            
        }
        Iterator it = map.values().iterator();
        while (it.hasNext()) {
            YLogIdentifier ylog = (YLogIdentifier) it.next();
            ylog.setTime(ylog.getTime()/ylog.getCount());
            aggregated.add(ylog);
            System.out.println("Calculating Averages....!!!!!!!!! " + ylog.getTime() + " " + ylog.getCreatedby());
        }
        
        return aggregated;
    }
    /**
     */
    
    public List reduceToPercentage(List reallyfinal, QueryFilter filter) {
        
        List reducedList = new LinkedList();
        
        int maxSize = (int) (reallyfinal.size() * filter.intervaltime / 100);
        if (maxSize < 1) {
            maxSize = 1;
        }
        
        
        System.out.println("MAX SIZE: " + maxSize);
        
        for (int i = 0; i < reallyfinal.size();i++) {
            AggregatedWorkItemRecord witem = (AggregatedWorkItemRecord) reallyfinal.get(i);
            
            if (reducedList.size() < maxSize) {
                reducedList.add(witem);
            } else {
                long timetaken = 0;
                long old_timetaken = 0;
                boolean replaced = false;
                for (int k = 0; k < reducedList.size() && !replaced ;k++) {
                    AggregatedWorkItemRecord old = (AggregatedWorkItemRecord) reducedList.get(k);
                    
                    if (filter.intervaloption.equals("Started - Completed")) {
                        old_timetaken = old.getStarted2Complete();
                        timetaken = witem.getStarted2Complete();
                    } else if (filter.intervaloption.equals("Enabled - Started")) {
                        old_timetaken = old.getFired2Started();
                        timetaken = witem.getFired2Started();
                    } else if (filter.intervaloption.equals("Enabled - Completed")) {
                        old_timetaken = old.getFired2Complete();
                        timetaken = witem.getFired2Complete();
                    }
                    
                    if (filter.LessThan) {
                        if (old_timetaken > timetaken) {
                            reducedList.remove(old);
                            reducedList.add(witem);
                            replaced = true;
                        }
                    } else {
                        if (old_timetaken < timetaken) {
                            reducedList.remove(old);
                            reducedList.add(witem);
                            replaced = true;
                        }
                    }
                    
                }
            }
        }
        return reducedList;
    }
    
    public String buildInitialQuery(HttpServletRequest req) {
        
        /*
          Start building new query
         */        
        currentquery = "";
        String element = req.getParameter("selectelement");
        if (element!=null) {
            if (element.equals("Cases")) {
                querystartstring = new String("Select distinct from au.edu.qut.yawl.engine.YLogIdentifier as element");
                querytype = "case";
                querylist = new CommonQuery(req.getParameter("queryname"));
            } else {
                querystartstring = new String("Select distinct from au.edu.qut.yawl.engine.YWorkItemEvent as element");
                querytype = "task";
                querylist = new CommonQuery(req.getParameter("queryname"));
            }
        }
        
     
        /*
          Check for graph type parameters
                  
         <option>Bar chart</option>
          <option>Pie chart</option>
          </select>
         */
        charttype = req.getParameter("selectgraph");
        
        currentqueryname = req.getParameter("queryname");
        
        return "&queryname="+currentqueryname;
    }
    
    public String buildQuery(HttpServletRequest req) {
        
        boolean setWhere = false;
        
        String specid = req.getParameter("specfilter");
        String caseid = req.getParameter("casefilter");
        String taskid = req.getParameter("taskfilter");
        String workitemid = req.getParameter("workitemfilter");
        String resourceid = req.getParameter("resourcefilter");
        
        if (specid!=null || caseid!=null || taskid!=null || workitemid !=null) {
                /*
                  Check to see if any of the filters requires
                  us to add a WHERE clause in the query
                 */
            if ((specid!=null && !specid.equals("--All--")) ||
                    (caseid!=null && !caseid.equals("--All--")) ||
                    (taskid!=null && !taskid.equals("--All--")) ||
                    (resourceid!=null && !resourceid.equals("--All--")) ||
                    (workitemid!=null && !workitemid.equals("--All--"))) {
                currentquery = querystartstring + " where";
                setWhere = true;
            } else {
                currentquery = querystartstring;
            }
        }
        
           /*
             Start building the query and include AND Condition between these...if required
            */
        boolean setand = false;
        if (specid!=null && !specid.equals("--All--")) {
            setand = true;
            if (querytype.equals("case")) {
                currentquery = currentquery + " element.specification = '" + specid + "'";
            }
            if (querytype.equals("task")) {
                currentquery = currentquery + " element.description = '" + specid + "'";
            }
        }
        if (caseid!=null && !caseid.equals("--All--")) {
            if (setand) {
                currentquery = currentquery + " AND ";
            }
            setand = true;
            if (querytype.equals("case")) {
                currentquery = currentquery + " element.identifier = '" + caseid + "'";
            }
            if (querytype.equals("task")) {
                currentquery = currentquery + " element.identifier like '" + caseid + "%'";
            }
        }
        if (taskid!=null && !taskid.equals("--All--")) {
            if (setand) {
                currentquery = currentquery + " AND ";
            }
            setand = true;
            if (querytype.equals("task")) {
                currentquery = currentquery + " element.taskid = '" + taskid + "'";
            }
        }
        if (workitemid!=null && !workitemid.equals("--All--")) {
            if (setand) {
                currentquery = currentquery + " AND ";
            }
            setand = true;
            if (querytype.equals("task")) {
                currentquery = currentquery + " element.identifier = '" + workitemid + "'";
            }
        }
        if (resourceid!=null && !resourceid.equals("--All--")) {
            if (setand) {
                currentquery = currentquery + " AND ";
            }
            setand = true;
            if (querytype.equals("case")) {
                currentquery = currentquery + " element.createdby = '" + resourceid + "'";
            }
            if (querytype.equals("task")) {
                currentquery = currentquery + " element.resource = '" + resourceid + "'";
            }
        }
        
            /*
              Check for status parameters
             */
        String statusoption = req.getParameter("statusoption");
        System.out.println("status: " + statusoption + " " + currentquery);
        
        System.out.println("status: " + statusoption + " " + currentquery);
        
        String timeoption = req.getParameter("timeoption");
        String timelength = req.getParameter("lengthoftime");
        System.out.println("time: " + timeoption + " " + timelength);
        
        String status_granularity = req.getParameter("status_granularity");
        
            /*
              Insert the time option
             */
        if (statusoption!=null && !statusoption.equals("--Select Option--") &&
                timelength!=null && timelength.length()>0) {
            System.out.println("inside");
            String operator = "";
            String requiredtime = "";
            if (timeoption.equals("At")) {
                operator = "=";
                requiredtime = DateTransform.convertTime(timelength);
            }
            if (timeoption.equals("After")) {
                operator = ">";
                requiredtime = DateTransform.convertTime(timelength);
            }
            if (timeoption.equals("Before")) {
                operator = "<";
                requiredtime = DateTransform.convertTime(timelength);
            }
            
            String column = "";
            
            if (querytype.equals("case")) {
                    /*
                      CASE FILTER
                     */
                if (timeoption.equals("Completed")) {
                    column = "completed";
                } else {
                    column = "created";
                }
                if (setWhere) {
                    currentquery = currentquery + " AND element."+ column +" " + operator + " " + requiredtime;
                } else {
                    currentquery = currentquery + " WHERE element."+ column +" " + operator + " " + requiredtime;
                }
            } else {
                    /*
                      WORKITEM FILTER
                     */
                column = "time";
                if (querytype.equals("task") && statusoption!=null && !statusoption.equals("--Select Option--")) {
                    if (setWhere) {
                        currentquery =
                                currentquery + " AND ((element.event = '" + statusoption +
                                "' AND element."+ column +" " + operator + " " + requiredtime+
                                ") OR (element.event != '" + statusoption+ "'))";
                    } else {
                        currentquery =
                                currentquery + " WHERE ((element.event = '" + statusoption +
                                "' AND element."+ column +" " + operator + " " + requiredtime+
                                ") OR (element.event != '" + statusoption+ "'))";
                        setWhere = true;
                    }
                }
                
            }
        }
        
            /*
              Create the filter
             */
        
            /*
              Check for interval parameters
             
              This can not be translated into a query
              as the intervals are not stored explicitly
              It is instead added as filters to be applied to query results
              after the execution of the HQL query
             */
        
        QueryFilter filter = new QueryFilter();
        
        filter.query = currentquery;
        querylist.add(filter);
        
        
        filter.intervaltime = -1;
        String intervaloption = req.getParameter("intervaloption");
        filter.intervaloption = intervaloption;
        String interval_granularity = req.getParameter("interval_granularity");
        filter.granularity = interval_granularity;
        String timearea = req.getParameter("timearea");
        String timeforarea = req.getParameter("timeforarea");
        if (intervaloption!=null && !intervaloption.equals("--Select Option--")) {
            filter.intervaltime = new Long(timeforarea).longValue();
            String event1 = "";
            String event2 = "";
            if (interval_granularity.equals("Days")) {
                filter.intervaltime = filter.intervaltime*1000*60*60*24;
            } else if (interval_granularity.equals("Hours")) {
                filter.intervaltime = filter.intervaltime*1000*60*60;
            } else if (interval_granularity.equals("Minutes")) {
                filter.intervaltime = filter.intervaltime*1000*60;
            } else if (interval_granularity.equals("Seconds")) {
                filter.intervaltime = filter.intervaltime*1000;
            }
            
            if (timearea.equals("More")) {
                filter.LessThan = false;
            }
            if (timearea.equals("Less")) {
                filter.LessThan = true;
            }
            
            
        }
        
        String params = "&queryname="+currentqueryname+"&filter=true";
        return params;
    }    

}





