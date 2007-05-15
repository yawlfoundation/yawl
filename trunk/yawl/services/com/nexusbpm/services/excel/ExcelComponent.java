package com.nexusbpm.services.excel;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.nexusbpm.services.data.NexusServiceData;

/**
 * The Excel component reads data from a SQL query and then inserts part or all
 * of that data into a specified Excel file.
 *
 *
 * <b>Use case:</b><br>
 * <p>ExcelComponent will read from a SqlQueryAttribute and put the data
 * retrieved from the SQL query into an excel document sheet. The anchor and
 * sheet name can be specified. The truncated number of rows and columns may
 * also be specified. If the number of truncated rows is greater than the actual
 * number of rows retrieved from the query, we will store all the rows into the
 * excel document. The user can specify if we should use Apache-POI or ExtenXLS
 * to write the document with. Apache-POI is faster and less memory intensive
 * than ExtenXLS, but cannot handle as many types of documents as ExtenXLS.
 *
 * @author Austin Chau
 * @author Daniel Gredler
 * @created May 27, 2003
 * @hibernate.subclass discriminator-value="33"
 * @javabean.class name="ExcelComponent"
 * displayName="Excel Component"
 */
public class ExcelComponent {

	private static final long serialVersionUID = -6869852020440266381L;
	private static final Log LOG = LogFactory.getLog( ExcelComponent.class );

	public final static Integer OUTPUT_TYPE_UNKNOWN = new Integer( 0 );
	public final static Integer OUTPUT_TYPE_POI = new Integer( 1 );
	public final static Integer OUTPUT_TYPE_CSV = new Integer( 2 );
	public final static Integer OUTPUT_TYPE_ACCESS = new Integer( 3 );
	
	public static final String POI_TYPE = "POI";
	public static final String CSV_TYPE = "CSV";
	public static final String ACCESS_TYPE = "Access";

	protected final static String[] OUTPUT_TYPES = {"Unknown", POI_TYPE, CSV_TYPE, ACCESS_TYPE};

	/**
	 * Default constructor. Required by Hibernate.
	 */
	public ExcelComponent() {
		super();
	}//ExcelComponent()

	public ExcelComponent(NexusServiceData data) {
		super();
		setColLimit(new Integer(data.getPlain("columnLimit")));
		setRowLimit(new Integer(data.getPlain("rowLimit")));
		setExcelAnchor(data.getPlain("anchor"));
		setExcelOutputType(new Integer(data.getPlain("outputType")));
		setSheetName(data.getPlain("sheetName"));
		setTemplateName(data.getPlain("template"));
//		setTableData(null);
	}
	
	
//	/**
//	 * Creates a new <code>ExcelComponent</code> with mandatory attributes.
//	 *
//	 * @param name   the <code>ExcelComponent</code>'s name
//	 * @param folder the <code>ExcelComponent</code>'s parent folder
//	 */
//	public ExcelComponent( String name, FolderComponent folder ) {
//		super( name, folder );
//	}//ExcelComponent()
//
//	/**
//	 * @see Component#construct()
//	 */
//	protected void construct() {
//		super.construct();
//		setTypeID( new Long( ComponentType.EXCEL ) );
//	}//construct()

//	/**
//	 * @see Component#clientValidate(Collection)
//	 */
//	public Collection<ValidationMessage> clientValidate( Collection<ValidationMessage> validationErrors ) {
//		validationErrors = super.clientValidate( validationErrors );
//		// The Excel component should should have a reference to a template Excel file.
//		if( getExcelFile() == null ) {
//			String name = this.getName();
//			String msg = "The Excel component '" + name + "' is missing a template Excel file.";
//			validationErrors.add( new ValidationMessage( msg, this, ValidationMessage.WARNING ) );
//		}//if
//		return validationErrors;
//	}//clientValidate()

	public ExcelObject buildExcelObject( String excelFile ) throws Exception {
		ExcelObject excelObject = null;
		Integer excelOutputType = getExcelOutputType();
		int excelOutputTypeInt;
		if( null == excelOutputType ) {
			excelOutputType = OUTPUT_TYPE_CSV;
		}
		excelOutputTypeInt = excelOutputType.intValue();

		LOG.info( "buildExcelObject t=" + excelOutputTypeInt + ";f=" + excelFile
				+ ";sheet=" + getSheetName() + ";templateName=" + getTemplateName());		

		if( excelOutputTypeInt == OUTPUT_TYPE_POI.intValue() ) {
			excelObject = new ExcelPOIObject( excelFile, getSheetName(), getTemplateName() );
		}
		else if( excelOutputTypeInt == OUTPUT_TYPE_CSV.intValue() ) {
			excelObject = new CsvObject( excelFile, getSheetName(), getTemplateName() );
		}
//		else if( excelOutputTypeInt == OUTPUT_TYPE_ACCESS.intValue() ) {
//			excelObject = new JackcessObject( excelFile, getSheetName(), getTemplateName() );
//		}

		return excelObject;
	}

	/**
	 * @see com.nexusbpm.services.component.Component#run()
	 */
	public void run() throws Exception {

//		SqlTable table = null;
//
//		try {
//
//			// The Excel component must have a SQL query from which to draw data.
//			if( getTableData() == null ) { throw new Exception( "There is no SQL query from which to draw data." ); }
//
//			// The Excel component must have a template Excel file to put the data in.
//			if( getExcelFile() == null ) { throw new Exception( "There is no Excel file to insert data into." ); }
//
//			// Make sure we are operating on a local file first. If not, we retrieve the
//			// file from the FTP server and put it in our operating directory.
//			getExcelFile().makeSyntheticFileLocal();
//			String excelFile = getExcelFile().getSyntheticAbsolutePath();
//			LOG.info( "Processing excel file: " + getExcelFile().toString()
//					+ " - sheet=" + getSheetName()
//					+ ";outputType=" + getOutputType()
//					+ ";anchor=" + getExcelAnchor()
//					+ ";rowLimit=" + getRowLimit()
//					+ ";colLimit=" + getColLimit()
//					+ ";template=" + getTemplateName());
//
//			// Initialize an object with a common interface, regardless of whether we
//			// are using Extentech's ExtenXLS library or Apache's POI library.
//			ExcelObject excelObject = buildExcelObject( excelFile );
//
//			// Pull the data using the SQL query.
//			table = new SqlTable( this.getTableData() );
//			int numRows = table.getRowCount();
//			int myRowLimit = _rowLimit;
//			if( numRows < _rowLimit ) {
//				myRowLimit = numRows;
//			}
//
//			// Insert the data into the Excel file and save it.
//			LOG.info( "Adding " + myRowLimit + " rows into spreadsheet - " + "anchor=" + getExcelAnchor()
//					+ ";sheet=" + getSheetName() + ";totalRows=" + numRows + ";rowLimit=" + getRowLimit()
//					+ ";colLimit=" + getColLimit() + ";outputType=" + getExcelOutputType() + ";file=" + excelFile );
//			excelObject.insertTableAtAnchor( table, _anchor, myRowLimit, _colLimit );
//			table.clear();
//			table = null;
//			excelObject.save();
//			LOG.info( "Finished adding " + myRowLimit + " rows into spreadsheet - " + "anchor=" + getExcelAnchor()
//					+ ";sheet=" + getSheetName() + ";rowLimit=" + getRowLimit() + ";colLimit=" + getColLimit()
//					+ ";outputType=" + getExcelOutputType() + ";file=" + excelFile );
//
//			// If we aren't on the FTP server, then we want to move our local file
//			// back to the FTP server so that others can access the new changes.
//			getExcelFile().makeSyntheticFileRemote();
//			getExcelFile().deleteSyntheticLocalFile();
//
//		}
//		catch( Exception e ) {
//
//			// Don't wrap Exceptions in Exceptions.
//			throw e;
//
//		}
//		finally {
//			if( null != table ) table.clear();
//		}

	} //run()


	private Integer _excelOutputType;

	/**
	 * Returns the type of format/method to use for exporting from excel component.
	 * 1=ExtenXLS
	 * 2=POI
	 * 3=CSV
	 * 4=Microsoft Access
	 *
	 * @return excel export type
	 * @hibernate.property column="EXCEL_OUTPUT_TYPE"
	 * @javabean.property displayName="ExcelOutputType"
	 * hidden="true"
	 */
	public Integer getExcelOutputType() {
		return _excelOutputType;
	}

	public void setExcelOutputType( Integer excelOutputType ) {
		_excelOutputType = excelOutputType;

	}

	public String getOutputType() {
		Integer excelOutputType = getExcelOutputType();
		int excelOutputTypeInt;
		if( null == excelOutputType ) {
			excelOutputType = OUTPUT_TYPE_UNKNOWN;
		}
		excelOutputTypeInt = excelOutputType.intValue();

		if( excelOutputTypeInt > OUTPUT_TYPES.length - 1 ) {
			excelOutputTypeInt = 0;
		}

		String outputType = OUTPUT_TYPES[ excelOutputTypeInt ];
		LOG.info( "getOutputType i=" + excelOutputTypeInt + ";s=" + outputType );

		return outputType;
	}

	public void setOutputType( String outputType ) {
		int outputTypeIndex = 0;
		for( int ii = 0; ii < OUTPUT_TYPES.length; ii++ ) {
			if( OUTPUT_TYPES[ ii ].equals( outputType ) ) {
				outputTypeIndex = ii;
				break;
			}
		}

		LOG.info( "setOutputType i=" + outputTypeIndex + ";s=" + outputType );

		setExcelOutputType( new Integer( outputTypeIndex ) );
	}


	static private final String ATTR_SQL_QUERY = "tableData";

	/**
	 * Retrieves the SQL query.
	 *
	 * @return the tableData value
	 */
//	public SqlQueryAttribute getTableData() {
//		return (SqlQueryAttribute) getComponentAttribute( ATTR_SQL_QUERY );
//	}//getTableData()

	/**
	 * Sets the SQL query.
	 *
	 * @param tableData the SQL query.
	 * @throws Exception shouldn't ever be thrown.
	 */
//	public void setTableData( SqlQueryAttribute tableData ) throws Exception {
//		SqlQueryAttribute sqa = addSqlQueryAttribute( ATTR_SQL_QUERY, tableData );
//	}//setTableData()

	private int _colLimit;

	/**
	 * Returns the column limit.
	 *
	 * @return the column limit
	 * @hibernate.property column="EXCEL_COLUMN_LIMIT"
	 * @javabean.property displayName="Column Limit"
	 * hidden="false"
	 */
	public int getColLimit() {
		return _colLimit;
	}//getColLimit()

	/**
	 * Sets the column limit.
	 *
	 * @param colLimit the column limit.
	 */
	public void setColLimit( int colLimit ) {
		_colLimit = colLimit;
	}//setColLimit()

	private int _rowLimit;

	/**
	 * Returns the row limit.
	 *
	 * @return the row limit
	 * @hibernate.property column="EXCEL_ROW_LIMIT"
	 * @javabean.property displayName="Row Limit"
	 * hidden="false"
	 */
	public int getRowLimit() {
		return _rowLimit;
	}//getRowLimit()

	/**
	 * Sets the row limit.
	 *
	 * @param rowLimit the row limit.
	 */
	public void setRowLimit( int rowLimit ) {
		_rowLimit = rowLimit;
	}//setRowLimit()

	private final static String ATTRIBUTE_FILE = "excel_file_attribute";

	/**
	 * Retrieves the excel file.
	 *
	 * @return the excel component's excel file
	 * @javabean.property displayName="Excel file"
	 * hidden="false"
	 */
//	public FileAttribute getExcelFile() {
//		return (FileAttribute) getComponentAttribute( ATTRIBUTE_FILE );
//	}//getExcelFile()

	/**
	 * Sets the excel file.
	 *
	 * @param file the excel file.
	 * @throws Exception shouldn't ever be thrown.
	 */
//	public void setExcelFile( FileAttribute file ) throws Exception {
//		FileAttribute attribute = addFileAttribute( ATTRIBUTE_FILE, file );
//	}//setExcelFile()

	private String _sheetName = "Sheet1";

	/**
	 * Returns the sheet name.
	 *
	 * @return the sheet name
	 * @hibernate.property column="EXCEL_SHEET_NAME"
	 * @javabean.property displayName="Sheet Name"
	 * hidden="false"
	 */
	public String getSheetName() {
		return _sheetName;
	}//getSheetName()

	/**
	 * Sets the sheet name.
	 *
	 * @param sheetName the sheet name.
	 */
	public void setSheetName( String sheetName ) {
		_sheetName = sheetName;
	}//setSheetName()

	private String _anchor = "A1";

	/**
	 * Returns the anchor attribute of the DatabaseSource object
	 *
	 * @return the anchor value
	 * @hibernate.property column="EXCEL_ANCHOR"
	 * @javabean.property displayName="Excel Anchor"
	 * hidden="false"
	 */
	public String getExcelAnchor() {
		return _anchor;
	}//getExcelAnchor()

	/**
	 * @param anchor The anchor to set.
	 */
	public void setExcelAnchor( String anchor ) {
		_anchor = anchor;
	}//setExcelAnchor

	private String _templateName = "";
	/**
	 * Returns the sheet name.
	 *
	 * @return the sheet name
	 * @hibernate.property column="EXCEL_SHEET_TEMPLATE_NAME"
	 * @javabean.property displayName="Template Name"
	 * hidden="false"
	 */
	public String getTemplateName() {
		return _templateName;
	}

	
	public void setTemplateName( String templateName ) {
		_templateName = templateName;
	}
}
