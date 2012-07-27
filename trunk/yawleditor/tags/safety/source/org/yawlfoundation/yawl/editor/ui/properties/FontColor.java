package org.yawlfoundation.yawl.editor.ui.properties;

import java.awt.*;

/**
 * @author Michael Adams
 * @date 24/07/12
 */
public class FontColor {

    private Font font;
    private Color colour;

    public FontColor(Font font, Color colour) {
        this.font = font;
        this.colour = colour;
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public Color getColour() {
        return colour;
    }

    public void setColour(Color colour) {
        this.colour = colour;
    }

    public String toString() {
        return font != null ?
                font.getFamily() + getStyleName(font.getStyle()) + font.getSize() : "";
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
