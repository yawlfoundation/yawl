/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.services.invoker;

import java.io.IOException;
import java.util.Properties;

import junit.framework.TestCase;

import com.nexusbpm.services.data.NexusServiceData;
import com.nexusbpm.services.data.Variable;

/**
 * Tests the Jython Preprocessor.
 * 
 * @author Nathan Rose
 */
public class TestJythonPreprocessor extends TestCase {
    private NexusServiceData data;
    private JythonPreprocessor processor;
    
    @Override
    protected void setUp() throws Exception {
        // clear everything on startup so we know one test isn't affected by previous runs
        data = null;
        processor = null;
    }
    
    @Override
    protected void tearDown() throws Exception {
        // clean everything up after we're done (not strictly necessary...)
        data = null;
        processor = null;
    }
    
    /**
     * Test that with a couple simple variables, where some variables should evaluate to
     * contain the value of other variables, the processor can evaluate those variables
     * properly.
     */
    public void testSimpleVariableEvaluation() {
        data = new NexusServiceData();
        
        data.setPlain( "foo", "foo-val" );
        data.setPlain( "bar", "bar now has foo's value:<<<foo>>>" );
        
        processor = new JythonPreprocessor( data );
        
        processor.evaluate();
        
        assertNotNull( data.getPlain( "foo" ) );
        assertTrue( data.getPlain( "foo" ), data.getPlain( "foo" ).equals( "foo-val" ) );
        
        assertNotNull( data.getPlain( "bar" ) );
        assertTrue( data.getPlain( "bar" ), data.getPlain( "bar" ).equals( "bar now has foo's value:foo-val" ) );
    }
    
    /**
     * Extending {@link #testSimpleVariableEvaluation()}, check that the processor can
     * restore the values of the variables properly.
     */
    public void testSimpleVariableRestoration() {
        // this test relies on the testSimpleVariableEvaluation test passing
        testSimpleVariableEvaluation();
        
        processor.restore();
        
        assertNotNull( data.getPlain( "foo" ) );
        assertTrue( data.getPlain( "foo" ), data.getPlain( "foo" ).equals( "foo-val" ) );
        
        assertNotNull( data.getPlain( "bar" ) );
        assertTrue( data.getPlain( "bar" ), data.getPlain( "bar" ).equals( "bar now has foo's value:<<<foo>>>" ) );
    }
    
    /**
     * Extends {@link #testSimpleVariableEvaluation()} to ensure that variables that have
     * been modified between when the processor evaluates them and when it restores variables
     * do not get reverted back to their original values.
     */
    public void testSimpleVariableNonRestoration() {
        // this test relies on the testSimpleVariableEvaluation test passing
        testSimpleVariableEvaluation();
        
        data.setPlain( "foo", "foo's new val" );
        data.setPlain( "bar", "bar's new val" );
        
        processor.restore();
        
        assertNotNull( data.getPlain( "foo" ) );
        assertTrue( data.getPlain( "foo" ), data.getPlain( "foo" ).equals( "foo's new val" ) );
        
        assertNotNull( data.getPlain( "bar" ) );
        assertTrue( data.getPlain( "bar" ), data.getPlain( "bar" ).equals( "bar's new val" ) );
    }
    
    /**
     * Test that the processor can handle having variables that have multiple parts
     * that need to be evaluated separately.
     * @throws Exception
     */
    public void testManyVariableEvaluation() throws Exception {
        data = new NexusServiceData();
        
        data.setPlain( "name", "Nathan" );
        data.setPlain( "lastname", "Rose" );
        data.setBase64( "password", "mypassword" );
        data.setPlain( "foo", "foo-val" );
        data.setPlain( "address", "1234 Some Street" );
        data.setPlain( "city", "Winchestertonfieldville" );
        data.setPlain( "state", "Iowa" );
        data.setPlain( "phonenumber", "123-123-1234" );
        data.setPlain( "fullname", "<<<name>>> <<<lastname>>>" );
        data.setPlain( "fulladdress", "<<<address>>>\n<<<city>>> <<<state>>>" );
        data.setPlain( "numericEval", "<<<2 + 4 + 6>>>" );
        
        processor = new JythonPreprocessor( data );
        
        processor.evaluate();
        
        assertNotNull( data.getPlain( "name" ) );
        assertTrue( data.getPlain( "name" ), data.getPlain( "name" ).equals( "Nathan" ) );
        
        assertNotNull( data.getPlain( "lastname" ) );
        assertTrue( data.getPlain( "lastname" ), data.getPlain( "lastname" ).equals( "Rose" ) );
        
        assertNotNull( data.get( "password" ) );
        assertTrue( data.getType( "password" ), data.getType( "password" ).equals( Variable.TYPE_BASE64 ) );
        assertTrue( data.get( "password" ).toString(), data.get( "password" ).equals( "mypassword" ) );
        
        assertNotNull( data.getPlain( "foo" ) );
        assertTrue( data.getPlain( "foo" ), data.getPlain( "foo" ).equals( "foo-val" ) );
        
        assertNotNull( data.getPlain( "address" ) );
        assertTrue( data.getPlain( "address" ), data.getPlain( "address" ).equals( "1234 Some Street" ) );
        
        assertNotNull( data.getPlain( "city" ) );
        assertTrue( data.getPlain( "city" ), data.getPlain( "city" ).equals( "Winchestertonfieldville" ) );
        
        assertNotNull( data.getPlain( "state" ) );
        assertTrue( data.getPlain( "state" ), data.getPlain( "state" ).equals( "Iowa" ) );
        
        assertNotNull( data.getPlain( "phonenumber" ) );
        assertTrue( data.getPlain( "phonenumber" ), data.getPlain( "phonenumber" ).equals( "123-123-1234" ) );
        
        assertNotNull( data.getPlain( "fullname" ) );
        assertTrue( data.getPlain( "fullname" ), data.getPlain( "fullname" ).equals( "Nathan Rose" ) );
        
        assertNotNull( data.getPlain( "fulladdress" ) );
        assertTrue( data.getPlain( "fulladdress" ), data.getPlain( "fulladdress" )
                .equals( "1234 Some Street\nWinchestertonfieldville Iowa" ) );
        
        assertNotNull( data.getPlain( "numericEval" ) );
        assertTrue( data.getPlain( "numericEval" ), data.getPlain( "numericEval" ).equals( "12" ) );
    }
    
    /**
     * Extending {@link #testManyVariableEvaluation()}, check that the processor can
     * restore the values of the variables properly.
     * @throws Exception
     */
    public void testManyVariableRestoration() throws Exception {
        // this test relies on the testManyVariableEvaluation test passing
        testManyVariableEvaluation();
        
        processor.restore();
        
        assertNotNull( data.getPlain( "name" ) );
        assertTrue( data.getPlain( "name" ), data.getPlain( "name" ).equals( "Nathan" ) );
        
        assertNotNull( data.getPlain( "lastname" ) );
        assertTrue( data.getPlain( "lastname" ), data.getPlain( "lastname" ).equals( "Rose" ) );
        
        assertNotNull( data.get( "password" ) );
        assertTrue( data.getType( "password" ), data.getType( "password" ).equals( Variable.TYPE_BASE64 ) );
        assertTrue( data.get( "password" ).toString(), data.get( "password" ).equals( "mypassword" ) );
        
        assertNotNull( data.getPlain( "foo" ) );
        assertTrue( data.getPlain( "foo" ), data.getPlain( "foo" ).equals( "foo-val" ) );
        
        assertNotNull( data.getPlain( "address" ) );
        assertTrue( data.getPlain( "address" ), data.getPlain( "address" ).equals( "1234 Some Street" ) );
        
        assertNotNull( data.getPlain( "city" ) );
        assertTrue( data.getPlain( "city" ), data.getPlain( "city" ).equals( "Winchestertonfieldville" ) );
        
        assertNotNull( data.getPlain( "state" ) );
        assertTrue( data.getPlain( "state" ), data.getPlain( "state" ).equals( "Iowa" ) );
        
        assertNotNull( data.getPlain( "phonenumber" ) );
        assertTrue( data.getPlain( "phonenumber" ), data.getPlain( "phonenumber" ).equals( "123-123-1234" ) );
        
        assertNotNull( data.getPlain( "fullname" ) );
        assertTrue( data.getPlain( "fullname" ), data.getPlain( "fullname" ).equals( "<<<name>>> <<<lastname>>>" ) );
        
        assertNotNull( data.getPlain( "fulladdress" ) );
        assertTrue( data.getPlain( "fulladdress" ), data.getPlain( "fulladdress" )
                .equals( "<<<address>>>\n<<<city>>> <<<state>>>" ) );
        
        assertNotNull( data.getPlain( "numericEval" ) );
        assertTrue( data.getPlain( "numericEval" ), data.getPlain( "numericEval" ).equals( "<<<2 + 4 + 6>>>" ) );
    }
    
    /**
     * Extends {@link #testManyVariableEvaluation()} to ensure that when some variables
     * have their values changed after the processor has processed the variables will
     * cause the changed variables to not get restored, and that the variables that do not
     * get changed after the processor evaluates them DO get restored.
     * @throws Exception
     */
    public void testManyVariableNonRestoration() throws Exception {
        // this test relies on the testManyVariableEvaluation test passing
        testManyVariableEvaluation();
        
        data.setPlain( "address", "new address" );
        data.setPlain( "fullname", null );
        data.setPlain( "numericEval", "non numeric val" );
        
        processor.restore();
        
        assertNotNull( data.getPlain( "name" ) );
        assertTrue( data.getPlain( "name" ), data.getPlain( "name" ).equals( "Nathan" ) );
        
        assertNotNull( data.getPlain( "lastname" ) );
        assertTrue( data.getPlain( "lastname" ), data.getPlain( "lastname" ).equals( "Rose" ) );
        
        assertNotNull( data.get( "password" ) );
        assertTrue( data.getType( "password" ), data.getType( "password" ).equals( Variable.TYPE_BASE64 ) );
        assertTrue( data.get( "password" ).toString(), data.get( "password" ).equals( "mypassword" ) );
        
        assertNotNull( data.getPlain( "foo" ) );
        assertTrue( data.getPlain( "foo" ), data.getPlain( "foo" ).equals( "foo-val" ) );
        
        assertNotNull( data.getPlain( "address" ) );
        assertTrue( data.getPlain( "address" ), data.getPlain( "address" ).equals( "new address" ) );
        
        assertNotNull( data.getPlain( "city" ) );
        assertTrue( data.getPlain( "city" ), data.getPlain( "city" ).equals( "Winchestertonfieldville" ) );
        
        assertNotNull( data.getPlain( "state" ) );
        assertTrue( data.getPlain( "state" ), data.getPlain( "state" ).equals( "Iowa" ) );
        
        assertNotNull( data.getPlain( "phonenumber" ) );
        assertTrue( data.getPlain( "phonenumber" ), data.getPlain( "phonenumber" ).equals( "123-123-1234" ) );
        
        assertNull( data.getPlain( "fullname" ), data.getPlain( "fullname" ) );
        
        // full address wasn't changed, so it should get restored
        assertNotNull( data.getPlain( "fulladdress" ) );
        assertTrue( data.getPlain( "fulladdress" ), data.getPlain( "fulladdress" )
                .equals( "<<<address>>>\n<<<city>>> <<<state>>>" ) );
        
        assertNotNull( data.getPlain( "numericEval" ) );
        assertTrue( data.getPlain( "numericEval" ), data.getPlain( "numericEval" ).equals( "non numeric val" ) );
    }
    
    /**
     * Tests that the processor can handle evaluating variables that evaluate to
     * values that need to be re-evaluated.
     */
    public void testTwoPassVariableEvaluation() {
        data = new NexusServiceData();
        
        data.setPlain( "foo", "foo-val" );
        data.setPlain( "bar", "bar now has foo's value:<<<baz>>>" );
        data.setPlain( "baz", "<<<foo>>>" );
        
        processor = new JythonPreprocessor( data );
        
        processor.evaluate();
        
        assertNotNull( data.getPlain( "foo" ) );
        assertTrue( data.getPlain( "foo" ), data.getPlain( "foo" ).equals( "foo-val" ) );
        
        assertNotNull( data.getPlain( "bar" ) );
        assertTrue( data.getPlain( "bar" ), data.getPlain( "bar" ).equals( "bar now has foo's value:foo-val" ) );
        
        assertNotNull( data.getPlain( "baz" ) );
        assertTrue( data.getPlain( "baz" ), data.getPlain( "baz" ).equals( "foo-val" ) );
    }
    
    /**
     * Extends {@link #testTwoLevelVariableEvaluation()} to ensure that the processor
     * can restore variables that needed to be evaluated in two passes.
     */
    public void testTwoPassVariableRestoration() {
        // this test relies on the testSimpleVariableEvaluation test passing
        testTwoPassVariableEvaluation();
        
        processor.restore();
        
        assertNotNull( data.getPlain( "foo" ) );
        assertTrue( data.getPlain( "foo" ), data.getPlain( "foo" ).equals( "foo-val" ) );
        
        assertNotNull( data.getPlain( "bar" ) );
        assertTrue( data.getPlain( "bar" ), data.getPlain( "bar" ).equals( "bar now has foo's value:<<<baz>>>" ) );
        
        assertNotNull( data.getPlain( "baz" ) );
        assertTrue( data.getPlain( "baz" ), data.getPlain( "baz" ).equals( "<<<foo>>>" ) );
    }
    
    /**
     * Extends {@link #testTwoPassVariableEvaluation()} to ensure that variables
     * that get evaluated in multiple passes and then are modified after the processor
     * evaluates them don't get restored.
     */
    public void testTwoPassVariableNonRestoration() {
        // this test relies on the testSimpleVariableEvaluation test passing
        testTwoPassVariableEvaluation();
        
        data.setPlain( "foo", "foo's new val" );
        data.setPlain( "bar", "bar now has foo's value:<<<foo>>>" );
        
        processor.restore();
        
        assertNotNull( data.getPlain( "foo" ) );
        assertTrue( data.getPlain( "foo" ), data.getPlain( "foo" ).equals( "foo's new val" ) );
        
        assertNotNull( data.getPlain( "bar" ) );
        assertTrue( data.getPlain( "bar" ), data.getPlain( "bar" ).equals( "bar now has foo's value:<<<foo>>>" ) );
        
        // baz didn't get modified, so it should be restored
        assertNotNull( data.getPlain( "baz" ) );
        assertTrue( data.getPlain( "baz" ), data.getPlain( "baz" ).equals( "<<<foo>>>" ) );
    }
    
    public void testManyPassVariableEvaluation() {
        data = new NexusServiceData();
        
        data.setPlain( "foo", "foo-val" );
        data.setPlain( "bar", "bar now has foo's value:<<<baz>>>" );
        data.setPlain( "baz", "<<<foo>>>" );
        data.setPlain( "a", "<<<b>>>" );
        data.setPlain( "b", "<<<c>>>" );
        data.setPlain( "c", "<<<d>>>" );
        data.setPlain( "d", "<<<e>>>" );
        data.setPlain( "e", "<<<f>>>" );
        data.setPlain( "f", "<<<g>>>" );
        data.setPlain( "g", "<<<h>>>" );
        data.setPlain( "h", "<<<i>>>" );
        data.setPlain( "i", "<<<j>>>" );
        data.setPlain( "j", "<<<k>>>" );
        data.setPlain( "k", "<<<l>>>" );
        data.setPlain( "l", "<<<m>>>" );
        data.setPlain( "m", "<<<n>>>" );
        data.setPlain( "n", "<<<o>>>" );
        data.setPlain( "o", "<<<p>>>" );
        data.setPlain( "p", "<<<q>>>" );
        data.setPlain( "q", "<<<r>>>" );
        data.setPlain( "r", "<<<s>>>" );
        data.setPlain( "s", "<<<t>>>" );
        data.setPlain( "t", "<<<u>>>" );
        data.setPlain( "u", "<<<v>>>" );
        data.setPlain( "v", "<<<w>>>" );
        data.setPlain( "w", "<<<x>>>" );
        data.setPlain( "x", "<<<y>>>" );
        data.setPlain( "y", "<<<z>>>" );
        data.setPlain( "z", "<<<baz>>>" );
        
        processor = new JythonPreprocessor( data );
        
        processor.evaluate();
        
        assertNotNull( data.getPlain( "foo" ) );
        assertTrue( data.getPlain( "foo" ), data.getPlain( "foo" ).equals( "foo-val" ) );
        
        assertNotNull( data.getPlain( "bar" ) );
        assertTrue( data.getPlain( "bar" ), data.getPlain( "bar" ).equals( "bar now has foo's value:foo-val" ) );
        
        assertNotNull( data.getPlain( "baz" ) );
        assertTrue( data.getPlain( "baz" ), data.getPlain( "baz" ).equals( "foo-val" ) );
        
        for( int c = 0; c < 26; c++ ) {
            String s = new String( new char[] {(char)('a' + c)} );
            assertNotNull( s, data.getPlain( s ) );
            assertTrue( s + data.getPlain( s ), data.getPlain( s ).equals( "foo-val" ) );
        }
    }
    
    public void testManyPassVariableRestoration() {
        // this test relies on the testSimpleVariableEvaluation test passing
        testManyPassVariableEvaluation();
        
        processor.restore();
        
        assertNotNull( data.getPlain( "foo" ) );
        assertTrue( data.getPlain( "foo" ), data.getPlain( "foo" ).equals( "foo-val" ) );
        
        assertNotNull( data.getPlain( "bar" ) );
        assertTrue( data.getPlain( "bar" ), data.getPlain( "bar" ).equals( "bar now has foo's value:<<<baz>>>" ) );
        
        assertNotNull( data.getPlain( "baz" ) );
        assertTrue( data.getPlain( "baz" ), data.getPlain( "baz" ).equals( "<<<foo>>>" ) );
        
        for( int c = 0; c < 25; c++ ) {
            String s = new String( new char[] {(char)('a' + c)} );
            String v = new String( new char[] {(char)('a' + c + 1)} );
            assertNotNull( s, data.getPlain( s ) );
            assertTrue( s + data.getPlain( s ), data.getPlain( s ).equals( "<<<" + v + ">>>" ) );
        }
        
        assertNotNull( data.getPlain( "z" ) );
        assertTrue( data.getPlain( "z"), data.getPlain( "z" ).equals( "<<<baz>>>" ) );
    }
    
    public void testManyPassVariableNonRestoration() {
        // this test relies on the testSimpleVariableEvaluation test passing
        testManyPassVariableEvaluation();
        
        data.setPlain( "a", "aval" );
        data.setPlain( "z", "zval" );
        data.setPlain( "baz", "bazval" );
        
        processor.restore();
        
        assertNotNull( data.getPlain( "foo" ) );
        assertTrue( data.getPlain( "foo" ), data.getPlain( "foo" ).equals( "foo-val" ) );
        
        assertNotNull( data.getPlain( "bar" ) );
        assertTrue( data.getPlain( "bar" ), data.getPlain( "bar" ).equals( "bar now has foo's value:<<<baz>>>" ) );
        
        assertNotNull( data.getPlain( "baz" ) );
        assertTrue( data.getPlain( "baz" ), data.getPlain( "baz" ).equals( "bazval" ) );
        
        for( int c = 1; c < 25; c++ ) {
            String s = new String( new char[] {(char)('a' + c)} );
            String v = new String( new char[] {(char)('a' + c + 1)} );
            assertNotNull( s, data.getPlain( s ) );
            assertTrue( s + data.getPlain( s ), data.getPlain( s ).equals( "<<<" + v + ">>>" ) );
        }
        
        assertNotNull( data.getPlain( "a" ) );
        assertTrue( data.getPlain( "a"), data.getPlain( "a" ).equals( "aval" ) );
        
        assertNotNull( data.getPlain( "z" ) );
        assertTrue( data.getPlain( "z"), data.getPlain( "z" ).equals( "zval" ) );
    }
    
    /**
     * Tests a set of variables whose values would cause the Python preprocessor to
     * go into an infinite loop of evaluation (where the result of evaluating the
     * section of jython code will always produce a result that itself will require
     * being evaluated) to make sure the preprocessor can handle that kind of
     * situation gracefully.
     */
    public void testInfinitelyRecursiveVariableEvaluation() {
        data = new NexusServiceData();
        
        data.setPlain( "foo", "<<<foo>>><<<foo>>>" );
        data.setPlain( "bar", "<<<baz>>>" );
        data.setPlain( "baz", "<<<'<' + '<<baz>>' + '>'>>>" );
        data.setPlain( "var1", "<<<var2>>>" );
        data.setPlain( "var2", "<<<var1>>>" );
        
        processor = new JythonPreprocessor( data );
        
        processor.evaluate();
        
        assertNotNull( data.getPlain( "foo" ) );
        assertTrue( data.getPlain( "foo" ), data.getPlain( "foo" ).equals( "<<<foo>>><<<foo>>>" ) );
        
        assertNotNull( data.getPlain( "bar" ) );
        assertTrue( data.getPlain( "bar" ), data.getPlain( "bar" ).equals( "<<<baz>>>" ) );
        
        assertNotNull( data.getPlain( "baz" ) );
        assertTrue( data.getPlain( "baz" ), data.getPlain( "baz" ).equals( "<<<baz>>>" ) );
        
        assertNotNull( data.getPlain( "var1" ) );
        assertTrue( data.getPlain( "var1" ), data.getPlain( "var1" ).equals( "<<<var2>>>" ) );
        
        assertNotNull( data.getPlain( "var2" ) );
        assertTrue( data.getPlain( "var2" ), data.getPlain( "var2" ).equals( "<<<var1>>>" ) );
    }
    
    public void testInfinitelyRecursiveVariableRestoration() {
        // this test relies on the testInfinitelyRecursiveVariableEvaluation test passing
        testInfinitelyRecursiveVariableEvaluation();
        
        processor.restore();
        
        assertNotNull( data.getPlain( "foo" ) );
        assertTrue( data.getPlain( "foo" ), data.getPlain( "foo" ).equals( "<<<foo>>><<<foo>>>" ) );
        
        assertNotNull( data.getPlain( "bar" ) );
        assertTrue( data.getPlain( "bar" ), data.getPlain( "bar" ).equals( "<<<baz>>>" ) );
        
        assertNotNull( data.getPlain( "baz" ) );
        assertTrue( data.getPlain( "baz" ), data.getPlain( "baz" ).equals( "<<<'<' + '<<baz>>' + '>'>>>" ) );
        
        assertNotNull( data.getPlain( "var1" ) );
        assertTrue( data.getPlain( "var1" ), data.getPlain( "var1" ).equals( "<<<var2>>>" ) );
        
        assertNotNull( data.getPlain( "var2" ) );
        assertTrue( data.getPlain( "var2" ), data.getPlain( "var2" ).equals( "<<<var1>>>" ) );
    }
    
    public void testInfinitelyRecursiveVariableNonRestoration() {
        // this test relies on the testInfinitelyRecursiveVariableEvaluation test passing
        testInfinitelyRecursiveVariableEvaluation();
        
        data.setPlain( "foo", "foo-val" );
        data.setPlain( "baz", "baz-val" );
        data.setPlain( "var2", "v2val" );
        
        processor.restore();
        
        assertNotNull( data.getPlain( "foo" ) );
        assertTrue( data.getPlain( "foo" ), data.getPlain( "foo" ).equals( "foo-val" ) );
        
        assertNotNull( data.getPlain( "bar" ) );
        assertTrue( data.getPlain( "bar" ), data.getPlain( "bar" ).equals( "<<<baz>>>" ) );
        
        assertNotNull( data.getPlain( "baz" ) );
        assertTrue( data.getPlain( "baz" ), data.getPlain( "baz" ).equals( "baz-val" ) );
        
        assertNotNull( data.getPlain( "var1" ) );
        assertTrue( data.getPlain( "var1" ), data.getPlain( "var1" ).equals( "<<<var2>>>" ) );
        
        assertNotNull( data.getPlain( "var2" ) );
        assertTrue( data.getPlain( "var2" ), data.getPlain( "var2" ).equals( "v2val" ) );
    }
    
    /**
     * Tests having more complex jython code that uses java objects and not just
     * the name of another variable.
     */
    public void testObjectVariableEvaluation() throws IOException, ClassNotFoundException {
        data = new NexusServiceData();
        
        data.setPlain( "foo", "<<<var.getProperty(\"p1\") + \":\" + var.getProperty(\"p2\")>>>" );
        Properties p = new Properties();
        p.setProperty( "p1", "v1" );
        p.setProperty( "p2", "v2" );
        data.setObject( "var", p );
        
        processor = new JythonPreprocessor( data );
        
        processor.evaluate();
        
        assertNotNull( data.getPlain( "foo" ) );
        assertTrue( data.getPlain( "foo" ), data.getPlain( "foo" ).equals( "v1:v2" ) );
        
        assertNotNull( data.getType( "var" ) );
        assertTrue( data.getType( "var" ), data.getType( "var" ).equals( Variable.TYPE_OBJECT ) );
        
        assertNotNull( data.get( "var" ) );
        assertTrue( data.get( "var" ).getClass().toString(), data.get( "var" ) instanceof Properties );
    }
    
    public void testObjectVariableRestoration() throws IOException, ClassNotFoundException {
        testObjectVariableEvaluation();
        
        processor.restore();
        
        assertNotNull( data.getPlain( "foo" ) );
        assertTrue( data.getPlain( "foo" ), data.getPlain( "foo" ).equals(
                "<<<var.getProperty(\"p1\") + \":\" + var.getProperty(\"p2\")>>>" ) );
        
        assertNotNull( data.getType( "var" ) );
        assertTrue( data.getType( "var" ), data.getType( "var" ).equals( Variable.TYPE_OBJECT ) );
        
        assertNotNull( data.get( "var" ) );
        assertTrue( data.get( "var" ).getClass().toString(), data.get( "var" ) instanceof Properties );
    }
}
