package org.yawlfoundation.yawl.editor.ui.properties.editor;

import com.l2fprod.common.swing.renderer.DefaultCellRenderer;

import java.awt.*;

/**
 * @author Michael Adams
 * @date 13/07/12
 */
public class FontPropertyRenderer extends DefaultCellRenderer {

    protected String convertToString(Object value) {
        if (! (value instanceof Font)) return null;
        Font font = (Font) value;
        return font.getFontName() + getStyleName(font.getStyle()) + font.getSize();
    }


    private String getStyleName(int style) {
        switch (style) {
            case Font.BOLD : return ", Bold, ";
            case Font.ITALIC : return ", Italic, ";
            case Font.BOLD | Font.ITALIC : return ", Bold+Italic, ";
            default : return ", ";       // plain
        }
    }

}
