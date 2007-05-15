package com.nexusbpm.services.excel;

public abstract class ExcelObject {
	private String fileName = null;
	private String templateName = null;
	private String sheetName;
	private int colLimit;
  
	public ExcelObject(String file_name, String sheet_name, String template_name) throws Exception {
		this.setFileName( file_name );
		if (null != sheet_name && sheet_name.length() > 0) {
			this.setSheetName( sheet_name );
		}
		else {
			this.setSheetName( "Sheet1" );
		}

		if (null != template_name && template_name.length() > 0) {
			this.setTemplateName( template_name );
		}
	}

	abstract public void save() throws Exception;
  
	abstract public void insertTableAtAnchor(String table, String anchorString, int rowLimit, int colLimit) throws Exception;
	public String getFileName() {
		return fileName;
	}
	public void setFileName( String fileName ) {
		this.fileName = fileName;
	}
	public String getTemplateName() {
		return templateName;
	}
	public void setTemplateName( String templateName ) {
		this.templateName = templateName;
	}
	public String getSheetName() {
		return sheetName;
	}
	public void setSheetName( String sheetName ) {
		this.sheetName = sheetName;
	}
	public int getColLimit() {
		return colLimit;
	}
	public void setColLimit( int colLimit ) {
		this.colLimit = colLimit;
	}
}
