package org.bgee.controller;

import java.util.EnumMap;

import javax.servlet.http.HttpServletRequest;

import org.bgee.utils.BgeeStringUtils;

/**
 * Prototype of RequestParameters to test the concept.
 * @author Mathieu Seppey
 */
public class RequestParameters {

	// One enum map for each type
	private EnumMap<Parameter, String> parameterStringMap = new EnumMap<Parameter,
			String>(Parameter.class);
	private EnumMap<Parameter, Integer> parameterIntegerMap = new EnumMap<Parameter,
			Integer>(Parameter.class);
	private EnumMap<Parameter, Boolean> parameterBooleanMap = new EnumMap<Parameter,
			Boolean>(Parameter.class);

	RequestParameters(HttpServletRequest request){

		loadParams();

	}

	private void loadParams(){

		// Let's say that a method extract values from the URL

		String value1 = "asdf";
		String value2 = "1";
		String value3 = "true";

		// We can store the value of each param dynamically...
		for (Parameter p : Parameter.values()){

			switch(p.getType()){

			case("java.lang.String") :
				parameterStringMap.put(p, BgeeStringUtils.secureString(value1, p.getMaxSize(),
						p.getFormat()));
			break;

			case("java.lang.Integer") :
				parameterIntegerMap.put(p, Integer.valueOf(value2));
			break;

			case("java.lang.Boolean") :
				parameterBooleanMap.put(p, Boolean.valueOf(value3));

			break;
			default:
				// throw exception if this portion of code is reached
				break;
			}
		}
	}

	// Option 1, still does not work safely... possible to return a param that is an Integer 
	// when T is actually a String, lead to cast excep... 
	public <T> T getValue(Parameter p){

		switch(p.getType()){

		case("java.lang.String") :

			return (T) parameterStringMap.get(p);

		case("java.lang.Integer") :

			return (T) parameterIntegerMap.get(p);

		case("java.lang.Boolean") :

			return (T) parameterBooleanMap.get(p);

		// etc.	}

		}

		return null;

	}

	// Option 2, It works, no cast exception possible, but need a check to validate that the param 
	// has the type that is asked and throw an exception if it is not the case...
	public Integer getIntegerValue(Parameter p){
		if(p.getType() == "java.lang.Integer"){
			return null;
		}
		// else throw excep
		return null;
	}

	// Option 3, Safe, but one setter/getter for each param !
	public String getAction(){
		return null;
	}
}


