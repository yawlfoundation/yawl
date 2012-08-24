package org.yawlfoundation.yawl.analyser.reductionrules;

/**
 * @author Michael Adams
 * @date 14/05/12
 */
public enum YAWLReductionRuleType {

    FSPY (FSPYrule.class),
    FSTY (FSTYrule.class),
    FPPY (FPPYrule.class),
    FPTY (FPTYrule.class),
    FAPY (FAPYrule.class),
    FATY (FATYrule.class),
    ELTY (ELTYrule.class),
    ELPY (ELPYrule.class),
    FXOR (FXORrule.class),
    FAND (FANDrule.class),
    FIE (FIErule.class),
    FOR (FORrule.class);


    private Class ruleClass;


    private YAWLReductionRuleType(Class clazz) {
        ruleClass = clazz;
    }

    public YAWLReductionRule getRule() {
        try {
            return (YAWLReductionRule) ruleClass.newInstance();
        }
        catch (Exception e) {
            return null;
        }
    }


}
