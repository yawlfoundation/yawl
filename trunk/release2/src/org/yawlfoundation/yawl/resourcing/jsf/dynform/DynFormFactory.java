/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing.jsf.dynform;

/**
 * This class is responsible for generating dynamic forms
 *
 * Author: Michael Adams
 * Creation Date: 19/01/2008
 * Refactored 10/08/2008 - 04/2010
 */

import com.sun.rave.web.ui.appbase.AbstractSessionBean;
import com.sun.rave.web.ui.component.Button;
import com.sun.rave.web.ui.component.Calendar;
import com.sun.rave.web.ui.component.Checkbox;
import com.sun.rave.web.ui.component.*;
import com.sun.rave.web.ui.component.Label;
import com.sun.rave.web.ui.component.TextArea;
import com.sun.rave.web.ui.component.TextField;
import org.jdom.Element;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.resourcing.jsf.ApplicationBean;
import org.yawlfoundation.yawl.resourcing.jsf.FontUtil;
import org.yawlfoundation.yawl.resourcing.jsf.SessionBean;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import javax.faces.event.ActionEvent;
import java.awt.*;
import java.util.*;
import java.util.List;

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


    private String containerStyle ;

    public String getContainerStyle() { return containerStyle; }

    public void setContainerStyle(String style) { containerStyle = style; }


    private String btnOKStyle ;

    public String getBtnOKStyle() { return btnOKStyle; }

    public void setBtnOKStyle(String style) { btnOKStyle = style; }


    private String btnCancelStyle ;

    public String getBtnCancelStyle() { return btnCancelStyle; }

    public void setBtnCancelStyle(String style) { btnCancelStyle = style; }


    private String btnCompleteStyle ;

    public String getBtnCompleteStyle() { return btnCompleteStyle; }

    public void setBtnCompleteStyle(String style) { btnCompleteStyle = style; }


    private String bottomPanelStyle ;

    public String getBottomPanelStyle() { return bottomPanelStyle; }

    public void setBottomPanelStyle(String style) { bottomPanelStyle = style; }


    private String title ;

    public String getTitle() { return title; }

    public void setTitle(String s) { title = s; }


    private String focus ;

    public String getFocus() { return focus; }

    public void setFocus(String s) { focus = s; }

    
    /****************************************************************************/

    // a running set of component id's - used to ensure id uniqueness
    private Set<String> _usedIDs = new HashSet<String>();

    // the set of generated subpanels on the current form
    private Hashtable<String, SubPanelController> _subPanelTable =
            new Hashtable<String, SubPanelController>();

    // a map of inputs to the textfields they generated (required for validation)
    private Hashtable<UIComponent, DynFormField> _componentFieldTable;

    // a map of the non-SubPanel components of the outermost panel and their y-coords
    private Hashtable<UIComponent, Integer> _outermostTops =
            new Hashtable<UIComponent, Integer>();

    // used to maintain the 'status' of the component add process
    protected enum ComponentType { nil, panel, field, radio, line, textblock, image }

    // some constants for layout arithmetic
    static final int Y_DEF_INCREMENT = 10 ;     // default gap between components
    static final int Y_CHOICE_DECREMENT = 20;   // dec of y coord for choice container top
    static final int SUBPANEL_INSET = 10 ;      // gap between panel side walls
    static final int OUTER_PANEL_TO_BUTTONS = 20;   // gap from panel bottom to buttons
    static final int OUTER_PANEL_TOP = 80;      // top (y) coord of outer panel
    static final int OUTER_PANEL_LEFT = 0;      // left (x) coord of outer panel
    static final int FORM_BUTTON_WIDTH = 76;    // buttons under outer panel
    static final int FORM_BUTTON_HEIGHT = 30;
    static final int FORM_BUTTON_GAP = 15;      // ... and the gap between them
    static final int X_LABEL_OFFSET = 10;
    static final int DEFAULT_FIELD_OFFSET = 125;
    static final int DEFAULT_PANEL_BASE_WIDTH = 250;         // width of innermost panel
    static final int DEFAULT_FIELD_WIDTH = 145;
    static final int DEFAULT_LABEL_FIELD_GAP = 20;
    static final int CHECKBOX_FIELD_WIDTH = 10;
    static final int DEFAULT_FIELD_HEIGHT = 18;
    static final int LABEL_V_OFFSET = 5;
    static final int FIELD_VSPACE = 5;

    static int X_FIELD_OFFSET = DEFAULT_FIELD_OFFSET;
    static int PANEL_BASE_WIDTH = DEFAULT_PANEL_BASE_WIDTH;
    static int FIELD_WIDTH = DEFAULT_FIELD_WIDTH;


    /*********************************************************************************/

    /**  a reference to the sessionbean */
    private SessionBean _sb = (SessionBean) getBean("SessionBean") ;

    /** the workitem's extended attributes (decomposition level) **/
    private DynFormUserAttributes _userAttributes ;

    /** the object that manufactures the form's fields **/
    private DynFormFieldAssembler _fieldAssembler;

    // the wir currently populating the form
    private WorkItemRecord _displayedWIR;

    // user defined fonts store
    private DynFormFont _formFonts;

    public WorkItemRecord getDisplayedWIR() { return _displayedWIR; }

    public void setDisplayedWIR(WorkItemRecord wir) { _displayedWIR = wir; }


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
                _userAttributes = getUserAttributes();
                _fieldAssembler = new DynFormFieldAssembler(schema, data, getParamInfo());
                setFormFonts();
                buildForm();
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
        if (_sb.getDynFormType() == ApplicationBean.DynFormType.netlevel)
            return _sb.getCaseSchema() ;
        else
            return _sb.getTaskSchema(_displayedWIR);
    }


    /** @return the instance data for the currently displayed case/workitem */
    private String getInstanceData(String schema) {
        if (_sb.getDynFormType() == ApplicationBean.DynFormType.netlevel)
            return _sb.getInstanceData(schema) ;
        else {
            return getWorkItemData();
        }
    }


    /** @return a map of case or workitem parameters [param name, param] **/
    private Map<String, FormParameter> getParamInfo() {
        if (_sb.getDynFormType() == ApplicationBean.DynFormType.netlevel)
           return _sb.getCaseParams();
        else
            return ((ApplicationBean) getBean("ApplicationBean")).getWorkItemParams(_displayedWIR);
    }


    /** @return the decomposition-level extended attributes **/
    private DynFormUserAttributes getUserAttributes() {
        if (_sb.getDynFormType() == ApplicationBean.DynFormType.netlevel)
            return null;
        else
            return new DynFormUserAttributes(_displayedWIR.getAttributeTable());
    }


    /** @return the data of the displayed workitem **/
    public String getWorkItemData() {
        if (_displayedWIR == null) return null;
        Element data = (_displayedWIR.getUpdatedData() != null) ?
                        _displayedWIR.getUpdatedData() :
                        _displayedWIR.getDataList();
        return JDOMUtil.elementToStringDump(data);
    }


    public DynFormFont getFormFonts() {
        return _formFonts;
    }
    

    private void setFormFonts() {
        _formFonts = new DynFormFont() ;
        Font font = getFormFont();
        if (font != null) {
            _formFonts.setUserDefinedFormFont(font);
            _formFonts.setUserDefinedFormFontStyle(getFormFontStyle());
        }
        font = getFormHeaderFont();
        if (font != null) {
            _formFonts.setUserDefinedFormHeaderFont(font);
            _formFonts.setUserDefinedFormHeaderFontStyle(this.getFormHeaderFontStyle());
        }
    }

    private void buildForm() {
        DynFormComponentBuilder builder = new DynFormComponentBuilder(this);
        List<DynFormField> fieldList = _fieldAssembler.getFieldList();
  //      DynAttributeFactory.adjustFields(fieldList, _displayedWIR, _sb.getParticipant());   // 1st pass
        compPanel.getChildren().add(builder.makeHeaderText(getTaskLabel(), _fieldAssembler.getFormName())) ;
        compPanel.getChildren().addAll(buildInnerForm(null, builder, fieldList)) ;
   //     DynAttributeFactory.applyAttributes(compPanel, _displayedWIR, _sb.getParticipant());  // 2nd pass
        _componentFieldTable = builder.getComponentFieldMap();
        setBaseWidths(builder);
        sizeAndPositionContent(compPanel) ;
    }


    private void setBaseWidths(DynFormComponentBuilder builder) {

        // set the base width of all input fields
        FIELD_WIDTH = builder.hasOnlyCheckboxes() ? CHECKBOX_FIELD_WIDTH :
                          Math.max(FIELD_WIDTH, builder.getMaxFieldWidth());

        // find the left offset to position fields by getting the composite width of
        // longest label + offset of label start from panel side + the default gap
        // between the end of the label and its field
        X_FIELD_OFFSET = builder.getMaxLabelWidth() + X_LABEL_OFFSET + DEFAULT_LABEL_FIELD_GAP;

        // set the width of the innermost panel content (not including panel insets from
        // edges) to the greater of the widest image and (the field left offset +
        // the field width)
        PANEL_BASE_WIDTH = Math.max(builder.getMaxImageWidth(), X_FIELD_OFFSET + FIELD_WIDTH);
    }

    

    public int getStaticTextHeight(StaticText statText) {
        Font font;
        if (statText instanceof StaticTextBlock)
            font = ((StaticTextBlock) statText).getFont();
        else
            font = _formFonts.getFormHeaderFont();

        Dimension textBounds = FontUtil.getFontMetrics((String) statText.getText(), font);

        int parentWidth = getContainerWidth(statText);
        int lines = (int) Math.ceil(textBounds.getWidth() / (parentWidth - (SUBPANEL_INSET * 2)));
        return lines * (int) textBounds.getHeight() ;
    }


    private String getStyle(UIComponent component) {
        String style = "";
        if ((component instanceof Label))
            style = ((Label) component).getStyle();
        else if ((component instanceof SelectorBase))
            style = ((SelectorBase) component).getStyle();
        else if ((component instanceof FieldBase))
            style = ((FieldBase) component).getStyle();
        else if ((component instanceof FlatPanel))
            style = ((FlatPanel) component).getStyle();
        else if ((component instanceof StaticText))
            style = ((StaticText) component).getStyle();
        else if ((component instanceof ImageComponent))
            style = ((ImageComponent) component).getStyle();
        return style;
    }


    private int getContainerWidth(UIComponent component) {
        UIComponent parent = component.getParent();
        if ((parent != null) && (parent instanceof SubPanel)) {
            return ((SubPanel) parent).getWidth();
        }
        else return PANEL_BASE_WIDTH;
    }

    private void setStyle(UIComponent component, String style) {
        if ((component instanceof Label))
            ((Label) component).setStyle(style);
        else if ((component instanceof SelectorBase))
            ((SelectorBase) component).setStyle(style);
        else if ((component instanceof FieldBase))
            ((FieldBase) component).setStyle(style);
        else if ((component instanceof FlatPanel))
            ((FlatPanel) component).setStyle(style);
        else if ((component instanceof StaticText))
            ((StaticText) component).setStyle(style);
        else if ((component instanceof ImageComponent))
            ((ImageComponent) component).setStyle(style);
    }


    private boolean isComponentVisible(Object o) {
        boolean visible = true;
        if ((o instanceof SelectorBase))
            visible = ((SelectorBase) o).isVisible();
        else if ((o instanceof FieldBase))
            visible = ((FieldBase) o).isVisible();
        return visible;
    }


    private String replaceTopInStyle(UIComponent component, int adjustment) {
        String style = getStyle(component);
        if (style != null) {
            String topStyle = StringUtil.extract(style, "top:\\s*\\d+px") ;
            if (topStyle != null) {
                String value = StringUtil.extract(topStyle, "\\d+");
                int top;
                try {
                    top = Integer.parseInt(value);
                }
                catch (NumberFormatException nfe) {
                    top = -1;    
                }
                if (top > -1) {
                    String newTopStyle = String.format("top: %dpx", top + adjustment);
                    style = style.replace(topStyle, newTopStyle);
                }
            }
        }
        return style;
    }


    private void adjustFieldOffsetsAndWidths(List content) {
        String template = "%s; left:%dpx; width:%dpx;";
        for (Object o : content) {
            if (o instanceof SubPanel) {
                adjustFieldOffsetsAndWidths(((SubPanel) o).getChildren());
            }
            else if (o instanceof TextField) {
                FieldBase field = (FieldBase) o;
                field.setStyle(String.format(template, field.getStyle(),
                                             X_FIELD_OFFSET, FIELD_WIDTH));
            }
            else if (o instanceof TextArea) {
                TextArea field = (TextArea) o;
                field.setStyle(String.format(template, field.getStyle(),
                                             X_FIELD_OFFSET, FIELD_WIDTH + 6));
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
            else if (o instanceof FlatPanel) {
                FlatPanel field = (FlatPanel) o;
                field.setStyle(String.format("%s; width:%dpx;", field.getStyle(),
                                             getFormWidth() - 20));
            }
            else if (o instanceof StaticTextBlock) {
                StaticTextBlock field = (StaticTextBlock) o;
                field.setStyle(String.format("%s; width:%dpx;", field.getStyle(),
                                             getFormWidth() - 20));
            }
            else if (o instanceof ImageComponent) {
                ImageComponent field = (ImageComponent) o;
                String align = field.getAlign();
                int left = 10;
                if (align != null) {
                    if (align.equals("right"))
                        left = getFormWidth() - field.getWidth() - 10;
                    else if (align.equals("center"))
                        left = (10 + (getFormWidth() - field.getWidth())) / 2;
                }
                field.setStyle(String.format("%s; left:%dpx;", field.getStyle(), left));
            }
        }
    }


    private int getComponentHeight(UIComponent component) {
        int height = 0;
        if (component instanceof SubPanel) {
            height = ((SubPanel) component).getHeight();
        }
        else if (component instanceof StaticText) {
           height = getStaticTextHeight((StaticText) component);
        }
        else if (component instanceof FlatPanel) {
           height = ((FlatPanel) component).getHeight();
        }
        else if (component instanceof ImageComponent) {
            height = ((ImageComponent) component).getHeight();
        }
        else if (component instanceof TextArea) {
            height = getFieldHeight(component) * ((TextArea) component).getRows();
        }
        else {
            height = getFieldHeight(component) + FIELD_VSPACE;
        }
        return height;
    }

    protected int getFieldHeight(UIComponent component) {
        DynFormField field = _componentFieldTable.get(component);
        return (field != null) ? getFieldHeight(field) : DEFAULT_FIELD_HEIGHT;
    }

    protected int getFieldHeight(DynFormField field) {
        Font font = field.getFont();
        if (font == null) font = _formFonts.getFormFont();
        return (int) Math.ceil(FontUtil.getFontMetrics("dummyText", font).getHeight());
    }

    protected int getTextWidth(String s, Font font) {
        if ((s == null) || (s.length() == 0)) return 0;
        return (int) Math.ceil(FontUtil.getFontMetrics(s, font).getWidth());
    }

    
    private void sizeAndPositionContent(PanelLayout panel) {
        int maxLevel = -1 ;                          // -1 means no inner subpanels

        // set the size and position of inner panels relative to their nested level
        if (! _subPanelTable.isEmpty()) {
            maxLevel = getMaxDepthLevel();
            for (SubPanelController spc : _subPanelTable.values())
                spc.assignStyleToSubPanels(maxLevel);
        }

        // position input fields by setting the left coord
        adjustFieldOffsetsAndWidths(panel.getChildren());

        // calc and set height, width & top of outermost panel
        int width = getFormWidth() ;
        int height = setContentTops(panel);
        int outerPanelTop = getOuterPanelTop(width);

        // set the style of the outermost panel and its container
        setPanelStyles(width, height, outerPanelTop);

        // reposition buttons to go directly under resized panel, centered
        positionButtons(width, height, outerPanelTop);
    }

    
    private int getNumberOfVisibleButtons() {
        return (_sb.getDynFormType() == ApplicationBean.DynFormType.netlevel) ? 2 : 3;
    }


    private void setPanelStyles(int width, int height, int top) {

        // set the style of the outermost panel...
        String outerPanelStyle =
                String.format("position: absolute; height: %dpx; width: %dpx; top: %dpx",
                                      height, width, top);

        // ...and the user-defined background colour (if any)...
        String udBgColour = getFormBackgroundColour();
        if (udBgColour != null) {
            outerPanelStyle += "; background-color: " + udBgColour;
        }
        compPanel.setStyle(outerPanelStyle);

        // ...and its container
        containerStyle = String.format("position: relative; height: 10px; top: 0; width: %dpx",
                                        width);
    }


    private void positionButtons(int width, int height, int top) {
        int btnTop = top + height + OUTER_PANEL_TO_BUTTONS;
        int btnCount = getNumberOfVisibleButtons();
        int btnBlockWidth = (btnCount * FORM_BUTTON_WIDTH) + (FORM_BUTTON_GAP * (btnCount - 1));
        int btnCancelLeft = OUTER_PANEL_LEFT + ( (width - btnBlockWidth) / 2 ) ;
        int btnOKLeft = btnCancelLeft + FORM_BUTTON_WIDTH + FORM_BUTTON_GAP;
        btnOKStyle = String.format("left: %dpx; top: %dpx", btnOKLeft, btnTop);
        btnCancelStyle = String.format("left: %dpx; top: %dpx", btnCancelLeft, btnTop);
        if (btnCount == 3) {
            int btnCompleteLeft = btnOKLeft + FORM_BUTTON_WIDTH + FORM_BUTTON_GAP;
            btnCompleteStyle = String.format("left: %dpx; top: %dpx", btnCompleteLeft, btnTop);
        }
        bottomPanelStyle = String.format("top: %dpx", btnTop + FORM_BUTTON_HEIGHT);
    }


    private int setContentTops(PanelLayout panel) {
        int top = 5;
        for (Object o : panel.getChildren()) {
            if (o instanceof SubPanel) {
                SubPanel subPanel = (SubPanel) o;
                subPanel.setHeight(setContentTops(subPanel));  // recurse
                subPanel.setTop(top);
                subPanel.assignStyle(getMaxDepthLevel());
                top += subPanel.getHeight() + Y_DEF_INCREMENT;
            }
            else {
                UIComponent component = (UIComponent) o;
                if (! (component instanceof Button)) {
                    if (component instanceof Label) {
                        top = Math.max(top, 25);                      // if no header
                        String forID = ((Label) component).getFor();
                        if (isComponentVisible(panel.findComponent(forID))) {
                            setTopStyle(component, top + LABEL_V_OFFSET);
                        }
                    }
                    else {
                        if (! (component instanceof StaticText)) {
                            top = Math.max(top, 15);                      // for radio
                        }
                        if (isComponentVisible(component)) {
                            setTopStyle(component, top);
                            top += getComponentHeight(component) + Y_DEF_INCREMENT;
                        }
                    }
                }
            }
        }
        return top;
    }

    private void setTopStyle(UIComponent component, int top) {
        String style = getStyle(component);
        if (style == null) style = "";
        setStyle(component, String.format("%stop:%dpx;", style, top));

        Object panel = component.getParent();
        if (panel instanceof SubPanel) {
            ((SubPanel) panel).setContentTop(component, top);
        }
        else {
            _outermostTops.put(component, top);
        }
    }


    private int getOuterPanelTop(int width) {
        Font font = _formFonts.getFormTitleFont();
        Dimension bounds = FontUtil.getFontMetrics(headerText, font);
        int lines = (int) Math.ceil(bounds.getWidth() / width);
        return lines * (int) (bounds.getHeight() + (font.getSize() / 2)) + 50 ;
    }


    private DynFormComponentList buildInnerForm(SubPanel container,
                                                DynFormComponentBuilder builder,
                                                List<DynFormField> fieldList) {
        DynFormComponentList componentList = new DynFormComponentList();
        if (fieldList == null) return componentList;

        for (DynFormField field : fieldList) {
            if (field.isChoiceField()) {
                componentList.addAll(buildChoiceSelector(container, builder, field));
            }
            if (field.isFieldContainer()) {

                // new complex type - recurse in a new sub-container
                componentList.addAll(buildSubPanel(builder, field));
            }
            else  {  // create the field (inside a panel)

                // if min and/or max defined at the field level, enclose it in a subpanel
                if (field.getGroupID() != null) {
                    componentList.addAll(buildSubPanel(builder, field));
                }
                else {
                    componentList.addAll(builder.makeInputField(field));
                }
            }
        }
        return componentList;
    }


    private DynFormComponentList buildChoiceSelector(SubPanel container,
                             DynFormComponentBuilder builder, DynFormField field) {
        DynFormComponentList compList = new DynFormComponentList();

        RadioButton rButton = builder.makeRadioButton(field);
        rButton.setSelected((container == null) ||
                  isFirstRadioGroupMember(container.getChildren(), rButton.getName()));

        compList.add(rButton);
        return compList;
    }


    private DynFormComponentList buildSubPanel(DynFormComponentBuilder builder,
                         DynFormField field) {
        DynFormComponentList compList =  builder.makePeripheralComponents(field, true);
        SubPanelController spc = _subPanelTable.get(field.getGroupID());
        SubPanel subPanel = builder.makeSubPanel(field, spc);
        _subPanelTable.put(field.getGroupID(), subPanel.getController());
        DynFormComponentList innerContent;
        if (field.isFieldContainer()) {
            innerContent = buildInnerForm(subPanel, builder, field.getSubFieldList());
        }
        else {
            innerContent = builder.makeInputField(field);
            field.addSubField(field.clone());
        }
        subPanel.getChildren().addAll(innerContent);
        compList.add(subPanel);
        compList.addAll(builder.makePeripheralComponents(field, false));

        return compList;
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
            button.setDisabled(true);         // can't have less than one panel instance
        }
        return button ;
    }


    private MethodBinding bindOccursButtonListener() {
        Application app = FacesContext.getCurrentInstance().getApplication();
        return app.createMethodBinding("#{dynForm.btnOccursAction}",
                                                  new Class[]{ActionEvent.class});
    }

    private boolean isFirstRadioGroupMember(List content, String groupID) {
        for (Object component : content) {
            if (component instanceof RadioButton) {
                if (((RadioButton) component).getName().equals(groupID)) {
                    return false;
                }
            }
        }
        return true;
    }


    public String createUniqueID(String id) {
        int suffix = 0 ;
        while (_usedIDs.contains(id + ++suffix)) ;
        String result = id + suffix;
        _usedIDs.add(result);
        return result ;
    }


    public String getDefaultFormName() {
        return _fieldAssembler.getFormName();
    }


    private int getMaxDepthLevel() {
        int result = -1 ;
        for (SubPanelController spc : _subPanelTable.values())
            result = Math.max(result, spc.getDepthlevel());

        return result ;
    }


    private void addSubPanel(SubPanel panel) {
        SubPanel newPanel = new SubPanelCloner().clone(panel, this, createUniqueID("clone")) ;

        // get container of this panel
        UIComponent parent = panel.getParent();
        List children = parent.getChildren();

        // insert the new panel directly after the cloned one
        children.add(children.indexOf(panel) + 1, newPanel);

        SubPanel level0Container = panel.getController().addSubPanel(newPanel);
        int adjustment = newPanel.getHeight() + DynFormFactory.Y_DEF_INCREMENT;
        adjustLayouts(level0Container, adjustment);
    }


    private void removeSubPanel(SubPanel panel) {
        SubPanel level0Container = panel.getController().removeSubPanel(panel);
        removeOrphanedControllers(panel);
        int adjustment = - (panel.getHeight() + DynFormFactory.Y_DEF_INCREMENT);
        adjustLayouts(level0Container, adjustment);

        UIComponent parent = panel.getParent();
        parent.getChildren().remove(panel);
    }


    private void adjustLayouts(SubPanel level0Container, int adjustment) {
        repositionLevel0Panels(level0Container, adjustment, level0Container.getTop()) ;
        repositionOutermostFields(level0Container.getTop(), adjustment);
    }


    private void repositionLevel0Panels(SubPanel container, int adjustment, int top) {
        for (SubPanelController controller : _subPanelTable.values()) {
            if (controller.getDepthlevel() == 0) {
                if (controller != container.getController())
                    controller.incSubPanelTops(top, adjustment);
            }
        }
    }


    public void addSubPanelController(SubPanel panel) {
        _subPanelTable.put(panel.getName(), panel.getController());
    }


    public void addSubPanelControllerMap(Map<String, SubPanelController> map) {
        for (SubPanelController controller : map.values()) {
             _subPanelTable.put(createUniqueID("clonedGroup"), controller); 
        }
    }


    public void removeSubPanelController(SubPanel panel) {
        _subPanelTable.remove(panel.getName());
    }


    private void removeOrphanedControllers(SubPanel panel) {
        for (Object o : panel.getChildren()) {
            if (o instanceof SubPanel) {
                SubPanel orphan = (SubPanel) o;
                removeSubPanelController(orphan);
                removeOrphanedControllers(orphan);  // recurse
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
        for (UIComponent component : _outermostTops.keySet()) {
            int top = _outermostTops.get(component);
            if (top > startingY) {
                int newTop = top + adjustment;
                replaceTopInStyle(component, newTop);
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
        sizeAndPositionContent(compPanel) ;
    }


    public String getDataList() {
        return new DataListGenerator(this).generate(compPanel, _fieldAssembler.getFieldList()) ;
    }


    public int getFormWidth() {
        return PANEL_BASE_WIDTH + (SUBPANEL_INSET * 2 * (getMaxDepthLevel() + 2));
    }


    public boolean validateInputs() {
        return new DynFormValidator().validate(compPanel, _componentFieldTable,
                                               _sb.getMessagePanel());
    }


    public DynFormUserAttributes getAttributes() {
        return _userAttributes;
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

    
    // support for decomposition extended attributes

    public String getPageBackgroundURL() {
        return getAttributeValue("background-image");
    }


    public String getFormBackgroundColour() {
        return getAttributeValue("background-color");
    }


    public String getFormAltBackgroundColour() {
        return getAttributeValue("background-alt-color");
    }


    public String getFormFontStyle() {
        return (getUserAttributes() == null) ? null :
                getUserAttributes().getUserDefinedFontStyle();
    }


    public Font getFormFont() {
        return (getUserAttributes() == null) ? null :
                getUserAttributes().getUserDefinedFont();
    }


    public Font getFormHeaderFont() {
        return (getUserAttributes() == null) ? null :
                getUserAttributes().getFormHeaderFont();
    }


    public String getFormHeaderFontStyle() {
        return (getUserAttributes() == null) ? null :
                getUserAttributes().getFormHeaderFontStyle();
    }


    public String getFormJustify() {
        return (getUserAttributes() == null) ? null :
                getUserAttributes().getTextJustify();        
    }


    public boolean isFormReadOnly() {
        return (getUserAttributes() != null) && getUserAttributes().isReadOnly();
    }


    public String getTaskLabel() {
        return getAttributeValue("label");        
    }


    public String getAttributeValue(String key) {
        return (getUserAttributes() == null) ? null :
                getUserAttributes().getValue(key);
    }


}
