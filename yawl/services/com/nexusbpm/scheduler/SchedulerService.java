/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package com.nexusbpm.scheduler;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerUtils;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SchedulerService extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private Scheduler scheduler;

	@Override
	public void init(ServletConfig arg0) throws ServletException {
		super.init(arg0);
		try {
			start();
		} catch (Exception e) {
			System.out.println("Unable to start quartz scheduler");
			e.printStackTrace();
		}
	}

	public void start() throws SchedulerException, IOException {

		Properties p = new Properties();
		InputStream inStream = this.getServletContext().getResourceAsStream("/quartz.server.properties");
		p.load(inStream);
		p.list(System.out);
		System.getProperties().putAll(p);
		System.out.println("checking schema existence...");
		try {
			QuartzSchema.createIfMissing();
		} catch (Exception e) {} 

		scheduler = StdSchedulerFactory.getDefaultScheduler();

        try {
    		String[] paths = { p.getProperty("org.quartz.plugin.yawlevent.appContextUrl") };
    		ApplicationContext ctx = new ClassPathXmlApplicationContext(paths);
    		QuartzEventDao dao = (QuartzEventDao) ctx.getBean("quartzDao");

            String name = "QuartzEventDataSource";
            QuartzEventDataSource engine = new QuartzEventDataSourceImpl(dao);
            QuartzEventDataSource stub =
                (QuartzEventDataSource) UnicastRemoteObject.exportObject(engine, 0);
            String host = p.getProperty("org.quartz.scheduler.rmi.registryHost");
            int port = Integer.parseInt(p.getProperty("org.quartz.scheduler.rmi.registryPort"));
            Registry registry = LocateRegistry.getRegistry(host, port);
            registry.rebind(name, stub);
            System.out.println("QuartzEventDataSource bound");
        } catch (Exception e) {
            System.err.println("QuartzEventDataSource exception:");
            e.printStackTrace();
        }
		scheduler.start();

		System.out.println("scheduler startup completed for "
				+ scheduler.getClass().getName() + ":"
				+ scheduler.getSchedulerInstanceId());
	}

}
