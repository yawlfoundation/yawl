/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package com.nexusbpm.scheduler;

import java.util.Date;
import java.util.List;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class SpringHibernateQuartzEventDao extends HibernateDaoSupport
		implements QuartzEventDao {

	public QuartzEvent getRecord(Long id) {
		return (QuartzEvent) getHibernateTemplate().get(QuartzEvent.class, id);
	}

	public List<QuartzEvent> getRecords(Date startDate, Date endDate) {
		return getHibernateTemplate().find(
				"from QuartzEvent qe where (qe.scheduledFireTime >= ?) and (qe.scheduledFireTime < ?)", 
				new Object[] {startDate, endDate});
	}

	public void saveRecord(QuartzEvent record) {
		getHibernateTemplate().saveOrUpdate(record);
	}

	public void removeRecord(Long id) {
		Object record = getHibernateTemplate().load(QuartzEvent.class, id);
		getHibernateTemplate().delete(record);
	}
}
