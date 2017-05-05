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
 * @author Valentine Rech de Laval
 * @version Bgee 13, Feb. 2016
 * @since   Bgee 1
 * 
 * @see org.bgee.view.html.HtmlFactory
 * @see org.bgee.view.xml.XmlFactory
 * @see org.bgee.view.csv.CsvFactory
 * @see ViewFactoryProvider
 * 
 */
public abstract class ViewFactory {

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
     * An instance of {@code BgeeProperties} to 
     * provide the all the properties values
     */
    protected BgeeProperties prop;

    /**
     * Constructor with injected {@code RequestParameters}
     * 
     * @param response          The {@code HttpServletResponse} where the outputs of the view
     *                          classes will be written
     * @param requestParameters The {@code RequestParameters} handling the parameters of the 
     *                          current request, to determine the requested displayType, 
     *                          and for display purposes.
     * @param prop              An instance of {@code BgeeProperties} to provide the all 
     *                          the properties values
     */
    public ViewFactory(HttpServletResponse response, RequestParameters requestParameters,
            BgeeProperties prop) {
        log.entry(response, requestParameters, prop);
        this.response = response;
        this.requestParameters = requestParameters;
        this.prop = prop;
        log.exit();
    }

    /**                             
     * @return A {@code GeneralDisplay} instance that is the view to be used
     * 
     * @throws IOException  If an error occurs with the {@code PrintWriter} when writing the
     *                      response output.
     */
    public abstract GeneralDisplay getGeneralDisplay() throws IOException;
    
    /**                             
     * @return  An {@code ErrorDisplay} used to display error messages when a request 
     *          has failed.
     * 
     * @throws IOException  If an error occurs with the {@code PrintWriter} when writing the
     *                      response output.
     */
    public abstract ErrorDisplay getErrorDisplay() throws IOException;

    /**
     *                              
     * @return A {@code DownloadDisplay} instance that is the view to be used
     * 
     * @throws IOException  If an error occurs with the {@code PrintWriter} when writing the
     *                      response output.
     */
    public abstract DownloadDisplay getDownloadDisplay() throws IOException;

    /**
     *                              
     * @return A {@code DocumentationDisplay} instance that is the view to be used.
     * 
     * @throws IOException  If an error occurs with the {@code PrintWriter} when writing the
     *                      response output.
     */
    public abstract DocumentationDisplay getDocumentationDisplay() throws IOException;

    /**
     *                              
     * @return A {@code AboutDisplay} instance that is the view to be used
     * 
     * @throws IOException  If an error occurs with the {@code PrintWriter} when writing the
     *                      response output.
     */
    public abstract AboutDisplay getAboutDisplay() throws IOException;
    
    /**
     *                              
     * @return A {@code TopAnatDisplay} instance of the appropriate display type.
     * 
     * @throws IOException  If an error occurs with the {@code PrintWriter} when writing the
     *                      response output.
     */
    public abstract TopAnatDisplay getTopAnatDisplay() throws IOException;
    
    /**
     * 
     * @return A {@code GeneDisplay} instance of the appropriate display type.
     * @throws IOException If an error occurs with the {@code PrintWriter} when writing the
     * 	                   response output.
     */
    public abstract GeneDisplay getGeneDisplay() throws IOException;

    /**
     *                              
     * @return A {@code SpeciesDisplay} instance of the appropriate display type.
     * 
     * @throws IOException  If an error occurs with the {@code PrintWriter} when writing the
     *                      response output.
     */
    public abstract SpeciesDisplay getSpeciesDisplay() throws IOException;

    /**
     *                              
     * @return A {@code SearchDisplay} instance of the appropriate display type.
     * 
     * @throws IOException  If an error occurs with the {@code PrintWriter} when writing the
     *                      response output.
     */
    public abstract SearchDisplay getSearchDisplay() throws IOException;

    /**
     *                              
     * @return A {@code SourceDisplay} instance of the appropriate display type.
     * 
     * @throws IOException  If an error occurs with the {@code PrintWriter} when writing the
     *                      response output.
     */
    public abstract SourceDisplay getSourceDisplay() throws IOException;

    /**                      
     * @return A {@code DAODisplay} instance of the appropriate display type.
     * 
     * @throws IOException  If an error occurs with the {@code PrintWriter} when writing the
     *                      response output.
     */
    public abstract DAODisplay getDAODisplay() throws IOException;

    /**                      
     * @return A {@code JobDisplay} instance of the appropriate display type.
     * 
     * @throws IOException  If an error occurs with the {@code PrintWriter} when writing the
     *                      response output.
     */
    public abstract JobDisplay getJobDisplay() throws IOException;
    
    /**                      
     * @return A {@code RPackageDisplay} instance of the appropriate display type.
     * 
     * @throws IOException  If an error occurs with the {@code PrintWriter} when writing the
     *                      response output.
     */
    public abstract RPackageDisplay getRPackageDisplay() throws IOException;
}
