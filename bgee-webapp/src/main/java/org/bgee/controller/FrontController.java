package org.bgee.controller;

import javax.servlet.http.HttpServletRequest;

/**
 * Prototype of FrontController to test the concept.
 * @author Mathieu Seppey
 */
public class FrontController {
	FrontController(){
		
		// Instanciate a request parameters, 
		HttpServletRequest request = null;
		RequestParameters rp = new RequestParameters(request);
		
		String action = rp.getValue(Parameter.ACTION);
		// Cast error here :
		Integer actionWrong = rp.getValue(Parameter.ACTION);

	}
}
