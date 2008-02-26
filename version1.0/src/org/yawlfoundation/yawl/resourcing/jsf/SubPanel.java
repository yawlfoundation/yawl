package org.yawlfoundation.yawl.resourcing.jsf;

import com.sun.rave.web.ui.component.Button;
import com.sun.rave.web.ui.component.PanelLayout;

// a panel layout with a few extra members

class SubPanel extends PanelLayout implements Cloneable {

    private int height ;
    private int top ;
    private String name ;

    private SubPanelController controller;

    // 'occurs' buttons
    private Button btnPlus;
    private Button btnMinus;

    // Constructor //
    public SubPanel() { super(); }


    // Getters & Setters //

    public int getHeight() { return height; }

    public void setHeight(int height) { this.height = height; }

    public int getTop() { return top; }

    public void setTop(int top) { this.top = top; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public Button getBtnPlus() { return btnPlus; }

    public void setBtnPlus(Button btnPlus) { this.btnPlus = btnPlus; }

    public Button getBtnMinus() { return btnMinus; }

    public void setBtnMinus(Button btnMinus) { this.btnMinus = btnMinus; }

    public SubPanelController getController() { return controller; }

    public void setController(SubPanelController controller) {
        this.controller = controller;
    }


    /***************************************************************************/

    // Public Methods //

    /**
     * Calculates the screen co-ords for this panel (relative to the entire form)
     * @param maxLevel the highest depth level of all panels on this form
     */
    public void assignStyle(int maxLevel) {

        // resize width for the nested depth of this panel
        int width =  DynFormFactory.PANEL_BASE_WIDTH +
                     (DynFormFactory.SUBPANEL_INSET * 2 *
                     (maxLevel - controller.getDepthlevel() + 1)) ;

        String style = String.format("top: %dpx; left: %dpx; height: %dpx; width: %dpx",
                                      top, DynFormFactory.SUBPANEL_INSET, height, width);

        this.setStyle(style);
        this.setStyleClass(controller.getSubPanelStyleClass());

        // set 'occurs' buttons in top right of panel (if they exist)
        if (btnPlus != null) {
            btnPlus.setStyle(String.format("left: %dpx", width - 21));  // 20 = btn width
            btnMinus.setStyle(String.format("left: %dpx", width - 41));
        }
    }


    public void addOccursButton(Button btn) {
        if (btn.getText().equals("+"))
           btnPlus = btn ;
        else
           btnMinus = btn ;
        this.getChildren().add(btn);
    }


    public SubPanel clone() {
        SubPanel result = new SubPanel();
        result.setTop(top);
        result.setHeight(height);
        result.setName(name);
        result.setController(controller);
        result.setStyle(this.getStyle());
        result.setStyleClass(this.getStyleClass());
        return result;
    }

    public int incTop(int amount) {
        top += amount ;
        return top;
    }

    public int decTop(int amount) {
        top -= amount ;
        return top;
    }

}
