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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.exception.PageNotFoundException;
import org.bgee.view.GeneralDisplay;
import org.bgee.view.ViewFactory;

public class CommandHome extends CommandParent
{

    private final static Logger log = LogManager.getLogger(CommandHome.class.getName());


    public CommandHome(HttpServletResponse response, 
            RequestParameters requestParameters, BgeeProperties prop, ViewFactory viewFactory)
    {
        super(response, requestParameters, prop, viewFactory);
    }

    @Override
    public void processRequest() throws IOException, PageNotFoundException 
    {
        log.entry();
        GeneralDisplay display = this.viewFactory.getGeneralDisplay(prop);

        if (requestParameters.isTheHomePage()) {
            display.displayAbout();
        } else {
            throw new PageNotFoundException("Wrong parameters");
        }
        log.exit();
    }

}
