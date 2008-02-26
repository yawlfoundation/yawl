package org.yawlfoundation.yawl.resourcing.jsf;

import com.sun.rave.web.ui.component.Calendar;
import com.sun.rave.web.ui.component.Checkbox;
import com.sun.rave.web.ui.component.Label;
import com.sun.rave.web.ui.component.TextField;

import javax.faces.component.UIComponent;
import java.util.*;

/**
 * Author: Michael Adams
 * Creation Date: 21/02/2008
 */
public class SubPanelController {

    private int _minOccurs ;
    private int _maxOccurs ;
    private int _currOccurs ;            // current display count (min <= curr <= max)
    private int _depthlevel ;
    private String _name;

    // mapping of the tops (y-coords) of the non-panel component members of the subpanels
    // controlled by ths controller
    private Hashtable<UIComponent, Integer> _contentTops = new Hashtable<UIComponent, Integer>() ;

    // the list of subpanel instances (all instance of the same subpanel
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
   
    /** @return the int value of the min or max Occurs string for this panel */
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


    private SubPanel repositionAncestor(SubPanel subPanel, int adjustment, int top) {
        subPanel.getController().incSubPanelTops(top, adjustment);
        subPanel.setHeight(subPanel.getHeight() + adjustment);

        UIComponent parentPanel = subPanel.getParent();
        if (parentPanel instanceof SubPanel) {
            SubPanel ancestor = (SubPanel) parentPanel ;
            ancestor.getController().incComponentTops(top, adjustment);
            incSiblingSubPanelTops(ancestor, top, adjustment);
            return repositionAncestor(ancestor, adjustment, ancestor.getTop());
        }
        else return subPanel ;    // the level 0 'patriarch'
    }

    
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

    /** @return the appropriate style for this depthlevel */
    public String getSubPanelStyleClass() {
        return (_depthlevel % 2 == 0) ? "dynformSubPanelAlt" : "dynformSubPanel";
    }


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


    public SubPanel addSubPanel(SubPanel newPanel) {
        int adjustment = newPanel.getHeight() + DynFormFactory.Y_PP_INCREMENT;
        int top = newPanel.getTop();

        incSubPanelTops(top, adjustment) ;

        // move down any subsequent components
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
        setOccursButtonsEnablement();

        return patriarch;
    }


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



    public void incSubPanelTops(int top, int adjustment) {
        for (SubPanel subPanel : _panelList)
            if (subPanel.getTop() > top) subPanel.incTop(adjustment) ;
    }
    

    public void incComponentTops(int top, int adjustment) {
        for (UIComponent component : _contentTops.keySet()) {
            int oldTop = _contentTops.get(component);
            if (oldTop > top) {
                resetTopStyle(component, oldTop + adjustment);
                _contentTops.put(component, oldTop + adjustment);
            }
        }

    }


    public void setOccursButtonsEnablement() {
        boolean disableMinus = ((_currOccurs == 1) || (_currOccurs == _minOccurs)) ;
        boolean disablePlus = (_currOccurs == _maxOccurs);

        for (SubPanel panel : _panelList) {
            panel.getBtnMinus().setDisabled(disableMinus);
            panel.getBtnPlus().setDisabled(disablePlus);
        }
    }


    public SubPanelController clone() {
        SubPanelController controller = new SubPanelController() ;
        controller.setCurrOccurs(_currOccurs);
        controller.setMaxOccurs(_maxOccurs);
        controller.setMinOccurs(_minOccurs);
        controller.setDepthlevel(_depthlevel);
        return controller;
    }

    public void storeSubPanel(SubPanel panel) {
        _panelList.add(panel) ;
        panel.setController(this);
    }


}
