/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.util;

import java.io.File;
import java.util.List;

import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.unmarshal.YMarshal;

/**
 * Extends {@link au.edu.qut.yawl.persistence.StringProducerXML StringProducerXML} to provide
 * a simple mechanism to read Specifications from a file.<br>
 * 
 * This class is used so that other classes can extend {@link junit.framework.TestCase} instead
 * of having to extend {@link au.edu.qut.yawl.persistence.StringProducerXML}.
 * 
 * @author Nathan Rose
 */
public class SpecReader {
	/**
	 * @return the specification contained in the file with the given name.
	 */
	public static YSpecification readSpecification( String fileName, boolean isXmlFileInPackage,
			Class classInsidePackage ) throws Exception {
		File specificationFile = getTranslatedFile( fileName, isXmlFileInPackage, classInsidePackage );
		List specifications = YMarshal.unmarshalSpecifications(specificationFile.getAbsolutePath());
		return (YSpecification) specifications.iterator().next();
    }

	public static File getTranslatedFile( String originalName, boolean isXmlFileInPackage,
			Class classInsidePackage ) {
		File file = null;
		if( isXmlFileInPackage ) {
			file = new File( classInsidePackage.getResource( originalName ).getFile() );
		}
		else {
			file = new File( originalName );
		}
		return file;
	}
}
