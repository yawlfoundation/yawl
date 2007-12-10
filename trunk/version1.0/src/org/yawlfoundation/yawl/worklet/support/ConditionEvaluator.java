/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.worklet.support;

import org.yawlfoundation.yawl.util.JDOMUtil;

import java.util.*;

import org.jdom.Element ;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;


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
 *  BPM Group, QUT Australia
 *  m3.adams@yawlfoundation.org
 *  v0.8, 04-09/2006
 */

public class ConditionEvaluator {

    // define the operators
    private String[] _NumericOps = { "+", "-", "*", "/" } ;
    private String[] _CompareOps = { "=", "!=", ">", ">=", "<", "<=" } ;
    private String[] _BooleanOps = { "&", "|" } ;
    private String[] _UnaryOps   = { "+", "-", "!" } ;
    private String[] _AllOps     = { "*", "/", "+", "-", ">=", "<=",
                                    "<", ">", "!=", "=", "&", "|", "!"} ;

    private String _condition = null ;        // the condition to be evaluated
    private Element _dataList ;               // the list of variables & values

    private static Logger _log = Logger.getLogger("org.yawlfoundation.yawl.worklet.support.ConditionEvaluator");

    /**
     * CONSTRUCTORS
     */

    public ConditionEvaluator() {
        _log.setLevel(Level.ERROR);
    }


    public ConditionEvaluator(String cond, Element datalist) {
        this();
        _condition = cond ;
        _dataList = datalist ;
    }

//==========================================================================//    

    /**
     * PUBLIC METHODS A - SETTERS & GETTERS
     */

    public void setCondition(String cond) {
        _condition = cond ;
    }


    public String getCondition() {
        return _condition ;
    }


    public void setDatalist(Element e) {
        _dataList = e ;
    }


    public Element getDataList() {
        return _dataList ;
    }

//==========================================================================//    

    /**
     * PUBLIC METHODS B - TWO EVALUATE() VERSIONS
     */

    /**
     *  Evaluates a previously supplied condition using a previously 
     *  supplied datalist. 
     *
     *  Throws an RdrConditionException if those two items have not been
     *  previously supplied.
     */
    public boolean evaluate() throws RdrConditionException {
        if (_condition == null)
           throw new RdrConditionException(getMessage(0)) ;
        else if (_dataList == null)
           throw new RdrConditionException(getMessage(14)) ;

         return evaluate(_condition, _dataList) ;  // call parameterized version
    }

    /**
     *  Evaluate the condition using the datalist of variables and values.
     *  @param cond - the condition to evaluate
     *  @param dlist - the datalist of variables and values
     *  
     *  @return the boolean result of the evaluation
     */
    public boolean evaluate(String cond, Element dlist)
                                         throws RdrConditionException {
        _dataList = dlist ;

        // DEBUG: log received items
        _log.info("received condition: " + cond );
        _log.info("data = " + JDOMUtil.elementToString(dlist)) ;


        String result = parseAndEvaluate(cond) ;                // evaluate

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
    private boolean isLiteralValue(String s) {
       return isString(s) || isBoolean(s) || isNumber(s) || !isVarName(s) ;
    }


    /** @return true if expression is the name of a child of the _datalist
     *          Element (i.e. is the name of an item of data) */
    private boolean isVarName(String s) {
       Element var = _dataList.getChild(s) ;
       return (var != null) ;
    }


    /** @return true if expression is a registered function name in the
     *  RdrConditionFunctions class */
    private boolean isFunctionName(String s) {
       return RdrConditionFunctions.isRegisteredFunction(s) ;
    }


    private boolean isFunctionCall(String s) {
       return s.endsWith("]") ;
    }



    /** @return true if expression is of the leftop/operator/rightop kind */
    private boolean isSimpleExpression(String s) {

       if (isString(s) || (s.length() == 0)) return false ;

       // ignore leading sign
       if ( s.startsWith("+") || s.startsWith("-") ) s = s.substring(1) ;

       // look for an operator
       for (int i=0; i < _AllOps.length; i++)
             if (s.indexOf(_AllOps[i]) > 0 ) return true ;

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

          for (int i=0; i < a.length; i++) {
             if (s.compareTo(a[i]) == 0) return true ;
          }
          return false ;
    }

    /** @return true if 'c' is one of '0'-'9' or '.' */
    private boolean isDigitOrDot(char c) {
        return Character.isDigit(c) || (c == '.') ;
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
         for (int i=0; i<opChar.length;i++){
             if (c == opChar[i]) return true ;
         }
         return false ;
       }


    private boolean isFunctionArgumentDelimiter(String s, int fadPos) {
        int tmp = fadPos - 1 ;

        // find start of token preceding the '('
        while ((tmp >0) && isValidVarNameChar(s.charAt(tmp))) tmp-- ;
        return isFunctionName(s.substring(tmp, fadPos));
    }


   /** replace ()'s with []'s where they represent function argument delimiters */
    private String maskArgumentDelimiters(String s, int fadPos) {
        int closer = s.indexOf(')', fadPos);
        StringBuffer result = new StringBuffer(s) ;
        result.deleteCharAt(fadPos);
        result.insert(fadPos, '[');
        result.deleteCharAt(closer);
        result.insert(closer, ']');
        return result.toString() ;
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

       StringBuffer sb = new StringBuffer(s) ;
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
       StringBuffer b = new StringBuffer(s) ;
       int insPos = b.indexOf(cut) ;
       b.delete(insPos, insPos + cut.length()) ;
       b.insert(insPos, paste) ;
       return b.toString() ;
    }


//==========================================================================//    

    /**
     *  PARSING METHODS
     */


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
        Vector v = new Vector() ;
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
                   String msg = "Expression contains an invalid token at char "
                                 + ix;
                   throw new RdrConditionException(msg) ;
               }
               v.addElement(token) ;
               ix += token.length() ;
        }
        result = new String[v.size()] ;
        v.copyInto(result);

        // make sure tokens are ordered correctly
        if (ValidateTokenization(result))
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
       private String getVarName(String s, int start) {
        String result ;
        int tmp = start + 1 ;

        while ((tmp < s.length()) && isValidVarNameChar(s.charAt(tmp))) tmp++ ;
          result = s.substring(start, tmp) ;
        if (isFunctionName(result)) {                     // read arguments also
            while ((tmp < s.length()) && (s.charAt(tmp) != ']')) tmp++ ;
            tmp++ ;                                       // add one more for the ']'
        }

        return s.substring(start, tmp) ;
    }


       /** returns an operator from start position in expression */
       private String getOperator(String s, int start) {
           int tmp = start + 1 ;
        while ((tmp < s.length()) && isOperator(s.charAt(tmp))) tmp++ ;
          return s.substring(start, tmp) ;
       }


       /** @return true if alltokens are in a valid order (term op term ...) */
       private boolean ValidateTokenization(String[] a) {

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
    private String parseAndEvaluate(String s) throws RdrConditionException {

       String subExpr, ans ;
       String[] tokens ;
       int opIndex ;

       while (s.indexOf('(') > -1) {

          // special case if () are part of function call
          if (isFunctionArgumentDelimiter(s, s.indexOf('(') )) {
              s = maskArgumentDelimiters(s, s.indexOf('('));
          }
          else {
             // evaluate parenthesised sub-expressions first
             subExpr = extractSubExpr(s) ;                // get ( subexpr )
             ans = parseAndEvaluate(deQuote(subExpr)) ;   // recurse
             s = replaceStr(s, subExpr, ans) ;            // insert result
          }
       }

       // break expression tokens into a string array
       tokens = tokenize(s.trim()) ;            // ( ) any have been removed

        if (_log.isDebugEnabled())
           for (int i=0;i<tokens.length;i++)
              _log.debug("token " + i + " = " + tokens[i]) ;

       opIndex = findNextOperator(tokens) ;

       // while the expression has more operators, evaluate a part  
       while (opIndex > -1) {
          ans = evalExpression(tokens[opIndex-1], tokens[opIndex],
                               tokens[opIndex+1]) ;
          tokens = reduceTokens(tokens, opIndex, ans) ;

          if (_log.isDebugEnabled())
              for (int i=0;i<tokens.length;i++)
                  _log.debug("token " + i + " = " + tokens[i]) ;

             opIndex = findNextOperator(tokens) ;
       }

       // one token left - can be boolean string or single (boolean) function call
       if (isFunctionCall(tokens[0]))
          tokens[0] = evalFunction(tokens[0]);

       return tokens[0] ;                // 'true' or 'false' if all went well!
    }



   /** evaluates an expression and returns the result
    *  @param lOp - the left operand
    *  @param operator - as the name implies
    *  @param rOp - the right operand
    */
   private String evalExpression(String lOp, String operator, String rOp)
                                                     throws RdrConditionException {

       // if either op is a function call, replace it with its evaluation
       if (isFunctionCall(lOp)) lOp = getFunctionResult(lOp) ;
       if (isFunctionCall(rOp)) rOp = getFunctionResult(rOp) ;

       // if either op is a varname, replace it with its value
       if (!isLiteralValue(lOp)) lOp = getVarValue(lOp) ;
       if (!isLiteralValue(rOp)) rOp = getVarValue(rOp) ;

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
     * PRE: the function is a member of the ceFunctcions class
     * @param func the fucntion to call
     * @return the result of the function          *
     */
    private String evalFunction(String func) {
        String funcName, varName, varValue, result ;
        HashMap args = new HashMap() ;

        // strip out arguments & get their values
        String[] argList = parseArgsList(func);
        for (int i=0; i<argList.length;i++) {
            varName = argList[i].trim() ;
            varValue = getVarValue(varName);
            args.put(varName, varValue);
        }

        // extract function name
        funcName = func.substring(0, func.indexOf('['));

        // run function
        result = RdrConditionFunctions.execute(funcName, args) ;
        if (result == null) result = "null";
        return result ;
    }

   /** translates a bad result to 'undefined' */
    private String getFunctionResult(String func) {
        String result = evalFunction(func);
        if ((result == null) || (result.length() == 0))
           result = "undefined" ;
        return result ;
    }


    /** retrieves the value for a variable or function from the datalist Element */
    private String getVarValue(String var) {
       String result ;
       _log.debug("in getVarValue, var = " + var) ;

       // var "this" refers to the workitem associated with the task named in this rule
       if (var.equalsIgnoreCase("this"))
          result = getThisData() ;
       else
          result = _dataList.getChildText(var) ;

       if ((result == null) || (result.length() == 0))
             result = "undefined" ;
//   	      return formatVarValue(result) ;
       return result ;
    }

    /** extracts the names of arguments from a function call */
    private String[] parseArgsList(String list) {
        int start = list.indexOf('[') + 1;
        int end   = list.indexOf(']');
        String result = list.substring(start, end);      // remove [ ]
        return result.split(",");
    }

   /** get the value of the 'this' argument */
    private String getThisData() {
        String result = null;
        Element eThis = _dataList.getChild("process_info").getChild("itemRecord");
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


   /** Convert operands to nubmers and perform operation */
   private String doNumericOperation(String l, String op, String r)
                                                  throws RdrConditionException {
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
       if (op.compareTo("=") == 0)
          return String.valueOf(l.compareTo(r) == 0) ;
       if (op.compareTo("!=") == 0)
          return String.valueOf(l.compareTo(r) != 0) ;
       throw new RdrConditionException(getMessage(10)) ;  // error if gets here
   }



    /*********************************************************************/

    public static void main(String args[]) {
        // unit testing
        String s = "(22<25) = bval";
        Element e = new Element("testElement");
        e.setAttribute("nval", "17") ;
        e.setAttribute("sval", "\"apples\"") ;
        e.setAttribute("bval", "true") ;

        ConditionEvaluator t = new ConditionEvaluator();

        try {
//		    t.p(t.evalExpression("27", "!=", "26")) ;
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
    




