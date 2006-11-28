/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package com.nexusbpm.scheduler;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.context.ApplicationContext;

import au.edu.qut.yawl.util.configuration.BootstrapConfiguration;

import com.nexusbpm.services.ServiceConfiguration;

public class SchedulerService extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private Scheduler scheduler;

	@Override
	public void init(ServletConfig arg0) throws ServletException {
		super.init(arg0);
		try {
			System.setProperty("java.rmi.server.useCodebaseOnly", "true");
			ServiceConfiguration sc = new ServiceConfiguration(arg0);
			BootstrapConfiguration.setInstance(sc);
			start();
		} catch (Exception e) {
			System.out.println("Unable to start quartz scheduler");
			e.printStackTrace();
		}
	}

	public void start() throws SchedulerException, IOException {
		ApplicationContext ac = BootstrapConfiguration.getInstance().getApplicationContext();
		System.out.println("checking schema existence...");
		try {
			QuartzSchema schema = (QuartzSchema) ac.getBean("quartzSchema");
			schema.createIfMissing();
		} catch (Exception e) {
			if (!(e instanceof SQLException)) {
				e.printStackTrace();
			}
		} 
		scheduler = ((StdSchedulerFactory) ac.getBean("schedulerFactory")).getDefaultScheduler();
        try {
        	QuartzEventDataSourceFactory factory = (QuartzEventDataSourceFactory) ac.getBean("quartzEventDataSourceFactory");
        	factory.registerDataSource();
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

	public Scheduler getScheduler() {
		return scheduler;
	}
}