/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing.jsf.dynform;

import com.sun.rave.web.ui.component.*;
import com.sun.rave.web.ui.component.Checkbox;
import com.sun.rave.web.ui.component.Label;
import com.sun.rave.web.ui.component.TextArea;
import com.sun.rave.web.ui.component.TextField;
import com.sun.rave.web.ui.model.Option;
import org.yawlfoundation.yawl.util.JDOMUtil;

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
    private Hashtable<FieldBase, DynFormField> _componentFieldTable;

    private int _maxDropDownChars = 0;
    private int _maxLabelChars = 0;
    private int _maxTextValueChars = 0;
    private int _maxImageWidth = 0;
    private boolean _hasCheckboxOnly = true;

    private final int FONT_WIDTH = 6;
    private final int DROPDOWN_BUTTON_WIDTH = 15;
    private final int TEXTAREA_ROWS = 4;
    private final SimpleDateFormat _sdf = new SimpleDateFormat("yyyy-MM-dd");


    public DynFormComponentBuilder() { }

    public DynFormComponentBuilder(DynFormFactory factory) {
        _factory = factory;
        _componentFieldTable = new Hashtable<FieldBase, DynFormField>();
    }


    public SubPanel makeSubPanel(int top, DynFormField field, SubPanelController spc) {
        String name = field.getName();
        SubPanel subPanel = new SubPanel();
        subPanel.setTop(top);
        subPanel.setName(name);
        subPanel.setId(_factory.createUniqueID("sub" + name));
        if ((! name.startsWith("choice")) && field.isFieldContainer())
            subPanel.getChildren().add(makeHeaderText(name)) ;

        if (spc != null) {
            spc.storeSubPanel(subPanel);
        }
        else {
            spc = new SubPanelController(subPanel,field.getMinoccurs(),
                                         field.getMaxoccurs(), field.getLevel()) ;
            subPanel.setController(spc);
        }
        if (spc.canVaryOccurs()) {
            subPanel.addOccursButton(_factory.makeOccursButton(name, "+"));
            subPanel.addOccursButton(_factory.makeOccursButton(name, "-"));

            if (field.isInputOnly()) {
                subPanel.enableOccursButtons(false);
            }
            else {
                spc.setOccursButtonsEnablement();
            }    
        }
        return subPanel ;
    }


    public DynFormComponentList makeInputField(int top, DynFormField input,
                                            DynFormFactory.ComponentType prevComponent) {
        UIComponent field;
        int startingTop = top;
        DynFormComponentList fieldList = new DynFormComponentList();

        DynFormComponentList preList = makePeripheralComponents(input, top, true, prevComponent) ;
        if (! preList.isEmpty()) {
            fieldList.addAll(preList);      // line, text and/or image before
            top += preList.getHeight();
            prevComponent = preList.getLastComponent();
        }
        top += _factory.getNextInc(prevComponent, DynFormFactory.ComponentType.field);

        // create and add a label for the parameter
        Label label = makeLabel(input, top);
        fieldList.add(label);

        String type = input.getDataTypeUnprefixed();
        if (type.equals("boolean"))
            field = makeCheckbox(input, top);
        else {
            _hasCheckboxOnly = false;
            if (type.equals("date")) {
                field = makeCalendar(input, top);
            }
            else if (input.hasEnumeratedValues()) {
                field = makeEnumeratedList(input, top);
            }
            else if (input.isTextArea()) {
                field = makeTextArea(input, top);
                top += TEXTAREA_ROWS * 18;
            }
            else field = makeTextField(input, top);
        }
        fieldList.add(field);
        label.setFor(field.getId());
        if (! focusSet) focusSet = setFocus(field) ;
        prevComponent = DynFormFactory.ComponentType.field;

        DynFormComponentList postList = makePeripheralComponents(input, top, false, prevComponent) ;
        if (! postList.isEmpty()) {
            fieldList.addAll(postList);  // line, text and/or image after
            top += postList.getHeight();
            prevComponent = postList.getLastComponent();
        }
        fieldList.setHeight(top - startingTop);
        fieldList.setLastComponent(prevComponent);
        return fieldList ;
    }


    public DynFormComponentList makePeripheralComponents(DynFormField input, int top,
                    boolean above, DynFormFactory.ComponentType prevComponent) {
        DynFormComponentList list = new DynFormComponentList();
        int startingTop = top;
        boolean makeLine = above ? input.isLineAbove() : input.isLineBelow() ;
        if (makeLine) {
            top += _factory.getNextInc(prevComponent, DynFormFactory.ComponentType.line);
            FlatPanel line = makeFlatPanel(top);
            list.add(line);
            top += line.getHeight() ;
            prevComponent = DynFormFactory.ComponentType.line;
        }
        String text = above ? input.getTextAbove() : input.getTextBelow();
        if (text != null) {
            top += _factory.getNextInc(prevComponent, DynFormFactory.ComponentType.textblock);
            StaticTextBlock block = makeStaticTextBlock(input, top, text);
            list.add(block);
            top += _factory.getAdjustmentForWrappingText(block);
            prevComponent = DynFormFactory.ComponentType.textblock;
        }
        String imagePath = above ? input.getImageAbove() : input.getImageBelow();
        if (imagePath != null) {
            int preImageTop = top;
            top += _factory.getNextInc(prevComponent, DynFormFactory.ComponentType.image);            
            ImageComponent image = makeImageComponent(top, imagePath);
            if (image != null) {
                String align = above ? input.getImageAboveAlign() : input.getImageBelowAlign();
                if (align != null) {
                    image.setAlign(align);
                }
                list.add(image);
                top += image.getHeight();
                prevComponent = DynFormFactory.ComponentType.image;
            }
            else top = preImageTop;   // reset top if image didn't work for any reason
        }
        list.setHeight(top - startingTop);
        list.setLastComponent(prevComponent);
        return list;
    }


    private String makeStyle(UIComponent field, DynFormField input, int top) {

        // increment y-coord for each component's top (relative to current panel)
        String style = String.format("top: %dpx%s", top, input.getUserDefinedFontStyle());

        if (field instanceof TextField || field instanceof TextArea || field instanceof DropDown) {
            String justify = input.getTextJusify();
            if (justify != null) style += ";text-align: " + justify;

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
        return style;
    }


    public Label makeLabel(DynFormField input, int top) {
        Label label = makeSimpleLabel(input.getLabelText()) ;
        label.setStyleClass("dynformLabel");
        label.setRequiredIndicator(false);
        label.setStyle(makeStyle(label, input, top + 5)) ;
        if (input.isHidden(_factory.getWorkItemData())) {
            label.setVisible(false);
        }
        else {
            setMaxLabelChars(label);
        }
        return label;
    }


    public Label makeSimpleLabel(String text) {
        Label label = new Label() ;
        label.setId(_factory.createUniqueID("lbl" + _factory.despace(text)));
        label.setText(text + ": ");
        return label;
    }


    public StaticText makeHeaderText(String text) {
        StaticText header = new StaticText() ;
        header.setId(_factory.createUniqueID("stt" + text));
        header.setText(_factory.enspace(text));
        header.setStyleClass("dynFormPanelHeader");
        return header;
    }


    public Checkbox makeCheckbox(DynFormField input, int top) {
        Checkbox cbox = new Checkbox();
        cbox.setId(_factory.createUniqueID("cbx" + input.getName()));
        cbox.setSelected((input.getValue() != null) &&
                          input.getValue().equalsIgnoreCase("true")) ;
        cbox.setDisabled(input.isInputOnly());
        cbox.setStyleClass("dynformInput");
        cbox.setStyle(makeStyle(cbox, input, top)) ;
        cbox.setVisible(! input.isHidden(_factory.getWorkItemData()));
        return cbox ;
    }


    public Calendar makeCalendar(DynFormField input, int top) {
        Calendar cal = new Calendar();
        cal.setId(_factory.createUniqueID("cal" + input.getName()));
        cal.setSelectedDate(createDate(input.getValue()));
        cal.setDateFormatPatternHelp("");
        cal.setDisabled(input.isInputOnly());
        cal.setMinDate(new Date(1));
        cal.setMaxDate(getDate(25));
        cal.setColumns(15);
        cal.setStyleClass(getInputStyleClass(input));
        cal.setStyle(makeStyle(cal, input, top)) ;
        cal.setVisible(! input.isHidden(_factory.getWorkItemData()));
        return cal;
    }

    private Date getDate(int yearAdj) {
        GregorianCalendar result = new GregorianCalendar() ;
        result.add(java.util.Calendar.YEAR, yearAdj);
        return result.getTime();
    }


    public DropDown makeEnumeratedList(DynFormField input, int top) {
        DropDown dropdown = new DropDown();
        dropdown.setId(_factory.createUniqueID("cal" + input.getName()));
        dropdown.setStyleClass(getInputStyleClass(input));
        dropdown.setStyle(makeStyle(dropdown, input, top)) ;
        dropdown.setItems(getEnumeratedList(input));
        dropdown.setSelected(input.getValue());
        dropdown.setDisabled(input.isInputOnly());
        dropdown.setVisible(! input.isHidden(_factory.getWorkItemData()));
        return dropdown;
    }

    private Option[] getEnumeratedList(DynFormField input) {
        List<String> values = input.getEnumeratedValues();
        Option[] result = new Option[values.size()];
        for (int i=0; i < values.size(); i++) {
            result[i] = new Option(values.get(i));
            setMaxDropDownChars(values.get(i));
        }
        return result;
    }


    public TextArea makeTextArea(DynFormField input, int top) {
        TextArea textarea = new TextArea();
        textarea.setId(_factory.createUniqueID("txa" + input.getName()));
        textarea.setStyleClass(getInputStyleClass(input));
        textarea.setStyle(makeStyle(textarea, input, top));
        textarea.setDisabled(input.isInputOnly()); 
        textarea.setToolTip(input.getToolTip());
        textarea.setRows(TEXTAREA_ROWS);
        if (input.hasBlackoutAttribute()) {
            textarea.setText("");
        }
        else {
            textarea.setText(JDOMUtil.decodeEscapes(input.getValue()));
        }
        if (input.isHidden(_factory.getWorkItemData())) {
            textarea.setVisible(false);
        }
        else {
            _componentFieldTable.put(textarea, input);     // store for validation later
        }
        input.setRestrictionAttributes();
        return textarea ;

    }

    public TextField makeTextField(DynFormField input, int top) {
        TextField textField = new TextField() ;
        textField.setId(_factory.createUniqueID("txt" + input.getName()));
        textField.setStyleClass(getInputStyleClass(input));
        textField.setStyle(makeStyle(textField, input, top));
        textField.setDisabled(input.isInputOnly());
        textField.setToolTip(input.getToolTip());
        if (input.hasBlackoutAttribute()) {
            textField.setText("");
        }
        else {
            textField.setText(JDOMUtil.decodeEscapes(input.getValue()));
        }
        setMaxTextValueChars((String) textField.getText());
        if (input.isHidden(_factory.getWorkItemData())) {
            textField.setVisible(false);
        }
        else {
            _componentFieldTable.put(textField, input);     // store for validation later
        }
        input.setRestrictionAttributes();
        return textField ;
    }


    public RadioButton makeRadioButton(DynFormField input, int top) {
        RadioButton rb = new RadioButton();
        rb.setId(_factory.createUniqueID("rb" + input.getName()));
        rb.setLabel("");
        rb.setName(input.getChoiceID());               // same name means same rb group
        rb.setStyle(makeStyle(rb, input, top));
        rb.setDisabled(input.isInputOnly());
        rb.setStyleClass("dynformRadioButton");
        rb.setVisible(! input.isHidden(_factory.getWorkItemData()));
        return rb;
    }


    private StaticTextBlock makeStaticTextBlock(DynFormField input, int top, String text) {
        StaticTextBlock block = new StaticTextBlock() ;
        block.setId(_factory.createUniqueID("stb"));
        block.setText(text);
        block.setStyle(String.format("position: absolute; text-align: left; left: 10px; top: %dpx%s",
                top, input.getUserDefinedFontStyle()));
        block.setFont(input.getUserDefinedFont());
        return block; 
    }


    private FlatPanel makeFlatPanel(int top) {
        FlatPanel line = new FlatPanel();                                        
        line.setId(_factory.createUniqueID("fpl"));
        line.setStyle(String.format("position: absolute; background-color: black; " +
                "left: 10px; height: 2px; top: %dpx", top));
        return line;
    }


    private ImageComponent makeImageComponent(int top, String imagePath) {
        Dimension size = getImageSize(imagePath);
        if (size.getHeight() < 0) return null;
       
        ImageComponent image = new ImageComponent();
        image.setId(_factory.createUniqueID("img"));
        image.setUrl(imagePath);
        image.setHeight((int) size.getHeight());
        image.setWidth((int) size.getWidth());
        image.setStyle(String.format("position: absolute; width: %dpx; height: %dpx; " +
              "top: %dpx", image.getWidth(), image.getHeight(), top));
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
        if (input.isInputOnly()) {
            return "dynformInputReadOnly";
        }
        else if (input.isRequired()) {
            return "dynformInputRequired";
        }
        else {                                         // read-write and not required
            return "dynformInput";
        }    
    }

    private void setMaxLabelChars(Label label) {
        _maxLabelChars = Math.max(_maxLabelChars, ((String) label.getText()).length());
    }

    public int getMaxLabelWidth() {
        return _maxLabelChars * FONT_WIDTH ;
    }


    private void setMaxDropDownChars(String text) {
        _maxDropDownChars = Math.max(_maxDropDownChars, text.length());        
    }

    public int getMaxDropDownWidth() {
        return (_maxDropDownChars * FONT_WIDTH) + DROPDOWN_BUTTON_WIDTH;
    }


    private void setMaxTextValueChars(String text) {
        if (text != null)
            _maxTextValueChars = Math.max(_maxTextValueChars, text.length());
    }

    public int getMaxTextValueWidth() {
        return _maxTextValueChars * FONT_WIDTH;
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


    public Hashtable<FieldBase, DynFormField> getTextFieldMap() {
        return _componentFieldTable;
    }


}
