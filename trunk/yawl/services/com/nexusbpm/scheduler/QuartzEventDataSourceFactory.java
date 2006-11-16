package com.nexusbpm.scheduler;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;



public class QuartzEventDataSourceFactory {

	public static QuartzEventDataSource getDataSource(boolean isLocal) throws RemoteException, NotBoundException{
		QuartzEventDataSource source;
		if (isLocal) {
			String path = System.getProperty("org.quartz.plugin.yawlevent.appContextUrl");
			ApplicationContext ctx = new ClassPathXmlApplicationContext(new String[] {path});
			QuartzEventDao dao = (QuartzEventDao) ctx.getBean("quartzDao");
			source = new QuartzEventDataSourceImpl(dao);
		} else {
			String host = System.getProperty("org.quartz.scheduler.rmi.registryHost", "localhost");
			int port = Integer.parseInt(System.getProperty("org.quartz.scheduler.rmi.registryPort", "1099"));
			Registry registry = LocateRegistry.getRegistry(host, port);
			source = (QuartzEventDataSource) registry.lookup("QuartzEventDataSource");
		}
		return source;
	}

}
