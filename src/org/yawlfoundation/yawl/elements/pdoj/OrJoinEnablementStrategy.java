package org.yawlfoundation.yawl.elements.pdoj;

/**
 *
 * @author Michael Adams
 * @date 10/10/2026
 */
public enum OrJoinEnablementStrategy {

    UNDEFINED,
    TOTAL_ORDER,
    POST_DOMINANCE,
    STATE_SPACE ;

    private static OrJoinEnablementStrategy _selectedStrategy = TOTAL_ORDER;  // default


    public static OrJoinEnablementStrategy fromInt(int i) {
        return switch (i) {
            case 2 -> POST_DOMINANCE;
            case 3 -> STATE_SPACE;
            default -> TOTAL_ORDER;
        };
    }


    public static void setStrategy(int i) {
        _selectedStrategy = fromInt(i);
    }


    public static OrJoinEnablementStrategy getSelectedStrategy() {
        return _selectedStrategy;
    }

}
