package com.nexusbpm.scheduler;

import java.util.List;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class SpringHibernateQuartzEventDao extends HibernateDaoSupport
		implements QuartzEventDao {

	public QuartzEvent getRecord(Long id) {
		return (QuartzEvent) getHibernateTemplate().get(QuartzEvent.class, id);
	}

	public List<QuartzEvent> getRecords() {
		return getHibernateTemplate().find("from QuartzEvent");
	}

	public void saveRecord(QuartzEvent record) {
		getHibernateTemplate().saveOrUpdate(record);
	}

	public void removeRecord(Long id) {
		Object record = getHibernateTemplate().load(QuartzEvent.class, id);
		getHibernateTemplate().delete(record);
	}
}
