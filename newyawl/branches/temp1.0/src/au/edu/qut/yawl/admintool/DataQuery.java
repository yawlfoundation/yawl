package au.edu.qut.yawl.admintool;

public class DataQuery {
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