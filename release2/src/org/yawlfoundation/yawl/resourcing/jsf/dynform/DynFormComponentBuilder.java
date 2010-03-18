/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing.jsf.dynform;

import com.sun.rave.web.ui.component.*;
import com.sun.rave.web.ui.model.Option;
import org.yawlfoundation.yawl.util.JDOMUtil;

import javax.faces.component.UIComponent;
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
    private Hashtable<TextField, DynFormField> _componentFieldTable;

    private int _maxDropDownChars = 0;
    private int _maxLabelChars = 0;
    private int _maxTextValueChars = 0;
    private boolean _hasCheckboxOnly = true;

    private final int FONT_WIDTH = 6;
    private final int DROPDOWN_BUTTON_WIDTH = 15;
    private final SimpleDateFormat _sdf = new SimpleDateFormat("yyyy-MM-dd");
    

    public DynFormComponentBuilder() { }

    public DynFormComponentBuilder(DynFormFactory factory) {
        _factory = factory;
        _componentFieldTable = new Hashtable<TextField, DynFormField>();
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


    public DynFormComponentList makeInputField(int top, DynFormField input) {
        UIComponent field;
        int startingTop = top;
        DynFormComponentList result = new DynFormComponentList();

        DynFormComponentList preList = makePreComponents(top, input) ;
        if (! preList.isEmpty()) {
            result.addAll(preList);      // line, text and/or image before
            top += preList.getHeight() + 25;
        }

        String type = input.getDataTypeUnprefixed();

        // create and add a label for the parameter
        Label label = makeLabel(input, top);

        if (type.equals("boolean"))
            field = makeCheckbox(input, top);
        else {
            _hasCheckboxOnly = false;
            if (type.equals("date"))
                field = makeCalendar(input, top);
            else if (input.hasEnumeratedValues())
                field = makeEnumeratedList(input, top);
            else
                field = makeTextField(input, top);
        }
        
        label.setFor(field.getId());

        result.add(label);
        result.add(field);

        DynFormComponentList postList = makePostComponents(top, input) ;
        if (! postList.isEmpty()) {
            result.addAll(postList);  // line, text and/or image after
            top += postList.getHeight();
        }
        result.setHeight(top - startingTop);

        if (! focusSet) focusSet = setFocus(field) ;

        return result ;
    }


    private DynFormComponentList makePreComponents(int top, DynFormField input) {
        DynFormComponentList list = new DynFormComponentList();
        String textAbove = input.getTextAbove();
        if (textAbove != null) {
            list.add(makeStaticTextBlock(input, top));
            top += 25;
        }
        if (input.isLineAbove()) {
            list.add(makeFlatPanel(top));
            top += 25;
        }
        String imagePath = input.getImageAbove();
        if (imagePath != null) {
            ImageComponent image = makeImageComponent(top, imagePath);
            list.add(image);
            top += image.getHeight();
        }
        list.setHeight(top);
        return list;
    }


    private DynFormComponentList makePostComponents(int top, DynFormField input) {
        DynFormComponentList list = new DynFormComponentList();
        String textBelow = input.getTextBelow();
        if (textBelow != null) {
            list.add(makeStaticTextBlock(input, top));
            top += 25;
        }
        if (input.isLineBelow()) {
            list.add(makeFlatPanel(top));
            top += 25;
        }
        String imagePath = input.getImageBelow();
        if (imagePath != null) {
            ImageComponent image = makeImageComponent(top, imagePath);
            list.add(image);
            top += image.getHeight();
        }
        list.setHeight(top);
        return list;

    }

    private String makeStyle(UIComponent field, DynFormField input, int top) {

        // increment y-coord for each component's top (relative to current panel)
        String style = String.format("top: %dpx%s", top, input.getUserDefinedFontStyle());

        if (field instanceof TextField || field instanceof DropDown) {
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
        if (input.hasHideAttribute()) {
            label.setVisible(false);
        }
        else {
            setMaxLabelChars(label);
        }
        return label;
    }


    public Label makeSimpleLabel(String text) {
        Label label = new Label() ;
        label.setId(_factory.createUniqueID("lbl" + text));
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
        cbox.setVisible(! input.hasHideAttribute());
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
        cal.setVisible(! input.hasHideAttribute());
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
        dropdown.setVisible(! input.hasHideAttribute());
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


    public TextField makeTextField(DynFormField input, int top) {
        TextField textField = new TextField() ;
        textField.setId(_factory.createUniqueID("txt" + input.getName()));
        textField.setText(JDOMUtil.decodeEscapes(input.getValue()));
        setMaxTextValueChars(input.getValue());
        textField.setStyleClass(getInputStyleClass(input));
        textField.setStyle(makeStyle(textField, input, top));
        textField.setDisabled(input.isInputOnly());
        textField.setToolTip(input.getToolTip());
        textField.setMaxLength(input.getMaxLength());
        if (input.hasHideAttribute()) {
            textField.setVisible(false);
        }
        else {
            _componentFieldTable.put(textField, input);     // store for validation later
        }
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
        rb.setVisible(! input.hasHideAttribute());
        return rb;
    }


    private StaticTextBlock makeStaticTextBlock(DynFormField input, int top) {
        StaticTextBlock block = new StaticTextBlock() ;
        block.setId(_factory.createUniqueID("stb"));
        block.setStyle(String.format("position: absolute; left: 5px; top: %dpx%s",
                top, input.getUserDefinedFontStyle()));
        return block; 
    }


    private FlatPanel makeFlatPanel(int top) {
        FlatPanel line = new FlatPanel();
        line.setId(_factory.createUniqueID("fpl"));
        line.setStyle(String.format(
                "position: absolute; border: 2px solid gray; left: 5px; height: 2px; top: %dpx", top));
        return line;
    }


    private ImageComponent makeImageComponent(int top, String imagePath) {
        ImageComponent image = new ImageComponent();
        image.setId(_factory.createUniqueID("img"));
        image.setUrl(imagePath);
        image.setStyle(String.format("position: absolute; left: 5px; top: %dpx", top));
        return image;
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

    public boolean hasOnlyCheckboxes() {
        return _hasCheckboxOnly ;
    }

    public int getMaxFieldWidth() {
        return Math.max(getMaxDropDownWidth(), getMaxTextValueWidth());
    }


    public Hashtable<TextField, DynFormField> getTextFieldMap() {
        return _componentFieldTable;
    }


}
