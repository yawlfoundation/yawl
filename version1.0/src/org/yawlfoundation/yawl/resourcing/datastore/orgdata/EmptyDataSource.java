package org.yawlfoundation.yawl.resourcing.datastore.orgdata;

/**
 * This class provides a default DataSource object to the ResourceManager in the event
 * that there is no actual Datasource set and/or initialised.
 *
 * Author: Michael Adams
 * Date: 10/04/2008
 */
public class EmptyDataSource extends DataSource {

    ResourceDataSet _rds;

    public EmptyDataSource() {
        _rds = new ResourceDataSet();
    }

    public ResourceDataSet getDataSource() {
        return _rds ;
    }

    public ResourceDataSet loadResources() {
        return null;
    }

    public void update(Object obj) { }

    public void delete(Object obj) { }

    public String insert(Object obj) {
        return null; 
    }
}
