package org.yawlfoundation.yawl.resourcing.jsf.dynform;

import com.sun.rave.web.ui.component.*;

import javax.faces.component.UIComponent;
import java.util.List;
import java.util.ArrayList;

/**
 * Author: Michael Adams
 * Creation Date: 7/08/2008
 */
public class SubPanelCloner {

    private DynFormFactory _factory ;

    public SubPanelCloner() { }

    public SubPanel clone(SubPanel panel, DynFormFactory factory) {
        _factory = factory;
        return cloneSubPanel(panel);
    }

        private SubPanel cloneSubPanel(SubPanel panel) {
        SubPanel newPanel = panel.clone() ;
        String name = newPanel.getName();
        newPanel.setId(_factory.createUniqueID("sub" + name));

        // clone panel content
        List content = cloneContent(panel);
        if (! content.isEmpty())
            newPanel.getChildren().addAll(content) ;

        if (newPanel.getController().canVaryOccurs()) {
            newPanel.addOccursButton(_factory.makeOccursButton(name, "+"));
            newPanel.addOccursButton(_factory.makeOccursButton(name, "-"));
            newPanel.getBtnPlus().setStyle(panel.getBtnPlus().getStyle());
            newPanel.getBtnMinus().setStyle(panel.getBtnMinus().getStyle());
        }
        return newPanel ;
    }

    private List cloneContent(SubPanel panel) {
//        Map<Object, Integer> tops = new HashMap<Object, Integer>();
        List content = panel.getChildren();
        List result = new ArrayList() ;
        for (Object obj : content) {
            if (obj instanceof SubPanel)
                result.add(cloneSubPanel((SubPanel) obj)) ;               // recurse
            else if (! (obj instanceof Button))  {
                List<UIComponent> newContent = cloneSimpleComponent((UIComponent) obj, panel) ;
//                int top = panel.getController().getHeight((UIComponent) obj);
//                tops.put(newComponent, top);
                if (newContent != null) result.addAll(newContent) ;
            }
        }

        // clone the controller for contents
//        if (! result.isEmpty()) {
//            SubPanelController newController = panel.getController().clone();
//            for (Object obj : result) {
//                if (obj instanceof SubPanel) {
//                   newController.storeSubPanel(((SubPanel) obj)) ;
//                }
//                else
//                   newController.addSimpleContent((UIComponent) obj, tops.get(obj));
//            }
//            subPanelSet.add(newController);
//        }
        return result ;
    }


    private List<UIComponent> cloneSimpleComponent(UIComponent component, SubPanel panel) {
        List<UIComponent> result = new ArrayList<UIComponent>();
        UIComponent newComponent = null ;

        if (component instanceof StaticText)
            newComponent = cloneStaticText(component) ;
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
//            else
//                result = cloneReadOnlyField(component);

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
        return newField;
    }

    public UIComponent cloneCalendar(UIComponent field) {
        Calendar oldField = (Calendar) field ;
        Calendar newField = new Calendar() ;
        newField.setId(_factory.createUniqueID(oldField.getId()));
        newField.setDateFormatPatternHelp("");
        newField.setDisabled(oldField.isDisabled());
        newField.setRequired(oldField.isRequired());
        newField.setColumns(oldField.getColumns());
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


    public UIComponent cloneStaticText(UIComponent field) {
        StaticText oldStatic = (StaticText) field ;
        StaticText newStatic = new StaticText() ;
        newStatic.setId(_factory.createUniqueID(oldStatic.getId()));
        newStatic.setText(oldStatic.getText());
        newStatic.setStyleClass(oldStatic.getStyleClass());
        return newStatic;
    }


    private int getIndexOf(UIComponent parent, SubPanel panel) {
        List components = parent.getChildren();
        for (int i = 0; i < components.size(); i++) {
            UIComponent component = (UIComponent) components.get(i);
            if (component == panel) return i;
        }
        return -1 ;                 // not found
    }

    private void insertSubPanel(UIComponent parent, SubPanel panel, int i) {
        if (i < parent.getChildCount())
            parent.getChildren().add(i, panel);
        else
            parent.getChildren().add(panel);
    }
}
