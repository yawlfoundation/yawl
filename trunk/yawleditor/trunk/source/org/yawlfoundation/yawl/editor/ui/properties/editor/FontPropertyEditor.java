package org.yawlfoundation.yawl.editor.ui.properties.editor;

import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.properties.FontColor;
import org.yawlfoundation.yawl.editor.ui.properties.dialog.FontDialog;

import java.awt.*;

/**
 * @author Michael Adams
 * @date 12/07/12
 */
public class FontPropertyEditor extends DialogPropertyEditor {

    private Font currentFont;
    private Color colour;

    public FontPropertyEditor() {
        super(new FontPropertyRenderer());
    }

    public Object getValue() {
        return colour != null ? new FontColor(currentFont, colour) : currentFont;
    }

    public void setValue(Object value) {
        if (value instanceof FontColor) {
            FontColor fontColor = (FontColor) value;
            currentFont = fontColor.getFont();
            colour = fontColor.getColour();
        }
        else {
            currentFont = (Font) value;
            colour = null;
        }
        ((FontPropertyRenderer) label).setValue(currentFont);
    }


    protected void showDialog() {
        FontDialog dialog = new FontDialog(YAWLEditor.getInstance(), currentFont);
        if (colour != null) dialog.setColour(colour);
        Font newFont = dialog.showDialog();

        if (newFont != null) {
            boolean colourChange = false;
            Color newColour = null;
            if (colour != null) {
                newColour = dialog.getColour();
                colourChange = ! newColour.equals(colour);
            }
            if (colourChange || ! newFont.equals(currentFont)) {
                Object oldValue = getValue();
                Object newValue = colour != null ? new FontColor(newFont, newColour) : newFont;
                setValue(newValue);
                firePropertyChange(oldValue, newFont);
            }
        }
    }

}

