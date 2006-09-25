package au.edu.qut.yawl.timeService;

import au.edu.qut.yawl.elements.KeyValue;
import au.edu.qut.yawl.elements.YAWLServiceGateway;
import au.edu.qut.yawl.elements.YAWLServiceReference;
import au.edu.qut.yawl.elements.YAtomicTask;
import au.edu.qut.yawl.elements.YCompositeTask;
import au.edu.qut.yawl.elements.YCondition;
import au.edu.qut.yawl.elements.YDecomposition;
import au.edu.qut.yawl.elements.YExternalNetElement;
import au.edu.qut.yawl.elements.YFlow;
import au.edu.qut.yawl.elements.YInputCondition;
import au.edu.qut.yawl.elements.YMetaData;
import au.edu.qut.yawl.elements.YMultiInstanceAttributes;
import au.edu.qut.yawl.elements.YNet;
import au.edu.qut.yawl.elements.YOutputCondition;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.elements.YTask;
import au.edu.qut.yawl.elements.data.YParameter;
import au.edu.qut.yawl.elements.data.YVariable;
import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.elements.state.YInternalCondition;
import au.edu.qut.yawl.engine.YNetRunner;
import au.edu.qut.yawl.engine.domain.YCaseData;
import au.edu.qut.yawl.engine.domain.YWorkItem;
import au.edu.qut.yawl.engine.domain.YWorkItemID;
import au.edu.qut.yawl.events.Event;
import au.edu.qut.yawl.persistence.dao.restrictions.RestrictionCriterionConverter;
import au.edu.qut.yawl.persistence.dao.restrictions.Unrestricted;
import au.edu.qut.yawl.worklist.model.WorkItemRecord;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.Basic;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Entity;

import org.hibernate.Criteria;
import org.hibernate.Transaction;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Environment;

@Entity
public class InternalRunnerHibernateDAO implements DAO{

	private static SessionFactory sessionFactory;
	private static AnnotationConfiguration cfg;
	private static Session session;
	private static Class[] classes = new Class[] {
						WorkItemRecord.class,
						InternalRunnerHibernateDAO.class,

				};
	
	private synchronized static void initializeSessions() {
		//if ( sessionFactory != null ) sessionFactory.close();
		try {
			AnnotationConfiguration config = (AnnotationConfiguration) new AnnotationConfiguration()
			.setProperty(Environment.HBM2DDL_AUTO, "update");
			cfg = config;
	        
			for (int i=0; i<classes.length; i++) {
				cfg.addAnnotatedClass( classes[i] );
			}
			sessionFactory = cfg.buildSessionFactory();
			session = sessionFactory.openSession();
		}
		catch (Error e) {
			e.printStackTrace();
		}
	}
	
	private Session openSession() throws HibernateException {
		if (sessionFactory==null) {
			initializeSessions();
		} if (session==null) {
			session = sessionFactory.openSession();
		}
		
		return session;
	}
	
	WorkItemRecord itemRecord = null;
	long time = 0;
	String absolutetime = "";
	
	
	public InternalRunnerHibernateDAO() {
		if (session==null) {
			initializeSessions();
		}

	}
	
	public void save(InternalRunner r) {
		this.itemRecord = r.itemRecord;
		this.time = r.time;
		this.absolutetime= r.absolutetime;
		
		Transaction tx = session.beginTransaction();
		session.saveOrUpdate(this);
		tx.commit();
	}
	
	public void delete() {
		Transaction tx = session.beginTransaction();
		session.delete(this);
		tx.commit();		
	}


	public List retrieveAll() {
		
		if (session==null) {
			initializeSessions();
		}
		
		Transaction tx = session.beginTransaction();
        Criteria query = session.createCriteria( InternalRunnerHibernateDAO.class );
        List tmp = query.list();
        
        Set set = new HashSet( tmp );
		
        List retval = new ArrayList( set );
        List<InternalRunner> runners = new ArrayList<InternalRunner>();
        
		for (int i = 0; i < retval.size();i++) {
			InternalRunnerHibernateDAO runnerdao = (InternalRunnerHibernateDAO) retval.get(i);
			InternalRunner runner = null;
			if (runnerdao.getAbsoluteTime()!=null) {			
				runner = new InternalRunner(runnerdao.getAbsoluteTime(),
							runnerdao.getWorkItemRecord(),
							"NO_SESSION");
			} else {
				runner = new InternalRunner(runnerdao.getTime(),
							runnerdao.getWorkItemRecord(),
							"NO_SESSION");		
			}
			runner.setDAO(runnerdao);
			runners.add(runner);
		}
        
		tx.commit();
		
		return runners;
       
	}
	
    @OneToOne(cascade={CascadeType.ALL}, fetch = FetchType.EAGER)
	public WorkItemRecord getWorkItemRecord() {
		return itemRecord;
	}
	public void setWorkItemRecord(WorkItemRecord rec) {
		itemRecord = rec;
	}
	
	@Basic
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;	
	}
	
	@Basic
	public String getAbsoluteTime() {
		return this.absolutetime;
	}
	public void setAbsoluteTime(String abs) {
		this.absolutetime = abs;
	}
    Long _dbid;

    @Id
    @Column(name="decomp_id")
    @GeneratedValue(strategy=GenerationType.SEQUENCE)
    public Long getDbID() {
    	return _dbid;
    }

    private void setDbID(Long dbid) {
    	_dbid = dbid;
    }
	
}
