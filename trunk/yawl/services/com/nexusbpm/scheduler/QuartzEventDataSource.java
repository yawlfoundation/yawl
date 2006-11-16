package com.nexusbpm.scheduler;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;

public interface QuartzEventDataSource extends Remote {

	List<QuartzEvent> getEventsBetween(Date startDate, Date endDate) throws RemoteException;
	
}
