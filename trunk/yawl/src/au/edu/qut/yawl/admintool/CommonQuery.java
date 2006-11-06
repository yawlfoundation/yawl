/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.admintool;

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