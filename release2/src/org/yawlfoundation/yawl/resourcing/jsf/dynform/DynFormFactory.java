/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing.jsf.dynform;

/**
 * Factory class responsible for generating dynamic forms
 *
 * Author: Michael Adams
 * Creation Date: 19/01/2008
 * Refactored 10/08/2008
 */

import com.sun.rave.web.ui.appbase.AbstractSessionBean;
import com.sun.rave.web.ui.component.*;
import com.sun.rave.web.ui.component.Calendar;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.resourcing.jsf.ApplicationBean;
import org.yawlfoundation.yawl.resourcing.jsf.SessionBean;

import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import javax.faces.event.ActionEvent;
import java.util.*;

public class DynFormFactory extends AbstractSessionBean {

    // required JSF member and method
    private int __placeholder;

    private void _init() throws Exception { }

    /****************************************************************************/

    // components & settings of the dynamic form that are managed by this factory object

    private PanelLayout compPanel = new PanelLayout();

    public PanelLayout getCompPanel() { return compPanel; }

    public void setCompPanel(PanelLayout pl) { compPanel = pl; }


    private String headerText;

    public String getHeaderText() { return headerText; }

    public void setHeaderText(String text) { headerText = text ; }


    private String btnOKStyle ;

    public String getBtnOKStyle() { return btnOKStyle; }

    public void setBtnOKStyle(String style) { btnOKStyle = style; }


    private String btnCancelStyle ;

    public String getBtnCancelStyle() { return btnCancelStyle; }

    public void setBtnCancelStyle(String style) { btnCancelStyle = style; }


    private String btnCompleteStyle ;

    public String getBtnCompleteStyle() { return btnCompleteStyle; }

    public void setBtnCompleteStyle(String style) { btnCompleteStyle = style; }


    private String title ;

    public String getTitle() { return title; }

    public void setTitle(String s) { title = s; }


    private String focus ;

    public String getFocus() { return focus; }

    public void setFocus(String s) { focus = s; }

    
    /****************************************************************************/

    // the wir currently populating the dynamic form
    private WorkItemRecord _displayedWIR;

    public WorkItemRecord getDisplayedWIR() { return _displayedWIR; }

    public void setDisplayedWIR(WorkItemRecord wir) { _displayedWIR = wir; }

    
    // a running set of component id's - used to ensure id uniqueness
    private Set<String> _usedIDs = new HashSet<String>();

    // the set of generated subpanels on the current form
    private Hashtable<String, SubPanelController> _subPanelTable =
            new Hashtable<String, SubPanelController>();

    // a map of inputs to the textfields they generated (required for validation)
    private Hashtable<TextField, DynFormField> _componentFieldTable;

    // a map of the non-panel components of the outermost panel and their y-coords
    private Hashtable<UIComponent, Integer> _outermostTops =
            new Hashtable<UIComponent, Integer>(); 

    // the 'status' of the component add process
    private enum ComponentType { nil, panel, field, radio }

    // some constants for layout arithmetic
    static final int Y_NF_INCREMENT = 30 ;      // inc of y coord from start to 1st field
    static final int Y_FN_INCREMENT = 30 ;      // inc of y coord from last field to end
    static final int Y_PN_INCREMENT = 10 ;      // inc of y coord from last panel to end
    static final int Y_FF_INCREMENT = 25 ;      // inc of y coord between two fields
    static final int Y_PP_INCREMENT = 10 ;      // inc of y coord between two panels
    static final int Y_PF_INCREMENT = 20 ;      // inc of y coord between panel -> field
    static final int Y_FP_INCREMENT = 30 ;      // inc of y coord between field -> panel
    static final int Y_RF_INCREMENT = 20 ;      // inc of y cood between radio -> field
    static final int Y_RP_INCREMENT = 20 ;      // inc of y cood between radio -> panel
    static final int Y_FR_INCREMENT = 20 ;      // inc of y cood between field -> radio
    static final int Y_PR_INCREMENT = 20 ;      // inc of y cood between field -> panel
    static final int Y_NR_INCREMENT = 10 ;      // inc of y cood between start -> radio
    static final int Y_CHOICE_DECREMENT = 20;   // dec of y coord for choice container top
    static final int Y_SINGLE_ELEM_INCREMENT = 25;  // inc of y coord for single content box
    static final int SUBPANEL_INSET = 10 ;      // gap between panel side walls
    static final int OUTER_PANEL_TO_BUTTONS = 15;   // gap from panel bottom to buttons
    static final int OUTER_PANEL_TOP = 130;      // top (y) coord of outer panel
    static final int OUTER_PANEL_LEFT = 50;      // left (x) coord of outer panel
    static final int FORM_BUTTON_WIDTH = 76;     // buttons under outer panel
    static final int FORM_BUTTON_GAP = 15;       // ... and the gap between them
    static final int X_LABEL_OFFSET = 10;
    static final int DEFAULT_FIELD_OFFSET = 125;
    static final int DEFAULT_PANEL_BASE_WIDTH = 250;         // width of innermost panel
    static final int DEFAULT_FIELD_WIDTH = 115;
    static final int CHECKBOX_FIELD_WIDTH = 10;
    static int X_FIELD_OFFSET = DEFAULT_FIELD_OFFSET;
    static int PANEL_BASE_WIDTH = DEFAULT_PANEL_BASE_WIDTH;
    static int FIELD_WIDTH = DEFAULT_FIELD_WIDTH;


    /*********************************************************************************/

    /**  a reference to the sessionbean */
    private SessionBean _sb =  (SessionBean) getBean("SessionBean") ;


    /**
     * Initialises a new dynamic form
     * @param title the page title
     * @return true if form is successfully initialised
     */
    public boolean initDynForm(String title) {
        setTitle(title);

        // start with a clean form
        compPanel.getChildren().clear();
        _usedIDs.clear();
        _subPanelTable.clear();
        _outermostTops.clear();

        // reset default widths 
        X_FIELD_OFFSET = DEFAULT_FIELD_OFFSET;
        PANEL_BASE_WIDTH = DEFAULT_PANEL_BASE_WIDTH;
        FIELD_WIDTH = DEFAULT_FIELD_WIDTH;
        
        // get schema and data for case/workitem
        try {
            String schema = getSchema();
            if (schema != null) {
                String data = getInstanceData(schema) ;
                Map<String, FormParameter> params = getParamInfo();
                DynFormFieldAssembler fieldAssembler =
                        new DynFormFieldAssembler(schema, data, params);
                buildForm(fieldAssembler);
            }
            return (schema != null);
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /** @return the data schema for the case/item to be displayed */
    private String getSchema() {
        String result ;
        if (_sb.getDynFormType() == ApplicationBean.DynFormType.netlevel)
            result = _sb.getCaseSchema() ;
        else
            result = _sb.getTaskSchema(_displayedWIR);
        return result;
    }


    /** @return the instance data for the currently displayed case/workitem */
    private String getInstanceData(String schema) {
        String result ;
        if (_sb.getDynFormType() == ApplicationBean.DynFormType.netlevel)
            result = _sb.getInstanceData(schema) ;
        else
            result = _sb.getInstanceData(schema, _displayedWIR);
        return result;
    }


    private Map<String, FormParameter> getParamInfo() {
        if (_sb.getDynFormType() == ApplicationBean.DynFormType.netlevel)
           return _sb.getCaseParams();
        else
            return ((ApplicationBean) getBean("ApplicationBean")).getWorkItemParams(_displayedWIR);
    }


    private void buildForm(DynFormFieldAssembler fieldAssembler) {
        DynFormComponentBuilder builder = new DynFormComponentBuilder(this);
        List content = buildInnerForm(null, builder, fieldAssembler.getFieldList()) ;
        compPanel.getChildren().add(builder.makeHeaderText(fieldAssembler.getFormName())) ;
        compPanel.getChildren().addAll(content) ;
        setBaseWidths(builder);
        _componentFieldTable = builder.getTextFieldMap();
        sizeAndPositionContent(compPanel.getChildren()) ;
    }


    private void setBaseWidths(DynFormComponentBuilder builder) {
        int labelWidth = builder.getMaxLabelWidth();
        if (builder.hasOnlyCheckboxes())
            FIELD_WIDTH = CHECKBOX_FIELD_WIDTH;
        else
            FIELD_WIDTH = Math.max(FIELD_WIDTH, builder.getMaxFieldWidth());
        
        X_FIELD_OFFSET = Math.max(X_FIELD_OFFSET, labelWidth + (X_LABEL_OFFSET * 2));
        PANEL_BASE_WIDTH = Math.max(PANEL_BASE_WIDTH,
                                    labelWidth + FIELD_WIDTH + (X_LABEL_OFFSET * 3));
    }

    
    private int calcHeight(List content) {
        int height = 0 ;                      
        int lastInc = 0;
        ComponentType prevComponent = ComponentType.nil;
        ComponentType currComponent ;

        for (Object o : content) {

            // ignore labels and buttons 
            if (! ((o instanceof Label) || (o instanceof Button))) {
                if (o instanceof SubPanel) {
                    height += ((SubPanel) o).getHeight();
                    currComponent = ComponentType.panel;
                }
                else
                    currComponent = ComponentType.field;

                // add gap between components
                lastInc = getNextInc(prevComponent, currComponent);
                height += lastInc;
                prevComponent = currComponent;
            }
        }

        height -= (lastInc - getNextInc(prevComponent, ComponentType.nil));                                          // remove final gap

        return height ;
    }


    private void adjustFieldOffsetsAndWidths(List content) {
        String template = "%s; left:%dpx; width:%dpx";
        for (Object o : content) {
            if (o instanceof SubPanel) {
                adjustFieldOffsetsAndWidths(((SubPanel) o).getChildren());
            }
            else if (o instanceof TextField) {
                TextField field = (TextField) o;
                field.setStyle(String.format(template, field.getStyle(),
                                             X_FIELD_OFFSET, FIELD_WIDTH));
            }
            else if (o instanceof DropDown) {
                DropDown field = (DropDown) o;
                field.setStyle(String.format(template, field.getStyle(),
                                             X_FIELD_OFFSET, FIELD_WIDTH + 8));
            }
            else if (o instanceof Calendar) {
                Calendar field = (Calendar) o;
                field.setStyle(String.format("%s; left:%dpx;", field.getStyle(),
                                             X_FIELD_OFFSET));
            }
            else if (o instanceof Checkbox) {
                Checkbox field = (Checkbox) o;
                field.setStyle(String.format("%s; left:%dpx;", field.getStyle(),
                                             X_FIELD_OFFSET));                
            }
        }
    }


    private void sizeAndPositionContent(List content) {
        int maxLevel = -1 ;                          // -1 means no inner subpanels

        // set the size and position of inner panels relative to their nested level
        if (! _subPanelTable.isEmpty()) {
            maxLevel = getMaxDepthLevel();
            for (SubPanelController spc : _subPanelTable.values())
                spc.assignStyleToSubPanels(maxLevel);
        }

        // position input fields by setting the left coord
        adjustFieldOffsetsAndWidths(content);

        // calc and set height and width of outermost panel
        int height = calcHeight(content) ;
        int width = getFormWidth() ;
        String style = String.format("height: %dpx; width: %dpx", height, width);
        compPanel.setStyle(style);

        // reposition buttons to go directly under resized panel, centered
        int btnTop = OUTER_PANEL_TOP + height + OUTER_PANEL_TO_BUTTONS;
        int btnCount = getNumberOfVisibleButtons();
        int btnCancelLeft = OUTER_PANEL_LEFT +
                ( (width - (btnCount * FORM_BUTTON_WIDTH) - FORM_BUTTON_GAP) / btnCount ) ;
        int btnOKLeft = btnCancelLeft + FORM_BUTTON_WIDTH + FORM_BUTTON_GAP;
        btnOKStyle = String.format("left: %dpx; top: %dpx", btnOKLeft, btnTop);
        btnCancelStyle = String.format("left: %dpx; top: %dpx", btnCancelLeft, btnTop);
        if (btnCount == 3) {
            int btnCompleteLeft = btnOKLeft + FORM_BUTTON_WIDTH + FORM_BUTTON_GAP;
            btnCompleteStyle = String.format("left: %dpx; top: %dpx", btnCompleteLeft, btnTop);
        }
    }

    private int getNumberOfVisibleButtons() {
        if (_sb.getDynFormType() == ApplicationBean.DynFormType.netlevel)
            return 2;
        else
            return 3;
    }


    private DynFormComponentList buildInnerForm(SubPanel container,
                                                DynFormComponentBuilder builder,
                                                List<DynFormField> fieldList) {
        DynFormComponentList result = new DynFormComponentList();
        DynFormComponentList innerContent ;
        int top = 0 ;                                  // top (yPos) posn of component
        ComponentType prevComponent = ComponentType.nil ;

        if (fieldList == null) return result;
        
        for (DynFormField field : fieldList) {
            if (field.isChoiceField()) {

                // complexType choice has a header, simpletype choice does not
                if ((container != null) && (container.getChildCount() == 1))
                    top += getNextInc(prevComponent, ComponentType.field);
                else
                    top += getNextInc(prevComponent, ComponentType.radio);

                RadioButton rButton = builder.makeRadioButton(field, top);
                if (prevComponent == ComponentType.nil) rButton.setSelected(true);
                result.add(rButton);
                if (container == null) _outermostTops.put(rButton, top);
                prevComponent = ComponentType.radio ;  
            }

            if (field.isFieldContainer()) {

                // new complex type - recurse in a new container
                top += getNextInc(prevComponent, ComponentType.panel);
                SubPanelController spc = _subPanelTable.get(field.getGroupID());
                SubPanel subPanel = builder.makeSubPanel(top, field, spc);
                _subPanelTable.put(field.getGroupID(), subPanel.getController());
                innerContent = buildInnerForm(subPanel, builder, field.getSubFieldList());
                subPanel.getChildren().addAll(innerContent);
                result.add(subPanel);
                top += subPanel.getHeight() ;
                prevComponent = ComponentType.panel ;
            }
            else  {


//                if (container != null) {
//                    addOccursButtons(container, field);
//                }

                // create the field (inside a panel)
                if ((container != null) && container.isChoicePanel() && result.isEmpty())
                    top += Y_NR_INCREMENT;
                else if (field.getGroupID() != null)
                    top += getNextInc(prevComponent, ComponentType.panel);
                else
                    top += getNextInc(prevComponent, ComponentType.field);

                // if min and/or max defined at the field level, enclose it in a subpanel
                if (field.getGroupID() != null) {
                    SubPanelController spc = _subPanelTable.get(field.getGroupID());
                    SubPanel subPanel = builder.makeSubPanel(top, field, spc);
                    _subPanelTable.put(field.getGroupID(), subPanel.getController());
                    innerContent = builder.makeInputField(Y_SINGLE_ELEM_INCREMENT, field);
                    subPanel.getChildren().addAll(innerContent);
                    result.add(subPanel);
                    subPanel.setHeight(Y_SINGLE_ELEM_INCREMENT +
                                       getNextInc(ComponentType.field, ComponentType.nil)) ;
                    top += subPanel.getHeight() ;
                    prevComponent = ComponentType.panel ;
                }
                else {
                    innerContent = builder.makeInputField(top, field);
                    if (container != null) {
                        container.getController().addSimpleContent(innerContent, top);
                    }
                    else {
                        for (UIComponent component : innerContent)
                            _outermostTops.put(component, top);
                    }
                    result.addAll(innerContent);
                    prevComponent = ComponentType.field ;
                }    
            }
        }
        if (container != null)
            container.setHeight(top + getNextInc(prevComponent, ComponentType.nil)) ;

        return result;
    }


    private void addOccursButtons(SubPanel container, DynFormField field) {
        if (! (field.isNullMinoccurs() && field.isNullMaxoccurs())) {
            SubPanelController controller = container.getController();
            if (container.getBtnPlus() == null) {
                controller.setMaxOccurs(field.getMaxoccurs());
                controller.setMinOccurs(field.getMinoccurs());
                controller.setCurrOccurs(field.getOccursCount());

                if (controller.canVaryOccurs()) {
                    container.addOccursButton(makeOccursButton(
                                               container.getName(), "+"));
                    container.addOccursButton(makeOccursButton(
                                               container.getName(), "-"));
                    controller.setOccursButtonsEnablement();
                }
            }
        }
    }


    public Button makeOccursButton(String name, String text) {
        Button button = new Button();
        button.setId(createUniqueID("btn" + name));
        button.setText(text);
        button.setNoTextPadding(true);
        button.setMini(true);
        button.setEscape(false);
        button.setStyleClass("dynformOccursButton");
        button.setImmediate(true);
        button.setActionListener(bindOccursButtonListener());
        if (text.equals("+"))
            button.setToolTip("Add another content set to this panel");
        else {
            button.setToolTip("Remove a content set from this panel");
            button.setDisabled(true);  // can't have less than one panel instance
        }
        return button ;
    }


    private MethodBinding bindOccursButtonListener() {
        Application app = FacesContext.getCurrentInstance().getApplication();
        return app.createMethodBinding("#{dynForm.btnOccursAction}",
                                                  new Class[]{ActionEvent.class});
    }


    public String createUniqueID(String id) {
        int suffix = 0 ;
        while (_usedIDs.contains(id + ++suffix)) ;
        String result = id + suffix;
        _usedIDs.add(result);
        return result ;
    }


    private int getMaxDepthLevel() {
        int result = 0 ;
        for (SubPanelController spc : _subPanelTable.values())
            result = Math.max(result, spc.getDepthlevel());

        return result ;
    }


    private int getNextInc(ComponentType prev, ComponentType curr) {
        int result = Y_NF_INCREMENT;       // default for prev == nil
        switch (prev) {
            case panel: { if (curr == ComponentType.panel)
                              result = Y_PP_INCREMENT ;
                          else if (curr == ComponentType.field)
                              result = Y_PF_INCREMENT ;
                          else if (curr == ComponentType.radio)
                              result = Y_PR_INCREMENT;
                          else
                              result = Y_PN_INCREMENT ;
                          break ;
                        }

            case field: { if (curr == ComponentType.panel)
                              result = Y_FP_INCREMENT ;
                          else if (curr == ComponentType.field)
                              result = Y_FF_INCREMENT ;
                          else if (curr == ComponentType.radio)
                              result = Y_FR_INCREMENT;
                          else
                             result = Y_FN_INCREMENT ;
                          break;
                        }

            case radio: { if (curr == ComponentType.panel)
                              result = Y_RP_INCREMENT ;
                          else if (curr == ComponentType.field)
                              result = Y_RF_INCREMENT ;
                          else
                              result = Y_FN_INCREMENT ;
                          break;
                       }

            case nil:  { if (curr == ComponentType.radio)
                             result = Y_NR_INCREMENT;
                       }
        }
        return result ;
    }

    private void addSubPanel(SubPanel panel) {
        SubPanel newPanel = new SubPanelCloner().clone(panel, this, createUniqueID("clone")) ;

        // get container of this panel
        UIComponent parent = panel.getParent();
        parent.getChildren().add(newPanel);

        SubPanel level0Container = panel.getController().addSubPanel(newPanel);
        int adjustment = newPanel.getHeight() + DynFormFactory.Y_PP_INCREMENT;
        repositionLevel0Panels(level0Container, adjustment, panel.getTop()) ;
        repositionOutermostFields(panel.getTop(), adjustment);
    }


    private void removeSubPanel(SubPanel panel) {
        SubPanel level0Container = panel.getController().removeSubPanel(panel);
        int adjustment = - (panel.getHeight() + DynFormFactory.Y_PP_INCREMENT);
        repositionLevel0Panels(level0Container, adjustment, panel.getTop()) ;
        repositionOutermostFields(panel.getTop(), adjustment);

        UIComponent parent = panel.getParent();
        parent.getChildren().remove(panel);
    }

    
    private void repositionLevel0Panels(SubPanel container, int adjustment, int top) {
        for (SubPanelController controller : _subPanelTable.values()) {
            if (controller.getDepthlevel() == 0) {
                if (controller != container.getController())
                    controller.incSubPanelTops(top, adjustment);
            }
        }
    }
    

    public void addClonedFieldToTable(TextField orig, TextField clone) {
        DynFormField field = _componentFieldTable.get(orig);
        if (field != null) {
            _componentFieldTable.put(clone, field);
        }
    }


    private void repositionOutermostFields(int startingY, int adjustment) {
        String style = "top:%dpx";
        for (UIComponent component : _outermostTops.keySet()) {
            int top = _outermostTops.get(component);
            if (top > startingY) {
               int newTop = top + adjustment;
                if ((component instanceof Label))
                    ((Label) component).setStyle(String.format(style, newTop));
                else if ((component instanceof SelectorBase))
                    ((SelectorBase) component).setStyle(String.format(style, newTop));
                else if ((component instanceof FieldBase))
                    ((FieldBase) component).setStyle(String.format(style, newTop));
                
                _outermostTops.put(component, newTop);
            }    
        }

    }


    public void processOccursAction(SubPanel panel, String btnType) {
        if (btnType.equals("+"))
            addSubPanel(panel);
        else
            removeSubPanel(panel);

        // resize outermost panel
        sizeAndPositionContent(compPanel.getChildren()) ;
    }


    public String getDataList() {
        return new DataListGenerator(this).generate(compPanel) ;
    }

    public int getFormWidth() {
        return PANEL_BASE_WIDTH + (SUBPANEL_INSET * 2 * (getMaxDepthLevel() + 2));
    }


    public boolean validateInputs() {
        return new DynFormValidator().validate(compPanel, _componentFieldTable,
                                               _sb.getMessagePanel());
    }


    public String enspace(String text) {
        return replaceInternalChars(text, '_', ' ');
    }

    public String despace(String text) {
        return replaceInternalChars(text, ' ', '_');
    }

    // replaces each internally occurring 'pre' char with a 'post' char
    private String replaceInternalChars(String text, char pre, char post) {
        if ((text == null) || (text.length() < 3)) return text;

        char[] chars = text.toCharArray();

        // ignore leading and trailling underscores
        for (int i = 1; i < chars.length - 2; i++) {
            if (chars[i] == pre) chars[i] = post;
        }
        return new String(chars);
    }

}
