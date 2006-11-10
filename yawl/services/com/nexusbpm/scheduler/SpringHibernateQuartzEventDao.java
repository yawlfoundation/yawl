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
