package org.bgee.view.html;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.bgee.controller.BgeeProperties;
import org.bgee.controller.CommandAnatEntity.AnatEntityResponse;
import org.bgee.controller.RequestParameters;
import org.bgee.view.AnatEntityDisplay;
import org.bgee.view.JsonHelper;

/**
 * This class is the HTML implementation of the {@code AnatEntityDisplay}.
 * 
 * @author  Julien Wollbrett
 * @version Bgee 13, Jan. 2017
 * @since   Bgee 13, Jan. 2017
 */
public class HtmlAnatEntityDisplay extends HtmlParentDisplay implements AnatEntityDisplay{
	
	public HtmlAnatEntityDisplay(HttpServletResponse response, RequestParameters requestParameters, BgeeProperties prop,
	        JsonHelper jsonHelper, HtmlFactory factory) throws IllegalArgumentException, IOException {
		super(response, requestParameters, prop, jsonHelper, factory);
	}

	@Override
	public void displayAnatEntityHomePage() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void displayAnatEntity(AnatEntityResponse anatEntityResponse) {
		// TODO Auto-generated method stub
		
	}

}
