/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.resourcing.jsf.dynform;

/**
 * This class is responsible for generating dynamic forms
 *
 * Author: Michael Adams
 * Creation Date: 19/01/2008
 * Refactored 10/08/2008 - 04/2010 - 09/2014
 */

import com.sun.rave.web.ui.appbase.AbstractSessionBean;
import com.sun.rave.web.ui.component.Button;
import com.sun.rave.web.ui.component.Calendar;
import com.sun.rave.web.ui.component.Checkbox;
import com.sun.rave.web.ui.component.*;
import com.sun.rave.web.ui.component.Label;
import com.sun.rave.web.ui.component.TextArea;
import com.sun.rave.web.ui.component.TextField;
import org.apache.log4j.Logger;
import org.jdom2.Element;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.resourcing.DynamicForm;
import org.yawlfoundation.yawl.resourcing.jsf.ApplicationBean;
import org.yawlfoundation.yawl.resourcing.jsf.FontUtil;
import org.yawlfoundation.yawl.resourcing.jsf.MessagePanel;
import org.yawlfoundation.yawl.resourcing.jsf.SessionBean;
import org.yawlfoundation.yawl.resourcing.jsf.dynform.dynattributes.DynAttributeFactory;
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

public class DynFormFactory extends AbstractSessionBean implements DynamicForm {

    // required JSF member and method
    private int __placeholder;

    private void _init() throws Exception { }

    /**************************************************************************/

    // components & settings of the dynamic form that are managed by this factory object

    private PanelLayout compPanel = new PanelLayout();

    public PanelLayout getCompPanel() { return compPanel; }

    public void setCompPanel(PanelLayout pl) { compPanel = pl; }


    private String headerText;

    public String getHeaderText() { return headerText; }

    public void setHeaderText(String text) { headerText = text; }


    private String containerStyle;

    public String getContainerStyle() { return containerStyle; }

    public void setContainerStyle(String style) { containerStyle = style; }


    private String btnOKStyle;

    public String getBtnOKStyle() { return btnOKStyle; }

    public void setBtnOKStyle(String style) { btnOKStyle = style; }


    private String btnCancelStyle;

    public String getBtnCancelStyle() { return btnCancelStyle; }

    public void setBtnCancelStyle(String style) { btnCancelStyle = style; }


    private String btnCompleteStyle;

    public String getBtnCompleteStyle() { return btnCompleteStyle; }

    public void setBtnCompleteStyle(String style) { btnCompleteStyle = style; }


    private String bottomPanelStyle;

    public String getBottomPanelStyle() { return bottomPanelStyle; }

    public void setBottomPanelStyle(String style) { bottomPanelStyle = style; }


    private String title;

    public String getTitle() { return title; }

    public void setTitle(String s) { title = s; }


    private String focus;

    public String getFocus() { return focus; }

    public void setFocus(String s) { focus = s; }


    /***************************************************************************/

    // the set of generated subpanels on the current form
    private Map<String, SubPanelController> _subPanelTable =
            new HashMap<String, SubPanelController>();

    // a map of inputs to the textfields they generated (required for validation)
    private Map<UIComponent, DynFormField> _componentFieldTable;

    // a map of the non-SubPanel components of the outermost panel and their y-coords
    private Map<UIComponent, Integer> _outermostTops = new HashMap<UIComponent, Integer>();

    // a reference to the sessionbean
    private SessionBean _sb = (SessionBean) getBean("SessionBean");

    // the workitem's extended attributes (decomposition level) *
    private DynFormUserAttributes _userAttributes;

    // the object that manufactures the form's fields *
    private DynFormFieldAssembler _fieldAssembler;

    // the wir currently populating the form
    private WorkItemRecord _displayedWIR;

    // user defined fonts store
    private DynFormFont _formFonts;

    // overall height in pixels of the generated form
    private int _overallHeight;

    // some constants for layout arithmetic
    static final int Y_DEF_INCREMENT = 10;          // default gap between components
    static final int Y_CHOICE_DECREMENT = 20;   // dec of y coord for choice container top
    static final int SUBPANEL_INSET = 10;           // gap between panel side walls
    static final int OUTER_PANEL_TO_BUTTONS = 20;   // gap from panel bottom to buttons
    static final int OUTER_PANEL_LEFT = 0;          // left (x) coord of outer panel
    static final int FORM_BUTTON_WIDTH = 76;        // buttons under outer panel
    static final int FORM_BUTTON_HEIGHT = 30;
    static final int FORM_BUTTON_GAP = 15;          // ... and the gap between them
    static final int BOTTOM_PANEL_HEIGHT = 20;
    static final int X_LABEL_OFFSET = 10;
    static final int DEFAULT_FIELD_OFFSET = 125;
    static final int DEFAULT_PANEL_BASE_WIDTH = 250;       // width of innermost panel
    static final int DEFAULT_FIELD_WIDTH = 145;
    static final int DEFAULT_LABEL_FIELD_GAP = 20;
    static final int CHECKBOX_FIELD_WIDTH = 10;
    static final int DEFAULT_FIELD_HEIGHT = 18;
    static final int LABEL_V_OFFSET = 5;
    static final int FIELD_VSPACE = 5;

    static int X_FIELD_OFFSET = DEFAULT_FIELD_OFFSET;
    static int PANEL_BASE_WIDTH = DEFAULT_PANEL_BASE_WIDTH;
    static int FIELD_WIDTH = DEFAULT_FIELD_WIDTH;


    /*******************************************************************************/
    // INTERFACE METHOD IMPLEMENTATIONS

    /**
     * Build and show a form to capture the work item's output data values.
     *
     * @param title  The form's title
     * @param header A header text for the form top
     * @param schema An XSD schema of the data types and attributes to display
     * @param wir    the work item record
     * @return true if form creation is successful
     */
    public boolean makeForm(String title, String header, String schema, WorkItemRecord wir) {
        reset();
        _userAttributes = new DynFormUserAttributes(wir.getAttributeTable());
        _displayedWIR = wir;
        setShowBanner();
        return buildForm(title, header, schema, getWorkItemData(wir), getParamInfo(wir));
     }


    /**
     * Build and show a form to capture the input data values on a case start.
     *
     * @param title      The form's title
     * @param header     A header text for the form top
     * @param schema     An XSD schema of the data types and attributes to display
     * @param parameters a list of the root net's input parameters
     * @return true if form creation is successful
     */
    public boolean makeForm(String title, String header, String schema,
                            List<YParameter> parameters) {
        reset();
        return buildForm(title, header, schema, null, getCaseParamMap(parameters));
    }


    /**
     * Gets the form's data list on completion of the form. The data list must be
     * a well-formed XML string representing the expected data structure for the work
     * item or case start. The opening and closing tag must be the name of task of which
     * the work item is an instance, or of the root net name of the case instance.
     *
     * @return A well-formed XML String of the work item's output data values
     */
    public String getDataList() {
        return new DataListGenerator(this).generate(compPanel, _fieldAssembler.getFieldList());
    }


    public List<Long> getDocComponentIDs() {
        List<Long> ids = new ArrayList<Long>();
        for (Object o : compPanel.getChildren()) {
            if (o instanceof DocComponent) {
                DocComponent docComponent = (DocComponent) o;
                ids.add(docComponent.getID());
            }
        }
        return ids;
    }


    /***********************************************************************************/

    public void processOccursAction(SubPanel panel, String btnType) {
        if (btnType.equals("+"))
            addSubPanel(panel);
        else
            removeSubPanel(panel);

        // resize outermost panel
        sizeAndPositionContent(compPanel);
    }


    public int getFormWidth() {
        return Math.max(getButtonBlockWidth(), getOuterPanelWidth());
    }


    public int getFormHeight() { return _overallHeight; }


    public void resetFormHeight() { _overallHeight = -1; }


    public boolean validateInputs(boolean reportErrors) {
        MessagePanel msgPanel = reportErrors ? _sb.getMessagePanel() : null;
        return new DynFormValidator().validate(compPanel, _componentFieldTable, msgPanel);
    }


    protected int getButtonBlockWidth() {
        int btnCount = getNumberOfVisibleButtons();
        return (btnCount * FORM_BUTTON_WIDTH) + (FORM_BUTTON_GAP * (btnCount - 1));
    }

    protected Button makeOccursButton(String name, String text) {
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
        return button;
    }


    protected String getDefaultFormName() { return _fieldAssembler.getFormName(); }


    protected DynFormField getFieldForComponent(UIComponent component) {
        return (component != null) ? _componentFieldTable.get(component) : null;
    }


    protected void addSubPanelControllerMap(Map<String, SubPanelController> map) {
        for (SubPanelController controller : map.values()) {
            _subPanelTable.put(createUniqueID("clonedGroup"), controller);
        }
    }


    protected void addClonedFieldToTable(TextField orig, TextField clone) {
        DynFormField field = _componentFieldTable.get(orig);
        if (field != null) {
            _componentFieldTable.put(clone, field);
        }
    }


    protected String enspace(String text) { return replaceInternalChars(text, '_', ' '); }


    protected String despace(String text) { return replaceInternalChars(text, ' ', '_'); }


    private void reset() {
        IdGenerator.clear();
        compPanel.getChildren().clear();
        _subPanelTable.clear();
        _outermostTops.clear();
        _displayedWIR = null;
        _userAttributes = null;

        // reset default widths
        X_FIELD_OFFSET = DEFAULT_FIELD_OFFSET;
        PANEL_BASE_WIDTH = DEFAULT_PANEL_BASE_WIDTH;
        FIELD_WIDTH = DEFAULT_FIELD_WIDTH;
    }


    private boolean buildForm(String title, String header, String schema, String data,
                              Map<String, FormParameter> paramMap) {
        try {
            _fieldAssembler = new DynFormFieldAssembler(schema, data, paramMap);
        }
        catch (DynFormException dfe) {
            Logger.getLogger(this.getClass()).error("Failed to build dynamic form", dfe);
            return false;
        }
        setFormFonts();
        setTitle(title);
        setHeader(header);
        buildForm();
        return true;
    }


    /**
     * @return a map of case or workitem parameters [param name, param] *
     */
    private Map<String, FormParameter> getParamInfo(WorkItemRecord wir) {
        return ((ApplicationBean) getBean("ApplicationBean")).getWorkItemParams(wir);
    }


    private Map<String, FormParameter> getCaseParamMap(List<YParameter> paramList) {
        Map<String, FormParameter> map = new HashMap<String, FormParameter>();
        if (paramList != null) {
            for (YParameter param : paramList) {
                 map.put(param.getName(), new FormParameter(param));
            }
        }
        return map;
    }



    /**
     * @return the decomposition-level extended attributes *
     */
    private DynFormUserAttributes getUserAttributes() { return _userAttributes; }


    /**
     * @return the data of the displayed workitem *
     */
    protected String getWorkItemData() { return getWorkItemData(_displayedWIR); }


    private String getWorkItemData(WorkItemRecord wir) {
        if (wir == null) return null;
        Element data = wir.getUpdatedData() != null ? wir.getUpdatedData() :
                wir.getDataList();
        return JDOMUtil.elementToStringDump(data);
    }


    protected DynFormFont getFormFonts() {
        return _formFonts;
    }


    private void setFormFonts() {
        _formFonts = new DynFormFont();
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


    private void setHeader(String header) {

        // set user defined header if any, or a default if not
        if (getAttributes() != null) {
            if (header == null) header = getAttributeValue("title");
            if (header == null) header = "Edit Work Item: " + _displayedWIR.getCaseID();
        }
        setHeaderText(header);
    }


    // hide the banner if user requested
    private void setShowBanner() {
        boolean hide = getAttributes() != null && getAttributes().getBooleanValue("hideBanner");
        _sb.setShowYAWLBanner(!hide);
    }


    private void buildForm() {
        DynFormComponentBuilder builder = new DynFormComponentBuilder(this);
        List<DynFormField> fieldList = _fieldAssembler.getFieldList();
        DynAttributeFactory.adjustFields(fieldList, _displayedWIR, _sb.getParticipant());   // 1st pass
        compPanel.getChildren().add(builder.makeHeaderText(getTaskLabel(), _fieldAssembler.getFormName()));
        compPanel.getChildren().addAll(buildInnerForm(null, builder, fieldList));
        DynAttributeFactory.applyAttributes(compPanel, _displayedWIR, _sb.getParticipant());  // 2nd pass
        _componentFieldTable = builder.getComponentFieldMap();
        setBaseWidths(builder);
        sizeAndPositionContent(compPanel);
    }


    private void setBaseWidths(DynFormComponentBuilder builder) {

        // find the left offset to position fields by getting the composite width of
        // longest label + offset of label start from panel side + the default gap
        // between the end of the label and its field
        X_FIELD_OFFSET = builder.getMaxLabelWidth() + X_LABEL_OFFSET + DEFAULT_LABEL_FIELD_GAP;

        // width of the Cancel/Save/Complete button panel
        int buttonBlockWidth = getButtonBlockWidth();

        // set the base width of all input fields
        FIELD_WIDTH = builder.hasOnlyCheckboxes() ? CHECKBOX_FIELD_WIDTH :
                Math.max(buttonBlockWidth - X_FIELD_OFFSET, getMaxFieldWidth(builder));


        // set the width of the innermost panel content (not including panel insets from
        // edges) to the greatest of the widest image, the button block and
        // (the field left offset + the field width)
        PANEL_BASE_WIDTH = Math.max(builder.getMaxImageWidth(),
                Math.max(buttonBlockWidth, X_FIELD_OFFSET + FIELD_WIDTH));
    }


    private double getStaticTextHeight(StaticText statText) {
        Font font;
        if (statText instanceof StaticTextBlock)
            font = ((StaticTextBlock) statText).getFont();
        else
            font = _formFonts.getFormHeaderFont();

        Dimension textBounds = FontUtil.getFontMetrics((String) statText.getText(), font);

        int parentWidth = getContainerWidth(statText);
        int lines = (int) Math.ceil(textBounds.getWidth() / (parentWidth - (SUBPANEL_INSET * 2)));
        return lines * textBounds.getHeight();
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
        else if ((component instanceof DocComponent))
            style = ((DocComponent) component).getStyle();
        return style;
    }


    private int getContainerWidth(UIComponent component) {
        UIComponent parent = component.getParent();
        if ((parent != null) && (parent instanceof SubPanel)) {
            return ((SubPanel) parent).getWidth();
        } else return PANEL_BASE_WIDTH;
    }


    private int getMaxFieldWidth(DynFormComponentBuilder builder) {
        int udWidth = getAttributes() != null ? getAttributes().getMaxFieldWidth() : -1;
        int buildWidth = builder.getMaxFieldWidth();
        return udWidth > -1 ? Math.min(udWidth, buildWidth) : buildWidth;
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
        else if ((component instanceof DocComponent))
            ((DocComponent) component).setStyle(style);
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
            String topStyle = StringUtil.extract(style, "top:\\s*\\d+px");
            if (topStyle != null) {
                String value = StringUtil.extract(topStyle, "\\d+");
                int top = StringUtil.strToInt(value, -1);
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
            } else if (o instanceof TextField) {
                FieldBase field = (FieldBase) o;
                field.setStyle(String.format(template, field.getStyle(),
                        X_FIELD_OFFSET, FIELD_WIDTH));
            } else if (o instanceof TextArea) {
                TextArea field = (TextArea) o;
                field.setStyle(String.format(template, field.getStyle(),
                        X_FIELD_OFFSET, FIELD_WIDTH + 6));
            } else if (o instanceof DropDown) {
                DropDown field = (DropDown) o;
                field.setStyle(String.format(template, field.getStyle(),
                        X_FIELD_OFFSET, FIELD_WIDTH + 8));
            } else if (o instanceof Calendar) {
                Calendar field = (Calendar) o;
                field.setStyle(String.format("%s; left:%dpx;", field.getStyle(),
                        X_FIELD_OFFSET));
            } else if (o instanceof Checkbox) {
                Checkbox field = (Checkbox) o;
                field.setStyle(String.format("%s; left:%dpx;", field.getStyle(),
                        X_FIELD_OFFSET));
            } else if (o instanceof FlatPanel) {
                FlatPanel field = (FlatPanel) o;
                field.setStyle(String.format("%s; width:%dpx;", field.getStyle(),
                        getFormWidth() - 20));
            } else if (o instanceof DocComponent) {
                DocComponent field = (DocComponent) o;
                field.setStyle(String.format(template, field.getStyle(),
                        X_FIELD_OFFSET, FIELD_WIDTH));
                field.setSubComponentStyles(FIELD_WIDTH);
                field.setFormWidth(getFormWidth());
                if (_displayedWIR != null) field.setCaseID(_displayedWIR.getRootCaseID());
            } else if (o instanceof StaticTextBlock) {
                StaticTextBlock field = (StaticTextBlock) o;
                field.setStyle(String.format("%s; width:%dpx;", field.getStyle(),
                        getFormWidth() - 20));
            } else if (o instanceof ImageComponent) {
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


    private double getComponentHeight(UIComponent component) {
        double height = 0;
        if (component instanceof SubPanel) {
            height = ((SubPanel) component).getHeight();
        } else if (component instanceof StaticText) {
            height = getStaticTextHeight((StaticText) component);
        } else if (component instanceof FlatPanel) {
            height = ((FlatPanel) component).getHeight();
        } else if (component instanceof ImageComponent) {
            height = ((ImageComponent) component).getHeight();
        } else if (component instanceof TextArea) {
            height = getFieldHeight(component) * ((TextArea) component).getRows()
                    + FIELD_VSPACE;
        } else {
            height = getFieldHeight(component) + FIELD_VSPACE;
        }
        return height;
    }

    private double getFieldHeight(UIComponent component) {
        DynFormField field = _componentFieldTable.get(component);
        return (field != null) ? getFieldHeight(field) : DEFAULT_FIELD_HEIGHT;
    }

    private double getFieldHeight(DynFormField field) {
        Font font = field.getFont();
        if (font == null) font = _formFonts.getFormFont();
        return Math.ceil(FontUtil.getFontMetrics("dummyText", font).getHeight());
    }


    private void sizeAndPositionContent(PanelLayout panel) {
        int maxLevel = -1;                          // -1 means no inner subpanels

        // set the size and position of inner panels relative to their nested level
        if (!_subPanelTable.isEmpty()) {
            maxLevel = getMaxDepthLevel();
            for (SubPanelController spc : _subPanelTable.values())
                spc.assignStyleToSubPanels(maxLevel);
        }

        // position input fields by setting the left coord
        adjustFieldOffsetsAndWidths(panel.getChildren());

        // calc and set height (adjusted for field count), width & top of outermost panel
        int width = getFormWidth();
        int height = (int) setContentTops(panel);
        int outerPanelTop = getOuterPanelTop(width);

        // set the style of the outermost panel and its container
        setPanelStyles(width, height, outerPanelTop);

        // reposition buttons to go directly under resized panel, centered
        positionButtons(width, height, outerPanelTop);
    }


    private int getNumberOfVisibleButtons() {
        return (_sb.getDynFormType() == ApplicationBean.DynFormType.netlevel) ? 2 : 3;
    }


    private void setPanelStyles(int containerWidth, int height, int top) {
        int outerPanelWidth = getOuterPanelWidth();

        // set the style of the outermost panel...
        String outerPanelStyle =
                String.format("position: absolute; height: %dpx; width: %dpx; top: %dpx",
                        height, outerPanelWidth, top);

        // ... and a left inset if the panel is narrower than the container ...
        if (outerPanelWidth < containerWidth) {
            outerPanelStyle += "; left: " + (containerWidth - outerPanelWidth) / 2 + "px";
        }

        // ...and the user-defined background colour (if any)...
        String udBgColour = getFormBackgroundColour();
        if (udBgColour != null) {
            outerPanelStyle += "; background-color: " + udBgColour;
        }
        compPanel.setStyle(outerPanelStyle);

        // ...and finally its container
        containerStyle = String.format("position: relative; height: %dpx; top: 0; width: %dpx",
                top + height + OUTER_PANEL_TO_BUTTONS + FORM_BUTTON_HEIGHT + BOTTOM_PANEL_HEIGHT,
                containerWidth);
    }


    private void positionButtons(int width, int height, int top) {
        int btnTop = top + height + OUTER_PANEL_TO_BUTTONS;
        int btnCount = getNumberOfVisibleButtons();
        int btnBlockWidth = (btnCount * FORM_BUTTON_WIDTH) + (FORM_BUTTON_GAP * (btnCount - 1));
        int btnCancelLeft = OUTER_PANEL_LEFT + ((width - btnBlockWidth) / 2);
        int btnOKLeft = btnCancelLeft + FORM_BUTTON_WIDTH + FORM_BUTTON_GAP;
        btnOKStyle = String.format("left: %dpx; top: %dpx", btnOKLeft, btnTop);
        btnCancelStyle = String.format("left: %dpx; top: %dpx", btnCancelLeft, btnTop);
        if (btnCount == 3) {
            int btnCompleteLeft = btnOKLeft + FORM_BUTTON_WIDTH + FORM_BUTTON_GAP;
            btnCompleteStyle = String.format("left: %dpx; top: %dpx", btnCompleteLeft, btnTop);
        }
        _overallHeight = btnTop + FORM_BUTTON_HEIGHT;
        bottomPanelStyle = String.format("top: %dpx", _overallHeight);
    }


    private double setContentTops(PanelLayout panel) {
        double top = 5;
        for (Object o : panel.getChildren()) {
            if (o instanceof SubPanel) {
                SubPanel subPanel = (SubPanel) o;
                subPanel.setHeight(setContentTops(subPanel));  // recurse
                subPanel.setTop((int) top);
                subPanel.assignStyle(getMaxDepthLevel());
                if (subPanel.isVisible()) top += subPanel.getHeight() + Y_DEF_INCREMENT;
            } else {
                UIComponent component = (UIComponent) o;
                if (component instanceof Button) {
                    continue;
                }
                else if (component instanceof Label) {
                    top = Math.max(top, 25);                      // if no header
                    if (isComponentVisible(getComponentForLabel(panel, (Label) component))) {
                        setTopStyle(component, (int) top + LABEL_V_OFFSET);
                    }
                } else {
                    if (!(component instanceof StaticText)) {
                        top = Math.max(top, 15);                      // for radio
                    }
                    if (isComponentVisible(component)) {
                        setTopStyle(component, (int) top);
                        top += getComponentHeight(component) + Y_DEF_INCREMENT;
                    }
                }
            }

        }
        return top;
    }


    private UIComponent getComponentForLabel(PanelLayout panel, Label label) {
        String forID = label.getFor();
        if (forID != null) {
            return panel.findComponent(forID);
        } else {          // must be a doc component
            for (Object o : panel.getChildren()) {
                if (o instanceof DocComponent) {
                    DocComponent docComponent = (DocComponent) o;
                    if (docComponent.getLabel().equals(label)) {
                        return docComponent;
                    }
                }
            }
        }
        return null;
    }


    private void setTopStyle(UIComponent component, int top) {
        String style = getStyle(component);
        if (style == null) style = "";
        setStyle(component, String.format("%stop:%dpx;", style, top));

        Object panel = component.getParent();
        if (panel instanceof SubPanel) {
            ((SubPanel) panel).setContentTop(component, top);
        } else {
            _outermostTops.put(component, top);
        }
    }


    private int getOuterPanelTop(int width) {
        Font font = _formFonts.getFormTitleFont();
        Dimension bounds = FontUtil.getFontMetrics(headerText, font);
        int lines = (int) Math.ceil(bounds.getWidth() / width);
        return lines * (int) (bounds.getHeight() + (font.getSize() / 2)) + 50;
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
            } else if (!field.isEmptyOptionalInputOnly()) {  // create the field (inside a panel)

                // if min and/or max defined at the field level, enclose it in a subpanel
                if (field.isGroupedField()) {
                    componentList.addAll(buildSubPanel(builder, field));
                } else {
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
        DynFormComponentList compList = builder.makePeripheralComponents(field, true);
        SubPanelController spc = _subPanelTable.get(field.getGroupID());
        SubPanel subPanel = builder.makeSubPanel(field, spc);
        _subPanelTable.put(field.getGroupID(), subPanel.getController());
        DynFormComponentList innerContent;
        if (field.isFieldContainer()) {
            innerContent = buildInnerForm(subPanel, builder, field.getSubFieldList());
        } else {
            innerContent = builder.makeInputField(field);
            field.addSubField(field.clone());
        }
        subPanel.getChildren().addAll(innerContent);
        compList.add(subPanel);
        compList.addAll(builder.makePeripheralComponents(field, false));

        return compList;
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


    private String createUniqueID(String id) { return IdGenerator.uniquify(id); }


    private int getMaxDepthLevel() {
        int result = -1;
        for (SubPanelController spc : _subPanelTable.values())
            result = Math.max(result, spc.getDepthlevel());

        return result;
    }


    private void addSubPanel(SubPanel panel) {
        SubPanel newPanel = new SubPanelCloner().clone(panel, this, createUniqueID("clone"));

        // get container of this panel
        UIComponent parent = panel.getParent();
        List children = parent.getChildren();

        // insert the new panel directly after the cloned one
        children.add(children.indexOf(panel) + 1, newPanel);

        SubPanel level0Container = panel.getController().addSubPanel(newPanel);
        int adjustment = (int) newPanel.getHeight() + DynFormFactory.Y_DEF_INCREMENT;
        adjustLayouts(level0Container, adjustment);
    }


    private void removeSubPanel(SubPanel panel) {
        SubPanel level0Container = panel.getController().removeSubPanel(panel);
        removeOrphanedControllers(panel);
        int adjustment = -((int) panel.getHeight() + DynFormFactory.Y_DEF_INCREMENT);
        adjustLayouts(level0Container, adjustment);

        UIComponent parent = panel.getParent();
        parent.getChildren().remove(panel);
    }


    private void adjustLayouts(SubPanel level0Container, int adjustment) {
        repositionLevel0Panels(level0Container, adjustment, level0Container.getTop());
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


    private void removeSubPanelController(SubPanel panel) {
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


    private int getOuterPanelWidth() {
        return PANEL_BASE_WIDTH + (SUBPANEL_INSET * 2 * (getMaxDepthLevel() + 2));
    }


    private DynFormUserAttributes getAttributes() { return _userAttributes; }


    // replaces each internally occurring 'pre' char with a 'post' char
    private String replaceInternalChars(String text, char pre, char post) {
        if ((text == null) || (text.length() < 3) || (text.indexOf(pre) < 0)) return text;

        char[] chars = text.toCharArray();

        // ignore leading and trailling underscores
        for (int i = 1; i < chars.length - 1; i++) {
            if (chars[i] == pre) chars[i] = post;
        }
        return new String(chars);
    }


    // support for decomposition extended attributes

    public String getPageBackgroundURL() {
        return getAttributeValue("page-background-image");
    }

    public String getPageBackgroundColour() {
        return getAttributeValue("page-background-color");
    }


    protected String getFormBackgroundColour() {
        return getAttributeValue("background-color");
    }


    protected String getFormAltBackgroundColour() {
        return getAttributeValue("background-alt-color");
    }


    private String getFormFontStyle() {
        return (getUserAttributes() == null) ? null :
                getUserAttributes().getUserDefinedFontStyle();
    }


    private Font getFormFont() {
        return (getUserAttributes() == null) ? null :
                getUserAttributes().getUserDefinedFont();
    }


    private Font getFormHeaderFont() {
        return (getUserAttributes() == null) ? null :
                getUserAttributes().getFormHeaderFont();
    }


    private String getFormHeaderFontStyle() {
        return (getUserAttributes() == null) ? null :
                getUserAttributes().getFormHeaderFontStyle();
    }


    protected String getFormJustify() {
        return (getUserAttributes() == null) ? null :
                getUserAttributes().getTextJustify();
    }


    protected boolean isFormReadOnly() {
        return (getUserAttributes() != null) && getUserAttributes().isReadOnly();
    }


    private String getTaskLabel() {
        return getAttributeValue("label");
    }


    private String getAttributeValue(String key) {
        return (getUserAttributes() == null) ? null :
                getUserAttributes().getValue(key);
    }


}
