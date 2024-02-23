package com.bsc.qa.stt.utility;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Edi834Utility class that gets fields data from 834 file to be used for 834
 * file validation.
 * 
 * @author Automation team
 *
 */
public class Edi837Utility {

	/**
	 * For getting 837 file data from 834 EDI file
	 * 
	 * @param strCompleteFilePath: 834 file path
	 * @param strInputFileName:    834 file name
	 * @return @: To capture the exception
	 */
	public SortedMap<String, SortedMap<String, String>> get837FileData(String strInputFileName) {

		String primaryKey;
		SortedMap<String, SortedMap<String, String>> flatFileValuesMap = null;
		SortedMap<String, String> singleRecordMap = null;
		int counter=1;
		// To get 837 file type
		flatFileValuesMap = new TreeMap<String, SortedMap<String, String>>();
		// For capturing all patient account numbers from 837 file
		List<String> rowsListPANumbers = parse837FileForPatientAccNumbers(strInputFileName);
		
		// Looping through all Patient Account numbers for validation
		for (String strPANumber : rowsListPANumbers) {

			singleRecordMap = new TreeMap<String, String>();

			primaryKey = strPANumber;
			// For retrieving Patient Account number specific data from 837 file
			List<String> rowsList = parse837File(strInputFileName, primaryKey);
			// To retrieve first subscriber section data in "NM1*IL*" segment
			int intSubscriber = 0;
			String line1 = "";
			int intDependent=0;

			// singleRecordMap.put("Patient Acc Number", strPANumber.trim().toLowerCase());
			System.out.println(
					" Patient Acc Number used while storing flat file data: " + strPANumber.trim().toLowerCase());

			// Displaying test data mandatory values in the logger

			// Looping through all CINN number records to retrieve required values
			for (int i = 0; i < rowsList.size(); i++) {
				// Storing field values to validate subscriber's First name, last Name and
				// Middle initial
				
				if (rowsList.get(i).startsWith("NM1*IL*") && intSubscriber == 0) {
					line1 = rowsList.get(i).toString().toLowerCase();
					line1 = line1.replace("~", "");
					singleRecordMap.put("last_name", line1.split("\\*")[3]);
					singleRecordMap.put("first_name", line1.split("\\*")[4]);
					singleRecordMap.put("subscriberid", line1.split("\\*")[9].replace("~", ""));
					// flatFileValuesMap.put("MIDINIT",
					// line1.split("\\*")[5].replace("~", ""));

					intSubscriber = intSubscriber + 1;
				}
				else if(rowsList.get(i).startsWith("NM1*QC*") && intDependent == 0) {
					
						line1 =rowsList.get(i).toString().toLowerCase(); 
						line1 = line1.replace("~", "");
						singleRecordMap.put("last_name", line1.split("\\*")[3]);
						singleRecordMap.put("first_name", line1.split("\\*")[4]);

						intDependent = intDependent + 1; }				  
				
				// Storing field values to validate trad partner
				else if (rowsList.get(i).startsWith("ISA*")) {
					line1 = rowsList.get(i).toString().toLowerCase();

					line1 = line1.replace("~", "");
					String[] trad = line1.split("\\*");
					singleRecordMap.put("TRAD_PARTNER", trad[6]);

				}
				// Storing field values to validate from and todate
				else if (rowsList.get(i).startsWith("LX*")) {
					
					// Storing field values to validate cdml chg amt
					if (rowsList.get(i+1).startsWith("SV2*")) {
						line1 = rowsList.get(i+1).toString().toLowerCase();
						line1 = line1.replace("~", "");
						singleRecordMap.put("rcrc_id"+counter, line1.split("\\*")[1]);
						singleRecordMap.put("chg_amt"+counter, line1.split("\\*")[3]);
					}
					
					line1 = rowsList.get(i+2).toString().toLowerCase();

					line1 = line1.replace("~", "");
					String[] lineDate = line1.split("\\*");
					if(lineDate[3].contains("-")) {
						singleRecordMap.put("fromdate"+counter, lineDate[3].split("-")[0]);
						singleRecordMap.put("todate"+counter, lineDate[3].split("-")[1].replace("~", ""));
					}
					else {
						singleRecordMap.put("fromdate"+counter, lineDate[3]);
						singleRecordMap.put("todate"+counter, lineDate[3]);
					}
					counter++;

				}
				// Storing field values to validate subscriber's Address

				else if (rowsList.get(i).startsWith("HL*2*") && rowsList.get(i+3).startsWith("N3*")) { 
		
						line1 = rowsList.get(i+3).toString().toLowerCase();
						line1 = line1.replace("~", ""); String[] address = line1.split("\\*");
						singleRecordMap.put("address1", address[1].substring(0).replace("~", ""));
				}


				//Storing field values to validate subscriber's city state and zip
				else if (rowsList.get(i).startsWith("HL*2*") && rowsList.get(i).startsWith("N4*")) {
					line1 = rowsList.get(i).toString().toLowerCase();

					line1 = line1.replace("~", "");
					String[] address = line1.split("\\*");
					singleRecordMap.put("city", address[1]);
					singleRecordMap.put("state", address[2]);
					singleRecordMap.put("zip", address[3].replace("~", ""));

				}
				// Storing field values to validate Claim total Charges
				else if (rowsList.get(i).startsWith("CLM*")) {
					line1 = rowsList.get(i).toString().toLowerCase();
					line1 = line1.replace("~", "");
					singleRecordMap.put("clcl_pa_acct_no", line1.split("\\*")[1]);
					singleRecordMap.put("total_charge", line1.split("\\*")[2].replace("~", ""));
				}

				// Storing field values to validate Date of Birth
				else if (rowsList.get(i).startsWith("DMG*D8*")) {
					line1 = rowsList.get(i).toString().toLowerCase();
					line1 = line1.replace("~", "");
					singleRecordMap.put("dateofbirth", line1.split("\\*")[2].replace("~", ""));
					singleRecordMap.put("sex", line1.split("\\*")[3].replace("~", ""));
				}

				// Storing field values to validate billing provider
				 if (rowsList.get(i).startsWith("NM1*85")) {
					line1 = rowsList.get(i).toString().toLowerCase();
					line1 = line1.replace("~", "");
					if(line1.split("\\*")[4].equals(""))
						
					{
						singleRecordMap.put("billing_provider", line1.split("\\*")[3].replace("~", ""));
					}
					else if(!line1.split("\\*")[4].equals("") && line1.split("\\*")[5].equals("")) {
						
						singleRecordMap.put("billing_provider", line1.split("\\*")[3]+", "+line1.split("\\*")[4]);
					}
					else {
						
						singleRecordMap.put("billing_provider", line1.split("\\*")[3]+", "+line1.split("\\*")[4]+" "+line1.split("\\*")[5]+".");
					}
					singleRecordMap.put("prpr_npi", line1.split("\\*")[9].replace("~", ""));
				}
				// Storing field values to validate billing provider address
					if (rowsList.get(i).startsWith("NM1*85")) {
						line1 = rowsList.get(i + 1).toString().toLowerCase();
						line1 = line1.replace("~", "");
						singleRecordMap.put("billing_provider_address1", line1.split("\\*")[1].replace("~", ""));
					}

					// Storing field values to validate billing provider address
					if (rowsList.get(i).startsWith("NM1*85")) {
						line1 = rowsList.get(i + 2).toString().toLowerCase();
						line1 = line1.replace("~", "");
						singleRecordMap.put("billing_provider_location", line1.split("\\*")[1].replace("~", ""));
						singleRecordMap.put("billing_provider_state", line1.split("\\*")[2]);
						singleRecordMap.put("billing_provider_zip", line1.split("\\*")[3].replace("~", ""));
					}

				// Storing field values to validate billing provider address
				if (rowsList.get(i).startsWith("NM1*85")) {
					line1 = rowsList.get(i + 3).toString().toLowerCase();
					line1 = line1.replace("~", "");
					singleRecordMap.put("billing_provider_taxid", line1.split("\\*")[2].replace("~", ""));

				}
				// Storing field values to validate idcd_cd
				else if (rowsList.get(i).startsWith("HI*")) {
					line1 = rowsList.get(i).toString().toLowerCase();
					line1 = line1.replace("~", "");
					String idcdCode="";
					if(line1.split("\\*")[1].contains(":")) {
						idcdCode=line1.split("\\*")[1].split(":")[1];
					}
					else if(line1.split("\\*")[1].contains(">")) {
						idcdCode=line1.split("\\*")[1].split(">")[1];
						}
					singleRecordMap.put("idcd_cd",idcdCode);
					

				}
				// Storing field values to validate iPcd_cd
				else if (rowsList.get(i).startsWith("SV*")) {
					line1 = rowsList.get(i).toString().toLowerCase();
					line1 = line1.replace("~", "");
					String ipcdCode="";
					if(line1.split("\\*")[1].contains(":")) {
						ipcdCode=line1.split("\\*")[1].split(":")[1]+line1.split("\\*")[1].split(":")[2];
					}
					else if(line1.split("\\*")[1].contains(">")) {
						ipcdCode=line1.split("\\*")[1].split(">")[1]+line1.split("\\*")[1].split(">")[2];
						}
					singleRecordMap.put("ipcd_cd"+counter,ipcdCode);
					

				}
			}
			// Storing all subscriber filed values map as a value and subscriber id as key
			flatFileValuesMap.put(primaryKey, singleRecordMap);

		}

		// Returning SortedMap<String, SortedMap<String, String>> with 834 file values
		return flatFileValuesMap;
	}

	/**
	 * Changing 837 EDI file format when it is having one line
	 * 
	 * @param strFlatFileCompletePath: 837 file complete path @: To capture
	 *                                 exception
	 */
	public void fileFormatChange834(String strFlatFileCompletePath) {
		FileWriter fileWriter = null;
		FileReader fileReader = null;
		BufferedReader bufferedReader = null;

		String strLines = "";
		String line = "";
		// Checking for the file existence
		try {
			fileReader = new FileReader(strFlatFileCompletePath);
			bufferedReader = new BufferedReader(fileReader);
			int intICounter = 0;
			
			// Reading each line from the file
			while ((line = bufferedReader.readLine()) != null) {
				strLines = strLines + line;
				intICounter += 1;
				// Checking for single line 834 file
				if (intICounter > 1) {
					bufferedReader.close();
					break;
				}

				byte[] strBytes = strLines.getBytes();
				String strAllLines = new String(strBytes, StandardCharsets.UTF_8);
				// Replacing "~" symbol with "~\r\n"
				String strFormatLines = strAllLines.replaceAll("~", "~\r\n");
				fileWriter = new FileWriter(strFlatFileCompletePath);
				// Writing the data once again into the file after changes
				fileWriter.write(strFormatLines);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("Failed due to Exception : " + e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Failed due to Exception : " + e);
		} finally {
			try {
				bufferedReader.close();
				fileReader.close();
				fileWriter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("Failed due to Exception : " + e);
			}
		}

	}

	/**
	 * @param testFlatFileCompletePath: File complete path
	 * @param strCINNNumber:            To capture specific subscriber section
	 * @param strFileName:              837 file type
	 * @return: List of subscriber lines @" To capture the exception
	 */
	public List<String> parse837File(String testFlatFileCompletePath, String strPatAccNumber) {

		String line = null;
		BufferedReader bufferedReader = null;
		List<String> rowsList = new ArrayList<String>();
		FileReader fileReader = null;

		boolean flag = false;
		try {
			fileReader = new FileReader(testFlatFileCompletePath);
			bufferedReader = new BufferedReader(fileReader);
			
			while ((line = bufferedReader.readLine()) != null) {
				// Checking for the patient Account Number
				if (line.contains(strPatAccNumber)) {
					flag = true;
				}
				// Checking for the starting line for each claim section
				if (line.startsWith("ST")) {
					if (flag) {
						break;
					} else {
						rowsList.clear();
					}
				}
				rowsList.add(line);

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Failed due to Exception : " + e);
		} finally {
			// To close the bufferedReader object
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
					fileReader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.out.println("Failed due to Exception : " + e);
				}
			}

		}

		return rowsList;
	}

	/**
	 * To capture patient account Numbers from 837 file
	 * 
	 * @param testFlatFileCompletePath: 837 File complete path
	 * @param strFileName:              837 file type
	 * @return: List of p Numbers @: Throwing exception
	 */
	public List<String> parse837FileForPatientAccNumbers(String strFileName) {

		String line = null;
		BufferedReader bufferedReader = null;
		List<String> rowsList = new ArrayList<String>();
		FileReader fileReader = null;

		try {
			// To capture patient Account Numbers from 837 file

			fileReader = new FileReader(strFileName);
			bufferedReader = new BufferedReader(fileReader);
			// Looping through all lined for checking CINN Number line
			
			while ((line = bufferedReader.readLine())!= null) {
				if (line.startsWith("CLM*")) {
					// Adding patient account number to the list
					rowsList.add(line.split("\\*")[1]);
				}

			}
		}
		// To capture and print the exception
		catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Failed due to Exception : " + e);
		} finally {
			try {
				bufferedReader.close();
				fileReader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("Failed due to Exception : " + e);
			}


		}
		// For returning list of CINN numbers
		return rowsList;
	}

}

