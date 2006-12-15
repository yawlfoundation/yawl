/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package au.edu.qut.yawl.engine;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import au.edu.qut.yawl.authentication.UserList;
import au.edu.qut.yawl.elements.YAWLServiceReference;
import au.edu.qut.yawl.elements.YCondition;
import au.edu.qut.yawl.elements.YConditionInterface;
import au.edu.qut.yawl.elements.YDecomposition;
import au.edu.qut.yawl.elements.YExternalNetElement;
import au.edu.qut.yawl.elements.YFlow;
import au.edu.qut.yawl.elements.YNet;
import au.edu.qut.yawl.elements.YNetElement;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.elements.YTask;
import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.elements.state.YInternalCondition;
import au.edu.qut.yawl.engine.domain.YWorkItem;
import au.edu.qut.yawl.engine.domain.YWorkItemRepository;
import au.edu.qut.yawl.engine.interfce.interfaceX.ExceptionGateway;
import au.edu.qut.yawl.engine.interfce.interfaceX.InterfaceX_EngineSideClient;
import au.edu.qut.yawl.events.YErrorEvent;
import au.edu.qut.yawl.events.YawlEventLogger;
import au.edu.qut.yawl.exceptions.YDataStateException;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.exceptions.YQueryException;
import au.edu.qut.yawl.exceptions.YSchemaBuildingException;
import au.edu.qut.yawl.exceptions.YStateException;
import au.edu.qut.yawl.exceptions.YSyntaxException;
import au.edu.qut.yawl.persistence.dao.DAO;
import au.edu.qut.yawl.persistence.dao.DAOFactory;
import au.edu.qut.yawl.persistence.dao.DAOFactory.PersistenceType;
import au.edu.qut.yawl.persistence.dao.restrictions.LogicalRestriction;
import au.edu.qut.yawl.persistence.dao.restrictions.PropertyRestriction;
import au.edu.qut.yawl.persistence.dao.restrictions.Restriction;
import au.edu.qut.yawl.persistence.dao.restrictions.LogicalRestriction.Operation;
import au.edu.qut.yawl.persistence.dao.restrictions.PropertyRestriction.Comparison;
import au.edu.qut.yawl.persistence.managed.DataContext;
import au.edu.qut.yawl.persistence.managed.DataProxy;
import au.edu.qut.yawl.unmarshal.YMarshal;
import au.edu.qut.yawl.util.JDOMConversionTools;
import au.edu.qut.yawl.util.YDocumentCleaner;
import au.edu.qut.yawl.util.YMessagePrinter;
import au.edu.qut.yawl.util.YVerificationMessage;

/**
 *
 * @author Lachlan Aldred
 *         Date: 17/06/2003
 *         Time: 13:46:54
 *
 */
public abstract class AbstractEngine implements InterfaceADesign,
        InterfaceAManagement,
        InterfaceBClient,
        InterfaceBInterop {
    private static Logger logger = Logger.getLogger(YEngine.class);

    protected Map _caseIDToNetRunnerMap = new HashMap();
    private Map _runningCaseIDToSpecIDMap = new HashMap();

    private InterfaceAManagementObserver _interfaceAClient;
    private InterfaceBClientObserver _interfaceBClient;
    protected ObserverGatewayController observerGatewayController;

    protected static UserList _userList = UserList.getInstance();

    protected static List<ExceptionGateway> _exceptionObservers = new ArrayList();

    /*************************************************/
    /*INSERTED VARIABLES AND METHODS FOR PERSISTANCE */
    /**
     * *********************************************
     */
    protected static boolean journalising;
//    protected static boolean restoring;
    protected static int maxcase = 0;
//    protected static SessionFactory factory = null;

    /**
     * AJH: Switch indicating if we generate user interface attributes with a tasks output XML doclet.
     */
    private static boolean generateUIMetaData = false;

    /**
     * Consructor.
     */
    protected AbstractEngine() {
        observerGatewayController = new ObserverGatewayController();
    }

    public void restore() throws YPersistenceException {

        getDataContext();
        if (context!=null) {
            List runners = context.retrieveAll(YNetRunner.class ,null);
            for (int i = 0; i < runners.size();i++) {
                YNetRunner r = (YNetRunner) ((DataProxy) runners.get(i)).getData();
                if (!r.getArchived()) {
                    logger.debug("RESTORING RUNNER" + r.getNet().getParent().getID());

                    /*
                     * Ensure that the net (decomposition) has the
                     * updated data
                     * */
                    r.getNet().rebuildData();

                    YIdentifier yid = r.getCaseID();
                    YIdentifier.saveIdentifier(yid,null,null);
                    r.restoreObservers();           // case observers via IB
                    restoreRunner(r,r.getNet().getParent().getID());
                }
            }

            List workItems = context.retrieveAll(YWorkItem.class, null);
            for (int i = 0; i < workItems.size();i++) {
                YWorkItem item = (YWorkItem) ((DataProxy) workItems.get(i)).getData();
                logger.debug("RESTORING WORKITEM" + item.getIDString());
                item.addToRepository();
            }
        }
    }

    private static DataContext context;

    public static void setDataContext( DataContext context ) {
    	AbstractEngine.context = context;
    }

    public static DataContext getDataContext() {
    	if( context == null ) {
    		if (journalising) {
    			DAO mem = DAOFactory.getDAO( PersistenceType.SPRING );
    			context = new DataContext( mem );
    		} else {
    			DAO mem = DAOFactory.getDAO( PersistenceType.MEMORY );
    			context = new DataContext( mem );
    		}
    	}
    	return context;
    }

    //###################################################################################
    //                                MUTATORS
    //###################################################################################
    /**
     * Adds the specification contained in the parameter file to the engine
     *
     * AJH - MODIFIED FOR TRANSACTIONAL PERSISTENCE
     *
     *
     * @param specificationFile
     * @param ignoreErors       ignore verfication errors and load the spec anyway.
     * @param errorMessages     - an in/out param passing any error messages.
     * @return the specification ids of the sucessfully loaded specs
     */
    public List<String> addSpecifications(File specificationFile, boolean ignoreErors, List<YVerificationMessage> errorMessages) throws JDOMException, IOException, YPersistenceException {
            logger.debug("--> addSpecification: File=" + specificationFile.getAbsolutePath());

            List<String> returnIDs = new Vector<String>();
            List newSpecifications;
            String parsingMsg;
            try {
                //if there is an XML Schema problem _report it and abort
                newSpecifications = YMarshal.unmarshalSpecifications(specificationFile.getAbsolutePath());
            } catch (YSyntaxException e) {
                parsingMsg = e.getMessage();
                //catch the xml parsers exception,
                //transform it into YAWL format and abort the load
                for (StringTokenizer tokens = new StringTokenizer(parsingMsg, "\n"); tokens.hasMoreTokens();) {
                    String msg = tokens.nextToken();
                    errorMessages.add(new YVerificationMessage(null, msg, YVerificationMessage.ERROR_STATUS));
                }
                logger.debug("<-- addSpecifcations: syntax exceptions found");
                return returnIDs;
            }
            catch (YSchemaBuildingException e) {
                logger.error("Could not build schema.", e);
                e.printStackTrace();
                return null;
            }
            for (Iterator iterator = newSpecifications.iterator(); iterator.hasNext();) {
                YSpecification specification = (YSpecification) iterator.next();
                List messages = specification.verify();
                if (messages.size() > 0 && !ignoreErors) {
                    YMessagePrinter.printMessages(messages);
                    errorMessages.addAll(messages);
                }
                //if the error messages are empty or contain only warnings
                if (YVerificationMessage.containsNoErrors(errorMessages)) {
                    boolean success = loadSpecification(specification);
                    if (success) {
                        logger.info("Persisted specification loaded from file " + specificationFile.getAbsolutePath());
                        returnIDs.add(specification.getID());
                    } else {//the user has loaded the specification with identical id
                        errorMessages.add(new YVerificationMessage(this,
                                "There is a specification with an identical id to ["
                                + specification.getID() + "] already loaded into the engine.",
                                YVerificationMessage.ERROR_STATUS));
                    }
                }
            }

            logger.debug("<-- addSpecifications: " + returnIDs.size() + " IDs loaded");
            return returnIDs;
    }


    public boolean loadSpecification(YSpecification spec) throws YPersistenceException {
    	List<DataProxy> specs = getDataContext().retrieveByRestriction(
    			YSpecification.class,
    			new LogicalRestriction(
    					new PropertyRestriction( "archived", Comparison.EQUAL, false ),
    					Operation.AND,
    					new PropertyRestriction( "ID", Comparison.EQUAL, spec.getID() ) ),
    			null );
    	if( specs.size() == 0 ) {
    		DataProxy proxy = getDataContext().createProxy( spec, null );
    		getDataContext().attachProxy( proxy, spec, null );
    		getDataContext().save( proxy );
    		return true;
    	}
    	return false;
    }


    public YIdentifier startCase(String username, String specID, String caseParams, URI completionObserver) throws YStateException, YSchemaBuildingException, YDataStateException, YPersistenceException {
        SAXBuilder builder = new SAXBuilder();
        Element data = null;
        if(caseParams != null && !"".equals(caseParams)) {
            try {
                Document dirtyDoc;
                dirtyDoc = builder.build(new StringReader(caseParams));
                data = YDocumentCleaner.cleanDocument(dirtyDoc).getRootElement();
            } catch (Exception e) {
                YStateException f = new YStateException(e.getMessage());
                f.setStackTrace(e.getStackTrace());
                throw f;
            }
        }

        YSpecification specification = getSpecification(specID);

        //Go to the database if the specification is not found, error
        //if the specification is not in the database either

        if (specification != null) {
            YNetRunner runner = new YNetRunner(specification.getRootNet(), data);

            // register exception service with the net runner (MJA 4/4/06)
                announceCheckCaseConstraints(specID,
                                             runner.getCaseID().toString(), caseParams, true);

            if(completionObserver != null) {
                YAWLServiceReference observer = getRegisteredYawlService(completionObserver.toString());
                if (observer != null) {
                    runner.setObserver(observer);
                } else {
                    logger.warn("Completion observer: " + completionObserver + " is not a registered YAWL service.");
                }
            }

            YNetRunner.saveNetRunner( runner, null );
            /*
             * INSERTED FOR PERSISTANCE
             */
//  TODO          if (!restoring) {
//                logger.info("Persisting process instance " + runner.getCaseID().get_idString());
////AJH                yper.storeData(runner);
////                if (pmgr != null) {
////                    pmgr.storeObject(runner);
////                }
//                DaoFactory.createYDao().create(runner);
//            }

            /*
             * REMOVE THIS...ONLY USED FOR UNIT TESTS
             *
             * *//*
            if (true) {

            	YPersistenceException ypers = new YPersistenceException("test");
            	throw ypers;
            }*/

            runner.continueIfPossible();

            // LOG CASE EVENT
            YawlEventLogger.getInstance().logCaseCreated(runner.getCaseID().toString(), username, specID);

            runner.start();
            _caseIDToNetRunnerMap.put(runner.getCaseID(), runner);
            _runningCaseIDToSpecIDMap.put(runner.getCaseID(), specID);

            if (_interfaceBClient != null) {
                logger.debug("Asking client to add case " + runner.getCaseID().toString());
                _interfaceBClient.addCase(specID, runner.getCaseID().toString());
            }

            return runner.getCaseID();
        } else {
            throw new YStateException(
                    "No specification found with ID [" + specID + "]");
        }
    }

    public void restoreRunner(YNetRunner runner, String specID) {
    	try {
    		_caseIDToNetRunnerMap.put(runner.getCaseID(), runner);
    		_runningCaseIDToSpecIDMap.put(runner.getCaseID(), specID);
    		runner.start();
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }

    protected void finishCase(YIdentifier caseIDForNet) throws YPersistenceException {
        logger.debug("--> finishCase: Case=" + caseIDForNet.getId());

        YNetRunner runner = (YNetRunner) _caseIDToNetRunnerMap.get(caseIDForNet);

        _caseIDToNetRunnerMap.remove(caseIDForNet);
        _runningCaseIDToSpecIDMap.remove(caseIDForNet);
        //YEngine._workItemRepository.cancelNet(caseIDForNet);

        //  LOG CASE EVENT
        YawlEventLogger.getInstance().logCaseCompleted(caseIDForNet.toString());
        if (_interfaceBClient != null) {
            _interfaceBClient.removeCase(caseIDForNet.toString());
        }

        logger.debug("<-- finishCase");
    }


    /**
     *
     *
     * AJH - MODIFIED FOR TRANSACTIONAL PERSISTENCE
     *
     * @param specID
     * @throws YStateException
     * @throws YPersistenceException
     */
    public void unloadSpecification(String specID) throws YStateException, YPersistenceException {
        logger.debug("--> unloadSpecification: ID=" + specID);

        YSpecification spec = getSpecification(specID);

        if (spec != null) {
            /* Mark as archived */
            logger.info("Removing process specification " + specID);

            DataProxy<YSpecification> proxy = getDataContext().getDataProxy(spec);
            spec.markArchived();

            getDataContext().save( proxy );
        } else {
            throw new YStateException(
            		"Engine contains no such specification with id [" + specID + "].");
        }

        logger.debug("<-- unloadSpecification");
    }



    /**
     * Cancels a running case - Internal interface that requires reference to current transaction's persistence
     * manager object.<P>
     *
     * @param id
     * @throws YPersistenceException
     */
    public void cancelCase(YIdentifier id) throws YPersistenceException {
        logger.debug("--> cancelCase");

        if (id == null) {
            throw new IllegalArgumentException("should not cancel case with a null id");
        }

//        YPersistance.getInstance().clearCase(id);
        //todo AJH - Suspect NetRunner is also clearing from database ????
        logger.info("Deleting persisted process instance " + id);

// AJH: Replaced
//        getYper().clearCase(id);

        try {
            clearCase(id);

            YNetRunner runner = (YNetRunner) _caseIDToNetRunnerMap.get(id);
            //commented by LJA because finishCase() 'cause gets performed again in finishCase()
            //_runningCaseIDToSpecIDMap.remove(id);

            // LOG CASE EVENT
            YawlEventLogger.getInstance().logCaseCancelled(id.toString());
            runner.cancel();

            YEngine._workItemRepository.removeWorkItemsForCase(id);

            finishCase(id);

            // announce cancellation to exception service (if required)
                announceCancellationToExceptionService(id) ;

        } catch (YPersistenceException e) {
            throw new YPersistenceException("Failure whilst persisting new specification", e);
        }
    }


    /**
     * Cancels the case - External interface.<P>
     *
     * For NetRunner calls to cancel a case, see the other overloaded method which accepts an instance of the
     * current persistence manager object.
     *
     * AJH - MODIFIED FOR TRANSACTIONAL PERSISTENCE
     *
     *
     * @param id the case ID.
     */
//    public void cancelCase(YIdentifier id) throws YPersistenceException {
//        /**
//         * SYNC'D External interface
//         */
//        synchronized (mutex) {
////
////    TODO        YPersistenceManager pmgr = null;
////
////            if (isJournalising()) {
////                pmgr = new YPersistenceManager(getPMSessionFactory());
////                pmgr.startTransactionalSession();
////            }
//
//            cancelCase(id);
//
//            try {
////   TODO             if (isJournalising()) {
////                    pmgr.commit();
////                }
//            } catch (YPersistenceException e) {
//                logger.warn("Persistence exception ignored (to be fixed)", e);
//            }
//        }
//    }


    //####################################################################################
    //                          ACCESSORS
    //####################################################################################
    /**
     * Provides the set of specification ids for specs loaded into the engine.  It returns
     * those that were loaded as well as those with running instances that are unloaded.
     *
     * @return a set of spec id strings.
     */
    public Set getSpecIDs() throws YPersistenceException {
    	List<DataProxy> proxies = getDataContext().retrieveAll( YSpecification.class, null );
    	Set<String> specIDs = new HashSet<String>();
    	for( DataProxy<YSpecification> proxy : proxies ) {
    		specIDs.add( proxy.getData().getID() );
    	}
    	return specIDs;
    }

    public Set<YSpecification> getSpecifications() throws YPersistenceException {
    	List<DataProxy> proxies = getDataContext().retrieveAll( YSpecification.class, null );
    	Set<YSpecification> specs = new HashSet<YSpecification>();
    	for( DataProxy<YSpecification> proxy : proxies ) {
    		specs.add( proxy.getData() );
    	}
    	return specs;
    }


    /**
     * Returns a set of all loaded process specifications.
     *
     * @return  A set of specification ids
     */

    public Set getLoadedSpecifications() throws YPersistenceException {
    	Set<YSpecification> specs = new HashSet<YSpecification>();
    	List<DataProxy> specList = getDataContext().retrieveByRestriction(
    			YSpecification.class,
    			new PropertyRestriction( "archived", Comparison.EQUAL, false ),
    			null );
    	for( DataProxy<YSpecification> proxy : specList ) {
    		specs.add( proxy.getData() );
    	}
    	return specs;
    }


    public YSpecification getSpecification(String specID) throws YPersistenceException {
    	List<DataProxy> specs = getDataContext().retrieveByRestriction(
    			YSpecification.class,
    			new LogicalRestriction(
    					new PropertyRestriction( "archived", Comparison.EQUAL, false ),
    					Operation.AND,
    					new PropertyRestriction( "ID", Comparison.EQUAL, specID ) ),
    			null );
    	if( specs != null && specs.size() == 1 ) {
    		DataProxy<YSpecification> proxy = specs.get( 0 );
    		return proxy.getData();
    	}
    	return null;
    }

    /**
     * Gets all spec
     * @param specID
     * @param versions
     * @return
     * @throws YPersistenceException
     */
    public List<YSpecification> getSpecificationVersions(
    		String specID, Set<Integer> versions) throws YPersistenceException {
    	Restriction restriction = new PropertyRestriction("ID", Comparison.EQUAL, specID);
    	if( versions != null ) {
    		for( Integer version : versions ) {
    			restriction = new LogicalRestriction(
    					new PropertyRestriction("version", Comparison.EQUAL, version),
    					Operation.OR,
    					restriction );
    		}
    	}
    	List<DataProxy> proxies = getDataContext().retrieveByRestriction(
    			YSpecification.class, restriction, null );
    	List<YSpecification> specs = new ArrayList<YSpecification>( proxies.size() + 1 );
    	for( DataProxy<YSpecification> proxy : proxies ) {
    		specs.add( proxy.getData() );
    	}
    	return specs;
    }


    public YIdentifier getCaseID(String caseIDStr) {
            logger.debug("--> getCaseID");

            Set idSet = _caseIDToNetRunnerMap.keySet();
            for (Iterator idSetIter = idSet.iterator(); idSetIter.hasNext();) {
                YIdentifier identifier = (YIdentifier) idSetIter.next();
                if (identifier.toString().equals(caseIDStr)) {
                    return identifier;
                }
            }
            return null;
    }


    public String getStateTextForCase(YIdentifier caseID) throws YPersistenceException {
            logger.debug("--> getStateTextForCase: ID=" + caseID.getId());

            Set allChildren = caseID.getDescendants();
            Set allLocations = new HashSet();
            for (Iterator childIter = allChildren.iterator(); childIter.hasNext();) {
                YIdentifier identifier = (YIdentifier) childIter.next();
                allLocations.addAll(identifier.getLocations());
            }
            StringBuffer stateText = new StringBuffer();
            stateText.append("#######################################" +
                    "######################\r\n" + "CaseID: ")
                    .append(caseID)
                    .append("\r\n" + "Spec:   ")
                    .append(_runningCaseIDToSpecIDMap.get(caseID))
                    .append("\r\n" + "###############################" +
                    "##############################\r\n");
            for (Iterator locationsIter = allLocations.iterator(); locationsIter.hasNext();) {
                YNetElement element = (YNetElement) locationsIter.next();
                if (element instanceof YCondition) {
                    stateText.append("CaseIDs in: ")
                            .append(element.toString())
                            .append("\r\n");
                    List identifiers = ((YConditionInterface) element).getIdentifiers();
                    stateText.append("\thashcode ")
                            .append(element.hashCode())
                            .append("\r\n");
                    for (Iterator idIter = identifiers.iterator(); idIter.hasNext();) {
                        YIdentifier identifier = (YIdentifier) idIter.next();
                        stateText.append("\t")
                                .append(identifier.toString())
                                .append("\r\n");
                    }
                } else if (element instanceof YTask) {
                    stateText.append("CaseIDs in: ")
                            .append(element.toString())
                            .append("\r\n");
                    YTask task = (YTask) element;
                    for (int i = 0; i < 4; i++) {
                        YInternalCondition internalCondition;
                        if (i == 0) {
                            internalCondition = task.getMIActive();
                        }
                        else if (i == 1) {
                            internalCondition = task.getMIEntered();
                        }
                        else if (i == 2) {
                            internalCondition = task.getMIExecuting();
                        }
                        else {//(i == 3)
                            internalCondition = task.getMIComplete();
                        }
                        if (internalCondition.containsIdentifier()) {
                            stateText.append("\t")
                                    .append(internalCondition.toString())
                                    .append("\r\n");
                            List identifiers = internalCondition.getIdentifiers();
                            for (Iterator iterator = identifiers.iterator(); iterator.hasNext();) {
                                YIdentifier identifier = (YIdentifier) iterator.next();
                                stateText.append("\t\t")
                                        .append(identifier.toString())
                                        .append("\r\n");
                            }
                        }
                    }
                }
            }
            return stateText.toString();
    }

    public String getStateForCase(String caseID) throws YPersistenceException {

       //PropertyRestriction restriction = new PropertyRestriction("archived", PropertyRestriction.Comparison.EQUAL , new Boolean(false));
       PropertyRestriction restriction = new PropertyRestriction("basicCaseId", PropertyRestriction.Comparison.EQUAL , caseID);
       List runners = context.retrieveByRestriction(YNetRunner.class, restriction ,null);

       if (runners.size()==0) {
           restriction = new PropertyRestriction("basicCaseId",
        		   PropertyRestriction.Comparison.EQUAL ,
        		   caseID.substring(0,caseID.indexOf(".")));
           runners = context.retrieveByRestriction(YNetRunner.class, restriction ,null);
       }


       //
    	Set allLocations = new HashSet();

    	if (!(runners.size()>1)) {
    		DataProxy<YNetRunner> proxy = (DataProxy) runners.get(0);
    		YNetRunner r = proxy.getData();
    		YIdentifier yid = r.getCaseID();
        	List l = r.getNet().getNetElements();
        	for (int i = 0; i < l.size(); i++) {
        		YNetElement elem = (YNetElement) l.get(i);
        		if (elem instanceof YCondition) {
        			if (((YCondition) elem).contains(yid)) {
        				allLocations.add(elem);
        			}
        		} else if (elem instanceof YTask) {
    				YIdentifier contained = ((YTask) elem).getContainingIdentifier();
        			if (contained!=null && contained.equals(yid)) {
        				allLocations.add(elem);
        			}
        		}
        	}
            return evaluateStateForCase(yid, allLocations, r.getNet().getParent().getID());
    	} else {
    		//multiple runners with same caseid, this is wrong
    	}

    	return "";

    }

    public String getStateForCase(YIdentifier caseID) throws YPersistenceException {
        Set allChildren = caseID.getDescendants();
        Set allLocations = new HashSet();
        for (Iterator childIter = allChildren.iterator(); childIter.hasNext();) {
            YIdentifier identifier = (YIdentifier) childIter.next();
            allLocations.addAll(identifier.getLocations());
        }

        return evaluateStateForCase(caseID, allLocations, _runningCaseIDToSpecIDMap.get(caseID).toString());

    }


    public String evaluateStateForCase(YIdentifier caseID, Set allLocations, String spec) {
            StringBuffer stateText = new StringBuffer();
            stateText.append("<caseState " + "caseID=\"")
                    .append(caseID)
                    .append("\" " + "specID=\"")
                    .append(spec)
                    .append("\">");
            for (Iterator locationsIter = allLocations.iterator(); locationsIter.hasNext();) {
                YNetElement element = (YNetElement) locationsIter.next();
                if (element instanceof YCondition) {
                    stateText.append("<condition " + "id=\"")
                            .append(element.toString())
                            .append("\" " + "name=\"")
                            .append(((YCondition) element).getName())
                            .append("\" " + "documentation=\"")
                            .append(((YCondition) element).getDocumentation())
                            .append("\">");
                    List identifiers = ((YConditionInterface) element).getIdentifiers();
                    for (Iterator idIter = identifiers.iterator(); idIter.hasNext();) {
                        YIdentifier identifier = (YIdentifier) idIter.next();
                        stateText.append("<identifier>")
                                .append(identifier.toString())
                                .append("</identifier>");
                    }

                    /**
                     * AJH: Add in flow/link data
                     */
                    stateText.append("<flowsInto>");

                    Iterator postsetFlows = ((YCondition)element).getPostsetFlows().iterator();
                    if (postsetFlows != null)
                    {
                        while(postsetFlows.hasNext())
                        {
                            Object obj = postsetFlows.next();
                            if (obj instanceof YFlow)
                            {
                                YFlow flow = (YFlow)obj;
                                String doc;
                                if (flow.getDocumentation() == null)
                                {
                                    doc = "";
                                }
                                else
                                {
                                    doc = flow.getDocumentation();
                                }

                                stateText.append("<nextElementRef id=\"")
                                        .append(flow.getNextElement().getID())
                                        .append("\" " + "documentation=\"")
                                        .append(doc)
                                        .append("\"/>");
                            }
                        }
                    }

                    stateText.append("</flowsInto>");

                    stateText.append("</condition>");
                } else if (element instanceof YTask) {
                    stateText.append("<task " + "id=\"")
                            .append(element.toString())
                            .append("\" " + "name=\"")
                            .append(((YTask) element)
                                    .getDecompositionPrototype().getId())
                            .append("\">");
                    YTask task = (YTask) element;
                    for (int i = 0; i < 4; i++) {
                        YInternalCondition internalCondition;
                        if (i == 0) {
                            internalCondition = task.getMIActive();
                        }
                        else if (i == 1) {
                            internalCondition = task.getMIEntered();
                        }
                        else if (i == 2) {
                            internalCondition = task.getMIExecuting();
                        }
                        else {//if (i == 3)
                            internalCondition = task.getMIComplete();
                        }
                        if (internalCondition.containsIdentifier()) {
                            stateText.append("<internalCondition " + "id=\"")
                                    .append(internalCondition.toString())
                                    .append("\">");
                            List identifiers = internalCondition.getIdentifiers();
                            for (Iterator iterator = identifiers.iterator(); iterator.hasNext();) {
                                YIdentifier identifier = (YIdentifier) iterator.next();
                                stateText.append("<identifier>")
                                        .append(identifier.toString())
                                        .append("</identifier>");
                            }
                            stateText.append("</internalCondition>");
                        }
                    }
                    stateText.append("</task>");
                }
            }
            stateText.append("<Errors>");
            List<YErrorEvent> errorlist = caseID.getErrors();
            for (int j = 0; j < errorlist.size(); j++) {
            	stateText.append(errorlist.get(j).toXML());
            }
            stateText.append("</Errors>");

            stateText.append("</caseState>");
            return stateText.toString();
    }


    public void registerInterfaceAClient(InterfaceAManagementObserver observer) {
            _interfaceAClient = observer;
    }

    public void registerInterfaceBObserver(InterfaceBClientObserver observer) {
            _interfaceBClient = observer;
    }

    /**
     * Registers an InterfaceB Observer Gateway with the engine in order to receive callbacks.<P>
     *
     * @param gateway
     */
    public void registerInterfaceBObserverGateway(ObserverGateway gateway) {
            observerGatewayController.addGateway(gateway);
    }


    //################################################################################
    //   BEGIN REST-FUL SERVICE METHODS
    //################################################################################
    public Set<YWorkItem> getAvailableWorkItems() {
        // TESTING
        if (logger.isDebugEnabled()) {
            dump();
        }

            if (logger.isDebugEnabled()) {
            	try {
            		logger.debug("--> getAvailableWorkItems: Enabled=" + YEngine._workItemRepository.getEnabledWorkItems().size() +
            				" Fired=" + YEngine._workItemRepository.getFiredWorkItems().size());
            	}
            	catch( YPersistenceException e ) {
            		logger.debug( "error logging debug information", e );
            	}
            }

            Set<YWorkItem> allItems = new HashSet<YWorkItem>();
            try {
            	allItems.addAll(YEngine._workItemRepository.getEnabledWorkItems());
            	allItems.addAll(YEngine._workItemRepository.getFiredWorkItems());
            }
            catch( YPersistenceException e ) {
            	// FIXME
            	throw new RuntimeException( e );
            }

            logger.debug("<-- getAvailableWorkItems");
            return allItems;
    }


    public YSpecification getProcessDefinition(String specID) throws YPersistenceException {
    	return getSpecification(specID);
    }


    public YWorkItem getWorkItem(String workItemID) {
            YWorkItem workItem = YEngine._workItemRepository.getWorkItem(workItemID);
            if (workItem != null) {
                return workItem;
            } else {
                return null;
            }
    }


    public Set getAllWorkItems() {
            return YEngine._workItemRepository.getAllWorkItems();
    }


    /**
     * Starts a work item.  If the workitem param is enabled this method fires the task
     * and returns the first of its child instances in the exectuting state.
     * Else if the workitem is fired then it moves the state from fired to exectuing.
     * Either way the method returns the resultant work item.
     *
     * @param workItem the enabled, or fired workitem.
     * @param userID   the user id.
     * @return the resultant work item in the executing state.
     * @throws YStateException     if the workitem is not in either of these
     *                             states.
     * @throws YDataStateException
     */
    public YWorkItem startWorkItem(YWorkItem workItem, String userID) throws YStateException, YDataStateException, YQueryException, YSchemaBuildingException, YPersistenceException {

//            YPersistenceManager pmgr = null;

            logger.debug("--> startWorkItem");

            try {
//    TODO            if (isJournalising()) {
//                    pmgr = new YPersistenceManager(getPMSessionFactory());
//                    pmgr.startTransactionalSession();
//                }

                YNetRunner netRunner = null;
                YWorkItem resultantItem = null;
                if (workItem != null) {
                    if (workItem.getStatus() == YWorkItem.Status.Enabled) {
                        netRunner = YEngine._workItemRepository.getNetRunner(workItem.getCaseID());
                        List childCaseIDs;
                        childCaseIDs = netRunner.attemptToFireAtomicTask(workItem.getTaskID());

                        if (childCaseIDs != null) {
                            for (int i = 0; i < childCaseIDs.size(); i++) {
                                YIdentifier childID = (YIdentifier) childCaseIDs.get(i);
                                YWorkItem nextWorkItem = workItem.createChild(childID);
                                if (i == 0) {
                                    netRunner.startWorkItemInTask(nextWorkItem.getCaseID(), workItem.getTaskID());
                                    nextWorkItem.setStatusToStarted(userID);
                                    Element dataList = ((YTask)
                                            netRunner.getNetElement(workItem.getTaskID())).getData(childID);
                                    nextWorkItem.setData(dataList);
                                    resultantItem = nextWorkItem;
                                }
                            }
                        }
                    } else if (workItem.getStatus() == YWorkItem.Status.Fired) {
                        workItem.setStatusToStarted(userID);
                        netRunner = YEngine._workItemRepository.getNetRunner(workItem.getCaseID().getParent());
                        Element dataList = ((YTask) netRunner.getNetElement(workItem.getTaskID())
                                ).getData(workItem.getCaseID());
                        workItem.setData(dataList);
                        netRunner.startWorkItemInTask(workItem.getCaseID(), workItem.getTaskID());
                        resultantItem = workItem;
                    } else if (workItem.getStatus() == YWorkItem.Status.Deadlocked) {
                        resultantItem = workItem;
                    } else {
//     TODO                   if (isJournalising()) {
//                            pmgr.rollbackTransaction();
//                        }

                        throw new YStateException("Item (" + workItem.getIDString() + ") status (" +
                                workItem.getStatus() + ") does not permit starting.");
                        //this work item is likely already executing.
                    }
                } else {
//      TODO              if (isJournalising()) {
//                        pmgr.rollbackTransaction();
//                    }
                    throw new YStateException("No such work item currently available.");
                }

               // YNetRunner.saveNetRunner(netRunner, null);
                // COMMIT POINT
//   TODO             if (journalising) {
//                    pmgr.commit();
//                }

                logger.debug("<-- startWorkItem");
                return resultantItem;
            } catch (Exception e) {
//    TODO            if (journalising) {
//                    pmgr.rollbackTransaction();
//                }

                // re-throw exception (tacky code ....)
                if (e instanceof YStateException) {
                    throw (YStateException) e;
                } else if (e instanceof YDataStateException) {
                    throw (YDataStateException) e;
                } else if (e instanceof YQueryException) {
                    throw (YQueryException) e;
                } else if (e instanceof YSchemaBuildingException) {
                    throw (YSchemaBuildingException) e;
                } else if (e instanceof YPersistenceException) {
                    throw (YPersistenceException) e;
                }
                /**
                 * AJH - Catch other exceptions here to avoid silient failures ....
                 */
                else {
                    logger.error("Failure starting workitem " + workItem.getWorkItemID().toString(), e);
                    throw new YQueryException(e);
                }
            }
    }


    /**
     * Returns the task definition, not the task instance.
     *
     * @param specificationID the specification id
     * @param taskID          the task id
     * @return the task definition object.
     */
    public YTask getTaskDefinition(String specificationID, String taskID) throws YPersistenceException {
            YSpecification specification = getSpecification(specificationID);
            if (specification != null) {
                List decompositions = specification.getDecompositions();
                for (Iterator iterator2 = decompositions.iterator(); iterator2.hasNext();) {
                    YDecomposition decomposition = (YDecomposition) iterator2.next();
                    if (decomposition instanceof YNet) {
                        if (((YNet) decomposition).getNetElement(taskID) != null) {
                            YExternalNetElement el = ((YNet) decomposition).getNetElement(taskID);
                            if (el instanceof YTask) {
                                return (YTask) ((YNet) decomposition).getNetElement(taskID);
                            }
                        }
                    }
                }
            }
            return null;
    }


    /**
     * Completes the work item.
     *
     * @param workItem
     * @param data
     * @throws YStateException
     */
    public void completeWorkItem(YWorkItem workItem, String data, boolean force)
            throws YStateException, YDataStateException, YQueryException, YSchemaBuildingException, YPersistenceException {
//            YPersistenceManager pmgr = null;
            logger.debug("--> completeWorkItem");

            try {
                // Create a PM
//    TODO            if (isJournalising()) {
//                    pmgr = new YPersistenceManager(getPMSessionFactory());
//                    pmgr.startTransactionalSession();
//                }

                Document doc;
                if (workItem != null) {
                    if (workItem.getStatus() == YWorkItem.Status.Executing) {
                        YNetRunner netRunner = YEngine._workItemRepository.getNetRunner(workItem.getCaseID().getParent());
                        synchronized (netRunner) {

                                if (netRunner.isTimeServiceTask(workItem)) {
                                    List timeOutSet = netRunner.getTimeOutTaskSet(workItem);
                                    announceTimeOutToExceptionService(workItem, timeOutSet);
                                }
                            SAXBuilder builder = new SAXBuilder();
                            //doing this because saxon can't do an effective query when the whitespace is there
                            try {
                                Document d = builder.build(new StringReader(data));
                                Document e = YDocumentCleaner.cleanDocument(d);
                                doc = e;
                                workItem.setStatusToComplete(force);
                                boolean taskExited = netRunner.completeWorkItemInTask(workItem, workItem.getCaseID(), workItem.getTaskID(), e);
                                if (taskExited) {
                                    //todo AJH:
                                    /* BUG - If we post the same task again (i.e. tight loop), we end up clearing the task we've just posted.
                                     */
                                    //                            YEngine._workItemRepository.removeWorkItemFamily(workItem);

                                    /* Calling this to fix a problem.
                                     * When a Task is enabled twice by virtue of having two enabling sets of
                                     * tokens in the current marking the work items are not created twice.
                                     * Instead an Enabled work item is created for one of the enabling sets.
                                     * Once that task has well and truly finished it is then an appropriate
                                     * time to notify the worklists that it is enabled again.
                                     * This is done by calling continueIfPossible().*/
                                    logger.debug("Recalling continue (looping bugfix???)");
                                    netRunner.continueIfPossible();
                                }
                            } catch (JDOMException e) {
                                YStateException f = new YStateException(e.getMessage());
                                f.setStackTrace(e.getStackTrace());
//    TODO                            if (isJournalising()) {
//                                    pmgr.rollbackTransaction();
//                                }
                                throw f;
                            } catch (IOException e) {
                                YStateException f = new YStateException(e.getMessage());
                                f.setStackTrace(e.getStackTrace());
//    TODO                            if (isJournalising()) {
//                                    pmgr.rollbackTransaction();
//                                }
                                throw f;
                            }
                        }
                        /*
                         * This method call is inserted purely for logging
                         * purposes
                         * */
                        workItem.completeData(doc);

                        /*
                         * Can't save the net runner here, because it could have been
                         * deleted through cancel methods.
                         * */
                        //YNetRunner.saveNetRunner(netRunner, null);

                    } else if (workItem.getStatus() == YWorkItem.Status.Deadlocked) {
                        YEngine._workItemRepository.removeWorkItemFamily(workItem);
                    } else {
                        throw new YStateException("WorkItem with ID [" + workItem.getIDString() +
                                "] not in executing state.");
                    }

                    // COMMIT POINT
//    TODO                if (isJournalising()) {
//                        pmgr.commit();
//                    }

                    /**
                     * AJH: Test hook for revised persistence
                     */
                    persistCase(workItem.getCaseID());
                } else {
//    TODO                if (isJournalising()) {
//                        pmgr.rollbackTransaction();
//                    }
                    throw new YStateException("WorkItem argument is equal to null.");
                }
            } catch (Exception e) {
//    TODO            if (isJournalising()) {
//                    pmgr.rollbackTransaction();
//                }

                // Re-Throw exception
                if (e instanceof YStateException) {
                    throw (YStateException) e;
                } else if (e instanceof YDataStateException) {
                    throw (YDataStateException) e;
                } else if (e instanceof YQueryException) {
                    throw (YQueryException) e;
                } else if (e instanceof YSchemaBuildingException) {
                    throw (YSchemaBuildingException) e;
                } else if (e instanceof YPersistenceException) {
                    throw (YPersistenceException) e;
                }
                /**
                 * AJH: More error handling
                 */
                else {
                    e.printStackTrace();
                    throw new YSchemaBuildingException(e);
                }
            }
            logger.debug("<-- completeWorkItem");
    }


    /**
     * Determines whether or not a task will aloow a dynamically
     * created new instance to be created.  MultiInstance Task with
     * dyanmic instance creation.
     *
     * @param workItemID the workItemID of a sibling work item.
     * @throws YStateException if task is not MultiInstance, or
     *                         if task does not allow dynamic instance creation,
     *                         or if current number of instances is not less than the maxInstances
     *                         for the task.
     */
    public void checkElegibilityToAddInstances(String workItemID) throws YStateException {
    	try {
            YWorkItem item = YEngine._workItemRepository.getWorkItem(workItemID);
            if (item != null) {
                if (item.getStatus() == YWorkItem.Status.Executing) {
                    if (item.allowsDynamicCreation()) {
                        YIdentifier identifier = item.getCaseID().getParent();
                        YNetRunner netRunner =
                                YEngine._workItemRepository.getNetRunner(identifier);
                        boolean addEnabled =
                                netRunner.isAddEnabled(item.getTaskID(), item.getCaseID());
                        if (addEnabled) {
                            //do nothing
                        } else {
                            throw new YStateException("Adding instances is not possible in " +
                                    "current state.");
                        }
                    } else {
                        throw new YStateException("WorkItem[" + workItemID +
                                "] does not allow new instance creation.");
                    }
                } else {
                    throw new YStateException("WorkItem[" + workItemID +
                            "] is not in appropriate (executing) " +
                            "state for instance adding.");
                }
            } else {
                throw new YStateException("No work Item Found with id : " +
                        workItemID);
            }
    	}
    	catch( YQueryException e ) {
    		throw new YStateException( e );
    	}
    }


    /**
     * Creates a new work item instance when possible.
     *
     * @param workItem                the id of a work item inside the task to have a new instance.
     * @param paramValueForMICreation format "<data>[InputParam]*</data>
     *                                InputParam == <varName>varValue</varName>
     * @return the work item of the new instance.
     * @throws YStateException if the task is not able to create a new instance, due to
     *                         its state or its design.
     */
    public YWorkItem createNewInstance(YWorkItem workItem, String paramValueForMICreation) throws YStateException, YPersistenceException {

//            YPersistenceManager pmgr = null;

//   TODO         if (isJournalising()) {
//                pmgr = new YPersistenceManager(getPMSessionFactory());
//                pmgr.startTransactionalSession();
//            }

            try {
                if (workItem == null) {
                    throw new YStateException("No work item found.");
                }
                String taskID = workItem.getTaskID();
                YIdentifier siblingID = workItem.getCaseID();
                YNetRunner netRunner = YEngine._workItemRepository.getNetRunner(siblingID.getParent());
                synchronized (netRunner) {
                    checkElegibilityToAddInstances(workItem.getIDString());
                    //calling it again to double check while we hold the semaphore to the netRunner
                    SAXBuilder builder = new SAXBuilder();
                    //doing this because Saxon can't do an effective query when the whitespace is there
                    try {
                        Document d;
                        d = builder.build(new StringReader(paramValueForMICreation));
                        Document e = YDocumentCleaner.cleanDocument(d);
                        Element el = e.detachRootElement();
                        YIdentifier id = netRunner.addNewInstance(taskID, workItem.getCaseID(), el);
                        if (id != null) {
                            YWorkItem firedItem = workItem.getParent().createChild(id);

//   TODO                         if (pmgr != null) {
//                                pmgr.commit();
//                            }

                            return firedItem;
                            //success!!!!
                        } else {
//   TODO                         if (isJournalising()) {
//                                pmgr.rollbackTransaction();
//                            }

                            throw new YStateException("New work item not created.");
                        }
                    } catch (Exception e) {
//   TODO                     if (isJournalising()) {
//                            pmgr.rollbackTransaction();
//                        }
                        throw new YStateException(e.getMessage());
                    }
                }
            } catch (YStateException e1) {
//   TODO             if (pmgr != null) {
//                    pmgr.rollbackTransaction();
//                }
                throw e1;
            }
    }

   //added method
    @Deprecated
    public YWorkItem suspendWorkItem(String workItemID) throws YStateException, YPersistenceException {

        YWorkItem workItem = YEngine._workItemRepository.getWorkItem(workItemID);
        if (workItem != null) {
            if (workItem.hasLiveStatus()) {
                workItem.setStatusToSuspended();  //added
            }
        }

        return workItem ;
    }


    public YWorkItem unsuspendWorkItem(String workItemID) throws YStateException, YPersistenceException {

        YWorkItem workItem = YEngine._workItemRepository.getWorkItem(workItemID);
        if (workItem != null) {
            if (workItem.getStatus().equals(YWorkItem.Status.Suspended)) {
                    workItem.setStatusToUnsuspended();  //added
            }
        }
        return workItem ;
    }

    // rolls back a workitem from executing to fired
    public void rollbackWorkItem(String workItemID, String userName) throws YStateException, YPersistenceException {
        /**
         * SYNC'D External interface
         */


            YWorkItem workItem = YEngine._workItemRepository.getWorkItem(workItemID);
            if (workItem != null) {
//   TODO             if (isJournalising()) {
//                    pmgr = new YPersistenceManager(getPMSessionFactory());
//                    pmgr.startTransactionalSession();
//                }

                if (workItem.getStatus() == YWorkItem.Status.Executing) {
                    workItem.rollBackStatus();
                    YNetRunner netRunner = YEngine._workItemRepository.getNetRunner(workItem.getCaseID().getParent());
                    if (netRunner.rollbackWorkItem(workItem.getCaseID(), workItem.getTaskID())) {
                    } else {
//   TODO                     if (isJournalising()) {
//                            pmgr.rollbackTransaction();
//                        }

                        throw new YStateException("Work Item[" + workItemID +
                                "] is not in executing state.");
                    }
                    YNetRunner.saveNetRunner( netRunner, null );

                }
//   TODO             if (pmgr != null) {
//                    pmgr.commit();
//                }
            } else {
//   TODO             if (isJournalising()) {
//                    pmgr.rollbackTransaction();
//                }
                throw new YStateException("Work Item[" + workItemID + "] not found.");
            }
    }


    public String launchCase(String username, String specID, String caseParams, URI completionObserver) throws YStateException, YDataStateException, YSchemaBuildingException, YPersistenceException {
//            YPersistenceManager pmgr = null;
            String caseIDString = null;

            logger.debug("--> launchCase");

            // Create a PM
//  TODO          if (isJournalising()) {
//                pmgr = new YPersistenceManager(getPMSessionFactory());
//                pmgr.startTransactionalSession();
//            }

            YIdentifier caseID = startCase(username, specID, caseParams, completionObserver);

            if (caseID != null) {
                caseIDString = caseID.toString();
            } else {
//   TODO             if (isJournalising()) {
//                    pmgr.rollbackTransaction();
//                }

                throw new YStateException("No specification found for [" + specID + "].");
            }

//  TODO          if (isJournalising()) {
//                pmgr.commit();
//            }
            logger.debug("<-- launchCase");
            return caseIDString;
    }




    /**
     * Given a process specification id return the cases that are its running
     * instances.
     *
     * @param specID the process specification id string.
     * @return a set of YIdentifer caseIDs that are run time instances of the
     *         process specification with id = specID
     */
    public Set getCasesForSpecification(String specID) {
        Set resultSet = new HashSet();
        Set caseIDs = _runningCaseIDToSpecIDMap.keySet();
        for (Iterator iterator = caseIDs.iterator(); iterator.hasNext();) {
            YIdentifier caseID = (YIdentifier) iterator.next();
            String specIDForCaseID = (String) _runningCaseIDToSpecIDMap.get(caseID);
            if (specIDForCaseID.equals(specID)) {
                resultSet.add(caseID);
            }
        }
        return resultSet;
    }


    public YAWLServiceReference getRegisteredYawlService(String yawlServiceID) {
    	try {
    		DataProxy proxy = getDataContext().retrieve( YAWLServiceReference.class, yawlServiceID, null );
    		if( proxy != null ) {
    			return (YAWLServiceReference) proxy.getData();
    		}
    	}
    	catch( YPersistenceException e ) {
    		// FIXME
    		throw new RuntimeException( e );
    	}
    	return null;
//            return (YAWLServiceReference) _yawlServices.get(yawlServiceID);
    }


    /**
     * Returns a set of YAWL service references registered in the engine.
     *
     * @return the set of current YAWL services
     */
    public abstract Set getYAWLServices() throws YPersistenceException;


    /**
     * Adds a YAWL service to the engine.<P>
     *
     * AJH - MODIFIED FOR TRANSACTIONAL PERSISTENCE
     *
     * @param yawlService
     */
    public void addYawlService(YAWLServiceReference yawlService) throws YPersistenceException {
            logger.debug("--> addYawlService: Service=" + yawlService.getURI());


            /*
             * Check if the service already exists
             * */
            DataProxy<YAWLServiceReference> proxy = getDataContext().retrieve( YAWLServiceReference.class, yawlService.getURI(), null );
            YAWLServiceReference service = null;
            if( proxy != null ) {
            	//service exists, update data
            	service = proxy.getData();
            	service.setEnabled(yawlService.getEnabled());
            	service.setDocumentation(yawlService.getDocumentation());
            	getDataContext().save( proxy );
            } else {
            	//new service, just store it
            	proxy = getDataContext().createProxy( yawlService, null );
            	getDataContext().attachProxy( proxy, yawlService, null );
            	getDataContext().save( proxy );
            }

            logger.debug("<-- addYawlService");
    }


    public Set getChildrenOfWorkItem(YWorkItem workItem) {
            return YEngine._workItemRepository.getChildrenOf(workItem.getIDString());
    }


    /**
     * Announces an enabled task to a YAWL service.  This is a classic push style
     * interaction where the Engine pushes the work item out into the
     * YAWL service.
     * PRE: the YAWL service exists and is on line.
     *
     * @param yawlService the YAWL service
     * @param item        the work item must be enabled.
     */
    protected abstract void announceEnabledTask(YAWLServiceReference yawlService, YWorkItem item);


    public abstract void announceCancellationToEnvironment(YAWLServiceReference yawlService, YWorkItem item);

    protected abstract void announceCaseCompletionToEnvironment(YAWLServiceReference yawlService, YIdentifier caseID, Document casedata);


    public Set getUsers() {
            logger.debug("--> getUsers");
            logger.debug("<-- getUsers: Returned " + _userList.getUsers().size() + " entries");

            return _userList.getUsers();
    }


    /**
     * Returns a list of the YIdentifiers objects for running cases.
     *
     * @return the case ids of the current unfinished processes.
     * @deprecated this method is not being used
     */
//    public List getRunningCaseIDs() {
//        return new ArrayList(_runningCaseIDToSpecIDMap.keySet());
//    }

    /**
     *
     */
    public String getLoadStatus(String specID) throws YPersistenceException {
        if (getSpecification(specID) != null) {
            return YSpecification._loaded;
        } else if (getSpecificationVersions(specID, null).size() > 0) {
            return YSpecification._unloaded;
        } else {
            throw new RuntimeException("SpecID [" + specID + "] is neither loaded nor unloaded.");
        }
    }

    /**
     *
     *
     * AJH - MODIFIED FOR TRANSACTIONAL PERSISTENCE
     *
     * @param serviceURI
     * @return
     * @throws YPersistenceException
     */
    public YAWLServiceReference removeYawlService(String serviceURI) throws YPersistenceException {
            /*
              INSERTED FOR PERSISTANCE
             */
//            YAWLServiceReference service = (YAWLServiceReference) _yawlServices.get(serviceURI);
            DataProxy<YAWLServiceReference> proxy = getDataContext().retrieve( YAWLServiceReference.class, serviceURI, null );
            YAWLServiceReference service = null;
            if( proxy != null ) {
            	service = proxy.getData();
            	service.setEnabled(false);
            	getDataContext().save( proxy );
            }
//            if (service != null) {
// TODO               if (isJournalising()) {
//                    logger.info("Deleting persisted entry for YAWL service " + service.getURI() + " with ID " + service.get_yawlServiceID());

//  TODO                  YPersistenceManager pmgr = new YPersistenceManager(getPMSessionFactory());
//
//                    try {
//                        pmgr.startTransactionalSession();
//                        pmgr.deleteObject(service);
//                        pmgr.commit();
//                    } catch (YPersistenceException e) {
//                        logger.fatal("Failure whilst removing YAWL service", e);
//                        throw e;
//                    }
//                }
//            }

            return service;
    }

    /**
     * Indicates if persistence is to the database.
     *
     * @return if the engine if configured to store data in persistence.
     */
// TODO   public static boolean isJournalising() {
//        return journalising;
//    }

    /**
     * Indicates if persistence is to the database.
     *
     * @param arg
     */
    public static void setPersistence(boolean arg) {
    	journalising = arg;
    }

    /**
     * Performs a diagnostic dump of the engine internal tables and state to trace.<P>
     */
    public void dump() {
            logger.debug("*** DUMP OF ENGINE STATS ***");

            try {
            	List<DataProxy> proxies = getDataContext().retrieveAll(
            			YSpecification.class, null );
	            logger.debug("\n*** DUMPING " + proxies.size() + " SPECIFICATIONS ***");
	            {
	                for( int index = 0; index < proxies.size(); index++ ) {
	                    DataProxy<YSpecification> proxy = proxies.get( index );
	                    YSpecification spec = proxy.getData();
	                	
	                    logger.debug("Entry " + (index + 1) + ":");
	                    logger.debug("    DbID           " + spec.getDbID());
	                    logger.debug("    ID             " + spec.getID());
	                    logger.debug("    Version        " + spec.getVersion());
	                    logger.debug("    Name           " + spec.getName());
	                    logger.debug("    Archived       " + spec.getArchived());
	                    logger.debug("    Beta Version   " + spec.getBetaVersion());
	                }
	            }
	            logger.debug("*** DUMP OF SPECIFICATIONS ENDS ***");
            }
            catch( YPersistenceException e ) {
            	logger.debug( "*** DUMP OF SPECIFICATIONS FAILED:", e );
            	logger.debug( "*** END OF SPECIFICATION DUMP ***" );
            }

            logger.debug("\n*** DUMPING " + _caseIDToNetRunnerMap.size() + " ENTRIES IN CASE_ID_2_NETRUNNER MAP ***");
            {
                Iterator keys = _caseIDToNetRunnerMap.keySet().iterator();
                int sub = 0;
                while (keys.hasNext()) {
                    sub++;
                    Object objKey = keys.next();
                    if (objKey == null) {
                        logger.debug("Key = NULL !!!");
                    } else {
                        YIdentifier key = (YIdentifier) objKey;
                        YNetRunner runner = (YNetRunner) _caseIDToNetRunnerMap.get(key);

                        logger.debug("Entry " + sub + " Key=" + key.getId());
                        logger.debug(("    CaseID        " + runner.getCaseID().toString()));
                        logger.debug("     YNetID        " + runner.getYNetID());
                    }
                }
            }

            logger.debug("*** DUMP OF CASE_ID_2_NETRUNNER_MAP ENDS");

            logger.debug("*** DUMP OF RUNNING CASES TO SPEC MAP STARTS ***");
            {
                Iterator keys = _runningCaseIDToSpecIDMap.keySet().iterator();
                int sub = 0;
                while (keys.hasNext()) {
                    sub++;
                    Object objKey = keys.next();

                    if (objKey == null) {
                        logger.debug("key is NULL !!!");
                    } else {
                        YIdentifier key = (YIdentifier) objKey;
                        String spec = (String) _runningCaseIDToSpecIDMap.get(key);
                        logger.debug("Entry " + sub + " Key=" + key);
                        logger.debug("    ID             " + spec);
                    }
                }
            }
            logger.debug("*** DUMP OF RUNNING CASES TO SPEC MAP ENDS ***");

            if (getWorkItemRepository() != null) {
                getWorkItemRepository().dump(logger);
            }

            logger.debug("*** DUMP OF ENGINE ENDS ***");
    }

    public static YWorkItemRepository getWorkItemRepository() {
        return YEngine._workItemRepository;
    }

// TODO   private static SessionFactory getPMSessionFactory() {
//        return factory;
//    }

    /**
     * Public interface to allow engine clients to ask the engine to store an object reference in its
     * persistent storage. It does this in its own transaction block.<P>
     *
     * @param obj
     * @throws YPersistenceException
     */
    public void storeObject(Object obj) throws YPersistenceException {
    	throw new UnsupportedOperationException( "This function is no longer supported. Use the DataContext" );
//   TODO         if (isJournalising()) {
//                YPersistenceManager pmgr = new YPersistenceManager(getPMSessionFactory());
//                pmgr.startTransactionalSession();
//                pmgr.storeObject(obj);
//                pmgr.commit();
//            }
    }

    /**
     *
     *
     * AJH: Relocated from YPersistance
     *
     * @param id
     * @throws YPersistenceException
     */
    protected void clearCase(YIdentifier id) throws YPersistenceException {
        logger.debug("--> clearCase: CaseID = " + id.getId());

//  TODO      if (journalising) {
//            clearCaseDelegate(id);
//        }

        logger.debug("<-- clearCase");
    }

    /**
     * Indicate if user interface metadata is to be generated within a tasks input XML doclet.
     *
     * @param arg
     */
    public void setGenerateUIMetaData(boolean arg)
    {
        generateUIMetaData = arg;
    }

    /**
     * Indicates if user interface metadata is to be generated within a tasks input XML doclet.
     */
    public boolean generateUIMetaData()
    {
        return generateUIMetaData;
    }

    /**
     * AJH: Stub method for testing revised persistence mechanism
     * @param caseID
     */
    private void persistCase(YIdentifier caseID) {
        logger.debug("--> persistCase: CaseID = " + caseID.getId());
        logger.debug("<-- persistCase");
    }


    /***************************************************************************/
    /** Required methods to enable exception handling service (MJA 04-07/2006) */
    /***************************************************************************/
    /*
     * Right now this method is only used in testing, to insert a mock
     * interfaceX client
     **/
    public boolean addExceptionObserver(ExceptionGateway ix) {
    	_exceptionObservers.add(ix);
    	return true;
    }
    
    public void addExceptionObserver(String observerURI){
        _exceptionObservers.add(new InterfaceX_EngineSideClient(observerURI));        
    }


    public void removeExceptionObservers() {
        _exceptionObservers = new ArrayList();
    }

    protected abstract void announceCheckWorkItemConstraints(
                                   YWorkItem item,
                                   Document data, boolean preCheck);

    protected abstract void announceCheckCaseConstraints(
                                   String specID,
                                   String caseID, String data, boolean preCheck);

    public abstract void announceCancellationToExceptionService(
                                   YIdentifier caseID);

    public abstract void announceTimeOutToExceptionService(
                                   YWorkItem item, List timeOutTaskIds);
    
    public abstract void announceServiceUnavailable(YWorkItem item, YAWLServiceReference ref);
    
    public abstract void announceServiceError(YWorkItem item, YAWLServiceReference ref);


    public boolean updateWorkItemData(String workItemID, String data) {
        YWorkItem workItem = getWorkItem(workItemID);

        if (workItem != null) {
            try {
                workItem.setData(JDOMConversionTools.stringToElement(data));
                return true ;
            }
            catch (YPersistenceException e) {
                return false ;
            }
        }
        return false ;
    }


    public boolean updateCaseData(String idStr, String data) {
        YNetRunner runner = (YNetRunner) _caseIDToNetRunnerMap.get(getCaseID(idStr));

        if (runner != null) {
            try {
                YNet net = runner.getNet();
                Element updatedVars = JDOMConversionTools.stringToElement(data);
                List<Element> vars = updatedVars.getChildren();
                for (Element var : vars) {
                    net.assignData((Element) var.clone());
                }
                return true;
            }
            catch (Exception e) {
                logger.error("Problem updating Case Data for case " + idStr, e);
            }
        }
        return false;
    }


    public void cancelWorkItem(YWorkItem workItem, boolean statusFail)  {

        try {
            if (workItem != null) {
               if (workItem.getStatus() == YWorkItem.Status.Executing) {
                   YIdentifier caseID = workItem.getCaseID().getParent() ;
                   YNetRunner runner = YEngine._workItemRepository.getNetRunner(caseID);
                   String taskID = workItem.getTaskID();
                   runner.cancelTask(taskID);
                   workItem.setStatusToDeleted(statusFail);
                   runner.continueIfPossible();
                }
            }
        }
        catch (YPersistenceException e) {
            logger.error("Failure whilst persisting workitem cancellation", e);
        }
    }

}
