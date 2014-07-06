package org.bgee.controller;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.bgee.controller.exception.RequestParametersNotFoundException;
import org.bgee.controller.exception.RequestParametersNotStorableException;
import org.bgee.controller.servletutils.BgeeHttpServletRequest;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link RequestParameters}.
 * It tests indirectly {@link URLParameters} and {@link URLParameter} which do not have public methods
 * As it is not possible to declare other {@code URLParameter} than those instantiated by 
 * {@code URLParameters} and it would be not relevant to mock our own classes, the tests use the real
 * parameters ACTION, CHOSEN_DATA_TYPE and ALL_ORGANS. If these parameters have their properties 
 * modified in {@code URLParameters} for any reason, the unit tests have to be checked.
 * 
 * @author Mathieu Seppey
 * @version Bgee 13
 * @since Bgee 13
 */
public class RequestParametersTest {

	/**
	 * A mock {@code BgeeHttpServletRequest}
	 */
	private BgeeHttpServletRequest mockHttpServletRequest ;

	/**
	 * Default Constructor. 
	 * @throws RequestParametersNotStorableException 
	 * @throws RequestParametersNotFoundException 
	 */
	public RequestParametersTest() throws RequestParametersNotFoundException, 
	RequestParametersNotStorableException
	{}
	
	/**
	 * @return A mock {@code BgeeHttpServletRequest}
	 */
	public BgeeHttpServletRequest getMockHttpServletRequest() {
		return mockHttpServletRequest;
	}

	/**
	 * To do before each test, configure the mock {@code BgeeHttpServletRequest}
	 */
	@Before
	public void loadMockRequest(){

		mockHttpServletRequest = mock(BgeeHttpServletRequest.class);

		when(mockHttpServletRequest.getParameter("action"))
		.thenReturn("v1").thenReturn("v2").thenReturn("v3");

		when(mockHttpServletRequest.getParameterValues("action"))
		.thenReturn(new String[]{"v1","v2","v3"});

		when(mockHttpServletRequest.getParameter("all_organs"))
		.thenReturn("true").thenReturn("false").thenReturn("stringvalue");

		when(mockHttpServletRequest.getParameterValues("all_organs"))
		.thenReturn(new String[]{"true","false","stringvalue"});

		when(mockHttpServletRequest.getParameter("chosen_data_type"))
		.thenReturn("1").thenReturn("2").thenReturn("stringvalue");

		// "stringvalue" should generate an error in the log system, this is expected
		when(mockHttpServletRequest.getParameterValues("chosen_data_type"))
		.thenReturn(new String[]{"1","2","stringvalue"});

		// the first time, do not return a key, the second time provide a key.
		// see testGetParametersQuery
		when(mockHttpServletRequest.getParameter("data"))
		.thenReturn(null).thenReturn("14b58eb5c131f18236f7b8845b51cbf2d0e61265");

	}

	/**
	 * Test of the method getParametersQuery()
	 * @throws RequestParametersNotFoundException
	 * @throws RequestParametersNotStorableException
	 */
	@Test
	public void testGetParametersQuery() throws RequestParametersNotFoundException,
	RequestParametersNotStorableException {

		RequestParameters rp = new RequestParameters(this.mockHttpServletRequest);

		// Check that the query returned corresponds to the parameters declared in
		// the mockHttpServletRequest. Note that the parameter action allows only
		// one value despite several values provided in the url and is not storable.
		assertEquals("Incorrect query returned ", 
				"action=v1%26chosen_data_type=1%26chosen_data_type=2%26all_organs=true"
						+ "%26all_organs=false%26all_organs=false%2", 
						rp.getParametersQuery());

		// Add a parameter to exceed the threshold over which a key is used, and check that
		// indeed a key is present after a new call of getParametersQuery()
		rp.addValue(URLParameters.ACTION, "longlonglonglonglongstring");

		assertEquals("Incorrect query returned ", 
				"action=longlonglonglonglongstring%26data=14b58eb5c131f18236f7b8845b51cbf2d0e61265", 
				rp.getParametersQuery());

		// Declare a second RequestParameters that this time will get a key from the 
		// mockHttpServletRequest and check that getParametersQuery() returns it.
		RequestParameters rp2 = new RequestParameters(this.mockHttpServletRequest);

		assertEquals("Incorrect query returned ", 
				"action=v1%26data=14b58eb5c131f18236f7b8845b51cbf2d0e61265", 
				rp2.getParametersQuery());

	}

	/**
	 * Test getValues() 
	 * @throws RequestParametersNotFoundException
	 * @throws RequestParametersNotStorableException
	 */
	@Test
	public void testGetValues() throws RequestParametersNotFoundException, 
	RequestParametersNotStorableException {

		RequestParameters rp = new RequestParameters(this.mockHttpServletRequest);

		ArrayList<String> action = rp.getValues(URLParameters.ACTION);

		// Action allows only one value
		assertTrue("Incorrect data type returned",action.get(0) instanceof String);		
		assertEquals("Incorrect list returned ", "[v1]",action.toString());

		ArrayList<Integer> chosen_data_type = rp.getValues(URLParameters.CHOSEN_DATA_TYPE);

		// The value stringvalue was excluded because not an Integer
		assertTrue("Incorrect data type returned",chosen_data_type.get(0) instanceof Integer);		
		assertEquals("Incorrect list returned ", "[1, 2]",chosen_data_type.toString());		

		ArrayList<Boolean> all_organs = rp.getValues(URLParameters.ALL_ORGANS);

		assertTrue("Incorrect data type returned",all_organs .get(0) instanceof Boolean);		
		assertEquals("Incorrect list returned ", "[true, false, false]",all_organs.toString());		

	}

	/**
	 * Test getValue() with and without a provided index
	 * @throws RequestParametersNotFoundException
	 * @throws RequestParametersNotStorableException
	 */
	@Test
	public void testGetValue() throws RequestParametersNotFoundException,
	RequestParametersNotStorableException {

		RequestParameters rp = new RequestParameters(this.mockHttpServletRequest);

		// Action allows only one value and thus return always a unique value
		String action1 = rp.getValue(URLParameters.ACTION);
		String action2 = rp.getValue(URLParameters.ACTION,1);
		assertEquals("Incorrect value returned ", "v1",action1.toString());
		assertEquals("Incorrect value returned ", "v1",action2.toString());

		Integer chosen_data_type1 = rp.getValue(URLParameters.CHOSEN_DATA_TYPE);
		Integer chosen_data_type2 = rp.getValue(URLParameters.CHOSEN_DATA_TYPE,1);
		assertEquals("Incorrect value returned ","1",chosen_data_type1.toString());
		assertEquals("Incorrect value returned ","2",chosen_data_type2.toString());	

		// if the index provided does not exit, it returns the last value
		Boolean all_organs1 = rp.getValue(URLParameters.ALL_ORGANS);
		Boolean all_organs2 = rp.getValue(URLParameters.ALL_ORGANS,99);
		assertEquals("Incorrect value returned ","true",all_organs1.toString());
		assertEquals("Incorrect value returned ","false",all_organs2.toString());		
	}

	/**
	 * Test setValue() with and without a provided index
	 * @throws RequestParametersNotFoundException
	 * @throws RequestParametersNotStorableException
	 */
	@Test
	public void testSetValue() throws RequestParametersNotStorableException,
	RequestParametersNotFoundException {

		// Action allows only one parameter, thus replace always the unique value
		RequestParameters rp = new RequestParameters(this.mockHttpServletRequest);
		rp.setValue(URLParameters.ACTION, "v4");
		ArrayList<String> action = rp.getValues(URLParameters.ACTION);
		assertEquals("Incorrect list returned after setting the value", "[v4]",action.toString());
		rp.setValue(URLParameters.ACTION, "v5",3);
		action = rp.getValues(URLParameters.ACTION);
		assertEquals("Incorrect list returned after setting the value", "[v5]",action.toString());

		// Test the setting of the value with and without index when they were no previous values
		// If the index is incorrect, add the value at the end
		RequestParameters rp2 = new RequestParameters();
		rp2.setValue(URLParameters.CHOSEN_DATA_TYPE, 10);
		rp2.setValue(URLParameters.CHOSEN_DATA_TYPE, 11,1);
		rp2.setValue(URLParameters.CHOSEN_DATA_TYPE, 12,1);
		rp2.setValue(URLParameters.CHOSEN_DATA_TYPE, 13,99);
		ArrayList<Integer> chosen_data_type = rp2.getValues(URLParameters.CHOSEN_DATA_TYPE);
		assertEquals("Incorrect list returned after setting the value ", "[10, 12, 13]",
				chosen_data_type.toString());

	}

	/**
	 * Test addValue() that actually simply call setValue with an inexistant index
	 * @throws RequestParametersNotFoundException
	 * @throws RequestParametersNotStorableException
	 */
	@Test
	public void testAddValues() throws RequestParametersNotFoundException, 
	RequestParametersNotStorableException {
		RequestParameters rp = new RequestParameters(this.mockHttpServletRequest);
		// As action allows only one value, the existing value is replaced
		rp.addValue(URLParameters.ACTION, "v6");
		ArrayList<String> action = rp.getValues(URLParameters.ACTION);
		assertEquals("Incorrect list returned after adding the value ", "[v6]",
				action.toString());
		rp.addValue(URLParameters.CHOSEN_DATA_TYPE, 20);
		ArrayList<Integer> chosen_data_type = rp.getValues(URLParameters.CHOSEN_DATA_TYPE);
		assertEquals("Incorrect list returned after setting the value ", "[1, 2, 20]",
				chosen_data_type.toString());
	}

	/**
	 * Test resetValues()
	 * @throws RequestParametersNotFoundException
	 * @throws RequestParametersNotStorableException
	 */
	@Test
	public void testResetValues() throws RequestParametersNotFoundException,
	RequestParametersNotStorableException {
		RequestParameters rp = new RequestParameters(this.mockHttpServletRequest);
		rp.resetValues(URLParameters.CHOSEN_DATA_TYPE);
		assertNull(rp.getValue(URLParameters.CHOSEN_DATA_TYPE));
	}

	/**
	 * Test testcloneWithAllParameters()
	 * @throws RequestParametersNotFoundException
	 * @throws RequestParametersNotStorableException
	 */
	@Test
	public void testcloneWithAllParameters() throws RequestParametersNotFoundException,
	RequestParametersNotStorableException {
		// Test that the cloned parameters has exactly the same behavior as the source
		// when there is no key
		RequestParameters rp1 = new RequestParameters(this.mockHttpServletRequest);
		RequestParameters rp2 = rp1.cloneWithAllParameters();
		assertEquals("Wrong state of the parameters of the cloned object",rp2.getParametersQuery(),
				rp1.getParametersQuery());
		
		// Test that the cloned parameters has exactly the same behavior as the source
		// when there is a key
		rp1.addValue(URLParameters.ACTION, "longlonglonglonglonglongstring");
		rp2 = rp1.cloneWithAllParameters();
		assertEquals("Wrong state of the parameters of the cloned object",rp2.getParametersQuery(),
				rp1.getParametersQuery());
				
	}

	/**
	 * Test cloneWithStorableParameters()
	 * @throws RequestParametersNotFoundException
	 * @throws RequestParametersNotStorableException
	 */
	@Test
	public void cloneWithStorableParameters() throws RequestParametersNotFoundException, 
	RequestParametersNotStorableException {
		
		// Test that the cloned parameters has kept only the storable when there is no key
		RequestParameters rp1 = new RequestParameters(this.mockHttpServletRequest);
		RequestParameters rp2 = rp1.cloneWithStorableParameters();
		assertEquals("Wrong state of the parameters of the cloned object",rp2.getParametersQuery(),
				"chosen_data_type=1%26chosen_data_type=2%26all_organs=true%26"
				+ "all_organs=false%26all_organs=false%2");
		
		// Test that the cloned parameters has kept only the storable parameters when there is a key,
		// which means that the key is the only parameter in the url
		rp1.addValue(URLParameters.ACTION, "longlonglonglonglonglongstring");
		rp1.addValue(URLParameters.CHOSEN_DATA_TYPE, 123456890);
				
		rp2 = rp1.cloneWithStorableParameters();
		assertEquals("Wrong state of the parameters of the cloned object",rp2.getParametersQuery(),
				"data=b56ee6efcc66305cfe620b2381bae78d4722238a");
	}


}
