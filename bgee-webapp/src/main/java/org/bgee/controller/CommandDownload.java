package org.bgee.controller;

/**
 * Controller that handles requests related to download pages
 *  
 * @author 	Mathieu Seppey
 * @version Bgee 13 Aug 2014
 * @since 	Bgee 13
 *
 */

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.view.DownloadDisplay;
import org.bgee.view.ViewFactory;

public class CommandDownload extends CommandParent
{

    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = LogManager.getLogger(CommandDownload.class.getName());

    
    public CommandDownload
    (HttpServletResponse response, RequestParameters requestParameters, 
            BgeeProperties prop, ViewFactory viewFactory) 
    {
        super(response, requestParameters, prop, viewFactory);
    }

    @Override
    public void processRequest() throws IOException 
    {
        log.entry();
        DownloadDisplay display = this.viewFactory.getDownloadDisplay(prop);
        display.displayDownloadPage();
        log.exit();
    }
}
