package com.bsc.qa.stt.utility;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * CsvUtils class is used for CSV writing
 */
public class CsvUtils {

	/**to store the time stamp*/
	public static String timestamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
	
	/**to store the report location folder*/
	public static String suiteName = System.getProperty("user.dir").substring(System.getProperty("user.dir").lastIndexOf("\\") + 1);

	/**to store the complete report location with time stamp*/
	public static String outputFile = "test-output\\BSC-reports\\" + suiteName + "_" + timestamp + ".csv";

	/**
	 * To write the data in to the CSV file
	 * @param data
	 */
	public static void writeAllData(List<String[]> data) 
	{
		String[] columnArray = {"SubscriberID", "Key", "Actual Filevalue", "Actual FacetDBvalue", "Status","TestCase Name", "Filename" };
		try {
			CSVWriter writer = new CSVWriter(Files.newBufferedWriter(Paths.get(outputFile)), ',');
			writer.writeNext(columnArray);
			writer.writeAll(data);
			writer.close();
		} catch (IOException e) {
			System.out.println("Exception while writing to csv"+e.getMessage());
		}
	}
	
}
