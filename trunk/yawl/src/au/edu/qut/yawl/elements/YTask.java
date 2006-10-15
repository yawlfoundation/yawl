/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.elements;

import java.io.StringReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Lob;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.ManyToMany;
import javax.persistence.Transient;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.Configuration;
import net.sf.saxon.om.DocumentInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.query.DynamicQueryContext;
import net.sf.saxon.query.QueryProcessor;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.xpath.XPathException;

import org.apache.log4j.Logger;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Where;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;
import org.xml.sax.InputSource;

import au.edu.qut.yawl.elements.data.YParameter;
import au.edu.qut.yawl.elements.data.YVariable;
import au.edu.qut.yawl.elements.e2wfoj.E2WFOJNet;
import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.elements.state.YInternalCondition;
import au.edu.qut.yawl.engine.domain.YWorkItemRepository;
import au.edu.qut.yawl.events.StateEvent;
import au.edu.qut.yawl.exceptions.YAWLException;
import au.edu.qut.yawl.exceptions.YDataQueryException;
import au.edu.qut.yawl.exceptions.YDataStateException;
import au.edu.qut.yawl.exceptions.YDataValidationException;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.exceptions.YQueryException;
import au.edu.qut.yawl.exceptions.YSchemaBuildingException;
import au.edu.qut.yawl.exceptions.YStateException;
import au.edu.qut.yawl.schema.Instruction;
import au.edu.qut.yawl.schema.XMLToolsForYAWL;
import au.edu.qut.yawl.util.YSaxonOutPutter;
import au.edu.qut.yawl.util.YVerificationMessage;

/**
 * A superclass of any type of task in the YAWL paper.
 *
 *
 * @author Lachlan Aldred
 *
 *
 * ***************************************************************************************
 *
 * This abstract class is the superclass of the two types of concrete task (YAtomicTask &
 * YCompositeTask).  This class contains possibly the lion's share of the business process
 * execution logic.  It also contains many internal state elements (such as
 * YInternalCondition)
 *
 * @hibernate.subclass discriminator-value="1"
 */
@Entity
@DiscriminatorValue("task")
public abstract class YTask extends YExternalNetElement {
    private static final Logger LOG = Logger.getLogger(YTask.class);
    //class members
    private static final Random _random = new Random(new Date().getTime());
    public static final int _AND = 95;
    public static final int _OR = 103;
    public static final int _XOR = 126;

    //internal state nodes
    protected YIdentifier _i;
    //sorry for now need public for tests
    public YInternalCondition _mi_active = new YInternalCondition(new StateEvent(StateEvent.ACTIVE), this);
    protected YInternalCondition _mi_entered = new YInternalCondition(new StateEvent(StateEvent.ENTERED), this);
    protected YInternalCondition _mi_complete = new YInternalCondition(new StateEvent(StateEvent.COMPLETE), this);
    protected YInternalCondition _mi_executing = new YInternalCondition(new StateEvent(StateEvent.EXECUTING), this);
    protected static final YWorkItemRepository _workItemRepository = YWorkItemRepository.getInstance();

    //private attributes
    private int _splitType;
    private int _joinType;
    protected YMultiInstanceAttributes _multiInstAttr;
    private Set<YExternalNetElement> _removeSet = new HashSet<YExternalNetElement>();
    protected Map<String, KeyValue> dataMappingsForTaskEnablementSet = new HashMap<String, KeyValue>();
    protected YDecomposition _decompositionPrototype;
    private static final String PERFORM_OUTBOUND_SCHEMA_VALIDATION = "skipOutboundSchemaValidation";

    //input data storage
    private Map _caseToDataMap = new HashMap();
    private Iterator _multiInstanceSpecificParamsIterator;
    private Map _localVariableNameToReplaceableOuptutData;
    private Document _groupedMultiInstanceOutputData;
    private String _schemaForTaskCompletion;

    //Reset net association
    private E2WFOJNet _resetNet;


    /*
      INSERTED METHODS FOR PERSISTANCE
     */
    public void setI(YIdentifier i) {
        this._i = i;
    }

    @OneToOne(cascade=CascadeType.ALL) 
    public YIdentifier getContainingIdentifier() {
    	return this._i;
    }

    public void setContainingIdentifier(YIdentifier i) {
    	this._i = i;
    }

    /**
     * Null constructor necessary for hibernate
     *
     */
    public YTask() {
    	super();
    }

    /**
     * Constructor
     * @param id
     * @param splitType
     * @param joinType
     * @param container
     */
    public YTask(String id, int joinType, int splitType, YNet container) {
        super(id, container);
        _splitType = splitType;
        _joinType = joinType;
    }

    @Transient
    public E2WFOJNet getResetNet() {
        return _resetNet;
    }

    public void setResetNet(E2WFOJNet net) {
        _resetNet = net;
    }

    private String buildSchema(Collection params) throws YSchemaBuildingException {

        XMLToolsForYAWL xmlt = _net.getParent().getToolsForYAWL();

        Instruction[] instructionsArr = xmlt.buildInstructions(params);

        return xmlt.createYAWLSchema(instructionsArr, getDecompositionPrototype().getRootDataElementName());
    }

    /**
     *
     * @return
     * @hibernate.property column="SPLIT_TYPE"
     */
    @Column(name="split_type")
    public int getSplitType() {
        return _splitType;
    }

    /**
     *
     * @return
     * @hibenate.property column="JOIN_TYPE"
     */
    @Column(name="join_type")
    public int getJoinType() {
        return _joinType;
    }


    public void setSplitType(int splitType) {
        _splitType = splitType;
    }


    public void setJoinType(int joinType) {
        _joinType = joinType;
    }

    @Transient
    public boolean isMultiInstance() {
        return _multiInstAttr != null && _multiInstAttr.isMultiInstance();
    }

    @Transient
    public String getPredicate(YExternalNetElement netElement) {
        YFlow flow = getPostsetFlow(netElement);
        return flow.getXpathPredicate();
    }

    /**
     * @hibernate.one-to-one name="multiInstanceAttributes"
     *    class="au.edu.qut.yawl.elements.YMultiInstanceAttributes"
     */
    @OneToOne(cascade={CascadeType.ALL}, fetch = FetchType.EAGER)
    @JoinColumn(name="multiinstanceattributes_fk")
    public YMultiInstanceAttributes getMultiInstanceAttributes() {
        return _multiInstAttr;
    }
    /**
     * Inserted for hibernate
     *
     * @param attr
     */
    public void setMultiInstanceAttributes(YMultiInstanceAttributes attr) {
    	_multiInstAttr = attr;
    }


    public void setUpMultipleInstanceAttributes(String minInstanceQuery, String maxInstanceQuery,
                                                String thresholdQuery, String creationMode) {
        _multiInstAttr = new YMultiInstanceAttributes
                (this, minInstanceQuery, maxInstanceQuery, thresholdQuery, creationMode);
    }


    public void setMultiInstanceInputDataMappings(String remoteVariableName, String inputProcessingExpression) {
        _multiInstAttr.setMIFormalInputParam(remoteVariableName);
        _multiInstAttr.setUniqueInputMISplittingQuery(inputProcessingExpression);
    }


    public void setMultiInstanceOutputDataMappings(String remoteOutputQuery,
                                                   String aggregationQuery) {
        _multiInstAttr.setMIFormalOutputQuery(remoteOutputQuery);
        _multiInstAttr.setUniqueOutputMIJoiningQuery(aggregationQuery);
    }

    @Transient
    public Collection getParamNamesForTaskCompletion() {
        return getDataMappingsForTaskCompletion().values();
    }

    protected List<YVerificationMessage> checkXQuery(String xQuery, String param) {
        List<YVerificationMessage> messages = new ArrayList<YVerificationMessage>();

        if (null == xQuery || xQuery.length() == 0) {
            messages.add(new YVerificationMessage(this, this +
                    "(id= " + this.getID() + ") the XQuery for param [" +
                    param + "] cannot be equal to null" +
                    " or the empty string.",
                    YVerificationMessage.ERROR_STATUS));
        } else {
            Configuration configuration = new Configuration();
            StaticQueryContext staticQueryContext = new StaticQueryContext();
            QueryProcessor qp = new QueryProcessor(configuration, staticQueryContext);
            try {
                qp.compileQuery(xQuery);
            } catch (XPathException e) {
                messages.add(new YVerificationMessage(this, this +
                        "(id= " + this.getID() + ") the XQuery could not be successfully" +
                        " parsed. [" + e.getMessage() + "]",
                        YVerificationMessage.ERROR_STATUS));
            }
        }
        return messages;
    }

    @Transient
    protected Set getParamNamesForTaskEnablement() {
        return new HashSet(getDataMappingsForEnablement().keySet());
    }


    @Transient
    protected Set getParamNamesForTaskStarting() {
        return new HashSet(getDataMappingsForTaskStarting().keySet());
    }


    @Transient
    private Set getQueriesForTaskCompletion() {
        return getDataMappingsForTaskCompletion().keySet();
    }

    @ManyToMany
    @JoinTable(name="yexternalnetelement_removeset")
    public Set<YExternalNetElement> getRemoveSet() {
    	return _removeSet;
    }

    private void setRemoveSet(Set<YExternalNetElement> s) {
    	_removeSet = s;
    }

    @Transient
    public void setRemovesTokensFrom(List<YExternalNetElement> removeSet) {
        _removeSet.addAll(removeSet);
        
        //Add to populate _cancelledBySet.
        Iterator removeSetIter = removeSet.iterator();
        while (removeSetIter.hasNext()) {
 	      YExternalNetElement element = (YExternalNetElement) removeSetIter.next();
 	      element.addToCancelledBySet(this);
 	    }     
    }

    public void removeFromRemoveSet(YExternalNetElement e){
        
        if (e != null && e instanceof YExternalNetElement)
      	{ 

          _removeSet.remove(e);	
      	  e.removeFromCancelledBySet(this);
      	}
      	
      }
    
    public synchronized List t_fire() throws YStateException, YDataStateException, YQueryException, YPersistenceException {
        YIdentifier id = getI();
        if (!t_enabled(id)) {
            throw new YStateException(
                    this + " cannot fire due to not being enabled");
        }
        _i = id;
        _i.addLocation(this);
        List conditions = new Vector(getPresetElements());
        Iterator conditionsIt = getPresetElements().iterator();
        long numToSpawn = determineHowManyInstancesToCreate();
        List childIdentifiers = new Vector();
        for (int i = 0; i < numToSpawn; i++) {
            YIdentifier childID = createFiredIdentifier();
            try {
                prepareDataForInstanceStarting(childID);
            } catch (YAWLException e) {

                //if there was a problem firing the task then roll back the case.
                cancel();
                if (e instanceof YStateException) {
                    throw (YStateException) e;
                }
                if (e instanceof YDataStateException) {
                    throw (YDataStateException) e;
                }
                if (e instanceof YQueryException) {
                    throw (YQueryException) e;
                }

            }
            childIdentifiers.add(childID);
        }
        prepareDataDocsForTaskOutput();
        switch (_joinType) {
            case YTask._AND:
                while (conditionsIt.hasNext()) {
                    ((YConditionInterface) conditionsIt.next()).removeOne();
                }
                break;
            case YTask._OR:
                while (conditionsIt.hasNext()) {
                    YConditionInterface condition = (YConditionInterface) conditionsIt.next();
                    if (condition.containsIdentifier()) {
                        condition.removeOne();
                    }
                }
                break;
            case YTask._XOR:
                boolean done = false;
                do {
                    int i = Math.abs(_random.nextInt()) % conditions.size();
                    YConditionInterface condition = (YConditionInterface) conditions.get(i);
                    if (condition.containsIdentifier()) {
                        condition.removeOne();
                        done = true;
                    }
                } while (!done);
                break;
        }
        return childIdentifiers;
    }

    /*changed to public for persistance*/
    public void prepareDataDocsForTaskOutput() {
        if (null == getDecompositionPrototype() ||
        		getDecompositionPrototype().getParent() == null) {
            return;
        }
        _groupedMultiInstanceOutputData = new Document();

        _groupedMultiInstanceOutputData.setRootElement(
                new Element(getDecompositionPrototype().getRootDataElementName()));

        _localVariableNameToReplaceableOuptutData = new HashMap();
    }


    public synchronized YIdentifier t_add(
                                          YIdentifier siblingWithPermission, Element newInstanceData)
            throws YDataStateException, YStateException, YQueryException, YSchemaBuildingException, YPersistenceException {
        if (!YMultiInstanceAttributes._creationModeDynamic.equals(_multiInstAttr.getCreationMode())) {
            throw new RuntimeException(this + " does not allow dynamic instance creation.");
        }
        if (t_addEnabled(siblingWithPermission)) {
            List newData = new Vector();
            newData.add(newInstanceData);
            _multiInstanceSpecificParamsIterator = newData.iterator();
            YIdentifier newInstance = createFiredIdentifier();
            prepareDataForInstanceStarting(newInstance);
            return newInstance;
        }
        return null;
    }


    public boolean t_addEnabled(YIdentifier identifier) throws YQueryException {
        if (!isMultiInstance()) {
            return false;
        } else {
            return t_isBusy()
                    && YMultiInstanceAttributes._creationModeDynamic.equals(_multiInstAttr.getCreationMode())
                    && _mi_executing.contains(identifier)
                    && _mi_active.getIdentifiers().size() < _multiInstAttr.getMaxInstances();
        }
    }


    private long determineHowManyInstancesToCreate() throws YDataStateException, YStateException, YQueryException {
        if (!isMultiInstance()) {
            return 1;
        }
        int max = _multiInstAttr.getMaxInstances();
        int min = _multiInstAttr.getMinInstances();
        String queryString = getPreSplittingMIQuery();
        Element dataToSplit = evaluateTreeQuery(queryString, _net.getInternalDataDocument());
        if (dataToSplit == null) {
            throw new YDataQueryException(
                    queryString,
                    dataToSplit,
                    this.getID(),
                    "No data avaliable for MI splitting at task Start");
        }

        generateBeginReport1();

        List multiInstanceList = evaluateListQuery(_multiInstAttr.getMISplittingQuery(), dataToSplit);
        int listSize = multiInstanceList.size();
        if (listSize > max || listSize < min) {
            throw new YDataQueryException(
                    _multiInstAttr.getMISplittingQuery(),
                    dataToSplit,
                    this.getID(),
                    "The number of instances produced by MI split (" + listSize + ") is too " +
                    (listSize > max ? "large" : "small") +
                    ".");
        }
        _multiInstanceSpecificParamsIterator = multiInstanceList.iterator();
        return listSize;
    }

    private void generateBeginReport1() {
        LOG.info("\n\nYTask::firing");
        LOG.info("\ttaskID = " + getID());
    }


    @Transient
    public String getPreSplittingMIQuery() {
        String miVarNameInDecomposition = _multiInstAttr.getMIFormalInputParam();
        for (Iterator iterator = getDataMappingsForTaskStarting().keySet().iterator();
             iterator.hasNext();) {
            String name = (String) iterator.next();
            if (miVarNameInDecomposition.equals(name)) {
                return getDataMappingsForTaskStarting().get(name);
            }
        }
        return null;
    }


    public synchronized boolean t_isExitEnabled() {
        return
                t_isBusy() &&
                ((
                _mi_active.getIdentifiers().containsAll(_mi_complete.getIdentifiers())
                && _mi_complete.getIdentifiers().containsAll(_mi_active.getIdentifiers())
                ) || (
                _mi_complete.getIdentifiers().size() >= _multiInstAttr.getThreshold()
                ));
    }


    public synchronized boolean t_complete(YIdentifier childID, Document decompositionOutputData)
            throws YDataStateException, YStateException, YQueryException, YSchemaBuildingException, YPersistenceException {
        if (t_isBusy()) {

            //if the specification is beta 4 or greater then do validation.
            if (!_net._specification.usesSimpleRootData()) {
                if (null != getDecompositionPrototype()) {
                    if (null == _schemaForTaskCompletion) {
                        Collection outputPCol = getDecompositionPrototype().getOutputParameters();
                        _schemaForTaskCompletion = buildSchema(outputPCol);
                    }
                    validateDataAgainstTypes(
                            _schemaForTaskCompletion,
                            decompositionOutputData.getRootElement(), getID());
                }
            }

            Set queries = getQueriesForTaskCompletion();
            for (Iterator iter = queries.iterator(); iter.hasNext();) {
                String query = (String) iter.next();
                String localVarThatQueryResultGetsAppliedTo = getMIOutputAssignmentVar(query);
                Element queryResultElement = evaluateTreeQuery(query, decompositionOutputData);
                //debugging method call
                generateCompletingReport1(query, decompositionOutputData, queryResultElement);

                if (query.equals(getPreJoiningMIQuery())) {
                    _groupedMultiInstanceOutputData.getRootElement().addContent(
                            (Element) queryResultElement.clone());
                } else {
                    _localVariableNameToReplaceableOuptutData.put(
                            localVarThatQueryResultGetsAppliedTo, queryResultElement);
                }
                //Now we check that the resulting transformation produced data according
                //to the net variable's type.
                if (_net.getParent().isSchemaValidating() &&
                        !query.equals(getPreJoiningMIQuery())) {
                    YVariable var = _net.getLocalVariable(localVarThatQueryResultGetsAppliedTo);
                    Set<YVariable> col = new HashSet<YVariable>();
                    col.add(var);
                    String schemaForVariable = buildSchema(col);
                    try {
                        Element tempRoot = new Element(getDecompositionPrototype().getId());
                        tempRoot.addContent((Element) queryResultElement.clone());
                        validateDataAgainstTypes(
                                schemaForVariable,
                                tempRoot, getID());
                    } catch (YDataValidationException e) {
                        YDataStateException f = new YDataStateException(
                                query,
                                decompositionOutputData.getRootElement(),
                                schemaForVariable,
                                queryResultElement,
                                e.getErrors(),
                                getID(),
                                "BAD PROCESS DEFINITION. Data extraction failed schema validation at task completion.");
                        f.setStackTrace(e.getStackTrace());
                        throw f;
                    }
                    generateCompletingReport2(queryResultElement, localVarThatQueryResultGetsAppliedTo,
                            query, decompositionOutputData);
                }
            }
            //another debugging method call


            _mi_executing.removeOne(childID);
            _mi_complete.add(childID);
            if (t_isExitEnabled()) {
                t_exit();

//                } catch (Exception e) {
                //todo add rollback code to restore previous state
//                }
                return true;
            }
            return false;
        } else {
            LOG.error("This task [" +
                    getName() != null ? getName() : getID() +
                    "] is not active, and therefore cannot be completed. "
            );

            throw new RuntimeException(
                    "This task [" +
                    getName() != null ? getName() : getID() +
                    "] is not active, and therefore cannot be completed.");
        }
    }


    private static void generateCompletingReport2(Element resultElem, String forNetVar, String query, Document data) {
        if(LOG.isInfoEnabled()) {
            XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
            LOG.info("\n\nYTask::t_completing " +
                    "\n\tstatus: transforming output for net" +
                    "\n\tforNetVar = " + forNetVar +
                    "\n\tquery = " + query +
                    "\n\tover data = " + out.outputString(data).trim() +
                    "\n\tresulting data = " + out.outputString(resultElem).trim());
        }
    }

    private void generateCompletingReport1(String query, Document rawDecompositionData, Element queryResultElement) {
        if(LOG.isInfoEnabled()) {
            LOG.info("\n\n\nYTask::completing" +
                    "\n\tTaskID = " + getID() +
                    "\n\tquery " + query);
            if (query.equals(getPreJoiningMIQuery())) {
                LOG.info("\tquery = [" + query + "] is pre-joining MI query.");
            }

            XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
            String rawDataStr = out.outputString(rawDecompositionData).trim();
            LOG.info("\n\trawDecomositionData = " + rawDataStr);
            String queryResultStr = out.outputString(queryResultElement).trim();
            LOG.info("\n\tresult = " + queryResultStr.trim());
        }
    }

    @Transient
    public String getMIOutputAssignmentVar(String query) {
        return getDataMappingsForTaskCompletion().get(query);
    }


    @Transient
    private String getPreJoiningMIQuery() {
        return _multiInstAttr != null ? _multiInstAttr.getMIFormalOutputQuery() : null;
    }


    public synchronized void t_start(YIdentifier child) throws YSchemaBuildingException, YDataStateException, YPersistenceException {
        if (t_isBusy()) {
            startOne(child);
        }
    }


    @Transient
    private YIdentifier getI() {
        Iterator iter;
        iter = getPresetElements().iterator();
        while (iter.hasNext()) {
            YConditionInterface condition = (YConditionInterface) iter.next();
            if (condition.containsIdentifier()) {
                return (YIdentifier) condition.getIdentifiers().get(0);
            }
        }
        return null;
    }


    private synchronized void t_exit() throws YDataStateException, YStateException, YQueryException, YSchemaBuildingException, YPersistenceException {
        if (!t_isExitEnabled()) {
            throw new RuntimeException(this + "_exit() is not enabled.");
        }
        performDataAssignmentsAccordingToOutputExpressions();
        YIdentifier i = _i;
        if (this instanceof YCompositeTask) {
            cancel();
        }
        //remove tokens from cancellation set
        for (Iterator removeIter = _removeSet.iterator(); removeIter.hasNext();) {
            YExternalNetElement netElement = (YExternalNetElement) removeIter.next();
            if (netElement instanceof YTask) {
                ((YTask) netElement).cancel();
            } else if (netElement instanceof YCondition) {
                ((YCondition) netElement).removeAll();
            }
        }
        _mi_active.removeAll();
        _mi_complete.removeAll();
        _mi_entered.removeAll();
        _mi_executing.removeAll();

        synchronized (_random) {
            switch (_splitType) {
                case YTask._AND:
                    doAndSplit(i);
                    break;
                case YTask._OR:
                    doOrSplit(i);
                    break;
                case YTask._XOR:
                    doXORSplit(i);
                    break;
            }
        }

        i.removeLocation(this);
        LOG.info("YTask::" + getID() + ".exit() caseID(" + _i + ") " +
                "_parentDecomposition.getInternalDataDocument() = "
                + new XMLOutputter(Format.getPrettyFormat()).outputString(_net.getInternalDataDocument()).trim());
        _i = null;
    }


    private void performDataAssignmentsAccordingToOutputExpressions()
            throws YDataStateException, YStateException, YQueryException, YSchemaBuildingException, YPersistenceException {
        if (null == getDecompositionPrototype()) {
            return;
        }
        generateExitReport1();
        for (Iterator iter = _localVariableNameToReplaceableOuptutData.keySet().iterator();
             iter.hasNext();) {
            String localVariableName = (String) iter.next();
            Element queryResult =
                    (Element) _localVariableNameToReplaceableOuptutData.get(localVariableName);
            //todo check that queryResult is valid instance of variable type
            _net.addData(queryResult);
        }
        if (this.isMultiInstance() && _multiInstAttr.getMIJoiningQuery() != null) {
            Element result;
            String schemaForVariable;

            result = evaluateTreeQuery(
                    _multiInstAttr.getMIJoiningQuery(),
                    _groupedMultiInstanceOutputData);
            if (_net.getParent().isSchemaValidating()) {
                //if betaversion > beta3 then validate the results of the aggregation query
                String uniqueInstanceOutputQuery = _multiInstAttr.getMIFormalOutputQuery();
                String localVarThatQueryResultGetsAppliedTo =
                        getDataMappingsForTaskCompletion().get(uniqueInstanceOutputQuery);

                YVariable var = _net.getLocalVariable(localVarThatQueryResultGetsAppliedTo);
                Set col = new HashSet();
                col.add(var);
                schemaForVariable = buildSchema(col);

                Element tempRoot = new Element(getDecompositionPrototype().getId());
                tempRoot.addContent((Element) result.clone());
                try {
                    validateDataAgainstTypes(
                            schemaForVariable,
                            tempRoot, getID());
                } catch (YDataValidationException e) {

                    YDataStateException f = new YDataStateException(
                            _multiInstAttr.getMIJoiningQuery(),
                            _groupedMultiInstanceOutputData.getRootElement(),
                            schemaForVariable, result,
                            e.getErrors(), getID(),
                            "BAD PROCESS DEFINITION. " +
                            "Data extraction failed schema validation at task completion.");
                    f.setStackTrace(e.getStackTrace());
                    throw f;
                }
            }
            generateExitReports2(
                    _multiInstAttr.getMIJoiningQuery(),
                    _groupedMultiInstanceOutputData,
                    result);
            _net.addData(result);
            generateExitReports3();
        }
    }

    private void generateExitReports3() {
        if(LOG.isInfoEnabled()) {
            XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
            LOG.info("\tresulting net data = " +
                    out.outputString(_net.getInternalDataDocument()));
        }
    }

    private void generateExitReports2(String miJoiningQuery, Document groupedOutputData, Element result) {
        if(LOG.isInfoEnabled()) {
            LOG.info("\tmi JoiningQuery = " + miJoiningQuery);
            XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
            LOG.info("\tmi groupedOutputData = " + out.outputString(groupedOutputData));
            LOG.info("\tmi result = " + out.outputString(result));
        }
    }

    private void generateExitReport1() {
        if(LOG.isInfoEnabled()) {
            LOG.info("\n\nYTask::exit()");
            LOG.info("\tgetID = " + getID());
            for (Iterator iter = _localVariableNameToReplaceableOuptutData.keySet().iterator();
                 iter.hasNext();) {
                String localVariableName = (String) iter.next();
                Element queryResult =
                        (Element) _localVariableNameToReplaceableOuptutData.get(localVariableName);
                XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
                LOG.info("\tqueryResult = " + out.outputString(queryResult));
            }
        }
    }


    @Transient
    private Set getLocalVariablesForTaskCompletion() {
        return new HashSet(getDataMappingsForTaskCompletion().values());
    }


    private void doXORSplit(YIdentifier tokenToSend) throws YPersistenceException {
        List flows = new ArrayList(getPostsetFlows());
        //sort the flows according to their evaluation ordering,
        //and with the default flow occurring last.
        Collections.sort(flows);
        YFlow flow;

        for (int i = 0; i < flows.size(); i++) {
            flow = (YFlow) flows.get(i);
            if (flow.isDefaultFlow()) {
                ((YCondition) flow.getNextElement()).add(tokenToSend);
                return;
            }
            try {
                XPath xpath = XPath.newInstance("boolean(" + flow.getXpathPredicate() + ")");
                Boolean result = (Boolean) xpath.selectSingleNode(_net.getInternalDataDocument());
                if (result.booleanValue()) {
                    ((YCondition) flow.getNextElement()).add(tokenToSend);
                    return;
                }
            } catch (JDOMException e) {
                e.printStackTrace();
            }
        }
    }


    private void doOrSplit( YIdentifier tokenToSend) throws YPersistenceException {
        boolean noTokensOutputted = true;
        List flows = new ArrayList(getPostsetFlows());
        Collections.sort(flows);
        YFlow flow;
        for (int i = 0; i < flows.size(); i++) {
            flow = (YFlow) flows.get(i);
            try {
                XPath xpath = XPath.newInstance("boolean(" + flow.getXpathPredicate() + ")");
                Boolean result = (Boolean) xpath.selectSingleNode(_net.getInternalDataDocument());
                if (result.booleanValue()) {
                    ((YCondition) flow.getNextElement()).add(tokenToSend);
                    noTokensOutputted = false;
                }
            } catch (JDOMException e) {
                e.printStackTrace();
            }
            if (flow.isDefaultFlow() && noTokensOutputted) {
                ((YCondition) flow.getNextElement()).add(tokenToSend);
            }
        }
    }


    private void doAndSplit(YIdentifier tokenToSend) throws YPersistenceException {
        List<YExternalNetElement> postset = getPostsetElements();

        for (Iterator iterator = postset.iterator(); iterator.hasNext();) {
            YCondition condition = (YCondition) iterator.next();
            if (tokenToSend == null) {
                throw new RuntimeException("token is equal to null");
            }
            condition.add(tokenToSend);
        }
    }


    public synchronized boolean t_enabled(YIdentifier id) {
        //busy tasks are never enabled
        if (_i != null) {
            return false;
        }
        List<YExternalNetElement> preset = getPresetElements();

        
        YCondition[] conditions =
                preset.toArray(new YCondition[preset.size()]);
        switch (_joinType) {
            case YTask._AND:
                for (int i = 0; i < conditions.length; i++) {
                    if (!conditions[i].containsIdentifier()) {
                        return false;
                    }
                }
                return true;
            case YTask._OR:
                {
                    return _net.orJoinEnabled(this, id);
                }
            case YTask._XOR:
                for (int i = 0; i < conditions.length; i++) {
                    if (conditions[i].containsIdentifier()) {
                        return true;
                    }
                }
                return false;
            default:
                return false;
        }
    }


    public Object clone() throws CloneNotSupportedException {
        YTask copy = (YTask) super.clone();
        copy._mi_active = new YInternalCondition(new StateEvent(StateEvent.ACTIVE), copy);
        copy._mi_complete = new YInternalCondition(new StateEvent(StateEvent.COMPLETE), copy);
        copy._mi_entered = new YInternalCondition(new StateEvent(StateEvent.ENTERED), copy);
        copy._mi_executing = new YInternalCondition(new StateEvent(StateEvent.EXECUTING), copy);
        copy._removeSet = new HashSet<YExternalNetElement>();
        Iterator iter = _removeSet.iterator();
        while (iter.hasNext()) {
            YExternalNetElement elem = (YExternalNetElement) iter.next();
            YExternalNetElement elemsClone = copy._net.getNetElement(elem.getID());
            if (elemsClone == null) {
                elemsClone = (YExternalNetElement) elem.clone();
            }
            copy._removeSet.add(elemsClone);
        }

        if (this.isMultiInstance()) {
            copy._multiInstAttr = (YMultiInstanceAttributes) _multiInstAttr.clone();
            copy._multiInstAttr._myTask = copy;
        }
        
        copy.dataMappingsForTaskStartingSet = new HashMap<String, KeyValue>();
        for( Map.Entry<String, KeyValue> entry : dataMappingsForTaskStartingSet.entrySet() ) {
            copy.dataMappingsForTaskStartingSet.put(
                    entry.getKey(),
                    new KeyValue(
                            entry.getValue().getType(),
                            entry.getValue().getKey(),
                            entry.getValue().getValue(),
                            copy ) );
        }
        
        copy.dataMappingsForTaskEnablementSet = new HashMap<String, KeyValue>();
        for( Map.Entry<String, KeyValue> entry : dataMappingsForTaskEnablementSet.entrySet() ) {
            copy.dataMappingsForTaskEnablementSet.put(
                    entry.getKey(),
                    new KeyValue(
                            entry.getValue().getType(),
                            entry.getValue().getKey(),
                            entry.getValue().getValue(),
                            copy ) );
        }
        
        copy.dataMappingsForTaskCompletionSet = new HashSet<KeyValue>();
        for( KeyValue kv : dataMappingsForTaskCompletionSet ) {
            copy.dataMappingsForTaskCompletionSet.add(
                    new KeyValue( kv.getType(), kv.getKey(), kv.getValue(), copy ) );
        }
        
        return copy;
    }
    
    protected YExternalNetElement deepClone( YExternalNetElement task ) {
        super.deepClone( task );
        YTask clone = (YTask) task;
        
        if( _i != null || _caseToDataMap.size() > 0 ||
                _multiInstanceSpecificParamsIterator != null ) {
            throw new IllegalStateException( "Deep clone of executing tasks is unsupported!" );
        }
        
        clone._splitType = _splitType;
        clone._joinType = _joinType;
        
        if( getMultiInstanceAttributes() != null ) {
            YMultiInstanceAttributes attr =
                (YMultiInstanceAttributes) getMultiInstanceAttributes().clone();
            attr.setContainerTask( clone );
            clone.setMultiInstanceAttributes( attr );
        }
        
        // remove set stitching done in YNet.deepClone
        
        clone.dataMappingsForTaskStartingSet = new HashMap<String, KeyValue>();
        for( Map.Entry<String, KeyValue> entry : dataMappingsForTaskStartingSet.entrySet() ) {
            clone.dataMappingsForTaskStartingSet.put(
                    entry.getKey(),
                    new KeyValue(
                            entry.getValue().getType(),
                            entry.getValue().getKey(),
                            entry.getValue().getValue(),
                            clone ) );
        }
        
        clone.dataMappingsForTaskEnablementSet = new HashMap<String, KeyValue>();
        for( Map.Entry<String, KeyValue> entry : dataMappingsForTaskEnablementSet.entrySet() ) {
            clone.dataMappingsForTaskEnablementSet.put(
                    entry.getKey(),
                    new KeyValue(
                            entry.getValue().getType(),
                            entry.getValue().getKey(),
                            entry.getValue().getValue(),
                            clone ) );
        }
        
        clone.dataMappingsForTaskCompletionSet = new HashSet<KeyValue>();
        for( KeyValue kv : dataMappingsForTaskCompletionSet ) {
            clone.dataMappingsForTaskCompletionSet.add(
                    new KeyValue( kv.getType(), kv.getKey(), kv.getValue(), clone ) );
        }
        
        clone.setDecompositionPrototype( null );
        
        // setting this to null because I don't see it used anywhere...
        // no use in wasted effort actually cloning it if it isn't used.
        clone.setResetNet( null );
        
        return clone;
    }


    protected abstract void startOne(YIdentifier id) throws YDataStateException, YSchemaBuildingException, YPersistenceException;


    @Transient
    protected YIdentifier createFiredIdentifier() throws YPersistenceException {
        YIdentifier childCaseID = _i.createChild();
        _mi_active.add(childCaseID);
        _mi_entered.add(childCaseID);
        return childCaseID;
    }

    
    private void prepareDataForInstanceStarting(YIdentifier childInstanceID) throws YDataStateException, YStateException, YQueryException, YSchemaBuildingException {
    	if (null == getDecompositionPrototype()) {
            return;
        }
        Element dataForChildCase = produceDataRootElement();

        List inputParams = new ArrayList(getDecompositionPrototype().getInputParameters());
        Collections.sort(inputParams);
        for (int i = 0; i < inputParams.size(); i++) {
            YParameter parameter = (YParameter) inputParams.get(i);
            String inputParamName = parameter.getName() != null ?
                    parameter.getName() : parameter.getElementName();
            String expression = getDataMappingsForTaskStarting().get(inputParamName);
            if (this.isMultiInstance() && inputParamName.equals(
                    _multiInstAttr.getMIFormalInputParam())) {
                Element specificMIData = (Element)
                        _multiInstanceSpecificParamsIterator.next();

                //todo replace with param info in param metadata
                addXMLAttributes(parameter.getAttributes(), specificMIData);

                dataForChildCase.addContent(specificMIData);
            } else {
                Element result = performDataExtraction(expression, parameter);

                //todo replace with param info in param metadata
                addXMLAttributes(parameter.getAttributes(), result);

                dataForChildCase.addContent((Element) result.clone());
            }
        }
        //todo replace with param info in task metadata
        addXMLAttributes(getDecompositionPrototype().getAttributes(), dataForChildCase);

        _caseToDataMap.put(childInstanceID, dataForChildCase);
    }

    private void addXMLAttributes(Map<String, String> attributes, Element specificMIData) {
        if (attributes != null) {
            Set<String> keys = attributes.keySet();
            for(String attrName: keys) {
                String attrValue = attributes.get(attrName);
                specificMIData.setAttribute(attrName, attrValue);
            }
        }
    }

    protected Element performDataExtraction(String expression, YParameter inputParamName)
            throws YSchemaBuildingException, YDataStateException, YQueryException, YStateException {
        Element result = evaluateTreeQuery(expression, _net.getInternalDataDocument());

        /**
         * AJH: Allow option to inhibit schema validation for outbound data.
         *      Ideally need to support this at task level.
         */
        if (_net.getParent().isSchemaValidating()) {
            if (!skipOutboundSchemaChecks()) {
                performSchemaValidationOverExtractionResult(expression, inputParamName, result); }
        }
        return result;
    }

    private void performSchemaValidationOverExtractionResult(String expression, YParameter param, Element result)
            throws YSchemaBuildingException, YDataStateException {
        Set col = new HashSet();
        col.add(param);
        String schemaForParam = buildSchema(col);
        Element tempRoot = new Element(getDecompositionPrototype().getId());
        try {
            tempRoot.addContent((Element) result.clone());
            validateDataAgainstTypes(schemaForParam, tempRoot, getID());
        } catch (YDataValidationException e) {
            YDataStateException f = new YDataStateException(
                    expression,
                    _net.getInternalDataDocument().getRootElement(),
                    schemaForParam,
                    tempRoot,
                    e.getErrors(), getID(),
                    "BAD PROCESS DEFINITION. " +
                    "Data extraction failed schema validation at task starting.");
            f.setStackTrace(e.getStackTrace());
            throw f;
        }
    }

    @Transient
    protected Element produceDataRootElement() {
        return new Element(getDecompositionPrototype().getRootDataElementName());
    }

    private Element evaluateTreeQuery(String query, Document document)
            throws YStateException, YDataStateException, YQueryException {
        Element resultingData;
        Object resultObj = null;
        try {
            //JDOM code for produce string for reading by SAXON
            XMLOutputter outputter = new XMLOutputter();
            //SAXON XQuery code
            Configuration configuration = new Configuration();
            StaticQueryContext staticQueryContext = new StaticQueryContext();
            QueryProcessor qp = new QueryProcessor(configuration, staticQueryContext);
            XQueryExpression xQueryExpression = qp.compileQuery(query);
            //if SAXON worked with JDOM directly we could replace the next with:
            ///DocumentInfo saxonDocInfo = qp.buildDocument(new DocumentWrapper(_parentDecomposition.getInternalDataDocument(), null));
            DocumentInfo saxonDocInfo = qp.buildDocument(new StreamSource(
                    new StringReader(outputter.outputString(document))));
            DynamicQueryContext dynamicQueryContext = new DynamicQueryContext();
            dynamicQueryContext.setContextNode(saxonDocInfo);
            resultObj = xQueryExpression.evaluateSingle(dynamicQueryContext);
            NodeInfo nodeInfo = (NodeInfo) resultObj;
            //my code to parse SAXON resulting XML tree and produce a string
            //because saxons QueryResult class isn't yet able to produce the desired string from anything
            //but the root node
            if (nodeInfo != null) {
                YSaxonOutPutter saxonOutputter = new YSaxonOutPutter(nodeInfo);
                String result = saxonOutputter.getString();
                SAXBuilder builder = new SAXBuilder();
                Document doclet = builder.build(new StringReader(result));
                resultingData = doclet.detachRootElement();
            } else {
                throw new YDataQueryException(
                        query,
                        document.getRootElement(),
                        getID(),
                        "No data produced.");
            }
        } catch (ClassCastException e) {
            RuntimeException f = new RuntimeException("The result of the query yeilded an object of type "
                    + resultObj.getClass()
                    + ", but this engine insists on queries that produce elements only.");
            f.setStackTrace(e.getStackTrace());
            throw f;
        } catch (XPathException e) {
            YQueryException de = new YQueryException(
                    "Something Wrong with Process Specification:\n" +
                    "The engine failed to parse an invalid query.\n" +
                    "Please check task:\n\t" +
                    "id[ " + getID() + " ]\n\t" +
                    "query: \n\t" + query + ".\n" +
                    "Message from parser: [" + e.getMessage() + "]");
            de.setStackTrace(e.getStackTrace());
            throw de;
        } catch (Exception e) {
            if (e instanceof YDataStateException) {
                throw (YDataStateException) e;
            }
            YStateException se = new YStateException(e.getMessage());
            se.setStackTrace(e.getStackTrace());
            throw se;
        }
        return resultingData;
    }


    private List evaluateListQuery(String query, Element element) throws YQueryException, YStateException {
        List resultingData = new Vector();
        try {
            //JDOM code for produce string for reading by SAXON
            XMLOutputter outputter = new XMLOutputter();
            //SAXON XQuery code
            Configuration configuration = new Configuration();
            StaticQueryContext staticQueryContext = new StaticQueryContext();
            QueryProcessor qp = new QueryProcessor(configuration, staticQueryContext);
            XQueryExpression xQueryExpression = qp.compileQuery(query);
            //if SAXON worked with JDOM directly (like it claims to) we could replace the next with:
            ///DocumentInfo saxonDocInfo = qp.buildDocument(new DocumentWrapper(_parentDecomposition.getInternalDataDocument(), null));
            DocumentInfo saxonDocInfo = qp.buildDocument(new StreamSource(
                    new StringReader(outputter.outputString(element))));
            DynamicQueryContext dynamicQueryContext = new DynamicQueryContext();
            dynamicQueryContext.setContextNode(saxonDocInfo);
            List nodeList = xQueryExpression.evaluate(dynamicQueryContext);
            //my code to parse SAXON resulting XML tree and produce a string
            //because saxons QueryResult class isn't yet able to produce the desired string from anything
            //but the root node
            for (int i = 0; i < nodeList.size(); i++) {
                NodeInfo nodeInfo = (NodeInfo) nodeList.get(i);
                YSaxonOutPutter saxonOutputter = new YSaxonOutPutter(nodeInfo);
                String result = saxonOutputter.getString();
                if (result != null) {
                    SAXBuilder builder = new SAXBuilder();
                    Document doclet = builder.build(new StringReader(result));
                    resultingData.add(doclet.detachRootElement());
                }
            }
        } catch (XPathException e) {
            YQueryException de = new YQueryException(e.getMessage());
            de.setStackTrace(e.getStackTrace());
            throw de;
        } catch (TransformerException e) {
            YQueryException de = new YQueryException(e.getMessage());
            de.setStackTrace(e.getStackTrace());
            throw de;
        } catch (Exception e) {
            YStateException se = new YStateException(e.getMessage());
            se.setStackTrace(e.getStackTrace());
            throw se;
        }
        return resultingData;
    }


    @Transient
    public Element getData(YIdentifier childInstanceID) {
       Element e = (Element) _caseToDataMap.get(childInstanceID);
       
       try {
    	   if (e == null) {      

    		   /*
    	        * This should only occur after restoring a fired identifier
    	        * */

    		   if (isMultiInstance()) {
    			   String queryString = getPreSplittingMIQuery();
    			   Element dataToSplit = evaluateTreeQuery(queryString, _net.getInternalDataDocument());
    			   List multiInstanceList = evaluateListQuery(_multiInstAttr.getMISplittingQuery(), dataToSplit);
    			   _multiInstanceSpecificParamsIterator = multiInstanceList.iterator();
    		   }
    		   
    		   prepareDataForInstanceStarting(childInstanceID);
    		   e = (Element) _caseToDataMap.get(childInstanceID);
   		   
    		   
    	   }
       } catch (Exception ex) {
    	   ex.printStackTrace();
           LOG.error("Failure in preparing data in task [" +
                   getName() != null ? getName() : getID() +
                   "]");

    	   e = null;
       }
       return e;
    }


    public synchronized boolean t_isBusy() {
        return _i != null;
    }

    public synchronized void cancel() throws YPersistenceException {
        _mi_active.removeAll();
        _mi_complete.removeAll();
        _mi_entered.removeAll();
        _mi_executing.removeAll();
        if (_i != null) {
            _i.removeLocation(this);
            _i = null;
        }
    }

    @OneToOne(cascade={CascadeType.ALL}, fetch = FetchType.EAGER)
    public YInternalCondition getMIActive() {
        return _mi_active;
    }

    @OneToOne(cascade={CascadeType.ALL}, fetch = FetchType.EAGER)
    public YInternalCondition getMIEntered() {
        return _mi_entered;
    }

    @OneToOne(cascade={CascadeType.ALL}, fetch = FetchType.EAGER)
    public YInternalCondition getMIComplete() {
        return _mi_complete;
    }

    @OneToOne(cascade={CascadeType.ALL}, fetch = FetchType.EAGER)
    public YInternalCondition getMIExecuting() {
        return _mi_executing;
    }
    public void setMIExecuting(YInternalCondition condition) {
        _mi_executing = condition;
    }
    public void setMIActive(YInternalCondition condition) {
        _mi_active = condition;
    }
    public void setMIEntered(YInternalCondition condition) {
        _mi_entered = condition;
    }
    public void setMIComplete(YInternalCondition condition) {
        _mi_complete = condition;
    }

    /**
     * http://forums.hibernate.org/viewtopic.php?t=955600&highlight=strings+map&sid=0811afbccf4990d002ddfeb507ccfe8d
     *
     * Hibernate does not support Map<String, String> yet so we're going to use a set of KeyValue pairs as replacement.
     */
    private Map<String, KeyValue> dataMappingsForTaskStartingSet = new HashMap<String, KeyValue>();

    @OneToMany(mappedBy="task", cascade={CascadeType.ALL}, fetch = FetchType.EAGER)
    @MapKey(name="key")
    @OnDelete(action=OnDeleteAction.CASCADE)
    @Where(clause="Type='"+KeyValue.STARTING + "'")
    public Map<String, KeyValue> getDataMappingsForTaskStartingSet() {
    	return dataMappingsForTaskStartingSet;
    }
    private void setDataMappingsForTaskStartingSet(Map<String, KeyValue> set) {
    	dataMappingsForTaskStartingSet = set;
    }

    @Transient
    public Map<String, String> getDataMappingsForTaskStarting() {
    	Map<String, String> map = new HashMap<String, String>();
    	for(KeyValue keyValue:dataMappingsForTaskStartingSet.values()) {
    		map.put(keyValue.getKey(), keyValue.getValue());
    	}
    	return map;
    }

    /**
     * The input must be map of [key="variableName", value="expression"]
     * @param map
     */
    public void setDataMappingsForTaskStarting(Map<String, String> map) {
    	for(Map.Entry entry:map.entrySet()) {
    		String key = entry.getKey().toString();
    		String value = entry.getValue().toString();
    		dataMappingsForTaskStartingSet.put(key, new KeyValue(KeyValue.STARTING, key, value, this));
    	}
    }

    /**
     * http://forums.hibernate.org/viewtopic.php?t=955600&highlight=strings+map&sid=0811afbccf4990d002ddfeb507ccfe8d
     *
     * Hibernate does not support Map<String, String> yet so we're going to use a set of KeyValue pairs as replacement.
     */
    public Set<KeyValue> dataMappingsForTaskCompletionSet = new HashSet<KeyValue>();
    @OneToMany(mappedBy="task", cascade={CascadeType.ALL}, fetch = FetchType.EAGER)
    @OnDelete(action=OnDeleteAction.CASCADE)
    @Where(clause="Type='" + KeyValue.COMPLETION+ "'")
    public Set<KeyValue> getDataMappingsForTaskCompletionSet() {
    	return dataMappingsForTaskCompletionSet;
    }
    public void setDataMappingsForTaskCompletionSet(Set<KeyValue> set) {
    	dataMappingsForTaskCompletionSet = set;
    }

    /**
     *
     * @return
     * @hibernate.property
     */
    @Transient
    public Map<String, String> getDataMappingsForTaskCompletion() {
    	Map<String, String> map = new HashMap<String, String>();
    	for(KeyValue keyValue:dataMappingsForTaskCompletionSet) {
    		map.put(keyValue.getKey(), keyValue.getValue());
    	}
    	return map;
    }


    /**
     * The input must be map of [key="expression", value="variableName"]
     * @param map
     */
    public void setDataMappingsForTaskCompletion(Map<String, String> map) {
    	for(Map.Entry entry:map.entrySet()) {
    		String key = entry.getKey().toString();
    		String value = entry.getValue().toString();
    		dataMappingsForTaskCompletionSet.add(new KeyValue(KeyValue.COMPLETION, key, value, this));
    	}
    }


    /**
     * This belongs in the YTask class since YTask is reponsible for persisting
     * this data according to the toXML() method.
     *
     * @return
     * @hibernate.property
     */
    @Transient
    public Map<String, KeyValue> getDataMappingsForEnablement() {
    	return dataMappingsForTaskEnablementSet;
    }

    public String toXML() {
        StringBuffer xml = new StringBuffer();
        xml.append("<task id=\"").append(this.getID()).append("\"");
        if (this.isMultiInstance()) {
            xml.append(" xsi:type=\"MultipleInstanceExternalTaskFactsType\"");
            xml.append(" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
        }
        xml.append(">");
        xml.append(super.toXML());
        String joinType = null;
        switch (_joinType) {
            case _AND:
                joinType = "and";
                break;
            case _OR:
                joinType = "or";
                break;
            case _XOR:
                joinType = "xor";
                break;
        }
        xml.append("<join code=\"").append(joinType).append("\"/>");
        String splitType = null;
        switch (_splitType) {
            case _AND:
                splitType = "and";
                break;
            case _OR:
                splitType = "or";
                break;
            case _XOR:
                splitType = "xor";
                break;
        }
        xml.append("<split code=\"").append(splitType).append("\"/>");
        StringBuffer removeTokensFromFlow = new StringBuffer();
        List removeList = new ArrayList(_removeSet);
        Collections.sort(removeList);
        for (Iterator iterator = removeList.iterator(); iterator.hasNext();) {
            YExternalNetElement netElement = (YExternalNetElement) iterator.next();
            boolean implicitElement = false;
            if (netElement instanceof YCondition) {
                YCondition maybeImplicit = (YCondition) netElement;
                if (maybeImplicit.isImplicit()) {
                    removeTokensFromFlow.append("<removesTokensFromFlow>");
                    YExternalNetElement pre = (YTask) maybeImplicit.
                            getPresetElements().iterator().next();
                    removeTokensFromFlow.append("<flowSource id=\"").
                            append(pre.getID()).append("\"/>");
                    YExternalNetElement post = (YTask) maybeImplicit.
                            getPostsetElements().iterator().next();
                    removeTokensFromFlow.append("<flowDestination id=\"").
                            append(post.getID()).append("\"/>");
                    removeTokensFromFlow.append("</removesTokensFromFlow>");
                    implicitElement = true;
                }
            }
            if (!implicitElement) {
                xml.append("<removesTokens id=\"").
                        append(netElement.getID()).
                        append("\"/>");
            }
        }
        xml.append(removeTokensFromFlow);
        if (getDataMappingsForTaskStartingSet().size() > 0) {
            if (!(this.isMultiInstance() && getDataMappingsForTaskStartingSet().size() == 1)) {
                xml.append("<startingMappings>");
                for (KeyValue entry:dataMappingsForTaskStartingSet.values()) {
                    String mapsTo = entry.getKey();
                    String expression = entry.getValue();
                    if (!isMultiInstance() || !getPreSplittingMIQuery().equals(expression)) {
                        xml.append("<mapping>");
                        xml.append("<expression query=\"").
                                append(marshal(expression)).
                                append("\"/>");
                        xml.append("<mapsTo>").
                                append(mapsTo).
                                append("</mapsTo>");
                        xml.append("</mapping>");
                    }
                }
                xml.append("</startingMappings>");
            }
        }
        if (dataMappingsForTaskCompletionSet.size() > 0) {
            if (!(this.isMultiInstance() && dataMappingsForTaskCompletionSet.size() == 1)) {
                xml.append("<completedMappings>");
                for (KeyValue entry:dataMappingsForTaskCompletionSet) {
                    String expression = entry.getKey();
                    String mapsTo = entry.getValue();
                    if (!isMultiInstance() || !_multiInstAttr.getMIFormalOutputQuery().equals(expression)) {
                        xml.append("<mapping><expression query=\"").
                                append(marshal(expression)).
                                append("\"/>");
                        xml.append("<mapsTo>").
                                append(mapsTo).
                                append("</mapsTo></mapping>");
                    }
                }
                xml.append("</completedMappings>");
            }
        }
        if (dataMappingsForTaskEnablementSet.size() > 0) {
            xml.append("<enablementMappings>");
            for (KeyValue entry:dataMappingsForTaskEnablementSet.values()) {
                String mapsTo = entry.getKey();
                String expression = entry.getValue();
                xml.append("<mapping><expression query=\"").
                        append(marshal(expression)).
                        append("\"/>");
                xml.append("<mapsTo>").
                        append(mapsTo).
                        append("</mapsTo></mapping>");
            }
            xml.append("</enablementMappings>");
        }
        if (getDecompositionPrototype() != null) {
            xml.append("<decomposesTo id=\"").
                    append(getDecompositionPrototype().getId()).
                    append("\"/>");
        }
        if (isMultiInstance()) {
            xml.append(_multiInstAttr.toXML());
        }
        xml.append("</task>");
        return xml.toString();
    }


    public static String marshal(String expression) {
        if (expression != null) {
            expression = expression.replaceAll("&", "&amp;");
            expression = expression.replaceAll("<", "&lt;");
            expression = expression.replaceAll(">", "&gt;");
            expression = expression.replaceAll("\"", "&quot;");
            expression = expression.replaceAll("'", "&apos;");
        }
        return expression;
    }

    @OneToOne(cascade={CascadeType.PERSIST})
    @OnDelete(action=OnDeleteAction.CASCADE)
    public YDecomposition getDecompositionPrototype() {
    	return _decompositionPrototype;    	
    }

    @OneToOne(cascade={CascadeType.PERSIST})
    @OnDelete(action=OnDeleteAction.CASCADE)
    public void setDecompositionPrototype(YDecomposition decomposition) {
    	_decompositionPrototype = decomposition;
    	prepareDataDocsForTaskOutput();
        /**
         * AJH: Check if this task is to perform outbound schema validation. This is currently configured
         *      via the tasks UI MetaData.
         */
//        String attrVal = decomposition.getAttributes().get(PERFORM_OUTBOUND_SCHEMA_VALIDATION);
//        if("TRUE".equalsIgnoreCase(attrVal)) {
//            setSkipOutboundSchemaChecks(true);
//        }
    }

    /**
     * Returns the query to a decomposition enablement parameter.
     * @param paramName the decomposition enablement variable.
     * @return the data binding query for that parameter.
     */
    @Transient
    public String getDataBindingForEnablementParam(String paramName) {
      return getDataMappingsForEnablement().get(paramName).getValue();
    }
    
    /**
     * Connects the query to a decomposition parameter.
     * @param query a query applied to the net variables in the net containing
     * this task.
     * @param paramName the decomposition parameter to which to apply the result.
     */
    public void setDataBindingForInputParam(String query, String paramName) {
    	dataMappingsForTaskStartingSet.put(paramName, new KeyValue(KeyValue.STARTING, paramName, query, this));
    }

    /**
	 * Returns the query to a decomposition input parameter.
	 * @param paramName the decomposition input parameter.
	 * @return the data binding query for that parameter.
	 */
    @Transient
    public String getDataBindingForInputParam(String paramName) {
      return getDataMappingsForTaskStarting().get(paramName);
    }

    /**
     * Binds an output expression of a decomposition to a net variable.
     * @param query the ouptut expression belonging to the tasks decomposition
     * @param netVarName the net scope variable to which to apply the result.
     */
    public void setDataBindingForOutputExpression(String query, String netVarName) {
    	dataMappingsForTaskCompletionSet.add(new KeyValue(KeyValue.COMPLETION, query, netVarName, this));
    }

    @Transient
    public String getInformation() {
        try {
            YAWLServiceGateway gateway = (YAWLServiceGateway) getDecompositionPrototype();
            StringBuffer result = new StringBuffer();
            result.append("<taskInfo>");

            result.append("<specificationID>");
            result.append(_net.getParent().getID());
            result.append("</specificationID>");

            result.append("<taskID>");
            result.append(getID());
            result.append("</taskID>");

            result.append("<taskName>");
            result.append(_name != null ? _name : getDecompositionPrototype().getId());
            result.append("</taskName>");

            if (_documentation != null) {
                result.append("<taskDocumentation>");
                result.append(_documentation);
                result.append("</taskDocumentation>");
            }
            if (getDecompositionPrototype() != null) {
                result.append("<decompositionID>");
                result.append( getDecompositionPrototype().getId() );
				result.append( "</decompositionID>" );


				for( Iterator atts = _decompositionPrototype.getAttributes().keySet().iterator(); atts.hasNext(); ) {
					result.append( "<attributes>" );

					String key = (String) atts.next();
					String value = _decompositionPrototype.getAttributes().get( key );
					result.append( "<" + key + ">" + value + "</" + key + ">" );

					result.append( "</attributes>" );
				}

				YAWLServiceGateway wsgw = (YAWLServiceGateway) getDecompositionPrototype();
				YAWLServiceReference ys = wsgw.getYawlService();
                if (ys != null) {
                    result.append("<yawlService>");
                    String ysID = ys.getURI();
                    result.append("<id>");
                    result.append(ysID);
                    result.append("</id>");
                    result.append("</yawlService>");
                }
            }

            result.append("<params>");
            if (isMultiInstance()) {
                result.append("<formalInputParam>").
                        append(getMultiInstanceAttributes().getMIFormalInputParam()).
                        append("</formalInputParam>");
            }
            Collection inputParams = gateway.getInputParameters();
            for (Iterator iterator = inputParams.iterator(); iterator.hasNext();) {
                YParameter parameter = (YParameter) iterator.next();
                String paramAsXML = parameter.toSummaryXML();
                result.append(paramAsXML);
            }
            Collection outputParams = gateway.getOutputParameters();
            for (Iterator iterator = outputParams.iterator(); iterator.hasNext();) {
                YParameter parameter = (YParameter) iterator.next();
                String paramAsXML = parameter.toSummaryXML();
                result.append(paramAsXML);
            }
            result.append("</params>");
            result.append("</taskInfo>");
            return result.toString();
        } catch (ClassCastException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Gets the version of the specification.
     * @return the specification version.
     */
    @Transient
    public String getSpecVersion() {
        return _net.getParent().getBetaVersion();
    }


    //######################################################################################
    //###########################  BEGIN VERIFICATION CODE  ################################
    //######################################################################################
    public List<YVerificationMessage> verify() {
        List<YVerificationMessage> messages = new Vector<YVerificationMessage>();
        messages.addAll(super.verify());
        if (_splitType != _AND
                && _splitType != _OR
                && _splitType != _XOR) {
            messages.add(new YVerificationMessage(this, this + " Incorrect value for split type",
                    YVerificationMessage.ERROR_STATUS));
        }
        if (_joinType != _AND
                && _joinType != _OR
                && _joinType != _XOR) {
            messages.add(new YVerificationMessage(this, this + " Incorrect value for join type",
                    YVerificationMessage.ERROR_STATUS));
        }
        if (_splitType != _AND &&
                (_splitType == _OR || _splitType == _XOR)) {
            int defaultCount = 0;
            List<YFlow> postsetFlows = new ArrayList<YFlow>(getPostsetFlows());
            Collections.sort(postsetFlows);
            long lastOrdering = Long.MIN_VALUE;
            for (Iterator iterator = postsetFlows.iterator(); iterator.hasNext();) {
                YFlow flow = (YFlow) iterator.next();
                if (flow.getEvalOrdering() != null) {
                    int thisOrdering = flow.getEvalOrdering().intValue();
                    if (thisOrdering == lastOrdering) {
                        messages.add(new YVerificationMessage(this,
                                this + " no two elements may posess the same " +
                                "ordering (" + flow + ") for the same task.",
                                YVerificationMessage.ERROR_STATUS));
                    }
                    lastOrdering = thisOrdering;
                }
                if (flow.isDefaultFlow()) {
                    defaultCount++;
                }
            }
            if (defaultCount != 1) {
                messages.add(new YVerificationMessage(this, this + " the postset of any OR/XOR " +
                        "split must have one default flow. (not " + defaultCount + ")",
                        YVerificationMessage.ERROR_STATUS));
            }
        }
        if (_multiInstAttr != null) {
            messages.addAll(_multiInstAttr.verify());
        }
        Iterator removeSetIt = _removeSet.iterator();
        while (removeSetIt.hasNext()) {
            YExternalNetElement element = (YExternalNetElement) removeSetIt.next();
            if (element == null) {
                messages.add(new YVerificationMessage(this,
                        this + " refers to a non existant element in its remove set.",
                        YVerificationMessage.ERROR_STATUS));
            } else if (!element._net.equals(_net)) {
                messages.add(new YVerificationMessage(this,
                        this + " and " + element + " must be contained in the same net."
                        + " (container " + _net + " & " + element._net + ")",
                        YVerificationMessage.ERROR_STATUS));
            }
        }
        if (getDecompositionPrototype() != null) {
            messages.addAll(checkParameterMappings());
        } else {
            if (dataMappingsForTaskStartingSet.size() > 0) {
                messages.add(new YVerificationMessage(
                        this, "Syntax error for " + this + " to have startingMappings and no decomposition.",
                        YVerificationMessage.ERROR_STATUS));
            }
            if (dataMappingsForTaskCompletionSet.size() > 0) {
                messages.add(new YVerificationMessage(
                        this,
                        "Syntax error for " + this + " to have completionMappings and no decomposition.",
                        YVerificationMessage.ERROR_STATUS));
            }
        }
        return messages;
    }

    @Transient
    private Collection checkParameterMappings() {
        List messages = new ArrayList();
        messages.addAll(checkInputParameterMappings());
        messages.addAll(checkForDuplicateParameterMappings());
        messages.addAll(checkOutputParameterMappings());
        return messages;
    }

    @Transient
    private Collection checkOutputParameterMappings() {
        List messages = new ArrayList();
//        dropping pre-beta4 support
//        if (_net._specification.usesSimpleRootData()) {
//            messages.addAll(checkOutputParamsPreBeta4());
//        }
        //check that each output query has valid syntax
        for(KeyValue entry:dataMappingsForTaskCompletionSet) {
        	String nextQuery = entry.getKey();
        	String netVarNam = entry.getValue();
        	messages.addAll(checkXQuery(nextQuery, netVarNam));
        }
        //check that non existant local variables are not assigned output.
        Set outPutVarsAssignedTo = getLocalVariablesForTaskCompletion();
        List<YParameter> netInputParameters = _net.getInputParameters();
        
        for (Iterator iterator = outPutVarsAssignedTo.iterator(); iterator.hasNext();) {
            String localVarName = (String) iterator.next();
            if (_net.getLocalVariable(localVarName) == null &&
            	findInputParameter(localVarName) == null) {
                messages.add(new YVerificationMessage(this,
                        "The task (id= " + getID() + ") claims to assign its " +
                        "output to a net variable named (" + localVarName + ").  " +
                        "However the containing net does not have " +
                        "such a variable.",
                        YVerificationMessage.ERROR_STATUS));
            }
        }
        return messages;
    }

    //remove this hack when the parms is mapped like the netlocals above!!!!!
    public YParameter findInputParameter(String name) {
    	for (YParameter parm: _net.getInputParameters()) {
    		if (parm.getName().equals(name)) return parm;
    	}
    	return null;
    }    

    @Transient
    private Collection checkForDuplicateParameterMappings() {
        List messages = new ArrayList();
        //catch the case where serveral expressions map to the same decomposition input param
        //The only case where the schema misses this is where the muilti-instance input
        //is the same as one the regular variable mappings
        int numOfUniqueParamsMappedTo = new HashSet(
                getDataMappingsForTaskStarting().values()).size();
        int numParams = dataMappingsForTaskStartingSet.size();
        if (numOfUniqueParamsMappedTo != numParams) {
            messages.add(new YVerificationMessage(this,
                    "A input parameter is used twice.  The task (id=" + getID() + ") " +
                    "uses the same parameter through its multi-instance input " +
                    "and its regular input.",
                    YVerificationMessage.ERROR_STATUS));
        }
        //check that the MI data output extract process does not map to a net variable that is
        //already mapped to by the said task.
        //The only case where the schema misses this is where the muilti-instance output
        //is applied to the same net variable as one of regular outputs.
        int numOfUniqueNetVarsMappedTo = new HashSet(getDataMappingsForTaskCompletion().values()).size();
        numParams = dataMappingsForTaskCompletionSet.size();
        if (numOfUniqueNetVarsMappedTo != numParams) {
            messages.add(new YVerificationMessage(this,
                    "An output parameter is used twice.  The task (id=" + getID() + ") " +
                    "uses the same parameter through its multi-instance output " +
                    "and its regular output.",
                    YVerificationMessage.ERROR_STATUS));
        }
        return messages;
    }
/*
    @Transient
    private Collection checkOutputParamsPreBeta4() {
        List messages = new ArrayList();
        //check there is link to each to each output param(query).
        Set outputQueriesAtDecomposition =
                getDecompositionPrototype().getOutputQueries();
        Set outputQueriesAtTask = getQueriesForTaskCompletion();
        for (Iterator iterator = outputQueriesAtDecomposition.iterator();
             iterator.hasNext();) {
            String query = (String) iterator.next();
            if (!outputQueriesAtTask.contains(query)) {
                messages.add(new YVerificationMessage(this,
                        this + " there exists an output" +
                        " query(" + query + ") in " + getDecompositionPrototype() +
                        " that is" + " not mapped to by this Task.",
                        YVerificationMessage.ERROR_STATUS));
            }
        }
        for (Iterator iterator = outputQueriesAtTask.iterator();
             iterator.hasNext();) {
            String query = (String) iterator.next();
            if (!outputQueriesAtDecomposition.contains(query)) {
                messages.add(new YVerificationMessage(this,
                        this + " there exists an output" +
                        " query(" + query + ") in this Task that has no " +
                        "corresponding mapping at its decomposition(" +
                        getDecompositionPrototype() + ").",
                        YVerificationMessage.ERROR_STATUS));
            }
        }
        return messages;
    }
*/

    @Transient
    private Collection checkInputParameterMappings() {
        List messages = new ArrayList();
        //check that there is a link to each inputParam
        Set inputParamNamesAtDecomposition = getDecompositionPrototype().getInputParameterNames();
        Set inputParamNamesAtTask = getParamNamesForTaskStarting();
        //check that task input var maps to decomp input var
        for (Iterator iterator = inputParamNamesAtDecomposition.iterator(); iterator.hasNext();) {
            String paramName = (String) iterator.next();
            String datamappings = getDataMappingsForTaskStarting().get(paramName);
            if (datamappings == null) {
            	throw new NullPointerException("Task '" + this.getID() + "' unable to find " +
            			"query for data mappings for input parameter '" + paramName + "' using mappings " + getDataMappingsForTaskStarting().toString());
            }
            String query = datamappings;
            messages.addAll(checkXQuery(query, paramName));

            if (!inputParamNamesAtTask.contains(paramName)) {
                messages.add(new YVerificationMessage(this,
                        "The task (id= " + this.getID() + ")" +
                        " needs to be connected with the input parameter (" +
                        paramName + ")" + " of decomposition (" +
                        getDecompositionPrototype() + ").",
                        YVerificationMessage.ERROR_STATUS));
            }
        }
        return messages;
    }
    /**
     * Indicates if schema validation is to be performed when starting the task.
     * @return whether or not to skip validation on task starting.
     */
    @Transient
    private boolean skipOutboundSchemaChecks() {
        return getDecompositionPrototype().skipOutboundSchemaChecks();
    }

    /**
     * Defines if schema validation is to be performed when starting the task.
     * @param performOutboundSchemaChecks
     */
    private void setSkipOutboundSchemaChecks(boolean performOutboundSchemaChecks) {
        getDecompositionPrototype().setSkipOutboundSchemaChecks(performOutboundSchemaChecks);
    }

    @Transient
    private YMultiInstanceAttributes getInitedMultiInstAttr() {
	    if (_multiInstAttr == null) {
	    	_multiInstAttr = new YMultiInstanceAttributes();
			_multiInstAttr.setContainerTask(this);
	    }
	    return _multiInstAttr;
    }

    public String getMinimum() throws YQueryException {
       	return isMultiInstance() ? Integer.toString(getInitedMultiInstAttr().getMinInstances()) : null;
    }
    public void setMinimum(String value) {
        if (value != null) this.getInitedMultiInstAttr().setMinInstancesHibernate(new Integer(value));
    }
    public String getMaximum() throws YQueryException {
    	return isMultiInstance() ? Integer.toString(getInitedMultiInstAttr().getMaxInstances()): null;
    }
    public void setMaximum(String value) {
    	if (value != null) this.getInitedMultiInstAttr().setMaxInstancesHibernate(new Integer(value));
    }
    @Transient
    public String getThreshold() {
        return Integer.toString(getInitedMultiInstAttr().getThreshold());
    }
    @Transient
    public void setThreshold(String value) {
        this.getInitedMultiInstAttr().setThresholdHibernate(new Integer(value));
    }
    
    @Lob
    public String getGroupedMultiInstanceData()  {
    	if (_groupedMultiInstanceOutputData!=null) {
    		return new XMLOutputter().outputString(_groupedMultiInstanceOutputData);
    	} return null;
    }
    
    public void setGroupedMultiInstanceData(String xmlString) throws YPersistenceException {
    	if (xmlString!=null) {
    		SAXBuilder saxBuilder = new SAXBuilder();
    		try {  	    	                       
    			this._groupedMultiInstanceOutputData = saxBuilder.build(new InputSource(new StringReader(xmlString))); 
    		} catch (JDOMException e) {
    	        _groupedMultiInstanceOutputData = new Document();
    	        _groupedMultiInstanceOutputData.setRootElement(
    	                new Element(getDecompositionPrototype().getRootDataElementName()));  			
    		} catch (IOException e) {
    			throw new YPersistenceException("Error when restoring multi instance data - IO");
    		}
    	}
    }
}
