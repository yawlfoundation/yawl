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

public class DynFormFactory extends AbstractSessionBean {

    // required JSF member and method
    private int __placeholder;

    private void _init() throws Exception { }


    // the are components of the form that are managed by this object

//    private Head head = new Head();
//
//    public Head getHead() { return head; }
//
//    public void setHead(Head h) { head = h; }
//
//
//    private Body body = new Body();
//
//    public Body getBody() { return body; }
//
//    public void setBody(Body b) { body = b; }


    private PanelLayout compPanel = new PanelLayout();

    public PanelLayout getCompPanel() { return compPanel; }

    public void setCompPanel(PanelLayout pl) { compPanel = pl; }


//    private Button btnOK = new Button();
//
//    public Button getBtnOK() { return btnOK; }
//
//    public void setBtnOK(Button b) { btnOK = b; }
//
//
//    private Button btnCancel = new Button();
//
//    public Button getBtnCancel() { return btnCancel; }
//
//    public void setBtnCancel(Button b) { btnCancel = b; }
//
//
//    private Page page1 = new Page();
//
//    public Page getPage1() { return page1; }
//
//    public void setPage1(Page p) { page1 = p; }
//
//
//    private Html html1 = new Html();
//
//    public Html getHtml1() { return html1; }
//
//    public void setHtml1(Html h) { html1 = h; }
//
//
//    private Head head1 = new Head();
//
//    public Head getHead1() { return head1; }
//
//    public void setHead1(Head h) { head1 = h; }
//
//
//    private Link link1 = new Link();
//
//    public Link getLink1() { return link1; }
//
//    public void setLink1(Link l) { link1 = l; }
//
//
//    private Body body1 = new Body();
//
//    public Body getBody1() { return body1; }
//
//    public void setBody1(Body b) { body1 = b; }
//
//
//    private Form form1 = new Form();
//
//    public Form getForm1() { return form1; }
//
//    public void setForm1(Form f) { form1 = f; }
//
//
//    private StaticText txtHeader = new StaticText();
//
//    public StaticText getTxtHeader() { return txtHeader; }
//
//    public void setTxtHeader(StaticText st) { txtHeader = st; }
//
//


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

            // create and add a label for the parameter
            compPanel.getChildren().add(makeLabel(param, topStyle));

            // create and add the appropriate input field
            UIComponent field ;
            if (param.getDataTypeName().equals("boolean"))
                field = makeCheckbox(param, topStyle);
            else {
                if (param.isInputOnly())
                    field = makeReadOnlyTextField(param, topStyle);
                else
                    field = makeTextField(param, topStyle);
            }
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


    public Label makeLabel(FormParameter param, String topStyle) {
        String pName = param.getName();
        Label label = new Label() ;
        label.setId("lbl" + pName);
        label.setText(pName + ": ");
        label.setStyleClass("dynformLabel");
        label.setStyle(topStyle) ;
        label.setRequiredIndicator(param.isMandatory() || param.isRequired());
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
   //     panel.setTransient(true);

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

}
