package com.ericsson.oss.bsim.data;

import java.util.List;

import com.ericsson.cifwk.taf.utils.csv.CsvReader;

public class CsvItemRetriever {
	
	String EMPTY_STRING_PLACEHOLDER = "";

	
	public int retrieveAttribureRowNumber(CsvReader csvReader, String columnName, String cellValue) {
		int rowNo = -1;

		List<String> name = csvReader.getColumn(columnName);
		for(String s : name){
			if (s.equals(cellValue)){
				rowNo = name.indexOf(cellValue)+1;
			}
		}
		return rowNo;
	}
	
	public String retrieveCellValueFromCSV(CsvReader csvReader, final String columnName,
			final int rowNo) {

		final int columnNo = csvReader.getColumnNoByHeader(columnName);
		if (columnNo != -1) {
			final String value = csvReader.getCell(columnNo, rowNo);

			return value;
		} else {
			
			return EMPTY_STRING_PLACEHOLDER;
		}
	}
	
}
