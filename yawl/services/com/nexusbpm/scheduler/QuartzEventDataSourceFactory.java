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
import java.rmi.server.UnicastRemoteObject;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class QuartzEventDataSourceFactory {

	public String rmiHost;
	public String rmiPort;
	public String registeredName;
	public QuartzEventDao localDao;
	
	public QuartzEventDataSource getDataSource(boolean isLocal, boolean isCached) throws RemoteException, NotBoundException {
		QuartzEventDataSource source = getDataSource(isLocal);
		if (isCached) {
			source = new CachedQuartzEventDataSource(source);
		}
		return source;
	}
	
	public QuartzEventDataSource getDataSource(boolean isLocal) throws RemoteException, NotBoundException{
		QuartzEventDataSource source;
		if (isLocal) {
			source = new QuartzEventDataSourceImpl(localDao);
		} else {
			Registry registry = LocateRegistry.getRegistry(rmiHost, Integer.parseInt(rmiPort));
			for (int i = 0; i < registry.list().length; i++) {
				System.out.println(registry.list()[i]);
			}
			source = (QuartzEventDataSource) registry.lookup(registeredName);
		}
		return source;
	}
	
	public void registerDataSource() throws RemoteException {
		try {
			LocateRegistry.createRegistry(Integer.parseInt(rmiPort));
		} catch (RemoteException e) {}//dont worry about it...
		QuartzEventDataSource engine = new QuartzEventDataSourceImpl(localDao);
        QuartzEventDataSource stub =
            (QuartzEventDataSource) UnicastRemoteObject.exportObject(engine, 0);
        Registry registry = LocateRegistry.getRegistry(rmiHost, Integer.parseInt(rmiPort));
        registry.rebind(registeredName, stub);
	}

	public String getRegisteredName() {
		return registeredName;
	}

	public void setRegisteredName(String registryName) {
		this.registeredName = registryName;
	}

	public String getRmiHost() {
		return rmiHost;
	}

	public void setRmiHost(String rmiUri) {
		this.rmiHost = rmiUri;
	}

	public String getRmiPort() {
		return rmiPort;
	}

	public void setRmiPort(String rmiPort) {
		this.rmiPort = rmiPort;
	}

	public QuartzEventDao getLocalDao() {
		return localDao;
	}

	public void setLocalDao(QuartzEventDao localDao) {
		this.localDao = localDao;
	}

}
