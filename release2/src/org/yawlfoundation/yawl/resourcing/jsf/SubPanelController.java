/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing.jsf;

import com.sun.rave.web.ui.component.Calendar;
import com.sun.rave.web.ui.component.Checkbox;
import com.sun.rave.web.ui.component.Label;
import com.sun.rave.web.ui.component.TextField;

import javax.faces.component.UIComponent;
import java.util.*;

/**
 * Manages a set of 'cloned' panels on a dynamic form
 *
 * Author: Michael Adams
 * Date: 21/02/2008
 */

public class SubPanelController {

    private int _minOccurs ;
    private int _maxOccurs ;
    private int _currOccurs ;            // current display count (min <= curr <= max)
    private int _depthlevel ;            // the nested level of this panel set 
    private String _name;

    // mapping of the tops (y-coords) of the non-panel component members of the subpanels
    // controlled by ths controller
    private Hashtable<UIComponent, Integer> _contentTops = new Hashtable<UIComponent, Integer>() ;

    // the list of subpanel instances (all instance of the same subpanel)
    private List<SubPanel> _panelList = new ArrayList<SubPanel>();

    public SubPanelController() {}

    public SubPanelController(SubPanel panel, String minOccurs, String maxOccurs, int level) {
        _panelList.add(panel);
        _minOccurs = convertOccurs(minOccurs);
        _maxOccurs = convertOccurs(maxOccurs);
        _depthlevel = level;
        _currOccurs = 1;
        _name = panel.getName();
    }


    /********************************************************************************/

    // Getters & Setters //

    public int getMinOccurs() { return _minOccurs; }

    public void setMinOccurs(int minOccurs) { _minOccurs = minOccurs; }

    public void setMinOccurs(String minOccurs) { _minOccurs = convertOccurs(minOccurs); }


    public int getMaxOccurs() { return _maxOccurs; }

    public void setMaxOccurs(int maxOccurs) { _maxOccurs = maxOccurs; }

    public void setMaxOccurs(String maxOccurs) { _maxOccurs = convertOccurs(maxOccurs); }    


    public int getCurrOccurs() { return _currOccurs; }

    public void setCurrOccurs(int currOccurs) { _currOccurs = currOccurs; }


    public int getDepthlevel() { return _depthlevel; }

    public void setDepthlevel(int depthlevel) { _depthlevel = depthlevel; }


    public String getName() { return _name; }

    public void setName(String name) { _name = name; }
    

    public List<SubPanel> getSubPanels() { return _panelList; }
    

    public int getTop(UIComponent component) {
        Integer result = _contentTops.get(component);
        if (result == null)
            return 0 ;
        else
            return result;
    }

    public boolean hasPanel(SubPanel panel) {
        return _panelList.contains(panel);
    }

    /********************************************************************************/

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


    /**
     * A recursive method that resizes and repositions all the containing subpanels
     * of the one passed.
     * @param subPanel the subpanel that has instigated the change
     * @param adjustment how much to increase panel heights & tops by
     * @param top the new top position
     * @return the outermost containing subpanel in the nest of subpanels
     */
    private SubPanel repositionAncestor(SubPanel subPanel, int adjustment, int top) {
        subPanel.getController().incSubPanelTops(top, adjustment);
        subPanel.setHeight(subPanel.getHeight() + adjustment);

        // reposition parent (and parent's contents) and recurse
        UIComponent parentPanel = subPanel.getParent();
        if (parentPanel instanceof SubPanel) {
            SubPanel ancestor = (SubPanel) parentPanel ;
            ancestor.getController().incComponentTops(top, adjustment);
            incSiblingSubPanelTops(ancestor, top, adjustment);
            return repositionAncestor(ancestor, adjustment, ancestor.getTop());
        }
        else return subPanel ;    // the level 0 'patriarch'
    }


    /**
     * Increment the relevant subpanel tops for all subpanels at the same depth level
     * that are not ancestors of the subpanel passed
     * @param ancestor an outer subpanel of the one that triggered the repositioning
     * @param top the new top position of the triggering subpanel
     * @param adjustment how much to add to the top of affected subpanels
     */
    private void incSiblingSubPanelTops(SubPanel ancestor, int top, int adjustment) {
        List components = ancestor.getChildren() ;
        for (Object component : components) {
            if ((component instanceof SubPanel) && (component != ancestor)) {
                SubPanel subPanel = (SubPanel) component ;
                if (subPanel.getTop() > top) subPanel.incTop(adjustment) ;
            }
        }
    }


    /*********************************************************************************/

    // Public Methods //

    /** @return the int value of the min or max Occurs string for this panel set */
    public static int convertOccurs(String occurs) {
        int result = 1 ;

        if (occurs != null) {
            if (occurs.equals("unbounded"))
                result = Integer.MAX_VALUE ;
            else {
                try {
                    result = new Integer(occurs) ;
                }
                catch (Exception e) {
                    // nothing to do - default 1 will be returned
                }
            }
        }
        return result;
    }


    /** @return the appropriate style for this depthlevel */
    public String getSubPanelStyleClass() {
        return (_depthlevel % 2 == 0) ? "dynformSubPanelAlt" : "dynformSubPanel";
    }


    /** @return true if this subpanel can appear more times that it currently is */
    public boolean canVaryOccurs() {
        return ((_maxOccurs > 1) && (_minOccurs < _maxOccurs));
    }


    
    public void assignStyleToSubPanels(int maxLevel) {
        for (SubPanel subPanel : _panelList) subPanel.assignStyle(maxLevel);
    }


    public void addSimpleContent(DynFormContentList content, int top) {
        for (UIComponent component : content) {
            _contentTops.put(component, top);
        }        
    }

    public void addSimpleContent(UIComponent component, int top) {
        _contentTops.put(component, top);
    }

    /**
     * Adds a new, cloned subpanel to this controller
     * 
     * @param newPanel the subpanel to add
     * @return the outermost containing subpanel of the one added 
     */
    public SubPanel addSubPanel(SubPanel newPanel) {

        // adjust the tops of subsquent panels at the same depth level
        int adjustment = newPanel.getHeight() + DynFormFactory.Y_PP_INCREMENT;
        int top = newPanel.getTop();
        incSubPanelTops(top, adjustment) ;

        // adjust the heights of all containing panels in the nested hierarchy
        UIComponent parent = newPanel.getParent();
        SubPanel patriarch;
        if (parent instanceof SubPanel) {
            SubPanel parentPanel = (SubPanel) parent;
            parentPanel.getController().incComponentTops(top, adjustment);            
            patriarch = repositionAncestor(parentPanel, adjustment, parentPanel.getTop());
        }
        else
            patriarch = newPanel ;

        newPanel.incTop(adjustment);        
        _currOccurs += 1;
        _panelList.add(newPanel);

        // enable/disable buttons as required
        setOccursButtonsEnablement();

        return patriarch;
    }

    /**
     * Remove a subpanel from the set
     * @param oldPanel the panel to remove
     * @return the outermost containing subpanel of the one removed
     */
    public SubPanel removeSubPanel(SubPanel oldPanel) {
        int adjustment = - (oldPanel.getHeight() + DynFormFactory.Y_PP_INCREMENT);
        int top = oldPanel.getTop();

        incSubPanelTops(top, adjustment) ;

        // move up any subsequent components
        UIComponent parent = oldPanel.getParent();
        SubPanel patriarch;
        if (parent instanceof SubPanel) {
            SubPanel parentPanel = (SubPanel) parent;
            parentPanel.getController().incComponentTops(top, adjustment);
            patriarch = repositionAncestor(parentPanel, adjustment, parentPanel.getTop());
        }
        else
            patriarch = oldPanel ;

        _currOccurs -= 1;
        _panelList.remove(oldPanel);
        setOccursButtonsEnablement();

        return patriarch;

    }


    /**
     * reset the tops of all subpanels lower than the top specified
     * @param top the y-coord below which subpanels should be moved down
     * @param adjustment how much to move them down by
     */
    public void incSubPanelTops(int top, int adjustment) {
        for (SubPanel subPanel : _panelList)
            if (subPanel.getTop() > top) subPanel.incTop(adjustment) ;
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


    /** enble/disable the occurs buttons as required */
    public void setOccursButtonsEnablement() {
        boolean disableMinus = ((_currOccurs == 1) || (_currOccurs == _minOccurs)) ;
        boolean disablePlus = (_currOccurs == _maxOccurs);

        for (SubPanel panel : _panelList) {
            panel.getBtnMinus().setDisabled(disableMinus);
            panel.getBtnPlus().setDisabled(disablePlus);
        }
    }


    /** clone this controller */
    public SubPanelController clone() {
        SubPanelController controller = new SubPanelController() ;
        controller.setCurrOccurs(_currOccurs);
        controller.setMaxOccurs(_maxOccurs);
        controller.setMinOccurs(_minOccurs);
        controller.setDepthlevel(_depthlevel);
        return controller;
    }


    /**
     * Adds a subpanel to the set without making any surrounding adjustments to
     * screen coordinates
     *
     * @param panel the panel to add
     */
    public void storeSubPanel(SubPanel panel) {
        _panelList.add(panel) ;
        panel.setController(this);
    }


}
