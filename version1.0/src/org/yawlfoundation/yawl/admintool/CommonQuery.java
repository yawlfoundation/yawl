package org.yawlfoundation.yawl.admintool;

import java.util.List;
import java.util.LinkedList;

public class CommonQuery {

    /*
      list of query filters
     */
    List queryfilters = new LinkedList();

    String name = "";

    public void setName(String name) {
	this.name = name;
    }
    
    public String getName() {
	return name;
    }
    
    public CommonQuery(String name) {
	this.name = name;
    }	

    public void add(QueryFilter filter) {
	queryfilters.add(filter);
    }

    public int size() {
	return queryfilters.size();
    }

    public QueryFilter get(int i) {
	return (QueryFilter) queryfilters.get(i);
    }	

}