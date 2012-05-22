package org.yawlfoundation.yawl.editor.swing.data;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Token;
import net.sf.saxon.query.QueryModule;
import net.sf.saxon.query.QueryParser;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.trans.XPathException;

/**
 * @author Michael Adams
 * @date 8/05/12
 */
class SimpleXQueryParser extends QueryParser {
  private static final StaticQueryContext context =
     new StaticQueryContext(new Configuration());

  public Expression parse(String query) throws XPathException {
    return super.parse(query, 0, Token.EOF, -1, new QueryModule(context));
  }

  public Expression parseForExpression(String query) throws XPathException {
    return super.parseForExpression();
  }
}
