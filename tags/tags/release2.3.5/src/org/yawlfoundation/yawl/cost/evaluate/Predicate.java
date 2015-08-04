package org.yawlfoundation.yawl.cost.evaluate;

/**
 * @author Michael Adams
 * @date 3/12/12
 */
public class Predicate {

    private String taskID;
    private String resourceID;
    private String caseRange;

    private boolean allTasks;
    private boolean allResources;
    private boolean wholeCase;
    private boolean allCases;

    private Operator op;
    private double rhs;

    private enum Operator {eq, ne, lt, le, gt, ge}

    public Predicate() { }

    public Predicate(String predicate) throws CostPredicateParseException {
        parse(predicate);
    }

    public void parse(String predicate) throws CostPredicateParseException {
        if (predicate == null || !predicate.startsWith("cost(")) {
            throw new CostPredicateParseException("Invalid cost predicate: " + predicate);
        }

        String args = getContent(predicate, "cost");
        if (args.length() > 0) {
            String content;
            for (String arg : args.split(",")) {
                if (arg.startsWith("task(")) {
                    content = getContent(arg, "task");
                    if (content.length() > 0) {
                        taskID = content;
                    } else allTasks = true;
                } else if (taskID.startsWith("resource(")) {
                    content = getContent(arg, "resource");
                    if (content.length() > 0) {
                        resourceID = content;
                    } else allResources = true;
                } else if (taskID.startsWith("case(")) {
                    content = getContent(arg, "case");
                    if (content.length() > 0) {
                        caseRange = content;
                    } else allCases = true;
                } else throw new CostPredicateParseException(
                        "Unrecognised argument in cost predicate: " + arg);
            }
        } else wholeCase = true;

        parseOpAndValue(predicate.substring(predicate.lastIndexOf(')') + 1));
    }

    public String getTaskID() {
        return taskID;
    }

    public void setTaskID(String taskID) {
        this.taskID = taskID;
    }

    public String getResourceID() {
        return resourceID;
    }

    public void setResourceID(String resourceID) {
        this.resourceID = resourceID;
    }

    public String getCaseRange() {
        return caseRange;
    }

    public void setCaseRange(String caseRange) {
        this.caseRange = caseRange;
    }

    public boolean isAllTasks() {
        return allTasks;
    }

    public void setAllTasks(boolean allTasks) {
        this.allTasks = allTasks;
    }

    public boolean isAllResources() {
        return allResources;
    }

    public void setAllResources(boolean allResources) {
        this.allResources = allResources;
    }

    public boolean isWholeCase() {
        return wholeCase;
    }

    public void setWholeCase(boolean wholeCase) {
        this.wholeCase = wholeCase;
    }

    public boolean isAllCases() {
        return allCases;
    }

    public void setAllCases(boolean allCases) {
        this.allCases = allCases;
    }


    public double getValue() {
        return rhs;
    }

    public void setValue(double value) {
        this.rhs = value;
    }

    private String getContent(String expr, String prefix)
            throws CostPredicateParseException {
        expr = expr.trim();
        int pos = expr.lastIndexOf(')');
        if (pos == -1) {
            throw new CostPredicateParseException(
                    "Malformed cost predicate content - missing closing ')': " + expr);
        }
        return expr.substring(prefix.length() + 1, pos).trim();
    }


    private void parseOpAndValue(String exprSuffix) throws CostPredicateParseException {
        try {
            rhs = Double.parseDouble(parseOp(exprSuffix));
        } catch (NumberFormatException nfe) {
            throw new CostPredicateParseException("Invalid value, " + nfe.getMessage());
        }
    }


    private String parseOp(String exprSuffix) throws CostPredicateParseException {
        exprSuffix = exprSuffix.trim();
        if (exprSuffix.startsWith(">=")) op = Operator.ge;
        else if (exprSuffix.startsWith("<=")) op = Operator.le;
        else if (exprSuffix.startsWith("!=")) op = Operator.ne;
        else if (exprSuffix.startsWith("=")) op = Operator.eq;
        else if (exprSuffix.startsWith("<")) op = Operator.lt;
        else if (exprSuffix.startsWith(">")) op = Operator.gt;
        else throw new CostPredicateParseException("Invalid operand in cost predicate");
        return exprSuffix.substring(2);
    }


    public boolean evaluate(double lhs) {
        switch (op) {
            case ge:
                return lhs >= rhs;
            case le:
                return lhs <= rhs;
            case ne:
                return lhs != rhs;
            case eq:
                return lhs == rhs;
            case lt:
                return lhs < rhs;
            case gt:
                return lhs > rhs;
        }
        return false;
    }


}
