package au.edu.qut.yawl.persistence.managed;

import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.Map;


public class ManagedDataObject<DataType> {
    
    private KeyedDataMapper<DataType> mapper;
    private Object key;
    
    /**
     * Utility field used by constrained properties.
     */
    private java.beans.VetoableChangeSupport vetoableChangeSupport =  new java.beans.VetoableChangeSupport(this);
    
    /**
     * Adds a VetoableChangeListener to the listener list.
     * @param l The listener to add.
     */
    public void addVetoableChangeListener(java.beans.VetoableChangeListener l) {
        
        vetoableChangeSupport.addVetoableChangeListener(l);
    }
    
    /**
     * Removes a VetoableChangeListener from the listener list.
     * @param l The listener to remove.
     */
    public void removeVetoableChangeListener(java.beans.VetoableChangeListener l) {
        
        vetoableChangeSupport.removeVetoableChangeListener(l);
    }
    
    /**
     * Getter for property data.
     * @return Value of property data.
     */
    public DataType getData() {
        return mapper.getData(key);
    }
    
    /**
     * Setter for property data.
     * @param data New value of property data.
     *
     * @throws PropertyVetoException if some vetoable listeners reject the new value
     */
    public void setData(DataType data) throws java.beans.PropertyVetoException {
        DataType oldData = mapper.getData(key);
        vetoableChangeSupport.fireVetoableChange("data", oldData, data);
    }
    
    public ManagedDataObject(Object key, KeyedDataMapper mapper, VetoableChangeListener listener) {
        this.mapper = mapper;
        this.key = key;
        this.addVetoableChangeListener(listener);
    }
    
    /**
     * Getter for property metaData.
     * @return Value of property metaData.
     */
    public Object getMetaData() {
        return mapper.getMetadata(key);
    }
    
    /**
     * Setter for property metaData.
     * @param metaData New value of property metaData.
     *
     * @throws PropertyVetoException if some vetoable listeners reject the new value
     */
    public void setMetaData(Object metaData) throws java.beans.PropertyVetoException {
        Map oldMetaData = mapper.getMetadata(key);
        vetoableChangeSupport.fireVetoableChange("metaData", oldMetaData, metaData);
    }
}
