/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are commited to improving
 * workflow technology.
 */

package au.edu.qut.yawl.worklet.support;

import au.edu.qut.yawl.worklet.rdr.*;
import au.edu.qut.yawl.worklet.WorkletService;

import au.edu.qut.yawl.worklist.model.WorkItemRecord;
import au.edu.qut.yawl.util.JDOMConversionTools;

import org.jdom.*;
import org.jdom.output.*;

import org.apache.log4j.Logger;

import java.util.*;
import java.io.*;

/** The WorkletRecord class maintains a generic dataset for derived classes
 *  that manage a currently running worklet for a 'parent' process.
 *
 * It is implemented by selection.CheckedOutChildItem and exception.HandlerRunner
 *
 *  @author Michael Adams
 *  BPM Group, QUT Australia
 *  m3.adams@qut.edu.au
 *  @version 0.8, 04-09/2006
 */


public class WorkletRecord {

    protected CaseMap _runners = new CaseMap() ;  // executing caseid <==> name mapping
    protected WorkItemRecord _wir ;          // the child workitem
    protected Element _datalist ;            // data passed to this workitem
    protected RdrNode[] _searchPair ;        // rule pair returned from search
    protected int _reasonType ;              // why the worklet was raised
    protected static Logger _log ;           // log file for debug messages

    protected String _persistID ;            // unique id field for persistence
    protected boolean _hasPersisted = false; // set to true when row is inserted
    protected String _searchPairStr ;        // intermediate str for persistence
    protected String _wirStr ;               // intermediate str for persistence
    protected String _runningCaseIdStr ;     // intermediate str for persistence
    protected String _runningWorkletStr ;     // intermediate str for persistence


    /**
     *  Constructs a basic WorkletRecord
     */
    public WorkletRecord() {}

//===========================================================================//

    // SETTERS //

    public void setItem(WorkItemRecord w) {
        _wir = w ;
        if (_hasPersisted) persistThis();
    }


    public void setDatalist(Element d) {
        _datalist = d ;
    }


    public void addRunner(String caseID, String wName) {
        _runners.addCase(caseID, wName);
        if (_hasPersisted) persistThis();
    }


    public void setSearchPair(RdrNode[] pair) {
        _searchPair = pair ;
        if (_hasPersisted) persistThis();
    }


    public void setExType(int xType) {
        _reasonType = xType ;
        if (_hasPersisted) persistThis();
    }


    public void removeRunnerByCaseID(String caseID) {
        _runners.removeCase(caseID) ;
        if (_hasPersisted) persistThis();
    }

    public void removeRunnerByWorkletName(String wName) {
        _runners.removeWorklet(wName);
        if (_hasPersisted) persistThis();
    }

    public void removeAllCases() {
        _runners.removeAllCases();
    }


 //===========================================================================//

    //*** GETTERS ***//

    public String getWorkletName(String caseID) {
        return _runners.getWorkletName(caseID) ;
    }

    public String getWorkletCaseID(String wName) {
        return _runners.getCaseID(wName) ;
    }

    public Set getWorkletList() {
        return _runners.getAllWorkletNames() ;
    }

    public HashMap getCaseMapAsCSVList() {
        return _runners.getCaseMapAsCSVLists();
    }


     public WorkItemRecord getItem() {
        return _wir ;
    }


    public String getItemId() {
        return _wir.getID() ;
    }


    public RdrNode[] getSearchPair() {
        return _searchPair ;
    }


    public Set getRunningCaseIds() {
        return _runners.getAllCaseIDs() ;                      // the worklet case ids
    }


    public Element getDatalist() {
        return _datalist ;
    }

    public String getCaseID() {
        return _wir.getCaseID();            // the originating workitem's case id
    }

    public String getSpecID() {
        return _wir.getSpecificationID();      // i.e. of the originating workitem
    }

    public int getReasonType() {
        return _reasonType ;
    }

    public boolean hasRunningWorklet() {
        return _runners.hasRunningWorklets();
    }

//===========================================================================//

    // ACCESSORS & MUTATORS FOR PERSISTENCE //

    protected void set_wirStr(String s) {
        _wirStr = s;
    }

    public String get_wirStr() {
        if (_wirStr == null)

            // only if there's a workitem for this record, create its string
            if (_wir != null) _wirStr = _wir.toXML() ;
        return _wirStr ;
    }


    protected void set_runningCaseIdStr(String s) {
        _runningCaseIdStr = s;
    }

    protected String get_runningCaseIdStr() {
        if (_runners != null) {
            HashMap cases = _runners.getCaseMapAsCSVLists();
            _runningCaseIdStr = (String) cases.get("caseIDs");
        }
         return _runningCaseIdStr ;
    }


    protected void set_runningWorkletStr(String s) {
        _runningWorkletStr = s ;
    }

    protected void set_reasonType(int i) {
        _reasonType = i ;
    }

    protected String get_runningWorkletStr() {
        if (_runners != null) {
            HashMap cases = _runners.getCaseMapAsCSVLists();
            _runningWorkletStr = (String) cases.get("workletNames");
        }
        return _runningWorkletStr ;
    }

    protected int get_reasonType() {
        return _reasonType ;
    }


    protected void set_searchPairStr(String s) {
        _searchPairStr = s;
    }

    public String get_searchPairStr() {
        if (_searchPair != null)
            _searchPairStr = SearchPairToString(_searchPair);
        return _searchPairStr ;
    }


    private String SearchPairToString(RdrNode[] pair) {
        String result = "";
        if (pair[0] != null) {
            result = pair[0].toXML() + ":::" + pair[1].toXML();
        }
        return result ;
    }


    public void rebuildSearchPair(String specID, String taskID) {

        RdrSet ruleSet = new RdrSet(specID);                  // make a new set
        RdrTree tree ;

        switch (_reasonType) {
            case WorkletService.XTYPE_CASE_PRE_CONSTRAINTS :
            case WorkletService.XTYPE_CASE_POST_CONSTRAINTS :
            case WorkletService.XTYPE_CASE_EXTERNAL_TRIGGER :
                                tree = ruleSet.getTree(_reasonType) ; break ;
            default :
                                tree = ruleSet.getTree(_reasonType, taskID) ;
        }

        if (tree != null)
            _searchPair = RdrConversionTools.stringToSearchPair(_searchPairStr, tree);
    }

    public void restoreCaseMap() {
        _runners.restore(_runningCaseIdStr, _runningWorkletStr) ;
    }

    public void ObjectPersisted() {
        _hasPersisted = true ;
    }


    protected void persistThis() {
        DBManager dbMgr = DBManager.getInstance(false);
        if ((dbMgr != null) && dbMgr.isPersisting())
             dbMgr.persist(this, DBManager.DB_UPDATE);
    }


//===========================================================================//

    //*** SAVE METHODS **//

    /**
     * writes the node id's for the nodes returned from the rdr search
     * and the data for the current workitem, to a file for later
     * input into the 'add rule' process, if required
     */
    public void saveSearchResults() {

        // create the required components for the output file
        Document doc = new Document(new Element("searchResult")) ;
        Element eLastNode = new Element("lastNode") ;
        Element eSatisfied = new Element("satisfied") ;
        Element eTested = new Element("tested") ;
        Element eId = new Element("id") ;
        Element eSpecid = new Element("specid") ;
        Element eTaskid = new Element("taskid") ;
        Element eCaseid = new Element("caseid") ;
        Element eCaseData = new Element("casedata") ;
        Element eReason = new Element("extype") ;
        Element eWorklets = new Element("worklets") ;

        try {
            // transfer the workitem's data items to the file
            List dataItems = _datalist.getChildren() ;
            Iterator itr = dataItems.iterator();
            while (itr.hasNext()) {
                Element e = (Element) itr.next() ;
                eCaseData.addContent((Element) e.clone());
            }

            //set values for the workitem identifiers
            eId.setText(_wir.getID()) ;
            eSpecid.setText(_wir.getSpecificationID());
            eTaskid.setText(WorkletService.getInstance().getDecompID(_wir));
            eCaseid.setText(_wir.getCaseID());
            eReason.setText(String.valueOf(_reasonType));

            // add the worklet names and case ids
            Iterator witr = _runners.getAllWorkletNames().iterator() ;
            while (witr.hasNext()) {
                Element eWorkletName = new Element("workletName") ;
                Element eRunningCaseId = new Element("runningcaseid") ;
                Element eWorklet = new Element("worklet");
                String wName = (String) witr.next();
                eWorkletName.setText(wName) ;
                eRunningCaseId.setText(_runners.getCaseID(wName)) ;
                eWorklet.addContent(eWorkletName);
                eWorklet.addContent(eRunningCaseId);
                eWorklets.addContent(eWorklet);
            }

            // add the nodeids to the relevent elements
            eSatisfied.setText(_searchPair[0].getNodeIdAsString()) ;
            eTested.setText(_searchPair[1].getNodeIdAsString()) ;
            eLastNode.addContent(eSatisfied) ;
            eLastNode.addContent(eTested) ;

            // add the elements to the document
            Element root = doc.getRootElement();
            root.addContent(eId) ;
            root.addContent(eSpecid);
            root.addContent(eTaskid);
            root.addContent(eCaseid);
            root.addContent(eWorklets) ;
            root.addContent(eReason);
            root.addContent(eLastNode) ;
            root.addContent(eCaseData) ;

             // create the output file
             saveDocument(createFileName(), doc) ;
         }
        catch (IllegalAddException iae) {
            WorkletRecord._log.error("Exception when adding content", iae) ;
        }
     }


     /** saves a JDOM Document to a file */
     protected void saveDocument(String fileName, Document doc)   {
        try {
           FileOutputStream fos = new FileOutputStream(fileName);
           XMLOutputter xop = new XMLOutputter(Format.getPrettyFormat());
           xop.output(doc, fos);
           fos.flush();
           fos.close();
      }
      catch (IOException ioe){
          _log.error("IO Exeception in saving Document to file", ioe) ;
      }
   }

    protected String createFileName() {
        // build file name from workitem and worklet identifiers
        String extn = ".xws";
        String counter = "" ;
        int i = 0 ;
        StringBuffer fName = new StringBuffer(Library.wsSelectedDir) ;
        fName.append(getCaseID()) ;                         // begin with caseID
        fName.append("_") ;
        fName.append(getSpecID()) ;                         // then spec id
        fName.append("_") ;
        fName.append(WorkletService.getShortXTypeString(_reasonType));

        // if item-level, add the task name also
        if (_wir != null) {
            fName.append("_");
            fName.append(_wir.getTaskID());
        }

        String result = fName.toString();
        while (Library.fileExists(result + counter + extn)) {
            counter = "_" + String.valueOf(++i);
        }

        return (result + counter + extn);
    }

//===========================================================================//

    /** returns a String representation of current WorkletRecord */
    public String toString() {
        StringBuffer s = new StringBuffer("##### WORKLET RECORD #####") ;
        s.append(Library.newline);
        s.append(toStringSub());

        return s.toString() ;
    }

    public String toStringSub() {
        StringBuffer s = new StringBuffer() ;
        String wirStr = (_wir != null)? _wir.toXML() : "null";
        Library.appendLine(s, "WORKITEM", wirStr);

        String data = JDOMConversionTools.elementToStringDump(_datalist);
        Library.appendLine(s, "DATALIST", data);

        Library.appendLine(s, "WORKLET NAMES", _runners.getWorkletCSVList());
        Library.appendLine(s, "RUNNING CASE IDs", _runners.getCaseIdCSVList());

        s.append("SEARCH PAIR: ");
        if (_searchPair != null) {
            s.append(Library.newline);
            Library.appendLine(s, "Last Satisfied Node", _searchPair[0].toXML());
            Library.appendLine(s, "Last Tested Node", _searchPair[1].toXML());
        }
        else s.append("null");

        s.append(Library.newline);
        return s.toString() ;
    }

//===========================================================================//
//===========================================================================//

}