/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.scheduler;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class QuartzEventDataSourceFactory {

	public static QuartzEventDataSource getDataSource(boolean isLocal, boolean isCached) throws RemoteException, NotBoundException {
		QuartzEventDataSource source = getDataSource(isLocal);
		if (isCached) {
			source = new CachedQuartzEventDataSource(source);
		}
		return source;
	}
	
	public static QuartzEventDataSource getDataSource(boolean isLocal) throws RemoteException, NotBoundException{
		QuartzEventDataSource source;
		if (isLocal) {
			String path = System.getProperty("org.quartz.plugin.yawlevent.appContextUrl");
			ApplicationContext ctx = new ClassPathXmlApplicationContext(new String[] {path});
			QuartzEventDao dao = (QuartzEventDao) ctx.getBean("quartzDao");
			source = new QuartzEventDataSourceImpl(dao);
		} else {
			String host = System.getProperty("org.quartz.scheduler.rmi.registryHost", "localhost");
			int port = Integer.parseInt(System.getProperty("org.quartz.scheduler.rmi.registryPort", "1098"));
			Registry registry = LocateRegistry.getRegistry(host, port);
			source = (QuartzEventDataSource) registry.lookup("QuartzEventDataSource");
		}
		return source;
	}

}
