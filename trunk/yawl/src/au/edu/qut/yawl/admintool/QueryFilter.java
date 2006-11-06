/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.admintool;

public class QueryFilter {
    public String query = "";
    
    boolean LessThan = false;
    long intervaltime = -1;

    String function = "";
    String groupbyvalue = "";

    String intervaloption = "";
    String granularity = "Seconds";
    String column = "created";

    public void setQuery(String query) {
	this.query = query;
    }

    public String getQuery() {
	return query;
    }	
    
    
}