package com.nexusbpm.services.excel;

class ExcelOutOfBoundException extends Exception 
{
	private int index = 0;

	public ExcelOutOfBoundException(int index) {
		this.index = index;
	}

	public String getMessage() {
		return "Exception: Excel spreadsheet column index " + index + " exceeds the limit 256 (IV)";
	}
}
