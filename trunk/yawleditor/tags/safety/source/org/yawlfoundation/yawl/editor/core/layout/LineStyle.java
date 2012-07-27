package org.yawlfoundation.yawl.editor.core.layout;

/**
 *  An enumeration of line styles for flows
 *
 * @author Michael Adams
 * @date 20/06/12
 */
public enum LineStyle {

    // the indices match the legacy jgraph equivalents
    Orthogonal(11), Bezier(12), Spline(13);

    private int index;

    private LineStyle(int i) {index = i; }

    public int getCardinality() { return index; }

    public static LineStyle valueOf(int i) {
        switch(i) {
            case 12 : return Bezier;
            case 13 : return Spline;
            default : return Orthogonal;
        }
    }
}
