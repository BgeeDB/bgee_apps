package org.bgee.controller;

/**
 * Controller that handles requests related to download pages
 *  
 * @author 	Mathieu Seppey
 * @version Bgee 13 Jul 2014
 * @since 	Bgee 13
 *
 */

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.bgee.view.DownloadDisplay;

public class CommandDownload extends CommandParent
{

    public CommandDownload
    (HttpServletResponse response, RequestParameters requestParameters, 
            BgeeProperties prop) 
    {
        super(response, requestParameters, prop);
    }

    @Override
    public void processRequest() throws IOException 
    {
        DownloadDisplay display = this.viewFactory.getDownloadDisplay(prop);

        display.displayDownloadPage();
    }
}
