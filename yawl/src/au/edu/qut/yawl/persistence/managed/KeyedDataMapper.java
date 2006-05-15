/*
 * KeyedDataMapper.java
 *
 * Created on April 24, 2006, 10:10 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package au.edu.qut.yawl.persistence.managed;

import java.util.Map;

/**
 * 
 * @author SandozM
 */
public interface KeyedDataMapper<DataType> {
    public DataType getData(Object mapKey);
    public Map getMetadata(Object mapKey);
    public boolean contains(Object mapKey);
}
