package com.bsc.qa.stt.test_factory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import com.bsc.qa.stt.tests.Edi837FacetsDbTest;
/**
 * 
 * @author automation team
 *
 */
public class Edi837FacetsDbTestFactory {
	/**
	 * environmentVariable is to fetch input file 
	 * 
	 */
	public String environmentVariable = null;

	/**
	 * Factory method to trigger simple parameter tests from Factory
	 * 
	 * @return
	 */
	@Factory(dataProvider = "data")
	public Object[] factoryMethod(String inputFileName) {
		return new Object[] { new Edi837FacetsDbTest(inputFileName) };
	}
	/**
	 * setUp method to fetch input file from NAS path
	 * 
	 */
	@BeforeMethod(alwaysRun = true)
	@Parameters({ "EnvironmentVariable_" })
	public void setUp(@Optional("environmentVariable") String status) {
	
		System.out.println("NAS Path Environment Variable: " + this.environmentVariable);
	}
	/**
	 * 
	 * @return object
	 */
	@DataProvider(name = "data")
	public Object[] getData() {
		Object[] tableData = null;

		try (Stream<Path> walk = Files.walk(Paths.get(System.getenv(getEnvironmentVariableName())))) {
			List<String> result = walk.filter(Files::isRegularFile).map(x -> x.toString()).collect(Collectors.toList());

			tableData = (Object[]) result.toArray();

		} catch (IOException e) {
			System.out.println("Failed due to Exception : " + e);
		}
		return tableData;
	}

	private String getEnvironmentVariableName() {
		String environmentVariableName = null;
		try (InputStream input = new FileInputStream("src/test/resources/config.properties")) {

			Properties prop = new Properties();

			// load a properties file
			prop.load(input);

			environmentVariableName = prop.getProperty("NAS_PATH_ENV_NAME");
			// get the property value and print it out
			System.out.println(prop.getProperty("NAS_PATH_ENV_NAME"));

		} catch (IOException e) {
			System.out.println("Failed due to Exception : " + e);
		}		
		return environmentVariableName;
	}

}
