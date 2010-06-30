/*
 * Copyright (c) 2004-2010 The YAWL Foundation. All rights reserved.
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

import com.sun.rave.web.ui.component.*;
import com.sun.rave.web.ui.component.Checkbox;
import com.sun.rave.web.ui.component.Label;
import com.sun.rave.web.ui.component.TextArea;
import com.sun.rave.web.ui.component.TextField;
import com.sun.rave.web.ui.model.Option;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.faces.component.UIComponent;
import javax.swing.*;
import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.List;

/**
 * Author: Michael Adams
 * Creation Date: 10/08/2008
 */
public class DynFormComponentBuilder {

    // for setting focus on first available component
    private boolean focusSet = false ;
    private DynFormFactory _factory ;
    private DynTextParser _textParser;
    private Hashtable<UIComponent, DynFormField> _componentFieldTable;

    private int _maxDropDownWidth = 0;
    private int _maxLabelWidth = 0;
    private int _maxTextValueWidth = 0;
    private int _maxImageWidth = 0;
    private boolean _hasCheckboxOnly = true;

    private final int DROPDOWN_BUTTON_WIDTH = 15;
    private final int TEXTAREA_ROWS = 4;
    private final SimpleDateFormat _sdf = new SimpleDateFormat("yyyy-MM-dd");


    public DynFormComponentBuilder() { }

    public DynFormComponentBuilder(DynFormFactory factory) {
        _factory = factory;
        _componentFieldTable = new Hashtable<UIComponent, DynFormField>();
    }


    public SubPanel makeSubPanel(DynFormField field, SubPanelController spc) {
        String name = field.getName();
        SubPanel subPanel = new SubPanel();
        subPanel.setName(name);
        subPanel.setId(_factory.createUniqueID("sub" + name));
        if ((! name.startsWith("choice")) && field.isFieldContainer())
            subPanel.getChildren().add(makeHeaderText(null, name)) ;

        if (spc != null) {
            spc.storeSubPanel(subPanel);
        }
        else {
            spc = new SubPanelController(subPanel, field.getMinoccurs(),
                                         field.getMaxoccurs(), field.getLevel(),
                                         _factory.getFormBackgroundColour(),
                                         _factory.getFormAltBackgroundColour()) ;
            subPanel.setController(spc);
        }
        if (spc.canVaryOccurs()) {
            subPanel.addOccursButton(_factory.makeOccursButton(name, "+"));
            subPanel.addOccursButton(_factory.makeOccursButton(name, "-"));

            if (isDisabled(field)) {
                subPanel.enableOccursButtons(false);
            }
            else {
                spc.setOccursButtonsEnablement();
            }    
        }
        return subPanel ;
    }


    public DynFormComponentList makeInputField(DynFormField input) {
        UIComponent field;
        DynFormComponentList fieldList = makePeripheralComponents(input, true);

        // create and add a label for the parameter
        Label label = makeLabel(input);
        fieldList.add(label);

        String type = input.getDataTypeUnprefixed();
        if (type.equals("boolean"))
            field = makeCheckbox(input);
        else {
            _hasCheckboxOnly = false;
            if (type.equals("date")) {
                field = makeCalendar(input);
            }
            else if (input.hasEnumeratedValues()) {
                field = makeEnumeratedList(input);
            }
            else if (input.isTextArea()) {
                field = makeTextArea(input);
            }
            else field = makeTextField(input);
        }
        fieldList.add(field);
        label.setFor(field.getId());
        if (! focusSet) focusSet = setFocus(field) ;
        _componentFieldTable.put(field, input);            // store for validation later

        fieldList.addAll(makePeripheralComponents(input, false));
        return fieldList ;
    }


    public DynFormComponentList makePeripheralComponents(DynFormField input, boolean above) {
        DynFormComponentList list = new DynFormComponentList();
        boolean makeLine = above ? input.isLineAbove() : input.isLineBelow() ;
        if (makeLine) {
            list.add(makeFlatPanel());
        }
        String text = above ? input.getTextAbove() : input.getTextBelow();
        if (text != null) {
            list.add(makeStaticTextBlock(input, text));
        }
        String imagePath = above ? input.getImageAbove() : input.getImageBelow();
        if (imagePath != null) {
            ImageComponent image = makeImageComponent(imagePath);
            if (image != null) {
                String align = above ? input.getImageAboveAlign() : input.getImageBelowAlign();
                if (align != null) {
                    image.setAlign(align);
                }
                list.add(image);
            }
        }
        return list;
    }


    private String makeStyle(UIComponent field, DynFormField input) {

        // increment y-coord for each component's top (relative to current panel)
        String style = mergeFontStyles(input.getUserDefinedFontStyle());

        // set justify - precedence is variable, form, none
        if (field instanceof TextField || field instanceof TextArea || field instanceof DropDown) {
            String justify = input.getTextJustify();

            // if not set at variable level, user global form level (if defined)
            if (justify == null) {
                justify = _factory.getFormJustify();    
            }
            if (justify != null) style += ";text-align: " + justify ;

        }
        if (! (field instanceof Label)) {
            if (input.hasBlackoutAttribute()) {
               style += ";background-color: black";
            }
            else {
                String bgColour = input.getBackgroundColour();
                if (bgColour != null) style += ";background-color: " + bgColour;
            }
        }
        if (style.length() > 0) style += ";";
        return style;
    }


    private String mergeFontStyles(String fieldStyle) {
        String formStyle = _factory.getFormFonts().getUserDefinedFormFontStyle();
        if (! StringUtil.isNullOrEmpty(formStyle)) {
            String[] formStyles = formStyle.split(";");
            for (String style : formStyles) {
                String key = style.split(":")[0];
                if (! fieldStyle.contains(key)) {
                    fieldStyle += ";" + style;
                }
            }
        }
        return fieldStyle;
    }


    public Label makeLabel(DynFormField input) {
        Label label = makeSimpleLabel(input.getLabelText()) ;
        label.setStyleClass("dynformLabel");
        label.setRequiredIndicator(false);
        label.setStyle(makeStyle(label, input)) ;
        label.setVisible(isVisible(input));
        if (label.isVisible()) setMaxLabelWidth(input);
        return label;
    }


    public Label makeSimpleLabel(String text) {
        Label label = new Label() ;
        label.setId(_factory.createUniqueID("lbl" + _factory.despace(text)));
        label.setText(parseText(text) + ": ");
        return label;
    }


    public StaticText makeHeaderText(String text, String defText) {
        StaticText header = new StaticText() ;
        header.setId(_factory.createUniqueID("stt" + defText));
        String headerText = (text != null) ? text : _factory.enspace(defText);
        header.setText(headerText);
        header.setStyleClass("dynFormPanelHeader");
        String fontStyle = _factory.getFormFonts().getUserDefinedFormHeaderFontStyle();
        if (fontStyle != null) {
            header.setStyle(fontStyle);
        }
        return header;
    }


    public Checkbox makeCheckbox(DynFormField input) {
        Checkbox cbox = new Checkbox();
        cbox.setId(_factory.createUniqueID("cbx" + input.getName()));
        cbox.setSelected((input.getValue() != null) &&
                          input.getValue().equalsIgnoreCase("true")) ;
        cbox.setDisabled(isDisabled(input));
        cbox.setStyleClass("dynformInput");
        cbox.setStyle(makeStyle(cbox, input)) ;
        cbox.setVisible(isVisible(input));
        return cbox ;
    }


    public Calendar makeCalendar(DynFormField input) {
        Calendar cal = new Calendar();
        cal.setId(_factory.createUniqueID("cal" + input.getName()));
        cal.setSelectedDate(createDate(input.getValue()));
        cal.setDateFormatPatternHelp("");
        cal.setDisabled(isDisabled(input));
        cal.setMinDate(new Date(1));
        cal.setMaxDate(getDate(25));
        cal.setColumns(15);
        cal.setStyleClass(getInputStyleClass(input));    
        cal.setStyle(makeStyle(cal, input)) ;
        cal.setVisible(isVisible(input));
        return cal;
    }

    private Date getDate(int yearAdj) {
        GregorianCalendar result = new GregorianCalendar() ;
        result.add(java.util.Calendar.YEAR, yearAdj);
        return result.getTime();
    }


    public DropDown makeEnumeratedList(DynFormField input) {
        DropDown dropdown = new DropDown();
        dropdown.setId(_factory.createUniqueID("cal" + input.getName()));
        dropdown.setStyleClass(getInputStyleClass(input));
        dropdown.setStyle(makeStyle(dropdown, input)) ;
        dropdown.setItems(getEnumeratedList(input));
        dropdown.setSelected(input.getValue());
        dropdown.setDisabled(isDisabled(input));
        dropdown.setVisible(isVisible(input));
        return dropdown;
    }

    private Option[] getEnumeratedList(DynFormField input) {
        List<String> values = input.getEnumeratedValues();
        Option[] result = new Option[values.size()];
        for (int i=0; i < values.size(); i++) {
            result[i] = new Option(values.get(i));
            setMaxDropDownWidth(input, values.get(i));
        }
        return result;
    }


    public TextArea makeTextArea(DynFormField input) {
        TextArea textarea = new TextArea();
        textarea.setId(_factory.createUniqueID("txa" + input.getName()));
        textarea.setStyleClass(getInputStyleClass(input));
        textarea.setStyle(makeStyle(textarea, input));
        textarea.setDisabled(isDisabled(input));
        textarea.setToolTip(input.getToolTip());
        textarea.setRows(TEXTAREA_ROWS);
        textarea.setVisible(isVisible(input));
        if (input.hasBlackoutAttribute()) {
            textarea.setText("");
        }
        else {
            textarea.setText(JDOMUtil.decodeEscapes(input.getValue()));
        }
        input.setRestrictionAttributes();
        return textarea ;

    }

    public TextField makeTextField(DynFormField input) {
        TextField textField = new TextField() ;
        textField.setId(_factory.createUniqueID("txt" + input.getName()));
        textField.setStyleClass(getInputStyleClass(input));
        textField.setStyle(makeStyle(textField, input));
        textField.setDisabled(isDisabled(input));
        textField.setToolTip(input.getToolTip());
        textField.setVisible(isVisible(input));
        if (textField.isVisible()) setMaxTextValueWidth(input, (String) textField.getText());
        if (input.hasBlackoutAttribute()) {
            textField.setText("");
        }
        else {
            textField.setText(JDOMUtil.decodeEscapes(input.getValue()));
        }
        input.setRestrictionAttributes();
        return textField ;
    }


    public RadioButton makeRadioButton(DynFormField input) {
        RadioButton rb = new RadioButton();
        rb.setId(_factory.createUniqueID("rb" + input.getName()));
        rb.setLabel("");
        rb.setName(input.getChoiceID());               // same name means same rb group
        rb.setStyle(makeStyle(rb, input));
        rb.setDisabled(isDisabled(input));
        rb.setStyleClass("dynformRadioButton");
        rb.setVisible(isVisible(input));
        return rb;
    }


    private StaticTextBlock makeStaticTextBlock(DynFormField input, String text) {
        StaticTextBlock block = new StaticTextBlock() ;
        block.setId(_factory.createUniqueID("stb"));
        block.setText(parseText(text));
        block.setStyle(String.format("position: absolute; text-align: left; left: 10px; %s;",
                input.getUserDefinedFontStyle()));
        block.setFont(input.getUserDefinedFont());
        return block; 
    }


    private FlatPanel makeFlatPanel() {
        FlatPanel line = new FlatPanel();                                        
        line.setId(_factory.createUniqueID("fpl"));
        line.setStyle("position: absolute; background-color: black; left: 10px; height: 2px;");
        return line;
    }


    private ImageComponent makeImageComponent(String imagePath) {
        Dimension size = getImageSize(imagePath);
        if (size.getHeight() < 0) return null;
       
        ImageComponent image = new ImageComponent();
        image.setId(_factory.createUniqueID("img"));
        image.setUrl(imagePath);
        image.setHeight((int) size.getHeight());
        image.setWidth((int) size.getWidth());
        image.setStyle(String.format("position: absolute; width: %dpx; height: %dpx; ",
               image.getWidth(), image.getHeight()));
        setMaxImageWidth(image.getWidth());
        return image;
    }


    private Dimension getImageSize(String imagePath) {
        try {
            URL url = new URL(imagePath);
            ImageIcon image = new ImageIcon(url);
            return new Dimension(image.getIconWidth(), image.getIconHeight());
        }
        catch (MalformedURLException mue) {
            return new Dimension(-1, -1);           // def size
        }
    }

    public boolean setFocus(UIComponent component) {
        if (component instanceof PanelLayout) return false ;
        _factory.setFocus("form1:" + component.getId());
        return true ;
    }


    private boolean isVisible(DynFormField input) {
        return ! input.isHidden(_factory.getWorkItemData());
    }


    private boolean isDisabled(DynFormField input) {
        return input.isInputOnly() || _factory.isFormReadOnly();
    }


    private String parseText(String text) {
        if (_textParser == null)
            _textParser = new DynTextParser(_factory.getWorkItemData());
        return _textParser.parse(text);
    }


    private Date createDate(String dateStr) {
        // set the date to the param's input value if possible, else default to today
        Date result = null;

        if (dateStr != null) {
            try {
                result = _sdf.parse(dateStr) ;
            }
            catch (ParseException pe) {
                result = new Date();
            }
        }
        return result ;
    }


    private String getInputStyleClass(DynFormField input) {
        if (isDisabled(input)) {
            return "dynformInputReadOnly";
        }
        else if (input.isRequired()) {
            return "dynformInputRequired";
        }
        else {                                         // read-write and not required
            return "dynformInput";
        }    
    }

    private void setMaxLabelWidth(DynFormField input) {
        _maxLabelWidth = Math.max(_maxLabelWidth, getTextWidth(input,
                input.getLabelText() + ":"));
    }

    public int getMaxLabelWidth() {
        return _maxLabelWidth;
    }


    private void setMaxDropDownWidth(DynFormField input, String text) {
        _maxDropDownWidth = Math.max(_maxDropDownWidth, getTextWidth(input, text));
    }

    public int getMaxDropDownWidth() {
        return _maxDropDownWidth + DROPDOWN_BUTTON_WIDTH;
    }


    private void setMaxTextValueWidth(DynFormField input, String text) {
        _maxTextValueWidth = Math.max(_maxTextValueWidth, getTextWidth(input, text));
    }

    public int getMaxTextValueWidth() {
        return _maxTextValueWidth;
    }

    public void setMaxImageWidth(int width) {
        _maxImageWidth = Math.max(_maxImageWidth, width);
    }

    public int getMaxImageWidth() {
        return _maxImageWidth;
    }

    public boolean hasOnlyCheckboxes() {
        return _hasCheckboxOnly ;
    }

    public int getMaxFieldWidth() {
        return Math.max(getMaxDropDownWidth(), getMaxTextValueWidth());
    }


    public Hashtable<UIComponent, DynFormField> getComponentFieldMap() {
        return _componentFieldTable;
    }


    private int getTextWidth(DynFormField input, String text) {

        // use field's udFont first, then form's, then default (via formfonts)
        Font font = input.getFont();
        if (font == null) font = _factory.getFormFonts().getFormFont();
        return _factory.getTextWidth(text, font);
    }
}
