package org.yawlfoundation.yawl.editor.ui.properties;

import com.l2fprod.common.propertysheet.Property;
import org.yawlfoundation.yawl.editor.ui.YAWLEditor;

import javax.swing.*;
import java.awt.*;
import java.beans.*;

/**
 * Binds a Properties Bean to a set of property descriptors and handles update events
 * between them
 * @author Michael Adams
 * @date 2/07/12
 */
public class Binder implements PropertyChangeListener {

    private YPropertiesBean _bean;
    private YBeanInfo _beanInfo;
    private final YPropertySheet _sheet;


    public Binder(YPropertiesBean bean, YBeanInfo beanInfo) {
        _bean = bean;
        _beanInfo = beanInfo;
        _sheet = YAWLEditor.getInstance().getPropertySheet();
        _sheet.getTable().setBackground(Color.WHITE);
        adjustForReadOnly(bean, beanInfo);
        _sheet.setProperties(beanInfo.getPropertyDescriptors());
        loadBasicProperties(beanInfo);
        _sheet.readFromObject(bean);
        _sheet.addPropertySheetChangeListener(this);
    }


    public void propertyChange(PropertyChangeEvent event) {
        Property property = (Property) event.getSource();
        updateReadOnly(event);
        try {
            if (property.getParentProperty() != null) {
                property = writeParentProperty(property);
            }
            else property.writeToObject(_bean);
        }
        catch (RuntimeException e) {

            // handle PropertyVetoException and restore previous value
            if (e.getCause() instanceof PropertyVetoException) {
                UIManager.getLookAndFeel().provideErrorFeedback(_sheet);
                property.setValue(event.getOldValue());
            }
        }
    }


    public YPropertiesBean getBoundBean() { return _bean; }


    public void unbind() {
        _beanInfo = null;
        _sheet.removePropertySheetChangeListener(this);
        _sheet.setProperties(new Property[0]);
        _sheet.getTable().setBackground(YAWLEditor.getInstance().getBackground());
    }


    private void loadBasicProperties(BeanInfo beanInfo) {
        if (beanInfo instanceof CellBeanInfo) {
            for (Property property : ((CellBeanInfo) beanInfo).getBasicProperties()) {
                _sheet.addProperty(property);
            }
        }
    }


    private Property writeParentProperty(Property property) {
        Property child = property;
        while (property.getParentProperty() != null) property = property.getParentProperty();
        if (property.getType() == Point.class) {
            Point point = new Point();
            if (child.getName().equals("X")) {
                point.setLocation((Double) child.getValue(),
                        ((Point) property.getValue()).getY());
            }
            else {
                point.setLocation(((Point) property.getValue()).getX(),
                        (Double) child.getValue());
            }
            property.setValue(point);
        }
        return property;
    }


    private void adjustForReadOnly(YPropertiesBean bean, YBeanInfo beanInfo) {
        if (((NetProperties) bean).isRootNet()) {
            for (PropertyDescriptor descriptor : beanInfo.getPropertyDescriptors()) {
                if (descriptor.getName().equals("RootNet")) {
                    try {
                        descriptor.setWriteMethod(null);
                    }
                    catch (IntrospectionException ie) {
                        //
                    }
                    break;
                }
            }
        }
    }


    private void updateReadOnly(PropertyChangeEvent event) {
        Property property = (Property) event.getSource();
        String name = property.getName();
        if (name.equals("split")) {
            _sheet.setReadOnly("splitPosition", event.getNewValue().equals("None"));
        }
        else if (name.equals("join")) {
            _sheet.setReadOnly("joinPosition", event.getNewValue().equals("None"));
        }
        else if (name.equals("Decomposition")) {
            _sheet.setReadOnly("Timer", event.getNewValue().equals("None"));
            _sheet.setReadOnly("CustomForm", event.getNewValue().equals("None"));
        }
    }


}
