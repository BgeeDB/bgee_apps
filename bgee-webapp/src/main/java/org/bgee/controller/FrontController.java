package org.bgee.controller;

import javax.servlet.http.HttpServletRequest;

import org.bgee.controller.exception.RequestParametersNotFoundException;
import org.bgee.controller.exception.RequestParametersNotStorableException;

/**
 * Prototype of FrontController
 * @author Mathieu Seppey
 */
public class FrontController {
	FrontController(){
		
		// Instanciate a request parameters, 
		HttpServletRequest request = null;
		RequestParameters rp = null;
		try {
			rp = new RequestParameters(request);
		} catch (RequestParametersNotFoundException
				| RequestParametersNotStorableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String action = rp.getValue(URLParameters.ACTION);

		S

	}
}
