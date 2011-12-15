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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

/**
 * Parses and evaluates a numeric expression. Valid operators are + - * and \ (note
 * the backslash for division). Sub-expressions may be parenthesised to any level.
 * @author Michael Adams
 * @date 15/11/11
 */
public class ExpressionParser {

    private String expression;
    private Map<String, String> variables;

    // operators grouped in reverse order of precedence
    private static final String[] ORDERED_OPERATORS = { "+-", "*\\" };
    
    
    public ExpressionParser() { }

    /**
     * Creates a new parser
     * @param expr the expression to parse
     * @param vars a set of variables and their values
     */
    public ExpressionParser(String expr, Map<String, String> vars) {
        setExpression(expr);
        setVariables(vars);
    }


    /**
     * sets the map of variables and their values for this parser
     * @param vars the map of variables and their values
     */
    public void setVariables(Map<String, String> vars) { variables = vars; }


    /**
     * Sets the primary expression for this parser
     * @param expr the expression to parse
     */
    public void setExpression(String expr) { expression = expr; }


    /**
     * Evaluates a previously added expression
     * @return the result of the evaluation
     * @throws NumberFormatException if the expression is malformed
     */
    public double evaluate() {
        return evaluate(expression);
    }


    /**
     * Evaluates an expression
     * @param expr the expression to evaluate
     * @return the result of the evaluation
     * @throws NumberFormatException if the expression is malformed
     */
    public double evaluate(String expr) {
        if (expr != null) {
            ExprNode root = parse(expr);
            return root.evaluate();
        }
        throw new NumberFormatException("Null expression");
    }
    

    /********************************************************************************/

    /**
     * Parses an expression into a binary tree
     * @param expr the expression to parse
     * @return the root node of the tree
     */
    private ExprNode parse(String expr) {
        expr = expr.trim();

        // sub-evaluate parenthesised sub-expressions
        while (expr.contains("(")) {
            expr = parenthesisParse(expr);
        }

        char[] chars = expr.toCharArray();
        ExprNode node = new ExprNode();
        for (String ops : ORDERED_OPERATORS) {

            // parse right-to-left so that tree can be evaluated bottom-to-top
            for (int i = chars.length -1; i > 0; i--) {   // start at 1 handles unary ops
                if (chars[i] == ' ') continue;            // ignore spaces
                if (ops.indexOf(chars[i]) > -1) {
                    node.content = String.valueOf(chars[i]);
                    if (i > 0) node.left = parse(expr.substring(0, i));
                    if ((i + 1) < expr.length()) node.right = parse(expr.substring(i + 1));
                    return node;
                }    
            }
        }
        node.content = expr;               // no ops found in expression = simple content
        return node;
    }


    /**
     * Parses a parentheses sub-expression within an expression and replaces it in the
     * expression with its evaluated result
     * @param expr the full expression, containing a parentheses part
     * @return the expression, with the part replaced with its evaluation
     */
    private String parenthesisParse(String expr) {
        char[] chars = expr.toCharArray();
        int start = expr.indexOf('(');
        int open = 0;
        for (int i = start; i < chars.length; i++) {
            if (chars[i] == '(') open++;
            if (chars[i] == ')') open--;
            if (open == 0) {
                StringBuilder sb = new StringBuilder(chars.length);
                sb.append(chars, 0, start);
                sb.append(parse(expr.substring(start + 1, i)).evaluate());
                if (i < chars.length - 1) sb.append(chars, i + 1, chars.length - i - 1);
                return sb.toString();
            }
        }
        throw new NumberFormatException("Unbalanced parentheses in expression");
    }


    /**********************************************************************************/

    /**
     * One node in a binary tree representing a parsed arithmetic expression
     */
    class ExprNode {
        String content;
        ExprNode left;
        ExprNode right;


        /**
         * Evaluates a (sub-) expression, using this node as the root
         * @return the numeric result of the evaluation
         * @throws NumberFormatException if the expression is unbalanced
         */
        double evaluate() {
            if (isOperator(content) && (left == null || right == null)) {
                throw new NumberFormatException("Unbalanced expression");
            }
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
            return valueOf();                  // not an operator
        }


        /**
         * Checks if the string passed is a known operator
         * @param s the string to check
         * @return true if the string passed is a known operator
         */
        boolean isOperator(String s) {
            for (String ops : ORDERED_OPERATORS) {
                if (ops.contains(s)) return true;
            }
            return false;
        }


        /**
         * Gets the value of this node. If the node's content is non-numeric, it is
         * assumed to be a variable name, in which case the variable's corresponding
         * value is returned if it exists in the variable map.
         * @return the node's value
         * @throws NumberFormatException if the node's content is non-numeric and does
         * not match the name of a known variable
         */
        double valueOf() {
            if (isValidDouble(content)) {
                return Double.valueOf(content);
            }
            if ((variables != null) && variables.containsKey(content)) {
                return Double.valueOf(variables.get(content));
            }
            
            // not a valid number or a variable
            String err;
            if (Character.isJavaIdentifierStart(content.charAt(0)))
                err = "Unknown variable: ";
            else if (Character.isDigit(content.charAt(0)))
                err = "Invalid number: ";
            else err = "Malformed expression part: ";
            throw new NumberFormatException(err + content);
        }


        /**
         * Checks if a string can be converted to a valid double value
         * @param s the string to check
         * @return whether the string can be converted without error
         */
        boolean isValidDouble(String s) {
            if (s == null || s.length() == 0) return false;
            boolean foundDecimal = false;
            char[] chars = s.toCharArray();
            int start = (chars[0] == '+' || chars[0] == '-') ? 1 : 0;     // unary op
            for (int i = start; i < chars.length; i++) {
                if (Character.isDigit(chars[i])) continue;
                if ((chars[i] == '.') && (! foundDecimal)) {         // one '.' allowed
                    foundDecimal = true;
                    continue;
                }
                return false;                                       // non-double char
            }
            return true;                                            // all chars passed
        }
    }


    /**********************************************************************************/

    /**
     * Unit tests the class with expressions entered from the command line
     * @param args runtime arguments - none required
     */
    public static void main(String[] args) {
        ExpressionParser parser = new ExpressionParser();
        String expression;
        BufferedReader c = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            try {
                System.out.print("Enter expression ('X' to exit): ");
                expression = c.readLine();
                if (expression.equalsIgnoreCase("x")) break;           // exit
                long start = System.currentTimeMillis();
                double result = parser.evaluate(expression);
                long duration = System.currentTimeMillis() - start;
                System.out.println("Result: " + result + " (" + duration + " msecs)");
            }
            catch (NumberFormatException nfe) {
                System.err.println("Result: " + nfe.getMessage());
            }
            catch (IOException ioe) {
                System.err.println(ioe.getMessage());
                break;
            }            
        }
    }
}
