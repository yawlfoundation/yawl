package com.nexusbpm.editor;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * File chooser factory class. File choosers can be extremely slow
 * to initialize (Felix was having to wait 20-30 seconds for an
 * email sender editor to come up). We want to initialize file choosers
 * only when we have to, but we also want to keep the initialized
 * file choosers around so we only have to initialize them once.
 * Best to have a factory class using lazy initialization.
 * 
 * @author Daniel Gredler
 */
public abstract class FileChooser {

	private final static Log LOG = LogFactory.getLog( FileChooser.class );

	private static JFileChooser _chooser;

	/**
	 * Retrieves the single file chooser, initializing it first if necessary.
	 */
	private static JFileChooser getFileChooser() {
		if( _chooser == null ) {
			LOG.debug( "Initializing the file chooser." );
			_chooser = new JFileChooser();
		}
		return _chooser;
	}

	/**
	 * Retrieves the standard file chooser. The returned chooser must
	 * not be modified!
	 * @return the standard file chooser.
	 */
	public static JFileChooser getStandardChooser() {
		JFileChooser jfc = getFileChooser();
		return jfc;
	}//getStandardChooser()

	/**
	 * Retrieves the excel file chooser. The returned chooser must not
	 * be modified!
	 * @return the excel file chooser.
	 */
	public static JFileChooser getExcelFileChooser() {
		JFileChooser jfc = getFileChooser();
		jfc.setFileFilter( new XlsFileFilter() );
		return jfc;
	}//getExcelFileChooser()

	/**
	 * Retrieves the directory chooser. The returned chooser must not
	 * be modified!
	 * @return the directory chooser.
	 */
	public static JFileChooser getDirectoryChooser() {
		JFileChooser jfc = getFileChooser();
		jfc.setFileFilter( new DirectoryFileFilter() );
		return jfc;
	}//getDirectoryChooser()

	/**
	 * Retrieves the XML file chooser. The returned chooser must not
	 * be modified!
	 * @return the XML file chooser.
	 */
	public static JFileChooser getXmlChooser() {
		JFileChooser jfc = getFileChooser();
		jfc.setFileFilter( new XmlFileFilter() );
		return jfc;
	}//getXmlChooser()

	/**
	 * Retrieves the image file chooser. The returned chooser must not
	 * be modified!
	 * @return the image file chooser.
	 */
	public static JFileChooser getImageChooser() {
		JFileChooser jfc = getFileChooser();
		jfc.setFileFilter( new ImageFileFilter() );
		return jfc;
	}//getImageChooser()

	/**
	 * Used in conjuction with a JFileChooser to show only directories and
	 * Excel files.
	 */
	private static class XlsFileFilter extends FileFilter {
		/**
		 * Returns whether the given file is accepted by this filter.
		 * @param f the file to check
		 * @return whether the given file is accepted.
		 */
		public boolean accept(File f) {
			return (f.isDirectory() || f.getName().toLowerCase().endsWith(".xls"));
		}//accept()
		/**
		 * @return the description of this filter.
		 */
		public String getDescription() {
			return "Excel files";
		}//getDescription()
	}//XlsFileFilter

	/**
	 * Used in conjuction with a JFileChooser to show only directories.
	 */
	private static class DirectoryFileFilter extends FileFilter {
		/**
		 * Returns whether the given file is accepted by this filter.
		 * @param f the file to check
		 * @return whether the given file is accepted.
		 */
		public boolean accept(File f) {
			return f.isDirectory();
		}//accept()
		/**
		 * @return the description of this filter.
		 */
		public String getDescription() {
			return "Directories";
		}//getDescription()
	}//DirectoryFileFilter

	/**
	 * Used in conjuction with a JFileChooser to show only directories
	 * and XML files.
	 */
	private static class XmlFileFilter extends FileFilter {
		/**
		 * Returns whether the given file is accepted by this filter.
		 * @param f the file to check
		 * @return whether the given file is accepted.
		 */
		public boolean accept(File f) {
			return (f.isDirectory() || f.getName().toLowerCase().endsWith(".xml"));
		}//accept()
		/**
		 * @return the description of this filter.
		 */
		public String getDescription() {
			return "XML files";
		}//getDescription()
	}//XmlFileFilter

	/**
	 * Used in conjuction with a JFileChooser to show only directories
	 * and image files.
	 */
	private static class ImageFileFilter extends FileFilter {
		/**
		 * Returns whether the given file is accepted by this filter.
		 * @param f the file to check
		 * @return whether the given file is accepted.
		 */
		public boolean accept(File f) {
			String s = f.getName().toLowerCase();
			return (
				f.isDirectory() ||
				s.endsWith(".jpg") ||
				s.endsWith(".jpeg") ||
				s.endsWith(".gif") ||
				s.endsWith(".png") ||
				s.endsWith(".bmp") );
		}//accept()
		/**
		 * @return the description of this filter.
		 */
		public String getDescription() {
			return "Image files";
		}//getDescription()
	}//XmlFileFilter
}