/*
 * Copyright  2000-2002,2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.replacement.junit;


import java.io.File;
import java.util.Enumeration;
import java.util.Vector;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;

/**
 * <p> Create then run <code>JUnitTest</code>'s based on the list of files
 *     given by the fileset attribute.
 *
 * <p> Every <code>.java</code> or <code>.class</code> file in the fileset is
 * assumed to be a testcase.
 * A <code>JUnitTest</code> is created for each of these named classes with
 * basic setup inherited from the parent <code>BatchTest</code>.
 *
 * @see JUnitTest
 */
public final class BatchTest extends BaseTest {

    /** the reference to the project */
    private Project project;

    /** the list of filesets containing the testcase filename rules */
    private Vector filesets = new Vector();

    /**
     * create a new batchtest instance
     * @param project     the project it depends on.
     */
    public BatchTest(Project project) {
        this.project = project;
    }

    /**
     * Add a new fileset instance to this batchtest. Whatever the fileset is,
     * only filename that are <tt>.java</tt> or <tt>.class</tt> will be
     * considered as 'candidates'.
     * @param     fs the new fileset containing the rules to get the testcases.
     */
    public void addFileSet(FileSet fs) {
        filesets.addElement(fs);
    }

    /**
     * Return all <tt>JUnitTest</tt> instances obtain by applying the fileset rules.
     * @return  an enumeration of all elements of this batchtest that are
     * a <tt>JUnitTest</tt> instance.
     */
    public final Enumeration elements() {
        JUnitTest[] tests = createAllJUnitTest();
        return Enumerations.fromArray(tests);
    }

    /**
     * Convenient method to merge the <tt>JUnitTest</tt>s of this batchtest
     * to a <tt>Vector</tt>.
     * @param v the vector to which should be added all individual tests of this
     * batch test.
     */
    final void addTestsTo(Vector v) {
        JUnitTest[] tests = createAllJUnitTest();
        v.ensureCapacity(v.size() + tests.length);
        for (int i = 0; i < tests.length; i++) {
            v.addElement(tests[i]);
        }
    }

    /**
     * Create all <tt>JUnitTest</tt>s based on the filesets. Each instance
     * is configured to match this instance properties.
     * @return the array of all <tt>JUnitTest</tt>s that belongs to this batch.
     */
    private JUnitTest[] createAllJUnitTest() {
        String[] filenames = getFilenames();
        JUnitTest[] tests = new JUnitTest[filenames.length];
        for (int i = 0; i < tests.length; i++) {
            String classname = javaToClass(filenames[i]);
            tests[i] = createJUnitTest(classname);
        }
        return tests;
    }

    /**
     * Iterate over all filesets and return the filename of all files
     * that end with <tt>.java</tt> or <tt>.class</tt>. This is to avoid
     * wrapping a <tt>JUnitTest</tt> over an xml file for example. A Testcase
     * is obviously a java file (compiled or not).
     * @return an array of filenames without their extension. As they should
     * normally be taken from their root, filenames should match their fully
     * qualified class name (If it is not the case it will fail when running the test).
     * For the class <tt>org/apache/Whatever.class</tt> it will return <tt>org/apache/Whatever</tt>.
     */
    private String[] getFilenames() {
        Vector v = new Vector();
        final int size = this.filesets.size();
        for (int j = 0; j < size; j++) {
            FileSet fs = (FileSet) filesets.elementAt(j);
            DirectoryScanner ds = fs.getDirectoryScanner(project);
            ds.scan();
            String[] f = ds.getIncludedFiles();
            for (int k = 0; k < f.length; k++) {
                String pathname = f[k];
                if (pathname.endsWith(".java")) {
                    v.addElement(pathname.substring(0, pathname.length() - ".java".length()));
                } else if (pathname.endsWith(".class")) {
                    v.addElement(pathname.substring(0, pathname.length() - ".class".length()));
                }
            }
        }

        String[] files = new String[v.size()];
        v.copyInto(files);
        return files;
    }

    /**
     * Convenient method to convert a pathname without extension to a
     * fully qualified classname. For example <tt>org/apache/Whatever</tt> will
     * be converted to <tt>org.apache.Whatever</tt>
     * @param filename the filename to "convert" to a classname.
     * @return the classname matching the filename.
     */
    public static final String javaToClass(String filename) {
        return filename.replace(File.separatorChar, '.');
    }

    /**
     * Create a <tt>JUnitTest</tt> that has the same property as this
     * <tt>BatchTest</tt> instance.
     * @param classname the name of the class that should be run as a
     * <tt>JUnitTest</tt>. It must be a fully qualified name.
     * @return the <tt>JUnitTest</tt> over the given classname.
     */
    private JUnitTest createJUnitTest(String classname) {
        JUnitTest test = new JUnitTest();
        test.setName(classname);
        test.setHaltonerror(this.haltOnError);
        test.setHaltonfailure(this.haltOnFail);
        test.setFiltertrace(this.filtertrace);
        test.setFork(this.fork);
        test.setIf(this.ifProperty);
        test.setUnless(this.unlessProperty);
        test.setTodir(this.destDir);
        test.setFailureProperty(failureProperty);
        test.setErrorProperty(errorProperty);
        Enumeration list = this.formatters.elements();
        while (list.hasMoreElements()) {
            test.addFormatter((FormatterElement) list.nextElement());
        }
        return test;
    }

}
