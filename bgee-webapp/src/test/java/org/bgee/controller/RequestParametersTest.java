package org.bgee.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.bgee.controller.exception.MultipleValuesNotAllowedException;
import org.bgee.controller.exception.RequestParametersNotFoundException;
import org.bgee.controller.exception.RequestParametersNotStorableException;
import org.bgee.controller.servletutils.BgeeHttpServletRequest;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit tests for {@link RequestParameters}.
 * It obtains the test parameters from {@link TestURLParameters} that
 * extends {@link URLParameters} 
 * 
 * @author Mathieu Seppey
 * @version Bgee 13
 * @since Bgee 13
 */
public class RequestParametersTest {

	/**
	 * A mock {@code BgeeHttpServletRequest}
	 */
	private BgeeHttpServletRequest mockHttpServletRequest;
	
	/**
	 * The instance of {@code URLParameters} that provides the parameters
	 */
	private static TestURLParameters testURLParameters;
	
	/**
	 * An instance of {@link RequestParameters} to do the tests on.
	 */
	private RequestParameters requestParametersWithNoKey;
	
	/**
	 * An instance of {@link RequestParameters} to do the tests on.
	 */
	private RequestParameters requestParametersHavingAKey;
	
	/**
	 * Default Constructor. 
	 */
	public RequestParametersTest() {}
		
	/**
	 * Load the object that will provide the parameters
	 */
	@BeforeClass
	public static void loadParameters(){
		
		System.getProperties().setProperty(
				"org.bgee.webapp.requestParametersStorageDirectory",
				System.getProperty("java.io.tmpdir"));
		
		testURLParameters = new TestURLParameters();	
	
	}

	/**
	 * To do before each test, (re)set the mock 
	 * {@code BgeeHttpServletRequest} and the {@link RequestParameters} instances
	 * @throws MultipleValuesNotAllowedException 
	 * @throws RequestParametersNotStorableException 
	 * @throws RequestParametersNotFoundException 
	 */
	@Before
	public void loadMockRequest() throws RequestParametersNotFoundException,
	RequestParametersNotStorableException, MultipleValuesNotAllowedException{
	
		mockHttpServletRequest = mock(BgeeHttpServletRequest.class);

		// note : test_string cannot contain uppercase letters to be valid
		when(mockHttpServletRequest.getParameterValues("test_string"))
		.thenReturn(new String[]{"string1"}) // for new RequestParameters
		.thenReturn(new String[]{"string1"}) // for requestParametersWithNoKey
		.thenReturn(new String[]{"string1"}) // for requestParametersHavingAKey
		.thenReturn(new String[]{"string1","explode"}) // for testLoadTooMuchValue
		.thenReturn(new String[]{"STRING1"}); // for testLoadWrongFormatValue

		when(mockHttpServletRequest.getParameterValues("test_boolean"))
		.thenReturn(new String[]{"true","false"});

		when(mockHttpServletRequest.getParameterValues("test_integer"))
		.thenReturn(new String[]{"1234","2345"});

		// the first time, do not return a key, the second time provide a key.
		when(mockHttpServletRequest.getParameter("data"))
		.thenReturn(null) // for new RequestParameters
		.thenReturn(null) // for requestParametersWithNoKey
		.thenReturn("7ebf4a8a9365930d16773b7b202f166bcebd6efa");
		// for requestParametersHavingAKey
		
		// TO ensure that the key is generated and written on the disk, generate
		// a request parameter with parameters corresponding to the key
		new RequestParameters(this.mockHttpServletRequest,
				RequestParametersTest.testURLParameters).addValue(
				testURLParameters.getParamTestInteger(),987654321);
		
		this.requestParametersWithNoKey = new RequestParameters(
				this.mockHttpServletRequest,
				RequestParametersTest.testURLParameters);
		
		this.requestParametersHavingAKey= new RequestParameters(
				this.mockHttpServletRequest,
				RequestParametersTest.testURLParameters);
	}

	/**
	 * Test of the method getRequestURL()
	 * @throws MultipleValuesNotAllowedException 
	 * @throws RequestParametersNotStorableException 
	 * @throws RequestParametersNotFoundException 
	 */
	@Test
	public void testgetRequestURL() throws RequestParametersNotStorableException,
	MultipleValuesNotAllowedException, RequestParametersNotFoundException{

		// Check that the query returned corresponds to the parameters declared in
		// the mockHttpServletRequest.
		assertEquals("Incorrect query returned ","?test_string=string1%26test_integer="
				+ "1234%26test_integer=2345%26test_boolean=true%26test_boolean="
				+ "false%2",this.requestParametersWithNoKey.getRequestURL());

		// Add a parameter value to exceed the threshold over which a key is used
		// (110 for tests),
		// and check that indeed, a key is present after a new call of 
		// getRequestURL(), with still test_string written because
		// it is non storable
		this.requestParametersWithNoKey.addValue(
				testURLParameters.getParamTestInteger(),987654321);

		assertEquals("Incorrect query returned ", 
				"?test_string=string1"
				+ "%26data=7ebf4a8a9365930d16773b7b202f166bcebd6efa%2", 
				this.requestParametersWithNoKey.getRequestURL());


		// Check that the storable parameters are loaded correctly from the 
		// provided key
		// and that getRequestURL() returns it correctly with the 
		// non storable parameters as well.

		assertEquals("Incorrect query returned ", 
				"?test_string=string1"
				+ "%26data=7ebf4a8a9365930d16773b7b202f166bcebd6efa%2", 
				this.requestParametersHavingAKey.getRequestURL());

	}

	/**
	 * Test getValues() 
	 * @throws MultipleValuesNotAllowedException 
	 * @throws RequestParametersNotStorableException 
	 */
	@Test
	public void testGetValues() throws RequestParametersNotStorableException, 
	MultipleValuesNotAllowedException {

		// Check for each type that the object returned is a list of the correct
		// data type and that contains the values provided by the request
		
		List<String> testString = this.requestParametersWithNoKey.getValues(
				testURLParameters.getParamTestString());
		assertTrue("Incorrect data type returned",testString.get(0) 
				instanceof String);		
		assertEquals("Incorrect list returned ", "[string1]",
				testString.toString());

		List<Integer> testInteger = requestParametersWithNoKey.getValues(
				testURLParameters.getParamTestInteger());
		assertTrue("Incorrect data type returned",testInteger.get(0)  
				instanceof Integer);		
		assertEquals("Incorrect list returned ", "[1234, 2345]",
				testInteger.toString());		

		List<Boolean> testBoolean = requestParametersWithNoKey.getValues(
				testURLParameters.getParamTestBoolean());
		assertTrue("Incorrect data type returned",testBoolean.get(0)  
				instanceof Boolean);		
		assertEquals("Incorrect list returned ", "[true, false]",
				testBoolean.toString());	
		
		// Check that getValues return a copy of the list and not 
		// the list itself. As all objects contained within the list should be
		// immutable stuff, there is no need to care whether the content of the 
		// list is a copy or a reference
		
		List<Boolean> testBoolean2 = requestParametersWithNoKey.getValues(
				testURLParameters.getParamTestBoolean());
		
		assertFalse("The method did not return a copy of the original object",
				testBoolean == testBoolean2);
		
		// Test that an empty parameter returns null
		requestParametersWithNoKey.resetValues(
				testURLParameters.getParamTestBoolean());
		
		assertNull("The value returned is not null as expected",
				requestParametersWithNoKey.getValues(
				testURLParameters.getParamTestBoolean()));

	}

	/**
	 * Test getFirstValue()
	 * @throws MultipleValuesNotAllowedException 
	 * @throws RequestParametersNotStorableException 
	 */
	@Test
	public void testGetFirstValue() throws RequestParametersNotStorableException,
	MultipleValuesNotAllowedException{

		// Test that the value is indeed the first one
		int integerValue = this.requestParametersWithNoKey
				.getFirstValue(testURLParameters.getParamTestInteger());
		
		assertEquals("Incorrect value returned ",1234,integerValue);
		
		// Test that an empty parameter return null
		requestParametersWithNoKey.resetValues(
				testURLParameters.getParamTestBoolean());
		
		assertNull("The value returned is not null as expected",
				requestParametersWithNoKey.getFirstValue(
				testURLParameters.getParamTestBoolean()));
	
	}

	/**
	 * Test addValue()
	 * @throws MultipleValuesNotAllowedException 
	 * @throws RequestParametersNotStorableException 
	 */
	@Test
	public void testAddValue() throws RequestParametersNotStorableException,
	MultipleValuesNotAllowedException {

		// Add two values and check that they are there
		this.requestParametersWithNoKey.addValue(
				testURLParameters.getParamTestInteger(), 20);
		
		this.requestParametersWithNoKey.addValue(
				testURLParameters.getParamTestInteger(), 21);
		
		List<Integer> integerList = this.requestParametersWithNoKey.getValues(
				testURLParameters.getParamTestInteger());
		assertEquals("Incorrect list returned after setting the value ", 
				"[1234, 2345, 20, 21]",
				integerList.toString());
		
		// Check that adding a value on an empty parameter works
		requestParametersWithNoKey.resetValues(
				testURLParameters.getParamTestInteger());
		
		this.requestParametersWithNoKey.addValue(
				testURLParameters.getParamTestInteger(), 21);
		
		integerList = this.requestParametersWithNoKey.getValues(
				testURLParameters.getParamTestInteger());
		assertEquals("Incorrect list returned after setting the value ", 
				"[21]",
				integerList.toString());
		
	}
	
	/**
	 * Test addValue() with too much values
	 * @throws MultipleValuesNotAllowedException 
	 * @throws RequestParametersNotStorableException 
	 */
	@Test (expected=MultipleValuesNotAllowedException.class)
	public void testAddTooMuchValue() throws RequestParametersNotStorableException,
	MultipleValuesNotAllowedException {

		// Try to add to much param when it is not allowed,
		// i.e test_string does not allow multiple values => exception
		this.requestParametersWithNoKey.addValue(
				testURLParameters.getParamTestString(), "explode");
		
	}

	/**
	 * Test resetValues()
	 * @throws MultipleValuesNotAllowedException 
	 * @throws RequestParametersNotStorableException 
	 */
	@Test
	public void testResetValues() throws RequestParametersNotStorableException, 
	MultipleValuesNotAllowedException {
		this.requestParametersWithNoKey.resetValues(
				testURLParameters.getParamTestInteger());
		assertNull(this.requestParametersWithNoKey.getFirstValue(
				testURLParameters.getParamTestInteger()));
	}

	/**
	 * Test testcloneWithAllParameters()
	 * @throws MultipleValuesNotAllowedException 
	 * @throws RequestParametersNotStorableException 
	 * @throws RequestParametersNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	@Test
	public void testcloneWithAllParameters() throws InstantiationException, 
	IllegalAccessException, RequestParametersNotFoundException, 
	RequestParametersNotStorableException, MultipleValuesNotAllowedException {
		
		// Test that the cloned parameters has exactly the same behavior as the source
		// when there is no key
		
		RequestParameters clone = this.requestParametersWithNoKey
				.cloneWithAllParameters();
		
		assertEquals("Wrong state of the parameters of the cloned object",
				this.requestParametersWithNoKey.getRequestURL(),
				clone.getRequestURL());
		

		// Test that the cloned parameters has exactly the same behavior as the source
		// when there is a key
		
		RequestParameters clone2 = this.requestParametersHavingAKey
				.cloneWithAllParameters();
				
		assertEquals("Wrong state of the parameters of the cloned object",
				clone2.getRequestURL(),
				this.requestParametersHavingAKey.getRequestURL());
				
	}

	/**
	 * Test cloneWithStorableParameters()
	 * @throws MultipleValuesNotAllowedException 
	 * @throws RequestParametersNotStorableException 
	 * @throws RequestParametersNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	@Test
	public void cloneWithStorableParameters() throws InstantiationException, 
	IllegalAccessException, RequestParametersNotFoundException, 
	RequestParametersNotStorableException, MultipleValuesNotAllowedException {
		
		// Test that the cloned parameters return the same storable parameters
		// only as the source when there is no key
		
		RequestParameters clone = this.requestParametersWithNoKey
				.cloneWithStorableParameters();
		
		// After cloning, remove the non storable parameter of the source to do the 
		// comparison (as there were already a key, it will continue to use it
		// if even if it is now shorter than the limit)
		this.requestParametersWithNoKey.resetValues(
				testURLParameters.getParamTestString());
		
		assertEquals("Wrong state of the parameters of the cloned object",
				this.requestParametersWithNoKey.getRequestURL(),
				clone.getRequestURL());
		

		// Test that the cloned parameters return only the same storable parameters
		// as the source when there is a key
		
		RequestParameters clone2 = this.requestParametersHavingAKey
				.cloneWithStorableParameters();
		
		
		// After cloning, remove the non storable parameter of the source to do the 
		// comparison (as there were already a key, it will continue to use it
		// if even if it is now shorter than the limit)
		this.requestParametersHavingAKey.resetValues(
				testURLParameters.getParamTestString());
				
		assertEquals("Wrong state of the parameters of the cloned object",
				this.requestParametersHavingAKey.getRequestURL(),
				clone2.getRequestURL()
				);
	}
	
	/**
	 * Test that trying to load too much values for a param that does not
	 * allow multiple values throws an exception
	 * @throws MultipleValuesNotAllowedException 
	 * @throws RequestParametersNotStorableException 
	 * @throws RequestParametersNotFoundException 
	 */
	@Test (expected=MultipleValuesNotAllowedException.class)
	public void testLoadTooMuchValue() throws RequestParametersNotFoundException,
	RequestParametersNotStorableException, MultipleValuesNotAllowedException{
		
		// This is actually the forth time a request parameter is instantiated
		// for this test => see the @before loadMockRequest, thus it tries to load 
		// too much params for test_string (string1,explode) => exception
		
		new RequestParameters(
				this.mockHttpServletRequest,
				RequestParametersTest.testURLParameters);
	}
	
	/**
	 * Check that loading a value that does not match the format is not 
	 * accepted 
	 * @throws RequestParametersNotStorableException 
	 * @throws MultipleValuesNotAllowedException 
	 * @throws RequestParametersNotFoundException 
	 */
	@Test
	public void testLoadWrongFormatValue() throws MultipleValuesNotAllowedException, 
	RequestParametersNotStorableException, RequestParametersNotFoundException{
		
		// Load the forth instance of RequestParameters that is used in
		// testLoadTooMuchValue and just ignore the exception that is not
		// interesting here. That's ugly and should be handled in a different
		// way
		try{
		new RequestParameters(
				this.mockHttpServletRequest,
				RequestParametersTest.testURLParameters);
		}
		catch(Exception e){
			// Do nothing
		}
		
		// This is the forth time a request parameter is instantiated
		// for this test => see the @before loadMockRequest, thus it tries to load 
		// a wrong params for test_string (STRING1) => null
		
		RequestParameters rp = new RequestParameters(
				this.mockHttpServletRequest,
				RequestParametersTest.testURLParameters);
		
		assertNull("An unauthorized value has been added",
				rp.getFirstValue(
						testURLParameters.getParamTestString())
				);
				
	}
	
	/**
	 * Check that adding a value that does not match the format is not 
	 * accepted 
	 * @throws RequestParametersNotStorableException 
	 * @throws MultipleValuesNotAllowedException 
	 */
	@Test
	public void testAddWrongFormatValue() throws MultipleValuesNotAllowedException, 
	RequestParametersNotStorableException{
		
		// test_string does not accept upper case
				
		this.requestParametersWithNoKey.resetValues(
				testURLParameters.getParamTestString());
		
		this.requestParametersWithNoKey.addValue(
				testURLParameters.getParamTestString(),"STRING1");
	
		assertNull("An unauthorized value has been added",
				this.requestParametersWithNoKey.getFirstValue(
						testURLParameters.getParamTestString())
				);

				
	}
	
}
