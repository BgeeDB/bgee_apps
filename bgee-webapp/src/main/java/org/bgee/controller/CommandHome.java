package org.bgee.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.bgee.controller.exception.PageNotFoundException;

import org.bgee.view.GeneralDisplay;

public class CommandHome extends CommandParent
{
	
    public CommandHome(HttpSession session, HttpServletResponse response, 
    		RequestParameters requestParameters)
    {
    	super(session, response, requestParameters);
    }
	
	@Override
	public void processRequest() throws IOException, PageNotFoundException 
	{
	    
		GeneralDisplay display = this.viewFactory.getGeneralDisplay();
		
		 if (requestParameters.isTheHomePage()) {
			display.displayAbout();
		} else {
			throw new PageNotFoundException("Wrong parameters");
		}
		
	}

}
