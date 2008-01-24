/*
 * dynForm.java
 *
 * Created on 6 January 2008, 11:57
 * Copyright adamsmj
 */

package org.yawlfoundation.yawl.resourcing.jsf;

import com.sun.rave.web.ui.appbase.AbstractPageBean;
import com.sun.rave.web.ui.component.*;

import javax.faces.FacesException;
import javax.faces.event.ValueChangeEvent;
import javax.faces.component.UIComponent;

import java.util.HashMap;
import java.util.Map;

public class dynForm extends AbstractPageBean {

    // Constructor
     public dynForm() { }

    /****** JSF Required Members and Methods ***************************************/
    private int __placeholder;

    private void _init() throws Exception { }

    /** @return a reference to the application scoped data bean. */
    protected ApplicationBean getApplicationBean() {
        return (ApplicationBean) getBean("ApplicationBean");
    }

    /** @return a reference to the session scoped data bean. */
    protected SessionBean getSessionBean() {
        return (SessionBean) getBean("SessionBean");
    }

    /** @return a reference to the session scoped factory bean. */
    private DynFormFactory getDynFormFactory() {
        return (DynFormFactory) getBean("DynFormFactory");
    }

    private DynFormFactory dff = getDynFormFactory();

    public void init() {
        super.init();

        // *Note* - JSF requirement this block should NOT be modified
        try {
            _init();
        } catch (Exception e) {
            log("dynForm Initialization Failure", e);
            throw e instanceof FacesException ? (FacesException) e: new FacesException(e);
        }
 //       initDynForm();
//        compPanel.getChildren().addAll(getDynFormFactory().getFieldList());
    }

    /****** Abstract Method Implementations *************************************/

    public void preprocess() {
    }

    public void prerender() {
 //       compPanel = getDynFormFactory().getCompPanel();
        getSessionBean().setActivePage("dynForm");
//       if (getDynFormFactory().isDynFormPost()) {
//            postOKAction() ;
//        }
    }

    public void destroy() { }


    /****** Page Components ****************************************************/

    private Page page1 = new Page();
    
    public Page getPage1() { return page1; }
    
    public void setPage1(Page p) { page1 = p; }


    private Html html1 = new Html();
    
    public Html getHtml1() { return html1; }
    
    public void setHtml1(Html h) { html1 = h; }


    private Head head1 = new Head();
    
    public Head getHead1() { return head1; }
    
    public void setHead1(Head h) { head1 = h; }


    private Link link1 = new Link();
    
    public Link getLink1() { return link1; }
    
    public void setLink1(Link l) { link1 = l; }


    private Body body1 = new Body();
    
    public Body getBody1() { return body1; }
    
    public void setBody1(Body b) { body1 = b; }


    private Form form1 = new Form();
    
    public Form getForm1() { return form1; }
    
    public void setForm1(Form f) { form1 = f; }


    private StaticText txtHeader = new StaticText();

    public StaticText getTxtHeader() { return txtHeader; }

    public void setTxtHeader(StaticText st) { txtHeader = st; }


//    private PanelLayout compPanel = new PanelLayout();
//
//    public PanelLayout getCompPanel() { return compPanel; }
//
//    public void setCompPanel(PanelLayout pl) { compPanel = pl; }


    private Button btnOK = new Button();

    public Button getBtnOK() { return btnOK; }

    public void setBtnOK(Button b) { btnOK = b; }


    private Button btnCancel = new Button();

    public Button getBtnCancel() { return btnCancel; }

    public void setBtnCancel(Button b) { btnCancel = b; }


    /****** Custom Methods ******************************************************/

//    private String title = "test";
//
//    public String getTitle() {
//        return title;
//    }
//
//    public void setTitle(String title) {
//        this.title = title;
//    }


//    public void initDynForm() {
//        int lineHeight = 25 ;                      // nbr of pixels between each row
//        int startingYPos = 10 ;
//        int line = 0 ;
//        boolean focusSet = false ;
//
////        setTitle(title);
//
//        List<FormParameter> params = new ArrayList(getSessionBean().getDynFormParams().values());
//
//        compPanel.getChildren().clear();                     // start with clean panel
// //       compPanel.setTransient(true);
//
//        for (FormParameter param : params) {
//
//            // increment y-coord for each line
//            String topStyle = String.format("top: %dpx",
//                                             line++ * lineHeight + startingYPos) ;
//
//            // create and add a label for the parameter
//            compPanel.getChildren().add(dff.makeLabel(param, topStyle));
//
//            // create and add the appropriate input field
//            UIComponent field ;
//            if (param.getDataTypeName().equals("boolean"))
//                field = dff.makeCheckbox(param, topStyle);
//            else {
//                if (param.isInputOnly())
//                    field = dff.makeReadOnlyTextField(param, topStyle);
//                else
//                    field = dff.makeTextField(param, topStyle);
//            }
//            compPanel.getChildren().add(field);
//            if (! focusSet) focusSet = dff.setFocus(field) ;
//        }
//
//        // resize page panel for the number of fields added
//        int height = (compPanel.getChildCount() * lineHeight + startingYPos) / 2;
//        String heightStyle = String.format("height: %dpx", height);
//        compPanel.setStyle(heightStyle);
//
//        // reposition buttons to go directly under resized panel
//        int btnOrigYPos = 225;
//        String topStyle = String.format("top: %dpx", btnOrigYPos + height) ;
//        btnOK.setStyle("left: 270px; " + topStyle);
//        btnCancel.setStyle("left: 170px; " + topStyle);
//    }



    /**
     * Updates workitem parameters with values added by user
     * @return a reference to the referring page
     */
    public String btnOK_action() {
//        getDynFormFactory().setDynFormPost(true) ;
//        return null ;
//    }
//
//    public void postOKAction() {
 //       getDynFormFactory().setFieldList(compPanel.getChildren());
        String referringPage;
        SessionBean sb = getSessionBean();

//        FacesContext facesContext = FacesContext.getCurrentInstance();
//        UIViewRoot root = facesContext.getViewRoot();
//        root.
//            FacesContext context = FacesContext.getCurrentInstance();
//  //       context.renderResponse();
//
//        context.getExternalContext().getSessionMap(). ;
        
//     Application application = context.getApplication() ;
//     UIViewRoot root = context.getViewRoot();
//     UIComponent comp = (UIComponent) root.getChildren().get(0);
//            ViewHandler viewHandler = application.getViewHandler();
//    UIViewRoot newRoot = viewHandler.createView(context, root.getViewId());
//     newRoot.getChildren().add(comp);
//    context.setViewRoot(newRoot);
 //  application.getStateManager().saveSerializedView(context);
        
//        root.getChildren();
//
//        List l = body1.getChildren();
//        l.remove(0) ;

//         List l = form1.getChildren();
//        l.clear();
//        root.processUpdates(facesContext);

        // map the updated values back to the params
//        getDynFormFactory().setCompPanel(compPanel);
//        Map<String, FormParameter> params = sb.getDynFormParams();
//        params = getDynFormFactory().updateValues(params) ;
//        sb.setDynFormParams(params) ;

//        List l = form1.getChildren();
//        Iterator itr = l.iterator() ;
//        while (itr.hasNext()) {
//            UIComponent uic = (UIComponent) itr.next();
//            if (uic.getId().equals("compPanel")) {
//                getDynFormFactory().setFieldList(uic.getChildren()) ;
//                break;
//            }
//        }

 //       compPanel.setTransient(true);
        if (sb.getDynFormLevel().equals("case")) {
            sb.setCaseLaunch(true);                        // temp flag for post action
            referringPage = "showCaseMgt";
        }
        else {
            sb.setWirEdit(true) ;
            referringPage = "showUserQueues";
        }
        getDynFormFactory().setDynFormPost(false);
   //     getSessionBean().gotoPage(referringPage);
        return referringPage;
    }


    /**
     * Returns to referring page without saving changed values
     * @return a reference to the referring page
     */
    public String btnCancel_action() {
        if (getSessionBean().getDynFormLevel().equals("case"))
           return "showCaseMgt";
        else
           return "showUserQueues";
    }

    private Map<String, String> updateMap = new HashMap<String, String>();


    public void saveValueChange(ValueChangeEvent event) {
        String value ;
        UIComponent source = event.getComponent() ;

        if (source instanceof Checkbox)
            value = event.getNewValue().toString();
        else
            value = (String) event.getNewValue() ;

        updateMap.put(source.getId(), value);
    }
}



