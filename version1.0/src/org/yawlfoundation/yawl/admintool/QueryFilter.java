package org.yawlfoundation.yawl.admintool;

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