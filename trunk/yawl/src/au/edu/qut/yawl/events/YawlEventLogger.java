/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.events;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

import au.edu.qut.yawl.elements.state.IdentifierSequence;
import au.edu.qut.yawl.engine.EngineFactory;
import au.edu.qut.yawl.engine.domain.YWorkItem;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.persistence.dao.DAO;
import au.edu.qut.yawl.persistence.dao.restrictions.LogicalRestriction;
import au.edu.qut.yawl.persistence.dao.restrictions.PropertyRestriction;
import au.edu.qut.yawl.persistence.dao.restrictions.Restriction;
import au.edu.qut.yawl.persistence.dao.restrictions.PropertyRestriction.Comparison;

public class YawlEventLogger {

	List<YEventDispatcher> dispatchers = new ArrayList<YEventDispatcher>();
	
    public boolean enabled = true;

    public static YawlEventLogger yawllog = null;

    public static YawlEventLogger getInstance() {
        if (yawllog == null) {
            yawllog = new YawlEventLogger();
        }
        return yawllog;
    }

    private YawlEventLogger() {
    	yawllog = this;
    }


    public void retry() {

        enabled = true;
    }

    public void logWorkItemEvent(
            String identifier,
            String taskid,
            YWorkItem.Status event,
            String resource,
            String description
            ) throws YPersistenceException {


        this.logWorkItemEvent(
                identifier,
                taskid,
                event,
                resource,
                description,
                null);

    }

    public void logWorkItemEvent(
            String identifier,
            String taskid,
            YWorkItem.Status event,
            String resource,
            String description,
            String time
            ) throws YPersistenceException {


        if (!enabled)
            return ;



        YWorkItemEvent newevent = new YWorkItemEvent();
        newevent.setId(getId());
        
        newevent.setIdentifier(identifier);

        newevent.setTaskid(taskid);

        newevent.setEvent(""+event);
        newevent.setResource(resource);
        newevent.setDescription(description);

        if (time != null) {
            newevent.setTime(new Long(time).longValue());
        } else {
            newevent.setTime(System.currentTimeMillis());
        }


        for (YEventDispatcher dispatcher: dispatchers) {
        	dispatcher.fireEvent(newevent);
        }

    }

    public int logData(String name, String data, String io) throws YPersistenceException {

        if (!enabled)
            return -1;

        YDataEvent ylogdata = new YDataEvent();
        ylogdata.setIo(io);
        
        ylogdata.setEventid(""+getCurrentId());
        
        ylogdata.setValue(data);
        ylogdata.setPort(name);

        for (YEventDispatcher dispatcher: dispatchers) {
        	dispatcher.fireEvent(ylogdata);
        }


        return 0;

    }

    /**
     *
     *
     * AJH - Modified to return the Log Indentifier such that caller can persist it.
     *
     * @param caseid
     * @param resource
     * @param spec
     * @throws YPersistenceException
     */
    public void logCaseCreated(String caseid,
                               String resource,
                               String spec) throws YPersistenceException {
        if (!enabled) return;

        YCaseEvent ylogid = new YCaseEvent();
        ylogid.setIdentifier(caseid);
        ylogid.setCreatedby(resource);
        ylogid.setSpecification(spec);
        ylogid.setCreated(System.currentTimeMillis());

        for (YEventDispatcher dispatcher: dispatchers) {

        	dispatcher.fireEvent(ylogid);
        }
    }

    public void logCaseCancelled(String caseid) throws YPersistenceException {

        if (!enabled)
            return;

        Restriction restriction = new PropertyRestriction("identifier",Comparison.EQUAL,caseid);
        List<YCaseEvent> events = EngineFactory.createYEngine().getDao().retrieveByRestriction(YCaseEvent.class,restriction);
        
        if (events.size()!=1) {
        	return;
        }
        
        YCaseEvent ylogid = events.get(0); 
        if (ylogid != null) {
            ylogid.setIdentifier(caseid);
            ylogid.setCancelled(System.currentTimeMillis());

            for (YEventDispatcher dispatcher: dispatchers) {
            	dispatcher.fireEvent(ylogid);
            }

        }
    }

    public void logCaseCompleted(String caseid) throws YPersistenceException {

        if (!enabled)
            return;

        Restriction restriction = new PropertyRestriction("identifier",Comparison.EQUAL,caseid);
        List<YCaseEvent> events = EngineFactory.createYEngine().getDao().retrieveByRestriction(YCaseEvent.class,restriction);
        
        if (events.size()!=1) {
        	return;
        }
        
        YCaseEvent ylogid = events.get(0); 
        
        if (ylogid != null) {
            ylogid.setIdentifier(caseid);
            ylogid.setCompleted(System.currentTimeMillis());

            for (YEventDispatcher dispatcher: dispatchers) {
            	dispatcher.fireEvent(ylogid);
            }

        }

    }

    public void createChild(String parent, String child) throws YPersistenceException {

        if (!enabled)
            return;

        YCaseEvent ylogid = new YCaseEvent();
        ylogid.setIdentifier(child);
        ylogid.setParent(parent);

        for (YEventDispatcher dispatcher: dispatchers) {
        	dispatcher.fireEvent(ylogid);
        }

    }
    
    
    public void setDispatchers(List<YEventDispatcher> disp) {
    	this.dispatchers = disp;
    }
    public List<YEventDispatcher> getDispatchers() {
    	return this.dispatchers;
    }
    
    
    static int nextID = 100000;

    public String getNextCaseId() {
    	return "" + nextID++;
//        if (!enabled) {
//            int id = new Integer(caseId).intValue();
//            id++;
//            caseId = new Integer(id).toString();
//        } else {
//            String nextid = null;
//            try {
//
////todo AJH Changed for persistence
////        nextid = YPersistance.getInstance().getMaxCase();
//                nextid =  EngineFactory.createYEngine().getMaxCase();
//
//                if (nextid.indexOf(".") != -1) {
//                    String idstring = nextid.substring(0, nextid.indexOf("."));
//                    int x = new Integer(idstring).intValue();
//                    nextid = new String(new Integer(++x).toString());
//                } else {
//                    int x = new Integer(nextid).intValue();
//                    nextid = new String(new Integer(++x).toString());
//                }
//
//            } catch (Exception e) {
//                nextid = null;
//                e.printStackTrace();
//
//            }
//
//            if (nextid == null || nextid.equals("-1")) {
//                enabled = false;
//                int id = new Integer(caseId).intValue();
//                id++;
//                caseId = new Integer(id).toString();
//            } else {
//                caseId = nextid;
//            }
//        }

//        return caseId;
    }
    
    public long getId() throws YPersistenceException {
    	DAO dao = EngineFactory.createYEngine().getDao();
    	Restriction restriction = new PropertyRestriction("sequence", Comparison.EQUAL, "logsequence");
    	List sequences = dao.retrieveByRestriction(IdentifierSequence.class, restriction);

    	int value = 1;
    	IdentifierSequence sequence = new IdentifierSequence("logsequence");            
    	if(sequences.size() > 0) {
    		sequence = (IdentifierSequence) sequences.get(0);
    		value = sequence.getValue().intValue() + 1;
    	}          
    	sequence.setValue(Long.valueOf(value));

    	dao.save(sequence);
    	return value;
    }
    
    public long getCurrentId() throws YPersistenceException {
    	DAO dao = EngineFactory.createYEngine().getDao();
    	Restriction restriction = new PropertyRestriction("sequence", Comparison.EQUAL, "logsequence");
    	List sequences = dao.retrieveByRestriction(IdentifierSequence.class, restriction);

    	int value = 1;
    	IdentifierSequence sequence = new IdentifierSequence("logsequence");            
    	if(sequences.size() > 0) {
    		sequence = (IdentifierSequence) sequences.get(0);
    		value = sequence.getValue().intValue() + 1;
    	}          

    	return value;
    }

}
