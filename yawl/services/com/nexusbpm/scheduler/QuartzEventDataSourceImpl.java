/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

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
