/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.elements;

import net.sf.saxon.s9api.SaxonApiException;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.elements.data.YVariable;
import org.yawlfoundation.yawl.elements.e2wfoj.E2WFOJNet;
import org.yawlfoundation.yawl.elements.state.YIdentifier;
import org.yawlfoundation.yawl.elements.state.YInternalCondition;
import org.yawlfoundation.yawl.engine.YEngine;
import org.yawlfoundation.yawl.engine.YPersistenceManager;
import org.yawlfoundation.yawl.engine.YWorkItemRepository;
import org.yawlfoundation.yawl.engine.time.YTimer;
import org.yawlfoundation.yawl.engine.time.YWorkItemTimer;
import org.yawlfoundation.yawl.exceptions.*;
import org.yawlfoundation.yawl.resourcing.ResourceMap;
import org.yawlfoundation.yawl.schema.YDataValidator;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.SaxonUtil;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.YVerificationMessage;

import javax.xml.datatype.Duration;
import java.net.URL;
import java.util.*;

/**
 * A superclass of any type of task in the YAWL paper.
 *
 * 
 * @author Lachlan Aldred
 * 
 */
public abstract class YTask extends YExternalNetElement {
    private static final Logger logger = Logger.getLogger(YTask.class);
    //class members
    private static final Random _random = new Random(new Date().getTime());
    public static final int _AND = 95;
    public static final int _OR = 103;
    public static final int _XOR = 126;

    //internal state nodes
    protected YIdentifier _i;
    protected YInternalCondition _mi_active = new YInternalCondition(YInternalCondition._mi_active, this);
    protected YInternalCondition _mi_entered = new YInternalCondition(YInternalCondition._mi_entered, this);
    protected YInternalCondition _mi_complete = new YInternalCondition(YInternalCondition._mi_complete, this);
    protected YInternalCondition _mi_executing = new YInternalCondition(YInternalCondition._executing, this);
    protected static YWorkItemRepository _workItemRepository = YWorkItemRepository.getInstance();

    //private attributes
    private int _splitType;
    private int _joinType;
    protected YMultiInstanceAttributes _multiInstAttr;
    private Set _removeSet = new HashSet();
    protected Map<String, String> _dataMappingsForTaskStarting =
            new HashMap<String, String>();//[key=ParamName, value=query]
    private Map<String, String> _dataMappingsForTaskCompletion =
            new HashMap<String, String>();//[key=query, value=NetVarName]
    protected Map<String, String> _dataMappingsForTaskEnablement =
            new HashMap<String, String>();//[key=ParamName, value=query]
    protected YDecomposition _decompositionPrototype;

    //input data storage
    private Map _caseToDataMap = new HashMap();
    private Iterator _multiInstanceSpecificParamsIterator;
    private Map _localVariableNameToReplaceableOuptutData;
    private Document _groupedMultiInstanceOutputData;
    private String _schemaForTaskCompletion;

    //Reset net association
    private E2WFOJNet _resetNet;

    // task resourcing mappings
    private ResourceMap _resourceMap ;
    private Element _resourcingSpec ;

    // optional timer params [name, value]
    private Map<String, Object> _timerParams ;

    // optional URI to a custom form (rather than inbuilt dynamic form)
    private URL _customFormURL;

    /**
     * AJH: Extensions to cater for task level XML attributes.
     *
     * Encoded list of standard XML attributes used on atomic tasks
     */
    private static final String STANDARD_TASK_ATTRIBUTES = "/id/type/skipOutboundSchemaValidation";
    private static final String PERFORM_OUTBOUND_SCHEMA_VALIDATION = "skipOutboundSchemaValidation";
    private boolean _skipOutboundSchemaChecks = false;         // False by default


    /*
      INSERTED METHODS FOR PERSISTANCE
     */
    public void setI(YIdentifier i) {
        this._i = i;
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


    public E2WFOJNet getResetNet() {
        if (_resetNet != null) {
            return _resetNet;
        }
        return null;
    }

    public void setResetNet(E2WFOJNet net) {
        _resetNet = net;
    }

    public int getSplitType() {
        return _splitType;
    }


    public int getJoinType() {
        return _joinType;
    }


    public void setSplitType(int splitType) {
        _splitType = splitType;
    }


    public void setJoinType(int joinType) {
        _joinType = joinType;
    }


    public boolean isMultiInstance() {
        return _multiInstAttr != null && _multiInstAttr.isMultiInstance();
    }


    public String getPredicate(YExternalNetElement netElement) {
        YFlow flow = getPostsetFlow(netElement);
        return flow.getXpathPredicate();
    }



/*    public Map getPredicates() {
        return _predicateMap != null ? new HashMap(_predicateMap) : null;
    }
*/


    public YMultiInstanceAttributes getMultiInstanceAttributes() {
        return _multiInstAttr;
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


    public Collection<String> getParamNamesForTaskCompletion() {
        return _dataMappingsForTaskCompletion.values();
    }


    protected List<YVerificationMessage> checkXQuery(String xQuery, String param) {
        List<YVerificationMessage> messages = new ArrayList<YVerificationMessage>();

        if ((xQuery != null) && (xQuery.length() > 0)) {
            try {
                SaxonUtil.compileXQuery(xQuery);
            }
            catch (SaxonApiException e) {
                messages.add(new YVerificationMessage(this, this +
                    "(id= " + this.getID() + ") the XQuery could not be successfully" +
                    " parsed. [" + e.getMessage() + "]",
                    YVerificationMessage.ERROR_STATUS));
            }
        }
        else messages.add(new YVerificationMessage(this, this +
                    "(id= " + this.getID() + ") the XQuery for param [" +
                    param + "] cannot be equal to null" +
                    " or the empty string.",
                    YVerificationMessage.ERROR_STATUS));

        return messages;
    }


    protected Set<String> getParamNamesForTaskEnablement() {
        return new HashSet<String>(_dataMappingsForTaskEnablement.keySet());
    }

    
    protected Set<String> getParamNamesForTaskStarting() {
        return new HashSet<String>(_dataMappingsForTaskStarting.keySet());
    }


    private Set<String> getQueriesForTaskCompletion() {
        return _dataMappingsForTaskCompletion.keySet();
    }

    public Set getRemoveSet() {
        if (_removeSet != null) {
            return new HashSet(_removeSet);
        }
        return null;
    }


    public void setRemovesTokensFrom(List removeSet) {
        _removeSet.addAll(removeSet);
    }

   //Added for reduction rules
    //need to use id to check for equal!
    public void removeFromRemoveSet(YExternalNetElement e){
    
    if (e != null && e instanceof YExternalNetElement)
  	{ 
  	 /* for (Iterator i = _removeSet.iterator(); i.hasNext();) {
         YExternalNetElement re = (YExternalNetElement) i.next(); 
         if (re.getID().equals(e.getID()))
         { _removeSet.remove(re);
         }	
      }
      */
      _removeSet.remove(e);	
  	  e.removeFromCancelledBySet(this);
  	}
  }

    public synchronized List t_fire(YPersistenceManager pmgr) throws YStateException, YDataStateException, YQueryException, YPersistenceException, YSchemaBuildingException {
        YIdentifier id = getI();

        if (!t_enabled(id)) {
            throw new YStateException(
                    this + " cannot fire due to not being enabled");
        }
        _i = id;
        _i.addLocation(pmgr, this);
        List conditions = new Vector(getPresetElements());
        Iterator conditionsIt = getPresetElements().iterator();
        long numToSpawn = determineHowManyInstancesToCreate();
        List childIdentifiers = new Vector();
        for (int i = 0; i < numToSpawn; i++) {
            YIdentifier childID = createFiredIdentifier(pmgr);
            try {
                prepareDataForInstanceStarting(childID);
            } catch (YSchemaBuildingException e) {

                //if there was a problem firing the task then roll back the case.
                cancel(pmgr);
                throw e;
                }
            childIdentifiers.add(childID);
        }
        prepareDataDocsForTaskOutput();
        switch (_joinType) {
            case YTask._AND:
                while (conditionsIt.hasNext()) {
                    ((YConditionInterface) conditionsIt.next()).removeOne(pmgr);
                }
                break;
            case YTask._OR:
                while (conditionsIt.hasNext()) {
                    YConditionInterface condition = (YConditionInterface) conditionsIt.next();
                    if (condition.containsIdentifier()) {
                        condition.removeOne(pmgr);
                    }
                }
                break;
            case YTask._XOR:
                boolean done = false;
                do {
                    int i = Math.abs(_random.nextInt()) % conditions.size();
                    YConditionInterface condition = (YConditionInterface) conditions.get(i);
                    if (condition.containsIdentifier()) {
                        condition.removeOne(pmgr);
                        done = true;
                    }
                } while (!done);
                break;
        }
        return childIdentifiers;
    }

    /*changed to public for persistance*/
    public void prepareDataDocsForTaskOutput() {
        if (null == getDecompositionPrototype()) {
            return;
        }
        _groupedMultiInstanceOutputData = new Document();

        _groupedMultiInstanceOutputData.setRootElement(
                new Element(getDecompositionPrototype().getRootDataElementName()));

        _localVariableNameToReplaceableOuptutData = new HashMap();
    }


    public synchronized YIdentifier t_add(YPersistenceManager pmgr,
                                          YIdentifier siblingWithPermission, Element newInstanceData)
            throws YDataStateException, YStateException, YQueryException, YSchemaBuildingException, YPersistenceException {
        if (!YMultiInstanceAttributes._creationModeDynamic.equals(_multiInstAttr.getCreationMode())) {
            throw new RuntimeException(this + " does not allow dynamic instance creation.");
        }
        if (t_addEnabled(siblingWithPermission)) {
            List newData = new Vector();
            newData.add(newInstanceData);
            _multiInstanceSpecificParamsIterator = newData.iterator();
            YIdentifier newInstance = createFiredIdentifier(pmgr);
            prepareDataForInstanceStarting(newInstance);
            return newInstance;
        }
        return null;
    }


    public boolean t_addEnabled(YIdentifier identifier) {
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
                    "The number of instances produced by MI split is too " +
                    (listSize > max ? "large" : "small") +
                    ".");
        }
        _multiInstanceSpecificParamsIterator = multiInstanceList.iterator();
        return listSize;
    }

    private void generateBeginReport1() {
        logger.debug("\n\nYTask::firing");
        logger.debug("\ttaskID = " + getID());
    }


    public String getPreSplittingMIQuery() {
        String miVarNameInDecomposition = _multiInstAttr.getMIFormalInputParam();
        for (String name : _dataMappingsForTaskStarting.keySet()) {
            if (miVarNameInDecomposition.equals(name)) {
                return _dataMappingsForTaskStarting.get(name);
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


    public synchronized boolean t_complete(YPersistenceManager pmgr, YIdentifier childID, Document decompositionOutputData)
            throws YDataStateException, YStateException, YQueryException, YSchemaBuildingException, YPersistenceException {
        if (t_isBusy()) {
            YDataValidator validator = _net._specification.getDataValidator();

            //if the specification is beta 4 or greater then do validation.
            if (!_net._specification.usesSimpleRootData()) {
                if (null != getDecompositionPrototype()) {
                    validator.validate(_decompositionPrototype.getOutputParameters().values(), decompositionOutputData.getRootElement(), getID());
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
                if (_net.getSpecification().isSchemaValidating() &&
                        !query.equals(getPreJoiningMIQuery())) {
                    YVariable var = _net.getLocalVariables().containsKey(localVarThatQueryResultGetsAppliedTo) ?
                            (YVariable) _net.getLocalVariables().get(localVarThatQueryResultGetsAppliedTo) :
                            (YVariable) _net.getInputParameters().get(localVarThatQueryResultGetsAppliedTo);
                    try {
                        Element tempRoot = new Element(_decompositionPrototype.getID());
                        tempRoot.addContent((Element) queryResultElement.clone());
                        /**
                         * MF: Skip schema checking if we have an empty XQuery result to allow us to effectively blank-out
                         * a net variable.
                         */
                        if ((queryResultElement.getChildren().size() != 0 || (queryResultElement.getContent().size() != 0)))
                        {
                            validator.validate(var, tempRoot, getID());
                        
                        }
                    } catch (YDataValidationException e) {
                        YDataStateException f = new YDataStateException(
                                query,
                                decompositionOutputData.getRootElement(),
                                validator.getSchema(),
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


            _mi_executing.removeOne(pmgr, childID);
            _mi_complete.add(pmgr, childID);
            if (t_isExitEnabled()) {
//                try{
                t_exit(pmgr);
//                } catch (Exception e) {
                //todo add rollback code to restore previous state
//                }
                return true;
            }
            return false;
        } else {
            throw new RuntimeException(
                    "This task [" +
                    getName() != null ? getName() : getID() +
                    "] is not active, and therefore cannot be completed.");
        }
    }


    private static void generateCompletingReport2(Element resultElem, String forNetVar, String query, Document data) {
        if(logger.isDebugEnabled()) {
            XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
            logger.debug("\n\nYTask::t_completing " +
                    "\n\tstatus: transforming output for net" +
                    "\n\tforNetVar = " + forNetVar +
                    "\n\tquery = " + query +
                    "\n\tover data = " + out.outputString(data).trim() +
                    "\n\tresulting data = " + out.outputString(resultElem).trim());
        }
    }

    private void generateCompletingReport1(String query, Document rawDecompositionData, Element queryResultElement) {
        if(logger.isDebugEnabled()) {
            String debug = "\n\n\nYTask::completing\n\tTaskID = " + getID() + "\n\tquery " + query;
            if (query.equals(getPreJoiningMIQuery())) {
                debug = debug + "\tquery = [" + query + "] is pre-joining MI query.";
            }

            XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
            String rawDataStr = out.outputString(rawDecompositionData).trim();
            debug = debug + "\n\trawDecomositionData = " + rawDataStr;
            String queryResultStr = out.outputString(queryResultElement).trim();
            debug = debug + "\n\tresult = " + queryResultStr.trim();
            logger.debug(debug);
        }
    }


    public String getMIOutputAssignmentVar(String query) {
        return _dataMappingsForTaskCompletion.get(query);
    }


    private String getPreJoiningMIQuery() {
        return _multiInstAttr != null ? _multiInstAttr.getMIFormalOutputQuery() : null;
    }


    public synchronized void t_start(YPersistenceManager pmgr, YIdentifier child) throws YSchemaBuildingException, YDataStateException, YPersistenceException, YQueryException, YStateException {
        if (t_isBusy()) {
            startOne(pmgr, child);
        }
    }


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


    private synchronized void t_exit(YPersistenceManager pmgr) throws YDataStateException, YStateException, YQueryException, YSchemaBuildingException, YPersistenceException {
        if (!t_isExitEnabled()) {
            throw new RuntimeException(this + "_exit() is not enabled.");
        }
        performDataAssignmentsAccordingToOutputExpressions(pmgr);
        YIdentifier i = _i;
        if (this instanceof YCompositeTask) {
            cancel(pmgr);
        }
        //remove tokens from cancellation set
        for (Iterator removeIter = _removeSet.iterator(); removeIter.hasNext();) {
            YExternalNetElement netElement = (YExternalNetElement) removeIter.next();
            if (netElement instanceof YTask) {
                ((YTask) netElement).cancel(pmgr);
            } else if (netElement instanceof YCondition) {
                ((YCondition) netElement).removeAll(pmgr);
            }
        }
        _mi_active.removeAll(pmgr);
        _mi_complete.removeAll(pmgr);
        _mi_entered.removeAll(pmgr);
        _mi_executing.removeAll(pmgr);
            switch (_splitType) {
                case YTask._AND:
                    doAndSplit(pmgr, i);
                    break;
                case YTask._OR:
                    doOrSplit(pmgr, i);
                    break;
                case YTask._XOR:
                    doXORSplit(pmgr, i);
                    break;
            }
        i.removeLocation(pmgr, this);
        logger.debug("YTask::" + getID() + ".exit() caseID(" + _i + ") " +
                "_parentDecomposition.getInternalDataDocument() = "
                + new XMLOutputter(Format.getPrettyFormat()).outputString(_net.getInternalDataDocument()).trim());
        _i = null;
    }


    private void performDataAssignmentsAccordingToOutputExpressions(YPersistenceManager pmgr)
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
            _net.addData(pmgr, queryResult);
        }
        if (this.isMultiInstance() && _multiInstAttr.getMIJoiningQuery() != null) {
            Element result;

            result = evaluateTreeQuery(
                    _multiInstAttr.getMIJoiningQuery(),
                    _groupedMultiInstanceOutputData);
            if (_net.getSpecification().isSchemaValidating()) {
                //if betaversion > beta3 then validate the results of the aggregation query
                String uniqueInstanceOutputQuery = _multiInstAttr.getMIFormalOutputQuery();
                String localVarThatQueryResultGetsAppliedTo =
                        _dataMappingsForTaskCompletion.get(uniqueInstanceOutputQuery);

                YVariable var = _net.getLocalVariables().containsKey(
                        localVarThatQueryResultGetsAppliedTo) ?
                        (YVariable) _net.getLocalVariables().get(localVarThatQueryResultGetsAppliedTo) :
                        (YVariable) _net.getInputParameters().get(localVarThatQueryResultGetsAppliedTo);

                Element tempRoot = new Element(_decompositionPrototype.getID());
                tempRoot.addContent((Element) result.clone());
                try {
                    _net.getSpecification().getDataValidator().validate(var,tempRoot,getID());
                } catch (YDataValidationException e) {

                    YDataStateException f = new YDataStateException(
                            _multiInstAttr.getMIJoiningQuery(),
                            _groupedMultiInstanceOutputData.getRootElement(),
                            _net.getSpecification().getDataValidator().getSchema(), result,
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
            _net.addData(pmgr, result);
            generateExitReports3();
        }
    }

    private void generateExitReports3() {
        if(logger.isInfoEnabled()) {
            XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
            logger.debug("\tresulting net data = " +
                    out.outputString(_net.getInternalDataDocument()));
        }
    }

    private void generateExitReports2(String miJoiningQuery, Document groupedOutputData, Element result) {
        if(logger.isInfoEnabled()) {
            logger.debug("\tmi JoiningQuery = " + miJoiningQuery);
            XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
            logger.debug("\tmi groupedOutputData = " + out.outputString(groupedOutputData));
            logger.debug("\tmi result = " + out.outputString(result));
        }
    }

    private void generateExitReport1() {
        if(logger.isInfoEnabled()) {
            logger.debug("\n\nYTask::exit()");
            logger.debug("\tgetID = " + getID());
            for (Iterator iter = _localVariableNameToReplaceableOuptutData.keySet().iterator();
                 iter.hasNext();) {
                String localVariableName = (String) iter.next();
                Element queryResult =
                        (Element) _localVariableNameToReplaceableOuptutData.get(localVariableName);
                XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
                logger.debug("\tqueryResult = " + out.outputString(queryResult));
            }
        }
    }


    private Set<String> getLocalVariablesForTaskCompletion() {
        return new HashSet<String>(_dataMappingsForTaskCompletion.values());
    }


    private void doXORSplit(YPersistenceManager pmgr, YIdentifier tokenToSend)
            throws YQueryException, YPersistenceException {
        logger.debug("Evaluating XQueries against Net: " +
                     JDOMUtil.documentToString(_net.getInternalDataDocument()));

        // get & sort the flows according to their evaluation ordering,
        // and with the default flow occurring last.
        List<YFlow> flows = new ArrayList<YFlow>(getPostsetFlows());
        Collections.sort(flows);

        for (YFlow flow : flows) {
            if (flow.isDefaultFlow()) {                 // last flow reached - default
                logger.debug("Following default path.");
                ((YCondition) flow.getNextElement()).add(pmgr, tokenToSend);
                return;
            }

            // check if this query evaluates to true
            String xquery = "boolean(" + flow.getXpathPredicate() + ")";
            try
            {
                logger.debug("Evaluating XQuery: " + xquery);
                String result = SaxonUtil.evaluateQuery(xquery, _net.getInternalDataDocument());

                if (result != null) {
                    if (result.equalsIgnoreCase("true")) {
                        logger.debug("XQuery evaluated TRUE.");
                        ((YCondition) flow.getNextElement()).add(pmgr, tokenToSend);
                        return;
                    }
                    else if (result.equalsIgnoreCase("false")) {
                        logger.debug("XQuery evaluated FALSE.");
                    }
                    else {
                        logger.error(
                            "Evaluated XQuery did not return a singular boolean result.");
                        throw new YQueryException(
                            "Evaluated XQuery did not return a singular boolean result." +
                            " Evaluated: '" + xquery + "'");
                    }    
                }
            }
            catch (SaxonApiException e) {
                logger.error("Invalid XQuery expression (" + xquery + ").", e);
                throw new YQueryException("Invalid XQuery expression (" + xquery + ").");
            }
        }
    }


    private void doOrSplit(YPersistenceManager pmgr, YIdentifier tokenToSend)
            throws YQueryException, YPersistenceException {
        boolean noTokensOutputted = true;

        logger.debug("Evaluating XQueries against Net: " +
                     JDOMUtil.documentToString(_net.getInternalDataDocument()));
        List<YFlow> flows = new ArrayList<YFlow>(getPostsetFlows());
        Collections.sort(flows);
        for (YFlow flow : flows) {

            // check if this query evaluates to true
            String xquery = "boolean(" + flow.getXpathPredicate() + ")";
            try
            {
                logger.debug("Evaluating XQuery: " + xquery);
                String result = SaxonUtil.evaluateQuery(xquery, _net.getInternalDataDocument());

                if (result != null) {
                    if (result.equalsIgnoreCase("true")) {
                        logger.debug("XQuery evaluated TRUE.");
                        ((YCondition) flow.getNextElement()).add(pmgr, tokenToSend);
                        noTokensOutputted = false;
                    }
                    else if (result.equalsIgnoreCase("false")) {
                        logger.debug("XQuery evaluated FALSE.");
                    }
                    else {
                        logger.error(
                            "Evaluated XQuery did not return a singular boolean result.");
                        throw new YQueryException(
                            "Evaluated XQuery did not return a singular boolean result." +
                            " Evaluated: '" + xquery + "'");
                    }
                }
            }
            catch (SaxonApiException e)
            {
                logger.error("Invalid XPath expression (" + xquery + ").", e);
                throw new YQueryException("Invalid XPath expression (" + xquery + ").");
            }
            if (flow.isDefaultFlow() && noTokensOutputted) {
                ((YCondition) flow.getNextElement()).add(pmgr, tokenToSend);
            }
        }
    }


    private void doAndSplit(YPersistenceManager pmgr, YIdentifier tokenToSend)
            throws YPersistenceException {
        Set<YExternalNetElement> postset = getPostsetElements();
        for (YExternalNetElement element : postset) {
            YCondition condition = (YCondition) element;
            if (tokenToSend == null) {
                throw new RuntimeException("token is equal to null = " + tokenToSend);
            }
            condition.add(pmgr, tokenToSend);
        }
    }


    public synchronized boolean t_enabled(YIdentifier id) {
        //busy tasks are never enabled
        if (_i != null) {
            return false;
        }
        Set preset = getPresetElements();
        YCondition[] conditions = (YCondition[])
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
        copy._mi_active = new YInternalCondition(YInternalCondition._mi_active, copy);
        copy._mi_complete = new YInternalCondition(YInternalCondition._mi_complete, copy);
        copy._mi_entered = new YInternalCondition(YInternalCondition._mi_entered, copy);
        copy._mi_executing = new YInternalCondition(YInternalCondition._executing, copy);
        copy._removeSet = new HashSet();
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
        return copy;
    }


    protected abstract void startOne(YPersistenceManager pmgr, YIdentifier id)
            throws YDataStateException, YSchemaBuildingException, YPersistenceException,
                   YQueryException, YStateException;


    protected YIdentifier createFiredIdentifier(YPersistenceManager pmgr) throws YPersistenceException {
        YIdentifier childCaseID = _i.createChild(pmgr);
        _mi_active.add(pmgr, childCaseID);
        _mi_entered.add(pmgr, childCaseID);
        return childCaseID;
    }

    public void prepareDataForInstanceStarting(YIdentifier childInstanceID) throws YDataStateException, YStateException, YQueryException, YSchemaBuildingException {

        logger.debug("--> prepareDataForInstanceStarting" + childInstanceID);

        if (null == getDecompositionPrototype()) {
            return;
        }
        Element dataForChildCase = produceDataRootElement();

        List inputParams = new ArrayList(_decompositionPrototype.getInputParameters().values());
        Collections.sort(inputParams);
        for (int i = 0; i < inputParams.size(); i++) {
            YParameter parameter = (YParameter) inputParams.get(i);                                                //todo log XQuerries

            String inputParamName = parameter.getName() != null ?
                    parameter.getName() : parameter.getElementName();
            String expression = _dataMappingsForTaskStarting.get(inputParamName);
            if (this.isMultiInstance() && inputParamName.equals(
                    _multiInstAttr.getMIFormalInputParam())) {
                Element specificMIData = (Element)
                        _multiInstanceSpecificParamsIterator.next();

                if (specificMIData == null) {
                    continue;
                }
                else
                {
                if (YEngine.getInstance().generateUIMetaData())
                {
                    /**
                     * Add in attributes for input parameter
                     */
                    if (parameter.getAttributes() != null)
                    {
                        Enumeration enumeration = parameter.getAttributes().keys();
                        while(enumeration.hasMoreElements())
                        {
                            String attrName = (String)enumeration.nextElement();
                            String attrValue = (String)parameter.getAttributes().get(attrName);
                            specificMIData.setAttribute(attrName, attrValue);
                        }
                    }
                }
                dataForChildCase.addContent(specificMIData);
               }

            } else {
                Element result = performDataExtraction(expression, parameter);

                if (result == null) {
                    continue;
                }
                else
                {
                if (YEngine.getInstance().generateUIMetaData())
                {
                    /**
                     * Add in attributes for input parameter
                     */
                    if (parameter.getAttributes() != null)
                    {
                        Enumeration enumeration = parameter.getAttributes().keys();
                        while(enumeration.hasMoreElements())
                        {
                            String attrName = (String)enumeration.nextElement();
                            String attrValue = (String)parameter.getAttributes().get(attrName);
                            result.setAttribute(attrName, attrValue);
                        }
                    }
                }
                dataForChildCase.addContent((Element) result.clone());
            }
          }
        }

        if (YEngine.getInstance().generateUIMetaData())
        {
            /**
             * AJH: Add in task level attributes for specifcation to XMLdoclet pass-thru.
             * Note that we skip processing of the YAWL standard task attributes as we only
             * pass-thru the additional (user interface hints) attributes.
             */
            Enumeration enumKeys = getDecompositionPrototype().getAttributes().keys();
            while(enumKeys.hasMoreElements())
            {
                String attrName = (String)enumKeys.nextElement();
                String attrValue = (String)getDecompositionPrototype().getAttributes().get(attrName);
                if (STANDARD_TASK_ATTRIBUTES.indexOf("/" + attrName + "/") == -1)
                {
                    dataForChildCase.setAttribute(attrName, attrValue);
                }
            }
        }
        _caseToDataMap.put(childInstanceID, dataForChildCase);
        logger.debug("<-- prepareDataForInstanceStarting");
    }

    protected Element performDataExtraction(String expression, YParameter inputParamName)
            throws YSchemaBuildingException, YDataStateException, YQueryException, YStateException {
        Element result = evaluateTreeQuery(expression, _net.getInternalDataDocument());

//        TODO: REVIEW THIS BIT OF CODE: (i) is xforms assumption correct in all cases?
//        /**
//         * AJH: If we have an empty element and the element is not mandatory and the task is not destined for
//         * an XForm (assumed here if there are no UI meta-data attributes), don't pass the element out to the
//         * task as input data.
//         */
        if (!inputParamName.isMandatory())
//        {
//            if (!_skipOutboundSchemaChecks)
//            {
//                if ((inputParamName.getAttributes() == null) || (inputParamName.getAttributes().size() == 0))
//                {
//                    if ((result.getChildren().size() == 0) && (result.getContent().size() ==0 ))
//                    {
//                        return null;
//                    }
//                }
//            }
//        }

        /**
         * AJH: Allow option to inhibit schema validation for outbound data.
         *      Ideally need to support this at task level.
         */
        if (_net.getSpecification().isSchemaValidating()) {
            if (!skipOutboundSchemaChecks()) {
                performSchemaValidationOverExtractionResult(expression, inputParamName, result); }
        }
        return result;
    }

    private void performSchemaValidationOverExtractionResult(String expression, YParameter param, Element result)
            throws YSchemaBuildingException, YDataStateException {
        Element tempRoot = new Element(_decompositionPrototype.getID());
        try {
            tempRoot.addContent((Element) result.clone());
            _net.getSpecification().getDataValidator().validate(param, tempRoot, getID());
        } catch (YDataValidationException e) {
            YDataStateException f = new YDataStateException(
                    expression,
                    _net.getInternalDataDocument().getRootElement(),
                    _net.getSpecification().getDataValidator().getSchema(),
                    tempRoot,
                    e.getErrors(), getID(),
                    "BAD PROCESS DEFINITION. " +
                    "Data extraction failed schema validation at task starting.");
            f.setStackTrace(e.getStackTrace());
            throw f;
        }
    }

    protected Element produceDataRootElement() {
        return new Element(getDecompositionPrototype().getRootDataElementName());
    }

    
    private Element evaluateTreeQuery(String query, Document document)
            throws YQueryException {

        try {
            logger.debug("Evaluating XQuery: " + query);
            return SaxonUtil.evaluateTreeQuery(query, document);
        }
        catch (SaxonApiException e) {
            YQueryException qe = new YQueryException(
                    "Something Wrong with Process Specification:\n" +
                    "The engine failed to parse an invalid query.\n" +
                    "Please check task:\n\t" +
                    "id[ " + getID() + " ]\n\t" +
                    "query: \n\t" + query + ".\n" +
                    "Message from parser: [" + e.getMessage() + "]");
            qe.setStackTrace(e.getStackTrace());
            throw qe;
        } 
    }


    private List evaluateListQuery(String query, Element element)
            throws YQueryException {

        try {
            logger.debug("Evaluating XQuery: " + query);
            return SaxonUtil.evaluateListQuery(query, element);
        }
        catch (SaxonApiException e) {
            YQueryException de = new YQueryException(e.getMessage());
            de.setStackTrace(e.getStackTrace());
            throw de;
        }
    }


    public Element getData(YIdentifier childInstanceID) {
        return (Element) _caseToDataMap.get(childInstanceID);
    }


    public synchronized boolean t_isBusy() {
        return _i != null;
    }


    public synchronized void cancel(YPersistenceManager pmgr) throws YPersistenceException {
        _mi_active.removeAll(pmgr);
        _mi_complete.removeAll(pmgr);
        _mi_entered.removeAll(pmgr);
        _mi_executing.removeAll(pmgr);
        if (_i != null) {
            _i.removeLocation(pmgr, this);
            _i = null;
        }
    }

    public YInternalCondition getMIActive() {
        return _mi_active;
    }

    public YInternalCondition getMIEntered() {
        return _mi_entered;
    }

    public YInternalCondition getMIComplete() {
        return _mi_complete;
    }

    public YInternalCondition getMIExecuting() {
        return _mi_executing;
    }

    /**
     * The input must be map of [key="variableName", value="expression"]
     * @param map
     */
    public void setDataMappingsForTaskStarting(Map<String, String> map) {
        _dataMappingsForTaskStarting.putAll(map);
    }


    /**
     * The input must be map of [key="expression", value="variableName"]
     * @param map
     */
    public void setDataMappingsForTaskCompletion(Map<String, String> map) {
        _dataMappingsForTaskCompletion.putAll(map);
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
        if (_dataMappingsForTaskStarting.size() > 0) {
            if (!(this.isMultiInstance() && _dataMappingsForTaskStarting.size() == 1)) {
                xml.append("<startingMappings>");
                for (String mapsTo : _dataMappingsForTaskStarting.keySet()) {
                    String expression = _dataMappingsForTaskStarting.get(mapsTo);
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
        if (_dataMappingsForTaskCompletion.size() > 0) {
            if (!(this.isMultiInstance() && _dataMappingsForTaskCompletion.size() == 1)) {
                xml.append("<completedMappings>");
                for (String expression : _dataMappingsForTaskCompletion.keySet()) {
                    String mapsTo = _dataMappingsForTaskCompletion.get(expression);
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
        if (_dataMappingsForTaskEnablement.size() > 0) {
            xml.append("<enablementMappings>");
            for (String mapsTo : _dataMappingsForTaskEnablement.keySet()) {
                String expression = _dataMappingsForTaskEnablement.get(mapsTo);
                xml.append("<mapping><expression query=\"").
                        append(marshal(expression)).
                        append("\"/>");
                xml.append("<mapsTo>").
                        append(mapsTo).
                        append("</mapsTo></mapping>");
            }
            xml.append("</enablementMappings>");
        }
        if (_timerParams != null) {
            xml.append(timerParamsToXML());
        }
        if (_resourceMap != null) {
            xml.append(_resourceMap.toXML()) ;
        }
        if (_customFormURL != null) {
            xml.append(StringUtil.wrap(_customFormURL.toString(), "customForm"));
        }
        if (_decompositionPrototype != null) {
            xml.append("<decomposesTo id=\"").
                    append(_decompositionPrototype.getID()).
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
            expression = expression.replaceAll("\"", "&qout;");
            expression = expression.replaceAll("'", "&apos;");
        }
        return expression;
    }


    public YDecomposition getDecompositionPrototype() {
        return _decompositionPrototype;
    }


    public void setDecompositionPrototype(YDecomposition decomposition) {
        _decompositionPrototype = decomposition;

        /**
         * AJH: Check if this task is to perform outbound schema validation. This is currently configured
         *      via the tasks UI MetaData.
         */
        String attrVal = (String)decomposition.getAttributes().get(PERFORM_OUTBOUND_SCHEMA_VALIDATION);

        if("TRUE".equalsIgnoreCase(attrVal))
        {
            setSkipOutboundSchemaChecks(true);
        }
    }

    /**
     * Connects the query to a decomposition enablement parameter.
     * @param query a query applied to the net enablement variable in the net 
     *      containing this task.
     * @param paramName the enablement decomposition parameter to which to apply the result.
     */
    public void setDataBindingForEnablementParam(String query, String paramName) {
        _dataMappingsForTaskEnablement.put(paramName, query);
    }

    /**
     * Returns the query to a decomposition enablement parameter.
     * @param paramName the decomposition enablement variable.
     * @return the data binding query for that parameter.
     */
    
    public String getDataBindingForEnablementParam(String paramName) {
      return _dataMappingsForTaskEnablement.get(paramName);
    }
    
    /**
     * Connects the query to a decomposition parameter.
     * @param query a query applied to the net variables in the net containing
     * this task.
     * @param paramName the decomposition parameter to which to apply the result.
     */
    public void setDataBindingForInputParam(String query, String paramName) {
        _dataMappingsForTaskStarting.put(paramName, query);
    }

    /**
     * Returns the query to a decomposition input parameter.
     * @param paramName the decomposition input parameter.
     * @return the data binding query for that parameter.
     */
    
    public String getDataBindingForInputParam(String paramName) {
      return _dataMappingsForTaskStarting.get(paramName);
    }

    /**
     * Binds an output expression of a decomposition to a net variable.
     * @param query the ouptut expression belonging to the tasks decomposition
     * @param netVarName the net scope variable to which to apply the result.
     */
    public void setDataBindingForOutputExpression(String query, String netVarName) {
        _dataMappingsForTaskCompletion.put(query, netVarName);
    }

    /**
     * Returns the query to a decomposition output parameter.
     * @param paramName the decomposition output parameter.
     * @return the data binding query for that parameter.
     */
    public String getDataBindingForOutputParam(String paramName) {
      for (String outputParameterQuery : _dataMappingsForTaskCompletion.keySet()) {
        String outputParameter = _dataMappingsForTaskCompletion.get(outputParameterQuery);
        if (paramName.equals(outputParameter)) {
          return outputParameterQuery;
        }
      }
      return null; 
    }

    public String getInformation() {
        try {
            YAWLServiceGateway gateway = (YAWLServiceGateway) getDecompositionPrototype();
            StringBuffer result = new StringBuffer();
            result.append("<taskInfo>");

            result.append("<specificationID>");
            result.append(_net.getSpecification().getID());
            result.append("</specificationID>");

            result.append("<taskID>");
            result.append(getID());
            result.append("</taskID>");

            result.append("<taskName>");
            result.append(_name != null ? _name : _decompositionPrototype.getID());
            result.append("</taskName>");

            if (_documentation != null) {
                result.append("<taskDocumentation>");
                result.append(_documentation);
                result.append("</taskDocumentation>");
            }
            if (_decompositionPrototype != null) {
                result.append("<decompositionID>");
                result.append(_decompositionPrototype.getID());
                result.append("</decompositionID>");

        for (Enumeration atts = _decompositionPrototype.getAttributes().keys(); atts.hasMoreElements() ;) {
		    result.append("<attributes>");
		    
		    String key = (String) atts.nextElement();		    
		    String value = (String) _decompositionPrototype.getAttributes().get(key);
		    result.append("<"+key+">"+value+"</"+key+">");
		    
		    result.append("</attributes>");		    
		}
                YAWLServiceGateway wsgw = (YAWLServiceGateway) _decompositionPrototype;
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
            Collection inputParams = gateway.getInputParameters().values();
            for (Iterator iterator = inputParams.iterator(); iterator.hasNext();) {
                YParameter parameter = (YParameter) iterator.next();
                String paramAsXML = parameter.toSummaryXML();
                result.append(paramAsXML);
            }
            Collection outputParams = gateway.getOutputParameters().values();
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
    public String getSpecVersion() {
        return _net.getSpecification().getSpecVersion();
    }


    //######################################################################################
    //###########################  BEGIN VERIFICATION CODE  ################################
    //######################################################################################
    public List verify() {
        List messages = new Vector();
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
            List postsetFlows = new ArrayList(getPostsetFlows());
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
        if (_decompositionPrototype != null) {
            messages.addAll(checkParameterMappings());
        } else {
            if (_dataMappingsForTaskStarting.size() > 0) {
                messages.add(new YVerificationMessage(
                        this, "Syntax error for " + this + " to have startingMappings and no decomposition.",
                        YVerificationMessage.ERROR_STATUS));
            }
            if (_dataMappingsForTaskCompletion.size() > 0) {
                messages.add(new YVerificationMessage(
                        this,
                        "Syntax error for " + this + " to have completionMappings and no decomposition.",
                        YVerificationMessage.ERROR_STATUS));
            }
        }
        return messages;
    }

    private Collection checkParameterMappings() {
        List messages = new ArrayList();
        messages.addAll(checkInputParameterMappings());
        messages.addAll(checkForDuplicateParameterMappings());
        messages.addAll(checkOutputParameterMappings());
        return messages;
    }

    private Collection checkOutputParameterMappings() {
        List messages = new ArrayList();
        if (_net._specification.usesSimpleRootData()) {
            messages.addAll(checkOutputParamsPreBeta4());
        }
        //check that each output query has valid syntax
        for (String nextQuery : _dataMappingsForTaskCompletion.keySet()) {
            String netVarNam = _dataMappingsForTaskCompletion.get(nextQuery);
            messages.addAll(checkXQuery(nextQuery, netVarNam));
        }
        //check that non existant local variables are not assigned output.
        Set outPutVarsAssignedTo = getLocalVariablesForTaskCompletion();
        for (Iterator iterator = outPutVarsAssignedTo.iterator(); iterator.hasNext();) {
            String localVarName = (String) iterator.next();
            if (_net.getLocalVariables().get(localVarName) == null &&
                    _net.getInputParameters().get(localVarName) == null) {
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


    private Collection checkForDuplicateParameterMappings() {
        List messages = new ArrayList();
        //catch the case where serveral expressions map to the same decomposition input param
        //The only case where the schema misses this is where the muilti-instance input
        //is the same as one the regular variable mappings
        int numOfUniqueParamsMappedTo = new HashSet(
                _dataMappingsForTaskStarting.values()).size();
        int numParams = _dataMappingsForTaskStarting.size();
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
        int numOfUniqueNetVarsMappedTo = new HashSet<String>(_dataMappingsForTaskCompletion.values()).size();
        numParams = _dataMappingsForTaskCompletion.size();
        if (numOfUniqueNetVarsMappedTo != numParams) {
            messages.add(new YVerificationMessage(this,
                    "A output parameter is used twice.  The task (id=" + getID() + ") " +
                    "uses the same parameter through its multi-instance output " +
                    "and its regular output.",
                    YVerificationMessage.ERROR_STATUS));
        }
        return messages;
    }

    private Collection checkOutputParamsPreBeta4() {
        List messages = new ArrayList();
        //check there is link to each to each output param(query).
        Set outputQueriesAtDecomposition =
                _decompositionPrototype.getOutputQueries();
        Set outputQueriesAtTask = getQueriesForTaskCompletion();
        for (Iterator iterator = outputQueriesAtDecomposition.iterator();
             iterator.hasNext();) {
            String query = (String) iterator.next();
            if (!outputQueriesAtTask.contains(query)) {
                messages.add(new YVerificationMessage(this,
                        this + " there exists an output" +
                        " query(" + query + ") in " + _decompositionPrototype +
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
                        _decompositionPrototype + ").",
                        YVerificationMessage.ERROR_STATUS));
            }
        }
        return messages;
    }


    private Collection checkInputParameterMappings() {
        List messages = new ArrayList();
        //check that there is a link to each inputParam
        Set inputParamNamesAtDecomposition = _decompositionPrototype.getInputParameterNames();
        Set inputParamNamesAtTask = getParamNamesForTaskStarting();
        //check that task input var maps to decomp input var
        for (Iterator iterator = inputParamNamesAtDecomposition.iterator(); iterator.hasNext();) {
            String paramName = (String) iterator.next();

            String query = _dataMappingsForTaskStarting.get(paramName);
            messages.addAll(checkXQuery(query, paramName));

            if (!inputParamNamesAtTask.contains(paramName)) {
                messages.add(new YVerificationMessage(this,
                        "The task (id= " + this.getID() + ")" +
                        " needs to be connected with the input parameter (" +
                        paramName + ")" + " of decomposition (" +
                        _decompositionPrototype + ").",
                        YVerificationMessage.ERROR_STATUS));
            }
        }
        return messages;
    }
    /**
     * Indicates if schema validation is to be performed when starting the task.
     * @return whether or not to skip validation on task starting.
     */
    private boolean skipOutboundSchemaChecks() {
        return _skipOutboundSchemaChecks;
    }

    /**
     * Defines if schema validation is to be performed when starting the task.
     * @param performOutboundSchemaChecks
     */
    private void setSkipOutboundSchemaChecks(boolean performOutboundSchemaChecks) {
        _skipOutboundSchemaChecks = performOutboundSchemaChecks;
    }


    public ResourceMap getResourceMap() { return _resourceMap; }

    public ResourceMap getResourceMap(boolean forceNew) {
        if ((_resourceMap == null) && forceNew)
           _resourceMap = new ResourceMap(this.getID());

        return _resourceMap;
    }


    public void setResourceMap(ResourceMap map) { _resourceMap = map ; }


    public Element getResourcingSpecs() { return _resourcingSpec ; }

    public void setResourcingSpecs(Element resSpec) {
        _resourcingSpec = resSpec ;
    }


    public void setCustomFormURI(URL formURL) {
        _customFormURL = formURL ;
    }

    public URL getCustomFormURL() { return _customFormURL; }


    /*** TIMER SETTINGS ***/

    public void setTimerParameters(String netParamName) {
        _timerParams = new HashMap<String, Object>() ;
        _timerParams.put("netparam", netParamName) ;
    }


    public void setTimerParameters(YWorkItemTimer.Trigger trigger, Date expiryTime) {
        _timerParams = new HashMap<String, Object>() ;
        _timerParams.put("trigger", trigger) ;
        _timerParams.put("expiry", expiryTime) ;
    }


    public void setTimerParameters(YWorkItemTimer.Trigger trigger, long ticks,
                                   YTimer.TimeUnit timeUnit) {
        _timerParams = new HashMap<String, Object>() ;
        _timerParams.put("trigger", trigger) ;
        _timerParams.put("ticks", ticks) ;

        if (timeUnit == null) timeUnit = YTimer.TimeUnit.MSEC ;

        _timerParams.put("interval", timeUnit);
    }

    public void setTimerParameters(YWorkItemTimer.Trigger trigger, Duration duration) {
        _timerParams = new HashMap<String, Object>() ;
        _timerParams.put("trigger", trigger) ;
        _timerParams.put("duration", duration) ;        
    }


    public Map getTimeParameters() { return _timerParams; }


    public String timerParamsToXML() {
        if (_timerParams == null) return null ;

        StringBuilder xml = new StringBuilder("<timer>") ;

        // if there's a net-level param specified, that's all we need
        String netParam = (String) _timerParams.get("netparam");
        if (netParam != null) {
            xml.append(StringUtil.wrap(netParam, "netparam")) ;
        }
        else {
            YWorkItemTimer.Trigger trigger =
                                  (YWorkItemTimer.Trigger) _timerParams.get("trigger") ;
            xml.append(StringUtil.wrap(trigger.name(), "trigger"));

            // if there's an expiry time, get it and we're done
            Date expiry = (Date) _timerParams.get("expiry") ;
            if (expiry != null) {
                Long dateAsLong = expiry.getTime();
                xml.append(StringUtil.wrap(dateAsLong.toString(), "expiry"));
            }
            else {
                // this is a duration timer
                Duration duration = (Duration) _timerParams.get("duration");
                if (duration != null) {
                    xml.append(StringUtil.wrap(duration.toString(), "duration"));
                }
                else {
                    // duration supplied as ticks / interval params
                    xml.append("<durationparams>");
                    Long ticks = (Long) _timerParams.get("ticks") ;
                    YTimer.TimeUnit interval = (YTimer.TimeUnit) _timerParams.get("interval") ;
                    xml.append(StringUtil.wrap(ticks.toString(), "ticks"));
                    xml.append(StringUtil.wrap(interval.name(), "interval"));
                    xml.append("</durationparams>");
                }
            }
        }
        xml.append("</timer>") ;
        return xml.toString();
    }

}
