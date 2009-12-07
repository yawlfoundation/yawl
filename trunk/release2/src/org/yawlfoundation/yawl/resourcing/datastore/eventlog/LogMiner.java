/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */

package org.yawlfoundation.yawl.resourcing.datastore.eventlog;

import org.apache.log4j.Logger;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.resourcing.datastore.persistence.Persister;
import org.yawlfoundation.yawl.util.StringUtil;

import java.util.List;

/**
 * An API to retrieve data from the resource service's event logs
 * and pass it back as XML.
 *
 * Create Date: 16/12/2008
 *
 *  @author Michael Adams
 *  @version 2.0
 */

public class LogMiner {

    private static LogMiner _me ;
    private Persister _reader ;
    private static final Logger _log = Logger.getLogger(LogMiner.class);

    // some error messages
    private final String _exErrStr = "<failure>Unable to retrieve data.</failure>";
    private final String _pmErrStr = "<failure>Error connecting to database.</failure>";
    private final String _noRowsStr = "<failure>No rows returned.</failure>";


    // CONSTRUCTOR - called from getInstance() //

    private LogMiner() {
        _reader = Persister.getInstance();
    }

    public static LogMiner getInstance() {
        if (_me == null) _me = new LogMiner();
        return _me ;
    }


    /*****************************************************************************/

    /**
     * @param specID the specification id to get the case eventids for
     * @return the set of all case ids for the specID passed
     */
    public String getWorkItemDurationsForParticipant(YSpecificationID specID,
                                                     String taskName, String participantID) {
        String result ;
        List rows ;
        if (_reader != null) {
            String keyField = (specID.getIdentifier() != null) ? "identifier" : "uri";
            StringBuilder template = new StringBuilder("FROM ResourceEvent AS re ");
            template.append("WHERE re._specID.%s='%s' ")
                    .append("AND re._specID.version.version='%s' ")
                    .append("AND re._taskID='%s' ")
                    .append("AND re._participantID='%s' ")
                    .append("ORDER BY re._itemID, re._timeStamp");

            String query = String.format(template.toString(), keyField, specID.getKey(),
                                   specID.getVersionAsString(), taskName, participantID);

            rows = _reader.execQuery(query) ;
            if (rows != null) {
                StringBuilder xml = new StringBuilder() ;
                String currentItemID = "";
                xml.append(String.format(
                        "<workitems specID=\"%s\" taskName=\"%s\" participantID=\"%s\">",
                        specID.toString(), taskName, participantID));
                for (Object o : rows) {
                    ResourceEvent event = (ResourceEvent) o ;
                    if (! event.get_itemID().equals(currentItemID)) {
                        if (! "".equals(currentItemID)) {
                            xml.append("</workitem>");
                        }
                        currentItemID = event.get_itemID();
                        xml.append(String.format("<workitem ID=\"%s\">", currentItemID));
                    }
                    xml.append("<event>");
                    xml.append(StringUtil.wrap(event.get_event(), "type")) ;
                    xml.append(StringUtil.wrap(String.valueOf(event.get_timeStamp()), "time")) ;
                    xml.append("</event>");
                }
                xml.append("</workitem></workitems>");
                result = xml.toString();
            }
            else result = _noRowsStr ;

        }
        else result = _pmErrStr ;

        return result ;
    }

    /*****************************************************************************/

}