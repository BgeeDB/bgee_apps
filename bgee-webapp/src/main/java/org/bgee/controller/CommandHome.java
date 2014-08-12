package org.bgee.controller;

/**
 * Controller that handles requests related the home (or default) page
 *  
 * @author 	Mathieu Seppey
 * @version Bgee 13 Jul 2014
 * @since 	Bgee 13
 *
 */

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.bgee.controller.exception.PageNotFoundException;

import org.bgee.view.GeneralDisplay;

public class CommandHome extends CommandParent
{

    public CommandHome(HttpSession session, HttpServletResponse response, 
            RequestParameters requestParameters, BgeeProperties prop)
    {
        super(session, response, requestParameters, prop);
    }

    @Override
    public void processRequest() throws IOException, PageNotFoundException 
    {

        GeneralDisplay display = this.viewFactory.getGeneralDisplay(prop);

        if (requestParameters.isTheHomePage()) {
            display.displayAbout();
        } else {
            throw new PageNotFoundException("Wrong parameters");
        }

    }

}
