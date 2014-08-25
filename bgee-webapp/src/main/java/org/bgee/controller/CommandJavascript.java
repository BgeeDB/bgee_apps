package org.bgee.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.view.ViewFactory;
import org.bgee.view.js.JavascriptDisplay;

/**
 * Controller that handles requests related to javascript files.
 * Note that it is a class that extends {@code CommandParent}, but that possesses a simplified
 * logic as it does not use the {@code ViewFactory}. Indeed, javascript files do not have to be 
 * generated for several display types, they are the page and the display type at the same time.
 *  
 * @author  Mathieu Seppey
 * @version Bgee 13 Aug 2014
 * @since   Bgee 13
 *
 */
public class CommandJavascript extends CommandParent
{

    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = LogManager.getLogger(CommandJavascript.class.getName());

    /**
     * Constructor
     * 
     * @param response          A {@code HttpServletResponse} that will be used to display the page to 
     *                          the client
     *                          
     * @param requestParameters The {@code RequestParameters} that handles the parameters of the 
     *                          current request.
     *                          
     * @param prop              A {@code BgeeProperties} instance that contains the properties
     *                          to use.
     *                          
     * @param viewFactory       A {@code ViewFactory} mandatory to extends {@code CommandParent}
     *                          but in this particular case, it can be {@code null}
     *                          because it is the value that will be forwarded to the parent
     *                          constructor anyway.
     */
    public CommandJavascript
    (HttpServletResponse response, RequestParameters requestParameters, 
            BgeeProperties prop, ViewFactory viewFactory) 
    {
        super(response, requestParameters, prop, null);
    }

    /**
     * Process the request. It return the appropriate javascript display depending on
     * the javascript file name
     * @see RequestParameters#getJavascriptFileName
     */
    @Override
    public void processRequest() throws IOException 
    {
        log.entry();
        JavascriptDisplay display = new JavascriptDisplay(this.response, this.requestParameters, this.prop);
        if(this.requestParameters.getJavascriptFileName().equals("bgeeproperties.js")){
            display.displayBgeeProperties();
        } else if(this.requestParameters.getJavascriptFileName().equals("urlparameters.js")){
            display.displayURLParameters();
        }
        log.exit();
    }
}
