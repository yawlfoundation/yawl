package org.yawlfoundation.yawl.analyser.reductionrules;

/**
 * @author Michael Adams
 * @date 14/05/12
 */
public enum ResetReductionRuleType {

    FSPR (FSPRrule.class),
    FSTR (FSTRrule.class),
    FPPR (FPPRrule.class),
    FPTR (FPTRrule.class),
    DEAR (DEARrule.class),
    ELTR (ELTRrule.class),
    FESR (FESRrule.class);


    private Class ruleClass;


    private ResetReductionRuleType(Class clazz) {
        ruleClass = clazz;
    }

    public ResetReductionRule getRule() {
        try {
            return (ResetReductionRule) ruleClass.newInstance();
        }
        catch (Exception e) {
            return null;
        }
    }


}
