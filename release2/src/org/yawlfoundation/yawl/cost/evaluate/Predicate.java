package org.yawlfoundation.yawl.cost.evaluate;

import org.yawlfoundation.yawl.cost.CostService;
import org.yawlfoundation.yawl.util.StringUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Michael Adams
 * @date 3/12/12
 */
public class Predicate {

    private Set<String> taskList;
    private Set<String> resourceList;
    private Set<String> caseList;
    private boolean wholeCase;
    private boolean allCases;
    private boolean averageFlag;
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
            for (String arg : parseArgs(args)) {
                if (arg.startsWith("task(")) {
                    content = getContent(arg, "task");
                    if (content.length() > 0) {
                        taskList = parseTaskArgs(content);
                    }
                }
                else if (arg.startsWith("resource(")) {
                    content = getContent(arg, "resource");
                    if (content.length() > 0) {
                        resourceList = parseResourceArgs(content);
                    }
                }
                else if (arg.startsWith("case(")) {
                    content = getContent(arg, "case");
                    if (content.length() > 0) {
                        caseList = parseCaseArgs(content);
                    }
                    else allCases = true;
                }
                else if (arg.equalsIgnoreCase("average")) {
                    averageFlag = true;
                }
                else throw new CostPredicateParseException(
                        "Unrecognised argument in cost predicate: " + arg);
            }
        }
        else wholeCase = true;

        parseOpAndValue(predicate.substring(predicate.lastIndexOf(')') + 1));
    }

    public Set<String> getTaskList() { return taskList; }

    public void setTaskList(Set<String> list) { taskList = list; }


    public Set<String> getResourceList() { return resourceList; }

    public void setResourceList(Set<String> list) { resourceList = list; }


    public Set<String> getCaseList() { return caseList; }

    public void setCaseList(Set<String> caseList) { this.caseList = caseList; }

    public boolean hasCaseList() { return caseList != null; }



    public boolean isAllTasks() { return ! hasItems(taskList); }

    public boolean isAllResources() { return ! hasItems(resourceList); }

    public boolean isAllCases() { return allCases; }

    public boolean isWholeCase() { return wholeCase; }

    public boolean isCurrentCaseOnly() { return ! (isAllCases() || hasCaseList()); }

    public boolean average() { return averageFlag; }


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


    private List<String> parseArgs(String args) {
        List<String> argList = new ArrayList<String>();
        Pattern p = Pattern.compile(
                "((task|resource|case)\\(('|\")*[^\\)]*('|\")*\\)|average)");
        Matcher m = p.matcher(args);
        while (m.find()) {
            argList.add(m.group());
        }
        return argList;
    }


    private Set<String> parseTaskArgs(String args) throws CostPredicateParseException {
        if (args == null) return null;
        Set<String> taskSet = new HashSet<String>();
        for (String taskID : args.split("\\s*,\\s*")) {
            taskSet.add(StringUtil.deQuote(taskID));
        }
        return taskSet;
    }


    private Set<String> parseResourceArgs(String args) throws CostPredicateParseException {
        if (args == null) return null;
        Set<String> resourceSet = new HashSet<String>();
        for (String item : args.split("\\s*,\\s*")) {
            resourceSet.addAll(getResourceIDs(item));
        }
        return resourceSet;
    }

    private Set<String> parseCaseArgs(String args) throws CostPredicateParseException {
        if (args == null) return null;
        Set<String> caseSet = new HashSet<String>();
        for (String item : args.split("\\s*,\\s*")) {
            if (item.contains("-")) {
                caseSet.addAll(parseCaseRange(StringUtil.deQuote(item)));
            }
            else {
                int caseID = StringUtil.strToInt(StringUtil.deQuote(item), -1);
                if (caseID > 0) {
                    caseSet.add(String.valueOf(caseID));
                }
                else throw new CostPredicateParseException("Malformed case argument: " +
                        caseID);
            }
        }
        return caseSet;
    }


    private Set<String> parseCaseRange(String range) throws CostPredicateParseException {
        String exMsg = "Malformed range in predicate case argument: " + range;
        String[] bounds = range.split("\\s*-\\s*");
        Set<String> caseSet = new HashSet<String>();
        if (bounds.length == 1) {
            caseSet.add(range);
        }
        else if (bounds.length == 2) {
            int lower = StringUtil.strToInt(StringUtil.deQuote(bounds[0]), -1);
            int upper = StringUtil.strToInt(StringUtil.deQuote(bounds[1]), -1);
            if (lower < 0 || upper < 0) {
                throw new CostPredicateParseException(exMsg);
            }
            for (int i = lower; i <= upper; i++) {
                caseSet.add(String.valueOf(i));
            }
        }
        else {
            throw new CostPredicateParseException(exMsg);
        }
        return caseSet;
    }


    private Set<String> getResourceIDs(String idOrName) {
        return CostService.getInstance().resolveResources(StringUtil.deQuote(idOrName));
    }


    private boolean hasItems(Set<String> list) {
        return ! (list == null || list.isEmpty());
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
