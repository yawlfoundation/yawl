package org.yawlfoundation.yawl.editor.ui.properties.editor;

import com.l2fprod.common.swing.renderer.ColorCellRenderer;

import javax.swing.*;
import java.awt.*;

/**
 * @author Michael Adams
 * @date 12/07/12
 */
public class ColorPropertyEditor extends DialogPropertyEditor {


    private Color currentColour;

    public ColorPropertyEditor() {
        super(new ColorCellRenderer());
    }

    public Object getValue() {
        return currentColour;
    }

    public void setValue(Object value) {
        currentColour = (Color) value;
        ((ColorCellRenderer) label).setValue(currentColour);
    }

    protected void showDialog() {
        Color selectedColour = JColorChooser.showDialog(editor, "Choose a Colour",
                currentColour);

        if (!( selectedColour == null || selectedColour.equals(currentColour))) {
            Color oldColour = currentColour;
            setValue(selectedColour);
            firePropertyChange(oldColour, selectedColour);
        }
    }

}

