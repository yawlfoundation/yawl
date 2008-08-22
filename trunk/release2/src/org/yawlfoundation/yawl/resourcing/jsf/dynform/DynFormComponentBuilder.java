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

    private final int FONT_WIDTH = 6;



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
        if (! name.startsWith("choice"))
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
        }
        return subPanel ;
    }


    private boolean isRequired(FormParameter param) {
 //       return (param.isRequired() || param.isMandatory());
        return false;
    }


    public DynFormComponentList makeInputField(int top, DynFormField input) {
        UIComponent field;
        DynFormComponentList result = new DynFormComponentList();

        String type = input.getDataTypeUnprefixed();

        // create and add a label for the parameter
        Label label = makeLabel(input, top);

        if (type.equals("boolean"))
            field = makeCheckbox(input, top);
        else if (type.equals("date"))
            field = makeCalendar(input, top);
        else if (input.hasEnumeratedValues())
            field = makeEnumeratedList(input, top);
        else
            field = makeTextField(input, top);

        label.setFor(field.getId());

        result.add(label);
        result.add(field);

        if (! focusSet) focusSet = setFocus(field) ;

        return result ;
    }


    private String makeTopStyle(int top) {

        // increment y-coord for each component's top (relative to current panel)
        return String.format("top: %dpx", top) ;

    }


    public Label makeLabel(DynFormField input, int top) {
        Label label = makeSimpleLabel(input.getName()) ;
        label.setStyleClass("dynformLabel");
        label.setRequiredIndicator(false);
        label.setStyle(makeTopStyle(top + 5)) ;
        setMaxLabelChars(label);
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
        cbox.setStyle(makeTopStyle(top)) ;
        return cbox ;
    }


    public Calendar makeCalendar(DynFormField input, int top) {
        Calendar cal = new Calendar();
        cal.setId(_factory.createUniqueID("cal" + input.getName()));
        cal.setSelectedDate(createDate(input.getValue()));
        cal.setDateFormatPatternHelp("");
        cal.setDisabled(input.isInputOnly());
        cal.setRequired(input.isRequired());
        cal.setMinDate(new Date(1));
        cal.setMaxDate(getDate(25));
        cal.setColumns(15);
        cal.setStyleClass(getInputStyleClass(input));
        cal.setStyle(makeTopStyle(top)) ;
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
        dropdown.setStyle(makeTopStyle(top)) ;
        dropdown.setItems(getEnumeratedList(input));
        dropdown.setSelected(input.getValue());
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

    /**
     * Readonly textFields are rendered to look like labels - so this method fakes
     * a textfield's visuals, but with a grayed background and italic text
     * @param param the parameter for which the value is being displayed
     * @param topStyle a setting for the Y-coord
     * @return the 'faked' readonly textField component (actually a PanelLayout)
     */
    public PanelLayout makeReadOnlyTextField(FormParameter param, String topStyle) {
        PanelLayout panel = new PanelLayout();
        panel.setId(_factory.createUniqueID("pnl" + param.getName()));
        panel.setPanelLayout("flow");
        panel.setStyleClass("dynformReadOnlyPanel");
        panel.setStyle(topStyle);

        StaticText roText = new StaticText() ;
        roText.setId(_factory.createUniqueID("stt" + param.getName()));
        roText.setText(param.getValue());
        roText.setStyleClass("dynformReadOnlyText") ;
        panel.getChildren().add(roText) ;
        return panel ;
    }


    public TextField makeTextField(DynFormField input, int top) {
        TextField textField = new TextField() ;
        textField.setId(_factory.createUniqueID("txt" + input.getName()));
        textField.setText(JDOMUtil.decodeEscapes(input.getValue()));
        setMaxTextValueChars(input.getValue());
        textField.setStyleClass(getInputStyleClass(input));
        textField.setStyle(makeTopStyle(top));
        textField.setDisabled(input.isInputOnly());
        textField.setToolTip(input.getToolTip());
        _componentFieldTable.put(textField, input);        // store for validation later
        return textField ;
    }


    public RadioButton makeRadioButton(DynFormField input, int top) {
        RadioButton rb = new RadioButton();
        rb.setId(_factory.createUniqueID("rb" + input.getName()));
        rb.setLabel("");
        rb.setName(input.getChoiceID());
        rb.setStyle(makeTopStyle(top));
        rb.setStyleClass("dynformRadioButton");        
        return rb;
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
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                result = sdf.parse(dateStr) ;
            }
            catch (ParseException pe) {
                result = new Date();
            }
        }
        return result ;
    }


    private String getInputStyleClass(DynFormField input) {
        if (input.isRequired())
            return "dynformInputRequired";
        else
             return "dynformInput";
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
        return (_maxDropDownChars * FONT_WIDTH) + 15;
    }


    private void setMaxTextValueChars(String text) {
        if (text != null)
            _maxTextValueChars = Math.max(_maxTextValueChars, text.length());
    }

    public int getMaxTextValueWidth() {
        return _maxTextValueChars * FONT_WIDTH;
    }

    public Hashtable<TextField, DynFormField> getTextFieldMap() {
        return _componentFieldTable;
    }
    

}
