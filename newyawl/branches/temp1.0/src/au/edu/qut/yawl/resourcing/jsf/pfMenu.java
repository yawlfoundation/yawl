/*
 * pfMenu.java
 *
 * Created on October 31, 2007, 10:41 AM
 * Copyright adamsmj
 */
package au.edu.qut.yawl.resourcing.jsf;

import com.sun.rave.web.ui.appbase.AbstractFragmentBean;
import javax.faces.FacesException;
import javax.faces.component.html.HtmlPanelGrid;
import com.sun.rave.web.ui.component.Tree;
import com.sun.rave.web.ui.component.TreeNode;
import com.sun.rave.web.ui.component.ImageComponent;
import com.sun.rave.web.ui.component.Label;

/**
 * <p>Fragment bean that corresponds to a similarly named JSP page
 * fragment.  This class contains component definitions (and initialization
 * code) for all components that you have defined on this fragment, as well as
 * lifecycle methods and event handlers where you may add behavior
 * to respond to incoming events.</p>
 */
public class pfMenu extends AbstractFragmentBean {
    // <editor-fold defaultstate="collapsed" desc="Creator-managed Component Definition">
    private int __placeholder;
    
    /**
     * <p>Automatically managed component initialization. <strong>WARNING:</strong>
     * This method is automatically generated, so any user-specified code inserted
     * here is subject to being replaced.</p>
     */
    private void _init() throws Exception {
    }

    private HtmlPanelGrid gridPanel1 = new HtmlPanelGrid();

    public HtmlPanelGrid getGridPanel1() {
        return gridPanel1;
    }

    public void setGridPanel1(HtmlPanelGrid hpg) {
        this.gridPanel1 = hpg;
    }

    private Tree tree1 = new Tree();

    public Tree getTree1() {
        return tree1;
    }

    public void setTree1(Tree t) {
        this.tree1 = t;
    }

    private TreeNode mnuUser = new TreeNode();

    public TreeNode getMnuUser() {
        return mnuUser;
    }

    public void setMnuUser(TreeNode tn) {
        this.mnuUser = tn;
    }

    private ImageComponent image1 = new ImageComponent();

    public ImageComponent getImage1() {
        return image1;
    }

    public void setImage1(ImageComponent ic) {
        this.image1 = ic;
    }

    private TreeNode mnuUserWorkQueues = new TreeNode();

    public TreeNode getMnuUserWorkQueues() {
        return mnuUserWorkQueues;
    }

    public void setMnuUserWorkQueues(TreeNode tn) {
        this.mnuUserWorkQueues = tn;
    }

    private ImageComponent image2 = new ImageComponent();

    public ImageComponent getImage2() {
        return image2;
    }

    public void setImage2(ImageComponent ic) {
        this.image2 = ic;
    }

    private TreeNode mnuUserProfile = new TreeNode();

    public TreeNode getMnuUserProfile() {
        return mnuUserProfile;
    }

    public void setMnuUserProfile(TreeNode tn) {
        this.mnuUserProfile = tn;
    }

    private ImageComponent image3 = new ImageComponent();

    public ImageComponent getImage3() {
        return image3;
    }

    public void setImage3(ImageComponent ic) {
        this.image3 = ic;
    }

    private TreeNode mnuAdmin1 = new TreeNode();

    public TreeNode getMnuAdmin1() {
        return mnuAdmin1;
    }

    public void setMnuAdmin1(TreeNode tn) {
        this.mnuAdmin1 = tn;
    }

    private ImageComponent image4 = new ImageComponent();

    public ImageComponent getImage4() {
        return image4;
    }

    public void setImage4(ImageComponent ic) {
        this.image4 = ic;
    }

    private TreeNode mnuCaseMgt = new TreeNode();

    public TreeNode getMnuCaseMgt() {
        return mnuCaseMgt;
    }

    public void setMnuCaseMgt(TreeNode tn) {
        this.mnuCaseMgt = tn;
    }

    private ImageComponent image5 = new ImageComponent();

    public ImageComponent getImage5() {
        return image5;
    }

    public void setImage5(ImageComponent ic) {
        this.image5 = ic;
    }

    private TreeNode mnuAddService = new TreeNode();

    public TreeNode getMnuAddService() {
        return mnuAddService;
    }

    public void setMnuAddService(TreeNode tn) {
        this.mnuAddService = tn;
    }

    private ImageComponent image6 = new ImageComponent();

    public ImageComponent getImage6() {
        return image6;
    }

    public void setImage6(ImageComponent ic) {
        this.image6 = ic;
    }

    private TreeNode mnuAdminQueues = new TreeNode();

    public TreeNode getMnuAdminQueues() {
        return mnuAdminQueues;
    }

    public void setMnuAdminQueues(TreeNode tn) {
        this.mnuAdminQueues = tn;
    }

    private ImageComponent image7 = new ImageComponent();

    public ImageComponent getImage7() {
        return image7;
    }

    public void setImage7(ImageComponent ic) {
        this.image7 = ic;
    }

    private TreeNode mnuUserMgt = new TreeNode();

    public TreeNode getMnuUserMgt() {
        return mnuUserMgt;
    }

    public void setMnuUserMgt(TreeNode tn) {
        this.mnuUserMgt = tn;
    }

    private ImageComponent image8 = new ImageComponent();

    public ImageComponent getImage8() {
        return image8;
    }

    public void setImage8(ImageComponent ic) {
        this.image8 = ic;
    }

    private TreeNode mnuAdUserQueues = new TreeNode();

    public TreeNode getMnuAdUserQueues() {
        return mnuAdUserQueues;
    }

    public void setMnuAdUserQueues(TreeNode tn) {
        this.mnuAdUserQueues = tn;
    }

    private ImageComponent image9 = new ImageComponent();

    public ImageComponent getImage9() {
        return image9;
    }

    public void setImage9(ImageComponent ic) {
        this.image9 = ic;
    }

    private TreeNode mnuTeamQueues = new TreeNode();

    public TreeNode getMnuTeamQueues() {
        return mnuTeamQueues;
    }

    public void setMnuTeamQueues(TreeNode tn) {
        this.mnuTeamQueues = tn;
    }

    private ImageComponent image10 = new ImageComponent();

    public ImageComponent getImage10() {
        return image10;
    }

    public void setImage10(ImageComponent ic) {
        this.image10 = ic;
    }
    // </editor-fold>
    
    public pfMenu() {
    }

    /** 
     * <p>Return a reference to the scoped data bean.</p>
     */
    protected RequestBean getRequestBean() {
        return (RequestBean)getBean("RequestBean");
    }


    /** 
     * <p>Return a reference to the scoped data bean.</p>
     */
    protected ApplicationBean getApplicationBean() {
        return (ApplicationBean)getBean("ApplicationBean");
    }


    /** 
     * <p>Return a reference to the scoped data bean.</p>
     */
    protected SessionBean getSessionBean() {
        return (SessionBean)getBean("SessionBean");
    }


    /** 
     * <p>Callback method that is called whenever a page containing
     * this page fragment is navigated to, either directly via a URL,
     * or indirectly via page navigation.  Override this method to acquire
     * resources that will be needed for event handlers and lifecycle methods.</p>
     * 
     * <p>The default implementation does nothing.</p>
     */
    public void init() {
        // Perform initializations inherited from our superclass
        super.init();
        // Perform application initialization that must complete
        // *before* managed components are initialized
        // TODO - add your own initialiation code here

        // <editor-fold defaultstate="collapsed" desc="Creator-managed Component Initialization">
        // Initialize automatically managed components
        // *Note* - this logic should NOT be modified
        try {
            _init();
        } catch (Exception e) {
            log("pfMenu Initialization Failure", e);
            throw e instanceof FacesException ? (FacesException) e: new FacesException(e);
        }
        // </editor-fold>
        // Perform application initialization that must complete
        // *after* managed components are initialized
        // TODO - add your own initialization code here

    }

    /** 
     * <p>Callback method that is called after rendering is completed for
     * this request, if <code>init()</code> was called.  Override this
     * method to release resources acquired in the <code>init()</code>
     * resources that will be needed for event handlers and lifecycle methods.</p>
     * 
     * <p>The default implementation does nothing.</p>
     */
    public void destroy() {
    }


    public String mnuUserWorkQueues_action() {
        // TODO: Replace with your code
        
        return null;
    }


    public String mnuUserProfile_action() {
        // TODO: Replace with your code
        
        return null;
    }


    public String mnuCaseMgt_action() {
        // TODO: Replace with your code
        
        return null;
    }


    public String mnuAddService_action() {
        // TODO: Replace with your code
        
        return null;
    }


    public String mnuAdminQueues_action() {
        // TODO: Replace with your code
        
        return null;
    }


    public String mnuUserMgt_action() {
        // TODO: Replace with your code
        
        return null;
    }


    public String mnuAdUserQueues_action() {
        // TODO: Replace with your code
        
        return null;
    }


    public String mnuTeamQueues_action() {
        // TODO: Replace with your code
        
        return null;
    }
}
