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
import java.sql.Connection;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import au.edu.qut.yawl.engine.interfce.InterfaceA_EnvironmentBasedClient;

/**
 * @author heijens
 * @version 11.06.2005 added javadoc comments, changed queries because 
 * of removing spaces in tablenames and columns and removed code to test output.
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class createChart extends HttpServlet{
	
	private InterfaceA_EnvironmentBasedClient iaClient = new InterfaceA_EnvironmentBasedClient("http://localhost:8080/yawl/ia");
	

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
	        } catch (Exception e) {
      	      e.printStackTrace();
	        }
        }
    }

    

    private void initialiseDBConn() {
    }


    public void doGet(HttpServletRequest req, HttpServletResponse res){
	if (image!=null) {
	    res.setContentType("image/jpg");
	    try {
		ChartUtilities.writeBufferedImageAsJPEG(res.getOutputStream(),image);
	    } catch (Exception e) {
		e.printStackTrace();
	    }	
	    //doPost(req, res);
	} else if (parseList !=null) {
	    //return a table
	    System.out.println("creating table!!!!!!!!!!!!!!!");

	    try {
		PrintWriter out = res.getWriter();
		
		out.println("<table width=\"90%\" border=\"1\" bgcolor=\"#ffffff\">");	
		
		for (int i = 0; i < parseList.size(); i++) {
		    Object[] results = (Object[]) parseList.get(i);
		    out.println("<tr>");
		    for (int j = 0; j < results.length; j++) {
			out.println("<td> " + results[j] + "</td>");			
		    }
		    out.println("</tr>");
		    
		}
		out.println("</table>");	
		
	    } catch (Exception e) {
		e.printStackTrace();
	    }	
	} else {
	    System.out.println("creating nothing :(");
	}
	         
	
    }
    
    
    public void doPost(HttpServletRequest req, HttpServletResponse res){	
	
	Connection connection = null;
	DBconnection.loadDriver("org.postgresql.Driver");
	
	boolean isClosed = DBconnection.getConnection();
	if(isClosed == true){
	    DBconnection.getConnection();
	}
	
	DBconnection.printMetaData();
	
	String query = req.getParameter("query");
	String graph = req.getParameter("selectgraph");
	String spec = req.getParameter("selectspec");
	String caseid = req.getParameter("caseid");
	String action = req.getParameter("action");
	
	int granularity = 0;
	String time = req.getParameter("time");
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

	System.out.println(query);
	System.out.println(action);
	
	if(query.compareToIgnoreCase("Display a summary of the status of the work items")== 0){
	    if (action.equals("Create chart")) {
		if (graph.equals("Pie chart")) {
		    generatePieChart(_model.getWorkItemStatus(spec), graph, caseid, res);
		} else {
		    generateBarChart(_model.getWorkItemStatus(spec), graph, caseid, res);
		}
	    } else {
		generateTable(_model.getWorkItemStatus(spec), graph, caseid, res);		
	    }	
	}
	if(query.compareToIgnoreCase("Display completion time for the different cases")== 0){
	    if (action.equals("Create chart")) {
		if (graph.equals("Pie chart")) {
		    generatePieChart(_model.getCaseProcessingTime(spec,DatabaseGatewayImpl.TIMEFORMAT_LONG, granularity), graph, caseid, res);
		} else {
		    generateBarChart(_model.getCaseProcessingTime(spec,DatabaseGatewayImpl.TIMEFORMAT_LONG, granularity), graph, caseid, res);
		}		
	    } else {
		generateTable(_model.getCaseProcessingTime(spec,DatabaseGatewayImpl.TIMEFORMAT_STRING, granularity), graph, caseid, res);
	    }	
	}
	if (query.compareToIgnoreCase("Display average specification completion time")==0) {
	    if (action.equals("Create chart")) {
		if (graph.equals("Pie chart")) {
		    generatePieChart(_model.getAverageSpecTimes(DatabaseGatewayImpl.TIMEFORMAT_LONG, granularity), graph, caseid, res);	
		} else {
		    generateBarChart(_model.getAverageSpecTimes(DatabaseGatewayImpl.TIMEFORMAT_LONG, granularity), graph, caseid, res);	
		}
		
	    } else {
		generateTable(_model.getAverageSpecTimes(DatabaseGatewayImpl.TIMEFORMAT_STRING, granularity), graph, caseid, res);
	    }	
	}	
	try {
	    if (action.equals("Create chart")) {
		getServletContext().getRequestDispatcher("/overview.jsp?success=true").forward(req, res);
	    } else {
		getServletContext().getRequestDispatcher("/overview.jsp?table=true").forward(req, res);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
	
    
    public void generateTable(List parseList, String graph, String caseid, HttpServletResponse res) {

	image = null;
	this.parseList = parseList;
	
	String url =  new String(getServletContext().getInitParameter("admintool")+"/overview.jsp?table=true");						
    }

    public void generateBarChart(List parseList, String graph, String caseid, HttpServletResponse res){
	
	String idcase = "1";
	

    	DefaultCategoryDataset dataset = new DefaultCategoryDataset();
	try {	    		
	    for (int i = 0; i < parseList.size(); i++) {
		Object[] results = (Object[]) parseList.get(i);
		dataset.setValue(((Integer) results[0]).intValue(),"", (String) results[1]);
	    }
	    
	}
	catch (Exception e) {
	    e.printStackTrace();//nothing needs doing
	}
    	
	
	JFreeChart chart =  ChartFactory.createBarChart("",
							null, null, dataset,
							PlotOrientation.VERTICAL, true, false, false);
	try {
		image = chart.createBufferedImage(500,300);
	    
	    //ChartUtilities.saveChartAsJPEG(new File("../webapps/admintool/graphics/piegraph.jpg"), chart, 500, 300);
	    System.out.println("created file");
	}
	catch (Exception exc){
	    System.err.println("Error writing image to file");
	    exc.printStackTrace();
	}
	

	String url =  new String(getServletContext().getInitParameter("admintool")+"/overview.jsp?success=true");		
    }                              

    public void generatePieChart(List parseList, String graph, String caseid, HttpServletResponse res){
	
	LinkedList nrList = new LinkedList();
	List statusList =  new LinkedList();	       
	String idcase = "1";
	
	try {
	    		
	    for (int i = 0; i < parseList.size(); i++) {
		Object[] results = (Object[]) parseList.get(i);
		nrList.add(results[0]);
		statusList.add(results[1]);
	    }
	    
	}
	catch (Exception e) {
	    e.printStackTrace();//nothing needs doing
	}
	
	DefaultPieDataset pieDataSet = new DefaultPieDataset();
	for (int i=0; i<nrList.size(); i++){
	    Integer nrstring = (Integer) nrList.get(i);
	    int nr = nrstring.intValue();
	    String status = (String) statusList.get(i);
	    pieDataSet.setValue(status, nr);
	}
	
	JFreeChart chart = ChartFactory.createPieChart ("", pieDataSet, true, false, false);
	try {
		image = chart.createBufferedImage(500,300);
	    
	    //ChartUtilities.saveChartAsJPEG(new File("../webapps/admintool/graphics/piegraph.jpg"), chart, 500, 300);
	    System.out.println("created file");
	}
	catch (Exception exc){
	    System.err.println("Error writing image to file");
	    exc.printStackTrace();
	}
	

	String url =  new String(getServletContext().getInitParameter("admintool")+"/overview.jsp?success=true");		
    }                          
}
		
		
	       
