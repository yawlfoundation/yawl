package org.yawlfoundation.yawl.editor.ui.properties.data.validation;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;
import org.yawlfoundation.yawl.resourcing.jsf.dynform.DynFormFieldRestriction;
import org.yawlfoundation.yawl.schema.XSDType;
import org.yawlfoundation.yawl.util.SaxonUtil;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.xml.datatype.Duration;
import java.util.List;
import java.util.Random;

/**
 * @author Michael Adams
 * @date 10/11/2013
 */
public class RestrictionSampleValueGenerator {

    private final DynFormFieldRestriction _restriction;

    public RestrictionSampleValueGenerator(DynFormFieldRestriction restriction) {
        _restriction = restriction;
    }

    public String generateValue() {
        if (_restriction.hasEnumeration()) {
            List<String> enums = _restriction.getEnumeration();
            return enums.get(enums.size() -1);
        }
        if (_restriction.hasMinInclusive()) {
            return _restriction.getMinInclusive();
        }
        if (_restriction.hasMaxInclusive()) {
            return _restriction.getMaxInclusive();
        }
        if (_restriction.hasMinExclusive()) {
            return generateMinExclusiveValue(_restriction.getMinExclusive());
        }
        if (_restriction.hasMaxExclusive()) {
            return generateMaxExclusiveValue(_restriction.getMaxExclusive());
        }
        if (_restriction.hasTotalDigits()) {
            return generateTotalDigitsValue(_restriction.getTotalDigitsValue());
        }
        if (_restriction.hasFractionDigits()) {
            return generateFractionDigitsValue(_restriction.getFractionDigitsValue());
        }
        if (_restriction.hasLength()) {
            return generateLengthValue(_restriction.getLengthValue());
        }
        if (_restriction.hasMaxLength()) {
            return generateLengthValue(_restriction.getMaxLengthValue());
        }
        if (_restriction.hasMinLength()) {
            generateLengthValue(_restriction.getMinLengthValue());
        }
        if (_restriction.hasPattern()) {
            return generatePatternMatch(_restriction.getPattern());
        }
        return "";
    }


    private String generateMinExclusiveValue(String value) {
        String type = _restriction.getBaseType();
        if (XSDType.isIntegralType(type)) {
            int intValue = StringUtil.strToInt(value, 0);
            return String.valueOf(++intValue);
        }
        if (XSDType.isFloatType(type)) {
            int radixPos = value.indexOf('.');
            int rhsLength = radixPos > -1 ? value.length() - radixPos - 1 : 0;
            double doubleValue = StringUtil.strToDouble(value, 0.0);
            return String.valueOf(doubleValue + (10^-rhsLength));
        }
        if (type.equals("duration")) {
            Duration d = StringUtil.strToDuration(value);
            return d.add(StringUtil.strToDuration("PT1S")).toString();
        }
        if (type.equals("gDay")) {
            int day = StringUtil.strToInt(value.substring(3), 30);
            return "---" + ++day;
        }
        if (type.equals("gMonth")) {
            int month = StringUtil.strToInt(value.substring(2), 11);
            return "--" + ++month;
        }
        if (type.equals("gYear")) {
            int year = StringUtil.strToInt(value.substring(0,4), 0);
            return String.valueOf(++year);
        }
        if (type.equals("gMonthDay")) {
            int day = StringUtil.strToInt(value.substring(5), 31);
            if (day == 31) {
                int month = StringUtil.strToInt(value.substring(2,2), 11);
                return "--" + ++month + "-" + day;
            }
            else return value.substring(0,5) + ++day;
        }
        if (type.equals("gYearMonth")) {
            int month = StringUtil.strToInt(value.substring(5), 12);
            if (month == 12) {
                int year = StringUtil.strToInt(value.substring(0,4), 0);
                return ++year + "-" + month;
            }
            else return value.substring(0,5) + ++month;
        }
        if (type.equals("date")) {
            String query = "xs:date('" + value + "') + xs:dayTimeDuration('P1D')";
            return evaluateQuery(query, null);
        }
        if (type.equals("time")) {
            String query = "xs:time('" + value + "') + xs:dayTimeDuration('P1S')";
            return evaluateQuery(query, null);
        }
        if (type.equals("dateTime")) {
            String query = "xs:dateTime('" + value + "') + xs:dayTimeDuration('P1S')";
            return evaluateQuery(query, null);
        }
        return "";
    }



    private String generateMaxExclusiveValue(String value) {
        String type = _restriction.getBaseType();
        if (XSDType.isIntegralType(type)) {
            int intValue = StringUtil.strToInt(value, 0);
            return String.valueOf(--intValue);
        }
        if (XSDType.isFloatType(type)) {
            int radixPos = value.indexOf('.');
            int rhsLength = radixPos > -1 ? value.length() - radixPos - 1 : 0;
            double doubleValue = StringUtil.strToDouble(value, 0.0);
            return String.valueOf(doubleValue - (10^-rhsLength));
        }
        if (type.equals("duration")) {
            Duration d = StringUtil.strToDuration(value);
            return d.subtract(StringUtil.strToDuration("PT1S")).toString();
        }
        if (type.equals("gDay")) {
            int day = StringUtil.strToInt(value.substring(3), 30);
            return "---" + --day;
        }
        if (type.equals("gMonth")) {
            int month = StringUtil.strToInt(value.substring(2), 11);
            return "--" + --month;
        }
        if (type.equals("gYear")) {
            int year = StringUtil.strToInt(value.substring(0,4), 0);
            return String.valueOf(--year);
        }
        if (type.equals("gMonthDay")) {
            int day = StringUtil.strToInt(value.substring(5), 31);
            if (day == 31) {
                int month = StringUtil.strToInt(value.substring(2,2), 11);
                return "--" + --month + "-" + day;
            }
            else return value.substring(0,5) + --day;
        }
        if (type.equals("gYearMonth")) {
            int month = StringUtil.strToInt(value.substring(5), 12);
            if (month == 12) {
                int year = StringUtil.strToInt(value.substring(0,4), 0);
                return --year + "-" + month;
            }
            else return value.substring(0,5) + --month;
        }
        if (type.equals("date")) {
            String query = "xs:date('" + value + "') - xs:dayTimeDuration('P1D')";
            return evaluateQuery(query, null);
        }
        if (type.equals("time")) {
            String query = "xs:time('" + value + "') - xs:dayTimeDuration('P1S')";
            return evaluateQuery(query, null);
        }
        if (type.equals("dateTime")) {
            String query = "xs:dateTime('" + value + "') - xs:dayTimeDuration('P1S')";
            return evaluateQuery(query, null);
        }
        return "";
    }


    private String generateTotalDigitsValue(int totalDigits) {
        char[] chars = new char[totalDigits];
        for (int i=0; i< totalDigits; i++) chars[i] = '1';
        return new String(chars);
    }


    private String generateFractionDigitsValue(int fractionDigits) {
        return "0." + generateTotalDigitsValue(fractionDigits);
    }


    private String generateLengthValue(int length) {
        String type = _restriction.getBaseType();
        if (XSDType.isListType(type)) {
            StringBuilder s = new StringBuilder();
            for (int i=0; i< length; i++) {
                if (s.length() > 0) s.append(" ");
                s.append("name");
            }
            return s.toString();
        }
        if (XSDType.isBinaryType(type)) {
            return generateTotalDigitsValue(length * 2);
        }
        return generateTotalDigitsValue(length);
    }


    private String evaluateQuery(String query, String def) {
        try {
            return SaxonUtil.evaluateQuery(query, null);
        }
        catch (Exception e) {
            return def;
        }
    }


    private String generatePatternMatch(String pattern) {
        Automaton automaton = new RegExp(pattern).toAutomaton();
        StringBuilder builder = new StringBuilder();
        generatePatternMatch(builder, automaton.getInitialState(), new Random());
        return builder.toString();
    }


    private void generatePatternMatch(StringBuilder builder, State state, Random random) {
        List<Transition> transitions = state.getSortedTransitions(true);
        if (transitions.size() == 0) {
            return;
        }
        int options = state.isAccept() ? transitions.size() : transitions.size() - 1;
        int option = getRandomInt(0, options, random);
        if (state.isAccept() && option == 0) {          // 0 is considered stop
            return;
        }
        // Move to next transition
        Transition transition = transitions.get(option - (state.isAccept() ? 1 : 0));
        char c = (char) getRandomInt(transition.getMin(), transition.getMax(), random);
        builder.append(c);
        generatePatternMatch(builder, transition.getDest(), random);
    }


    private int getRandomInt(int min, int max, Random random) {
        int dif = max - min;
        float number = random.nextFloat();              // 0 <= number < 1
        return min + Math.round(number * dif);
    }

}
