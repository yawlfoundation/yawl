package org.yawlfoundation.yawl.resourcing.jsf;

/**
 * Author: Michael Adams
 * Creation Date: 19/01/2008
 */

import com.sun.rave.web.ui.appbase.AbstractSessionBean;
import com.sun.rave.web.ui.component.*;

import javax.faces.component.UIComponent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.el.MethodBinding;
import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import java.util.List;
import java.util.Map;
import java.util.Date;
import java.util.Iterator;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.jdom.Element;
import org.yawlfoundation.yawl.util.JDOMUtil;

public class DynFormFactory extends AbstractSessionBean {

    // required JSF member and method
    private int __placeholder;

    private void _init() throws Exception { }


    // these are components of the dynamic form that are managed by this object

    private PanelLayout compPanel = new PanelLayout();

    public PanelLayout getCompPanel() { return compPanel; }

    public void setCompPanel(PanelLayout pl) { compPanel = pl; }


    /****************************************************************************/

    private String headerText;

    public String getHeaderText() { return headerText; }

    public void setHeaderText(String text) { headerText = text ; }


    private String btnOKStyle ;

    public String getBtnOKStyle() {
        return btnOKStyle;
    }

    public void setBtnOKStyle(String btnOKStyle) {
        this.btnOKStyle = btnOKStyle;
    }

    private String btnCancelStyle ;

    public String getBtnCancelStyle() {
        return btnCancelStyle;
    }

    public void setBtnCancelStyle(String btnCancelStyle) {
        this.btnCancelStyle = btnCancelStyle;
    }



    private String title ;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    private String focus ;

    public String getFocus() {
        return focus;
    }

    public void setFocus(String focus) {
        this.focus = focus;
    }


    boolean dynFormPost ;

    public boolean isDynFormPost() {
        return dynFormPost;
    }

    public void setDynFormPost(boolean dynFormPost) {
        this.dynFormPost = dynFormPost;
    }

    private List fieldList ;

    public List getFieldList() {
        return compPanel.getChildren();
    }

    public void setFieldList(List fieldList) {
        this.fieldList = fieldList;
    }

    private long instance = System.currentTimeMillis();

    public long getX() { return instance; }

    public void initDynForm(List<FormParameter> params, String title) {
        int lineHeight = 25 ;                      // nbr of pixels between each row
        int startingYPos = 10 ;
        int line = 0 ;
        boolean focusSet = false ;

        setTitle(title);

        compPanel.getChildren().clear();                     // start with clean panel
 
        for (FormParameter param : params) {

            // increment y-coord for each line
            String topStyle = String.format("top: %dpx",
                                             line++ * lineHeight + startingYPos) ;

            // create and add the appropriate input field
            UIComponent field ;
            if (! isPrimitiveType(param.getDataTypeName()))
                field = makeComplexType(param, topStyle) ;
            else
                field = makeSimpleType(param, topStyle) ;

            compPanel.getChildren().add(field);
            if (! focusSet) focusSet = setFocus(field) ;
        }

        // resize page panel for the number of fields added
        int height = (compPanel.getChildCount() * lineHeight + startingYPos) / 2;
        String heightStyle = String.format("height: %dpx", height);
        compPanel.setStyle(heightStyle);

        // reposition buttons to go directly under resized panel
        int btnOrigYPos = 225;
        String topStyle = String.format("top: %dpx", btnOrigYPos + height) ;
        btnOKStyle = "left: 270px; " + topStyle;
        btnCancelStyle = "left: 170px; " + topStyle;
    }


    public Map<String, FormParameter> updateValues(Map<String, FormParameter> params) {
        List compList = compPanel.getChildren();
        for (Object o : compList) {
            String name ;
            FormParameter param ;

            // ignore labels and read-only fields
            if (o instanceof TextField) {
                TextField textField = (TextField) o ;
                name = textField.getId().substring(3);
                param = params.get(name) ;
                if (param != null) {
                    param.setValue((String) textField.getValue());
                }
            }
            else if (o instanceof Checkbox) {
                Checkbox cbox = (Checkbox) o ;
                name = cbox.getId().substring(3);
                param = params.get(name) ;
                if (param != null) {
                    Boolean selected = (Boolean) cbox.getValue() ;
                    param.setValue(selected.toString());
                }    
            }
            else if (o instanceof Calendar) {
                Calendar cal = (Calendar) o ;
                name = cal.getId().substring(3);
                param = params.get(name) ;
                if (param != null) {
                    String val = new SimpleDateFormat("yyyy-MM-dd")
                                     .format(cal.getSelectedDate());
                    param.setValue(val);
                }
            }
        }
        return params;
    }


    public String rPadSp(String str, int padlen) {
        int len = padlen - str.length();
        if (len < 1) return str ;

        StringBuilder result = new StringBuilder(str) ;
        for (int i = 0; i < len; i++) {
            result.append("&nbsp;");
        }
        return result.toString();
    }


    private Element getTypeDef(FormParameter param) {
        Element result = null ;

        // get schema library for the spec this param is a member of
        String library = ((SessionBean) getBean("SessionBean"))
                                            .getSchemaLibrary("dummyID");
        Element eLibrary = JDOMUtil.stringToElement(library);

        // search the library for the definition of this param's type
        String dataType = param.getDataTypeName();
        Iterator libItr = eLibrary.getChildren().iterator();
        while (libItr.hasNext()) {
            Element schema = (Element) libItr.next() ;
            if (schema.getAttributeValue("name").equals(dataType)) {
                result = schema ;
                break ;                               // found & done
            }
        }
        return result;
    }


    public UIComponent makeSimpleType(FormParameter param, String topStyle) {
        UIComponent field ;

        // create and add a label for the parameter
        compPanel.getChildren().add(makeLabel(param, topStyle));

        if (param.getDataTypeName().equals("boolean"))
            field = makeCheckbox(param, topStyle);
        else if (param.getDataTypeName().equals("date"))
            field = makeCalendar(param, topStyle);
        else {
            if (param.isInputOnly())
                field = makeReadOnlyTextField(param, topStyle);
            else
                field = makeTextField(param, topStyle);
        }
        return field ;
    }


    public PanelLayout makeComplexType(FormParameter param, String topStyle) {
        PanelLayout result = null;
        Element schema = getTypeDef(param);

        if (schema != null) {
            result = composeComplexType(schema, param) ;
            // parse the data type schema

            // each element can be:
            // - another complex type - recurse
            // - sequence with min & max - affects the following elements
            // - element withe name, min & max - affects the following elements
            // - element with name and type
            //    - if type is complex, recurse to top
            //    - if type is primitive, render it
        }
        return result ;
    }


    private PanelLayout composeComplexType(Element schema, FormParameter param) {
        PanelLayout result = new PanelLayout();

        Element sequence = schema.getChild("sequence");

        // assume all children are 'element' types
        Iterator itr = sequence.getChildren().iterator();
        while (itr.hasNext()) {
            Element element = (Element) itr.next();
            String dataType = element.getAttributeValue("type");
            UIComponent component;
            if (dataType != null) {
                if (! isPrimitiveType(dataType))
                   component = makeComplexType(param, null) ;
                else
                   component = makeSimpleType(param, null) ;

                result.getChildren().add(component) ;
            }
        }



//        int seqMin, seqMax, eleMin, eleMax ;
//
//        String name = schema.getAttributeValue("name");
//        Label headLabel = makeSimpleLabel(name);
//        result.getChildren().add(headLabel);               // todo: style for position
//
//        Iterator itr = schema.getChildren().iterator();
//        while (itr.hasNext()) {
//            Element child = (Element) itr.next();
//            String eleName = child.getName();
//            if (eleName.equals("complexType"))
//                composeComplexType(child, param);           // recurse for inner type
//            else if (eleName.equals("sequence")) {
//                seqMin = new Integer(child.getAttributeValue("minOccurs"));
//                seqMax = new Integer(child.getAttributeValue("maxOccurs"));
//            }
//            else if (eleName.equals("element")) {
//                String dataType = child.getAttributeValue("type");
//                if (dataType != null) {
//                    if (isPrimitiveType(dataType))
//                        makeSimpleType(param, null);
//                    else
//                        makeComplexType(param, null);       // todo: build component
//                }
//                else {    // element with no type def
//                    eleMin = new Integer(child.getAttributeValue("minOccurs"));
//                    eleMax = new Integer(child.getAttributeValue("maxOccurs"));
//                    // get name
//                }
//            }
//        }

        return result ;
    }


    public Label makeLabel(FormParameter param, String topStyle) {
        Label label = makeSimpleLabel(param.getName()) ;
        label.setStyleClass("dynformLabel");
        label.setStyle(topStyle) ;
        label.setRequiredIndicator(param.isMandatory() || param.isRequired());
        return label;
    }


    public Label makeSimpleLabel(String text) {
        Label label = new Label() ;
        label.setId("lbl" + text);
        label.setText(text + ": ");
        return label;
    }


    public Checkbox makeCheckbox(FormParameter param, String topStyle) {
        Checkbox cbox = new Checkbox();
        cbox.setId("cbx" + param.getName());
        String val = param.getValue() ;
        if (val == null) val = "false" ;
        cbox.setSelected(val.equalsIgnoreCase("true")) ;
        cbox.setReadOnly(param.isInputOnly());
        cbox.setStyleClass("dynformInput");
        cbox.setStyle(topStyle) ;
  //      cbox.setValueChangeListener(bindListener());
        return cbox ;
    }


    public Calendar makeCalendar(FormParameter param, String topStyle) {
        Calendar cal = new Calendar();
        cal.setId("cal" + param.getName());
        cal.setSelectedDate(createDate(param.getValue()));
        cal.setDateFormatPatternHelp("");        
        cal.setReadOnly(param.isInputOnly());
        cal.setColumns(15);
        cal.setStyleClass("dynformInput");
        cal.setStyle(topStyle) ;
        return cal;
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
        panel.setId("pnl" + param.getName());
        panel.setPanelLayout("flow");
        panel.setStyleClass("dynformReadOnlyPanel");
        panel.setStyle(topStyle);

        StaticText roText = new StaticText() ;
        roText.setId("stt" + param.getName());
        roText.setText(param.getValue());
        roText.setStyleClass("dynformReadOnlyText") ;
        panel.getChildren().add(roText) ;
        return panel ;
    }


    public TextField makeTextField(FormParameter param, String topStyle) {
        TextField textField = new TextField() ;
        textField.setId("txt" + param.getName());
        textField.setText(param.getValue());
        textField.setRequired(param.isMandatory() || param.isRequired());
        textField.setStyleClass("dynformInput");
        textField.setStyle(topStyle);
  //      textField.setValueChangeListener(bindListener());

        return textField ;
    }

    private MethodBinding bindListener() {
        Application app = FacesContext.getCurrentInstance().getApplication();
        return app.createMethodBinding("#{dynForm.saveValueChange}",
                                                  new Class[]{ValueChangeEvent.class});
    }


    public boolean setFocus(UIComponent component){
        if (component instanceof PanelLayout) return false ;
        setFocus("form1:" + component.getId());
        return true ;
    }

    private Date createDate(String dateStr) {
        // set the date to the param's input value if possible, else default to today
        Date result = new Date() ;

        if (dateStr != null) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                result = sdf.parse(dateStr) ;
            }
            catch (ParseException pe) {
                // nothing to do - accept new Date default of today
            }
        }
        return result ;
    }

    
    private boolean isPrimitiveType(String type) {
        return (type.equals("string")  ||
                type.equals("double")  ||
                type.equals("long")    ||
                type.equals("boolean") ||
                type.equals("date")    ||
                type.equals("time")    ||
                type.equals("duration"));
    }


}
