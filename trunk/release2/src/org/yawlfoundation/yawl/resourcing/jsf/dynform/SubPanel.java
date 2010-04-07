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
import java.util.Hashtable;

/**
 * a panel layout with a few extra members
 *
 * @author: Michael Adams
 * Date: 26/02/2008
 */

public class SubPanel extends PanelLayout implements Cloneable {

    private int height ;
    private int width;
    private int top ;
    private String name ;

    private SubPanelController controller;

    // 'occurs' buttons
    private Button btnPlus;
    private Button btnMinus;

    // mapping of the tops (y-coords) of the non-panel component members of this subpanel
    private Hashtable<UIComponent, Integer> _contentTops = new Hashtable<UIComponent, Integer>() ;


    // Constructor //
    public SubPanel() { super(); }


    // Getters & Setters //

    public int getHeight() { return height; }

    public void setHeight(int height) { this.height = height; }

    public int getWidth() { return width; }

    public void setWidth(int width) { this.width = width; }

    public int getTop() { return top; }

    public void setTop(int top) { this.top = top; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public Button getBtnPlus() { return btnPlus; }

    public void setBtnPlus(Button btnPlus) { this.btnPlus = btnPlus; }

    public Button getBtnMinus() { return btnMinus; }

    public void setBtnMinus(Button btnMinus) { this.btnMinus = btnMinus; }

    public SubPanelController getController() { return controller; }

    public void setController(SubPanelController controller) {
        this.controller = controller;
    }

    public boolean isChoicePanel() {
        return getName().equals("choicePanel");
    }

    public boolean isEmpty() {
        return getChildren().isEmpty();
    }


    /***************************************************************************/

    /**
     * Calculates the screen co-ords for this panel (relative to the entire form)
     * @param maxLevel the highest depth level of all panels on this form
     */
    public void assignStyle(int maxLevel) {

        // resize width for the nested depth of this panel
        width =  DynFormFactory.PANEL_BASE_WIDTH +
                     (DynFormFactory.SUBPANEL_INSET * 2 *
                     (maxLevel - controller.getDepthlevel() + 1)) ;

        String style = String.format("position: absolute; top: %dpx; left: %dpx; height: %dpx; width: %dpx",
                                      top, DynFormFactory.SUBPANEL_INSET, height, width);

        // set user-defined background colour (if given)
        String backColour = controller.getUserDefinedBackgroundColour();
        if (backColour != null) style += "; background-color: " + backColour;
        
        setStyle(style);
        setStyleClass(controller.getSubPanelStyleClass());

        // set 'occurs' buttons in top right of panel (if they exist)
        if (btnPlus != null) {
            btnPlus.setStyle(String.format("left: %dpx", width - 21));  // 20 = btn width
            btnMinus.setStyle(String.format("left: %dpx", width - 41));
        }
    }


    public void addOccursButton(Button btn) {
        if (btn.getText().equals("+"))
           btnPlus = btn ;
        else
           btnMinus = btn ;
        this.getChildren().add(btn);
    }


    public SubPanel clone() {
        SubPanel result = new SubPanel();
        result.setTop(top);
        result.setHeight(height);
        result.setName(name);
        result.setController(controller);
        result.setStyle(this.getStyle());
        result.setStyleClass(this.getStyleClass());
        return result;
    }

    public int incTop(int amount) {
        top += amount ;
        return top;
    }

    public int decTop(int amount) {
        top -= amount ;
        return top;
    }

    public int incHeight(int amount) {
        height += amount;
        return height;
    }


    public void enableOccursButtons(boolean enable) {
        if (btnPlus != null) btnPlus.setDisabled(! enable);
        if (btnMinus != null) btnMinus.setDisabled(! enable);
    }

    /*********************************************************************/

    public int getTop(UIComponent component) {
        Integer result = _contentTops.get(component);
        if (result == null)
            return 0 ;
        else
            return result;
    }

    public void setContentTops(DynFormComponentList content, int top) {
        for (UIComponent component : content) {
            _contentTops.put(component, top);
        }
    }


    public void setContentTop(UIComponent component, int top) {
        _contentTops.put(component, top);
    }

    
    public int adjustTopForChoiceContainer(int top) {
        if (_contentTops.isEmpty())
            return top - DynFormFactory.Y_CHOICE_DECREMENT;
        else
            return top;
    }

    /**
     * reset the tops of all simple components lower than the top specified
     * @param top the y-coord below which components should be moved down
     * @param adjustment how much to move them down by
     */
    public void incComponentTops(int top, int adjustment) {
        for (UIComponent component : _contentTops.keySet()) {
            int oldTop = _contentTops.get(component);
            if (oldTop > top) {
                resetTopStyle(component, oldTop + adjustment);
                _contentTops.put(component, oldTop + adjustment);
            }
        }
    }

    // Private Methods //

    /**
     * Sets the top (y-coord) of a component (via its style setting)
     * @param component the componnt to reposition
     * @param top the new top value
     */
    private void resetTopStyle(UIComponent component, int top) {
        String style = String.format("top: %dpx", top) ;
        if (component instanceof Label)
            ((Label) component).setStyle(style);
        else if (component instanceof TextField)
            ((TextField) component).setStyle(style);
        else if (component instanceof Calendar)
            ((Calendar) component).setStyle(style);
        else if (component instanceof Checkbox)
            ((Checkbox) component).setStyle(style);
    }

}
