package org.bgee.view;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;

/**
 * This abstract class defines a {@code ViewFactory}. A class that extends this one
 * will return for a particular display type (i.e. html, xml, etc.) all appropriate views
 * 
 * @author Mathieu Seppey
 * @author Frederic Bastian
 * @version Bgee 13 Aug 2014
 * @since   Bgee 1
 * 
 * @see org.bgee.view.html.HtmlFactory
 * @see org.bgee.view.xml.XmlFactory
 * @see org.bgee.view.dsv.DsvFactory
 * @see TestFactoryProvider
 * 
 */
public abstract class ViewFactory
{
    
    private final static Logger log = LogManager.getLogger(ViewFactory.class.getName());
    
    /**
     * The {@code HttpServletResponse} used to return the result to the client
     */
    protected HttpServletResponse response;
    
    /**
     * The {@code RequestParameters} handling the parameters of the current request, 
     * used for display purposes.
     */
    protected RequestParameters requestParameters;

    /**
     * Constructor with injected {@code RequestParameters}
     * 
     * @param response          The {@code HttpServletResponse} where the outputs of the view
     *                          classes will be written
     * @param requestParameters The {@code RequestParameters} handling the parameters of the 
     *                          current request, to determine the requested displayType, 
     *                          and for display purposes.
     */
    public ViewFactory(HttpServletResponse response, RequestParameters requestParameters)
    {
        log.entry(response, requestParameters);
        this.response = response;
        this.requestParameters = requestParameters;
        log.exit();
    }

    /**
     * @param prop                  An instance of {@code BgeeProperties}  that is injected to 
     *                              provide the all the properties values
     *                              
     * @return A {@code GeneralDisplay} instance that is the view to be used
     * 
     * @throws IOException  If an error occurs with the {@code PrintWriter} when writing the
     *                      response output.
     */
    public abstract GeneralDisplay getGeneralDisplay(BgeeProperties prop) throws IOException;

    /**
     * @param prop                  An instance of {@code BgeeProperties}  that is injected to 
     *                              provide the all the properties values
     *                              
     * @return A {@code DownloadDisplay} instance that is the view to be used
     * 
     * @throws IOException  If an error occurs with the {@code PrintWriter} when writing the
     *                      response output.
     */
    public abstract DownloadDisplay getDownloadDisplay(BgeeProperties prop) throws IOException;

}
