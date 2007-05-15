package com.nexusbpm.services.r;

import java.io.IOException;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import com.nexusbpm.services.data.NexusServiceData;

import junit.framework.TestCase;

public class RConnectionTest extends TestCase {

	InternalRService r;
	String unique = "output-" + System.currentTimeMillis();
	
	private void setSharedData(RServiceData data) {
		data.setServer("10.161.27.159");
		data.setPlain("requestId", unique);
	}
	
	private RServiceData getPlotData() throws Exception {
		RServiceData data = new RServiceData();
		setSharedData(data);
		data.setDouble("radius", 1000.0);
		data.setImage("imageLocation", null);
		data.setCode( 
			"t=seq(0,2*pi,length=10000);\n" +
			"fileName=sprintf(\"%s.png\", requestId);\n" +
			"png(filename=fileName, width=800, height=600, bg=\"grey\");\n" +
			"plot(radius*cos(t * 5),radius*sin(t * 3), type=\"l\", col=\"blue\");\n" +
			"dev.off();\n" +
			"imageLocation=sprintf(\"//%s/rserve/%s\", serverAddress, fileName);\n"
		);
		return data;
	}

	public void testRPlottingWithOutputGraph() throws Exception{
		r = new InternalRService();
		RServiceData data = (RServiceData) r.execute(getPlotData());
		System.out.println(data.getOutput());
		System.out.println(data.getError());
		
		if (!"".equals(data.getPlain("error"))) {
			System.out.println("error: " + data.getPlain("error"));
			return;
		}
		else {
			System.out.println(data.getImageURL("imageLocation"));
			Icon icon = (Icon) new ImageIcon(data.getImageURL("imageLocation"));
			JOptionPane.showConfirmDialog(null, "", "", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, icon);
		}
	}


	private RServiceData getDBData() throws Exception {
		RServiceData data = new RServiceData();
		setSharedData(data);
		data.setPlain("fileName", null);
		data.setPlain("code", 
			"library(RJDBC);\n" +
			"drv<-JDBC(\"org.postgresql.Driver\",\"D:/workspace/yawl/build/3rdParty/lib/postgresql-8.0-311.jdbc3.jar\");\n" + 
			"conn<-dbConnect(drv,\"jdbc:postgresql:yawl\",\"postgres\",\"admin\");\n" + 
			"d<-dbGetQuery(conn, \"select * from yspecification\");\n" +
			"location<-sprintf(\"%s.csv\", requestId);\n" +
			"write.table(d, file=location,sep=\",\",row.names=FALSE);\n" + 
			"fileName=sprintf(\"//%s/rserve/%s\", serverAddress, location);\n"
			);
		return data;
	}

	public void testDBR() throws Exception { //first i installed the rjdbc package on R...
		r = new InternalRService();
		RServiceData data = (RServiceData) r.execute(getDBData());
		System.out.println(data.getOutput());
		System.out.println(data.getError());
		
	}
	
	
}
