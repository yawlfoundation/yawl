/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.elements;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;

import org.jdom.Element;

import au.edu.qut.yawl.elements.data.YParameter;
import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.engine.EngineFactory;
import au.edu.qut.yawl.engine.domain.YWorkItem;
import au.edu.qut.yawl.exceptions.YDataStateException;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.exceptions.YQueryException;
import au.edu.qut.yawl.exceptions.YSchemaBuildingException;
import au.edu.qut.yawl.exceptions.YStateException;
import au.edu.qut.yawl.persistence.PolymorphicPersistableObject;
import au.edu.qut.yawl.util.YVerificationMessage;


/**
 * 
 * A YAtomicTask object is the executable equivalent of the YAtomicTask
 * in the YAWL paper.   They have the same properties and behaviour.
 * @author Lachlan Aldred
 * 
 * 
 * ***************************************************************************************
 * 
 * an atomic task represents an atomic unit of work (from the perspective of the 
 * workflow engine).
 * 
 * Set class to non-final for purposes of hibernate
 * 
 * @hibernate.subclass discriminator-value="2"
 */
@Entity
@DiscriminatorValue("atomic")
public class YAtomicTask extends YTask implements PolymorphicPersistableObject {
	/**
	 * One should only change the serialVersionUID when the class method signatures have changed.  The
	 * UID should stay the same so that future revisions of the class can still be backwards compatible
	 * with older revisions if method signatures have not changed. 
	 * Serial version format: year (4 digit) - month (2 digit) - yawl release version (4 digit)
	 */
	private static final long serialVersionUID = 2006030080l;

	/**
	 * Null constructor for hibernate only
	 *
	 */
	protected YAtomicTask() {
		super();
	}

    public YAtomicTask(String id, int joinType, int splitType, YNet container) {
        super(id, joinType, splitType, container);
    }


	public void setDataMappingsForEnablement(Map<String, String> map) {
		for (Map.Entry entry : map.entrySet()) {
			String key = entry.getKey().toString();
			String value = entry.getValue().toString();
			dataMappingsForTaskEnablementSet.add(new KeyValue(key, value));
		}
	}
    /**
     * http://forums.hibernate.org/viewtopic.php?t=955600&highlight=strings+map&sid=0811afbccf4990d002ddfeb507ccfe8d
     * 
     * Hibernate does not support Map<String, String> yet so we're going to use a set of KeyValue pairs as replacement.
     */
    @OneToMany(cascade=CascadeType.ALL)
    @JoinTable(
            name="task_enablement_map",
            joinColumns = { @JoinColumn( name="extern_id") },
            inverseJoinColumns = @JoinColumn( name="key_id")
    )
    @XmlTransient
    private Set<KeyValue> getDataMappingsForTaskEnablementSet() {
    	return dataMappingsForTaskEnablementSet;
    }
    private void setDataMappingsForTaskEnablementSet(Set<KeyValue> set) {
    	dataMappingsForTaskEnablementSet = set;
    }

    @Transient
    public List verify() {
        List messages = new Vector();
        messages.addAll(super.verify());
        if (_decompositionPrototype == null) {
            if (_multiInstAttr != null && (_multiInstAttr.getMaxInstances() > 1 ||
                    _multiInstAttr.getThreshold() > 1)) {
                messages.add(new YVerificationMessage(this, this + " cannot have multiInstances and a "
                        + " blank work description.", YVerificationMessage.ERROR_STATUS));
            }
        } else if( _decompositionPrototype instanceof YAWLServiceGateway ) {
        	messages.addAll(checkEnablementParameterMappings());
        } else /* if (!(_decompositionPrototype instanceof YAWLServiceGateway)) */ {
            messages.add(new YVerificationMessage(this, this + " task may not decompose to " +
                    "other than a WebServiceGateway.", YVerificationMessage.ERROR_STATUS));
        }
        return messages;
    }

    @Transient
    private Collection checkEnablementParameterMappings() {
        List messages = new ArrayList();
        //check that there is a link to each enablementParam
        Set enablementParamNamesAtGateway =
                ((YAWLServiceGateway) _decompositionPrototype).getEnablementParametersMap().keySet();
        Set enablementParamNamesAtTask = getDataMappingsForEnablement().keySet();
        //check that task input var maps to decomp input var
        for (Iterator iterator = enablementParamNamesAtGateway.iterator(); iterator.hasNext();) {
            String paramName = (String) iterator.next();
            if (!enablementParamNamesAtTask.contains(paramName)) {
                messages.add(new YVerificationMessage(this,
                        "The task (id= " + this.getID() + ")" +
                        " needs to be connected with the enablement parameter (" +
                        paramName + ")" + " of decomposition (" +
                        _decompositionPrototype + ").",
                        YVerificationMessage.ERROR_STATUS));
            }
        }
        for (Iterator iterator = enablementParamNamesAtTask.iterator(); iterator.hasNext();) {
            String paramNameAtTask = (String) iterator.next();

            String query = (String) getDataMappingsForEnablement().get(paramNameAtTask);
            messages.addAll(checkXQuery(query, paramNameAtTask));
            if (!enablementParamNamesAtGateway.contains(paramNameAtTask)) {
                messages.add(new YVerificationMessage(this,
                        "The task (id= " + this.getID() + ") " +
                        "cannot connect with enablement parameter (" +
                        paramNameAtTask + ") because it doesn't exist" +
                        " at its decomposition(" + _decompositionPrototype + ").",
                        YVerificationMessage.ERROR_STATUS));
            }
        }
        return messages;
    }


    protected void startOne(YIdentifier id) throws YPersistenceException {
        this._mi_entered.removeOne(id);
        this._mi_executing.add(id);
    }

    // FIXME: XXX this is never called anywhere, should it be here?
    /**
     * @deprecated
     */
    @Deprecated
    @Transient
    public boolean isRunning() {
        return _i != null;
    }


    public synchronized void cancel() throws YPersistenceException {
        YIdentifier i = _i;
        super.cancel();
        if (i != null && _decompositionPrototype != null) {
            YWorkItem workItem = _workItemRepository.getWorkItem(i.toString(), getID());
            i = null;
            if (null != workItem) {
                _workItemRepository.removeWorkItemFamily(workItem);
                //if applicable cancel yawl service
                YAWLServiceGateway wsgw = (YAWLServiceGateway) getDecompositionPrototype();
                //DM: this IF isn't needed: the outermost IF makes sure the decomposition isn't null
                //LA: Actually it is needed - exists(decomp) !==> exists(webservicegateway)
                if (wsgw != null) {
                    YAWLServiceReference ys = wsgw.getYawlService();
                    if (ys != null) {
                    	EngineFactory.createYEngine().announceCancellationToEnvironment(ys, workItem);
                    }
                }
            }
        }
    }

    public boolean t_rollBackToFired(YIdentifier caseID) throws YPersistenceException {
        if (_mi_executing.contains(caseID)) {
            _mi_executing.removeOne(caseID);
            _mi_entered.add(caseID);
            return true;
        }
        return false;
    }


    /**
     * 
     * @return
     * @hibernate.one-to-one name="net" class="au.edu.qut.yawl.elements.YNet"
     */
    @OneToOne(cascade={CascadeType.ALL})
    public YNet getNet() {
        return _net;
    }
    
    /**
     * Mutator method required by hibernate.
     * 
     * @param net
     */
    protected void setNet(YNet net) {
    	_net = net;
    }

    @Transient
    public Object clone() throws CloneNotSupportedException {
        YNet copyContainer = _net.getCloneContainer();
        if (copyContainer.getNetElements().containsKey(this.getID())) {
            return copyContainer.getNetElement(this.getID());
        }
        YAtomicTask copy = (YAtomicTask) super.clone();
        return copy;
    }

    @Transient
    public Element prepareEnablementData()
            throws YQueryException, YSchemaBuildingException, YDataStateException, YStateException {
        if (null == getDecompositionPrototype()) {
            return null;
        }
        Element enablementData = produceDataRootElement();
        YAWLServiceGateway serviceGateway = (YAWLServiceGateway) _decompositionPrototype;
        List enablementParams = new ArrayList(serviceGateway.getEnablementParametersMap().values());
        Collections.sort(enablementParams);
        for (int i = 0; i < enablementParams.size(); i++) {
            YParameter parameter = (YParameter) enablementParams.get(i);
            String paramName = parameter.getName() != null ?
                    parameter.getName() : parameter.getElementName();
            String expression = (String) getDataMappingsForEnablement().get(paramName);

            Element result = performDataExtraction(expression, parameter);
            enablementData.addContent((Element) result.clone());
        }
        return enablementData;
    }
}
