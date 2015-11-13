package org.bgee.controller;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This class extends {@link URLParameters} to provide
 * the additional parameters used for unit tests
 * 
 * @author Mathieu Seppey
 * @author Valentine Rech de Laval
 * @version Bgee 13, Nov 15
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
			        false, false, null, false, 
			        false, 128, "[a-z0-9]*",
			        String.class);
	/**
     * Test {@code Parameter} of the type  {@code String} that does 
     * allow separated values and is not storable
     */
    private static final Parameter<String> TEST_STRING_LIST = 
            new Parameter<String>("test_list",
                    false, true, URLParameters.DEFAULT_SEPARATORS, false, 
                    false, 128, URLParameters.DEFAULT_LIST_FORMAT,
                    String.class);
	/**
	 * Test {@code Parameter}  of the type  {@code Integer} that does allow
	 * multiple values and is storable
	 */	
	private static final Parameter<Integer> TEST_INTEGER = 
			new Parameter<Integer>("test_integer",
			        true, false, null, true, true, 
			        128, null,Integer.class);
	/**
	 * Test {@code Parameter} of the type  {@code Boolean} that does allow 
	 * multiple values and is storable
	 */	
	private static final Parameter<Boolean> TEST_BOOLEAN = 
			new Parameter<Boolean>("test_boolean",
			        true, false, null, true, true, 
			        128, null,Boolean.class);
	
	/**
     * An {@code List<Parameter<T>>} to list all declared {@code Parameter<T>}
     * in the order they will appear in the URL
     */
    private final List<Parameter<?>> list;
	
	/**
	 * Constructor, update the inherited Parameter list
	 */
	protected TestURLParameters(){
	    this.list = Arrays.<Parameter<?>>asList(
                TEST_STRING,
                TEST_STRING_LIST,
	            TEST_INTEGER,
	            TEST_BOOLEAN,
	            super.getParamPage(),
	            super.getParamDisplayType(),
	            super.getParamData());
	}
	
    /**
     * @return Test {@code Parameter} of the type  {@code String} that does 
     * not allow multiple values and is not storable
     */
    public Parameter<String >getParamTestString(){
        return TEST_STRING;
    }   
    
    /**
     * @return Test {@code Parameter} of the type {@code String} that does 
     * allow separated values and is not storable
     */
    public Parameter<String> getParamTestStringList(){
        return TEST_STRING_LIST;
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

	@Override
	public List<Parameter<?>> getList() {
	    return this.list;
	}
}
