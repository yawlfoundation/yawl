package org.yawlfoundation.yawl.cost.evaluate;

import org.yawlfoundation.yawl.cost.CostService;
import org.yawlfoundation.yawl.resourcing.datastore.eventlog.ResourceEvent;
import org.yawlfoundation.yawl.util.StringUtil;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;
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
    private int firstCases;
    private int lastCases;
    private int randomCases;
    private long fromDate;
    private long toDate;
    private Set<Integer> dowList;
    private boolean wholeCase;
    private boolean averageFlag;
    private boolean minFlag;
    private boolean maxFlag;
    private boolean simpleExpression;
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

    public boolean hasCaseFilter() { return caseList != null || hasNamedRange(); }


    public boolean hasNamedRange() {
        return firstCases > 0 || lastCases > 0 || randomCases > 0;
    }

    public Set<String> getNamedRange(List<String> allCaseIDs) {
        Collections.sort(allCaseIDs, new Comparator<String>() {
            public int compare(String s, String other) {
                return StringUtil.strToInt(s, -1) - StringUtil.strToInt(other, -1);
            }
        });

        Set<String> range = new HashSet<String>();
        if (firstCases > 0) {
            if (allCaseIDs.size() < firstCases) range.addAll(allCaseIDs);
            else for (int i=0; i < firstCases; i++) range.add(allCaseIDs.get(i));
        }
        if (lastCases > 0) {
            if (allCaseIDs.size() < lastCases) range.addAll(allCaseIDs);
            else for (int i=allCaseIDs.size() - 1; i >= lastCases; i--)
                range.add(allCaseIDs.get(i));
        }
        if (randomCases > 0) {
            if (allCaseIDs.size() < randomCases) range.addAll(allCaseIDs);
            else {
                Random r = new Random();
                for (int i=0; i < randomCases; i++) {
                  range.add(allCaseIDs.remove(r.nextInt(allCaseIDs.size()))); // no duplicate gets
                }
            }
        }
        return range;
    }


    public List<ResourceEvent> applyDateFilter(List<ResourceEvent> events) {
        if (! hasDateFilter()) return events;
        long from = hasFromDate() ? fromDate : 0;
        long to = hasToDate() ? toDate : Long.MAX_VALUE;
        List<ResourceEvent> filtered = new ArrayList<ResourceEvent>();
        for (ResourceEvent event : events) {
            long timeStamp = event.get_timeStamp();
            if (timeStamp >= from && timeStamp <= to && meetsDOWCriterion(timeStamp)) {
                filtered.add(event);
            }
        }
        return filtered;
    }


    public boolean isAllTasks() { return ! hasItems(taskList); }

    public boolean isAllResources() { return ! hasItems(resourceList); }

    public boolean isAllCases() {
        return (caseList == null || caseList.isEmpty()) && ! hasNamedRange();
    }

    public boolean isWholeCase() { return wholeCase; }

    public boolean isCurrentCaseOnly() { return ! (isAllCases() || hasCaseFilter()); }

    public boolean isSimpleExpression() { return simpleExpression; }

    public boolean average() { return averageFlag; }

    public boolean max() { return maxFlag; }

    public boolean min() { return minFlag; }

    public boolean hasFromDate() { return fromDate > 0; }

    public boolean hasToDate() { return toDate > 0; }

    public boolean hasDOWFilter() { return dowList != null; }

    public boolean hasDateFilter() {
        return hasFromDate() || hasToDate() || hasDOWFilter();
    }

    public long getFromDate() { return fromDate; }

    public long getToDate() { return toDate; }


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
                "((task|resource|case)\\(('|\")*[^\\)]*('|\")*\\))");
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
            else if (item.equalsIgnoreCase("average")) {
                 averageFlag = true;
            }
            else if (item.equalsIgnoreCase("max")) {
                 maxFlag = true;
            }
            else if (item.equalsIgnoreCase("min")) {
                 minFlag = true;
            }
            else if (! isBlockArg(item)) {       // plain case id
                int caseID = StringUtil.strToInt(StringUtil.deQuote(item), -1);
                if (caseID > 0) {
                    caseSet.add(String.valueOf(caseID));
                }
                else throw new CostPredicateParseException("Malformed case argument: " +
                        caseID);
            }
        }
        if ((averageFlag && maxFlag) || (averageFlag && minFlag) || (minFlag && maxFlag)) {
            throw new CostPredicateParseException(
                    "At most one of 'min', 'max' or 'average' may be included");
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


    private boolean isBlockArg(String item) {
        String arg = item.trim().toLowerCase();
        if (arg.startsWith("first ")) {
            firstCases = parseInt(arg, 6);
            return firstCases > -1;
        }
        if (arg.startsWith("last ")) {
            lastCases = parseInt(arg, 5);
            return lastCases > -1;
        }
        if (arg.startsWith("random ")) {
            randomCases = parseInt(arg, 7);
            return randomCases > -1;
        }
        if (arg.startsWith("from ")) {
            fromDate = parseDate(arg, 5);
            return fromDate > 0;
        }
        if (arg.startsWith("to ")) {
            toDate = parseDate(arg, 3);
            return toDate > 0;
        }
        if (arg.startsWith("dow ")) {
            int dow = parseInt(arg, 4);
            if (dow > 0 && dow < 8) {
                if (dowList == null) dowList = new HashSet<Integer>();
                dowList.add(dow);
                return true;
            }
        }
        return false;
    }


    private boolean meetsDOWCriterion(long time) {
        if (! hasDOWFilter()) return true;               // no filter
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(time);
        int timeDOW = c.get(Calendar.DAY_OF_WEEK);
        for (int dow : dowList) {
            if (timeDOW == dow) return true;
        }
        return false;
    }


    private int parseInt(String arg, int offset) {
        return StringUtil.strToInt(arg.substring(offset), -1);
    }


    private long parseDate(String arg, int offset) {
        Date date = new SimpleDateFormat("yyyy-MM-dd").parse(arg, new ParsePosition(offset));
        return date != null ? date.getTime() : 0;
    }


    private Set<String> getResourceIDs(String idOrName) {
        return CostService.getInstance().resolveResources(StringUtil.deQuote(idOrName));
    }


    private boolean hasItems(Set<String> list) {
        return ! (list == null || list.isEmpty());
    }

    private void parseOpAndValue(String exprSuffix) throws CostPredicateParseException {
        if (StringUtil.isNullOrEmpty(exprSuffix)) {
            simpleExpression = true;
            return;
        }
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
