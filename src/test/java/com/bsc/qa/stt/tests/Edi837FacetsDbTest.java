package com.bsc.qa.stt.tests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.assertj.core.api.SoftAssertions;
import org.testng.IHookCallBack;
import org.testng.IHookable;
import org.testng.ITestResult;
import org.testng.annotations.DataProvider;										   
import org.testng.annotations.Test;


import com.bsc.qa.framework.base.BaseTest;
import com.bsc.qa.framework.utility.DBUtils;
import com.bsc.qa.framework.utility.ExcelUtils;
import com.bsc.qa.stt.utility.CsvUtils;
import com.bsc.qa.stt.utility.Edi837Utility;
import com.relevantcodes.extentreports.LogStatus;
/**
 * 
 * @author Automation team
 *
 */
public class Edi837FacetsDbTest extends BaseTest implements IHookable {
	/**
	 * inputfilename: 837 input file name
	 */
	public String inputFileName = null;
	/** csvDataList is used while writing to csv report */
	public List<String[]> csvDataList = new ArrayList<String[]>();

	/**
	 * 
	 * @param inputFileName
	 */
	public Edi837FacetsDbTest(String inputFileName) {
		this.inputFileName = inputFileName;
	}
	
	// ************************************** TEST
	// METHODS************************

	// 
	/**
	 * Main test method to validate 837 file against FACETS database.
	 * @param data
	 */
	@Test(dataProvider="data")
	public void test837FileValidation(Map<String,String> data) {
		
		try {
			
		
		
			String sqlQuery = data.get("SQL_TEMPLATE");
			String primarykey=data.get("PRIMARY_KEY");
			
			SortedMap<String, SortedMap<String, String>> ediMap  = new Edi837Utility().get837FileData(inputFileName);
			
			SortedMap<String, SortedMap<String, String>> facetsMap = getDbData(sqlQuery, ediMap,primarykey);
			SortedMap<String, SortedMap<String, String>> finalFacetsMap=new TreeMap<String, SortedMap<String, String>>(); 
			
			for(String ediKey:ediMap.keySet()) {
				
			SortedMap<String,String> facetsMultipleRowMap= getDbDataWithMultipleRow(data.get("SQL_LINE"),ediKey);
			//facetsMap.put(ediMap.firstKey(), facetsMultipleRowMap);
			SortedMap<String, String> facetsTempMap=new TreeMap<String,String>();
					facetsTempMap.putAll(facetsMultipleRowMap);
				
					facetsTempMap.putAll(facetsMap.get(ediKey.toLowerCase()));
				
			
					finalFacetsMap.put(ediKey.toLowerCase(), facetsTempMap);
			}
			
			assertDbToEdi(ediMap, finalFacetsMap);
			
			
			
		} catch (Exception e) {
			
			System.out.println("Exception "+e);
		}
		CsvUtils.writeAllData(csvDataList);
	}

	/**
	 * Common assertion logic to compare dbValue vs fileValue
	 * 
	 * @param ediMap Map of content from EDI file
	 * @param dbMap  Map of content from DB
	 * 
	 */
	private void assertDbToEdi(SortedMap<String, SortedMap<String, String>> ediMap,
			SortedMap<String, SortedMap<String, String>> dbMap) {
		String status = null;
		
		softAssertions.assertThat(dbMap).as("DBMap is " + dbMap).size().isGreaterThan(0);
		
		if (dbMap.size() > 0) {
			for (String dbPrimaryKey : ediMap.keySet()) {
				SortedMap<String, String> dbLineMap = dbMap.get(dbPrimaryKey.toLowerCase());
				SortedMap<String, String> ediLineMap = ediMap.get(dbPrimaryKey);
				
				for (String ediLineKey : ediLineMap.keySet()) {

					String dbValue = dbLineMap.get(ediLineKey);
					String fileValue = ediLineMap.get(ediLineKey);
					
					 fileValue = fileValue == null ? "" :  fileValue.replaceAll("\\s+", "");
					   dbValue = dbValue == null ? "" :dbValue.replaceAll("\\s+", "");
					  
					  status = fetchStatus(fileValue, dbValue); String[] fieldDataArray = {
							  dbPrimaryKey, ediLineKey, fileValue, dbValue, status, "", inputFileName };
					  csvDataList.add(fieldDataArray); softAssertions
					  .assertThat(dbValue).as("PAc act no is " + dbPrimaryKey + ", Key is " +
							  ediLineKey + ",  DbValue is " + dbValue + ", FileValue is " + fileValue)
					  .isEqualToIgnoringCase(fileValue); // Write all data to csv report
					  CsvUtils.writeAllData(csvDataList);
					
				}
			}
		}
	}
	

	/**
	 * used for validating file and DB Value and returning status
	 * 
	 * @param fileValue
	 * @param dbValue
	 * @return status
	 */
	public String fetchStatus(String fileValue, String dbValue) {
		String status = null;
		if (fileValue.isEmpty()) // if the field value is blank in the file for the respective Key in dbMap
		{
			if (dbValue == null || dbValue.contains("No Value in DB") || dbValue.equalsIgnoreCase("")) {
				status = "Pass";
			} else {
				status = "Need Review";
			}
		} else // if the field value is not blank in the file for the respective Key in dbMap
		{
			if (dbValue == null) {
				status = "Fail";
			} else if (fileValue.equalsIgnoreCase(dbValue.trim())) {
				status = "Pass";
			} else {
				status = "Fail";
			}
		}
		return status;
	}

	/**
	 * Common logic to get data from the DB
	 * 
	 * @param ediMap         Map of content from EDI file
	 * @param sqlQueryPrefix Query from the Query data sheet
	 * @param primarKey      PrimaryKey from the Query data sheet
	 * 
	 */

	private SortedMap<String, SortedMap<String, String>> getDbData(String sqlQueryPrefix,
			SortedMap<String, SortedMap<String, String>> ediMap, String primarkey) {
		StringBuilder whereInStringBuilder = new StringBuilder();
		int setSize = ediMap.size();
		for (String key : ediMap.keySet()) {
			// "'" + key + "',"
			setSize--;
			whereInStringBuilder.append("'");
			whereInStringBuilder.append(key);
			whereInStringBuilder.append("'");

			if (setSize > 0) {
				whereInStringBuilder.append(", ");
			}
		}

		
		
		String whereInClause = whereInStringBuilder.toString();
		if (setSize > 0) {
			whereInClause = whereInClause.substring(0, whereInClause.length()-2).trim();
		}
		
		String generatedSqlQuery = sqlQueryPrefix + "  " + primarkey + "  " + "in (" + whereInClause + "))";

		
		// Get data from Facets
	//	System.out.println("########"+generatedSqlQuery);
		SortedMap<String, SortedMap<String, String>> dbMap = new DBUtils().getResultSetAsSortedMap("facets",
				generatedSqlQuery, primarkey);
//System.out.println("dbmap is "+dbMap);
		return dbMap;
	}

	// Fetching multiple rows from facets database
		private SortedMap<String,String> getDbDataWithMultipleRow(String preparedStatement, String params)
				throws IOException {
			DBUtils dataBase = new DBUtils();
			 
			SortedMap<String,String> finalDbNap=new TreeMap<String, String>();
		//	System.out.println("prepated "+preparedStatement+"pkjj "+params);
			Map<String, SortedMap<String, String>> dbValueMap = dataBase.getMultiRowsFromPreparedQuery("facets", preparedStatement, "ROWSNUM", params);
		//	System.out.println("mappp "+dbValueMap);
			for(String key:dbValueMap.keySet()) {
				
				for(String innerKey:dbValueMap.get(key).keySet()) {
					
				
				finalDbNap.put(innerKey+key, dbValueMap.get(key).get(innerKey));
				}
			}
			//System.out.println("dbmap for multiple row is "+finalDbNap);
			dataBase.tearDown();
			return finalDbNap;

		}

	
	/**
	 * //To run test method, this method will initiate the HTML report
	 * 
	 * @Override run is a hook before @Test method
	 */
	@Override
	public void run(IHookCallBack callBack, ITestResult testResult) {

		reportInit(testResult.getTestContext().getName(), testResult.getName());
		softAssertions = new SoftAssertions();
		logger.log(LogStatus.INFO, "Starting test " + testResult.getName());
		callBack.runTestMethod(testResult);
		softAssertions.assertAll();

	}
/**
 * fetching data from excel sheet
 * @return
 */
	@DataProvider(name="data")
	public Object[] getData()  {
		List<Map<String, String>> dataMapList = ExcelUtils.getAllExcelDataOnly("src/test/resources/" + this.getClass().getSimpleName() + ".xlsx");
		
		return dataMapList.toArray(); 
																  

	}

}
