package org.bgee.controller;

/**
 * This class extends {@link URLParameters} to provide
 * the additional parameters used for unit tests
 * 
 * @author Mathieu Seppey
 * @version Bgee 13
 * @since Bgee 13
 * @see RequestParametersTest
 */
public class TestURLParameters extends URLParameters {
	
	/**
	 * Test {@code Parameter} of the type  {@code String} that does 
	 * not allow multiple values and is not storable
	 */
	private static final Parameter<String> TEST_STRING = 
			new Parameter<String>("test_string",
			false, false, false, 
			128, "[a-z0-9]*",
			String.class);
	/**
	 * Test {@code Parameter}  of the type  {@code Integer} that does allow
	 * multiple values and is storable
	 */	
	private static final Parameter<Integer> TEST_INTEGER = 
			new Parameter<Integer>("test_integer",
			true, true, true, 
			128, null,Integer.class);
	/**
	 * Test {@code Parameter} of the type  {@code Boolean} that does allow 
	 * multiple values and is storable
	 */	
	private static final Parameter<Boolean> TEST_BOOLEAN = 
			new Parameter<Boolean>("test_boolean",
			true, true, true, 
			128, null,Boolean.class);
	
	/**
	 * Constructor, update the inherited Parameter list
	 */
	protected TestURLParameters(){
		list.remove(this.getParamData());
		list.add(TEST_STRING);
		list.add(TEST_INTEGER);
		list.add(TEST_BOOLEAN);
		list.add(this.getParamData());
		
	}
	
	/**
	 * @return Test {@code Parameter} of the type  {@code String} that does 
	 * not allow multiple values and is not storable
	 */
	public Parameter<String >getParamTestString(){
		return TEST_STRING;
	}	
	
	/**
	 * @return Test {@code Parameter}  of the type  {@code Integer} that does allow
	 * multiple values and is storable
	 */
	public Parameter<Integer >getParamTestInteger(){
		return TEST_INTEGER;
	}	
	
	/**
	 * @return Test {@code Parameter}  of the type  {@code Boolean} that does allow
	 * multiple values and is storable
	 */	
	public Parameter<Boolean >getParamTestBoolean(){
		return TEST_BOOLEAN;
	}

}
