/*
 * Copyright (c) 2004-2011 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.cost.data;

import java.util.Map;

/**
 * @author Michael Adams
 * @date 15/11/11
 */
public class ExpressionParser {

    private String expression;
    private Map<String, String> variables;

    // reverse order of precedence
    private static final String[] ORDERED_OPERATORS = { "+-", "*\\" };
    
    
    public ExpressionParser() { }
    
    public ExpressionParser(String expr, Map<String, String> vars) {
        setExpression(expr);
        setVariables(vars);
    }


    public void setVariables(Map<String, String> vars) { variables = vars; }

    public void setExpression(String expr) { expression = expr; }


    public double evaluate() {
        BinaryNode root = parse(expression.trim());
        return root.evaluate();
    }


    public double evaluate(String expr) {
        setExpression(expr);
        return evaluate();
    }
    

    /********************************************************************************/

    private BinaryNode parse(String expr) {
        BinaryNode node = new BinaryNode();
        char[] chars = expr.toCharArray();
        for (String ops : ORDERED_OPERATORS) {
            for (int i = 1; i < chars.length; i++) {      // start at 1 handles unary ops
                if (chars[i] == ' ') continue;            // ignore spaces
                if (ops.indexOf(chars[i]) > -1) {
                    node.content = String.valueOf(chars[i]);
                    if (i > 0) node.left = parse(expr.substring(0, i).trim());
                    if ((i + 1) < expr.length()) node.right = parse(expr.substring(i + 1).trim());
                    return node;
                }    
            }
        }
        node.content = expr;               // no ops found in expression = simple content
        return node;
    }


    class BinaryNode {
        String content;
        BinaryNode left;
        BinaryNode right;

        double evaluate() {
            if (content.equals("+")) {
                return left.evaluate() + right.evaluate();
            }
            else if (content.equals("-")) {
                return left.evaluate() - right.evaluate();
            }
            else if (content.equals("*")) {
                return left.evaluate() * right.evaluate();
            }
            else if (content.equals("\\")) {
                return left.evaluate() / right.evaluate();
            }
            return valueOf();
        }


        double valueOf() {
            if (isValidDouble(content)) {
                return Double.valueOf(content);
            }
            if ((variables != null) && variables.containsKey(content)) {
                return Double.valueOf(variables.get(content));
            }
            throw new NumberFormatException("Unknown variable: " + content);
        }
        
        
        boolean isValidDouble(String s) {
            boolean foundUnary = false;
            boolean foundDecimal = false;
            for (char c : s.toCharArray()) {
                if (Character.isDigit(c)) continue;
                if (((c == '+') || (c == '-')) && (! foundUnary)) {
                    foundUnary = true;
                    continue;
                }
                if ((c == '.') && (! foundDecimal)) {
                    foundDecimal = true;
                    continue;
                }
                return false;
            }
            return true;
        }
    }
}
