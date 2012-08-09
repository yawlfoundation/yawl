package org.yawlfoundation.yawl.editor.core.layout;

/**
 * An enumeration of decorator positions, following the Java convention
 *
 * @author Michael Adams
 * @date 20/06/12
*/
public enum DecoratorPosition {

    // the indices match the legacy jgraph ordinals
    North(10), South(11), West(12), East(13);

    private int index;

    private DecoratorPosition(int i) {index = i; }

    public int getCardinality() { return index; }

    public static DecoratorPosition valueOf(int i) {
        switch(i) {
            case 10 : return North;
            case 11 : return South;
            case 12 : return West;
            default : return East;
        }
    }
}
