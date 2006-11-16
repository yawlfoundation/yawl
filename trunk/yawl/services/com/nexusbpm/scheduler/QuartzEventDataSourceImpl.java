package com.nexusbpm.scheduler;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;

public class QuartzEventDataSourceImpl implements QuartzEventDataSource {

	private QuartzEventDao dao;	
	
	public QuartzEventDataSourceImpl(QuartzEventDao dao) {
		this.dao = dao;
	}
	
	public List<QuartzEvent> getEventsBetween(Date startDate, Date endDate)
			throws RemoteException {
		List<QuartzEvent> retval = dao.getRecords(startDate, endDate);
		return retval;
	}

}
