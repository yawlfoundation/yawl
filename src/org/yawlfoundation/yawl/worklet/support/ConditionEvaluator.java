/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.worklet.support;

import net.sf.saxon.s9api.SaxonApiException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.jdom2.Document;
import org.jdom2.Element;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.SaxonUtil;
import org.yawlfoundation.yawl.util.StringUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;


/** ConditionEvaluator is a member class of the Worklet Dynamic Selection
 *  Service. It is used here by the RdrNode class to evaluate the its condition 
 *  and thus allow the rule traversal to occur.
 *
 *  It takes an expression (provided as a String) and evaluates it
 *  to a boolean value. The datalist member is a JDOM Element that, if 
 *  supplied, will be used to retrieve values for any variable names used 
 *  in the expression.
 *
 *  The expression may contain the following operators:
 *      - Arithmetic: * / + -
 *      - Comparison: = != > < >= <=
 *      - Logical:    & | !
 *
 *  The order of precedence observed is:
 *      1.  * /
 *      2.  + -
 *      3. the comparison operators
 *      4. the logical operators
 *
 *  Operands may be numeric literals, string literals or variable names.
 *
 *  Parentheses may be used to group sub-expressions.
 *
 *  An RdrConditionException will be raised if the expression is malformed or
 *  does not evaluate to a boolean value (see getMessage() for the kinds of
 *  things that can go wrong).
 *
 *  @author Michael Adams
 *  v0.8, 04-09/2006
 */

public class ConditionEvaluator {

    // define the operators
    private static final String[] _NumericOps = { "+", "-", "*", "/" } ;
    private static final String[] _CompareOps = { "=", "!=", ">", ">=", "<", "<=" } ;
    private static final String[] _BooleanOps = { "&", "|" } ;
    private static final String[] _UnaryOps   = { "+", "-", "!" } ;
    private static final String[] _AllOps     = { "*", "/", "+", "-", ">=", "<=",
            "<", ">", "!=", "=", "&", "|", "!"} ;

    private static final Logger _log = LogManager.getLogger(ConditionEvaluator.class);


    public ConditionEvaluator() {
        // setLogLevel(_log, Level.ERROR);
    }


    /**
     *  Evaluate the condition using the datalist of variables and values.
     *  @param cond - the condition to evaluate
     *  @param data - the datalist of variables and values
     *
     *  @return the boolean result of the evaluation
     */
    public boolean evaluate(String cond, Element data) throws RdrConditionException {
        if (StringUtil.isNullOrEmpty(cond)) {
            throw new RdrConditionException("Cannot evaluate tree: condition is empty");
        }
        if (data == null) {
            throw new RdrConditionException("Cannot evaluate tree: data element is null");
        }

        String result;

        // DEBUG: log received items
        _log.debug("Received condition: {}");
        _log.debug("Data = {}", JDOMUtil.elementToString(data)) ;

        // check if it's an XQuery or cost predicate
        if (cond.startsWith("{") || cond.startsWith("/")) {
            result = evaluateXQuery(cond, data);
        }
        else {
            result = parseAndEvaluate(cond, data) ;            // evaluate ordinary condition
        }

        // if a boolean result, return it
        if (isBoolean(result))
            return result.equalsIgnoreCase("TRUE") ;
        else throw new RdrConditionException(getMessage(1));   // result not T/F
    }

    //==========================================================================//

    /**
     * ERROR MESSAGE LIST
     */

    private String getMessage(int ix) {
        String[] msg = {
                "Expression string has not yet been initialized",              //  0
                "Expression does not evaluate to a boolean value",
                "Expression is invalid - contains mis-ordered tokens",
                "Expression contains unterminated literal string",
                "Expression contains an invalid literal numeric token",
                "Attempted to retrieve numeric value for non-numeric token",   //  5
                "Attempted to retrieve boolean value for non-boolean token",
                "Attempted to retrieve string value for non-string token",
                "Invalid numeric comparison operator for operands",
                "Invalid boolean comparison operator for operands",
                "Invalid string comparison operator for operands",             // 10
                "Invalid numeric operator for arithmetic operands",
                "Malformed operators in Expression",
                "Could not determine operation type",
                "DataList element has not yet been initialized",
                "Left and right operands are different data types"} ;          // 15
        return msg[ix] ;
    }

    //==========================================================================//

    /**
     * "IS" VALIDATION METHODS
     */

    /** @return true if string = "true" or "false" (case insensitive) */
    private boolean isBoolean(String s) {
        return (s.equalsIgnoreCase("TRUE")) || (s.equalsIgnoreCase("FALSE")) ;
    }


    /** @return true if whole expression is a single string surrounded by "" */
    private boolean isString(String s) {
        return (s.indexOf('"') == 0) &&
                (s.lastIndexOf('"') == s.length() - 1) ;
    }


    /** @return true if whole string represents a valid number */
    private boolean isNumber(String s) {
        return ( isInteger(s) || isDouble(s) ) ;
    }


    /** @return true if whole string represents a valid integer */
    private boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
            return true ;
        }
        catch (NumberFormatException e) {
            return false ;
        }
    }


    /** @return true if whole string represents a valid double */
    private boolean isDouble(String s) {
        try {
            Double.parseDouble(s);
            return true ;
        }
        catch (NumberFormatException e) {
            return false ;
        }
    }


    /** @return true if operator is a unary */
    private boolean isUnaryOperator(String s) {
        return ( ( s.startsWith("-") ) || ( s.startsWith("+") ) ) ;
    }


    /** @return true if expression is a literal value (string or numeric) */
    private boolean isLiteralValue(String s, Element data) {
        return isString(s) || isBoolean(s) || isNumber(s) || !isVarName(s, data) ;
    }


    /** @return true if expression is the name of a child of the _datalist
     *          Element (i.e. is the name of an item of data) */
    private boolean isVarName(String s, Element data) {
        Element var = data != null ? data.getChild(s) : null ;
        return (var != null) ;
    }


    /** @return true if expression is a registered function name in the
     *  RdrConditionFunctions class */
    private boolean isFunctionName(String s) {
        return s != null && (isCostFunctionName(s) ||
                RdrConditionFunctions.isRegisteredFunction(s) ||
                RdrFunctionLoader.getNames().contains(s)) ;
    }


    private boolean isFunctionCall(String s) {
        return s.endsWith("]") ;
    }


    private boolean isCostExpression(String s) {
        return s.startsWith("cost[") ||
                s.startsWith("cheapestVariant[") ||
                s.startsWith("dearestVariant[");
    }

    private boolean isCostFunctionName(String s) {
        return s.startsWith("cost") ||
                s.startsWith("cheapestVariant") ||
                s.startsWith("dearestVariant");
    }


    /** @return true if expression is of the leftop/operator/rightop kind */
    private boolean isSimpleExpression(String s) {

        if (isString(s) || (s.length() == 0)) return false ;

        // ignore leading sign
        if ( s.startsWith("+") || s.startsWith("-") ) s = s.substring(1) ;

        // look for an operator
        for (String _AllOp : _AllOps) if (s.indexOf(_AllOp) > 0) return true;

        return false ;	//no ops found
    }


    /** @return true if 'op' is a valid numeric operator */
    private boolean isNumericOp(String op) {
        return isInArray(op, _NumericOps) ;
    }


    /** @return true if 'op' is a valid boolean operator */
    private boolean isBooleanOp(String op) {
        return isInArray(op, _BooleanOps) ;
    }


    /** @return true if 'op' is a valid operator */
    private boolean isOperator(String op) {
        return isInArray(op, _AllOps) ;
    }


    /** @return true if 's' is a member element of array 'a' */
    private boolean isInArray(String s, String[] a) {

        for (String element : a) {
            if (s.compareTo(element) == 0) return true;
        }
        return false ;
    }

    /** @return true if 'c' is one of '0'-'9' or '.' */
    private boolean isDigitOrDot(char c) {
        return Character.isDigit(c) || (c == '.') ;
    }

    private boolean isNumeric(char c) {
        return isDigitOrDot(c) || (c == '-') || (c == '+');
    }


    /** @return true if 'c' is one of 'a'-'z' or 'A'-'Z' or '_' */
    private boolean isLetterOrUScore(char c) {
        return Character.isLetter(c) || (c == '_') ;
    }


    /** @return true if 'c' is one of 'a'-'z', 'A'-'Z', '_', '0'-'9' or '.' */
    private boolean isValidVarNameChar(char c) {
        return Character.isDigit(c) || isLetterOrUScore(c) ;
    }


    /** @return true if 'c' is a valid operator character */
    private boolean isOperator(char c) {
        char[] opChar = { '*', '/', '+', '-', '>', '<', '!', '=', '&', '|', '!'} ;
        for (char anOpChar : opChar) if (c == anOpChar) return true;

        return false ;
    }


    private boolean isFunctionArgumentDelimiter(String s, int fadPos) {
        if (fadPos == 0) return false;
        int tmp = fadPos - 1 ;

        // find start of token preceding the '('
        while ((tmp >0) && isValidVarNameChar(s.charAt(tmp))) tmp-- ;
        return isFunctionName(s.substring(tmp, fadPos));
    }


    /** replace ()'s with []'s where they represent function argument delimiters */
    private String maskArgumentDelimiters(String s, int fadPos)
            throws RdrConditionException {
        char[] array = s.toCharArray();
        int counter = 0;
        for (int i = fadPos; i < s.length(); i++) {
            char c = array[i];
            if (c == '(') {
                array[i] = '[';
                counter++;
            }
            else if (c == ')') {
                array[i] = ']';
                counter--;
            }
            if (counter == 0) break;
        }
        if (counter == 0) return new String(array);

        throw new RdrConditionException("Invalid expression: unbalanced parentheses");
    }



    //==========================================================================//


    /**
     *  STRING MANIPULATION METHODS
     */

    /** removes the double quotes from around a string */
    private String deQuote(String s) {
        return s.substring(1, s.length() - 1) ;
    }


    /** replaces all signs fronting numeric values with placeholding '@' 's */
    private String maskUnaryOps(String s) {

        StringBuilder sb = new StringBuilder(s) ;
        String opSet = "*/+-><=", unSet = "+-" ;
        int unPos = -1, j ;

        // mask any leading + or -
        if (unSet.indexOf(sb.charAt(0)) > -1) sb.setCharAt(0, '@') ;

        for (int i=0; i < unSet.length();i++) {    // for + and -
            unPos = sb.indexOf(unSet.substring(i, i+1), unPos + 1) ;

            while ((unPos > -1) && (unPos < sb.length())) {
                j = unPos - 1 ;
                while (( j > 0 ) && ( sb.charAt(j) == ' ' )) --j ;  //go back thru wspace

                // if sign is preceded by another sign, mask it
                if (opSet.indexOf(sb.charAt(j)) > -1)
                    sb.setCharAt(unPos, '@') ;
                unPos = sb.indexOf(unSet.substring(i, i+1), unPos + 1) ;
            }
        }
        return sb.toString() ;
    }


    /** returns a parenthesised sub expression within a string expression */
    private String extractSubExpr(String s) {
        int parCount = 1, start, i ;

        start = Pos( '(', s ) ;                          // find opening '('
        if (start == -1) return "" ;

        for (i = start + 1; i < s.length(); i++) {       // find matching ')'
            if (s.charAt(i) == '(' ) parCount++ ;
            else if (s.charAt(i) == ')' ) parCount-- ;

            if (parCount == 0) break ;
        }

        // return operand with the enclosing parentheses
        return s.substring(start, i + 1 ) ;
    }


    /** removes and returns an embedded string literal from an expression */
    private String extractString(String s) {
        int start = s.indexOf('"') ;
        int end = s.indexOf('"', start + 1) ;

        if ((start == -1) || (end == -1)) return "" ;   // 0 or one quote only

        return s.substring(start, end + 1) ;
    }


    /** replaces the first instance of "cut" in "s" with "paste" */
    private String replaceStr(String s, String cut, String paste) {
        StringBuilder b = new StringBuilder(s) ;
        int insPos = b.indexOf(cut) ;
        b.delete(insPos, insPos + cut.length()) ;
        b.insert(insPos, paste) ;
        return b.toString() ;
    }


    //==========================================================================//

    /**
     *  PARSING METHODS
     */


    private String evaluateXQuery(String expr, Element data) throws RdrConditionException {
        try {
            if (expr.startsWith("{")) expr = deQuote(expr);      // remove braces
            String query = String.format("boolean(%s)", expr);
            return SaxonUtil.evaluateQuery(query, new Document(data));
        }
        catch (SaxonApiException sae) {
            throw new RdrConditionException("Invalid XPath expression (" + expr + ").");
        }
    }

    /** returns the index position of 'c' in 's', or -1 if not found */
    private int Pos(char c, String s) {
        return s.indexOf(c) ;
    }


    /** returns the index in 'a' of string 's', or -1 if not found */
    private int indexOfArray(String[] a, String s) {
        for (int i=0; i<a.length; i++) {
            if (s.compareTo(a[i]) == 0) return i ;
        }
        return -1 ;
    }


    /** finds the position of the left most operator for a level of precedence
     *  @param lowerBound, upperBound - the range of ops to search for
     **/

    private int findLeftMostOp(int lowerBound, int upperBound, String[] s) {

        int foundPos, leftMostOpPos = 10000 ;
        boolean found = false ;

        for (int i = lowerBound; i <= upperBound; i++) {
            foundPos = indexOfArray(s, _AllOps[i]) ;
            if ((foundPos > -1) && (foundPos < leftMostOpPos)) {
                leftMostOpPos = foundPos ;
                found = true ;
            }
        }
        if (found)
            return leftMostOpPos ;
        else
            return -1 ;
    }


    /** finds the position of next operator in an expression,
     *  in order of precedence */
    private int findNextOperator(String[] s) {

        int op = findLeftMostOp( 0, 1, s ) ;
        if (op < 0) op = findLeftMostOp( 2, 3, s ) ;
        if (op < 0) op = findLeftMostOp( 4, 7, s ) ;
        if (op < 0) op = findLeftMostOp( 8, 9, s ) ;
        if (op < 0) op = findLeftMostOp( 10, 12, s ) ;
        return op ;
    }



    /**
     * replaces a subexpression with its evaluated result
     * @param a - the array of tokens for the whole expression
     * @param ix - the index of the operator token
     * @param val - the evaluated value of the subexpression
     */
    private String[] reduceTokens(String[] a, int ix, String val) {
        String[] result = new String[a.length-2] ;  // replace 3 tokens with 1
        int i, j ;

        for (i=0;i<ix;i++) result[i] = a[i] ;      // copy array up to op index

        result[--i] = val ;                          // replace with simple val
        for (j=i+1;j<result.length;j++) result[j] = a[j+2] ;     //copy rest
        return result ;
    }


    /** Splits an expression into its token parts 
     *  pre: any parenthesised sub expressions have been evaluated to 
     *       simple values 	
     */
    private String[] tokenize(String s) throws RdrConditionException {
        Vector<String> v = new Vector<String>() ;
        int ix = 0, ln = s.length();
        String token ;
        String[] result ;

        while (ix < ln) {

            // unary ops???

            ix = skipWhitespace(s, ix) ;
            if (ix == ln) break ;

            // get next token
            if (s.charAt(ix) == '"') {                     // literal string
                token = getLiteralString(s, ix) ;
            }
            else if (isDigitOrDot(s.charAt(ix))) {         // literal number
                token = getLiteralNumber(s, ix) ;
            }
            else if (isLetterOrUScore(s.charAt(ix))) {      // var or function name
                token = getVarName(s, ix) ;
            }
            else if (isOperator(s.charAt(ix))) {            // operator
                token = getOperator(s, ix) ;
            }
            else {
                String msg = "Expression contains an invalid token at char " + ix;
                throw new RdrConditionException(msg) ;
            }
            v.addElement(token) ;
            ix += token.length() ;
        }

        result = new String[v.size()] ;
        v.copyInto(result);

        // make sure tokens are ordered correctly
        if (validateTokenization(result))
            return result ;
        else throw new RdrConditionException(getMessage(2)) ;

    }


    /** skip any whitespace by incrementing the start char over it */
    private int skipWhitespace(String s, int start) {
        while ((start < s.length()) && Character.isWhitespace(s.charAt(start)))
            start++ ;
        return start ;
    }


    /** returns an embedded literal string of the form "abc"
     *  pre: s[start] == quote char
     */
    private String getLiteralString(String s, int start)
            throws RdrConditionException {
        int tmp = s.indexOf('"', start + 1) ;

        // no ending quote
        if (tmp == -1) throw new RdrConditionException(getMessage(3)) ;

        return s.substring(start, tmp + 1) ;
    }


    /** returns a literal number from start position in s */
    private String getLiteralNumber(String s, int start)
            throws RdrConditionException {
        int tmp = start + 1 ;
        String result ;
        while ((tmp < s.length()) && isDigitOrDot(s.charAt(tmp))) tmp++ ;
        result = s.substring(start, tmp) ;
        if (! isNumber(result)) throw new RdrConditionException(getMessage(4)) ;
        return result ;
    }


    /** returns a variable or function name from start position in expression */
    private String getVarName(String s, int start) throws RdrConditionException {
        String result ;
        int tmp = start + 1 ;

        while ((tmp < s.length()) && isValidVarNameChar(s.charAt(tmp))) tmp++ ;
        result = s.substring(start, tmp) ;
        if (isFunctionName(result)) {                     // read arguments also
            tmp = findCloserIndex(s, tmp, '[', ']');
            tmp++ ;                                       // add one more for the ']'
        }

        if (tmp > 0) return s.substring(start, tmp);
        throw new RdrConditionException("Invalid expression: unbalanced parentheses");
    }


    private int findCloserIndex(String s, int from, char left, char right) {
        int counter = 0;
        for (int i = from; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == left) counter++;
            else if (c == right) counter--;
            if (counter == 0) return i;
        }
        return -1;
    }


    /** returns an operator from start position in expression */
    private String getOperator(String s, int start) {
        int tmp = start + 1 ;
        while ((tmp < s.length()) && isOperator(s.charAt(tmp))) tmp++ ;
        return s.substring(start, tmp) ;
    }


    /** @return true if alltokens are in a valid order (term op term ...) */
    private boolean validateTokenization(String[] a) {

        //a.length must be odd
        if ((a.length % 2) == 0) return false ;

        //every even element must not be an op
        for (int i=0;i<a.length;i+=2) if (isOperator(a[i])) return false ;

        //every odd element must be an op
        for (int i=1;i<a.length;i+=2) if (!isOperator(a[i])) return false ;

        //all checks out!
        return true ;
    }



    //==========================================================================//

    /**
     *  EVALUATION METHODS
     */


    /** parses and evaluates expression 's' using operator precedence */
    private String parseAndEvaluate(String s, Element data) throws RdrConditionException {

        String subExpr, ans ;
        String[] tokens ;
        int opIndex ;
        boolean negation = false;
        int parIndex = s.indexOf('(');

        while (parIndex > -1) {

            // special case if () are part of a cost expression or function call
            if (isFunctionArgumentDelimiter(s, parIndex)) {
                s = maskArgumentDelimiters(s, parIndex);
            }
            else {
                // evaluate parenthesised sub-expressions first
                subExpr = extractSubExpr(s) ;                      // get ( subexpr )
                ans = parseAndEvaluate(deQuote(subExpr), data) ;   // recurse
                s = replaceStr(s, subExpr, ans) ;                  // insert result
            }
            parIndex = s.indexOf('(');
        }
        if (s.charAt(0) == '!') {
            negation = true;
            s = s.substring(1);
        }

        // break expression tokens into a string array
        tokens = tokenize(s.trim()) ;            // ( ) any have been removed

        if (_log.isDebugEnabled())
            for (int i=0;i<tokens.length;i++)
                _log.debug("token {} = {}", i, tokens[i]) ;

        opIndex = findNextOperator(tokens) ;

        // while the expression has more operators, evaluate a part
        while (opIndex > -1) {
            ans = evalExpression(tokens[opIndex-1], tokens[opIndex],
                    tokens[opIndex+1], data) ;
            tokens = reduceTokens(tokens, opIndex, ans) ;

            if (_log.isDebugEnabled())
                for (int i=0;i<tokens.length;i++)
                    _log.debug("token {} = {}", i, tokens[i]) ;

            opIndex = findNextOperator(tokens) ;
        }

        // one token left - can be boolean string or single (boolean) function call
        if (isFunctionCall(tokens[0]))
            tokens[0] = evalFunction(tokens[0], data);
        if (negation) {
            tokens[0] = tokens[0].equalsIgnoreCase("true") ? "false" : "true";
        }

        return tokens[0] ;                // 'true' or 'false' if all went well!
    }



    /** evaluates an expression and returns the result
     *  @param lOp - the left operand
     *  @param operator - as the name implies
     *  @param rOp - the right operand
     */
    private String evalExpression(String lOp, String operator, String rOp, Element data)
            throws RdrConditionException {

        // if either op is a function call, replace it with its evaluation
        if (isFunctionCall(lOp)) lOp = getFunctionResult(lOp, data) ;
        if (isFunctionCall(rOp)) rOp = getFunctionResult(rOp, data) ;

        // if either op is a varname, replace it with its value
        if (!isLiteralValue(lOp, data)) lOp = getVarValue(lOp, data) ;
        if (!isLiteralValue(rOp, data)) rOp = getVarValue(rOp, data) ;

        // make sure any data variables used contain valid data
        if ((lOp.equals("undefined")) || (rOp.equals("undefined") ) ||
                (lOp.length() == 0 )  || (rOp.length() == 0) ) {
            throw new RdrConditionException(getMessage(12)) ;
        }

        // make sure the two operands are the same data type
        if ((isNumber(lOp) && !isNumber(rOp))  ||
                (isBoolean(lOp) && !isBoolean(rOp))) {
            throw new RdrConditionException(getMessage(15) + ". Left = " +
                    lOp + ", Right = " + rOp) ;
        }

        // evaluate depending on the operator data types
        if (isNumber(lOp))
            return doNumericOperation(lOp, operator, rOp) ;
        else if (isBoolean(lOp))
            return doBooleanOperation(lOp, operator, rOp) ;
        else
            return doStringOperation(lOp, operator, rOp) ;
    }


    /**
     * Evaluates a function embedded in a condition
     * PRE: the function is a member of the ceFunctions class
     * @param func the function to call
     * @return the result of the function
     */
    private String evalFunction(String func, Element data) throws RdrConditionException {

        // special case: cost expression
        if (isCostExpression(func)) {
            String exp = func.replace('[', '(').replace(']', ')');
            return new CostPredicateEvaluator().evaluate(exp, data);
        }

        String funcName, varName, varValue, result ;
        Map<String, String> args = new HashMap<String, String>() ;

        // strip out arguments & get their values
        String[] argList = parseArgsList(func);
        for (String arg : argList) {
            varName = arg.trim();
            varValue = getVarValue(varName, data);
            args.put(varName, varValue);
        }

        // extract function name
        funcName = func.substring(0, func.indexOf('['));

        // run function
        if (RdrConditionFunctions.isRegisteredFunction(funcName)) {
            result = RdrConditionFunctions.execute(funcName, args) ;
        }
        else {
            result = RdrFunctionLoader.execute(funcName, args);
        }
        if (result == null) result = "null";
        return result ;
    }

    /** translates a bad result to 'undefined' */
    private String getFunctionResult(String func, Element data) throws RdrConditionException {
        return clarifyResult(evalFunction(func, data));
    }


    /** retrieves the value for a variable or function from the datalist Element */
    private String getVarValue(String var, Element data) {
        _log.debug("in getVarValue, var = {}", var) ;

        // var "this" refers to the workitem associated with the task named in this rule
        String result = var.equalsIgnoreCase("this") ? getThisData(data) :
                data.getChildText(var) ;

        //   	      return formatVarValue(result) ;
        return clarifyResult(result);
    }


    private String clarifyResult(String result) {
        if (result == null || result.equals("null")) result = "undefined";
        else if (result.length() == 0) result = "nodata";
        return result;
    }


    /** extracts the names of arguments from a function call */
    private String[] parseArgsList(String list) {
        int start = list.indexOf('[');
        int end = findCloserIndex(list, start, '[', ']');
        String result = list.substring(start + 1, end);      // remove [ ]
        return result.split(",");
    }

    /** get the value of the 'this' argument */
    private String getThisData(Element data) {
        String result = null;
        Element eThis = data.getChild("process_info").getChild("workItemRecord");
        if (eThis != null) result = JDOMUtil.elementToString(eThis);
        return result;
    }


    /**
     * If the value is a string, en-quote it
     * At this level, a value can be either a number, boolean or string
     */
    private String formatVarValue(String val) {
        if (isNumber(val) || isBoolean(val)) return val ;

        // only need to format a string literal
        return "\"" + val + "\"" ;
    }


    /** Convert operands to numbers and perform operation */
    private String doNumericOperation(String l, String op, String r)
            throws RdrConditionException {
        if (l.equals("nodata")) l = "0";
        if (r.equals("nodata")) r = "0";
        double dLeft = Double.parseDouble(l) ;
        double dRight = Double.parseDouble(r) ;

        if (isNumericOp(op))
            return doArithmeticOperation(dLeft, op, dRight) ;
        else
            return doNumericComparison(dLeft, op, dRight) ;

    }


    /** performs the comparison and returns "true" or "false" */
    private String doNumericComparison(double l, String op, double r)
            throws RdrConditionException {
        if (op.compareTo("=") == 0)  return String.valueOf(l == r) ;
        if (op.compareTo(">") == 0)  return String.valueOf(l >  r) ;
        if (op.compareTo(">=") == 0) return String.valueOf(l >= r) ;
        if (op.compareTo("<") == 0)  return String.valueOf(l <  r) ;
        if (op.compareTo("<=") == 0) return String.valueOf(l <= r) ;
        if (op.compareTo("!=") == 0) return String.valueOf(l != r) ;
        throw new RdrConditionException(getMessage(8)) ;  // error if gets here
    }


    /** performs the operation and returns result as a string */
    private String doArithmeticOperation(double l, String op, double r)
            throws RdrConditionException {
        if (op.compareTo("+") == 0) return String.valueOf(l + r) ;
        if (op.compareTo("-") == 0) return String.valueOf(l - r) ;
        if (op.compareTo("*") == 0) return String.valueOf(l * r) ;
        if (op.compareTo("/") == 0) return String.valueOf(l / r) ;
        throw new RdrConditionException(getMessage(11)) ;  // error if gets here
    }


    /** performs the operation and returns "true" or "false" */
    private String doBooleanOperation(String l, String op, String r)
            throws RdrConditionException {
        // convert string operands to boolean
        boolean bLeft = (l.equalsIgnoreCase("TRUE")) ;
        boolean bRight = (r.equalsIgnoreCase("TRUE")) ;

        if (op.compareTo("=") == 0)  return String.valueOf(bLeft == bRight) ;
        if (op.compareTo("!=") == 0) return String.valueOf(bLeft != bRight) ;
        if (op.compareTo("&") == 0)  return String.valueOf(bLeft && bRight) ;
        if (op.compareTo("|") == 0)  return String.valueOf(bLeft || bRight) ;
        throw new RdrConditionException(getMessage(9)) ;  // error if gets here
    }


    /** performs the comparison and returns "true" or "false" */
    private String doStringOperation(String l, String op, String r)
            throws RdrConditionException {
        if (isString(l)) l = deQuote(l);
        if (isString(r)) r = deQuote(r);
        if (l.equals("nodata")) l = "";
        if (r.equals("nodata")) r = "";
        if (op.compareTo("=") == 0)
            return String.valueOf(l.compareTo(r) == 0) ;
        if (op.compareTo("!=") == 0)
            return String.valueOf(l.compareTo(r) != 0) ;
        throw new RdrConditionException(getMessage(10)) ;  // error if gets here
    }


    private static void setLogLevel(Logger logger, Level level) {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig(logger.getName());
        loggerConfig.setLevel(level);
        ctx.updateLoggers();
    }


    /*********************************************************************/

    public static void main(String args[]) {
        // unit testing
        //  String s = "(Name = JOHN)";
        String s = "-50 > 20";
        Element e = new Element("testElement");
        e.setAttribute("nval", "17") ;
        e.setAttribute("sval", "\"apples\"") ;
        e.setAttribute("bval", "true") ;
        Element d = new Element("Age");
        d.setText("30");
        e.addContent(d) ;

        ConditionEvaluator t = new ConditionEvaluator();

        try {
            //		    t.p(t.evalExpression("27", "!=", "26")) ;
            s = "cost(case()) > 5";
            boolean b = t.evaluate(s, e) ;
            t.p("expression: " + s + ", returns: " + b) ;
        }
        catch ( RdrConditionException re) { re.printStackTrace() ;}

    }

    private void p(String s) {
        System.out.println(s);
    }

    private void p(boolean b) {
        System.out.println(b);
    }

} // end class ConditionEvaluator
    




