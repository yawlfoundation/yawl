/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing.jsf.dynform;

import com.sun.rave.web.ui.component.*;

import javax.faces.component.UIComponent;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * Author: Michael Adams
 * Creation Date: 7/08/2008
 */
public class SubPanelCloner {

    private DynFormFactory _factory ;
    private String _rbGroupID;

    public SubPanelCloner() { }

    public SubPanel clone(SubPanel panel, DynFormFactory factory, String id) {
        _factory = factory;
        _rbGroupID = id;
        SubPanel cloned = cloneSubPanel(panel);
        resolveChildControllers(cloned);
        return cloned;
    }

    private SubPanel cloneSubPanel(SubPanel panel) {
        SubPanel newPanel = panel.clone() ;
        String name = newPanel.getName();
        newPanel.setId(_factory.createUniqueID("sub" + name));

        // clone panel content
        Hashtable<UIComponent, Integer> tops = new Hashtable<UIComponent, Integer>();
        List content = cloneContent(panel, tops);
        if (! content.isEmpty()) {
            newPanel.getChildren().addAll(content) ;
            for (UIComponent component : tops.keySet()) {
                newPanel.setContentTop(component, tops.get(component));
            }
        }
        if (newPanel.getController().canVaryOccurs()) {
            newPanel.addOccursButton(_factory.makeOccursButton(name, "+"));
            newPanel.addOccursButton(_factory.makeOccursButton(name, "-"));
            newPanel.getBtnPlus().setStyle(panel.getBtnPlus().getStyle());
            newPanel.getBtnMinus().setStyle(panel.getBtnMinus().getStyle());
        }
        return newPanel ;
    }

    private List cloneContent(SubPanel panel, Hashtable<UIComponent, Integer> tops) {
        List content = panel.getChildren();
        List result = new ArrayList() ;
        for (Object obj : content) {
            if (obj instanceof SubPanel)
                result.add(cloneSubPanel((SubPanel) obj)) ;               // recurse
            else if (! (obj instanceof Button))  {
                UIComponent component = (UIComponent) obj;
                List<UIComponent> newContent = cloneSimpleComponent(component, panel) ;
                if (newContent != null) {
                    for (UIComponent cloned : newContent) {
                        tops.put(cloned, panel.getTop(component));
                    }
                    result.addAll(newContent) ;
                }
            }
        }
        return result ;
    }


    private List<UIComponent> cloneSimpleComponent(UIComponent component, SubPanel panel) {
        List<UIComponent> result = new ArrayList<UIComponent>();
        UIComponent newComponent = null ;

        if (component instanceof StaticText)
            newComponent = cloneStaticText(component) ;
        else if (component instanceof RadioButton)
            newComponent = cloneRadioButton(component);
        else if (component instanceof Label) {
            Label newLabel = (Label) cloneLabel(component);
            String labelFor = ((Label) component).getFor();
            UIComponent compFor = panel.findComponent(labelFor);
            if (compFor instanceof TextField)
                newComponent = cloneTextField(compFor);
            else if (compFor instanceof Calendar)
                newComponent = cloneCalendar(compFor);
            else if (compFor instanceof Checkbox)
                newComponent = cloneCheckbox(compFor);
            else if (compFor instanceof DropDown)
                newComponent = cloneDropDown(compFor);

            if (newComponent != null) {
                newLabel.setFor(newComponent.getId());
                result.add(newLabel);
            }
        }
        if (newComponent != null) {
            result.add(newComponent);
            return result ;
        }
        else return null ;
    }


    public UIComponent cloneLabel(UIComponent label) {
        Label oldLabel = (Label) label ;
        Label newLabel = new Label() ;
        newLabel.setText(oldLabel.getText());
        newLabel.setId(_factory.createUniqueID(oldLabel.getId())) ;
        newLabel.setRequiredIndicator(oldLabel.isRequiredIndicator());
        newLabel.setStyle(oldLabel.getStyle());
        newLabel.setStyleClass(oldLabel.getStyleClass());
        newLabel.setRequiredIndicator(oldLabel.isRequiredIndicator()) ;
        return newLabel;
    }


    public UIComponent cloneTextField(UIComponent field) {
        TextField oldField = (TextField) field ;
        TextField newField = new TextField() ;
        newField.setText(oldField.getText());
        newField.setId(_factory.createUniqueID(oldField.getId()));
        newField.setRequired(oldField.isRequired());
        newField.setDisabled(oldField.isDisabled());
        newField.setStyleClass(oldField.getStyleClass());
        newField.setStyle(oldField.getStyle());
        newField.setToolTip(oldField.getToolTip());
        _factory.addClonedFieldToTable(oldField, newField);      // for later validation
        return newField;
    }


    public UIComponent cloneCalendar(UIComponent field) {
        Calendar oldField = (Calendar) field ;
        Calendar newField = new Calendar() ;
        newField.setDateFormatPatternHelp("");
        newField.setId(_factory.createUniqueID(oldField.getId()));
        newField.setSelectedDate(oldField.getSelectedDate());
        newField.setDisabled(oldField.isDisabled());
        newField.setRequired(oldField.isRequired());
        newField.setColumns(oldField.getColumns());
        newField.setMinDate(oldField.getMinDate());
        newField.setMaxDate(oldField.getMaxDate());       
        newField.setStyleClass(oldField.getStyleClass());
        newField.setStyle(oldField.getStyle()) ;
        return newField ;
    }


    public UIComponent cloneCheckbox(UIComponent field) {
        Checkbox oldCbox = (Checkbox) field ;
        Checkbox newCbox = new Checkbox() ;
        newCbox.setId(_factory.createUniqueID(oldCbox.getId()));
        newCbox.setSelected(oldCbox.isChecked()) ;
        newCbox.setRequired(oldCbox.isRequired());
        newCbox.setDisabled(oldCbox.isDisabled());
        newCbox.setStyleClass(oldCbox.getStyleClass());
        newCbox.setStyle(oldCbox.getStyle()) ;
        return newCbox ;
    }

    
    public UIComponent cloneDropDown(UIComponent field) {
        DropDown oldDrop = (DropDown) field ;
        DropDown newDrop = new DropDown() ;
        newDrop.setId(_factory.createUniqueID(oldDrop.getId()));
        newDrop.setStyleClass(oldDrop.getStyleClass());
        newDrop.setStyle(oldDrop.getStyle()) ;
        newDrop.setItems(oldDrop.getItems());
        newDrop.setSelected(oldDrop.getSelected());
        return newDrop ;
    }


    public RadioButton cloneRadioButton(UIComponent field) {
        RadioButton oldRadio = (RadioButton) field;
        RadioButton newRadio = new RadioButton();
        newRadio.setId(_factory.createUniqueID(oldRadio.getId()));
        newRadio.setName(oldRadio.getName() + _rbGroupID);                // new rb group
        newRadio.setSelected(oldRadio.getSelected());
        newRadio.setStyle(oldRadio.getStyle());
        newRadio.setStyleClass(oldRadio.getStyleClass());
        return newRadio;
    }


    public UIComponent cloneStaticText(UIComponent field) {
        StaticText oldStatic = (StaticText) field ;
        StaticText newStatic = new StaticText() ;
        newStatic.setId(_factory.createUniqueID(oldStatic.getId()));
        newStatic.setText(oldStatic.getText());
        newStatic.setStyleClass(oldStatic.getStyleClass());
        return newStatic;
    }


    private void resolveChildControllers(SubPanel panel) {
        Map<String, SubPanelController> processed =
                new Hashtable<String, SubPanelController>();

        for (Object component : panel.getChildren()) {
            if (component instanceof SubPanel) {          // child panel
                SubPanel childPanel = (SubPanel) component;
                String name = childPanel.getName();
                SubPanelController controller = processed.get(name);
                if (controller != null) {
                    controller = processed.get(name) ;
                }
                else {
                    controller = childPanel.getController().clone();
                    processed.put(name, controller);
                }
                controller.storeSubPanel(childPanel);
                if (controller.canVaryOccurs()) controller.setOccursButtonsEnablement();
                childPanel.setController(controller);
                resolveChildControllers(childPanel);                   // recurse
            }
        }
        if (! processed.isEmpty()) _factory.addSubPanelControllerMap(processed);
    }

}
