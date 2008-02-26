package org.yawlfoundation.yawl.resourcing.jsf;

/**
 * Author: Michael Adams
 * Creation Date: 19/01/2008
 */

import com.sun.rave.web.ui.appbase.AbstractSessionBean;
import com.sun.rave.web.ui.component.*;
import com.sun.rave.web.ui.component.Calendar;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.util.JDOMUtil;

import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import javax.faces.event.ActionEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class DynFormFactory extends AbstractSessionBean {

    // required JSF member and method
    private int __placeholder;

    private void _init() throws Exception { }

    /****************************************************************************/

    // components & settings of the dynamic form that are managed by this object

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


    private String title ;

    public String getTitle() { return title; }

    public void setTitle(String s) { title = s; }


    private String focus ;

    public String getFocus() { return focus; }

    public void setFocus(String s) { focus = s; }

    
    /****************************************************************************/

    // the wir currently populating the dynamic form
    private WorkItemRecord displayedWIR ;

    public WorkItemRecord getDisplayedWIR() { return displayedWIR; }

    public void setDisplayedWIR(WorkItemRecord wir) { displayedWIR = wir; }

    // a running set of component id's - used to ensure id uniqueness
    private Set<String> usedIDs = new HashSet<String>();

    // the set of generated subpanels on the current form
    private Set<SubPanelController> subPanelSet = new HashSet<SubPanelController>();

    // the 'status' of the component add process
    private enum ComponentType { nil, panel, field }

    // some constants for layout arithmetic
    static final int Y_NF_INCREMENT = 30 ;      // inc of y coord from start to 1st field
    static final int Y_FN_INCREMENT = 30 ;      // inc of y coord from last field to end
    static final int Y_PN_INCREMENT = 10 ;      // inc of y coord from last panel to end
    static final int Y_FF_INCREMENT = 25 ;      // inc of y coord between two fields
    static final int Y_PP_INCREMENT = 10 ;      // inc of y coord between two panels
    static final int Y_PF_INCREMENT = 20 ;      // inc of y coord between panel -> field
    static final int Y_FP_INCREMENT = 30 ;      // inc of y coord between field -> panel
    static final int SUBPANEL_INSET = 10 ;      // gap between panel side walls
    static final int PANEL_BASE_WIDTH = 250;    // width of innermost panel
    static final int OUTER_PANEL_TO_BUTTONS = 15;   // gap from panel bottom to buttons
    static final int OUTER_PANEL_TOP = 130;      // top (y) coord of outer panel
    static final int OUTER_PANEL_LEFT = 50;      // left (x) coord of outer panel
    static final int FORM_BUTTON_WIDTH = 66;     // buttons under outer panel
    static final int FORM_BUTTON_GAP = 30;       // ... and the gap between them

    // for setting focus on first available component
    private boolean focusSet = false ;

    /*********************************************************************************/

    /** @return a reference to the sessionbean */
    private SessionBean getSessionBean() {
        return (SessionBean) getBean("SessionBean") ;
    }


    /**
     * Initialises a new dynamic form
     * @param title the page title
     */
    public void initDynForm(String title) {
        setTitle(title);

        // start with a clean form
        compPanel.getChildren().clear();
        usedIDs.clear();
        subPanelSet.clear();

        // get schema and data for case/workitem
        String schema = getSchema();
        if (schema != null) {
            String data = getInstanceData(schema) ;
            buildForm(schema, data);
        }
    }


    /** @return the data schema for the case/item to be displayed */
    private String getSchema() {
        String result ;
        SessionBean sb = getSessionBean() ;
        if (sb.getDynFormLevel().equals("case"))
            result = sb.getCaseSchema() ;
        else
            result = sb.getTaskSchema(displayedWIR);
        return result;
    }


    /** @return the instance data for the currently displayed case/workitem */
    private String getInstanceData(String schema) {
        String result ;
        SessionBean sb = getSessionBean() ;
        if (sb.getDynFormLevel().equals("case"))
            result = sb.getInstanceData(schema) ;
        else
            result = sb.getInstanceData(schema, displayedWIR);
        return result;
    }



    private void buildForm(String schema, String dataStr) {
        Element data = JDOMUtil.stringToElement(dataStr);
        Document doc = JDOMUtil.stringToDocument(schema);
        Element root = doc.getRootElement();                        // schema
        Namespace ns = root.getNamespace();
        Element element = root.getChild("element", ns) ;
        String topLabel = element.getAttributeValue("name") ;       // name of case/task
        List content = buildInnerForm(null, data, element, ns, -1) ;
        compPanel.getChildren().add(makeHeaderText(topLabel)) ;
        compPanel.getChildren().addAll(content) ;
        sizeAndPositionContent(content) ;
    }


    private int calcHeight(List content) {
        int height = Y_NF_INCREMENT ;
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

                height += getNextInc(prevComponent, currComponent);
                prevComponent = currComponent;
            }
        }

        return height ;
    }


    private void sizeAndPositionContent(List content) {
        int maxLevel = -1 ;                          // -1 means no inner subpanels

        // set the size and position of inner panels relative to their nested level
        if (! subPanelSet.isEmpty()) {
            maxLevel = getMaxDepthLevel();
            for (SubPanelController spc : subPanelSet)
                spc.assignStyleToSubPanels(maxLevel);
        }

        // calc and set height and width of outermost panel
        int height = calcHeight(content) ;
        int width =  PANEL_BASE_WIDTH + (SUBPANEL_INSET * 2 * (maxLevel + 2)) ;
        String style = String.format("height: %dpx; width: %dpx", height, width);
        compPanel.setStyle(style);

        // reposition buttons to go directly under resized panel, centered
        int btnTop = OUTER_PANEL_TOP + height + OUTER_PANEL_TO_BUTTONS;
        int btnCancelLeft = OUTER_PANEL_LEFT +
                ( (width - (2 * FORM_BUTTON_WIDTH) - FORM_BUTTON_GAP) / 2 ) ;
        int btnOKLeft = btnCancelLeft + FORM_BUTTON_WIDTH + FORM_BUTTON_GAP;
        btnOKStyle = String.format("left: %dpx; top: %dpx", btnOKLeft, btnTop);
        btnCancelStyle = String.format("left: %dpx; top: %dpx", btnCancelLeft, btnTop);
    }

    
    private DynFormContentList buildInnerForm(SubPanel container, Element data, Element element,
                                Namespace ns, int level) {
        DynFormContentList result = new DynFormContentList();
        DynFormContentList innerContent ;
        int top = 0 ;                                  // top (yPos) posn of component
        ComponentType prevComponent = ComponentType.nil ;

        ++level ;                                     // increment nested depth level

        Element complex = element.getChild("complexType", ns);
        Element sequence = complex.getChild("sequence", ns);

        List content = sequence.getChildren() ;
        Iterator itr = content.iterator();
        while (itr.hasNext()) {
            Element field = (Element) itr.next();
            String minOccurs = field.getAttributeValue("minOccurs");
            String maxOccurs = field.getAttributeValue("maxOccurs");
            String name = field.getAttributeValue("name");
            String type = field.getAttributeValue("type");

            if (type == null) {

                // new complex type - recurse in a new container
                SubPanelController spc = null;
                Element subData = (data != null)? data.getChild(name) : null;


                int instances = getInitialInstanceCount(minOccurs, subData) ;
                for (int i = 0; i < instances; i++) {
                    top += getNextInc(prevComponent, ComponentType.panel);
                    SubPanel subPanel = makeSubPanel(top, name, level, minOccurs, maxOccurs);
                    Element e = (instances > 1) ? getIteratedContent(subData, i) : subData ;
                    innerContent = buildInnerForm(subPanel, e, field, ns, level);
                    subPanel.getChildren().addAll(innerContent);
                    result.add(subPanel);
                    if (spc != null)
                        spc.storeSubPanel(subPanel);
                    else
                        spc = subPanel.getController();
                    top += subPanel.getHeight() ;
                    prevComponent = ComponentType.panel ;
                }
            }
            else  {

                // if min and/or max defined at the field level, add buttons to container
                if ((container != null) && ((minOccurs != null) || (maxOccurs != null))) {
                    SubPanelController controller = container.getController();
                    if (container.getBtnPlus() == null) {
                        controller.setMaxOccurs(maxOccurs);
                        controller.setMinOccurs(minOccurs);
                        controller.setCurrOccurs(1);

                        if (controller.canVaryOccurs()) {
                            container.addOccursButton(makeOccursButton(
                                                             container.getName(), "+"));
                            container.addOccursButton(makeOccursButton(
                                                             container.getName(), "-"));
                            controller.setOccursButtonsEnablement();
                        }
                    }
                }

                // create the field (for first panel)
                top += getNextInc(prevComponent, ComponentType.field);

                String value = (data != null)? data.getChildText(name) : "";
                innerContent = makeInputField(name, type, top, value);
                if (container != null)
                    container.getController().addSimpleContent(innerContent, top);
                result.addAll(innerContent);
                prevComponent = ComponentType.field ;
            }
        }
        if (container != null)
            container.setHeight(top + getNextInc(prevComponent, ComponentType.nil)) ;
        
        return result;
    }


    private int getInitialInstanceCount(String min, Element data) {
        int dataCount = 1;
        int minOccurs = 1;
        if ((data != null) && (data.getContentSize() > 1)) {
            String dataName = ((Element) data.getContent(0)).getName();
            dataCount = data.getChildren(dataName).size();
        }
        if (min != null)
            minOccurs = Math.max(SubPanelController.convertOccurs(min), 1) ;

        return Math.max(minOccurs, dataCount) ;
    }


    private Element getIteratedContent(Element data, int index) {
        Element result = null ;
        if ((data != null) && (index < data.getContentSize())) {
            result = new Element(data.getName());
            Element iteratedContent = (Element) data.getContent(index);
            result.addContent((Element) iteratedContent.clone());
        }
        return result ;
    }

    
    public SubPanel makeSubPanel(int top, String name, int level,
                                 String minOccurs, String maxOccurs) {
        SubPanel subPanel = new SubPanel();
        subPanel.setTop(top);
        subPanel.setName(name);
        subPanel.setId(createUniqueID("sub" + name));
        subPanel.getChildren().add(makeHeaderText(name)) ;

        SubPanelController spc = new SubPanelController(subPanel, minOccurs,
                                                        maxOccurs, level) ;
        if (spc.canVaryOccurs()) {
            subPanel.addOccursButton(makeOccursButton(name, "+"));
            subPanel.addOccursButton(makeOccursButton(name, "-"));
        }
        subPanelSet.add(spc);
        subPanel.setController(spc);
        return subPanel ;
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

    public DynFormContentList makeInputField(String name, String type, int top, String value) {
        UIComponent field;
        DynFormContentList result = new DynFormContentList();

        // create and add a label for the parameter
        Label label = makeLabel(name, makeTopStyle(top));

        if (type.equals("xsd:boolean"))
            field = makeCheckbox(name, top, value);
        else if (type.equals("xsd:date"))
            field = makeCalendar(name, top, value);
        else {
//            if (param.isInputOnly())
//                field = makeReadOnlyTextField(param, topStyle);
//            else
                field = makeTextField(name, type, top, value);
        }

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


    public Label makeLabel(String name, String topStyle) {
        Label label = makeSimpleLabel(name) ;
        label.setStyleClass("dynformLabel");
        label.setStyle(topStyle) ;
  //      label.setRequiredIndicator(param.isMandatory() || param.isRequired());
        return label;
    }


    public Label makeSimpleLabel(String text) {
        Label label = new Label() ;
        label.setId(createUniqueID("lbl" + text));
        label.setText(text + ": ");
        return label;
    }


    public StaticText makeHeaderText(String text) {
        StaticText header = new StaticText() ;
        header.setId(createUniqueID("stt" + text));
        header.setText(text);
        header.setStyleClass("dynFormPanelHeader");
        return header;
    }


    public Checkbox makeCheckbox(String name, int top, String value) {
        Checkbox cbox = new Checkbox();
        cbox.setId(createUniqueID("cbx" + name));
        if (value == null) value = "false" ;
        cbox.setSelected(value.equalsIgnoreCase("true")) ;
 //       cbox.setReadOnly(param.isInputOnly());
        cbox.setStyleClass("dynformInput");
        cbox.setStyle(makeTopStyle(top)) ;
  //      cbox.setValueChangeListener(bindListener());
        return cbox ;
    }


    public Calendar makeCalendar(String name, int top, String value) {
        Calendar cal = new Calendar();
        cal.setId(createUniqueID("cal" + name));
        cal.setSelectedDate(createDate(value));
        cal.setDateFormatPatternHelp("");
    //    cal.setReadOnly(param.isInputOnly());
        cal.setColumns(15);
        cal.setStyleClass("dynformInput");
        cal.setStyle(makeTopStyle(top)) ;
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
        panel.setId(createUniqueID("pnl" + param.getName()));
        panel.setPanelLayout("flow");
        panel.setStyleClass("dynformReadOnlyPanel");
        panel.setStyle(topStyle);

        StaticText roText = new StaticText() ;
        roText.setId(createUniqueID("stt" + param.getName()));
        roText.setText(param.getValue());
        roText.setStyleClass("dynformReadOnlyText") ;
        panel.getChildren().add(roText) ;
        return panel ;
    }



    public TextField makeTextField(String name, String type, int top, String value) {
        TextField textField = new TextField() ;
        textField.setId(createUniqueID("txt" + name));
        textField.setText(value);
     //   textField.setRequired(param.isMandatory() || param.isRequired());
        textField.setStyleClass("dynformInput");
        textField.setStyle(makeTopStyle(top));
  //      textField.setValueChangeListener(bindListener());
        textField.setToolTip(makeToolTip(type));
        return textField ;
    }


    private String makeToolTip(String type) {
        type = type.substring(4) ;
        return "Please enter a value of " + type + " type";
    }

    private MethodBinding bindOccursButtonListener() {
        Application app = FacesContext.getCurrentInstance().getApplication();
        return app.createMethodBinding("#{dynForm.btnOccursAction}",
                                                  new Class[]{ActionEvent.class});
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

    
    private String createUniqueID(String id) {
        int suffix = 0 ;
        while (usedIDs.contains(id + ++suffix)) ;
        String result = id + suffix;
        usedIDs.add(result);
        return result ;
    }


    private int getMaxDepthLevel() {
        int result = 0 ;
        for (SubPanelController spc : subPanelSet)
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
                          else
                              result = Y_PN_INCREMENT ;
                          break ;
                        }

            case field: { if (curr == ComponentType.panel)
                              result = Y_FP_INCREMENT ;
                          else if (curr == ComponentType.field)
                              result = Y_FF_INCREMENT ;
                          else
                              result = Y_FN_INCREMENT ;
                         }
        }
        return result ;
    }

    private void addSubPanel(SubPanel panel) {
        SubPanel newPanel = cloneSubPanel(panel) ;

        // get container of this panel
        UIComponent parent = panel.getParent();
        parent.getChildren().add(newPanel);

        SubPanel level0Container = panel.getController().addSubPanel(newPanel);
        int adjustment = newPanel.getHeight() + DynFormFactory.Y_PP_INCREMENT;
        repositionLevel0Panels(level0Container, adjustment, panel.getTop()) ;
    }


    private void removeSubPanel(SubPanel panel) {
        SubPanel level0Container = panel.getController().removeSubPanel(panel);
        int adjustment = - (panel.getHeight() + DynFormFactory.Y_PP_INCREMENT);
        repositionLevel0Panels(level0Container, adjustment, panel.getTop()) ;

        UIComponent parent = panel.getParent();
        parent.getChildren().remove(panel);
    }

    
    private void repositionLevel0Panels(SubPanel container, int adjustment, int top) {
        for (SubPanelController controller : subPanelSet) {
            if (controller.getDepthlevel() == 0) {
                if (controller != container.getController())
                    controller.incSubPanelTops(top, adjustment);
            }
        }
    }


    private String adjustTopStyle(String oldStyle, int adjustment) {

        // extract top value from style string
        String topStr = oldStyle.split(":")[1].replaceFirst("px", "").trim();
        int top = new Integer(topStr) + adjustment ;
        return makeTopStyle(top);
    }



    private SubPanel cloneSubPanel(SubPanel panel) {
        SubPanel newPanel = panel.clone() ;
        String name = newPanel.getName();
        newPanel.setId(createUniqueID("sub" + name));
        
        // clone panel content
        List content = cloneContent(panel);
        if (! content.isEmpty())
            newPanel.getChildren().addAll(content) ;

        
        if (newPanel.getController().canVaryOccurs()) {
            newPanel.addOccursButton(makeOccursButton(name, "+"));
            newPanel.addOccursButton(makeOccursButton(name, "-"));
            newPanel.getBtnPlus().setStyle(panel.getBtnPlus().getStyle());
            newPanel.getBtnMinus().setStyle(panel.getBtnMinus().getStyle());
        }
        return newPanel ;
    }

    private List cloneContent(SubPanel panel) {
//        Map<Object, Integer> tops = new HashMap<Object, Integer>();
        List content = panel.getChildren();
        List result = new ArrayList() ;
        for (Object obj : content) {
            if (obj instanceof SubPanel)
                result.add(cloneSubPanel((SubPanel) obj)) ;               // recurse
            else if (! (obj instanceof Button))  {
                List<UIComponent> newContent = cloneSimpleComponent((UIComponent) obj, panel) ;
//                int top = panel.getController().getHeight((UIComponent) obj);
//                tops.put(newComponent, top);
                if (newContent != null) result.addAll(newContent) ;
            }
        }

        // clone the controller for contents
//        if (! result.isEmpty()) {
//            SubPanelController newController = panel.getController().clone();
//            for (Object obj : result) {
//                if (obj instanceof SubPanel) {
//                   newController.storeSubPanel(((SubPanel) obj)) ;
//                }
//                else
//                   newController.addSimpleContent((UIComponent) obj, tops.get(obj));
//            }
//            subPanelSet.add(newController);
//        }
        return result ;
    }


    private List<UIComponent> cloneSimpleComponent(UIComponent component, SubPanel panel) {
        List<UIComponent> result = new ArrayList<UIComponent>();
        UIComponent newComponent = null ;

        if (component instanceof StaticText)
            newComponent = cloneStaticText(component) ;
        else if (component instanceof Label) {
            Label newLabel = (Label) cloneLabel(component);
            String labelFor = ((Label) component).getFor();
            UIComponent compFor = panel.findComponent(labelFor);
            if (compFor instanceof TextField)
                newComponent = cloneTextField(compFor);
            else if (compFor instanceof Calendar)
                newComponent = cloneCalendar(compFor);
            else if (compFor instanceof Checkbox)
                newComponent = cloneCheckbox(compFor);
//            else
//                result = cloneReadOnlyField(component);

            if (newComponent != null) {
                newLabel.setFor(newComponent.getId());
                result.add(newLabel);
            }
        }
        if (newComponent != null) {
            result.add(newComponent);
            return result ;
        }
        else return null ;
    }


    public UIComponent cloneLabel(UIComponent label) {
        Label oldLabel = (Label) label ;
        Label newLabel = new Label() ;
        newLabel.setText(oldLabel.getText());
        newLabel.setId(createUniqueID(oldLabel.getId())) ;
        newLabel.setStyle(oldLabel.getStyle());
        newLabel.setStyleClass(oldLabel.getStyleClass());
        newLabel.setRequiredIndicator(oldLabel.isRequiredIndicator()) ;
        return newLabel;
    }


    public UIComponent cloneTextField(UIComponent field) {
        TextField oldField = (TextField) field ;
        TextField newField = new TextField() ;
        newField.setText(oldField.getText());
        newField.setId(createUniqueID(oldField.getId()));
        newField.setRequired(oldField.isRequired());
        newField.setStyleClass(oldField.getStyleClass());
        newField.setStyle(oldField.getStyle());
        return newField;
    }

    public UIComponent cloneCalendar(UIComponent field) {
        Calendar oldField = (Calendar) field ;
        Calendar newField = new Calendar() ;
        newField.setId(createUniqueID(oldField.getId()));
        newField.setDateFormatPatternHelp("");
        newField.setReadOnly(oldField.isReadOnly());
        newField.setColumns(oldField.getColumns());
        newField.setStyleClass(oldField.getStyleClass());
        newField.setStyle(oldField.getStyle()) ;
        return newField ;
    }


    public UIComponent cloneCheckbox(UIComponent field) {
        Checkbox oldCbox = (Checkbox) field ;
        Checkbox newCbox = new Checkbox() ;
        newCbox.setId(createUniqueID(oldCbox.getId()));
        newCbox.setSelected(oldCbox.isChecked()) ;
        newCbox.setReadOnly(oldCbox.isReadOnly());
        newCbox.setStyleClass(oldCbox.getStyleClass());
        newCbox.setStyle(oldCbox.getStyle()) ;
        return newCbox ;
    }


    public UIComponent cloneStaticText(UIComponent field) {
        StaticText oldStatic = (StaticText) field ;
        StaticText newStatic = new StaticText() ;
        newStatic.setId(createUniqueID(oldStatic.getId()));
        newStatic.setText(oldStatic.getText());
        newStatic.setStyleClass(oldStatic.getStyleClass());
        return newStatic;
    }

    
    private int getIndexOf(UIComponent parent, SubPanel panel) {
        List components = parent.getChildren();
        for (int i = 0; i < components.size(); i++) {
            UIComponent component = (UIComponent) components.get(i);
            if (component == panel) return i;
        }
        return -1 ;                 // not found
    }

    private void insertSubPanel(UIComponent parent, SubPanel panel, int i) {
        if (i < parent.getChildCount())
            parent.getChildren().add(i, panel);
        else
            parent.getChildren().add(panel);
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
        return getPanelDataList(compPanel) ;
    }


    private String getPanelDataList(PanelLayout panel) {
        StringBuilder result = new StringBuilder();
        DynFormDataFormatter formatter = new DynFormDataFormatter(panel) ;
        result.append(formatter.getHeaderOpen());
        result.append(formatter.getBody());

        // process subpanels recursively
        Set<SubPanelController> controllerSet = formatter.getSubPanels();
        for (SubPanelController controller : controllerSet) {
            String name = controller.getName();
            StringBuilder subResult = new StringBuilder();
            for (SubPanel subPanel : controller.getSubPanels()) {
                subResult.append(getPanelDataList(subPanel));
            }
            String output = formatter.cleanPanelSetOutput(name, subResult.toString()) ;
            result.append(output);
        }
        result.append(formatter.getHeaderClose());
        return result.toString();
    }

}
