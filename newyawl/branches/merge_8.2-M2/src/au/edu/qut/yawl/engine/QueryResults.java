/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package au.edu.qut.yawl.engine;

import net.sf.hibernate.Query;
import net.sf.hibernate.Session;


/**
 * <<DESCRIPTION HERE>>.
 *
 * @author Andrew Hastie
 *         Creation Date: 29-Jun-2005
 */
public class QueryResults {
    private Session session = null;
    private Query query = null;

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public Query getQuery() {
        return query;
    }

    public void setQuery(Query query) {
        this.query = query;
    }
}
